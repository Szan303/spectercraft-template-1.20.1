package com.szan.jade;

//import com.szan.block.BrickFurnace_old;
import com.szan.block.WetClayBlock;
import com.szan.block.entity.WetClayBlockEntity;
//import com.szan.block.entity.BrickFurnaceEntity_old;
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
//        registration.registerBlockDataProvider(BrickFurnaceJadeProvider.INSTANCE, BrickFurnaceEntity_old.class);
        registration.registerBlockDataProvider(WetClayJadeProvider.INSTANCE, WetClayBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        // =======================================================================================
        // Zarejestruj providera dla bloku (wyświetlanie tooltiptów) TUTAJ KLASĘ PARENTA ENTITY
        // =======================================================================================
//        registration.registerBlockComponent(BrickFurnaceJadeProvider.INSTANCE, BrickFurnace_old.class);
        registration.registerBlockComponent(WetClayJadeProvider.INSTANCE, WetClayBlock.class);
    }
}