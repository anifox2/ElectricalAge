import os

target_import = "import mods.eln.node.transparent.TransparentNodeBlockEntity"
wildcard_import = "import mods.eln.node.transparent.*"
package_prefix = "mods.eln.node.transparent"

def fix_file(filepath):
    with open(filepath, 'r') as f:
        lines = f.readlines()

    content = "".join(lines)
    if "TransparentNodeBlockEntity" not in content:
        return

    if target_import in content:
        return
    
    # Check package declaration
    package_line = next((line for line in lines if line.strip().startswith("package ")), None)
    if package_line:
        pkg = package_line.strip().replace("package ", "").replace(";", "")
        if pkg == package_prefix:
            return

    print(f"Fixing {filepath}")
    
    new_lines = []
    inserted = False
    
    # Try to insert before wildcard import if it exists
    for line in lines:
        if not inserted and wildcard_import in line:
            new_lines.append(target_import + "\n")
            new_lines.append(line)
            inserted = True
        else:
            new_lines.append(line)
            
    if not inserted:
        # If no wildcard import, find the last import and append
        # But we need to be careful not to insert inside a class or comment
        # Just finding the last line starting with "import " is usually safe enough for Kotlin files
        last_import_idx = -1
        for i, line in enumerate(new_lines):
            if line.strip().startswith("import "):
                last_import_idx = i
        
        if last_import_idx != -1:
            new_lines.insert(last_import_idx + 1, target_import + "\n")
        else:
            # No imports? Insert after package
            package_idx = -1
            for i, line in enumerate(new_lines):
                if line.strip().startswith("package "):
                    package_idx = i
                    break
            if package_idx != -1:
                new_lines.insert(package_idx + 1, "\n" + target_import + "\n")
            else:
                new_lines.insert(0, target_import + "\n")

    with open(filepath, 'w') as f:
        f.writelines(new_lines)

# Walk through src/main/kotlin
for root, dirs, files in os.walk("src/main/kotlin"):
    for file in files:
        if file.endswith(".kt"):
            fix_file(os.path.join(root, file))
