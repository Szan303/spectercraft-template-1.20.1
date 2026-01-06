package com.szan.handler;

import com.szan.SpecterCraft;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity. ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net. minecraft.server.network.ServerPlayerEntity;
import net.minecraft. text.Text;
import net. minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkHandler {
    private static final Logger LOGGER = LoggerFactory. getLogger("SpecterCraft/Network");

    // Przechowuje sesje craftingu dla menu wyboru
    private static final Map<ServerPlayerEntity, CraftingMenuSession> menuSessions = new HashMap<>();

    private static class CraftingMenuSession {
        List<ItemEntity> itemEntities;
        List<CraftingHelper.RecipeOption> recipeOptions;

        CraftingMenuSession(List<ItemEntity> entities, List<CraftingHelper. RecipeOption> options) {
            this.itemEntities = entities;
            this.recipeOptions = options;
        }
    }

    public static void register() {
        LOGGER.info("Rejestrowanie NetworkHandler.. .");

        // PICKUP/CRAFT packet (klient → serwer)
        ServerPlayNetworking. registerGlobalReceiver(
                SpecterCraft.PICKUP_PACKET_ID,
                (server, player, handler, buf, responseSender) -> {
                    int entityId = buf. readInt();
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

                            player. currentScreenHandler.sendContentUpdates();
                            player.playerScreenHandler.onContentChanged(player. getInventory());
                        }
                    });
                }
        );

        // RECIPE_SELECTED packet (klient → serwer)
        ServerPlayNetworking.registerGlobalReceiver(
                SpecterCraft. RECIPE_SELECTED_PACKET_ID,
                (server, player, handler, buf, responseSender) -> {
                    Identifier recipeId = buf.readIdentifier();

                    server. execute(() -> {
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
                            LOGGER. warn("[Network] Nie znaleziono receptury {}", recipeId);
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

        // ====== SLOT_SWAP packet (klient → serwer) - STARY (można usunąć jeśli nie używany) ======
        ServerPlayNetworking.registerGlobalReceiver(
                SpecterCraft.SLOT_SWAP_PACKET_ID,
                (server, player, handler, buf, responseSender) -> {
                    int fromSlot = buf.readInt();
                    int toSlot = buf.readInt();

                    server.execute(() -> {
                        ItemStack fromStack = player.getInventory().getStack(fromSlot).copy();
                        ItemStack toStack = player.getInventory().getStack(toSlot).copy();

                        player.getInventory().setStack(fromSlot, toStack);
                        player.getInventory().setStack(toSlot, fromStack);

                        player.currentScreenHandler. sendContentUpdates();

                        LOGGER.info("[Network] Zamieniono sloty {} <-> {}", fromSlot, toSlot);
                    });
                }
        );

        // ====== SLOT_CLICK packet (klient → serwer) - NOWY!  ======
        ServerPlayNetworking.registerGlobalReceiver(
                SpecterCraft. SLOT_CLICK_PACKET_ID,
                (server, player, handler, buf, responseSender) -> {
                    int slotIndex = buf.readInt();
                    int button = buf.readInt();  // 0=left, 1=right
                    int cursorSlot = buf.readInt();  // -1 jeśli cursor pusty, lub slot źródłowy
                    int actionType = buf.readInt();  // 0=pickup, 1=place, 2=swap, 3=split, 4=drop_one, 5=return

                    server.execute(() -> {
                        var inventory = player.getInventory();

                        LOGGER.info("[Network] SLOT_CLICK: slot={}, button={}, cursor={}, action={}",
                                slotIndex, button, cursorSlot, actionType);

                        switch (actionType) {
                            case 0: // PICKUP - podnieś item na cursor
                                LOGGER.info("[Network] Action: PICKUP from slot {}", slotIndex);
                                // Client już to zrobił (pokazuje item na kursorze)
                                // Server nic nie robi - item nadal jest w slocie
                                break;

                            case 1: // PLACE - połóż cały stack z cursora
                                if (cursorSlot >= 0) {
                                    LOGGER. info("[Network] Action: PLACE from cursor slot {} to {}", cursorSlot, slotIndex);

                                    // Walidacja
                                    ItemStack fromStack = inventory.getStack(cursorSlot);
                                    if (! canPlaceInSlot(slotIndex, fromStack)) {
                                        LOGGER.warn("[Network] ✗ Nieprawidłowa zamiana!");
                                        player.sendMessage(Text.literal("✗ Ten przedmiot nie pasuje tutaj! ").styled(s -> s.withColor(0xFF5555)), true);
                                        break;
                                    }

                                    ItemStack toStack = inventory.getStack(slotIndex);

                                    inventory.setStack(slotIndex, fromStack. copy());
                                    inventory. setStack(cursorSlot, toStack);
                                }
                                break;

                            case 2: // SWAP - zamień miejscami
                                if (cursorSlot >= 0) {
                                    LOGGER.info("[Network] Action: SWAP {} <-> {}", cursorSlot, slotIndex);

                                    // Walidacja
                                    ItemStack fromStack = inventory.getStack(cursorSlot);
                                    ItemStack toStack = inventory.getStack(slotIndex);

                                    if (! canPlaceInSlot(slotIndex, fromStack) || !canPlaceInSlot(cursorSlot, toStack)) {
                                        LOGGER.warn("[Network] ✗ Nieprawidłowa zamiana!");
                                        player.sendMessage(Text.literal("✗ Nieprawidłowa zamiana!").styled(s -> s.withColor(0xFF5555)), true);
                                        break;
                                    }

                                    inventory.setStack(cursorSlot, toStack. copy());
                                    inventory. setStack(slotIndex, fromStack.copy());
                                }
                                break;

                            case 3: // SPLIT - weź połowę (PPM na item)
                                LOGGER.info("[Network] Action: SPLIT from slot {}", slotIndex);

                                ItemStack stack = inventory.getStack(slotIndex);
                                if (! stack.isEmpty()) {
                                    int halfCount = (stack.getCount() + 1) / 2;
                                    int remainingCount = stack.getCount() - halfCount;

                                    stack.setCount(remainingCount);
                                    inventory.setStack(slotIndex, stack);

                                    LOGGER.info("[Network] Split:  pozostało {} w slocie", remainingCount);
                                }
                                break;

                            case 4: // DROP_ONE - upuść 1 item z cursora
                                if (cursorSlot >= 0) {
                                    LOGGER.info("[Network] Action: DROP_ONE from cursor slot {} to {}", cursorSlot, slotIndex);

                                    ItemStack fromStack = inventory.getStack(cursorSlot);
                                    ItemStack toStack = inventory.getStack(slotIndex);

                                    if (!fromStack.isEmpty()) {
                                        // Walidacja
                                        if (! canPlaceInSlot(slotIndex, fromStack)) {
                                            LOGGER.warn("[Network] ✗ Nie można umieścić tutaj!");
                                            break;
                                        }

                                        if (toStack.isEmpty()) {
                                            // Nowy stack z 1 itemem
                                            ItemStack newStack = fromStack.copy();
                                            newStack. setCount(1);
                                            inventory.setStack(slotIndex, newStack);

                                            fromStack.decrement(1);
                                            if (fromStack.isEmpty()) {
                                                inventory.setStack(cursorSlot, ItemStack. EMPTY);
                                            }

                                            LOGGER.info("[Network] Utworzono nowy stack z 1 itemem");
                                        } else if (ItemStack.areItemsEqual(fromStack, toStack) && toStack.getCount() < toStack.getMaxCount()) {
                                            // Dodaj 1 do istniejącego stacka
                                            toStack.increment(1);
                                            fromStack.decrement(1);

                                            if (fromStack.isEmpty()) {
                                                inventory.setStack(cursorSlot, ItemStack. EMPTY);
                                            }

                                            LOGGER.info("[Network] Dodano 1 do stacka (nowy count: {})", toStack.getCount());
                                        } else {
                                            LOGGER.warn("[Network] Stack pełny lub różne typy itemów!");
                                        }
                                    }
                                }
                                break;

                            case 5: // RETURN - oddaj item na ten sam slot (anuluj)
                                LOGGER.info("[Network] Action: RETURN to slot {} (cursor reset)", cursorSlot);
                                // Nic nie rób - item jest nadal w oryginalnym slocie
                                // Client zresetował cursor
                                break;
                            case 6:  // STACK - dodaj cały cursor stack do istniejącego stacka
                                if (cursorSlot >= 0) {
                                    LOGGER.info("[Network] Action:  STACK from cursor slot {} to {}", cursorSlot, slotIndex);

                                    ItemStack fromStack = inventory.getStack(cursorSlot);
                                    ItemStack toStack = inventory.getStack(slotIndex);

                                    if (!fromStack.isEmpty() && !toStack.isEmpty() && ItemStack.areItemsEqual(fromStack, toStack)) {
                                        int spaceAvailable = toStack.getMaxCount() - toStack.getCount();

                                        if (spaceAvailable > 0) {
                                            int amountToAdd = Math.min(fromStack.getCount(), spaceAvailable);

                                            toStack.increment(amountToAdd);
                                            fromStack.decrement(amountToAdd);

                                            if (fromStack.isEmpty()) {
                                                inventory.setStack(cursorSlot, ItemStack.EMPTY);
                                            }

                                            LOGGER.info("[Network] Dodano {} do stacka (nowy count: {})", amountToAdd, toStack.getCount());
                                        } else {
                                            LOGGER. warn("[Network] Stack już pełny!");
                                        }
                                    } else {
                                        LOGGER.warn("[Network] Nie można stackować - różne typy lub puste!");
                                    }
                                }
                                break;
                            default:
                                LOGGER.warn("[Network] Nieznany actionType: {}", actionType);
                                break;
                        }

                        // Aktualizuj inventory
                        player.currentScreenHandler.sendContentUpdates();
                        player.playerScreenHandler.onContentChanged(inventory);
                    });
                }
        );

        LOGGER.info("✓ NetworkHandler zarejestrowany!");
    }

    /**
     * Sprawdź czy item może być umieszczony w slocie (server-side validation)
     */
    private static boolean canPlaceInSlot(int slot, ItemStack stack) {
        if (stack. isEmpty()) return true;

        // Armor slots (36-39)
        if (slot >= 36 && slot <= 39) {
            var equipmentSlot = net.minecraft.entity.mob.MobEntity.getPreferredEquipmentSlot(stack);

            return switch (slot) {
                case 36 -> equipmentSlot == net.minecraft.entity. EquipmentSlot.FEET;   // Boots
                case 37 -> equipmentSlot == net.minecraft. entity.EquipmentSlot.LEGS;   // Leggings
                case 38 -> equipmentSlot == net. minecraft.entity.EquipmentSlot.CHEST;  // Chestplate
                case 39 -> equipmentSlot == net.minecraft.entity.EquipmentSlot.HEAD;   // Helmet
                default -> false;
            };
        }

        // Offhand (40) - wszystko dozwolone
        // Inventory (0-35) - wszystko dozwolone
        return true;
    }

    /**
     * Obsługuje żądanie craftingu (może otworzyć menu)
     */
    private static void handleCraftRequest(ServerPlayerEntity player, ItemEntity itemEntity) {
        boolean success = CraftingHelper.attemptCraft(player, itemEntity);
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

            buf. writeInt(ingredientStacks.size());
            for (ItemStack stack : ingredientStacks) {
                buf.writeItemStack(stack);
            }

            LOGGER.debug("[Network] Dodano recepturę: {} x{}",
                    option.totalResult.getItem().getName().getString(),
                    option. totalResult.getCount());
        }

        // Wyślij packet
        ServerPlayNetworking.send(player, SpecterCraft. RECIPE_LIST_PACKET_ID, buf);

        LOGGER.info("[Network] Packet RECIPE_LIST wysłany!");
    }

    /**
     * Obsługuje podnoszenie itemów
     */
    private static void handlePickup(ServerPlayerEntity player, ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getStack();
        ItemStack handStack = player.getInventory().getStack(0);

        if (handStack. isEmpty()) {
            player.getInventory().setStack(0, itemStack.copy());
            itemEntity.discard();
            LOGGER.info("[Network] ✓ Item podniesiony!");
        } else if (ItemStack.canCombine(handStack, itemStack)) {
            int totalCount = handStack.getCount() + itemStack.getCount();
            int maxCount = handStack.getMaxCount();

            if (totalCount <= maxCount) {
                handStack.setCount(totalCount);
                itemEntity.discard();
                LOGGER.info("[Network] ✓ Dodano do stacka!");
            } else {
                int canAdd = maxCount - handStack.getCount();
                handStack.setCount(maxCount);
                itemStack.setCount(itemStack. getCount() - canAdd);
                LOGGER.info("[Network] ⚠ Stack pełny!");
            }
        } else {
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