package com.smanzana.nostrummagica;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.blocks.NostrumBlocks;
import com.smanzana.nostrummagica.blocks.NostrumPortal;
import com.smanzana.nostrummagica.blocks.TemporaryTeleportationPortal;
import com.smanzana.nostrummagica.capabilities.IManaArmor;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.capabilities.ManaArmorAttributeProvider;
import com.smanzana.nostrummagica.capabilities.NostrumMagicAttributeProvider;
import com.smanzana.nostrummagica.capabilities.INostrumMagic.ElementalMastery;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf.WolfTameLore;
import com.smanzana.nostrummagica.entity.EntityKoid;
import com.smanzana.nostrummagica.entity.EntityWisp;
import com.smanzana.nostrummagica.entity.IEntityPet;
import com.smanzana.nostrummagica.entity.IMultiPartEntityPart;
import com.smanzana.nostrummagica.entity.ITameableEntity;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.entity.dragon.EntityTameDragonRed;
import com.smanzana.nostrummagica.entity.dragon.ITameDragon;
import com.smanzana.nostrummagica.entity.golem.EntityGolem;
import com.smanzana.nostrummagica.entity.tasks.FollowOwnerAdvancedGoal;
import com.smanzana.nostrummagica.entity.tasks.FollowOwnerGenericGoal;
import com.smanzana.nostrummagica.entity.tasks.PetTargetGoal;
import com.smanzana.nostrummagica.integration.aetheria.AetheriaClientProxy;
import com.smanzana.nostrummagica.integration.aetheria.AetheriaProxy;
import com.smanzana.nostrummagica.integration.curios.CuriosClientProxy;
import com.smanzana.nostrummagica.integration.curios.CuriosProxy;
import com.smanzana.nostrummagica.integration.curios.items.NostrumCurios;
import com.smanzana.nostrummagica.integration.musica.MusicaClientProxy;
import com.smanzana.nostrummagica.integration.musica.MusicaProxy;
import com.smanzana.nostrummagica.items.DragonArmor;
import com.smanzana.nostrummagica.items.DragonArmor.DragonArmorMaterial;
import com.smanzana.nostrummagica.items.DragonArmor.DragonEquipmentSlot;
import com.smanzana.nostrummagica.items.EnchantedArmor;
import com.smanzana.nostrummagica.items.EnchantedWeapon;
import com.smanzana.nostrummagica.items.EssenceItem;
import com.smanzana.nostrummagica.items.MagicArmorBase;
import com.smanzana.nostrummagica.items.MagicCharm;
import com.smanzana.nostrummagica.items.NostrumItems;
import com.smanzana.nostrummagica.items.ReagentBag;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.items.WarlockSword;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy;
import com.smanzana.nostrummagica.listeners.ManaArmorListener;
import com.smanzana.nostrummagica.listeners.PlayerListener;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.pet.PetCommandManager;
import com.smanzana.nostrummagica.pet.PetSoulRegistry;
import com.smanzana.nostrummagica.proxy.ClientProxy;
import com.smanzana.nostrummagica.proxy.CommonProxy;
import com.smanzana.nostrummagica.quests.NostrumQuest;
import com.smanzana.nostrummagica.quests.NostrumQuest.QuestType;
import com.smanzana.nostrummagica.quests.objectives.ObjectiveKill;
import com.smanzana.nostrummagica.quests.objectives.ObjectiveRitual;
import com.smanzana.nostrummagica.quests.objectives.ObjectiveSpellCast;
import com.smanzana.nostrummagica.quests.rewards.AlterationReward;
import com.smanzana.nostrummagica.quests.rewards.AttributeReward;
import com.smanzana.nostrummagica.quests.rewards.AttributeReward.AwardType;
import com.smanzana.nostrummagica.quests.rewards.IReward;
import com.smanzana.nostrummagica.quests.rewards.TriggerReward;
import com.smanzana.nostrummagica.research.NostrumResearch;
import com.smanzana.nostrummagica.research.NostrumResearch.NostrumResearchTab;
import com.smanzana.nostrummagica.research.NostrumResearch.Size;
import com.smanzana.nostrummagica.research.NostrumResearch.SpellSpec;
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
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.SpellRegistry;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.spells.components.shapes.AoEShape;
import com.smanzana.nostrummagica.spells.components.shapes.ChainShape;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.AtFeetTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.AuraTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.CasterTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.FieldTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.MortarTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.SeekingBulletTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.WallTrigger;
import com.smanzana.nostrummagica.spelltome.enhancement.SpellTomeEnhancement;
import com.smanzana.nostrummagica.trials.ShrineTrial;
import com.smanzana.nostrummagica.trials.TrialEarth;
import com.smanzana.nostrummagica.trials.TrialEnder;
import com.smanzana.nostrummagica.trials.TrialFire;
import com.smanzana.nostrummagica.trials.TrialIce;
import com.smanzana.nostrummagica.trials.TrialLightning;
import com.smanzana.nostrummagica.trials.TrialPhysical;
import com.smanzana.nostrummagica.trials.TrialWind;
import com.smanzana.nostrummagica.utils.Entities;
import com.smanzana.nostrummagica.world.NostrumKeyRegistry;
import com.smanzana.nostrummagica.world.NostrumLootHandler;
import com.smanzana.nostrummagica.world.dimension.NostrumDimensionMapper;
import com.smanzana.nostrummagica.world.dimension.NostrumEmptyDimension;

import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.boss.dragon.EnderDragonPartEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.Effects;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import top.theillusivec4.curios.api.CuriosAPI;

@Mod(NostrumMagica.MODID)
public class NostrumMagica {
	public static final String MODID = "nostrummagica";
	public static final String VERSION = "1.14.4-1.10.0";
	public static final Random rand = new Random();

	public static NostrumMagica instance;
	
	public final CommonProxy proxy;
	public final CuriosProxy curios;
	public final AetheriaProxy aetheria;
	//public final EnderIOProxy enderIO;
	public final MusicaProxy musica;

	public static ItemGroup creativeTab;
	public static ItemGroup enhancementTab;
	public static Logger logger = LogManager.getLogger(MODID);
	public static PlayerListener playerListener;
	public static MagicEffectProxy magicEffectProxy;
	public static ManaArmorListener manaArmorListener;

