package com.smanzana.nostrummagica.tile;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.entity.TileProxyTriggerEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public abstract class EntityProxiedTileEntity<E extends TileProxyTriggerEntity<?>> extends BlockEntity implements TickableBlockEntity {

	private E triggerEntity;
	
	protected EntityProxiedTileEntity(BlockEntityType<? extends EntityProxiedTileEntity<E>> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}
	
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.saveWithoutMetadata();
	}
	
	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		super.onDataPacket(net, pkt);
		//handleUpdateTag(pkt.getTag());
	}
	
	protected void dirty() {
		level.sendBlockUpdated(worldPosition, this.level.getBlockState(worldPosition), this.level.getBlockState(worldPosition), 3);
		setChanged();
	}
	
	protected abstract E makeTriggerEntity(Level world, double x, double y, double z);
	
	public @Nullable E getTriggerEntity() {
		return this.triggerEntity;
	}
	
	public abstract void trigger(@Nullable LivingEntity entity, DamageSource source, float damage);
	
	protected Vec3 getEntityOffset() {
		return new Vec3(.5, 0, .5);
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
			Vec3 offset = this.getEntityOffset();
			if (triggerEntity == null || !triggerEntity.isAlive() || triggerEntity.level != this.level
					|| triggerEntity.distanceToSqr(worldPosition.getX() + offset.x(), worldPosition.getY() + offset.y(), worldPosition.getZ() + offset.z()) > 1.5) {
				// Entity is dead OR is too far away
				if (triggerEntity != null && !triggerEntity.isAlive()) {
					triggerEntity.discard();
				}
				
				triggerEntity = makeTriggerEntity(this.getLevel(), worldPosition.getX() + offset.x(), worldPosition.getY() + offset.y(), worldPosition.getZ() + offset.z());
				level.addFreshEntity(triggerEntity);
			}
		} else {
			if (this.triggerEntity != null) {
				this.triggerEntity.discard();
				this.triggerEntity = null;
			}
		}
	}
	
	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
	}
}