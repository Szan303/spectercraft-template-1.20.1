package com.szan.registry;

import com. szan.SpecterCraft;
import com.szan. block.entity.WetClayBlockEntity;
import net.minecraft.block. entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft. registry.Registry;
import net. minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModBlockEntities {
    private static final Logger LOGGER = LoggerFactory.getLogger("SpecterCraft/Registry/BlockEntities");

    // ========== BLOCK ENTITY DEFINITIONS ==========
    public static BlockEntityType<WetClayBlockEntity> WET_CLAY_ENTITY;

    // ========== REGISTRATION ==========
    public static void register() {
        LOGGER.info("Rejestrowanie block entities...");

        WET_CLAY_ENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                new Identifier(SpecterCraft.MOD_ID, "wet_clay_entity"),
                BlockEntityType. Builder.create(WetClayBlockEntity::new, ModBlocks.WET_CLAY).build(null)
        );
    }
}