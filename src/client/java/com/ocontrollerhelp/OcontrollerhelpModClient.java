package com.ocontrollerhelp;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;
import net.minecraft.world.entity.monster.Monster;

public class OcontrollerhelpModClient implements ClientModInitializer {

	public static KeyMapping controllerKey; // 设为静态方便 Mixin 读取
	public static Entity currentTarget = null; // 设为静态方便 Mixin 读取
	public static boolean isLockOnMode = false;

	KeyMapping.Category controllerAim = new KeyMapping.Category(
			Identifier.fromNamespaceAndPath(OcontrollerhelpMod.MOD_ID, "controller_aim")
	);

	@Override
	public void onInitializeClient() {
		controllerKey = KeyBindingHelper.registerKeyBinding(
				new KeyMapping(
						"key.ocontrollerhelp-mod.lock", // The translation key for the key mapping.
						InputConstants.Type.KEYSYM, // The type of the keybinding; KEYSYM for keyboard, MOUSE for mouse.
						GLFW.GLFW_KEY_J, // The GLFW keycode of the key.
						controllerAim // The category of the mapping.
				));

		// 注册客户端 Tick 事件
		ClientTickEvents.END_CLIENT_TICK.register(this::onTick);

	}

	// 解决 Text 找不到的问题
	private void sendNotify(LocalPlayer player, String message) {
		// 下面这行适用于较新的 Minecraft 版本 (1.19+)
		player.displayClientMessage(Component.literal(message), true);

		// 如果你是旧版本，请尝试：
		// player.displayClientMessage(new TextComponent(message), true);
	}

	private void onTick(Minecraft client) {
		if (client.player == null || client.level == null) return;

		// 使用 consumeClick() 检测“按下一瞬间”的动作
		while (controllerKey.consumeClick()) {
			isLockOnMode = !isLockOnMode; // 切换状态



			if (isLockOnMode) {
				// 开启锁定时，立即寻找一次目标
				currentTarget = findNewTarget(client.player);
				// 如果周围没怪，可以选择立即关闭锁定模式
				if (currentTarget == null) {
					isLockOnMode = false;
				}
			} else {
				// 关闭锁定时，清除目标
				currentTarget = null;
			}
		}

		// 逻辑维护：如果正在锁定模式，但目标失效了（死了或跑远了），自动关闭锁定
		if (isLockOnMode) {
			if (!isValidTarget(client.player, currentTarget)) {
				// 如果当前目标丢了，尝试自动寻找下一个最近的目标（可选）
				currentTarget = findNewTarget(client.player);

				// 如果依然找不到新目标，彻底关闭模式
				if (currentTarget == null) {
					isLockOnMode = false;
				}
			}
		}
	}

	private boolean isValidTarget(LocalPlayer player, Entity target) {
		return target != null && target.isAlive() && target.distanceTo(player) <= 15.0 && player.hasLineOfSight(target);
	}

	private Entity findNewTarget(LocalPlayer player) {
		double range = 15.0;
		AABB scanBox = player.getBoundingBox().inflate(range);
		Entity bestEntity = null;
		double bestDistSq = Double.MAX_VALUE;

		for (Entity e : player.level().getEntities(player, scanBox)) {
			if (e instanceof Monster && e.isAlive() && player.hasLineOfSight(e)) {
				double distSq = player.distanceToSqr(e);
				if (distSq < bestDistSq) {
					bestDistSq = distSq;
					bestEntity = e;
				}
			}
		}
		return bestEntity;
	}
}
