package com.smanzana.nostrummagica.trial;

import com.smanzana.nostrummagica.entity.NostrumEntityTypes;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class CombatTrialEnder extends CombatTrialStaged {
	
	protected CombatTrialEnder(ServerLevel world, BlockPos center, Player player) {
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
