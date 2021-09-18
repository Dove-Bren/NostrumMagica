package com.smanzana.nostrummagica.integration.baubles;

import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrumaetheria.api.proxy.APIProxy;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.integration.baubles.inventory.BaubleInventoryHelper;
import com.smanzana.nostrummagica.integration.baubles.items.ItemAetherCloak;
import com.smanzana.nostrummagica.integration.baubles.items.ItemMagicBauble;
import com.smanzana.nostrummagica.integration.baubles.items.ItemMagicBauble.ItemType;
import com.smanzana.nostrummagica.items.InfusedGemItem;
import com.smanzana.nostrummagica.items.NostrumResourceItem;
import com.smanzana.nostrummagica.items.NostrumResourceItem.ResourceType;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.research.NostrumResearch;
import com.smanzana.nostrummagica.research.NostrumResearch.NostrumResearchTab;
import com.smanzana.nostrummagica.research.NostrumResearch.Size;
import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.rituals.RitualRegistry;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeModifyCenterItemGeneric;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeSpawnItem;
import com.smanzana.nostrummagica.rituals.requirements.RRequirementResearch;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.triggers.DamagedTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.SelfTrigger;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;

//
public class BaublesProxy {
	
	private boolean enabled;
	
	public BaublesProxy() {
		this.enabled = false;
	}
	
	public void enable() {
		this.enabled = true;
	}
	
	public boolean preInit() {
		if (!enabled) {
			return false;
		}
		
		MinecraftForge.EVENT_BUS.register(this);
		
		registerBaubleQuests();
		return true;
	}
	
	public boolean init() {
		if (!enabled) {
			return false;
		}

		registerBaubleRituals();
		registerBaubleResearch();
		
		return true;
	}
	
	public boolean postInit() {
		if (!enabled) {
			return false;
		}
		
		registerLore();
		
		return true;
	}
	
	@SubscribeEvent
	private void registerItems(RegistryEvent.Register<Item> event) {
		final IForgeRegistry<Item> registry = event.getRegistry();
		
    	registry.register(ItemMagicBauble.instance());
    	registry.register(ItemAetherCloak.instance());
	}
	
	private void registerBaubleQuests() {
		
	}
	
