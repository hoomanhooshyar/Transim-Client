from pydantic import BaseModel
from typing import Optional, Literal

# مدل تنظیمات اولیه (که اول کار از موبایل می‌آید)
class ClientConfig(BaseModel):
    type: Literal["config"]
    hostLanguage: str
    targetLanguage: str
    voiceGender: Literal["MALE", "FEMALE"]

# پیام‌های عادی موبایل (صدا و دستورات)
class MobileMessage(BaseModel):
    type: str  # "audio_chunk", "cycle_agent", یا "config"
    data: Optional[str] = None

class ServerMessage(BaseModel):
    type: str
    data: str