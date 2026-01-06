package com.szan.client;

import com.szan.client.screen.RecipeSelectionScreen;
import com.szan.registry.ModPackets;
import net.fabricmc.fabric.api. client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft. item.ItemStack;
import net.minecraft. util. Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ClientNetworking {
    private static final Logger LOGGER = LoggerFactory.getLogger("SpecterCraft/ClientNetworking");

    public static void register() {
        LOGGER.info("Rejestrowanie client packet handlers...");

        // RECIPE_LIST packet (serwer → klient)
        ClientPlayNetworking.registerGlobalReceiver(
                ModPackets.RECIPE_LIST,
                (client, handler, buf, responseSender) -> {
                    int count = buf.readInt();
                    List<RecipeSelectionScreen.RecipeEntry> recipes = new ArrayList<>();

                    LOGGER.info("[ClientNetworking] Otrzymano {} receptur z serwera", count);

                    for (int i = 0; i < count; i++) {
                        Identifier recipeId = buf.readIdentifier();
                        ItemStack result = buf.readItemStack();
                        int totalCount = buf.readInt();

                        int ingredientCount = buf.readInt();
                        List<ItemStack> ingredients = new ArrayList<>();
                        for (int j = 0; j < ingredientCount; j++) {
                            ingredients. add(buf.readItemStack());
                        }

                        recipes.add(new RecipeSelectionScreen.RecipeEntry(
                                recipeId,
                                result,
                                totalCount,
                                ingredients
                        ));
                    }

                    client.execute(() -> {
                        client.setScreen(new RecipeSelectionScreen(recipes));
                        LOGGER.info("[ClientNetworking] Otwarto RecipeSelectionScreen");
                    });
                }
        );
    }
}