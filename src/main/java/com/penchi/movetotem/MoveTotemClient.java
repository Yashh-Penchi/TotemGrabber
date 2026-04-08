package com.penchi.movetotem;

import com.penchi.movetotem.command.MoveTotemCommand;
import com.penchi.movetotem.config.ModConfig;
import com.penchi.movetotem.handler.TotemCursorHandler;
import net.fabricmc.api.ClientModInitializer;

public class MoveTotemClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Load persistent config from disk
        ModConfig.load();

        // Register /movetotem command (client-side, no server needed)
        MoveTotemCommand.register();

        // Register inventory open listener for cursor jump
        TotemCursorHandler.register();
    }
}
