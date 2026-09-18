package com.nearbyplayers;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class NearbyPlayersClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        System.out.println("[NearbyPlayers] MOD LOADED!");

        HudElementRegistry.addLast(
            Identifier.of("nearbyplayers", "test_hud"),
            NearbyPlayersClient::render
        );
    }

    private static void render(
        DrawContext context,
        net.minecraft.client.render.RenderTickCounter tickCounter
    ) {

        MinecraftClient client =
            MinecraftClient.getInstance();

        context.drawText(
            client.textRenderer,
            Text.literal("NEARBY PLAYERS HUD TEST"),
            10,
            10,
            0xFFFFFF,
            true
        );

        context.drawText(
            client.textRenderer,
            Text.literal("MOD WORKING"),
            10,
            25,
            0x00FF00,
            true
        );
    }
}
