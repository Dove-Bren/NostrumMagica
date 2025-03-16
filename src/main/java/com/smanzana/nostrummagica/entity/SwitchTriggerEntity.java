package com.smanzana.nostrummagica.entity;


import com.smanzana.nostrummagica.tile.SwitchBlockTileEntity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class SwitchTriggerEntity extends TileProxyTriggerEntity<SwitchBlockTileEntity> {
	
	public static final String ID = "entity_switch_trigger";
	
	public SwitchTriggerEntity(EntityType<? extends SwitchTriggerEntity> type, Level worldIn) {
		super(type, worldIn);
	}
}
