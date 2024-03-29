package com.smanzana.nostrummagica.utils;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.entity.EntitySpellSaucer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.DamagingProjectileEntity;
import net.minecraft.entity.projectile.ThrowableEntity;

public class Projectiles {

	public static final @Nullable LivingEntity getShooter(Entity projectile) {
		Entity shooter;
		LivingEntity source = null;
		if (projectile instanceof AbstractArrowEntity) {
			shooter = ((AbstractArrowEntity) projectile).getShooter();
			if (shooter != null && shooter instanceof LivingEntity)
				source = (LivingEntity) shooter;
		} else if (projectile instanceof DamagingProjectileEntity) {
			source = ((DamagingProjectileEntity) projectile).shootingEntity;
		} else if (projectile instanceof ThrowableEntity) {
			source = ((ThrowableEntity) projectile).getThrower();
		} else if (projectile instanceof EntitySpellSaucer) {
			source = ((EntitySpellSaucer) projectile).getShooter();
		}
		
		return source;
	}
	
}
