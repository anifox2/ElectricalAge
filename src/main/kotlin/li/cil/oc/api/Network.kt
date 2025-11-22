package li.cil.oc.api

import li.cil.oc.api.network.Environment
import li.cil.oc.api.network.Node
import li.cil.oc.api.network.Visibility
import net.minecraft.world.level.block.entity.BlockEntity

object Network {
    fun joinOrCreateNetwork(tileEntity: BlockEntity) {}

    fun newNode(environment: Environment, visibility: Visibility): Builder {
        return Builder()
    }

    class Builder {
        fun withConnector(): Builder = this
        fun create(): Node? = null
    }
}
