package com.smanzana.nostrummagica.entity;


import com.smanzana.nostrummagica.spells.components.MagicDamageSource;
import com.smanzana.nostrummagica.tiles.SwitchBlockTileEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntitySwitchTrigger extends MobEntity {
	
	private BlockPos cachePos;
	private SwitchBlockTileEntity cacheEntity;
	
	public EntitySwitchTrigger(World worldIn) {
		super(worldIn);
		this.setSize(.8f, 1.8f);
		cachePos = null;
		cacheEntity = null;
		this.setNoGravity(true);
	}
	
	@Override
	public boolean isEntityInvulnerable(DamageSource source) {
		return true;
	}
	
	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(1D);
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
	public void onLivingUpdate() {
		super.onLivingUpdate();
		
		if (!this.isDead && !this.dead) {
			if (!world.isRemote && this.ticksExisted > 20) {
				
				if (this.ticksExisted % 20 == 0) {
					// Clear cache every once in a while
					this.cacheEntity = null;
				}
				
				// Should be on top of a trigger block
				if (getLinkedTileEntity() == null) {
					this.setDead();
				}
			}
		}
	}
}
