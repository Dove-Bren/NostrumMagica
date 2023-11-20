package com.smanzana.nostrummagica.entity;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.smanzana.nostrummagica.blocks.MysticAnchor;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.serializers.MagicElementDataSerializer;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.triggers.MortarTrigger.MortarTriggerInstance;
import com.smanzana.nostrummagica.utils.RayTrace;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class EntitySpellMortar extends FireballEntity {
	
	public static final String ID = "spell_mortar";
	
	protected static final DataParameter<EMagicElement> ELEMENT = EntityDataManager.<EMagicElement>createKey(EntitySpellMortar.class, MagicElementDataSerializer.instance);
	
	private MortarTriggerInstance trigger;
	private Vec3d origin;
	
	private double gravity;
	
	private @Nullable Predicate<Entity> filter;

	public EntitySpellMortar(EntityType<? extends EntitySpellMortar> type, World world) {
		super(type, world);
	}
	
	public EntitySpellMortar(EntityType<? extends EntitySpellMortar> type, MortarTriggerInstance trigger, LivingEntity shooter,
			World world, Vec3d start, Vec3d velocity,
			float speedFactor, double gravity) {
		//super(world, start.x, start.y, start.z, 0, 0, 0);
		this(type, world);
		this.setPosition(start.x, start.y, start.z);
		this.accelerationX = 0; // have no be non-zero or they're NAN lol
		this.accelerationY = 0;
		this.accelerationZ = 0;
		this.setMotion(velocity);
		this.shootingEntity = shooter;
		
		this.trigger = trigger;
		this.origin = start;
		this.gravity = gravity;
		
		this.setElement(trigger.getElement());
		
//		System.out.println("Starting at [" + this.posX + ", " + this.posY + ", " + this.posZ + "] -> ("
//					+ this.getMotion().x + ", " + this.getMotion().y + ", " + this.getMotion().z + ")");
	}
	
	@Override
	protected void registerData() {
		super.registerData();
		
		this.dataManager.register(ELEMENT, EMagicElement.PHYSICAL);
	}
	
	public void setFilter(@Nullable Predicate<Entity> filter) {
		this.filter = filter;
	}
	
	@Override
	public void tick() {
		super.tick();
		
		// if client
//		if (this.ticksExisted % 5 == 0) {
//			this.world.addParticle(ParticleTypes.CRIT_MAGIC,
//					posX, posY, posZ, 0, 0, 0);
//		}
		
		if (!world.isRemote) {
			if (origin == null) {
				// We got loaded...
				this.remove();
				return;
			}
			
			// Gravity!
			this.setMotion(this.getMotion().add(0, -gravity, 0));
			
//			System.out.println("[" + this.posX + ", " + this.posY + ", " + this.posZ + "] -> ("
//					+ this.getMotion().x + ", " + this.getMotion().y + ", " + this.getMotion().z + ")"
//					);
		} else {
			int color = getElement().getColor();
			color = (0x19000000) | (color & 0x00FFFFFF);
			NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
					2,
					posX, posY + getHeight()/2f, posZ, 0, 40, 0,
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
		
		if (result.getType() == RayTraceResult.Type.MISS) {
			; // Do nothing
		} else if (result.getType() == RayTraceResult.Type.BLOCK) {
			BlockPos pos = RayTrace.blockPosFromResult(result);
			trigger.onProjectileHit(pos);
			
			// Proc mystic anchors if we hit one
			if (world.isAirBlock(pos)) pos = pos.down();
			BlockState state = world.getBlockState(pos);
			if (state.getBlock() instanceof MysticAnchor) {
				state.onEntityCollision(world, pos, this);
			}
			
			this.remove();
		} else if (result.getType() == RayTraceResult.Type.ENTITY) {
			final Entity entityHit = RayTrace.entFromRaytrace(result);
			if (filter == null || filter.apply(entityHit)) {
				if ((entityHit != shootingEntity && !shootingEntity.isRidingOrBeingRiddenBy(entityHit))
						|| this.ticksExisted > 20) {
					trigger.onProjectileHit(entityHit);
					this.remove();
				}
			}
		}
	}
	
	@Override
	public boolean writeUnlessRemoved(CompoundNBT compound) {
		// Returning false means we won't be saved. That's what we want.
		return false;
    }
	
	@Override
	protected boolean isFireballFiery() {
		return false;
	}
	
	@Override
	protected IParticleData getParticle() {
		return ParticleTypes.WITCH;
	}
	
	public void setElement(EMagicElement element) {
		this.dataManager.set(ELEMENT, element);
	}
	
	public EMagicElement getElement() {
		return this.dataManager.get(ELEMENT);
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		// Have to override and use forge to use with non-living Entity types even though parent defines
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}
