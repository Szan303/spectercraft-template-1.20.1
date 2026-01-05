package com.szan.client;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net. minecraft.client.gui.DrawContext;
import net.minecraft. client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net. minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecipeSelectionScreen extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger("SpecterCraft/RecipeScreen");

    private static final int SLOT_SIZE = 18; // Vanilla slot size (16x16 + 2px border)
    private static final int SLOT_PADDING = 4;
    private static final int COLUMNS = 9; // Więcej kolumn bo są mniejsze
    private static final int MAX_VISIBLE_ROWS = 6; // Więcej rzędów

    private final List<RecipeEntry> recipes;
    private int hoveredIndex = -1;
    private int scrollOffset = 0;
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
        renderBackground(context);

        // Tytuł
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                this.title,
                this.width / 2,
                10,
                0xFFFFFF
        );

        // Oblicz layout
        int totalWidth = COLUMNS * (SLOT_SIZE + SLOT_PADDING) - SLOT_PADDING;
        int startX = (this.width - totalWidth) / 2;
        int startY = 30;

        hoveredIndex = -1;

        int startIndex = scrollOffset * COLUMNS;
        int endIndex = Math.min(startIndex + (MAX_VISIBLE_ROWS * COLUMNS), recipes.size());

        // Renderuj sloty
        for (int i = startIndex; i < endIndex; i++) {
            RecipeEntry recipe = recipes.get(i);

            int relativeIndex = i - startIndex;
            int col = relativeIndex % COLUMNS;
            int row = relativeIndex / COLUMNS;

            int slotX = startX + col * (SLOT_SIZE + SLOT_PADDING);
            int slotY = startY + row * (SLOT_SIZE + SLOT_PADDING);

            boolean isHovered = mouseX >= slotX && mouseX < slotX + SLOT_SIZE &&
                    mouseY >= slotY && mouseY < slotY + SLOT_SIZE;

            if (isHovered) {
                hoveredIndex = i;
            }

            renderRecipeSlot(context, recipe, slotX, slotY, isHovered);
        }

        // Tooltip
        if (hoveredIndex >= 0 && hoveredIndex < recipes.size()) {
            renderTooltip(context, recipes. get(hoveredIndex), mouseX, mouseY);
        }

        // Scroll indicator
        if (maxScrollOffset > 0) {
            String scrollText = String.format("(%d/%d)", scrollOffset + 1, maxScrollOffset + 1);
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    Text.literal(scrollText).styled(s -> s.withColor(0xAAAAAA)),
                    this.width / 2,
                    this. height - 30,
                    0xAAAAAA
            );
        }

        // Instrukcja
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("ESC = Anuluj").styled(s -> s.withColor(0xAAAAAA)),
                this.width / 2,
                this.height - 15,
                0xAAAAAA
        );

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderRecipeSlot(DrawContext context, RecipeEntry recipe, int x, int y, boolean hovered) {
        // Tło slotu (vanilla-like)
        int bgColor = hovered ? 0xAA808080 : 0xAA3A3A3A;
        context.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, bgColor);

        // Border (vanilla-like)
        int borderColorLight = hovered ? 0xFFFFFFFF : 0xFF8B8B8B;
        int borderColorDark = hovered ? 0xFFAAAAAA : 0xFF373737;

        // Światło (góra, lewo)
        context.fill(x, y, x + SLOT_SIZE, y + 1, borderColorLight);
        context.fill(x, y, x + 1, y + SLOT_SIZE, borderColorLight);

        // Cień (dół, prawo)
        context.fill(x, y + SLOT_SIZE - 1, x + SLOT_SIZE, y + SLOT_SIZE, borderColorDark);
        context.fill(x + SLOT_SIZE - 1, y, x + SLOT_SIZE, y + SLOT_SIZE, borderColorDark);

        // Item icon (16x16, centered w slocie 18x18)
        int itemX = x + 1;
        int itemY = y + 1;

        context.drawItem(recipe.result, itemX, itemY);

        // Item count overlay (vanilla-style)
        if (recipe.count > 1) {
            context.drawItemInSlot(this.textRenderer, recipe.result, itemX, itemY, String.valueOf(recipe.count));
        }
    }

    private void renderTooltip(DrawContext context, RecipeEntry recipe, int mouseX, int mouseY) {
        List<Text> tooltip = new ArrayList<>();

        // Nazwa + count
        Text recipeName = recipe.result.getName().copy();
        if (recipe.count > 1) {
            recipeName = recipeName.copy()
                    .append(Text.literal(" x" + recipe.count).styled(s -> s.withColor(0xAAAAAA)));
        }
        tooltip.add(recipeName);

        tooltip.add(Text.literal(""));
        tooltip.add(Text. literal("Wymaga: ").styled(s -> s.withColor(0xAAAAAA)));

        // Grupuj identyczne ingredienty
        Map<String, Integer> ingredientCounts = new java.util.HashMap<>();
        Map<String, ItemStack> ingredientStacks = new java.util.HashMap<>();

        for (ItemStack ingredient :  recipe.ingredients) {
            String key = ingredient.getItem().toString();
            ingredientCounts.put(key, ingredientCounts.getOrDefault(key, 0) + 1);
            ingredientStacks.putIfAbsent(key, ingredient);
        }

        for (Map.Entry<String, Integer> entry : ingredientCounts. entrySet()) {
            ItemStack stack = ingredientStacks.get(entry.getKey());
            int count = entry.getValue();

            tooltip.add(
                    Text.literal("  • ").styled(s -> s.withColor(0x555555))
                            .append(stack.getName())
                            .append(Text. literal(" x" + count).styled(s -> s.withColor(0xAAAAAA)))
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
                scrollOffset = Math.max(0, scrollOffset - 1);
            } else if (amount < 0) {
                scrollOffset = Math.min(maxScrollOffset, scrollOffset + 1);
            }

            LOGGER.debug("[RecipeScreen] Scroll offset:  {}", scrollOffset);
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