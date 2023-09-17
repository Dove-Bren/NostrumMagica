package com.smanzana.nostrummagica.entity;

import javax.annotation.Nonnull;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.DamageSource;

public class MultiPartEntityPart extends Entity implements IMultiPartEntityPart {

	protected final IMultiPartEntity parent;
	
	public MultiPartEntityPart(@Nonnull IMultiPartEntity parent) {
		super(((Entity) parent).getType(), parent.getWorld());
		this.parent = parent;
		
	}
	
	@Override
	public boolean isEntityEqual(Entity entityIn) {
		return entityIn == this || entityIn == this.getParent();
	}
	
	@Override
	public @Nonnull IMultiPartEntity getParent() {
		return this.parent;
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

	@Override
	public IPacket<?> createSpawnPacket() {
		throw new UnsupportedOperationException(); // Copied from ender dragon
	}
	
	@Override
	public boolean canBeCollidedWith() {
		return true;
	}
	
	/**
	 * Called when the entity is attacked.
	 */
	public boolean attackEntityFrom(DamageSource source, float amount) {
		return this.isInvulnerableTo(source) ? false : getParent().attackEntityFromPart(this, source, amount);
	}
}
