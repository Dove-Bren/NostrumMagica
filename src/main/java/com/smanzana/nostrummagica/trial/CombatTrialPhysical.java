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

public class CombatTrialPhysical extends CombatTrialStaged {
	
	protected CombatTrialPhysical(ServerWorld world, BlockPos center, PlayerEntity player) {
		super(world, center, player);
		
		CachedSpawnProvider spawnProvider = new CachedSpawnProvider(world, center, CombatTrialPhysical::isSpawnSuggestion);
		this.setStages(
				new CombatTrialStage(player, new RandomPoolMobProvider(NostrumEntityTypes.golemPhysical), spawnProvider, 3),
				new CombatTrialStage(player, new TweakedMobPool(NostrumEntityTypes.golemPhysical, EntityType.IRON_GOLEM), spawnProvider, 4),
				new CombatTrialStage(player, new TweakedMobPool(NostrumEntityTypes.willo), spawnProvider, 1),
				new CombatTrialStage(player, new TweakedMobPool(NostrumEntityTypes.golemPhysical, NostrumEntityTypes.koid, EntityType.IRON_GOLEM), spawnProvider, 5)
				);
	}
	
	protected static boolean isSpawnSuggestion(BlockState state) {
		final Block block = state.getBlock();
		return block == Blocks.IRON_TRAPDOOR
				|| block == Blocks.IRON_BLOCK;
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
