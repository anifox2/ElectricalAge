import os

def apply_fixes(directory):
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith(".kt"):
                filepath = os.path.join(root, file)
                with open(filepath, 'r') as f:
                    content = f.read()
                
                new_content = content
                
                # GridRender specific fixes
                if file == "GridRender.kt":
                    new_content = new_content.replace(".blockPos.x", ".x")
                    new_content = new_content.replace(".blockPos.y", ".y")
                    new_content = new_content.replace(".blockPos.z", ".z")
                
                # Global renames for consistency with modern mappings
                new_content = new_content.replace("fun writeToNBT", "fun save")
                new_content = new_content.replace("fun readFromNBT", "fun load")
                new_content = new_content.replace(".readFromNBT", ".load")
                
                # Fix super calls if they weren't caught
                new_content = new_content.replace("super.readFromNBT", "super.load")
                
                # Fix GridSwitch nullable
                if file == "GridSwitch.kt":
                     new_content = new_content.replace("Eln.instance.highVoltageCableDescriptor,", "Eln.instance.highVoltageCableDescriptor!!,")
                     new_content = new_content.replace("Eln.instance.highVoltageCableDescriptor.", "Eln.instance.highVoltageCableDescriptor!!.")

                if new_content != content:
                    print(f"Updating {filepath}")
                    with open(filepath, 'w') as f:
                        f.write(new_content)

if __name__ == "__main__":
    apply_fixes("src/main/kotlin")
