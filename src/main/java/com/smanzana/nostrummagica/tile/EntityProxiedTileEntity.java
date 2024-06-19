package com.smanzana.nostrummagica.tile;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.entity.EntityTileProxyTrigger;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public abstract class EntityProxiedTileEntity<E extends EntityTileProxyTrigger<?>> extends TileEntity implements ITickableTileEntity {

	private E triggerEntity;
	
	protected EntityProxiedTileEntity(TileEntityType<? extends EntityProxiedTileEntity<E>> type) {
		super(type);
	}
	
	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(this.pos, 3, this.getUpdateTag());
	}

	@Override
	public CompoundNBT getUpdateTag() {
		return this.write(new CompoundNBT());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		super.onDataPacket(net, pkt);
		handleUpdateTag(this.getBlockState(), pkt.getNbtCompound());
	}
	
	protected void dirty() {
		world.notifyBlockUpdate(pos, this.world.getBlockState(pos), this.world.getBlockState(pos), 3);
		markDirty();
	}
	
	protected abstract E makeTriggerEntity(World world, double x, double y, double z);
	
	public @Nullable E getTriggerEntity() {
		return this.triggerEntity;
	}
	
	public abstract void trigger(LivingEntity entity, DamageSource source, float damage);
	
	protected Vector3d getEntityOffset() {
		return new Vector3d(.5, 0, .5);
	}
	
	@Override
	public void tick() {
		if (world.isRemote) {
			return;
		}
		
		// Create entity here if it doesn't exist
		Vector3d offset = this.getEntityOffset();
		if (triggerEntity == null || !triggerEntity.isAlive() || triggerEntity.world != this.world
				|| triggerEntity.getDistanceSq(pos.getX() + offset.getX(), pos.getY() + offset.getY(), pos.getZ() + offset.getZ()) > 1.5) {
			// Entity is dead OR is too far away
			if (triggerEntity != null && !triggerEntity.isAlive()) {
				triggerEntity.remove();
			}
			
			triggerEntity = makeTriggerEntity(this.getWorld(), pos.getX() + offset.getX(), pos.getY() + offset.getY(), pos.getZ() + offset.getZ());
			world.addEntity(triggerEntity);
		}
	}
	
	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		nbt = super.write(nbt);
		
		return nbt;
	}
	
	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
	}
}