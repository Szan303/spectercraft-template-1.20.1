package com.szan.client;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client. networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net. minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net. minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomInventoryScreen extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger("SpecterCraft/CustomInventory");

    private static final int SLOT_SIZE = 18;
    private static final int GRID_COLS = 4;
    private static final int GRID_ROWS = 4;

    // Layout:  # = armor, 0 = empty, 1 = inventory, 2 = offhand, 3 = mainhand
    private static final int[][] LAYOUT = {
            {'#', '1', '1', '0'},  // Row 0: Head, slot, slot, empty
            {'#', '1', '1', '0'},  // Row 1: Chest, slot, slot, empty
            {'#', '1', '1', '2'},  // Row 2: Legs, slot, slot, offhand
            {'#', '1', '1', '3'}   // Row 3: Boots, slot, slot, mainhand
    };

    // 8 slotów inventory (9-16)
    private static final int[] INVENTORY_SLOTS = {9, 10, 11, 12, 13, 14, 15, 16};

    private int hoveredSlot = -1;
    private int startX, startY;

    // Drag & drop state
    private ItemStack draggedStack = ItemStack.EMPTY;
    private int draggedFromSlot = -1;
    private boolean isDragging = false;

    public CustomInventoryScreen() {
        super(Text.literal("Ekwipunek"));
    }

    @Override
    protected void init() {
        super.init();

        // Wycentruj grid
        int totalWidth = GRID_COLS * SLOT_SIZE;
        int totalHeight = GRID_ROWS * SLOT_SIZE;

        startX = (this.width - totalWidth) / 2;
        startY = (this.height - totalHeight) / 2 - 10;

        LOGGER.info("[CustomInventory] Grid: {}x{} at ({}, {})", GRID_COLS, GRID_ROWS, startX, startY);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);

        // Tytuł
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Twój Ekwipunek"),
                this.width / 2,
                startY - 20,
                0xFFFFFF
        );

        // Renderuj grid
        PlayerEntity player = MinecraftClient. getInstance().player;
        if (player == null) return;

        hoveredSlot = -1;
        int inventorySlotIndex = 0;

        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int x = startX + col * SLOT_SIZE;
                int y = startY + row * SLOT_SIZE;

                char slotType = (char) LAYOUT[row][col];

                boolean isHovered = mouseX >= x && mouseX < x + SLOT_SIZE &&
                        mouseY >= y && mouseY < y + SLOT_SIZE;

                ItemStack stack = ItemStack.EMPTY;
                int slotIndex = row * GRID_COLS + col;

                switch (slotType) {
                    case '#':  // Armor
                        int armorSlot = getArmorSlot(row);
                        stack = player.getInventory().getArmorStack(armorSlot);

                        // Jeśli przeciągamy z tego slotu, pokaż pusty
                        if (isDragging && draggedFromSlot == slotIndex) {
                            stack = ItemStack.EMPTY;
                        }

                        renderSlot(context, x, y, stack, isHovered, getArmorLabel(row));
                        if (isHovered) hoveredSlot = slotIndex;
                        break;

                    case '1': // Inventory slot
                        if (inventorySlotIndex < INVENTORY_SLOTS.length) {
                            int vanillaSlot = INVENTORY_SLOTS[inventorySlotIndex];
                            stack = player.getInventory().getStack(vanillaSlot);

                            if (isDragging && draggedFromSlot == slotIndex) {
                                stack = ItemStack. EMPTY;
                            }

                            renderSlot(context, x, y, stack, isHovered, null);
                            if (isHovered) hoveredSlot = slotIndex;
                            inventorySlotIndex++;
                        }
                        break;

                    case '2': // Offhand
                        stack = player. getOffHandStack();

                        if (isDragging && draggedFromSlot == slotIndex) {
                            stack = ItemStack.EMPTY;
                        }

                        renderSlot(context, x, y, stack, isHovered, "Off");
                        if (isHovered) hoveredSlot = slotIndex;
                        break;

                    case '3': // Main hand (slot 0)
                        stack = player.getInventory().getStack(0);

                        if (isDragging && draggedFromSlot == slotIndex) {
                            stack = ItemStack.EMPTY;
                        }

                        renderSlot(context, x, y, stack, isHovered, "Hand");
                        if (isHovered) hoveredSlot = slotIndex;
                        break;

                    case '0': // Empty - nie rysuj nic
                    default:
                        break;
                }
            }
        }

        // Tooltip (tylko gdy nie przeciągamy)
        if (!isDragging && hoveredSlot >= 0) {
            ItemStack hoveredStack = getStackAtSlot(player, hoveredSlot);
            if (!hoveredStack.isEmpty()) {
                context.drawItemTooltip(this.textRenderer, hoveredStack, mouseX, mouseY);
            }
        }

        // Rysuj przeciągany item
        if (isDragging && ! draggedStack.isEmpty()) {
            context.drawItem(draggedStack, mouseX - 8, mouseY - 8);
            context.drawItemInSlot(this.textRenderer, draggedStack, mouseX - 8, mouseY - 8);
        }

        // Instrukcja
        context.drawCenteredTextWithShadow(
                this. textRenderer,
                Text.literal("E = Zamknij | Przeciągnij = Przenieś").styled(s -> s.withColor(0xAAAAAA)),
                this.width / 2,
                startY + GRID_ROWS * SLOT_SIZE + 20,
                0xAAAAAA
        );

        super.render(context, mouseX, mouseY, delta);
    }

    /**
     * Renderuj pojedynczy slot
     */
    private void renderSlot(DrawContext context, int x, int y, ItemStack stack, boolean hovered, String label) {
        // Tło slotu
        int bgColor = hovered ? 0xAA808080 : 0xAA3A3A3A;
        context.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, bgColor);

        // Border (vanilla-like)
        int borderLight = hovered ? 0xFFFFFFFF : 0xFF8B8B8B;
        int borderDark = hovered ? 0xFFAAAAAA : 0xFF373737;

        context.fill(x, y, x + SLOT_SIZE, y + 1, borderLight);
        context.fill(x, y, x + 1, y + SLOT_SIZE, borderLight);
        context.fill(x, y + SLOT_SIZE - 1, x + SLOT_SIZE, y + SLOT_SIZE, borderDark);
        context.fill(x + SLOT_SIZE - 1, y, x + SLOT_SIZE, y + SLOT_SIZE, borderDark);

        // Item
        if (!stack.isEmpty()) {
            context.drawItem(stack, x + 1, y + 1);
            context.drawItemInSlot(this.textRenderer, stack, x + 1, y + 1);
        }

        // Label
        if (label != null && stack.isEmpty()) {
            int textWidth = this.textRenderer.getWidth(label);
            int textX = x + (SLOT_SIZE - textWidth) / 2;

            context.drawText(
                    this.textRenderer,
                    Text.literal(label),
                    textX,
                    y + 5,
                    0x555555,
                    false
            );
        }
    }

    /**
     * Pobierz armor slot index (0=boots, 1=legs, 2=chest, 3=head)
     */
    private int getArmorSlot(int row) {
        return 3 - row;
    }

    /**
     * Pobierz label dla armor slotu
     */
    private String getArmorLabel(int row) {
        return switch (row) {
            case 0 -> "Head";
            case 1 -> "Chest";
            case 2 -> "Legs";
            case 3 -> "Boots";
            default -> "";
        };
    }

    /**
     * Pobierz ItemStack na danym slocie
     */
    private ItemStack getStackAtSlot(PlayerEntity player, int slotIndex) {
        int row = slotIndex / GRID_COLS;
        int col = slotIndex % GRID_COLS;

        if (row < 0 || row >= GRID_ROWS || col < 0 || col >= GRID_COLS) {
            return ItemStack.EMPTY;
        }

        char slotType = (char) LAYOUT[row][col];

        switch (slotType) {
            case '#':
                return player.getInventory().getArmorStack(getArmorSlot(row));

            case '1':
                // Oblicz który to slot inventory
                int inventoryIndex = 0;
                for (int r = 0; r < GRID_ROWS; r++) {
                    for (int c = 0; c < GRID_COLS; c++) {
                        if (LAYOUT[r][c] == '1') {
                            if (r == row && c == col) {
                                // Znaleźliśmy nasz slot
                                if (inventoryIndex < INVENTORY_SLOTS.length) {
                                    return player.getInventory().getStack(INVENTORY_SLOTS[inventoryIndex]);
                                }
                            }
                            inventoryIndex++;
                        }
                    }
                }
                break;

            case '2':
                return player.getOffHandStack();

            case '3':
                return player.getInventory().getStack(0);
        }

        return ItemStack.EMPTY;
    }

    /**
     * Pobierz vanilla slot ID dla danego grid slotu
     */
    private int getVanillaSlotId(int slotIndex) {
        int row = slotIndex / GRID_COLS;
        int col = slotIndex % GRID_COLS;

        if (row < 0 || row >= GRID_ROWS || col < 0 || col >= GRID_COLS) {
            return -1;
        }

        char slotType = (char) LAYOUT[row][col];

        switch (slotType) {
            case '#':
                return 36 + getArmorSlot(row); // Armor slots:  36=boots, 37=legs, 38=chest, 39=head

            case '1':
                int inventoryIndex = 0;
                for (int r = 0; r < GRID_ROWS; r++) {
                    for (int c = 0; c < GRID_COLS; c++) {
                        if (LAYOUT[r][c] == '1') {
                            if (r == row && c == col) {
                                if (inventoryIndex < INVENTORY_SLOTS.length) {
                                    return INVENTORY_SLOTS[inventoryIndex];
                                }
                            }
                            inventoryIndex++;
                        }
                    }
                }
                break;

            case '2':
                return 40; // Offhand slot

            case '3':
                return 0; // Main hand (hotbar slot 0)
        }

        return -1;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        PlayerEntity player = MinecraftClient. getInstance().player;
        if (player == null) return super.mouseClicked(mouseX, mouseY, button);

        if (button == 0 && hoveredSlot >= 0) { // Left click
            ItemStack clickedStack = getStackAtSlot(player, hoveredSlot);

            if (!clickedStack.isEmpty()) {
                // Zacznij przeciąganie
                isDragging = true;
                draggedStack = clickedStack. copy();
                draggedFromSlot = hoveredSlot;

                LOGGER.info("[CustomInventory] Rozpoczęto przeciąganie z slotu {}:  {}",
                        hoveredSlot, draggedStack.getItem().getName().getString());

                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return super.mouseReleased(mouseX, mouseY, button);

        if (button == 0 && isDragging) {
            if (hoveredSlot >= 0 && hoveredSlot != draggedFromSlot) {
                // Upuść item na nowy slot
                int fromSlot = getVanillaSlotId(draggedFromSlot);
                int toSlot = getVanillaSlotId(hoveredSlot);

                if (fromSlot >= 0 && toSlot >= 0) {
                    LOGGER.info("[CustomInventory] Przenoszenie:  slot {} -> slot {}", fromSlot, toSlot);

                    // Wyślij packet do serwera (TODO: zaimplementuj server-side)
                    swapSlots(fromSlot, toSlot);
                } else {
                    LOGGER. warn("[CustomInventory] Nieprawidłowe sloty:  {} -> {}", fromSlot, toSlot);
                }
            } else {
                LOGGER.info("[CustomInventory] Anulowano przeciąganie");
            }

            // Reset drag state
            isDragging = false;
            draggedStack = ItemStack.EMPTY;
            draggedFromSlot = -1;

            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    /**
     * Zamień miejscami 2 sloty (CLIENT-SIDE - tymczasowo)
     */
    private void swapSlots(int fromSlot, int toSlot) {
        PlayerEntity player = MinecraftClient. getInstance().player;
        if (player == null) return;

        ItemStack fromStack = player.getInventory().getStack(fromSlot).copy();
        ItemStack toStack = player.getInventory().getStack(toSlot).copy();

        // Temporary swap (CLIENT-SIDE ONLY - będzie się resetować!)
        player.getInventory().setStack(fromSlot, toStack);
        player.getInventory().setStack(toSlot, fromStack);

        LOGGER.info("[CustomInventory] ✓ Zamieniono sloty {} <-> {} (CLIENT-SIDE)", fromSlot, toSlot);

        // TODO: Wyślij packet do serwera aby zapisać zmianę permanentnie
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void close() {
        // Reset drag state przy zamykaniu
        isDragging = false;
        draggedStack = ItemStack.EMPTY;
        draggedFromSlot = -1;

        LOGGER.info("[CustomInventory] Zamknięto");
        super.close();
    }
}