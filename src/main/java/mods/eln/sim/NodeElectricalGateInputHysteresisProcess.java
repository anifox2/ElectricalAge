package mods.eln.sim;

import mods.eln.Eln;
import mods.eln.misc.INBTTReady;
import mods.eln.sim.nbt.NbtElectricalGateInput;
import net.minecraft.nbt.CompoundTag;

public abstract class NodeElectricalGateInputHysteresisProcess implements IProcess, INBTTReady {

    NbtElectricalGateInput gate;
    String name;

    boolean state = false;

    public NodeElectricalGateInputHysteresisProcess(String name, NbtElectricalGateInput gate) {
        this.gate = gate;
        this.name = name;
    }

        protected abstract void setOutput(boolean value);

    @Override
    public void readFromNBT(CompoundTag nbt, String str) {
        if (gate != null) gate.readFromNBT(nbt, str + name);
        if (nbt.contains(str + name + "state")) {
            state = nbt.getBoolean(str + name + "state");
        }
    }

    @Override
    public void writeToNBT(CompoundTag nbt, String str) {
        if (gate != null) gate.writeToNBT(nbt, str + name);
        nbt.putBoolean(str + name + "state", state);
    }


    @Override
    public void process(double time) {
        if (state) {
            if (gate.getVoltage() < Eln.SVU * 0.3) {
                state = false;
                setOutput(false);
            } else setOutput(true);
        } else {
            if (gate.getVoltage() > Eln.SVU * 0.7) {
                state = true;
                setOutput(true);
            } else setOutput(false);
        }
    }


}
