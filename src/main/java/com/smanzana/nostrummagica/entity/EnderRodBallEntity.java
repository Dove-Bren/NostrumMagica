package com.smanzana.nostrummagica.entity;


import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

public class EnderRodBallEntity extends Entity {
	
	public static final String ID = "entity_ender_rod_ball";
	
	private final @Nullable LivingEntity owner;
	
	public EnderRodBallEntity(EntityType<? extends EnderRodBallEntity> type, Level worldIn) {
		this(type, worldIn, null);
	}
	
	public EnderRodBallEntity(EntityType<? extends EnderRodBallEntity> type, Level worldIn, @Nullable LivingEntity owner) {
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
	public boolean saveAsPassenger(CompoundTag compound) {
		// Returning false means we won't be saved. That's what we want.
		return false;
	}
	
	@Override
	public Packet<?> getAddEntityPacket() {
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
	protected void readAdditionalSaveData(CompoundTag compound) {
		;
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compound) {
		;
	}
	
	public @Nullable LivingEntity getOwner() {
		return this.owner;
	}
}
