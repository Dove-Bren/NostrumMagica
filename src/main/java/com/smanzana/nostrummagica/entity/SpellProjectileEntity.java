package com.smanzana.nostrummagica.entity;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.block.dungeon.MysticAnchor;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.serializer.MagicElementDataSerializer;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.SpellLocation;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.DamagingProjectileEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

public class SpellProjectileEntity extends DamagingProjectileEntity {
	
	public static interface ISpellProjectileShape {

		public void onProjectileHit(SpellLocation location);
		
		public void onProjectileHit(Entity entity);
		
		public void onProjectileEnd(Vector3d pos);
		
		public EMagicElement getElement();
	}
	
	public static final String ID = "spell_projectile";
	protected static final DataParameter<EMagicElement> ELEMENT = EntityDataManager.<EMagicElement>createKey(SpellProjectileEntity.class, MagicElementDataSerializer.instance);
	
	// Generic projectile members
	protected final ISpellProjectileShape trigger;
	protected final Vector3d origin;
	protected LivingEntity shootingEntity;
	protected Predicate<Entity> filter;
	
	// Base class implementation variables
	private double maxDistance; // Squared distance so no sqrt
	
	public SpellProjectileEntity(EntityType<? extends SpellProjectileEntity> type, World world) {
		super(type, world);
		this.trigger = null;
		this.origin = null;
		this.shootingEntity = null;
	}
	
	protected SpellProjectileEntity(EntityType<? extends SpellProjectileEntity> type,
			ISpellProjectileShape trigger, World world, LivingEntity shooter,
			Vector3d origin, Vector3d direction,
			float speedFactor, double maxDistance) {
		super(type, origin.getX(), origin.getY(), origin.getZ(), 0, 0, 0, world);
		Vector3d accel = getAccel(direction, speedFactor);
		this.accelerationX = accel.x;
		this.accelerationY = accel.y;
		this.accelerationZ = accel.z;
		this.setShooter(shooter);
		
		this.shootingEntity = shooter;
		this.trigger = trigger;
		this.maxDistance = Math.pow(maxDistance, 2);
		this.origin = origin;
		
		this.setElement(trigger.getElement());
	}
	
	protected SpellProjectileEntity(EntityType<? extends SpellProjectileEntity> type, ISpellProjectileShape trigger,
			LivingEntity shooter, float speedFactor, double maxDistance) {
		this(type,
				trigger,
				shooter.world,
				shooter,
				shooter.getPositionVec(),
				shooter.getLookVec(),
				speedFactor, maxDistance
				);
	}
	
	public SpellProjectileEntity(ISpellProjectileShape trigger,	LivingEntity shooter, float speedFactor, double maxDistance) {
		this(NostrumEntityTypes.spellProjectile, trigger, shooter, speedFactor, maxDistance);
	}

	public SpellProjectileEntity(ISpellProjectileShape trigger,	LivingEntity shooter, Vector3d origin, Vector3d direction, float speedFactor, double maxDistance) {
		this(NostrumEntityTypes.spellProjectile, trigger, shooter.world, shooter, origin, direction, speedFactor, maxDistance);
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
	@OnlyIn(Dist.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		return distance <= 64 * 64 * 64;
	}
	
	private Vector3d getAccel(Vector3d direction, double scale) {
		Vector3d base = direction.normalize();
		final double tickScale = .05;
		
		return new Vector3d(base.x * scale * tickScale, base.y * scale * tickScale, base.z * scale * tickScale);
	}
	
	protected boolean dieOnImpact(BlockPos pos) {
		return true;
	}
	
	protected boolean dieOnImpact(Entity entity) {
		return true;
	}
	
	public boolean canImpact(BlockPos pos) {
		return true;
	}
	
	public boolean canImpact(Entity entity) {
		return this.shootingEntity == null || ((!entity.equals(shootingEntity) && !shootingEntity.isRidingOrBeingRiddenBy(entity)));
	}
	
	protected void doImpact(Entity entity) {
		trigger.onProjectileHit(entity);
	}
	
	protected void doImpact(SpellLocation location) {
		trigger.onProjectileHit(location);
		
		// Proc mystic anchors if we hit one
		final BlockPos selectedPos = location.selectedBlockPos;
		BlockState state = world.getBlockState(selectedPos);
		if (state.getBlock() instanceof MysticAnchor) {
			state.onEntityCollision(world, selectedPos, this);
		}
	}
	
	protected void doClientEffect() {
		int color = getElement().getColor();
		color = (0x19000000) | (color & 0x00FFFFFF);
		NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
				2,
				getPosX(), getPosY() + getHeight()/2f, getPosZ(), 0, 40, 0,
				new Vector3d(rand.nextFloat() * .05 - .025, rand.nextFloat() * .05, rand.nextFloat() * .05 - .025), null
			).color(color));
	}
	
	protected void onProjectileDeath() {
		
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (!world.isRemote()) {
			if (origin == null) {
				// We got loaded...
				this.remove();
				return;
			}
			// Can't avoid a SQR; tracking motion would require SQR, too to get path length
			if (this.getPositionVec().squareDistanceTo(origin) > maxDistance) {
				trigger.onProjectileEnd(this.getPositionVec());
				this.onProjectileDeath();
				this.remove();
			}
		} else {
			doClientEffect();
		}
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		if (world.isRemote())
			return;
		
		if (result.getType() == RayTraceResult.Type.MISS) {
			; // Do nothing
		} else if (result.getType() == RayTraceResult.Type.BLOCK) {
			BlockPos pos = ((BlockRayTraceResult) result).getPos();
			boolean canImpact = this.canImpact(pos);
			if (canImpact) {
				this.doImpact(new SpellLocation(world, result));
				if (this.dieOnImpact(pos)) {
					this.onProjectileDeath();
					this.remove();
					return;
				}
			}
		} else if (result.getType() == RayTraceResult.Type.ENTITY) {
			Entity entityHit = ((EntityRayTraceResult) result).getEntity();
			if (entityHit instanceof SpellProjectileEntity) {
				; // Just don't hit other projectiles
			} else {
				boolean canImpact = this.canImpact(entityHit);
				if (canImpact) {
					this.doImpact(entityHit);
					if (this.dieOnImpact(entityHit)) {
						this.onProjectileDeath();
						this.remove();
					}
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
		return ParticleTypes.ENCHANT;
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
	
	public @Nullable Entity getShooter() {
		return super.func_234616_v_();
	}
	
	@Override
	protected boolean func_230298_a_(Entity entity) {
		return this.canImpact(entity);
	}
}
