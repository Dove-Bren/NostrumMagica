package com.smanzana.nostrummagica.aetheria;

import com.smanzana.nostrumaetheria.api.proxy.APIProxy;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.aetheria.blocks.WispBlock;
import com.smanzana.nostrummagica.aetheria.items.AetherResourceType;
import com.smanzana.nostrummagica.aetheria.items.NostrumAetherResourceItem;
import com.smanzana.nostrummagica.entity.EntityWisp;
import com.smanzana.nostrummagica.items.AltarItem;
import com.smanzana.nostrummagica.items.NostrumResourceItem;
import com.smanzana.nostrummagica.items.NostrumResourceItem.ResourceType;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.research.NostrumResearch;
import com.smanzana.nostrummagica.research.NostrumResearch.NostrumResearchTab;
import com.smanzana.nostrummagica.research.NostrumResearch.Size;
import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.rituals.RitualRegistry;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeSpawnItem;
import com.smanzana.nostrummagica.rituals.requirements.RRequirementResearch;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
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
	
	public static Item ItemResources = null;
	public static Block BlockWisp = null;
	
	public boolean preInit() {
		if (!enabled) {
			return false;
		}
		
		registerItems();
		registerBlocks();
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
		
		registerLore();
		
		return true;
	}
	
	private void registerItems() {
		ItemResources = NostrumAetherResourceItem.instance();
		ItemResources.setRegistryName(NostrumMagica.MODID, NostrumAetherResourceItem.ID);
    	GameRegistry.register(ItemResources);
    	NostrumAetherResourceItem.init();
	}
	
	private void registerBlocks() {
		BlockWisp = WispBlock.instance();
		GameRegistry.register(BlockWisp,
    			new ResourceLocation(NostrumMagica.MODID, WispBlock.ID));
    	GameRegistry.register(
    			(new ItemBlock(BlockWisp).setRegistryName(WispBlock.ID)
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
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("wisp_crystal",
						new ItemStack(WispBlock.instance()),
						null,
						new ReagentType[] {ReagentType.MANI_DUST, ReagentType.MANI_DUST, ReagentType.MANI_DUST, ReagentType.MANI_DUST},
						new ItemStack(AltarItem.instance()),
						new ItemStack[] {NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1), new ItemStack(APIProxy.AetherBatterySmallBlock), new ItemStack(Blocks.OBSIDIAN, 1, OreDictionary.WILDCARD_VALUE), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1)},
						new RRequirementResearch("wispblock"),
						new OutcomeSpawnItem(new ItemStack(WispBlock.instance()))
						)
				);
	}
	
	private void registerAetheriaResearch() {
		NostrumResearch.startBuilding()
			.hiddenParent("kani")
			.hiddenParent("aether_battery")
			.lore(EntityWisp.LoreKey)
			.reference("ritual::wisp_crystal", "ritual.wisp_crystal.name")
		.build("wispblock", (NostrumResearchTab) APIProxy.ResearchTab, Size.NORMAL, -3, 2, true, new ItemStack(WispBlock.instance()));
	}
	
	private void registerLore() {
		LoreRegistry.instance().register((ILoreTagged) APIProxy.PassivePendantItem);
		LoreRegistry.instance().register((ILoreTagged) APIProxy.ActivePendantItem);
	}
	
	public boolean isEnabled() {
		return this.enabled;
	}
	
	public ItemStack getResourceItem(AetherResourceType type, int count) {
		return NostrumAetherResourceItem.getItem(type, count);
	}

	public void reinitResearch() {
		registerAetheriaResearch();
	}
}
