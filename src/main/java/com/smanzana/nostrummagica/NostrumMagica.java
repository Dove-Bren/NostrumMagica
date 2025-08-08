package com.smanzana.nostrummagica;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.smanzana.nostrummagica.block.PortalBlock;
import com.smanzana.nostrummagica.block.TemporaryTeleportationPortalBlock;
import com.smanzana.nostrummagica.capabilities.CapabilityHandler;
import com.smanzana.nostrummagica.capabilities.IBonusJumpCapability;
import com.smanzana.nostrummagica.capabilities.IManaArmor;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.capabilities.INostrumMana;
import com.smanzana.nostrummagica.capabilities.ISpellCrafting;
import com.smanzana.nostrummagica.client.listener.ClientPlayerListener;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.entity.IMultiPartEntityPart;
import com.smanzana.nostrummagica.entity.dragon.ITameDragon;
import com.smanzana.nostrummagica.init.ModInit;
import com.smanzana.nostrummagica.integration.curios.CuriosClientProxy;
import com.smanzana.nostrummagica.integration.curios.CuriosProxy;
import com.smanzana.nostrummagica.integration.minecolonies.MinecoloniesProxy;
import com.smanzana.nostrummagica.inventory.IInventorySlotKey;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.item.SpellRune;
import com.smanzana.nostrummagica.item.equipment.ReagentBag;
import com.smanzana.nostrummagica.item.equipment.SpellTome;
import com.smanzana.nostrummagica.listener.ImbuedItemListener;
import com.smanzana.nostrummagica.listener.ItemSetListener;
import com.smanzana.nostrummagica.listener.MagicEffectProxy;
import com.smanzana.nostrummagica.listener.ManaArmorListener;
import com.smanzana.nostrummagica.listener.PlayerListener;
import com.smanzana.nostrummagica.listener.PlayerStatListener;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.pet.PetSoulRegistry;
import com.smanzana.nostrummagica.progression.quest.NostrumQuest;
import com.smanzana.nostrummagica.progression.requirement.IRequirement;
import com.smanzana.nostrummagica.progression.research.NostrumResearch;
import com.smanzana.nostrummagica.progression.research.NostrumResearches;
import com.smanzana.nostrummagica.progression.skill.NostrumSkills;
import com.smanzana.nostrummagica.progression.skill.Skill;
import com.smanzana.nostrummagica.proxy.ClientProxy;
import com.smanzana.nostrummagica.proxy.CommonProxy;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EElementalMastery;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.RegisteredSpell;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.SpellChargeTracker;
import com.smanzana.nostrummagica.spell.SpellCooldownTracker;
import com.smanzana.nostrummagica.spell.SpellRegistry;
import com.smanzana.nostrummagica.spell.SpellType;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.component.shapes.NostrumSpellShapes;
import com.smanzana.nostrummagica.stat.PlayerStatTracker;
import com.smanzana.nostrummagica.util.Entities;
import com.smanzana.nostrummagica.util.Location;
import com.smanzana.nostrummagica.world.dimension.NostrumDimensionMapper;
import com.smanzana.nostrummagica.world.dimension.NostrumSorceryDimension;
import com.smanzana.petcommand.api.PetFuncs;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import top.theillusivec4.curios.api.CuriosApi;

@Mod(NostrumMagica.MODID)
public class NostrumMagica {
	public static final String MODID = "nostrummagica";
	public static final Random rand = new Random();

	public static NostrumMagica instance;
	
	public static final CommonProxy Proxy = DistExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
	public static final CuriosProxy CuriosProxy = DistExecutor.unsafeRunForDist(() -> CuriosClientProxy::new, () -> CuriosProxy::new);
	//public static final AetheriaProxy AetheriaProxy = DistExecutor.safeRunForDist(() -> AetheriaClientProxy::new, () -> AetheriaProxy::new);;
	//public final EnderIOProxy enderIO;
	public static final MinecoloniesProxy MinecoloniesProxy = new MinecoloniesProxy();

	public static CreativeModeTab creativeTab;
	public static CreativeModeTab equipmentTab;
	public static CreativeModeTab enhancementTab;
	public static CreativeModeTab runeTab;
	public static CreativeModeTab dungeonTab;
	public static Logger logger = LogManager.getLogger(MODID);
	public static PlayerListener playerListener;
	public static PlayerStatListener statListener;
	public static MagicEffectProxy magicEffectProxy;
	public static ManaArmorListener manaArmorListener;
	public static ItemSetListener itemSetListener;
	public static SpellChargeTracker spellChargeTracker;
	public static ImbuedItemListener imbuedItemListener;
	
