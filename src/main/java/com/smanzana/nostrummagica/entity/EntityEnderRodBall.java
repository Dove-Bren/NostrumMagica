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

public class EntityEnderRodBall extends Entity {
	
	public static final String ID = "entity_ender_rod_ball";
	
	private final @Nullable LivingEntity owner;
	
	public EntityEnderRodBall(EntityType<? extends EntityEnderRodBall> type, World worldIn) {
		this(type, worldIn, null);
	}
	
	public EntityEnderRodBall(EntityType<? extends EntityEnderRodBall> type, World worldIn, @Nullable LivingEntity owner) {
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
	public boolean writeUnlessRemoved(CompoundNBT compound) {
		// Returning false means we won't be saved. That's what we want.
		return false;
	}
	
	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
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
	public boolean attackEntityFrom(DamageSource source, float amount) {
		return false;
	}

	@Override
	protected void registerData() {
		;
	}

	@Override
	protected void readAdditional(CompoundNBT compound) {
		;
	}

	@Override
	protected void writeAdditional(CompoundNBT compound) {
		;
	}
	
	public @Nullable LivingEntity getOwner() {
		return this.owner;
	}
}
