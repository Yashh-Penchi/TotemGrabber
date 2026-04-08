package com.penchi.movetotem.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;

public class ModConfig {

    // ── Runtime state ──────────────────────────────────────────────
    public boolean enabled = true;
    public int delayMs = 100;

    // ── Singleton ──────────────────────────────────────────────────
    private static ModConfig INSTANCE;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("movetotem.json");

    public static ModConfig get() {
        if (INSTANCE == null) load();
        return INSTANCE;
    }

    // ── Persistence ────────────────────────────────────────────────
    public static void load() {
        File file = CONFIG_PATH.toFile();
        if (file.exists()) {
            try (Reader r = new FileReader(file)) {
                INSTANCE = GSON.fromJson(r, ModConfig.class);
                if (INSTANCE == null) INSTANCE = new ModConfig();
            } catch (Exception e) {
                INSTANCE = new ModConfig();
            }
        } else {
            INSTANCE = new ModConfig();
            save();
        }
    }

    public static void save() {
        try (Writer w = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(INSTANCE, w);
        } catch (Exception e) {
            // silent fail — not critical
        }
    }
}