	// Cached references that have sketchy access rules. See uses in this file.
	private static SpellRegistry spellRegistry;
	private static NostrumDimensionMapper serverDimensionMapper;
	private static PetSoulRegistry petSoulRegistry;
	private static PetCommandManager petCommandManager;
	private static NostrumKeyRegistry worldKeys;

	public static boolean initFinished = false;
	
	public NostrumMagica() {
		instance = this;
		
		proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> CommonProxy::new);
		curios = DistExecutor.runForDist(() -> CuriosClientProxy::new, () -> CuriosProxy::new);
		aetheria = DistExecutor.runForDist(() -> AetheriaClientProxy::new, () -> AetheriaProxy::new);
		//enderIO = DistExecutor.runForDist(() -> EnderIOClientProxy::new, () -> EnderIOProxy::new);
		musica = DistExecutor.runForDist(() -> MusicaClientProxy::new, () -> MusicaProxy::new);
		
		playerListener = new PlayerListener();
		magicEffectProxy = new MagicEffectProxy();
		manaArmorListener = new ManaArmorListener();

		NostrumMagica.creativeTab = new ItemGroup(MODID) {
			@Override
			@OnlyIn(Dist.CLIENT)
			public ItemStack createIcon() {
				return new ItemStack(NostrumItems.spellTomeNovice);
			}
		};
		// NostrumItems.spellTomeNovice.setCreativeTab(NostrumMagica.creativeTab); TODO still need this?
		NostrumMagica.enhancementTab = new ItemGroup(MODID + "_enhancements") {
			@Override
			@OnlyIn(Dist.CLIENT)
			public ItemStack createIcon() {
				return new ItemStack(NostrumItems.spellTomePage);
			}
		};
		//NostrumItems.spellTomePage.setCreativeTab(NostrumMagica.enhancementTab); // TODO still need this?

		if (ModList.get().isLoaded(CuriosAPI.MODID)) {
			curios.enable();
		}
		if (ModList.get().isLoaded("nostrumaetheria")) {
			aetheria.enable();
		}
//		if (ModList.get().isLoaded("enderio") || ModList.get().isLoaded("enderio")) {
//			enderIO.enable();
//		}
		if (ModList.get().isLoaded("musica")) {
			musica.enable();
		}
		
		(new ModConfig()).register();

		FMLJavaModLoadingContext.get().getModEventBus().register(this);
		
