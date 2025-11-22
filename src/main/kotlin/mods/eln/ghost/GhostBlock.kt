@file:Suppress("NAME_SHADOWING")
package mods.eln.ghost

import mods.eln.Eln
import mods.eln.misc.Coordinate
import mods.eln.misc.Direction.Companion.fromIntMinecraftSide
import mods.eln.node.transparent.TransparentNodeEntity
import net.minecraft.world.level.block.Block
import net.minecraft.block.material.Material
import net.minecraft.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.util.AABB
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import net.minecraft.world.IBlockAccess
import net.minecraft.world.level.Level
import java.util.*

class GhostBlock : Block(Material.iron) {

    override fun getItemDropped(p_149650_1_: Int, p_149650_2_: Random, p_149650_3_: Int): Item? {
        return null
    }

    override fun addCollisionBoxesToList(world: World, x: Int, y: Int, z: Int, par5AABB: AABB, list: MutableList<*>, entity: Entity?) {
        @Suppress("UNCHECKED_CAST") var list = list as MutableList<AABB?>
        val meta = world.getBlockMetadata(x, y, z)
        when (meta) {
            tFloor -> {
                val axisalignedbb1 = AABB.getBoundingBox(x.toDouble(), y.toDouble(), z.toDouble(), x.toDouble() + 1, y.toDouble() + 0.0625, z.toDouble() + 1)
                if (axisalignedbb1 != null && par5AABB.intersectsWith(axisalignedbb1)) {
                    list.add(axisalignedbb1)
                }
            }
            tLadder -> {
            }
            else -> {
                val element = getElement(world, x, y, z)
                val coord = if (element == null) null else element.observatorCoordonate
                val te = coord?.tileEntity
                if (te != null && te is TransparentNodeEntity) {
                    te.addCollisionBoxesToList(par5AABB, list, element!!.elementCoordinate)
                } else {
                    super.addCollisionBoxesToList(world, x, y, z, par5AABB, list, entity)
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    override fun getSelectedBoundingBoxFromPool(w: World, x: Int, y: Int, z: Int): AABB {
        val meta = w.getBlockMetadata(x, y, z)
        return when (meta) {
            tFloor -> AABB.getBoundingBox(x.toDouble(), y.toDouble(), z.toDouble(), x.toDouble() + 1, y.toDouble() + 0.0625, z.toDouble() + 1)
            tLadder -> AABB.getBoundingBox(x.toDouble(), y.toDouble(), z.toDouble(), x.toDouble() + 0, y.toDouble() + 0.0, z.toDouble() + 0)
            else -> super.getSelectedBoundingBoxFromPool(w, x, y, z)
        }
    }

    override fun collisionRayTrace(world: World, x: Int, y: Int, z: Int, startVec: Vec3, endVec: Vec3): MovingObjectPosition? {
        val meta = world.getBlockMetadata(x, y, z)
        when (meta) {
            tFloor -> maxY = 0.0625
            tLadder -> {
                maxX = 0.01
                maxY = 0.01
                maxZ = 0.01
            }
            else -> {
            }
        }
        val m = super.collisionRayTrace(world, x, y, z, startVec, endVec)
        when (meta) {
            tFloor -> maxY = 1.0
            tLadder -> {
                maxX = 1.0
                maxY = 1.0
                maxZ = 1.0
            }
            else -> {
            }
        }
        return m
    }

    override fun isLadder(world: IBlockAccess, x: Int, y: Int, z: Int, entity: LivingEntity): Boolean {
        return world.getBlockMetadata(x, y, z) == tLadder
    }

    /*
	 * @Override
	 *
	 * @SideOnly(Side.CLIENT) public int idPicked(World par1World, int par2, int par3, int par4) {
	 *
	 * return Block.dirt.blockID; }
	 */
    override fun isOpaqueCube(): Boolean {
        return false
    }

    override fun renderAsNormalBlock(): Boolean {
        return false
    }

    override fun getRenderType(): Int {
        return -1
    }

    override fun getPickBlock(target: MovingObjectPosition, world: World, x: Int, y: Int, z: Int, player: Player): ItemStack? {
        return null
    }

    override fun isBlockSolid(blockAccess: IBlockAccess, x: Int, y: Int, z: Int, side: Int): Boolean {
        return false
    }

    override fun breakBlock(world: World, x: Int, y: Int, z: Int, par5: Block, par6: Int) {
        if (world.isRemote == false) {
            val element = getElement(world, x, y, z)
            if (element != null) element.breakBlock()
        }
        super.breakBlock(world, x, y, z, par5, par6)
    }

    override fun onBlockActivated(world: World, x: Int, y: Int, z: Int, player: Player, side: Int, vx: Float, vy: Float, vz: Float): Boolean {
        if (world.isRemote == false) {
            val element = getElement(world, x, y, z)
            if (element != null) return element.onBlockActivated(player, fromIntMinecraftSide(side), vx, vy, vz)
        }
        return true
    }

    fun getElement(world: World?, x: Int, y: Int, z: Int): GhostElement? {
        return Eln.ghostManager.getGhost(Coordinate(x, y, z, world!!))
    }

    override fun getBlockHardness(par1World: World, par2: Int, par3: Int, par4: Int): Float {
        return 0.5f
    }

    val nodeUuid: String
        get() = "g"

    companion object {
        const val tCube = 0
        const val tFloor = 1
        const val tLadder = 2
    }
}
