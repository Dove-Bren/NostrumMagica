package com.smanzana.nostrummagica.integration.enderio;

//
public class EnderIOClientProxy extends EnderIOProxy {
	
	public EnderIOClientProxy() {
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
//		List<ResourceLocation> list = new LinkedList<>();
//    	for (ItemType type : ItemType.values()) {
//    		list.add(new ResourceLocation(NostrumMagica.MODID, type.getUnlocalizedKey()));
//    	}
//		
//    	ResourceLocation variants[] = list.toArray(new ResourceLocation[0]);
//    	ModelBakery.registerItemVariants(ItemMagicEnderIO.instance(), variants);
	}
	
	private void registerItemModels() {
//    	for (ItemType type : ItemType.values()) {
//    		ClientProxy.registerModel(ItemMagicEnderIO.instance(),
//    				ItemMagicEnderIO.getMetaFromType(type),
//					type.getUnlocalizedKey());
//		}
	}
}
