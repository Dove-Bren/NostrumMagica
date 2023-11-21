package com.smanzana.nostrummagica.trials;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
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
		stage.spawnStage(world, center);
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
		
		MutableBlockPos cursor = new MutableBlockPos();
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
		public LivingEntity provideEntity(World world);
	}
	
	protected static interface TrialSpawnProvider {
		public BlockPos provideSpawn(World world, BlockPos center, LivingEntity entity);
	}
	
	protected static class CombatTrialStage {
		
		protected final TrialMobProvider provider;
		protected final TrialSpawnProvider spawnFinder;
		protected final int numEntities;
		protected final List<LivingEntity> stageEntities;
		
		public CombatTrialStage(TrialMobProvider provider, TrialSpawnProvider spawnFinder, int numEntities) {
			stageEntities = new ArrayList<>();
			this.provider = provider;
			this.spawnFinder = spawnFinder;
			this.numEntities = numEntities;
		}
		
		public void spawnStage(World world, BlockPos center) {
			for (int i = 0; i < numEntities; i++) {
				stageEntities.add(spawnOneEntity(world, center));
			}
		}
		
		protected LivingEntity spawnOneEntity(World world, BlockPos center) {
			final LivingEntity ent = genEntity(world);
			final BlockPos spawn = findSpawnPos(world, center, ent);
			ent.setPosition(spawn.getX() + .5, spawn.getY(), spawn.getZ() + .5);
			world.addEntity(ent);
			CombatTrial.playSpawnEffects(center, ent);
			return ent;
		}
		
		protected BlockPos findSpawnPos(World world, BlockPos center, LivingEntity ent) {
			return this.spawnFinder.provideSpawn(world, center, ent);
		}
		
		protected LivingEntity genEntity(World world) {
			return this.provider.provideEntity(world);
		}
		
		protected boolean isComplete() {
			// Clean up dead entities, and then return if list is empty
			Iterator<LivingEntity> it = this.stageEntities.iterator();
			while (it.hasNext()) {
				LivingEntity ent = it.next();
				if (ent == null || !ent.isAlive()) {
					it.remove();
				}
			}
			
			return stageEntities.isEmpty();
		}
		
		public void stopStage() {
			for (LivingEntity ent : this.stageEntities) {
				ent.remove();
			}
		}
	}
	
	protected static class RandomPoolMobProvider implements TrialMobProvider {

		protected final List<EntityType<? extends LivingEntity>> types;
		
		@SafeVarargs
		public RandomPoolMobProvider(EntityType<? extends LivingEntity> ... types) {
			this.types = new ArrayList<>(types.length);
			for (EntityType<? extends LivingEntity> type : types) {
				this.types.add(type);
			}
		}
		
		@Override
		public LivingEntity provideEntity(World world) {
			return types.get(NostrumMagica.rand.nextInt(types.size())).create(world);
		}
	}
	
	protected static class CachedSpawnProvider implements TrialSpawnProvider {
		private final List<BlockPos> spawnCandidates;
		
		public CachedSpawnProvider(World world, BlockPos center, Predicate<BlockState> filter) {
			spawnCandidates = findCacheableSpawnSuggestions(world, center, filter);
		}
		
		@Override
		public BlockPos provideSpawn(World world, BlockPos center, LivingEntity entity) {
			if (this.spawnCandidates.isEmpty()) {
				return CombatTrialStaged.findRandomSpawnPos(world, center, entity);
			} else {
				BlockPos randSpawn = spawnCandidates.get(NostrumMagica.rand.nextInt(spawnCandidates.size()));
				return randSpawn.up();
			}
		}
	}
}
