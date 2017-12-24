package com.smanzana.nostrummagica.proxy;

import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.potions.RootedPotion;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.spells.components.shapes.AoEShape;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.SelfTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.TouchTrigger;

import net.minecraftforge.fml.common.registry.GameRegistry;

public class CommonProxy {

	
	
	
	
	
	public void preinit() {

	}
	
	public void init() {
    	registerShapes();
    	registerTriggers();
    	registerPotions();
    	registerItems();
	}
	
	public void postinit() {
		
	}
    
    private void registerShapes() {
    	SpellShape.register(SingleShape.instance());
    	SpellShape.register(AoEShape.instance());
    }
    
    private void registerTriggers() {
    	SpellTrigger.register(SelfTrigger.instance());
    	SpellTrigger.register(TouchTrigger.instance());
    }
    
    private void registerPotions() {
    	RootedPotion.instance();
    }
    
    private void registerItems() {
    	GameRegistry.registerItem(SpellTome.instance(), SpellTome.id);
    }
	
}
