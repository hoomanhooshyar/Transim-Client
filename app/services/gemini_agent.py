from google import genai
from google.genai.types import (
    LiveConnectConfig, Content, Part, SpeechConfig, VoiceConfig, PrebuiltVoiceConfig, Modality, HttpOptions
)

class GeminiAgent:
    def __init__(self, agent_id: str, api_key: str):
        self.agent_id = agent_id
        # کلاینت بدون تنظیمات اولیه ساخته می‌شود
        self.client = genai.Client(api_key=api_key,http_options=HttpOptions(api_version='v1alpha'))
        self.model_id = "gemini-2.5-flash-native-audio-preview-12-2025"
        self.session = None

    def get_config(self, host_lang: str, target_lang: str, voice_name: str) -> LiveConnectConfig:
        # --- پرامپت اصلی و داینامیک شما ---
        system_prompt = f"""
        You are a professional bi-directional interpreter. 
        Languages: {host_lang} and {target_lang}.
        Logic:
        - If user speaks {host_lang}, translate to {target_lang}.
        - If user speaks {target_lang}, translate to {host_lang}.
        - DO NOT echo the input language. 
        - Output ONLY the translation of the input; Even when its a very short word or sentence or a question.
        - EMOTION: Mimic the speaker's emotional state, urgency, speed, and emphasis perfectly.
        - Your identity: Agent {self.agent_id}. You must be seamless and invisible.
        """

        return LiveConnectConfig(
            response_modalities=[Modality.AUDIO],
            speech_config=SpeechConfig(
                voice_config=VoiceConfig(
                    prebuilt_voice_config=PrebuiltVoiceConfig(voice_name=voice_name)
                )
            ),
            system_instruction=Content(
                parts=[Part(text=system_prompt)]
            )
        )

    def connect(self, host_lang: str, target_lang: str, voice_name: str):
        """
        اتصال به گوگل با تنظیمات مخصوص این کاربر
        """
        config = self.get_config(host_lang, target_lang, voice_name)
        return self.client.aio.live.connect(model=self.model_id, config=config)