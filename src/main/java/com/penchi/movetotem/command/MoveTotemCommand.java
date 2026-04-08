package com.penchi.movetotem.command;

import com.penchi.movetotem.config.ModConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;

public class MoveTotemCommand {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(
                literal("totemgrabber")

                    // /movetotem on
                    .then(literal("on").executes(ctx -> {
                        ModConfig.get().enabled = true;
                        ModConfig.save();
                        sendChat("§6[§eTotemGrabber§6] §aMod §2ON");
                        return 1;
                    }))

                    // /movetotem off
                    .then(literal("off").executes(ctx -> {
                        ModConfig.get().enabled = false;
                        ModConfig.save();
                        sendChat("§6[§eTotemGrabber§6] §cMod §4OFF");
                        return 1;
                    }))

                    // /movetotem toggle
                    .then(literal("toggle").executes(ctx -> {
                        boolean now = !ModConfig.get().enabled;
                        ModConfig.get().enabled = now;
                        ModConfig.save();
                        sendChat(now
                                ? "§6[§eTotemGrabber§6] §aMod §2ON"
                                : "§6[§eTotemGrabber§6] §cMod §4OFF");
                        return 1;
                    }))

                    // /movetotem status
                    .then(literal("status").executes(ctx -> {
                        ModConfig cfg = ModConfig.get();
                        String state = cfg.enabled ? "§aON" : "§cOFF";
                        sendChat("§6[§eTotemGrabber§6] §7Status: " + state
                                + " §8| §7Delay: §e" + cfg.delayMs + "ms");
                        return 1;
                    }))

                    // /movetotem delay <ms>
                    .then(literal("delay")
                        .then(argument("ms", integer(0, 2000)).executes(ctx -> {
                            int ms = getInteger(ctx, "ms");
                            // clamp minimum to 50ms to avoid same-tick issues
                            if (ms < 50) ms = 50;
                            ModConfig.get().delayMs = ms;
                            ModConfig.save();
                            sendChat("§6[§eTotemGrabber§6] §7Delay set to §e" + ms + "ms");
                            return 1;
                        })))
            )
        );
    }

    private static void sendChat(String msg) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal(msg), false);
        }
    }
}
