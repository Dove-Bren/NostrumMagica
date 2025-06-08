package com.smanzana.nostrummagica.tile;

import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.api.block.entity.IOrientedTileEntity;
import com.smanzana.autodungeons.world.blueprints.IBlueprint;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.ITriggeredBlock;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.block.dungeon.MatchSpawnerBlock;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.client.particles.ParticleTargetBehavior;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.util.Entities;
import com.smanzana.nostrummagica.util.TargetLocation;
import com.smanzana.nostrummagica.util.WorldUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MatchSpawnerTileEntity extends SingleSpawnerTileEntity implements IOrientedTileEntity {
	
	private static final String NBT_ENTITY_ID = "entity_id";
	private static final String NBT_TRIGGER_OFFSET = "trigger_offset";
	
	protected LivingEntity entity;
	protected BlockPos triggerOffset;
	
	private UUID unlinkedEntID;
	
	protected MatchSpawnerTileEntity(BlockEntityType<? extends MatchSpawnerTileEntity> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}
	
	public MatchSpawnerTileEntity(BlockPos pos, BlockState state) {
		this(NostrumBlockEntities.MatchSpawner, pos, state);
	}
	
	public void setTriggerOffset(BlockPos offset) {
		setTriggerOffset(offset, false);
	}
	
	public void setTriggerOffset(BlockPos offset, boolean isWorldGen) {
		triggerOffset = offset;
		if (!isWorldGen) {
			this.setChanged();
		}
	}
	
	public void setTriggerPosition(int x, int y, int z) {
		this.setTriggerOffset(new BlockPos(x - worldPosition.getX(), y - worldPosition.getY(), z - worldPosition.getZ()));
	}
	
	public BlockPos getTriggerOffset() {
		return triggerOffset;
	}
	
	/**
	 * Trigger target trigger block, presumably because the match has ended
	 * @param state
	 */
	protected void doTrigger(BlockState state) {
		
		if (triggerOffset != null) {
			state = level.getBlockState(worldPosition.offset(this.triggerOffset));
			if (state.getBlock() instanceof ITriggeredBlock) {
				((ITriggeredBlock) state.getBlock()).trigger(level, worldPosition.offset(triggerOffset), state, worldPosition);
			}
			triggerOffset = null;
		}
	}
	
	protected void updateBlockState() {
		level.setBlock(worldPosition, this.getBlockState().setValue(MatchSpawnerBlock.TRIGGERED, true), 3);
	}
	
	protected void spawnMatch(BlockState state) {
		entity = NostrumBlocks.matchSpawner.spawn(level, worldPosition, state, NostrumMagica.rand);
		//world.notifyBlockUpdate(pos, state, state, 2);
		//world.addBlockEvent(pos, state.getBlock(), 9, 0);
		updateBlockState();
		this.setChanged();
	}
	
	protected boolean shouldSpawnMatch(BlockState state) {
		for (Player player : ((ServerLevel) level).players()) {
			if (!player.isSpectator() && !player.isCreative() && player.distanceToSqr(worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5) < MatchSpawnerBlock.SPAWN_DIST_SQ) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	protected void majorTick(BlockState state) {
		if (unlinkedEntID != null) {
			// Need to find our entity!
			@Nullable Entity foundEnt = Entities.FindEntity(level, unlinkedEntID);
			if (foundEnt != null && foundEnt instanceof LivingEntity) {
				this.entity = (LivingEntity) foundEnt;
				unlinkedEntID = null;
			}
//			for (LivingEntity ent : world.getEntities(LivingEntity.class, (ent) -> { return ent.getPersistentID().equals(unlinkedEntID);})) {
//				this.entity = ent;
//				unlinkedEntID = null;
//				break;
//			}
			
			if (entity == null && ticksExisted > 20 * 15) {
				// Give up
				NostrumMagica.logger.warn("Trigger spawner failed to find spawned entity and is giving up.");
				unlinkedEntID = null;
				this.doTrigger(state);
				level.removeBlock(worldPosition, false);
			}
		} else if (entity == null) {
			if (shouldSpawnMatch(state)) {
				spawnMatch(state);
				return;
			}
		} else {
			if (!entity.isAlive()) {
				if (entity.getRemovalReason() != RemovalReason.UNLOADED_TO_CHUNK) {
					NostrumMagicaSounds.AMBIENT_WOOSH2.play(level, worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5);
					
					if (this.triggerOffset != null) {
						NostrumParticles.GLOW_TRAIL.spawn(level, new SpawnParams(1, entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
								0, 300, 0, new TargetLocation(Vec3.atCenterOf(worldPosition.offset(this.triggerOffset)))
								).setTargetBehavior(new ParticleTargetBehavior().joinMode(true)).color(1f, .8f, 1f, .3f));
					}
					
					this.doTrigger(state);
					level.removeBlock(worldPosition, false);
				} else {
					this.unlinkedEntID = entity.getUUID();
				}
			}
		}
	}
	
	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		if (triggerOffset != null) {
			nbt.put(NBT_TRIGGER_OFFSET, NbtUtils.writeBlockPos(triggerOffset));
		}
		if (entity != null) {
			nbt.putUUID(NBT_ENTITY_ID, entity.getUUID());
		} else if (this.unlinkedEntID != null) {
			nbt.putUUID(NBT_ENTITY_ID, this.unlinkedEntID);
		}
	}
	
	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		
		if (nbt.hasUUID(NBT_ENTITY_ID)) {
			this.unlinkedEntID = nbt.getUUID(NBT_ENTITY_ID);
		}
		
		if (nbt.contains(NBT_TRIGGER_OFFSET, Tag.TAG_LONG)) {
			// Legacy!
			this.triggerOffset = WorldUtil.blockPosFromLong1_12_2(nbt.getLong(NBT_TRIGGER_OFFSET));
		} else if (nbt.contains(NBT_TRIGGER_OFFSET)) {
			this.triggerOffset = NbtUtils.readBlockPos(nbt.getCompound(NBT_TRIGGER_OFFSET));
		}
	}
	
	public UUID getUnlinkedEntID() {
		return unlinkedEntID;
	}
	
	public @Nullable Entity getSpawnedEntity() {
		return entity;
	}

	@Override
	public void setSpawnedFromRotation(Direction rotation, boolean isWorldGen) {
		if (this.getTriggerOffset() != null) {
			this.setTriggerOffset(IBlueprint.ApplyRotation(this.getTriggerOffset(), rotation), isWorldGen);
		}
	}
}