package com.smanzana.nostrummagica.client.particles;

import javax.annotation.Nonnull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.smanzana.nostrummagica.util.NetUtils;

import net.minecraft.nbt.CompoundTag;

public class ParticleTargetBehavior {

	public static enum TargetBehavior {
		JOIN, // Fly towards and into the target (ent: x, y + h/2, z, pos: x, y, z)
		ORBIT, // Fly towards and then orbit the entity (r = w*2 by default for ents, 1 for pos)
		ATTACH, // Follow the position of the target exactly and instantly
		ORBIT_LAZY, // Orbit, but with motion updates which allow the particle to lag behind and loop a bit
	}
	
	public @Nonnull TargetBehavior entityBehavior = TargetBehavior.JOIN;

	public boolean dieWithTarget = true; // Instead of staying in place, die when target is complete. Works well with JOIN (die when reaching) or ATTACH (die when target dies)
	
	public float orbitRadius = .5f; // If orbitting, the radius to orbit at
	public float orbitPeriod = 20f; // "" and the period, in ticks
	
	public ParticleTargetBehavior() {
		;
	}
	
	protected ParticleTargetBehavior(TargetBehavior entityBehavior, boolean dieWithTarget, float orbitRadius,
			float orbitPeriod) {
		this.entityBehavior = entityBehavior;
		this.dieWithTarget = dieWithTarget;
		this.orbitRadius = orbitRadius;
		this.orbitPeriod = orbitPeriod;
	}
	
	public ParticleTargetBehavior(ParticleTargetBehavior copy) {
		this();
		copyFrom(copy);
	}

	public ParticleTargetBehavior copyFrom(ParticleTargetBehavior copy) {
		this.entityBehavior = copy.entityBehavior;
		this.dieWithTarget = copy.dieWithTarget;
		this.orbitRadius = copy.orbitRadius;
		this.orbitPeriod = copy.orbitPeriod;
		return this;
	}

	public ParticleTargetBehavior joinMode(boolean dieOnTarget) {
		this.entityBehavior = TargetBehavior.JOIN;
		this.dieWithTarget = dieOnTarget;
		return this;
	}
	
	public ParticleTargetBehavior orbitMode(float radius, float period, boolean lazy) {
		this.entityBehavior = lazy ? TargetBehavior.ORBIT_LAZY : TargetBehavior.ORBIT;
		this.orbitRadius = radius;
		this.orbitPeriod = period;
		return this;
	}
	
	public ParticleTargetBehavior orbitMode(float radius) {
		return orbitMode(radius, 20f, true);
	}
	
	public ParticleTargetBehavior orbitMode(boolean lazy) {
//		if (this.target != null) {
//			return orbitMode(target.getTargetWidth() * 2);
//		}
		return orbitMode(1f, 20f, lazy);
	}
	
	public ParticleTargetBehavior orbitMode() {
		return orbitMode(true);
	}
	
	public ParticleTargetBehavior attachMode() {
		this.entityBehavior = TargetBehavior.ATTACH;
		return this;
	}
	
	public ParticleTargetBehavior dieWithTarget() {
		return dieWithTarget(true);
	}
	
	public ParticleTargetBehavior dieWithTarget(boolean on) {
		this.dieWithTarget = on;
		return this;
	}
	
	public CompoundTag toNBT() {
		CompoundTag tag = new CompoundTag();
		tag.put("behavior", NetUtils.ToNBT(this.entityBehavior));
		tag.putBoolean("dieWithTarget", dieWithTarget);
		tag.putFloat("orbitRadius", this.orbitRadius);
		tag.putFloat("orbitPeriod", this.orbitPeriod);
		return tag;
	}
	
	public static final ParticleTargetBehavior FromNBT(CompoundTag tag) {
		final TargetBehavior behavior = NetUtils.FromNBT(TargetBehavior.class, tag.get("behavior"));
		final boolean dieWithTarget = tag.getBoolean("dieWithTarget");
		final float orbitRadius = tag.getFloat("orbitRadius");
		final float orbitPeriod = tag.getFloat("orbitPeriod");
		return new ParticleTargetBehavior(behavior, dieWithTarget, orbitRadius, orbitPeriod);
	}
	
	public static final Codec<ParticleTargetBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.xmap(s -> TargetBehavior.valueOf(s.toUpperCase()), e -> e.name()).fieldOf("").forGetter((p) -> p.entityBehavior),
			Codec.BOOL.fieldOf("dieOnTarget").forGetter((p) -> p.dieWithTarget),
			Codec.FLOAT.fieldOf("orbitRadius").forGetter((p) -> p.orbitRadius),
			Codec.FLOAT.fieldOf("orbitPeriod").forGetter((p) -> p.orbitPeriod)
		).apply(instance, ParticleTargetBehavior::new));
}
