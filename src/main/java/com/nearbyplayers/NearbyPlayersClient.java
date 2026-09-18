package com.nearbyplayers;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import org.lwjgl.glfw.GLFW;

import java.util.Comparator;
import java.util.List;

public class NearbyPlayersClient implements ClientModInitializer {

    private static final int[] RADII = {
        16, 32, 48, 64, 96, 128
    };

    private static int radiusIndex = 3;

    private static boolean hudEnabled = true;
    private static boolean showDistance = true;
    private static boolean showHealth = false;
    private static boolean compact = false;

    private static KeyBinding toggleKey;
    private static KeyBinding settingsKey;

    @Override
    public void onInitializeClient() {

        toggleKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                "key.nearbyplayers.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                "category.nearbyplayers"
            )
        );

        settingsKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                "key.nearbyplayers.settings",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "category.nearbyplayers"
            )
        );

        HudRenderCallback.EVENT.register(
            (context, tickDelta) -> renderHud(context)
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            while (toggleKey.wasPressed()) {
                hudEnabled = !hudEnabled;
            }

            while (settingsKey.wasPressed()) {

                if (client.currentScreen == null) {
                    client.setScreen(new SettingsScreen());
                }
            }
        });
    }

    private static int getRadius() {
        return RADII[radiusIndex];
    }

    private static void renderHud(DrawContext context) {

        MinecraftClient client =
            MinecraftClient.getInstance();

        if (!hudEnabled) return;
        if (client.player == null) return;
        if (client.world == null) return;

        List<? extends PlayerEntity> players =
            client.world.getPlayers()
                .stream()
                .filter(player -> player != client.player)
                .filter(player ->
                    player.squaredDistanceTo(client.player)
                        <= getRadius() * getRadius()
                )
                .sorted(
                    Comparator.comparingDouble(
                        player ->
                            player.squaredDistanceTo(
                                client.player
                            )
                    )
                )
                .limit(12)
                .toList();

        int x = 8;
        int y = 8;

        int rowHeight =
            compact ? 12 : 14;

        context.drawText(
            client.textRenderer,
            Text.literal(
                "Nearby Players (" +
                getRadius() +
                "m)"
            ),
            x,
            y,
            0xFFFFFF,
            true
        );

        y += rowHeight;

        for (PlayerEntity player : players) {

            int distance =
                (int) Math.sqrt(
                    player.squaredDistanceTo(
                        client.player
                    )
                );

            StringBuilder line =
                new StringBuilder(
                    player.getName().getString()
                );

            if (showDistance) {
                line.append(" ")
                    .
