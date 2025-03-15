package com.smanzana.nostrummagica.tile;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.entity.TileProxyTriggerEntity;

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

public abstract class EntityProxiedTileEntity<E extends TileProxyTriggerEntity<?>> extends TileEntity implements ITickableTileEntity {

	private E triggerEntity;
	
	protected EntityProxiedTileEntity(TileEntityType<? extends EntityProxiedTileEntity<E>> type) {
		super(type);
	}
	
	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(this.worldPosition, 3, this.getUpdateTag());
	}

	@Override
	public CompoundNBT getUpdateTag() {
		return this.save(new CompoundNBT());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		super.onDataPacket(net, pkt);
		handleUpdateTag(this.getBlockState(), pkt.getTag());
	}
	
	protected void dirty() {
		level.sendBlockUpdated(worldPosition, this.level.getBlockState(worldPosition), this.level.getBlockState(worldPosition), 3);
		setChanged();
	}
	
	protected abstract E makeTriggerEntity(World world, double x, double y, double z);
	
	public @Nullable E getTriggerEntity() {
		return this.triggerEntity;
	}
	
	public abstract void trigger(@Nullable LivingEntity entity, DamageSource source, float damage);
	
	protected Vector3d getEntityOffset() {
		return new Vector3d(.5, 0, .5);
	}
	
	protected boolean shouldHaveProxy() {
		return true;
	}
	
	@Override
	public void tick() {
		if (level.isClientSide) {
			return;
		}
		
		if (shouldHaveProxy()) {
			// Create entity here if it doesn't exist
			Vector3d offset = this.getEntityOffset();
			if (triggerEntity == null || !triggerEntity.isAlive() || triggerEntity.level != this.level
					|| triggerEntity.distanceToSqr(worldPosition.getX() + offset.x(), worldPosition.getY() + offset.y(), worldPosition.getZ() + offset.z()) > 1.5) {
				// Entity is dead OR is too far away
				if (triggerEntity != null && !triggerEntity.isAlive()) {
					triggerEntity.remove();
				}
				
				triggerEntity = makeTriggerEntity(this.getLevel(), worldPosition.getX() + offset.x(), worldPosition.getY() + offset.y(), worldPosition.getZ() + offset.z());
				level.addFreshEntity(triggerEntity);
			}
		} else {
			if (this.triggerEntity != null) {
				this.triggerEntity.remove();
				this.triggerEntity = null;
			}
		}
	}
	
	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		nbt = super.save(nbt);
		
		return nbt;
	}
	
	@Override
	public void load(BlockState state, CompoundNBT nbt) {
		super.load(state, nbt);
	}
}