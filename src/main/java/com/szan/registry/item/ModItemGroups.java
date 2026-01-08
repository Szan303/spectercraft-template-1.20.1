package com.szan.registry.item;

import com.szan. SpecterCraft;
import com.szan.registry.Block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModItemGroups {
    private static final Logger LOGGER = LoggerFactory.getLogger("SpecterCraft/Registry/ItemGroups");

    public static final ItemGroup SPECTERCRAFT_GROUP = Registry.register(
            Registries.ITEM_GROUP,
            new Identifier(SpecterCraft.MOD_ID, "spectercraft"),
            FabricItemGroup.builder()
                    .displayName(Text.translatable("itemgroup.spectercraft"))
                    .icon(() -> new ItemStack(ModBlocks.WET_CLAY))
                    .entries((displayContext, entries) -> {
                        entries.add(ModBlocks.WET_CLAY);
                    })
                    .build()
    );

    public static void register() {
        LOGGER.info("Rejestrowanie creative tabs...");
    }
}