package com.smanzana.nostrummagica.integration.curios;

import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.integration.curios.inventory.CurioInventoryWrapper;
import com.smanzana.nostrummagica.integration.curios.items.DragonWingPendantItem;
import com.smanzana.nostrummagica.integration.curios.items.NostrumCurios;
import com.smanzana.nostrummagica.items.MagicArmor;
import com.smanzana.nostrummagica.items.NostrumItems;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.research.NostrumResearch;
import com.smanzana.nostrummagica.research.NostrumResearch.NostrumResearchTab;
import com.smanzana.nostrummagica.research.NostrumResearch.Size;
import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeModifyCenterItemGeneric;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeSpawnItem;
import com.smanzana.nostrummagica.rituals.requirements.IRitualRequirement;
import com.smanzana.nostrummagica.rituals.requirements.RRequirementResearch;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import top.theillusivec4.curios.api.CuriosAPI;
import top.theillusivec4.curios.api.imc.CurioIMCMessage;

public class CuriosProxy {

	@SubscribeEvent
	public void sendImc(InterModEnqueueEvent evt) {
		InterModComms.sendTo("curios", CuriosAPI.IMC.REGISTER_TYPE, () -> new CurioIMCMessage("belt"));
		InterModComms.sendTo("curios", CuriosAPI.IMC.REGISTER_TYPE, () -> new CurioIMCMessage("necklace"));
		InterModComms.sendTo("curios", CuriosAPI.IMC.REGISTER_TYPE, () -> new CurioIMCMessage("ring").setSize(2));
		InterModComms.sendTo("curios", CuriosAPI.IMC.REGISTER_TYPE, () -> new CurioIMCMessage("charm"));
		InterModComms.sendTo("curios", CuriosAPI.IMC.REGISTER_TYPE, () -> new CurioIMCMessage("body"));
		InterModComms.sendTo("curios", CuriosAPI.IMC.REGISTER_TYPE, () -> new CurioIMCMessage("pendant"));
	}
	
	private boolean enabled;
	
	public CuriosProxy() {
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}
	
	public void enable() {
		this.enabled = true;
	}

	public void preInit() {
		// TODO Auto-generated method stub
		
	}
	
	public void init() {
		MinecraftForge.EVENT_BUS.register(this);
		registerCurioQuests();
		//registerCurioRituals();
		registerCurioResearch();
		registerLore();
	}
	
	private void registerCurioQuests() {
		
	}
	
	@SubscribeEvent
	public void registerCurioRituals(RegistryEvent.Register<RitualRecipe> event) {
		final IForgeRegistry<RitualRecipe> registry = event.getRegistry();
		RitualRecipe recipe;
		
		recipe = RitualRecipe.createTier3("small_ribbon",
				new ItemStack(NostrumCurios.smallRibbon),
				null,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.SPIDER_SILK, ReagentType.MANI_DUST, ReagentType.SPIDER_SILK},
				Ingredient.fromTag(NostrumTags.Items.CrystalSmall),
				new Ingredient[] {Ingredient.fromTag(Tags.Items.NUGGETS_GOLD), Ingredient.fromTag(ItemTags.WOOL), Ingredient.fromTag(ItemTags.WOOL), Ingredient.fromTag(Tags.Items.NUGGETS_GOLD)},
				new RRequirementResearch("ribbons"),
				new OutcomeSpawnItem(new ItemStack(NostrumCurios.smallRibbon)));
		registry.register(recipe);
		
