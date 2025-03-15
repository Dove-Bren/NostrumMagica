package com.smanzana.nostrummagica.entity;


import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class EnderRodBallEntity extends Entity {
	
	public static final String ID = "entity_ender_rod_ball";
	
	private final @Nullable LivingEntity owner;
	
	public EnderRodBallEntity(EntityType<? extends EnderRodBallEntity> type, World worldIn) {
		this(type, worldIn, null);
	}
	
	public EnderRodBallEntity(EntityType<? extends EnderRodBallEntity> type, World worldIn, @Nullable LivingEntity owner) {
		super(type, worldIn);
		this.setNoGravity(true);
		this.setInvulnerable(true);
		this.owner = owner;
	}
	
	@Override
	public boolean isInvulnerableTo(DamageSource source) {
		return false;
	}
	
	@Override
	public boolean saveAsPassenger(CompoundNBT compound) {
		// Returning false means we won't be saved. That's what we want.
		return false;
	}
	
	@Override
	public IPacket<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
	
	@Override
	public boolean isPushable() {
		return false;
	}
	
	@Override
	public void push(Entity entityIn) {
		return;
	}
	
	protected void collideWithEntity(Entity entity) {
		;
	}
	
	@Override
	public boolean hurt(DamageSource source, float amount) {
		return false;
	}

	@Override
	protected void defineSynchedData() {
		;
	}

	@Override
	protected void readAdditionalSaveData(CompoundNBT compound) {
		;
	}

	@Override
	protected void addAdditionalSaveData(CompoundNBT compound) {
		;
	}
	
	public @Nullable LivingEntity getOwner() {
		return this.owner;
	}
}
