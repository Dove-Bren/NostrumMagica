package com.smanzana.nostrummagica.integration.minecolonies;

import net.minecraft.entity.LivingEntity;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class MinecoloniesProxy {

	private boolean enabled;
	
	public MinecoloniesProxy() {
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}
	
	public void enable() {
		this.enabled = true;
	}
	
	public boolean IsSameColony(LivingEntity ent1, LivingEntity ent2) {
		if (enabled) {
			return MinecoloniesUtils.IsSameColony(ent1, ent2);
		}
		
		return false;
	}
}
