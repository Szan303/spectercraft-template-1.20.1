package com.szan.handler;

import net.minecraft.entity.ItemEntity;
import net.minecraft. entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft. recipe.CraftingRecipe;
import net.minecraft.recipe. Ingredient;
import net.minecraft. recipe.RecipeType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CraftingHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger("SpecterCraft/Crafting");
    private static final double SEARCH_RADIUS = 1.5;

    private static final Map<UUID, CraftingSession> activeSessions = new HashMap<>();
    private static final long SESSION_TIMEOUT_MS = 3000;

    /**
     * Sesja craftingu - pamięta ostatni klik gracza
     */
    private static class CraftingSession {
        long timestamp;
        List<ItemStack> availableStacks;
        List<ItemEntity> itemEntities;
        List<RecipeOption> recipeOptions;

        CraftingSession(long timestamp, List<ItemStack> stacks, List<ItemEntity> entities, List<RecipeOption> options) {
            this.timestamp = timestamp;
            this.availableStacks = stacks;
            this.itemEntities = entities;
            this.recipeOptions = options;
        }

        boolean isExpired() {
            return System. currentTimeMillis() - timestamp > SESSION_TIMEOUT_MS;
        }
    }

    /**
     * Opcja receptury w menu
     */
    public static class RecipeOption {
        public final CraftingRecipe recipe;
        public final int maxCrafts;
        public final ItemStack totalResult;

        public RecipeOption(CraftingRecipe recipe, int maxCrafts, World world) {
            this.recipe = recipe;
            this.maxCrafts = maxCrafts;

            ItemStack singleResult = recipe.getOutput(world.getRegistryManager());
            this.totalResult = singleResult.copy();
            this.totalResult.setCount(singleResult.getCount() * maxCrafts);
        }
    }

    /**
     * GŁÓWNA METODA - wywołana gdy gracz robi SHIFT + klik
     */
    public static boolean attemptCraft(PlayerEntity player, ItemEntity clickedItem) {
        World world = player.getWorld();
        UUID playerId = player.getUuid();
        long now = System.currentTimeMillis();

        // 1. ZBIERZ ITEMY W PROMIENIU
        Vec3d pos = clickedItem.getPos();
        Box searchArea = new Box(
                pos.x - SEARCH_RADIUS, pos.y - 1.0, pos.z - SEARCH_RADIUS,
                pos.x + SEARCH_RADIUS, pos.y + 1.0, pos.z + SEARCH_RADIUS
        );

        List<ItemEntity> nearbyItems = world.getEntitiesByClass(
                ItemEntity.class,
                searchArea,
                e -> ! e.isRemoved()
        );

        // 2. STWÓRZ LISTĘ DOSTĘPNYCH ITEMÓW (RĘKA + ZIEMIA)
        List<ItemStack> availableStacks = new ArrayList<>();
        ItemStack handStack = player.getInventory().getStack(0);

        if (! handStack.isEmpty()) {
            availableStacks.add(handStack. copy());
        }

        for (ItemEntity entity : nearbyItems) {
            availableStacks.add(entity.getStack().copy());
        }

        if (availableStacks.isEmpty()) {
            player.sendMessage(
                    Text.literal("✗ Brak itemów do craftowania")
                            .styled(s -> s.withColor(0xFF5555)),
                    true
            );
            return false;
        }

        // DEBUG:  Co mamy?
        LOGGER.info("[Crafting] Znaleziono {} stacków:", availableStacks.size());
        for (ItemStack stack : availableStacks) {
            LOGGER.info("  - {} x{}", stack.getItem().getName().getString(), stack.getCount());
        }

        // 3. ZNAJDŹ WSZYSTKIE MOŻLIWE RECEPTURY
        List<RecipeOption> recipeOptions = findAllRecipes(availableStacks, world);

        if (recipeOptions.isEmpty()) {
            player.sendMessage(
                    Text.literal("✗ Nie znaleziono receptury")
                            .styled(s -> s.withColor(0xFF5555)),
                    true
            );
            activeSessions.remove(playerId);
            return false;
        }

        LOGGER.info("[Crafting] Znaleziono {} receptur", recipeOptions.size());

        // 4. SPRAWDŹ CZY TO DOUBLE-CLICK (drugi klik w 3s)
        CraftingSession session = activeSessions.get(playerId);

        if (session != null && ! session.isExpired()) {
            // ===== DOUBLE-CLICK =====
            LOGGER.info("[Crafting] Double-click wykryty!");

            if (session.recipeOptions.size() == 1) {
                // Jedna receptura - multi-craft
                RecipeOption option = session.recipeOptions.get(0);
                performMultiCraft(player, session. itemEntities, option);
                activeSessions.remove(playerId);
                return true;
            } else {
                // Wiele receptur - otwórz menu
                LOGGER.info("[Crafting] Otwieranie menu.. .");

                if (player instanceof ServerPlayerEntity serverPlayer) {
                    NetworkHandler.sendRecipeList(serverPlayer, session.itemEntities, session.recipeOptions);
                }

                activeSessions.remove(playerId);
                return false;
            }
        } else {
            // ===== PIERWSZY KLIK =====

            if (recipeOptions.size() == 1) {
                RecipeOption option = recipeOptions. get(0);

                if (option.maxCrafts == 1) {
                    // Tylko 1 craft możliwy - zrób od razu
                    performMultiCraft(player, nearbyItems, option);
                    activeSessions.remove(playerId);
                    return true;
                } else {
                    // Wiele craftów możliwe - zapytaj
                    player.sendMessage(
                            Text.literal("✓ Craftować ")
                                    .styled(s -> s.withColor(0x55FF55))
                                    .append(option.totalResult.getName())
                                    .append(Text.literal(" x" + option.totalResult.getCount() + "? ")
                                            .styled(s -> s.withColor(0xFFFFFF)))
                                    .append(Text.literal("\n   (SHIFT+klik ponownie w 3s)")
                                            .styled(s -> s.withColor(0xAAAAAA))),
                            false
                    );

                    activeSessions.put(playerId, new CraftingSession(now, availableStacks, nearbyItems, recipeOptions));
                    return false;
                }
            } else {
                // Wiele receptur - otwórz menu
                LOGGER.info("[Crafting] Wiele receptur - otwieranie menu...");

                if (player instanceof ServerPlayerEntity serverPlayer) {
                    NetworkHandler.sendRecipeList(serverPlayer, nearbyItems, recipeOptions);
                }

                activeSessions.put(playerId, new CraftingSession(now, availableStacks, nearbyItems, recipeOptions));
                return false;
            }
        }
    }

    /**
     * ZNAJDŹ WSZYSTKIE RECEPTURY które można zrobić z dostępnych itemów
     * IGNORUJE GRID/SHAPED - sprawdza tylko składniki!
     */
    private static List<RecipeOption> findAllRecipes(List<ItemStack> availableStacks, World world) {
        List<RecipeOption> options = new ArrayList<>();

        // Pobierz WSZYSTKIE receptury craftingowe z gry
        Collection<CraftingRecipe> allRecipes = world.getRecipeManager()
                .listAllOfType(RecipeType.CRAFTING);

        LOGGER.info("[Crafting] Sprawdzanie {} receptur...", allRecipes. size());

        for (CraftingRecipe recipe : allRecipes) {
            int maxCrafts = calculateMaxCrafts(availableStacks, recipe);

            if (maxCrafts > 0) {
                options.add(new RecipeOption(recipe, maxCrafts, world));

                LOGGER.debug("[Crafting] ✓ {} (max {}x)",
                        recipe.getOutput(world.getRegistryManager()).getItem().getName().getString(),
                        maxCrafts);
            }
        }

        LOGGER.info("[Crafting] Znaleziono {} możliwych receptur", options.size());

        // Usuń duplikaty (ten sam output item)
        Map<String, RecipeOption> uniqueRecipes = new HashMap<>();

        for (RecipeOption option :  options) {
            String outputKey = option.totalResult.getItem().toString();

            RecipeOption existing = uniqueRecipes.get(outputKey);
            if (existing == null || option. maxCrafts > existing.maxCrafts) {
                uniqueRecipes.put(outputKey, option);
            }
        }

        List<RecipeOption> filtered = new ArrayList<>(uniqueRecipes. values());

        // Sortuj:  najprostsze (najmniej craftów) pierwsze
        filtered.sort(Comparator.comparingInt(o -> o.maxCrafts));

        return filtered;
    }

    /**
     * OBLICZ ILE RAZY można scraftować recepturę
     * IGNORUJE GRID - sprawdza tylko czy masz składniki!
     */
    private static int calculateMaxCrafts(List<ItemStack> availableStacks, CraftingRecipe recipe) {
        // Zlicz co MASZ
        Map<String, Integer> available = new HashMap<>();

        for (ItemStack stack : availableStacks) {
            String key = stack.getItem().toString();
            available.put(key, available.getOrDefault(key, 0) + stack.getCount());
        }

        // Zlicz co POTRZEBUJESZ
        Map<String, Integer> required = new HashMap<>();
        DefaultedList<Ingredient> ingredients = recipe.getIngredients();

        if (ingredients. isEmpty()) {
            return 0;
        }

        for (Ingredient ingredient : ingredients) {
            if (ingredient.isEmpty()) continue;

            ItemStack[] matchingStacks = ingredient.getMatchingStacks();
            if (matchingStacks. length == 0) continue;

            // Sprawdź czy masz KTÓRYKOLWIEK matching item
            boolean hasAny = false;
            String matchedKey = null;

            for (ItemStack matching : matchingStacks) {
                String key = matching.getItem().toString();
                if (available.getOrDefault(key, 0) > 0) {
                    hasAny = true;
                    matchedKey = key;
                    break;
                }
            }

            if (! hasAny) {
                return 0; // Nie masz tego składnika
            }

            // Zlicz ile potrzeba tego składnika
            required.put(matchedKey, required.getOrDefault(matchedKey, 0) + 1);
        }

        // Oblicz ile razy można scraftować (minimum z wszystkich składników)
        int minCrafts = Integer.MAX_VALUE;

        for (Map.Entry<String, Integer> entry : required.entrySet()) {
            String itemKey = entry.getKey();
            int neededPerCraft = entry.getValue();
            int availableCount = available.getOrDefault(itemKey, 0);

            if (availableCount == 0) {
                return 0;
            }

            int possibleCrafts = availableCount / neededPerCraft;
            minCrafts = Math. min(minCrafts, possibleCrafts);
        }

        return minCrafts == Integer.MAX_VALUE ? 0 : minCrafts;
    }

    /**
     * WYKONAJ CRAFTING (multi-craft jeśli możliwe)
     */
    private static void performMultiCraft(PlayerEntity player, List<ItemEntity> itemEntities, RecipeOption option) {
        World world = player. getWorld();
        CraftingRecipe recipe = option.recipe;
        int craftsToPerform = option.maxCrafts;

        LOGGER. info("[Crafting] Multi-craft: {} x{}.. .",
                recipe.getOutput(world.getRegistryManager()).getItem().getName().getString(),
                craftsToPerform);

        // Zbierz wszystkie stacki (ręka + ziemia) - BEZ . copy() bo musimy zużywać oryginały!
        List<ItemStack> allStacks = new ArrayList<>();
        ItemStack handStack = player.getInventory().getStack(0);

        if (!handStack. isEmpty()) {
            allStacks.add(handStack);
        }

        for (ItemEntity entity : itemEntities) {
            allStacks. add(entity.getStack());
        }

        // Craftuj w pętli
        List<ItemStack> results = new ArrayList<>();

        for (int i = 0; i < craftsToPerform; i++) {
            if (! canCraft(allStacks, recipe)) {
                LOGGER.warn("[Crafting] Nie można kontynuować po {} iteracjach", i);
                break;
            }

            ItemStack result = recipe.getOutput(world.getRegistryManager()).copy();
            results.add(result);

            consumeIngredientsFromStacks(allStacks, recipe);

            LOGGER.debug("[Crafting] Craft #{} ukończony", i + 1);
        }

        if (results.isEmpty()) {
            LOGGER.error("[Crafting] Nie udało się scraftować!");
            return;
        }

        // Połącz wyniki w jeden stack
        ItemStack combinedResult = results.get(0).copy();
        int totalCount = results.stream().mapToInt(ItemStack::getCount).sum();
        combinedResult.setCount(totalCount);

        // Daj graczowi
        giveResult(player, itemEntities. get(0), combinedResult);

        // Usuń puste ItemEntity
        for (ItemEntity entity : itemEntities) {
            if (entity.getStack().isEmpty()) {
                entity.discard();
            }
        }

        // Komunikat sukcesu
        player.sendMessage(
                Text.literal("✓ Utworzono:  ")
                        .styled(s -> s.withColor(0x55FF55))
                        .append(combinedResult.getName())
                        .append(Text.literal(" x" + totalCount).styled(s -> s.withColor(0xFFFFFF))),
                true
        );

        LOGGER.info("[Crafting] Multi-craft ukończony:  {} x{}",
                combinedResult. getItem().getName().getString(),
                totalCount);
    }

    /**
     * Sprawdź czy można scraftować
     */
    private static boolean canCraft(List<ItemStack> stacks, CraftingRecipe recipe) {
        return calculateMaxCrafts(stacks, recipe) > 0;
    }

    /**
     * ZUŻYJ SKŁADNIKI (usuwa 1 item z każdego wymaganego składnika)
     */
    private static void consumeIngredientsFromStacks(List<ItemStack> stacks, CraftingRecipe recipe) {
        DefaultedList<Ingredient> ingredients = recipe.getIngredients();

        for (Ingredient ingredient : ingredients) {
            if (ingredient.isEmpty()) continue;

            // Znajdź pierwszy matching stack i zużyj 1
            for (ItemStack stack : stacks) {
                if (ingredient.test(stack)) {
                    stack.decrement(1);
                    break;
                }
            }
        }
    }

    /**
     * DAJ WYNIK graczowi (do ręki lub drop)
     */
    private static void giveResult(PlayerEntity player, ItemEntity nearEntity, ItemStack result) {
        ItemStack handStack = player.getInventory().getStack(0);

        if (handStack. isEmpty()) {
            // Ręka pusta - daj do ręki
            player.getInventory().setStack(0, result. copy());
            LOGGER.info("[Crafting] Wynik do ręki");

        } else if (ItemStack.canCombine(handStack, result)) {
            // Ten sam item - stackuj
            int newCount = Math.min(handStack.getCount() + result.getCount(), handStack.getMaxCount());
            int remainder = (handStack.getCount() + result.getCount()) - newCount;

            handStack.setCount(newCount);

            if (remainder > 0) {
                // Drop reszty
                ItemStack remainderStack = result.copy();
                remainderStack.setCount(remainder);

                ItemEntity resultEntity = new ItemEntity(
                        player.getWorld(),
                        nearEntity.getX(),
                        nearEntity.getY(),
                        nearEntity. getZ(),
                        remainderStack
                );
                PlayerDroppedItemTracker.markAsPlayerDropped(resultEntity);
                player.getWorld().spawnEntity(resultEntity);
            }

            LOGGER.info("[Crafting] Wynik stackowany do ręki");

        } else {
            // Ręka zajęta innym itemem - drop na ziemię
            ItemEntity resultEntity = new ItemEntity(
                    player.getWorld(),
                    nearEntity.getX(),
                    nearEntity.getY(),
                    nearEntity.getZ(),
                    result. copy()
            );
            PlayerDroppedItemTracker.markAsPlayerDropped(resultEntity);
            player.getWorld().spawnEntity(resultEntity);

            LOGGER.info("[Crafting] Wynik dropped (ręka zajęta)");
        }
    }

    /**
     * Publiczne API dla NetworkHandler (gdy gracz wybierze recepturę z menu)
     */
    public static void performMultiCraftDirect(PlayerEntity player, List<ItemEntity> itemEntities, RecipeOption option) {
        performMultiCraft(player, itemEntities, option);
    }
}