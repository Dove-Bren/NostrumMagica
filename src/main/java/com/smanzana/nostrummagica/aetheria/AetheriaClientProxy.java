package com.smanzana.nostrummagica.aetheria;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.aetheria.blocks.WispBlock;
import com.smanzana.nostrummagica.aetheria.items.AetherResourceType;
import com.smanzana.nostrummagica.aetheria.items.NostrumAetherResourceItem;
import com.smanzana.nostrummagica.client.render.TileEntityWispBlockRenderer;
import com.smanzana.nostrummagica.proxy.ClientProxy;

import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

//
public class AetheriaClientProxy extends AetheriaProxy {
	
	public AetheriaClientProxy() {
		super();
	}
	
	@Override
	public boolean preInit() {
		if (!super.preInit()) {
			return false;
		}

    	TileEntityWispBlockRenderer.init();
		
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
    	for (AetherResourceType type : AetherResourceType.values()) {
    		list.add(new ResourceLocation(NostrumMagica.MODID, type.getUnlocalizedKey()));
    	}
		
    	ResourceLocation variants[] = list.toArray(new ResourceLocation[list.size()]);
    	ModelBakery.registerItemVariants(NostrumAetherResourceItem.instance(), variants);
	}
	
	private void registerItemModels() {
		ClientProxy.registerModel(Item.getItemFromBlock(WispBlock.instance()),
				0,
				WispBlock.ID);
    	
    	for (AetherResourceType type : AetherResourceType.values()) {
    		ClientProxy.registerModel(NostrumAetherResourceItem.instance(),
    				NostrumAetherResourceItem.getMetaFromType(type),
					type.getUnlocalizedKey());
		}
	}
}
