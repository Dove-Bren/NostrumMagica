package com.smanzana.nostrummagica;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.smanzana.nostrummagica.aetheria.AetheriaProxy;
import com.smanzana.nostrummagica.baubles.BaublesProxy;
import com.smanzana.nostrummagica.baubles.items.ItemMagicBauble;
import com.smanzana.nostrummagica.baubles.items.ItemMagicBauble.ItemType;
import com.smanzana.nostrummagica.blocks.Candle;
import com.smanzana.nostrummagica.blocks.DungeonBlock;
import com.smanzana.nostrummagica.blocks.LoreTable;
import com.smanzana.nostrummagica.blocks.MimicBlock;
import com.smanzana.nostrummagica.blocks.ModificationTable;
import com.smanzana.nostrummagica.blocks.NostrumPortal;
import com.smanzana.nostrummagica.blocks.PutterBlock;
import com.smanzana.nostrummagica.blocks.SorceryPortal;
import com.smanzana.nostrummagica.blocks.TeleportRune;
import com.smanzana.nostrummagica.capabilities.AttributeProvider;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.command.CommandAllQuests;
import com.smanzana.nostrummagica.command.CommandCreateGeotoken;
import com.smanzana.nostrummagica.command.CommandEnhanceTome;
import com.smanzana.nostrummagica.command.CommandForceBind;
import com.smanzana.nostrummagica.command.CommandGiveResearchpoint;
import com.smanzana.nostrummagica.command.CommandGiveSkillpoint;
import com.smanzana.nostrummagica.command.CommandGotoDungeon;
import com.smanzana.nostrummagica.command.CommandReadRoom;
import com.smanzana.nostrummagica.command.CommandReloadResearch;
import com.smanzana.nostrummagica.command.CommandSetDimension;
import com.smanzana.nostrummagica.command.CommandSetLevel;
import com.smanzana.nostrummagica.command.CommandSpawnDungeon;
import com.smanzana.nostrummagica.command.CommandSpawnObelisk;
import com.smanzana.nostrummagica.command.CommandTestConfig;
import com.smanzana.nostrummagica.command.CommandUnlock;
import com.smanzana.nostrummagica.command.CommandUnlockAll;
import com.smanzana.nostrummagica.command.CommandWriteRoom;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.entity.EntityKoid;
import com.smanzana.nostrummagica.entity.IEntityTameable;
import com.smanzana.nostrummagica.entity.dragon.EntityTameDragonRed;
import com.smanzana.nostrummagica.entity.dragon.ITameDragon;
import com.smanzana.nostrummagica.entity.golem.EntityGolem;
import com.smanzana.nostrummagica.items.AltarItem;
import com.smanzana.nostrummagica.items.BlankScroll;
import com.smanzana.nostrummagica.items.ChalkItem;
import com.smanzana.nostrummagica.items.DragonEggFragment;
import com.smanzana.nostrummagica.items.EnchantedArmor;
import com.smanzana.nostrummagica.items.EnchantedWeapon;
import com.smanzana.nostrummagica.items.EssenceItem;
import com.smanzana.nostrummagica.items.HookshotItem;
import com.smanzana.nostrummagica.items.HookshotItem.HookshotType;
import com.smanzana.nostrummagica.items.InfusedGemItem;
import com.smanzana.nostrummagica.items.MageStaff;
import com.smanzana.nostrummagica.items.MagicArmorBase;
import com.smanzana.nostrummagica.items.MagicCharm;
import com.smanzana.nostrummagica.items.MagicSwordBase;
import com.smanzana.nostrummagica.items.MasteryOrb;
import com.smanzana.nostrummagica.items.MirrorItem;
import com.smanzana.nostrummagica.items.MirrorShield;
import com.smanzana.nostrummagica.items.MirrorShieldImproved;
import com.smanzana.nostrummagica.items.NostrumResourceItem;
import com.smanzana.nostrummagica.items.NostrumResourceItem.ResourceType;
import com.smanzana.nostrummagica.items.NostrumRoseItem;
import com.smanzana.nostrummagica.items.NostrumRoseItem.RoseType;
import com.smanzana.nostrummagica.items.NostrumSkillItem;
import com.smanzana.nostrummagica.items.NostrumSkillItem.SkillItemType;
import com.smanzana.nostrummagica.items.PositionCrystal;
import com.smanzana.nostrummagica.items.PositionToken;
import com.smanzana.nostrummagica.items.ReagentBag;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.items.RuneBag;
import com.smanzana.nostrummagica.items.ShrineSeekingGem;
import com.smanzana.nostrummagica.items.SpellPlate;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.items.SpellTomePage;
import com.smanzana.nostrummagica.items.ThanoPendant;
import com.smanzana.nostrummagica.items.ThanosStaff;
import com.smanzana.nostrummagica.items.WarlockSword;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy;
import com.smanzana.nostrummagica.listeners.PlayerListener;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
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
import com.smanzana.nostrummagica.research.NostrumResearch;
import com.smanzana.nostrummagica.research.NostrumResearch.NostrumResearchTab;
import com.smanzana.nostrummagica.research.NostrumResearch.Size;
import com.smanzana.nostrummagica.research.NostrumResearch.SpellSpec;
import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.rituals.RitualRegistry;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeBindSpell;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeConstructGeotoken;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeCreateObelisk;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeCreatePortal;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeCreateTome;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeMark;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomePotionEffect;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeRecall;
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
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.SpellRegistry;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.spells.components.shapes.AoEShape;
import com.smanzana.nostrummagica.spells.components.shapes.ChainShape;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spelltome.enhancement.SpellTomeEnhancement;
import com.smanzana.nostrummagica.spelltome.enhancement.SpellTomeEnhancementWrapper;
import com.smanzana.nostrummagica.trials.ShrineTrial;
import com.smanzana.nostrummagica.trials.TrialEarth;
import com.smanzana.nostrummagica.trials.TrialEnder;
import com.smanzana.nostrummagica.trials.TrialFire;
import com.smanzana.nostrummagica.trials.TrialIce;
import com.smanzana.nostrummagica.trials.TrialLightning;
import com.smanzana.nostrummagica.trials.TrialPhysical;
import com.smanzana.nostrummagica.trials.TrialWind;
import com.smanzana.nostrummagica.world.NostrumChunkLoader;
import com.smanzana.nostrummagica.world.NostrumDungeonGenerator.DungeonGen;
import com.smanzana.nostrummagica.world.NostrumLootHandler;
import com.smanzana.nostrummagica.world.dimension.NostrumDimensionMapper;
import com.smanzana.nostrummagica.world.dimension.NostrumEmptyDimension;
import com.smanzana.nostrummagica.world.dungeon.room.DungeonRoomRegistry;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

@Mod(modid = NostrumMagica.MODID, version = NostrumMagica.VERSION, guiFactory = "com.smanzana.nostrummagica.config.ConfigGuiFactory", dependencies = "after:Baubles")
public class NostrumMagica
{
    public static final String MODID = "nostrummagica";
    public static final String VERSION = "1.0";
	public static final Random rand = new Random();
    
    @SidedProxy(clientSide="com.smanzana.nostrummagica.proxy.ClientProxy", serverSide="com.smanzana.nostrummagica.proxy.CommonProxy")
    public static CommonProxy proxy;
    public static NostrumMagica instance;
    @SidedProxy(clientSide="com.smanzana.nostrummagica.baubles.BaublesClientProxy", serverSide="com.smanzana.nostrummagica.baubles.BaublesProxy")
    public static BaublesProxy baubles;
    @SidedProxy(clientSide="com.smanzana.nostrummagica.aetheria.AetheriaClientProxy", serverSide="com.smanzana.nostrummagica.aetheria.AetheriaProxy")
    public static AetheriaProxy aetheria;
    
    public static CreativeTabs creativeTab;
    public static CreativeTabs enhancementTab;
    public static Logger logger = LogManager.getLogger(MODID);
    public static PlayerListener playerListener;
    public static MagicEffectProxy magicEffectProxy;
    
    // Cached references that have sketchy access rules. See uses in this file.
    private static SpellRegistry spellRegistry;
    private static NostrumDimensionMapper serverDimensionMapper;
    
