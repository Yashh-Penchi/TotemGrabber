package com.penchi.movetotem.handler;

import com.penchi.movetotem.config.ModConfig;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.lang.reflect.Method;
import java.util.List;

public class TotemCursorHandler {

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof InventoryScreen invScreen)) return;
            scheduleJump(client, invScreen);
        });
    }

    private static void scheduleJump(MinecraftClient client, InventoryScreen invScreen) {
        if (!ModConfig.get().enabled) return;

        int delayMs = ModConfig.get().delayMs;

        Thread t = new Thread(() -> {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException ignored) {}

            client.execute(() -> {
                if (client.currentScreen != invScreen) return;
                if (client.player == null) return;

                // Agar offhand mein totem ya shield hai — vanilla behaviour, koi jump nahi
                ItemStack offhand = client.player.getOffHandStack();
                if (offhand.isOf(Items.TOTEM_OF_UNDYING) || offhand.isOf(Items.SHIELD)) return;

                Slot target = findTargetSlot(client, invScreen);
                if (target == null) return;

                moveCursorToSlot(client, invScreen, target);
            });
        });
        t.setDaemon(true);
        t.start();
    }

    private static Slot findTargetSlot(MinecraftClient client, InventoryScreen invScreen) {
        List<Slot> slots = invScreen.getScreenHandler().slots;

        Slot firstInvTotem    = null;
        Slot firstHotbarTotem = null;
        Slot firstInvShield   = null;

        for (Slot slot : slots) {
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) continue;

            int invIndex = slot.getIndex();
            if (invIndex >= 36) continue;

            boolean isHotbar = (invIndex <= 8);

            if (stack.isOf(Items.TOTEM_OF_UNDYING)) {
                if (!isHotbar && firstInvTotem == null)        firstInvTotem = slot;
                else if (isHotbar && firstHotbarTotem == null) firstHotbarTotem = slot;
            }
            if (stack.isOf(Items.SHIELD)) {
                if (!isHotbar && firstInvShield == null)       firstInvShield = slot;
            }
        }

        if (firstInvTotem != null)    return firstInvTotem;
        if (firstHotbarTotem != null) return firstHotbarTotem;
        if (firstInvShield != null)   return firstInvShield;
        return null;
    }

    private static void moveCursorToSlot(MinecraftClient client, InventoryScreen invScreen, Slot slot) {
        try {
            int screenWidth  = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();

            int bgWidth  = 176;
            int bgHeight = 166;
            int originX  = (screenWidth  - bgWidth)  / 2;
            int originY  = (screenHeight - bgHeight) / 2;

            int invIndex = slot.getIndex();
            int slotX, slotY;

            if (invIndex >= 9 && invIndex <= 35) {
                int col = (invIndex - 9) % 9;
                int row = (invIndex - 9) / 9;
                slotX = originX + 8  + col * 18 + 8;
                slotY = originY + 84 + row * 18 + 8;
            } else if (invIndex <= 8) {
                slotX = originX + 8 + invIndex * 18 + 8;
                slotY = originY + 142 + 8;
            } else {
                return;
            }

            double scale  = client.getWindow().getScaleFactor();
            double pixelX = slotX * scale;
            double pixelY = slotY * scale;

            long windowHandle = client.getWindow().getHandle();
            org.lwjgl.glfw.GLFW.glfwSetCursorPos(windowHandle, pixelX, pixelY);

            Method m = findMethod(client.mouse.getClass(), "onCursorPos", long.class, double.class, double.class);
            if (m != null) {
                m.setAccessible(true);
                m.invoke(client.mouse, windowHandle, pixelX, pixelY);
            }

        } catch (Exception e) {
            // silent
        }
    }

    // ── Shift+Click listener — totem on hovered slot → offhand (only if offhand empty) ──
    public static boolean handleShiftClick(MinecraftClient client, Slot hoveredSlot) {
        if (!ModConfig.get().enabled) return false;
        if (client.player == null) return false;

        // Sirf tab kaam kare jab offhand bilkul empty ho
        ItemStack offhand = client.player.getOffHandStack();
        if (!offhand.isEmpty()) return false;

        // Hovered slot mein totem hona chahiye
        if (hoveredSlot == null) return false;
        if (!hoveredSlot.getStack().isOf(Items.TOTEM_OF_UNDYING)) return false;

        // Offhand slot index vanilla inventory screen mein = 45
        int offhandSlotIndex = 45;

        // Swap: pick up from hovered slot, place in offhand using slot swap (button 40 = offhand swap)
        // SlotActionType.SWAP with button=40 swaps hovered slot with offhand
        client.interactionManager.clickSlot(
                client.player.currentScreenHandler.syncId,
                hoveredSlot.id,
                40, // 40 = offhand slot button
                SlotActionType.SWAP,
                client.player
        );

        return true; // event consumed — vanilla shift click cancel
    }

    private static Method findMethod(Class<?> cls, String name, Class<?>... params) {
        while (cls != null) {
            try {
                return cls.getDeclaredMethod(name, params);
            } catch (NoSuchMethodException ignored) {
                cls = cls.getSuperclass();
            }
        }
        return null;
    }
}