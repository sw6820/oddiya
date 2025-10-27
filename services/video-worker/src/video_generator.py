import subprocess
import os
import logging

logger = logging.getLogger(__name__)

class VideoGenerator:
    def __init__(self):
        self.temp_dir = "/tmp/oddiya-videos"
        os.makedirs(self.temp_dir, exist_ok=True)
    
    def generate_video(self, photo_paths, output_path, template="default"):
        """
        Generate video from photos using FFmpeg
        Basic template: slideshow with 2 seconds per photo
        """
        try:
            # Create concat file for FFmpeg
            concat_file = os.path.join(self.temp_dir, "concat.txt")
            with open(concat_file, 'w') as f:
                for photo in photo_paths:
                    f.write(f"file '{photo}'\n")
                    f.write("duration 2\n")  # 2 seconds per photo
                # Last photo needs to be repeated for proper duration
                if photo_paths:
                    f.write(f"file '{photo_paths[-1]}'\n")
            
            # FFmpeg command for slideshow
            cmd = [
                'ffmpeg',
                '-f', 'concat',
                '-safe', '0',
                '-i', concat_file,
                '-vf', 'scale=1080:1920:force_original_aspect_ratio=decrease,pad=1080:1920:(ow-iw)/2:(oh-ih)/2',
                '-c:v', 'libx264',
                '-pix_fmt', 'yuv420p',
                '-y',
                output_path
            ]
            
            logger.info(f"Running FFmpeg: {' '.join(cmd)}")
            result = subprocess.run(cmd, capture_output=True, text=True)
            
            if result.returncode == 0:
                logger.info(f"Video generated successfully: {output_path}")
                return True
            else:
                logger.error(f"FFmpeg failed: {result.stderr}")
                return False
                
        except Exception as e:
            logger.error(f"Video generation failed: {e}")
            return False
        finally:
            # Cleanup concat file
            if os.path.exists(concat_file):
                os.remove(concat_file)
    
    def cleanup(self, *paths):
        for path in paths:
            if os.path.exists(path):
                os.remove(path)
                logger.info(f"Cleaned up {path}")

