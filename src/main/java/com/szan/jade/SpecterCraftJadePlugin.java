package com.szan.jade;

import com.szan.block.WetClayBlock;
import com.szan.block.entity.WetClayBlockEntity;
import com.szan.jade.BrickFurnaceJadeProvider; // jeśli używasz providera
import com.szan.block.BrickFurnace;
import com.szan.block.entity.BrickFurnaceEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class SpecterCraftJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        // =======================================================================================
        // Zarejestruj providera dla Entity (wysyłanie danych serwer→klient) TUTAJ REJESTRUJ ENTITY
        // =======================================================================================
        registration.registerBlockDataProvider(BrickFurnaceJadeProvider.INSTANCE, BrickFurnaceEntity.class);
        registration.registerBlockDataProvider(WetClayJadeProvider.INSTANCE, WetClayBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        // =======================================================================================
        // Zarejestruj providera dla bloku (wyświetlanie tooltiptów) TUTAJ KLASĘ PARENTA ENTITY
        // =======================================================================================
        registration.registerBlockComponent(BrickFurnaceJadeProvider.INSTANCE, BrickFurnace.class);
        registration.registerBlockComponent(WetClayJadeProvider.INSTANCE, WetClayBlock.class);
    }
}