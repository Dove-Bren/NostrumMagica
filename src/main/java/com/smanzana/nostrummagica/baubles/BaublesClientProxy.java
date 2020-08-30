package com.smanzana.nostrummagica.baubles;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.baubles.items.ItemMagicBauble;
import com.smanzana.nostrummagica.baubles.items.ItemMagicBauble.ItemType;
import com.smanzana.nostrummagica.proxy.ClientProxy;

import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.util.ResourceLocation;

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
		
		registerItemVariants();
		return true;
	}
	
	@Override
	public boolean init() {
		if (!super.init()) {
			return false;
		}
		
		registerItemModels();
		return true;
	}
	
	@Override
	public boolean postInit() {
		if (!super.postInit()) {
			return false;
		}
		
		return true;
	}
	
	private void registerItemVariants() {
		List<ResourceLocation> list = new LinkedList<>();
    	for (ItemType type : ItemType.values()) {
    		list.add(new ResourceLocation(NostrumMagica.MODID, type.getUnlocalizedKey()));
    	}
		
    	ResourceLocation variants[] = list.toArray(new ResourceLocation[0]);
    	ModelBakery.registerItemVariants(ItemMagicBauble.instance(), variants);
	}
	
	private void registerItemModels() {
		
    	
    	for (ItemType type : ItemType.values()) {
    		ClientProxy.registerModel(ItemMagicBauble.instance(),
    				ItemMagicBauble.getMetaFromType(type),
					type.getUnlocalizedKey());
		}
	}
}
