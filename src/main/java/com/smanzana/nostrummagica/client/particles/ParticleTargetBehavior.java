package com.smanzana.nostrummagica.client.particles;

public class ParticleTargetBehavior {

	public static enum TargetBehavior {
		JOIN, // Fly towards and into the target (ent: x, y + h/2, z, pos: x, y, z)
		ORBIT, // Fly towards and then orbit the entity (r = w*2 by default for ents, 1 for pos)
	}
	
	public TargetBehavior entityBehavior;

	public boolean dieOnTarget; // Instead of staying in place, die when reaching the target. Works well with JOIN
	
	public float orbitRadius; // If orbitting, the radius to orbit at
	
}
