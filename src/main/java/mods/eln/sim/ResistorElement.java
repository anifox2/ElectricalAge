package mods.eln.sim;

public class ResistorElement {
    public double nominalRs = 1.0;
    public Control control;
    
    public void needPublish() {}
    
    public static class Control {
        public double getNormalized() { return 0.0; }
    }
}
