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
		return new BlockPos(this.getPosX() - 1, this.getPosY(), this.getPosZ() - 1);
	}
	
	@Override
	public boolean isEntityInsideOpaqueBlock() {
		return false;
	}
}
