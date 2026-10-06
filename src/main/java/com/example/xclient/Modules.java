package com.example.xclient;

import com.example.xclient.Module.Category;
import net.minecraft.client.MinecraftClient;

import java.util.List;

public final class Modules {
    // Movement
    public static final Module FLY      = new Module("Fly", Category.MOVEMENT);
    public static final Module SPEED    = new Module("Speed", Category.MOVEMENT);
    public static final Module SPRINT   = new Module("Sprint", Category.MOVEMENT);
    public static final Module STEP     = new Module("Step", Category.MOVEMENT);
    public static final Module HIGHJUMP = new Module("HighJump", Category.MOVEMENT);
    public static final Module SPIDER   = new Module("Spider", Category.MOVEMENT);
    public static final Module GLIDE    = new Module("Glide", Category.MOVEMENT);
    public static final Module NOFALL   = new Module("NoFall", Category.MOVEMENT);
    // Combat
    public static final Module REACH    = new Module("Reach", Category.COMBAT);
    public static final Module KILLAURA = new Module("KillAura", Category.COMBAT);
    // Player
    public static final Module GODMODE  = new Module("Godmode", Category.PLAYER);
    public static final Module NOHUNGER = new Module("NoHunger", Category.PLAYER);
    public static final Module NODROWN  = new Module("NoDrown", Category.PLAYER);
    // Render
    public static final Module ESP        = new Module("ESP", Category.RENDER);
    public static final Module XRAY       = new Module("X-Ray", Category.RENDER);
    public static final Module FULLBRIGHT = new Module("Fullbright", Category.RENDER);
    // Client
    public static final Module HUD = new Module("HUD", Category.CLIENT);

    public static final Setting FLY_SPEED       = new Setting("Speed", 0.5, 5.0, 0.5, 1.0);
    public static final Setting SPEED_MULT      = new Setting("Multiplier", 1.0, 5.0, 0.5, 2.0);
    public static final Setting STEP_HEIGHT     = new Setting("Height", 1.0, 5.0, 0.5, 2.0);   // vanilla 0.6
    public static final Setting JUMP_STRENGTH   = new Setting("Strength", 0.5, 3.0, 0.1, 1.0); // vanilla 0.42
    public static final Setting REACH_BLOCK     = new Setting("Block", 3.0, 8.0, 0.5, 6.0);    // vanilla 4.5
    public static final Setting REACH_ENTITY    = new Setting("Entity", 3.0, 8.0, 0.5, 6.0);   // vanilla 3.0

    public static final List<Module> ALL = List.of(
        FLY, SPEED, SPRINT, STEP, HIGHJUMP, SPIDER, GLIDE, NOFALL,
        REACH, KILLAURA,
        GODMODE, NOHUNGER, NODROWN,
        ESP, XRAY, FULLBRIGHT,
        HUD);

    static {
        HUD.enabled = true;
        FLY.settings.add(FLY_SPEED);
        SPEED.settings.add(SPEED_MULT);
        STEP.settings.add(STEP_HEIGHT);
        HIGHJUMP.settings.add(JUMP_STRENGTH);
        REACH.settings.add(REACH_BLOCK);
        REACH.settings.add(REACH_ENTITY);
        XRAY.onChange = on -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.worldRenderer != null) mc.worldRenderer.reload(); // rebuild chunk meshes
        };
    }

    private Modules() {}
}
