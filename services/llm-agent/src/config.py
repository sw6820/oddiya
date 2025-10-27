"""Configuration settings"""
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """Application settings"""
    
    # AWS Bedrock
    aws_region: str = "ap-northeast-2"
    bedrock_model_id: str = "anthropic.claude-3-sonnet-20240229-v1:0"
    use_bedrock_mock: bool = True
    
    # Redis
    redis_host: str = "localhost"
    redis_port: int = 6379
    redis_password: str = ""
    cache_ttl: int = 3600  # 1 hour
    
    # External APIs
    kakao_rest_api_key: str = ""
    
    # OpenWeatherMap (Priority 2)
    openweather_api_key: str = ""
    
    # ExchangeRate (Priority 2)
    exchangerate_api_key: str = ""
    
    class Config:
        env_file = ".env"
        case_sensitive = False


settings = Settings()

