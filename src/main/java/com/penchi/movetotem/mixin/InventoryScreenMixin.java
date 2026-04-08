package com.penchi.movetotem.mixin;

import com.penchi.movetotem.handler.TotemCursorHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public class InventoryScreenMixin {

    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V",
            at = @At("HEAD"), cancellable = true)
    private void onSlotClick(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        // Sirf InventoryScreen pe fire karo
        MinecraftClient client = MinecraftClient.getInstance();
        if (!(client.currentScreen instanceof InventoryScreen)) return;

        // Sirf shift+click intercept karo (actionType = QUICK_MOVE, button = 0)
        if (actionType != SlotActionType.QUICK_MOVE) return;

        boolean consumed = TotemCursorHandler.handleShiftClick(client, slot);
        if (consumed) ci.cancel(); // vanilla shift click rok do
    }
}