    public static boolean initFinished = false;
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
        aetheria.init();
        baubles.init();
        new NostrumLootHandler();
        NostrumDimensionMapper.registerDimensions();
    }
    
    @EventHandler
    public void preinit(FMLPreInitializationEvent event) {
    	instance = this;
    	playerListener = new PlayerListener();
    	magicEffectProxy = new MagicEffectProxy();
    	
    	NostrumMagica.creativeTab = new CreativeTabs(MODID){
	    	@Override
	        @SideOnly(Side.CLIENT)
	        public Item getTabIconItem(){
	    		return SpellTome.instance();
	        }
	    };
	    SpellTome.instance().setCreativeTab(NostrumMagica.creativeTab);
	    NostrumMagica.enhancementTab = new CreativeTabs(MODID + "_enhancements") {
	    	@Override
	    	@SideOnly(Side.CLIENT)
	    	public Item getTabIconItem() {
	    		return SpellTomePage.instance();
	    	}
	    };
	    SpellTomePage.instance().setCreativeTab(NostrumMagica.enhancementTab);
	    
	    if (Loader.isModLoaded("Baubles")) {
	    	baubles.enable();
	    }
	    if (Loader.isModLoaded("nostrumaetheria")) {
	    	aetheria.enable();
	    }
	    
    	new ModConfig(new Configuration(event.getSuggestedConfigurationFile()));
    	
    	proxy.preinit();
    	aetheria.preInit();
    	baubles.preInit();
    	
    	DungeonRoomRegistry.instance().loadRegistryFromDisk();
    	
    	RitualRegistry.instance();
    	
    	registerDefaultRituals();
    	registerDefaultQuests();
    	registerDefaultTrials();
    	registerDefaultResearch();

    	NostrumChunkLoader.instance();
    	
    	SpellTomeEnhancement.initDefaultEnhancements();
    	
    }
    
    @EventHandler
    public void postinit(FMLPostInitializationEvent event) {
    	proxy.postinit();
    	aetheria.postInit();
    	baubles.postInit();
    	
    	initFinished = true;
    	
    	MinecraftForge.EVENT_BUS.register(this);
    }
    
    @EventHandler
    public void startup(FMLServerStartingEvent event) {
    	event.registerServerCommand(new CommandGotoDungeon());
    	event.registerServerCommand(new CommandTestConfig());
    	event.registerServerCommand(new CommandSpawnObelisk());
    	event.registerServerCommand(new CommandEnhanceTome());
    	event.registerServerCommand(new CommandSetLevel());
    	event.registerServerCommand(new CommandUnlock());
    	event.registerServerCommand(new CommandGiveSkillpoint());
    	event.registerServerCommand(new CommandAllQuests());
    	event.registerServerCommand(new CommandCreateGeotoken());
    	event.registerServerCommand(new CommandForceBind());
    	event.registerServerCommand(new CommandSpawnDungeon());
    	event.registerServerCommand(new CommandUnlockAll());
    	event.registerServerCommand(new CommandSetDimension());
    	event.registerServerCommand(new CommandWriteRoom());
    	event.registerServerCommand(new CommandReadRoom());
    	event.registerServerCommand(new CommandGiveResearchpoint());
    	event.registerServerCommand(new CommandReloadResearch());
    }
    
    /**
     * Convenience wrapper. Pulls out magic wrapper from an entity, if they have them
     * @param e The entity to pull off of
     * @return The attributes, if they exist. Otherwise, returns null
     * Get a null you don't expect? Make sure the server and client configs match, AND
     * that the config includes all the mobs you won't to be tagged.
     */
    public static INostrumMagic getMagicWrapper(Entity e) {
    	if (e == null)
    		return null;
    	
    	return e.getCapability(AttributeProvider.CAPABILITY, null);
    }
    
    private static int potionID = 65;
	
    public static int registerPotion(Potion potion, ResourceLocation loc) {
    	while (Potion.getPotionById(potionID) != null)
    		potionID++;
    	Potion.REGISTRY.register(potionID, loc, potion);
    	return potionID;
    }
    
    public static ItemStack findTome(EntityPlayer entity, int tomeID) {
    	// We look in mainhand first, then offhand, then just down
    	// hotbar.
    	for (ItemStack item : entity.inventory.mainInventory) {
    		if (item != null && item.getItem() instanceof SpellTome)
    			if (SpellTome.getTomeID(item) == tomeID)
    				return item;
    	}
    	
    	for (ItemStack item : entity.inventory.offHandInventory) {
    		if (item != null && item.getItem() instanceof SpellTome)
    			if (SpellTome.getTomeID(item) == tomeID)
    				return item;
    	}
    	
    	return null;
    }
    
    public static ItemStack getCurrentTome(EntityPlayer entity) {
    	// We look in mainhand first, then offhand, then just down
    	// hotbar.
    	ItemStack tome = null;
    	
    	if (entity.getHeldItemMainhand() != null &&
    			entity.getHeldItemMainhand().getItem() instanceof SpellTome) {
    		tome = entity.getHeldItemMainhand();
    	} else if (entity.getHeldItemOffhand() != null &&
    			entity.getHeldItemOffhand().getItem() instanceof SpellTome) {
    		tome = entity.getHeldItemOffhand();
    	} else {
    		// hotbar is items 0-8
    		int count = 0;
    		for (ItemStack stack : entity.inventory.mainInventory) {
        		if (stack != null && stack.getItem() instanceof SpellTome) {
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
    
    public static Spell getCurrentSpell(EntityPlayer player) {
    	List<Spell> spells = getSpells(player);
    	if (spells == null || spells.isEmpty())
    		return null;
    	
    	return spells.get(0);
    }
    
    public static int getReagentCount(EntityPlayer player, ReagentType type) {
    	int count = 0;
    	for (ItemStack item : player.inventory.mainInventory) {
    		if (item != null && item.getItem() instanceof ReagentBag) {
    			count += ReagentBag.getReagentCount(item, type);
    		}
    	}
    	for (ItemStack item : player.inventory.offHandInventory) {
    		if (item != null && item.getItem() instanceof ReagentBag) {
    			count += ReagentBag.getReagentCount(item, type);
    		}
    	}
    	for (ItemStack item : player.inventory.mainInventory) {
    		if (item != null && item.getItem() instanceof ReagentItem
    				&& ReagentItem.findType(item) == type) {
    			count += item.stackSize;
    		}
    	}
    	for (ItemStack item : player.inventory.offHandInventory) {
    		if (item != null && item.getItem() instanceof ReagentBag
    				&& ReagentItem.findType(item) == type) {
    			count += item.stackSize;
    		}
    	}
    	
    	return count;
    }
    
    public static boolean removeReagents(EntityPlayer player, ReagentType type, int count) {
    	if (getReagentCount(player, type) < count)
    		return false;
    	
		for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
			ItemStack item = player.inventory.getStackInSlot(i);
			if (item == null)
				continue;
			
			if (item.getItem() instanceof ReagentBag) {
				count = ReagentBag.removeCount(item, type, count);
			} else if (item.getItem() instanceof ReagentItem) {
				if (ReagentItem.getTypeFromMeta(item.getMetadata())
					== type) {
					if (item.stackSize > count) {
						item.stackSize -= count;
						count = 0;
						break;
					} else {
						count -= item.stackSize;
						player.inventory.setInventorySlotContents(i, null);
					}
				}
			}
			
			if (count == 0)
				break;
		}
		
		return count == 0;
    }
    
    
    public static List<Spell> getSpells(EntityPlayer entity) {
    	if (entity == null)
    		return null;
    	
    	// We just return the spells from the curernt tome.
    	ItemStack tome = getCurrentTome(entity);
    	
    	if (tome == null)
    		return null;
    	
    	return SpellTome.getSpells(tome);
    	
    }
    
    public static List<NostrumQuest> getActiveQuests(EntityPlayer player) {
    	return getActiveQuests(getMagicWrapper(player));
    }
    
    public static List<NostrumQuest> getActiveQuests(INostrumMagic attr) {
    	List<NostrumQuest> list = new LinkedList<>();
    	List<String> quests = attr.getCurrentQuests();
    	
    	if (quests != null && !quests.isEmpty()) 
    	for (String quest : quests){
    		NostrumQuest q = NostrumQuest.lookup(quest);
    		if (q != null)
    			list.add(q);
    	}
    	
    	return list;
    }
    
    public static List<NostrumQuest> getCompletedQuests(EntityPlayer player) {
    	return getCompletedQuests(getMagicWrapper(player));
    }
    
    public static List<NostrumQuest> getCompletedQuests(INostrumMagic attr) {
    	List<NostrumQuest> list = new LinkedList<>();
    	List<String> quests = attr.getCompletedQuests();
    	
    	if (quests != null && !quests.isEmpty()) 
    	for (String quest : quests){
    		NostrumQuest q = NostrumQuest.lookup(quest);
    		if (q != null)
    			list.add(q);
    	}
    	
    	return list;
    }
    
    public static List<NostrumResearch> getCompletedResearch(EntityPlayer player) {
    	return getCompletedResearch(getMagicWrapper(player));
    }
    
    public static List<NostrumResearch> getCompletedResearch(INostrumMagic attr) {
    	List<NostrumResearch> list = new LinkedList<>();
    	List<String> research = attr.getCompletedResearches();
    	
    	if (research != null && !research.isEmpty()) 
    	for (String researchKey : research){
    		NostrumResearch r = NostrumResearch.lookup(researchKey);
    		if (r != null)
    			list.add(r);
    	}
    	
    	return list;
    }
    
    private void registerDefaultRituals() {
		RitualRecipe recipe;
		
		for (EMagicElement element : EMagicElement.values()) {
			recipe = RitualRecipe.createTier2("rune." + element.name().toLowerCase(),
					SpellRune.getRune(element, 1),
					null,
					new ReagentType[] {ReagentType.CRYSTABLOOM, ReagentType.MANDRAKE_ROOT, ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST},
					EssenceItem.getEssence(element, 1),
					IRitualRequirement.AND(
							new RRequirementElementMastery(element),
							new RRequirementResearch("spellrunes")
					),
					new OutcomeSpawnItem(SpellRune.getRune(element, 1)));
			RitualRegistry.instance().addRitual(recipe);
		}
		
		// Shape Runes
		recipe = RitualRecipe.createTier2("rune.single",
				SpellRune.getRune(SingleShape.instance()),
				null,
				new ReagentType[] {ReagentType.GINSENG, ReagentType.GINSENG, ReagentType.SPIDER_SILK, ReagentType.SKY_ASH},
				NostrumResourceItem.getItem(ResourceType.TOKEN, 1),
				IRitualRequirement.AND(
						new RRequirementShapeMastery(SingleShape.instance()),
						new RRequirementResearch("spellrunes")
				),
				new OutcomeSpawnItem(SpellRune.getRune(SingleShape.instance())));
		RitualRegistry.instance().addRitual(recipe);
		
		recipe = RitualRecipe.createTier3("rune.chain",
				SpellRune.getRune(ChainShape.instance()),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.SPIDER_SILK, ReagentType.MANDRAKE_ROOT},
				SpellRune.getRune(SingleShape.instance()),
				new ItemStack[] {SpellRune.getRune(SingleShape.instance()), new ItemStack(Items.GOLD_INGOT, 1), SpellRune.getRune(SingleShape.instance()), SpellRune.getRune(SingleShape.instance())},
				IRitualRequirement.AND(
						new RRequirementShapeMastery(ChainShape.instance()),
						new RRequirementResearch("spellrunes")
				),
				new OutcomeSpawnItem(SpellRune.getRune(ChainShape.instance())));
		RitualRegistry.instance().addRitual(recipe);
		
		recipe = RitualRecipe.createTier3("rune.aoe",
				SpellRune.getRune(AoEShape.instance()),
				null,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.GRAVE_DUST, ReagentType.SPIDER_SILK, ReagentType.MANDRAKE_ROOT},
				SpellRune.getRune(ChainShape.instance()),
				new ItemStack[] {SpellRune.getRune(ChainShape.instance()), new ItemStack(Items.DIAMOND, 1), SpellRune.getRune(SingleShape.instance()), SpellRune.getRune(ChainShape.instance())},
				IRitualRequirement.AND(
						new RRequirementShapeMastery(AoEShape.instance()),
						new RRequirementResearch("spellrunes")
				),
				new OutcomeSpawnItem(SpellRune.getRune(AoEShape.instance())));
		RitualRegistry.instance().addRitual(recipe);
		
		for (EAlteration alteration : EAlteration.values()) {
			recipe = RitualRecipe.createTier2("rune." + alteration.name().toLowerCase(),
					SpellRune.getRune(alteration),
					null,
					new ReagentType[] {ReagentType.GINSENG, ReagentType.GRAVE_DUST, ReagentType.SKY_ASH, ReagentType.GRAVE_DUST},
					alteration.getReagents().get(0),
					IRitualRequirement.AND(
							new RRequirementAlterationMastery(alteration),
							new RRequirementResearch("spellrunes")
					),
					new OutcomeSpawnItem(SpellRune.getRune(alteration, 1)));
			RitualRegistry.instance().addRitual(recipe);
		}
		
		for (SpellTrigger trigger : SpellTrigger.getAllTriggers()) {
			recipe = RitualRecipe.createTier3("rune." + trigger.getTriggerKey().toLowerCase(),
					SpellRune.getRune(trigger),
					null,
					new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.GINSENG, ReagentType.GINSENG},
					NostrumResourceItem.getItem(ResourceType.TOKEN, 1),
					new ItemStack[] {new ItemStack(Items.GOLD_NUGGET), trigger.getCraftItem(), NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), new ItemStack(Items.GOLD_NUGGET, 1)},
					IRitualRequirement.AND(
							new RRequirementTriggerMastery(trigger),
							new RRequirementResearch("spellrunes")
					),
					new OutcomeSpawnItem(SpellRune.getRune(trigger	, 1)));
			RitualRegistry.instance().addRitual(recipe);
		}
		
		
		// Boons
		{
			recipe = RitualRecipe.createTier1("buff.luck",
					new ItemStack(Items.RABBIT_FOOT),
					EMagicElement.PHYSICAL,
					ReagentType.SPIDER_SILK,
					new RRequirementResearch("boon"),
					new OutcomePotionEffect(Potion.getPotionFromResourceLocation("luck"), 0, 120 * 20));
			RitualRegistry.instance().addRitual(recipe);

			recipe = RitualRecipe.createTier1("buff.speed",
					new ItemStack(Items.ARROW),
					EMagicElement.WIND,
					ReagentType.SKY_ASH,
					new RRequirementResearch("boon"),
					new OutcomePotionEffect(Potion.getPotionFromResourceLocation("speed"), 0, 120 * 20));
			RitualRegistry.instance().addRitual(recipe);

			recipe = RitualRecipe.createTier1("buff.strength",
					new ItemStack(Items.IRON_SWORD),
					EMagicElement.FIRE,
					ReagentType.MANDRAKE_ROOT,
					new RRequirementResearch("boon"),
					new OutcomePotionEffect(Potion.getPotionFromResourceLocation("strength"), 0, 120 * 20));
			RitualRegistry.instance().addRitual(recipe);

			recipe = RitualRecipe.createTier1("buff.leaping",
					new ItemStack(Item.getItemFromBlock(Blocks.QUARTZ_STAIRS)),
					EMagicElement.LIGHTNING,
					ReagentType.MANI_DUST,
					new RRequirementResearch("boon"),
					new OutcomePotionEffect(Potion.getPotionFromResourceLocation("jump_boost"), 0, 120 * 20));
			RitualRegistry.instance().addRitual(recipe);

			recipe = RitualRecipe.createTier1("buff.regen",
					new ItemStack(Items.GOLDEN_APPLE),
					EMagicElement.EARTH,
					ReagentType.GINSENG,
					new RRequirementResearch("boon"),
					new OutcomePotionEffect(Potion.getPotionFromResourceLocation("regeneration"), 0, 120 * 20));
			RitualRegistry.instance().addRitual(recipe);

			recipe = RitualRecipe.createTier1("buff.fireresist",
					new ItemStack(Items.MAGMA_CREAM),
					EMagicElement.FIRE,
					ReagentType.CRYSTABLOOM,
					new RRequirementResearch("boon"),
					new OutcomePotionEffect(Potion.getPotionFromResourceLocation("fire_resistance"), 0, 120 * 20));
			RitualRegistry.instance().addRitual(recipe);

			recipe = RitualRecipe.createTier1("buff.invisibility",
					new ItemStack(Items.ENDER_EYE),
					EMagicElement.ENDER,
					ReagentType.GRAVE_DUST,
					new RRequirementResearch("boon"),
					new OutcomePotionEffect(Potion.getPotionFromResourceLocation("invisibility"), 0, 120 * 20));
			RitualRegistry.instance().addRitual(recipe);

			recipe = RitualRecipe.createTier1("buff.nightvision",
					new ItemStack(Items.GOLDEN_CARROT),
					EMagicElement.PHYSICAL,
					ReagentType.BLACK_PEARL,
					new RRequirementResearch("boon"),
					new OutcomePotionEffect(Potion.getPotionFromResourceLocation("night_vision"), 0, 120 * 20));
			RitualRegistry.instance().addRitual(recipe);

			recipe = RitualRecipe.createTier1("buff.waterbreathing",
					new ItemStack(Items.FISH),
					EMagicElement.ICE,
					ReagentType.MANI_DUST,
					new RRequirementResearch("boon"),
					new OutcomePotionEffect(Potion.getPotionFromResourceLocation("water_breathing"), 0, 120 * 20));
			RitualRegistry.instance().addRitual(recipe);
		}
		
		// Enchantment
