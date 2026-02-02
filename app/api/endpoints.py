import json
import base64
import asyncio
from fastapi import APIRouter, WebSocket, WebSocketDisconnect
from app.services.relay_service import create_relay_service
from app.schemas.messages import MobileMessage, ServerMessage
from google.genai.types import LiveClientRealtimeInput, LiveClientContent, Blob

router = APIRouter()


@router.websocket("/relay")
async def websocket_endpoint(mobile_ws: WebSocket):
    # برای هر کاربر جدید، یک سرویس رله جدید ساخته می‌شود
    relay_service = create_relay_service()

    await mobile_ws.accept()
    print("📱 Mobile Connected. Waiting for config...")

    try:
        # --- مرحله ۱: دریافت کانفیگ (Handshake) ---
        config_data = await mobile_ws.receive_text()
        try:
            config_json = json.loads(config_data)
            if config_json.get("type") == "config":
                relay_service.set_config(
                    host=config_json["hostLanguage"],
                    target=config_json["targetLanguage"],
                    gender=config_json["voiceGender"]
                )
                print("✅ Configuration received successfully.")
            else:
                print("⚠️ Warning: First message was NOT config. Using defaults.")
        except Exception as e:
            print(f"❌ Config Error: {e}")

        # --- مرحله ۲: اتصال به گوگل ---
        from contextlib import AsyncExitStack
        async with AsyncExitStack() as stack:
            print("🔗 Connecting agents with User Config...")

            # اتصال هر ۳ ایجنت با تنظیمات کاربر
            session_a = await stack.enter_async_context(
                relay_service.agents["AGENT_A"].connect(
                    relay_service.host_lang, relay_service.target_lang, relay_service.voice_name
                )
            )
            relay_service.agents["AGENT_A"].session = session_a

            session_b = await stack.enter_async_context(
                relay_service.agents["AGENT_B"].connect(
                    relay_service.host_lang, relay_service.target_lang, relay_service.voice_name
                )
            )
            relay_service.agents["AGENT_B"].session = session_b

            session_c = await stack.enter_async_context(
                relay_service.agents["AGENT_C"].connect(
                    relay_service.host_lang, relay_service.target_lang, relay_service.voice_name
                )
            )
            relay_service.agents["AGENT_C"].session = session_c

            print("✅ All Agents Ready!")
            print(f"   AGENT_A session: {relay_service.agents['AGENT_A'].session is not None}")
            print(f"   AGENT_B session: {relay_service.agents['AGENT_B'].session is not None}")
            print(f"   AGENT_C session: {relay_service.agents['AGENT_C'].session is not None}")
            print(f"   Active agent: {relay_service.active_agent_id}")

            # اعلام آمادگی به موبایل
            await mobile_ws.send_text(ServerMessage(type="system", data="READY").model_dump_json())

            # --- حلقه‌های اصلی ---
            async def listen_to_agent(agent_id: str, session):
                print(f"🎧 Starting listener for {agent_id}...")
                try:
                    async for response in session.receive():
                        print(f"📨 [{agent_id}] Received response from Gemini")
                        if agent_id == relay_service.active_agent_id:
                            server_content = response.server_content
                            if server_content is None:
                                print(f"⚠️ [{agent_id}] server_content is None")
                                continue

                            model_turn = server_content.model_turn
                            if model_turn:
                                print(f"🗣️ [{agent_id}] Got model_turn with {len(model_turn.parts)} parts")
                                for part in model_turn.parts:
                                    if part.inline_data:
                                        b64 = base64.b64encode(part.inline_data.data).decode('utf-8')
                                        msg = ServerMessage(type="audio", data=b64)
                                        await mobile_ws.send_text(msg.model_dump_json())
                                        print(f"🔊 [{agent_id}] Sent audio to mobile ({len(b64)} bytes)")
                                    if part.text:
                                        msg = ServerMessage(type="text", data=part.text)
                                        await mobile_ws.send_text(msg.model_dump_json())
                                        print(f"📝 [{agent_id}]: {part.text[:50]}...")

                            if server_content.turn_complete:
                                print(f"🏁 Turn Complete ({agent_id})")
                        else:
                            print(f"⏸️ [{agent_id}] Ignoring response (not active agent)")

                except Exception as e:
                    import traceback
                    print(f"❌ Error {agent_id}: {e}")
                    traceback.print_exc()

            async def listen_to_mobile():
                chunk_count = 0
                print("👂 Server is listening for mobile data...")
                while True:
                    try:
                        data = await mobile_ws.receive_text()
                        print(f"📦 RAW DATA RECEIVED: {len(data)} chars")
                        message = MobileMessage.model_validate_json(data)

                        if message.type == "audio_chunk" and message.data:
                            chunk_count += 1
                            if chunk_count % 30 == 0:
                                print(f"🎤 Received Audio Chunk #{chunk_count}")
                            current_agent = relay_service.active_agent
                            active_id = relay_service.active_agent_id
                            
                            if current_agent is None:
                                print(f"⚠️ current_agent is None!")
                                continue
                                
                            if current_agent.session is None:
                                print(f"⚠️ Session for {active_id} is None!")
                                continue
                            
                            try:
                                pcm_bytes = base64.b64decode(message.data)
                                r_input = LiveClientRealtimeInput(
                                    media_chunks=[Blob(mime_type="audio/pcm;rate=16000", data=pcm_bytes)]
                                )
                                await current_agent.session.send(input=r_input)
                                if chunk_count % 30 == 0:
                                    print(f"📤 Sent audio chunk #{chunk_count} to {active_id}")
                            except Exception as send_error:
                                print(f"❌ Error sending audio to {active_id}: {send_error}")

                        elif message.type == "cycle_agent":
                            # فقط به agent بعدی سوئیچ کن - بدون ارسال turn_complete
                            new_agent = relay_service.cycle_agent()
                            print(f"🔄 Switched to {new_agent}")
                            sys_msg = ServerMessage(type="system", data=f"Switched to {new_agent}")
                            await mobile_ws.send_text(sys_msg.model_dump_json())

                    except WebSocketDisconnect:
                        raise
                    except Exception as e:
                        import traceback
                        print(f"❌ Mobile Loop Error: {e}")
                        traceback.print_exc()

            await asyncio.gather(
                listen_to_agent("AGENT_A", session_a),
                listen_to_agent("AGENT_B", session_b),
                listen_to_agent("AGENT_C", session_c),
                listen_to_mobile()
            )

    except WebSocketDisconnect:
        print("📱 Mobile Disconnected")
    except Exception as e:
        print(f"❌ Server Error: {e}")
    finally:
        try:
            await mobile_ws.close()
        except:
            pass