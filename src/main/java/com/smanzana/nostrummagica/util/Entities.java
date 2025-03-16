package com.smanzana.nostrummagica.util;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;

public class Entities {

	public static @Nullable Entity FindEntity(Level world, UUID id) {
		if (world.isClientSide() && world instanceof ClientLevel) {
			Iterable<Entity> entities = ((ClientLevel)world).entitiesForRendering();
			for (Entity ent : entities) {
				if (ent.getUUID().equals(id)) {
					return ent;
				}
			}
		} else if (world instanceof ServerLevel) {
			return ((ServerLevel) world).getEntity(id);
		}
		
		return null;
	}
	
	public static @Nullable LivingEntity FindLiving(Level world, UUID id) {
		Entity ent = FindEntity(world, id);
		if (ent != null && ent instanceof LivingEntity) {
			return (LivingEntity) ent;
		}
		
		return null;
	}
	
	public static List<LivingEntity> GetEntities(ServerLevel world, Predicate<LivingEntity> predicate) {
		List<LivingEntity> livingList = Lists.newArrayList(world.getEntities(EntityTypeTest.forClass(LivingEntity.class), predicate));
		return livingList;
	}
	
}
