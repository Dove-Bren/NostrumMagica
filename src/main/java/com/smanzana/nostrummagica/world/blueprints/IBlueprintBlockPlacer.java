package com.smanzana.nostrummagica.world.blueprints;

import com.smanzana.nostrummagica.world.blueprints.RoomBlueprint.SpawnContext;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public interface IBlueprintBlockPlacer {
	
	public void spawnBlock(SpawnContext context, BlockPos pos, Direction direction, BlueprintBlock block);
}