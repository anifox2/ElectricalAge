import os

def replace_in_file(filepath, old, new):
    if not os.path.exists(filepath):
        print(f"File not found: {filepath}")
        return
    with open(filepath, 'r') as f:
        content = f.read()
    if old in content:
        content = content.replace(old, new)
        with open(filepath, 'w') as f:
            f.write(content)
        print(f"Updated {filepath}")
    else:
        print(f"String not found in {filepath}: {old}")

# 1. Disable mods/eln/cable/ElectricalCableDescriptor.kt
cable_desc_path = 'src/main/kotlin/mods/eln/cable/ElectricalCableDescriptor.kt'
if os.path.exists(cable_desc_path):
    os.rename(cable_desc_path, cable_desc_path + '.disabled')
    print(f"Disabled {cable_desc_path}")

# 2. Update imports in DcDc files
files_to_update_imports = [
    'src/main/kotlin/mods/eln/transparentnode/LegacyDcDc.kt',
    'src/main/kotlin/mods/eln/transparentnode/DcDc.kt',
    'src/main/kotlin/mods/eln/transparentnode/VariableDcDc.kt'
]
for fp in files_to_update_imports:
    replace_in_file(fp, 'import mods.eln.cable.ElectricalCableDescriptor', 'import mods.eln.sixnode.electricalcable.ElectricalCableDescriptor')

# 3. Fix CurrentRelay.kt
current_relay_path = 'src/main/java/mods/eln/sixnode/currentrelay/CurrentRelay.kt'
replace_in_file(current_relay_path, 'tileEntity', 'blockEntity')

# 4. Fix ElectricalFuseDescriptor.kt
fuse_desc_path = 'src/main/kotlin/mods/eln/item/ElectricalFuseDescriptor.kt'
with open(fuse_desc_path, 'r') as f:
    content = f.read()
# Remove IItemRenderer interface and methods
# This is hard with simple replace.
# I'll just comment out the interface and methods.
content = content.replace(', IItemRenderer', '') # Remove interface
content = content.replace('override fun shouldUseRenderHelper', '// override fun shouldUseRenderHelper')
content = content.replace('override fun handleRenderType', '// override fun handleRenderType')
content = content.replace('override fun renderItem', '// override fun renderItem')
# Also imports
content = content.replace('import net.minecraftforge.client.IItemRenderer', '// import net.minecraftforge.client.IItemRenderer')
with open(fuse_desc_path, 'w') as f:
    f.write(content)
print(f"Updated {fuse_desc_path}")

# 5. Fix GridRender.kt
grid_render_path = 'src/main/kotlin/mods/eln/gridnode/GridRender.kt'
replace_in_file(grid_render_path, 'rotateAroundY', 'yRot')
replace_in_file(grid_render_path, 'rotateAroundX', 'xRot')

# 6. Fix GhostBlock.kt
ghost_block_path = 'src/main/kotlin/mods/eln/ghost/GhostBlock.kt'
replace_in_file(ghost_block_path, 'side.3DDataValue', 'side.ordinal')

# 7. Fix BrushDescriptor.kt
brush_desc_path = 'src/main/kotlin/mods/eln/item/BrushDescriptor.kt'
replace_in_file(brush_desc_path, 'thePlayer', 'player')
replace_in_file(brush_desc_path, 'tagCompound', 'tag')
# item -> this?
# The error was `Unresolved reference: item`.
# Context: `item.setTextureName(...)`?
# In 1.20.1 items don't set texture name.
# I'll just comment it out if I can find it.
with open(brush_desc_path, 'r') as f:
    content = f.read()
content = content.replace('item.setTextureName', '// item.setTextureName')
content = content.replace('item.unlocalizedName', '// item.unlocalizedName') # If exists
with open(brush_desc_path, 'w') as f:
    f.write(content)
print(f"Updated {brush_desc_path}")

