package com.smanzana.nostrummagica.blocks.tiles;

import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.ITriggeredBlock;
import com.smanzana.nostrummagica.blocks.NostrumSpawnAndTrigger;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

public class SpawnerTriggerTileEntity extends SingleSpawnerTileEntity {
	
	private static final String NBT_ENTITY_ID = "entity_id";
	private static final String NBT_TRIGGER_OFFSET = "trigger_offset";
	
	protected LivingEntity entity;
	protected BlockPos triggerOffset;
	
	private UUID unlinkedEntID;
	
	public SpawnerTriggerTileEntity() {
		super();
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
	
	protected void trigger(IBlockState state) {
		
		if (triggerOffset != null) {
			state = world.getBlockState(pos.add(this.triggerOffset));
			if (state.getBlock() instanceof ITriggeredBlock) {
				((ITriggeredBlock) state.getBlock()).trigger(world, pos.add(triggerOffset), state, pos);
			}
			triggerOffset = null;
		}
	}
	
	@Override
	protected void majorTick(IBlockState state) {
		if (unlinkedEntID != null) {
			// Need to find our entity!
			for (LivingEntity ent : world.getEntities(LivingEntity.class, (ent) -> { return ent.getPersistentID().equals(unlinkedEntID);})) {
				this.entity = ent;
				unlinkedEntID = null;
				break;
			}
			
			if (entity == null && ticksExisted > 20 * 15) {
				// Give up
				unlinkedEntID = null;
				this.trigger(state);
				world.setBlockToAir(pos);
			}
		} else if (entity == null) {
			for (PlayerEntity player : world.playerEntities) {
				if (!player.isSpectator() && !player.isCreative() && player.getDistanceSq(pos) < NostrumSpawnAndTrigger.SPAWN_DIST_SQ) {
					entity = NostrumSpawnAndTrigger.instance().spawn(world, pos, state, NostrumMagica.rand);
					world.notifyBlockUpdate(pos, state, state, 2);
					world.addBlockEvent(pos, state.getBlock(), 9, 0);
					this.markDirty();
					return;
				}
			}
		} else {
			if (entity.isDead) {
				this.trigger(state);
				world.setBlockToAir(pos);
			}
		}
	}
	
	@Override
	public void update() {
		super.update();
	}
	
	@Override
	public CompoundNBT writeToNBT(CompoundNBT nbt) {
		nbt = super.writeToNBT(nbt);
		
		if (triggerOffset != null) {
			nbt.putLong(NBT_TRIGGER_OFFSET, triggerOffset.toLong());
		}
		if (entity != null) {
			nbt.setUniqueId(NBT_ENTITY_ID, entity.getPersistentID());
		} else if (this.unlinkedEntID != null) {
			nbt.setUniqueId(NBT_ENTITY_ID, this.unlinkedEntID);
		}
		
		return nbt;
	}
	
	@Override
	public void readFromNBT(CompoundNBT nbt) {
		super.readFromNBT(nbt);
		
		if (nbt.hasUniqueId(NBT_ENTITY_ID)) {
			this.unlinkedEntID = nbt.getUniqueId(NBT_ENTITY_ID);
		}
		
		if (nbt.contains(NBT_TRIGGER_OFFSET)) {
			this.triggerOffset = BlockPos.fromLong(nbt.getLong(NBT_TRIGGER_OFFSET));
		}
	}
	
	public UUID getUnlinkedEntID() {
		return unlinkedEntID;
	}
	
	public @Nullable Entity getSpawnedEntity() {
		return entity;
	}
}