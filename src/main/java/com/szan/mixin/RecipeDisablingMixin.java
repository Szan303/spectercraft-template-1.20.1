package com.szan.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.szan.registry.DisabledRecipeRegistry;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.util.Identifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.profiler.Profiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.szan.SpecterCraft.MOD_ID;

@Mixin(RecipeManager.class)

public class RecipeDisablingMixin {
    @Inject(at = @At("HEAD"), method = "apply", cancellable = true)

    private void spectercraft$apply(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler, CallbackInfo ci) {
        Map<Identifier, JsonElement> filteredMap = new HashMap<>();
        for (Map.Entry<Identifier, JsonElement> entry : map.entrySet()) {
            Identifier identifier = entry.getKey();
            JsonElement jsonElement = entry.getValue();

            try {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                JsonElement resultElement = jsonObject.get("result");
                if (resultElement == null && jsonObject.has("output")) {
                    resultElement = jsonObject.get("output");
                }

                boolean shouldDisable = false;

                // Obsługa różnych typów result/output
                if (resultElement != null) {
                    if (resultElement.isJsonObject()) {
                        String itemId = getResultItemId(resultElement.getAsJsonObject());
                        if (itemId != null && DisabledRecipeRegistry.isRecipeDisabled(itemId)) {
                            shouldDisable = true;
                        }
                    } else if (resultElement.isJsonPrimitive() && resultElement.getAsJsonPrimitive().isString()) {
                        String itemId = resultElement.getAsString();
                        if (itemId != null && DisabledRecipeRegistry.isRecipeDisabled(itemId)) {
                            shouldDisable = true;
                        }
                    } else if (resultElement.isJsonArray()) {
                        for (JsonElement element : resultElement.getAsJsonArray()) {
                            if (element.isJsonObject()) {
                                String itemId = getResultItemId(element.getAsJsonObject());
                                if (itemId != null && DisabledRecipeRegistry.isRecipeDisabled(itemId)) {
                                    shouldDisable = true;
                                    LoggerFactory.getLogger(MOD_ID).info("Recipe disabling for item " + itemId);
                                    break;
                                }
                            } else if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                                String itemId = element.getAsString();
                                if (itemId != null && DisabledRecipeRegistry.isRecipeDisabled(itemId)) {
                                    shouldDisable = true;
                                    LoggerFactory.getLogger(MOD_ID).info("Recipe disabling for item " + itemId);
                                    break;
                                }
                            }
                        }
                    }
                }
                // jeśli przepis NIE do wyłączenia, dodaj do mapy
                if (!shouldDisable) {
                    filteredMap.put(identifier, jsonElement);
                }

            } catch (JsonParseException | IllegalArgumentException e) {
                // Jeśli nie można sparsować - zostaw w przepisach
                filteredMap.put(identifier, jsonElement);
            }
        }

        map.clear();
        map.putAll(filteredMap);
    }

    private String getResultItemId(JsonObject resultObject) {
        if (resultObject.has("item")) {
            return resultObject.get("item").getAsString();
        } else if (resultObject.has("id")) {
            return resultObject.get("id").getAsString();
        } else if (resultObject.has("result")) {
            return resultObject.get("result").getAsString();
        } else if (resultObject.has("output")) {
            return resultObject.get("output").getAsString();
        } else {
            return null;
        }
    }
}