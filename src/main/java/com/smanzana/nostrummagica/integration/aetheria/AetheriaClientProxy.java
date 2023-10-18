package com.smanzana.nostrummagica.integration.aetheria;

import com.smanzana.nostrummagica.client.render.tile.TileEntityAetherInfuserRenderer;
import com.smanzana.nostrummagica.integration.aetheria.blocks.AetherInfuserTileEntity;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

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
		// All of this handled because each item has its own ID?
//		ClientProxy.registerModel(Item.getItemFromBlock(WispBlock.instance()),
//				0,
//				WispBlock.ID);
    	
//    	for (AetherResourceType type : AetherResourceType.values()) {
//    		ClientProxy.registerModel(NostrumAetherResourceItem.instance(),
//    				NostrumAetherResourceItem.getMetaFromType(type),
//					type.getUnlocalizedKey());
//		}

//		ClientProxy.registerModel(Item.getItemFromBlock(AetherInfuser.instance()),
//				0,
//				AetherInfuser.ID);
    	
//    	for (LensType type : LensType.values()) {
//    		ClientProxy.registerModel(ItemAetherLens.instance(),
//    				ItemAetherLens.MetaFromType(type),
//    				ItemAetherLens.UNLOC_PREFIX + type.getUnlocSuffix());
//		}
	}
	
	@SubscribeEvent
	public void clientSetup(FMLClientSetupEvent event) {
		ClientRegistry.bindTileEntitySpecialRenderer(AetherInfuserTileEntity.class, new TileEntityAetherInfuserRenderer());
	}
}
