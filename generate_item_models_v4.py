import os
import re
import json

# Configuration
registry_files = [
    "src/main/kotlin/mods/eln/registration/ItemRegistration.kt",
    "src/main/kotlin/mods/eln/registration/SixNodeRegistration.kt"
]
assets_dir = "src/main/resources/assets/eln"
models_dir = os.path.join(assets_dir, "models/item")
textures_item_dir = os.path.join(assets_dir, "textures/item")
textures_block_dir = os.path.join(assets_dir, "textures/block")

os.makedirs(models_dir, exist_ok=True)

# Heuristics for specific classes/types
HEURISTICS = {
    "RegulatorOnOffDescriptor": "onoffregulator",
    "RegulatorAnalogDescriptor": "analogicregulator",
    "ElectricalFuseDescriptor": "electricalfuse",
    "BrushDescriptor": None, # Use name
    "HeatingCorpElement": None, # Use name, keep dots
    "OreDescriptor": None, # Check blocks
}

# Manual overrides for tricky items
MANUAL_MAPPINGS = {
    "Economic Light Bulb": "fluorescentlamp",
    "Incandescent Light Bulb": "incandescentironlamp", # Guessing
    "LED Bulb": "ledlamp",
    "Neon Light Bulb": "neon_lamp", # Check texture
    "Mining Pipe": "miningpipe",
    "Tree Resin": "treeresin",
    "Raw Rubber": "rubber",
    "Rubbers": "rubber",
    "Copper Cable": "coppercable",
    "Pb Fuse for Low Voltage Cables": "electricalfuse", # Guessing
    "Pb Fuse for Medium Voltage Cables": "electricalfuse",
    "Pb Fuse for High Voltage Cables": "electricalfuse",
    "Pb Fuse for Very High Voltage Cables": "electricalfuse",
    "Blown Pb Fuse": "blownelectricalfuse",
    "Lead Fuse for Low Voltage Cables": "electricalfuse",
    "Lead Fuse for Medium Voltage Cables": "electricalfuse",
    "Lead Fuse for High Voltage Cables": "electricalfuse",
    "Lead Fuse for Very High Voltage Cables": "electricalfuse",
    "Blown Lead Fuse": "blownelectricalfuse",
    "Small Flashlight": "smallflashlightoff",
    "Improved Flashlight": "improvedflashlightoff",
    "X-Ray Scanner": "x-rayscanner",
    "Data Logger Print": "block/datalogger", # Block texture
    "E-Coal Helmet": "ecoal_helmet",
    "E-Coal Chestplate": "ecoal_chestplate",
    "E-Coal Leggings": "ecoal_leggings",
    "E-Coal Boots": "ecoal_boots",
    "Copper Sword": "copper_sword",
    "Copper Shovel": "copper_shovel",
    "Copper Pickaxe": "copper_pickaxe",
}

def clean_name_func(name, keep_dots=False):
    name = name.lower()
    if not keep_dots:
        name = name.replace(".", "")
    name = name.replace(" ", "").replace("_", "").replace("-", "")
    return name

def find_texture(base_name, search_dirs):
    # Try exact match first
    for d in search_dirs:
        if os.path.exists(os.path.join(d, base_name + ".png")):
            return os.path.join(d, base_name + ".png")
    
    # Try cleaning the name
    cleaned = clean_name_func(base_name)
    for d in search_dirs:
        if os.path.exists(os.path.join(d, cleaned + ".png")):
            return os.path.join(d, cleaned + ".png")

    # Try cleaning but keeping dots (for 3.2kV)
    cleaned_dots = clean_name_func(base_name, keep_dots=True)
    for d in search_dirs:
        if os.path.exists(os.path.join(d, cleaned_dots + ".png")):
            return os.path.join(d, cleaned_dots + ".png")
            
    return None

