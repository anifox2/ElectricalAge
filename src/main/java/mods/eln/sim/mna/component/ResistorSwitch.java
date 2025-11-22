package mods.eln.sim.mna.component;

import mods.eln.misc.INBTTReady;
import mods.eln.sim.mna.misc.MnaConst;
import mods.eln.sim.mna.state.State;
import net.minecraft.nbt.CompoundTag;

public class ResistorSwitch extends Resistor implements INBTTReady {

    boolean ultraImpedance = false;
    String name;

    boolean state = false;

    protected double baseResistance = 1;

    public ResistorSwitch(String name, State aPin, State bPin) {
        super(aPin, bPin);
        this.name = name;
    }

    public void setState(boolean state) {
        this.state = state;
        setResistance(baseResistance);
    }

    @Override
    public Resistor setResistance(double resistance) {
        baseResistance = resistance;
        return super.setResistance(state ? resistance : (ultraImpedance ? MnaConst.ultraImpedance : MnaConst.highImpedance));
    }

    public boolean getState() {
        return state;
    }

    @Override
    public void readFromNBT(CompoundTag nbt, String str) {
        str += name;
        setResistance(nbt.getDouble(str + "R"));
        if (Double.isNaN(baseResistance) || baseResistance == 0) {
            if (ultraImpedance) ultraImpedance();
            else highImpedance();
        }
        setState(nbt.getBoolean(str + "State"));
    }

    @Override
    public void writeToNBT(CompoundTag nbt, String str) {
        str += name;
        nbt.putDouble(str + "R", baseResistance);
        nbt.putBoolean(str + "State", getState());
    }

    public void mustUseUltraImpedance() {
        ultraImpedance = true;
    }
}
