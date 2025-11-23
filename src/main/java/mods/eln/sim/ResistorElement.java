package mods.eln.sim;

public class ResistorElement implements IResistorElement {
    public double nominalRs = 1.0;
    public Control control;
    
    public void needPublish() {}

    @Override
    public double getNominalRs() {
        return nominalRs;
    }

    @Override
    public IResistorControl getControl() {
        return control;
    }
    
    public static class Control implements IResistorElement.IResistorControl {
        public double getNormalized() { return 0.0; }
    }
}
