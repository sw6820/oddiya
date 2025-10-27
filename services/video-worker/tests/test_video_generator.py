import pytest
import os
from src.video_generator import VideoGenerator

def test_video_generator_initialization():
    generator = VideoGenerator()
    assert generator.temp_dir == "/tmp/oddiya-videos"
    assert os.path.exists(generator.temp_dir)

def test_cleanup():
    generator = VideoGenerator()
    test_file = "/tmp/test_cleanup.txt"
    
    with open(test_file, 'w') as f:
        f.write("test")
    
    assert os.path.exists(test_file)
    generator.cleanup(test_file)
    assert not os.path.exists(test_file)

