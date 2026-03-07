import os
import PIL
from PIL import Image

src_img_path = "/home/munaim/.gemini/antigravity/brain/9b50cfbf-5c3e-436f-b672-5907f33b359b/media__1772866171960.png"
res_path = "/home/munaim/Documents/github/rapidoc/app/src/main/res"

if not os.path.exists(src_img_path):
    print("Source image not found.")
    exit(1)

# Copy the exact image to polyclinic_logo.png
img = Image.open(src_img_path)
img.save(os.path.join(res_path, "drawable", "polyclinic_logo.png"))

# Create launcher icons
sizes = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192
}

# The image is vertical, let's keep aspect ratio and paste onto a transparent square canvas
for density, size in sizes.items():
    # Regular ic_launcher
    out_dir = os.path.join(res_path, f"mipmap-{density}")
    os.makedirs(out_dir, exist_ok=True)

    # Scale to fit inside the square with some padding
    target_len = int(size * 0.8)
    img.thumbnail((target_len, target_len), Image.Resampling.LANCZOS)
    
    # Create transparent background
    canvas = Image.new("RGBA", (size, size), (255, 255, 255, 0))
    # Center the logo
    x = (size - img.width) // 2
    y = (size - img.height) // 2
    canvas.paste(img, (x, y))
    
    canvas.save(os.path.join(out_dir, "ic_launcher.png"))
    canvas.save(os.path.join(out_dir, "ic_launcher_round.png"))

# Create adaptive foreground (108x108 dp) - which is typically higher res based on density, but let's just make one high-res one or base it on xxhdpi
# We can just put a single high res in drawable-v24 or drawable
bg = Image.new("RGBA", (1024, 1024), (255, 255, 255, 0))
img_hi = Image.open(src_img_path)
img_hi.thumbnail((700, 700), Image.Resampling.LANCZOS) # Keep some margin
x = (1024 - img_hi.width) // 2
y = (1024 - img_hi.height) // 2
bg.paste(img_hi, (x, y))
bg.save(os.path.join(res_path, "drawable", "ic_launcher_foreground.png"))

# Remove the xml version of ic_launcher_foreground so it doesn't conflict
xml_path = os.path.join(res_path, "drawable", "ic_launcher_foreground.xml")
if os.path.exists(xml_path):
    os.remove(xml_path)

print("Icons generated successfully.")
