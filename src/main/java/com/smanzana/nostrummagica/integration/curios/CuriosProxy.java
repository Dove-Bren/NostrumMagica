package com.smanzana.nostrummagica.integration.curios;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.integration.curios.inventory.CurioInventoryWrapper;
import com.smanzana.nostrummagica.integration.curios.inventory.CurioSlotReference;
import com.smanzana.nostrummagica.integration.curios.items.DragonWingPendantItem;
import com.smanzana.nostrummagica.integration.curios.items.NostrumCurios;
import com.smanzana.nostrummagica.inventory.IInventorySlotKey;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.item.armor.ElementalArmor;
import com.smanzana.nostrummagica.progression.requirement.IRequirement;
import com.smanzana.nostrummagica.progression.requirement.ResearchRequirement;
import com.smanzana.nostrummagica.progression.research.NostrumResearch;
import com.smanzana.nostrummagica.progression.research.NostrumResearch.NostrumResearchTab;
import com.smanzana.nostrummagica.progression.research.NostrumResearch.Size;
import com.smanzana.nostrummagica.ritual.RitualRecipe;
import com.smanzana.nostrummagica.ritual.RitualRegistry;
import com.smanzana.nostrummagica.ritual.outcome.OutcomeModifyCenterItemGeneric;
import com.smanzana.nostrummagica.ritual.outcome.OutcomeSpawnItem;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;

public class CuriosProxy {

