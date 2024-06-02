package com.smanzana.nostrummagica.trial;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

/**
 * A combat trial with stages of mob spawns that wait for the prevous spawns to die
 */
public abstract class CombatTrialStaged extends CombatTrial {

	protected CombatTrialStage[] stages;
	protected int stage;
	protected boolean success;
	
	protected CombatTrialStaged(ServerWorld world, BlockPos center, @Nullable PlayerEntity player, CombatTrialStage ... stages) {
		super(world, center, player);
		this.stages = stages;
		stage = 0;
		success = false;
	}
	
	protected CombatTrialStaged(ServerWorld world, BlockPos center, @Nullable PlayerEntity player) {
		this(world, center, player, (CombatTrialStage[])null);
	}
	
	protected void setStages(CombatTrialStage ... stages) {
		this.stages = stages;
	}
	
	public int getStageCount() {
		return stages.length;
	}
	
	public int getCurrentStageIndex() {
		return this.stage;
	}
	
	protected @Nullable CombatTrialStage getCurrentStage() {
		final int idx = this.getCurrentStageIndex(); 
		return idx < stages.length ? stages[idx] : null;
	}
	
	@Override
	public boolean isComplete() {
		return this.getCurrentStageIndex() >= this.getStageCount();
	}
	
	@Override
	public boolean wasSuccess() {
		return this.success;
	}
	
	protected void startStage(CombatTrialStage stage) {
		stage.spawnStage(world, center, this.focusedPlayer);
	}
	
	protected void advanceStage() {
		this.stage++;
		if (!isComplete()) {
			startStage(this.getCurrentStage());
		} else {
			success = true;
		}
	}
	
	protected boolean isStageComplete(CombatTrialStage stage) {
		return stage.isComplete();
	}
	
	@Override
	public void startTrial() {
		// Crash if no stages?
		startStage(this.getCurrentStage());
	}

	@Override
	public void endTrial() {
		if (!this.isComplete()) {
			this.getCurrentStage().stopStage();
			success = false;
			stage = this.getStageCount();
		}
	}
	
	@Override
	public void trialTick() {
		super.trialTick();
		
		// super may cancel us, so check if we're still good to run
		if (!this.isComplete()) {
			if (isStageComplete(getCurrentStage())) {
				advanceStage();
			}
		}
	}
	
	protected static final List<BlockPos> findCacheableSpawnSuggestions(World world, BlockPos center, Predicate<BlockState> filter) {
		List<BlockPos> list = new ArrayList<>();
		
		BlockPos.Mutable cursor = new BlockPos.Mutable();
		for (int x = -10; x <= 10; x++)
		for (int z = -10; z <= 10; z++)
		for (int y = -2; y <= 2; y++) {
			cursor.setPos(center.getX() + x, center.getY() + y, center.getZ() + z);
			if (filter.test(world.getBlockState(cursor))) {
				list.add(cursor.toImmutable());
			}
		}
		
		return list;
	}
	
	/**
	 * Like vanilla mob spawners: just random
	 * @param world
	 * @param center
	 * @param entity
	 * @return
	 */
	protected static final BlockPos findRandomSpawnPos(World world, BlockPos center, LivingEntity entity) {
		int attempts = 50; // Vanilla just doesn't spawn if it can't find one
		while (attempts-- > 0) {
			BlockPos attemptSpot = center.add(
					NostrumMagica.rand.nextInt(16) - 8,
					NostrumMagica.rand.nextInt(3) - 1,
					NostrumMagica.rand.nextInt(16) - 8
					);
			if (world.isAirBlock(attemptSpot)) {
				if (entity.getHeight() <= 1 || world.isAirBlock(attemptSpot.up())) {
					return attemptSpot;
				}
			}
		}
		
		return center.up().up();
	}
	
	protected static interface TrialMobProvider {
		public MobEntity provideEntity(World world);
	}
	
	protected static interface TrialSpawnProvider {
		public BlockPos provideSpawn(World world, BlockPos center, MobEntity entity);
	}
	
	protected static class CombatTrialStage {
		
		protected final TrialMobProvider provider;
		protected final TrialSpawnProvider spawnFinder;
		protected final int numEntities;
		protected final List<MobEntity> stageEntities;
		protected final @Nullable PlayerEntity focusPlayer;
		
		public CombatTrialStage(@Nullable PlayerEntity focusPlayer, TrialMobProvider provider, TrialSpawnProvider spawnFinder, int numEntities) {
			stageEntities = new ArrayList<>();
			this.provider = provider;
			this.spawnFinder = spawnFinder;
			this.numEntities = numEntities;
			this.focusPlayer = focusPlayer;
		}
		
		public void spawnStage(World world, BlockPos center, @Nullable PlayerEntity focusPlayer) {
			for (int i = 0; i < numEntities; i++) {
				stageEntities.add(spawnOneEntity(world, center, focusPlayer));
			}
		}
		
		protected MobEntity spawnOneEntity(World world, BlockPos center, @Nullable PlayerEntity focusPlayer) {
			final MobEntity ent = genEntity(world);
			final BlockPos spawn = findSpawnPos(world, center, ent);
			ent.setPosition(spawn.getX() + .5, spawn.getY(), spawn.getZ() + .5);
			world.addEntity(ent);
			if (focusPlayer != null) {
				ent.setAttackTarget(focusPlayer);
			}
			CombatTrial.playSpawnEffects(center, ent);
			return ent;
		}
		
		protected BlockPos findSpawnPos(World world, BlockPos center, MobEntity ent) {
			return this.spawnFinder.provideSpawn(world, center, ent);
		}
		
		protected MobEntity genEntity(World world) {
			return this.provider.provideEntity(world);
		}
		
		protected boolean isComplete() {
			// Clean up dead entities, and then return if list is empty
			Iterator<MobEntity> it = this.stageEntities.iterator();
			while (it.hasNext()) {
				MobEntity ent = it.next();
				if (ent == null || !ent.isAlive()) {
					it.remove();
				}
			}
			
			return stageEntities.isEmpty();
		}
		
		public void stopStage() {
			for (MobEntity ent : this.stageEntities) {
				ent.remove();
			}
		}
	}
	
	protected static class RandomPoolMobProvider implements TrialMobProvider {

		protected final List<EntityType<? extends MobEntity>> types;
		
		@SafeVarargs
		public RandomPoolMobProvider(EntityType<? extends MobEntity> ... types) {
			this.types = new ArrayList<>(types.length);
			for (EntityType<? extends MobEntity> type : types) {
				this.types.add(type);
			}
		}
		
		@Override
		public MobEntity provideEntity(World world) {
			return types.get(NostrumMagica.rand.nextInt(types.size())).create(world);
		}
	}
	
	protected static class CachedSpawnProvider implements TrialSpawnProvider {
		private final List<BlockPos> spawnCandidates;
		
		public CachedSpawnProvider(World world, BlockPos center, Predicate<BlockState> filter) {
			spawnCandidates = findCacheableSpawnSuggestions(world, center, filter);
		}
		
		@Override
		public BlockPos provideSpawn(World world, BlockPos center, MobEntity entity) {
			if (this.spawnCandidates.isEmpty()) {
				return CombatTrialStaged.findRandomSpawnPos(world, center, entity);
			} else {
				BlockPos randSpawn = spawnCandidates.get(NostrumMagica.rand.nextInt(spawnCandidates.size()));
				return randSpawn.up();
			}
		}
	}
}
