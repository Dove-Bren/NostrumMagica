package com.smanzana.nostrummagica.integration.baubles;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.integration.baubles.items.ItemAetherCloak;
import com.smanzana.nostrummagica.integration.baubles.items.ItemMagicBauble;
import com.smanzana.nostrummagica.integration.baubles.items.ItemMagicBauble.ItemType;
import com.smanzana.nostrummagica.proxy.ClientProxy;

import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

//
public class BaublesClientProxy extends BaublesProxy {
	
	public BaublesClientProxy() {
		super();
	}
	
	@Override
	public boolean preInit() {
		if (!super.preInit()) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean init() {
		if (!super.init()) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean postInit() {
		if (!super.postInit()) {
			return false;
		}
		
		return true;
	}
	
	@SubscribeEvent
	public void registerAllModels(ModelRegistryEvent event) {
		registerItemVariants(event);
		registerItemModels(event);
	}
	
	private void registerItemVariants(ModelRegistryEvent event) {
		List<ResourceLocation> list = new LinkedList<>();
    	for (ItemType type : ItemType.values()) {
    		list.add(new ResourceLocation(NostrumMagica.MODID, type.getUnlocalizedKey()));
    	}
		
    	ResourceLocation variants[] = list.toArray(new ResourceLocation[0]);
    	ModelBakery.registerItemVariants(ItemMagicBauble.instance(), variants);
	}
	
	private void registerItemModels(ModelRegistryEvent event) {
    	for (ItemType type : ItemType.values()) {
    		ClientProxy.registerModel(ItemMagicBauble.instance(),
    				ItemMagicBauble.getMetaFromType(type),
					type.getUnlocalizedKey());
		}
    	
    	if (NostrumMagica.aetheria.isEnabled()) {
    		ClientProxy.registerModel(ItemAetherCloak.instance(), 0, ItemAetherCloak.ID);
    	}
	}
}
