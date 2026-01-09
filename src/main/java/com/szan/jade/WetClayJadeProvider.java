package com.szan.jade;

import com.szan.block.WetClayBlock;
import com.szan.block.entity.WetClayBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.config.IPluginConfig;

import static com.szan.SpecterCraft.MOD_ID;

public enum WetClayJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public void appendServerData(NbtCompound data, BlockAccessor accessor) {
        WetClayBlockEntity entity = (WetClayBlockEntity) accessor.getBlockEntity();
        if (entity != null) {
            data.putInt("TicksLeft", entity.getTicksToNextStage());
            data.putInt("CurrentTicks", entity.getCurrentTicks());
            data.putInt("MaxTicks", entity.getStageTicks());
            // Możesz dodać dodatkowe dane, np. aktualny drying_stage:
            int stage = accessor.getBlockState().get(WetClayBlock.DRYING_STAGE);
            data.putInt("DryingStage", stage);
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getServerData().contains("TicksLeft")) {
            int left = accessor.getServerData().getInt("TicksLeft");
            int current = accessor.getServerData().getInt("CurrentTicks");
            int max = accessor.getServerData().getInt("MaxTicks");
            int stage = accessor.getServerData().getInt("DryingStage");

            int totalSeconds = left / 20;
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;

            tooltip.add(Text.literal("Etap schnięcia: " + stage));
            tooltip.add(Text.literal("To next stage: " + minutes + " min " + seconds + " s"));
            tooltip.add(Text.literal("Postęp: " + (100 * current / max) + "%"));
        }
    }

    @Override
    public Identifier getUid() {
        return new Identifier(MOD_ID, "wet_clay_drying");
    }
}