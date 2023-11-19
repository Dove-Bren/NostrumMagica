package com.smanzana.nostrummagica.client.particles;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.SpawnNostrumParticleMessage;
import com.smanzana.nostrummagica.utils.ColorUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor.TargetPoint;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public enum NostrumParticles {

	GLOW_ORB(new NostrumParticleType("glow_orb"), ParticleGlowOrb.Factory.class),
	LIGHTNING_STATIC(new NostrumParticleType("lightning_static"), ParticleLightningStatic.Factory.class),
	FILLED_ORB(new NostrumParticleType("filled_orb"), ParticleFilledOrb.Factory.class),
	WARD(new NostrumParticleType("ward"), ParticleWard.Factory.class),
	;
	
	@SubscribeEvent
	public static void registerParticles(RegistryEvent.Register<ParticleType<?>> event) {
		for (NostrumParticles p : NostrumParticles.values()) {
			event.getRegistry().register(p.getType());
		}
	}
	
	private final NostrumParticleType type;
	private final Class<? extends INostrumParticleFactory<?>> factoryClazz;
	private INostrumParticleFactory<?> factory;
	
	private <F extends INostrumParticleFactory<?>> NostrumParticles(NostrumParticleType type, Class<F> factoryClazz) {
		this.type = type;
		this.factoryClazz = factoryClazz;
	}
	
	public NostrumParticleType getType() {
		return this.type;
	}

	public @Nullable INostrumParticleFactory<?> getFactory() {
		if (factory == null) {
			try {
				factory = factoryClazz.newInstance();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return factory;
	}
	
	public void spawn(World world, SpawnParams params) {
		Spawn(this, world, params);
	}
	
	public static @Nullable NostrumParticles FromType(NostrumParticleType type) {
		for (NostrumParticles p : values()) {
			if (p.getType() == type) {
				return p;
			}
		}
		return null;
	}
	
	public static void Spawn(NostrumParticles type, World world, SpawnParams params) {
		if (!world.isRemote) {
			NetworkHandler.sendToAllAround(new SpawnNostrumParticleMessage(type, params),
					new TargetPoint(params.spawnX, params.spawnY, params.spawnZ, 50, world.getDimension().getType())
					);
		} else {
			final Minecraft mc = Minecraft.getInstance();
			if (mc.gameRenderer.getActiveRenderInfo().getProjectedView().squareDistanceTo(params.spawnX, params.spawnY, params.spawnZ)
					<  50 * 50) {
				INostrumParticleFactory<?> factory = type.getFactory();
				if (factory != null) {
					factory.createParticle(world, params);
				}
			}
		}
	}
	
	public static class SpawnParams {
		
		public static enum EntityBehavior {
			JOIN, // Fly towards and into the entity (x, y + h/2, z)
			ORBIT, // Fly towards and then orbit the entity (r = w*2)
		}
		
		// Required params
		public final int count;
		public final double spawnX;
		public final double spawnY;
		public final double spawnZ;
		public final double spawnJitterRadius;
		public final int lifetime;
		public final int lifetimeJitter;
		
		// One of the below is required
		public final @Nullable Vec3d velocity;
		public final @Nullable Vec3d targetPos;
		public final @Nullable Integer targetEntID;
		
		public final @Nullable Vec3d velocityJitter; // 0-1 where 1 is completely random
		
		// Rest is completely optional and may or may not be used
		public @Nullable Integer color; // ARGB
		public boolean dieOnTarget;
		public float gravityStrength;
		public EntityBehavior entityBehavior;
		
		public SpawnParams(int count, double spawnX, double spawnY, double spawnZ, double spawnJitterRadius, int lifetime, int lifetimeJitter, 
				Vec3d velocity, Vec3d velocityJitter) {
			super();
			this.count = count;
			this.spawnX = spawnX;
			this.spawnY = spawnY;
			this.spawnZ = spawnZ;
			this.spawnJitterRadius = spawnJitterRadius;
			this.lifetime = lifetime;
			this.lifetimeJitter = lifetimeJitter;
			this.velocity = velocity;
			this.velocityJitter = velocityJitter;
			this.targetPos = null;
			this.targetEntID = null;
			this.dieOnTarget = false;
			this.gravityStrength = 0f;
			this.entityBehavior = EntityBehavior.JOIN;
		}
		
		public SpawnParams(int count, double spawnX, double spawnY, double spawnZ, double spawnJitterRadius, int lifetime, int lifetimeJitter,
				Vec3d targetPos) {
			super();
			this.count = count;
			this.spawnX = spawnX;
			this.spawnY = spawnY;
			this.spawnZ = spawnZ;
			this.spawnJitterRadius = spawnJitterRadius;
			this.lifetime = lifetime;
			this.lifetimeJitter = lifetimeJitter;
			this.velocity = null;
			this.targetPos = targetPos;
			this.targetEntID = null;
			this.velocityJitter = null;
			this.dieOnTarget = false;
			this.gravityStrength = 0f;
			this.entityBehavior = EntityBehavior.JOIN;
		}
		
		public SpawnParams(int count, double spawnX, double spawnY, double spawnZ, double spawnJitterRadius, int lifetime, int lifetimeJitter,
				int targetEntID) {
			super();
			this.count = count;
			this.spawnX = spawnX;
			this.spawnY = spawnY;
			this.spawnZ = spawnZ;
			this.spawnJitterRadius = spawnJitterRadius;
			this.lifetime = lifetime;
			this.lifetimeJitter = lifetimeJitter;
			this.velocity = null;
			this.targetPos = null;
			this.targetEntID = targetEntID;
			this.velocityJitter = null;
			this.dieOnTarget = false;
			this.gravityStrength = 0f;
			this.entityBehavior = EntityBehavior.JOIN;
		}
		
		public SpawnParams color(int color) {
			this.color = color;
			return this;
		}
		
		public SpawnParams color(float alpha, float red, float green, float blue) {
			return color(ColorUtil.colorToARGB(red, green, blue, alpha));
		}
		
		public SpawnParams dieOnTarget(boolean die) {
			this.dieOnTarget = die;
			return this;
		}
		
		public SpawnParams gravity(boolean gravity) {
			return this.gravity(.2f);
		}
		
		public SpawnParams gravity(float gravityStrength) {
			this.gravityStrength = gravityStrength;
			return this;
		}
		
		public SpawnParams setEntityBehavior(EntityBehavior behavior) {
			this.entityBehavior = behavior;
			return this;
		}
		
		private static final String NBT_COUNT = "count";
		private static final String NBT_SPAWN_X = "spawn_x";
		private static final String NBT_SPAWN_Y = "spawn_y";
		private static final String NBT_SPAWN_Z = "spawn_z";
		private static final String NBT_SPAWN_JITTER = "spawn_jitter";
		private static final String NBT_LIFETIME = "lifetime";
		private static final String NBT_LIFETIME_JITTER = "lifetime_jitter";
		private static final String NBT_VELOCITY = "velocity";
		private static final String NBT_VELOCITY_JITTER = "velocity_jitter";
		private static final String NBT_TARGET_POS = "target_pos";
		private static final String NBT_TARGET_ENT_ID = "target_ent_id";
		private static final String NBT_DIE_ON_TARGET = "die_on_target";
		private static final String NBT_GRAVITY_STRENGTH = "gravity_strength";
		private static final String NBT_ENTITY_BEHAVIOR = "entity_behavior";
		
		public static CompoundNBT WriteNBT(SpawnParams params, @Nullable CompoundNBT tag) {
			if (tag == null) {
				tag = new CompoundNBT();
			}
			
			tag.putInt(NBT_COUNT, params.count);
			tag.putDouble(NBT_SPAWN_X, params.spawnX);
			tag.putDouble(NBT_SPAWN_Y, params.spawnY);
			tag.putDouble(NBT_SPAWN_Z, params.spawnZ);
			tag.putDouble(NBT_SPAWN_JITTER, params.spawnJitterRadius);
			tag.putInt(NBT_LIFETIME, params.lifetime);
			tag.putInt(NBT_LIFETIME_JITTER, params.lifetimeJitter);
			tag.putBoolean(NBT_DIE_ON_TARGET, params.dieOnTarget);
			tag.putInt(NBT_ENTITY_BEHAVIOR, params.entityBehavior.ordinal());
			
			if (params.velocity != null) {
				CompoundNBT subtag = new CompoundNBT();
				subtag.putDouble("x", params.velocity.x);
				subtag.putDouble("y", params.velocity.y);
				subtag.putDouble("z", params.velocity.z);
				tag.put(NBT_VELOCITY, subtag);
			}
			
			if (params.velocityJitter != null) {
				CompoundNBT subtag = new CompoundNBT();
				subtag.putDouble("x", params.velocityJitter.x);
				subtag.putDouble("y", params.velocityJitter.y);
				subtag.putDouble("z", params.velocityJitter.z);
				tag.put(NBT_VELOCITY_JITTER, subtag);
			}
			
			if (params.targetPos != null) {
				CompoundNBT subtag = new CompoundNBT();
				subtag.putDouble("x", params.targetPos.x);
				subtag.putDouble("y", params.targetPos.y);
				subtag.putDouble("z", params.targetPos.z);
				tag.put(NBT_TARGET_POS, subtag);
			}
			
			if (params.targetEntID != null) {
				tag.putInt(NBT_TARGET_ENT_ID, params.targetEntID);
			}
			
			if (params.color != null) {
				tag.putInt("color", params.color);
			}
			
			if (params.gravityStrength != 0f) {
				tag.putFloat(NBT_GRAVITY_STRENGTH, params.gravityStrength);
			}
			
			return tag;
		}
		
		public CompoundNBT toNBT(@Nullable CompoundNBT tag) {
			return WriteNBT(this, tag);
		}
		
		public static SpawnParams FromNBT(CompoundNBT tag) {
			final int count = tag.getInt(NBT_COUNT);
			final double spawnX = tag.getDouble(NBT_SPAWN_X);
			final double spawnY = tag.getDouble(NBT_SPAWN_Y);
			final double spawnZ = tag.getDouble(NBT_SPAWN_Z);
			final double spawnJitter = tag.getDouble(NBT_SPAWN_JITTER);
			final int lifetime = tag.getInt(NBT_LIFETIME);
			final int lifetimeJitter = tag.getInt(NBT_LIFETIME_JITTER);
			
			final SpawnParams params;
			if (tag.contains(NBT_VELOCITY, NBT.TAG_COMPOUND)) {
				CompoundNBT subtag = tag.getCompound(NBT_VELOCITY);
				final double velocityX = subtag.getDouble("x");
				final double velocityY = subtag.getDouble("y");
				final double velocityZ = subtag.getDouble("z");
				final Vec3d velocityJitter;
				if (tag.contains(NBT_VELOCITY_JITTER)) {
					subtag = tag.getCompound(NBT_VELOCITY_JITTER);
					velocityJitter = new Vec3d(
							subtag.getDouble("x"),
							subtag.getDouble("y"),
							subtag.getDouble("z"));
				} else {
					velocityJitter = null;
				}
				params = new SpawnParams(
						count,
						spawnX, spawnY, spawnZ, spawnJitter,
						lifetime, lifetimeJitter,
						new Vec3d(velocityX, velocityY, velocityZ),
						velocityJitter
						);
			} else if (tag.contains(NBT_TARGET_POS, NBT.TAG_COMPOUND)) {
				CompoundNBT subtag = tag.getCompound(NBT_TARGET_POS);
				final double targetX = subtag.getDouble("x");
				final double targetY = subtag.getDouble("y");
				final double targetZ = subtag.getDouble("z");
				params = new SpawnParams(
						count,
						spawnX, spawnY, spawnZ, spawnJitter,
						lifetime, lifetimeJitter,
						new Vec3d(targetX, targetY, targetZ)
						);
			} else if (tag.contains(NBT_TARGET_ENT_ID, NBT.TAG_INT)) {
				final int ID = tag.getInt(NBT_TARGET_ENT_ID);
				params = new SpawnParams(
						count,
						spawnX, spawnY, spawnZ, spawnJitter,
						lifetime, lifetimeJitter,
						ID);
			} else {
				// Just default to moving up?
				params = new SpawnParams(
						count,
						spawnX, spawnY, spawnZ, spawnJitter,
						lifetime, lifetimeJitter,
						new Vec3d(0, .1, 0), null
						);
			}
			
			// Extra optional data
			if (tag.contains("color", NBT.TAG_INT)) {
				params.color(tag.getInt("color"));
			}
			if (tag.contains(NBT_DIE_ON_TARGET, NBT.TAG_BYTE)) {
				params.dieOnTarget(tag.getBoolean(NBT_DIE_ON_TARGET));
			}
			
			if (tag.contains(NBT_GRAVITY_STRENGTH, NBT.TAG_FLOAT)) {
				params.gravity(tag.getFloat(NBT_GRAVITY_STRENGTH));
			}
			
			if (tag.contains(NBT_ENTITY_BEHAVIOR, NBT.TAG_INT)) {
				final int ord = tag.getInt(NBT_ENTITY_BEHAVIOR);
				if (ord < EntityBehavior.values().length) {
					params.entityBehavior = EntityBehavior.values()[ord];
				} else {
					params.entityBehavior = EntityBehavior.JOIN;
				}
			}
			
			return params;
		}
	}
}
