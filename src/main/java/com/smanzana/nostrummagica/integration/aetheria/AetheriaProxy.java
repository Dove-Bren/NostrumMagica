package com.smanzana.nostrummagica.integration.aetheria;

import javax.annotation.Nullable;

import com.smanzana.nostrumaetheria.api.capability.IAetherBurnable;
import com.smanzana.nostrumaetheria.api.proxy.APIProxy;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ObjectHolder;

public class AetheriaProxy {
	
	// Handles to these Aetheria items if they're loaded.
	// Here because they used to be NostrumMagica items and could be referenced in NBT.
	@ObjectHolder("nostrumaetheria:shield_ring_small") public static @Nullable Item ringShieldSmall; // Requires Aether
	@ObjectHolder("nostrumaetheria:shield_ring_large") public static @Nullable Item ringShieldLarge; // Requires Aether
	@ObjectHolder("nostrumaetheria:elude_cloak") public static @Nullable Item eludeCape; // Requires Aether
	
	@CapabilityInject(IAetherBurnable.class) public static @Nullable Capability<?> AetherBurnableCapability;
	
	private boolean enabled;
	
	public AetheriaProxy() {
		this.enabled = false;
	}
	
	public void enable() {
		this.enabled = true;
	}
	
	public boolean preInit() {
		if (!enabled) {
			return false;
		}
		
		//MinecraftForge.EVENT_BUS.register(this);
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
		
		return true;
	}
	
	public boolean init() {
		if (!enabled) {
			return false;
		}

		registerAetheriaQuests();
		registerAetheriaRituals();
		registerAetheriaResearch();
		
		return true;
	}
	
	public boolean postInit() {
		if (!enabled) {
			return false;
		}
		
		//registerLore();
		
		return true;
	}
	
	protected Item.Properties propAetheria() {
		return new Item.Properties()
				.tab(APIProxy.creativeTab);
	}
	
	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
    	
	}
	
	@SubscribeEvent
	public void registerBlocks(RegistryEvent.Register<Block> event) {
	}
	
	@SubscribeEvent
	public void registerTileEntities(RegistryEvent.Register<BlockEntityType<?>> event) {
	}
	
	private void registerAetheriaQuests() {
		
	}
	
	private void registerAetheriaRituals() {
		
	}
	
	private void registerAetheriaResearch() {
		
	}
	
//	private void registerLore() {
//		LoreRegistry.instance().register((ILoreTagged) APIProxy.PassivePendantItem);
//		LoreRegistry.instance().register((ILoreTagged) APIProxy.ActivePendantItem);
//		LoreRegistry.instance().register((ILoreTagged) spreadAetherLens);
//	}
	
	public boolean isEnabled() {
		return this.enabled;
	}
	
//	public ItemStack getResourceItem(AetherResourceType type, int count) {
//		Item item = null;
//		switch (type) {
//		case FLOWER_GINSENG:
//			item = ginsengFlower;
//			break;
//		case FLOWER_MANDRAKE:
//			item = mandrakeFlower;
//			break;
//		default:
//			break;
//		}
//		
//		if (item == null) {
//			return ItemStack.EMPTY;
//		} else {
//			return new ItemStack(item, count);
//		}
//	}


	public void reinitResearch() {
		registerAetheriaResearch();
	}

	public Object makeBurnable(int burnTicks, float aether) {
		return APIProxy.makeBurnable(burnTicks, aether);
	}
	
	
}
