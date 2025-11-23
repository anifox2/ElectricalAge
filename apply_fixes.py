import os

replacements = {
    'Vec3.createVectorHelper': 'Vec3',
    '.dotProduct': '.dot',
    '.crossProduct': '.cross',
    '.addVector': '.add',
    '.xCoord': '.blockPos.x',
    '.yCoord': '.blockPos.y',
    '.zCoord': '.blockPos.z',
    '.setDouble': '.putDouble',
    '.setTag': '.put',
    '.setBoolean': '.putBoolean',
    '.setByte': '.putByte',
    '.setFloat': '.putFloat',
    '.setLong': '.putLong',
    '.setString': '.putString',
    '.removeTag': '.remove',
    '.hasKey': '.contains',
    'ItemStack.loadItemStackFromNBT': 'ItemStack.of',
    '.writeToNBT': '.save',
    'Minecraft.getMinecraft()': 'Minecraft.getInstance()'
}

def apply_fixes(directory):
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith(".kt"):
                filepath = os.path.join(root, file)
                with open(filepath, 'r') as f:
                    content = f.read()
                
                new_content = content
                for old, new in replacements.items():
                    new_content = new_content.replace(old, new)
                
                if new_content != content:
                    print(f"Updating {filepath}")
                    with open(filepath, 'w') as f:
                        f.write(new_content)

if __name__ == "__main__":
    apply_fixes("src/main/kotlin")
