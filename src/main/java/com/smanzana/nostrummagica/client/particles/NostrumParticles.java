package com.smanzana.nostrummagica.client.particles;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.SpawnNostrumParticleMessage;
import com.smanzana.nostrummagica.util.ColorUtil;
import com.smanzana.nostrummagica.util.NetUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.Tag;
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
	
	public void spawn(Level world, SpawnParams params) {
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
	
	public static void Spawn(NostrumParticles type, Level world, SpawnParams params) {
		if (!world.isClientSide) {
			NetworkHandler.sendToAllAround(new SpawnNostrumParticleMessage(type, params),
					new TargetPoint(params.spawnX, params.spawnY, params.spawnZ, 50, world.dimension())
					);
		} else {
			final Minecraft mc = Minecraft.getInstance();
			if (mc.gameRenderer.getMainCamera().getPosition().distanceToSqr(params.spawnX, params.spawnY, params.spawnZ)
					<  50 * 50) {
				INostrumParticleFactory<?> factory = type.getFactory();
				if (factory != null) {
					factory.createParticle((ClientLevel) world, params);
				}
			}
		}
	}
	
	public static class SpawnParams {
		
		public static enum TargetBehavior {
			JOIN, // Fly towards and into the target (ent: x, y + h/2, z, pos: x, y, z)
			ORBIT, // Fly towards and then orbit the entity (r = w*2 by default for ents, 1 for pos)
		}
		
		public static final Codec<SpawnParams> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.fieldOf("count").forGetter((p) -> p.count),
				Codec.DOUBLE.fieldOf("spawnX").forGetter((p) -> p.spawnX),
				Codec.DOUBLE.fieldOf("spawnY").forGetter((p) -> p.spawnY),
				Codec.DOUBLE.fieldOf("spawnZ").forGetter((p) -> p.spawnZ),
				Codec.DOUBLE.fieldOf("spawnJitterRadius").forGetter((p) -> p.spawnJitterRadius),
				Codec.INT.fieldOf("lifetime").forGetter((p) -> p.lifetime),
				Codec.INT.fieldOf("lifetimeJitter").forGetter((p) -> p.lifetimeJitter),
				NetUtils.CODEC_VECTOR3D.fieldOf("velocity").forGetter((p) -> p.velocity),
				NetUtils.CODEC_VECTOR3D.fieldOf("targetPos").forGetter((p) -> p.targetPos),
				NetUtils.CODEC_VECTOR3D.fieldOf("velocityJitter").forGetter((p) -> p.velocityJitter),
				Codec.INT.fieldOf("targetEntID").forGetter((p) -> p.targetEntID),
				Codec.INT.optionalFieldOf("color", 0xFFFFFFFF).forGetter((p) -> p.color),
				Codec.BOOL.fieldOf("dieOnTarget").forGetter((p) -> p.dieOnTarget),
				Codec.FLOAT.fieldOf("gravityStrength").forGetter((p) -> p.gravityStrength),
				Codec.STRING.xmap(s -> TargetBehavior.valueOf(s.toUpperCase()), e -> e.name()).fieldOf("").forGetter((p) -> p.targetBehavior),
				Codec.FLOAT.fieldOf("orbitRadius").forGetter((p) -> p.orbitRadius)
			).apply(instance, SpawnParams::UnpackSpawnParams));
		
		
		// Required params
		public final int count;
		public final double spawnX;
		public final double spawnY;
		public final double spawnZ;
		public final double spawnJitterRadius;
		public final int lifetime;
		public final int lifetimeJitter;
		
		// One of the below is required
		public final @Nullable Vec3 velocity;
		public final @Nullable Vec3 targetPos;
		public final @Nullable Integer targetEntID;
		
		public final @Nullable Vec3 velocityJitter; // 0-1 where 1 is completely random
		
		// Rest is completely optional and may or may not be used
		public @Nullable Integer color; // ARGB
		public boolean dieOnTarget;
		public float gravityStrength;
		public TargetBehavior targetBehavior;
		public float orbitRadius;
		
		public SpawnParams(int count, double spawnX, double spawnY, double spawnZ, double spawnJitterRadius, int lifetime, int lifetimeJitter, 
				Vec3 velocity, Vec3 velocityJitter) {
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
			this.orbitRadius = 0f;
			this.targetBehavior = TargetBehavior.JOIN;
		}
		
		public SpawnParams(int count, double spawnX, double spawnY, double spawnZ, double spawnJitterRadius, int lifetime, int lifetimeJitter,
				Vec3 targetPos) {
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
			this.orbitRadius = 0f;
			this.targetBehavior = TargetBehavior.JOIN;
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
			this.orbitRadius = 0f;
			this.targetBehavior = TargetBehavior.JOIN;
		}
		
		protected static SpawnParams UnpackSpawnParams(int count, double spawnX, double spawnY, double spawnZ, double spawnJitterRadius, int lifetime, int lifetimeJitter,
				@Nullable Vec3 velocity, @Nullable Vec3 velocityJitter, @Nullable Vec3 targetPos, int targetEntID,
				@Nullable Integer color, boolean dieOnTarget, float gravityStrength, TargetBehavior targetBehavior, float orbitRadius
				) {
			final SpawnParams params;
			// For CODEC, prefer velocity, then targetPos, then targetEntID
			if (velocity != null) {
				params = new SpawnParams(count, spawnX, spawnY, spawnZ, spawnJitterRadius, lifetime, lifetimeJitter,
						velocity, velocityJitter);
			} else if (targetPos != null) {
				params = new SpawnParams(count, spawnX, spawnY, spawnZ, spawnJitterRadius, lifetime, lifetimeJitter,
						targetPos);
			} else {
				params = new SpawnParams(count, spawnX, spawnY, spawnZ, spawnJitterRadius, lifetime, lifetimeJitter,
						targetEntID);
			}
			
			params.color = color;
			params.dieOnTarget = dieOnTarget;
			params.gravityStrength = gravityStrength;
			params.targetBehavior = targetBehavior;
			params.orbitRadius = orbitRadius;
			
			return params;
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
		
		public SpawnParams setTargetBehavior(TargetBehavior behavior) {
			this.targetBehavior = behavior;
			return this;
		}
		
		public SpawnParams setOrbitRadius(float radius) {
			this.orbitRadius = radius;
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
		private static final String NBT_TARGET_BEHAVIOR = "target_behavior";
		private static final String NBT_ORBIT_RADIUS = "orbit_radius";
		
		public static CompoundTag WriteNBT(SpawnParams params, @Nullable CompoundTag tag) {
			if (tag == null) {
				tag = new CompoundTag();
			}
			
			tag.putInt(NBT_COUNT, params.count);
			tag.putDouble(NBT_SPAWN_X, params.spawnX);
			tag.putDouble(NBT_SPAWN_Y, params.spawnY);
			tag.putDouble(NBT_SPAWN_Z, params.spawnZ);
			tag.putDouble(NBT_SPAWN_JITTER, params.spawnJitterRadius);
			tag.putInt(NBT_LIFETIME, params.lifetime);
			tag.putInt(NBT_LIFETIME_JITTER, params.lifetimeJitter);
			tag.putBoolean(NBT_DIE_ON_TARGET, params.dieOnTarget);
			tag.putInt(NBT_TARGET_BEHAVIOR, params.targetBehavior.ordinal());
			tag.putFloat(NBT_ORBIT_RADIUS, params.orbitRadius);
			
			if (params.velocity != null) {
				CompoundTag subtag = new CompoundTag();
				subtag.putDouble("x", params.velocity.x);
				subtag.putDouble("y", params.velocity.y);
				subtag.putDouble("z", params.velocity.z);
				tag.put(NBT_VELOCITY, subtag);
			}
			
			if (params.velocityJitter != null) {
				CompoundTag subtag = new CompoundTag();
				subtag.putDouble("x", params.velocityJitter.x);
				subtag.putDouble("y", params.velocityJitter.y);
				subtag.putDouble("z", params.velocityJitter.z);
				tag.put(NBT_VELOCITY_JITTER, subtag);
			}
			
			if (params.targetPos != null) {
				CompoundTag subtag = new CompoundTag();
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
		
		public CompoundTag toNBT(@Nullable CompoundTag tag) {
			return WriteNBT(this, tag);
		}
		
		public static SpawnParams FromNBT(CompoundTag tag) {
			final int count = tag.getInt(NBT_COUNT);
			final double spawnX = tag.getDouble(NBT_SPAWN_X);
			final double spawnY = tag.getDouble(NBT_SPAWN_Y);
			final double spawnZ = tag.getDouble(NBT_SPAWN_Z);
			final double spawnJitter = tag.getDouble(NBT_SPAWN_JITTER);
			final int lifetime = tag.getInt(NBT_LIFETIME);
			final int lifetimeJitter = tag.getInt(NBT_LIFETIME_JITTER);
			
			final SpawnParams params;
			if (tag.contains(NBT_VELOCITY, Tag.TAG_COMPOUND)) {
				CompoundTag subtag = tag.getCompound(NBT_VELOCITY);
				final double velocityX = subtag.getDouble("x");
				final double velocityY = subtag.getDouble("y");
				final double velocityZ = subtag.getDouble("z");
				final Vec3 velocityJitter;
				if (tag.contains(NBT_VELOCITY_JITTER)) {
					subtag = tag.getCompound(NBT_VELOCITY_JITTER);
					velocityJitter = new Vec3(
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
						new Vec3(velocityX, velocityY, velocityZ),
						velocityJitter
						);
			} else if (tag.contains(NBT_TARGET_POS, Tag.TAG_COMPOUND)) {
				CompoundTag subtag = tag.getCompound(NBT_TARGET_POS);
				final double targetX = subtag.getDouble("x");
				final double targetY = subtag.getDouble("y");
				final double targetZ = subtag.getDouble("z");
				params = new SpawnParams(
						count,
						spawnX, spawnY, spawnZ, spawnJitter,
						lifetime, lifetimeJitter,
						new Vec3(targetX, targetY, targetZ)
						);
			} else if (tag.contains(NBT_TARGET_ENT_ID, Tag.TAG_INT)) {
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
						new Vec3(0, .1, 0), null
						);
			}
			
			// Extra optional data
			if (tag.contains("color", Tag.TAG_INT)) {
				params.color(tag.getInt("color"));
			}
			if (tag.contains(NBT_DIE_ON_TARGET, Tag.TAG_BYTE)) {
				params.dieOnTarget(tag.getBoolean(NBT_DIE_ON_TARGET));
			}
			
			if (tag.contains(NBT_GRAVITY_STRENGTH, Tag.TAG_FLOAT)) {
				params.gravity(tag.getFloat(NBT_GRAVITY_STRENGTH));
			}
			
			if (tag.contains(NBT_TARGET_BEHAVIOR, Tag.TAG_INT)) {
				final int ord = tag.getInt(NBT_TARGET_BEHAVIOR);
				if (ord < TargetBehavior.values().length) {
					params.targetBehavior = TargetBehavior.values()[ord];
				} else {
					params.targetBehavior = TargetBehavior.JOIN;
				}
			}
			params.orbitRadius = tag.getFloat(NBT_ORBIT_RADIUS);
			
			return params;
		}
	}
}
