package com.example.xclient;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;
import java.util.function.Consumer;

/** Per-tick logic for the movement / combat / player cheats. Singleplayer. */
public final class Features {
    private static boolean wasGod;

    private Features() {}

    public static void tick(MinecraftClient mc, ClientPlayerEntity p, int tick) {
        // ---- Movement ----
        if (Modules.FLY.enabled) fly(mc, p);
        else if (Modules.SPEED.enabled) speed(mc, p);

        if (Modules.SPRINT.enabled && mc.options.forwardKey.isPressed() && !p.isSneaking()
                && !p.horizontalCollision && p.getHungerManager().getFoodLevel() > 6) {
            p.setSprinting(true);
        }
        if (Modules.SPIDER.enabled && p.horizontalCollision) {
            Vec3d v = p.getVelocity();
            p.setVelocity(v.x, 0.2, v.z);
        }
        if (Modules.GLIDE.enabled && !p.isOnGround()) {
            Vec3d v = p.getVelocity();
            if (v.y < -0.1) p.setVelocity(v.x, -0.1, v.z);
        }
        if (Modules.NOFALL.enabled || Modules.FLY.enabled || Modules.GLIDE.enabled) {
            p.fallDistance = 0;
            server(mc, p, sp -> sp.fallDistance = 0);
        }

        // ---- Combat ----
        if (Modules.KILLAURA.enabled) aura(mc, p);

        // ---- Player (applied on the integrated server) ----
        boolean god = Modules.GODMODE.enabled, hunger = Modules.NOHUNGER.enabled, drown = Modules.NODROWN.enabled;
        boolean prevGod = wasGod;
        if (god || hunger || drown || prevGod) {
            server(mc, p, sp -> {
                if (god) {
                    sp.setInvulnerable(true);
                    if (sp.getHealth() < sp.getMaxHealth()) sp.setHealth(sp.getMaxHealth());
                } else if (prevGod) {
                    sp.setInvulnerable(false);
                }
                if (hunger) {
                    sp.getHungerManager().setFoodLevel(20);
                    sp.getHungerManager().setSaturationLevel(20f);
                }
                if (drown) sp.setAir(sp.getMaxAir());
            });
        }
        wasGod = god;

        if (tick % 5 == 0) attributes(mc, p);
    }

    // ---- helpers ----
    private static Vec3d moveDir(MinecraftClient mc, ClientPlayerEntity p) {
        double f = (mc.options.forwardKey.isPressed() ? 1 : 0) - (mc.options.backKey.isPressed() ? 1 : 0);
        double s = (mc.options.leftKey.isPressed() ? 1 : 0) - (mc.options.rightKey.isPressed() ? 1 : 0);
        double len = Math.sqrt(f * f + s * s);
        if (len == 0) return Vec3d.ZERO;
        f /= len; s /= len;
        double yaw = Math.toRadians(p.getYaw());
        return new Vec3d(-Math.sin(yaw) * f + Math.cos(yaw) * s, 0, Math.cos(yaw) * f + Math.sin(yaw) * s);
    }

    private static void fly(MinecraftClient mc, ClientPlayerEntity p) {
        double sp = Modules.FLY_SPEED.value;
        Vec3d d = moveDir(mc, p);
        double vy = 0;
        if (mc.options.jumpKey.isPressed()) vy += sp;
        if (mc.options.sneakKey.isPressed()) vy -= sp;
        p.setVelocity(d.x * sp, vy, d.z * sp);
    }

    private static void speed(MinecraftClient mc, ClientPlayerEntity p) {
        Vec3d d = moveDir(mc, p);
        if (d.equals(Vec3d.ZERO)) return;
        double v = 0.22 * Modules.SPEED_MULT.value;
        p.setVelocity(d.x * v, p.getVelocity().y, d.z * v);
    }

    private static void aura(MinecraftClient mc, ClientPlayerEntity p) {
        if (mc.world == null || mc.interactionManager == null) return;
        if (p.getAttackCooldownProgress(0.5f) < 1.0f) return;
        double range = Modules.REACH.enabled ? Modules.REACH_ENTITY.value : 3.0;
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity e : mc.world.getEntities()) {
            if (e instanceof Monster && e instanceof LivingEntity le && le.isAlive()) { // hostile mobs only
                double d = p.distanceTo(le);
                if (d <= range && d < bestDist) { best = le; bestDist = d; }
            }
        }
        if (best != null) {
            mc.interactionManager.attackEntity(p, best);
            p.swingHand(Hand.MAIN_HAND);
        }
    }

    private static void attributes(MinecraftClient mc, ClientPlayerEntity p) {
        double step = Modules.STEP.enabled ? Modules.STEP_HEIGHT.value : 0.6;
        double jump = Modules.HIGHJUMP.enabled ? Modules.JUMP_STRENGTH.value : 0.42;
        set(p, EntityAttributes.STEP_HEIGHT, step);
        set(p, EntityAttributes.JUMP_STRENGTH, jump);
        server(mc, p, sp -> set(sp, EntityAttributes.STEP_HEIGHT, step));
    }

    private static void set(PlayerEntity p, RegistryEntry<EntityAttribute> attr, double value) {
        EntityAttributeInstance a = p.getAttributeInstance(attr);
        if (a != null && a.getBaseValue() != value) a.setBaseValue(value);
    }

    /** Run something on the integrated server's copy of the player (singleplayer only). */
    private static void server(MinecraftClient mc, ClientPlayerEntity p, Consumer<ServerPlayerEntity> action) {
        IntegratedServer s = mc.getServer();
        if (s == null) return;
        UUID id = p.getUuid();
        s.execute(() -> {
            ServerPlayerEntity sp = s.getPlayerManager().getPlayer(id);
            if (sp != null) action.accept(sp);
        });
    }
}
