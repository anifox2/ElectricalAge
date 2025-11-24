package mods.eln.sixnode.lampsocket;

import mods.eln.cable.CableRenderDescriptor;
import mods.eln.item.LampDescriptor;
import mods.eln.item.LampDescriptor.Type;
import mods.eln.misc.*;
import mods.eln.node.six.SixNodeDescriptor;
import mods.eln.node.six.SixNodeElementInventory;
import mods.eln.node.six.SixNodeElementRender;
import mods.eln.node.six.SixNodeEntity;
import mods.eln.sixnode.electricalcable.ElectricalCableDescriptor;
import mods.eln.sound.SoundCommand;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LampSocketRender extends SixNodeElementRender {

    LampSocketDescriptor lampSocketDescriptor = null;
    LampSocketDescriptor descriptor;

    SixNodeElementInventory inventory = new SixNodeElementInventory(2, 64, this);
    boolean grounded = true;
    public boolean poweredByLampSupply;

    float pertuVy = 0, pertuPy = 0;
    float pertuVz = 0, pertuPz = 0;
    float weatherAlphaZ = 0, weatherAlphaY = 0;

    List<Entity> entityList = new ArrayList<>();
    float entityTimout = 0;

    public String channel;
    LampDescriptor lampDescriptor = null;
    float alphaZ;
    byte light, oldLight = -1;
    int paintColor = 15;

    public boolean isConnectedToLampSupply;

    ElectricalCableDescriptor cable;

    public LampSocketRender(SixNodeEntity tileEntity, Direction side, SixNodeDescriptor descriptor) {
        super(tileEntity, side, descriptor);
        this.descriptor = (LampSocketDescriptor) descriptor;
        lampSocketDescriptor = (LampSocketDescriptor) descriptor;
    }

    @Nullable
    @Override
    public Screen newGuiDraw(@NotNull Direction side, @NotNull Player player) {
        return new LampSocketGuiDraw(player, inventory, this);
    }

    public Container getInventory() {
        return inventory;
    }

    @Override
    public void draw() {
        super.draw(); //Colored cable only
        
        PoseStack poseStack = getCurrentPoseStack();
        if (poseStack == null) return;
        MultiBufferSource buffer = getCurrentBuffer();
        if (buffer == null) return;
        int light = getCurrentLight();
        int overlay = getCurrentOverlay();

        poseStack.pushPose();
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(descriptor.initialRotateDeg));
        
        descriptor.render.draw(this, poseStack, buffer, light, overlay);
        
        poseStack.popPose();
    }

    @Override
    public void refresh(float deltaT) {
        if (descriptor.render instanceof LampSocketSuspendedObjRender) {
            float dt = deltaT;

            entityTimout -= dt;
            if (entityTimout < 0) {
                if (blockEntity != null && blockEntity.getLevel() != null) {
                    net.minecraft.world.phys.AABB aabb = new Coordinate(blockEntity).getAxisAlignedBB(2).move(0, -2, 0);
                    entityList = blockEntity.getLevel().getEntitiesOfClass(Entity.class, aabb);
                }
                entityTimout = 0.1f;
            }

            for (Entity e : entityList) {
                float eFactor = 0;
                if (e instanceof Arrow)
                    eFactor = 1f;
                if (e instanceof LivingEntity)
                    eFactor = 4f;

                if (eFactor == 0)
                    continue;
                pertuVz += e.getDeltaMovement().x * eFactor * dt;
                pertuVy += e.getDeltaMovement().z * eFactor * dt;
            }

            if (blockEntity != null && blockEntity.getLevel() != null && blockEntity.getLevel().getBrightness(LightLayer.SKY, blockEntity.getBlockPos()) > 3) {
                float weather = (float) UtilsClient.getWeather(blockEntity.getLevel()) * 0.9f + 0.1f;

                // TODO: Reduce swinging of lamps to some degree?
                weatherAlphaY += (0.4 - Math.random()) * dt * Math.PI / 0.2 * weather;
                weatherAlphaZ += (0.4 - Math.random()) * dt * Math.PI / 0.2 * weather;
                if (weatherAlphaY > 2 * Math.PI)
                    weatherAlphaY -= 2 * Math.PI;
                if (weatherAlphaZ > 2 * Math.PI)
                    weatherAlphaZ -= 2 * Math.PI;
                pertuVy += Math.random() * Math.sin(weatherAlphaY) * weather * weather * dt * 3;
                pertuVz += Math.random() * Math.cos(weatherAlphaY) * weather * weather * dt * 3;

                pertuVy += 0.4 * dt * weather * Math.signum(pertuVy) * Math.random();
                pertuVz += 0.4 * dt * weather * Math.signum(pertuVz) * Math.random();
            }

            pertuVy -= pertuPy / 10 * dt;
            pertuVy *= (1 - 0.2 * dt);
            pertuPy += pertuVy;

            pertuVz -= pertuPz / 10 * dt;
            pertuVz *= (1 - 0.2 * dt);
            pertuPz += pertuVz;
        }
    }

    void setLight(byte newLight) {
        light = newLight;
        if (lampDescriptor != null && lampDescriptor.type == Type.ECO && oldLight != -1 && oldLight < 9 && light >= 9) {
            float rand = (float) Math.random();
            if (rand > 0.1f)
                play(new SoundCommand("eln:neon_lamp").mulVolume(0.7f, 1.0f + (rand / 6.0f)).smallRange());
            else
                play(new SoundCommand("eln:NEON_LFNOISE").mulVolume(0.2f, 1f).verySmallRange());
        }
        oldLight = light;
    }

    @Override
    public void publishUnserialize(DataInputStream stream) {
        super.publishUnserialize(stream);
        try {
            Byte b;
            b = stream.readByte();
            grounded = (b & (1 << 6)) != 0;

            ItemStack lampStack = Utils.unserialiseItemStack(stream);
            if (lampStack != null) {
                lampDescriptor = (LampDescriptor) Utils.getItemObject(lampStack);
            } else {
                lampDescriptor = null;
            }
            alphaZ = stream.readFloat();
            ItemStack itemStack = Utils.unserialiseItemStack(stream);
            if (itemStack != null && !itemStack.isEmpty()) {
                cable = (ElectricalCableDescriptor) ElectricalCableDescriptor.getDescriptor(itemStack);
            }

            poweredByLampSupply = stream.readBoolean();
            channel = stream.readUTF();

            isConnectedToLampSupply = stream.readBoolean();

            setLight(stream.readByte());
            paintColor = stream.readByte();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void serverPacketUnserialize(DataInputStream stream) throws IOException {
        super.serverPacketUnserialize(stream);
        setLight(stream.readByte());
    }

    public boolean getGrounded() {
        return grounded;
    }

    public void setGrounded(boolean grounded) {
        this.grounded = grounded;
    }

    @Nullable
    @Override
    public CableRenderDescriptor getCableRender(@NotNull LRDU lrdu) {
        if (cable == null
            || (lrdu == front && !descriptor.cableFront)
            || (lrdu == front.left() && !descriptor.cableLeft)
            || (lrdu == front.right() && !descriptor.cableRight)
            || (lrdu == front.inverse() && !descriptor.cableBack))
            return null;
        return cable.render;
    }

    public void clientSetGrounded(boolean value) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(bos);

            preparePacketForServer(stream);

            stream.writeByte(LampSocketElement.setGroundedId);
            stream.writeByte(value ? 1 : 0);

            sendPacketToServer(bos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean cameraDrawOptimisation() {
        return descriptor.cameraOpt;
    }
    
    public LampSocketDescriptor getDescriptor() {
        return descriptor;
    }
}
