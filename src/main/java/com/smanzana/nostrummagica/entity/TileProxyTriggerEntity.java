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
	}
	
	@Override
	public boolean isInvulnerableTo(DamageSource source) {
		return false;
	}
	
	public static final AttributeModifierMap.MutableAttribute BuildAttributes() {
		return MobEntity.func_233666_p_()
				.createMutableAttribute(Attributes.MAX_HEALTH, 1D);
	}
	
	@Override
	public void applyKnockback(float strenght, double xRatio, double zRatio) {
		return; // Do not get knocked around
	}
	
	@Override
	public boolean canBePushed() {
		return false;
	}
	
	@Override
	public void applyEntityCollision(Entity entityIn) {
		return;
	}
	
	protected void collideWithEntity(Entity entity) {
		;
	}
	
	@Override
	protected void collideWithNearbyEntities() {
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
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.world.isRemote()) {
			return true;
		}
		
		E te = getLinkedTileEntity();
		if (te == null) {
			return false;
		}
		
		@Nullable LivingEntity livingSource = (source.getTrueSource() != null && source.getTrueSource() instanceof LivingEntity)
				? (LivingEntity) source.getTrueSource()
				: null;
		if (canBeHitBy(livingSource)) {
			te.trigger(livingSource, source, amount);
			return true;
		}
		return false;
	}
	
	protected BlockPos getCheckPos() {
		return this.getPosition();
	}
	
	@SuppressWarnings("unchecked")
	public E getLinkedTileEntity() {
		final BlockPos checkPos = getCheckPos();
		if (this.cachePos == null || this.cacheEntity == null || !checkPos.equals(cachePos) || cacheEntity.getTriggerEntity() != this) {
			cacheEntity = null;
			this.cachePos = checkPos.toImmutable();
			TileEntity te = world.getTileEntity(cachePos);
			if (te != null && te instanceof EntityProxiedTileEntity) {
				E ent = (E) te;
				if (world.isRemote || ent.getTriggerEntity() == this) {
					cacheEntity = ent;
				}
			}
		}
		
		return this.cacheEntity;
	}
	
	@Override
	public void livingTick() {
		super.livingTick();
		
		setInvulnerable(false);
		
		if (this.isAlive() && !this.dead) {
			if (!world.isRemote && this.ticksExisted > 20) {
				
				if (this.ticksExisted % 20 == 0) {
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
