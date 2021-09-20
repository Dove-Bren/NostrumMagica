package com.smanzana.nostrummagica.client.particles;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.SpawnNostrumParticleMessage;
import com.smanzana.nostrummagica.utils.ColorUtil;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public enum NostrumParticles {

	GLOW_ORB(1, ParticleGlowOrb.Factory.class),
	LIGHTNING_STATIC(2, ParticleLightningStatic.Factory.class),
	FILLED_ORB(3, ParticleFilledOrb.Factory.class)
	;
	
	private final int ID;
	private final Class<? extends INostrumParticleFactory<?>> factoryClazz;
	private INostrumParticleFactory<?> factory;
	
	private <F extends INostrumParticleFactory<?>> NostrumParticles(int ID, Class<F> factoryClazz) {
		this.ID = ID;
		this.factoryClazz = factoryClazz;
	}
	
	public int getID() {
		return ID;
	}
	
	public void spawn(World world, SpawnParams params) {
		Spawn(this, world, params);
	}
	
	public static @Nullable NostrumParticles FromID(int ID) {
		for (NostrumParticles type : values()) {
			if (type.getID() == ID) {
				return type;
			}
		}
		return null;
	}
	
	public static void Spawn(NostrumParticles type, World world, SpawnParams params) {
		if (!world.isRemote) {
			NetworkHandler.getSyncChannel().sendToDimension(new SpawnNostrumParticleMessage(type, params),
					world.provider.getDimension());
		} else {
			if (type.factory == null) {
				try {
					type.factory = type.factoryClazz.newInstance();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if (type.factory != null) {
				type.factory.createParticle(world, params);
			}
		}
	}
	
	public static class SpawnParams {
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
		
		// Rest is completely optional and may or may not be used
		public @Nullable Integer color; // ARGB
		
		public SpawnParams(int count, double spawnX, double spawnY, double spawnZ, double spawnJitterRadius, int lifetime, int lifetimeJitter, 
				Vec3d velocity, boolean unused) {
			super();
			this.count = count;
			this.spawnX = spawnX;
			this.spawnY = spawnY;
			this.spawnZ = spawnZ;
			this.spawnJitterRadius = spawnJitterRadius;
			this.lifetime = lifetime;
			this.lifetimeJitter = lifetimeJitter;
			this.velocity = velocity;
			this.targetPos = null;
			this.targetEntID = null;
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
		}
		
		public SpawnParams color(int color) {
			this.color = color;
			return this;
		}
		
		public SpawnParams color(float alpha, float red, float green, float blue) {
			return color(ColorUtil.colorToARGB(red, green, blue, alpha));
		}
		
		private static final String NBT_COUNT = "count";
		private static final String NBT_SPAWN_X = "spawn_x";
		private static final String NBT_SPAWN_Y = "spawn_y";
		private static final String NBT_SPAWN_Z = "spawn_z";
		private static final String NBT_SPAWN_JITTER = "spawn_jitter";
		private static final String NBT_LIFETIME = "lifetime";
		private static final String NBT_LIFETIME_JITTER = "lifetime_jitter";
		private static final String NBT_VELOCITY = "velocity";
		private static final String NBT_TARGET_POS = "target_pos";
		private static final String NBT_TARGET_ENT_ID = "target_ent_id";
		
		public static NBTTagCompound WriteNBT(SpawnParams params, @Nullable NBTTagCompound tag) {
			if (tag == null) {
				tag = new NBTTagCompound();
			}
			
			tag.setInteger(NBT_COUNT, params.count);
			tag.setDouble(NBT_SPAWN_X, params.spawnX);
			tag.setDouble(NBT_SPAWN_Y, params.spawnY);
			tag.setDouble(NBT_SPAWN_Z, params.spawnZ);
			tag.setDouble(NBT_SPAWN_JITTER, params.spawnJitterRadius);
			tag.setInteger(NBT_LIFETIME, params.lifetime);
			tag.setInteger(NBT_LIFETIME_JITTER, params.lifetimeJitter);
			
			if (params.velocity != null) {
				NBTTagCompound subtag = new NBTTagCompound();
				subtag.setDouble("x", params.velocity.xCoord);
				subtag.setDouble("y", params.velocity.yCoord);
				subtag.setDouble("z", params.velocity.zCoord);
				tag.setTag(NBT_VELOCITY, subtag);
			}
			
			if (params.targetPos != null) {
				NBTTagCompound subtag = new NBTTagCompound();
				subtag.setDouble("x", params.targetPos.xCoord);
				subtag.setDouble("y", params.targetPos.yCoord);
				subtag.setDouble("z", params.targetPos.zCoord);
				tag.setTag(NBT_TARGET_POS, subtag);
			}
			
			if (params.targetEntID != null) {
				tag.setInteger(NBT_TARGET_ENT_ID, params.targetEntID);
			}
			
			if (params.color != null) {
				tag.setInteger("color", params.color);
			}
			
			return tag;
		}
		
		public NBTTagCompound toNBT(@Nullable NBTTagCompound tag) {
			return WriteNBT(this, tag);
		}
		
		public static SpawnParams FromNBT(NBTTagCompound tag) {
			final int count = tag.getInteger(NBT_COUNT);
			final double spawnX = tag.getDouble(NBT_SPAWN_X);
			final double spawnY = tag.getDouble(NBT_SPAWN_Y);
			final double spawnZ = tag.getDouble(NBT_SPAWN_Z);
			final double spawnJitter = tag.getDouble(NBT_SPAWN_JITTER);
			final int lifetime = tag.getInteger(NBT_LIFETIME);
			final int lifetimeJitter = tag.getInteger(NBT_LIFETIME_JITTER);
			
			final SpawnParams params;
			if (tag.hasKey(NBT_VELOCITY, NBT.TAG_COMPOUND)) {
				NBTTagCompound subtag = tag.getCompoundTag(NBT_VELOCITY);
				final double velocityX = subtag.getDouble("x");
				final double velocityY = subtag.getDouble("y");
				final double velocityZ = subtag.getDouble("z");
				params = new SpawnParams(
						count,
						spawnX, spawnY, spawnZ, spawnJitter,
						lifetime, lifetimeJitter,
						new Vec3d(velocityX, velocityY, velocityZ), false
						);
			} else if (tag.hasKey(NBT_TARGET_POS, NBT.TAG_COMPOUND)) {
				NBTTagCompound subtag = tag.getCompoundTag(NBT_TARGET_POS);
				final double targetX = subtag.getDouble("x");
				final double targetY = subtag.getDouble("y");
				final double targetZ = subtag.getDouble("z");
				params = new SpawnParams(
						count,
						spawnX, spawnY, spawnZ, spawnJitter,
						lifetime, lifetimeJitter,
						new Vec3d(targetX, targetY, targetZ)
						);
			} else if (tag.hasKey(NBT_TARGET_ENT_ID, NBT.TAG_INT)) {
				final int ID = tag.getInteger(NBT_TARGET_ENT_ID);
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
						new Vec3d(0, .1, 0), false
						);
			}
			
			// Extra optional data
			if (tag.hasKey("color", NBT.TAG_INT)) {
				params.color(tag.getInteger("color"));
			}
			
			
			return params;
		}
	}
}
