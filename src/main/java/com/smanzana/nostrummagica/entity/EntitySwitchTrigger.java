package com.smanzana.nostrummagica.entity;


import com.smanzana.nostrummagica.tile.SwitchBlockTileEntity;

import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public class EntitySwitchTrigger extends EntityTileProxyTrigger<SwitchBlockTileEntity> {
	
	public static final String ID = "entity_switch_trigger";
	
	public EntitySwitchTrigger(EntityType<? extends EntitySwitchTrigger> type, World worldIn) {
		super(type, worldIn);
	}
}
