package com.smanzana.nostrummagica.entity;

import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public abstract class EntityDragonFlying extends EntityDragon {

	protected static enum FlyState {
		LANDED,
		TAKING_OFF,
		FLYING,
		LANDING,
	}
	
	private static final DataParameter<Integer> DRAGON_FLYING =
			EntityDataManager.<Integer>createKey(EntityDragonFlying.class, DataSerializers.VARINT);
	
	private static final String DRAGON_SERIAL_FLYING_TOK = "DragonFlying";

	// How long landing or taking off takes, in milliseconds
	public static long ANIM_UNFURL_DUR = 1000;
	
	// Time we entered our current flying state.
	// Used for animations.
	// Set on every state change, meaning the time indicated
	// depends on our state.
	protected long flyStateTime;
	
	public EntityDragonFlying(World worldIn) {
		super(worldIn);
		
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
	
	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(DRAGON_FLYING, FlyState.LANDED.ordinal());
	}
	
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);

        if (compound.hasKey(DRAGON_SERIAL_FLYING_TOK, NBT.TAG_ANY_NUMERIC)) {
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
	
	public void writeEntityToNBT(NBTTagCompound compound) {
    	super.writeEntityToNBT(compound);
        compound.setByte(DRAGON_SERIAL_FLYING_TOK, (byte)this.getFlyState().ordinal());
	}
	
	public void startFlying() {
		if (getFlyState() == FlyState.LANDED) {
			setFlyState(FlyState.TAKING_OFF);
		}
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
		if (!this.worldObj.isRemote) {
			this.moveHelper = new EntityDragon.DragonFlyMoveHelper(this);
			this.navigator = new EntityDragon.PathNavigateDragonFlier(this, worldObj);
			this.setFlyingAI();
			this.addVelocity(Math.cos(this.rotationYaw) * .2, 0.5, Math.sin(this.rotationYaw) * .2);
		}
		this.setNoGravity(true);
		
	}
	
	protected void entityStopFlying() {
		if (!this.worldObj.isRemote) {
			this.moveHelper = new EntityMoveHelper(this);
			this.navigator = this.getNewNavigator(worldObj);
			this.setGroundedAI();
		}
		this.setNoGravity(false);
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		
		//System.out.println("(" + this.posX + ", " + this.posZ + ")");
		
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

}
