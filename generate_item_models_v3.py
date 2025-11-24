import os
import re
import json

# Path to ItemRegistration.kt
registration_file = "src/main/kotlin/mods/eln/registration/ItemRegistration.kt"
# Path to assets
assets_dir = "src/main/resources/assets/eln"
models_dir = os.path.join(assets_dir, "models/item")
textures_item_dir = os.path.join(assets_dir, "textures/item")
textures_block_dir = os.path.join(assets_dir, "textures/block")

# Ensure models directory exists
os.makedirs(models_dir, exist_ok=True)

# Read the file
with open(registration_file, 'r') as f:
    content = f.read()

# Find all string literals
# Matches "String"
string_pattern = re.compile(r'"([^"]+)"')

generated_count = 0
skipped_count = 0

found_strings = set()

for match in string_pattern.finditer(content):
    s = match.group(1)
    # Filter out short strings or unlikely names
    if len(s) < 3:
        continue
    found_strings.add(s)

print(f"Found {len(found_strings)} unique strings.")

# Manual mappings or heuristics
heuristics = [
    ("Carbon Incandescent Light Bulb", "incandescentcarbonlamp"),
    ("Incandescent Light Bulb", "incandescenttungstenlamp"),
    ("LED Bulb", "ledlamp"),
    ("Economic Light Bulb", "fluorescentlamp"),
    ("Farming Lamp", "farminglamp"),
    ("Brush", "brush"), # e.g. "Blue Brush" -> "bluebrush" (already covered by replace space), but maybe "Brush" suffix?
]

for s in found_strings:
    # Normalize to registry name
    # val regName = name.lowercase().replace(" ", "_").replace(Regex("[^a-z0-9_]"), "")
    reg_name_base = s.lower().replace(" ", "_")
    # Allow dots for 3.2kV
    reg_name = re.sub(r'[^a-z0-9_.]', '', reg_name_base)
    
    if not reg_name:
        continue

    # Check if model already exists
    model_path = os.path.join(models_dir, f"{reg_name}.json")
    # if os.path.exists(model_path):
    #     continue

    # Try different texture name variations
    variations = [
        reg_name,                       # copper_ingot
        reg_name.replace("_", ""),      # copperingot
        reg_name.replace("_", "-"),     # copper-ingot
    ]
    
    # Add heuristic variations
    for key, value in heuristics:
        if key in s:
            variations.append(value)
            # Also try with prefixes if any (e.g. "Small ...")
            # But usually the texture is generic for the type?
            # Or maybe "small50v" + value?
            # Let's just try the value first.

    texture_location = None
    
    for var in variations:
        texture_path_item = os.path.join(textures_item_dir, f"{var}.png")
        texture_path_block = os.path.join(textures_block_dir, f"{var}.png")
        
        if os.path.exists(texture_path_item):
            texture_location = f"eln:item/{var}"
            break
        elif os.path.exists(texture_path_block):
            texture_location = f"eln:block/{var}"
            break
            
    if texture_location:
        # Create JSON model
        model_content = {
            "parent": "item/generated",
            "textures": {
                "layer0": texture_location
            }
        }
        
        with open(model_path, 'w') as f:
            json.dump(model_content, f, indent=2)
        
        generated_count += 1
        print(f"Generated model for {s} -> {reg_name}.json (texture: {texture_location})")
    else:
        # print(f"Skipping {s} -> {reg_name} (no texture found in {variations})")
        skipped_count += 1

print(f"Total generated: {generated_count}")
print(f"Total skipped (no texture): {skipped_count}")
