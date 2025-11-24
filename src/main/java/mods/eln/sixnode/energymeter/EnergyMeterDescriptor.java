package mods.eln.sixnode.energymeter;

import mods.eln.misc.Obj3D;
import mods.eln.misc.Obj3D.Obj3DPart;
import mods.eln.misc.Utils;
import mods.eln.misc.VoltageLevelColor;
import mods.eln.node.six.SixNodeDescriptor;
import mods.eln.wiki.Data;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;

public class EnergyMeterDescriptor extends SixNodeDescriptor {

    private Obj3D obj;
    public Obj3DPart base, powerDisk, energySignWheel, timeUnitWheel, energyUnitWheel;
    public Obj3DPart[] energyNumberWheel, timeNumberWheel;
    public float[] pinDistance;

    float alphaOff, alphaOn, speed;

    public EnergyMeterDescriptor(String name, Obj3D obj, int energyWheelCount, int timeWheelCount) {
        super(name, EnergyMeterElement.class, EnergyMeterRender.class);
        this.obj = obj;
        if (obj != null) {
            base = obj.getPart("Base");
            powerDisk = obj.getPart("PowerDisk");
            energySignWheel = obj.getPart("EnergySignWheel");
            timeUnitWheel = obj.getPart("TimeUnitWheel");
            energyUnitWheel = obj.getPart("EnergyUnitWheel");

            energyNumberWheel = new Obj3DPart[energyWheelCount];
            for (int idx = 0; idx < energyNumberWheel.length; idx++) {
                energyNumberWheel[idx] = obj.getPart("EnergyNumberWheel" + idx);
            }
            timeNumberWheel = new Obj3DPart[timeWheelCount];
            for (int idx = 0; idx < timeNumberWheel.length; idx++) {
                timeNumberWheel[idx] = obj.getPart("TimeNumberWheel" + idx);
            }
        }

        pinDistance = Utils.getSixNodePinDistance(base);

        voltageLevelColor = VoltageLevelColor.Neutral;
    }

    @Override
    public void setParent(Item item, int damage) {
        super.setParent(item, damage);
        Data.addWiring(newItemStack());
    }

    public void draw(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, double energy, double time, int energyUnit, int timeUnit, boolean drawAll) {
        // UtilsClient.disableCulling();
        base.draw(poseStack, buffer, light, overlay);
        powerDisk.draw(poseStack, buffer, light, overlay, -(float) energy, 0f, 1f, 0f);

        {// render energy
            float ox = 0.20859f, oy = 0.15625f, oz = 0;
            double delta = 0;
            boolean propagate = true;
            double oldRot = 0;

            if (drawAll) {
                {
                    double rot;
                    if (energy > 0.5)
                        rot = 0;
                    else if (energy < -0.5)
                        rot = 1;
                    else
                        rot = 0.5 - energy;
                    rot *= 36;
                    poseStack.pushPose();
                    poseStack.translate(ox, oy, oz);
                    poseStack.mulPose(Axis.ZP.rotationDegrees((float) rot));
                    poseStack.translate(-ox, -oy, -oz);
                    energySignWheel.draw(poseStack, buffer, light, overlay);
                    poseStack.popPose();
                }
                if (energyUnitWheel != null) {
                    double rot = energyUnit * 36;
                    poseStack.pushPose();
                    poseStack.translate(ox, oy, oz);
                    poseStack.mulPose(Axis.ZP.rotationDegrees((float) rot));
                    poseStack.translate(-ox, -oy, -oz);
                    energyUnitWheel.draw(poseStack, buffer, light, overlay);
                    poseStack.popPose();
                }

                energy = Math.max(0.0, Math.abs(energy));
                if (energy < 5) propagate = false;

                for (int idx = 0; idx < energyNumberWheel.length; idx++) {
                    double rot = ((energy) % 10) + 0.0;
                    rot += 0.00;

                    if (idx == 1) {
                        delta = ((rot) % 1) * 2 - 1;
                        delta *= delta * delta;
                        delta *= 0.5;
                    }
                    if (idx != 0) {
                        if (propagate) {
                            if (rot < 9.5 && rot > 0.5) {
                                propagate = false;
                            }
                            rot = (int) (rot) + delta;
                        } else
                            rot = (int) (rot);
                    }

                    oldRot = rot;
                    rot *= 36;
                    poseStack.pushPose();
                    poseStack.translate(ox, oy, oz);
                    poseStack.mulPose(Axis.ZP.rotationDegrees((float) rot));
                    poseStack.translate(-ox, -oy, -oz);
                    energyNumberWheel[idx].draw(poseStack, buffer, light, overlay);
                    poseStack.popPose();

                    energy /= 10.0;
                }
            }
        }

        if (energyNumberWheel.length != 0) { // Render Times
            float ox = 0.20859f, oy = 0.03125f, oz = 0;
            double delta = 0;
            boolean propagate = true;
            double oldRot = 0;

            if (drawAll) {
                if (timeUnitWheel != null) {
                    double rot = timeUnit * 36;
                    poseStack.pushPose();
                    poseStack.translate(ox, oy, oz);
                    poseStack.mulPose(Axis.ZP.rotationDegrees((float) rot));
                    poseStack.translate(-ox, -oy, -oz);
                    timeUnitWheel.draw(poseStack, buffer, light, overlay);
                    poseStack.popPose();
                }

                time = Math.max(0.0, Math.abs(time));
                if (time < 5) propagate = false;

                for (int idx = 0; idx < timeNumberWheel.length; idx++) {
                    double rot = ((time) % 10) + 0.0;
                    rot += 0.00;

                    if (idx == 1) {
                        delta = ((rot) % 1) * 2 - 1;
                        delta *= delta * delta;
                        delta *= delta * delta;
                        delta *= delta * delta;
                        delta *= 0.5;
                    }
                    if (idx != 0) {
                        if (propagate) {
                            if (rot < 9.5 && rot > 0.5) {
                                propagate = false;
                            }
                            rot = (int) (rot) + delta;
                        } else
                            rot = (int) (rot);
                    }

                    oldRot = rot;
                    rot *= 36;
                    poseStack.pushPose();
                    poseStack.translate(ox, oy, oz);
                    poseStack.mulPose(Axis.ZP.rotationDegrees((float) rot));
                    poseStack.translate(-ox, -oy, -oz);
                    timeNumberWheel[idx].draw(poseStack, buffer, light, overlay);
                    poseStack.popPose();

                    time /= 10.0;
                }
            }
        }
        // UtilsClient.enableCulling();
    }
}
