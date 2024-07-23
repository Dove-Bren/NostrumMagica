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
import com.smanzana.nostrummagica.capabilities.IManaArmor;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
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
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.item.SpellRune;
import com.smanzana.nostrummagica.item.SpellTome;
import com.smanzana.nostrummagica.item.equipment.ReagentBag;
import com.smanzana.nostrummagica.listener.MagicEffectProxy;
import com.smanzana.nostrummagica.listener.ManaArmorListener;
import com.smanzana.nostrummagica.listener.PlayerListener;
import com.smanzana.nostrummagica.listener.PlayerStatListener;
import com.smanzana.nostrummagica.pet.PetSoulRegistry;
import com.smanzana.nostrummagica.progression.quest.NostrumQuest;
import com.smanzana.nostrummagica.progression.requirement.IRequirement;
import com.smanzana.nostrummagica.progression.research.NostrumResearch;
import com.smanzana.nostrummagica.progression.skill.NostrumSkills;
import com.smanzana.nostrummagica.progression.skill.Skill;
import com.smanzana.nostrummagica.proxy.ClientProxy;
import com.smanzana.nostrummagica.proxy.CommonProxy;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EElementalMastery;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.SpellCooldownTracker;
import com.smanzana.nostrummagica.spell.SpellRegistry;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.component.shapes.NostrumSpellShapes;
import com.smanzana.nostrummagica.stat.PlayerStatTracker;
import com.smanzana.nostrummagica.util.Entities;
import com.smanzana.nostrummagica.world.dimension.NostrumDimensionMapper;
import com.smanzana.nostrummagica.world.dimension.NostrumSorceryDimension;
import com.smanzana.petcommand.api.PetFuncs;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPartEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.EntityTeleportEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.Cancelable;
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
	public static ItemGroup equipmentTab;
	public static ItemGroup enhancementTab;
	public static ItemGroup runeTab;
	public static ItemGroup dungeonTab;
	public static Logger logger = LogManager.getLogger(MODID);
	public static PlayerListener playerListener;
	public static PlayerStatListener statListener;
	public static MagicEffectProxy magicEffectProxy;
	public static ManaArmorListener manaArmorListener;
	
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
		
		proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
		curios = DistExecutor.safeRunForDist(() -> CuriosClientProxy::new, () -> CuriosProxy::new);
		aetheria = DistExecutor.safeRunForDist(() -> AetheriaClientProxy::new, () -> AetheriaProxy::new);
		//enderIO = DistExecutor.safeRunForDist(() -> EnderIOClientProxy::new, () -> EnderIOProxy::new);
		musica = DistExecutor.safeRunForDist(() -> MusicaClientProxy::new, () -> MusicaProxy::new);
		
		(new ModConfig()).register();
		
		playerListener = new PlayerListener();
		magicEffectProxy = new MagicEffectProxy();
		manaArmorListener = new ManaArmorListener();
		statListener = new PlayerStatListener();

		NostrumMagica.creativeTab = new ItemGroup(MODID) {
			@Override
			@OnlyIn(Dist.CLIENT)
			public ItemStack createIcon() {
				return new ItemStack(NostrumItems.spellTomeNovice);
			}
		};
		
		NostrumMagica.equipmentTab = new ItemGroup(MODID + "_equipment") {
			@Override
			@OnlyIn(Dist.CLIENT)
			public ItemStack createIcon() {
				return new ItemStack(NostrumItems.mageStaff);
			}
		};
		
		NostrumMagica.enhancementTab = new ItemGroup(MODID + "_enhancements") {
			@Override
			@OnlyIn(Dist.CLIENT)
			public ItemStack createIcon() {
				return new ItemStack(NostrumItems.spellTomePage);
			}
		};

		NostrumMagica.runeTab = new ItemGroup(MODID + "_runes") {
			@Override
			@OnlyIn(Dist.CLIENT)
			public ItemStack createIcon() {
				return SpellRune.getRune(NostrumSpellShapes.Touch);
			}
		};
		
		NostrumMagica.dungeonTab = new ItemGroup(MODID + "_dungeon") {
			@Override
			@OnlyIn(Dist.CLIENT)
			public ItemStack createIcon() {
				return new ItemStack(NostrumItems.worldKey);
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
		
		IInventory curios = NostrumMagica.instance.curios.getCurios(player);
		if (curios != null) {
			for (int i = 0; i < curios.getSizeInventory(); i++) {
				ItemStack equip = curios.getStackInSlot(i);
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

	public static boolean removeReagents(PlayerEntity player, ReagentType type, int count) {
		if (getReagentCount(player, type) < count)
			return false;
		
		IInventory curios = NostrumMagica.instance.curios.getCurios(player);
		if (curios != null) {
			for (int i = 0; i < curios.getSizeInventory(); i++) {
				ItemStack equip = curios.getStackInSlot(i);
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

	private List<Runnable> researchReloadHooks = new LinkedList<>();

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
	public static boolean getQuestAvailable(PlayerEntity player, NostrumQuest quest) {
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
	public static boolean canTakeQuest(PlayerEntity player, NostrumQuest quest) {
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

	public static boolean canCast(Spell spell, INostrumMagic attr, @Nonnull List<ITextComponent> problemsOut) {
		boolean success = true;

		Map<EMagicElement, EElementalMastery> neededMasteries = new EnumMap<>(EMagicElement.class);

		for (SpellEffectPart part : spell.getSpellEffectParts()) {
			EMagicElement elem = part.getElement();
			if (elem == null)
				elem = EMagicElement.PHYSICAL;
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
					&& attr.hasSkill(NostrumSkills.Spellcasting_ElemFree)) {
				neededMastery = null;
			}
			
			if (neededMastery == null) {
				continue;
			}
			
			final EElementalMastery currentMastery = attr.getElementalMastery(elem);
			if (!currentMastery.isGreaterOrEqual(neededMastery)) {
				success = false;
				problemsOut.add(new TranslationTextComponent("info.spell.low_mastery", neededMastery.getName(), elem.getName(), currentMastery.getName()));
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

	public PlayerStatTracker getPlayerStats() {
		if (playerStats == null) {
			if (proxy.isServer()) {
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

		serverDimensionMapper = mapper;
		return mapper;
	}

	private void initSpellRegistry(World world) {
		spellRegistry = (SpellRegistry) ((ServerWorld) world).getServer().getWorld(World.OVERWORLD).getSavedData().getOrCreate(SpellRegistry::new,
				SpellRegistry.DATA_NAME);
	}

	private void initPetSoulRegistry(World world) {
		petSoulRegistry = (PetSoulRegistry) ((ServerWorld) world).getServer().getWorld(World.OVERWORLD).getSavedData().getOrCreate(PetSoulRegistry::new,
				PetSoulRegistry.DATA_NAME);
	}

	private void initPlayerStats(World world) {
		playerStats = (PlayerStatTracker) ((ServerWorld) world).getServer().getWorld(World.OVERWORLD).getSavedData().getOrCreate(PlayerStatTracker::new,
				PlayerStatTracker.DATA_NAME);
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
			initPlayerStats(world);
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
		PortalBlock.resetTimers();
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
    	private final Vector3d from;
    	private final Vector3d to;
    	
    	public NostrumTeleportedOtherEvent(Entity entity, LivingEntity causingEntity, Vector3d from, Vector3d to) {
    		super(entity);
    		this.causingEntity = causingEntity;
    		this.from = from;
    		this.to = to;
    	}
    	
    	public @Nonnull LivingEntity getCausingEntity() {
    		return this.causingEntity;
    	}

		public Vector3d getFrom() {
			return from;
		}

		public Vector3d getTo() {
			return to;
		}
    	
    }
	
	public static NostrumTeleportEvent fireTeleportAttemptEvent(Entity entity, double targetX, double targetY, double targetZ, @Nullable LivingEntity causingEntity) {
		NostrumTeleportEvent event = new NostrumTeleportEvent(entity, targetX, targetY, targetZ, causingEntity);
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}
	
	public static NostrumTeleportedOtherEvent fireTeleprotedOtherEvent(Entity entity, LivingEntity cause, Vector3d from, Vector3d to) {
		NostrumTeleportedOtherEvent event = new NostrumTeleportedOtherEvent(entity, cause, from, to);
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}

	public static boolean attemptTeleport(World world, BlockPos target, PlayerEntity player, boolean allowPortal,
			boolean spawnBristle, @Nullable LivingEntity causingEntity) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		boolean success = false;

		if (allowPortal && attr != null && attr.hasEnhancedTeleport()) {
			BlockPos portal = TemporaryTeleportationPortalBlock.spawnNearby(world, player.getPosition().up(), 4, true,
					target, 20 * 30);
			if (portal != null) {
				TemporaryTeleportationPortalBlock.spawnNearby(world, target, 4, true, portal, 20 * 30);
				success = true;
			}
		} else {
			NostrumTeleportEvent event = fireTeleportAttemptEvent(player, target.getX() + .5, target.getY() + .1, target.getZ() + .5, causingEntity);
			if (!event.isCanceled()) {
				event.getEntity().setPositionAndUpdate(event.getTargetX(), event.getTargetY(), event.getTargetZ());
				success = true;
				if (causingEntity != null) {
					fireTeleprotedOtherEvent(event.getEntity(), causingEntity, event.getPrev(), event.getTarget());
				}
			}
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
	
	public SpellCooldownTracker getSpellCooldownTracker(World world) {
		if (world.isRemote()) {
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
	
	public static final @Nonnull ResourceLocation Loc(String path) {
		return new ResourceLocation(NostrumMagica.MODID, path);
	}
}
