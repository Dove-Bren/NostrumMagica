package com.smanzana.nostrummagica.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ChakramSpellSaucerEntity extends SpellSaucerEntity {
	
	public static final String ID = "entity_internal_spellsaucer_chakram";
	
	public ChakramSpellSaucerEntity(EntityType<? extends ChakramSpellSaucerEntity> type, Level world) {
		super(type, world);
	}
	
	protected ChakramSpellSaucerEntity(EntityType<? extends ChakramSpellSaucerEntity> type, ISpellProjectileShape trigger, Level world, LivingEntity shooter, float speed, double maxDistance) {
		super(type, trigger, world, shooter, speed, maxDistance);
	}
	
	protected ChakramSpellSaucerEntity(EntityType<? extends ChakramSpellSaucerEntity> type, ISpellProjectileShape trigger, Level world, LivingEntity shooter,
			Vec3 from, Vec3 direction,
			float speedFactor, double maxDistance) {
		super(type, trigger, shooter.level, shooter, from, direction, speedFactor, maxDistance, -1);
	}
	
	public ChakramSpellSaucerEntity(ISpellProjectileShape trigger, Level world, LivingEntity shooter, float speed, double maxDistance) {
		super(NostrumEntityTypes.chakramSpellSaucer, trigger, world, shooter, speed, maxDistance);
	}
	
	public ChakramSpellSaucerEntity(ISpellProjectileShape trigger, Level world, LivingEntity shooter,
			Vec3 from, Vec3 direction,
			float speedFactor, double maxDistance) {
		super(NostrumEntityTypes.chakramSpellSaucer, trigger, shooter.level, shooter, from, direction, speedFactor, maxDistance, -1);
	}

	@Override
	public void tick() {
		super.tick();
		
		if (!level.isClientSide) {
			
			if (origin == null) {
				// We got loaded...
				this.discard();
				return;
			}
		}
	}
	
	@Override
	public boolean canImpact(BlockPos pos) {
		return true;
	}
	
	@Override
	public boolean dieOnImpact(BlockPos pos) {
		return false;
	}
	
	@Override
	public boolean canImpact(Entity entity) {
		return super.canImpact(entity);
	}
	
	@Override
	public boolean dieOnImpact(Entity entity) {
		return false;
	}
}
