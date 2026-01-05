package com.szan.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.szan.SpecterCraft;
import com.szan.handler. CustomScreenHandler;
import net. minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft. client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util. Identifier;
import org.slf4j.Logger;
import org. slf4j.LoggerFactory;

public class CustomInventoryHandledScreen extends HandledScreen<CustomScreenHandler> {
    private static final Logger LOGGER = LoggerFactory. getLogger("SpecterCraft/CustomInventory");

    // Vanilla texture jako fallback
    private static final Identifier TEXTURE = new Identifier("minecraft", "textures/gui/container/generic_54.png");

    public CustomInventoryHandledScreen(CustomScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        // Rozmiar GUI
        this.backgroundWidth = 176;
        this.backgroundHeight = 166;

        this.playerInventoryTitleY = 9999;  // Ukryj "Inventory" label (poza ekranem)
    }

    @Override
    protected void init() {
        super.init();

        // Wycentruj tytuł
        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;
        this.titleY = 6;

        LOGGER. info("[CustomInventory] Zainicjalizowano GUI");
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this. height - this.backgroundHeight) / 2;

        // Proste tło (bez textury)
        // Ciemne tło
        context.fill(x, y, x + this. backgroundWidth, y + this.backgroundHeight, 0xFFC6C6C6);

        // Border
        context.fill(x, y, x + this.backgroundWidth, y + 1, 0xFF8B8B8B); // Top
        context.fill(x, y, x + 1, y + this.backgroundHeight, 0xFF8B8B8B); // Left
        context. fill(x, y + this. backgroundHeight - 1, x + this.backgroundWidth, y + this.backgroundHeight, 0xFF373737); // Bottom
        context. fill(x + this.backgroundWidth - 1, y, x + this.backgroundWidth, y + this.backgroundHeight, 0xFF373737); // Right

        // Rysuj tła slotów
        drawSlotBackgrounds(context, x, y);
    }

    /**
     * Rysuj tła slotów
     */
    private void drawSlotBackgrounds(DrawContext context, int guiX, int guiY) {
        for (int i = 0; i < this.handler.slots.size(); i++) {
            var slot = this.handler.slots.get(i);
            int slotX = guiX + slot.x;
            int slotY = guiY + slot.y;

            // Tło slotu (vanilla-like)
            context.fill(slotX, slotY, slotX + 16, slotY + 16, 0xFF8B8B8B);
            context.fill(slotX + 1, slotY + 1, slotX + 15, slotY + 15, 0xFF373737);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Ciemne tło za GUI
        this.renderBackground(context);

        // Renderuj GUI + sloty + itemy
        super.render(context, mouseX, mouseY, delta);

        // Tooltips
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // Tytuł
        context. drawText(this.textRenderer, this.title, this.titleX, this.titleY, 0x404040, false);

        // Text instruction = Text.literal("Przeciągnij itemki | E = Zamknij");
        // context.drawText(... );
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}