package com.smanzana.nostrummagica.trial;

import com.smanzana.nostrummagica.entity.KoidEntity;
import com.smanzana.nostrummagica.entity.WilloEntity;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class CombatTrialWind extends CombatTrialStaged {
	
	protected CombatTrialWind(ServerLevel world, BlockPos center, Player player) {
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
		public TweakedMobPool(EntityType<? extends Mob> ... types) {
			super(types);
		}
		
		@Override
		public Mob provideEntity(Level world) {
			Mob ent = super.provideEntity(world);
			
			if (ent instanceof WilloEntity) {
				((WilloEntity) ent).setElement(EMagicElement.PHYSICAL);
			} else if (ent instanceof KoidEntity) {
				((KoidEntity) ent).setElement(EMagicElement.PHYSICAL);
			} else if (ent instanceof IronGolem) {
				; // Thought I was going to have to do something but doesn't seem like it
			}
			
			return ent;
		}
	}
}
