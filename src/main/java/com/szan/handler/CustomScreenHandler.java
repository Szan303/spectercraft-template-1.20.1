package com.szan.handler;

import com.szan.SpecterCraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net. minecraft.screen.ScreenHandler;
import net.minecraft.screen. slot.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomScreenHandler extends ScreenHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("SpecterCraft/ScreenHandler");

    private final PlayerInventory playerInventory;

    // Layout
    private static final int[][] LAYOUT = {
            {'#', '1', '1', '0'},
            {'#', '1', '1', '0'},
            {'#', '1', '1', '2'},
            {'#', '1', '1', '3'}
    };

    private static final int SLOT_SIZE = 18;

    public CustomScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(SpecterCraft.CUSTOM_SCREEN_HANDLER, syncId);
        this.playerInventory = playerInventory;

        addSlots();

        LOGGER.info("[ScreenHandler] Utworzono z {} slotami", this.slots.size());
    }

    private void addSlots() {
        int startX = 8;
        int startY = 18;

        int inventorySlotIndex = 0;
        int[] inventorySlots = {9, 10, 11, 12, 13, 14, 15, 16};

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                int x = startX + col * SLOT_SIZE;
                int y = startY + row * SLOT_SIZE;

                char slotType = (char) LAYOUT[row][col];

                switch (slotType) {
                    case '#':  // Armor
                        int armorSlot = 3 - row;
                        int vanillaArmorSlot = 36 + armorSlot;
                        this.addSlot(new Slot(playerInventory, vanillaArmorSlot, x, y));
                        break;

                    case '1':  // Inventory
                        if (inventorySlotIndex < inventorySlots.length) {
                            this.addSlot(new Slot(playerInventory, inventorySlots[inventorySlotIndex], x, y));
                            inventorySlotIndex++;
                        }
                        break;

                    case '2':  // Offhand
                        this.addSlot(new Slot(playerInventory, 40, x, y));
                        break;

                    case '3':  // Main hand
                        this.addSlot(new Slot(playerInventory, 0, x, y));
                        break;

                    case '0':  // Empty
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot != null && slot.hasStack()) {
            ItemStack slotStack = slot.getStack();
            stack = slotStack.copy();

            // Simple shift-click:  move to any free slot
            if (!this.insertItem(slotStack, 0, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return stack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;  // Zawsze można używać
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        LOGGER.info("[ScreenHandler] Zamknięto");
    }
}