	private void registerBaubleRituals() {
		RitualRecipe recipe;
		
		recipe = RitualRecipe.createTier3("small_ribbon",
				ItemMagicBauble.getItem(ItemType.RIBBON_SMALL, 1),
				null,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.SPIDER_SILK, ReagentType.MANI_DUST, ReagentType.SPIDER_SILK},
				NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1),
				new ItemStack[] {new ItemStack(Items.GOLD_NUGGET), new ItemStack(Item.getItemFromBlock(Blocks.WOOL)), new ItemStack(Item.getItemFromBlock(Blocks.WOOL)), new ItemStack(Items.GOLD_NUGGET)},
				new RRequirementResearch("ribbons"),
				new OutcomeSpawnItem(ItemMagicBauble.getItem(ItemType.RIBBON_SMALL, 1)));
		RitualRegistry.instance().addRitual(recipe);
		
		recipe = RitualRecipe.createTier3("mana_ribbon",
				ItemMagicBauble.getItem(ItemType.RIBBON_MEDIUM, 1),
				null,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.SPIDER_SILK, ReagentType.MANI_DUST, ReagentType.SPIDER_SILK},
				ItemMagicBauble.getItem(ItemType.RIBBON_SMALL, 1),
				new ItemStack[] {new ItemStack(Items.GOLD_INGOT), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1), null, new ItemStack(Items.GOLD_INGOT)},
				new RRequirementResearch("ribbons"),
				new OutcomeSpawnItem(ItemMagicBauble.getItem(ItemType.RIBBON_MEDIUM, 1)));
		RitualRegistry.instance().addRitual(recipe);
		
		recipe = RitualRecipe.createTier3("jeweled_ribbon",
				ItemMagicBauble.getItem(ItemType.RIBBON_LARGE, 1),
				null,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.SPIDER_SILK, ReagentType.MANI_DUST, ReagentType.SPIDER_SILK},
				ItemMagicBauble.getItem(ItemType.RIBBON_MEDIUM, 1),
				new ItemStack[] {new ItemStack(Items.DIAMOND), NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1), new ItemStack(Items.EMERALD), new ItemStack(Items.DIAMOND)},
				new RRequirementResearch("ribbons"),
				new OutcomeSpawnItem(ItemMagicBauble.getItem(ItemType.RIBBON_LARGE, 1)));
		RitualRegistry.instance().addRitual(recipe);
		
		recipe = RitualRecipe.createTier3("fierce_ribbon",
				ItemMagicBauble.getItem(ItemType.RIBBON_FIERCE, 1),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST, ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST},
				ItemMagicBauble.getItem(ItemType.RIBBON_LARGE, 1),
				new ItemStack[] {NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), NostrumResourceItem.getItem(ResourceType.SLAB_FIERCE, 1), new ItemStack(Items.DIAMOND), NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1)},
				new RRequirementResearch("ribbons_enhanced"),
				new OutcomeSpawnItem(ItemMagicBauble.getItem(ItemType.RIBBON_FIERCE, 1)));
		RitualRegistry.instance().addRitual(recipe);
		
		recipe = RitualRecipe.createTier3("kind_ribbon",
				ItemMagicBauble.getItem(ItemType.RIBBON_KIND, 1),
				null,
				new ReagentType[] {ReagentType.SKY_ASH, ReagentType.CRYSTABLOOM, ReagentType.SKY_ASH, ReagentType.CRYSTABLOOM},
				ItemMagicBauble.getItem(ItemType.RIBBON_LARGE, 1),
				new ItemStack[] {NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), NostrumResourceItem.getItem(ResourceType.SLAB_KIND, 1), new ItemStack(Items.EMERALD), NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1)},
				new RRequirementResearch("ribbons_enhanced"),
				new OutcomeSpawnItem(ItemMagicBauble.getItem(ItemType.RIBBON_KIND, 1)));
		RitualRegistry.instance().addRitual(recipe);
		
		recipe = RitualRecipe.createTier3("belt_ender",
				ItemMagicBauble.getItem(ItemType.BELT_ENDER, 1),
				EMagicElement.ENDER,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.GINSENG, ReagentType.SPIDER_SILK, ReagentType.SPIDER_SILK},
				InfusedGemItem.instance().getGem(EMagicElement.ENDER, 1),
				new ItemStack[] {new ItemStack(Items.LEATHER), new ItemStack(Items.LEATHER), new ItemStack(Items.LEATHER), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1)},
				new RRequirementResearch("belts"),
				new OutcomeSpawnItem(ItemMagicBauble.getItem(ItemType.BELT_ENDER, 1)));
		RitualRegistry.instance().addRitual(recipe);
		
		recipe = RitualRecipe.createTier3("belt_lightning",
				ItemMagicBauble.getItem(ItemType.BELT_LIGHTNING, 1),
				EMagicElement.LIGHTNING,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.BLACK_PEARL, ReagentType.SPIDER_SILK, ReagentType.SPIDER_SILK},
				InfusedGemItem.instance().getGem(EMagicElement.LIGHTNING, 1),
				new ItemStack[] {new ItemStack(Items.LEATHER), new ItemStack(Items.LEATHER), new ItemStack(Items.LEATHER), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1)},
				new RRequirementResearch("belts"),
				new OutcomeSpawnItem(ItemMagicBauble.getItem(ItemType.BELT_LIGHTNING, 1)));
		RitualRegistry.instance().addRitual(recipe);
		
		recipe = RitualRecipe.createTier3("ring_gold",
				ItemMagicBauble.getItem(ItemType.RING_GOLD, 1),
				null,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.MANI_DUST, ReagentType.MANI_DUST, ReagentType.MANI_DUST},
				new ItemStack(Items.GOLD_INGOT),
				new ItemStack[] {new ItemStack(Items.GOLD_NUGGET), NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), new ItemStack(Items.GOLD_NUGGET), new ItemStack(Items.GOLD_NUGGET)},
				new RRequirementResearch("rings"),
				new OutcomeSpawnItem(ItemMagicBauble.getItem(ItemType.RING_GOLD, 1)));
		RitualRegistry.instance().addRitual(recipe);
		
		recipe = RitualRecipe.createTier3("ring_gold_true",
				ItemMagicBauble.getItem(ItemType.RING_GOLD_TRUE, 1),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANDRAKE_ROOT, ReagentType.CRYSTABLOOM, ReagentType.BLACK_PEARL},
				ItemMagicBauble.getItem(ItemType.RING_GOLD, 1),
				new ItemStack[] {new ItemStack(Items.GOLD_INGOT), NostrumResourceItem.getItem(ResourceType.SLAB_FIERCE, 1), null, new ItemStack(Items.GOLD_INGOT)},
				new RRequirementResearch("rings_true"),
				new OutcomeSpawnItem(ItemMagicBauble.getItem(ItemType.RING_GOLD_TRUE, 1)));
		RitualRegistry.instance().addRitual(recipe);
		
		recipe = RitualRecipe.createTier3("ring_gold_corrupted",
				ItemMagicBauble.getItem(ItemType.RING_GOLD_CORRUPTED, 1),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANDRAKE_ROOT, ReagentType.CRYSTABLOOM, ReagentType.BLACK_PEARL},
				ItemMagicBauble.getItem(ItemType.RING_GOLD, 1),
				new ItemStack[] {new ItemStack(Items.GOLD_INGOT), NostrumResourceItem.getItem(ResourceType.SLAB_KIND, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), new ItemStack(Items.GOLD_INGOT)},
				new RRequirementResearch("rings_corrupted"),
				new OutcomeSpawnItem(ItemMagicBauble.getItem(ItemType.RING_GOLD_CORRUPTED, 1)));
		RitualRegistry.instance().addRitual(recipe);
		
		
		ItemStack silver;
		List<ItemStack> silvers = OreDictionary.getOres("ingotSilver");
		
		if (silvers == null || silvers.isEmpty()) {
			silver = new ItemStack(Items.IRON_INGOT);
		} else {
			silver = new ItemStack(silvers.get(0).getItem(), 1, OreDictionary.WILDCARD_VALUE);
		}
		
		recipe = RitualRecipe.createTier3("ring_silver",
				ItemMagicBauble.getItem(ItemType.RING_SILVER, 1),
				null,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.MANI_DUST, ReagentType.MANI_DUST, ReagentType.MANI_DUST},
				silver,
				new ItemStack[] {silver, NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), silver, silver},
				new RRequirementResearch("rings"),
				new OutcomeSpawnItem(ItemMagicBauble.getItem(ItemType.RING_SILVER, 1)));
		RitualRegistry.instance().addRitual(recipe);
		
		recipe = RitualRecipe.createTier3("ring_silver_true",
				ItemMagicBauble.getItem(ItemType.RING_SILVER_TRUE, 1),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANDRAKE_ROOT, ReagentType.CRYSTABLOOM, ReagentType.BLACK_PEARL},
				ItemMagicBauble.getItem(ItemType.RING_SILVER, 1),
				new ItemStack[] {silver, NostrumResourceItem.getItem(ResourceType.SLAB_KIND, 1), silver, silver},
				new RRequirementResearch("rings_true"),
				new OutcomeSpawnItem(ItemMagicBauble.getItem(ItemType.RING_SILVER_TRUE, 1)));
		RitualRegistry.instance().addRitual(recipe);
		
		recipe = RitualRecipe.createTier3("ring_silver_corrupted",
				ItemMagicBauble.getItem(ItemType.RING_SILVER_CORRUPTED, 1),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANDRAKE_ROOT, ReagentType.CRYSTABLOOM, ReagentType.BLACK_PEARL},
				ItemMagicBauble.getItem(ItemType.RING_SILVER, 1),
				new ItemStack[] {silver, NostrumResourceItem.getItem(ResourceType.SLAB_FIERCE, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), silver},
				new RRequirementResearch("rings_corrupted"),
				new OutcomeSpawnItem(ItemMagicBauble.getItem(ItemType.RING_SILVER_CORRUPTED, 1)));
		RitualRegistry.instance().addRitual(recipe);
		
		recipe = RitualRecipe.createTier3("float_guard",
				ItemMagicBauble.getItem(ItemType.TRINKET_FLOAT_GUARD, 1),
				EMagicElement.WIND,
				new ReagentType[] {ReagentType.SKY_ASH, ReagentType.GRAVE_DUST, ReagentType.MANI_DUST, ReagentType.SKY_ASH},
				new ItemStack(Items.GOLD_INGOT),
				new ItemStack[] {NostrumResourceItem.getItem(ResourceType.SPRITE_CORE, 1), NostrumResourceItem.getItem(ResourceType.SPRITE_CORE, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1), NostrumResourceItem.getItem(ResourceType.SPRITE_CORE, 1)},
				new RRequirementResearch("ribbons"),
				new OutcomeSpawnItem(ItemMagicBauble.getItem(ItemType.TRINKET_FLOAT_GUARD, 1)));
		RitualRegistry.instance().addRitual(recipe);
		
		recipe = RitualRecipe.createTier3("shield_ring_small",
				ItemMagicBauble.getItem(ItemType.SHIELD_RING_SMALL, 1),
				EMagicElement.EARTH,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST, ReagentType.MANI_DUST, ReagentType.MANDRAKE_ROOT},
				ItemMagicBauble.getItem(ItemType.RING_SILVER, 1),
				new ItemStack[] {NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), SpellRune.getRune(SelfTrigger.instance()), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1)},
				new RRequirementResearch("shield_rings"),
				new OutcomeSpawnItem(ItemMagicBauble.getItem(ItemType.SHIELD_RING_SMALL, 1)));
		RitualRegistry.instance().addRitual(recipe);
		
		recipe = RitualRecipe.createTier3("shield_ring_large",
				ItemMagicBauble.getItem(ItemType.SHIELD_RING_LARGE, 1),
				EMagicElement.EARTH,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST, ReagentType.MANI_DUST, ReagentType.MANDRAKE_ROOT},
				ItemMagicBauble.getItem(ItemType.SHIELD_RING_SMALL, 1),
				new ItemStack[] {NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), silver, NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1)},
				new RRequirementResearch("shield_rings"),
				new OutcomeSpawnItem(ItemMagicBauble.getItem(ItemType.SHIELD_RING_LARGE, 1)));
		RitualRegistry.instance().addRitual(recipe);
		
		recipe = RitualRecipe.createTier3("elude_cape_small",
				ItemMagicBauble.getItem(ItemType.ELUDE_CAPE_SMALL, 1),
				EMagicElement.WIND,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST, ReagentType.MANI_DUST, ReagentType.MANDRAKE_ROOT},
				new ItemStack(Blocks.WOOL, 1, OreDictionary.WILDCARD_VALUE),
				new ItemStack[] {NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), SpellRune.getRune(DamagedTrigger.instance()), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1)},
				new RRequirementResearch("elude_capes"),
				new OutcomeSpawnItem(ItemMagicBauble.getItem(ItemType.ELUDE_CAPE_SMALL, 1)));
		RitualRegistry.instance().addRitual(recipe);
		
		recipe = RitualRecipe.createTier3("aether_cloak",
				new ItemStack(ItemAetherCloak.instance()),
				EMagicElement.ICE,
				new ReagentType[] {ReagentType.MANDRAKE_ROOT, ReagentType.SPIDER_SILK, ReagentType.BLACK_PEARL, ReagentType.SKY_ASH},
				new ItemStack(APIProxy.AetherBatterySmallBlock),
				new ItemStack[] {new ItemStack(APIProxy.AetherGemItem, 1, OreDictionary.WILDCARD_VALUE), ItemMagicBauble.getItem(ItemType.ELUDE_CAPE_SMALL, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1), new ItemStack(APIProxy.AetherGemItem, 1, OreDictionary.WILDCARD_VALUE)},
				new RRequirementResearch("aether_cloaks"),
				new OutcomeSpawnItem(new ItemStack(ItemAetherCloak.instance())));
		RitualRegistry.instance().addRitual(recipe);
		
		ItemStack casterCloak = new ItemStack(ItemAetherCloak.instance());
		ItemAetherCloak.instance().setAetherCaster(casterCloak, true);
		recipe = RitualRecipe.createTier3("aether_cloak_caster_upgrade",
				casterCloak,
				EMagicElement.FIRE,
				new ReagentType[] {ReagentType.GRAVE_DUST, ReagentType.SKY_ASH, ReagentType.BLACK_PEARL, ReagentType.CRYSTABLOOM},
				new ItemStack(ItemAetherCloak.instance()),
				new ItemStack[] {new ItemStack(APIProxy.PassivePendantItem, 1, OreDictionary.WILDCARD_VALUE), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1), null, new ItemStack(APIProxy.PassivePendantItem, 1, OreDictionary.WILDCARD_VALUE)},
				new RRequirementResearch("aether_cloaks"),
				new OutcomeModifyCenterItemGeneric((world, player, item, otherItems, centerPos, recipeIn) -> {
					if (item != null && item.getItem() instanceof ItemAetherCloak) {
						((ItemAetherCloak) item.getItem()).setAetherCaster(item, true);
					}
				}, Lists.newArrayList("Allows using aether from the cloak in place of reagents")));
		RitualRegistry.instance().addRitual(recipe);
	}
	
	private void registerBaubleResearch() {
		NostrumResearch.startBuilding()
			.parent("enchanted_armor")
			.lore(NostrumResourceItem.instance())
			.reference("ritual::small_ribbon", "ritual.small_ribbon.name")
		.build("ribbons", NostrumResearchTab.OUTFITTING, Size.NORMAL, -3, 0, true, ItemMagicBauble.getItem(ItemType.RIBBON_SMALL, 1));
		
		NostrumResearch.startBuilding()
			.parent("ribbons")
			.hiddenParent("vani")
			.reference("ritual::mana_ribbon", "ritual.mana_ribbon.name")
			.reference("ritual::jeweled_ribbon", "ritual.jeweled_ribbon.name")
			.reference("ritual::fierce_ribbon", "ritual.fierce_ribbon.name")
			.reference("ritual::kind_ribbon", "ritual.kind_ribbon.name")
		.build("ribbons_enhanced", NostrumResearchTab.OUTFITTING, Size.NORMAL, -3, 1, true, ItemMagicBauble.getItem(ItemType.RIBBON_LARGE, 1));
		
		NostrumResearch.startBuilding()
			.parent("ribbons")
			.reference("ritual::ring_gold", "ritual.ring_gold.name")
			.reference("ritual::ring_silver", "ritual.ring_silver.name")
		.build("rings", NostrumResearchTab.OUTFITTING, Size.NORMAL, -4, 0, true, ItemMagicBauble.getItem(ItemType.RING_GOLD, 1));
		
		NostrumResearch.startBuilding()
			.parent("rings")
			.hiddenParent("kind_infusion")
			.hiddenParent("fierce_infusion")
			.reference("ritual::ring_gold_true", "ritual.ring_gold_true.name")
			.reference("ritual::ring_silver_true", "ritual.ring_silver_true.name")
		.build("rings_true", NostrumResearchTab.OUTFITTING, Size.NORMAL, -4, 1, true, ItemMagicBauble.getItem(ItemType.RING_GOLD_TRUE, 1));
		
		NostrumResearch.startBuilding()
			.parent("rings_true")
			.reference("ritual::ring_gold_corrupted", "ritual.ring_gold_corrupted.name")
			.reference("ritual::ring_silver_corrupted", "ritual.ring_silver_corrupted.name")
		.build("rings_corrupted", NostrumResearchTab.OUTFITTING, Size.NORMAL, -4, 2, true, ItemMagicBauble.getItem(ItemType.RING_GOLD_CORRUPTED, 1));
		
		NostrumResearch.startBuilding()
			.parent("rings")
			.hiddenParent("kani")
			.reference("ritual::belt_ender", "ritual.belt_ender.name")
			.reference("ritual::belt_lightning", "ritual.belt_lightning.name")
		.build("belts", NostrumResearchTab.OUTFITTING, Size.NORMAL, -5, 0, true, ItemMagicBauble.getItem(ItemType.BELT_ENDER, 1));
		
		if (NostrumMagica.aetheria.isEnabled()) {
			NostrumResearch.startBuilding()
				.parent("rings")
				.hiddenParent("kani")
				.hiddenParent("aether_gem")
				.reference("ritual::shield_ring_small", "ritual.shield_ring_small.name")
				.reference("ritual::shield_ring_large", "ritual.shield_ring_large.name")
			.build("shield_rings", NostrumResearchTab.OUTFITTING, Size.NORMAL, -4, -1, true, ItemMagicBauble.getItem(ItemType.SHIELD_RING_SMALL, 1));

			NostrumResearch.startBuilding()
				.parent("belts")
				.hiddenParent("shield_rings")
				.reference("ritual::elude_cape_small", "ritual.elude_cape_small.name")
			.build("elude_capes", NostrumResearchTab.OUTFITTING, Size.NORMAL, -6, 0, true, ItemMagicBauble.getItem(ItemType.ELUDE_CAPE_SMALL, 1));
			
			NostrumResearch.startBuilding()
				.parent("elude_capes")
				.reference("ritual::aether_cloak", "ritual.aether_cloak.name")
				.reference("ritual::aether_cloak_caster_upgrade", "ritual.aether_cloak_caster_upgrade.name")
			.build("aether_cloaks", NostrumResearchTab.OUTFITTING, Size.NORMAL, -6, 1, true, new ItemStack(ItemAetherCloak.instance()));
			
		}
	}
	
	private void registerLore() {
		LoreRegistry.instance().register(ItemMagicBauble.instance());
		LoreRegistry.instance().register(ItemAetherCloak.instance());
	}
	
	public void reinitResearch() {
		registerBaubleResearch();
	}
	
	public IInventory getBaubles(EntityPlayer player) {
		if (!enabled) {
			return null;
		}
		
		return BaubleInventoryHelper.getBaubleInventory(player);
	}
	
	public boolean isEnabled() {
		return this.enabled;
	}
	
	
}
