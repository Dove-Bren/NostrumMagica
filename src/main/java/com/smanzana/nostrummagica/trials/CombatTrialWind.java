package com.smanzana.nostrummagica.trials;

import com.smanzana.nostrummagica.entity.NostrumEntityTypes;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class CombatTrialWind extends CombatTrialStaged {
	
	protected CombatTrialWind(ServerWorld world, BlockPos center, PlayerEntity player) {
		super(world, center, player);
		
		CachedSpawnProvider spawnProvider = new CachedSpawnProvider(world, center, CombatTrialWind::isSpawnSuggestion);
		this.setStages(
				new CombatTrialStage(new RandomPoolMobProvider(NostrumEntityTypes.golemWind), spawnProvider, 1),
				new CombatTrialStage(new RandomPoolMobProvider(NostrumEntityTypes.golemWind), spawnProvider, 2),
				new CombatTrialStage(new RandomPoolMobProvider(NostrumEntityTypes.golemWind), spawnProvider, 3)
				);
	}
	
	protected static boolean isSpawnSuggestion(BlockState state) {
		final Block block = state.getBlock();
		return block == Blocks.RED_MUSHROOM_BLOCK
				|| block == Blocks.BROWN_MUSHROOM_BLOCK;
	}
}
