package com.szan. client;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net. minecraft.client.gui.DrawContext;
import net.minecraft. client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net. minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import com.szan.SpecterCraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomInventoryScreen extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger("SpecterCraft/CustomInventory");

    private static final int SLOT_SIZE = 18;
    private static final int GRID_COLS = 4;
    private static final int GRID_ROWS = 4;

    // Layout
    private static final int[][] LAYOUT = {
            {'#', '1', '1', '0'},
            {'#', '1', '1', '0'},
            {'#', '1', '1', '2'},
            {'#', '1', '1', '3'}
    };

    private static final int[] INVENTORY_SLOTS = {9, 10, 11, 12, 13, 14, 15, 16};

    private int startX, startY;
    private int hoveredSlot = -1;

    // Cursor state (UŻYWAMY -1 jako virtual cursor!)
    private ItemStack cursorStack = ItemStack.EMPTY;
    private int cursorSourceSlot = -1;  // Vanilla slot ID źródła

    public CustomInventoryScreen() {
        super(Text.literal("Ekwipunek"));
    }

    @Override
    protected void init() {
        super.init();

        int totalWidth = GRID_COLS * SLOT_SIZE;
        int totalHeight = GRID_ROWS * SLOT_SIZE;

        startX = (this.width - totalWidth) / 2;
        startY = (this.height - totalHeight) / 2;

        LOGGER.info("[CustomInventory] GUI zainicjalizowane");
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);

        // Tytuł
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                this.title,
                this.width / 2,
                startY - 20,
                0xFFFFFF
        );

        var player = MinecraftClient.getInstance().player;
        if (player == null) return;

        hoveredSlot = -1;
        int inventorySlotIndex = 0;

        // Renderuj grid
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int x = startX + col * SLOT_SIZE;
                int y = startY + row * SLOT_SIZE;

                char slotType = (char) LAYOUT[row][col];
                int gridSlot = row * GRID_COLS + col;

                boolean isHovered = mouseX >= x && mouseX < x + SLOT_SIZE &&
                        mouseY >= y && mouseY < y + SLOT_SIZE;

                ItemStack stack = ItemStack.EMPTY;
                int vanillaSlot = -1;

                switch (slotType) {
                    case '#':  // Armor
                        int armorSlot = 3 - row;
                        vanillaSlot = 36 + armorSlot;
                        stack = player.getInventory().getArmorStack(armorSlot);

                        // Jeśli cursor ma item z tego slotu, pokaż pusty
                        if (!cursorStack.isEmpty() && cursorSourceSlot == vanillaSlot) {
                            stack = ItemStack.EMPTY;
                        }

                        renderSlot(context, x, y, stack, isHovered, getArmorLabel(row));
                        if (isHovered) hoveredSlot = gridSlot;
                        break;

                    case '1':  // Inventory
                        if (inventorySlotIndex < INVENTORY_SLOTS.length) {
                            vanillaSlot = INVENTORY_SLOTS[inventorySlotIndex];
                            stack = player. getInventory().getStack(vanillaSlot);

                            if (!cursorStack.isEmpty() && cursorSourceSlot == vanillaSlot) {
                                stack = ItemStack.EMPTY;
                            }

                            renderSlot(context, x, y, stack, isHovered, null);
                            if (isHovered) hoveredSlot = gridSlot;
                            inventorySlotIndex++;
                        }
                        break;

                    case '2':  // Offhand
                        vanillaSlot = 40;
                        stack = player. getOffHandStack();

                        if (!cursorStack.isEmpty() && cursorSourceSlot == vanillaSlot) {
                            stack = ItemStack. EMPTY;
                        }

                        renderSlot(context, x, y, stack, isHovered, "Off");
                        if (isHovered) hoveredSlot = gridSlot;
                        break;

                    case '3':  // Main hand
                        vanillaSlot = 0;
                        stack = player. getInventory().getStack(0);

                        if (!cursorStack.isEmpty() && cursorSourceSlot == vanillaSlot) {
                            stack = ItemStack.EMPTY;
                        }

                        renderSlot(context, x, y, stack, isHovered, "Hand");
                        if (isHovered) hoveredSlot = gridSlot;
                        break;
                }
            }
        }

        // Tooltip
        if (cursorStack.isEmpty() && hoveredSlot >= 0) {
            ItemStack hoveredStack = getStackAtSlot(player, hoveredSlot);
            if (!hoveredStack.isEmpty()) {
                context.drawItemTooltip(this.textRenderer, hoveredStack, mouseX, mouseY);
            }
        }

        // Rysuj cursor (slot -1 wirtualnie)
        if (!cursorStack.isEmpty()) {
            context.drawItem(cursorStack, mouseX - 8, mouseY - 8);
            context.drawItemInSlot(this.textRenderer, cursorStack, mouseX - 8, mouseY - 8);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderSlot(DrawContext context, int x, int y, ItemStack stack, boolean hovered, String label) {
        context.fill(x, y, x + 16, y + 16, 0xFF8B8B8B);
        context.fill(x + 1, y + 1, x + 15, y + 15, 0xFF373737);

        if (hovered) {
            context.fill(x, y, x + 16, y + 16, 0x80FFFFFF);
        }

        if (!stack.isEmpty()) {
            context.drawItem(stack, x, y);
            context.drawItemInSlot(this.textRenderer, stack, x, y);
        }

        if (label != null && stack.isEmpty()) {
            int textWidth = this.textRenderer.getWidth(label);
            int textX = x + (16 - textWidth) / 2;
            context.drawText(this.textRenderer, Text.literal(label), textX, y + 4, 0x404040, false);
        }
    }

    private int getArmorSlot(int row) {
        return 3 - row;
    }

    private String getArmorLabel(int row) {
        return switch (row) {
            case 0 -> "Head";
            case 1 -> "Chest";
            case 2 -> "Legs";
            case 3 -> "Boots";
            default -> "";
        };
    }

    private ItemStack getStackAtSlot(net.minecraft.entity.player.PlayerEntity player, int gridSlot) {
        int vanillaSlot = getVanillaSlotId(gridSlot);
        if (vanillaSlot < 0) return ItemStack.EMPTY;

        // Jeśli cursor ma item z tego slotu, zwróć pusty
        if (! cursorStack.isEmpty() && cursorSourceSlot == vanillaSlot) {
            return ItemStack.EMPTY;
        }

        return player.getInventory().getStack(vanillaSlot);
    }

    private int getVanillaSlotId(int gridSlot) {
        int row = gridSlot / GRID_COLS;
        int col = gridSlot % GRID_COLS;

        if (row < 0 || row >= GRID_ROWS || col < 0 || col >= GRID_COLS) {
            return -1;
        }

        char slotType = (char) LAYOUT[row][col];

        switch (slotType) {
            case '#':
                return 36 + (3 - row);

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
                return 40;

            case '3':
                return 0;
        }

        return -1;
    }

    private boolean canPlaceInSlot(int vanillaSlot, ItemStack stack) {
        if (stack.isEmpty()) return true;

        // Armor slots (36-39)
        if (vanillaSlot >= 36 && vanillaSlot <= 39) {
            var equipmentSlot = net.minecraft.entity.mob.MobEntity.getPreferredEquipmentSlot(stack);

            return switch (vanillaSlot) {
                case 36 -> equipmentSlot == net.minecraft.entity. EquipmentSlot.FEET;
                case 37 -> equipmentSlot == net.minecraft.entity.EquipmentSlot.LEGS;
                case 38 -> equipmentSlot == net.minecraft.entity. EquipmentSlot.CHEST;
                case 39 -> equipmentSlot == net.minecraft.entity.EquipmentSlot.HEAD;
                default -> false;
            };
        }

        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        var player = MinecraftClient.getInstance().player;
        if (player == null) return super.mouseClicked(mouseX, mouseY, button);

        if (hoveredSlot < 0) return super.mouseClicked(mouseX, mouseY, button);

        int vanillaSlot = getVanillaSlotId(hoveredSlot);
        if (vanillaSlot < 0) return super.mouseClicked(mouseX, mouseY, button);

        if (button == 0) {  // LEFT CLICK
            return handleLeftClick(player, vanillaSlot);
        } else if (button == 1) {  // RIGHT CLICK
            return handleRightClick(player, vanillaSlot);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleLeftClick(net.minecraft.entity.player.PlayerEntity player, int vanillaSlot) {
        ItemStack clickedStack = player.getInventory().getStack(vanillaSlot);

        // SCENARIUSZ 1: Cursor pusty + klikamy item = PICKUP
        if (cursorStack.isEmpty() && !clickedStack.isEmpty()) {
            cursorStack = clickedStack.copy();
            cursorSourceSlot = vanillaSlot;

            sendClickPacket(vanillaSlot, 0, cursorSourceSlot, 0);  // action=0 (PICKUP)
            LOGGER.info("[CustomInventory] PICKUP: slot={}, item={}", vanillaSlot, cursorStack.getItem().getName().getString());
            return true;
        }

        // SCENARIUSZ 2: Cursor ma item + klikamy pusty slot = PLACE
        else if (! cursorStack.isEmpty() && clickedStack.isEmpty()) {
            if (! canPlaceInSlot(vanillaSlot, cursorStack)) {
                player.sendMessage(Text.literal("✗ Ten przedmiot nie pasuje tutaj!").styled(s -> s.withColor(0xFF5555)), true);
                return true;
            }

            sendClickPacket(vanillaSlot, 0, cursorSourceSlot, 1);  // action=1 (PLACE)
            LOGGER.info("[CustomInventory] PLACE: from={} to={}", cursorSourceSlot, vanillaSlot);

            cursorStack = ItemStack.EMPTY;
            cursorSourceSlot = -1;
            return true;
        }

        // SCENARIUSZ 3: Cursor ma item + klikamy TEN SAM TYP itemu = STACK (NOWY!)
        else if (!cursorStack.isEmpty() && !clickedStack.isEmpty() && vanillaSlot != cursorSourceSlot && ItemStack.areItemsEqual(cursorStack, clickedStack)) {
            int spaceAvailable = clickedStack.getMaxCount() - clickedStack.getCount();

            if (spaceAvailable > 0) {
                // Można dodać do stacka
                sendClickPacket(vanillaSlot, 0, cursorSourceSlot, 6);  // action=6 (STACK)
                LOGGER. info("[CustomInventory] STACK: {} -> {} (space={})", cursorSourceSlot, vanillaSlot, spaceAvailable);

                int amountToAdd = Math.min(cursorStack.getCount(), spaceAvailable);

                // Client-side preview
                cursorStack.decrement(amountToAdd);
                if (cursorStack.isEmpty()) {
                    cursorStack = ItemStack.EMPTY;
                    cursorSourceSlot = -1;
                }

                return true;
            } else {
                // Stack pełny - swap
                if (!canPlaceInSlot(vanillaSlot, cursorStack)) {
                    player.sendMessage(Text.literal("✗ Ten przedmiot nie pasuje tutaj! ").styled(s -> s.withColor(0xFF5555)), true);
                    return true;
                }

                sendClickPacket(vanillaSlot, 0, cursorSourceSlot, 2);  // action=2 (SWAP)
                LOGGER. info("[CustomInventory] SWAP (stack full): {} <-> {}", cursorSourceSlot, vanillaSlot);

                cursorStack = clickedStack.copy();
                cursorSourceSlot = vanillaSlot;
                return true;
            }
        }

        // SCENARIUSZ 4: Cursor ma item + klikamy inny item (inny typ) = SWAP
        else if (!cursorStack.isEmpty() && !clickedStack.isEmpty() && vanillaSlot != cursorSourceSlot) {
            if (!canPlaceInSlot(vanillaSlot, cursorStack)) {
                player.sendMessage(Text.literal("✗ Ten przedmiot nie pasuje tutaj!").styled(s -> s.withColor(0xFF5555)), true);
                return true;
            }

            sendClickPacket(vanillaSlot, 0, cursorSourceSlot, 2);  // action=2 (SWAP)
            LOGGER.info("[CustomInventory] SWAP:  {} <-> {}", cursorSourceSlot, vanillaSlot);

            cursorStack = clickedStack.copy();
            cursorSourceSlot = vanillaSlot;
            return true;
        }

        // SCENARIUSZ 5: Cursor ma item + klikamy TEN SAM slot = RETURN
        else if (!cursorStack.isEmpty() && vanillaSlot == cursorSourceSlot) {
            sendClickPacket(vanillaSlot, 0, cursorSourceSlot, 5);  // action=5 (RETURN)
            LOGGER.info("[CustomInventory] RETURN: slot={}", vanillaSlot);

            cursorStack = ItemStack.EMPTY;
            cursorSourceSlot = -1;
            return true;
        }

        return false;
    }

    private boolean handleRightClick(net.minecraft.entity.player. PlayerEntity player, int vanillaSlot) {
        ItemStack clickedStack = player.getInventory().getStack(vanillaSlot);

        // SCENARIUSZ 1: Cursor pusty + PPM na item = SPLIT (weź połowę)
        if (cursorStack.isEmpty() && !clickedStack.isEmpty()) {
            int halfCount = (clickedStack.getCount() + 1) / 2;

            cursorStack = clickedStack.copy();
            cursorStack.setCount(halfCount);
            cursorSourceSlot = vanillaSlot;

            sendClickPacket(vanillaSlot, 1, -1, 3);  // action=3 (SPLIT)
            LOGGER. info("[CustomInventory] SPLIT: slot={}, half={}", vanillaSlot, halfCount);
            return true;
        }

        // SCENARIUSZ 2: Cursor ma item + PPM na pusty slot = DROP_ONE
        else if (!cursorStack.isEmpty() && clickedStack.isEmpty()) {
            if (!canPlaceInSlot(vanillaSlot, cursorStack)) {
                player.sendMessage(Text.literal("✗ Ten przedmiot nie pasuje tutaj!").styled(s -> s.withColor(0xFF5555)), true);
                return true;
            }

            sendClickPacket(vanillaSlot, 1, cursorSourceSlot, 4);  // action=4 (DROP_ONE)
            LOGGER. info("[CustomInventory] DROP_ONE: from={} to={}", cursorSourceSlot, vanillaSlot);

            cursorStack. decrement(1);
            if (cursorStack.isEmpty()) {
                cursorStack = ItemStack.EMPTY;
                cursorSourceSlot = -1;
            }
            return true;
        }

        // SCENARIUSZ 3: Cursor ma item + PPM na TEN SAM slot = RETURN
        else if (!cursorStack.isEmpty() && vanillaSlot == cursorSourceSlot) {
            sendClickPacket(vanillaSlot, 1, cursorSourceSlot, 5);  // action=5 (RETURN)
            LOGGER.info("[CustomInventory] RETURN (PPM): slot={}", vanillaSlot);

            cursorStack = ItemStack.EMPTY;
            cursorSourceSlot = -1;
            return true;
        }

        // SCENARIUSZ 4: Cursor ma item + PPM na inny item tego samego typu = DROP_ONE (dodaj)
        else if (!cursorStack.isEmpty() && !clickedStack.isEmpty() && ItemStack.areItemsEqual(cursorStack, clickedStack)) {
            if (clickedStack.getCount() < clickedStack.getMaxCount()) {
                sendClickPacket(vanillaSlot, 1, cursorSourceSlot, 4);  // action=4 (DROP_ONE)
                LOGGER.info("[CustomInventory] DROP_ONE (add): from={} to={}", cursorSourceSlot, vanillaSlot);

                cursorStack.decrement(1);
                if (cursorStack. isEmpty()) {
                    cursorStack = ItemStack.EMPTY;
                    cursorSourceSlot = -1;
                }
            }
            return true;
        }

        return false;
    }

    private void sendClickPacket(int slotIndex, int button, int cursorSlot, int actionType) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(slotIndex);
        buf.writeInt(button);
        buf.writeInt(cursorSlot);
        buf.writeInt(actionType);
        ClientPlayNetworking.send(SpecterCraft.SLOT_CLICK_PACKET_ID, buf);
    }

    @Override
    public void close() {
        if (! cursorStack.isEmpty()) {
            LOGGER.info("[CustomInventory] Zamykanie z itemem na kursorze - oddaję");
            sendClickPacket(cursorSourceSlot, 0, cursorSourceSlot, 5);  // RETURN
            cursorStack = ItemStack. EMPTY;
            cursorSourceSlot = -1;
        }

        super.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}