		recipe = RitualRecipe.createTier3("mana_ribbon",
				new ItemStack(NostrumCurios.mediumRibbon),
				null,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.SPIDER_SILK, ReagentType.MANI_DUST, ReagentType.SPIDER_SILK},
				Ingredient.fromItems(NostrumCurios.smallRibbon),
				new Ingredient[] {Ingredient.fromTag(Tags.Items.INGOTS_GOLD), Ingredient.fromTag(NostrumTags.Items.CrystalMedium), Ingredient.EMPTY, Ingredient.fromTag(Tags.Items.INGOTS_GOLD)},
				new RRequirementResearch("ribbons"),
				new OutcomeSpawnItem(new ItemStack(NostrumCurios.mediumRibbon)));
		registry.register(recipe);
		
		recipe = RitualRecipe.createTier3("jeweled_ribbon",
				new ItemStack(NostrumCurios.largeRibbon),
				null,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.SPIDER_SILK, ReagentType.MANI_DUST, ReagentType.SPIDER_SILK},
				Ingredient.fromItems(NostrumCurios.mediumRibbon),
				new Ingredient[] {Ingredient.fromTag(Tags.Items.GEMS_DIAMOND), Ingredient.fromTag(NostrumTags.Items.CrystalLarge), Ingredient.fromTag(Tags.Items.GEMS_EMERALD), Ingredient.fromTag(Tags.Items.GEMS_DIAMOND)},
				new RRequirementResearch("ribbons"),
				new OutcomeSpawnItem(new ItemStack(NostrumCurios.largeRibbon)));
		registry.register(recipe);
		
		recipe = RitualRecipe.createTier3("fierce_ribbon",
				new ItemStack(NostrumCurios.fierceRibbon),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST, ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST},
				Ingredient.fromItems(NostrumCurios.largeRibbon),
				new Ingredient[] {Ingredient.fromTag(NostrumTags.Items.CrystalSmall), Ingredient.fromTag(NostrumTags.Items.SlabFierce), Ingredient.fromTag(Tags.Items.GEMS_DIAMOND), Ingredient.fromTag(NostrumTags.Items.CrystalSmall)},
				new RRequirementResearch("ribbons_enhanced"),
				new OutcomeSpawnItem(new ItemStack(NostrumCurios.fierceRibbon)));
		registry.register(recipe);
		
		recipe = RitualRecipe.createTier3("kind_ribbon",
				new ItemStack(NostrumCurios.kindRibbon),
				null,
				new ReagentType[] {ReagentType.SKY_ASH, ReagentType.CRYSTABLOOM, ReagentType.SKY_ASH, ReagentType.CRYSTABLOOM},
				Ingredient.fromItems(NostrumCurios.largeRibbon),
				new Ingredient[] {Ingredient.fromTag(NostrumTags.Items.CrystalSmall), Ingredient.fromTag(NostrumTags.Items.SlabKind), Ingredient.fromTag(Tags.Items.GEMS_EMERALD), Ingredient.fromTag(NostrumTags.Items.CrystalSmall)},
				new RRequirementResearch("ribbons_enhanced"),
				new OutcomeSpawnItem(new ItemStack(NostrumCurios.kindRibbon)));
		registry.register(recipe);
		
		recipe = RitualRecipe.createTier3("belt_ender",
				new ItemStack(NostrumCurios.enderBelt),
				EMagicElement.ENDER,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.GINSENG, ReagentType.SPIDER_SILK, ReagentType.SPIDER_SILK},
				Ingredient.fromTag(NostrumTags.Items.InfusedGemEnder),
				new Ingredient[] {Ingredient.fromTag(Tags.Items.LEATHER), Ingredient.fromTag(Tags.Items.LEATHER), Ingredient.fromTag(Tags.Items.LEATHER), Ingredient.fromTag(NostrumTags.Items.CrystalMedium)},
				new RRequirementResearch("belts"),
				new OutcomeSpawnItem(new ItemStack(NostrumCurios.enderBelt)));
		registry.register(recipe);
		
		recipe = RitualRecipe.createTier3("belt_lightning",
				new ItemStack(NostrumCurios.lightningBelt),
				EMagicElement.LIGHTNING,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.BLACK_PEARL, ReagentType.SPIDER_SILK, ReagentType.SPIDER_SILK},
				Ingredient.fromTag(NostrumTags.Items.InfusedGemLightning),
				new Ingredient[] {Ingredient.fromTag(Tags.Items.LEATHER), Ingredient.fromTag(Tags.Items.LEATHER), Ingredient.fromTag(Tags.Items.LEATHER), Ingredient.fromTag(NostrumTags.Items.CrystalMedium)},
				new RRequirementResearch("belts"),
				new OutcomeSpawnItem(new ItemStack(NostrumCurios.lightningBelt)));
		registry.register(recipe);
		
		recipe = RitualRecipe.createTier3("ring_gold",
				new ItemStack(NostrumCurios.ringGold),
				null,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.MANI_DUST, ReagentType.MANI_DUST, ReagentType.MANI_DUST},
				Ingredient.fromTag(Tags.Items.INGOTS_GOLD),
				new Ingredient[] {Ingredient.fromTag(Tags.Items.NUGGETS_GOLD), Ingredient.fromTag(NostrumTags.Items.CrystalSmall), Ingredient.fromTag(Tags.Items.NUGGETS_GOLD), Ingredient.fromTag(Tags.Items.NUGGETS_GOLD)},
				new RRequirementResearch("rings"),
				new OutcomeSpawnItem(new ItemStack(NostrumCurios.ringGold)));
		registry.register(recipe);
		
		recipe = RitualRecipe.createTier3("ring_gold_true",
				new ItemStack(NostrumCurios.ringTrueGold),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANDRAKE_ROOT, ReagentType.CRYSTABLOOM, ReagentType.BLACK_PEARL},
				Ingredient.fromItems(NostrumCurios.ringGold),
				new Ingredient[] {Ingredient.fromTag(Tags.Items.INGOTS_GOLD), Ingredient.fromTag(NostrumTags.Items.SlabFierce), Ingredient.EMPTY, Ingredient.fromTag(Tags.Items.INGOTS_GOLD)},
				new RRequirementResearch("rings_true"),
				new OutcomeSpawnItem(new ItemStack(NostrumCurios.ringTrueGold)));
		registry.register(recipe);
		
		recipe = RitualRecipe.createTier3("ring_gold_corrupted",
				new ItemStack(NostrumCurios.ringCorruptedGold),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANDRAKE_ROOT, ReagentType.CRYSTABLOOM, ReagentType.BLACK_PEARL},
				Ingredient.fromItems(NostrumCurios.ringGold),
				new Ingredient[] {Ingredient.fromTag(Tags.Items.INGOTS_GOLD), Ingredient.fromTag(NostrumTags.Items.SlabKind), Ingredient.fromTag(NostrumTags.Items.CrystalSmall), Ingredient.fromTag(Tags.Items.INGOTS_GOLD)},
				new RRequirementResearch("rings_corrupted"),
				new OutcomeSpawnItem(new ItemStack(NostrumCurios.ringCorruptedGold)));
		registry.register(recipe);
		
		// Try to use silver, but use iron if no silver is in the modpack
		Ingredient silver = NostrumTags.Items.SilverIngot.getAllElements().isEmpty()
				? Ingredient.fromTag(Tags.Items.INGOTS_IRON)
				: Ingredient.fromTag(NostrumTags.Items.SilverIngot);
		
		recipe = RitualRecipe.createTier3("ring_silver",
				new ItemStack(NostrumCurios.ringSilver),
				null,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.MANI_DUST, ReagentType.MANI_DUST, ReagentType.MANI_DUST},
				silver,
				new Ingredient[] {silver, Ingredient.fromTag(NostrumTags.Items.CrystalSmall), silver, silver},
				new RRequirementResearch("rings"),
				new OutcomeSpawnItem(new ItemStack(NostrumCurios.ringSilver)));
		registry.register(recipe);
		
		recipe = RitualRecipe.createTier3("ring_silver_true",
				new ItemStack(NostrumCurios.ringTrueSilver),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANDRAKE_ROOT, ReagentType.CRYSTABLOOM, ReagentType.BLACK_PEARL},
				Ingredient.fromItems(NostrumCurios.ringSilver),
				new Ingredient[] {silver, Ingredient.fromTag(NostrumTags.Items.SlabKind), silver, silver},
				new RRequirementResearch("rings_true"),
				new OutcomeSpawnItem(new ItemStack(NostrumCurios.ringTrueSilver)));
		registry.register(recipe);
		
		recipe = RitualRecipe.createTier3("ring_silver_corrupted",
				new ItemStack(NostrumCurios.ringCorruptedSilver),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANDRAKE_ROOT, ReagentType.CRYSTABLOOM, ReagentType.BLACK_PEARL},
				Ingredient.fromItems(NostrumCurios.ringSilver),
				new Ingredient[] {silver, Ingredient.fromTag(NostrumTags.Items.SlabFierce), Ingredient.fromTag(NostrumTags.Items.CrystalSmall), silver},
				new RRequirementResearch("rings_corrupted"),
				new OutcomeSpawnItem(new ItemStack(NostrumCurios.ringCorruptedSilver)));
		registry.register(recipe);
		
		recipe = RitualRecipe.createTier3("float_guard",
				new ItemStack(NostrumCurios.floatGuard),
				EMagicElement.WIND,
				new ReagentType[] {ReagentType.SKY_ASH, ReagentType.GRAVE_DUST, ReagentType.MANI_DUST, ReagentType.SKY_ASH},
				Ingredient.fromTag(Tags.Items.INGOTS_GOLD),
				new Ingredient[] {Ingredient.fromTag(NostrumTags.Items.SpriteCore), Ingredient.fromTag(NostrumTags.Items.SpriteCore), Ingredient.fromTag(NostrumTags.Items.CrystalMedium), Ingredient.fromTag(NostrumTags.Items.SpriteCore)},
				new RRequirementResearch("ribbons"),
				new OutcomeSpawnItem(new ItemStack(NostrumCurios.floatGuard)));
		registry.register(recipe);
		
		ItemStack dragonwings = new ItemStack(NostrumCurios.dragonWingPendant);
		((DragonWingPendantItem) dragonwings.getItem()).setEmbeddedElement(dragonwings, EMagicElement.PHYSICAL);
		recipe = RitualRecipe.createTier3("create_dragon_wing_pendant",
				dragonwings,
				EMagicElement.PHYSICAL,
				new ReagentType[] {ReagentType.GRAVE_DUST, ReagentType.SKY_ASH, ReagentType.BLACK_PEARL, ReagentType.CRYSTABLOOM},
				Ingredient.fromTag(Tags.Items.INGOTS_GOLD),
				new Ingredient[] {Ingredient.fromTag(NostrumTags.Items.DragonWing), Ingredient.fromItems(NostrumCurios.ringGold), Ingredient.fromTag(NostrumTags.Items.CrystalMedium), Ingredient.fromTag(NostrumTags.Items.DragonWing)},
				new RRequirementResearch("dragon_wing_pendants"),
				new OutcomeSpawnItem(dragonwings));
		registry.register(recipe);
		
		//SetHasWingUpgrade
		for (EMagicElement elem : new EMagicElement[] {EMagicElement.ICE, EMagicElement.WIND, EMagicElement.LIGHTNING}) {
			MagicArmor armor = MagicArmor.get(elem, EquipmentSlotType.CHEST, MagicArmor.Type.TRUE);
			ItemStack upgradedStack = new ItemStack(armor);
			MagicArmor.SetHasWingUpgrade(upgradedStack, true);
			
			recipe = RitualRecipe.createTier3("wing_upgrade_armor_" + elem.name().toLowerCase(),
					upgradedStack,
					elem,
					new ReagentType[] {ReagentType.SKY_ASH, ReagentType.SKY_ASH, ReagentType.MANI_DUST, ReagentType.CRYSTABLOOM},
					Ingredient.fromItems(armor),
					new Ingredient[] {Ingredient.EMPTY, Ingredient.fromItems(NostrumCurios.dragonWingPendant), Ingredient.fromTag(NostrumTags.Items.CrystalMedium), Ingredient.EMPTY},
					IRitualRequirement.AND(
							new RRequirementResearch("dragon_wing_pendants"),
							new RRequirementResearch("enchanted_armor_adv")
					),
					new OutcomeModifyCenterItemGeneric((world, player, item, otherItems, centerPos, recipeIn) -> {
						if (!item.isEmpty() && item.getItem() instanceof MagicArmor) {
							MagicArmor.SetHasWingUpgrade(item, true);
						}
					}, Lists.newArrayList("Upgrades the elytra on the Corrupted Armors to dragon wings")));
			registry.register(recipe);
		}
	}
	
	private void registerCurioResearch() {
		NostrumResearch.startBuilding()
			.parent("enchanted_armor")
			.lore(NostrumItems.resourceToken)
			.reference("ritual::small_ribbon", "ritual.small_ribbon.name")
		.build("ribbons", NostrumResearchTab.OUTFITTING, Size.NORMAL, -3, 0, true, new ItemStack(NostrumCurios.smallRibbon));
		
		NostrumResearch.startBuilding()
			.parent("ribbons")
			.hiddenParent("vani")
			.reference("ritual::mana_ribbon", "ritual.mana_ribbon.name")
			.reference("ritual::jeweled_ribbon", "ritual.jeweled_ribbon.name")
			.reference("ritual::fierce_ribbon", "ritual.fierce_ribbon.name")
			.reference("ritual::kind_ribbon", "ritual.kind_ribbon.name")
		.build("ribbons_enhanced", NostrumResearchTab.OUTFITTING, Size.NORMAL, -3, 1, true, new ItemStack(NostrumCurios.largeRibbon));
		
		NostrumResearch.startBuilding()
			.parent("ribbons")
			.reference("ritual::ring_gold", "ritual.ring_gold.name")
			.reference("ritual::ring_silver", "ritual.ring_silver.name")
		.build("rings", NostrumResearchTab.OUTFITTING, Size.NORMAL, -4, 0, true, new ItemStack(NostrumCurios.ringGold));
		
		NostrumResearch.startBuilding()
			.parent("rings")
			.hiddenParent("kind_infusion")
			.hiddenParent("fierce_infusion")
			.reference("ritual::ring_gold_true", "ritual.ring_gold_true.name")
			.reference("ritual::ring_silver_true", "ritual.ring_silver_true.name")
		.build("rings_true", NostrumResearchTab.OUTFITTING, Size.NORMAL, -4, 1, true, new ItemStack(NostrumCurios.ringTrueGold));
		
		NostrumResearch.startBuilding()
			.parent("rings_true")
			.reference("ritual::ring_gold_corrupted", "ritual.ring_gold_corrupted.name")
			.reference("ritual::ring_silver_corrupted", "ritual.ring_silver_corrupted.name")
		.build("rings_corrupted", NostrumResearchTab.OUTFITTING, Size.NORMAL, -4, 2, true, new ItemStack(NostrumCurios.ringCorruptedGold));
		
		NostrumResearch.startBuilding()
			.parent("rings")
			.hiddenParent("kani")
			.reference("ritual::belt_ender", "ritual.belt_ender.name")
			.reference("ritual::belt_lightning", "ritual.belt_lightning.name")
		.build("belts", NostrumResearchTab.OUTFITTING, Size.NORMAL, -5, 0, true, new ItemStack(NostrumCurios.enderBelt));
		
		NostrumResearch.startBuilding()
			.hiddenParent("rings")
			.parent("enchanted_armor_adv")
			.reference("ritual::create_dragon_wing_pendant", "ritual.create_dragon_wing_pendant.name")
			.reference("ritual::wing_upgrade_armor_ice", "ritual.wing_upgrade_armor_ice.name")
			.reference("ritual::wing_upgrade_armor_wind", "ritual.wing_upgrade_armor_wind.name")
			.reference("ritual::wing_upgrade_armor_lightning", "ritual.wing_upgrade_armor_lightning.name")
		.build("dragon_wing_pendants", NostrumResearchTab.OUTFITTING, Size.LARGE, -1, 4, true, new ItemStack(NostrumCurios.dragonWingPendant));
	}
	
	private void registerLore() {
		NostrumCurios.registerLore();
	}
	
	public void reinitResearch() {
		registerCurioResearch();
	}
	
	public IInventory getCurios(PlayerEntity player) {
		if (!enabled) {
			return null;
		}
		
		return CurioInventoryWrapper.getCuriosInventory(player);
	}
	
	public void forEachCurio(LivingEntity entity, Predicate<ItemStack> action) {
		if (!enabled) {
			return;
		}
		
		CurioInventoryWrapper.forEach(entity, action);
	}
	
	public boolean isEnabled() {
		return this.enabled;
	}
}
