package com.smanzana.nostrummagica.entity;


import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public class EntityKeySwitchTrigger extends EntitySwitchTrigger {
	
	public static final String ID = "entity_key_switch_trigger";
	
	public EntityKeySwitchTrigger(EntityType<? extends EntityKeySwitchTrigger> type, World worldIn) {
		super(type, worldIn);
	}
	
	@Override
	public void livingTick() {
		super.livingTick();
	}
}