	// Better way to do this?
	private static SpellCooldownTracker server_spellCooldownTracker;
	private static SpellCooldownTracker client_spellCooldownTracker;

	// Cached references that have sketchy access rules. See uses in this file.
	private static SpellRegistry spellRegistry;
	private static NostrumDimensionMapper serverDimensionMapper;
	private static PetSoulRegistry petSoulRegistry;
	private static PlayerStatTracker playerStats;

	public static boolean initFinished = false;
	
	public NostrumMagica() {
		instance = this;
		
		//enderIO = DistExecutor.safeRunForDist(() -> EnderIOClientProxy::new, () -> EnderIOProxy::new);
		
		(new ModConfig()).register();
		
		playerListener = DistExecutor.unsafeRunForDist(() -> ClientPlayerListener::new, () -> PlayerListener::new);
		magicEffectProxy = new MagicEffectProxy();
		manaArmorListener = new ManaArmorListener();
		statListener = new PlayerStatListener();
		itemSetListener = new ItemSetListener();
		spellChargeTracker = new SpellChargeTracker();
		imbuedItemListener = new ImbuedItemListener();

		NostrumMagica.creativeTab = new CreativeModeTab(MODID) {
			@Override
			@OnlyIn(Dist.CLIENT)
			public ItemStack makeIcon() {
				return new ItemStack(NostrumItems.spellTomeNovice);
			}
		};
		
		NostrumMagica.equipmentTab = new CreativeModeTab(MODID + "_equipment") {
			@Override
			@OnlyIn(Dist.CLIENT)
			public ItemStack makeIcon() {
				return new ItemStack(NostrumItems.mageStaff);
			}
		};
		
		NostrumMagica.enhancementTab = new CreativeModeTab(MODID + "_enhancements") {
			@Override
			@OnlyIn(Dist.CLIENT)
			public ItemStack makeIcon() {
				return new ItemStack(NostrumItems.spellTomePage);
			}
		};

		NostrumMagica.runeTab = new CreativeModeTab(MODID + "_runes") {
			@Override
			@OnlyIn(Dist.CLIENT)
			public ItemStack makeIcon() {
				return SpellRune.getRune(NostrumSpellShapes.Touch);
			}
		};
		
		NostrumMagica.dungeonTab = new CreativeModeTab(MODID + "_dungeon") {
			@Override
			@OnlyIn(Dist.CLIENT)
			public ItemStack makeIcon() {
				return new ItemStack(NostrumItems.worldKey);
			}
		};

		if (ModList.get().isLoaded(CuriosApi.MODID)) {
			CuriosProxy.enable();
		}
//		if (ModList.get().isLoaded("nostrumaetheria")) {
//			AetheriaProxy.enable();
//		}
//		if (ModList.get().isLoaded("enderio") || ModList.get().isLoaded("enderio")) {
//			enderIO.enable();
//		}
		if (ModList.get().isLoaded("minecolonies")) {
			MinecoloniesProxy.enable();
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
		
		return e.getCapability(CapabilityHandler.CAPABILITY_MAGIC).orElse(null);
	}

	public static IManaArmor getManaArmor(Entity e) {
		if (e == null)
			return null;

		return e.getCapability(CapabilityHandler.CAPABILITY_MANAARMOR).orElse(null);
	}
	
	public static ISpellCrafting getSpellCrafting(Entity e) {
		if (e == null) {
			return null;
		}
		
		return e.getCapability(CapabilityHandler.CAPABILITY_SPELLCRAFTING).orElse(null);
	}
	
	public static IBonusJumpCapability getBonusJump(Entity e) {
		if (e == null) {
			return null;
		}
		
		return e.getCapability(CapabilityHandler.CAPABILITY_BONUSJUMP).orElse(null);
	}
	
	public static INostrumMana getManaWrapper(Entity e) {
		if (e == null) {
			return null;
		}
		
		return e.getCapability(CapabilityHandler.CAPABILITY_MANA).orElse(null);
	}

	public static ItemStack findTome(Player entity, int tomeID) {
		// We look in mainhand first, then offhand, then just down
		// hotbar.
		@Nullable IInventorySlotKey<LivingEntity> key = NostrumMagica.CuriosProxy.getTomeSlotKey(entity);
		if (key != null) {
			final ItemStack item = key.getHeldStack(entity);
			if (!item.isEmpty() && item.getItem() instanceof SpellTome)
				if (SpellTome.getTomeID(item) == tomeID)
					return item;
		}
		
		for (ItemStack item : entity.getInventory().items) {
			if (!item.isEmpty() && item.getItem() instanceof SpellTome)
				if (SpellTome.getTomeID(item) == tomeID)
					return item;
		}

		for (ItemStack item : entity.getInventory().offhand) {
			if (!item.isEmpty() && item.getItem() instanceof SpellTome)
				if (SpellTome.getTomeID(item) == tomeID)
					return item;
		}

		return ItemStack.EMPTY;
	}

	public static @Nonnull ItemStack getCurrentTome(Player entity) {
		// We look in mainhand first, then offhand, then just down
		// hotbar.
		ItemStack tome = ItemStack.EMPTY;
		
		@Nullable IInventorySlotKey<LivingEntity> key = NostrumMagica.CuriosProxy.getTomeSlotKey(entity);
		final ItemStack tomeSlotItem = (key == null ? ItemStack.EMPTY : key.getHeldStack(entity));

		if (!tomeSlotItem.isEmpty() && tomeSlotItem.getItem() instanceof SpellTome) {
			tome = tomeSlotItem;
		} else if (!entity.getMainHandItem().isEmpty() && entity.getMainHandItem().getItem() instanceof SpellTome) {
			tome = entity.getMainHandItem();
		} else if (!entity.getOffhandItem().isEmpty()
				&& entity.getOffhandItem().getItem() instanceof SpellTome) {
			tome = entity.getOffhandItem();
		} else {
			// hotbar is items 0-8
			int count = 0;
			for (ItemStack stack : entity.getInventory().items) {
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

	public static @Nonnull RegisteredSpell[] getCurrentSpellLoadout(Player player) {
		if (player == null) {
			return new RegisteredSpell[0];
		}
		
		// We just return the spells from the curernt tome.
		ItemStack tome = getCurrentTome(player);

		if (tome.isEmpty())
			return null;
		
		RegisteredSpell[] spells = SpellTome.getSpellsInCurrentPage(tome);
		return spells;
	}

	public static int getReagentCount(Player player, ReagentType type) {
		int count = 0;
		for (ItemStack item : player.getInventory().items) {
			if (!item.isEmpty() && item.getItem() instanceof ReagentBag) {
				count += ReagentBag.getReagentCount(item, type);
			}
		}
		for (ItemStack item : player.getInventory().offhand) {
			if (!item.isEmpty() && item.getItem() instanceof ReagentBag) {
				count += ReagentBag.getReagentCount(item, type);
			}
		}
		for (ItemStack item : player.getInventory().items) {
			if (!item.isEmpty() && item.getItem() instanceof ReagentItem && ReagentItem.FindType(item) == type) {
				count += item.getCount();
			}
		}
		for (ItemStack item : player.getInventory().offhand) {
			if (!item.isEmpty() && item.getItem() instanceof ReagentBag && ReagentItem.FindType(item) == type) {
				count += item.getCount();
			}
		}
		
		Container curios = NostrumMagica.CuriosProxy.getCurios(player);
		if (curios != null) {
			for (int i = 0; i < curios.getContainerSize(); i++) {
				ItemStack equip = curios.getItem(i);
				if (equip.isEmpty()) {
					continue;
				}
				
				if (equip.getItem() instanceof ReagentBag) {
					count += ReagentBag.getReagentCount(equip, type);
				}
			}
		}

		return count;
	}

	public static boolean removeReagents(Player player, ReagentType type, int count) {
		if (getReagentCount(player, type) < count)
			return false;
		
		Container curios = NostrumMagica.CuriosProxy.getCurios(player);
		if (curios != null) {
			for (int i = 0; i < curios.getContainerSize(); i++) {
				ItemStack equip = curios.getItem(i);
				if (equip.isEmpty()) {
					continue;
				}
				
				if (equip.getItem() instanceof ReagentBag) {
					count = ReagentBag.removeCount(equip, type, count);
				}
				
				if (count == 0) {
					break;
				}
			}
		}

		if (count != 0)
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack item = player.getInventory().getItem(i);
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
						player.getInventory().setItem(i, ItemStack.EMPTY);
					}
				}
			}

			if (count == 0)
				break;
		}

		return count == 0;
	}

