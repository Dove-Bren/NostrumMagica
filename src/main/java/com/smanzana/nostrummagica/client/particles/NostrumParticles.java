package com.smanzana.nostrummagica.client.particles;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.SpawnNostrumParticleMessage;
import com.smanzana.nostrummagica.util.ColorUtil;
import com.smanzana.nostrummagica.util.NetTargetLocation;
import com.smanzana.nostrummagica.util.NetUtils;
import com.smanzana.nostrummagica.util.TargetLocation;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor.TargetPoint;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public enum NostrumParticles {

	GLOW_ORB(new NostrumParticleType("glow_orb")),
	LIGHTNING_STATIC(new NostrumParticleType("lightning_static")),
	FILLED_ORB(new NostrumParticleType("filled_orb")),
	WARD(new NostrumParticleType("ward")),
	LIGHT_EXPLOSION(new NostrumParticleType("light_explosion")),
	GLOW_TRAIL(new NostrumParticleType("glow_trail")),
	SMOKE_TRAIL(new NostrumParticleType("smoke_trail")),
	RISING_GLOW(new NostrumParticleType("rising_glow")),
	LIGHTNING_CHAIN(new NostrumParticleType("lightning_chain")),
	;
	
	@SubscribeEvent
	public static void registerParticles(RegistryEvent.Register<ParticleType<?>> event) {
		for (NostrumParticles p : NostrumParticles.values()) {
			event.getRegistry().register(p.getType());
		}
	}
	
	private final NostrumParticleType type;
	
	private NostrumParticles(NostrumParticleType type) {
		this.type = type;
	}
	
	public NostrumParticleType getType() {
		return this.type;
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
//				INostrumParticleFactory<?> factory = type.getFactory();
//				if (factory != null) {
//					factory.createParticle((ClientLevel) world, mc.particleEngine., params);
//				}
				world.addParticle(new NostrumParticleData(type.getType(), params), true, params.spawnX, params.spawnY, params.spawnZ, 0, 0, 0);
			}
		}
	}
	
	public static class SpawnParams {
		
		public static final Codec<SpawnParams> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.fieldOf("count").forGetter((p) -> p.count),
				Codec.DOUBLE.fieldOf("spawnX").forGetter((p) -> p.spawnX),
				Codec.DOUBLE.fieldOf("spawnY").forGetter((p) -> p.spawnY),
				Codec.DOUBLE.fieldOf("spawnZ").forGetter((p) -> p.spawnZ),
				Codec.DOUBLE.fieldOf("spawnJitterRadius").forGetter((p) -> p.spawnJitterRadius),
				Codec.INT.fieldOf("lifetime").forGetter((p) -> p.lifetime),
				Codec.INT.fieldOf("lifetimeJitter").forGetter((p) -> p.lifetimeJitter),
				NetUtils.CODEC_VECTOR3D.fieldOf("velocity").forGetter((p) -> p.velocity),
				NetUtils.CODEC_VECTOR3D.fieldOf("velocityJitter").forGetter((p) -> p.velocityJitter),
				NetTargetLocation.CODEC.fieldOf("target").forGetter((p) -> p.target),
				NetTargetLocation.CODEC.fieldOf("extra_target").forGetter((p) -> p.target),
				//Codec.INT.fieldOf("targetEntID").forGetter((p) -> p.targetEntID),
				Codec.INT.optionalFieldOf("color", 0xFFFFFFFF).forGetter((p) -> p.color),
				//Codec.BOOL.fieldOf("dieOnTarget").forGetter((p) -> p.dieWithTarget),
				Codec.FLOAT.fieldOf("gravityStrength").forGetter((p) -> p.gravityStrength),
				ParticleTargetBehavior.CODEC.fieldOf("targetBehavior").forGetter((p) -> p.targetBehavior)
				//Codec.STRING.xmap(s -> TargetBehavior.valueOf(s.toUpperCase()), e -> e.name()).fieldOf("").forGetter((p) -> p.targetBehavior),
				//Codec.FLOAT.fieldOf("orbitRadius").forGetter((p) -> p.orbitRadius)
			).apply(instance, SpawnParams::UnpackSpawnParams));
		
		
		// Required params
		public final int count;
		public final double spawnX;
		public final double spawnY;
		public final double spawnZ;
		public final double spawnJitterRadius;
		public final int lifetime;
		public final int lifetimeJitter;
		public final @Nullable Vec3 velocity;
		
//		// One of the below is required
//		public final @Nullable Vec3 targetPos;
//		public final @Nullable Integer targetEntID;
		public final @Nullable NetTargetLocation target;
		public @Nullable NetTargetLocation extraTarget;
		
		public final @Nullable Vec3 velocityJitter; // 0-1 where 1 is completely random
		
		// Rest is completely optional and may or may not be used
		public @Nullable Integer color; // ARGB
		public @Nullable ParticleTargetBehavior targetBehavior;
		public float gravityStrength;
//		boolean dieWithTarget;
//		public TargetBehavior targetBehavior;
//		public float orbitRadius;
		
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
			this.target = null;
			this.targetBehavior = null;
		}
		
		public SpawnParams(int count, double spawnX, double spawnY, double spawnZ, double spawnJitterRadius, int lifetime, int lifetimeJitter, NetTargetLocation target) {
			super();
			this.count = count;
			this.spawnX = spawnX;
			this.spawnY = spawnY;
			this.spawnZ = spawnZ;
			this.spawnJitterRadius = spawnJitterRadius;
			this.lifetime = lifetime;
			this.lifetimeJitter = lifetimeJitter;
			this.velocity = null;
			this.target = target;
			this.velocityJitter = null;
			this.targetBehavior = null;
		}
		
		public SpawnParams(int count, double spawnX, double spawnY, double spawnZ, double spawnJitterRadius, int lifetime, int lifetimeJitter, TargetLocation target) {
			this(count, spawnX, spawnY, spawnZ, spawnJitterRadius, lifetime, lifetimeJitter, new NetTargetLocation(target));
		}
		
