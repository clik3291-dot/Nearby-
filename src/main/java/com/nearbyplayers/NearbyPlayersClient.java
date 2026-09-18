package com.nearbyplayers;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

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

        HudElementRegistry.addLast(
            Identifier.of("nearbyplayers", "nearby_players_hud"),
            (context, tickCounter) -> renderHud(context)
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

        int x = 8;
        int y = 8;

        int rowHeight = compact ? 12 : 14;

        context.drawText(
            client.textRenderer,
            Text.literal("NEARBY PLAYERS"),
            x,
            y,
            0x55FFFF,
            true
        );

        y += rowHeight + 2;

        context.drawText(
            client.textRenderer,
            Text.literal(
                "Radius: " + getRadius() + "m"
            ),
            x,
            y,
            0xFFFFFF,
            true
        );

        y += rowHeight + 2;

        List<? extends PlayerEntity> players =
            client.world.getPlayers()
                .stream()
                .filter(player -> player != client.player)
                .filter(player ->
                    player.squaredDistanceTo(client.player)
                        <= (double) getRadius() * getRadius()
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

        if (players.isEmpty()) {

            context.drawText(
                client.textRenderer,
                Text.literal("No nearby players"),
                x,
                y,
                0xAAAAAA,
                true
            );

            return;
        }

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
                    .append(distance)
                    .append("m");
            }

            if (showHealth) {
                line.append(" HP:")
                    .append(
                        Math.round(
                            player.getHealth()
                        )
                    );
            }

            context.drawText(
                client.textRenderer,
                Text.literal(line.toString()),
                x,
                y,
                0xFFFFFF,
                true
            );

            y += rowHeight;
        }
    }

    private static class SettingsScreen
        extends Screen {

        protected SettingsScreen() {
            super(
                Text.literal(
                    "Nearby Players Settings"
                )
            );
        }

        @Override
        protected void init() {

            int centerX = this.width / 2;
            int y = this.height / 2 - 70;

            addDrawableChild(
                ButtonWidget.builder(
                    Text.literal(
                        "Radius: " +
                        getRadius() +
                        "m"
                    ),
                    button -> {

                        radiusIndex =
                            (radiusIndex + 1)
                            % RADII.length;

                        button.setMessage(
                            Text.literal(
                                "Radius: " +
                                getRadius() +
                                "m"
                            )
                        );
                    }
                ).dimensions(
                    centerX - 100,
                    y,
                    200,
                    20
                ).build()
            );

            y += 26;

            addDrawableChild(
                ButtonWidget.builder(
                    Text.literal(
                        "Distance: " +
                        (showDistance
                            ? "ON"
                            : "OFF")
                    ),
                    button -> {

                        showDistance =
                            !showDistance;

                        button.setMessage(
                            Text.literal(
                                "Distance: " +
                                (showDistance
                                    ? "ON"
                                    : "OFF")
                            )
                        );
                    }
                ).dimensions(
                    centerX - 100,
                    y,
                    200,
                    20
                ).build()
            );

            y += 26;

            addDrawableChild(
                ButtonWidget.builder(
                    Text.literal(
                        "Health: " +
                        (showHealth
                            ? "ON"
                            : "OFF")
                    ),
                    button -> {

                        showHealth =
                            !showHealth;

                        button.setMessage(
                            Text.literal(
                                "Health: " +
                                (showHealth
                                    ? "ON"
                                    : "OFF")
                            )
                        );
                    }
                ).dimensions(
                    centerX - 100,
                    y,
                    200,
                    20
                ).build()
            );

            y += 26;

            addDrawableChild(
                ButtonWidget.builder(
                    Text.literal(
                        "Compact: " +
                        (compact
                            ? "ON"
                            : "OFF")
                    ),
                    button -> {

                        compact =
                            !compact;

                        button.setMessage(
                            Text.literal(
                                "Compact: " +
                                (compact
                                    ? "ON"
                                    : "OFF")
                            )
                        );
                    }
                ).dimensions(
                    centerX - 100,
                    y,
                    200,
                    20
                ).build()
            );

            y += 32;

            addDrawableChild(
                ButtonWidget.builder(
                    Text.literal("Done"),
                    button ->
                        MinecraftClient
                            .getInstance()
                            .setScreen(null)
                ).dimensions(
                    centerX - 100,
                    y,
                    200,
                    20
                ).build()
            );
        }

        @Override
        public void render(
            DrawContext context,
            int mouseX,
            int mouseY,
            float delta
        ) {

            MinecraftClient client =
                MinecraftClient.getInstance();

            context.drawCenteredTextWithShadow(
                client.textRenderer,
                this.title,
                this.width / 2,
                30,
                0xFFFFFF
            );

            super.render(
                context,
                mouseX,
                mouseY,
                delta
            );
        }
    }
}
