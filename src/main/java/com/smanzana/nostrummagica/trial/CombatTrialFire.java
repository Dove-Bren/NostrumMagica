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
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class CombatTrialFire extends CombatTrialStaged {
	
	protected CombatTrialFire(ServerLevel world, BlockPos center, Player player) {
		super(world, center, player);
		
		CachedSpawnProvider spawnProvider = new CachedSpawnProvider(world, center, CombatTrialFire::isSpawnSuggestion);
		this.setStages(
				new CombatTrialStage(player, new RandomPoolMobProvider(NostrumEntityTypes.golemFire), spawnProvider, 3),
				new CombatTrialStage(player, new TweakedMobPool(NostrumEntityTypes.golemFire, EntityType.MAGMA_CUBE, EntityType.BLAZE), spawnProvider, 5),
				new CombatTrialStage(player, new TweakedMobPool(NostrumEntityTypes.willo), spawnProvider, 1),
				new CombatTrialStage(player, new TweakedMobPool(NostrumEntityTypes.golemFire, NostrumEntityTypes.koid), spawnProvider, 5)
				);
	}
	
	protected static boolean isSpawnSuggestion(BlockState state) {
		final Block block = state.getBlock();
		return block == Blocks.MAGMA_BLOCK
				|| block == Blocks.OBSIDIAN;
	}
	
	private static class TweakedMobPool extends RandomPoolMobProvider {
		
		@SafeVarargs
		public TweakedMobPool(EntityType<? extends Mob> ... types) {
			super(types);
		}
		
		@Override
		public Mob provideEntity(Level world) {
			Mob ent = super.provideEntity(world);
			
			if (ent instanceof MagmaCube) {
				// Make it large boi
				CompoundTag customTag = new CompoundTag();
				customTag.putInt("Size", 4);
				ent.readAdditionalSaveData(customTag);
			} else if (ent instanceof WilloEntity) {
				((WilloEntity) ent).setElement(EMagicElement.FIRE);
			} else if (ent instanceof KoidEntity) {
				((KoidEntity) ent).setElement(EMagicElement.FIRE);
			}
			
			return ent;
		}
	}
}
