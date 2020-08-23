package com.smanzana.nostrummagica.aetheria;

import com.smanzana.nostrummagica.aetheria.blocks.WispBlock;
import com.smanzana.nostrummagica.client.render.TileEntityWispBlockRenderer;
import com.smanzana.nostrummagica.proxy.ClientProxy;

import net.minecraft.item.Item;

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
//		List<ResourceLocation> list = new LinkedList<>();
//    	for (ItemType type : ItemType.values()) {
//    		list.add(new ResourceLocation(NostrumMagica.MODID, type.getUnlocalizedKey()));
//    	}
//		
//    	ResourceLocation variants[] = list.toArray(new ResourceLocation[0]);
//    	ModelBakery.registerItemVariants(NostrumResourceItem.instance(), variants);
	}
	
	private void registerItemModels() {
		ClientProxy.registerModel(Item.getItemFromBlock(WispBlock.instance()),
				0,
				WispBlock.ID);
    	
//    	for (ItemType type : ItemType.values()) {
//    		ClientProxy.registerModel(ItemMagicBauble.instance(),
//    				ItemMagicBauble.getMetaFromType(type),
//					type.getUnlocalizedKey());
//		}
	}
}
