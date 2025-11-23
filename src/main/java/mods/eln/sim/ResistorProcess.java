package mods.eln.sim;

import mods.eln.sim.mna.component.Resistor;
import mods.eln.sim.mna.misc.MnaConst;


/**
 * Created by svein on 07/08/15.
 */
public class ResistorProcess implements IProcess {

    IResistorElement element;
    IResistorDescriptor descriptor;
    Resistor resistor;
    ThermalLoad thermal;

    private double lastResistance = -1;

    public ResistorProcess(IResistorElement element, Resistor resistor, ThermalLoad thermal, IResistorDescriptor descriptor) {
        this.element = element;
        this.descriptor = descriptor;
        this.resistor = resistor;
        this.thermal = thermal;
    }

    @Override
    public void process(double time) {
        double newResistance = Math.max(
            MnaConst.noImpedance,
            element.getNominalRs() * (1 + descriptor.getTempCoef() * thermal.temperatureCelsius));
        
        if (element.getControl() != null) {
            if (descriptor.isRheostat()) {
                newResistance = newResistance * element.getControl().getNormalized();
            } else {
                newResistance *= (element.getControl().getNormalized() + 0.01) / 1.01;
            }
        }
        if (newResistance > lastResistance * 1.01 || newResistance < lastResistance * 0.99) {
            resistor.setResistance(newResistance);
            lastResistance = newResistance;
            element.needPublish();
        }

//        /*
//        * https://en.wikipedia.org/wiki/Thermistor
//        *
//        * R = exp[(x - y/2)^(1/3) - (x + y/2)^(1/3)]
//        * y = 1/c*(a - 1/T)
//        * x = sqrt((b/3c)^3 + (y/2)^2)
//        */
//
//        double T = thermal.Tc;
//        double y = 1.0 / descriptor.shC * (descriptor.shA - 1.0/T);
//        double x = Math.sqrt(Math.pow(descriptor.shB / 3.0 / descriptor.shC, 3) + Math.pow(y / 2.0, 2));
//        double R = Math.exp(Math.pow(x - y/2, 1.0/3.0) - Math.pow(x + y/2, 1.0/3.0));
//
//        r.setR(R);
    }
}
