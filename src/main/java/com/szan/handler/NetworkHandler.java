package com.szan. handler;

import com.szan. SpecterCraft;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft. item.ItemStack;
import net.minecraft. network.PacketByteBuf;
import net.minecraft.recipe. Ingredient;
import net.minecraft. server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("SpecterCraft/Network");

    // Przechowuje sesje craftingu dla menu wyboru
    private static final Map<ServerPlayerEntity, CraftingMenuSession> menuSessions = new HashMap<>();

    private static class CraftingMenuSession {
        List<ItemEntity> itemEntities;
        List<CraftingHelper. RecipeOption> recipeOptions;

        CraftingMenuSession(List<ItemEntity> entities, List<CraftingHelper. RecipeOption> options) {
            this.itemEntities = entities;
            this.recipeOptions = options;
        }
    }

    public static void register() {
        LOGGER.info("Rejestrowanie NetworkHandler...");

        // PICKUP/CRAFT packet (klient → serwer)
        ServerPlayNetworking.registerGlobalReceiver(
                SpecterCraft.PICKUP_PACKET_ID,
                (server, player, handler, buf, responseSender) -> {
                    int entityId = buf.readInt();
                    boolean isCrafting = buf.readBoolean();

                    server.execute(() -> {
                        World world = player.getServerWorld();
                        Entity entity = world.getEntityById(entityId);

                        if (entity instanceof ItemEntity itemEntity) {
                            if (isCrafting) {
                                LOGGER.info("[Network] Gracz {} próbuje craftować", player.getName().getString());
                                handleCraftRequest(player, itemEntity);
                            } else {
                                handlePickup(player, itemEntity);
                            }

                            player.currentScreenHandler.sendContentUpdates();
                            player.playerScreenHandler.onContentChanged(player.getInventory());
                        }
                    });
                }
        );

        // RECIPE_SELECTED packet (klient → serwer)
        ServerPlayNetworking.registerGlobalReceiver(
                SpecterCraft.RECIPE_SELECTED_PACKET_ID,
                (server, player, handler, buf, responseSender) -> {
                    Identifier recipeId = buf.readIdentifier();

                    server.execute(() -> {
                        LOGGER.info("[Network] Gracz {} wybrał recepturę: {}", player.getName().getString(), recipeId);

                        CraftingMenuSession session = menuSessions.get(player);

                        if (session == null) {
                            LOGGER.warn("[Network] Brak sesji dla gracza {}", player.getName().getString());
                            return;
                        }

                        // Znajdź wybraną recepturę
                        CraftingHelper.RecipeOption selectedOption = null;
                        for (CraftingHelper.RecipeOption option : session.recipeOptions) {
                            if (option.recipe.getId().equals(recipeId)) {
                                selectedOption = option;
                                break;
                            }
                        }

                        if (selectedOption == null) {
                            LOGGER.warn("[Network] Nie znaleziono receptury {}", recipeId);
                            player.sendMessage(Text.literal("✗ Błąd: receptura nie istnieje"), true);
                            return;
                        }

                        // Wykonaj multi-craft
                        CraftingHelper.performMultiCraftDirect(player, session. itemEntities, selectedOption);

                        // Usuń sesję
                        menuSessions.remove(player);

                        LOGGER.info("[Network] Crafting ukończony dla gracza {}", player.getName().getString());
                    });
                }
        );

        LOGGER.info("✓ NetworkHandler zarejestrowany!");
    }

    /**
     * Obsługuje żądanie craftingu (może otworzyć menu)
     */
    private static void handleCraftRequest(ServerPlayerEntity player, ItemEntity itemEntity) {
        boolean success = CraftingHelper.attemptCraft(player, itemEntity);

        // Jeśli CraftingHelper zwrócił false i ma wiele receptur, otwórz menu
        // (To zostanie obsłużone przez callback)
    }

    /**
     * Wysyła listę receptur do klienta (otwiera menu)
     */
    public static void sendRecipeList(ServerPlayerEntity player, List<ItemEntity> itemEntities, List<CraftingHelper.RecipeOption> options) {
        LOGGER.info("[Network] Wysyłanie {} receptur do gracza {}", options.size(), player.getName().getString());

        // Zapisz sesję
        menuSessions.put(player, new CraftingMenuSession(itemEntities, options));

        // Stwórz packet
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        // Liczba receptur
        buf.writeInt(options.size());

        for (CraftingHelper.RecipeOption option : options) {
            // Recipe ID
            buf.writeIdentifier(option.recipe.getId());

            // Wynik (item + total count)
            buf.writeItemStack(option.totalResult);
            buf.writeInt(option.totalResult.getCount());

            // Ingredienty
            DefaultedList<Ingredient> ingredients = option.recipe.getIngredients();
            List<ItemStack> ingredientStacks = new ArrayList<>();

            for (Ingredient ingredient : ingredients) {
                if (! ingredient.isEmpty()) {
                    ItemStack[] matching = ingredient.getMatchingStacks();
                    if (matching. length > 0) {
                        ingredientStacks.add(matching[0]. copy());
                    }
                }
            }

            buf.writeInt(ingredientStacks.size());
            for (ItemStack stack : ingredientStacks) {
                buf.writeItemStack(stack);
            }

            LOGGER.debug("[Network] Dodano recepturę: {} x{}",
                    option.totalResult.getItem().getName().getString(),
                    option. totalResult.getCount());
        }

        // Wyślij packet
        ServerPlayNetworking.send(player, SpecterCraft.RECIPE_LIST_PACKET_ID, buf);

        LOGGER.info("[Network] Packet RECIPE_LIST wysłany!");
    }

    /**
     * Obsługuje podnoszenie itemów
     */
    private static void handlePickup(ServerPlayerEntity player, ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getStack();
        ItemStack handStack = player.getInventory().getStack(0);

        if (handStack.isEmpty()) {
            player.getInventory().setStack(0, itemStack. copy());
            itemEntity.discard();
            LOGGER.info("[Network] ✓ Item podniesiony!");
        }
        else if (ItemStack.canCombine(handStack, itemStack)) {
            int totalCount = handStack.getCount() + itemStack.getCount();
            int maxCount = handStack.getMaxCount();

            if (totalCount <= maxCount) {
                handStack.setCount(totalCount);
                itemEntity.discard();
                LOGGER.info("[Network] ✓ Dodano do stacka!");
            } else {
                int canAdd = maxCount - handStack.getCount();
                handStack.setCount(maxCount);
                itemStack.setCount(itemStack.getCount() - canAdd);
                LOGGER.info("[Network] ⚠ Stack pełny!");
            }
        }
        else {
            player.sendMessage(
                    Text.literal("✗ Najpierw upuść:  ")
                            .styled(style -> style.withColor(0xFF5555))
                            .append(handStack.getName().copy().styled(style -> style.withColor(0xFFAA00))),
                    true
            );
            LOGGER.info("[Network] ✗ Slot zajęty!");
        }
    }
}