package mods.eln.gui

import mods.eln.node.NodeBase

interface INodeContainer {
    val node: NodeBase?
    val refreshRateDivider: Int
}
