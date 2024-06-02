package com.smanzana.nostrummagica.trial;

import com.smanzana.nostrummagica.entity.EntityKoid;
import com.smanzana.nostrummagica.entity.EntityWillo;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class CombatTrialEarth extends CombatTrialStaged {
	
	protected CombatTrialEarth(ServerWorld world, BlockPos center, PlayerEntity player) {
		super(world, center, player);
		
		CachedSpawnProvider spawnProvider = new CachedSpawnProvider(world, center, CombatTrialEarth::isSpawnSuggestion);
		this.setStages(
				new CombatTrialStage(player, new RandomPoolMobProvider(NostrumEntityTypes.golemEarth), spawnProvider, 3),
				new CombatTrialStage(player, new RandomPoolMobProvider(NostrumEntityTypes.golemEarth, NostrumEntityTypes.sprite), spawnProvider, 4),
				new CombatTrialStage(player, new TweakedMobPool(NostrumEntityTypes.willo), spawnProvider, 1),
				new CombatTrialStage(player, new TweakedMobPool(NostrumEntityTypes.golemEarth, NostrumEntityTypes.koid), spawnProvider, 5)
				);
	}
	
	protected static boolean isSpawnSuggestion(BlockState state) {
		final Block block = state.getBlock();
		return block == Blocks.OBSIDIAN
				|| block == Blocks.GRASS_PATH;
	}
	
	private static class TweakedMobPool extends RandomPoolMobProvider {
		
		@SafeVarargs
		public TweakedMobPool(EntityType<? extends MobEntity> ... types) {
			super(types);
		}
		
		@Override
		public MobEntity provideEntity(World world) {
			MobEntity ent = super.provideEntity(world);
			
			if (ent instanceof EntityWillo) {
				((EntityWillo) ent).setElement(EMagicElement.EARTH);
			} else if (ent instanceof EntityKoid) {
				((EntityKoid) ent).setElement(EMagicElement.EARTH);
			}
			
			return ent;
		}
	}
}
