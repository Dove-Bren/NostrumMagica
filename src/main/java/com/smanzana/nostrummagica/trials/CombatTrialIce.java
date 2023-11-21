package com.smanzana.nostrummagica.trials;

import com.smanzana.nostrummagica.entity.NostrumEntityTypes;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class CombatTrialIce extends CombatTrialStaged {
	
	protected CombatTrialIce(ServerWorld world, BlockPos center, PlayerEntity player) {
		super(world, center, player);
		
		CachedSpawnProvider spawnProvider = new CachedSpawnProvider(world, center, CombatTrialIce::isSpawnSuggestion);
		this.setStages(
				new CombatTrialStage(new RandomPoolMobProvider(NostrumEntityTypes.golemIce), spawnProvider, 1),
				new CombatTrialStage(new RandomPoolMobProvider(NostrumEntityTypes.golemIce), spawnProvider, 2),
				new CombatTrialStage(new RandomPoolMobProvider(NostrumEntityTypes.golemIce), spawnProvider, 3)
				);
	}
	
	protected static boolean isSpawnSuggestion(BlockState state) {
		final Block block = state.getBlock();
		return block == Blocks.LAPIS_BLOCK
				|| block == Blocks.QUARTZ_PILLAR;
	}
}
