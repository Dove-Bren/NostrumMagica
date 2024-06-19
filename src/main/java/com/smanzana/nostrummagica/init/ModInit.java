package com.smanzana.nostrummagica.init;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.brigadier.CommandDispatcher;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.capabilities.CapabilityHandler;
import com.smanzana.nostrummagica.capabilities.IManaArmor;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.capabilities.ISpellCrafting;
import com.smanzana.nostrummagica.capabilities.ManaArmor;
import com.smanzana.nostrummagica.capabilities.ManaArmorStorage;
import com.smanzana.nostrummagica.capabilities.NostrumMagic;
import com.smanzana.nostrummagica.capabilities.NostrumMagicStorage;
import com.smanzana.nostrummagica.capabilities.SpellCraftingCapability;
import com.smanzana.nostrummagica.command.CommandAllPatterns;
import com.smanzana.nostrummagica.command.CommandAllQuests;
import com.smanzana.nostrummagica.command.CommandAllResearch;
import com.smanzana.nostrummagica.command.CommandCreateGeotoken;
import com.smanzana.nostrummagica.command.CommandDebugEffect;
import com.smanzana.nostrummagica.command.CommandEnhanceTome;
import com.smanzana.nostrummagica.command.CommandForceBind;
import com.smanzana.nostrummagica.command.CommandGiveResearchpoint;
import com.smanzana.nostrummagica.command.CommandGiveSkillpoint;
import com.smanzana.nostrummagica.command.CommandRandomSpell;
import com.smanzana.nostrummagica.command.CommandReadRoom;
import com.smanzana.nostrummagica.command.CommandReloadQuests;
import com.smanzana.nostrummagica.command.CommandReloadResearch;
import com.smanzana.nostrummagica.command.CommandReloadSkills;
import com.smanzana.nostrummagica.command.CommandSetDimension;
import com.smanzana.nostrummagica.command.CommandSetLevel;
import com.smanzana.nostrummagica.command.CommandSetManaArmor;
import com.smanzana.nostrummagica.command.CommandSetTier;
import com.smanzana.nostrummagica.command.CommandSpawnDungeon;
import com.smanzana.nostrummagica.command.CommandSpawnObelisk;
import com.smanzana.nostrummagica.command.CommandTestConfig;
import com.smanzana.nostrummagica.command.CommandUnlock;
import com.smanzana.nostrummagica.command.CommandUnlockAll;
import com.smanzana.nostrummagica.command.CommandWriteRoom;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.enchantment.ManaRecoveryEnchantment;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity.WolfTameLore;
import com.smanzana.nostrummagica.entity.KoidEntity;
import com.smanzana.nostrummagica.entity.WispEntity;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.entity.dragon.TameRedDragonEntity;
import com.smanzana.nostrummagica.integration.curios.items.NostrumCurios;
import com.smanzana.nostrummagica.item.EssenceItem;
import com.smanzana.nostrummagica.item.MagicCharm;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.item.SpellRune;
import com.smanzana.nostrummagica.item.armor.DragonArmor;
import com.smanzana.nostrummagica.item.armor.DragonArmor.DragonArmorMaterial;
import com.smanzana.nostrummagica.item.armor.DragonArmor.DragonEquipmentSlot;
import com.smanzana.nostrummagica.item.armor.MagicArmor;
import com.smanzana.nostrummagica.item.armor.MagicArmorBase;
import com.smanzana.nostrummagica.item.equipment.AspectedWeapon;
import com.smanzana.nostrummagica.item.equipment.WarlockSword;
import com.smanzana.nostrummagica.loot.NostrumLoot;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.pet.IPetWithSoul;
import com.smanzana.nostrummagica.progression.quest.NostrumQuest;
import com.smanzana.nostrummagica.progression.quest.NostrumQuest.QuestType;
import com.smanzana.nostrummagica.progression.requirement.AlterationMasteryRequirement;
import com.smanzana.nostrummagica.progression.requirement.ElementMasteryRequirement;
import com.smanzana.nostrummagica.progression.requirement.IRequirement;
import com.smanzana.nostrummagica.progression.requirement.ResearchRequirement;
import com.smanzana.nostrummagica.progression.requirement.ShapeMasteryRequirement;
import com.smanzana.nostrummagica.progression.requirement.SkillRequirement;
import com.smanzana.nostrummagica.progression.requirement.StatRequirement;
import com.smanzana.nostrummagica.progression.research.NostrumResearch;
import com.smanzana.nostrummagica.progression.research.NostrumResearch.NostrumResearchTab;
import com.smanzana.nostrummagica.progression.research.NostrumResearch.Size;
import com.smanzana.nostrummagica.progression.reward.AttributeReward;
import com.smanzana.nostrummagica.progression.reward.AttributeReward.AwardType;
import com.smanzana.nostrummagica.progression.reward.IReward;
import com.smanzana.nostrummagica.progression.skill.NostrumSkills;
import com.smanzana.nostrummagica.progression.skill.Skill;
import com.smanzana.nostrummagica.ritual.RitualRecipe;
import com.smanzana.nostrummagica.ritual.RitualRegistry;
import com.smanzana.nostrummagica.ritual.outcome.IRitualOutcome;
import com.smanzana.nostrummagica.ritual.outcome.OutcomeApplyTransformation;
import com.smanzana.nostrummagica.ritual.outcome.OutcomeBindSpell;
import com.smanzana.nostrummagica.ritual.outcome.OutcomeConstructGeotoken;
import com.smanzana.nostrummagica.ritual.outcome.OutcomeCreateObelisk;
import com.smanzana.nostrummagica.ritual.outcome.OutcomeCreatePortal;
import com.smanzana.nostrummagica.ritual.outcome.OutcomeCreateTome;
import com.smanzana.nostrummagica.ritual.outcome.OutcomeMark;
import com.smanzana.nostrummagica.ritual.outcome.OutcomePotionEffect;
import com.smanzana.nostrummagica.ritual.outcome.OutcomeRecall;
import com.smanzana.nostrummagica.ritual.outcome.OutcomeReviveSoulboundPet;
import com.smanzana.nostrummagica.ritual.outcome.OutcomeSpawnEntity;
import com.smanzana.nostrummagica.ritual.outcome.OutcomeSpawnEntity.IEntityFactory;
import com.smanzana.nostrummagica.ritual.outcome.OutcomeSpawnItem;
import com.smanzana.nostrummagica.ritual.outcome.OutcomeTeleportObelisk;
import com.smanzana.nostrummagica.serializer.ArcaneWolfElementalTypeSerializer;
import com.smanzana.nostrummagica.serializer.DragonArmorMaterialSerializer;
import com.smanzana.nostrummagica.serializer.FloatArraySerializer;
import com.smanzana.nostrummagica.serializer.HookshotTypeDataSerializer;
import com.smanzana.nostrummagica.serializer.MagicElementDataSerializer;
import com.smanzana.nostrummagica.serializer.OptionalDragonArmorMaterialSerializer;
import com.smanzana.nostrummagica.serializer.OptionalMagicElementDataSerializer;
import com.smanzana.nostrummagica.serializer.OptionalParticleDataSerializer;
import com.smanzana.nostrummagica.serializer.PetJobSerializer;
import com.smanzana.nostrummagica.serializer.PlantBossTreeTypeSerializer;
import com.smanzana.nostrummagica.serializer.RedDragonBodyPartTypeSerializer;
import com.smanzana.nostrummagica.serializer.WilloStatusSerializer;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.component.SpellComponentWrapper;
import com.smanzana.nostrummagica.spell.component.shapes.NostrumSpellShapes;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;
import com.smanzana.nostrummagica.spelltome.enhancement.SpellTomeEnhancement;
import com.smanzana.nostrummagica.stat.PlayerStat;
import com.smanzana.nostrummagica.trial.TrialEarth;
import com.smanzana.nostrummagica.trial.TrialEnder;
import com.smanzana.nostrummagica.trial.TrialFire;
import com.smanzana.nostrummagica.trial.TrialIce;
import com.smanzana.nostrummagica.trial.TrialLightning;
import com.smanzana.nostrummagica.trial.TrialPhysical;
import com.smanzana.nostrummagica.trial.TrialWind;
import com.smanzana.nostrummagica.trial.WorldTrial;
import com.smanzana.nostrummagica.util.Ingredients;
import com.smanzana.nostrummagica.world.NostrumLootHandler;
import com.smanzana.nostrummagica.world.dimension.NostrumDimensions;
import com.smanzana.nostrummagica.world.gen.NostrumFeatures;
import com.smanzana.nostrummagica.world.gen.NostrumStructures;

import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.Effects;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DataSerializerEntry;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Common (client and server) handler for MOD bus events.
 * MOD bus is not game event bus.
 * @author Skyler
 *
 */