	public static List<NostrumQuest> getActiveQuests(Player player) {
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

	public static List<NostrumQuest> getCompletedQuests(Player player) {
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

	public static List<NostrumResearch> getCompletedResearch(Player player) {
		return getCompletedResearch(getMagicWrapper(player));
	}

	public static List<NostrumResearch> getCompletedResearch(INostrumMagic attr) {
		List<NostrumResearch> list = new LinkedList<>();
		List<ResourceLocation> research = attr.getCompletedResearches();

		if (research != null && !research.isEmpty())
			for (ResourceLocation researchKey : research) {
				NostrumResearch r = NostrumResearch.lookup(researchKey);
				if (r != null)
					list.add(r);
			}

		return list;
	}

	private List<Runnable> researchReloadHooks = new LinkedList<>();

	public void reloadDefaultResearch() {
		NostrumResearch.ClearAllResearch();
		NostrumResearches.init();
		if (CuriosProxy.isEnabled()) {
			CuriosProxy.reinitResearch();
		}
//		if (enderIO.isEnabled()) {
//			enderIO.reinitResearch();
//		}

		for (Runnable hook : researchReloadHooks) {
			hook.run();
		}
		NostrumResearch.Validate();
	}

	public void registerResearchReloadHook(Runnable hook) {
		this.researchReloadHooks.add(hook);
	}
	
	private List<Runnable> questReloadHooks = new LinkedList<>();
	
	public void reloadDefaultQuests() {
		NostrumQuest.ClearAllQuests();
		ModInit.registerDefaultQuests();
		
		for (Runnable hook : questReloadHooks) {
			hook.run();
		}
		NostrumQuest.Validate();
	}
	
	public void registerQuestReloadHook(Runnable hook) {
		questReloadHooks.add(hook);
	}
	
	private List<Runnable> skillReloadHooks = new LinkedList<>();
	
	public void reloadDefaultSkills() {
		Skill.ClearSkills();
		NostrumSkills.init();
		
		for (Runnable hook : skillReloadHooks) {
			hook.run();
		}
		Skill.Validate();
	}
	
	public void registerSkillReloadHook(Runnable hook) {
		skillReloadHooks.add(hook);
	}

	/**
	 * Whether a quest is visible in the mirror by normal rules. This is either that
	 * one of the parents of the quest has been finished, OR that the quest has no
	 * parent but the conditions to take the quest are fulfilled.
	 * 
	 * @param player
	 * @param quest
	 * @return
	 */
	public static boolean getQuestAvailable(Player player, NostrumQuest quest) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);

		if (attr == null)
			return false;

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
	public static boolean canTakeQuest(Player player, NostrumQuest quest) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null)
			return false;

