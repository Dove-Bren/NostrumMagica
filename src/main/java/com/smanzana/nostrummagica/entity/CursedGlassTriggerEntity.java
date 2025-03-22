package com.smanzana.nostrummagica.entity;


import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class CursedGlassTriggerEntity extends SwitchTriggerEntity {
	
	public static final String ID = "entity_cursed_glass_trigger";
	
	public CursedGlassTriggerEntity(EntityType<? extends CursedGlassTriggerEntity> type, Level worldIn) {
		super(type, worldIn);
	}
	
	@Override
	protected BlockPos getCheckPos() {
		return new BlockPos(this.getX() + -.5, this.getY(), this.getZ() + -.5);
	}
	
	@Override
	public boolean isInWall() {
		return false;
	}
}