		proxy.preinit();
		aetheria.preInit();
		curios.preInit();
		//enderIO.preInit();
		musica.preInit();
	}

	@SubscribeEvent
	public void commonSetup(FMLCommonSetupEvent event) {
		
		RitualRegistry.instance();

		SpellTomeEnhancement.initDefaultEnhancements();
		
		// Register rituals, quests, etc. after item and block init
		//registerDefaultRituals();
		registerDefaultQuests();
		registerDefaultTrials();
		registerDefaultResearch();

		new NostrumLootHandler();
		//DungeonRoomRegistry.instance().loadRegistryFromDisk(); Done in feature loading since it's required by that system and this is too late :(
		//NostrumDimensionMapper.registerDimensions();
		//NostrumDungeonStructure.initGens();

		proxy.init();
		aetheria.init();
		curios.init();
		//enderIO.init();
		musica.init();
	
		// Used to be two different mod init steps!
		
		proxy.postinit();
		aetheria.postInit();
		//curios.postInit();
		//enderIO.postInit();
		musica.postInit();
		
		proxy.registerWorldGen();

		initFinished = true;

		MinecraftForge.EVENT_BUS.register(this);
	}

	/**
	 * Convenience wrapper. Pulls out magic wrapper from an entity, if they have
	 * them
	 * 
	 * @param e The entity to pull off of
	 * @return The attributes, if they exist. Otherwise, returns null Get a null you
	 *         don't expect? Make sure the server and client configs match, AND that
	 *         the config includes all the mobs you won't to be tagged.
	 */
	public static INostrumMagic getMagicWrapper(Entity e) {
		if (e == null)
			return null;
		
		return e.getCapability(NostrumMagicAttributeProvider.CAPABILITY).orElse(null);
	}

	public static IManaArmor getManaArmor(Entity e) {
		if (e == null)
			return null;

		return e.getCapability(ManaArmorAttributeProvider.CAPABILITY).orElse(null);
	}

	public static ItemStack findTome(PlayerEntity entity, int tomeID) {
		// We look in mainhand first, then offhand, then just down
		// hotbar.
		for (ItemStack item : entity.inventory.mainInventory) {
			if (!item.isEmpty() && item.getItem() instanceof SpellTome)
				if (SpellTome.getTomeID(item) == tomeID)
					return item;
		}

		for (ItemStack item : entity.inventory.offHandInventory) {
			if (!item.isEmpty() && item.getItem() instanceof SpellTome)
				if (SpellTome.getTomeID(item) == tomeID)
					return item;
		}

		return ItemStack.EMPTY;
	}

	public static @Nonnull ItemStack getCurrentTome(PlayerEntity entity) {
		// We look in mainhand first, then offhand, then just down
		// hotbar.
		ItemStack tome = ItemStack.EMPTY;

		if (!entity.getHeldItemMainhand().isEmpty() && entity.getHeldItemMainhand().getItem() instanceof SpellTome) {
			tome = entity.getHeldItemMainhand();
		} else if (!entity.getHeldItemOffhand().isEmpty()
				&& entity.getHeldItemOffhand().getItem() instanceof SpellTome) {
			tome = entity.getHeldItemOffhand();
		} else {
			// hotbar is items 0-8
			int count = 0;
			for (ItemStack stack : entity.inventory.mainInventory) {
				if (!stack.isEmpty() && stack.getItem() instanceof SpellTome) {
					tome = stack;
					break;
				}

				count++;
				if (count > 8)
					break; // Just want first 9
			}
		}

		return tome;
	}

	public static Spell getCurrentSpell(PlayerEntity player) {
		List<Spell> spells = getSpells(player);
		if (spells == null || spells.isEmpty())
			return null;

		return spells.get(0);
	}

	public static int getReagentCount(PlayerEntity player, ReagentType type) {
		int count = 0;
		for (ItemStack item : player.inventory.mainInventory) {
			if (!item.isEmpty() && item.getItem() instanceof ReagentBag) {
				count += ReagentBag.getReagentCount(item, type);
			}
		}
		for (ItemStack item : player.inventory.offHandInventory) {
			if (!item.isEmpty() && item.getItem() instanceof ReagentBag) {
				count += ReagentBag.getReagentCount(item, type);
			}
		}
		for (ItemStack item : player.inventory.mainInventory) {
			if (!item.isEmpty() && item.getItem() instanceof ReagentItem && ReagentItem.FindType(item) == type) {
				count += item.getCount();
			}
		}
		for (ItemStack item : player.inventory.offHandInventory) {
			if (!item.isEmpty() && item.getItem() instanceof ReagentBag && ReagentItem.FindType(item) == type) {
				count += item.getCount();
			}
		}

		return count;
	}

	public static boolean removeReagents(PlayerEntity player, ReagentType type, int count) {
		if (getReagentCount(player, type) < count)
			return false;

		for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
			ItemStack item = player.inventory.getStackInSlot(i);
			if (item.isEmpty())
				continue;

			if (item.getItem() instanceof ReagentBag) {
				count = ReagentBag.removeCount(item, type, count);
			} else if (item.getItem() instanceof ReagentItem) {
				if (ReagentItem.FindType(item) == type) {
					if (item.getCount() > count) {
						item.shrink(count);
						count = 0;
						break;
					} else {
						count -= item.getCount();
						player.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
					}
				}
			}

			if (count == 0)
				break;
		}

		return count == 0;
	}

	public static List<Spell> getSpells(PlayerEntity entity) {
		if (entity == null)
			return null;

		// We just return the spells from the curernt tome.
		ItemStack tome = getCurrentTome(entity);

		if (tome.isEmpty())
			return null;

		return SpellTome.getSpells(tome);

	}

	public static List<NostrumQuest> getActiveQuests(PlayerEntity player) {
		return getActiveQuests(getMagicWrapper(player));
	}

	public static List<NostrumQuest> getActiveQuests(INostrumMagic attr) {
		List<NostrumQuest> list = new LinkedList<>();
		List<String> quests = attr.getCurrentQuests();

		if (quests != null && !quests.isEmpty())
			for (String quest : quests) {
				NostrumQuest q = NostrumQuest.lookup(quest);
				if (q != null)
					list.add(q);
			}

		return list;
	}

	public static List<NostrumQuest> getCompletedQuests(PlayerEntity player) {
		return getCompletedQuests(getMagicWrapper(player));
	}

	public static List<NostrumQuest> getCompletedQuests(INostrumMagic attr) {
		List<NostrumQuest> list = new LinkedList<>();
		List<String> quests = attr.getCompletedQuests();

		if (quests != null && !quests.isEmpty())
			for (String quest : quests) {
				NostrumQuest q = NostrumQuest.lookup(quest);
				if (q != null)
					list.add(q);
			}

		return list;
	}

	public static List<NostrumResearch> getCompletedResearch(PlayerEntity player) {
		return getCompletedResearch(getMagicWrapper(player));
	}

	public static List<NostrumResearch> getCompletedResearch(INostrumMagic attr) {
		List<NostrumResearch> list = new LinkedList<>();
		List<String> research = attr.getCompletedResearches();

		if (research != null && !research.isEmpty())
			for (String researchKey : research) {
				NostrumResearch r = NostrumResearch.lookup(researchKey);
				if (r != null)
					list.add(r);
			}

		return list;
	}

	@SubscribeEvent
	public void registerRituals(RegistryEvent.Register<RitualRecipe> event) {
		final IForgeRegistry<RitualRecipe> registry = event.getRegistry();
		
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
					Ingredient.fromStacks(alteration.getReagents().get(0)),
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
					public void spawn(World world, Vec3d pos, PlayerEntity invoker, ItemStack centerItem) {
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
		Ingredient extra = (curios.isEnabled() ? Ingredient.fromItems(NostrumCurios.smallRibbon)
				: Ingredient.fromTag(NostrumTags.Items.CrystalSmall));

		registry.register(RitualRecipe.createTier3("mirror_shield",
				new ItemStack(NostrumItems.mirrorShield), null, new ReagentType[] { ReagentType.MANI_DUST,
						ReagentType.MANDRAKE_ROOT, ReagentType.SPIDER_SILK, ReagentType.BLACK_PEARL },
				Ingredient.fromItems(Items.SHIELD),
				new Ingredient[] { extra, Ingredient.fromTag(NostrumTags.Items.CrystalLarge),
						Ingredient.fromTag(Tags.Items.GLASS_PANES), extra },
				new RRequirementResearch("mirror_shield"),
				new OutcomeSpawnItem(new ItemStack(NostrumItems.mirrorShield))));

		extra = (curios.isEnabled() ? Ingredient.fromItems(NostrumCurios.mediumRibbon)
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
			if (!EnchantedArmor.isArmorElement(elem)) {
				continue;
			}
			
			for (EnchantedArmor.Type type : EnchantedArmor.Type.values()) {
				if (type == EnchantedArmor.Type.TRUE) {
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
					outcome = new ItemStack(EnchantedArmor.get(elem, slot, type));
					name = "spawn_enchanted_armor";
					regName = "spawn_enchanted_armor_" + elem.name().toLowerCase() + "_" + slot.name().toLowerCase() + "_" + type.name().toLowerCase();
					research = "enchanted_armor";
					if (type == EnchantedArmor.Type.NOVICE) {
						input = Ingredient.fromItems(MagicArmorBase.get(slot));
					} else {
						input = Ingredient.fromItems(EnchantedArmor.get(elem, slot, type.getPrev()));
					}
					
					essence = Ingredient.fromStacks(EssenceItem.getEssence(elem, 1)); // Would be cool to make this tag...
					if (type == EnchantedArmor.Type.NOVICE) {
						gem = Ingredient.fromTag(NostrumTags.Items.CrystalSmall);
					} else if (type == EnchantedArmor.Type.ADEPT) {
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

				final boolean isTrue = EnchantedArmor.isArmorElement(elem);
				final Ingredient augment = (isTrue ? Ingredient.fromTag(NostrumTags.Items.SlabKind)
						: Ingredient.fromTag(NostrumTags.Items.SlabFierce));

				final Ingredient input = Ingredient.fromItems(EnchantedArmor.get(isTrue ? elem : elem.getOpposite(), slot, EnchantedArmor.Type.MASTER));
				final String name = "spawn_enchanted_armor";
				final String regName = "spawn_enchanted_armor_" + elem.name().toLowerCase() + "_" + slot.name().toLowerCase() + "_" + EnchantedArmor.Type.TRUE.name().toLowerCase();
				final String research = "enchanted_armor_adv";
				final Ingredient wings = (isTrue ? Ingredient.fromTag(NostrumTags.Items.DragonWing)
						: Ingredient.fromStacks(new ItemStack(Items.ELYTRA)));
				final IRitualOutcome outcome = (isTrue
						? new OutcomeSpawnItem(new ItemStack(EnchantedArmor.get(elem, slot, EnchantedArmor.Type.TRUE)))
						: new OutcomeSpawnItem(new ItemStack(EnchantedArmor.get(elem, slot, EnchantedArmor.Type.TRUE)),
								new ItemStack(Items.ELYTRA)));

				registry
						.register(RitualRecipe.createTier3(regName, name, new ItemStack(EnchantedArmor.get(elem, slot, EnchantedArmor.Type.TRUE)),
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
			if (!EnchantedWeapon.isWeaponElement(elem)) {
				continue;
			}
			
			for (EnchantedWeapon.Type type : EnchantedWeapon.Type.values()) {
				ItemStack outcome;
				Ingredient input;
				Ingredient gem;
				Ingredient essence;
				String name;
				String regName;
				String research;
				
				outcome = new ItemStack(EnchantedWeapon.get(elem, type));
				name = "spawn_enchanted_weapon";
				regName = "spawn_enchanted_weapon_" + elem.name().toLowerCase() + "_" + type.name().toLowerCase();
				research = "enchanted_weapons";
				if (type == EnchantedWeapon.Type.NOVICE) {
					input = Ingredient.fromItems(NostrumItems.magicSwordBase);
				} else {
					input = Ingredient.fromItems(EnchantedWeapon.get(elem, type.getPrev()));
				}
				
				essence = Ingredient.fromStacks(EssenceItem.getEssence(elem, 1)); // Would be cool to make this tag...
				if (type == EnchantedWeapon.Type.NOVICE) {
					gem = Ingredient.fromTag(NostrumTags.Items.CrystalSmall);
				} else if (type == EnchantedWeapon.Type.ADEPT) {
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
				new Ingredient[] { Ingredient.fromItems(EnchantedWeapon.get(EMagicElement.WIND, EnchantedWeapon.Type.NOVICE)),
						Ingredient.fromItems(EnchantedWeapon.get(EMagicElement.LIGHTNING, EnchantedWeapon.Type.NOVICE)),
						Ingredient.fromTag(NostrumTags.Items.SlabFierce),
						Ingredient.fromItems(EnchantedWeapon.get(EMagicElement.ICE, EnchantedWeapon.Type.NOVICE)) },
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
		new NostrumQuest("lvl2", QuestType.REGULAR, 3, 0, 0, 0, new String[] { "lvl2-fin", "lvl2-con" }, null, null,
				new IReward[] { new TriggerReward(AtFeetTrigger.instance()) });
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
				new IReward[] { new TriggerReward(FieldTrigger.instance()) });
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
		new NostrumQuest("lvl12", QuestType.REGULAR, 12, 0, 0, 0, new String[] { "lvl10" }, null, null,
				new IReward[] { new TriggerReward(AuraTrigger.instance()) });

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
		new NostrumQuest("con2-tec3", QuestType.CHALLENGE, 0, 2, // Control
				3, // Technique
				0, // Finesse
				new String[] { "con1-tec2" }, null, null, new IReward[] { new TriggerReward(WallTrigger.instance()) });
		new NostrumQuest("con1-tec5", QuestType.REGULAR, 0, 1, // Control
				5, // Technique
				0, // Finesse
				new String[] { "con1-tec3", "tec3" }, null, null, wrapAttribute(AwardType.COST, -0.005f));

		new NostrumQuest("tec1", QuestType.REGULAR, 0, 0, // Control
				1, // Technique
				0, // Finesse
				new String[] { "start" }, null, null, wrapAttribute(AwardType.MANA, 0.01f));
		new NostrumQuest("tec3", QuestType.CHALLENGE, 0, 0, // Control
				3, // Technique
				0, // Finesse
				new String[] { "con1-tec2", "fin1-tec2" }, null, null,
				new IReward[] { new TriggerReward(SeekingBulletTrigger.instance()) });
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
		new NostrumQuest("fin2-tec1", QuestType.CHALLENGE, 0, 0, // Control
				1, // Technique
				2, // Finesse
				new String[] { "fin1" }, null, null, new IReward[] { new TriggerReward(CasterTrigger.instance()) });
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
		new NostrumQuest("fin2-tec3", QuestType.REGULAR, 0, 0, // Control
				3, // Technique
				2, // Finesse
				new String[] { "fin1-tec3", "fin2-tec5" }, null, null,
				new IReward[] { new TriggerReward(MortarTrigger.instance()) });
		;
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
		ShrineTrial.setTrial(EMagicElement.FIRE, new TrialFire());
		ShrineTrial.setTrial(EMagicElement.ICE, new TrialIce());
		ShrineTrial.setTrial(EMagicElement.WIND, new TrialWind());
		ShrineTrial.setTrial(EMagicElement.EARTH, new TrialEarth());
		ShrineTrial.setTrial(EMagicElement.ENDER, new TrialEnder());
		ShrineTrial.setTrial(EMagicElement.LIGHTNING, new TrialLightning());
		ShrineTrial.setTrial(EMagicElement.PHYSICAL, new TrialPhysical());
	}

	private void registerDefaultResearch() {
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

		NostrumResearch.startBuilding().parent("mage_staff").parent("enchanted_weapons").hiddenParent("vani")
				.reference("ritual::spawn_warlock_sword", "ritual.spawn_warlock_sword.name").build("warlock_sword",
						NostrumResearchTab.OUTFITTING, Size.LARGE, 1, 1, true, new ItemStack(NostrumItems.warlockSword));

		NostrumResearch.startBuilding().parent("thanos_staff").parent("warlock_sword").hiddenParent("vani")
				.reference("ritual::spawn_soul_dagger", "ritual.spawn_soul_dagger.name").build("soul_daggers",
						NostrumResearchTab.OUTFITTING, Size.LARGE, 2, 1, true, new ItemStack(NostrumItems.soulDagger));

		NostrumResearch.startBuilding().parent("enchanted_armor")
				.reference("ritual::spawn_enchanted_weapon", "ritual.spawn_enchanted_weapon.name")
				.build("enchanted_weapons", NostrumResearchTab.OUTFITTING, Size.LARGE, -1, 1, true,
						new ItemStack(EnchantedWeapon.get(EMagicElement.WIND, EnchantedWeapon.Type.MASTER)));

		NostrumResearch.startBuilding().hiddenParent("rituals").quest("lvl4")
				.reference("ritual::spawn_enchanted_armor", "ritual.spawn_enchanted_armor.name")
				.build("enchanted_armor", NostrumResearchTab.OUTFITTING, Size.GIANT, -2, 0, true,
						new ItemStack(EnchantedArmor.get(EMagicElement.FIRE, EquipmentSlotType.CHEST, EnchantedArmor.Type.MASTER)));

		NostrumResearch.startBuilding().parent("enchanted_armor").hiddenParent("kind_infusion")
				.hiddenParent("fierce_infusion")
				.reference("ritual::spawn_enchanted_armor", "ritual.spawn_enchanted_armor.name")
				.build("enchanted_armor_adv", NostrumResearchTab.OUTFITTING, Size.LARGE, -1, 2, true,
						new ItemStack(EnchantedArmor.get(EMagicElement.ENDER, EquipmentSlotType.CHEST, EnchantedArmor.Type.MASTER)));

		NostrumResearch.startBuilding().parent("enchanted_armor").lore(EntityTameDragonRed.TameRedDragonLore.instance())
				.reference("ritual::craft_dragonarmor_body_iron", "ritual.craft_dragonarmor_body_iron.name")
				.build("dragon_armor", NostrumResearchTab.OUTFITTING, Size.LARGE, -1, 3, true,
						new ItemStack(DragonArmor.GetArmor(DragonEquipmentSlot.HELM, DragonArmorMaterial.IRON)));

		NostrumResearch.startBuilding().parent("enchanted_armor").parent(curios.isEnabled() ? "belts" : "origin")
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

		NostrumResearch.startBuilding().hiddenParent("kani").lore(IEntityPet.SoulBoundLore.instance())
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

	private List<Function<Integer, Integer>> researchReloadHooks = new LinkedList<>();

	public void reloadDefaultResearch() {
		NostrumResearch.ClearAllResearch();
		registerDefaultResearch();
		if (curios.isEnabled()) {
			curios.reinitResearch();
		}
		if (aetheria.isEnabled()) {
			aetheria.reinitResearch();
		}
//		if (enderIO.isEnabled()) {
//			enderIO.reinitResearch();
//		}

		for (Function<Integer, Integer> hook : researchReloadHooks) {
			hook.apply(0);
		}
		NostrumResearch.Validate();
	}

	public void registerResearchReloadHook(Function<Integer, Integer> hook) {
		this.researchReloadHooks.add(hook);
	}

	private static IReward[] wrapAttribute(AwardType type, float val) {
		return new IReward[] { new AttributeReward(type, val) };
	}

	/**
	 * Whether a quest is visible in the mirror by normal rules. This is either that
	 * one of the parents of the quest has been finished, OR that the quest has no
	 * parent but the conditions to take the quest are fulfilled. Note: If there are
	 * lore requirements and those aren't filled, this returns false even if the
	 * other two conditions are true.
	 * 
	 * @param player
	 * @param quest
	 * @return
	 */
	public static boolean getQuestAvailable(PlayerEntity player, NostrumQuest quest) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);

		if (attr == null)
			return false;

		// Check lore requirements
		if (quest.getLoreKeys() != null && quest.getLoreKeys().length != 0) {
			for (String lore : quest.getLoreKeys()) {
				ILoreTagged loreItem = LoreRegistry.instance().lookup(lore);
				if (loreItem != null) {
					if (!attr.hasLore(loreItem)) {
						return false;
					}
				}
			}
		}

		if (quest.getParentKeys() == null || quest.getParentKeys().length == 0) {
			return canTakeQuest(player, quest);
		}

		for (String parent : quest.getParentKeys()) {
			if (attr.getCompletedQuests().contains(parent))
				return true;
		}

		return false;
	}

	/**
	 * Checks whether all of the conditions required to start a quest have been
	 * fulfilled.
	 * 
	 * @param player
	 * @param quest
	 * @return
	 */
	public static boolean canTakeQuest(PlayerEntity player, NostrumQuest quest) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null)
			return false;

		// Check lore requirements
		if (quest.getLoreKeys() != null && quest.getLoreKeys().length != 0) {
			for (String lore : quest.getLoreKeys()) {
				ILoreTagged loreItem = LoreRegistry.instance().lookup(lore);
				if (loreItem != null) {
					if (!attr.hasLore(loreItem)) {
						return false;
					}
				}
			}
		}

		String[] parents = quest.getParentKeys();
		if (parents != null && parents.length > 0) {
			List<String> completed = attr.getCompletedQuests();
			boolean found = false;
			for (String parent : parents) {
				for (String comp : completed) {
					if (comp.equalsIgnoreCase(parent)) {
						found = true;
						break;
					}
				}

				if (found) {
					break;
				}
			}

			if (!found) {
				return false;
			}
		}

		return quest.getReqLevel() <= attr.getLevel() && quest.getReqControl() <= attr.getControl()
				&& quest.getReqTechnique() <= attr.getTech() && quest.getReqFinesse() <= attr.getFinesse();
	}

	public static boolean getResearchVisible(PlayerEntity player, NostrumResearch research) {
		// Visible if any of parents is finished (unless hidden)
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null)
			return false;

		List<String> finished = attr.getCompletedResearches();
		if (finished.contains(research.getKey())) {
			return true;
		}

		if (research.isHidden()) {
			return canPurchaseResearch(player, research);
		}

		String[] parents = research.getAllParents();
		if (parents == null || parents.length == 0) {
			return true;
		}

		if (finished == null || finished.isEmpty()) {
			return false;
		}

		for (String parent : parents) {
			if (finished.contains(parent)) {
				return true;
			}
		}

		return false;
	}

	public static boolean canPurchaseResearch(PlayerEntity player, NostrumResearch research) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null)
			return false;

		// Check quest requirements
		if (research.getRequiredQuests() != null && research.getRequiredQuests().length != 0) {
			List<String> completedQuests = attr.getCompletedQuests();
			for (String questKey : research.getRequiredQuests()) {
				if (!completedQuests.contains(questKey)) {
					return false;
				}
			}
		}

		// Check lore requirements
		if (research.getRequiredLore() != null && research.getRequiredLore().length != 0) {
			for (String lore : research.getRequiredLore()) {
				ILoreTagged loreItem = LoreRegistry.instance().lookup(lore);
				if (loreItem != null) {
					if (!attr.hasLore(loreItem)) {
						return false;
					}
				}
			}
		}

		// Check spell requirements
		if (research.getRequiredSpellComponents() != null && research.getRequiredSpellComponents().length != 0) {
			for (SpellSpec spec : research.getRequiredSpellComponents()) {
				if (spec.element == null && spec.alteration == null) {
					continue;
				} else if (spec.element == null) {
					// Just alteration
					if (!attr.getAlterations().containsKey(spec.alteration)
							|| !attr.getAlterations().get(spec.alteration)) {
						return false;
					}
				} else if (spec.alteration == null) {
					// Just element
					if (!attr.getKnownElements().containsKey(spec.element)
							|| !attr.getKnownElements().get(spec.element)) {
						return false;
					}
				} else {
					// Both. Check that it's actually been cast, not just unlocked :)
					if (!attr.hasKnowledge(spec.element, spec.alteration)) {
						return false;
					}
				}
			}
		}

		// ALL parents must be completed before research can be taken
		String[] parents = research.getAllParents();
		if (parents != null && parents.length > 0) {
			List<String> finished = attr.getCompletedResearches();
			if (finished == null || finished.isEmpty()) {
				return false;
			}

			for (String parent : parents) {
				if (!finished.contains(parent)) {
					return false;
				}
			}
		}

		return true;
	}

	public static int getMaxComponents(INostrumMagic attr) {
		if (attr.isUnlocked())
			return 2 * (attr.getTech() + 1);

		return 0;
	}

	public static int getMaxTriggers(INostrumMagic attr) {
		if (attr.isUnlocked())
			return 1 + (attr.getFinesse());

		return 0;
	}

	public static int getMaxElements(INostrumMagic attr) {
		if (attr.isUnlocked())
			return 1 + attr.getControl() * 3;

		return 0;
	}

	public static boolean canCast(Spell spell, INostrumMagic attr) {
		int comps = getMaxComponents(attr);
		int triggers = getMaxTriggers(attr);
		int elements = getMaxElements(attr);

		if (spell.getComponentCount() > comps)
			return false;
		if (spell.getTriggerCount() > triggers)
			return false;
		if (spell.getElementCount() > elements)
			return false;

		for (SpellPart part : spell.getSpellParts()) {
			if (part.isTrigger())
				continue;
			EMagicElement elem = part.getElement();
			if (elem == null)
				elem = EMagicElement.PHYSICAL;
			int level = part.getElementCount();
			
			final ElementalMastery neededMastery;
			switch (level) {
			case 0:
			case 1:
				neededMastery = ElementalMastery.NOVICE;
				break;
			case 2:
				neededMastery = ElementalMastery.ADEPT;
				break;
			case 3:
			default:
				neededMastery = ElementalMastery.MASTER;
				break;
			}

			if (!attr.getElementalMastery(elem).isGreaterOrEqual(neededMastery)) {
				return false;
			}
		}

		return true;
	}

	public static List<ITameDragon> getNearbyTamedDragons(LivingEntity entity, double blockRadius,
			boolean onlyOwned) {
		List<ITameDragon> list = new LinkedList<>();

		AxisAlignedBB box = new AxisAlignedBB(entity.posX - blockRadius, entity.posY - blockRadius,
				entity.posZ - blockRadius, entity.posX + blockRadius, entity.posY + blockRadius,
				entity.posZ + blockRadius);

		List<EntityTameDragonRed> dragonList = entity.world.getEntitiesWithinAABB(EntityTameDragonRed.class, box,
				(dragon) -> {
					return dragon instanceof ITameDragon;
				});

		if (dragonList != null && !dragonList.isEmpty()) {
			for (EntityTameDragonRed dragon : dragonList) {
				ITameDragon tame = (ITameDragon) dragon;

				if (onlyOwned && (!tame.isEntityTamed() || tame.getLivingOwner() != entity)) {
					continue;
				}

				list.add((ITameDragon) dragon);
			}
		}

		return list;
	}

	public static List<LivingEntity> getTamedEntities(LivingEntity owner) {
		List<LivingEntity> ents = new ArrayList<>();
		
		Iterable<Entity> entities;
		
		if (owner.world instanceof ServerWorld) {
			entities = ((ServerWorld) owner.world).getEntities().collect(Collectors.toList());
		} else {
			entities = ((ClientWorld) owner.world).getAllEntities();
		}
		
		for (Entity e : entities) {
			if (!(e instanceof LivingEntity)) {
				continue;
			}

			LivingEntity ent = (LivingEntity) e;
			if (ent instanceof ITameableEntity) {
				ITameableEntity tame = (ITameableEntity) ent;
				if (tame.isEntityTamed() && tame.getLivingOwner() != null && tame.getLivingOwner().equals(owner)) {
					ents.add(ent);
				}
			} else if (ent instanceof TameableEntity) {
				TameableEntity tame = (TameableEntity) ent;
				if (tame.isTamed() && tame.isOwner(owner)) {
					ents.add(ent);
				}
			}
		}
		return ents;
	}

	public static @Nullable LivingEntity getOwner(MobEntity entity) {
		LivingEntity ent = (LivingEntity) entity;
		if (ent instanceof ITameableEntity) {
			ITameableEntity tame = (ITameableEntity) ent;
			return tame.getLivingOwner();
		} else if (ent instanceof TameableEntity) {
			TameableEntity tame = (TameableEntity) ent;
			return tame.getOwner();
		}
		return null;
	}
	
	public static @Nullable LivingEntity getOwner(Entity entity) {
		if (entity instanceof MobEntity) {
			return getOwner((MobEntity) entity);
		}
		
		return null;
	}

	public static @Nullable Entity getEntityByUUID(World world, UUID id) {
		return Entities.FindEntity(world, id);
	}

	public SpellRegistry getSpellRegistry() {
		if (spellRegistry == null) {
			if (proxy.isServer()) {
				throw new RuntimeException("Accessing SpellRegistry before a world has been loaded!");
			} else {
				spellRegistry = new SpellRegistry();
			}
		}

		return spellRegistry;
	}

	public PetSoulRegistry getPetSoulRegistry() {
		if (petSoulRegistry == null) {
			if (proxy.isServer()) {
				throw new RuntimeException("Accessing PetSoulRegistry before a world has been loaded!");
			} else {
				petSoulRegistry = new PetSoulRegistry();
			}
		}
		return petSoulRegistry;
	}

	public PetCommandManager getPetCommandManager() {
		if (petCommandManager == null) {
			if (proxy.isServer()) {
				throw new RuntimeException("Accessing PetCommandManager before a world has been loaded!");
			} else {
				petCommandManager = new PetCommandManager();
			}
		}
		return petCommandManager;
	}
	
	public NostrumKeyRegistry getWorldKeys() {
		if (worldKeys == null) {
			if (proxy.isServer()) {
				throw new RuntimeException("Accessing WorldKeys before a world has been loaded!");
			} else {
				worldKeys = new NostrumKeyRegistry();
			}
		}
		return worldKeys;
	}

	/**
	 * Finds (or creates) the offset for a player in the sorcery dimension
	 * 
	 * @param player
	 * @return
	 */
	public static BlockPos getOrCreatePlayerDimensionSpawn(PlayerEntity player) {
		NostrumDimensionMapper mapper = getDimensionMapper(player.world);

		// Either register or fetch existing mapping
		return mapper.register(player.getUniqueID()).getCenterPos(NostrumEmptyDimension.SPAWN_Y);
	}

	public static NostrumDimensionMapper getDimensionMapper(World worldAccess) {
		if (worldAccess.isRemote) {
			throw new RuntimeException("Accessing dimension mapper before a world has been loaded!");
		}

		NostrumDimensionMapper mapper = (NostrumDimensionMapper) ((ServerWorld) worldAccess).getServer().getWorld(DimensionType.OVERWORLD)
				.getSavedData()
				.getOrCreate(NostrumDimensionMapper::new, NostrumDimensionMapper.DATA_NAME);

		// TODO I think this is automatic now?
//		if (mapper == null) { // still
//			mapper = new NostrumDimensionMapper();
//			worldAccess.getMapStorage().setData(NostrumDimensionMapper.DATA_NAME, mapper);
//		}

		serverDimensionMapper = mapper;
		return mapper;
	}

	private void initSpellRegistry(World world) {
		spellRegistry = (SpellRegistry) ((ServerWorld) world).getServer().getWorld(DimensionType.OVERWORLD).getSavedData().getOrCreate(SpellRegistry::new,
				SpellRegistry.DATA_NAME);

		// TODO I think this is automatic now?
//		if (spellRegistry == null) { // still
//			spellRegistry = new SpellRegistry();
//			world.getMapStorage().setData(SpellRegistry.DATA_NAME, spellRegistry);
//		}
	}

	private void initPetSoulRegistry(World world) {
		petSoulRegistry = (PetSoulRegistry) ((ServerWorld) world).getServer().getWorld(DimensionType.OVERWORLD).getSavedData().getOrCreate(PetSoulRegistry::new,
				PetSoulRegistry.DATA_NAME);

		// TODO I think this is automatic now?
//		if (petSoulRegistry == null) {
//			petSoulRegistry = new PetSoulRegistry();
//			world.getMapStorage().setData(PetSoulRegistry.DATA_NAME, petSoulRegistry);
//		}
	}

	private void initPetCommandManager(World world) {
		petCommandManager = (PetCommandManager) ((ServerWorld) world).getServer().getWorld(DimensionType.OVERWORLD).getSavedData().getOrCreate(PetCommandManager::new,
				PetCommandManager.DATA_NAME);

		// TODO I think this is automatic now?
//		if (petCommandManager == null) {
//			petCommandManager = new PetCommandManager();
//			world.getMapStorage().setData(PetCommandManager.DATA_NAME, petCommandManager);
//		}
	}
	
	private void initWorldKeys(World world) {
		worldKeys = (NostrumKeyRegistry) ((ServerWorld) world).getServer().getWorld(DimensionType.OVERWORLD).getSavedData().getOrCreate(NostrumKeyRegistry::new,
				NostrumKeyRegistry.DATA_NAME);
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		// Keeping a static reference since some places want to access the registry that
		// don't have world info.
		// But registry should be global anyways, so we're going to try and allow it.
		// I'm not sure the 'right' way to use global save data like this.

		if (event.getWorld().isRemote()) {
			// Clients just get a spell registry that's empty that is constantly synced with
			// the server's
			// Create one if this is our first world.
			// If in the same session we're joining another server (or loading another
			// save), the server thread will load and sync with us.
			if (spellRegistry == null) {
				spellRegistry = new SpellRegistry();
			}

		} else {
			// force an exception here if this is wrong
			ServerWorld world = (ServerWorld) event.getWorld();
			
			// Do the correct initialization for persisted data
			initSpellRegistry(world);
			getDimensionMapper(world);
			initPetSoulRegistry(world);
			initPetCommandManager(world);
			initWorldKeys(world);
		}
	}

	@SubscribeEvent
	public void onServerShutdown(FMLServerStoppedEvent event) {
		// Clean up dimension mapping info.
		// For standalones, this is sort-of meaningless.
		// For integrated, this prevents previous world's dimensions from bleeding over
		if (serverDimensionMapper != null) {
			// Ran with client
			// TODO needed? Shouldn't another load clean it up?
			serverDimensionMapper.unregisterAll();
			serverDimensionMapper = null;
		}

		magicEffectProxy.clearAll();

		// Reset portal data so previous saves don't screw you over
		NostrumPortal.resetTimers();
	}

	public static final boolean isBlockLoaded(World world, BlockPos pos) {
		// TODO in the past, this didn't actually work. Does it now??
		return world.getChunkProvider().isChunkLoaded(new ChunkPos(pos));
	}

	public static boolean IsSameTeam(LivingEntity ent1, LivingEntity ent2) {
		if (ent1 == ent2) {
			return true;
		}

		if (ent1 == null || ent2 == null) {
			return false;
		}

		if (ent1.getTeam() != null || ent2.getTeam() != null) { // If teams are at play, just use those.
			return ent1.isOnSameTeam(ent2);
		}
		
		LivingEntity ent1Owner = getOwner(ent1);
		if (ent1Owner != null) {
			// Are they on the same team as our owner?
			return IsSameTeam((LivingEntity) ent1Owner, ent2);
		}
		
		LivingEntity ent2Owner = getOwner(ent2);
		if (ent2Owner != null) {
			// Are we on the same team as their owner?
			return IsSameTeam(ent1, ent2Owner);
		}

		// Non-owned entities with no teams involved.
		// Assume mobs are on a different team than anything else
		// return (ent1 instanceof IMob == ent2 instanceof IMob);

		// If both are players and teams aren't involved, assume they can work together
		if (ent1 instanceof PlayerEntity && ent2 instanceof PlayerEntity) {
			return true;
		}

		// More hostile; assume anything here is not on same team
		return false;
	}

	public static @Nullable LivingEntity resolveLivingEntity(@Nullable Entity entityOrSubEntity) {
		if (entityOrSubEntity == null) {
			return null;
		}

		if (entityOrSubEntity instanceof LivingEntity) {
			return (LivingEntity) entityOrSubEntity;
		}
		
		// Multiparts aren't living but may have living parents!
		if (entityOrSubEntity instanceof IMultiPartEntityPart) {
			if (((IMultiPartEntityPart<?>) entityOrSubEntity).getParent() instanceof LivingEntity) {
				return (LivingEntity) ((IMultiPartEntityPart<?>) entityOrSubEntity).getParent();
			}
		}
		
		// EnderDragons are multipart but with no interface anymore
		if (entityOrSubEntity instanceof EnderDragonPartEntity) {
			if (((EnderDragonPartEntity) entityOrSubEntity).dragon instanceof LivingEntity) {
				return (LivingEntity) ((EnderDragonPartEntity) entityOrSubEntity).dragon;
			}
		}

		return null;
	}

	@SubscribeEvent
	public void onEntitySpawn(EntityJoinWorldEvent e) {
		if (e.isCanceled()) {
			return;
		}

		if (!(e.getEntity() instanceof MobEntity)) {
			return;
		}
		
		if (!(e.getEntity() instanceof TameableEntity) && !(e.getEntity() instanceof ITameableEntity)) {
			return;
		}

		final MobEntity living = (MobEntity) e.getEntity();

		// Follow task for pets
		{
			PrioritizedGoal existingTask = null;
			PrioritizedGoal followTask = null;
			
			// Get private goal list
			LinkedHashSet<PrioritizedGoal> goals = ObfuscationReflectionHelper.getPrivateValue(GoalSelector.class, living.goalSelector, "field_220892_d"); 

			// Scan for existing task
			for (PrioritizedGoal entry : goals) {
				if (entry.getGoal() instanceof FollowOwnerAdvancedGoal) {
					if (existingTask == null) {
						existingTask = entry;
					} else if (existingTask.getPriority() > entry.getPriority()) {
						existingTask = entry; // cause > priority means less priority lol
					}
				} else if (entry.getGoal() instanceof FollowOwnerGoal
						|| entry.getGoal() instanceof FollowOwnerGenericGoal) {
					if (followTask == null) {
						followTask = entry;
					} else if (followTask.getPriority() > entry.getPriority()) {
						followTask = entry;
					}
				}
			}

			if (existingTask == null) {
				// Gotta inject task. May have to make space for it.
				FollowOwnerAdvancedGoal<MobEntity> task = new FollowOwnerAdvancedGoal<MobEntity>(living,
						1.5f, 0f, .5f);
				if (followTask == null) {
					// Can just add at end
					living.goalSelector.addGoal(100, task);
				} else {
					List<PrioritizedGoal> removes = Lists.newArrayList(goals);
					final int priority = followTask.getPriority();
					removes.removeIf((entry) -> {
						return entry.getPriority() < priority;
					});

					living.goalSelector.addGoal(priority, task);
					for (PrioritizedGoal entry : removes) {
						living.goalSelector.removeGoal(entry.getGoal());
						living.goalSelector.addGoal(entry.getPriority() + 1, entry.getGoal());
					}
				}
			}
		}

		// Target task for pets
		if (living instanceof CreatureEntity) {
			CreatureEntity creature = (CreatureEntity) living;
			boolean hasTaskAlready = false;
			
			// Get private goal list
			LinkedHashSet<PrioritizedGoal> targetGoals = ObfuscationReflectionHelper.getPrivateValue(GoalSelector.class, living.targetSelector, "field_220892_d"); 

			// Scan for existing task
			for (PrioritizedGoal entry : targetGoals) {
				if (entry.getGoal() instanceof PetTargetGoal) {
					hasTaskAlready = true;
					break;
				}
			}

			if (!hasTaskAlready) {
				List<PrioritizedGoal> removes = Lists.newArrayList(targetGoals);

				living.targetSelector.addGoal(1, new PetTargetGoal<CreatureEntity>(creature));
				for (PrioritizedGoal entry : removes) {
					living.targetSelector.removeGoal(entry.getGoal());
					living.targetSelector.addGoal(entry.getPriority() + 1, entry.getGoal());
				}
			}
		}
	}

	public static boolean attemptTeleport(World world, BlockPos target, PlayerEntity player, boolean allowPortal,
			boolean spawnBristle) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		boolean success = false;

		if (allowPortal && attr != null && attr.hasEnhancedTeleport()) {
			BlockPos portal = TemporaryTeleportationPortal.spawnNearby(world, player.getPosition().up(), 4, true,
					target, 20 * 30);
			if (portal != null) {
				TemporaryTeleportationPortal.spawnNearby(world, target, 4, true, portal, 20 * 30);
				success = true;
			}
		} else {
			player.setPositionAndUpdate(target.getX() + .5, target.getY() + .1, target.getZ() + .5);
			success = true;
		}

		if (success && spawnBristle) {
			float dist = 2 + NostrumMagica.rand.nextFloat() * 2;
			float dir = NostrumMagica.rand.nextFloat();
			double dirD = dir * 2 * Math.PI;
			double dx = Math.cos(dirD) * dist;
			double dz = Math.sin(dirD) * dist;
			ItemEntity drop = new ItemEntity(world, target.getX() + .5 + dx, target.getY() + 2, target.getZ() + .5 + dz,
					new ItemStack(NostrumItems.resourceEnderBristle));
			world.addEntity(drop);
			NostrumMagicaSounds.CAST_FAIL.play(world, target.getX() + .5, target.getY() + 2, target.getZ() + .5);
		}

		return success;
	}
}
