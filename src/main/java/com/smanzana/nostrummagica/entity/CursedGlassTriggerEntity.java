package com.smanzana.nostrummagica.entity;


import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CursedGlassTriggerEntity extends SwitchTriggerEntity {
	
	public static final String ID = "entity_cursed_glass_trigger";
	
	public CursedGlassTriggerEntity(EntityType<? extends CursedGlassTriggerEntity> type, World worldIn) {
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
