package com.smanzana.nostrummagica.tile;

import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.tile.IOrientedTileEntity;
import com.smanzana.autodungeons.world.blueprints.IBlueprint;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.ITriggeredBlock;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.block.dungeon.MatchSpawnerBlock;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.util.Entities;
import com.smanzana.nostrummagica.util.WorldUtil;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants.NBT;

public class MatchSpawnerTileEntity extends SingleSpawnerTileEntity implements IOrientedTileEntity {
	
	private static final String NBT_ENTITY_ID = "entity_id";
	private static final String NBT_TRIGGER_OFFSET = "trigger_offset";
	
	protected LivingEntity entity;
	protected BlockPos triggerOffset;
	
	private UUID unlinkedEntID;
	
	protected MatchSpawnerTileEntity(TileEntityType<? extends MatchSpawnerTileEntity> type) {
		super(type);
	}
	
	public MatchSpawnerTileEntity() {
		this(NostrumTileEntities.MatchSpawnerTileEntityType);
	}
	
	public void setTriggerOffset(BlockPos offset) {
		setTriggerOffset(offset, false);
	}
	
	public void setTriggerOffset(BlockPos offset, boolean isWorldGen) {
		triggerOffset = offset;
		if (!isWorldGen) {
			this.markDirty();
		}
	}
	
	public void setTriggerPosition(int x, int y, int z) {
		this.setTriggerOffset(new BlockPos(x - pos.getX(), y - pos.getY(), z - pos.getZ()));
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
			state = world.getBlockState(pos.add(this.triggerOffset));
			if (state.getBlock() instanceof ITriggeredBlock) {
				((ITriggeredBlock) state.getBlock()).trigger(world, pos.add(triggerOffset), state, pos);
			}
			triggerOffset = null;
		}
	}
	
	protected void updateBlockState() {
		world.setBlockState(pos, this.getBlockState().with(MatchSpawnerBlock.TRIGGERED, true), 3);
	}
	
	protected void spawnMatch(BlockState state) {
		entity = NostrumBlocks.matchSpawner.spawn(world, pos, state, NostrumMagica.rand);
		//world.notifyBlockUpdate(pos, state, state, 2);
		//world.addBlockEvent(pos, state.getBlock(), 9, 0);
		updateBlockState();
		this.markDirty();
	}
	
	protected boolean shouldSpawnMatch(BlockState state) {
		for (PlayerEntity player : ((ServerWorld) world).getPlayers()) {
			if (!player.isSpectator() && !player.isCreative() && player.getDistanceSq(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5) < MatchSpawnerBlock.SPAWN_DIST_SQ) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	protected void majorTick(BlockState state) {
		if (unlinkedEntID != null) {
			// Need to find our entity!
			@Nullable Entity foundEnt = Entities.FindEntity(world, unlinkedEntID);
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
				world.removeBlock(pos, false);
			}
		} else if (entity == null) {
			if (shouldSpawnMatch(state)) {
				spawnMatch(state);
				return;
			}
		} else {
			if (!entity.isAlive()) {
				NostrumMagicaSounds.AMBIENT_WOOSH2.play(world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
				this.doTrigger(state);
				world.removeBlock(pos, false);
			}
		}
	}
	
	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		nbt = super.write(nbt);
		
		if (triggerOffset != null) {
			nbt.put(NBT_TRIGGER_OFFSET, NBTUtil.writeBlockPos(triggerOffset));
		}
		if (entity != null) {
			nbt.putUniqueId(NBT_ENTITY_ID, entity.getUniqueID());
		} else if (this.unlinkedEntID != null) {
			nbt.putUniqueId(NBT_ENTITY_ID, this.unlinkedEntID);
		}
		
		return nbt;
	}
	
	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		
		if (nbt.hasUniqueId(NBT_ENTITY_ID)) {
			this.unlinkedEntID = nbt.getUniqueId(NBT_ENTITY_ID);
		}
		
		if (nbt.contains(NBT_TRIGGER_OFFSET, NBT.TAG_LONG)) {
			// Legacy!
			this.triggerOffset = WorldUtil.blockPosFromLong1_12_2(nbt.getLong(NBT_TRIGGER_OFFSET));
		} else if (nbt.contains(NBT_TRIGGER_OFFSET)) {
			this.triggerOffset = NBTUtil.readBlockPos(nbt.getCompound(NBT_TRIGGER_OFFSET));
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