		// Check requirements
		if (quest.getRequirements() != null) {
			for (IRequirement req : quest.getRequirements()) {
				if (!req.matches(player)) {
					return false;
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

		return true;
	}

	public static boolean getResearchVisible(Player player, NostrumResearch research) {
		// Visible if any of parents is finished (unless hidden)
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null)
			return false;
		
		if (research == NostrumResearches.Modification_Table) {
			System.out.println("Considering mod table");
		}

		List<ResourceLocation> finished = attr.getCompletedResearches();
		if (finished.contains(research.getID())) {
			return true;
		}
		
		if (research.isHidden()) {
			return canPurchaseResearch(player, research);
		}

		ResourceLocation[] parents = research.getAllParents();
		if (parents == null || parents.length == 0) {
			return true;
		}

		if (finished == null || finished.isEmpty()) {
			return false;
		}

		for (ResourceLocation parent : parents) {
			if (finished.contains(parent)) {
				return true;
			}
		}

		return false;
	}

	public static boolean canPurchaseResearch(Player player, NostrumResearch research) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null)
			return false;
		
		if (research.isPurchaseDisallowed()) {
			return false;
		}

		// Check requirements
		if (research.getRequirements() != null && research.getRequirements().length != 0) {
			for (IRequirement req: research.getRequirements()) {
				if (!req.matches(player)) {
					return false;
				}
			}
		}

