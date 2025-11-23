package mods.eln.sim;

public interface IResistorElement {
    double getNominalRs();
    IResistorControl getControl();
    void needPublish();
    
    interface IResistorControl {
        double getNormalized();
    }
}
