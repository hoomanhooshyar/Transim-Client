from app.services.gemini_agent import GeminiAgent
from app.core.config import settings


class RelayService:
    def __init__(self):
        # ساخت ۳ ایجنت با کلیدهای مختلف
        self.agents = {
            "AGENT_A": GeminiAgent("A", settings.GEMINI_API_KEY_A),
            "AGENT_B": GeminiAgent("B", settings.GEMINI_API_KEY_B),
            "AGENT_C": GeminiAgent("C", settings.GEMINI_API_KEY_C),
        }
        self.agent_order = ["AGENT_A", "AGENT_B", "AGENT_C"]
        self.current_index = 0

        # مقادیر پیش‌فرض (تا وقتی که از موبایل بیاید)
        self.host_lang = "English"
        self.target_lang = "Persian"
        self.voice_name = "Puck"

    def set_config(self, host: str, target: str, gender: str):
        """ذخیره تنظیمات دریافتی از موبایل"""
        self.host_lang = host
        self.target_lang = target

        # انتخاب صدا بر اساس جنسیت
        if gender.upper() == "FEMALE":
            self.voice_name = "Aoede"  # صدای زن
        else:
            self.voice_name = "Puck"  # صدای مرد

        print(f"⚙️ Config Set: {host} <-> {target} | Voice: {self.voice_name}")

    @property
    def active_agent_id(self) -> str:
        return self.agent_order[self.current_index]

    @property
    def active_agent(self) -> GeminiAgent:
        return self.agents[self.active_agent_id]

    def cycle_agent(self) -> str:
        self.current_index = (self.current_index + 1) % len(self.agent_order)
        return self.active_agent_id


# تابع سازنده (Factory)
def create_relay_service():
    return RelayService()