package com.smanzana.nostrummagica.aetheria;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.aetheria.blocks.WispBlock;
import com.smanzana.nostrummagica.items.AltarItem;
import com.smanzana.nostrummagica.items.NostrumResourceItem;
import com.smanzana.nostrummagica.items.NostrumResourceItem.ResourceType;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.rituals.RitualRegistry;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeSpawnItem;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

public class AetheriaProxy {
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
		
		registerItems();
		registerBlocks();
		registerAetheriaQuests();
		registerAetheriaRituals();
		return true;
	}
	
	public boolean init() {
		if (!enabled) {
			return false;
		}
		
		return true;
	}
	
	public boolean postInit() {
		if (!enabled) {
			return false;
		}
		
		registerLore();
		
		return true;
	}
	
	private void registerItems() {
//		ItemMagicBauble.instance().setRegistryName(NostrumMagica.MODID, ItemMagicBauble.ID);
//    	GameRegistry.register(ItemMagicBauble.instance());
//    	ItemMagicBauble.init();
	}
	
	private void registerBlocks() {
		GameRegistry.register(WispBlock.instance(),
    			new ResourceLocation(NostrumMagica.MODID, WispBlock.ID));
    	GameRegistry.register(
    			(new ItemBlock(WispBlock.instance()).setRegistryName(WispBlock.ID)
    					.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(WispBlock.ID))
    			);
    	WispBlock.init();
	}
	
	private void registerAetheriaQuests() {
//		new NostrumQuest("ribbons", QuestType.CHALLENGE, 3,
//    			0, // Control
//    			0, // Technique
//    			0, // Finesse
//    			new String[0],
//    			null, null,
//    			new IReward[]{new AttributeReward(AwardType.REGEN, 0.015f)})
//    		.offset(2, -1);
	}
	
	private void registerAetheriaRituals() {
//		RitualRecipe recipe;
//		
//		recipe = RitualRecipe.createTier3("small_ribbon",
//				ItemMagicBauble.getItem(ItemType.RIBBON_SMALL, 1),
//				null,
//				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.SPIDER_SILK, ReagentType.MANI_DUST, ReagentType.SPIDER_SILK},
//				NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1),
//				new ItemStack[] {new ItemStack(Items.GOLD_NUGGET), new ItemStack(Item.getItemFromBlock(Blocks.WOOL)), new ItemStack(Item.getItemFromBlock(Blocks.WOOL)), new ItemStack(Items.GOLD_NUGGET)},
//				new RRequirementQuest("ribbons"),
//				new OutcomeSpawnItem(ItemMagicBauble.getItem(ItemType.RIBBON_SMALL, 1)));
//		RitualRegistry.instance().addRitual(recipe);
		
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("wisp_crystal",
						new ItemStack(WispBlock.instance()),
						null,
						new ReagentType[] {ReagentType.MANI_DUST, ReagentType.MANI_DUST, ReagentType.MANI_DUST, ReagentType.MANI_DUST},
						new ItemStack(AltarItem.instance()),
						new ItemStack[] {NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1), new ItemStack(Blocks.OBSIDIAN, 1, OreDictionary.WILDCARD_VALUE), NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1)},
						null,
						new OutcomeSpawnItem(new ItemStack(WispBlock.instance()))
						)
				);
	}
	
	private void registerLore() {
		//LoreRegistry.instance().register(ItemMagicBauble.instance());
	}
	
	public boolean isEnabled() {
		return this.enabled;
	}
}
