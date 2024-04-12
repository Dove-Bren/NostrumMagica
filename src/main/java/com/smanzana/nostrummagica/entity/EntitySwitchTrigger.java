package com.smanzana.nostrummagica.entity;


import com.smanzana.nostrummagica.spells.components.MagicDamageSource;
import com.smanzana.nostrummagica.tiles.SwitchBlockTileEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntitySwitchTrigger extends MobEntity {
	
	public static final String ID = "entity_switch_trigger";
	
	private BlockPos cachePos;
	private SwitchBlockTileEntity cacheEntity;
	
	public EntitySwitchTrigger(EntityType<? extends EntitySwitchTrigger> type, World worldIn) {
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
	
	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1D);
	}
	
	
	@Override
	public void knockBack(Entity entityIn, float strenght, double xRatio, double zRatio) {
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
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		TileEntity te = world.getTileEntity(getPosition());
		if (te == null || !(te instanceof SwitchBlockTileEntity)) {
			return false;
		}
		
		boolean magic = false;
		
		if (source instanceof MagicDamageSource) {
			magic = true;
		}
		
		((SwitchBlockTileEntity) te).trigger(magic);
		return true;
	}
	
	public SwitchBlockTileEntity getLinkedTileEntity() {
		if (this.cachePos == null || this.cacheEntity == null || !this.getPosition().equals(cachePos) || cacheEntity.getTriggerEntity() != this) {
			cacheEntity = null;
			this.cachePos = this.getPosition().toImmutable();
			TileEntity te = world.getTileEntity(cachePos);
			if (te != null && te instanceof SwitchBlockTileEntity) {
				SwitchBlockTileEntity ent = (SwitchBlockTileEntity) te;
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
				
				// Should be on top of a trigger block
				if (getLinkedTileEntity() == null) {
					this.remove();
				}
			}
		}
	}
}
