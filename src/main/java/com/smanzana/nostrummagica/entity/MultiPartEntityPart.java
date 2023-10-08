package com.smanzana.nostrummagica.entity;

import javax.annotation.Nonnull;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.Pose;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.DamageSource;

public class MultiPartEntityPart extends Entity implements IMultiPartEntityPart {

	protected final IMultiPartEntity parent;
	protected final String name;
	protected EntitySize size;
	
	public MultiPartEntityPart(@Nonnull IMultiPartEntity parent, String name, float width, float height) {
		super(((Entity) parent).getType(), parent.getWorld());
		this.parent = parent;
		this.name = name;
		
		this.setSize(width, height);
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
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		return this.isInvulnerableTo(source) ? false : getParent().attackEntityFromPart(this, source, amount);
	}
	
	@Override
	public EntitySize getSize(Pose poseIn) {
		return this.size;
	}
	
	public void setSize(EntitySize size) {
		this.size = size;
		this.recalculateSize();
	}
	
	public void setSize(float width, float height) {
		this.setSize(EntitySize.flexible(width, height));
	}
}
