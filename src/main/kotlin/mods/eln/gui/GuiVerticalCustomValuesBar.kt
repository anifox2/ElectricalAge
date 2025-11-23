package mods.eln.gui

class GuiVerticalCustomValuesBar(x: Int, y:Int, width: Int, height: Int, helper: GuiHelper,
                                 val positions: Array<Float>) :
    GuiVerticalTrackBar(x, y, width, height, helper) {

    companion object {
        fun logarithmicScale(startDecade: Int, steps: Int) = Array(steps) {
            when(it.rem(3)) { 0 -> 1; 1 -> 2; else -> 5 } * Math.pow(10.0, startDecade + (it / 3).toDouble()).toFloat()
        }
    }

    init {
        setStepIdMax(positions.size - 1)
        setRange(0f, (positions.size - 1).toFloat())
    }

    override var value: Float
        get() = positions.getOrElse(super.value.toInt(), { 0f })
        set(v) {
            val pos = positions.indexOfFirst { it >= v }
            when(pos) {
                -1 -> super.value = 0f
                else -> super.value = pos.toFloat()
            }
        }
}
