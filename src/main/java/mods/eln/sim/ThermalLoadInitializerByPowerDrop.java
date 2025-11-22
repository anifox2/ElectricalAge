package mods.eln.sim;

import mods.eln.Eln;

public class ThermalLoadInitializerByPowerDrop extends ThermalLoadInitializer {

    // public double maximumTemperature, minimumTemperature; // Inherited
    // double heatingTao; // Inherited
    double TConductivityDrop;

    // public double Rs; // Inherited
    // public double Rp; // Inherited
    /**
     * Thermal capacitance.
     */
    // public double C; // Inherited

    /**
     * @param maximumTemperature Intended maximum temperature in celsius.
     * @param minimumTemperature Intended minimum temperature in celsius.
     * @param heatingTao
     * @param TConductivityDrop
     */
    public ThermalLoadInitializerByPowerDrop(double maximumTemperature, double minimumTemperature, double heatingTao, double TConductivityDrop) {
        super(maximumTemperature, minimumTemperature, heatingTao, 0.0);
        this.TConductivityDrop = TConductivityDrop;
    }

    @Override
    public void setMaximalPower(double power) {
        C = power * heatingTao / maximumTemperature;
        Rp = maximumTemperature / power;
        Rs = TConductivityDrop / power / 2;

        Eln.simulator.checkThermalLoad(Rs, Rp, C);
    }

    public void applyToThermalLoad(ThermalLoad load) {
        load.set(Rs, Rp, C);
    }

    @Override
    public ThermalLoadInitializerByPowerDrop copy() {
        ThermalLoadInitializerByPowerDrop thermalLoad = new ThermalLoadInitializerByPowerDrop(maximumTemperature, minimumTemperature, heatingTao, TConductivityDrop);
        thermalLoad.Rp = Rp;
        thermalLoad.Rs = Rs;
        thermalLoad.C = C;
        return thermalLoad;
    }
}
