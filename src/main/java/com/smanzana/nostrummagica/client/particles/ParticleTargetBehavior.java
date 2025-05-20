package com.smanzana.nostrummagica.client.particles;

import javax.annotation.Nonnull;

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
}