//		public SpawnParams(int count, double spawnX, double spawnY, double spawnZ, double spawnJitterRadius, int lifetime, int lifetimeJitter,
//				int targetEntID) {
//			this(count, spawnX, spawnY, spawnZ, spawnJitterRadius, lifetime, lifetimeJitter,)
//			super();
//			this.count = count;
//			this.spawnX = spawnX;
//			this.spawnY = spawnY;
//			this.spawnZ = spawnZ;
//			this.spawnJitterRadius = spawnJitterRadius;
//			this.lifetime = lifetime;
//			this.lifetimeJitter = lifetimeJitter;
//			this.velocity = null;
//			this.targetPos = null;
//			this.targetEntID = targetEntID;
//			this.velocityJitter = null;
//			this.dieWithTarget = false;
//			this.gravityStrength = 0f;
//			this.orbitRadius = 0f;
//			this.targetBehavior = TargetBehavior.JOIN;
//		}
		
		protected static SpawnParams UnpackSpawnParams(int count, double spawnX, double spawnY, double spawnZ, double spawnJitterRadius, int lifetime, int lifetimeJitter,
				@Nullable Vec3 velocity, @Nullable Vec3 velocityJitter, @Nullable NetTargetLocation target, @Nullable NetTargetLocation extraTarget,
				@Nullable Integer color, float gravityStrength, ParticleTargetBehavior targetBehavior
				) {
			final SpawnParams params;
			// For CODEC, prefer velocity, then targetPos, then targetEntID
			if (velocity != null) {
				params = new SpawnParams(count, spawnX, spawnY, spawnZ, spawnJitterRadius, lifetime, lifetimeJitter,
						velocity, velocityJitter);
			} else {
				params = new SpawnParams(count, spawnX, spawnY, spawnZ, spawnJitterRadius, lifetime, lifetimeJitter, target);
			}
			
			params.color = color;
			params.gravityStrength = gravityStrength;
			params.targetBehavior = targetBehavior;
			params.extraTarget = extraTarget;
			
			return params;
		}
		
		public SpawnParams color(int color) {
			this.color = color;
			return this;
		}
		
		public SpawnParams color(float alpha, float red, float green, float blue) {
			return color(ColorUtil.colorToARGB(red, green, blue, alpha));
		}
		
		public SpawnParams gravity(boolean gravity) {
			return this.gravity(gravity ? .2f : 0);
		}
		
		public SpawnParams gravity(float gravityStrength) {
			this.gravityStrength = gravityStrength;
			return this;
		}
		
		public SpawnParams setTargetBehavior(ParticleTargetBehavior behavior) {
			this.targetBehavior = behavior;
			return this;
		}
		
		public SpawnParams setTargetBehavior(ParticleTargetBehavior.TargetBehavior type) { // really shouldn't exist...
			final ParticleTargetBehavior base = new ParticleTargetBehavior();
			base.entityBehavior = type;
			return this.setTargetBehavior(base);
		}
		
		public SpawnParams setExtraTarget(TargetLocation extraTarget) {
			return setExtraTarget(new NetTargetLocation((extraTarget)));
		}
		
		public SpawnParams setExtraTarget(NetTargetLocation extraTarget) {
			this.extraTarget = extraTarget;
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
		private static final String NBT_TARGET = "target";
		private static final String NBT_EXTRA_TARGET = "extra_target";
		private static final String NBT_GRAVITY_STRENGTH = "gravity_strength";
		private static final String NBT_TARGET_BEHAVIOR = "target_behavior";
		
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
			if (params.targetBehavior != null) {
				tag.put(NBT_TARGET_BEHAVIOR, params.targetBehavior.toNBT());
			}
			
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
			
			if (params.target != null) {
				CompoundTag subtag = params.target.toNBT();
				tag.put(NBT_TARGET, subtag);
			}
			
			if (params.extraTarget != null) {
				CompoundTag subtag = params.extraTarget.toNBT();
				tag.put(NBT_EXTRA_TARGET, subtag);
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
			} else if (tag.contains(NBT_TARGET, Tag.TAG_COMPOUND)) {
				params = new SpawnParams(
						count,
						spawnX, spawnY, spawnZ, spawnJitter,
						lifetime, lifetimeJitter,
						NetTargetLocation.FromNBT(tag.getCompound(NBT_TARGET))
						);
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
			
			if (tag.contains(NBT_GRAVITY_STRENGTH, Tag.TAG_FLOAT)) {
				params.gravity(tag.getFloat(NBT_GRAVITY_STRENGTH));
			}
			
			if (tag.contains(NBT_TARGET_BEHAVIOR)) {
				params.targetBehavior = ParticleTargetBehavior.FromNBT(tag.getCompound(NBT_TARGET_BEHAVIOR));
			}
			
			if (tag.contains(NBT_EXTRA_TARGET)) {
				params.extraTarget = NetTargetLocation.FromNBT(tag.getCompound(NBT_EXTRA_TARGET));
			}
			
			return params;
		}
	}
}
