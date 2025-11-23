package mods.eln.sim;

public class ResistorDescriptor implements IResistorDescriptor {
    public double tempCoef = 0.0;
    public boolean isRheostat = false;

    @Override
    public double getTempCoef() {
        return tempCoef;
    }

    @Override
    public boolean isRheostat() {
        return isRheostat;
    }
}
