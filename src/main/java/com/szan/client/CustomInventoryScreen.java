package com.szan.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft. client.gui.screen.Screen;
import net.minecraft.text. Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomInventoryScreen extends Screen {
    private static final Logger LOGGER = LoggerFactory. getLogger("SpecterCraft/CustomInventory");

    public CustomInventoryScreen() {
        super(Text.literal("Ekwipunek"));
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);

        // Tytuł
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text. literal("Twój Ekwipunek"),
                this.width / 2,
                20,
                0xFFFFFF
        );

        // Placeholder
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text. literal("(tutaj będzie zawartość)"),
                this.width / 2,
                this.height / 2,
                0xAAAAAA
        );

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true; // zamykaj też na ESC!
    }

    @Override
    public void close() {
        super.close();
    }
}