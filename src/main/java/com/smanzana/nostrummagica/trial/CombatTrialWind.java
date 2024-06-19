package com.smanzana.nostrummagica.trial;

import com.smanzana.nostrummagica.entity.KoidEntity;
import com.smanzana.nostrummagica.entity.WilloEntity;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class CombatTrialWind extends CombatTrialStaged {
	
	protected CombatTrialWind(ServerWorld world, BlockPos center, PlayerEntity player) {
		super(world, center, player);
		
		CachedSpawnProvider spawnProvider = new CachedSpawnProvider(world, center, CombatTrialWind::isSpawnSuggestion);
		this.setStages(
				new CombatTrialStage(player, new RandomPoolMobProvider(NostrumEntityTypes.golemWind), spawnProvider, 3),
				new CombatTrialStage(player, new TweakedMobPool(NostrumEntityTypes.golemWind, EntityType.SKELETON, EntityType.VEX), spawnProvider, 2),
				new CombatTrialStage(player, new TweakedMobPool(NostrumEntityTypes.willo), spawnProvider, 1),
				new CombatTrialStage(player, new TweakedMobPool(NostrumEntityTypes.golemWind, NostrumEntityTypes.koid), spawnProvider, 3)
				);
	}
	
	protected static boolean isSpawnSuggestion(BlockState state) {
		final Block block = state.getBlock();
		return block == Blocks.RED_MUSHROOM_BLOCK
				|| block == Blocks.BROWN_MUSHROOM_BLOCK;
	}
	
	private static class TweakedMobPool extends RandomPoolMobProvider {
		
		@SafeVarargs
		public TweakedMobPool(EntityType<? extends MobEntity> ... types) {
			super(types);
		}
		
		@Override
		public MobEntity provideEntity(World world) {
			MobEntity ent = super.provideEntity(world);
			
			if (ent instanceof WilloEntity) {
				((WilloEntity) ent).setElement(EMagicElement.PHYSICAL);
			} else if (ent instanceof KoidEntity) {
				((KoidEntity) ent).setElement(EMagicElement.PHYSICAL);
			} else if (ent instanceof IronGolemEntity) {
				; // Thought I was going to have to do something but doesn't seem like it
			}
			
			return ent;
		}
	}
}
