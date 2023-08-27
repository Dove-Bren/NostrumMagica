package com.smanzana.nostrummagica.integration.enderio;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.integration.enderio.wrappers.EnderIOItemType;
import com.smanzana.nostrummagica.integration.enderio.wrappers.EnderIOItemWrapper;
import com.smanzana.nostrummagica.integration.enderio.wrappers.TravelControllerWrapper;
import com.smanzana.nostrummagica.integration.enderio.wrappers.TravelSourceWrapper;
import com.smanzana.nostrummagica.items.NostrumResourceItem;
import com.smanzana.nostrummagica.items.NostrumResourceItem.ResourceType;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.items.WarlockSword;
import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.rituals.RitualRegistry;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeModifyCenterItemGeneric;
import com.smanzana.nostrummagica.rituals.requirements.RRequirementResearch;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

//
public class EnderIOProxy {
	
	private boolean enabled;
	
	public EnderIOProxy() {
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
		registerEnderIOQuests();
		return true;
	}
	
	public boolean init() {
		if (!enabled) {
			return false;
		}

		registerEnderIORituals();
		registerEnderIOResearch();
		
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
//		ItemMagicEnderIO.instance().setRegistryName(NostrumMagica.MODID, ItemMagicEnderIO.ID);
//    	GameRegistry.register(ItemMagicEnderIO.instance());
//    	ItemMagicEnderIO.init();
	}
	
	private void registerEnderIOQuests() {
//		new NostrumQuest("ribbons", QuestType.CHALLENGE, 3,
//    			0, // Control
//    			0, // Technique
//    			0, // Finesse
//    			new String[0],
//    			null, null,
//    			new IReward[]{new AttributeReward(AwardType.REGEN, 0.015f)})
//    		.offset(2, -1);
//		
//		new NostrumQuest("ribbons_enhanced", QuestType.CHALLENGE, 6,
//    			0, // Control
//    			0, // Technique
//    			0, // Finesse
//    			new String[] {"ribbons"},
//    			null, null,
//    			new IReward[]{new AttributeReward(AwardType.MANA, 0.02f)})
//    		.offset(3, 2);
//		
//		new NostrumQuest("rings", QuestType.CHALLENGE, 4,
//    			0, // Control
//    			0, // Technique
//    			0, // Finesse
//    			new String[] {"ribbons"},
//    			null, null,
//    			new IReward[]{new AttributeReward(AwardType.REGEN, 0.025f)})
//    		.offset(2, 1);
//		
//		new NostrumQuest("rings_true", QuestType.CHALLENGE, 7,
//    			0, // Control
//    			0, // Technique
//    			0, // Finesse
//    			new String[] {"rings"},
//    			null, null,
//    			new IReward[]{new AttributeReward(AwardType.REGEN, 0.025f)})
//    		.offset(3, 4);
//		
//		new NostrumQuest("rings_corrupted", QuestType.CHALLENGE, 8,
//    			0, // Control
//    			0, // Technique
//    			0, // Finesse
//    			new String[] {"rings_true"},
//    			null, null,
//    			new IReward[]{new AttributeReward(AwardType.REGEN, 0.05f)})
//    		.offset(4, 5);
//		
//		new NostrumQuest("belts", QuestType.CHALLENGE, 5,
//    			0, // Control
//    			0, // Technique
//    			0, // Finesse
//    			new String[] {"rings"},
//    			null, null,
//    			new IReward[]{new AttributeReward(AwardType.REGEN, 0.025f)})
//    		.offset(2, 3);
		
	}
	
