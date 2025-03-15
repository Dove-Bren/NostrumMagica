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
import net.minecraft.entity.monster.MagmaCubeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class CombatTrialFire extends CombatTrialStaged {
	
	protected CombatTrialFire(ServerWorld world, BlockPos center, PlayerEntity player) {
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
		public TweakedMobPool(EntityType<? extends MobEntity> ... types) {
			super(types);
		}
		
		@Override
		public MobEntity provideEntity(World world) {
			MobEntity ent = super.provideEntity(world);
			
			if (ent instanceof MagmaCubeEntity) {
				// Make it large boi
				CompoundNBT customTag = new CompoundNBT();
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
