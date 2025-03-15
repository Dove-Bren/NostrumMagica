package com.smanzana.nostrummagica.entity;


import javax.annotation.Nullable;

import com.smanzana.nostrummagica.tile.EntityProxiedTileEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class TileProxyTriggerEntity<E extends EntityProxiedTileEntity<?>> extends MobEntity {
	
	private BlockPos cachePos;
	private E cacheEntity;
	
	protected TileProxyTriggerEntity(EntityType<? extends TileProxyTriggerEntity<E>> type, World worldIn) {
		super(type, worldIn);
		cachePos = null;
		cacheEntity = null;
		this.setNoGravity(true);
		this.setInvulnerable(true);
		this.setPersistenceRequired();
	}
	
	@Override
	public boolean isInvulnerableTo(DamageSource source) {
		return false;
	}
	
	public static final AttributeModifierMap.MutableAttribute BuildAttributes() {
		return MobEntity.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 1D);
	}
	
	@Override
	public void knockback(float strenght, double xRatio, double zRatio) {
		return; // Do not get knocked around
	}
	
	@Override
	public boolean isPushable() {
		return false;
	}
	
	@Override
	public void push(Entity entityIn) {
		return;
	}
	
	protected void doPush(Entity entity) {
		;
	}
	
	@Override
	protected void pushEntities() {
		;
	}
	
	@Override
	protected int decreaseAirSupply(int air) {
		return air;
	}
	
	protected boolean canBeHitBy(@Nullable LivingEntity attacker) {
		return true;
	}
	
	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (this.level.isClientSide()) {
			return true;
		}
		
		E te = getLinkedTileEntity();
		if (te == null) {
			return false;
		}
		
		@Nullable LivingEntity livingSource = (source.getEntity() != null && source.getEntity() instanceof LivingEntity)
				? (LivingEntity) source.getEntity()
				: null;
		if (canBeHitBy(livingSource)) {
			te.trigger(livingSource, source, amount);
			return true;
		}
		return false;
	}
	
	protected BlockPos getCheckPos() {
		return this.blockPosition();
	}
	
	@SuppressWarnings("unchecked")
	public E getLinkedTileEntity() {
		final BlockPos checkPos = getCheckPos();
		if (this.cachePos == null || this.cacheEntity == null || !checkPos.equals(cachePos) || cacheEntity.getTriggerEntity() != this) {
			cacheEntity = null;
			this.cachePos = checkPos.immutable();
			TileEntity te = level.getBlockEntity(cachePos);
			if (te != null && te instanceof EntityProxiedTileEntity) {
				E ent = (E) te;
				if (level.isClientSide || ent.getTriggerEntity() == this) {
					cacheEntity = ent;
				}
			}
		}
		
		return this.cacheEntity;
	}
	
	@Override
	public void aiStep() {
		super.aiStep();
		
		setInvulnerable(false);
		
		if (this.isAlive() && !this.dead) {
			if (!level.isClientSide && this.tickCount > 20) {
				
				if (this.tickCount % 20 == 0) {
					// Clear cache every once in a while
					this.cacheEntity = null;
				}
				
				// Should be on top of a shrine block
				if (getLinkedTileEntity() == null) {
					this.remove();
				}
			}
		}
	}
}
