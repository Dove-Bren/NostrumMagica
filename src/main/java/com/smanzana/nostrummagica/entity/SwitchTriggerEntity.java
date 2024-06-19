package com.smanzana.nostrummagica.entity;


import com.smanzana.nostrummagica.tile.SwitchBlockTileEntity;

import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public class SwitchTriggerEntity extends TileProxyTriggerEntity<SwitchBlockTileEntity> {
	
	public static final String ID = "entity_switch_trigger";
	
	public SwitchTriggerEntity(EntityType<? extends SwitchTriggerEntity> type, World worldIn) {
		super(type, worldIn);
	}
}