def generate_model(reg_name, texture_name, texture_type="item"):
    # Handle texture path with type prefix (e.g. block/datalogger)
    if "/" in texture_name:
        parts = texture_name.split("/")
        texture_type = parts[0]
        texture_name = parts[1]

    texture_name = texture_name.lower()
    model_content = {
        "parent": "item/generated",
        "textures": {
            "layer0": f"eln:{texture_type}/{texture_name}"
        }
    }
    
    # Sanitize reg_name for filename
    filename = reg_name.lower().replace(" ", "_").replace("/", "").replace(".", "")
    # Handle 3.2kV -> 32kv
    filename = re.sub(r'[^a-z0-9_-]', '', filename)
    
    # Special case for 3.2kV to match registry name if needed
    # But usually registry names are snake_case.
    # Let's try to match the registry name format used in the mod.
    # The mod seems to use "Small 3.2kV..." -> "small_32kv..."
    
    filepath = os.path.join(models_dir, f"{filename}.json")
    
    with open(filepath, "w") as f:
        json.dump(model_content, f, indent=4)
    print(f"Generated {filename}.json -> {texture_type}/{texture_name}")
    return filename

def process_file():
    generated_count = 0
    skipped_count = 0

    for registries_path in registry_files:
        print(f"Processing {registries_path}...")
        with open(registries_path, "r") as f:
            content = f.read()

        # Regex to find I18N names assigned to 'name' variable
        # name = I18N.TR_NAME(I18N.Type.NONE, "Copper Ore")
        name_assignments = {}
        
        # Split by "run {" blocks to handle local scopes roughly
        # Also handle "run<Unit> {"
        blocks = re.split(r'run(?:<[^>]+>)?\s*\{', content)
        
        for block in blocks:
            # Extract name
            name_match = re.search(r'name\s*=\s*I18N\.TR_NAME\([^,]+,\s*"([^"]+)"\)', block)
            if not name_match:
                # Try finding name in constructor directly
                # HeatingCorpElement(I18N.TR_NAME(..., "Name"), ...)
                name_match = re.search(r'I18N\.TR_NAME\([^,]+,\s*"([^"]+)"\)', block)
            
            if not name_match:
                continue
                
            human_name = name_match.group(1)
            
            # Determine registry name (snake_case)
            reg_name = human_name.lower().replace(" ", "_").replace("/", "").replace(".", "")
            # Handle 3.2kV -> 32kv
            reg_name = re.sub(r'[^a-z0-9_-]', '', reg_name)

            # Check for manual mapping
            if human_name in MANUAL_MAPPINGS:
                generate_model(reg_name, MANUAL_MAPPINGS[human_name])
                generated_count += 1
                continue

            # Check for GameRegistry.registerItem
            # .setTextureName("eln:copper_helmet")
            texture_match = re.search(r'\.setTextureName\("eln:([^"]+)"\)', block)
            if texture_match:
                texture_name = texture_match.group(1)
                generate_model(reg_name, texture_name)
                generated_count += 1
                continue

            # Check for explicit texture in descriptor constructors
            # RegulatorOnOffDescriptor(..., "onoffregulator", ...)
            # We need to see which class is being instantiated
            class_match = re.search(r'=\s*([a-zA-Z0-9_]+)\(', block)
            if class_match:
                class_name = class_match.group(1)
                
                if class_name in HEURISTICS and HEURISTICS[class_name]:
                    generate_model(reg_name, HEURISTICS[class_name])
                    generated_count += 1
                    continue
                    
                # If class has texture arg (string literal after name)
                # Descriptor(name, "texture", ...)
                # This is hard to regex reliably without a parser, but let's try
                # Look for "string" after the name argument
                texture_arg_match = re.search(r'I18N\.TR_NAME[^)]+\),\s*"([^"]+)"', block)
                if texture_arg_match:
                    texture_name = texture_arg_match.group(1)
                    generate_model(reg_name, texture_name)
                    generated_count += 1
                    continue

            # Fallback: Search for texture
            # Try item textures
            tex_path = find_texture(human_name, [textures_item_dir])
            if tex_path:
                tex_name = os.path.basename(tex_path).replace(".png", "")
                generate_model(reg_name, tex_name, "item")
                generated_count += 1
                continue
                
            # Try block textures (especially for Ores)
            tex_path = find_texture(human_name, [textures_block_dir])
            if tex_path:
                tex_name = os.path.basename(tex_path).replace(".png", "")
                generate_model(reg_name, tex_name, "block")
                generated_count += 1
                continue

            print(f"Skipped: {human_name} (Reg: {reg_name})")
            skipped_count += 1

    # Manually generate Yellow Brush because it's split across lines in the source
    generate_model("Yellow Brush", "yellowbrush")
    generated_count += 1

    print(f"Total generated: {generated_count}")
    print(f"Total skipped: {skipped_count}")

if __name__ == "__main__":
    process_file()
