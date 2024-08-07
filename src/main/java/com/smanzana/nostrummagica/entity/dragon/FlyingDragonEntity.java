package com.smanzana.nostrummagica.entity.dragon;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public abstract class FlyingDragonEntity extends DragonEntity {

	protected static enum FlyState {
		LANDED,
		TAKING_OFF,
		FLYING,
		LANDING,
	}
	
	private static final DataParameter<Integer> DRAGON_FLYING =
			EntityDataManager.<Integer>createKey(FlyingDragonEntity.class, DataSerializers.VARINT);
	
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
	
	public FlyingDragonEntity(EntityType<? extends FlyingDragonEntity> type, World worldIn) {
		super(type, worldIn);
		
		this.setFlyState(FlyState.LANDED);
	}
	
	protected FlyState getFlyState() {
		return FlyState.values()[this.dataManager.get(DRAGON_FLYING).intValue()];
	}
	
	protected void setFlyState(FlyState state) {
		this.dataManager.set(DRAGON_FLYING, state.ordinal());
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
	public void notifyDataManagerChange(DataParameter<?> key) {
		super.notifyDataManagerChange(key);
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
	protected void registerData() {
		super.registerData();
		this.dataManager.register(DRAGON_FLYING, FlyState.LANDED.ordinal());
	}
	
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);

        if (compound.contains(DRAGON_SERIAL_FLYING_TOK, NBT.TAG_ANY_NUMERIC)) {
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
	
	public void writeAdditional(CompoundNBT compound) {
    	super.writeAdditional(compound);
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
		if (!this.world.isRemote) {
			this.moveController = new DragonEntity.DragonFlyMoveHelper(this);
			this.navigator = new DragonEntity.PathNavigatorDragonFlier(this, world);
			this.setFlyingAI();
		}
		this.addVelocity(Math.cos(this.rotationYaw) * .2, 0.5, Math.sin(this.rotationYaw) * .2);
		this.setNoGravity(true);
		
	}
	
	protected void entityStopFlying() {
		if (!this.world.isRemote) {
			this.moveController = new MovementController(this);
			this.navigator = this.createNavigator(world);
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
	
	protected static final AttributeModifierMap.MutableAttribute BuildBaseFlyingAttributes() {
		return DragonEntity.BuildBaseDragonAttributes()
				.createMutableAttribute(Attributes.FLYING_SPEED);
	}

}
