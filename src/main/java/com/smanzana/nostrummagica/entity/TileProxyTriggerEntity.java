package com.smanzana.nostrummagica.entity;


import javax.annotation.Nullable;

import com.smanzana.nostrummagica.spell.component.SpellAction;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.tile.EntityProxiedTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class TileProxyTriggerEntity<E extends EntityProxiedTileEntity<?>> extends Mob implements ISpellHandlingEntity {
	
	private BlockPos cachePos;
	private E cacheEntity;
	
	protected TileProxyTriggerEntity(EntityType<? extends TileProxyTriggerEntity<E>> type, Level worldIn) {
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
	
	public static final AttributeSupplier.Builder BuildAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 1D);
	}
	
	@Override
	public void knockback(double strenght, double xRatio, double zRatio) {
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
	
	@Override
	public boolean processSpellEffect(LivingEntity caster, SpellEffectPart effect, SpellAction action) {
		if (canBeHitBy(caster)) {
			E te = getLinkedTileEntity();
			if (te != null) {
				te.trigger(caster, effect, action);
			}
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
			BlockEntity te = level.getBlockEntity(cachePos);
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
					this.discard();
				}
			}
		}
	}
}
