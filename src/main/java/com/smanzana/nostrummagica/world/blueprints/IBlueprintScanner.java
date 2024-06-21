package com.smanzana.nostrummagica.world.blueprints;

import net.minecraft.util.math.BlockPos;

public interface IBlueprintScanner {
	public void scan(BlockPos offset, BlueprintBlock block);
}