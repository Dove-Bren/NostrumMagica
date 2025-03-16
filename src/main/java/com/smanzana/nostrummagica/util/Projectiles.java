package com.smanzana.nostrummagica.util;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.entity.SpellSaucerEntity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class Projectiles {

	public static final @Nullable LivingEntity getShooter(Entity projectile) {
		Entity shooter;
		LivingEntity source = null;
		if (projectile instanceof Projectile) {
			shooter = ((Projectile) projectile).getOwner();// getShooter();
			if (shooter != null && shooter instanceof LivingEntity)
				source = (LivingEntity) shooter;
		} else if (projectile instanceof SpellSaucerEntity) {
			shooter = ((SpellSaucerEntity) projectile).getShooter();
			if (shooter != null && shooter instanceof LivingEntity)
				source = (LivingEntity) shooter;
		}
		
		return source;
	}
	
	// Copied from vanilla entity class
	public static final Vec3 getVectorForRotation(float pitch, float yaw) {
        float f = Mth.cos(-yaw * 0.017453292F - (float)Math.PI);
        float f1 = Mth.sin(-yaw * 0.017453292F - (float)Math.PI);
        float f2 = -Mth.cos(-pitch * 0.017453292F);
        float f3 = Mth.sin(-pitch * 0.017453292F);
        return new Vec3((double)(f1 * f2), (double)f3, (double)(f * f2));
    }
	
}
