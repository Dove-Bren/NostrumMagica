package com.smanzana.nostrummagica.utils;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityThrowable;

public class Projectiles {

	public static final @Nullable EntityLivingBase getShooter(Entity projectile) {
		Entity shooter;
		EntityLivingBase source = null;
		if (projectile instanceof EntityArrow) {
			shooter = ((EntityArrow) projectile).shootingEntity;
			if (shooter != null && shooter instanceof EntityLivingBase)
				source = (EntityLivingBase) shooter;
		} else if (projectile instanceof EntityFireball) {
			source = ((EntityFireball) projectile).shootingEntity;
		} else if (projectile instanceof EntityThrowable) {
			source = ((EntityThrowable) projectile).getThrower();
		}
		
		return source;
	}
	
}