//		{
//			recipe = RitualRecipe.createTier2("enchant.infinity", null,
//					new ReagentType[] {ReagentType.GRAVE_DUST, ReagentType.GRAVE_DUST, ReagentType.GRAVE_DUST, ReagentType.CRYSTABLOOM},
//					new ItemStack(Items.BOW),
//					new RRequirementQuest("enchant"),
//					new OutcomeEnchantItem(Enchantments.INFINITY, 1));
//			RitualRegistry.instance().addRitual(recipe);
//		}
		
		ItemStack enderpearl = new ItemStack(Items.ENDER_PEARL);
		recipe = RitualRecipe.createTier3("mark",
				new ItemStack(Items.WRITABLE_BOOK),
				EMagicElement.WIND,
				new ReagentType[] {ReagentType.GRAVE_DUST, ReagentType.MANI_DUST, ReagentType.MANI_DUST, ReagentType.CRYSTABLOOM},
				InfusedGemItem.instance().getGem(EMagicElement.EARTH, 1),
				new ItemStack[] {enderpearl, new ItemStack(Items.COMPASS), new ItemStack(Items.MAP, 1, OreDictionary.WILDCARD_VALUE), enderpearl},
				new RRequirementResearch("markrecall"),
				new OutcomeMark());
		RitualRegistry.instance().addRitual(recipe);
		
		recipe = RitualRecipe.createTier1("recall",
				new ItemStack(Items.COMPASS),
				EMagicElement.LIGHTNING,
				ReagentType.SKY_ASH,
				new RRequirementResearch("markrecall"),
				new OutcomeRecall());
		RitualRegistry.instance().addRitual(recipe);
		
		// medium crystal -- tier 2. Small crystal, reagents, basic crystal
		ItemStack crystal = NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1);
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier2("kani",
					crystal,	
					null,
					new ReagentType[] {ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST, ReagentType.GINSENG, ReagentType.GRAVE_DUST},
					NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1),
					new RRequirementResearch("kani"),
					new OutcomeSpawnItem(crystal))
				);
		
		// large crystal -- tier 3. Medium crystal, 4 medium crystals, reagents, basic crystal
		
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("vani",
					NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1),
					null,
					new ReagentType[] {ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST, ReagentType.BLACK_PEARL, ReagentType.CRYSTABLOOM},
					crystal,
					new ItemStack[] {crystal, crystal, crystal, crystal},
					new RRequirementResearch("vani"),
					new OutcomeSpawnItem(NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1)))
				);
		
		// magic token -- tier 1. Mani dust.
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier1("magic_token",
					NostrumResourceItem.getItem(ResourceType.TOKEN, 1),
					null,
					ReagentType.MANI_DUST,
					new RRequirementResearch("magic_token"),
					new OutcomeSpawnItem(NostrumResourceItem.getItem(ResourceType.TOKEN, 1)))
				);
		
		// magic token x 3 -- tier 3. 9 reagents.
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("magic_token_3",
					NostrumResourceItem.getItem(ResourceType.TOKEN, 3),
					null,
					new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.CRYSTABLOOM, ReagentType.GINSENG, ReagentType.GRAVE_DUST},
					ReagentItem.instance().getReagent(ReagentType.MANI_DUST, 1),
					new ItemStack[] {ReagentItem.instance().getReagent(ReagentType.MANDRAKE_ROOT, 1), ReagentItem.instance().getReagent(ReagentType.MANI_DUST, 1), ReagentItem.instance().getReagent(ReagentType.SKY_ASH, 1), ReagentItem.instance().getReagent(ReagentType.SPIDER_SILK, 1)},
					new RRequirementResearch("magic_token_3"),
					new OutcomeSpawnItem(NostrumResourceItem.getItem(ResourceType.TOKEN, 3)))
				);
		
		// fierce slab -- tier 3. Kani crystal. Fire + Wind gems
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("fierce_infusion",
					NostrumResourceItem.getItem(ResourceType.SLAB_FIERCE, 1),
					EMagicElement.LIGHTNING,
					new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST, ReagentType.MANDRAKE_ROOT, ReagentType.SPIDER_SILK},
					crystal,
					new ItemStack[] {InfusedGemItem.instance().getGem(EMagicElement.FIRE, 1), null, null, InfusedGemItem.instance().getGem(EMagicElement.WIND, 1)},
					new RRequirementResearch("fierce_infusion"),
					new OutcomeSpawnItem(NostrumResourceItem.getItem(ResourceType.SLAB_FIERCE, 1)))
				);
		
		// kind slab -- tier 3. Kani crystal. Ice + Earth gems
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("kind_infusion",
					NostrumResourceItem.getItem(ResourceType.SLAB_KIND, 1),
					EMagicElement.ENDER,
					new ReagentType[] {ReagentType.CRYSTABLOOM, ReagentType.GINSENG, ReagentType.MANI_DUST, ReagentType.SKY_ASH},
					crystal,
					new ItemStack[] {InfusedGemItem.instance().getGem(EMagicElement.ICE, 1), null, null, InfusedGemItem.instance().getGem(EMagicElement.EARTH, 1)},
					new RRequirementResearch("kind_infusion"),
					new OutcomeSpawnItem(NostrumResourceItem.getItem(ResourceType.SLAB_KIND, 1)))
				);
		
		// balanced slab -- tier 3. Vani crystal. Fierce and Kind slabs, + ender and lightning gems
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("balanced_infusion",
					NostrumResourceItem.getItem(ResourceType.SLAB_BALANCED, 1),	
					null,
					new ReagentType[] {ReagentType.GINSENG, ReagentType.CRYSTABLOOM, ReagentType.MANI_DUST, ReagentType.SPIDER_SILK},
					NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1),
					new ItemStack[] {NostrumResourceItem.getItem(ResourceType.SLAB_KIND, 1), InfusedGemItem.instance().getGem(EMagicElement.ENDER, 1), InfusedGemItem.instance().getGem(EMagicElement.LIGHTNING, 1), NostrumResourceItem.getItem(ResourceType.SLAB_FIERCE, 1)},
					new RRequirementResearch("balanced_infusion"),
					new OutcomeSpawnItem(NostrumResourceItem.getItem(ResourceType.SLAB_BALANCED, 1)))
				);
		
		// Thano Pendant -- tier 3. gold ingot. Paliv + Cerci fragments + 2 mani crystals.
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("thano_infusion",
					new ItemStack(ThanoPendant.instance()),
					null,
					new ReagentType[] {ReagentType.MANI_DUST, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.SPIDER_SILK},
					new ItemStack(Items.GOLD_INGOT),
					new ItemStack[] {NostrumResourceItem.getItem(ResourceType.PENDANT_LEFT, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), NostrumResourceItem.getItem(ResourceType.PENDANT_RIGHT, 1)},
					new RRequirementResearch("thano_pendant"),
					new OutcomeSpawnItem(new ItemStack(ThanoPendant.instance())))
				);
		
		// Obelisk -- tier 3. Vani crystal. Balanced slab, 2 eyes of ender, compass.
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("create_obelisk",
					new ItemStack(MirrorItem.instance()),	
					EMagicElement.ENDER,
					new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.GINSENG},
					NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1),
					new ItemStack[] {NostrumResourceItem.getItem(ResourceType.SLAB_BALANCED, 1), new ItemStack(Items.ENDER_EYE), new ItemStack(Items.ENDER_EYE), new ItemStack(Items.COMPASS)},
					new RRequirementResearch("obelisks"),
					new OutcomeCreateObelisk())
				);
		
		// GeoGem -- tier 3. Compass center. 2x Crystal, 2x reagent, Earth Crystal
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("geogem",
					new ItemStack(PositionCrystal.instance()),	
					EMagicElement.EARTH,
					new ReagentType[] {ReagentType.GRAVE_DUST, ReagentType.MANDRAKE_ROOT, ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST},
					new ItemStack(Items.COMPASS),
					new ItemStack[] {NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1), new ItemStack(ReagentItem.instance(), 1, OreDictionary.WILDCARD_VALUE), new ItemStack(ReagentItem.instance(), 1, OreDictionary.WILDCARD_VALUE), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1)},
					new RRequirementResearch("geogems"),
					new OutcomeSpawnItem(new ItemStack(PositionCrystal.instance(), 1)))
				);
		
		// GeoToken -- tier 3. Geogem center. Magic Token, earth crystal, blank scroll, diamond
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("geotoken",
					new ItemStack(PositionToken.instance()),	
					EMagicElement.EARTH,
					new ReagentType[] {ReagentType.GRAVE_DUST, ReagentType.GINSENG, ReagentType.MANDRAKE_ROOT, ReagentType.GRAVE_DUST},
					new ItemStack(PositionCrystal.instance()),
					new ItemStack[] {NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1), NostrumResourceItem.getItem(ResourceType.TOKEN, 1), InfusedGemItem.instance().getGem(EMagicElement.EARTH, 1), new ItemStack(BlankScroll.instance())},
					new RRequirementResearch("geotoken"),
					new OutcomeConstructGeotoken())
				);
		
		// Tele to obelisk -- tier 2. Position gem, reagents
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier2("teleport_obelisk",
					new ItemStack(Items.ENDER_PEARL),	
					EMagicElement.ENDER,
					new ReagentType[] {ReagentType.MANI_DUST, ReagentType.MANI_DUST, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK},
					new ItemStack(PositionCrystal.instance()),
					new RRequirementResearch("obelisk"),
					new OutcomeTeleportObelisk())
				);
		
		// Spawn Koids -- tier 3. Kani center. Magic Token, gold, gold, essence
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("koid",
					EssenceItem.getEssence(EMagicElement.ENDER, 1),	
					null,
					new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK, ReagentType.GRAVE_DUST},
					NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1),
					new ItemStack[] {new ItemStack(Items.GOLD_INGOT), NostrumResourceItem.getItem(ResourceType.TOKEN, 1), new ItemStack(EssenceItem.instance(), 1, OreDictionary.WILDCARD_VALUE), new ItemStack(Items.GOLD_INGOT, 1)},
					new RRequirementResearch("summonkoids"),
					new OutcomeSpawnEntity(new IEntityFactory() {
						@Override
						public void spawn(World world, Vec3d pos, EntityPlayer invoker) {
							EntityKoid koid = new EntityKoid(world);
							koid.setPosition(pos.xCoord, pos.yCoord, pos.zCoord);
							world.spawnEntityInWorld(koid);
							koid.setAttackTarget(invoker);
						}

						@Override
						public String getEntityName() {
							return "entity.nostrummagica.entity_koid.name";
						}
					}, 5))
				);
		
		// Mastery Orb
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("mastery_orb",
						new ItemStack(MasteryOrb.instance()),
						null,
						new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.MANI_DUST, ReagentType.MANI_DUST, ReagentType.SPIDER_SILK},
						new ItemStack(ThanoPendant.instance()),
						new ItemStack[] {new ItemStack(Items.GOLD_INGOT, 1, OreDictionary.WILDCARD_VALUE), new ItemStack(Items.ENDER_PEARL, 1, OreDictionary.WILDCARD_VALUE), new ItemStack(Items.BLAZE_POWDER, 1, OreDictionary.WILDCARD_VALUE), new ItemStack(Items.GOLD_INGOT, 1, OreDictionary.WILDCARD_VALUE)},
						new RRequirementResearch("elemental_trials"),
						new OutcomeSpawnItem(new ItemStack(MasteryOrb.instance())))
				);
		
		// Spell Tome Creation
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("tome",
					new ItemStack(SpellTome.instance()),
					null,
					new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK, ReagentType.MANI_DUST},
					new ItemStack(SpellPlate.instance(), 1, OreDictionary.WILDCARD_VALUE),
					new ItemStack[] {new ItemStack(SpellTomePage.instance()), new ItemStack(SpellTomePage.instance()), new ItemStack(SpellTomePage.instance()), new ItemStack(SpellTomePage.instance())},
					new RRequirementResearch("spelltomes_advanced"),
					new OutcomeCreateTome())
				);
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("tome",
					new ItemStack(SpellTome.instance()),
					null,
					new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK, ReagentType.MANI_DUST},
					new ItemStack(SpellPlate.instance(), 1, OreDictionary.WILDCARD_VALUE),
					new ItemStack[] {new ItemStack(SpellTomePage.instance()), new ItemStack(SpellTomePage.instance()), null, new ItemStack(SpellTomePage.instance())},
					new RRequirementResearch("spelltomes_advanced"),
					new OutcomeCreateTome())
				);
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("tome",
					new ItemStack(SpellTome.instance()),
					null,
					new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK, ReagentType.MANI_DUST},
					new ItemStack(SpellPlate.instance(), 1, OreDictionary.WILDCARD_VALUE),
					new ItemStack[] {new ItemStack(SpellTomePage.instance()), null, null, new ItemStack(SpellTomePage.instance())},
					new RRequirementResearch("spelltomes_advanced"),
					new OutcomeCreateTome())
				);
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("tome",
					new ItemStack(SpellTome.instance()),
					null,
					new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK, ReagentType.MANI_DUST},
					new ItemStack(SpellPlate.instance(), 1, OreDictionary.WILDCARD_VALUE),
					new ItemStack[] {new ItemStack(SpellTomePage.instance()), null, null, null},
					new RRequirementResearch("spelltomes_advanced"),
					new OutcomeCreateTome())
				);
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier2("tome",
					new ItemStack(SpellTome.instance()),
					null,
					new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK, ReagentType.MANI_DUST},
					new ItemStack(SpellPlate.instance(), 1, OreDictionary.WILDCARD_VALUE),
					new RRequirementResearch("spelltomes"),
					new OutcomeCreateTome())
				);
		
		// Spell Binding
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("spell_binding",
					new ItemStack(SpellTomePage.instance()),
					null,
					new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.MANI_DUST, ReagentType.SKY_ASH, ReagentType.BLACK_PEARL},
					new ItemStack(SpellTome.instance(), 1, OreDictionary.WILDCARD_VALUE),
					new ItemStack[] {new ItemStack(Items.GOLD_NUGGET), new ItemStack(SpellScroll.instance(), 1, OreDictionary.WILDCARD_VALUE), NostrumResourceItem.getItem(ResourceType.TOKEN, 1), new ItemStack(Items.GOLD_NUGGET)},
					new RRequirementResearch("spellbinding"),
					new OutcomeBindSpell())
				);
		
		// Magic Charms
		for (EMagicElement element : EMagicElement.values()) {
			RitualRegistry.instance().addRitual(
					RitualRecipe.createTier2("charm." + element.name().toLowerCase(),
							MagicCharm.getCharm(element, 1),
							null,
							new ReagentType[] {ReagentType.GRAVE_DUST, ReagentType.GRAVE_DUST, ReagentType.MANI_DUST, ReagentType.MANDRAKE_ROOT},
							EssenceItem.getEssence(element, 1),
							new RRequirementResearch("charms"), 
							new OutcomeSpawnItem(MagicCharm.getCharm(element, 8)))
					);
		}

		//Mirror from wing
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("form_primordial_mirror",
					NostrumSkillItem.getItem(SkillItemType.MIRROR, 1), null,
					new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK},
					new ItemStack(Item.getItemFromBlock(Blocks.GLASS_PANE), 1, OreDictionary.WILDCARD_VALUE),
					new ItemStack[] {NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1), NostrumSkillItem.getItem(SkillItemType.WING, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1)},
					new RRequirementResearch("stat_items_wing"),
					new OutcomeSpawnItem(NostrumSkillItem.getItem(SkillItemType.MIRROR, 1)))
				);
		
		//Mirror from roses
		for (RoseType type : RoseType.values()) {
			RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("form_primordial_mirror",
					NostrumSkillItem.getItem(SkillItemType.MIRROR, 1), null,
					new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK},
					new ItemStack(Item.getItemFromBlock(Blocks.GLASS_PANE), 1, OreDictionary.WILDCARD_VALUE),
					new ItemStack[] {NostrumRoseItem.getItem(type, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1), NostrumRoseItem.getItem(type, 1), NostrumRoseItem.getItem(type, 1)},
					new RRequirementResearch("stat_items_adv"),
					new OutcomeSpawnItem(NostrumSkillItem.getItem(SkillItemType.MIRROR, 1)))
				);
		}
		
		//Ooze
		RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("form_essential_ooze",
				NostrumSkillItem.getItem(SkillItemType.OOZE, 1), null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK},
				new ItemStack(Items.SLIME_BALL, 1, OreDictionary.WILDCARD_VALUE),
				new ItemStack[] {NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1), NostrumRoseItem.getItem(RoseType.PALE, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1)},
				new RRequirementResearch("stat_items"),
				new OutcomeSpawnItem(NostrumSkillItem.getItem(SkillItemType.OOZE, 1)))
			);
		
		//Flute
		RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("form_living_flute",
				NostrumSkillItem.getItem(SkillItemType.FLUTE, 1), null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK},
				new ItemStack(Items.REEDS, 1, OreDictionary.WILDCARD_VALUE),
				new ItemStack[] {NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1), NostrumRoseItem.getItem(RoseType.BLOOD, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1)},
				new RRequirementResearch("stat_items"),
				new OutcomeSpawnItem(NostrumSkillItem.getItem(SkillItemType.FLUTE, 1)))
			);
		
		//Pendant
		RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("form_eldrich_pendant",
				NostrumSkillItem.getItem(SkillItemType.PENDANT, 1), null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK},
				new ItemStack(Items.ENDER_PEARL, 1),
				new ItemStack[] {NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1), NostrumRoseItem.getItem(RoseType.ELDRICH, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1)},
				new RRequirementResearch("stat_items"),
				new OutcomeSpawnItem(NostrumSkillItem.getItem(SkillItemType.PENDANT, 1)))
			);
		
		// Ender pin
		RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("ender_pin",
				NostrumSkillItem.getItem(SkillItemType.ENDER_PIN, 1),
				EMagicElement.ENDER,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST, ReagentType.MANDRAKE_ROOT, ReagentType.SPIDER_SILK},
				NostrumResourceItem.getItem(ResourceType.WISP_PEBBLE, 1),
				new ItemStack[] {NostrumResourceItem.getItem(ResourceType.ENDER_BRISTLE, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1), NostrumResourceItem.getItem(ResourceType.ENDER_BRISTLE, 1), NostrumResourceItem.getItem(ResourceType.ENDER_BRISTLE, 1)},
				new RRequirementResearch("ender_pin"),
				new OutcomeSpawnItem(NostrumSkillItem.getItem(SkillItemType.ENDER_PIN, 1)))
			);
		
		//Mirror Shield
		ItemStack extra = (baubles.isEnabled() ? ItemMagicBauble.getItem(ItemType.RIBBON_SMALL, 1) : NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1));
		
		RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("mirror_shield",
				new ItemStack(MirrorShield.instance()), null,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.MANDRAKE_ROOT, ReagentType.SPIDER_SILK, ReagentType.BLACK_PEARL},
				new ItemStack(Items.SHIELD, 1),
				new ItemStack[] {extra, NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1), new ItemStack(Blocks.GLASS_PANE, 1, OreDictionary.WILDCARD_VALUE), extra},
				new RRequirementResearch("mirror_shield"),
				new OutcomeSpawnItem(new ItemStack(MirrorShield.instance())))
			);
		
		extra = (baubles.isEnabled() ? ItemMagicBauble.getItem(ItemType.RIBBON_MEDIUM, 1) : NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1));
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("true_mirror_shield",
					new ItemStack(MirrorShieldImproved.instance()), null,
					new ReagentType[] {ReagentType.MANI_DUST, ReagentType.BLACK_PEARL, ReagentType.BLACK_PEARL, ReagentType.MANI_DUST},
					new ItemStack(MirrorShield.instance()),
					new ItemStack[] {NostrumResourceItem.getItem(ResourceType.PENDANT_LEFT ,1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1), extra, NostrumResourceItem.getItem(ResourceType.PENDANT_RIGHT ,1)},
					new RRequirementResearch("true_mirror_shield"),
					new OutcomeSpawnItem(new ItemStack(MirrorShieldImproved.instance())))
				);
		
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier2("spawn_sorcery_portal",
					new ItemStack(SorceryPortal.instance()), EMagicElement.ENDER,
					new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.BLACK_PEARL, ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST},
					ShrineSeekingGem.getItemstack(DungeonGen.PORTAL),
					new RRequirementResearch("sorceryportal"),
					new OutcomeCreatePortal())
				);
		
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("spawn_warlock_sword",
					new ItemStack(WarlockSword.instance()), EMagicElement.FIRE,
					new ReagentType[] {ReagentType.MANDRAKE_ROOT, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK, ReagentType.MANI_DUST},
					new ItemStack(MagicSwordBase.instance()),
					new ItemStack[] {new ItemStack(MageStaff.instance()), NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1), new ItemStack(MageStaff.instance())},
					new RRequirementResearch("warlock_sword"),
					new OutcomeSpawnItem(new ItemStack(WarlockSword.instance())))
				);
		
		// Rituals for all the magic armors
		for (EMagicElement elem : EMagicElement.values())
		for (int i = 0; i < 3; i++)
		for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
			
			if (EnchantedArmor.isArmorElement(elem)) {
				if (slot == EntityEquipmentSlot.OFFHAND || slot == EntityEquipmentSlot.MAINHAND) {
					continue;
				}				
			} else {
				if (slot != EntityEquipmentSlot.MAINHAND) {
					continue;
				}
			}
			
			
			ItemStack outcome;
			ItemStack input;
			ItemStack gem;
			ItemStack essence;
			String name;
			String research;
			if (EnchantedArmor.isArmorElement(elem)) {
				outcome = new ItemStack(EnchantedArmor.get(elem, slot, i + 1));
				name = "spawn_enchanted_armor";
				research = "enchanted_armor";
				if (i == 0) {
					input = new ItemStack(MagicArmorBase.get(slot));
				} else {
					input = new ItemStack(EnchantedArmor.get(elem, slot, i ));
				}
			} else {
				outcome = new ItemStack(EnchantedWeapon.get(elem, i + 1));
				name = "spawn_enchanted_weapon";
				research = "enchanted_weapons";
				if (i == 0) {
					input = new ItemStack(MagicSwordBase.instance());
				} else {
					input = new ItemStack(EnchantedWeapon.get(elem, i));
				}
			}
			essence = EssenceItem.getEssence(elem, 1);
			if (i == 0) {
				gem = NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1);
			} else if (i == 1) {
				gem = NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1);
			} else {
				gem = NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1);
			}
			
			RitualRegistry.instance().addRitual(
					RitualRecipe.createTier3(name,
						outcome, elem == EMagicElement.PHYSICAL ? null : elem,
						new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.MANI_DUST, ReagentType.MANI_DUST},
						input,
						new ItemStack[] {essence, gem, essence, essence},
						new RRequirementResearch(research),
						new OutcomeSpawnItem(outcome.copy()))
					);
		}
		
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("improve_hookshot_medium",
						new ItemStack(HookshotItem.instance(), 1, HookshotItem.MakeMeta(HookshotType.MEDIUM, false)),
						EMagicElement.PHYSICAL,
						new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.MANI_DUST},
						new ItemStack(HookshotItem.instance(), 1, HookshotItem.MakeMeta(HookshotType.WEAK, false)),
						new ItemStack[] {new ItemStack(Blocks.IRON_BLOCK, 1, OreDictionary.WILDCARD_VALUE), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM,  1), null, new ItemStack(Blocks.IRON_BLOCK, 1, OreDictionary.WILDCARD_VALUE)},
						new RRequirementResearch("hookshot_medium"),
						new OutcomeSpawnItem(new ItemStack(HookshotItem.instance(), 1, HookshotItem.MakeMeta(HookshotType.MEDIUM, false)))
						)
				);
		
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("improve_hookshot_strong",
						new ItemStack(HookshotItem.instance(), 1, HookshotItem.MakeMeta(HookshotType.STRONG, false)),
						null,
						new ReagentType[] {ReagentType.MANI_DUST, ReagentType.BLACK_PEARL, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK},
						new ItemStack(HookshotItem.instance(), 1, HookshotItem.MakeMeta(HookshotType.MEDIUM, false)),
						new ItemStack[] {new ItemStack(Blocks.IRON_BLOCK, 1, OreDictionary.WILDCARD_VALUE), NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE,  1), new ItemStack(Blocks.IRON_BLOCK, 1, OreDictionary.WILDCARD_VALUE), new ItemStack(Blocks.IRON_BLOCK, 1, OreDictionary.WILDCARD_VALUE)},
						new RRequirementResearch("hookshot_strong"),
						new OutcomeSpawnItem(new ItemStack(HookshotItem.instance(), 1, HookshotItem.MakeMeta(HookshotType.STRONG, false)))
						)
				);
		
		// Reagent bag
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("reagent_bag",
						new ItemStack(ReagentBag.instance()),
						null,
						new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.MANDRAKE_ROOT, ReagentType.GINSENG, ReagentType.SPIDER_SILK},
						NostrumResourceItem.getItem(ResourceType.TOKEN, 1),
						new ItemStack[] {new ItemStack(Items.LEATHER), new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.LEATHER), new ItemStack(Items.LEATHER)},
						new RRequirementResearch("reagent_bag"),
						new OutcomeSpawnItem(new ItemStack(ReagentBag.instance()))
						)
				);
		
		// Rune bag
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("rune_bag",
						new ItemStack(RuneBag.instance()),
						null,
						new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.MANDRAKE_ROOT, ReagentType.GINSENG, ReagentType.SPIDER_SILK},
						new ItemStack(ReagentBag.instance()),
						new ItemStack[] {new ItemStack(Items.LEATHER), new ItemStack(Items.GOLD_INGOT), SpellRune.getRune(EMagicElement.FIRE, 1), new ItemStack(Items.LEATHER)},
						new RRequirementResearch("rune_bag"),
						new OutcomeSpawnItem(new ItemStack(RuneBag.instance()))
						)
				);
		
		// Mage Staff
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("mage_staff",
						new ItemStack(MageStaff.instance()),
						null,
						new ReagentType[] {ReagentType.SKY_ASH, ReagentType.BLACK_PEARL, ReagentType.GINSENG, ReagentType.MANI_DUST},
						NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1),
						new ItemStack[] {new ItemStack(Items.LEATHER), new ItemStack(Blocks.PLANKS, 1, OreDictionary.WILDCARD_VALUE), new ItemStack(Blocks.PLANKS, 1, OreDictionary.WILDCARD_VALUE), new ItemStack(Items.LEATHER)},
						new RRequirementResearch("mage_staff"),
						new OutcomeSpawnItem(new ItemStack(MageStaff.instance()))
						)
				);
		
		// Thanos Staff
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("thanos_staff",
						new ItemStack(ThanosStaff.instance()),
						null,
						new ReagentType[] {ReagentType.GRAVE_DUST, ReagentType.GRAVE_DUST, ReagentType.GRAVE_DUST, ReagentType.MANI_DUST},
						new ItemStack(ThanoPendant.instance()),
						new ItemStack[] {NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), new ItemStack(MageStaff.instance()), new ItemStack(MageStaff.instance()), NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1)},
						new RRequirementResearch("thanos_staff"),
						new OutcomeSpawnItem(new ItemStack(ThanosStaff.instance()))
						)
				);
		
		// Lore Table
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("lore_table",
						new ItemStack(LoreTable.instance()),
						null,
						new ReagentType[] {ReagentType.MANI_DUST, ReagentType.GINSENG, ReagentType.CRYSTABLOOM, ReagentType.MANI_DUST},
						NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1),
						new ItemStack[] {new ItemStack(Blocks.PLANKS, 1, OreDictionary.WILDCARD_VALUE), new ItemStack(Items.PAPER), new ItemStack(Blocks.CRAFTING_TABLE), new ItemStack(Blocks.PLANKS, 1, OreDictionary.WILDCARD_VALUE)},
						new RRequirementResearch("loretable"),
						new OutcomeSpawnItem(new ItemStack(LoreTable.instance()))
						)
				);
		
		// Modification Table
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("modification_table",
						new ItemStack(ModificationTable.instance()),
						null,
						new ReagentType[] {ReagentType.MANI_DUST, ReagentType.BLACK_PEARL, ReagentType.CRYSTABLOOM, ReagentType.BLACK_PEARL},
						NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1),
						new ItemStack[] {new ItemStack(Blocks.PLANKS, 1, OreDictionary.WILDCARD_VALUE), new ItemStack(Items.PAPER), new ItemStack(LoreTable.instance()), new ItemStack(Blocks.PLANKS, 1, OreDictionary.WILDCARD_VALUE)},
						new RRequirementResearch("modification_table"),
						new OutcomeSpawnItem(new ItemStack(ModificationTable.instance()))
						)
				);
		
		// Teleport Runes
		RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("teleportrune",
				new ItemStack(TeleportRune.instance()),
				EMagicElement.ENDER,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.BLACK_PEARL},
				new ItemStack(Blocks.CARPET, 1, OreDictionary.WILDCARD_VALUE),
				new ItemStack[] {NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), new ItemStack(Items.ENDER_PEARL), new ItemStack(ChalkItem.instance(), 1, OreDictionary.WILDCARD_VALUE), NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1)},
				new RRequirementResearch("teleportrune"),
				new OutcomeSpawnItem(new ItemStack(TeleportRune.instance(), 2)))
			);
		
		// Putter
		RitualRegistry.instance().addRitual(
			RitualRecipe.createTier2("putter",
				new ItemStack(PutterBlock.instance()),
				null,
				new ReagentType[] {ReagentType.MANDRAKE_ROOT, ReagentType.SPIDER_SILK, ReagentType.BLACK_PEARL, ReagentType.BLACK_PEARL},
				new ItemStack(Blocks.DROPPER),
				new RRequirementResearch("putter"),
				new OutcomeSpawnItem(new ItemStack(PutterBlock.instance())))
			);
		
		// Facade
		RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("mimic_facade",
				new ItemStack(MimicBlock.facade()),
				null,
				new ReagentType[] {ReagentType.SKY_ASH, ReagentType.SPIDER_SILK, ReagentType.GRAVE_DUST, ReagentType.GINSENG},
				new ItemStack(Blocks.GLASS, 1, OreDictionary.WILDCARD_VALUE),
				new ItemStack[] {new ItemStack(Blocks.PLANKS, 1, OreDictionary.WILDCARD_VALUE), NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), ReagentItem.instance().getReagent(ReagentType.MANI_DUST, 1), new ItemStack(Blocks.GLOWSTONE, 1, OreDictionary.WILDCARD_VALUE)},
				new RRequirementResearch("magicfacade"),
				new OutcomeSpawnItem(new ItemStack(MimicBlock.facade(), 8)))
			);
		
		// Door
		RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("mimic_door",
				new ItemStack(MimicBlock.door()),
				null,
				new ReagentType[] {ReagentType.GRAVE_DUST, ReagentType.CRYSTABLOOM, ReagentType.GRAVE_DUST, ReagentType.GINSENG},
				new ItemStack(Items.IRON_DOOR, 1, OreDictionary.WILDCARD_VALUE),
				new ItemStack[] {new ItemStack(MimicBlock.facade()), new ItemStack(MimicBlock.facade()), new ItemStack(MimicBlock.facade()), new ItemStack(MimicBlock.facade())},
				new RRequirementResearch("magicfacade"),
				new OutcomeSpawnItem(new ItemStack(MimicBlock.door(), 1)))
			);
		
		
