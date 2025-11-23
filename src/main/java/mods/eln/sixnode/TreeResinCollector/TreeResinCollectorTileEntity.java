package mods.eln.sixnode.treeresincollector;

import mods.eln.Eln;
import mods.eln.misc.Direction;
import mods.eln.misc.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.tags.BlockTags;

public class TreeResinCollectorTileEntity extends BlockEntity {

    float occupancy = 0f;
    final float occupancyMax = 2f;
    final float occupancyProductPerSecondPerTreeBlock = 1f / 5f / 5f;
    final float timeRandom = 0.2f;

    float timeTarget = (float) (Math.random() * timeRandom);
    float timeCounter = 0;

    public TreeResinCollectorTileEntity(BlockPos pos, BlockState state) {
        super(Eln.treeResinCollectorBlockEntity.get(), pos, state);
    }

    boolean onBlockActivated() {
        if (level.isClientSide) return true;
        while (occupancy >= 1f) {
            Utils.dropItem(Eln.treeResin.newItemStack(1), worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), level);
            occupancy -= 1f;
        }
        return true;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TreeResinCollectorTileEntity self) {
        self.tick();
    }

    public void tick() {
        if (level.isClientSide) return;
        timeCounter += 1f / 20f;
        if (timeCounter > timeTarget) {
            int[] posWood = new int[3];
            int[] posCollector = new int[3];
            
            Direction woodDirection = Direction.N; // Placeholder
            
            posWood[0] = worldPosition.getX();
            posWood[1] = worldPosition.getY();
            posWood[2] = worldPosition.getZ();
            posCollector[0] = worldPosition.getX();
            posCollector[1] = worldPosition.getY();
            posCollector[2] = worldPosition.getZ();
            woodDirection.applyTo(posWood, 1);

            int yStart, yEnd;

            while (level.getBlockState(new BlockPos(posWood[0], posWood[1] - 1, posWood[2])).is(BlockTags.LOGS)) {
                posWood[1]--;
            }
            yStart = posWood[1];

            posWood[1] = worldPosition.getY();
            timeCounter -= timeTarget;
            while (level.getBlockState(new BlockPos(posWood[0], posWood[1] + 1, posWood[2])).is(BlockTags.LOGS)) {
                posWood[1]++;
            }
            yEnd = posWood[1];

            int collectiorCount = 1; 
            
            occupancy += occupancyProductPerSecondPerTreeBlock * (yEnd - yStart + 1) * timeTarget / collectiorCount;

            if (occupancy > occupancyMax) occupancy = occupancyMax;

            timeTarget = (float) (Math.random() * timeRandom);
        }
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putFloat("occupancy", occupancy);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        occupancy = nbt.getFloat("occupancy");
    }
}
