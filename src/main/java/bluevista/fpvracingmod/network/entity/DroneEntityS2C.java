package bluevista.fpvracingmod.network.entity;

import bluevista.fpvracingmod.client.ClientTick;
import bluevista.fpvracingmod.network.PacketHelper;
import bluevista.fpvracingmod.client.physics.PhysicsEntity;
import bluevista.fpvracingmod.server.ServerInitializer;
import bluevista.fpvracingmod.server.entities.DroneEntity;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.stream.Stream;

public class DroneEntityS2C {
    public static final Identifier PACKET_ID = new Identifier(ServerInitializer.MODID, "drone_entity_s2c");

    public static void accept(PacketContext context, PacketByteBuf buf) {
        PlayerEntity player = context.getPlayer();

        int droneID = buf.readInt();
        int band = buf.readInt();
        int channel = buf.readInt();
        int cameraAngle = buf.readInt();
        int godMode = buf.readInt();
        boolean infiniteTracking = buf.readBoolean();
        Vector3f position = PacketHelper.deserializeVector3f(buf);
        Vector3f linearVel = PacketHelper.deserializeVector3f(buf);
        Vector3f angularVel = PacketHelper.deserializeVector3f(buf);
        Quat4f orientation = PacketHelper.deserializeQuaternion(buf);

        context.getTaskQueue().execute(() -> {
            DroneEntity drone = null;

            if(player != null)
                drone = (DroneEntity) player.world.getEntityById(droneID);

            if(drone != null) {
                drone.setBand(band);
                drone.setChannel(channel);
                drone.setCameraAngle(cameraAngle);
                drone.setGodMode(godMode);
                drone.setInfiniteTracking(infiniteTracking);

                if(drone.physics == null)
                    drone.physics = new PhysicsEntity(drone);

                if(ClientTick.boundDrone == null || droneID != ClientTick.boundDrone.getEntityId()) {
                    drone.setOrientation(orientation);
                    drone.physics.setPosition(position);
                    drone.physics.getRigidBody().setLinearVelocity(linearVel);
                    drone.physics.getRigidBody().setAngularVelocity(angularVel);
                }
            }
        });
    }

    public static void send(DroneEntity drone) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        buf.writeInt(drone.getEntityId());
        buf.writeInt(drone.getBand());
        buf.writeInt(drone.getChannel());
        buf.writeInt(drone.getCameraAngle());
        buf.writeInt(drone.getGodMode());
        buf.writeBoolean(drone.hasInfiniteTracking());
        PacketHelper.serializeVector3f(buf, drone.physics.getPosition());
        PacketHelper.serializeVector3f(buf, drone.physics.getRigidBody().getLinearVelocity(new Vector3f()));
        PacketHelper.serializeVector3f(buf, drone.physics.getRigidBody().getAngularVelocity(new Vector3f()));
        PacketHelper.serializeQuaternion(buf, drone.physics.getOrientation());

        Stream<PlayerEntity> watchingPlayers = PlayerStream.watching(drone.getEntityWorld(), new BlockPos(drone.getPos()));
        watchingPlayers.forEach(player -> ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, PACKET_ID, buf));
    }

    public static void register() {
        ClientSidePacketRegistry.INSTANCE.register(PACKET_ID, DroneEntityS2C::accept);
    }
}