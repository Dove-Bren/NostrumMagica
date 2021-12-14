package com.smanzana.nostrummagica.integration.aetheria;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.render.tile.TileEntityAetherInfuserRenderer;
import com.smanzana.nostrummagica.client.render.tile.TileEntityWispBlockRenderer;
import com.smanzana.nostrummagica.integration.aetheria.blocks.AetherInfuser;
import com.smanzana.nostrummagica.integration.aetheria.blocks.WispBlock;
import com.smanzana.nostrummagica.integration.aetheria.items.AetherResourceType;
import com.smanzana.nostrummagica.integration.aetheria.items.NostrumAetherResourceItem;
import com.smanzana.nostrummagica.integration.baubles.items.ItemAetherLens;
import com.smanzana.nostrummagica.integration.baubles.items.ItemAetherLens.LensType;
import com.smanzana.nostrummagica.proxy.ClientProxy;

import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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
    	TileEntityAetherInfuserRenderer.init();
		
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
	
	private void registerItemVariants(ModelRegistryEvent event) {
		List<ResourceLocation> list = new LinkedList<>();
    	for (AetherResourceType type : AetherResourceType.values()) {
    		list.add(new ResourceLocation(NostrumMagica.MODID, type.getUnlocalizedKey()));
    	}
		
    	ResourceLocation variants[] = list.toArray(new ResourceLocation[list.size()]);
    	ModelBakery.registerItemVariants(NostrumAetherResourceItem.instance(), variants);
    	
    	
    	list.clear();
		for (LensType type : LensType.values()) {
			list.add(new ResourceLocation(NostrumMagica.MODID,
					ItemAetherLens.UNLOC_PREFIX + type.getUnlocSuffix()));
		}
		variants = list.toArray(new ResourceLocation[list.size()]);
		ModelBakery.registerItemVariants(ItemAetherLens.instance(), variants);
	}
	
	@SubscribeEvent
	public void registerAllModels(ModelRegistryEvent event) {
		registerItemVariants(event);
		
		ClientProxy.registerModel(Item.getItemFromBlock(WispBlock.instance()),
				0,
				WispBlock.ID);
    	
    	for (AetherResourceType type : AetherResourceType.values()) {
    		ClientProxy.registerModel(NostrumAetherResourceItem.instance(),
    				NostrumAetherResourceItem.getMetaFromType(type),
					type.getUnlocalizedKey());
		}

		ClientProxy.registerModel(Item.getItemFromBlock(AetherInfuser.instance()),
				0,
				AetherInfuser.ID);
    	
    	for (LensType type : LensType.values()) {
    		ClientProxy.registerModel(ItemAetherLens.instance(),
    				ItemAetherLens.MetaFromType(type),
    				ItemAetherLens.UNLOC_PREFIX + type.getUnlocSuffix());
		}
	}
}
