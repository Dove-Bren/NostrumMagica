package com.smanzana.nostrummagica.init;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.brigadier.CommandDispatcher;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.NostrumBlocks;
import com.smanzana.nostrummagica.capabilities.CapabilityHandler;
import com.smanzana.nostrummagica.capabilities.IManaArmor;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.capabilities.ManaArmor;
import com.smanzana.nostrummagica.capabilities.ManaArmorStorage;
import com.smanzana.nostrummagica.capabilities.NostrumMagic;
import com.smanzana.nostrummagica.capabilities.NostrumMagicStorage;
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
import com.smanzana.nostrummagica.command.CommandReloadResearch;
import com.smanzana.nostrummagica.command.CommandSetDimension;
import com.smanzana.nostrummagica.command.CommandSetLevel;
import com.smanzana.nostrummagica.command.CommandSetManaArmor;
import com.smanzana.nostrummagica.command.CommandSpawnDungeon;
import com.smanzana.nostrummagica.command.CommandSpawnObelisk;
import com.smanzana.nostrummagica.command.CommandTestConfig;
import com.smanzana.nostrummagica.command.CommandUnlock;
import com.smanzana.nostrummagica.command.CommandUnlockAll;
import com.smanzana.nostrummagica.command.CommandWriteRoom;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.enchantments.EnchantmentManaRecovery;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf.WolfTameLore;
import com.smanzana.nostrummagica.entity.EntityKoid;
import com.smanzana.nostrummagica.entity.EntityWisp;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.entity.dragon.EntityTameDragonRed;
import com.smanzana.nostrummagica.entity.golem.EntityGolem;
import com.smanzana.nostrummagica.integration.curios.items.NostrumCurios;
import com.smanzana.nostrummagica.items.AspectedWeapon;
import com.smanzana.nostrummagica.items.DragonArmor;
import com.smanzana.nostrummagica.items.DragonArmor.DragonArmorMaterial;
import com.smanzana.nostrummagica.items.DragonArmor.DragonEquipmentSlot;
import com.smanzana.nostrummagica.items.EssenceItem;
import com.smanzana.nostrummagica.items.MagicArmor;
import com.smanzana.nostrummagica.items.MagicArmorBase;
import com.smanzana.nostrummagica.items.MagicCharm;
import com.smanzana.nostrummagica.items.NostrumItems;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.items.WarlockSword;
import com.smanzana.nostrummagica.loot.NostrumLoot;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.pet.IPetWithSoul;
import com.smanzana.nostrummagica.quests.NostrumQuest;
import com.smanzana.nostrummagica.quests.NostrumQuest.QuestType;
import com.smanzana.nostrummagica.quests.objectives.ObjectiveKill;
import com.smanzana.nostrummagica.quests.objectives.ObjectiveRitual;
import com.smanzana.nostrummagica.quests.objectives.ObjectiveSpellCast;
import com.smanzana.nostrummagica.quests.rewards.AlterationReward;
import com.smanzana.nostrummagica.quests.rewards.AttributeReward;
import com.smanzana.nostrummagica.quests.rewards.AttributeReward.AwardType;
import com.smanzana.nostrummagica.quests.rewards.IReward;
import com.smanzana.nostrummagica.research.NostrumResearch;
import com.smanzana.nostrummagica.research.NostrumResearch.NostrumResearchTab;
import com.smanzana.nostrummagica.research.NostrumResearch.Size;
import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.rituals.RitualRegistry;
import com.smanzana.nostrummagica.rituals.outcomes.IRitualOutcome;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeApplyTransformation;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeBindSpell;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeConstructGeotoken;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeCreateObelisk;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeCreatePortal;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeCreateTome;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeMark;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomePotionEffect;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeRecall;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeReviveSoulboundPet;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeSpawnEntity;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeSpawnEntity.IEntityFactory;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeSpawnItem;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeTeleportObelisk;
import com.smanzana.nostrummagica.rituals.requirements.IRitualRequirement;
import com.smanzana.nostrummagica.rituals.requirements.RRequirementAlterationMastery;
import com.smanzana.nostrummagica.rituals.requirements.RRequirementElementMastery;
import com.smanzana.nostrummagica.rituals.requirements.RRequirementResearch;
import com.smanzana.nostrummagica.rituals.requirements.RRequirementShapeMastery;
import com.smanzana.nostrummagica.rituals.requirements.RRequirementTriggerMastery;
import com.smanzana.nostrummagica.serializers.ArcaneWolfElementalTypeSerializer;
import com.smanzana.nostrummagica.serializers.DragonArmorMaterialSerializer;
import com.smanzana.nostrummagica.serializers.FloatArraySerializer;
import com.smanzana.nostrummagica.serializers.HookshotTypeDataSerializer;
import com.smanzana.nostrummagica.serializers.MagicElementDataSerializer;
import com.smanzana.nostrummagica.serializers.OptionalDragonArmorMaterialSerializer;
import com.smanzana.nostrummagica.serializers.OptionalMagicElementDataSerializer;
import com.smanzana.nostrummagica.serializers.OptionalParticleDataSerializer;
import com.smanzana.nostrummagica.serializers.PetJobSerializer;
import com.smanzana.nostrummagica.serializers.PlantBossTreeTypeSerializer;
import com.smanzana.nostrummagica.serializers.RedDragonBodyPartTypeSerializer;
import com.smanzana.nostrummagica.serializers.WilloStatusSerializer;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.spells.components.shapes.AoEShape;
import com.smanzana.nostrummagica.spells.components.shapes.ChainShape;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.AITargetTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.AtFeetTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.AuraTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.BeamTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.CasterTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.DamagedTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.DelayTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.FieldTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.FoodTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.HealthTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.MagicCutterTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.MagicCyclerTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ManaTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.MortarTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.OtherTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ProjectileTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ProximityTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.SeekingBulletTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.SelfTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.TouchTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.WallTrigger;
import com.smanzana.nostrummagica.spelltome.enhancement.SpellTomeEnhancement;
import com.smanzana.nostrummagica.trials.TrialEarth;
import com.smanzana.nostrummagica.trials.TrialEnder;
import com.smanzana.nostrummagica.trials.TrialFire;
import com.smanzana.nostrummagica.trials.TrialIce;
import com.smanzana.nostrummagica.trials.TrialLightning;
import com.smanzana.nostrummagica.trials.TrialPhysical;
import com.smanzana.nostrummagica.trials.TrialWind;
import com.smanzana.nostrummagica.trials.WorldTrial;
import com.smanzana.nostrummagica.utils.Ingredients;
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
		MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, ModInit::registerDataReloaders);
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
	}
	
	private static final void init() {
    	LoreRegistry.instance();
    	EntityTameDragonRed.init();
    	
    	CapabilityManager.INSTANCE.register(INostrumMagic.class, new NostrumMagicStorage(), NostrumMagic::new);
		CapabilityManager.INSTANCE.register(IManaArmor.class, new ManaArmorStorage(), ManaArmor::new);
		new CapabilityHandler();
	}
	
	private static final void postinit() {
		NostrumQuest.Validate();
		NostrumResearch.Validate();
	}
	
	public static final void registerDefaultRituals(RitualRegistry.RitualRegisterEvent event) {
		RitualRegistry registry = event.registry;
		
		RitualRecipe recipe;

		for (EMagicElement element : EMagicElement.values()) {
			recipe = RitualRecipe
					.createTier2("rune." + element.name().toLowerCase(), SpellRune.getRune(element, 1), null,
							new ReagentType[] { ReagentType.CRYSTABLOOM, ReagentType.MANDRAKE_ROOT,
									ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST },
							Ingredient.fromStacks(EssenceItem.getEssence(element, 1)),
							IRitualRequirement.AND(new RRequirementElementMastery(element),
									new RRequirementResearch("spellrunes")),
							new OutcomeSpawnItem(SpellRune.getRune(element, 1)));
			registry.register(recipe);
		}

		// Shape Runes
		recipe = RitualRecipe.createTier2("rune.single", SpellRune.getRune(SingleShape.instance()), null,
				new ReagentType[] {
						ReagentType.GINSENG, ReagentType.GINSENG, ReagentType.SPIDER_SILK, ReagentType.SKY_ASH },
				Ingredient.fromTag(NostrumTags.Items.MagicToken),
				IRitualRequirement.AND(new RRequirementShapeMastery(SingleShape.instance()),
						new RRequirementResearch("spellrunes")),
				new OutcomeSpawnItem(SpellRune.getRune(SingleShape.instance())));
		registry.register(recipe);

		recipe = RitualRecipe.createTier3("rune.chain", SpellRune.getRune(ChainShape.instance()), null,
				new ReagentType[] { ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.SPIDER_SILK,
						ReagentType.MANDRAKE_ROOT },
				Ingredient.fromStacks(SpellRune.getRune(SingleShape.instance())),
				new Ingredient[] { Ingredient.fromStacks(SpellRune.getRune(SingleShape.instance())), Ingredient.fromTag(Tags.Items.INGOTS_GOLD),
						Ingredient.fromStacks(SpellRune.getRune(SingleShape.instance())), Ingredient.fromStacks(SpellRune.getRune(SingleShape.instance())) },
				IRitualRequirement.AND(new RRequirementShapeMastery(ChainShape.instance()),
						new RRequirementResearch("spellrunes")),
				new OutcomeSpawnItem(SpellRune.getRune(ChainShape.instance())));
		registry.register(recipe);

		recipe = RitualRecipe.createTier3(
				"rune.aoe", SpellRune.getRune(AoEShape.instance()), null, new ReagentType[] { ReagentType.MANI_DUST,
						ReagentType.GRAVE_DUST, ReagentType.SPIDER_SILK, ReagentType.MANDRAKE_ROOT },
				Ingredient.fromStacks(SpellRune.getRune(ChainShape.instance())),
				new Ingredient[] { Ingredient.fromStacks(SpellRune.getRune(ChainShape.instance())), Ingredient.fromTag(Tags.Items.GEMS_DIAMOND),
						Ingredient.fromStacks(SpellRune.getRune(SingleShape.instance())), Ingredient.fromStacks(SpellRune.getRune(ChainShape.instance())) },
				IRitualRequirement.AND(new RRequirementShapeMastery(AoEShape.instance()),
						new RRequirementResearch("spellrunes")),
				new OutcomeSpawnItem(SpellRune.getRune(AoEShape.instance())));
		registry.register(recipe);

		for (EAlteration alteration : EAlteration.values()) {
			recipe = RitualRecipe.createTier2("rune." + alteration.name().toLowerCase(), SpellRune.getRune(alteration),
					null,
					new ReagentType[] {
							ReagentType.GINSENG, ReagentType.GRAVE_DUST, ReagentType.SKY_ASH, ReagentType.GRAVE_DUST },
					Ingredient.fromStacks(alteration.getCraftItem()),
					IRitualRequirement.AND(new RRequirementAlterationMastery(alteration),
							new RRequirementResearch("spellrunes")),
					new OutcomeSpawnItem(SpellRune.getRune(alteration)));
			registry.register(recipe);
		}

		for (SpellTrigger trigger : SpellTrigger.getAllTriggers()) {
			recipe = RitualRecipe.createTier3("rune." + trigger.getTriggerKey().toLowerCase(),
					SpellRune.getRune(trigger), null,
					new ReagentType[] {
							ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.GINSENG, ReagentType.GINSENG },
					Ingredient.fromTag(NostrumTags.Items.MagicToken),
					new Ingredient[] { Ingredient.fromTag(Tags.Items.NUGGETS_GOLD), Ingredient.fromStacks(trigger.getCraftItem()), Ingredient.EMPTY,
							Ingredient.fromTag(Tags.Items.NUGGETS_GOLD) },
					IRitualRequirement.AND(new RRequirementTriggerMastery(trigger),
							new RRequirementResearch("spellrunes")),
					new OutcomeSpawnItem(SpellRune.getRune(trigger)));
			registry.register(recipe);
		}

		// Boons
		{
			recipe = RitualRecipe.createTier1("buff.luck", new ItemStack(Items.RABBIT_FOOT), EMagicElement.PHYSICAL,
					ReagentType.SPIDER_SILK, new RRequirementResearch("boon"),
					new OutcomePotionEffect(Effects.LUCK, 0, 120 * 20));
			registry.register(recipe);

			recipe = RitualRecipe.createTier1("buff.speed", new ItemStack(Items.ARROW), EMagicElement.WIND,
					ReagentType.SKY_ASH, new RRequirementResearch("boon"),
					new OutcomePotionEffect(Effects.SPEED, 0, 120 * 20));
			registry.register(recipe);

			recipe = RitualRecipe.createTier1("buff.strength", new ItemStack(Items.IRON_SWORD), EMagicElement.FIRE,
					ReagentType.MANDRAKE_ROOT, new RRequirementResearch("boon"),
					new OutcomePotionEffect(Effects.STRENGTH, 0, 120 * 20));
			registry.register(recipe);

			recipe = RitualRecipe.createTier1("buff.leaping",
					new ItemStack(Blocks.QUARTZ_STAIRS), EMagicElement.LIGHTNING,
					ReagentType.MANI_DUST, new RRequirementResearch("boon"),
					new OutcomePotionEffect(Effects.JUMP_BOOST, 0, 120 * 20));
			registry.register(recipe);

			recipe = RitualRecipe.createTier1("buff.regen", new ItemStack(Items.GOLDEN_APPLE), EMagicElement.EARTH,
					ReagentType.GINSENG, new RRequirementResearch("boon"),
					new OutcomePotionEffect(Effects.REGENERATION, 0, 120 * 20));
			registry.register(recipe);

			recipe = RitualRecipe.createTier1("buff.fireresist", new ItemStack(Items.MAGMA_CREAM), EMagicElement.FIRE,
					ReagentType.CRYSTABLOOM, new RRequirementResearch("boon"),
					new OutcomePotionEffect(Effects.FIRE_RESISTANCE, 0, 120 * 20));
			registry.register(recipe);

			recipe = RitualRecipe.createTier1("buff.invisibility", new ItemStack(Items.ENDER_EYE), EMagicElement.ENDER,
					ReagentType.GRAVE_DUST, new RRequirementResearch("boon"),
					new OutcomePotionEffect(Effects.INVISIBILITY, 0, 120 * 20));
			registry.register(recipe);

			recipe = RitualRecipe.createTier1("buff.nightvision", new ItemStack(Items.GOLDEN_CARROT),
					EMagicElement.PHYSICAL, ReagentType.BLACK_PEARL, new RRequirementResearch("boon"),
					new OutcomePotionEffect(Effects.NIGHT_VISION, 0, 120 * 20));
			registry.register(recipe);

			recipe = RitualRecipe.createTier1("buff.waterbreathing", new ItemStack(Items.SALMON), EMagicElement.ICE,
					ReagentType.MANI_DUST, new RRequirementResearch("boon"),
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
				new RRequirementResearch("markrecall"), new OutcomeMark());
		registry.register(recipe);

		recipe = RitualRecipe.createTier1("recall", new ItemStack(Items.COMPASS), EMagicElement.LIGHTNING,
				ReagentType.SKY_ASH, new RRequirementResearch("markrecall"), new OutcomeRecall());
		registry.register(recipe);

		// medium crystal -- tier 2. Small crystal, reagents, basic crystal
		Ingredient crystal = Ingredient.fromTag(NostrumTags.Items.CrystalMedium);
		registry
				.register(RitualRecipe.createTier2("kani", new ItemStack(NostrumItems.crystalMedium), null,
						new ReagentType[] { ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST, ReagentType.GINSENG,
								ReagentType.GRAVE_DUST },
						Ingredient.fromTag(NostrumTags.Items.CrystalSmall), new RRequirementResearch("kani"),
						new OutcomeSpawnItem(new ItemStack(NostrumItems.crystalMedium))));

		// large crystal -- tier 3. Medium crystal, 4 medium crystals, reagents, basic
		// crystal

		registry
				.register(RitualRecipe.createTier3("vani", new ItemStack(NostrumItems.crystalLarge),
						null,
						new ReagentType[] { ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST, ReagentType.BLACK_PEARL,
								ReagentType.CRYSTABLOOM },
						crystal, new Ingredient[] { crystal, crystal, crystal, crystal },
						new RRequirementResearch("vani"),
						new OutcomeSpawnItem( new ItemStack(NostrumItems.crystalLarge))));

		// magic token -- tier 1. Mani dust.
		registry
				.register(RitualRecipe.createTier1("magic_token", new ItemStack(NostrumItems.resourceToken),
						null, ReagentType.MANI_DUST, new RRequirementResearch("magic_token"),
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
								new RRequirementResearch("magic_token_3"),
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
				new RRequirementResearch("essence_seeds"), new OutcomeSpawnItem(new ItemStack(NostrumItems.reagentSeedEssence))));
		
		// fierce slab -- tier 3. Kani crystal. Fire + Wind gems
		registry
				.register(RitualRecipe.createTier3("fierce_infusion", new ItemStack(NostrumItems.resourceSlabFierce),
						EMagicElement.LIGHTNING,
						new ReagentType[] { ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST, ReagentType.MANDRAKE_ROOT,
								ReagentType.SPIDER_SILK },
						crystal,
						new Ingredient[] { Ingredient.fromTag(NostrumTags.Items.InfusedGemFire), Ingredient.EMPTY,
								Ingredient.EMPTY, Ingredient.fromTag(NostrumTags.Items.InfusedGemWind) },
						new RRequirementResearch("fierce_infusion"),
						new OutcomeSpawnItem(new ItemStack(NostrumItems.resourceSlabFierce))));

		// kind slab -- tier 3. Kani crystal. Ice + Earth gems
		registry.register(RitualRecipe.createTier3("kind_infusion",
				new ItemStack(NostrumItems.resourceSlabKind), EMagicElement.ENDER,
				new ReagentType[] {
						ReagentType.CRYSTABLOOM, ReagentType.GINSENG, ReagentType.MANI_DUST, ReagentType.SKY_ASH },
				crystal,
				new Ingredient[] { Ingredient.fromTag(NostrumTags.Items.InfusedGemIce), Ingredient.EMPTY,
						Ingredient.EMPTY, Ingredient.fromTag(NostrumTags.Items.InfusedGemEarth) },
				new RRequirementResearch("kind_infusion"),
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
				new RRequirementResearch("balanced_infusion"),
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
				new RRequirementResearch("thano_pendant"),
				new OutcomeSpawnItem(new ItemStack(NostrumItems.thanoPendant))));

		// Obelisk -- tier 3. Vani crystal. Balanced slab, 2 eyes of ender, compass.
		registry.register(RitualRecipe.createTier3("create_obelisk",
				new ItemStack(NostrumItems.mirrorItem), EMagicElement.ENDER,
				new ReagentType[] {
						ReagentType.BLACK_PEARL, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.GINSENG },
				Ingredient.fromTag(NostrumTags.Items.CrystalLarge),
				new Ingredient[] { Ingredient.fromTag(NostrumTags.Items.SlabBalanced),
						Ingredient.fromItems(Items.ENDER_EYE), Ingredient.fromItems(Items.ENDER_EYE), Ingredient.fromItems(Items.COMPASS) },
				new RRequirementResearch("obelisks"), new OutcomeCreateObelisk()));

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
								new RRequirementResearch("geogems"),
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
						new RRequirementResearch("geotokens"), new OutcomeConstructGeotoken(1)));

		// GeoToken clone -- tier 3. Geotoken center. Magic Tokens and mani crystal
		registry.register(RitualRecipe.createTier3("geotoken_3", "geotoken",
				new ItemStack(NostrumItems.positionToken), null, new ReagentType[] { ReagentType.GRAVE_DUST,
						ReagentType.GINSENG, ReagentType.MANDRAKE_ROOT, ReagentType.GRAVE_DUST },
				Ingredient.fromItems(NostrumItems.positionToken),
				new Ingredient[] { Ingredient.fromTag(NostrumTags.Items.CrystalSmall),
						Ingredient.fromItems(NostrumItems.resourceToken),
						Ingredient.fromItems(NostrumItems.resourceToken),
						Ingredient.fromItems(NostrumItems.resourceToken) },
				new RRequirementResearch("geotokens"), new OutcomeConstructGeotoken(4)));

		// Mystic Anchor
		registry.register(RitualRecipe.createTier3("mystic_anchor", new ItemStack(NostrumBlocks.mysticAnchor),
						EMagicElement.ENDER, new ReagentType[] { ReagentType.BLACK_PEARL, ReagentType.MANDRAKE_ROOT,
								ReagentType.MANDRAKE_ROOT, ReagentType.GRAVE_DUST },
						Ingredient.fromTag(Tags.Items.OBSIDIAN),
						new Ingredient[] { Ingredient.fromTag(NostrumTags.Items.CrystalMedium),
								Ingredient.fromTag(NostrumTags.Items.SpriteCore),
								Ingredient.fromItems(Items.ENDER_PEARL),
								Ingredient.fromTag(NostrumTags.Items.Rose) },
						new RRequirementResearch("mystic_anchor"), new OutcomeSpawnItem(new ItemStack(NostrumBlocks.mysticAnchor))));

		// Tele to obelisk -- tier 2. Position gem, reagents
		registry
				.register(RitualRecipe.createTier2("teleport_obelisk", new ItemStack(Items.ENDER_PEARL),
						EMagicElement.ENDER,
						new ReagentType[] { ReagentType.MANI_DUST, ReagentType.MANI_DUST, ReagentType.SKY_ASH,
								ReagentType.SPIDER_SILK },
						Ingredient.fromItems(NostrumItems.positionCrystal), new RRequirementResearch("obelisks"),
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
				new RRequirementResearch("summonkoids"), new OutcomeSpawnEntity(new IEntityFactory() {
					@Override
					public void spawn(World world, Vector3d pos, PlayerEntity invoker, ItemStack centerItem) {
						EntityKoid koid = new EntityKoid(NostrumEntityTypes.koid, world);
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
				new RRequirementResearch("elemental_trials"),
				new OutcomeSpawnItem(new ItemStack(NostrumItems.masteryOrb))));

		// Spell Tome Creation
		registry.register(RitualRecipe.createTier3("tome", new ItemStack(NostrumItems.spellTomeNovice), null,
				new ReagentType[] {
						ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK, ReagentType.MANI_DUST },
				Ingredient.fromTag(NostrumTags.Items.TomePlate),
				new Ingredient[] { Ingredient.fromItems(NostrumItems.spellTomePage), Ingredient.fromItems(NostrumItems.spellTomePage),
						Ingredient.fromItems(NostrumItems.spellTomePage), Ingredient.fromItems(NostrumItems.spellTomePage) },
				new RRequirementResearch("spelltomes_advanced"), new OutcomeCreateTome()));
		registry.register(RitualRecipe.createTier3("tome2", "tome", new ItemStack(NostrumItems.spellTomeNovice), null,
				new ReagentType[] {
						ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK, ReagentType.MANI_DUST },
				Ingredient.fromTag(NostrumTags.Items.TomePlate),
				new Ingredient[] { Ingredient.fromItems(NostrumItems.spellTomePage), Ingredient.fromItems(NostrumItems.spellTomePage),
						Ingredient.EMPTY, Ingredient.fromItems(NostrumItems.spellTomePage) },
				new RRequirementResearch("spelltomes_advanced"), new OutcomeCreateTome()));
		registry
				.register(RitualRecipe.createTier3("tome3", "tome", new ItemStack(NostrumItems.spellTomeNovice), null,
						new ReagentType[] { ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK,
								ReagentType.MANI_DUST },
						Ingredient.fromTag(NostrumTags.Items.TomePlate),
						new Ingredient[] { Ingredient.fromItems(NostrumItems.spellTomePage), Ingredient.EMPTY, Ingredient.EMPTY,
								Ingredient.fromItems(NostrumItems.spellTomePage) },
						new RRequirementResearch("spelltomes_advanced"), new OutcomeCreateTome()));
		registry
				.register(RitualRecipe.createTier3("tome4", "tome", new ItemStack(NostrumItems.spellTomeNovice), null,
						new ReagentType[] { ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK,
								ReagentType.MANI_DUST },
						Ingredient.fromTag(NostrumTags.Items.TomePlate),
						new Ingredient[] { Ingredient.fromItems(NostrumItems.spellTomePage), Ingredient.EMPTY, Ingredient.EMPTY,
								Ingredient.EMPTY },
						new RRequirementResearch("spelltomes_advanced"), new OutcomeCreateTome()));
		registry.register(RitualRecipe.createTier2("tome5", "tome", new ItemStack(NostrumItems.spellTomeNovice), null,
				new ReagentType[] { ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK,
						ReagentType.MANI_DUST },
				Ingredient.fromTag(NostrumTags.Items.TomePlate),
				new RRequirementResearch("spelltomes"), new OutcomeCreateTome()));

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
						new RRequirementResearch("spellbinding"), new OutcomeBindSpell()));

		// Magic Charms
		for (EMagicElement element : EMagicElement.values()) {
			registry
					.register(RitualRecipe.createTier2("charm." + element.name().toLowerCase(),
							MagicCharm.getCharm(element, 1), null,
							new ReagentType[] { ReagentType.GRAVE_DUST, ReagentType.GRAVE_DUST, ReagentType.MANI_DUST,
									ReagentType.MANDRAKE_ROOT },
							Ingredient.fromItems(EssenceItem.getEssenceItem(element)), new RRequirementResearch("charms"),
							new OutcomeSpawnItem(MagicCharm.getCharm(element, 8))));
		}

		// Mirror from wing
		registry
				.register(RitualRecipe.createTier3("form_primordial_mirror",
						new ItemStack(NostrumItems.skillMirror), null,
						new ReagentType[] { ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.SKY_ASH,
								ReagentType.SPIDER_SILK },
						Ingredient.fromTag(Tags.Items.GLASS_PANES),
						new Ingredient[] { Ingredient.fromTag(NostrumTags.Items.CrystalMedium),
								Ingredient.fromTag(NostrumTags.Items.CrystalLarge),
								Ingredient.fromTag(NostrumTags.Items.DragonWing),
								Ingredient.fromTag(NostrumTags.Items.CrystalMedium)
								},
						new RRequirementResearch("stat_items_wing"),
						new OutcomeSpawnItem(new ItemStack(NostrumItems.skillMirror))));

		// Mirror from roses
		registry
				.register(RitualRecipe.createTier3("form_primordial_mirror_blood", "form_primordial_mirror", 
						new ItemStack(NostrumItems.skillMirror), null,
						new ReagentType[] { ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.SKY_ASH,
								ReagentType.SPIDER_SILK },
						Ingredient.fromTag(Tags.Items.GLASS_PANES),
						new Ingredient[] { Ingredient.fromTag(NostrumTags.Items.RoseBlood),
								Ingredient.fromTag(NostrumTags.Items.CrystalLarge),
								Ingredient.fromTag(NostrumTags.Items.RoseBlood),
								Ingredient.fromTag(NostrumTags.Items.RoseBlood)
								},
						new RRequirementResearch("stat_items_adv"),
						new OutcomeSpawnItem(new ItemStack(NostrumItems.skillMirror))));
		registry
		.register(RitualRecipe.createTier3("form_primordial_mirror_pale", "form_primordial_mirror",
				new ItemStack(NostrumItems.skillMirror), null,
				new ReagentType[] { ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.SKY_ASH,
						ReagentType.SPIDER_SILK },
				Ingredient.fromTag(Tags.Items.GLASS_PANES),
				new Ingredient[] { Ingredient.fromTag(NostrumTags.Items.RosePale),
						Ingredient.fromTag(NostrumTags.Items.CrystalLarge),
						Ingredient.fromTag(NostrumTags.Items.RosePale),
						Ingredient.fromTag(NostrumTags.Items.RosePale)
						},
				new RRequirementResearch("stat_items_adv"),
				new OutcomeSpawnItem(new ItemStack(NostrumItems.skillMirror))));
		registry
		.register(RitualRecipe.createTier3("form_primordial_mirror_eldrich", "form_primordial_mirror", 
				new ItemStack(NostrumItems.skillMirror), null,
				new ReagentType[] { ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.SKY_ASH,
						ReagentType.SPIDER_SILK },
				Ingredient.fromTag(Tags.Items.GLASS_PANES),
				new Ingredient[] { Ingredient.fromTag(NostrumTags.Items.RoseEldrich),
						Ingredient.fromTag(NostrumTags.Items.CrystalLarge),
						Ingredient.fromTag(NostrumTags.Items.RoseEldrich),
						Ingredient.fromTag(NostrumTags.Items.RoseEldrich)
						},
				new RRequirementResearch("stat_items_adv"),
				new OutcomeSpawnItem(new ItemStack(NostrumItems.skillMirror))));

		// Ooze
		registry
				.register(RitualRecipe.createTier3("form_essential_ooze",
						new ItemStack(NostrumItems.skillOoze), null,
						new ReagentType[] { ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK },
						Ingredient.fromTag(Tags.Items.SLIMEBALLS),
						new Ingredient[] { Ingredient.fromTag(NostrumTags.Items.CrystalMedium),
								Ingredient.fromTag(NostrumTags.Items.CrystalLarge),
								Ingredient.fromTag(NostrumTags.Items.RosePale),
								Ingredient.fromTag(NostrumTags.Items.CrystalMedium) },
						new RRequirementResearch("stat_items"),
						new OutcomeSpawnItem(new ItemStack(NostrumItems.skillOoze))));

		// Flute
		registry
				.register(RitualRecipe
						.createTier3("form_living_flute", new ItemStack(NostrumItems.skillFlute), null,
								new ReagentType[] { ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.SKY_ASH,
										ReagentType.SPIDER_SILK },
								Ingredient.fromItems(Items.SUGAR_CANE),
								new Ingredient[] { Ingredient.fromTag(NostrumTags.Items.CrystalMedium),
										Ingredient.fromTag(NostrumTags.Items.CrystalLarge),
										Ingredient.fromTag(NostrumTags.Items.RoseBlood),
										Ingredient.fromTag(NostrumTags.Items.CrystalMedium) },
								new RRequirementResearch("stat_items"),
								new OutcomeSpawnItem(new ItemStack(NostrumItems.skillFlute))));

		// Pendant
		registry.register(RitualRecipe.createTier3("form_eldrich_pendant",
				new ItemStack(NostrumItems.skillPendant), null,
				new ReagentType[] {
						ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK },
				Ingredient.fromTag(Tags.Items.ENDER_PEARLS),
				new Ingredient[] { Ingredient.fromTag(NostrumTags.Items.CrystalMedium),
						Ingredient.fromTag(NostrumTags.Items.CrystalLarge),
						Ingredient.fromTag(NostrumTags.Items.RoseEldrich),
						Ingredient.fromTag(NostrumTags.Items.CrystalMedium) },
				new RRequirementResearch("stat_items"),
				new OutcomeSpawnItem(new ItemStack(NostrumItems.skillPendant))));

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
						new RRequirementResearch("ender_pin"),
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
				new RRequirementResearch("mirror_shield"),
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
						new RRequirementResearch("true_mirror_shield"),
						new OutcomeSpawnItem(new ItemStack(NostrumItems.mirrorShieldImproved))));

		registry
				.register(RitualRecipe.createTier2("spawn_sorcery_portal", new ItemStack(NostrumBlocks.sorceryPortal),
						EMagicElement.ENDER,
						new ReagentType[] { ReagentType.BLACK_PEARL, ReagentType.BLACK_PEARL, ReagentType.MANDRAKE_ROOT,
								ReagentType.MANI_DUST },
						Ingredient.fromItems(NostrumItems.resourceSeekingGem), new RRequirementResearch("sorceryportal"),
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
						new RRequirementResearch("warlock_sword"),
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
						new RRequirementResearch("mage_blade"),
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
				new RRequirementResearch("sword_fire"),
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
				new RRequirementResearch("sword_ender"),
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
				new RRequirementResearch("sword_earth"),
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
				new RRequirementResearch("sword_physical"),
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
				new RRequirementResearch("seeking_gems"),
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
							new RRequirementResearch(research), new OutcomeSpawnItem(outcome.copy())));
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
								new RRequirementResearch(research), outcome));
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
						new RRequirementResearch(research), new OutcomeSpawnItem(outcome.copy())));
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
								new RRequirementResearch("dragon_armor"), new OutcomeSpawnItem(result.copy())));

				if (prev != Ingredient.EMPTY) {
					// Upgrade ritual
					registry
							.register(RitualRecipe.createTier3(
									"upgrade_dragonarmor_" + slot.getName() + "_" + material.name().toLowerCase(),
									result, EMagicElement.PHYSICAL,
									new ReagentType[] { ReagentType.MANDRAKE_ROOT, ReagentType.SKY_ASH,
											ReagentType.BLACK_PEARL, ReagentType.MANI_DUST },
									prev, new Ingredient[] { Ingredient.EMPTY, augment, cost, Ingredient.EMPTY },
									new RRequirementResearch("dragon_armor"), new OutcomeSpawnItem(result.copy())));
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
				new RRequirementResearch("hookshot_medium"), new OutcomeSpawnItem(
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
				new RRequirementResearch("hookshot_strong"), new OutcomeSpawnItem(
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
				new RRequirementResearch("hookshot_claw"), new OutcomeSpawnItem(
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
				new RRequirementResearch("reagent_bag"), new OutcomeSpawnItem(new ItemStack(NostrumItems.reagentBag))));

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
				new RRequirementResearch("rune_bag"), new OutcomeSpawnItem(new ItemStack(NostrumItems.runeBag))));

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
				new RRequirementResearch("mage_staff"), new OutcomeSpawnItem(new ItemStack(NostrumItems.mageStaff))));

		// Thanos Staff
		registry.register(RitualRecipe.createTier3("thanos_staff",
				new ItemStack(NostrumItems.thanosStaff), null,
				new ReagentType[] {
						ReagentType.GRAVE_DUST, ReagentType.GRAVE_DUST, ReagentType.GRAVE_DUST, ReagentType.MANI_DUST },
				Ingredient.fromItems(NostrumItems.thanoPendant),
				new Ingredient[] { Ingredient.fromTag(NostrumTags.Items.CrystalSmall),
						Ingredient.fromItems(NostrumItems.mageStaff), Ingredient.fromItems(NostrumItems.mageStaff),
						Ingredient.fromTag(NostrumTags.Items.CrystalSmall) },
				new RRequirementResearch("thanos_staff"), new OutcomeSpawnItem(new ItemStack(NostrumItems.thanosStaff))));

		// Lore Table
		registry.register(RitualRecipe.createTier3("lore_table", new ItemStack(NostrumBlocks.loreTable),
				null,
				new ReagentType[] {
						ReagentType.MANI_DUST, ReagentType.GINSENG, ReagentType.CRYSTABLOOM, ReagentType.MANI_DUST },
				Ingredient.fromTag(NostrumTags.Items.CrystalSmall),
				new Ingredient[] { Ingredient.fromTag(ItemTags.PLANKS),
						Ingredient.fromItems(Items.PAPER), Ingredient.fromItems(Blocks.CRAFTING_TABLE),
						Ingredient.fromTag(ItemTags.PLANKS) },
				new RRequirementResearch("loretable"), new OutcomeSpawnItem(new ItemStack(NostrumBlocks.loreTable))));

		// Modification Table
		registry
				.register(RitualRecipe.createTier3("modification_table", new ItemStack(NostrumBlocks.modificationTable),
						null, new ReagentType[] { ReagentType.MANI_DUST, ReagentType.BLACK_PEARL,
								ReagentType.CRYSTABLOOM, ReagentType.BLACK_PEARL },
						Ingredient.fromTag(NostrumTags.Items.CrystalLarge),
						new Ingredient[] { Ingredient.fromTag(ItemTags.PLANKS),
								Ingredient.fromItems(Items.PAPER), Ingredient.fromItems(NostrumBlocks.loreTable),
								Ingredient.fromTag(ItemTags.PLANKS) },
						new RRequirementResearch("modification_table"),
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
						new RRequirementResearch("teleportrune"),
						new OutcomeSpawnItem(new ItemStack(NostrumBlocks.teleportRune, 2))));

		// Putter
		registry
				.register(RitualRecipe.createTier2("putter", new ItemStack(NostrumBlocks.putterBlock), null,
						new ReagentType[] { ReagentType.MANDRAKE_ROOT, ReagentType.SPIDER_SILK, ReagentType.BLACK_PEARL,
								ReagentType.BLACK_PEARL },
						Ingredient.fromItems(Blocks.DROPPER), new RRequirementResearch("putter"),
						new OutcomeSpawnItem(new ItemStack(NostrumBlocks.putterBlock))));

		// Active Hopper
		registry.register(RitualRecipe.createTier3("active_hopper",
				new ItemStack(NostrumBlocks.activeHopper), null, new ReagentType[] { ReagentType.MANDRAKE_ROOT,
						ReagentType.SPIDER_SILK, ReagentType.GINSENG, ReagentType.CRYSTABLOOM },
				Ingredient.fromItems(Blocks.HOPPER),
				new Ingredient[] { Ingredient.fromStacks(new ItemStack(Blocks.HOPPER)), Ingredient.fromTag(Tags.Items.STORAGE_BLOCKS_REDSTONE),
						Ingredient.fromStacks(new ItemStack(Blocks.HOPPER)), Ingredient.fromStacks(new ItemStack(Blocks.HOPPER)) },
				new RRequirementResearch("active_hopper"),
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
				new RRequirementResearch("item_duct"), new OutcomeSpawnItem(new ItemStack(NostrumBlocks.itemDuct, 16))));

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
				new RRequirementResearch("magicfacade"), new OutcomeSpawnItem(new ItemStack(NostrumBlocks.mimicFacade, 8))));

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
				new RRequirementResearch("magicfacade"), new OutcomeSpawnItem(new ItemStack(NostrumBlocks.mimicDoor, 1))));

		// Dragon revive
		registry
				.register(RitualRecipe.createTier3("revive_soulbound_pet_dragon",
						new ItemStack(NostrumItems.dragonSoulItem), null, new ReagentType[] { ReagentType.GRAVE_DUST,
								ReagentType.MANDRAKE_ROOT, ReagentType.CRYSTABLOOM, ReagentType.MANI_DUST },
						Ingredient.fromItems(NostrumItems.dragonSoulItem),
						new Ingredient[] { Ingredient.EMPTY, Ingredient.fromTag(NostrumTags.Items.CrystalMedium),
								Ingredient.fromTag(Tags.Items.EGGS), Ingredient.EMPTY },
						new RRequirementResearch("soulbound_pets"), new OutcomeReviveSoulboundPet()));

		// Wolf revive
		registry
				.register(RitualRecipe.createTier3("revive_soulbound_pet_wolf",
						new ItemStack(NostrumItems.arcaneWolfSoulItem), null, new ReagentType[] { ReagentType.GRAVE_DUST,
								ReagentType.MANDRAKE_ROOT, ReagentType.CRYSTABLOOM, ReagentType.MANI_DUST },
						Ingredient.fromItems(NostrumItems.arcaneWolfSoulItem),
						new Ingredient[] { Ingredient.EMPTY, Ingredient.fromTag(NostrumTags.Items.CrystalMedium),
								Ingredient.fromTag(Tags.Items.EGGS), Ingredient.EMPTY },
						new RRequirementResearch("soulbound_pets"), new OutcomeReviveSoulboundPet()));

		// Soul dagger
		registry.register(RitualRecipe.createTier3("spawn_soul_dagger",
				new ItemStack(NostrumItems.soulDagger), EMagicElement.FIRE, new ReagentType[] { ReagentType.SKY_ASH,
						ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST, ReagentType.MANDRAKE_ROOT },
				Ingredient.fromItems(Items.END_CRYSTAL),
				new Ingredient[] { Ingredient.fromItems(AspectedWeapon.get(EMagicElement.WIND, AspectedWeapon.Type.NOVICE)),
						Ingredient.fromItems(AspectedWeapon.get(EMagicElement.LIGHTNING, AspectedWeapon.Type.NOVICE)),
						Ingredient.fromTag(NostrumTags.Items.SlabFierce),
						Ingredient.fromItems(AspectedWeapon.get(EMagicElement.ICE, AspectedWeapon.Type.NOVICE)) },
				new RRequirementResearch("soul_daggers"),
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
				new RRequirementResearch("wolf_transformation"), new OutcomeApplyTransformation(20 * 60, (e) -> {
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
						new RRequirementResearch("paradox_mirrors"),
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
								new RRequirementResearch("mana_armor"),
								new OutcomeSpawnItem(new ItemStack(NostrumBlocks.manaArmorerBlock, 1))));

//		registry.register(
//				RitualRecipe.createTier2("ritual.form_obelisk.name", EMagicElement.ENDER,
//					new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK},
//					center, outcome)
//				);
	}

	private static IReward[] wrapAttribute(AwardType type, float val) {
		return new IReward[] { new AttributeReward(type, val) };
	}

	private static void registerDefaultQuests() {
		/*
		 * String key, QuestType type, int reqLevel, int reqControl, int reqTechnique,
		 * int reqFinesse, String[] parentKeys, IObjective objective, IReward[] rewards
		 */
		new NostrumQuest("start", QuestType.REGULAR, 0, 0, 0, 0, null, null, null,
				wrapAttribute(AwardType.MANA, 0.0500f));
		new NostrumQuest("lvl1", QuestType.REGULAR, 2, 0, 0, 0, new String[] { "start" }, null, null,
				wrapAttribute(AwardType.MANA, 0.010f));
		new NostrumQuest("lvl2-fin", QuestType.REGULAR, 3, 0, 0, 1, new String[] { "lvl1" }, null, null,
				wrapAttribute(AwardType.REGEN, 0.0050f));
		new NostrumQuest("lvl2-con", QuestType.REGULAR, 3, 1, 0, 0, new String[] { "lvl1" }, null, null,
				wrapAttribute(AwardType.COST, -0.005f));
//		new NostrumQuest("lvl2", QuestType.REGULAR, 3, 0, 0, 0, new String[] { "lvl2-fin", "lvl2-con" }, null, null,
//				new IReward[] { new TriggerReward(AtFeetTrigger.instance()) });
		new NostrumQuest("lvl3", QuestType.CHALLENGE, 4, 0, 0, 0, new String[] { "lvl2-fin", "lvl2-con" }, null,
				new ObjectiveRitual("magic_token"), wrapAttribute(AwardType.MANA, 0.005f));
		new NostrumQuest("lvl4", QuestType.CHALLENGE, 5, 0, 0, 0, new String[] { "lvl3" }, null,
				new ObjectiveRitual("spell_binding"), new IReward[] { new AlterationReward(EAlteration.INFLICT) });

		// LVL-finesse tree
		new NostrumQuest("lvl6-fin", QuestType.REGULAR, 6, 0, 0, 3, new String[] { "lvl4" }, null, null,
				wrapAttribute(AwardType.REGEN, 0.005f));
		new NostrumQuest("lvl7-fin", QuestType.REGULAR, 7, 0, 0, 4, new String[] { "lvl6-fin" }, null, null,
				wrapAttribute(AwardType.MANA, 0.010f));
		new NostrumQuest("lvl7-fin7", QuestType.REGULAR, 7, 0, 0, 7, new String[] { "lvl7-fin" }, null, null,
				wrapAttribute(AwardType.REGEN, 0.010f));
		new NostrumQuest("lvl10-fin10", QuestType.CHALLENGE, 10, 0, 0, 10, new String[] { "lvl7-fin7" }, null,
				new ObjectiveSpellCast().numTriggers(10).requiredElement(EMagicElement.ICE),
				new IReward[] { new AlterationReward(EAlteration.SUPPORT) });

		// LVL-control tree
		new NostrumQuest("lvl6-con", QuestType.REGULAR, 6, 3, 0, 0, new String[] { "lvl4" }, null, null,
				wrapAttribute(AwardType.COST, -0.005f));
		new NostrumQuest("lvl7-con", QuestType.REGULAR, 7, 4, 0, 0, new String[] { "lvl6-con" }, null, null,
				wrapAttribute(AwardType.COST, -0.005f));
		new NostrumQuest("lvl7-con7", QuestType.REGULAR, 7, 7, 0, 0, new String[] { "lvl7-con" }, null, null,
				wrapAttribute(AwardType.COST, -0.010f));
		new NostrumQuest("lvl10-con10", QuestType.CHALLENGE, 10, 10, 0, 0, new String[] { "lvl7-con7" }, null,
				new ObjectiveSpellCast().numElems(10).requiredElement(EMagicElement.EARTH),
				new IReward[] { new AlterationReward(EAlteration.RESIST) });

		// LVL main tree
		new NostrumQuest("lvl7", QuestType.CHALLENGE, 7, 0, 0, 0, new String[] { "lvl6-con", "lvl6-fin" }, null,
				new ObjectiveRitual("kani"), wrapAttribute(AwardType.MANA, 0.020f));
		new NostrumQuest("lvl8", QuestType.CHALLENGE, 8, 0, 0, 0, new String[] { "lvl7" }, null, null,
				new IReward[] { new AlterationReward(EAlteration.CORRUPT) });
		new NostrumQuest("lvl8-fin3", QuestType.REGULAR, 8, 0, 0, 3, new String[] { "lvl7" }, null, null,
				wrapAttribute(AwardType.COST, -0.005f));
		new NostrumQuest("lvl8-fin5", QuestType.REGULAR, 8, 0, 0, 5, new String[] { "lvl7" }, null, null,
				wrapAttribute(AwardType.MANA, 0.020f));
		new NostrumQuest("lvl10-fin6", QuestType.REGULAR, 10, 0, 0, 6, new String[] { "lvl8-fin5" }, null, null,
				wrapAttribute(AwardType.REGEN, 0.100f));
		new NostrumQuest("lvl8-con3", QuestType.REGULAR, 8, 3, 0, 0, new String[] { "lvl7" }, null, null,
				wrapAttribute(AwardType.REGEN, 0.005f));
		new NostrumQuest("lvl8-con5", QuestType.REGULAR, 8, 5, 0, 0, new String[] { "lvl7" }, null, null,
				wrapAttribute(AwardType.MANA, 0.040f));
		new NostrumQuest("lvl10-con6", QuestType.REGULAR, 10, 6, 0, 0, new String[] { "lvl8-con5" }, null, null,
				wrapAttribute(AwardType.COST, -0.050f));
		new NostrumQuest("lvl10", QuestType.REGULAR, 10, 0, 0, 0, new String[] { "lvl8-con3", "lvl8-fin3" }, null, null,
				wrapAttribute(AwardType.MANA, 0.100f));
//		new NostrumQuest("lvl12", QuestType.REGULAR, 12, 0, 0, 0, new String[] { "lvl10" }, null, null,
//				new IReward[] { new TriggerReward(AuraTrigger.instance()) });

		new NostrumQuest("con1", QuestType.REGULAR, 0, 1, // Control
				0, // Technique
				0, // Finesse
				new String[] { "start" }, null, null, wrapAttribute(AwardType.COST, -0.002f));
		new NostrumQuest("con2", QuestType.REGULAR, 0, 2, // Control
				0, // Technique
				0, // Finesse
				new String[] { "con1" }, null, null, wrapAttribute(AwardType.MANA, 0.010f));
		new NostrumQuest("con7", QuestType.CHALLENGE, 0, 7, // Control
				0, // Technique
				0, // Finesse
				new String[] { "con2" }, null, new ObjectiveRitual("koid"), wrapAttribute(AwardType.COST, -0.050f));
		new NostrumQuest("con7-tec1", QuestType.CHALLENGE, 0, 7, // Control
				1, // Technique
				0, // Finesse
				new String[] { "con7", "con6-tec3" }, null,
				new ObjectiveSpellCast().numElems(6).requiredElement(EMagicElement.EARTH),
				new IReward[] { new AlterationReward(EAlteration.RUIN) });
		new NostrumQuest("con3-tec2", QuestType.REGULAR, 0, 3, // Control
				2, // Technique
				0, // Finesse
				new String[] { "con2" }, null, null, wrapAttribute(AwardType.REGEN, 0.005f));
		new NostrumQuest("con5-tec2", QuestType.REGULAR, 0, 5, // Control
				2, // Technique
				0, // Finesse
				new String[] { "con3-tec2" }, null, null, wrapAttribute(AwardType.COST, -0.010f));
		new NostrumQuest("con5-tec3", QuestType.REGULAR, 0, 5, // Control
				3, // Technique
				0, // Finesse
				new String[] { "con5-tec2", "con1-tec3" }, null, null, wrapAttribute(AwardType.COST, -0.015f));
		new NostrumQuest("con5-tec4", QuestType.REGULAR, 0, 5, // Control
				4, // Technique
				0, // Finesse
				new String[] { "con5-tec3" }, null, null, wrapAttribute(AwardType.COST, -0.015f));
		new NostrumQuest("con6-tec3", QuestType.REGULAR, 0, 6, // Control
				3, // Technique
				0, // Finesse
				new String[] { "con5-tec3" }, null, null, wrapAttribute(AwardType.REGEN, 0.005f));
		new NostrumQuest("con6-tec4", QuestType.CHALLENGE, 0, 6, // Control
				4, // Technique
				0, // Finesse
				new String[] { "con6-tec3", "con5-tec4" }, null, new ObjectiveKill(EntityGolem.class, "Golem", 30),
				new IReward[] { new AlterationReward(EAlteration.SUMMON) });
		new NostrumQuest("con1-tec2", QuestType.REGULAR, 0, 1, // Control
				2, // Technique
				0, // Finesse
				new String[] { "con1" }, null, null, wrapAttribute(AwardType.COST, -0.008f));
		new NostrumQuest("con1-tec3", QuestType.CHALLENGE, 0, 1, // Control
				3, // Technique
				0, // Finesse
				new String[] { "con1-tec2" }, null,
				new ObjectiveSpellCast().numElems(3).requiredElement(EMagicElement.LIGHTNING),
				wrapAttribute(AwardType.MANA, 0.030f));
//		new NostrumQuest("con2-tec3", QuestType.CHALLENGE, 0, 2, // Control
//				3, // Technique
//				0, // Finesse
//				new String[] { "con1-tec2" }, null, null, new IReward[] { new TriggerReward(WallTrigger.instance()) });
		new NostrumQuest("con1-tec5", QuestType.REGULAR, 0, 1, // Control
				5, // Technique
				0, // Finesse
				new String[] { "con1-tec3", "tec3" }, null, null, wrapAttribute(AwardType.COST, -0.005f));

		new NostrumQuest("tec1", QuestType.REGULAR, 0, 0, // Control
				1, // Technique
				0, // Finesse
				new String[] { "start" }, null, null, wrapAttribute(AwardType.MANA, 0.01f));
//		new NostrumQuest("tec3", QuestType.CHALLENGE, 0, 0, // Control
//				3, // Technique
//				0, // Finesse
//				new String[] { "con1-tec2", "fin1-tec2" }, null, null,
//				new IReward[] { new TriggerReward(SeekingBulletTrigger.instance()) });
		new NostrumQuest("tec7", QuestType.CHALLENGE, 0, 0, // Control
				7, // Technique
				0, // Finesse
				new String[] { "con1-tec5", "fin1-tec5" }, null, new ObjectiveRitual("vani"),
				new IReward[] { new AlterationReward(EAlteration.ENCHANT) });

		new NostrumQuest("fin1", QuestType.REGULAR, 0, 0, // Control
				0, // Technique
				1, // Finesse
				new String[] { "start" }, null, null, wrapAttribute(AwardType.REGEN, 0.002f));
		new NostrumQuest("fin3", QuestType.CHALLENGE, 0, 0, // Control
				0, // Technique
				3, // Finesse
				new String[] { "fin1" }, null,
				new ObjectiveSpellCast().numTriggers(3).requiredAlteration(EAlteration.INFLICT),
				wrapAttribute(AwardType.REGEN, 0.008f));
		new NostrumQuest("fin5", QuestType.REGULAR, 0, 0, // Control
				0, // Technique
				5, // Finesse
				new String[] { "fin3" }, null, null, wrapAttribute(AwardType.MANA, 0.020f));
		new NostrumQuest("fin7", QuestType.REGULAR, 0, 0, // Control
				0, // Technique
				7, // Finesse
				new String[] { "fin5" }, null, null, wrapAttribute(AwardType.REGEN, 0.075f));
//		new NostrumQuest("fin2-tec1", QuestType.CHALLENGE, 0, 0, // Control
//				1, // Technique
//				2, // Finesse
//				new String[] { "fin1" }, null, null, new IReward[] { new TriggerReward(CasterTrigger.instance()) });
		new NostrumQuest("fin5-tec2", QuestType.CHALLENGE, 0, 0, // Control
				2, // Technique
				5, // Finesse
				new String[] { "fin5", "fin2-tec3" }, null, new ObjectiveSpellCast().requiredShape(AoEShape.instance()),
				new IReward[] { new AlterationReward(EAlteration.GROWTH) });
		new NostrumQuest("fin1-tec2", QuestType.REGULAR, 0, 0, // Control
				2, // Technique
				1, // Finesse
				new String[] { "fin1" }, null, null, wrapAttribute(AwardType.REGEN, 0.010f));
		new NostrumQuest("fin2-tec2", QuestType.REGULAR, 0, 0, // Control
				2, // Technique
				2, // Finesse
				new String[] { "fin1-tec2" }, null, null, wrapAttribute(AwardType.COST, -0.010f));
		new NostrumQuest("fin1-tec3", QuestType.CHALLENGE, 0, 0, // Control
				3, // Technique
				1, // Finesse
				new String[] { "fin1-tec2" }, null, new ObjectiveKill(EntityKoid.class, "Koid", 5),
				wrapAttribute(AwardType.MANA, 0.025f));
//    	new NostrumQuest("fin2-tec3", QuestType.CHALLENGE, 0,
//    			0, // Control
//    			3, // Technique
//    			2, // Finesse
//    			new String[]{"con1-tec2", "fin1-tec2"},
//    			null, null,
//    			new IReward[] {new TriggerReward(WallTrigger.instance())});
		new NostrumQuest("fin1-tec5", QuestType.REGULAR, 0, 0, // Control
				5, // Technique
				1, // Finesse
				new String[] { "fin1-tec3", "tec3" }, null, null, wrapAttribute(AwardType.REGEN, 0.050f));
//		new NostrumQuest("fin2-tec3", QuestType.REGULAR, 0, 0, // Control
//				3, // Technique
//				2, // Finesse
//				new String[] { "fin1-tec3", "fin2-tec5" }, null, null,
//				new IReward[] { new TriggerReward(MortarTrigger.instance()) });
//		;
		new NostrumQuest("fin3-tec3", QuestType.REGULAR, 0, 0, // Control
				3, // Technique
				3, // Finesse
				new String[] { "fin2-tec3" }, null, null, wrapAttribute(AwardType.MANA, 0.050f));
		new NostrumQuest("fin2-tec5", QuestType.REGULAR, 0, 0, // Control
				5, // Technique
				2, // Finesse
				new String[] { "fin1-tec5" }, null, null, wrapAttribute(AwardType.REGEN, 0.020f));
		new NostrumQuest("fin3-tec6", QuestType.CHALLENGE, 0, 0, // Control
				6, // Technique
				3, // Finesse
				new String[] { "fin2-tec5" }, null, new ObjectiveRitual("balanced_infusion"),
				new IReward[] { new AlterationReward(EAlteration.CONJURE) });

//    	new NostrumQuest("geogem", QuestType.CHALLENGE, 5,
//    			0, // Control
//    			0, // Technique
//    			0, // Finesse
//    			new String[0],
//    			null, new ObjectiveSpellCast().requiredElement(EMagicElement.EARTH),
//    			wrapAttribute(AwardType.COST, -0.020f))
//    		.offset(-3, 2);
//    	
//    	new NostrumQuest("geotoken", QuestType.CHALLENGE, 5,
//    			0, // Control
//    			0, // Technique
//    			0, // Finesse
//    			new String[] {"geogem"},
//    			null, new ObjectiveRitual("geogem"),
//    			wrapAttribute(AwardType.COST, -0.030f))
//    		.offset(-4, 2);
//    	
//    	new NostrumQuest("obelisk", QuestType.CHALLENGE, 10,
//    			0, // Control
//    			0, // Technique
//    			0, // Finesse
//    			new String[] {"geotoken"},
//    			null, new ObjectiveSpellCast().requiredElement(EMagicElement.ENDER)
//    			.requiredElement(EMagicElement.ENDER)
//    			.requiredElement(EMagicElement.ENDER),
//    			wrapAttribute(AwardType.MANA, 0.040f))
//    		.offset(-5, 6);
//    	
//    	new NostrumQuest("obelisk2", QuestType.REGULAR, 10,
//    			0, // Control
//    			0, // Technique
//    			0, // Finesse
//    			new String[] {"obelisk"},
//    			null, null,
//    			wrapAttribute(AwardType.MANA, 0.010f))
//    		.offset(-6, 6);
//    	
//    	new NostrumQuest("recall", QuestType.CHALLENGE, 10,
//    			0, // Control
//    			0, // Technique
//    			0, // Finesse
//    			new String[] {"geotoken"},
//    			null, new ObjectiveSpellCast().requiredElement(EMagicElement.WIND),
//    			wrapAttribute(AwardType.REGEN, 0.040f))
//    		.offset(-5, 8);
//    	
//    	new NostrumQuest("recall2", QuestType.REGULAR, 10,
//    			0, // Control
//    			0, // Technique
//    			0, // Finesse
//    			new String[] {"recall"},
//    			null, null,
//    			wrapAttribute(AwardType.REGEN, 0.010f))
//    		.offset(-6, 8);
//    	
//    	new NostrumQuest("boon", QuestType.CHALLENGE, 12,
//    			0, // Control
//    			0, // Technique
//    			0, // Finesse
//    			new String[0],
//    			null, new ObjectiveSpellCast().requiredAlteration(EAlteration.RESIST)
//    									.requiredAlteration(EAlteration.SUPPORT)
//    									.requiredAlteration(EAlteration.GROWTH),
//    			wrapAttribute(AwardType.REGEN, 0.100f))
//    		.offset(1, 10);

//    	new NostrumQuest("advanced_bags", QuestType.REGULAR, 5,
//    			0, // Control
//    			0, // Technique
//    			0, // Finesse
//    			new String[0],
//    			new String[] {ReagentBag.instance().getLoreKey()}, // required lore
//    			null,
//    			wrapAttribute(AwardType.MANA, 0.050f))
//    		.offset(3, 1);

//    	new NostrumQuest("hex", QuestType.CHALLENGE, 14,
//    			0, // Control
//    			0, // Technique
//    			0, // Finesse
//    			new String[] {"boon"},
//    			new ObjectiveSpellCast().requiredAlteration(EAlteration.INFLICT)
//    									.numElems(5),
//    			wrapAttribute(AwardType.REGEN, 0.050f))
//    		.offset(4, 10);

//    	new NostrumQuest("enchant", QuestType.REGULAR, 13,
//    			0, // Control
//    			0, // Technique
//    			0, // Finesse
//    			new String[] {"boon"},
//    			null, null,
//    			wrapAttribute(AwardType.MANA, 0.050f))
//    		.offset(4, 11);

//    	new NostrumQuest("mastery_orb", QuestType.REGULAR,
//    			3,
//    			0,
//    			0,
//    			0,
//    			new String[]{"lvl1"},
//    			null, null,
//    			wrapAttribute(AwardType.MANA, 0.0100f));
//    	
//    	new NostrumQuest("mirror_shield", QuestType.CHALLENGE, 8,
//    			1, // Control
//    			0, // Technique
//    			1, // Finesse
//    			new String[] {"belts"}, // Potentially dependent on bauble quests :)
//    			null,
//    			null,
//    			new IReward[]{new AttributeReward(AwardType.MANA, 0.010f)})
//    		.offset(3, 6);
//    	new NostrumQuest("true_mirror_shield", QuestType.CHALLENGE, 8,
//    			1, // Control
//    			0, // Technique
//    			1, // Finesse
//    			new String[] {"mirror_shield"},
//    			new String[] {NostrumItems.mirrorShield.getLoreKey()},
//    			null,
//    			new IReward[]{new AttributeReward(AwardType.MANA, 0.025f)})
//    		.offset(4, 6);
//    	new NostrumQuest("magic_armor", QuestType.CHALLENGE, 5,
//    			0,
//    			0,
//    			0,
//    			null,
//    			new String[] {NostrumItems.mageStaff.getLoreKey()},
//    			null,
//    			new IReward[]{new AttributeReward(AwardType.MANA, 0.025f)})
//    		.offset(-4, 3);

//    	new NostrumQuest("con", QuestType.REGULAR, 0,
//    			0, // Control
//    			0, // Technique
//    			0, // Finesse
//    			new String[]{"lvl1"},
//    			null,
//    			wrapAttribute(AwardType.COST, -0.005f));

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

		NostrumResearch.startBuilding().parent("rituals").hiddenParent("magic_token").lore(EntityKoid.KoidLore.instance())
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
				.lore(EntityKoid.KoidLore.instance()).lore(EntityWisp.WispLoreTag.instance())
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
						SpellRune.getRune(SingleShape.instance()));

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

		NostrumResearch.startBuilding().parent("enchanted_armor").lore(EntityTameDragonRed.TameRedDragonLore.instance())
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
				.reference("ritual::form_eldrich_pendant", "ritual.form_eldrich_pendant.name").build("stat_items",
						NostrumResearchTab.ADVANCED_MAGICA, Size.GIANT, -2, -1, true,
						new ItemStack(NostrumItems.skillPendant));

		NostrumResearch.startBuilding().parent("stat_items")
				.reference("ritual::form_primordial_mirror", "ritual.form_primordial_mirror.name")
				.build("stat_items_adv", NostrumResearchTab.ADVANCED_MAGICA, Size.NORMAL, -2, 0, true,
						new ItemStack(NostrumItems.skillMirror));

		NostrumResearch.startBuilding().parent("stat_items_adv").lore(NostrumItems.dragonEggFragment)
				.lore(NostrumItems.skillPendant)
				.reference("ritual::form_primordial_mirror", "ritual.form_primordial_mirror.name")
				.build("stat_items_wing", NostrumResearchTab.ADVANCED_MAGICA, Size.NORMAL, -2, 1, true,
						new ItemStack(NostrumItems.resourceDragonWing));

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
    	SpellShape.register(SingleShape.instance());
    	SpellShape.register(AoEShape.instance());
    	SpellShape.register(ChainShape.instance());
    }
    
	@SubscribeEvent(priority=EventPriority.HIGH)
    public static void registerTriggers(RegistryEvent.Register<Item> event) {
		// Note: these are happening in the register<item> phase because they drive what items get
		// generated!
    	SpellTrigger.register(SelfTrigger.instance());
    	SpellTrigger.register(TouchTrigger.instance());
    	SpellTrigger.register(AITargetTrigger.instance());
    	SpellTrigger.register(ProjectileTrigger.instance());
    	SpellTrigger.register(BeamTrigger.instance());
    	SpellTrigger.register(DelayTrigger.instance());
    	SpellTrigger.register(ProximityTrigger.instance());
    	SpellTrigger.register(HealthTrigger.instance());
    	SpellTrigger.register(FoodTrigger.instance());
    	SpellTrigger.register(ManaTrigger.instance());
    	SpellTrigger.register(DamagedTrigger.instance());
    	SpellTrigger.register(OtherTrigger.instance());
    	SpellTrigger.register(MagicCutterTrigger.instance());
    	SpellTrigger.register(MagicCyclerTrigger.instance());
    	SpellTrigger.register(SeekingBulletTrigger.instance());
    	SpellTrigger.register(WallTrigger.instance());
    	SpellTrigger.register(MortarTrigger.instance());
    	SpellTrigger.register(FieldTrigger.instance());
    	SpellTrigger.register(AtFeetTrigger.instance());
    	SpellTrigger.register(AuraTrigger.instance());
    	SpellTrigger.register(CasterTrigger.instance());
    }
	
    @SubscribeEvent
    public static void registerEnchantments(RegistryEvent.Register<Enchantment> event) {
    	event.getRegistry().register(EnchantmentManaRecovery.instance());
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
