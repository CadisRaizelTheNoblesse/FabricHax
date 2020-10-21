package io.github.pranavgade20.fabrichax.mixins;

import io.github.pranavgade20.fabrichax.Settings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class PlayerManager {
    @Inject(at = @At("RETURN"), method = "onGameJoin")
    public void setPlayer(GameJoinS2CPacket p, CallbackInfo ci) {
        System.out.println("Joined a new world.");
        Settings.player = MinecraftClient.getInstance().player;
        Settings.world = MinecraftClient.getInstance().world;
    }

    @Inject(at = @At("RETURN"), method = "onDisconnect")
    public void setPlayer(DisconnectS2CPacket p, CallbackInfo ci) {
        System.out.println("Joined a new world.");
        Settings.player = null;
        Settings.world = null;
    }

    @Inject(at = @At("RETURN"), method = "onDisconnected")
    public void setPlayer(Text reason, CallbackInfo ci) {
        System.out.println("Joined a new world.");
        Settings.player = null;
        Settings.world = null;
    }

    @Inject(at = @At("RETURN"), method = "onPlayerRespawn")
    public void setPlayer(PlayerRespawnS2CPacket p, CallbackInfo ci) {
        System.out.println("Joined a new world.");
        Settings.player = MinecraftClient.getInstance().player;
        Settings.world = MinecraftClient.getInstance().world;
    }
}
