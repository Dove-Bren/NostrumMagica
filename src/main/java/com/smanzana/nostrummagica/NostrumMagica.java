package com.smanzana.nostrummagica;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.smanzana.nostrummagica.blocks.NostrumPortal;
import com.smanzana.nostrummagica.blocks.TemporaryTeleportationPortal;
import com.smanzana.nostrummagica.capabilities.IManaArmor;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.capabilities.INostrumMagic.ElementalMastery;
import com.smanzana.nostrummagica.capabilities.ISpellCrafting;
import com.smanzana.nostrummagica.capabilities.ManaArmorAttributeProvider;
import com.smanzana.nostrummagica.capabilities.NostrumMagicAttributeProvider;
import com.smanzana.nostrummagica.capabilities.SpellCraftingCapabilityProvider;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.entity.IMultiPartEntityPart;
import com.smanzana.nostrummagica.entity.dragon.ITameDragon;
import com.smanzana.nostrummagica.init.ModInit;
import com.smanzana.nostrummagica.integration.aetheria.AetheriaClientProxy;
import com.smanzana.nostrummagica.integration.aetheria.AetheriaProxy;
import com.smanzana.nostrummagica.integration.curios.CuriosClientProxy;
import com.smanzana.nostrummagica.integration.curios.CuriosProxy;
import com.smanzana.nostrummagica.integration.musica.MusicaClientProxy;
import com.smanzana.nostrummagica.integration.musica.MusicaProxy;
import com.smanzana.nostrummagica.items.NostrumItems;
import com.smanzana.nostrummagica.items.ReagentBag;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy;
import com.smanzana.nostrummagica.listeners.ManaArmorListener;
import com.smanzana.nostrummagica.listeners.PlayerListener;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.pet.PetSoulRegistry;
import com.smanzana.nostrummagica.proxy.ClientProxy;
import com.smanzana.nostrummagica.proxy.CommonProxy;
import com.smanzana.nostrummagica.quests.NostrumQuest;
import com.smanzana.nostrummagica.research.NostrumResearch;
import com.smanzana.nostrummagica.research.NostrumResearch.SpellSpec;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.SpellPart;
import com.smanzana.nostrummagica.spells.SpellRegistry;
import com.smanzana.nostrummagica.utils.Entities;
import com.smanzana.nostrummagica.world.NostrumKeyRegistry;
import com.smanzana.nostrummagica.world.dimension.NostrumDimensionMapper;
import com.smanzana.nostrummagica.world.dimension.NostrumSorceryDimension;
import com.smanzana.petcommand.api.PetFuncs;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPartEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import top.theillusivec4.curios.api.CuriosApi;

@Mod(NostrumMagica.MODID)
public class NostrumMagica {
	public static final String MODID = "nostrummagica";
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
	private static NostrumKeyRegistry worldKeys;

	public static boolean initFinished = false;
	
