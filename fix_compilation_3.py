import os

def apply_fixes(directory):
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith(".kt"):
                filepath = os.path.join(root, file)
                with open(filepath, 'r') as f:
                    content = f.read()
                
                new_content = content
                
                # GridDescriptor specific fixes
                if file == "GridDescriptor.kt":
                    new_content = new_content.replace("renderOffset.blockPos.x", "renderOffset.x")
                    new_content = new_content.replace("renderOffset.blockPos.y", "renderOffset.y")
                    new_content = new_content.replace("renderOffset.blockPos.z", "renderOffset.z")
                
                # FluidTank fixes
                if file in ["ElementFluidHandler.kt", "ElementSidedFluidHandler.kt", "TankData.kt"]:
                    new_content = new_content.replace("tank.load(", "tank.readFromNBT(")
                    new_content = new_content.replace("tank.save(", "tank.writeToNBT(")
                
                # TankData imports and registry
                if file == "TankData.kt":
                    new_content = new_content.replace("import net.minecraftforge.fluids.FluidTank", "import net.minecraftforge.fluids.capability.templates.FluidTank")
                    new_content = new_content.replace("import net.minecraftforge.fluids.FluidRegistry", "import net.minecraftforge.registries.ForgeRegistries\nimport net.minecraft.resources.ResourceLocation")
                    new_content = new_content.replace("import net.minecraftforge.fluids.Fluid", "import net.minecraft.world.level.material.Fluid")
                    new_content = new_content.replace("FluidRegistry.getFluid(it)", "ForgeRegistries.FLUIDS.getValue(ResourceLocation(it))")
                    new_content = new_content.replace('nbt.getCompoundTag("${str}tank")', 'nbt.getCompound("${str}tank")')

                if new_content != content:
                    print(f"Updating {filepath}")
                    with open(filepath, 'w') as f:
                        f.write(new_content)

if __name__ == "__main__":
    apply_fixes("src/main/kotlin")
