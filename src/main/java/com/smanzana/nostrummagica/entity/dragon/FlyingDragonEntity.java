package com.smanzana.nostrummagica.entity.dragon;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.Level;

public abstract class FlyingDragonEntity extends DragonEntity {

	protected static enum FlyState {
		LANDED,
		TAKING_OFF,
		FLYING,
		LANDING,
	}
	
	private static final EntityDataAccessor<Integer> DRAGON_FLYING =
			SynchedEntityData.<Integer>defineId(FlyingDragonEntity.class, EntityDataSerializers.INT);
	
	private static final String DRAGON_SERIAL_FLYING_TOK = "DragonFlying";

	// How long landing or taking off takes, in milliseconds
	public static long ANIM_UNFURL_DUR = 1000;
	
	protected static float WING_FLAP_PER_TICK = (1f / 20f * 1f);
	
	// Time we entered our current flying state.
	// Used for animations.
	// Set on every state change, meaning the time indicated
	// depends on our state.
	protected long flyStateTime;
	
	// Like vanilla's swing progress but for wing flags
	protected float wingFlapProgress;
	protected float wingFlapSpeed = 1f; // Relative speed. 1f is normal speed
	
	public FlyingDragonEntity(EntityType<? extends FlyingDragonEntity> type, Level worldIn) {
		super(type, worldIn);
		
		this.setFlyState(FlyState.LANDED);
	}
	
	protected FlyState getFlyState() {
		return FlyState.values()[this.entityData.get(DRAGON_FLYING).intValue()];
	}
	
	protected void setFlyState(FlyState state) {
		this.entityData.set(DRAGON_FLYING, state.ordinal());
	}
	
	private void onFlightStateChange() {
		flyStateTime = System.currentTimeMillis();
		
		FlyState state = getFlyState();
		if (state == FlyState.FLYING) {
			entityStartFlying();
		} else if (state == FlyState.LANDED) {
			entityStopFlying();
		}
	}
	
	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		super.onSyncedDataUpdated(key);
		if (key == DRAGON_FLYING) {
			onFlightStateChange();
		}
	}
	
	public boolean isFlying() {
		FlyState state = getFlyState();
		return state == FlyState.FLYING
				|| (state == FlyState.LANDING && !this.onGround);
	}
	
	// Bad name, but are we currently landing or taking off?
	public boolean isFlightTransitioning() {
		FlyState state = getFlyState();
		return state == FlyState.LANDING
				|| state == FlyState.TAKING_OFF;
	}
	
	// For use in conjunction with isFlightTransitioning.
	public boolean isLanding() {
		FlyState state = getFlyState();
		return state == FlyState.LANDING;
	}
	
	@Override
	public boolean isTryingToLand() {
		return isLanding();
	}
	
	public long getFlyStateTime() {
		return flyStateTime;
	}
	
	public float getWingFlag(float partialTicks) {
		return getWingFlapping()
				? wingFlapProgress + (partialTicks * WING_FLAP_PER_TICK * wingFlapSpeed)
				: 0f;
	}
	
	public void flapWing(float speed) {
		if (!getWingFlapping()) {
			this.wingFlapSpeed = speed;
			this.wingFlapProgress = WING_FLAP_PER_TICK * wingFlapSpeed;
		}
	}
	
	public boolean getWingFlapping() {
		return wingFlapProgress > 0f && wingFlapProgress < 1f;
	}
	
	public void flapWing() {
		flapWing(1f);
	}
	
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DRAGON_FLYING, FlyState.LANDED.ordinal());
	}
	
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);

        if (compound.contains(DRAGON_SERIAL_FLYING_TOK, Tag.TAG_ANY_NUMERIC)) {
        	int i = compound.getByte(DRAGON_SERIAL_FLYING_TOK);
        	FlyState state = FlyState.values()[i];
        	if (state == FlyState.LANDING) {
        		// Actaully flying. Cancel landing
        		state = FlyState.FLYING;
        	} else if (state == FlyState.TAKING_OFF) {
        		// Still on the ground
        		state = FlyState.LANDED;
        	}
            this.setFlyState(state);
        }
	}
	
	public void addAdditionalSaveData(CompoundTag compound) {
    	super.addAdditionalSaveData(compound);
        compound.putByte(DRAGON_SERIAL_FLYING_TOK, (byte)this.getFlyState().ordinal());
	}
	
	public void startFlying() {
		int unused; // too laggy when flying so trying to turn off for now :(
//		if (getFlyState() == FlyState.LANDED) {
//			setFlyState(FlyState.TAKING_OFF);
//		}
	}
	
	public void startLanding() {
		if (getFlyState() == FlyState.FLYING) {
			setFlyState(FlyState.LANDING);
		}
	}
	
	protected abstract void setFlyingAI();
	
	protected abstract void setGroundedAI();
	
	// Actually start flying. Called internally when animations are done.
	protected void entityStartFlying() {
		if (!this.level.isClientSide) {
			this.moveControl = new DragonEntity.DragonFlyMoveHelper(this);
			this.navigation = new DragonEntity.PathNavigatorDragonFlier(this, level);
			this.setFlyingAI();
		}
		this.push(Math.cos(this.getYRot()) * .2, 0.5, Math.sin(this.getYRot()) * .2);
		this.setNoGravity(true);
		
	}
	
	protected void entityStopFlying() {
		if (!this.level.isClientSide) {
			this.moveControl = new MoveControl(this);
			this.navigation = this.createNavigation(level);
			this.setGroundedAI();
		}
		this.setNoGravity(false);
	}
	
	@Override
	public void tick() {
		super.tick();
		
		//System.out.println("(" + this.getPosX() + ", " + this.getPosZ() + ")");
		
		long now = System.currentTimeMillis();
		if (isFlightTransitioning()) {
			// Still unfurling wings and stuff. Wait to transition!
			boolean landing = (FlyState.LANDING == getFlyState());
			
			if (landing && !this.onGround) {
				; // Let movement AI keep going till we find ground
			} else {
				if (now - flyStateTime >= ANIM_UNFURL_DUR) {
					if (landing) {
						setFlyState(FlyState.LANDED);
					} else {
						setFlyState(FlyState.FLYING);
					}
				}
			}
		}
		
	}
	
	@Override
	public void baseTick() {
		super.baseTick();
		
		if (this.isFlying()) {
			if (this.getWingFlapping()) {
				this.wingFlapProgress += WING_FLAP_PER_TICK * this.wingFlapSpeed;
			}
		}
	}
	
	protected static final AttributeSupplier.Builder BuildBaseFlyingAttributes() {
		return DragonEntity.BuildBaseDragonAttributes()
				.add(Attributes.FLYING_SPEED);
	}

}
