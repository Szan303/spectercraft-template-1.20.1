package com.szan.mixin;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractFurnaceBlockEntity.class)
public interface AbstractFurnaceBlockEntityAccessor {
    @Accessor("cookTime") int getCookTime();
    @Accessor("cookTime") void setCookTime(int cookTime);

    @Accessor("burnTime") int getBurnTime();
    @Accessor("burnTime") void setBurnTime(int burnTime);

    @Accessor("recipesUsed") int getRecipesUsed();
    @Accessor("recipesUsed") void setRecipesUsed(int recipesUsed);

    @Accessor("maxBurnTime") int getMaxBurnTime();
    @Accessor("maxBurnTime") void setMaxBurnTime(int maxBurnTime);
}