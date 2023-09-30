package com.smanzana.nostrummagica.entity;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.serializers.MagicElementDataSerializer;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.triggers.MortarTrigger.MortarTriggerInstance;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntitySpellMortar extends EntityFireball {
	
	protected static final DataParameter<EMagicElement> ELEMENT = EntityDataManager.<EMagicElement>createKey(EntitySpellMortar.class, MagicElementDataSerializer.instance);
	
	private MortarTriggerInstance trigger;
	private Vec3d origin;
	
	private double gravity;
	
	private @Nullable Predicate<Entity> filter;

	public EntitySpellMortar(World world) {
		super(world);
        this.setSize(0.75F, 0.75F);
	}
	
	public EntitySpellMortar(MortarTriggerInstance trigger, LivingEntity shooter,
			World world, Vec3d start, Vec3d velocity,
			float speedFactor, double gravity) {
		super(world, start.x, start.y, start.z, 0, 0, 0);
        this.setSize(0.75F, 0.75F);
		this.accelerationX = 0; // have no be non-zero or they're NAN lol
		this.accelerationY = 0;
		this.accelerationZ = 0;
        this.getMotion().x = velocity.x;
        this.getMotion().y = velocity.y;
        this.getMotion().z = velocity.z;
		this.shootingEntity = shooter;
		
		this.trigger = trigger;
		this.origin = start;
		this.gravity = gravity;
		
		this.setElement(trigger.getElement());
		
//		System.out.println("Starting at [" + this.posX + ", " + this.posY + ", " + this.posZ + "] -> ("
//					+ this.getMotion().x + ", " + this.getMotion().y + ", " + this.getMotion().z + ")");
	}
	
	@Override
	protected void entityInit() {
		super.entityInit();
		
		this.dataManager.register(ELEMENT, EMagicElement.PHYSICAL);
	}
	
	public void setFilter(@Nullable Predicate<Entity> filter) {
		this.filter = filter;
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		
		// if client
//		if (this.ticksExisted % 5 == 0) {
//			this.world.addParticle(ParticleTypes.CRIT_MAGIC,
//					posX, posY, posZ, 0, 0, 0);
//		}
		
		if (!world.isRemote) {
			if (origin == null) {
				// We got loaded...
				this.setDead();
				return;
			}
			
			// Gravity!
			this.getMotion().y -= gravity;
			
//			System.out.println("[" + this.posX + ", " + this.posY + ", " + this.posZ + "] -> ("
//					+ this.getMotion().x + ", " + this.getMotion().y + ", " + this.getMotion().z + ")"
//					);
		} else {
			int color = getElement().getColor();
			color = (0x19000000) | (color & 0x00FFFFFF);
			NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
					2,
					posX, posY + height/2f, posZ, 0, 40, 0,
					new Vec3d(rand.nextFloat() * .05 - .025, rand.nextFloat() * .05, rand.nextFloat() * .05 - .025), null
				).color(color));
		}
	}
	
	@Override
	protected float getMotionFactor() {
		return 1f; // no friction
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		if (world.isRemote)
			return;
		
		if (result.typeOfHit == RayTraceResult.Type.MISS) {
			; // Do nothing
		} else if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
			trigger.onProjectileHit(new BlockPos(result.hitVec));
			this.setDead();
		} else if (result.typeOfHit == RayTraceResult.Type.ENTITY) {
			if (filter == null || filter.apply(result.entityHit)) {
				if ((result.entityHit != shootingEntity && !shootingEntity.isRidingOrBeingRiddenBy(result.entityHit))
						|| this.ticksExisted > 20) {
					trigger.onProjectileHit(result.entityHit);
					this.setDead();
				}
			}
		}
	}
	
	@Override
	public boolean writeToNBTOptional(CompoundNBT compound) {
		// Returning false means we won't be saved. That's what we want.
		return false;
    }
	
	@Override
	protected boolean isFireballFiery() {
		return false;
	}
	
	@Override
	protected ParticleTypes getParticleType() {
		return ParticleTypes.SUSPENDED;
	}
	
	public void setElement(EMagicElement element) {
		this.dataManager.set(ELEMENT, element);
	}
	
	public EMagicElement getElement() {
		return this.dataManager.get(ELEMENT);
	}
}
