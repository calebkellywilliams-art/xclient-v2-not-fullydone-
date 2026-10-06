package com.example.xclient.mixin;

import com.example.xclient.XClient;
import com.example.xclient.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    // ESP: make every living entity render with the glow outline (visible through walls)
    @Inject(method = "hasOutline", at = @At("HEAD"), cancellable = true)
    private void xclient$esp(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (Modules.ESP.enabled && entity instanceof LivingEntity
                && entity != MinecraftClient.getInstance().player) {
            cir.setReturnValue(true);
        }
    }
}