	@SubscribeEvent
	public void sendImc(InterModEnqueueEvent evt) {
		InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.BELT.getMessageBuilder().build());
		InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.NECKLACE.getMessageBuilder().build());
		InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.RING.getMessageBuilder().size(2).build());
		InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.CHARM.getMessageBuilder().build());
		InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.BODY.getMessageBuilder().build());
		InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.BACK.getMessageBuilder().build());
		InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> new SlotTypeMessage.Builder("pendant").priority(10).icon(
		        new ResourceLocation(CuriosApi.MODID, "item/empty_" + "curio" + "_slot")).build());
		InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> new SlotTypeMessage.Builder("spelltome").priority(10).icon(
		        new ResourceLocation(CuriosApi.MODID, "slot/empty_spelltome_slot")).build());
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
		MinecraftForge.EVENT_BUS.addListener(this::registerCurioRituals);
		registerCurioQuests();
		//registerCurioRituals();
		registerCurioResearch();
		registerLore();
	}
	
	private void registerCurioQuests() {
		
	}
	
	public void registerCurioRituals(RitualRegistry.RitualRegisterEvent event) {
		RitualRegistry registry = event.registry;
		RitualRecipe recipe;
		
		recipe = RitualRecipe.createTier3("small_ribbon",
				new ItemStack(NostrumCurios.smallRibbon),
				null,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.SPIDER_SILK, ReagentType.MANI_DUST, ReagentType.SPIDER_SILK},
				Ingredient.of(NostrumTags.Items.CrystalSmall),
				new Ingredient[] {Ingredient.of(Tags.Items.NUGGETS_GOLD), Ingredient.of(ItemTags.WOOL), Ingredient.of(ItemTags.WOOL), Ingredient.of(Tags.Items.NUGGETS_GOLD)},
				new ResearchRequirement("ribbons"),
				new OutcomeSpawnItem(new ItemStack(NostrumCurios.smallRibbon)));
		registry.register(recipe);
		
		recipe = RitualRecipe.createTier3("mana_ribbon",
				new ItemStack(NostrumCurios.mediumRibbon),
				null,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.SPIDER_SILK, ReagentType.MANI_DUST, ReagentType.SPIDER_SILK},
				Ingredient.of(NostrumCurios.smallRibbon),
				new Ingredient[] {Ingredient.of(Tags.Items.INGOTS_GOLD), Ingredient.of(NostrumTags.Items.CrystalMedium), Ingredient.EMPTY, Ingredient.of(Tags.Items.INGOTS_GOLD)},
				new ResearchRequirement("ribbons"),
				new OutcomeSpawnItem(new ItemStack(NostrumCurios.mediumRibbon)));
		registry.register(recipe);
		
		recipe = RitualRecipe.createTier3("jeweled_ribbon",
				new ItemStack(NostrumCurios.largeRibbon),
				null,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.SPIDER_SILK, ReagentType.MANI_DUST, ReagentType.SPIDER_SILK},
				Ingredient.of(NostrumCurios.mediumRibbon),
				new Ingredient[] {Ingredient.of(Tags.Items.GEMS_DIAMOND), Ingredient.of(NostrumTags.Items.CrystalLarge), Ingredient.of(Tags.Items.GEMS_EMERALD), Ingredient.of(Tags.Items.GEMS_DIAMOND)},
				new ResearchRequirement("ribbons"),
				new OutcomeSpawnItem(new ItemStack(NostrumCurios.largeRibbon)));
		registry.register(recipe);
		
		recipe = RitualRecipe.createTier3("fierce_ribbon",
				new ItemStack(NostrumCurios.fierceRibbon),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST, ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST},
				Ingredient.of(NostrumCurios.largeRibbon),
				new Ingredient[] {Ingredient.of(NostrumTags.Items.CrystalSmall), Ingredient.of(NostrumTags.Items.SlabFierce), Ingredient.of(Tags.Items.GEMS_DIAMOND), Ingredient.of(NostrumTags.Items.CrystalSmall)},
				new ResearchRequirement("ribbons_enhanced"),
				new OutcomeSpawnItem(new ItemStack(NostrumCurios.fierceRibbon)));
		registry.register(recipe);
		
		recipe = RitualRecipe.createTier3("kind_ribbon",
				new ItemStack(NostrumCurios.kindRibbon),
				null,
				new ReagentType[] {ReagentType.SKY_ASH, ReagentType.CRYSTABLOOM, ReagentType.SKY_ASH, ReagentType.CRYSTABLOOM},
				Ingredient.of(NostrumCurios.largeRibbon),
				new Ingredient[] {Ingredient.of(NostrumTags.Items.CrystalSmall), Ingredient.of(NostrumTags.Items.SlabKind), Ingredient.of(Tags.Items.GEMS_EMERALD), Ingredient.of(NostrumTags.Items.CrystalSmall)},
				new ResearchRequirement("ribbons_enhanced"),
				new OutcomeSpawnItem(new ItemStack(NostrumCurios.kindRibbon)));
		registry.register(recipe);
		
		recipe = RitualRecipe.createTier3("belt_ender",
				new ItemStack(NostrumCurios.enderBelt),
				EMagicElement.ENDER,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.GINSENG, ReagentType.SPIDER_SILK, ReagentType.SPIDER_SILK},
				Ingredient.of(NostrumTags.Items.InfusedGemEnder),
				new Ingredient[] {Ingredient.of(Tags.Items.LEATHER), Ingredient.of(Tags.Items.LEATHER), Ingredient.of(Tags.Items.LEATHER), Ingredient.of(NostrumTags.Items.CrystalMedium)},
				new ResearchRequirement("belts"),
				new OutcomeSpawnItem(new ItemStack(NostrumCurios.enderBelt)));
		registry.register(recipe);
		
		recipe = RitualRecipe.createTier3("belt_lightning",
				new ItemStack(NostrumCurios.lightningBelt),
				EMagicElement.LIGHTNING,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.BLACK_PEARL, ReagentType.SPIDER_SILK, ReagentType.SPIDER_SILK},
				Ingredient.of(NostrumTags.Items.InfusedGemLightning),
				new Ingredient[] {Ingredient.of(Tags.Items.LEATHER), Ingredient.of(Tags.Items.LEATHER), Ingredient.of(Tags.Items.LEATHER), Ingredient.of(NostrumTags.Items.CrystalMedium)},
				new ResearchRequirement("belts"),
				new OutcomeSpawnItem(new ItemStack(NostrumCurios.lightningBelt)));
		registry.register(recipe);
		
		recipe = RitualRecipe.createTier3("ring_gold",
				new ItemStack(NostrumCurios.ringGold),
				null,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.MANI_DUST, ReagentType.MANI_DUST, ReagentType.MANI_DUST},
				Ingredient.of(Tags.Items.INGOTS_GOLD),
				new Ingredient[] {Ingredient.of(Tags.Items.NUGGETS_GOLD), Ingredient.of(NostrumTags.Items.CrystalSmall), Ingredient.of(Tags.Items.NUGGETS_GOLD), Ingredient.of(Tags.Items.NUGGETS_GOLD)},
				new ResearchRequirement("rings"),
				new OutcomeSpawnItem(new ItemStack(NostrumCurios.ringGold)));
		registry.register(recipe);
		
		recipe = RitualRecipe.createTier3("ring_gold_true",
				new ItemStack(NostrumCurios.ringTrueGold),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANDRAKE_ROOT, ReagentType.CRYSTABLOOM, ReagentType.BLACK_PEARL},
				Ingredient.of(NostrumCurios.ringGold),
				new Ingredient[] {Ingredient.of(Tags.Items.INGOTS_GOLD), Ingredient.of(NostrumTags.Items.SlabFierce), Ingredient.EMPTY, Ingredient.of(Tags.Items.INGOTS_GOLD)},
				new ResearchRequirement("rings_true"),
				new OutcomeSpawnItem(new ItemStack(NostrumCurios.ringTrueGold)));
		registry.register(recipe);
		
		recipe = RitualRecipe.createTier3("ring_gold_corrupted",
				new ItemStack(NostrumCurios.ringCorruptedGold),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANDRAKE_ROOT, ReagentType.CRYSTABLOOM, ReagentType.BLACK_PEARL},
				Ingredient.of(NostrumCurios.ringGold),
				new Ingredient[] {Ingredient.of(Tags.Items.INGOTS_GOLD), Ingredient.of(NostrumTags.Items.SlabKind), Ingredient.of(NostrumTags.Items.CrystalSmall), Ingredient.of(Tags.Items.INGOTS_GOLD)},
				new ResearchRequirement("rings_corrupted"),
				new OutcomeSpawnItem(new ItemStack(NostrumCurios.ringCorruptedGold)));
		registry.register(recipe);
		
		// Try to use silver, but use iron if no silver is in the modpack
		Ingredient silver = //NostrumTags.Items.SilverIngot.getValues().isEmpty()
				/*?*/ Ingredient.of(Tags.Items.INGOTS_IRON)
				//: Ingredient.of(NostrumTags.Items.SilverIngot)
				;
		
		recipe = RitualRecipe.createTier3("ring_silver",
				new ItemStack(NostrumCurios.ringSilver),
				null,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.MANI_DUST, ReagentType.MANI_DUST, ReagentType.MANI_DUST},
				silver,
				new Ingredient[] {silver, Ingredient.of(NostrumTags.Items.CrystalSmall), silver, silver},
				new ResearchRequirement("rings"),
				new OutcomeSpawnItem(new ItemStack(NostrumCurios.ringSilver)));
		registry.register(recipe);
		
		recipe = RitualRecipe.createTier3("ring_silver_true",
				new ItemStack(NostrumCurios.ringTrueSilver),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANDRAKE_ROOT, ReagentType.CRYSTABLOOM, ReagentType.BLACK_PEARL},
				Ingredient.of(NostrumCurios.ringSilver),
				new Ingredient[] {silver, Ingredient.of(NostrumTags.Items.SlabKind), silver, silver},
				new ResearchRequirement("rings_true"),
				new OutcomeSpawnItem(new ItemStack(NostrumCurios.ringTrueSilver)));
		registry.register(recipe);
		
		recipe = RitualRecipe.createTier3("ring_silver_corrupted",
				new ItemStack(NostrumCurios.ringCorruptedSilver),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANDRAKE_ROOT, ReagentType.CRYSTABLOOM, ReagentType.BLACK_PEARL},
				Ingredient.of(NostrumCurios.ringSilver),
				new Ingredient[] {silver, Ingredient.of(NostrumTags.Items.SlabFierce), Ingredient.of(NostrumTags.Items.CrystalSmall), silver},
				new ResearchRequirement("rings_corrupted"),
				new OutcomeSpawnItem(new ItemStack(NostrumCurios.ringCorruptedSilver)));
		registry.register(recipe);
		
		recipe = RitualRecipe.createTier3("ring_mage",
				new ItemStack(NostrumCurios.ringMage),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANDRAKE_ROOT, ReagentType.CRYSTABLOOM, ReagentType.BLACK_PEARL},
				Ingredient.of(NostrumCurios.ringSilver),
				new Ingredient[] {Ingredient.of(NostrumTags.Items.CrystalSmall), Ingredient.of(NostrumTags.Items.SlabKind), Ingredient.EMPTY, silver},
				new ResearchRequirement("rings_true"),
				new OutcomeSpawnItem(new ItemStack(NostrumCurios.ringMage)));
		registry.register(recipe);
		
		recipe = RitualRecipe.createTier3("float_guard",
				new ItemStack(NostrumCurios.floatGuard),
				EMagicElement.WIND,
				new ReagentType[] {ReagentType.SKY_ASH, ReagentType.GRAVE_DUST, ReagentType.MANI_DUST, ReagentType.SKY_ASH},
				Ingredient.of(Tags.Items.INGOTS_GOLD),
				new Ingredient[] {Ingredient.of(NostrumTags.Items.SpriteCore), Ingredient.of(NostrumTags.Items.SpriteCore), Ingredient.of(NostrumTags.Items.CrystalMedium), Ingredient.of(NostrumTags.Items.SpriteCore)},
				new ResearchRequirement("ribbons"),
				new OutcomeSpawnItem(new ItemStack(NostrumCurios.floatGuard)));
		registry.register(recipe);
		
		ItemStack dragonwings = new ItemStack(NostrumCurios.dragonWingPendant);
		((DragonWingPendantItem) dragonwings.getItem()).setEmbeddedElement(dragonwings, EMagicElement.PHYSICAL);
		recipe = RitualRecipe.createTier3("create_dragon_wing_pendant",
				dragonwings,
				EMagicElement.PHYSICAL,
				new ReagentType[] {ReagentType.GRAVE_DUST, ReagentType.SKY_ASH, ReagentType.BLACK_PEARL, ReagentType.CRYSTABLOOM},
				Ingredient.of(Tags.Items.INGOTS_GOLD),
				new Ingredient[] {Ingredient.of(NostrumTags.Items.DragonWing), Ingredient.of(NostrumCurios.ringGold), Ingredient.of(NostrumTags.Items.CrystalMedium), Ingredient.of(NostrumTags.Items.DragonWing)},
				new ResearchRequirement("dragon_wing_pendants"),
				new OutcomeSpawnItem(dragonwings));
		registry.register(recipe);
		
		//SetHasWingUpgrade
		for (EMagicElement elem : new EMagicElement[] {EMagicElement.ICE, EMagicElement.WIND, EMagicElement.LIGHTNING}) {
			ElementalArmor armor = ElementalArmor.get(elem, EquipmentSlot.CHEST, ElementalArmor.Type.MASTER);
			ItemStack upgradedStack = new ItemStack(armor);
			ElementalArmor.SetHasWingUpgrade(upgradedStack, true);
			
			recipe = RitualRecipe.createTier3("wing_upgrade_armor_" + elem.name().toLowerCase(),
					upgradedStack,
					elem,
					new ReagentType[] {ReagentType.SKY_ASH, ReagentType.SKY_ASH, ReagentType.MANI_DUST, ReagentType.CRYSTABLOOM},
					Ingredient.of(armor),
					new Ingredient[] {Ingredient.EMPTY, Ingredient.of(NostrumCurios.dragonWingPendant), Ingredient.of(NostrumTags.Items.CrystalMedium), Ingredient.EMPTY},
					IRequirement.AND(
							new ResearchRequirement("dragon_wing_pendants"),
							new ResearchRequirement("enchanted_armor_adv")
					),
					new OutcomeModifyCenterItemGeneric((world, player, item, otherItems, centerPos, recipeIn) -> {
						if (!item.isEmpty() && item.getItem() instanceof ElementalArmor) {
							ElementalArmor.SetHasWingUpgrade(item, true);
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
	
	public CurioInventoryWrapper getCurios(Player player) {
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
	
	public @Nullable IInventorySlotKey<LivingEntity> getTomeSlotKey(LivingEntity entity) {
		if (!enabled) {
			return null;
		}
		
		return new CurioSlotReference("spelltome", 0);
	}
	
	public boolean isEnabled() {
		return this.enabled;
	}
}
