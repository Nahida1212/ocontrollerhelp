package com.ocontrollerhelp.mixin.client;

import com.ocontrollerhelp.OcontrollerhelpModClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow @Final private Minecraft minecraft;

    // 注入 render 方法
    // 这个方法每帧运行，处理所有视角移动逻辑
    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(net.minecraft.client.DeltaTracker deltaTracker, boolean tick, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (OcontrollerhelpModClient.isLockOnMode && OcontrollerhelpModClient.currentTarget != null && client.player != null) {
            applySmoothLock(client.player, OcontrollerhelpModClient.currentTarget, deltaTracker.getGameTimeDeltaTicks());
        }
    }

    private void applySmoothLock(Entity player, Entity target, float partialTicks) {
        // 1. 获取平滑的目标位置（胸部高度）
        double targetY = target.getY() + target.getBbHeight() * 0.75;
        Vec3 targetPos = new Vec3(target.getX(), targetY, target.getZ());

        // 使用 partialTicks 获取插值后的玩家位置，防止跳变
        Vec3 playerEyePos = player.getEyePosition(partialTicks);

        double dx = targetPos.x - playerEyePos.x;
        double dy = targetPos.y - playerEyePos.y;
        double dz = targetPos.z - playerEyePos.z;
        double distXZ = Math.sqrt(dx * dx + dz * dz);

        // 2. 计算理想角度
        float targetYaw = Mth.wrapDegrees((float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0F));
        float targetPitch = Mth.wrapDegrees((float) (-Math.toDegrees(Math.atan2(dy, distXZ))));

        // 3. 获取当前角度
        float currentYaw = player.getYRot();
        float currentPitch = player.getXRot();

        // 4. 计算最短旋转路径的角度差
        float yawDiff = Mth.degreesDifference(currentYaw, targetYaw);
        float pitchDiff = targetPitch - currentPitch;

        // 5. 设定平滑系数
        // 这里的 0.2f 在渲染帧执行意味着非常快的响应。可以根据需求调小（更丝滑）或调大（更死锁）。
        float smoothStep = 0.2f;

        // 6. 应用旋转
        player.setYRot(currentYaw + yawDiff * smoothStep);
        player.setXRot(Mth.clamp(currentPitch + pitchDiff * smoothStep, -90f, 90f));

        // 同步头部转动
        player.setYHeadRot(player.getYRot());
    }
}