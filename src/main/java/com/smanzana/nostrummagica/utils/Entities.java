package com.smanzana.nostrummagica.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class Entities {

	public static @Nullable Entity FindEntity(World world, UUID id) {
		if (world.isRemote() && world instanceof ClientWorld) {
			Iterable<Entity> entities = ((ClientWorld)world).getAllEntities();
			for (Entity ent : entities) {
				if (ent.getUniqueID().equals(id)) {
					return ent;
				}
			}
		} else if (world instanceof ServerWorld) {
			return ((ServerWorld) world).getEntityByUuid(id);
		}
		
		return null;
	}
	
	public static @Nullable LivingEntity FindLiving(World world, UUID id) {
		Entity ent = FindEntity(world, id);
		if (ent != null && ent instanceof LivingEntity) {
			return (LivingEntity) ent;
		}
		
		return null;
	}
	
	public static List<LivingEntity> GetEntities(ServerWorld world, Predicate<LivingEntity> predicate) {
		List<Entity> entities = world.getEntities()
				.filter((e) -> {return e instanceof LivingEntity;})
				.filter((e) -> {return predicate.test((LivingEntity) e);})
				.collect(Collectors.toList());
		List<LivingEntity> livingList = new ArrayList<LivingEntity>();
		for (Entity e : entities) {
			livingList.add((LivingEntity) e);
		}
		return livingList;
	}
	
}