@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModInit {

	@SubscribeEvent
	public static void commonSetup(FMLCommonSetupEvent event) {
		
		// EARLY phase:
		////////////////////////////////////////////
//		registerShapes(); Should be here, but end up driving what items are created and have to be called super early!
//    	registerTriggers();
		
    	// NOTE: These registering methods are on the regular gameplay BUS,
    	// because they depend on data and re-fire when data is reloaded?
		MinecraftForge.EVENT_BUS.addListener(ModInit::registerCommands);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, ModInit::onBiomeLoad);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, ModInit::registerDataReloaders);
		MinecraftForge.EVENT_BUS.addListener(ModInit::registerDefaultRituals);
		
		preinit();
		NostrumMagica.instance.aetheria.preInit();
		NostrumMagica.instance.curios.preInit();
		//NostrumMagica.instance.enderIO.preInit();
		NostrumMagica.instance.musica.preInit();
		
		// MID phase:
		////////////////////////////////////////////
		RitualRegistry.instance();

		SpellTomeEnhancement.initDefaultEnhancements();
		
		// Register rituals, quests, etc. after item and block init
		registerDefaultQuests();
		registerDefaultTrials();
		registerDefaultResearch();

		NostrumLoot.RegisterLootFunctions();
		new NostrumLootHandler();
		//DungeonRoomRegistry.instance().loadRegistryFromDisk(); Done in feature loading since it's required by that system and this is too late :(
		//NostrumDimensionMapper.registerDimensions();
		//NostrumDungeonStructure.initGens();

		init();
		NostrumMagica.instance.aetheria.init();
		NostrumMagica.instance.curios.init();
		//NostrumMagica.instance.enderIO.init();
		NostrumMagica.instance.musica.init();
	
		// LATE phase:
		//////////////////////////////////////////
		// Used to be two different mod init steps!
		
		postinit();
		NostrumMagica.instance.aetheria.postInit();
		//NostrumMagica.instance.curios.postInit();
		//NostrumMagica.instance.enderIO.postInit();
		NostrumMagica.instance.musica.postInit();
		
		NostrumMagica.initFinished = true;
	}
	
	private static final void preinit() {
		NetworkHandler.getInstance();
		NostrumDimensions.init();
		NostrumSkills.init();
	}
	
	private static final void init() {
    	LoreRegistry.instance();
    	TameRedDragonEntity.init();
    	
    	CapabilityManager.INSTANCE.register(INostrumMagic.class, new NostrumMagicStorage(), NostrumMagic::new);
		CapabilityManager.INSTANCE.register(IManaArmor.class, new ManaArmorStorage(), ManaArmor::new);
		CapabilityManager.INSTANCE.register(ISpellCrafting.class, SpellCraftingCapability.Serializer.INSTANCE, SpellCraftingCapability::new);
		new CapabilityHandler();
	}
	
	private static final void postinit() {
		NostrumQuest.Validate();
		NostrumResearch.Validate();
		Skill.Validate();
	}
	
	public static final void registerDefaultRituals(RitualRegistry.RitualRegisterEvent event) {
		RitualRegistry registry = event.registry;
		
		RitualRecipe recipe;

		for (EMagicElement element : EMagicElement.values()) {
			recipe = RitualRecipe
					.createTier2("rune." + element.name().toLowerCase(), SpellRune.getRune(element), null,
							new ReagentType[] { ReagentType.CRYSTABLOOM, ReagentType.MANDRAKE_ROOT,
									ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST },
							Ingredient.fromStacks(EssenceItem.getEssence(element, 1)),
							IRequirement.AND(new ElementMasteryRequirement(element),
									new ResearchRequirement("spellrunes")),
							new OutcomeSpawnItem(SpellRune.getRune(element)));
			registry.register(recipe);
		}

		// Shape Runes
		for (SpellShape shape: SpellShape.getAllShapes()) {
			if (shape == NostrumSpellShapes.Cutter) {
				recipe = RitualRecipe.createTier3("rune." + shape.getShapeKey().toLowerCase(),
						SpellRune.getRune(shape), null,
						new ReagentType[] {
								ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.GINSENG, ReagentType.SPIDER_SILK },
						Ingredient.fromTag(NostrumTags.Items.MagicToken),
						new Ingredient[] { Ingredient.fromTag(Tags.Items.NUGGETS_GOLD), Ingredient.fromStacks(shape.getCraftItem()), Ingredient.EMPTY,
								Ingredient.fromTag(Tags.Items.NUGGETS_GOLD) },
						IRequirement.AND(new SkillRequirement(NostrumSkills.Wind_Adept),
								new ResearchRequirement("spellrunes")),
						new OutcomeSpawnItem(SpellRune.getRune(shape)));
				registry.register(recipe);
				continue;
			}
			recipe = RitualRecipe.createTier3("rune." + shape.getShapeKey().toLowerCase(),
					SpellRune.getRune(shape), null,
					new ReagentType[] {
							ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.GINSENG, ReagentType.SPIDER_SILK },
					Ingredient.fromTag(NostrumTags.Items.MagicToken),
					new Ingredient[] { Ingredient.fromTag(Tags.Items.NUGGETS_GOLD), Ingredient.fromStacks(shape.getCraftItem()), Ingredient.EMPTY,
							Ingredient.fromTag(Tags.Items.NUGGETS_GOLD) },
					IRequirement.AND(new ShapeMasteryRequirement(shape),
							new ResearchRequirement("spellrunes")),
					new OutcomeSpawnItem(SpellRune.getRune(shape)));
			registry.register(recipe);
		}
		
//		recipe = RitualRecipe.createTier2("rune.single", SpellRune.getRune(SingleShape.instance()), null,
//				new ReagentType[] {
//						ReagentType.GINSENG, ReagentType.GINSENG, ReagentType.SPIDER_SILK, ReagentType.SKY_ASH },
//				Ingredient.fromTag(NostrumTags.Items.MagicToken),
//				IRitualRequirement.AND(new RRequirementShapeMastery(SingleShape.instance()),
//						new RRequirementResearch("spellrunes")),
//				new OutcomeSpawnItem(SpellRune.getRune(SingleShape.instance())));
//		registry.register(recipe);
//
//		recipe = RitualRecipe.createTier3("rune.chain", SpellRune.getRune(ChainShape.instance()), null,
//				new ReagentType[] { ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.SPIDER_SILK,
//						ReagentType.MANDRAKE_ROOT },
//				Ingredient.fromStacks(SpellRune.getRune(SingleShape.instance())),
//				new Ingredient[] { Ingredient.fromStacks(SpellRune.getRune(SingleShape.instance())), Ingredient.fromTag(Tags.Items.INGOTS_GOLD),
//						Ingredient.fromStacks(SpellRune.getRune(SingleShape.instance())), Ingredient.fromStacks(SpellRune.getRune(SingleShape.instance())) },
//				IRitualRequirement.AND(new RRequirementShapeMastery(ChainShape.instance()),
//						new RRequirementResearch("spellrunes")),
//				new OutcomeSpawnItem(SpellRune.getRune(ChainShape.instance())));
//		registry.register(recipe);
//
//		recipe = RitualRecipe.createTier3(
//				"rune.aoe", SpellRune.getRune(AoEShape.instance()), null, new ReagentType[] { ReagentType.MANI_DUST,
//						ReagentType.GRAVE_DUST, ReagentType.SPIDER_SILK, ReagentType.MANDRAKE_ROOT },
//				Ingredient.fromStacks(SpellRune.getRune(ChainShape.instance())),
//				new Ingredient[] { Ingredient.fromStacks(SpellRune.getRune(ChainShape.instance())), Ingredient.fromTag(Tags.Items.GEMS_DIAMOND),
//						Ingredient.fromStacks(SpellRune.getRune(SingleShape.instance())), Ingredient.fromStacks(SpellRune.getRune(ChainShape.instance())) },
//				IRitualRequirement.AND(new RRequirementShapeMastery(AoEShape.instance()),
//						new RRequirementResearch("spellrunes")),
//				new OutcomeSpawnItem(SpellRune.getRune(AoEShape.instance())));
//		registry.register(recipe);

		for (EAlteration alteration : EAlteration.values()) {
			recipe = RitualRecipe.createTier2("rune." + alteration.name().toLowerCase(), SpellRune.getRune(alteration),
					null,
					new ReagentType[] {
							ReagentType.GINSENG, ReagentType.GRAVE_DUST, ReagentType.SKY_ASH, ReagentType.GRAVE_DUST },
					Ingredient.fromStacks(alteration.getCraftItem()),
					IRequirement.AND(new AlterationMasteryRequirement(alteration),
							new ResearchRequirement("spellrunes")),
					new OutcomeSpawnItem(SpellRune.getRune(alteration)));
			registry.register(recipe);
		}

		// Boons
		{
			recipe = RitualRecipe.createTier1("buff.luck", new ItemStack(Items.RABBIT_FOOT), EMagicElement.PHYSICAL,
					ReagentType.SPIDER_SILK, new ResearchRequirement("boon"),
					new OutcomePotionEffect(Effects.LUCK, 0, 120 * 20));
			registry.register(recipe);

			recipe = RitualRecipe.createTier1("buff.speed", new ItemStack(Items.ARROW), EMagicElement.WIND,
					ReagentType.SKY_ASH, new ResearchRequirement("boon"),
					new OutcomePotionEffect(Effects.SPEED, 0, 120 * 20));
			registry.register(recipe);

			recipe = RitualRecipe.createTier1("buff.strength", new ItemStack(Items.IRON_SWORD), EMagicElement.FIRE,
					ReagentType.MANDRAKE_ROOT, new ResearchRequirement("boon"),
					new OutcomePotionEffect(Effects.STRENGTH, 0, 120 * 20));
			registry.register(recipe);

			recipe = RitualRecipe.createTier1("buff.leaping",
					new ItemStack(Blocks.QUARTZ_STAIRS), EMagicElement.LIGHTNING,
					ReagentType.MANI_DUST, new ResearchRequirement("boon"),
					new OutcomePotionEffect(Effects.JUMP_BOOST, 0, 120 * 20));
			registry.register(recipe);

			recipe = RitualRecipe.createTier1("buff.regen", new ItemStack(Items.GOLDEN_APPLE), EMagicElement.EARTH,
					ReagentType.GINSENG, new ResearchRequirement("boon"),
					new OutcomePotionEffect(Effects.REGENERATION, 0, 120 * 20));
			registry.register(recipe);

			recipe = RitualRecipe.createTier1("buff.fireresist", new ItemStack(Items.MAGMA_CREAM), EMagicElement.FIRE,
					ReagentType.CRYSTABLOOM, new ResearchRequirement("boon"),
					new OutcomePotionEffect(Effects.FIRE_RESISTANCE, 0, 120 * 20));
			registry.register(recipe);

			recipe = RitualRecipe.createTier1("buff.invisibility", new ItemStack(Items.ENDER_EYE), EMagicElement.ENDER,
					ReagentType.GRAVE_DUST, new ResearchRequirement("boon"),
					new OutcomePotionEffect(Effects.INVISIBILITY, 0, 120 * 20));
			registry.register(recipe);

			recipe = RitualRecipe.createTier1("buff.nightvision", new ItemStack(Items.GOLDEN_CARROT),
					EMagicElement.PHYSICAL, ReagentType.BLACK_PEARL, new ResearchRequirement("boon"),
					new OutcomePotionEffect(Effects.NIGHT_VISION, 0, 120 * 20));
			registry.register(recipe);

			recipe = RitualRecipe.createTier1("buff.waterbreathing", new ItemStack(Items.SALMON), EMagicElement.ICE,
					ReagentType.MANI_DUST, new ResearchRequirement("boon"),
					new OutcomePotionEffect(Effects.WATER_BREATHING, 0, 120 * 20));
			registry.register(recipe);
		}

		// Enchantment
//		{
//			recipe = RitualRecipe.createTier2("enchant.infinity", null,
//					new ReagentType[] {ReagentType.GRAVE_DUST, ReagentType.GRAVE_DUST, ReagentType.GRAVE_DUST, ReagentType.CRYSTABLOOM},
//					new ItemStack(Items.BOW),
//					new RRequirementQuest("enchant"),
//					new OutcomeEnchantItem(Enchantments.INFINITY, 1));
//			registry.register(recipe);
//		}

		Ingredient enderpearl = Ingredient.fromTag(Tags.Items.ENDER_PEARLS);
		recipe = RitualRecipe.createTier3("mark", new ItemStack(Items.WRITABLE_BOOK), EMagicElement.WIND,
				new ReagentType[] {
						ReagentType.GRAVE_DUST, ReagentType.MANI_DUST, ReagentType.MANI_DUST, ReagentType.CRYSTABLOOM },
				Ingredient.fromTag(NostrumTags.Items.InfusedGemEarth),
				new Ingredient[] { enderpearl, Ingredient.fromStacks(new ItemStack(Items.COMPASS)),
						Ingredient.fromStacks(new ItemStack(Items.MAP)), enderpearl },
				new ResearchRequirement("markrecall"), new OutcomeMark());
		registry.register(recipe);

		recipe = RitualRecipe.createTier1("recall", new ItemStack(Items.COMPASS), EMagicElement.LIGHTNING,
				ReagentType.SKY_ASH, new ResearchRequirement("markrecall"), new OutcomeRecall());
		registry.register(recipe);

		// medium crystal -- tier 2. Small crystal, reagents, basic crystal
		Ingredient crystal = Ingredient.fromTag(NostrumTags.Items.CrystalMedium);
		registry
				.register(RitualRecipe.createTier2("kani", new ItemStack(NostrumItems.crystalMedium), null,
						new ReagentType[] { ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST, ReagentType.GINSENG,
								ReagentType.GRAVE_DUST },
						Ingredient.fromTag(NostrumTags.Items.CrystalSmall), new ResearchRequirement("kani"),
						new OutcomeSpawnItem(new ItemStack(NostrumItems.crystalMedium))));

		// large crystal -- tier 3. Medium crystal, 4 medium crystals, reagents, basic
		// crystal

		registry
				.register(RitualRecipe.createTier3("vani", new ItemStack(NostrumItems.crystalLarge),
						null,
						new ReagentType[] { ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST, ReagentType.BLACK_PEARL,
								ReagentType.CRYSTABLOOM },
						crystal, new Ingredient[] { crystal, crystal, crystal, crystal },
						new ResearchRequirement("vani"),
						new OutcomeSpawnItem( new ItemStack(NostrumItems.crystalLarge))));

		// magic token -- tier 1. Mani dust.
		registry
				.register(RitualRecipe.createTier1("magic_token", new ItemStack(NostrumItems.resourceToken),
						null, ReagentType.MANI_DUST, new ResearchRequirement("magic_token"),
						new OutcomeSpawnItem(new ItemStack(NostrumItems.resourceToken))));

		// magic token x 3 -- tier 3. 9 reagents.
		registry
				.register(RitualRecipe
						.createTier3("magic_token_3", new ItemStack(NostrumItems.resourceToken, 3), null,
								new ReagentType[] { ReagentType.BLACK_PEARL, ReagentType.CRYSTABLOOM,
										ReagentType.GINSENG, ReagentType.GRAVE_DUST },
								Ingredient.fromTag(NostrumTags.Items.ReagentManiDust),
								new Ingredient[] { Ingredient.fromTag(NostrumTags.Items.ReagentMandrakeRoot),
										Ingredient.fromTag(NostrumTags.Items.ReagentManiDust),
										Ingredient.fromTag(NostrumTags.Items.ReagentSkyAsh),
										Ingredient.fromTag(NostrumTags.Items.ReagentSpiderSilk) },
								new ResearchRequirement("magic_token_3"),
								new OutcomeSpawnItem(new ItemStack(NostrumItems.resourceToken, 3))));

		// essence plant seed
		registry.register(RitualRecipe.createTier3(
				"essence_seed", new ItemStack(NostrumItems.reagentSeedEssence), null, new ReagentType[] { ReagentType.SPIDER_SILK,
						ReagentType.MANDRAKE_ROOT, ReagentType.GINSENG, ReagentType.MANI_DUST },
				Ingredient.fromItems(NostrumItems.crystalSmall),
				new Ingredient[] { Ingredient.fromItems(NostrumItems.reagentSeedMandrake),
						Ingredient.fromTag(NostrumTags.Items.Essence),
						Ingredient.fromTag(NostrumTags.Items.Essence),
						Ingredient.fromItems(NostrumItems.reagentSeedGinseng) },
				new ResearchRequirement("essence_seeds"), new OutcomeSpawnItem(new ItemStack(NostrumItems.reagentSeedEssence))));
		
		// fierce slab -- tier 3. Kani crystal. Fire + Wind gems
		registry
				.register(RitualRecipe.createTier3("fierce_infusion", new ItemStack(NostrumItems.resourceSlabFierce),
						EMagicElement.LIGHTNING,
						new ReagentType[] { ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST, ReagentType.MANDRAKE_ROOT,
								ReagentType.SPIDER_SILK },
						crystal,
						new Ingredient[] { Ingredient.fromTag(NostrumTags.Items.InfusedGemFire), Ingredient.EMPTY,
								Ingredient.EMPTY, Ingredient.fromTag(NostrumTags.Items.InfusedGemWind) },
						new ResearchRequirement("fierce_infusion"),
						new OutcomeSpawnItem(new ItemStack(NostrumItems.resourceSlabFierce))));

		// kind slab -- tier 3. Kani crystal. Ice + Earth gems
		registry.register(RitualRecipe.createTier3("kind_infusion",
				new ItemStack(NostrumItems.resourceSlabKind), EMagicElement.ENDER,
				new ReagentType[] {
						ReagentType.CRYSTABLOOM, ReagentType.GINSENG, ReagentType.MANI_DUST, ReagentType.SKY_ASH },
				crystal,
				new Ingredient[] { Ingredient.fromTag(NostrumTags.Items.InfusedGemIce), Ingredient.EMPTY,
						Ingredient.EMPTY, Ingredient.fromTag(NostrumTags.Items.InfusedGemEarth) },
				new ResearchRequirement("kind_infusion"),
				new OutcomeSpawnItem(new ItemStack(NostrumItems.resourceSlabKind))));

		// balanced slab -- tier 3. Vani crystal. Fierce and Kind slabs, + ender and
		// lightning gems
		registry.register(RitualRecipe.createTier3("balanced_infusion",
				new ItemStack(NostrumItems.resourceSlabBalanced), null,
				new ReagentType[] {
						ReagentType.GINSENG, ReagentType.CRYSTABLOOM, ReagentType.MANI_DUST, ReagentType.SPIDER_SILK },
				Ingredient.fromTag(NostrumTags.Items.CrystalLarge),
				new Ingredient[] { Ingredient.fromTag(NostrumTags.Items.SlabKind),
						Ingredient.fromTag(NostrumTags.Items.InfusedGemEnder),
						Ingredient.fromTag(NostrumTags.Items.InfusedGemLightning),
						Ingredient.fromTag(NostrumTags.Items.SlabFierce) },
				new ResearchRequirement("balanced_infusion"),
				new OutcomeSpawnItem(new ItemStack(NostrumItems.resourceSlabBalanced))));

		// Thano Pendant -- tier 3. gold ingot. Paliv + Cerci fragments + 2 mani
		// crystals.
		registry.register(RitualRecipe.createTier3("thano_infusion",
				new ItemStack(NostrumItems.thanoPendant), null, new ReagentType[] { ReagentType.MANI_DUST,
						ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.SPIDER_SILK },
				Ingredient.fromTag(Tags.Items.INGOTS_GOLD),
				new Ingredient[] { Ingredient.fromStacks(new ItemStack(NostrumItems.resourcePendantLeft)),
						Ingredient.fromTag(NostrumTags.Items.CrystalSmall),
						Ingredient.fromTag(NostrumTags.Items.CrystalSmall),
						Ingredient.fromStacks(new ItemStack(NostrumItems.resourcePendantRight)) },
				new ResearchRequirement("thano_pendant"),
				new OutcomeSpawnItem(new ItemStack(NostrumItems.thanoPendant))));

		// Obelisk -- tier 3. Vani crystal. Balanced slab, 2 eyes of ender, compass.
		registry.register(RitualRecipe.createTier3("create_obelisk",
				new ItemStack(NostrumBlocks.mirrorBlock), EMagicElement.ENDER,
				new ReagentType[] {
						ReagentType.BLACK_PEARL, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.GINSENG },
				Ingredient.fromTag(NostrumTags.Items.CrystalLarge),
				new Ingredient[] { Ingredient.fromTag(NostrumTags.Items.SlabBalanced),
						Ingredient.fromItems(Items.ENDER_EYE), Ingredient.fromItems(Items.ENDER_EYE), Ingredient.fromItems(Items.COMPASS) },
				new ResearchRequirement("obelisks"), new OutcomeCreateObelisk()));

		// GeoGem -- tier 3. Compass center. 2x Crystal, 2x reagent, Earth Crystal
		registry
				.register(
						RitualRecipe.createTier3("geogem", new ItemStack(NostrumItems.positionCrystal),
								EMagicElement.EARTH, new ReagentType[] { ReagentType.GRAVE_DUST,
										ReagentType.MANDRAKE_ROOT, ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST },
								Ingredient.fromItems(Items.COMPASS),
								new Ingredient[] { Ingredient.fromTag(NostrumTags.Items.CrystalMedium),
										Ingredient.fromTag(NostrumTags.Items.Reagent),
										Ingredient.fromTag(NostrumTags.Items.Reagent),
										Ingredient.fromTag(NostrumTags.Items.CrystalMedium) },
								new ResearchRequirement("geogems"),
								new OutcomeSpawnItem(new ItemStack(NostrumItems.positionCrystal, 4))));

		// GeoToken -- tier 3. Geogem center. Magic Token, earth crystal, blank scroll,
		// diamond
		registry
				.register(RitualRecipe.createTier3("geotoken", new ItemStack(NostrumItems.positionToken),
						EMagicElement.EARTH, new ReagentType[] { ReagentType.GRAVE_DUST, ReagentType.GINSENG,
								ReagentType.MANDRAKE_ROOT, ReagentType.GRAVE_DUST },
						Ingredient.fromItems(NostrumItems.positionCrystal),
						new Ingredient[] { Ingredient.fromTag(NostrumTags.Items.CrystalMedium),
								Ingredient.fromItems(NostrumItems.resourceToken),
								Ingredient.fromTag(NostrumTags.Items.InfusedGemEarth),
								Ingredient.fromItems(NostrumItems.blankScroll) },
						new ResearchRequirement("geotokens"), new OutcomeConstructGeotoken(1)));

		// GeoToken clone -- tier 3. Geotoken center. Magic Tokens and mani crystal
		registry.register(RitualRecipe.createTier3("geotoken_3", "geotoken",
				new ItemStack(NostrumItems.positionToken), null, new ReagentType[] { ReagentType.GRAVE_DUST,
						ReagentType.GINSENG, ReagentType.MANDRAKE_ROOT, ReagentType.GRAVE_DUST },
				Ingredient.fromItems(NostrumItems.positionToken),
				new Ingredient[] { Ingredient.fromTag(NostrumTags.Items.CrystalSmall),
						Ingredient.fromItems(NostrumItems.resourceToken),
						Ingredient.fromItems(NostrumItems.resourceToken),
						Ingredient.fromItems(NostrumItems.resourceToken) },
				new ResearchRequirement("geotokens"), new OutcomeConstructGeotoken(4)));

		// Mystic Anchor
		registry.register(RitualRecipe.createTier3("mystic_anchor", new ItemStack(NostrumBlocks.mysticAnchor),
						EMagicElement.ENDER, new ReagentType[] { ReagentType.BLACK_PEARL, ReagentType.MANDRAKE_ROOT,
								ReagentType.MANDRAKE_ROOT, ReagentType.GRAVE_DUST },
						Ingredient.fromTag(Tags.Items.OBSIDIAN),
						new Ingredient[] { Ingredient.fromTag(NostrumTags.Items.CrystalMedium),
								Ingredient.fromTag(NostrumTags.Items.SpriteCore),
								Ingredient.fromItems(Items.ENDER_PEARL),
								Ingredient.fromTag(NostrumTags.Items.Rose) },
						new ResearchRequirement("mystic_anchor"), new OutcomeSpawnItem(new ItemStack(NostrumBlocks.mysticAnchor))));

		// Tele to obelisk -- tier 2. Position gem, reagents
		registry
				.register(RitualRecipe.createTier2("teleport_obelisk", new ItemStack(Items.ENDER_PEARL),
						EMagicElement.ENDER,
						new ReagentType[] { ReagentType.MANI_DUST, ReagentType.MANI_DUST, ReagentType.SKY_ASH,
								ReagentType.SPIDER_SILK },
						Ingredient.fromItems(NostrumItems.positionCrystal), new ResearchRequirement("obelisks"),
						new OutcomeTeleportObelisk()));

		// Spawn Koids -- tier 3. Kani center. Magic Token, gold, gold, essence
		registry.register(RitualRecipe.createTier3("koid",
				EssenceItem.getEssence(EMagicElement.ENDER, 1), null,
				new ReagentType[] {
						ReagentType.BLACK_PEARL, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK, ReagentType.GRAVE_DUST },
				Ingredient.fromTag(NostrumTags.Items.CrystalMedium),
				new Ingredient[] { Ingredient.fromTag(Tags.Items.INGOTS_GOLD), Ingredient.fromTag(NostrumTags.Items.MagicToken),
						Ingredient.fromTag(NostrumTags.Items.Essence),
						Ingredient.fromTag(Tags.Items.INGOTS_GOLD) },
				new ResearchRequirement("summonkoids"), new OutcomeSpawnEntity(new IEntityFactory() {
					@Override
					public void spawn(World world, Vector3d pos, PlayerEntity invoker, ItemStack centerItem) {
						KoidEntity koid = new KoidEntity(NostrumEntityTypes.koid, world);
						koid.setPosition(pos.x, pos.y, pos.z);
						world.addEntity(koid);
						koid.setAttackTarget(invoker);
					}

					@Override
					public String getEntityName() {
						return "entity.nostrummagica.entity_koid";
					}
				}, 5)));

		// Mastery Orb
		registry.register(RitualRecipe.createTier3("mastery_orb",
				new ItemStack(NostrumItems.masteryOrb), null, new ReagentType[] { ReagentType.SPIDER_SILK,
						ReagentType.MANI_DUST, ReagentType.MANI_DUST, ReagentType.SPIDER_SILK },
				Ingredient.fromItems(NostrumItems.thanoPendant),
				new Ingredient[] { Ingredient.fromTag(Tags.Items.INGOTS_GOLD),
						Ingredient.fromTag(Tags.Items.ENDER_PEARLS),
						Ingredient.fromItems(Items.BLAZE_POWDER),
						Ingredient.fromTag(Tags.Items.INGOTS_GOLD) },
				new ResearchRequirement("elemental_trials"),
				new OutcomeSpawnItem(new ItemStack(NostrumItems.masteryOrb))));

		// Spell Tome Creation
		registry.register(RitualRecipe.createTier3("tome", new ItemStack(NostrumItems.spellTomeNovice), null,
				new ReagentType[] {
						ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK, ReagentType.MANI_DUST },
				Ingredient.fromTag(NostrumTags.Items.TomePlate),
				new Ingredient[] { Ingredient.fromItems(NostrumItems.spellTomePage), Ingredient.fromItems(NostrumItems.spellTomePage),
						Ingredient.fromItems(NostrumItems.spellTomePage), Ingredient.fromItems(NostrumItems.spellTomePage) },
				new ResearchRequirement("spelltomes_advanced"), new OutcomeCreateTome()));
		registry.register(RitualRecipe.createTier3("tome2", "tome", new ItemStack(NostrumItems.spellTomeNovice), null,
				new ReagentType[] {
						ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK, ReagentType.MANI_DUST },
				Ingredient.fromTag(NostrumTags.Items.TomePlate),
				new Ingredient[] { Ingredient.fromItems(NostrumItems.spellTomePage), Ingredient.fromItems(NostrumItems.spellTomePage),
						Ingredient.EMPTY, Ingredient.fromItems(NostrumItems.spellTomePage) },
				new ResearchRequirement("spelltomes_advanced"), new OutcomeCreateTome()));
		registry
				.register(RitualRecipe.createTier3("tome3", "tome", new ItemStack(NostrumItems.spellTomeNovice), null,
						new ReagentType[] { ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK,
								ReagentType.MANI_DUST },
						Ingredient.fromTag(NostrumTags.Items.TomePlate),
						new Ingredient[] { Ingredient.fromItems(NostrumItems.spellTomePage), Ingredient.EMPTY, Ingredient.EMPTY,
								Ingredient.fromItems(NostrumItems.spellTomePage) },
						new ResearchRequirement("spelltomes_advanced"), new OutcomeCreateTome()));
		registry
				.register(RitualRecipe.createTier3("tome4", "tome", new ItemStack(NostrumItems.spellTomeNovice), null,
						new ReagentType[] { ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK,
								ReagentType.MANI_DUST },
						Ingredient.fromTag(NostrumTags.Items.TomePlate),
						new Ingredient[] { Ingredient.fromItems(NostrumItems.spellTomePage), Ingredient.EMPTY, Ingredient.EMPTY,
								Ingredient.EMPTY },
						new ResearchRequirement("spelltomes_advanced"), new OutcomeCreateTome()));
		registry.register(RitualRecipe.createTier2("tome5", "tome", new ItemStack(NostrumItems.spellTomeNovice), null,
				new ReagentType[] { ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK,
						ReagentType.MANI_DUST },
				Ingredient.fromTag(NostrumTags.Items.TomePlate),
				new ResearchRequirement("spelltomes"), new OutcomeCreateTome()));

		// Spell Binding
		registry
				.register(RitualRecipe.createTier3("spell_binding", new ItemStack(NostrumItems.spellTomePage), null,
						new ReagentType[] { ReagentType.SPIDER_SILK, ReagentType.MANI_DUST, ReagentType.SKY_ASH,
								ReagentType.BLACK_PEARL },
						Ingredient.fromTag(NostrumTags.Items.SpellTome),
						new Ingredient[] { Ingredient.fromTag(Tags.Items.NUGGETS_GOLD),
								Ingredient.fromItems(NostrumItems.spellScroll),
								Ingredient.fromTag(NostrumTags.Items.MagicToken),
								Ingredient.fromTag(Tags.Items.NUGGETS_GOLD) },
						new ResearchRequirement("spellbinding"), new OutcomeBindSpell()));

		// Magic Charms
		for (EMagicElement element : EMagicElement.values()) {
			registry
					.register(RitualRecipe.createTier2("charm." + element.name().toLowerCase(),
							MagicCharm.getCharm(element, 1), null,
							new ReagentType[] { ReagentType.GRAVE_DUST, ReagentType.GRAVE_DUST, ReagentType.MANI_DUST,
									ReagentType.MANDRAKE_ROOT },
							Ingredient.fromItems(EssenceItem.getEssenceItem(element)), new ResearchRequirement("charms"),
							new OutcomeSpawnItem(MagicCharm.getCharm(element, 8))));
		}

		// Skill mirror
		registry
				.register(RitualRecipe.createTier3("form_primordial_mirror",
						new ItemStack(NostrumItems.skillMirror), null,
						new ReagentType[] { ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.SKY_ASH,
								ReagentType.SPIDER_SILK },
						Ingredient.fromTag(Tags.Items.GLASS_PANES),
						new Ingredient[] { Ingredient.fromTag(NostrumTags.Items.SkillItemOoze),
								Ingredient.fromTag(NostrumTags.Items.SkillItemPendant),
								Ingredient.fromTag(NostrumTags.Items.DragonWing),
								Ingredient.fromTag(NostrumTags.Items.SkillItemFlute)
								},
						new ResearchRequirement("stat_items"),
						new OutcomeSpawnItem(new ItemStack(NostrumItems.skillMirror))));

		// Ooze
		registry
				.register(RitualRecipe.createTier3("form_essential_ooze",
						new ItemStack(NostrumItems.resourceSkillOoze), null,
						new ReagentType[] { ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK },
						Ingredient.fromTag(Tags.Items.SLIMEBALLS),
						new Ingredient[] { Ingredient.fromTag(NostrumTags.Items.CrystalMedium),
								Ingredient.fromTag(NostrumTags.Items.CrystalLarge),
								Ingredient.fromTag(NostrumTags.Items.RosePale),
								Ingredient.fromTag(NostrumTags.Items.CrystalMedium) },
						new ResearchRequirement("stat_items"),
						new OutcomeSpawnItem(new ItemStack(NostrumItems.resourceSkillOoze))));

		// Flute
		registry
				.register(RitualRecipe
						.createTier3("form_living_flute", new ItemStack(NostrumItems.resourceSkillFlute), null,
								new ReagentType[] { ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.SKY_ASH,
										ReagentType.SPIDER_SILK },
								Ingredient.fromItems(Items.SUGAR_CANE),
								new Ingredient[] { Ingredient.fromTag(NostrumTags.Items.CrystalMedium),
										Ingredient.fromTag(NostrumTags.Items.CrystalLarge),
										Ingredient.fromTag(NostrumTags.Items.RoseBlood),
										Ingredient.fromTag(NostrumTags.Items.CrystalMedium) },
								new ResearchRequirement("stat_items"),
								new OutcomeSpawnItem(new ItemStack(NostrumItems.resourceSkillFlute))));

		// Pendant
		registry.register(RitualRecipe.createTier3("form_eldrich_pendant",
				new ItemStack(NostrumItems.resourceSkillPendant), null,
				new ReagentType[] {
						ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK },
				Ingredient.fromTag(Tags.Items.ENDER_PEARLS),
				new Ingredient[] { Ingredient.fromTag(NostrumTags.Items.CrystalMedium),
						Ingredient.fromTag(NostrumTags.Items.CrystalLarge),
						Ingredient.fromTag(NostrumTags.Items.RoseEldrich),
						Ingredient.fromTag(NostrumTags.Items.CrystalMedium) },
				new ResearchRequirement("stat_items"),
				new OutcomeSpawnItem(new ItemStack(NostrumItems.resourceSkillPendant))));

		// Ender pin
		registry
				.register(RitualRecipe.createTier3("ender_pin", new ItemStack(NostrumItems.skillEnderPin),
						EMagicElement.ENDER, new ReagentType[] { ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST,
								ReagentType.MANDRAKE_ROOT, ReagentType.SPIDER_SILK },
						Ingredient.fromTag(NostrumTags.Items.WispPebble),
						new Ingredient[] { Ingredient.fromTag(NostrumTags.Items.EnderBristle),
								Ingredient.fromTag(NostrumTags.Items.CrystalLarge),
								Ingredient.fromTag(NostrumTags.Items.EnderBristle),
										Ingredient.fromTag(NostrumTags.Items.EnderBristle) },
						new ResearchRequirement("ender_pin"),
						new OutcomeSpawnItem(new ItemStack(NostrumItems.skillEnderPin))));

		// Mirror Shield
		Ingredient extra = (NostrumMagica.instance.curios.isEnabled() ? Ingredient.fromItems(NostrumCurios.smallRibbon)
				: Ingredient.fromTag(NostrumTags.Items.CrystalSmall));

		registry.register(RitualRecipe.createTier3("mirror_shield",
				new ItemStack(NostrumItems.mirrorShield), null, new ReagentType[] { ReagentType.MANI_DUST,
						ReagentType.MANDRAKE_ROOT, ReagentType.SPIDER_SILK, ReagentType.BLACK_PEARL },
				Ingredient.fromItems(Items.SHIELD),
				new Ingredient[] { extra, Ingredient.fromTag(NostrumTags.Items.CrystalLarge),
						Ingredient.fromTag(Tags.Items.GLASS_PANES), extra },
				new ResearchRequirement("mirror_shield"),
				new OutcomeSpawnItem(new ItemStack(NostrumItems.mirrorShield))));

		extra = (NostrumMagica.instance.curios.isEnabled() ? Ingredient.fromItems(NostrumCurios.mediumRibbon)
				: Ingredient.fromTag(NostrumTags.Items.CrystalMedium));
		registry
				.register(RitualRecipe.createTier3("true_mirror_shield",
						new ItemStack(NostrumItems.mirrorShieldImproved), null, new ReagentType[] { ReagentType.MANI_DUST,
								ReagentType.BLACK_PEARL, ReagentType.BLACK_PEARL, ReagentType.MANI_DUST },
						Ingredient.fromItems(NostrumItems.mirrorShield),
						new Ingredient[] { Ingredient.fromTag(NostrumTags.Items.PendantLeft),
								Ingredient.fromTag(NostrumTags.Items.CrystalLarge), extra,
								Ingredient.fromTag(NostrumTags.Items.PendantRight) },
						new ResearchRequirement("true_mirror_shield"),
						new OutcomeSpawnItem(new ItemStack(NostrumItems.mirrorShieldImproved))));

		registry
				.register(RitualRecipe.createTier2("spawn_sorcery_portal", new ItemStack(NostrumBlocks.sorceryPortal),
						EMagicElement.ENDER,
						new ReagentType[] { ReagentType.BLACK_PEARL, ReagentType.BLACK_PEARL, ReagentType.MANDRAKE_ROOT,
								ReagentType.MANI_DUST },
						Ingredient.fromItems(NostrumItems.resourceSeekingGem), new ResearchRequirement("sorceryportal"),
						new OutcomeCreatePortal()));

		registry
				.register(RitualRecipe.createTier3("spawn_warlock_sword", new ItemStack(NostrumItems.warlockSword),
						EMagicElement.FIRE, new ReagentType[] { ReagentType.MANDRAKE_ROOT, ReagentType.SKY_ASH,
								ReagentType.SPIDER_SILK, ReagentType.MANI_DUST },
						Ingredient.fromItems(NostrumItems.magicSwordBase),
						new Ingredient[] { Ingredient.fromItems(NostrumItems.mageStaff),
								Ingredient.fromTag(NostrumTags.Items.CrystalLarge),
								Ingredient.fromTag(NostrumTags.Items.CrystalMedium),
								Ingredient.fromItems(NostrumItems.mageStaff) },
						new ResearchRequirement("warlock_sword"),
						new OutcomeSpawnItem(WarlockSword.addCapacity(new ItemStack(NostrumItems.warlockSword), 10))));

		registry
				.register(RitualRecipe.createTier3("spawn_mage_blade", new ItemStack(NostrumItems.mageBlade),
						null, new ReagentType[] { ReagentType.BLACK_PEARL, ReagentType.MANI_DUST,
								ReagentType.SPIDER_SILK, ReagentType.GRAVE_DUST },
						Ingredient.fromItems(NostrumItems.magicSwordBase),
						new Ingredient[] { Ingredient.EMPTY,
								Ingredient.fromItems(NostrumItems.mageStaff),
								Ingredient.fromTag(NostrumTags.Items.SlabFierce),
								Ingredient.EMPTY
								},
						new ResearchRequirement("mage_blade"),
						new OutcomeSpawnItem(new ItemStack(NostrumItems.mageBlade))));
		
		registry.register(RitualRecipe.createTier3("spawn_sword_fire", new ItemStack(NostrumItems.flameRod),
				EMagicElement.FIRE, new ReagentType[] { ReagentType.GRAVE_DUST, ReagentType.MANI_DUST,
						ReagentType.SKY_ASH, ReagentType.BLACK_PEARL },
				Ingredients.MatchMageBlade(EMagicElement.FIRE),
				new Ingredient[] { Ingredient.EMPTY,
						Ingredient.fromTag(Tags.Items.INGOTS_GOLD),
						Ingredient.fromTag(Tags.Items.RODS_BLAZE),
						Ingredient.EMPTY
						},
				new ResearchRequirement("sword_fire"),
				new OutcomeSpawnItem(new ItemStack(NostrumItems.flameRod))));
		
		registry.register(RitualRecipe.createTier3("spawn_sword_ender", new ItemStack(NostrumItems.enderRod),
				EMagicElement.ENDER, new ReagentType[] { ReagentType.GRAVE_DUST, ReagentType.MANI_DUST,
						ReagentType.SKY_ASH, ReagentType.BLACK_PEARL },
				Ingredients.MatchMageBlade(EMagicElement.ENDER),
				new Ingredient[] { Ingredient.EMPTY,
						Ingredient.fromTag(Tags.Items.INGOTS_GOLD),
						Ingredient.fromItems(Items.END_ROD),
						Ingredient.EMPTY
						},
				new ResearchRequirement("sword_ender"),
				new OutcomeSpawnItem(new ItemStack(NostrumItems.enderRod))));
		
		registry.register(RitualRecipe.createTier3("spawn_sword_earth", new ItemStack(NostrumItems.earthPike),
				EMagicElement.EARTH, new ReagentType[] { ReagentType.GRAVE_DUST, ReagentType.MANI_DUST,
						ReagentType.SKY_ASH, ReagentType.BLACK_PEARL },
				Ingredients.MatchMageBlade(EMagicElement.EARTH),
				new Ingredient[] { Ingredient.fromTag(Tags.Items.OBSIDIAN),
						Ingredient.fromItems(Items.DIAMOND_SHOVEL),
						Ingredient.fromItems(Items.DIAMOND_PICKAXE),
						Ingredient.EMPTY
						},
				new ResearchRequirement("sword_earth"),
				new OutcomeSpawnItem(new ItemStack(NostrumItems.earthPike))));
		
		registry.register(RitualRecipe.createTier3("spawn_sword_physical", new ItemStack(NostrumItems.deepMetalAxe),
				null, new ReagentType[] { ReagentType.GRAVE_DUST, ReagentType.MANI_DUST,
						ReagentType.SKY_ASH, ReagentType.BLACK_PEARL },
				Ingredients.MatchMageBlade(EMagicElement.PHYSICAL),
				new Ingredient[] { Ingredient.EMPTY,
						Ingredient.fromItems(Items.DIAMOND_AXE),
						Ingredient.fromItems(Items.SHULKER_SHELL),
						Ingredient.EMPTY
						},
				new ResearchRequirement("sword_physical"),
				new OutcomeSpawnItem(new ItemStack(NostrumItems.deepMetalAxe))));

		registry.register(RitualRecipe.createTier3("create_seeking_gem",
				new ItemStack(NostrumItems.resourceSeekingGem), null,
				new ReagentType[] {
						ReagentType.GRAVE_DUST, ReagentType.GINSENG, ReagentType.GRAVE_DUST, ReagentType.BLACK_PEARL },
				Ingredient.fromItems(Items.ENDER_EYE),
				new Ingredient[] { Ingredient.fromTag(Tags.Items.INGOTS_GOLD),
						Ingredient.fromTag(NostrumTags.Items.CrystalMedium),
						Ingredient.fromTag(Tags.Items.INGOTS_GOLD),
						Ingredient.EMPTY
						},
				new ResearchRequirement("seeking_gems"),
				new OutcomeSpawnItem(new ItemStack(NostrumItems.resourceSeekingGem))));

		// Rituals for base the magic armors
		for (EMagicElement elem : EMagicElement.values()) {
			if (!MagicArmor.isArmorElement(elem)) {
				continue;
			}
			
			for (MagicArmor.Type type : MagicArmor.Type.values()) {
				if (type == MagicArmor.Type.TRUE) {
					continue; // True armors below
				}
				
				for (EquipmentSlotType slot : EquipmentSlotType.values()) {
					if (slot == EquipmentSlotType.OFFHAND || slot == EquipmentSlotType.MAINHAND) {
						continue;
					}

					ItemStack outcome;
					Ingredient input;
					Ingredient gem;
					Ingredient essence;
					String name;
					String regName;
					String research;
					outcome = new ItemStack(MagicArmor.get(elem, slot, type));
					name = "spawn_enchanted_armor";
					regName = "spawn_enchanted_armor_" + elem.name().toLowerCase() + "_" + slot.name().toLowerCase() + "_" + type.name().toLowerCase();
					research = "enchanted_armor";
					if (type == MagicArmor.Type.NOVICE) {
						input = Ingredient.fromItems(MagicArmorBase.get(slot));
					} else {
						input = Ingredient.fromItems(MagicArmor.get(elem, slot, type.getPrev()));
					}
					
					essence = Ingredient.fromStacks(EssenceItem.getEssence(elem, 1)); // Would be cool to make this tag...
					if (type == MagicArmor.Type.NOVICE) {
						gem = Ingredient.fromTag(NostrumTags.Items.CrystalSmall);
					} else if (type == MagicArmor.Type.ADEPT) {
						gem = Ingredient.fromTag(NostrumTags.Items.CrystalMedium);
					} else {
						gem = Ingredient.fromTag(NostrumTags.Items.CrystalLarge);
					}

					registry.register(RitualRecipe.createTier3(regName, name, 
							outcome,
							elem == EMagicElement.PHYSICAL ? null : elem,
							new ReagentType[] { ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.MANI_DUST,
									ReagentType.MANI_DUST },
							input, new Ingredient[] { essence, gem, essence, essence },
							new ResearchRequirement(research), new OutcomeSpawnItem(outcome.copy())));
				}
			}
		}

		// True and corrupted elemental armors
		for (EMagicElement elem : EMagicElement.values()) {
			for (EquipmentSlotType slot : EquipmentSlotType.values()) {
				if (slot == EquipmentSlotType.OFFHAND || slot == EquipmentSlotType.MAINHAND) {
					continue;
				}

				final boolean isTrue = MagicArmor.isArmorElement(elem);
				final Ingredient augment = (isTrue ? Ingredient.fromTag(NostrumTags.Items.SlabKind)
						: Ingredient.fromTag(NostrumTags.Items.SlabFierce));

				final Ingredient input = Ingredient.fromItems(MagicArmor.get(isTrue ? elem : elem.getOpposite(), slot, MagicArmor.Type.MASTER));
				final String name = "spawn_enchanted_armor";
				final String regName = "spawn_enchanted_armor_" + elem.name().toLowerCase() + "_" + slot.name().toLowerCase() + "_" + MagicArmor.Type.TRUE.name().toLowerCase();
				final String research = "enchanted_armor_adv";
				final Ingredient wings = (isTrue ? Ingredient.fromTag(NostrumTags.Items.DragonWing)
						: Ingredient.fromStacks(new ItemStack(Items.ELYTRA)));
				final IRitualOutcome outcome = (isTrue
						? new OutcomeSpawnItem(new ItemStack(MagicArmor.get(elem, slot, MagicArmor.Type.TRUE)))
						: new OutcomeSpawnItem(new ItemStack(MagicArmor.get(elem, slot, MagicArmor.Type.TRUE)),
								new ItemStack(Items.ELYTRA)));

				registry
						.register(RitualRecipe.createTier3(regName, name, new ItemStack(MagicArmor.get(elem, slot, MagicArmor.Type.TRUE)),
								elem == EMagicElement.PHYSICAL ? null : elem,
								new ReagentType[] { ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.MANI_DUST,
										ReagentType.MANI_DUST },
								input,
								new Ingredient[] { Ingredient.fromTag(Tags.Items.OBSIDIAN),
										wings, augment, Ingredient.fromTag(NostrumTags.Items.CrystalLarge) },
								new ResearchRequirement(research), outcome));
			}
		}
		
		// Rituals for base the magic weapons
		for (EMagicElement elem : EMagicElement.values()) {
			if (!AspectedWeapon.isWeaponElement(elem)) {
				continue;
			}
			
			for (AspectedWeapon.Type type : AspectedWeapon.Type.values()) {
				ItemStack outcome;
				Ingredient input;
				Ingredient gem;
				Ingredient essence;
				String name;
				String regName;
				String research;
				
				outcome = new ItemStack(AspectedWeapon.get(elem, type));
				name = "spawn_enchanted_weapon";
				regName = "spawn_enchanted_weapon_" + elem.name().toLowerCase() + "_" + type.name().toLowerCase();
				research = "enchanted_weapons";
				if (type == AspectedWeapon.Type.NOVICE) {
					input = Ingredient.fromItems(NostrumItems.magicSwordBase);
				} else {
					input = Ingredient.fromItems(AspectedWeapon.get(elem, type.getPrev()));
				}
				
				essence = Ingredient.fromStacks(EssenceItem.getEssence(elem, 1)); // Would be cool to make this tag...
				if (type == AspectedWeapon.Type.NOVICE) {
					gem = Ingredient.fromTag(NostrumTags.Items.CrystalSmall);
				} else if (type == AspectedWeapon.Type.ADEPT) {
					gem = Ingredient.fromTag(NostrumTags.Items.CrystalMedium);
				} else {
					gem = Ingredient.fromTag(NostrumTags.Items.CrystalLarge);
				}

				registry.register(RitualRecipe.createTier3(regName, name, outcome,
						elem == EMagicElement.PHYSICAL ? null : elem,
						new ReagentType[] { ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.MANI_DUST,
								ReagentType.MANI_DUST },
						input, new Ingredient[] { essence, gem, essence, essence },
						new ResearchRequirement(research), new OutcomeSpawnItem(outcome.copy())));
			}
		}

		// Dragon armors
		for (DragonArmorMaterial material : DragonArmorMaterial.values()) {
			final Ingredient augment;
			final Ingredient cost;
			final @Nullable DragonArmorMaterial prevMat;
			switch (material) {
			case DIAMOND:
			default:
				augment = Ingredient.fromTag(Tags.Items.STORAGE_BLOCKS_DIAMOND);
				cost = Ingredient.fromTag(NostrumTags.Items.CrystalMedium);
				prevMat = DragonArmorMaterial.GOLD;
				break;
			case GOLD:
				augment = Ingredient.fromTag(Tags.Items.STORAGE_BLOCKS_GOLD);
				cost = Ingredient.fromTag(NostrumTags.Items.CrystalMedium);
				prevMat = DragonArmorMaterial.IRON;
				break;
			case IRON:
				augment = Ingredient.fromTag(Tags.Items.STORAGE_BLOCKS_IRON);
				cost = Ingredient.fromTag(NostrumTags.Items.CrystalMedium);
				prevMat = null;
				break;
			}

			for (DragonEquipmentSlot slot : DragonEquipmentSlot.values()) {
				// 2 rituals each. 1 is 2 horse armor (base) + cost. The other is previous tier
				// + block of material (augment) + cost

				// NOT IMPLEMENTED TODO
				{
					if (slot == DragonEquipmentSlot.CREST || slot == DragonEquipmentSlot.WINGS) {
						continue;
					}
				}
				// NOT IMPLEMENTED TODO

				final ItemStack result = new ItemStack(DragonArmor.GetArmor(slot, material));
				final Ingredient base;
				final @Nonnull Ingredient prev = (prevMat == null ? Ingredient.EMPTY
						: Ingredient.fromItems(DragonArmor.GetArmor(slot, prevMat)));

				// Craft from horse armor
				switch (slot) {
				case BODY:
				case WINGS:
				case CREST:
				default:
					if (DragonArmorMaterial.IRON == material)
						base = Ingredient.fromItems(Items.IRON_HORSE_ARMOR);
					else if (DragonArmorMaterial.GOLD == material)
						base = Ingredient.fromItems(Items.GOLDEN_HORSE_ARMOR);
					else
						/* if (DragonArmorMaterial.DIAMOND == material) */ base = Ingredient.fromItems(
								Items.DIAMOND_HORSE_ARMOR);
					break;
				case HELM:
					if (DragonArmorMaterial.IRON == material)
						base = Ingredient.fromItems(Items.IRON_HELMET);
					else if (DragonArmorMaterial.GOLD == material)
						base = Ingredient.fromItems(Items.GOLDEN_HELMET);
					else
						/* if (DragonArmorMaterial.DIAMOND == material) */ base = Ingredient.fromItems(Items.DIAMOND_HELMET);
					break;
				}

				registry
						.register(RitualRecipe.createTier3(
								"craft_dragonarmor_" + slot.getName() + "_" + material.name().toLowerCase(), result,
								EMagicElement.PHYSICAL,
								new ReagentType[] { ReagentType.MANDRAKE_ROOT, ReagentType.SKY_ASH,
										ReagentType.BLACK_PEARL, ReagentType.MANI_DUST },
								base, new Ingredient[] { Ingredient.EMPTY, base, cost, Ingredient.EMPTY },
								new ResearchRequirement("dragon_armor"), new OutcomeSpawnItem(result.copy())));

				if (prev != Ingredient.EMPTY) {
					// Upgrade ritual
					registry
							.register(RitualRecipe.createTier3(
									"upgrade_dragonarmor_" + slot.getName() + "_" + material.name().toLowerCase(),
									result, EMagicElement.PHYSICAL,
									new ReagentType[] { ReagentType.MANDRAKE_ROOT, ReagentType.SKY_ASH,
											ReagentType.BLACK_PEARL, ReagentType.MANI_DUST },
									prev, new Ingredient[] { Ingredient.EMPTY, augment, cost, Ingredient.EMPTY },
									new ResearchRequirement("dragon_armor"), new OutcomeSpawnItem(result.copy())));
				}
			}
		}

		registry.register(RitualRecipe.createTier3("improve_hookshot_medium",
				new ItemStack(NostrumItems.hookshotMedium),
				EMagicElement.PHYSICAL,
				new ReagentType[] { ReagentType.SPIDER_SILK, ReagentType.SPIDER_SILK, ReagentType.SKY_ASH,
						ReagentType.MANI_DUST },
				Ingredient.fromTag(NostrumTags.Items.HookshotWeak),
				new Ingredient[] { Ingredient.fromTag(Tags.Items.STORAGE_BLOCKS_IRON),
						Ingredient.fromTag(NostrumTags.Items.CrystalMedium),
						Ingredient.EMPTY,
						Ingredient.fromTag(Tags.Items.STORAGE_BLOCKS_IRON) },
				new ResearchRequirement("hookshot_medium"), new OutcomeSpawnItem(
						new ItemStack(NostrumItems.hookshotMedium))));

		registry.register(RitualRecipe.createTier3("improve_hookshot_strong",
				new ItemStack(NostrumItems.hookshotStrong), null,
				new ReagentType[] { ReagentType.MANI_DUST, ReagentType.BLACK_PEARL, ReagentType.SKY_ASH,
						ReagentType.SPIDER_SILK },
				Ingredient.fromTag(NostrumTags.Items.HookshotMedium),
				new Ingredient[] { Ingredient.fromTag(Tags.Items.STORAGE_BLOCKS_IRON),
						Ingredient.fromTag(NostrumTags.Items.CrystalLarge),
						Ingredient.fromTag(Tags.Items.STORAGE_BLOCKS_IRON),
						Ingredient.fromTag(Tags.Items.STORAGE_BLOCKS_IRON) },
				new ResearchRequirement("hookshot_strong"), new OutcomeSpawnItem(
						new ItemStack(NostrumItems.hookshotStrong))));

		registry.register(RitualRecipe.createTier3("improve_hookshot_claw",
				new ItemStack(NostrumItems.hookshotClaw), null,
				new ReagentType[] { ReagentType.MANI_DUST, ReagentType.BLACK_PEARL, ReagentType.SKY_ASH,
						ReagentType.SPIDER_SILK },
				Ingredient.fromTag(NostrumTags.Items.HookshotStrong),
				new Ingredient[] { Ingredient.fromTag(Tags.Items.STORAGE_BLOCKS_IRON),
						Ingredient.fromTag(NostrumTags.Items.CrystalMedium),
						Ingredient.fromTag(Tags.Items.STORAGE_BLOCKS_IRON),
						Ingredient.fromTag(Tags.Items.STORAGE_BLOCKS_IRON) },
				new ResearchRequirement("hookshot_claw"), new OutcomeSpawnItem(
						new ItemStack(NostrumItems.hookshotClaw))));

		// Reagent bag
		registry.register(RitualRecipe.createTier3("reagent_bag",
				new ItemStack(NostrumItems.reagentBag), null, new ReagentType[] { ReagentType.SPIDER_SILK,
						ReagentType.MANDRAKE_ROOT, ReagentType.GINSENG, ReagentType.SPIDER_SILK },
				Ingredient.fromTag(NostrumTags.Items.MagicToken),
				new Ingredient[] { Ingredient.fromTag(Tags.Items.LEATHER),
						Ingredient.fromTag(Tags.Items.INGOTS_GOLD),
						Ingredient.fromTag(Tags.Items.LEATHER),
						Ingredient.fromTag(Tags.Items.LEATHER),
						},
				new ResearchRequirement("reagent_bag"), new OutcomeSpawnItem(new ItemStack(NostrumItems.reagentBag))));

		// Rune bag
		registry.register(RitualRecipe.createTier3(
				"rune_bag", new ItemStack(NostrumItems.runeBag), null, new ReagentType[] { ReagentType.SPIDER_SILK,
						ReagentType.MANDRAKE_ROOT, ReagentType.GINSENG, ReagentType.SPIDER_SILK },
				Ingredient.fromItems(NostrumItems.reagentBag),
				new Ingredient[] { Ingredient.fromTag(Tags.Items.LEATHER),
						Ingredient.fromTag(Tags.Items.INGOTS_GOLD),
						Ingredient.fromTag(NostrumTags.Items.RuneAny),
						Ingredient.fromTag(Tags.Items.LEATHER)
						},
				new ResearchRequirement("rune_bag"), new OutcomeSpawnItem(new ItemStack(NostrumItems.runeBag))));

		// Mage Staff
		registry.register(RitualRecipe.createTier3("mage_staff", new ItemStack(NostrumItems.mageStaff),
				null,
				new ReagentType[] {
						ReagentType.SKY_ASH, ReagentType.BLACK_PEARL, ReagentType.GINSENG, ReagentType.MANI_DUST },
				Ingredient.fromItems(NostrumItems.crystalSmall),
				new Ingredient[] { Ingredient.fromTag(Tags.Items.LEATHER),
						Ingredient.fromTag(ItemTags.PLANKS),
						Ingredient.fromTag(ItemTags.PLANKS),
						Ingredient.fromTag(Tags.Items.LEATHER) },
				new ResearchRequirement("mage_staff"), new OutcomeSpawnItem(new ItemStack(NostrumItems.mageStaff))));

		// Thanos Staff
		registry.register(RitualRecipe.createTier3("thanos_staff",
				new ItemStack(NostrumItems.thanosStaff), null,
				new ReagentType[] {
						ReagentType.GRAVE_DUST, ReagentType.GRAVE_DUST, ReagentType.GRAVE_DUST, ReagentType.MANI_DUST },
				Ingredient.fromItems(NostrumItems.thanoPendant),
				new Ingredient[] { Ingredient.fromTag(NostrumTags.Items.CrystalSmall),
						Ingredient.fromItems(NostrumItems.mageStaff), Ingredient.fromItems(NostrumItems.mageStaff),
						Ingredient.fromTag(NostrumTags.Items.CrystalSmall) },
				new ResearchRequirement("thanos_staff"), new OutcomeSpawnItem(new ItemStack(NostrumItems.thanosStaff))));

		// Lore Table
		registry.register(RitualRecipe.createTier3("lore_table", new ItemStack(NostrumBlocks.loreTable),
				null,
				new ReagentType[] {
						ReagentType.MANI_DUST, ReagentType.GINSENG, ReagentType.CRYSTABLOOM, ReagentType.MANI_DUST },
				Ingredient.fromTag(NostrumTags.Items.CrystalSmall),
				new Ingredient[] { Ingredient.fromTag(ItemTags.PLANKS),
						Ingredient.fromItems(Items.PAPER), Ingredient.fromItems(Blocks.CRAFTING_TABLE),
						Ingredient.fromTag(ItemTags.PLANKS) },
				new ResearchRequirement("loretable"), new OutcomeSpawnItem(new ItemStack(NostrumBlocks.loreTable))));

		// Modification Table
		registry
				.register(RitualRecipe.createTier3("modification_table", new ItemStack(NostrumBlocks.modificationTable),
						null, new ReagentType[] { ReagentType.MANI_DUST, ReagentType.BLACK_PEARL,
								ReagentType.CRYSTABLOOM, ReagentType.BLACK_PEARL },
						Ingredient.fromTag(NostrumTags.Items.CrystalLarge),
						new Ingredient[] { Ingredient.fromTag(ItemTags.PLANKS),
								Ingredient.fromItems(Items.PAPER), Ingredient.fromItems(NostrumBlocks.loreTable),
								Ingredient.fromTag(ItemTags.PLANKS) },
						new ResearchRequirement("modification_table"),
						new OutcomeSpawnItem(new ItemStack(NostrumBlocks.modificationTable))));

		// Teleport Runes
		registry
				.register(RitualRecipe.createTier3("teleportrune", new ItemStack(NostrumBlocks.teleportRune),
						EMagicElement.ENDER,
						new ReagentType[] { ReagentType.BLACK_PEARL, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT,
								ReagentType.BLACK_PEARL },
						Ingredient.fromTag(ItemTags.CARPETS),
						new Ingredient[] { Ingredient.fromTag(NostrumTags.Items.CrystalSmall),
								Ingredient.fromTag(Tags.Items.ENDER_PEARLS),
								Ingredient.fromItems(NostrumItems.chalkItem),
								Ingredient.fromTag(NostrumTags.Items.CrystalSmall) },
						new ResearchRequirement("teleportrune"),
						new OutcomeSpawnItem(new ItemStack(NostrumBlocks.teleportRune, 2))));

		// Putter
		registry
				.register(RitualRecipe.createTier2("putter", new ItemStack(NostrumBlocks.putterBlock), null,
						new ReagentType[] { ReagentType.MANDRAKE_ROOT, ReagentType.SPIDER_SILK, ReagentType.BLACK_PEARL,
								ReagentType.BLACK_PEARL },
						Ingredient.fromItems(Blocks.DROPPER), new ResearchRequirement("putter"),
						new OutcomeSpawnItem(new ItemStack(NostrumBlocks.putterBlock))));

		// Active Hopper
		registry.register(RitualRecipe.createTier3("active_hopper",
				new ItemStack(NostrumBlocks.activeHopper), null, new ReagentType[] { ReagentType.MANDRAKE_ROOT,
						ReagentType.SPIDER_SILK, ReagentType.GINSENG, ReagentType.CRYSTABLOOM },
				Ingredient.fromItems(Blocks.HOPPER),
				new Ingredient[] { Ingredient.fromStacks(new ItemStack(Blocks.HOPPER)), Ingredient.fromTag(Tags.Items.STORAGE_BLOCKS_REDSTONE),
						Ingredient.fromStacks(new ItemStack(Blocks.HOPPER)), Ingredient.fromStacks(new ItemStack(Blocks.HOPPER)) },
				new ResearchRequirement("active_hopper"),
				new OutcomeSpawnItem(new ItemStack(NostrumBlocks.activeHopper, 4))));

		// Item Duct
		registry.register(RitualRecipe.createTier3(
				"item_duct", new ItemStack(NostrumBlocks.itemDuct), null, new ReagentType[] { ReagentType.SPIDER_SILK,
						ReagentType.SPIDER_SILK, ReagentType.GINSENG, ReagentType.MANDRAKE_ROOT },
				Ingredient.fromItems(Blocks.HOPPER),
				new Ingredient[] { Ingredient.fromTag(Tags.Items.INGOTS_IRON),
						Ingredient.fromTag(Tags.Items.STORAGE_BLOCKS_IRON),
						Ingredient.fromTag(Tags.Items.INGOTS_IRON),
						Ingredient.fromTag(Tags.Items.STORAGE_BLOCKS_REDSTONE),
						},
				new ResearchRequirement("item_duct"), new OutcomeSpawnItem(new ItemStack(NostrumBlocks.itemDuct, 16))));

		// Facade
		registry.register(RitualRecipe.createTier3("mimic_facade", new ItemStack(NostrumBlocks.mimicFacade),
				null,
				new ReagentType[] {
						ReagentType.SKY_ASH, ReagentType.SPIDER_SILK, ReagentType.GRAVE_DUST, ReagentType.GINSENG },
				Ingredient.fromTag(Tags.Items.GLASS),
				new Ingredient[] { Ingredient.fromTag(ItemTags.PLANKS),
						Ingredient.fromTag(NostrumTags.Items.CrystalSmall),
						Ingredient.fromItems(NostrumItems.reagentManiDust),
						Ingredient.fromItems(Items.GLOWSTONE)
						},
				new ResearchRequirement("magicfacade"), new OutcomeSpawnItem(new ItemStack(NostrumBlocks.mimicFacade, 8))));

		// Door
		registry.register(RitualRecipe.createTier3("mimic_door", new ItemStack(NostrumBlocks.mimicDoor),
				null,
				new ReagentType[] {
						ReagentType.GRAVE_DUST, ReagentType.CRYSTABLOOM, ReagentType.GRAVE_DUST, ReagentType.GINSENG },
				Ingredient.fromItems(Items.IRON_DOOR),
				new Ingredient[] { Ingredient.fromStacks(new ItemStack(NostrumBlocks.mimicFacade)),
						Ingredient.fromStacks(new ItemStack(NostrumBlocks.mimicFacade)),
						Ingredient.fromStacks(new ItemStack(NostrumBlocks.mimicFacade)),
						Ingredient.fromStacks(new ItemStack(NostrumBlocks.mimicFacade))
						},
				new ResearchRequirement("magicfacade"), new OutcomeSpawnItem(new ItemStack(NostrumBlocks.mimicDoor, 1))));

		// Dragon revive
		registry
				.register(RitualRecipe.createTier3("revive_soulbound_pet_dragon",
						new ItemStack(NostrumItems.dragonSoulItem), null, new ReagentType[] { ReagentType.GRAVE_DUST,
								ReagentType.MANDRAKE_ROOT, ReagentType.CRYSTABLOOM, ReagentType.MANI_DUST },
						Ingredient.fromItems(NostrumItems.dragonSoulItem),
						new Ingredient[] { Ingredient.EMPTY, Ingredient.fromTag(NostrumTags.Items.CrystalMedium),
								Ingredient.fromTag(Tags.Items.EGGS), Ingredient.EMPTY },
						new ResearchRequirement("soulbound_pets"), new OutcomeReviveSoulboundPet()));

		// Wolf revive
		registry
				.register(RitualRecipe.createTier3("revive_soulbound_pet_wolf",
						new ItemStack(NostrumItems.arcaneWolfSoulItem), null, new ReagentType[] { ReagentType.GRAVE_DUST,
								ReagentType.MANDRAKE_ROOT, ReagentType.CRYSTABLOOM, ReagentType.MANI_DUST },
						Ingredient.fromItems(NostrumItems.arcaneWolfSoulItem),
						new Ingredient[] { Ingredient.EMPTY, Ingredient.fromTag(NostrumTags.Items.CrystalMedium),
								Ingredient.fromTag(Tags.Items.EGGS), Ingredient.EMPTY },
						new ResearchRequirement("soulbound_pets"), new OutcomeReviveSoulboundPet()));

		// Soul dagger
		registry.register(RitualRecipe.createTier3("spawn_soul_dagger",
				new ItemStack(NostrumItems.soulDagger), EMagicElement.FIRE, new ReagentType[] { ReagentType.SKY_ASH,
						ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST, ReagentType.MANDRAKE_ROOT },
				Ingredient.fromItems(Items.END_CRYSTAL),
				new Ingredient[] { Ingredient.fromItems(AspectedWeapon.get(EMagicElement.WIND, AspectedWeapon.Type.NOVICE)),
						Ingredient.fromItems(AspectedWeapon.get(EMagicElement.LIGHTNING, AspectedWeapon.Type.NOVICE)),
						Ingredient.fromTag(NostrumTags.Items.SlabFierce),
						Ingredient.fromItems(AspectedWeapon.get(EMagicElement.ICE, AspectedWeapon.Type.NOVICE)) },
				new ResearchRequirement("soul_daggers"),
				new OutcomeSpawnItem(new ItemStack(NostrumItems.soulDagger))));

		// Mark wolf for transformation
		registry.register(RitualRecipe.createTier3(
				"transform_wolf", new ItemStack(Items.BONE), null, new ReagentType[] { ReagentType.MANI_DUST,
						ReagentType.CRYSTABLOOM, ReagentType.GRAVE_DUST, ReagentType.MANDRAKE_ROOT },
				Ingredient.fromTag(NostrumTags.Items.CrystalMedium),
				new Ingredient[] {
						Ingredient.fromTag(NostrumTags.Items.ReagentManiDust),
						Ingredient.fromTag(Tags.Items.BONES),
						Ingredient.fromTag(NostrumTags.Items.SlabKind),
						Ingredient.fromTag(NostrumTags.Items.ReagentManiDust) },
				new ResearchRequirement("wolf_transformation"), new OutcomeApplyTransformation(20 * 60, (e) -> {
					return e instanceof WolfEntity;
				})));

		// Paradox Mirror
		registry
				.register(RitualRecipe.createTier3("paradox_mirror", new ItemStack(NostrumBlocks.paradoxMirror),
						EMagicElement.ENDER, new ReagentType[] { ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST,
								ReagentType.MANDRAKE_ROOT, ReagentType.MANDRAKE_ROOT },
						Ingredient.fromTag(Tags.Items.GLASS_PANES),
						new Ingredient[] { Ingredient.fromTag(Tags.Items.INGOTS_GOLD),
								Ingredient.fromTag(NostrumTags.Items.EnderBristle),
								Ingredient.fromTag(NostrumTags.Items.CrystalMedium),
								Ingredient.fromTag(Tags.Items.GEMS_EMERALD)
								},
						new ResearchRequirement("paradox_mirrors"),
						new OutcomeSpawnItem(new ItemStack(NostrumBlocks.paradoxMirror, 2))));

		// Mana Armorer
		registry
				.register(RitualRecipe
						.createTier3("mana_armorer", new ItemStack(NostrumBlocks.manaArmorerBlock), EMagicElement.ICE,
								new ReagentType[] { ReagentType.BLACK_PEARL, ReagentType.MANI_DUST,
										ReagentType.CRYSTABLOOM, ReagentType.MANDRAKE_ROOT },
								Ingredient.fromItems(Items.END_CRYSTAL),
								new Ingredient[] { Ingredient.fromTag(NostrumTags.Items.CrystalLarge),
										Ingredient.fromItems(NostrumItems.resourceManaLeaf),
										Ingredient.fromTag(NostrumTags.Items.SlabBalanced),
										Ingredient.fromItems(NostrumItems.dragonEggFragment) },
								new ResearchRequirement("mana_armor"),
								new OutcomeSpawnItem(new ItemStack(NostrumBlocks.manaArmorerBlock, 1))));

//		registry.register(
//				RitualRecipe.createTier2("ritual.form_obelisk.name", EMagicElement.ENDER,
//					new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK},
//					center, outcome)
//				);
	}

	private static IReward wrapAttribute(AwardType type, float val) {
		return new AttributeReward(type, val);
	}

	public static void registerDefaultQuests() {
		// key, type, parentKeys, int x, int y, IReward reward, ... requirements
		// key, parentKeys, int x, int y, IReward reward, ... requirements
		// key, parentKey, int x, int y, IReward reward, ... requirements
		// key, int x, int y, IReward reward, ... requirements
		
		new NostrumQuest("start", QuestType.CHALLENGE, null, 0, 0, wrapAttribute(AwardType.MANA, 0.025f));
		
		int x = 0;
		int y = 1;
		new NostrumQuest("spells1", "start", x, y++, wrapAttribute(AwardType.MANA, 0.01f), new StatRequirement(PlayerStat.SpellsCast, 1));
		new NostrumQuest("spells2", "spells1", x, y++, wrapAttribute(AwardType.MANA, 0.015f), new StatRequirement(PlayerStat.SpellsCast, 25));
		new NostrumQuest("spells3", "spells2", x, y++, wrapAttribute(AwardType.MANA, 0.02f), new StatRequirement(PlayerStat.SpellsCast, 500));
		new NostrumQuest("spells4", "spells3", x, y++, wrapAttribute(AwardType.MANA, 0.025f), new StatRequirement(PlayerStat.SpellsCast, 2000));
		new NostrumQuest("spells5", "spells4", x, y++, wrapAttribute(AwardType.MANA, 0.03f), new StatRequirement(PlayerStat.SpellsCast, 5000));
		new NostrumQuest("spells6", "spells5", x, y++, wrapAttribute(AwardType.MANA, 0.05f), new StatRequirement(PlayerStat.SpellsCast, 10000));
		
		x = 1;
		y = 2;
		new NostrumQuest("uniqspells1", "spells2", x, y++, wrapAttribute(AwardType.COST, -0.005f), new StatRequirement(PlayerStat.UniqueSpellsCast, 5));
		new NostrumQuest("uniqspells2", "uniqspells1", x, y++, wrapAttribute(AwardType.COST, -0.005f), new StatRequirement(PlayerStat.UniqueSpellsCast, 10));
		new NostrumQuest("uniqspells3", "uniqspells2", x, y++, wrapAttribute(AwardType.COST, -0.01f), new StatRequirement(PlayerStat.UniqueSpellsCast, 20));
		new NostrumQuest("uniqspells4", "uniqspells3", x, y++, wrapAttribute(AwardType.COST, -0.01f), new StatRequirement(PlayerStat.UniqueSpellsCast, 50));
		new NostrumQuest("uniqspells5", "uniqspells4", x, y++, wrapAttribute(AwardType.COST, -0.02f), new StatRequirement(PlayerStat.UniqueSpellsCast, 100));
		
		x = -1;
		y = 2;
		new NostrumQuest("spellweight1", "spells2", x, y++, wrapAttribute(AwardType.COST, -0.005f), new StatRequirement(PlayerStat.TotalSpellWeight, 5));
		new NostrumQuest("spellweight2", "spellweight1", x, y++, wrapAttribute(AwardType.COST, -0.005f), new StatRequirement(PlayerStat.TotalSpellWeight, 25));
		new NostrumQuest("spellweight3", "spellweight2", x, y++, wrapAttribute(AwardType.COST, -0.01f), new StatRequirement(PlayerStat.TotalSpellWeight, 100));
		new NostrumQuest("spellweight4", "spellweight3", x, y++, wrapAttribute(AwardType.COST, -0.01f), new StatRequirement(PlayerStat.TotalSpellWeight, 500));
		new NostrumQuest("spellweight5", "spellweight4", x, y++, wrapAttribute(AwardType.COST, -0.02f), new StatRequirement(PlayerStat.TotalSpellWeight, 1000));
		
		x = -2;
		y = 1;
		new NostrumQuest("kills1", "spells1", x, y++, wrapAttribute(AwardType.REGEN, 0.01f), new StatRequirement(PlayerStat.KillsWithMagic, 1));
		new NostrumQuest("kills2", "kills1", x, y++, wrapAttribute(AwardType.REGEN, 0.01f), new StatRequirement(PlayerStat.KillsWithMagic, 10));
		new NostrumQuest("kills3", "kills2", x, y++, wrapAttribute(AwardType.REGEN, 0.02f), new StatRequirement(PlayerStat.KillsWithMagic, 50));
		new NostrumQuest("kills4", "kills3", x, y++, wrapAttribute(AwardType.REGEN, 0.03f), new StatRequirement(PlayerStat.KillsWithMagic, 200));
		new NostrumQuest("kills5", "kills4", x, y++, wrapAttribute(AwardType.REGEN, 0.04f), new StatRequirement(PlayerStat.KillsWithMagic, 500));
		new NostrumQuest("kills6", "kills5", x, y++, wrapAttribute(AwardType.REGEN, 0.05f), new StatRequirement(PlayerStat.KillsWithMagic, 2000));
		new NostrumQuest("kills7", "kills6", x, y++, wrapAttribute(AwardType.REGEN, 0.06f), new StatRequirement(PlayerStat.KillsWithMagic, 5000));
		
		// Specialty types
		x = -3;
		y = 0;
		new NostrumQuest("phykills1", "kills1", x, y, wrapAttribute(AwardType.MANA, 0.01f), new StatRequirement(PlayerStat.KillsWithElement(EMagicElement.PHYSICAL), 25));
		new NostrumQuest("phykills2", "phykills1", x-1, y++, wrapAttribute(AwardType.REGEN, 0.04f), new StatRequirement(PlayerStat.KillsWithElement(EMagicElement.PHYSICAL), 1000));
		new NostrumQuest("firekills1", "kills2", x, y, wrapAttribute(AwardType.MANA, 0.01f), new StatRequirement(PlayerStat.KillsWithElement(EMagicElement.FIRE), 50));
		new NostrumQuest("firekills2", "firekills1", x-1, y++, wrapAttribute(AwardType.REGEN, 0.04f), new StatRequirement(PlayerStat.KillsWithElement(EMagicElement.FIRE), 1000));
		new NostrumQuest("icekills1", "kills2", x, y, wrapAttribute(AwardType.MANA, 0.01f), new StatRequirement(PlayerStat.KillsWithElement(EMagicElement.ICE), 50));
		new NostrumQuest("icekills2", "icekills1", x-1, y++, wrapAttribute(AwardType.REGEN, 0.04f), new StatRequirement(PlayerStat.KillsWithElement(EMagicElement.ICE), 1000));
		new NostrumQuest("earthkills1", "kills3", x, y, wrapAttribute(AwardType.MANA, 0.02f), new StatRequirement(PlayerStat.KillsWithElement(EMagicElement.EARTH), 100));
		new NostrumQuest("earthkills2", "earthkills1", x-1, y++, wrapAttribute(AwardType.REGEN, 0.04f), new StatRequirement(PlayerStat.KillsWithElement(EMagicElement.EARTH), 1000));
		new NostrumQuest("windkills1", "kills3", x, y, wrapAttribute(AwardType.MANA, 0.02f), new StatRequirement(PlayerStat.KillsWithElement(EMagicElement.WIND), 100));
		new NostrumQuest("windkills2", "windkills1", x-1, y++, wrapAttribute(AwardType.REGEN, 0.04f), new StatRequirement(PlayerStat.KillsWithElement(EMagicElement.WIND), 1000));
		new NostrumQuest("enderkills1", "kills4", x, y, wrapAttribute(AwardType.MANA, 0.02f), new StatRequirement(PlayerStat.KillsWithElement(EMagicElement.ENDER), 250));
		new NostrumQuest("enderkills2", "enderkills1", x-1, y++, wrapAttribute(AwardType.REGEN, 0.04f), new StatRequirement(PlayerStat.KillsWithElement(EMagicElement.ENDER), 1000));
		new NostrumQuest("lightningkills1", "kills4", x, y, wrapAttribute(AwardType.MANA, 0.02f), new StatRequirement(PlayerStat.KillsWithElement(EMagicElement.LIGHTNING), 250));
		new NostrumQuest("lightningkills2", "lightningkills1", x-1, y++, wrapAttribute(AwardType.REGEN, 0.04f), new StatRequirement(PlayerStat.KillsWithElement(EMagicElement.LIGHTNING), 1000));
		
		x = 2;
		y = 1;
		new NostrumQuest("dmgdealt1", "spells1", x, y++, wrapAttribute(AwardType.REGEN, 0.01f), new StatRequirement(PlayerStat.MagicDamageDealtTotal, 10));
		new NostrumQuest("dmgdealt2", "dmgdealt1", x, y++, wrapAttribute(AwardType.REGEN, 0.01f), new StatRequirement(PlayerStat.MagicDamageDealtTotal, 100));
		new NostrumQuest("dmgdealt3", "dmgdealt2", x, y++, wrapAttribute(AwardType.REGEN, 0.02f), new StatRequirement(PlayerStat.MagicDamageDealtTotal, 500));
		new NostrumQuest("dmgdealt4", "dmgdealt3", x, y++, wrapAttribute(AwardType.REGEN, 0.03f), new StatRequirement(PlayerStat.MagicDamageDealtTotal, 1000));
		new NostrumQuest("dmgdealt5", "dmgdealt4", x, y++, wrapAttribute(AwardType.REGEN, 0.04f), new StatRequirement(PlayerStat.MagicDamageDealtTotal, 2500));
		new NostrumQuest("dmgdealt6", "dmgdealt5", x, y++, wrapAttribute(AwardType.REGEN, 0.05f), new StatRequirement(PlayerStat.MagicDamageDealtTotal, 7500));
		new NostrumQuest("dmgdealt7", "dmgdealt6", x, y++, wrapAttribute(AwardType.REGEN, 0.05f), new StatRequirement(PlayerStat.MagicDamageDealtTotal, 10000));
		
		// Specialty types
		x = 3;
		y = 0;
		new NostrumQuest("phydmgdealt1", "dmgdealt1", x, y, wrapAttribute(AwardType.MANA, 0.01f), new StatRequirement(PlayerStat.ElementalDamgeDealt(EMagicElement.PHYSICAL), 50));
		new NostrumQuest("phydmgdealt2", "phydmgdealt1", x+1, y++, wrapAttribute(AwardType.REGEN, 0.04f), new StatRequirement(PlayerStat.ElementalDamgeDealt(EMagicElement.PHYSICAL), 5000));
		new NostrumQuest("firedmgdealt1", "dmgdealt2", x, y, wrapAttribute(AwardType.MANA, 0.01f), new StatRequirement(PlayerStat.ElementalDamgeDealt(EMagicElement.FIRE), 200));
		new NostrumQuest("firedmgdealt2", "firedmgdealt1", x+1, y++, wrapAttribute(AwardType.REGEN, 0.04f), new StatRequirement(PlayerStat.ElementalDamgeDealt(EMagicElement.FIRE), 5000));
		new NostrumQuest("icedmgdealt1", "dmgdealt2", x, y, wrapAttribute(AwardType.MANA, 0.01f), new StatRequirement(PlayerStat.ElementalDamgeDealt(EMagicElement.ICE), 200));
		new NostrumQuest("icedmgdealt2", "icedmgdealt1", x+1, y++, wrapAttribute(AwardType.REGEN, 0.04f), new StatRequirement(PlayerStat.ElementalDamgeDealt(EMagicElement.ICE), 5000));
		new NostrumQuest("earthdmgdealt1", "dmgdealt3", x, y, wrapAttribute(AwardType.MANA, 0.02f), new StatRequirement(PlayerStat.ElementalDamgeDealt(EMagicElement.EARTH), 500));
		new NostrumQuest("earthdmgdealt2", "earthdmgdealt1", x+1, y++, wrapAttribute(AwardType.REGEN, 0.04f), new StatRequirement(PlayerStat.ElementalDamgeDealt(EMagicElement.EARTH), 5000));
		new NostrumQuest("winddmgdealt1", "dmgdealt3", x, y, wrapAttribute(AwardType.MANA, 0.02f), new StatRequirement(PlayerStat.ElementalDamgeDealt(EMagicElement.WIND), 500));
		new NostrumQuest("winddmgdealt2", "winddmgdealt1", x+1, y++, wrapAttribute(AwardType.REGEN, 0.04f), new StatRequirement(PlayerStat.ElementalDamgeDealt(EMagicElement.WIND), 5000));
		new NostrumQuest("enderdmgdealt1", "dmgdealt4", x, y, wrapAttribute(AwardType.MANA, 0.02f), new StatRequirement(PlayerStat.ElementalDamgeDealt(EMagicElement.ENDER), 1000));
		new NostrumQuest("enderdmgdealt2", "enderdmgdealt1", x+1, y++, wrapAttribute(AwardType.REGEN, 0.04f), new StatRequirement(PlayerStat.ElementalDamgeDealt(EMagicElement.ENDER), 5000));
		new NostrumQuest("lightningdmgdealt1", "dmgdealt4", x, y, wrapAttribute(AwardType.MANA, 0.02f), new StatRequirement(PlayerStat.ElementalDamgeDealt(EMagicElement.LIGHTNING), 1000));
		new NostrumQuest("lightningdmgdealt2", "lightningdmgdealt1", x+1, y++, wrapAttribute(AwardType.REGEN, 0.04f), new StatRequirement(PlayerStat.ElementalDamgeDealt(EMagicElement.LIGHTNING), 5000));
		
		x = 0;
		y = -1;
		new NostrumQuest("manaspent1", new String[] {"start", "spells1"}, x, y--, wrapAttribute(AwardType.MANA, 0.01f), new StatRequirement(PlayerStat.ManaSpentTotal, 100));
		new NostrumQuest("manaspent2", "manaspent1", x, y--, wrapAttribute(AwardType.MANA, 0.01f), new StatRequirement(PlayerStat.ManaSpentTotal, 1000));
		new NostrumQuest("manaspent3", "manaspent2", x, y--, wrapAttribute(AwardType.MANA, 0.01f), new StatRequirement(PlayerStat.ManaSpentTotal, 5000));
		new NostrumQuest("manaspent4", "manaspent3", x, y--, wrapAttribute(AwardType.MANA, 0.015f), new StatRequirement(PlayerStat.ManaSpentTotal, 20000));
		new NostrumQuest("manaspent5", "manaspent4", x, y--, wrapAttribute(AwardType.MANA, 0.015f), new StatRequirement(PlayerStat.ManaSpentTotal, 50000));
		new NostrumQuest("manaspent6", "manaspent5", x, y--, wrapAttribute(AwardType.MANA, 0.015f), new StatRequirement(PlayerStat.ManaSpentTotal, 75000));
		new NostrumQuest("manaspent7", "manaspent6", x, y--, wrapAttribute(AwardType.MANA, 0.03f), new StatRequirement(PlayerStat.ManaSpentTotal, 100000));
		
		x = -2;
		y = -1;
		new NostrumQuest("dmgrecv1", "manaspent1", x, y--, wrapAttribute(AwardType.COST, -0.005f), new StatRequirement(PlayerStat.MagicDamageReceivedTotal, 10));
		new NostrumQuest("dmgrecv2", "dmgrecv1", x, y--, wrapAttribute(AwardType.COST, -0.005f), new StatRequirement(PlayerStat.MagicDamageReceivedTotal, 50));
		new NostrumQuest("dmgrecv3", "dmgrecv2", x, y--, wrapAttribute(AwardType.COST, -0.005f), new StatRequirement(PlayerStat.MagicDamageReceivedTotal, 100));
		new NostrumQuest("dmgrecv4", "dmgrecv3", x, y--, wrapAttribute(AwardType.COST, -0.005f), new StatRequirement(PlayerStat.MagicDamageReceivedTotal, 250));
		new NostrumQuest("dmgrecv5", "dmgrecv4", x, y--, wrapAttribute(AwardType.COST, -0.005f), new StatRequirement(PlayerStat.MagicDamageReceivedTotal, 500));
		new NostrumQuest("dmgrecv6", "dmgrecv5", x, y--, wrapAttribute(AwardType.COST, -0.01f), new StatRequirement(PlayerStat.MagicDamageReceivedTotal, 1000));
		new NostrumQuest("dmgrecv7", "dmgrecv6", x, y--, wrapAttribute(AwardType.COST, -0.015f), new StatRequirement(PlayerStat.MagicDamageReceivedTotal, 2500));
		
		x = 2;
		y = -1;
		new NostrumQuest("maxdmg1", "manaspent1", x, y--, wrapAttribute(AwardType.COST, -0.005f), new StatRequirement(PlayerStat.MaxSpellDamageDealt, 4));
		new NostrumQuest("maxdmg2", "maxdmg1", x, y--, wrapAttribute(AwardType.COST, -0.005f), new StatRequirement(PlayerStat.MaxSpellDamageDealt, 8));
		new NostrumQuest("maxdmg3", "maxdmg2", x, y--, wrapAttribute(AwardType.COST, -0.005f), new StatRequirement(PlayerStat.MaxSpellDamageDealt, 20));
		new NostrumQuest("maxdmg4", "maxdmg3", x, y--, wrapAttribute(AwardType.COST, -0.005f), new StatRequirement(PlayerStat.MaxSpellDamageDealt, 35));
		new NostrumQuest("maxdmg5", "maxdmg4", x, y--, wrapAttribute(AwardType.COST, -0.005f), new StatRequirement(PlayerStat.MaxSpellDamageDealt, 50));
		new NostrumQuest("maxdmg6", "maxdmg5", x, y--, wrapAttribute(AwardType.COST, -0.01f), new StatRequirement(PlayerStat.MaxSpellDamageDealt, 100));
		new NostrumQuest("maxdmg7", "maxdmg6", x, y--, wrapAttribute(AwardType.COST, -0.015f), new StatRequirement(PlayerStat.MaxSpellDamageDealt, 300));
		
	}

	private static void registerDefaultTrials() {
		WorldTrial.setTrial(EMagicElement.FIRE, new TrialFire());
		WorldTrial.setTrial(EMagicElement.ICE, new TrialIce());
		WorldTrial.setTrial(EMagicElement.WIND, new TrialWind());
		WorldTrial.setTrial(EMagicElement.EARTH, new TrialEarth());
		WorldTrial.setTrial(EMagicElement.ENDER, new TrialEnder());
		WorldTrial.setTrial(EMagicElement.LIGHTNING, new TrialLightning());
		WorldTrial.setTrial(EMagicElement.PHYSICAL, new TrialPhysical());
	}

	public static void registerDefaultResearch() {
		// Init tabs first
		NostrumResearchTab.MAGICA = new NostrumResearchTab("magica", new ItemStack(NostrumItems.spellTomeCombat));
		NostrumResearchTab.MYSTICISM = new NostrumResearchTab("mysticism", new ItemStack(NostrumItems.crystalSmall));
		NostrumResearchTab.OUTFITTING = new NostrumResearchTab("outfitting", new ItemStack(NostrumItems.mageStaff));
		NostrumResearchTab.TINKERING = new NostrumResearchTab("tinkering", new ItemStack(NostrumBlocks.putterBlock));
		NostrumResearchTab.ADVANCED_MAGICA = new NostrumResearchTab("advanced_magica", new ItemStack(NostrumItems.thanoPendant));

		// Then register researches

		// Magica Tab
		NostrumResearch.startBuilding().build("origin", NostrumResearchTab.MAGICA, Size.LARGE, 0, 0, false,
				new ItemStack(NostrumItems.spellTomeNovice));

		NostrumResearch.startBuilding().parent("origin")
				.reference("builtin::guides::spellmaking", "info.spellmaking.name").reference(NostrumItems.GetRune(new SpellComponentWrapper(EMagicElement.FIRE)))
				.reference(NostrumItems.blankScroll).reference(NostrumItems.spellScroll).reference(NostrumItems.reagentMandrakeRoot)
				.build("spellcraft", NostrumResearchTab.MAGICA, Size.GIANT, -1, 1, false,
						new ItemStack(NostrumItems.spellScroll));

		NostrumResearch.startBuilding().parent("spellcraft").hiddenParent("rituals").lore(NostrumItems.spellPlateNovice)
				.reference("builtin::guides::tomes", "info.tomes.name").reference("ritual::tome", "ritual.tome.name")
				.reference(NostrumItems.spellTomeNovice).reference(NostrumItems.spellPlateNovice).reference(NostrumItems.spellScroll)
				.build("spelltomes", NostrumResearchTab.MAGICA, Size.NORMAL, -2, 2, false,
						new ItemStack(NostrumItems.spellPlateNovice));

		NostrumResearch.startBuilding().parent("spelltomes").lore(NostrumItems.spellTomePage)
				.reference("builtin::guides::tomes", "info.tomes.name").reference("ritual::tome", "ritual.tome.name")
				.reference(NostrumItems.spellTomePage).build("spelltomes_advanced", NostrumResearchTab.MAGICA,
						Size.NORMAL, -1, 2, true, new ItemStack(NostrumItems.spellTomePage));

		NostrumResearch.startBuilding().parent("spelltomes")
				.reference("builtin::guides::spellmaking", "info.spellbinding.name")
				.reference("ritual::spell_binding", "ritual.spell_binding.name").build("spellbinding",
						NostrumResearchTab.MAGICA, Size.NORMAL, -2, 3, false, new ItemStack(NostrumItems.spellTomeNovice));

		NostrumResearch.startBuilding().parent("spellcraft").quest("lvl7").reference(NostrumItems.masteryOrb)
				.reference("builtin::trials::fire", "info.trial.fire.name")
				.reference("builtin::trials::ice", "info.trial.ice.name")
				.reference("builtin::trials::earth", "info.trial.earth.name")
				.reference("builtin::trials::wind", "info.trial.wind.name")
				.reference("builtin::trials::ender", "info.trial.ender.name")
				.reference("builtin::trials::lightning", "info.trial.lightning.name")
				.reference("builtin::trials::physical", "info.trial.physical.name").build("elemental_trials",
						NostrumResearchTab.MAGICA, Size.NORMAL, -3, 2, true, new ItemStack(NostrumItems.masteryOrb));

		NostrumResearch.startBuilding().parent("origin").reference("builtin::guides::rituals", "info.rituals.name")
				.reference(NostrumItems.altarItem).reference(NostrumItems.chalkItem).reference(NostrumItems.reagentMandrakeRoot)
				.reference(NostrumItems.infusedGemUnattuned).build("rituals", NostrumResearchTab.MAGICA, Size.GIANT, 1, 1,
						false, new ItemStack(NostrumItems.infusedGemUnattuned));

		NostrumResearch.startBuilding().parent("rituals").build("candles", NostrumResearchTab.MAGICA, Size.NORMAL, 2, 2,
				false, new ItemStack(NostrumBlocks.candle));

		NostrumResearch.startBuilding().hiddenParent("geotokens").parent("rituals").lore(NostrumItems.positionToken)
				.spellComponent(EMagicElement.ENDER, EAlteration.GROWTH)
				.spellComponent(EMagicElement.ENDER, EAlteration.SUPPORT).reference("ritual::mark", "ritual.mark.name")
				.reference("ritual::recall", "ritual.recall.name")
				.build("markrecall", NostrumResearchTab.MAGICA, Size.LARGE, 4, 2, true, new ItemStack(Items.COMPASS));

		NostrumResearch.startBuilding().parent("markrecall").lore(NostrumItems.resourceSeekingGem).build("adv_markrecall",
				NostrumResearchTab.MAGICA, Size.LARGE, 4, 3, false, new ItemStack(NostrumItems.positionToken));

		NostrumResearch.startBuilding().parent("rituals").reference("ritual::buff.luck", "ritual.buff.luck.name")
				.reference("ritual::buff.speed", "ritual.buff.speed.name")
				.reference("ritual::buff.strength", "ritual.buff.strength.name")
				.reference("ritual::buff.leaping", "ritual.buff.leaping.name")
				.reference("ritual::buff.regen", "ritual.buff.regen.name")
				.reference("ritual::buff.fireresist", "ritual.buff.fireresist.name")
				.reference("ritual::buff.invisibility", "ritual.buff.invisibility.name")
				.reference("ritual::buff.nightvision", "ritual.buff.nightvision.name")
				.reference("ritual::buff.waterbreathing", "ritual.buff.waterbreathing.name")
				.build("boon", NostrumResearchTab.MAGICA, Size.LARGE, 3, 0, true, new ItemStack(Items.SPLASH_POTION));

		NostrumResearch.startBuilding().parent("rituals").hiddenParent("magic_token").lore(KoidEntity.KoidLore.instance())
				.reference("ritual::koid", "ritual.koid.name").build("summonkoids", NostrumResearchTab.MAGICA,
						Size.NORMAL, 3, 2, true, new ItemStack(NostrumItems.essencePhysical));

		NostrumResearch.startBuilding().hiddenParent("spellcraft").parent("rituals")
				.reference("ritual::lore_table", "ritual.lore_table.name").build("loretable", NostrumResearchTab.MAGICA,
						Size.NORMAL, 2, 0, true, new ItemStack(NostrumBlocks.loreTable));

		// Mysticism Tab (Resources)
		NostrumResearch.startBuilding().hiddenParent("rituals").lore(NostrumItems.reagentMandrakeRoot)
				.reference("ritual::magic_token", "ritual.magic_token.name").build("magic_token",
						NostrumResearchTab.MYSTICISM, Size.NORMAL, -1, 0, true,
						new ItemStack(NostrumItems.resourceToken));

		NostrumResearch.startBuilding().parent("magic_token").quest("lvl3")
				.reference("ritual::magic_token_3", "ritual.magic_token_3.name").build("magic_token_3",
						NostrumResearchTab.MYSTICISM, Size.NORMAL, -2, 0, true,
						new ItemStack(NostrumItems.resourceToken));

		NostrumResearch.startBuilding().hiddenParent("magic_token").lore(NostrumItems.essencePhysical)
				.lore(KoidEntity.KoidLore.instance()).lore(WispEntity.WispLoreTag.instance())
				.reference("ritual::essence_seed", "ritual.essence_seed.name").build("essence_seeds",
						NostrumResearchTab.MYSTICISM, Size.NORMAL, -3, 0, false, new ItemStack(NostrumItems.reagentSeedEssence));

		NostrumResearch.startBuilding().parent("magic_token").lore(NostrumItems.resourceToken).quest("lvl3")
				.reference("ritual::kani", "ritual.kani.name").build("kani", NostrumResearchTab.MYSTICISM, Size.NORMAL,
						-1, 1, true, new ItemStack(NostrumItems.crystalMedium));

		NostrumResearch.startBuilding().parent("kani").quest("lvl7").lore(NostrumItems.resourceToken)
				.reference("ritual::vani", "ritual.vani.name").build("vani", NostrumResearchTab.MYSTICISM, Size.LARGE,
						-1, 2, true, new ItemStack(NostrumItems.crystalLarge));

		NostrumResearch.startBuilding().parent("kani")
				.reference("ritual::create_seeking_gem", "ritual.create_seeking_gem.name").build("seeking_gems",
						NostrumResearchTab.MYSTICISM, Size.NORMAL, 0, 1, true,
						new ItemStack(NostrumItems.resourceSeekingGem));

		NostrumResearch.startBuilding().hiddenParent("magic_token").hiddenParent("spellcraft")
				.lore(NostrumItems.GetRune(new SpellComponentWrapper(EMagicElement.FIRE))).reference("ritual::rune.physical", "ritual.rune.physical.name")
				.reference("ritual::rune.single", "ritual.rune.single.name")
				.reference("ritual::rune.inflict", "ritual.rune.inflict.name")
				.reference("ritual::rune.touch", "ritual.rune.touch.name")
				.reference("ritual::rune.self", "ritual.rune.self.name").build("spellrunes",
						NostrumResearchTab.MYSTICISM, Size.GIANT, 0, 0, true,
						SpellRune.getRune(EMagicElement.FIRE));

		NostrumResearch.startBuilding().hiddenParent("kani")
				.reference("ritual::fierce_infusion", "ritual.fierce_infusion.name").build("fierce_infusion",
						NostrumResearchTab.MYSTICISM, Size.NORMAL, 1, 0, true,
						new ItemStack(NostrumItems.resourceSlabFierce));

		NostrumResearch.startBuilding().hiddenParent("kani")
				.reference("ritual::kind_infusion", "ritual.kind_infusion.name").build("kind_infusion",
						NostrumResearchTab.MYSTICISM, Size.NORMAL, 3, 0, true,
						new ItemStack(NostrumItems.resourceSlabKind));

		NostrumResearch.startBuilding().hiddenParent("vani").parent("fierce_infusion").parent("kind_infusion")
				.reference("ritual::balanced_infusion", "ritual.balanced_infusion.name").build("balanced_infusion",
						NostrumResearchTab.MYSTICISM, Size.LARGE, 2, 1, true,
						new ItemStack(NostrumItems.resourceSlabBalanced));

		// Outfitting (weapon/armor)
		NostrumResearch.startBuilding().hiddenParent("rituals")
				.reference("ritual::mage_staff", "ritual.mage_staff.name").lore(NostrumItems.resourceToken)
				.build("mage_staff", NostrumResearchTab.OUTFITTING, Size.NORMAL, 1, 0, true,
						new ItemStack(NostrumItems.mageStaff));

		NostrumResearch.startBuilding().parent("mage_staff").hiddenParent("thano_pendant")
				.reference("ritual::thanos_staff", "ritual.thanos_staff.name").lore(NostrumItems.thanoPendant)
				.build("thanos_staff", NostrumResearchTab.OUTFITTING, Size.LARGE, 2, 0, true,
						new ItemStack(NostrumItems.thanosStaff));

		NostrumResearch.startBuilding().parent("mage_staff").parent("mage_blade").hiddenParent("vani")
				.reference("ritual::spawn_warlock_sword", "ritual.spawn_warlock_sword.name").build("warlock_sword",
						NostrumResearchTab.OUTFITTING, Size.LARGE, 1, 1, true, new ItemStack(NostrumItems.warlockSword));

		NostrumResearch.startBuilding().parent("thanos_staff").parent("warlock_sword").hiddenParent("vani")
				.reference("ritual::spawn_soul_dagger", "ritual.spawn_soul_dagger.name").build("soul_daggers",
						NostrumResearchTab.OUTFITTING, Size.LARGE, 2, 1, true, new ItemStack(NostrumItems.soulDagger));
		
		NostrumResearch.startBuilding().parent("enchanted_weapons").hiddenParent("mage_staff")
				.spellComponent(null, EAlteration.ENCHANT)
				.reference("ritual::spawn_mage_blade", "ritual.spawn_mage_blade.name").build("mage_blade",
				NostrumResearchTab.OUTFITTING, Size.LARGE, 0, 1, true, new ItemStack(NostrumItems.mageBlade));

		NostrumResearch.startBuilding().hiddenParent("mage_blade").hiddenParent("enchanted_armor_adv")
				.reference("ritual::spawn_sword_fire", "ritual.spawn_sword_fire.name").build("sword_fire",
				NostrumResearchTab.OUTFITTING, Size.NORMAL, 1, 3, true, new ItemStack(NostrumItems.flameRod));
		
		NostrumResearch.startBuilding().hiddenParent("mage_blade").hiddenParent("enchanted_armor_adv")
			.reference("ritual::spawn_sword_earth", "ritual.spawn_sword_earth.name").build("sword_earth",
			NostrumResearchTab.OUTFITTING, Size.NORMAL, 2, 3, true, new ItemStack(NostrumItems.earthPike));
		
		NostrumResearch.startBuilding().hiddenParent("mage_blade").hiddenParent("enchanted_armor_adv")
			.reference("ritual::spawn_sword_ender", "ritual.spawn_sword_ender.name").build("sword_ender",
			NostrumResearchTab.OUTFITTING, Size.NORMAL, 1, 4, true, new ItemStack(NostrumItems.enderRod));
		
		NostrumResearch.startBuilding().hiddenParent("mage_blade").hiddenParent("enchanted_armor_adv")
			.reference("ritual::spawn_sword_physical", "ritual.spawn_sword_physical.name").build("sword_physical",
			NostrumResearchTab.OUTFITTING, Size.NORMAL, 2, 4, true, new ItemStack(NostrumItems.deepMetalAxe));

		NostrumResearch.startBuilding().parent("enchanted_armor")
				.reference("ritual::spawn_enchanted_weapon", "ritual.spawn_enchanted_weapon.name")
				.build("enchanted_weapons", NostrumResearchTab.OUTFITTING, Size.LARGE, -1, 1, true,
						new ItemStack(AspectedWeapon.get(EMagicElement.WIND, AspectedWeapon.Type.MASTER)));

		NostrumResearch.startBuilding().hiddenParent("rituals").quest("lvl4")
				.reference("ritual::spawn_enchanted_armor", "ritual.spawn_enchanted_armor.name")
				.build("enchanted_armor", NostrumResearchTab.OUTFITTING, Size.GIANT, -2, 0, true,
						new ItemStack(MagicArmor.get(EMagicElement.FIRE, EquipmentSlotType.CHEST, MagicArmor.Type.MASTER)));

		NostrumResearch.startBuilding().parent("enchanted_armor").hiddenParent("kind_infusion")
				.hiddenParent("fierce_infusion")
				.reference("ritual::spawn_enchanted_armor", "ritual.spawn_enchanted_armor.name")
				.build("enchanted_armor_adv", NostrumResearchTab.OUTFITTING, Size.LARGE, -1, 3, true,
						new ItemStack(MagicArmor.get(EMagicElement.ENDER, EquipmentSlotType.CHEST, MagicArmor.Type.MASTER)));

		NostrumResearch.startBuilding().parent("enchanted_armor").lore(TameRedDragonEntity.TameRedDragonLore.instance())
				.reference("ritual::craft_dragonarmor_body_iron", "ritual.craft_dragonarmor_body_iron.name")
				.build("dragon_armor", NostrumResearchTab.OUTFITTING, Size.LARGE, -2, 4, true,
						new ItemStack(DragonArmor.GetArmor(DragonEquipmentSlot.HELM, DragonArmorMaterial.IRON)));

		NostrumResearch.startBuilding().parent("enchanted_armor").parent(NostrumMagica.instance.curios.isEnabled() ? "belts" : "origin")
				.reference("ritual::mirror_shield", "ritual.mirror_shield.name").build("mirror_shield",
						NostrumResearchTab.OUTFITTING, Size.LARGE, -3, 3, true, new ItemStack(NostrumItems.mirrorShield));

		NostrumResearch.startBuilding().parent("mirror_shield").lore(NostrumItems.mirrorShield)
				.reference("ritual::true_mirror_shield", "ritual.true_mirror_shield.name").build("true_mirror_shield",
						NostrumResearchTab.OUTFITTING, Size.NORMAL, -3, 4, false,
						new ItemStack(NostrumItems.mirrorShieldImproved));

		NostrumResearch.startBuilding().hiddenParent("rituals").lore(NostrumItems.reagentMandrakeRoot)
				.reference("ritual::reagent_bag", "ritual.reagent_bag.name").build("reagent_bag",
						NostrumResearchTab.OUTFITTING, Size.NORMAL, -2, -1, true, new ItemStack(NostrumItems.reagentBag));

		NostrumResearch.startBuilding().parent("reagent_bag").lore(NostrumItems.GetRune(new SpellComponentWrapper(EMagicElement.FIRE)))
				.reference("ritual::rune_bag", "ritual.rune_bag.name").build("rune_bag", NostrumResearchTab.OUTFITTING,
						Size.NORMAL, -3, -1, true, new ItemStack(NostrumItems.runeBag));

		NostrumResearch.startBuilding().hiddenParent("rituals").hiddenParent("kani").lore(NostrumItems.hookshotWeak)
				.reference("ritual::improve_hookshot_medium", "ritual.improve_hookshot_medium.name")
				.build("hookshot_medium", NostrumResearchTab.OUTFITTING, Size.NORMAL, 1, -1, true,
						new ItemStack(NostrumItems.hookshotMedium));

		NostrumResearch.startBuilding().parent("hookshot_medium").hiddenParent("vani")
				.reference("ritual::improve_hookshot_strong", "ritual.improve_hookshot_strong.name")
				.build("hookshot_strong", NostrumResearchTab.OUTFITTING, Size.NORMAL, 2, -1, false,
						new ItemStack(NostrumItems.hookshotStrong));

		NostrumResearch.startBuilding().parent("hookshot_strong")
				.reference("ritual::improve_hookshot_claw", "ritual.improve_hookshot_claw.name").build("hookshot_claw",
						NostrumResearchTab.OUTFITTING, Size.NORMAL, 3, -1, false,
						new ItemStack(NostrumItems.hookshotClaw));

		NostrumResearch.startBuilding().hiddenParent("magic_token").lore(NostrumItems.essencePhysical)
				.reference("ritual::charm.physical", "ritual.charm.physical.name")
				.reference("ritual::charm.fire", "ritual.charm.fire.name")
				.reference("ritual::charm.ice", "ritual.charm.ice.name")
				.reference("ritual::charm.earth", "ritual.charm.earth.name")
				.reference("ritual::charm.wind", "ritual.charm.wind.name")
				.reference("ritual::charm.lightning", "ritual.charm.lightning.name")
				.reference("ritual::charm.ender", "ritual.charm.ender.name").build("charms",
						NostrumResearchTab.OUTFITTING, Size.NORMAL, 0, 0, true,
						MagicCharm.getCharm(EMagicElement.ENDER, 1));

		// Tinkering
		NostrumResearch.startBuilding().parent("rituals").quest("lvl4").reference(NostrumItems.positionCrystal).build(
				"geogems", NostrumResearchTab.TINKERING, Size.LARGE, 1, -1, false,
				new ItemStack(NostrumItems.positionCrystal));

		NostrumResearch.startBuilding().parent("geogems").lore(NostrumItems.positionCrystal)
				.reference(NostrumItems.positionToken).build("geotokens", NostrumResearchTab.TINKERING, Size.LARGE, 1, 1,
						true, new ItemStack(NostrumItems.positionToken));

		NostrumResearch.startBuilding().parent("geotokens").hiddenParent("markrecall").hiddenParent("balanced_infusion")
				// .lore(NostrumItems.positionToken)
				.quest("lvl10").reference("builtin::guides::obelisks", "info.obelisks.name")
				.reference("ritual::create_obelisk", "ritual.create_obelisk.name").build("obelisks",
						NostrumResearchTab.TINKERING, Size.GIANT, 2, 2, true,
						new ItemStack(NostrumBlocks.dungeonBlock));

		NostrumResearch.startBuilding().hiddenParent("markrecall").parent("obelisks")
				.reference("ritual::spawn_sorcery_portal", "ritual.spawn_sorcery_portal.name").build("sorceryportal",
						NostrumResearchTab.TINKERING, Size.NORMAL, 2, 3, true, new ItemStack(NostrumBlocks.sorceryPortal));

		NostrumResearch.startBuilding().parent("geogems").hiddenParent("markrecall").lore(NostrumItems.positionCrystal)
				.quest("lvly").reference("ritual::teleportrune", "ritual.teleportrune.name").build("teleportrune",
						NostrumResearchTab.TINKERING, Size.NORMAL, 2, 0, true, new ItemStack(NostrumBlocks.teleportRune));

		NostrumResearch.startBuilding().parent("geogems").hiddenParent("item_duct").lore(NostrumItems.positionCrystal)
				.quest("lvly").reference("ritual::paradox_mirror", "ritual.paradox_mirror.name")
				.build("paradox_mirrors", NostrumResearchTab.TINKERING, Size.NORMAL, 3, 0, true,
						new ItemStack(NostrumBlocks.paradoxMirror));

		NostrumResearch.startBuilding().parent("geogems").hiddenParent("teleportrune").lore(NostrumItems.positionCrystal)
				.reference("ritual::mystic_anchor", "ritual.mystic_anchor.name")
				.build("mystic_anchor", NostrumResearchTab.TINKERING, Size.NORMAL, 4, 0, true,
						new ItemStack(NostrumBlocks.mysticAnchor));

		NostrumResearch.startBuilding().hiddenParent("rituals").hiddenParent("magic_token")
				.reference("ritual::putter", "ritual.putter.name").build("putter", NostrumResearchTab.TINKERING,
						Size.NORMAL, -1, -1, true, new ItemStack(NostrumBlocks.putterBlock));

		NostrumResearch.startBuilding().parent("putter").reference("ritual::active_hopper", "ritual.active_hopper.name")
				.build("active_hopper", NostrumResearchTab.TINKERING, Size.NORMAL, -1, 0, true,
						new ItemStack(NostrumBlocks.activeHopper));

		NostrumResearch.startBuilding().parent("active_hopper").reference("ritual::item_duct", "ritual.item_duct.name")
				.build("item_duct", NostrumResearchTab.TINKERING, Size.LARGE, -1, 1, true,
						new ItemStack(NostrumBlocks.itemDuct));

		NostrumResearch.startBuilding().hiddenParent("rituals")
				.reference("ritual::mimic_facade", "ritual.mimic_facade.name")
				.reference("ritual::mimic_door", "ritual.mimic_door.name").build("magicfacade",
						NostrumResearchTab.TINKERING, Size.NORMAL, -2, -1, true, new ItemStack(NostrumBlocks.mimicFacade));

		// Advanced Magica
		{
			NostrumResearch.Builder builder = NostrumResearch.startBuilding();
			builder.reference("ritual::thano_infusion", "ritual.thano_infusion.name");
			if (!ModConfig.config.usingEasierThano()) {
				builder.hiddenParent("vani").hiddenParent("reagent_bag").hiddenParent("mage_staff")
						.lore(NostrumItems.resourceToken).lore(NostrumItems.masteryOrb).lore(NostrumItems.essencePhysical);

			} else {
				builder.hiddenParent("kani").hiddenParent("reagent_bag").lore(NostrumItems.resourceToken)
						.lore(NostrumItems.essencePhysical);
			}

			builder.build("thano_pendant", NostrumResearchTab.ADVANCED_MAGICA, Size.GIANT, 2, -1, true,
					new ItemStack(NostrumItems.thanoPendant));
		}

		NostrumResearch.startBuilding().hiddenParent("vani").hiddenParent("loretable")
				.hiddenParent("spelltomes_advanced")
				.reference("ritual::modification_table", "ritual.modification_table.name").build("modification_table",
						NostrumResearchTab.ADVANCED_MAGICA, Size.GIANT, 0, -1, true,
						new ItemStack(NostrumBlocks.modificationTable));

		NostrumResearch.startBuilding().hiddenParent("vani").lore(NostrumItems.roseBlood)
				.reference("ritual::form_essential_ooze", "ritual.form_essential_ooze.name")
				.reference("ritual::form_living_flute", "ritual.form_living_flute.name")
				.reference("ritual::form_eldrich_pendant", "ritual.form_eldrich_pendant.name")
				.reference("ritual::form_primordial_mirror", "ritual.form_primordial_mirror.name")
				.build("stat_items",
						NostrumResearchTab.ADVANCED_MAGICA, Size.GIANT, -2, -1, true,
						new ItemStack(NostrumItems.resourceSkillPendant));

//		NostrumResearch.startBuilding().parent("stat_items")
//				.reference("ritual::form_primordial_mirror", "ritual.form_primordial_mirror.name")
//				.build("stat_items_adv", NostrumResearchTab.ADVANCED_MAGICA, Size.NORMAL, -2, 0, true,
//						new ItemStack(NostrumItems.skillMirror));
//
//		NostrumResearch.startBuilding().parent("stat_items_adv").lore(NostrumItems.dragonEggFragment)
//				.lore(NostrumItems.resourceSkillPendant)
//				.reference("ritual::form_primordial_mirror", "ritual.form_primordial_mirror.name")
//				.build("stat_items_wing", NostrumResearchTab.ADVANCED_MAGICA, Size.NORMAL, -2, 1, true,
//						new ItemStack(NostrumItems.resourceDragonWing));

		NostrumResearch.startBuilding().parent("stat_items").hiddenParent("sorceryportal")
				.reference("ritual::ender_pin", "ritual.ender_pin.name").build("ender_pin",
						NostrumResearchTab.ADVANCED_MAGICA, Size.LARGE, -3, 0, true,
						new ItemStack(NostrumItems.skillEnderPin));

		NostrumResearch.startBuilding().hiddenParent("kani").lore(IPetWithSoul.SoulBoundLore.instance())
				.reference("ritual::revive_soulbound_pet_dragon", "ritual.revive_soulbound_pet_dragon.name")
				.reference("ritual::revive_soulbound_pet_wolf", "ritual.revive_soulbound_pet_wolf.name")
				.build("soulbound_pets", NostrumResearchTab.ADVANCED_MAGICA, Size.GIANT, 0, 1, true,
						new ItemStack(NostrumItems.dragonSoulItem));

		NostrumResearch.startBuilding().parent("rituals").hiddenParent("soul_daggers").hiddenParent("kani")
				.lore(WolfTameLore.instance()).reference("ritual::transform_wolf", "ritual.transform_wolf.name")
				.build("wolf_transformation", NostrumResearchTab.MAGICA, Size.GIANT, 4, 0, true,
						new ItemStack(Items.BONE));

		NostrumResearch.startBuilding().hiddenParent("enchanted_armor_adv").hiddenParent("soul_daggers").quest("lvl10")
				.reference("ritual::mana_armorer", "ritual.mana_armorer.name").build("mana_armor",
						NostrumResearchTab.ADVANCED_MAGICA, Size.GIANT, 1, 0, true,
						new ItemStack(NostrumBlocks.manaArmorerBlock));

		// NostrumResearchTab tab, Size size, int x, int y, boolean hidden, ItemStack
		// icon
	}

	@SubscribeEvent(priority=EventPriority.HIGH)
	public static void registerShapes(RegistryEvent.Register<Item> event) {
		// Note: these are happening in the register<item> phase because they drive what items get
		// generated!
    	SpellShape.fireRegisterEvent();
    }
    
    @SubscribeEvent
    public static void registerEnchantments(RegistryEvent.Register<Enchantment> event) {
    	event.getRegistry().register(ManaRecoveryEnchantment.instance());
    }
    
    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
		for (NostrumMagicaSounds sound : NostrumMagicaSounds.values()) {
			event.getRegistry().register(sound.getEvent());
		}
    }
    
    @SubscribeEvent
    public static void registerDataSerializers(RegistryEvent.Register<DataSerializerEntry> event) {
    	final IForgeRegistry<DataSerializerEntry> registry = event.getRegistry();
    	
    	registry.register(new DataSerializerEntry(DragonArmorMaterialSerializer.instance).setRegistryName("nostrum.serial.dragon_armor"));
    	registry.register(new DataSerializerEntry(OptionalDragonArmorMaterialSerializer.instance).setRegistryName("nostrum.serial.dragon_armor_opt"));
    	registry.register(new DataSerializerEntry(MagicElementDataSerializer.instance).setRegistryName("nostrum.serial.element"));
    	registry.register(new DataSerializerEntry(HookshotTypeDataSerializer.instance).setRegistryName("nostrum.serial.hookshot_type"));
    	registry.register(new DataSerializerEntry(WilloStatusSerializer.instance).setRegistryName("nostrum.serial.willo_status"));
    	registry.register(new DataSerializerEntry(ArcaneWolfElementalTypeSerializer.instance).setRegistryName("nostrum.serial.arcane_wolf_type"));
    	registry.register(new DataSerializerEntry(FloatArraySerializer.instance).setRegistryName("nostrum.serial.float_array"));
    	registry.register(new DataSerializerEntry(OptionalMagicElementDataSerializer.instance).setRegistryName("nostrum.serial.element_opt"));
    	registry.register(new DataSerializerEntry(PlantBossTreeTypeSerializer.instance).setRegistryName("nostrum.serial.plantboss_tree_type"));
    	registry.register(new DataSerializerEntry(OptionalParticleDataSerializer.instance).setRegistryName("nostrum.serial.particle_opt"));
    	registry.register(new DataSerializerEntry(RedDragonBodyPartTypeSerializer.instance).setRegistryName("nostrum.serial.red_dragon.body_part_type"));
    	registry.register(new DataSerializerEntry(PetJobSerializer.GetInstance()).setRegistryName("nostrum.serial.pet_job"));
    }
	
	public static final void registerCommands(RegisterCommandsEvent event) {
		// Note: not in ModInit because it's not a MOD bus event. Commands get registered when data is reloaded.
		final CommandDispatcher<CommandSource> dispatcher = event.getDispatcher();
		
		CommandTestConfig.register(dispatcher);
		CommandTestConfig.register(dispatcher);
		CommandSpawnObelisk.register(dispatcher);
		CommandEnhanceTome.register(dispatcher);
		CommandSetLevel.register(dispatcher);
		CommandUnlock.register(dispatcher);
		CommandGiveSkillpoint.register(dispatcher);
		CommandAllQuests.register(dispatcher);
		CommandAllResearch.register(dispatcher);
		CommandCreateGeotoken.register(dispatcher);
		CommandForceBind.register(dispatcher);
		CommandSpawnDungeon.register(dispatcher);
		CommandUnlockAll.register(dispatcher);
		CommandSetDimension.register(dispatcher);
		CommandWriteRoom.register(dispatcher);
		CommandReadRoom.register(dispatcher);
		CommandGiveResearchpoint.register(dispatcher);
		CommandReloadResearch.register(dispatcher);
		CommandRandomSpell.register(dispatcher);
		CommandDebugEffect.register(dispatcher);
		CommandSetManaArmor.register(dispatcher);
		CommandAllPatterns.register(dispatcher);
		CommandReloadQuests.register(dispatcher);
		CommandReloadSkills.register(dispatcher);
		CommandSetTier.register(dispatcher);
	}
	
	public static final void onBiomeLoad(BiomeLoadingEvent event) {
		// Note: not in ModInit because it's not a MOD bus event.
		Biome.Category category = event.getCategory();
		
		if (category == Biome.Category.THEEND) {
			return;
		}
		
		if (category == Biome.Category.NETHER) {
			return;
		}
		
		// Filter this list maybe?
		final BiomeGenerationSettingsBuilder gen = event.getGeneration();
		gen.withFeature(GenerationStage.Decoration.VEGETAL_DECORATION, NostrumFeatures.CONFFEATURE_FLOWER_CRYSTABLOOM);
		gen.withFeature(GenerationStage.Decoration.VEGETAL_DECORATION, NostrumFeatures.CONFFEATURE_FLOWER_MIDNIGHTIRIS);
		
		gen.withFeature(GenerationStage.Decoration.UNDERGROUND_ORES, NostrumFeatures.CONFFEATURE_ORE_MANI);
		gen.withFeature(GenerationStage.Decoration.UNDERGROUND_ORES, NostrumFeatures.CONFFEATURE_ORE_ESSORE);
		
		gen.withStructure(NostrumStructures.CONFIGURED_DUNGEON_PORTAL);
		gen.withStructure(NostrumStructures.CONFIGURED_DUNGEON_DRAGON);
		gen.withStructure(NostrumStructures.CONFIGUREDDUNGEON_PLANTBOSS);
////		  Have to add structures as structures AND features.
////		 Vanilla adds all structs as features and then only some as structures to turn them on for different biomes.
////		 Adding as struct makes the world generate starts and the logical part. Adding as features makes them actually place in the world.
//		gen.withFeature(GenerationStage.Decoration.SURFACE_STRUCTURES, Biome.createDecoratedFeature(NostrumFeatures.portalDungeon, new NostrumDungeonConfig(), Placement.NOPE, IPlacementConfig.NO_PLACEMENT_CONFIG));
//		gen.withFeature(GenerationStage.Decoration.SURFACE_STRUCTURES, Biome.createDecoratedFeature(NostrumFeatures.dragonDungeon, new NostrumDungeonConfig(), Placement.NOPE, IPlacementConfig.NO_PLACEMENT_CONFIG));
//		gen.withFeature(GenerationStage.Decoration.SURFACE_STRUCTURES, Biome.createDecoratedFeature(NostrumFeatures.plantbossDungeon, new NostrumDungeonConfig(), Placement.NOPE, IPlacementConfig.NO_PLACEMENT_CONFIG));
	}
	
	public static final void registerDataReloaders(TagsUpdatedEvent.CustomTagTypes event) {
		NostrumMagica.logger.info("Got custom tag reload notification");
		RitualRegistry.instance().reloadRituals();
//		// This event is weird because it's for registering listeners of another event
//		event.addListener(new ReloadListener<Object>() {
//
//			@Override
//			protected Object prepare(IResourceManager resourceManagerIn, IProfiler profilerIn) {
//				return null;
//			}
//
//			@Override
//			protected void apply(Object objectIn, IResourceManager resourceManagerIn, IProfiler profilerIn) {
//				NostrumMagica.logger.info("Got data reload notification");
//				RitualRegistry.instance().reloadRituals();
//				if (ServerLifecycleHooks.getCurrentServer() == null) {
//					NostrumMagica.logger.info("Ignoring data reload with no server");
//					return;
//				}
//			}
//		});
	}
}
