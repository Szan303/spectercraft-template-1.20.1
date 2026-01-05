package com.szan.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft. client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net. minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class RecipeSelectionScreen extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger("SpecterCraft/RecipeScreen");

    private static final int SLOT_SIZE = 64;
    private static final int SLOT_PADDING = 8;
    private static final int COLUMNS = 3;
    private static final int MAX_VISIBLE_ROWS = 3; // Maksymalnie 3 rzędy widoczne

    private final List<RecipeEntry> recipes;
    private int hoveredIndex = -1;
    private int scrollOffset = 0; // Offset scrollowania (w wierszach)
    private int maxScrollOffset = 0;

    public static class RecipeEntry {
        public final Identifier recipeId;
        public final ItemStack result;
        public final int count;
        public final List<ItemStack> ingredients;

        public RecipeEntry(Identifier recipeId, ItemStack result, int count, List<ItemStack> ingredients) {
            this.recipeId = recipeId;
            this.result = result;
            this.count = count;
            this.ingredients = ingredients;
        }
    }

    public RecipeSelectionScreen(List<RecipeEntry> recipes) {
        super(Text.literal("Wybierz recepturę"));
        this.recipes = recipes;

        // Oblicz max scroll
        int totalRows = (int) Math.ceil((double) recipes.size() / COLUMNS);
        this.maxScrollOffset = Math.max(0, totalRows - MAX_VISIBLE_ROWS);

        LOGGER.info("[RecipeScreen] Utworzono screen z {} recepturami (max scroll: {})",
                recipes. size(), maxScrollOffset);
    }

    @Override
    protected void init() {
        super.init();
        LOGGER.info("[RecipeScreen] Screen zainicjalizowany");
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Tło
        renderBackground(context);

        // Tytuł
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                this.title,
                this.width / 2,
                20,
                0xFFFFFF
        );

        // Oblicz layout
        int totalWidth = COLUMNS * (SLOT_SIZE + SLOT_PADDING) - SLOT_PADDING;
        int startX = (this.width - totalWidth) / 2;
        int startY = 50;

        hoveredIndex = -1;

        // Oblicz które receptury są widoczne
        int startIndex = scrollOffset * COLUMNS;
        int endIndex = Math.min(startIndex + (MAX_VISIBLE_ROWS * COLUMNS), recipes.size());

        // Renderuj tylko widoczne sloty
        for (int i = startIndex; i < endIndex; i++) {
            RecipeEntry recipe = recipes.get(i);

            int relativeIndex = i - startIndex;
            int col = relativeIndex % COLUMNS;
            int row = relativeIndex / COLUMNS;

            int slotX = startX + col * (SLOT_SIZE + SLOT_PADDING);
            int slotY = startY + row * (SLOT_SIZE + SLOT_PADDING + 20);

            // Sprawdź hover
            boolean isHovered = mouseX >= slotX && mouseX < slotX + SLOT_SIZE &&
                    mouseY >= slotY && mouseY < slotY + SLOT_SIZE;

            if (isHovered) {
                hoveredIndex = i;
            }

            // Render slot
            renderRecipeSlot(context, recipe, slotX, slotY, isHovered);
        }

        // Render tooltip jeśli hover
        if (hoveredIndex >= 0 && hoveredIndex < recipes.size()) {
            renderTooltip(context, recipes. get(hoveredIndex), mouseX, mouseY);
        }

        // Scroll indicator (jeśli potrzebny)
        if (maxScrollOffset > 0) {
            String scrollText = String.format("(%d/%d)", scrollOffset + 1, maxScrollOffset + 1);
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    Text.literal(scrollText).styled(s -> s.withColor(0xAAAAAA)),
                    this.width / 2,
                    this.height - 50,
                    0xAAAAAA
            );

            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    Text.literal("Scroll = Przewijanie").styled(s -> s.withColor(0xAAAAAA)),
                    this.width / 2,
                    this.height - 40,
                    0xAAAAAA
            );
        }

        // Instrukcja
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("ESC = Anuluj").styled(s -> s.withColor(0xAAAAAA)),
                this.width / 2,
                this.height - 20,
                0xAAAAAA
        );

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderRecipeSlot(DrawContext context, RecipeEntry recipe, int x, int y, boolean hovered) {
        // Tło slotu
        int bgColor = hovered ? 0x80FFFFFF : 0x80000000;
        context.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, bgColor);

        // Border
        int borderColor = hovered ? 0xFFFFFFFF : 0xFF8B8B8B;
        context.drawBorder(x, y, SLOT_SIZE, SLOT_SIZE, borderColor);

        // Item icon (centered, 32x32)
        int itemSize = 32;
        int itemX = x + (SLOT_SIZE - itemSize) / 2;
        int itemY = y + (SLOT_SIZE - itemSize) / 2 - 8;

        context.drawItem(recipe.result, itemX, itemY);

        // Count badge
        if (recipe.count > 1) {
            String countText = "x" + recipe.count;
            int textWidth = this.textRenderer.getWidth(countText);
            int badgeX = x + SLOT_SIZE - textWidth - 4;
            int badgeY = y + SLOT_SIZE - 16;

            context.fill(badgeX - 2, badgeY - 2, badgeX + textWidth + 2, badgeY + 10, 0xAA000000);
            context.drawText(this.textRenderer, countText, badgeX, badgeY, 0xFFFFFF, true);
        }

        // Item name
        Text itemName = recipe.result.getName();
        int nameWidth = this.textRenderer.getWidth(itemName);
        int nameX = x + (SLOT_SIZE - nameWidth) / 2;
        int nameY = y + SLOT_SIZE + 4;

        context.drawText(
                this.textRenderer,
                itemName,
                nameX,
                nameY,
                hovered ? 0xFFFF55 : 0xFFFFFF,
                true
        );
    }

    private void renderTooltip(DrawContext context, RecipeEntry recipe, int mouseX, int mouseY) {
        List<Text> tooltip = new ArrayList<>();

        tooltip.add(
                recipe.result.getName().copy()
                        .append(Text.literal(" x" + recipe.count).styled(s -> s.withColor(0xAAAAAA)))
        );

        tooltip.add(Text.literal(""));
        tooltip.add(Text. literal("Wymaga: ").styled(s -> s.withColor(0xAAAAAA)));

        for (ItemStack ingredient : recipe.ingredients) {
            tooltip. add(
                    Text.literal("  • ").styled(s -> s.withColor(0x555555))
                            .append(ingredient.getName())
                            .append(Text. literal(" x" + ingredient.getCount()).styled(s -> s.withColor(0xAAAAAA)))
            );
        }

        context.drawTooltip(this.textRenderer, tooltip, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && hoveredIndex >= 0 && hoveredIndex < recipes.size()) {
            RecipeEntry selected = recipes.get(hoveredIndex);

            LOGGER.info("[RecipeScreen] Wybrano recepturę: {}", selected.recipeId);

            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeIdentifier(selected.recipeId);

            ClientPlayNetworking.send(com.szan. SpecterCraft.RECIPE_SELECTED_PACKET_ID, buf);

            this.close();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (maxScrollOffset > 0) {
            if (amount > 0) {
                // Scroll w górę
                scrollOffset = Math.max(0, scrollOffset - 1);
            } else if (amount < 0) {
                // Scroll w dół
                scrollOffset = Math.min(maxScrollOffset, scrollOffset + 1);
            }

            LOGGER.debug("[RecipeScreen] Scroll offset: {}", scrollOffset);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        LOGGER.info("[RecipeScreen] Screen zamknięty");
        super.close();
    }
}