package com.smanzana.nostrummagica.utils;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.entity.EntitySpellSaucer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;

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
	
}