	private void registerEnderIORituals() {
		RitualRecipe recipe;
		
		recipe = RitualRecipe.createTier3("warlock_sword.upgrade.travel",
				new ItemStack(EnderIOItemWrapper.GetItem(EnderIOItemType.TRAVEL_STAFF), 1),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.SKY_ASH, ReagentType.MANI_DUST, ReagentType.SPIDER_SILK},
				new ItemStack(WarlockSword.instance(), 1, OreDictionary.WILDCARD_VALUE),
				new ItemStack[] {new ItemStack(Items.ENDER_PEARL), new ItemStack(EnderIOItemWrapper.GetItem(EnderIOItemType.TRAVEL_STAFF), 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1), new ItemStack(Items.GOLD_INGOT)},
				new RRequirementResearch("warlock_sword"),
				new OutcomeModifyCenterItemGeneric((world, player, item, otherItems, centerPos, recipeIn) -> {
					if (!item.isEmpty() && item.getItem() instanceof WarlockSword) {
						WarlockSword.setEnderIOTravel(item, true);
					}
				}, Lists.newArrayList("Allows teleportation to EnderIO Travel Anchors")));
		RitualRegistry.instance().addRitual(recipe);
		
//		
//		recipe = RitualRecipe.createTier3("mana_ribbon",
//				ItemMagicEnderIO.getItem(ItemType.RIBBON_MEDIUM, 1),
//				null,
//				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.SPIDER_SILK, ReagentType.MANI_DUST, ReagentType.SPIDER_SILK},
//				ItemMagicEnderIO.getItem(ItemType.RIBBON_SMALL, 1),
//				new ItemStack[] {new ItemStack(Items.GOLD_INGOT), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1), null, new ItemStack(Items.GOLD_INGOT)},
//				new RRequirementResearch("ribbons"),
//				new OutcomeSpawnItem(ItemMagicEnderIO.getItem(ItemType.RIBBON_MEDIUM, 1)));
//		RitualRegistry.instance().addRitual(recipe);
//		
//		recipe = RitualRecipe.createTier3("jeweled_ribbon",
//				ItemMagicEnderIO.getItem(ItemType.RIBBON_LARGE, 1),
//				null,
//				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.SPIDER_SILK, ReagentType.MANI_DUST, ReagentType.SPIDER_SILK},
//				ItemMagicEnderIO.getItem(ItemType.RIBBON_MEDIUM, 1),
//				new ItemStack[] {new ItemStack(Items.DIAMOND), NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1), new ItemStack(Items.EMERALD), new ItemStack(Items.DIAMOND)},
//				new RRequirementResearch("ribbons"),
//				new OutcomeSpawnItem(ItemMagicEnderIO.getItem(ItemType.RIBBON_LARGE, 1)));
//		RitualRegistry.instance().addRitual(recipe);
//		
//		recipe = RitualRecipe.createTier3("fierce_ribbon",
//				ItemMagicEnderIO.getItem(ItemType.RIBBON_FIERCE, 1),
//				null,
//				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST, ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST},
//				ItemMagicEnderIO.getItem(ItemType.RIBBON_LARGE, 1),
//				new ItemStack[] {NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), NostrumResourceItem.getItem(ResourceType.SLAB_FIERCE, 1), new ItemStack(Items.DIAMOND), NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1)},
//				new RRequirementResearch("ribbons_enhanced"),
//				new OutcomeSpawnItem(ItemMagicEnderIO.getItem(ItemType.RIBBON_FIERCE, 1)));
//		RitualRegistry.instance().addRitual(recipe);
//		
//		recipe = RitualRecipe.createTier3("kind_ribbon",
//				ItemMagicEnderIO.getItem(ItemType.RIBBON_KIND, 1),
//				null,
//				new ReagentType[] {ReagentType.SKY_ASH, ReagentType.CRYSTABLOOM, ReagentType.SKY_ASH, ReagentType.CRYSTABLOOM},
//				ItemMagicEnderIO.getItem(ItemType.RIBBON_LARGE, 1),
//				new ItemStack[] {NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), NostrumResourceItem.getItem(ResourceType.SLAB_KIND, 1), new ItemStack(Items.EMERALD), NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1)},
//				new RRequirementResearch("ribbons_enhanced"),
//				new OutcomeSpawnItem(ItemMagicEnderIO.getItem(ItemType.RIBBON_KIND, 1)));
//		RitualRegistry.instance().addRitual(recipe);
//		
//		recipe = RitualRecipe.createTier3("belt_ender",
//				ItemMagicEnderIO.getItem(ItemType.BELT_ENDER, 1),
//				EMagicElement.ENDER,
//				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.GINSENG, ReagentType.SPIDER_SILK, ReagentType.SPIDER_SILK},
//				InfusedGemItem.instance().getGem(EMagicElement.ENDER, 1),
//				new ItemStack[] {new ItemStack(Items.LEATHER), new ItemStack(Items.LEATHER), new ItemStack(Items.LEATHER), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1)},
//				new RRequirementResearch("belts"),
//				new OutcomeSpawnItem(ItemMagicEnderIO.getItem(ItemType.BELT_ENDER, 1)));
//		RitualRegistry.instance().addRitual(recipe);
//		
//		recipe = RitualRecipe.createTier3("belt_lightning",
//				ItemMagicEnderIO.getItem(ItemType.BELT_LIGHTNING, 1),
//				EMagicElement.LIGHTNING,
//				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.BLACK_PEARL, ReagentType.SPIDER_SILK, ReagentType.SPIDER_SILK},
//				InfusedGemItem.instance().getGem(EMagicElement.LIGHTNING, 1),
//				new ItemStack[] {new ItemStack(Items.LEATHER), new ItemStack(Items.LEATHER), new ItemStack(Items.LEATHER), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1)},
//				new RRequirementResearch("belts"),
//				new OutcomeSpawnItem(ItemMagicEnderIO.getItem(ItemType.BELT_LIGHTNING, 1)));
//		RitualRegistry.instance().addRitual(recipe);
//		
//		recipe = RitualRecipe.createTier3("ring_gold",
//				ItemMagicEnderIO.getItem(ItemType.RING_GOLD, 1),
//				null,
//				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.MANI_DUST, ReagentType.MANI_DUST, ReagentType.MANI_DUST},
//				new ItemStack(Items.GOLD_INGOT),
//				new ItemStack[] {new ItemStack(Items.GOLD_NUGGET), NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), new ItemStack(Items.GOLD_NUGGET), new ItemStack(Items.GOLD_NUGGET)},
//				new RRequirementResearch("rings"),
//				new OutcomeSpawnItem(ItemMagicEnderIO.getItem(ItemType.RING_GOLD, 1)));
//		RitualRegistry.instance().addRitual(recipe);
//		
//		recipe = RitualRecipe.createTier3("ring_gold_true",
//				ItemMagicEnderIO.getItem(ItemType.RING_GOLD_TRUE, 1),
//				null,
//				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANDRAKE_ROOT, ReagentType.CRYSTABLOOM, ReagentType.BLACK_PEARL},
//				ItemMagicEnderIO.getItem(ItemType.RING_GOLD, 1),
//				new ItemStack[] {new ItemStack(Items.GOLD_INGOT), NostrumResourceItem.getItem(ResourceType.SLAB_FIERCE, 1), null, new ItemStack(Items.GOLD_INGOT)},
//				new RRequirementResearch("rings_true"),
//				new OutcomeSpawnItem(ItemMagicEnderIO.getItem(ItemType.RING_GOLD_TRUE, 1)));
//		RitualRegistry.instance().addRitual(recipe);
//		
//		recipe = RitualRecipe.createTier3("ring_gold_corrupted",
//				ItemMagicEnderIO.getItem(ItemType.RING_GOLD_CORRUPTED, 1),
//				null,
//				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANDRAKE_ROOT, ReagentType.CRYSTABLOOM, ReagentType.BLACK_PEARL},
//				ItemMagicEnderIO.getItem(ItemType.RING_GOLD, 1),
//				new ItemStack[] {new ItemStack(Items.GOLD_INGOT), NostrumResourceItem.getItem(ResourceType.SLAB_KIND, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), new ItemStack(Items.GOLD_INGOT)},
//				new RRequirementResearch("rings_corrupted"),
//				new OutcomeSpawnItem(ItemMagicEnderIO.getItem(ItemType.RING_GOLD_CORRUPTED, 1)));
//		RitualRegistry.instance().addRitual(recipe);
//		
//		
//		ItemStack silver;
//		List<ItemStack> silvers = OreDictionary.getOres("ingotSilver");
//		
//		if (silvers == null || silvers.isEmpty()) {
//			silver = new ItemStack(Items.IRON_INGOT);
//		} else {
//			silver = new ItemStack(silvers.get(0).getItem(), 1, OreDictionary.WILDCARD_VALUE);
//		}
//		
//		recipe = RitualRecipe.createTier3("ring_silver",
//				ItemMagicEnderIO.getItem(ItemType.RING_SILVER, 1),
//				null,
//				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.MANI_DUST, ReagentType.MANI_DUST, ReagentType.MANI_DUST},
//				silver,
//				new ItemStack[] {silver, NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), silver, silver},
//				new RRequirementResearch("rings"),
//				new OutcomeSpawnItem(ItemMagicEnderIO.getItem(ItemType.RING_SILVER, 1)));
//		RitualRegistry.instance().addRitual(recipe);
//		
//		recipe = RitualRecipe.createTier3("ring_silver_true",
//				ItemMagicEnderIO.getItem(ItemType.RING_SILVER_TRUE, 1),
//				null,
//				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANDRAKE_ROOT, ReagentType.CRYSTABLOOM, ReagentType.BLACK_PEARL},
//				ItemMagicEnderIO.getItem(ItemType.RING_SILVER, 1),
//				new ItemStack[] {silver, NostrumResourceItem.getItem(ResourceType.SLAB_KIND, 1), silver, silver},
//				new RRequirementResearch("rings_true"),
//				new OutcomeSpawnItem(ItemMagicEnderIO.getItem(ItemType.RING_SILVER_TRUE, 1)));
//		RitualRegistry.instance().addRitual(recipe);
//		
//		recipe = RitualRecipe.createTier3("ring_silver_corrupted",
//				ItemMagicEnderIO.getItem(ItemType.RING_SILVER_CORRUPTED, 1),
//				null,
//				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANDRAKE_ROOT, ReagentType.CRYSTABLOOM, ReagentType.BLACK_PEARL},
//				ItemMagicEnderIO.getItem(ItemType.RING_SILVER, 1),
//				new ItemStack[] {silver, NostrumResourceItem.getItem(ResourceType.SLAB_FIERCE, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), silver},
//				new RRequirementResearch("rings_corrupted"),
//				new OutcomeSpawnItem(ItemMagicEnderIO.getItem(ItemType.RING_SILVER_CORRUPTED, 1)));
//		RitualRegistry.instance().addRitual(recipe);
//		
//		recipe = RitualRecipe.createTier3("float_guard",
//				ItemMagicEnderIO.getItem(ItemType.TRINKET_FLOAT_GUARD, 1),
//				EMagicElement.WIND,
//				new ReagentType[] {ReagentType.SKY_ASH, ReagentType.GRAVE_DUST, ReagentType.MANI_DUST, ReagentType.SKY_ASH},
//				new ItemStack(Items.GOLD_INGOT),
//				new ItemStack[] {NostrumResourceItem.getItem(ResourceType.SPRITE_CORE, 1), NostrumResourceItem.getItem(ResourceType.SPRITE_CORE, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1), NostrumResourceItem.getItem(ResourceType.SPRITE_CORE, 1)},
//				new RRequirementResearch("ribbons"),
//				new OutcomeSpawnItem(ItemMagicEnderIO.getItem(ItemType.TRINKET_FLOAT_GUARD, 1)));
//		RitualRegistry.instance().addRitual(recipe);
//		
//		recipe = RitualRecipe.createTier3("shield_ring_small",
//				ItemMagicEnderIO.getItem(ItemType.SHIELD_RING_SMALL, 1),
//				EMagicElement.EARTH,
//				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST, ReagentType.MANI_DUST, ReagentType.MANDRAKE_ROOT},
//				ItemMagicEnderIO.getItem(ItemType.RING_SILVER, 1),
//				new ItemStack[] {NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), SpellRune.getRune(SelfTrigger.instance()), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1)},
//				new RRequirementResearch("shield_rings"),
//				new OutcomeSpawnItem(ItemMagicEnderIO.getItem(ItemType.SHIELD_RING_SMALL, 1)));
//		RitualRegistry.instance().addRitual(recipe);
//		
//		recipe = RitualRecipe.createTier3("shield_ring_large",
//				ItemMagicEnderIO.getItem(ItemType.SHIELD_RING_LARGE, 1),
//				EMagicElement.EARTH,
//				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST, ReagentType.MANI_DUST, ReagentType.MANDRAKE_ROOT},
//				ItemMagicEnderIO.getItem(ItemType.SHIELD_RING_SMALL, 1),
//				new ItemStack[] {NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), silver, NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1)},
//				new RRequirementResearch("shield_rings"),
//				new OutcomeSpawnItem(ItemMagicEnderIO.getItem(ItemType.SHIELD_RING_LARGE, 1)));
//		RitualRegistry.instance().addRitual(recipe);
//		
//		recipe = RitualRecipe.createTier3("elude_cape_small",
//				ItemMagicEnderIO.getItem(ItemType.ELUDE_CAPE_SMALL, 1),
//				EMagicElement.WIND,
//				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST, ReagentType.MANI_DUST, ReagentType.MANDRAKE_ROOT},
//				new ItemStack(Blocks.WOOL, 1, OreDictionary.WILDCARD_VALUE),
//				new ItemStack[] {NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), SpellRune.getRune(DamagedTrigger.instance()), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1)},
//				new RRequirementResearch("elude_capes"),
//				new OutcomeSpawnItem(ItemMagicEnderIO.getItem(ItemType.ELUDE_CAPE_SMALL, 1)));
//		RitualRegistry.instance().addRitual(recipe);
	}
	
	private void registerEnderIOResearch() {
//		NostrumResearch.startBuilding()
//			.parent("enchanted_armor")
//			.lore(NostrumResourceItem.instance())
//			.reference("ritual::small_ribbon", "ritual.small_ribbon.name")
//		.build("ribbons", NostrumResearchTab.OUTFITTING, Size.NORMAL, -3, 0, true, ItemMagicEnderIO.getItem(ItemType.RIBBON_SMALL, 1));
//		
//		NostrumResearch.startBuilding()
//			.parent("ribbons")
//			.hiddenParent("vani")
//			.reference("ritual::mana_ribbon", "ritual.mana_ribbon.name")
//			.reference("ritual::jeweled_ribbon", "ritual.jeweled_ribbon.name")
//			.reference("ritual::fierce_ribbon", "ritual.fierce_ribbon.name")
//			.reference("ritual::kind_ribbon", "ritual.kind_ribbon.name")
//		.build("ribbons_enhanced", NostrumResearchTab.OUTFITTING, Size.NORMAL, -3, 1, true, ItemMagicEnderIO.getItem(ItemType.RIBBON_LARGE, 1));
//		
//		NostrumResearch.startBuilding()
//			.parent("ribbons")
//			.reference("ritual::ring_gold", "ritual.ring_gold.name")
//			.reference("ritual::ring_silver", "ritual.ring_silver.name")
//		.build("rings", NostrumResearchTab.OUTFITTING, Size.NORMAL, -4, 0, true, ItemMagicEnderIO.getItem(ItemType.RING_GOLD, 1));
//		
//		NostrumResearch.startBuilding()
//			.parent("rings")
//			.hiddenParent("kind_infusion")
//			.hiddenParent("fierce_infusion")
//			.reference("ritual::ring_gold_true", "ritual.ring_gold_true.name")
//			.reference("ritual::ring_silver_true", "ritual.ring_silver_true.name")
//		.build("rings_true", NostrumResearchTab.OUTFITTING, Size.NORMAL, -4, 1, true, ItemMagicEnderIO.getItem(ItemType.RING_GOLD_TRUE, 1));
//		
//		NostrumResearch.startBuilding()
//			.parent("rings_true")
//			.reference("ritual::ring_gold_corrupted", "ritual.ring_gold_corrupted.name")
//			.reference("ritual::ring_silver_corrupted", "ritual.ring_silver_corrupted.name")
//		.build("rings_corrupted", NostrumResearchTab.OUTFITTING, Size.NORMAL, -4, 2, true, ItemMagicEnderIO.getItem(ItemType.RING_GOLD_CORRUPTED, 1));
//		
//		NostrumResearch.startBuilding()
//			.parent("rings")
//			.hiddenParent("kani")
//			.reference("ritual::belt_ender", "ritual.belt_ender.name")
//			.reference("ritual::belt_lightning", "ritual.belt_lightning.name")
//		.build("belts", NostrumResearchTab.OUTFITTING, Size.NORMAL, -5, 0, true, ItemMagicEnderIO.getItem(ItemType.BELT_ENDER, 1));
//		
//		if (NostrumMagica.aetheria.isEnabled()) {
//			NostrumResearch.startBuilding()
//				.parent("rings")
//				.hiddenParent("kani")
//				.hiddenParent("aether_gem")
//				.reference("ritual::shield_ring_small", "ritual.shield_ring_small.name")
//				.reference("ritual::shield_ring_large", "ritual.shield_ring_large.name")
//			.build("shield_rings", NostrumResearchTab.OUTFITTING, Size.NORMAL, -4, -1, true, ItemMagicEnderIO.getItem(ItemType.SHIELD_RING_SMALL, 1));
//
//			NostrumResearch.startBuilding()
//				.parent("belts")
//				.hiddenParent("shield_rings")
//				.reference("ritual::elude_cape_small", "ritual.elude_cape_small.name")
//			.build("elude_capes", NostrumResearchTab.OUTFITTING, Size.NORMAL, -6, 0, true, ItemMagicEnderIO.getItem(ItemType.ELUDE_CAPE_SMALL, 1));
//		}
	}
	
	private void registerLore() {
		//LoreRegistry.instance().register(ItemMagicEnderIO.instance());
	}
	
	public void reinitResearch() {
		registerEnderIOResearch();
	}
	
	public boolean isEnabled() {
		return this.enabled;
	}
	
	public boolean AttemptEnderIOTravel(ItemStack equipped, EnumHand hand, World world, PlayerEntity player, TravelSourceWrapper source) {
		if (enabled) {
			return TravelControllerWrapper.activateTravelAccessable(equipped, hand, world, player, source.getSource());
		}
		
		return false;
	}
	
	
}