		// ALL parents must be completed before research can be taken
		ResourceLocation[] parents = research.getAllParents();
		if (parents != null && parents.length > 0) {
			List<ResourceLocation> finished = attr.getCompletedResearches();
			if (finished == null || finished.isEmpty()) {
				return false;
			}

			for (ResourceLocation parent : parents) {
				if (!finished.contains(parent)) {
					return false;
				}
			}
		}

		return true;
	}

	public static boolean canCast(Spell spell, INostrumMagic attr, @Nonnull List<Component> problemsOut) {
		boolean success = true;

		Map<EMagicElement, EElementalMastery> neededMasteries = new EnumMap<>(EMagicElement.class);

		for (SpellEffectPart part : spell.getSpellEffectParts()) {
			EMagicElement elem = part.getElement();
			if (elem == null)
				elem = EMagicElement.NEUTRAL;
			int level = part.getElementCount();
			
			final EElementalMastery neededMastery;
			switch (level) {
			case 0:
			case 1:
				neededMastery = EElementalMastery.NOVICE;
				break;
			case 2:
				neededMastery = EElementalMastery.ADEPT;
				break;
			case 3:
			default:
				neededMastery = EElementalMastery.MASTER;
				break;
			}
			
			if (!neededMasteries.containsKey(elem) || !neededMasteries.get(elem).isGreaterOrEqual(neededMastery)) {
				neededMasteries.put(elem, neededMastery);
			}
		}
		
		for (EMagicElement elem : neededMasteries.keySet()) {
			@Nullable EElementalMastery neededMastery = neededMasteries.get(elem);
			
			if (neededMastery == EElementalMastery.NOVICE
					&& attr.hasSkill(NostrumSkills.Craftcast_ElemFree)
					&& spell.getType() == SpellType.Crafted) {
				neededMastery = null;
			}
			
			if (neededMastery == null) {
				continue;
			}
			
			final EElementalMastery currentMastery = attr.getElementalMastery(elem);
			if (!currentMastery.isGreaterOrEqual(neededMastery)) {
				success = false;
				problemsOut.add(new TranslatableComponent("info.spell.low_mastery", neededMastery.getName(), elem.getDisplayName(), currentMastery.getName()));
			}
		}

		return success;
	}

	public static List<ITameDragon> getNearbyTamedDragons(LivingEntity entity, double blockRadius,
			boolean onlyOwned) {
		double blockRadiusSq = blockRadius * blockRadius;
		return PetFuncs.GetTamedEntities(entity, (ent) -> {
			if (!(ent instanceof ITameDragon)) {
				return false;
			}
			
			if (ent.distanceToSqr(entity) > blockRadiusSq) {
				return false;
			}
			
			return true;
		}).stream().map(ent -> (ITameDragon) ent).collect(Collectors.toList());
	}
	
	public static boolean UnlockElementalMastery(LivingEntity entity, EMagicElement element, EElementalMastery mastery) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
		if (attr != null
				&& attr.setElementalMastery(element, mastery)) {
			if (mastery != EElementalMastery.UNKNOWN) {
				if (entity != null && !entity.level.isClientSide
						&& entity instanceof ServerPlayer player) {
					final Component msg = new TranslatableComponent("info.element_mastery." + mastery.getTranslationKey(), element.getDisplayName());
					player.sendMessage(msg, Util.NIL_UUID);
				}
			}
			return true;
		}
		
		return false;
	}

	public static @Nullable Entity getEntityByUUID(Level world, UUID id) {
		return Entities.FindEntity(world, id);
	}
	
	public static boolean awardLore(Entity ent, ILoreTagged lore, boolean full) {
		@Nullable INostrumMagic attr = NostrumMagica.getMagicWrapper(ent);
		if (attr != null && attr.isUnlocked()) {
			if (!full && !attr.hasLore(lore)) {
				attr.giveBasicLore(lore);
				return true;
			} else if (full) {
				boolean ret = attr.hasFullLore(lore);
				attr.giveFullLore(lore);
				return !ret;
			}
		}
		return false;
	}
	
	public static boolean awardLoreToNearbyPlayers(Level level, Vec3 center, ILoreTagged lore, boolean full, double radius) {
		boolean any = false;
		for (Entity ent : level.getEntities(null, AABB.ofSize(center, radius, radius, radius))) {
			if (awardLore(ent, lore, full)) {
				any = true;
			}
		}
		
		return any;
	}

	public SpellRegistry getSpellRegistry() {
		if (spellRegistry == null) {
			if (Proxy.isServer()) {
				throw new RuntimeException("Accessing SpellRegistry before a world has been loaded!");
			} else {
				spellRegistry = new SpellRegistry();
			}
		}

		return spellRegistry;
	}

	public PetSoulRegistry getPetSoulRegistry() {
		if (petSoulRegistry == null) {
			if (Proxy.isServer()) {
				throw new RuntimeException("Accessing PetSoulRegistry before a world has been loaded!");
			} else {
				petSoulRegistry = new PetSoulRegistry();
			}
		}
		return petSoulRegistry;
	}

	public PlayerStatTracker getPlayerStats() {
		if (playerStats == null) {
			if (Proxy.isServer()) {
				throw new RuntimeException("Accessing PlayerStats before a world has been loaded!");
			} else {
				playerStats = new PlayerStatTracker();
			}
		}
		return playerStats;
	}

	/**
	 * Finds (or creates) the offset for a player in the sorcery dimension
	 * 
	 * @param player
	 * @return
	 */
	public static BlockPos getOrCreatePlayerDimensionSpawn(Player player) {
		NostrumDimensionMapper mapper = getDimensionMapper(player.level);

		// Either register or fetch existing mapping
		return mapper.register(player.getUUID()).getCenterPos(NostrumSorceryDimension.SPAWN_Y);
	}

	public static NostrumDimensionMapper getDimensionMapper(Level worldAccess) {
		if (worldAccess.isClientSide) {
			throw new RuntimeException("Accessing dimension mapper before a world has been loaded!");
		}

		NostrumDimensionMapper mapper = (NostrumDimensionMapper) ((ServerLevel) worldAccess).getServer().getLevel(Level.OVERWORLD)
				.getDataStorage()
				.computeIfAbsent(NostrumDimensionMapper::load, NostrumDimensionMapper::new, NostrumDimensionMapper.DATA_NAME);

		serverDimensionMapper = mapper;
		return mapper;
	}

	private void initSpellRegistry(Level world) {
		spellRegistry = (SpellRegistry) ((ServerLevel) world).getServer().getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(SpellRegistry::Load, SpellRegistry::new,
				SpellRegistry.DATA_NAME);
	}

	private void initPetSoulRegistry(Level world) {
		petSoulRegistry = (PetSoulRegistry) ((ServerLevel) world).getServer().getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(PetSoulRegistry::Load, PetSoulRegistry::new,
				PetSoulRegistry.DATA_NAME);
	}

	private void initPlayerStats(Level world) {
		playerStats = (PlayerStatTracker) ((ServerLevel) world).getServer().getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(PlayerStatTracker::Load, PlayerStatTracker::new,
				PlayerStatTracker.DATA_NAME);
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		// Keeping a static reference since some places want to access the registry that
		// don't have world info.
		// But registry should be global anyways, so we're going to try and allow it.
		// I'm not sure the 'right' way to use global save data like this.

		if (event.getWorld().isClientSide()) {
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
			ServerLevel world = (ServerLevel) event.getWorld();
			
			// Do the correct initialization for persisted data
			initSpellRegistry(world);
			getDimensionMapper(world);
			initPetSoulRegistry(world);
			initPlayerStats(world);
		}
	}

	@SubscribeEvent
	public void onServerShutdown(ServerStoppedEvent event) {
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
		PortalBlock.resetTimers();
	}

	public static final boolean isBlockLoaded(Level world, BlockPos pos) {
		// TODO in the past, this didn't actually work. Does it now??
		return world.isLoaded(pos);
	}

	public static boolean IsSameTeam(LivingEntity ent1, LivingEntity ent2) {
		return PetFuncs.IsSameTeam(ent1, ent2) || MinecoloniesProxy.IsSameColony(ent1, ent2);
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
		if (entityOrSubEntity instanceof EnderDragonPart) {
			if (((EnderDragonPart) entityOrSubEntity).parentMob instanceof LivingEntity) {
				return (LivingEntity) ((EnderDragonPart) entityOrSubEntity).parentMob;
			}
		}

		return null;
	}

    @Cancelable
	public static class NostrumTeleportEvent extends EntityTeleportEvent {
		
		private final @Nullable LivingEntity causingEntity;

		public NostrumTeleportEvent(Entity entity, double targetX, double targetY, double targetZ, @Nullable LivingEntity causingEntity) {
			super(entity, targetX, targetY, targetZ);
			this.causingEntity = causingEntity;
		}
		
		public @Nullable LivingEntity getCausingEntity() {
			return this.causingEntity;
		}
		
	}
    
    public static class NostrumTeleportedOtherEvent extends EntityEvent {
    	
    	private final LivingEntity causingEntity;
    	private final Vec3 from;
    	private final Vec3 to;
    	
    	public NostrumTeleportedOtherEvent(Entity entity, LivingEntity causingEntity, Vec3 from, Vec3 to) {
    		super(entity);
    		this.causingEntity = causingEntity;
    		this.from = from;
    		this.to = to;
    	}
    	
    	public @Nonnull LivingEntity getCausingEntity() {
    		return this.causingEntity;
    	}

		public Vec3 getFrom() {
			return from;
		}

		public Vec3 getTo() {
			return to;
		}
    	
    }
	
	public static NostrumTeleportEvent fireTeleportAttemptEvent(Entity entity, double targetX, double targetY, double targetZ, @Nullable LivingEntity causingEntity) {
		NostrumTeleportEvent event = new NostrumTeleportEvent(entity, targetX, targetY, targetZ, causingEntity);
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}
	
	public static NostrumTeleportedOtherEvent fireTeleprotedOtherEvent(Entity entity, LivingEntity cause, Vec3 from, Vec3 to) {
		NostrumTeleportedOtherEvent event = new NostrumTeleportedOtherEvent(entity, cause, from, to);
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}

	public static boolean attemptTeleport(Location target, Player player, boolean allowPortal,
			boolean spawnBristle, @Nullable LivingEntity causingEntity) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		boolean success = false;

		if (allowPortal && attr != null && attr.hasEnhancedTeleport()) {
			BlockPos portal = TemporaryTeleportationPortalBlock.spawnNearby(player.getCommandSenderWorld(), player.blockPosition().above(), 4, true,
					target, 20 * 30);
			if (portal != null) {
				final Level targetWorld = ServerLifecycleHooks.getCurrentServer().getLevel(target.getDimension());
				final Location localPortalPos = new Location(player.getCommandSenderWorld(), portal);
				TemporaryTeleportationPortalBlock.spawnNearby(targetWorld, target.getPos(), 4, true, localPortalPos, 20 * 30);
				success = true;
			}
		} else {
			NostrumTeleportEvent event = fireTeleportAttemptEvent(player, target.getPos().getX() + .5, target.getPos().getY() + .1, target.getPos().getZ() + .5, causingEntity);
			if (!event.isCanceled()) {
				event.getEntity().teleportTo(event.getTargetX(), event.getTargetY(), event.getTargetZ());
				success = true;
				if (causingEntity != null) {
					fireTeleprotedOtherEvent(event.getEntity(), causingEntity, event.getPrev(), event.getTarget());
				}
			}
		}

		if (success && spawnBristle) {
			final Level targetWorld = ServerLifecycleHooks.getCurrentServer().getLevel(target.getDimension());
			float dist = 2 + NostrumMagica.rand.nextFloat() * 2;
			float dir = NostrumMagica.rand.nextFloat();
			double dirD = dir * 2 * Math.PI;
			double dx = Math.cos(dirD) * dist;
			double dz = Math.sin(dirD) * dist;
			ItemEntity drop = new ItemEntity(targetWorld, target.getPos().getX() + .5 + dx, target.getPos().getY() + 2, target.getPos().getZ() + .5 + dz,
					new ItemStack(NostrumItems.resourceEnderBristle));
			targetWorld.addFreshEntity(drop);
			NostrumMagicaSounds.CAST_FAIL.play(targetWorld, target.getPos().getX() + .5, target.getPos().getY() + 2, target.getPos().getZ() + .5);
		}

		return success;
	}
	
	public SpellCooldownTracker getSpellCooldownTracker(Level world) {
		if (world.isClientSide()) {
			if (client_spellCooldownTracker == null) {
				client_spellCooldownTracker = new SpellCooldownTracker();
			}
			return client_spellCooldownTracker;
		} else {
			if (server_spellCooldownTracker == null) {
				server_spellCooldownTracker = new SpellCooldownTracker();
			}
			return server_spellCooldownTracker;
		}
	}

	public SpellChargeTracker getSpellChargeTracker() {
		return spellChargeTracker;
	}
	
	public static final @Nonnull ResourceLocation Loc(String path) {
		return new ResourceLocation(NostrumMagica.MODID, path);
	}
}
