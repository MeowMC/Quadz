package dev.lazurite.quadz.common.util.type;

import dev.lazurite.quadz.common.entity.QuadcopterEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface QuadcopterState extends Bindable, VideoDevice {
    static QuadcopterEntity findQuadcopter(World world, Vec3d origin, int bindId, int range) {
        return world.getClosestEntity(
                QuadcopterEntity.class,
                TargetPredicate.DEFAULT.setPredicate(quad -> ((QuadcopterEntity) quad).getBindId() == bindId),
                null, origin.x, origin.y, origin.z,
                new Box(new BlockPos(origin)).expand(range));
    }

    void setGodMode(boolean godMode);
    void setPower(int power);
    void setCameraAngle(int cameraAngle);

    boolean isInGodMode();
    int getPower();
    int getCameraAngle();
}
