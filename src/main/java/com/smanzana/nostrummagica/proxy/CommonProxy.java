package com.smanzana.nostrummagica.proxy;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.capabilities.NostrumMagic;
import com.smanzana.nostrummagica.capabilities.NostrumMagicStorage;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.StatSyncMessage;
import com.smanzana.nostrummagica.potions.RootedPotion;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.spells.components.shapes.AoEShape;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.SelfTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.TouchTrigger;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CommonProxy {

	public void preinit() {
		CapabilityManager.INSTANCE.register(INostrumMagic.class, new NostrumMagicStorage(), NostrumMagic.class);
		NetworkHandler.getInstance();
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
    	SpellTome.instance().setRegistryName(NostrumMagica.MODID, SpellTome.id);
    	GameRegistry.register(SpellTome.instance());
    }
    
    public void syncPlayer(EntityPlayerMP player) {
    	System.out.println("Sending sync to client");
    	NetworkHandler.getSyncChannel().sendTo(
    			new StatSyncMessage(NostrumMagica.getMagicWrapper(player)),
    			player);
    }

	public EntityPlayer getPlayer() {
		return null; // Doesn't mean anything on the server
	}
	
}
