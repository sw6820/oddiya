"""
Prompt Loader - Load and manage prompts from external files
프롬프트를 코드에서 분리하여 관리
"""
import os
import yaml
import logging
from typing import Dict, Any
from pathlib import Path

logger = logging.getLogger(__name__)

class PromptLoader:
    """Load prompts from YAML files"""
    
    def __init__(self, prompts_dir: str = "prompts"):
        self.prompts_dir = Path(__file__).parent.parent.parent / prompts_dir
        self.prompts = {}
        self.load_prompts()
    
    def load_prompts(self):
        """Load all prompt files"""
        try:
            prompt_file = self.prompts_dir / "system_prompts.yaml"
            
            if not prompt_file.exists():
                logger.warning(f"Prompt file not found: {prompt_file}")
                self.prompts = self._get_default_prompts()
                return
            
            with open(prompt_file, 'r', encoding='utf-8') as f:
                self.prompts = yaml.safe_load(f)
            
            logger.info(f"Loaded prompts from {prompt_file}")
            
        except Exception as e:
            logger.error(f"Failed to load prompts: {e}")
            self.prompts = self._get_default_prompts()
    
    def get_system_message(self) -> str:
        """Get system message for LLM"""
        return self.prompts.get('system_message', '')
    
    def get_planning_prompt(self, **kwargs) -> str:
        """
        Get planning prompt with variables filled in
        
        Args:
            **kwargs: Variables to fill in template
                - num_days, location, title, start_date, end_date
                - budget_level, temperature, weather_condition, etc.
        
        Returns:
            Filled prompt string
        """
        template = self.prompts.get('planning_prompt_template', '')
        
        try:
            return template.format(**kwargs)
        except KeyError as e:
            logger.error(f"Missing variable in prompt template: {e}")
            return template
    
    def get_refinement_prompt(self, feedback: str) -> str:
        """
        Get refinement prompt with feedback
        
        Args:
            feedback: Feedback string to incorporate
        
        Returns:
            Refinement prompt
        """
        template = self.prompts.get('refinement_prompt_template', '')
        return template.format(feedback=feedback)
    
    def get_validation_criteria(self) -> str:
        """Get validation criteria for plan checking"""
        return self.prompts.get('validation_criteria', '')
    
    def reload_prompts(self):
        """Reload prompts from file (useful for hot-reloading)"""
        self.load_prompts()
        logger.info("Prompts reloaded")
    
    def _get_default_prompts(self) -> Dict[str, str]:
        """Fallback prompts if file not found"""
        return {
            'system_message': '당신은 한국 여행 전문가입니다.',
            'planning_prompt_template': '{num_days}일간의 {location} 여행 계획을 생성해주세요.',
            'refinement_prompt_template': '현재 계획의 문제점: {feedback}\n개선된 계획을 만들어주세요.',
            'validation_criteria': '계획이 실용적이고 현실적인지 확인'
        }

# Singleton instance
_prompt_loader = None

def get_prompt_loader() -> PromptLoader:
    """Get singleton prompt loader instance"""
    global _prompt_loader
    if _prompt_loader is None:
        _prompt_loader = PromptLoader()
    return _prompt_loader

