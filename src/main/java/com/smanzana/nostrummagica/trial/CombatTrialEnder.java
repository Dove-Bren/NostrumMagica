package com.smanzana.nostrummagica.trial;

import com.smanzana.nostrummagica.entity.NostrumEntityTypes;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class CombatTrialEnder extends CombatTrialStaged {
	
	protected CombatTrialEnder(ServerWorld world, BlockPos center, PlayerEntity player) {
		super(world, center, player);
		
		// Note: Ender willos and koids blink you and randomly teleport you, which would be bad in the sorc dim
		CachedSpawnProvider spawnProvider = new CachedSpawnProvider(world, center, CombatTrialEnder::isSpawnSuggestion);
		this.setStages(
				new CombatTrialStage(player, new RandomPoolMobProvider(NostrumEntityTypes.golemEnder), spawnProvider, 3),
				new CombatTrialStage(player, new RandomPoolMobProvider(NostrumEntityTypes.golemEnder, EntityType.ENDERMITE, EntityType.ENDERMAN), spawnProvider, 4),
				new CombatTrialStage(player, new RandomPoolMobProvider(NostrumEntityTypes.golemEnder, EntityType.VEX), spawnProvider, 3),
				new CombatTrialStage(player, new RandomPoolMobProvider(NostrumEntityTypes.golemEnder, EntityType.ILLUSIONER), spawnProvider, 3)
				);
	}
	
	protected static boolean isSpawnSuggestion(BlockState state) {
		final Block block = state.getBlock();
		return block == Blocks.END_STONE_BRICK_SLAB
				|| block == Blocks.OBSIDIAN;
	}
}