//		RitualRegistry.instance().addRitual(
//				RitualRecipe.createTier2("ritual.form_obelisk.name", EMagicElement.ENDER,
//					new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK},
//					center, outcome)
//				);
	}
    
    private static void registerDefaultQuests() {
    	/*
    	 * String key,
			QuestType type,
			int reqLevel,
			int reqControl,
			int reqTechnique,
			int reqFinesse,
			String[] parentKeys,
			IObjective objective,
			IReward[] rewards
    	 */
    	new NostrumQuest("start", QuestType.REGULAR, 0, 0, 0, 0, null, null,
    			null, wrapAttribute(AwardType.MANA, 0.0500f));
    	new NostrumQuest("lvl1", QuestType.REGULAR, 2, 0, 0, 0, new String[]{"start"},
    			null, null, wrapAttribute(AwardType.MANA, 0.010f));
    	new NostrumQuest("lvl2-fin", QuestType.REGULAR, 3, 0, 0, 1, new String[]{"lvl1"},
    			null, null, wrapAttribute(AwardType.REGEN, 0.0050f));
    	new NostrumQuest("lvl2-con", QuestType.REGULAR, 3, 1, 0, 0, new String[]{"lvl1"},
    			null, null, wrapAttribute(AwardType.COST, -0.005f));
    	new NostrumQuest("lvl3", QuestType.CHALLENGE, 4, 0, 0, 0, new String[]{"lvl2-fin", "lvl2-con"},
    			null, new ObjectiveRitual("magic_token"),
    			wrapAttribute(AwardType.MANA, 0.005f));
    	new NostrumQuest("lvl4", QuestType.CHALLENGE, 5, 0, 0, 0, new String[]{"lvl3"},
    			null, new ObjectiveRitual("spell_binding"),
    			new IReward[]{new AlterationReward(EAlteration.INFLICT)});
    	
    	// LVL-finesse tree
    	new NostrumQuest("lvl6-fin", QuestType.REGULAR, 6, 0, 0, 3, new String[]{"lvl4"},
    			null, null, wrapAttribute(AwardType.REGEN, 0.005f));
    	new NostrumQuest("lvl7-fin", QuestType.REGULAR, 7, 0, 0, 4, new String[]{"lvl6-fin"},
    			null, null, wrapAttribute(AwardType.MANA, 0.010f));
    	new NostrumQuest("lvl7-fin7", QuestType.REGULAR, 7, 0, 0, 7, new String[]{"lvl7-fin"},
    			null, null, wrapAttribute(AwardType.REGEN, 0.010f));
    	new NostrumQuest("lvl10-fin10", QuestType.CHALLENGE, 10, 0, 0, 10, new String[]{"lvl7-fin7"},
    			null, new ObjectiveSpellCast().numTriggers(10).requiredElement(EMagicElement.ICE),
    			new IReward[]{new AlterationReward(EAlteration.SUPPORT)});
    	
    	// LVL-control tree
    	new NostrumQuest("lvl6-con", QuestType.REGULAR, 6, 3, 0, 0, new String[]{"lvl4"},
    			null, null, wrapAttribute(AwardType.COST, -0.005f));
    	new NostrumQuest("lvl7-con", QuestType.REGULAR, 7, 4, 0, 0, new String[]{"lvl6-con"},
    			null, null, wrapAttribute(AwardType.COST, -0.005f));
    	new NostrumQuest("lvl7-con7", QuestType.REGULAR, 7, 7, 0, 0, new String[]{"lvl7-con"},
    			null, null, wrapAttribute(AwardType.COST, -0.010f));
    	new NostrumQuest("lvl10-con10", QuestType.CHALLENGE, 10, 10, 0, 0, new String[]{"lvl7-con7"},
    			null, new ObjectiveSpellCast().numElems(10).requiredElement(EMagicElement.EARTH),
    			new IReward[]{new AlterationReward(EAlteration.RESIST)});
    	
    	// LVL main tree
    	new NostrumQuest("lvl7", QuestType.CHALLENGE, 7, 0, 0, 0, new String[]{"lvl6-con", "lvl6-fin"},
    			null, new ObjectiveRitual("kani"),
    			wrapAttribute(AwardType.MANA, 0.020f));
    	new NostrumQuest("lvl8-fin3", QuestType.REGULAR, 8, 0, 0, 3, new String[]{"lvl7"},
    			null, null, wrapAttribute(AwardType.COST, -0.005f));
    	new NostrumQuest("lvl8-fin5", QuestType.REGULAR, 8, 0, 0, 5, new String[]{"lvl7"},
    			null, null, wrapAttribute(AwardType.MANA, 0.020f));
    	new NostrumQuest("lvl10-fin6", QuestType.REGULAR, 10, 0, 0, 6, new String[]{"lvl8-fin5"},
    			null, null, wrapAttribute(AwardType.REGEN, 0.100f));
    	new NostrumQuest("lvl8-con3", QuestType.REGULAR, 8, 3, 0, 0, new String[]{"lvl7"},
    			null, null, wrapAttribute(AwardType.REGEN, 0.005f));
    	new NostrumQuest("lvl8-con5", QuestType.REGULAR, 8, 5, 0, 0, new String[]{"lvl7"},
    			null, null, wrapAttribute(AwardType.MANA, 0.040f));
    	new NostrumQuest("lvl10-con6", QuestType.REGULAR, 10, 6, 0, 0, new String[]{"lvl8-con5"},
    			null, null, wrapAttribute(AwardType.COST, -0.050f));
    	new NostrumQuest("lvl10", QuestType.REGULAR, 10, 0, 0, 0, new String[]{"lvl8-con3", "lvl8-fin3"},
    			null, null, wrapAttribute(AwardType.MANA, 0.100f));
    	
    	new NostrumQuest("con1", QuestType.REGULAR, 0,
    			1, // Control
    			0, // Technique
    			0, // Finesse
    			new String[]{"start"},
    			null, null,
    			wrapAttribute(AwardType.COST, -0.002f));
    	new NostrumQuest("con2", QuestType.REGULAR, 0,
    			2, // Control
    			0, // Technique
    			0, // Finesse
    			new String[]{"con1"},
    			null, null,
    			wrapAttribute(AwardType.MANA, 0.010f));
    	new NostrumQuest("con7", QuestType.CHALLENGE, 0,
    			7, // Control
    			0, // Technique
    			0, // Finesse
    			new String[]{"con2"},
    			null, new ObjectiveRitual("koid"),
    			wrapAttribute(AwardType.COST, -0.050f));
    	new NostrumQuest("con7-tec1", QuestType.CHALLENGE, 0,
    			7, // Control
    			1, // Technique
    			0, // Finesse
    			new String[]{"con7", "con6-tec3"},
    			null, new ObjectiveSpellCast().numElems(6).requiredElement(EMagicElement.EARTH),
    			new IReward[] {new AlterationReward(EAlteration.RUIN)});
    	new NostrumQuest("con3-tec2", QuestType.REGULAR, 0,
    			3, // Control
    			2, // Technique
    			0, // Finesse
    			new String[]{"con2"},
    			null, null,
    			wrapAttribute(AwardType.REGEN, 0.005f));
    	new NostrumQuest("con5-tec2", QuestType.REGULAR, 0,
    			5, // Control
    			2, // Technique
    			0, // Finesse
    			new String[]{"con3-tec2"},
    			null, null,
    			wrapAttribute(AwardType.COST, -0.010f));
    	new NostrumQuest("con5-tec3", QuestType.REGULAR, 0,
    			5, // Control
    			3, // Technique
    			0, // Finesse
    			new String[]{"con5-tec2", "con1-tec3"},
    			null, null,
    			wrapAttribute(AwardType.COST, -0.015f));
    	new NostrumQuest("con5-tec4", QuestType.REGULAR, 0,
    			5, // Control
    			4, // Technique
    			0, // Finesse
    			new String[]{"con5-tec3"},
    			null, null,
    			wrapAttribute(AwardType.COST, -0.015f));
    	new NostrumQuest("con6-tec3", QuestType.REGULAR, 0,
    			6, // Control
    			3, // Technique
    			0, // Finesse
    			new String[]{"con5-tec3"},
    			null, null,
    			wrapAttribute(AwardType.REGEN, 0.005f));
    	new NostrumQuest("con6-tec4", QuestType.CHALLENGE, 0,
    			6, // Control
    			4, // Technique
    			0, // Finesse
    			new String[]{"con6-tec3", "con5-tec4"},
    			null, new ObjectiveKill(EntityGolem.class, "Golem", 30),
    			new IReward[]{new AlterationReward(EAlteration.SUMMON)});
    	new NostrumQuest("con1-tec2", QuestType.REGULAR, 0,
    			1, // Control
    			2, // Technique
    			0, // Finesse
    			new String[]{"con1"},
    			null, null,
    			wrapAttribute(AwardType.COST, -0.008f));
    	new NostrumQuest("con1-tec3", QuestType.CHALLENGE, 0,
    			1, // Control
    			3, // Technique
    			0, // Finesse
    			new String[]{"con1-tec2"},
    			null, new ObjectiveSpellCast().numElems(3).requiredElement(EMagicElement.LIGHTNING),
    			wrapAttribute(AwardType.MANA, 0.030f));
    	new NostrumQuest("con1-tec5", QuestType.REGULAR, 0,
    			1, // Control
    			5, // Technique
    			0, // Finesse
    			new String[]{"con1-tec3", "fin1-tec2"},
    			null, null,
    			wrapAttribute(AwardType.COST, -0.005f));
    	
    	new NostrumQuest("tec1", QuestType.REGULAR, 0,
    			0, // Control
    			1, // Technique
    			0, // Finesse
    			new String[]{"start"},
    			null, null,
    			wrapAttribute(AwardType.MANA, 0.01f));
    	new NostrumQuest("tec7", QuestType.CHALLENGE, 0,
    			0, // Control
    			7, // Technique
    			0, // Finesse
    			new String[]{"con1-tec5", "fin1-tec5"},
    			null, new ObjectiveRitual("vani"),
    			new IReward[] {new AlterationReward(EAlteration.ENCHANT)});

    	new NostrumQuest("fin1", QuestType.REGULAR, 0,
    			0, // Control
    			0, // Technique
    			1, // Finesse
    			new String[]{"start"},
    			null, null,
    			wrapAttribute(AwardType.REGEN, 0.002f));
    	new NostrumQuest("fin3", QuestType.CHALLENGE, 0,
    			0, // Control
    			0, // Technique
    			3, // Finesse
    			new String[]{"fin1"},
    			null, new ObjectiveSpellCast().numTriggers(3).requiredAlteration(EAlteration.INFLICT),
    			wrapAttribute(AwardType.REGEN, 0.008f));
    	new NostrumQuest("fin5", QuestType.REGULAR, 0,
    			0, // Control
    			0, // Technique
    			5, // Finesse
    			new String[]{"fin3"},
    			null, null,
    			wrapAttribute(AwardType.MANA, 0.020f));
    	new NostrumQuest("fin7", QuestType.REGULAR, 0,
    			0, // Control
    			0, // Technique
    			7, // Finesse
    			new String[]{"fin5"},
    			null, null,
    			wrapAttribute(AwardType.REGEN, 0.075f));
    	new NostrumQuest("fin5-tec2", QuestType.CHALLENGE, 0,
    			0, // Control
    			2, // Technique
    			5, // Finesse
    			new String[]{"fin5", "fin2-tec3"},
    			null, new ObjectiveSpellCast().requiredShape(AoEShape.instance()),
    			new IReward[] {new AlterationReward(EAlteration.GROWTH)});
    	new NostrumQuest("fin1-tec2", QuestType.REGULAR, 0,
    			0, // Control
    			2, // Technique
    			1, // Finesse
    			new String[]{"fin1"},
    			null, null,
    			wrapAttribute(AwardType.REGEN, 0.010f));
    	new NostrumQuest("fin1-tec3", QuestType.CHALLENGE, 0,
    			0, // Control
    			3, // Technique
    			1, // Finesse
    			new String[]{"fin1-tec2"},
    			null, new ObjectiveKill(EntityKoid.class, "Koid", 5),
    			wrapAttribute(AwardType.MANA, 0.025f));
    	new NostrumQuest("fin1-tec5", QuestType.REGULAR, 0,
    			0, // Control
    			5, // Technique
    			1, // Finesse
    			new String[]{"fin1-tec3", "con1-tec2"},
    			null, null,
    			wrapAttribute(AwardType.REGEN, 0.050f));
    	new NostrumQuest("fin2-tec3", QuestType.REGULAR, 0,
    			0, // Control
    			3, // Technique
    			2, // Finesse
    			new String[]{"fin1-tec3", "fin2-tec5"},
    			null, null,
    			wrapAttribute(AwardType.COST, -0.010f));
    	new NostrumQuest("fin3-tec3", QuestType.REGULAR, 0,
    			0, // Control
    			3, // Technique
    			3, // Finesse
    			new String[]{"fin2-tec3"},
    			null, null,
    			wrapAttribute(AwardType.MANA, 0.050f));
    	new NostrumQuest("fin2-tec5", QuestType.REGULAR, 0,
    			0, // Control
    			5, // Technique
    			2, // Finesse
    			new String[]{"fin1-tec5"},
    			null, null,
    			wrapAttribute(AwardType.REGEN, 0.020f));
    	new NostrumQuest("fin3-tec6", QuestType.CHALLENGE, 0,
    			0, // Control
    			6, // Technique
    			3, // Finesse
    			new String[]{"fin2-tec5"},
    			null, new ObjectiveRitual("balanced_infusion"),
    			new IReward[] {new AlterationReward(EAlteration.CONJURE)});
    	
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
//    			new String[] {MirrorShield.instance().getLoreKey()},
//    			null,
//    			new IReward[]{new AttributeReward(AwardType.MANA, 0.025f)})
//    		.offset(4, 6);
//    	new NostrumQuest("magic_armor", QuestType.CHALLENGE, 5,
//    			0,
//    			0,
//    			0,
//    			null,
//    			new String[] {MageStaff.instance().getLoreKey()},
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
    
    private static void registerDefaultResearch() {
    	// Init tabs first
    	NostrumResearchTab.MAGICA = new NostrumResearchTab("magica", SpellTome.getItemstack(3, 1, (List<SpellTomeEnhancementWrapper>)null));
		NostrumResearchTab.MYSTICISM = new NostrumResearchTab("mysticism", NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1));
		NostrumResearchTab.OUTFITTING = new NostrumResearchTab("outfitting", new ItemStack(MageStaff.instance()));
		NostrumResearchTab.TINKERING = new NostrumResearchTab("tinkering", new ItemStack(PutterBlock.instance()));
		NostrumResearchTab.ADVANCED_MAGICA = new NostrumResearchTab("advanced_magica", new ItemStack(ThanoPendant.instance()));
		
		// Then register researches
		
		// Magica Tab
		NostrumResearch.startBuilding()
		.build("origin", NostrumResearchTab.MAGICA, Size.LARGE, 0, 0, false, new ItemStack(SpellTome.instance()));
		
		NostrumResearch.startBuilding()
			.parent("origin")
			.reference("builtin::guides::spellmaking", "info.spellmaking.name")
			.reference(SpellRune.instance())
			.reference(BlankScroll.instance())
			.reference(SpellScroll.instance())
			.reference(ReagentItem.instance())
		.build("spellcraft", NostrumResearchTab.MAGICA, Size.GIANT, -1, 1, false, new ItemStack(SpellScroll.instance()));
		
		NostrumResearch.startBuilding()
			.parent("spellcraft")
			.hiddenParent("rituals")
			.lore(SpellPlate.instance())
			.reference("builtin::guides::tomes", "info.tomes.name")
			.reference("ritual::tome", "ritual.tome.name")
			.reference(SpellTome.instance())
			.reference(SpellPlate.instance())
			.reference(SpellScroll.instance())
		.build("spelltomes", NostrumResearchTab.MAGICA, Size.NORMAL, -2, 2, false, new ItemStack(SpellPlate.instance()));
		
		NostrumResearch.startBuilding()
			.parent("spelltomes")
			.lore(SpellTomePage.instance())
			.reference("builtin::guides::tomes", "info.tomes.name")
			.reference("ritual::tome", "ritual.tome.name")
			.reference(SpellTomePage.instance())
		.build("spelltomes_advanced", NostrumResearchTab.MAGICA, Size.NORMAL, -1, 2, true, new ItemStack(SpellTomePage.instance()));
		
		NostrumResearch.startBuilding()
			.parent("spelltomes")
			.reference("builtin::guides::spellmaking", "info.spellbinding.name")
			.reference("ritual::spell_binding", "ritual.spell_binding.name")
		.build("spellbinding", NostrumResearchTab.MAGICA, Size.NORMAL, -2, 3, false, new ItemStack(SpellTome.instance()));
		
		NostrumResearch.startBuilding()
			.parent("spellcraft")
			.quest("lvl7")
			.reference(MasteryOrb.instance())
			.reference("builtin::trials::fire", "info.trial.fire.name")
			.reference("builtin::trials::ice", "info.trial.ice.name")
			.reference("builtin::trials::earth", "info.trial.earth.name")
			.reference("builtin::trials::wind", "info.trial.wind.name")
			.reference("builtin::trials::ender", "info.trial.ender.name")
			.reference("builtin::trials::lightning", "info.trial.lightning.name")
			.reference("builtin::trials::physical", "info.trial.physical.name")
		.build("elemental_trials", NostrumResearchTab.MAGICA, Size.NORMAL, -3, 2, true, new ItemStack(MasteryOrb.instance()));
		
		NostrumResearch.startBuilding()
			.parent("origin")
			.reference("builtin::guides::rituals", "info.rituals.name")
			.reference(AltarItem.instance())
			.reference(ChalkItem.instance())
			.reference(ReagentItem.instance())
			.reference(InfusedGemItem.instance())
		.build("rituals", NostrumResearchTab.MAGICA, Size.GIANT, 1, 1, false, new ItemStack(InfusedGemItem.instance()));
		
		NostrumResearch.startBuilding()
			.parent("rituals")
		.build("candles", NostrumResearchTab.MAGICA, Size.NORMAL, 2, 2, false, new ItemStack(Candle.instance()));
		
		NostrumResearch.startBuilding()
			.hiddenParent("geotokens")
			.parent("rituals")
			.lore(PositionToken.instance())
			.spellComponent(EMagicElement.ENDER, EAlteration.GROWTH)
			.spellComponent(EMagicElement.ENDER, EAlteration.SUPPORT)
			.reference("ritual::mark", "ritual.mark.name")
			.reference("ritual::recall", "ritual.recall.name")
		.build("markrecall", NostrumResearchTab.MAGICA, Size.LARGE, 4, 2, true, new ItemStack(Items.COMPASS));
		
		NostrumResearch.startBuilding()
			.parent("rituals")
			.reference("ritual::buff.luck", "ritual.buff.luck.name")
			.reference("ritual::buff.speed", "ritual.buff.speed.name")
			.reference("ritual::buff.strength", "ritual.buff.strength.name")
			.reference("ritual::buff.leaping", "ritual.buff.leaping.name")
			.reference("ritual::buff.regen", "ritual.buff.regen.name")
			.reference("ritual::buff.fireresist", "ritual.buff.fireresist.name")
			.reference("ritual::buff.invisibility", "ritual.buff.invisibility.name")
			.reference("ritual::buff.nightvision", "ritual.buff.nightvision.name")
			.reference("ritual::buff.waterbreathing", "ritual.buff.waterbreathing.name")
		.build("boon", NostrumResearchTab.MAGICA, Size.LARGE, 3, 0, true, new ItemStack(Items.SPLASH_POTION));
		
		NostrumResearch.startBuilding()
			.parent("rituals")
			.hiddenParent("magic_token")
			.lore(EntityKoid.LoreKey)
			.reference("ritual::koid", "ritual.koid.name")
		.build("summonkoids", NostrumResearchTab.MAGICA, Size.NORMAL, 3, 2, true, new ItemStack(EssenceItem.instance()));
		
		NostrumResearch.startBuilding()
			.hiddenParent("spellcraft")
			.parent("rituals")
			.reference("ritual::lore_table", "ritual.lore_table.name")
		.build("loretable", NostrumResearchTab.MAGICA, Size.NORMAL, 2, 0, true, new ItemStack(LoreTable.instance()));
		
		
		// Mysticism Tab (Resources)
		NostrumResearch.startBuilding()
			.hiddenParent("rituals")
			.lore(ReagentItem.instance())
			.reference("ritual::magic_token", "ritual.magic_token.name")
		.build("magic_token", NostrumResearchTab.MYSTICISM, Size.NORMAL, -1, 0, true, NostrumResourceItem.getItem(ResourceType.TOKEN, 1));
		
		NostrumResearch.startBuilding()
			.parent("magic_token")
			.quest("lvl3")
			.reference("ritual::magic_token_3", "ritual.magic_token_3.name")
		.build("magic_token_3", NostrumResearchTab.MYSTICISM, Size.NORMAL, -2, 0, true, NostrumResourceItem.getItem(ResourceType.TOKEN, 1));
		
		NostrumResearch.startBuilding()
			.parent("magic_token")
			.lore(NostrumResourceItem.instance())
			.quest("lvl3")
			.reference("ritual::kani", "ritual.kani.name")
		.build("kani", NostrumResearchTab.MYSTICISM, Size.NORMAL, -1, 1, true, NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1));
		
		NostrumResearch.startBuilding()
			.parent("kani")
			.quest("lvl7")
			.lore(NostrumResourceItem.instance())
			.reference("ritual::vani", "ritual.vani.name")
		.build("vani", NostrumResearchTab.MYSTICISM, Size.LARGE, -1, 2, true, NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1));
		
		NostrumResearch.startBuilding()
			.hiddenParent("magic_token")
			.hiddenParent("spellcraft")
			.lore(SpellRune.instance())
			.reference("ritual::rune.physical", "ritual.rune.physical.name")
			.reference("ritual::rune.single", "ritual.rune.single.name")
			.reference("ritual::rune.inflict", "ritual.rune.inflict.name")
			.reference("ritual::rune.trigger_touch", "ritual.rune.trigger_touch.name")
			.reference("ritual::rune.trigger_self", "ritual.rune.trigger_self.name")
		.build("spellrunes", NostrumResearchTab.MYSTICISM, Size.GIANT, 0, 0, true, SpellRune.getRune(SingleShape.instance()));
		
		NostrumResearch.startBuilding()
			.hiddenParent("kani")
			.reference("ritual::fierce_infusion", "ritual.fierce_infusion.name")
		.build("fierce_infusion", NostrumResearchTab.MYSTICISM, Size.NORMAL, 1, 0, true, NostrumResourceItem.getItem(ResourceType.SLAB_FIERCE, 1));
		
		NostrumResearch.startBuilding()
			.hiddenParent("kani")
			.reference("ritual::kind_infusion", "ritual.kind_infusion.name")
		.build("kind_infusion", NostrumResearchTab.MYSTICISM, Size.NORMAL, 3, 0, true, NostrumResourceItem.getItem(ResourceType.SLAB_KIND, 1));
		
		NostrumResearch.startBuilding()
			.hiddenParent("vani")
			.parent("fierce_infusion")
			.parent("kind_infusion")
			.reference("ritual::balanced_infusion", "ritual.balanced_infusion.name")
		.build("balanced_infusion", NostrumResearchTab.MYSTICISM, Size.LARGE, 2, 1, true, NostrumResourceItem.getItem(ResourceType.SLAB_BALANCED, 1));
		
		
		// Outfitting (weapon/armor)
		NostrumResearch.startBuilding()
			.hiddenParent("rituals")
			.reference("ritual::mage_staff", "ritual.mage_staff.name")
			.lore(NostrumResourceItem.instance())
		.build("mage_staff", NostrumResearchTab.OUTFITTING, Size.NORMAL, 1, 0, true, new ItemStack(MageStaff.instance()));
		
		NostrumResearch.startBuilding()
			.parent("mage_staff")
			.hiddenParent("thano_pendant")
			.reference("ritual::thanos_staff", "ritual.thanos_staff.name")
			.lore(ThanoPendant.instance())
		.build("thanos_staff", NostrumResearchTab.OUTFITTING, Size.LARGE, 2, 1, true, new ItemStack(ThanosStaff.instance()));
		
		NostrumResearch.startBuilding()
			.parent("mage_staff")
			.parent("enchanted_weapons")
			.hiddenParent("vani")
			.reference("ritual::spawn_warlock_sword", "ritual.spawn_warlock_sword.name")
		.build("warlock_sword", NostrumResearchTab.OUTFITTING, Size.LARGE, 0, 2, true, new ItemStack(WarlockSword.instance()));
		
		NostrumResearch.startBuilding()
			.parent("enchanted_armor")
			.reference("ritual::spawn_enchanted_weapon", "ritual.spawn_enchanted_weapon.name")
		.build("enchanted_weapons", NostrumResearchTab.OUTFITTING, Size.LARGE, -1, 1, true, new ItemStack(EnchantedWeapon.get(EMagicElement.WIND, 2)));
		
		NostrumResearch.startBuilding()
			.hiddenParent("rituals")
			.quest("lvl4")
			.reference("ritual::spawn_enchanted_armor", "ritual.spawn_enchanted_armor.name")
		.build("enchanted_armor", NostrumResearchTab.OUTFITTING, Size.GIANT, -2, 0, true, new ItemStack(EnchantedArmor.get(EMagicElement.FIRE, EntityEquipmentSlot.CHEST, 2)));
		
		NostrumResearch.startBuilding()
			.parent("enchanted_armor")
			.parent(baubles.isEnabled() ? "belts" : "origin")
			.reference("ritual::mirror_shield", "ritual.mirror_shield.name")
		.build("mirror_shield", NostrumResearchTab.OUTFITTING, Size.LARGE, -3, 3, true, new ItemStack(MirrorShield.instance()));
		
		NostrumResearch.startBuilding()
			.parent("mirror_shield")
			.lore(MirrorShield.instance())
			.reference("ritual::true_mirror_shield", "ritual.true_mirror_shield.name")
		.build("true_mirror_shield", NostrumResearchTab.OUTFITTING, Size.NORMAL, -3, 4, false, new ItemStack(MirrorShieldImproved.instance()));
		
		NostrumResearch.startBuilding()
			.hiddenParent("rituals")
			.lore(ReagentItem.instance())
			.reference("ritual::reagent_bag", "ritual.reagent_bag.name")
		.build("reagent_bag", NostrumResearchTab.OUTFITTING, Size.NORMAL, -2, -1, true, new ItemStack(ReagentBag.instance()));
		
		NostrumResearch.startBuilding()
			.parent("reagent_bag")
			.lore(SpellRune.instance())
			.reference("ritual::rune_bag", "ritual.rune_bag.name")
		.build("rune_bag", NostrumResearchTab.OUTFITTING, Size.NORMAL, -3, -1, true, new ItemStack(RuneBag.instance()));
		
		NostrumResearch.startBuilding()
			.hiddenParent("rituals")
			.hiddenParent("kani")
			.lore(HookshotItem.instance())
			.reference("ritual::improve_hookshot_medium", "ritual.improve_hookshot_medium.name")
		.build("hookshot_medium", NostrumResearchTab.OUTFITTING, Size.NORMAL, 1, -1, true, new ItemStack(HookshotItem.instance(), 1, HookshotItem.MakeMeta(HookshotType.MEDIUM, false)));
		
		NostrumResearch.startBuilding()
			.parent("hookshot_medium")
			.hiddenParent("vani")
			.reference("ritual::improve_hookshot_strong", "ritual.improve_hookshot_strong.name")
		.build("hookshot_strong", NostrumResearchTab.OUTFITTING, Size.NORMAL, 2, -1, false, new ItemStack(HookshotItem.instance(), 1, HookshotItem.MakeMeta(HookshotType.STRONG, false)));
		
		NostrumResearch.startBuilding()
			.hiddenParent("magic_token")
			.lore(EssenceItem.instance())
			.reference("ritual::charm.physical", "ritual.charm.physical.name")
			.reference("ritual::charm.fire", "ritual.charm.fire.name")
			.reference("ritual::charm.ice", "ritual.charm.ice.name")
			.reference("ritual::charm.earth", "ritual.charm.earth.name")
			.reference("ritual::charm.wind", "ritual.charm.wind.name")
			.reference("ritual::charm.lightning", "ritual.charm.lightning.name")
			.reference("ritual::charm.ender", "ritual.charm.ender.name")
		.build("charms", NostrumResearchTab.OUTFITTING, Size.NORMAL, 0, 0, true, MagicCharm.getCharm(EMagicElement.ENDER, 1));
		
		
		// Tinkering
		NostrumResearch.startBuilding()
			.parent("rituals")
			.quest("lvl4")
			.reference(PositionCrystal.instance())
		.build("geogems", NostrumResearchTab.TINKERING, Size.NORMAL, 1, -1, false, new ItemStack(PositionCrystal.instance()));
		
		NostrumResearch.startBuilding()
			.parent("geogems")
			.lore(PositionCrystal.instance())
			.reference(PositionToken.instance())
		.build("geotokens", NostrumResearchTab.TINKERING, Size.LARGE, 1, 0, true, new ItemStack(PositionToken.instance()));
		
		NostrumResearch.startBuilding()
			.parent("geotokens")
			.hiddenParent("markrecall")
			.hiddenParent("balanced_infusion")
			.lore(PositionToken.instance())
			.quest("lvl10")
			.reference("builtin::guides::obelisks", "info.obelisks.name")
			.reference("ritual::create_obelisk", "ritual.create_obelisk.name")
		.build("obelisks", NostrumResearchTab.TINKERING, Size.GIANT, 2, 1, true, new ItemStack(DungeonBlock.instance(), 1, DungeonBlock.Type.DARK.ordinal()));
		
		NostrumResearch.startBuilding()
			.hiddenParent("markrecall")
			.parent("obelisks")
			.reference("ritual::spawn_sorcery_portal", "ritual.spawn_sorcery_portal.name")
		.build("sorceryportal", NostrumResearchTab.TINKERING, Size.NORMAL, 2, 2, true, new ItemStack(SorceryPortal.instance()));
		
		NostrumResearch.startBuilding()
			.parent("geotokens")
			.hiddenParent("markrecall")
			.lore(PositionToken.instance())
			.quest("lvly")
			.reference("ritual::teleportrune", "ritual.teleportrune.name")
		.build("teleportrune", NostrumResearchTab.TINKERING, Size.NORMAL, 2, -1, true, new ItemStack(TeleportRune.instance()));
		
		NostrumResearch.startBuilding()
			.hiddenParent("rituals")
			.hiddenParent("magic_token")
			.reference("ritual::putter", "ritual.putter.name")
		.build("putter", NostrumResearchTab.TINKERING, Size.NORMAL, -1, 1, true, new ItemStack(PutterBlock.instance()));
		
		NostrumResearch.startBuilding()
			.hiddenParent("rituals")
			.reference("ritual::mimic_facade", "ritual.mimic_facade.name")
			.reference("ritual::mimic_door", "ritual.mimic_door.name")
		.build("magicfacade", NostrumResearchTab.TINKERING, Size.NORMAL, -2, 1, true, new ItemStack(MimicBlock.facade()));
		
		
		// Advanced Magica
		NostrumResearch.startBuilding()
			.hiddenParent("vani")
			.hiddenParent("reagent_bag")
			.hiddenParent("mage_staff")
			.lore(NostrumResourceItem.instance())
			.lore(MasteryOrb.instance())
			.lore(EssenceItem.instance())
			.reference("ritual::thano_infusion", "ritual.thano_infusion.name")
		.build("thano_pendant", NostrumResearchTab.ADVANCED_MAGICA, Size.GIANT, 2, -1, true, new ItemStack(ThanoPendant.instance()));
		
		NostrumResearch.startBuilding()
			.hiddenParent("vani")
			.hiddenParent("loretable")
			.hiddenParent("spelltomes_advanced")
			.reference("ritual::modification_table", "ritual.modification_table.name")
		.build("modification_table", NostrumResearchTab.ADVANCED_MAGICA, Size.GIANT, 0, -1, true, new ItemStack(ModificationTable.instance()));
		
		NostrumResearch.startBuilding()
			.hiddenParent("vani")
			.lore(NostrumRoseItem.instance())
			.reference("ritual::form_essential_ooze", "ritual.form_essential_ooze.name")
			.reference("ritual::form_living_flute", "ritual.form_living_flute.name")
			.reference("ritual::form_eldrich_pendant", "ritual.form_eldrich_pendant.name")
		.build("stat_items", NostrumResearchTab.ADVANCED_MAGICA, Size.GIANT, -2, -1, true, NostrumSkillItem.getItem(SkillItemType.PENDANT, 1));
		
		NostrumResearch.startBuilding()
			.parent("stat_items")
			.reference("ritual::form_primordial_mirror", "ritual.form_primordial_mirror.name")
		.build("stat_items_adv", NostrumResearchTab.ADVANCED_MAGICA, Size.NORMAL, -2, 0, true, NostrumSkillItem.getItem(SkillItemType.MIRROR, 1));
		
		NostrumResearch.startBuilding()
			.parent("stat_items_adv")
			.lore(DragonEggFragment.instance())
			.lore(NostrumSkillItem.instance())
			.reference("ritual::form_primordial_mirror", "ritual.form_primordial_mirror.name")
		.build("stat_items_wing", NostrumResearchTab.ADVANCED_MAGICA, Size.NORMAL, -2, 1, true, NostrumSkillItem.getItem(SkillItemType.WING, 1));
		
		NostrumResearch.startBuilding()
			.parent("stat_items")
			.reference("ritual::ender_pin", "ritual.ender_pin.name")
		.build("ender_pin", NostrumResearchTab.ADVANCED_MAGICA, Size.LARGE, -3, 0, true, NostrumSkillItem.getItem(SkillItemType.ENDER_PIN, 1));
		
		//NostrumResearchTab tab, Size size, int x, int y, boolean hidden, ItemStack icon
    }
    
    private List<Function<Integer, Integer>> researchReloadHooks = new LinkedList<>();
    
    public void reloadDefaultResearch() {
    	NostrumResearch.ClearAllResearch();
    	registerDefaultResearch();
    	if (baubles.isEnabled()) {
    		baubles.reinitResearch();
    	}
    	if (aetheria.isEnabled()) {
    		aetheria.reinitResearch();
    	}
    	
    	for (Function<Integer, Integer> hook : researchReloadHooks) {
    		hook.apply(0);
    	}
    	NostrumResearch.Validate();
    }
    
    public void registerResearchReloadHook(Function<Integer, Integer> hook) {
    	this.researchReloadHooks.add(hook);
    }
    
    private static IReward[] wrapAttribute(AwardType type, float val) {
    	return new IReward[]{new AttributeReward(type, val)};
    }
    
    /**
     * Whether a quest is visible in the mirror by normal rules.
     * This is either that one of the parents of the quest has been finished, OR
     * that the quest has no parent but the conditions to take the quest are fulfilled.
     * Note: If there are lore requirements and those aren't filled, this returns false
     * even if the other two conditions are true.
     * @param player
     * @param quest
     * @return
     */
    public static boolean getQuestAvailable(EntityPlayer player, NostrumQuest quest) {
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
     * Checks whether all of the conditions required to start a quest have been fulfilled.
     * @param player
     * @param quest
     * @return
     */
    public static boolean canTakeQuest(EntityPlayer player, NostrumQuest quest) {
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
		
		return quest.getReqLevel() <= attr.getLevel()
				&& quest.getReqControl() <= attr.getControl()
				&& quest.getReqTechnique() <= attr.getTech()
				&& quest.getReqFinesse() <= attr.getFinesse();
	}
    
    public static boolean getResearchVisible(EntityPlayer player, NostrumResearch research) {
    	// Visible if any of parents is finished (unless hidden)
    	INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null)
			return false;
    	
    	if (research.isHidden()) {
    		return canPurchaseResearch(player, research);
    	}
    	
    	String[] parents = research.getAllParents();
    	if (parents == null || parents.length == 0) {
    		return true;
    	}
    	
    	List<String> finished = attr.getCompletedResearches();
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
    
    public static boolean canPurchaseResearch(EntityPlayer player, NostrumResearch research) {
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
    				if (!attr.getAlterations().containsKey(spec.alteration) || !attr.getAlterations().get(spec.alteration)) {
    					return false;
    				}
    			} else if (spec.alteration == null) {
    				// Just element
    				if (!attr.getKnownElements().containsKey(spec.element) || !attr.getKnownElements().get(spec.element)) {
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
    		
    		if (level == 1) {
    			Boolean know = attr.getKnownElements().get(elem);
    			if (know == null || !know)
    				return false;
			} else {
				Integer mast = attr.getElementMastery().get(elem);
				int mastery = (mast == null ? 0 : mast);
				if (mastery < level)
					return false;
			}
    	}
    	
    	return true;
    }
    
    public static List<ITameDragon> getNearbyTamedDragons(EntityLivingBase entity, double blockRadius, boolean onlyOwned) {
    	List<ITameDragon> list = new LinkedList<>();
    	
    	AxisAlignedBB box = new AxisAlignedBB(entity.posX - blockRadius, entity.posY - blockRadius, entity.posZ - blockRadius,
    			entity.posX + blockRadius, entity.posY + blockRadius, entity.posZ + blockRadius);
    	

    	List<EntityTameDragonRed> dragonList = entity.worldObj.getEntitiesWithinAABB(EntityTameDragonRed.class, box, (dragon) -> {
    		return dragon instanceof ITameDragon;
    	});
    	
    	if (dragonList != null && !dragonList.isEmpty()) {
    		for (EntityTameDragonRed dragon : dragonList) {
    			ITameDragon tame = (ITameDragon) dragon;
    			
    			if (onlyOwned && (!tame.isTamed() || tame.getOwner() != entity)) {
    				continue;
    			}
    			
    			list.add((ITameDragon) dragon);
    		}
    	}
    	
    	return list;
    }
    
    public static List<EntityLivingBase> getTamedEntities(EntityLivingBase owner) {
    	List<EntityLivingBase> ents = new ArrayList<>();
    	final UUID id = owner.getUniqueID();
    	for (Entity e : owner.worldObj.loadedEntityList) {
    		if (!(e instanceof EntityLivingBase)) {
    			continue;
    		}
    		
    		EntityLivingBase ent = (EntityLivingBase) e;
    		if (ent instanceof IEntityTameable) {
    			IEntityTameable tame = (IEntityTameable) ent;
    			if (tame.isTamed() && tame.getOwnerId() != null && tame.getOwnerId().equals(id)) {
    				ents.add(ent);
    			}
    		} else if (ent instanceof EntityTameable) {
    			EntityTameable tame = (EntityTameable) ent;
    			if (tame.isTamed() && tame.isOwner(owner)) {
    				ents.add(ent);
    			}
    		} else if (ent instanceof IEntityOwnable) {
    			IEntityOwnable tame = (IEntityOwnable) ent;
    			if (tame.getOwnerId() != null && tame.getOwnerId().equals(id)) {
    				ents.add(ent);
    			}
    		}
    		
    	}
    	return ents;
    }
    
    public static SpellRegistry getSpellRegistry() {
    	if (spellRegistry == null) {
    		if (proxy.isServer()) {
    			throw new RuntimeException("Accessing SpellRegistry before a world has been loaded!");
    		} else {
    			spellRegistry = new SpellRegistry();
    		}
    	}
    	
    	return spellRegistry;
    }
    
    /**
     * Finds (or creates) the offset for a player in the sorcery dimension
     * @param player
     * @return
     */
    public static BlockPos getOrCreatePlayerDimensionSpawn(EntityPlayer player) {
    	NostrumDimensionMapper mapper = getDimensionMapper(player.worldObj);
    	
    	// Either register or fetch existing mapping
    	return mapper.register(player.getUniqueID()).getCenterPos(NostrumEmptyDimension.SPAWN_Y);
    }
    
    public static NostrumDimensionMapper getDimensionMapper(World worldAccess) {
    	if (worldAccess.isRemote) {
    		throw new RuntimeException("Accessing dimension mapper before a world has been loaded!");
    	}
    	
    	NostrumDimensionMapper mapper = (NostrumDimensionMapper) worldAccess.getMapStorage().getOrLoadData(
    			NostrumDimensionMapper.class, NostrumDimensionMapper.DATA_NAME);
		
		if (mapper == null) { // still
			mapper = new NostrumDimensionMapper();
			worldAccess.getMapStorage().setData(NostrumDimensionMapper.DATA_NAME, mapper);
		}
		
		serverDimensionMapper = mapper;
		return mapper;
    }
    
    private void initSpellRegistry(World world) {
    	spellRegistry = (SpellRegistry) world.getMapStorage().getOrLoadData(
				SpellRegistry.class, SpellRegistry.DATA_NAME);
		
		if (spellRegistry == null) { // still
			spellRegistry = new SpellRegistry();
			world.getMapStorage().setData(SpellRegistry.DATA_NAME, spellRegistry);
		}
    }
    
    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
    	// Keeping a static reference since some places want to access the registry that don't have world info.
    	// But registry should be global anyways, so we're going to try and allow it.
    	// I'm not sure the 'right' way to use global save data like this.
    	
    	if (event.getWorld().isRemote) {
    		// Clients just get a spell registry that's empty that is constantly synced with the server's
    		// Create one if this is our first world.
    		// If in  the same session we're joining another server (or loading another save), the server thread will load and sync with us.
    		if (spellRegistry == null) {
    			spellRegistry = new SpellRegistry();
    		}
    		
    	} else {
    		// Do the correct initialization for persisted data
			initSpellRegistry(event.getWorld());
			getDimensionMapper(event.getWorld());
		}
    }
    
    @EventHandler
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
    	// world's method is stupid and always returns true. Internal func always returns true OR generates chunk. lol.
    	return (world.getChunkProvider().getLoadedChunk(pos.getX() >> 4, pos.getZ() >> 4) != null);
    }
}
