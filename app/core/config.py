import os
from dotenv import load_dotenv
from pydantic_settings import BaseSettings

# بارگذاری متغیرهای محیطی از فایل .env
load_dotenv()


class Settings(BaseSettings):
    GEMINI_API_KEY_A: str
    GEMINI_API_KEY_B: str
    GEMINI_API_KEY_C: str

    PORT: int = 8080
    HOST: str = "0.0.0.0"

    class Config:
        env_file = ".env"
        env_file_encoding = 'utf-8'
        extra = "ignore"


settings = Settings()