	public NostrumMagica() {
		instance = this;
		
		proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
		curios = DistExecutor.safeRunForDist(() -> CuriosClientProxy::new, () -> CuriosProxy::new);
		aetheria = DistExecutor.safeRunForDist(() -> AetheriaClientProxy::new, () -> AetheriaProxy::new);
		//enderIO = DistExecutor.safeRunForDist(() -> EnderIOClientProxy::new, () -> EnderIOProxy::new);
		musica = DistExecutor.safeRunForDist(() -> MusicaClientProxy::new, () -> MusicaProxy::new);
		
		(new ModConfig()).register();
		
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
		NostrumMagica.enhancementTab = new ItemGroup(MODID + "_enhancements") {
			@Override
			@OnlyIn(Dist.CLIENT)
			public ItemStack createIcon() {
				return new ItemStack(NostrumItems.spellTomePage);
			}
		};

		if (ModList.get().isLoaded(CuriosApi.MODID)) {
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
	
	public static ISpellCrafting getSpellCrafting(Entity e) {
		if (e == null) {
			return null;
		}
		
		return e.getCapability(SpellCraftingCapabilityProvider.CAPABILITY).orElse(null);
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

	public static @Nonnull Spell[] getCurrentSpellLoadout(PlayerEntity player) {
		if (player == null) {
			return new Spell[0];
		}
		
		// We just return the spells from the curernt tome.
		ItemStack tome = getCurrentTome(player);

		if (tome.isEmpty())
			return null;
		
		Spell[] spells = SpellTome.getSpellsInCurrentPage(tome);
		return spells;
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

	private List<Function<Integer, Integer>> researchReloadHooks = new LinkedList<>();

	public void reloadDefaultResearch() {
		NostrumResearch.ClearAllResearch();
		ModInit.registerDefaultResearch();
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

	public static boolean canCast(Spell spell, INostrumMagic attr, @Nonnull List<ITextComponent> problemsOut) {
		int comps = getMaxComponents(attr);
		int triggers = getMaxTriggers(attr);
		int elements = getMaxElements(attr);
		
		boolean success = true;

		if (spell.getComponentCount() > comps) {
			success = false;
			problemsOut.add(new TranslationTextComponent("info.spell.low_tech"));
		}
		if (spell.getTriggerCount() > triggers) {
			success = false;
			problemsOut.add(new TranslationTextComponent("info.spell.low_finesse"));
		}
		if (spell.getElementCount() > elements) {
			success = false;
			problemsOut.add(new TranslationTextComponent("info.spell.low_control"));
		}
		
		Map<EMagicElement, ElementalMastery> neededMasteries = new EnumMap<>(EMagicElement.class);

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
			
			if (!neededMasteries.containsKey(elem) || !neededMasteries.get(elem).isGreaterOrEqual(neededMastery)) {
				neededMasteries.put(elem, neededMastery);
			}
		}
		
		for (EMagicElement elem : neededMasteries.keySet()) {
			final @Nullable ElementalMastery neededMastery = neededMasteries.get(elem);
			if (neededMastery == null) {
				continue;
			}
			
			final ElementalMastery currentMastery = attr.getElementalMastery(elem);
			if (!currentMastery.isGreaterOrEqual(neededMastery)) {
				success = false;
				problemsOut.add(new TranslationTextComponent("info.spell.low_mastery", neededMastery.name().toLowerCase(), elem.getName(), currentMastery.name().toLowerCase()));
			}
		}

		return success;
	}

	public static List<ITameDragon> getNearbyTamedDragons(LivingEntity entity, double blockRadius,
			boolean onlyOwned) {
//		List<ITameDragon> list = new LinkedList<>();
//
//		AxisAlignedBB box = new AxisAlignedBB(entity.getPosX() - blockRadius, entity.getPosY() - blockRadius,
//				entity.getPosZ() - blockRadius, entity.getPosX() + blockRadius, entity.getPosY() + blockRadius,
//				entity.getPosZ() + blockRadius);
//
//		List<EntityTameDragonRed> dragonList = entity.world.getEntitiesWithinAABB(EntityTameDragonRed.class, box,
//				(dragon) -> {
//					return dragon instanceof ITameDragon;
//				});
//
//		if (dragonList != null && !dragonList.isEmpty()) {
//			for (EntityTameDragonRed dragon : dragonList) {
//				ITameDragon tame = (ITameDragon) dragon;
//
//				if (onlyOwned && (!tame.isEntityTamed() || tame.getLivingOwner() != entity)) {
//					continue;
//				}
//
//				list.add((ITameDragon) dragon);
//			}
//		}
//
//		return list;
		
		double blockRadiusSq = blockRadius * blockRadius;
		return PetFuncs.GetTamedEntities(entity, (ent) -> {
			if (!(ent instanceof ITameDragon)) {
				return false;
			}
			
			if (ent.getDistanceSq(entity) > blockRadiusSq) {
				return false;
			}
			
			return true;
		}).stream().map(ent -> (ITameDragon) ent).collect(Collectors.toList());
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
		return mapper.register(player.getUniqueID()).getCenterPos(NostrumSorceryDimension.SPAWN_Y);
	}

	public static NostrumDimensionMapper getDimensionMapper(World worldAccess) {
		if (worldAccess.isRemote) {
			throw new RuntimeException("Accessing dimension mapper before a world has been loaded!");
		}

		NostrumDimensionMapper mapper = (NostrumDimensionMapper) ((ServerWorld) worldAccess).getServer().getWorld(World.OVERWORLD)
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
		spellRegistry = (SpellRegistry) ((ServerWorld) world).getServer().getWorld(World.OVERWORLD).getSavedData().getOrCreate(SpellRegistry::new,
				SpellRegistry.DATA_NAME);

		// TODO I think this is automatic now?
//		if (spellRegistry == null) { // still
//			spellRegistry = new SpellRegistry();
//			world.getMapStorage().setData(SpellRegistry.DATA_NAME, spellRegistry);
//		}
	}

	private void initPetSoulRegistry(World world) {
		petSoulRegistry = (PetSoulRegistry) ((ServerWorld) world).getServer().getWorld(World.OVERWORLD).getSavedData().getOrCreate(PetSoulRegistry::new,
				PetSoulRegistry.DATA_NAME);

		// TODO I think this is automatic now?
//		if (petSoulRegistry == null) {
//			petSoulRegistry = new PetSoulRegistry();
//			world.getMapStorage().setData(PetSoulRegistry.DATA_NAME, petSoulRegistry);
//		}
	}

	private void initWorldKeys(World world) {
		worldKeys = (NostrumKeyRegistry) ((ServerWorld) world).getServer().getWorld(World.OVERWORLD).getSavedData().getOrCreate(NostrumKeyRegistry::new,
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
		return PetFuncs.IsSameTeam(ent1, ent2);
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
	
	public static final @Nonnull ResourceLocation Loc(String path) {
		return new ResourceLocation(NostrumMagica.MODID, path);
	}
}
