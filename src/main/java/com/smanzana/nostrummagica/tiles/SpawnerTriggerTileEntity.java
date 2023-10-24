package com.smanzana.nostrummagica.tiles;

import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.ITriggeredBlock;
import com.smanzana.nostrummagica.blocks.NostrumBlocks;
import com.smanzana.nostrummagica.blocks.NostrumSpawnAndTrigger;
import com.smanzana.nostrummagica.utils.Entities;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class SpawnerTriggerTileEntity extends SingleSpawnerTileEntity {
	
	private static final String NBT_ENTITY_ID = "entity_id";
	private static final String NBT_TRIGGER_OFFSET = "trigger_offset";
	
	protected LivingEntity entity;
	protected BlockPos triggerOffset;
	
	private UUID unlinkedEntID;
	
	public SpawnerTriggerTileEntity() {
		super(NostrumTileEntities.SpawnerTriggerTileEntityType);
	}
	
	public void setTriggerOffset(BlockPos offset) {
		triggerOffset = offset;
		this.markDirty();
	}
	
	public void setTriggerPosition(int x, int y, int z) {
		this.setTriggerOffset(new BlockPos(x - pos.getX(), y - pos.getY(), z - pos.getZ()));
	}
	
	public BlockPos getTriggerOffset() {
		return triggerOffset;
	}
	
	protected void trigger(BlockState state) {
		
		if (triggerOffset != null) {
			state = world.getBlockState(pos.add(this.triggerOffset));
			if (state.getBlock() instanceof ITriggeredBlock) {
				((ITriggeredBlock) state.getBlock()).trigger(world, pos.add(triggerOffset), state, pos);
			}
			triggerOffset = null;
		}
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
				unlinkedEntID = null;
				this.trigger(state);
				world.removeBlock(pos, false);
			}
		} else if (entity == null) {
			for (PlayerEntity player : ((ServerWorld) world).getPlayers()) {
				if (!player.isSpectator() && !player.isCreative() && player.getDistanceSq(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5) < NostrumSpawnAndTrigger.SPAWN_DIST_SQ) {
					entity = NostrumBlocks.triggerSpawner.spawn(world, pos, state, NostrumMagica.rand);
					world.notifyBlockUpdate(pos, state, state, 2);
					world.addBlockEvent(pos, state.getBlock(), 9, 0);
					this.markDirty();
					return;
				}
			}
		} else {
			if (!entity.isAlive()) {
				this.trigger(state);
				world.removeBlock(pos, false);
			}
		}
	}
	
	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		nbt = super.write(nbt);
		
		if (triggerOffset != null) {
			nbt.putLong(NBT_TRIGGER_OFFSET, triggerOffset.toLong());
		}
		if (entity != null) {
			nbt.putUniqueId(NBT_ENTITY_ID, entity.getUniqueID());
		} else if (this.unlinkedEntID != null) {
			nbt.putUniqueId(NBT_ENTITY_ID, this.unlinkedEntID);
		}
		
		return nbt;
	}
	
	@Override
	public void read(CompoundNBT nbt) {
		super.read(nbt);
		
		if (nbt.hasUniqueId(NBT_ENTITY_ID)) {
			this.unlinkedEntID = nbt.getUniqueId(NBT_ENTITY_ID);
		}
		
		if (nbt.contains(NBT_TRIGGER_OFFSET)) {
			this.triggerOffset = BlockPos.fromLong(nbt.getLong(NBT_TRIGGER_OFFSET)); // Warning: can break if save used across game versions
		}
	}
	
	public UUID getUnlinkedEntID() {
		return unlinkedEntID;
	}
	
	public @Nullable Entity getSpawnedEntity() {
		return entity;
	}
}