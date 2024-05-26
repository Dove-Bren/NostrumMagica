package com.smanzana.nostrummagica.utils;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.entity.EntitySpellSaucer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class Projectiles {

	public static final @Nullable LivingEntity getShooter(Entity projectile) {
		Entity shooter;
		LivingEntity source = null;
		if (projectile instanceof ProjectileEntity) {
			shooter = ((ProjectileEntity) projectile).func_234616_v_();// getShooter();
			if (shooter != null && shooter instanceof LivingEntity)
				source = (LivingEntity) shooter;
		} else if (projectile instanceof EntitySpellSaucer) {
			source = ((EntitySpellSaucer) projectile).getShooter();
		}
		
		return source;
	}
	
	// Copied from vanilla entity class
	public static final Vector3d getVectorForRotation(float pitch, float yaw) {
        float f = MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        return new Vector3d((double)(f1 * f2), (double)f3, (double)(f * f2));
    }
	
}
