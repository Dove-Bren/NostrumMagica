package com.smanzana.nostrummagica;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.smanzana.nostrummagica.capabilities.AttributeProvider;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.command.CommandAllQuests;
import com.smanzana.nostrummagica.command.CommandEnhanceTome;
import com.smanzana.nostrummagica.command.CommandGiveSkillpoint;
import com.smanzana.nostrummagica.command.CommandSetLevel;
import com.smanzana.nostrummagica.command.CommandSpawnObelisk;
import com.smanzana.nostrummagica.command.CommandTestConfig;
import com.smanzana.nostrummagica.command.CommandUnlock;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.entity.EntityGolem;
import com.smanzana.nostrummagica.entity.EntityKoid;
import com.smanzana.nostrummagica.items.BlankScroll;
import com.smanzana.nostrummagica.items.EssenceItem;
import com.smanzana.nostrummagica.items.InfusedGemItem;
import com.smanzana.nostrummagica.items.NostrumResourceItem;
import com.smanzana.nostrummagica.items.NostrumResourceItem.ResourceType;
import com.smanzana.nostrummagica.items.PositionCrystal;
import com.smanzana.nostrummagica.items.ReagentBag;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.items.SeekerIdol;
import com.smanzana.nostrummagica.items.SpellPlate;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.items.SpellTomePage;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy;
import com.smanzana.nostrummagica.listeners.PlayerListener;
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
import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.rituals.RitualRegistry;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeBindSpell;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeConstructGeotoken;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeCreateObelisk;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeCreateTome;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeEnchantItem;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeMark;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomePotionEffect;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeRecall;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeSpawnEntity;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeSpawnEntity.IEntityFactory;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeSpawnItem;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeTeleportObelisk;
import com.smanzana.nostrummagica.rituals.requirements.RRequirementAlterationMastery;
import com.smanzana.nostrummagica.rituals.requirements.RRequirementElementMastery;
import com.smanzana.nostrummagica.rituals.requirements.RRequirementQuest;
import com.smanzana.nostrummagica.rituals.requirements.RRequirementShapeMastery;
import com.smanzana.nostrummagica.rituals.requirements.RRequirementTriggerMastery;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.SpellRegistry;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.spells.components.shapes.AoEShape;
import com.smanzana.nostrummagica.spells.components.shapes.ChainShape;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spelltome.enhancement.SpellTomeEnhancement;
import com.smanzana.nostrummagica.world.NostrumChunkLoader;
import com.smanzana.nostrummagica.world.dungeon.NostrumLootHandler;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

@Mod(modid = NostrumMagica.MODID, version = NostrumMagica.VERSION, guiFactory = "com.smanzana.nostrummagica.config.ConfigGuiFactory")
public class NostrumMagica
{
    public static final String MODID = "nostrummagica";
    public static final String VERSION = "1.0";
	public static final Random rand = new Random();
    
    @SidedProxy(clientSide="com.smanzana.nostrummagica.proxy.ClientProxy", serverSide="com.smanzana.nostrummagica.proxy.CommonProxy")
    public static CommonProxy proxy;
    public static NostrumMagica instance;
    
    public static CreativeTabs creativeTab;
    public static CreativeTabs enhancementTab;
    public static Logger logger = LogManager.getLogger(MODID);
    public static PlayerListener playerListener;
    public static MagicEffectProxy magicEffectProxy;
    
    public static SpellRegistry spellRegistry;
    private File spellRegistryFile; 
    private File seekerRegistryFile;
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
        new NostrumLootHandler();
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
    	
    	proxy.preinit();
    	
    	spellRegistry = new SpellRegistry();
    	RitualRegistry.instance();
    	
    	registerDefaultRituals();
    	registerDefaultQuests();
    	
    	File dir = new File(event.getSuggestedConfigurationFile().getParentFile(), "NostrumMagica");
    	if (!dir.exists())
    		dir.mkdirs();
    	spellRegistryFile = new File(dir, "spells.dat"); 
    	loadSpellRegistry(spellRegistryFile);
    	seekerRegistryFile = new File(dir, "dungloc.dat"); 
    	//loadSeekerRegistry(spellRegistryFile);
    	
    	new ModConfig(new Configuration(event.getSuggestedConfigurationFile()));
    	
    	NostrumChunkLoader.instance();
    	
    	SpellTomeEnhancement.initDefaultEnhancements();
    }
    
    @EventHandler
    public void postinit(FMLPostInitializationEvent event) {
    	proxy.postinit();
    }
    
    @EventHandler
    public void shutdown(FMLServerStoppingEvent event) {
    	saveSpellRegistry(spellRegistryFile);
    	saveSeekerRegistry(seekerRegistryFile);
    }
    
    @EventHandler
    public void startup(FMLServerStartingEvent event) {
    	event.registerServerCommand(new CommandTestConfig());
    	event.registerServerCommand(new CommandSpawnObelisk());
    	event.registerServerCommand(new CommandEnhanceTome());
    	event.registerServerCommand(new CommandSetLevel());
    	event.registerServerCommand(new CommandUnlock());
    	event.registerServerCommand(new CommandGiveSkillpoint());
    	event.registerServerCommand(new CommandAllQuests());
    	loadSeekerRegistry(seekerRegistryFile);
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
    
    private void loadSpellRegistry(File file) {
    	if (file == null)
    		return;
    	
    	if (!file.exists())
			try {
				file.createNewFile();
			} catch (IOException e) {
				logger.error("Could not create empty file where eventually we'll save "
						+ "spells to. This is very bad!");
				e.printStackTrace();
				return;
			}
    	
    	if (file.length() == 0)
    		return;
    	
    	NBTTagCompound nbt;
    	try {
			nbt = CompressedStreamTools.read(file);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Could not read in spells data file");
			return;
		}
    	
    	spellRegistry.loadFromNBT(nbt);
    }
    
    private void saveSpellRegistry(File file) {
    	if (file == null)
    		return;
    	
    	NBTTagCompound nbt = spellRegistry.save();
    	try {
    		CompressedStreamTools.safeWrite(nbt, file);
    	} catch (IOException e) {
    		e.printStackTrace();
    		logger.error("Failed to save spell dictionary! Attempting to write "
    				+ "to temp file instead");
    		try {
				File backup = File.createTempFile("nostrummagica", "spells");
				CompressedStreamTools.write(nbt, backup);
				logger.info("\r\n\r\nSuccessfully backed up to " + backup.getAbsolutePath());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				logger.error("Failed to write backup file. Spell changes have been lost.");
			}
    	}
    }
    
    private void loadSeekerRegistry(File file) {
    	if (file == null)
    		return;
    	
    	if (!file.exists())
			try {
				file.createNewFile();
			} catch (IOException e) {
				logger.error("Could not create empty file where eventually we'll save "
						+ "dungeon locs to. This is very bad!");
				e.printStackTrace();
				return;
			}
    	
    	if (file.length() == 0)
    		return;
    	
    	NBTTagCompound nbt;
    	try {
			nbt = CompressedStreamTools.read(file);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Could not read in dungeon locations data file");
			return;
		}
    	
    	SeekerIdol.readRegistryFromNBT(nbt);
    }
    
    private void saveSeekerRegistry(File file) {
    	if (file == null)
    		return;
    	
    	NBTTagCompound nbt = SeekerIdol.saveRegistryToNBT();
    	try {
    		CompressedStreamTools.safeWrite(nbt, file);
    	} catch (IOException e) {
    		e.printStackTrace();
    		logger.error("Failed to save dungeon location dictionary! Attempting to write "
    				+ "to temp file instead");
    		try {
				File backup = File.createTempFile("nostrummagica", "dungeonlocations");
				CompressedStreamTools.write(nbt, backup);
				logger.info("\r\n\r\nSuccessfully backed up to " + backup.getAbsolutePath());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				logger.error("Failed to write backup file. Dungeon locations for seeking have been lost.");
			}
    	}
    }
    
    private void registerDefaultRituals() {
		RitualRecipe recipe;
		
		for (EMagicElement element : EMagicElement.values()) {
			recipe = RitualRecipe.createTier2("rune." + element.name().toLowerCase(),
					null,
					new ReagentType[] {ReagentType.CRYSTABLOOM, ReagentType.MANDRAKE_ROOT, ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST},
					EssenceItem.instance().getEssence(element, 1),
					new RRequirementElementMastery(element),
					new OutcomeSpawnItem(SpellRune.getRune(element, 1)));
			RitualRegistry.instance().addRitual(recipe);
		}
		
		// Shape Runes
		recipe = RitualRecipe.createTier2("rune.single",
				null,
				new ReagentType[] {ReagentType.GINSENG, ReagentType.GINSENG, ReagentType.SPIDER_SILK, ReagentType.SKY_ASH},
				NostrumResourceItem.getItem(ResourceType.TOKEN, 1),
				new RRequirementShapeMastery(SingleShape.instance()),
				new OutcomeSpawnItem(SpellRune.getRune(SingleShape.instance())));
		RitualRegistry.instance().addRitual(recipe);
		
		recipe = RitualRecipe.createTier3("rune.chain",
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.SPIDER_SILK, ReagentType.MANDRAKE_ROOT},
				SpellRune.getRune(SingleShape.instance()),
				new ItemStack[] {SpellRune.getRune(SingleShape.instance()), new ItemStack(Items.GOLD_INGOT, 1), SpellRune.getRune(SingleShape.instance()), SpellRune.getRune(SingleShape.instance())},
				new RRequirementShapeMastery(ChainShape.instance()),
				new OutcomeSpawnItem(SpellRune.getRune(ChainShape.instance())));
		RitualRegistry.instance().addRitual(recipe);
		
		recipe = RitualRecipe.createTier3("rune.aoe",
				null,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.GRAVE_DUST, ReagentType.SPIDER_SILK, ReagentType.MANDRAKE_ROOT},
				SpellRune.getRune(ChainShape.instance()),
				new ItemStack[] {SpellRune.getRune(ChainShape.instance()), new ItemStack(Items.DIAMOND, 1), SpellRune.getRune(SingleShape.instance()), SpellRune.getRune(ChainShape.instance())},
				new RRequirementShapeMastery(AoEShape.instance()),
				new OutcomeSpawnItem(SpellRune.getRune(AoEShape.instance())));
		RitualRegistry.instance().addRitual(recipe);
		
		for (EAlteration alteration : EAlteration.values()) {
			recipe = RitualRecipe.createTier2("rune." + alteration.name().toLowerCase(),
					null,
					new ReagentType[] {ReagentType.GINSENG, ReagentType.GRAVE_DUST, ReagentType.SKY_ASH, ReagentType.GRAVE_DUST},
					alteration.getReagents().get(0),
					new RRequirementAlterationMastery(alteration),
					new OutcomeSpawnItem(SpellRune.getRune(alteration, 1)));
			RitualRegistry.instance().addRitual(recipe);
		}
		
		for (SpellTrigger trigger : SpellTrigger.getAllTriggers()) {
			recipe = RitualRecipe.createTier3("rune." + trigger.getTriggerKey().toLowerCase(),
					null,
					new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.GINSENG, ReagentType.GINSENG},
					NostrumResourceItem.getItem(ResourceType.TOKEN, 1),
					new ItemStack[] {new ItemStack(Items.GOLD_NUGGET), trigger.getCraftItem(), NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), new ItemStack(Items.GOLD_NUGGET, 1)},
					new RRequirementTriggerMastery(trigger),
					new OutcomeSpawnItem(SpellRune.getRune(trigger	, 1)));
			RitualRegistry.instance().addRitual(recipe);
		}
		
		
		// Boons
		{
			recipe = RitualRecipe.createTier1("buff.luck", EMagicElement.PHYSICAL,
					ReagentType.SPIDER_SILK,
					new RRequirementQuest("boon"),
					new OutcomePotionEffect(Potion.getPotionFromResourceLocation("luck"), 0, 120 * 20));
			RitualRegistry.instance().addRitual(recipe);

			recipe = RitualRecipe.createTier1("buff.speed", EMagicElement.WIND,
					ReagentType.SKY_ASH,
					new RRequirementQuest("boon"),
					new OutcomePotionEffect(Potion.getPotionFromResourceLocation("speed"), 0, 120 * 20));
			RitualRegistry.instance().addRitual(recipe);

			recipe = RitualRecipe.createTier1("buff.strength", EMagicElement.FIRE,
					ReagentType.MANDRAKE_ROOT,
					new RRequirementQuest("boon"),
					new OutcomePotionEffect(Potion.getPotionFromResourceLocation("strength"), 0, 120 * 20));
			RitualRegistry.instance().addRitual(recipe);

			recipe = RitualRecipe.createTier1("buff.leaping", EMagicElement.LIGHTNING,
					ReagentType.MANI_DUST,
					new RRequirementQuest("boon"),
					new OutcomePotionEffect(Potion.getPotionFromResourceLocation("jump_boost"), 0, 120 * 20));
			RitualRegistry.instance().addRitual(recipe);

			recipe = RitualRecipe.createTier1("buff.regen", EMagicElement.EARTH,
					ReagentType.GINSENG,
					new RRequirementQuest("boon"),
					new OutcomePotionEffect(Potion.getPotionFromResourceLocation("regeneration"), 0, 120 * 20));
			RitualRegistry.instance().addRitual(recipe);

			recipe = RitualRecipe.createTier1("buff.fireresist", EMagicElement.FIRE,
					ReagentType.CRYSTABLOOM,
					new RRequirementQuest("boon"),
					new OutcomePotionEffect(Potion.getPotionFromResourceLocation("fire_resistance"), 0, 120 * 20));
			RitualRegistry.instance().addRitual(recipe);

			recipe = RitualRecipe.createTier1("buff.invisibility", EMagicElement.ENDER,
					ReagentType.GRAVE_DUST,
					new RRequirementQuest("boon"),
					new OutcomePotionEffect(Potion.getPotionFromResourceLocation("invisibility"), 0, 120 * 20));
			RitualRegistry.instance().addRitual(recipe);

			recipe = RitualRecipe.createTier1("buff.nightvision", EMagicElement.PHYSICAL,
					ReagentType.BLACK_PEARL,
					new RRequirementQuest("boon"),
					new OutcomePotionEffect(Potion.getPotionFromResourceLocation("night_vision"), 0, 120 * 20));
			RitualRegistry.instance().addRitual(recipe);

			recipe = RitualRecipe.createTier1("buff.waterbreathing", EMagicElement.ICE,
					ReagentType.MANI_DUST,
					new RRequirementQuest("boon"),
					new OutcomePotionEffect(Potion.getPotionFromResourceLocation("water_breathing"), 0, 120 * 20));
			RitualRegistry.instance().addRitual(recipe);
		}
		
		// Enchantment
		{
			recipe = RitualRecipe.createTier2("enchant.infinity", null,
					new ReagentType[] {ReagentType.GRAVE_DUST, ReagentType.GRAVE_DUST, ReagentType.GRAVE_DUST, ReagentType.CRYSTABLOOM},
					new ItemStack(Items.BOW),
					new RRequirementQuest("enchant"),
					new OutcomeEnchantItem(Enchantments.INFINITY, 1));
			RitualRegistry.instance().addRitual(recipe);
		}
		
		ItemStack enderpearl = new ItemStack(Items.ENDER_PEARL);
		recipe = RitualRecipe.createTier3("mark", EMagicElement.WIND,
				new ReagentType[] {ReagentType.GRAVE_DUST, ReagentType.MANI_DUST, ReagentType.MANI_DUST, ReagentType.CRYSTABLOOM},
				InfusedGemItem.instance().getGem(EMagicElement.EARTH, 1),
				new ItemStack[] {enderpearl, new ItemStack(Items.COMPASS), new ItemStack(Items.MAP, 1, OreDictionary.WILDCARD_VALUE), enderpearl},
				new RRequirementQuest("recall"),
				new OutcomeMark());
		RitualRegistry.instance().addRitual(recipe);
		
		recipe = RitualRecipe.createTier1("recall", EMagicElement.LIGHTNING,
				ReagentType.SKY_ASH,
				new RRequirementQuest("recall"),
				new OutcomeRecall());
		RitualRegistry.instance().addRitual(recipe);
		
		// medium crystal -- tier 2. Small crystal, reagents, basic crystal
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier2("kani", null,
					new ReagentType[] {ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST, ReagentType.GINSENG, ReagentType.GRAVE_DUST},
					NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1),
					new RRequirementQuest("lvl3"),
					new OutcomeSpawnItem(NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1)))
				);
		
		// large crystal -- tier 3. Medium crystal, 4 medium crystals, reagents, basic crystal
		ItemStack crystal = NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1);
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("vani", null,
					new ReagentType[] {ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST, ReagentType.BLACK_PEARL, ReagentType.CRYSTABLOOM},
					crystal,
					new ItemStack[] {crystal, crystal, crystal, crystal},
					new RRequirementQuest("lvl7"),
					new OutcomeSpawnItem(NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1)))
				);
		
		// magic token -- tier 1. Mani dust.
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier1("magic_token", null,
					ReagentType.MANI_DUST,
					null,
					new OutcomeSpawnItem(NostrumResourceItem.getItem(ResourceType.TOKEN, 1)))
				);
		
		// fierce slab -- tier 3. Kani crystal. Fire + Wind gems
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("fierce_infusion", EMagicElement.LIGHTNING,
					new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST, ReagentType.MANDRAKE_ROOT, ReagentType.SPIDER_SILK},
					crystal,
					new ItemStack[] {InfusedGemItem.instance().getGem(EMagicElement.FIRE, 1), null, null, InfusedGemItem.instance().getGem(EMagicElement.WIND, 1)},
					null,
					new OutcomeSpawnItem(NostrumResourceItem.getItem(ResourceType.SLAB_FIERCE, 1)))
				);
		
		// kind slab -- tier 3. Kani crystal. Ice + Earth gems
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("kind_infusion", EMagicElement.ENDER,
					new ReagentType[] {ReagentType.CRYSTABLOOM, ReagentType.GINSENG, ReagentType.MANI_DUST, ReagentType.SKY_ASH},
					crystal,
					new ItemStack[] {InfusedGemItem.instance().getGem(EMagicElement.ICE, 1), null, null, InfusedGemItem.instance().getGem(EMagicElement.EARTH, 1)},
					null,
					new OutcomeSpawnItem(NostrumResourceItem.getItem(ResourceType.SLAB_KIND, 1)))
				);
		
		// balanced slab -- tier 3. Vani crystal. Fierce and Kind slabs, + ender and lightning gems
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("balanced_infusion", null,
					new ReagentType[] {ReagentType.GINSENG, ReagentType.CRYSTABLOOM, ReagentType.MANI_DUST, ReagentType.SPIDER_SILK},
					NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1),
					new ItemStack[] {NostrumResourceItem.getItem(ResourceType.SLAB_KIND, 1), InfusedGemItem.instance().getGem(EMagicElement.ENDER, 1), InfusedGemItem.instance().getGem(EMagicElement.LIGHTNING, 1), NostrumResourceItem.getItem(ResourceType.SLAB_FIERCE, 1)},
					null,
					new OutcomeSpawnItem(NostrumResourceItem.getItem(ResourceType.SLAB_BALANCED, 1)))
				);
		
		// Thano Pendant -- tier 3. gold ingot. Paliv + Cerci fragments + 2 mani crystals.
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("thano_infusion", EMagicElement.ICE,
					new ReagentType[] {ReagentType.MANI_DUST, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.SPIDER_SILK},
					new ItemStack(Items.GOLD_INGOT),
					new ItemStack[] {NostrumResourceItem.getItem(ResourceType.PENDANT_LEFT, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), NostrumResourceItem.getItem(ResourceType.PENDANT_RIGHT, 1)},
					null,
					new OutcomeSpawnItem(NostrumResourceItem.getItem(ResourceType.PENDANT_WHOLE, 1)))
				);
		
		// Obelisk -- tier 3. Vani crystal. Balanced slab, 2 eyes of ender, compass.
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("create_obelisk", EMagicElement.ENDER,
					new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.GINSENG},
					NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1),
					new ItemStack[] {NostrumResourceItem.getItem(ResourceType.SLAB_BALANCED, 1), new ItemStack(Items.ENDER_EYE), new ItemStack(Items.ENDER_EYE), new ItemStack(Items.COMPASS)},
					new RRequirementQuest("obelisk"),
					new OutcomeCreateObelisk())
				);
		
		// GeoGem -- tier 3. Compass center. 2x Crystal, 2x reagent, Earth Crystal
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("geogem", EMagicElement.EARTH,
					new ReagentType[] {ReagentType.GRAVE_DUST, ReagentType.MANDRAKE_ROOT, ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST},
					new ItemStack(Items.COMPASS),
					new ItemStack[] {NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1), new ItemStack(ReagentItem.instance(), 1, OreDictionary.WILDCARD_VALUE), new ItemStack(ReagentItem.instance(), 1, OreDictionary.WILDCARD_VALUE), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1)},
					new RRequirementQuest("geogem"),
					new OutcomeSpawnItem(new ItemStack(PositionCrystal.instance(), 1)))
				);
		
		// GeoToken -- tier 3. Geogem center. Magic Token, earth crystal, blank scroll, diamond
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("geotoken", EMagicElement.EARTH,
					new ReagentType[] {ReagentType.GRAVE_DUST, ReagentType.GINSENG, ReagentType.MANDRAKE_ROOT, ReagentType.GRAVE_DUST},
					new ItemStack(PositionCrystal.instance()),
					new ItemStack[] {NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1), NostrumResourceItem.getItem(ResourceType.TOKEN, 1), InfusedGemItem.instance().getGem(EMagicElement.EARTH, 1), new ItemStack(BlankScroll.instance())},
					new RRequirementQuest("geotoken"),
					new OutcomeConstructGeotoken())
				);
		
		// Tele to obelisk -- tier 2. Position gem, reagents
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier2("teleport_obelisk", EMagicElement.ENDER,
					new ReagentType[] {ReagentType.MANI_DUST, ReagentType.MANI_DUST, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK},
					new ItemStack(PositionCrystal.instance()),
					new RRequirementQuest("obelisk"),
					new OutcomeTeleportObelisk())
				);
		
		// Spawn Koids -- tier 3. Kani center. Magic Token, gold, gold, essence
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("koid", null,
					new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK, ReagentType.GRAVE_DUST},
					NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1),
					new ItemStack[] {new ItemStack(Items.GOLD_INGOT), NostrumResourceItem.getItem(ResourceType.TOKEN, 1), new ItemStack(EssenceItem.instance(), 1, OreDictionary.WILDCARD_VALUE), new ItemStack(Items.GOLD_INGOT, 1)},
					null,
					new OutcomeSpawnEntity(new IEntityFactory() {
						@Override
						public void spawn(World world, Vec3d pos, EntityPlayer invoker) {
							EntityKoid koid = new EntityKoid(world);
							koid.setPosition(pos.xCoord, pos.yCoord, pos.zCoord);
							world.spawnEntityInWorld(koid);
							koid.setAttackTarget(invoker);
						}
					}, 5))
				);
		
		// Spell Tome Creation
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("tome", null,
					new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK, ReagentType.MANI_DUST},
					new ItemStack(SpellPlate.instance(), 1, OreDictionary.WILDCARD_VALUE),
					new ItemStack[] {new ItemStack(SpellTomePage.instance()), new ItemStack(SpellTomePage.instance()), new ItemStack(SpellTomePage.instance()), new ItemStack(SpellTomePage.instance())},
					null,
					new OutcomeCreateTome())
				);
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("tome", null,
					new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK, ReagentType.MANI_DUST},
					new ItemStack(SpellPlate.instance(), 1, OreDictionary.WILDCARD_VALUE),
					new ItemStack[] {new ItemStack(SpellTomePage.instance()), new ItemStack(SpellTomePage.instance()), null, new ItemStack(SpellTomePage.instance())},
					null,
					new OutcomeCreateTome())
				);
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("tome", null,
					new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK, ReagentType.MANI_DUST},
					new ItemStack(SpellPlate.instance(), 1, OreDictionary.WILDCARD_VALUE),
					new ItemStack[] {new ItemStack(SpellTomePage.instance()), null, null, new ItemStack(SpellTomePage.instance())},
					null,
					new OutcomeCreateTome())
				);
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("tome", null,
					new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK, ReagentType.MANI_DUST},
					new ItemStack(SpellPlate.instance(), 1, OreDictionary.WILDCARD_VALUE),
					new ItemStack[] {new ItemStack(SpellTomePage.instance()), null, null, null},
					null,
					new OutcomeCreateTome())
				);
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("tome", null,
					new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK, ReagentType.MANI_DUST},
					new ItemStack(SpellPlate.instance(), 1, OreDictionary.WILDCARD_VALUE),
					new ItemStack[] {null, null, null, null},
					null,
					new OutcomeCreateTome())
				);
		
		// Spell Binding
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("spell_binding", null,
					new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.MANI_DUST, ReagentType.SKY_ASH, ReagentType.BLACK_PEARL},
					new ItemStack(SpellTome.instance(), 1, OreDictionary.WILDCARD_VALUE),
					new ItemStack[] {new ItemStack(Items.GOLD_NUGGET), new ItemStack(SpellScroll.instance()), NostrumResourceItem.getItem(ResourceType.TOKEN, 1), new ItemStack(Items.GOLD_NUGGET)},
					null,
					new OutcomeBindSpell())
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
    			wrapAttribute(AwardType.MANA, 0.0500f));
    	new NostrumQuest("lvl1", QuestType.REGULAR, 2, 0, 0, 0, new String[]{"start"},
    			null, wrapAttribute(AwardType.MANA, 0.010f));
    	new NostrumQuest("lvl2-fin", QuestType.REGULAR, 3, 0, 0, 1, new String[]{"lvl1"},
    			null, wrapAttribute(AwardType.REGEN, 0.0050f));
    	new NostrumQuest("lvl2-con", QuestType.REGULAR, 3, 1, 0, 0, new String[]{"lvl1"},
    			null, wrapAttribute(AwardType.COST, -0.005f));
    	new NostrumQuest("lvl3", QuestType.CHALLENGE, 4, 0, 0, 0, new String[]{"lvl2-fin", "lvl2-con"},
    			new ObjectiveRitual("magic_token"),
    			wrapAttribute(AwardType.MANA, 0.005f));
    	new NostrumQuest("lvl4", QuestType.CHALLENGE, 5, 0, 0, 0, new String[]{"lvl3"},
    			new ObjectiveRitual("spell_binding"),
    			new IReward[]{new AlterationReward(EAlteration.INFLICT)});
    	
    	// LVL-finesse tree
    	new NostrumQuest("lvl6-fin", QuestType.REGULAR, 6, 0, 0, 3, new String[]{"lvl4"},
    			null, wrapAttribute(AwardType.REGEN, 0.005f));
    	new NostrumQuest("lvl7-fin", QuestType.REGULAR, 7, 0, 0, 4, new String[]{"lvl6-fin"},
    			null, wrapAttribute(AwardType.MANA, 0.010f));
    	new NostrumQuest("lvl7-fin7", QuestType.REGULAR, 7, 0, 0, 7, new String[]{"lvl7-fin"},
    			null, wrapAttribute(AwardType.REGEN, 0.010f));
    	new NostrumQuest("lvl10-fin10", QuestType.CHALLENGE, 10, 0, 0, 10, new String[]{"lvl7-fin7"},
    			new ObjectiveSpellCast().numTriggers(10).requiredElement(EMagicElement.ICE),
    			new IReward[]{new AlterationReward(EAlteration.SUPPORT)});
    	
    	// LVL-control tree
    	new NostrumQuest("lvl6-con", QuestType.REGULAR, 6, 3, 0, 0, new String[]{"lvl4"},
    			null, wrapAttribute(AwardType.COST, -0.005f));
    	new NostrumQuest("lvl7-con", QuestType.REGULAR, 7, 4, 0, 0, new String[]{"lvl6-con"},
    			null, wrapAttribute(AwardType.COST, -0.005f));
    	new NostrumQuest("lvl7-con7", QuestType.REGULAR, 7, 7, 0, 0, new String[]{"lvl7-con"},
    			null, wrapAttribute(AwardType.COST, -0.010f));
    	new NostrumQuest("lvl10-con10", QuestType.CHALLENGE, 10, 10, 0, 0, new String[]{"lvl7-con7"},
    			new ObjectiveSpellCast().numElems(10).requiredElement(EMagicElement.EARTH),
    			new IReward[]{new AlterationReward(EAlteration.RESIST)});
    	
    	// LVL main tree
    	new NostrumQuest("lvl7", QuestType.CHALLENGE, 7, 0, 0, 0, new String[]{"lvl6-con", "lvl6-fin"},
    			new ObjectiveRitual("kani"),
    			wrapAttribute(AwardType.MANA, 0.020f));
    	new NostrumQuest("lvl8-fin3", QuestType.REGULAR, 8, 0, 0, 3, new String[]{"lvl7"},
    			null, wrapAttribute(AwardType.COST, -0.005f));
    	new NostrumQuest("lvl8-fin5", QuestType.REGULAR, 8, 0, 0, 5, new String[]{"lvl7"},
    			null, wrapAttribute(AwardType.MANA, 0.020f));
    	new NostrumQuest("lvl10-fin6", QuestType.REGULAR, 10, 0, 0, 6, new String[]{"lvl8-fin5"},
    			null, wrapAttribute(AwardType.REGEN, 0.100f));
    	new NostrumQuest("lvl8-con3", QuestType.REGULAR, 8, 3, 0, 0, new String[]{"lvl7"},
    			null, wrapAttribute(AwardType.REGEN, 0.005f));
    	new NostrumQuest("lvl8-con5", QuestType.REGULAR, 8, 5, 0, 0, new String[]{"lvl7"},
    			null, wrapAttribute(AwardType.MANA, 0.040f));
    	new NostrumQuest("lvl10-con6", QuestType.REGULAR, 10, 6, 0, 0, new String[]{"lvl8-con5"},
    			null, wrapAttribute(AwardType.COST, -0.050f));
    	new NostrumQuest("lvl10", QuestType.REGULAR, 10, 0, 0, 0, new String[]{"lvl8-con3", "lvl8-fin3"},
    			null, wrapAttribute(AwardType.MANA, 0.100f));
    	
    	new NostrumQuest("con1", QuestType.REGULAR, 0,
    			1, // Control
    			0, // Technique
    			0, // Finesse
    			new String[]{"start"},
    			null,
    			wrapAttribute(AwardType.COST, -0.002f));
    	new NostrumQuest("con2", QuestType.REGULAR, 0,
    			2, // Control
    			0, // Technique
    			0, // Finesse
    			new String[]{"con1"},
    			null,
    			wrapAttribute(AwardType.MANA, 0.010f));
    	new NostrumQuest("con7", QuestType.CHALLENGE, 0,
    			7, // Control
    			0, // Technique
    			0, // Finesse
    			new String[]{"con2"},
    			new ObjectiveRitual("koid"),
    			wrapAttribute(AwardType.COST, -0.050f));
    	new NostrumQuest("con7-tec1", QuestType.CHALLENGE, 0,
    			7, // Control
    			1, // Technique
    			0, // Finesse
    			new String[]{"con7", "con6-tec3"},
    			new ObjectiveSpellCast().numElems(6).requiredElement(EMagicElement.EARTH),
    			new IReward[] {new AlterationReward(EAlteration.ENCHANT)});
    	new NostrumQuest("con3-tec2", QuestType.REGULAR, 0,
    			3, // Control
    			2, // Technique
    			0, // Finesse
    			new String[]{"con2"},
    			null,
    			wrapAttribute(AwardType.REGEN, 0.005f));
    	new NostrumQuest("con5-tec2", QuestType.REGULAR, 0,
    			5, // Control
    			2, // Technique
    			0, // Finesse
    			new String[]{"con3-tec2"},
    			null,
    			wrapAttribute(AwardType.COST, -0.010f));
    	new NostrumQuest("con5-tec3", QuestType.REGULAR, 0,
    			5, // Control
    			3, // Technique
    			0, // Finesse
    			new String[]{"con5-tec2", "con1-tec3"},
    			null,
    			wrapAttribute(AwardType.COST, -0.015f));
    	new NostrumQuest("con5-tec4", QuestType.REGULAR, 0,
    			5, // Control
    			4, // Technique
    			0, // Finesse
    			new String[]{"con5-tec3"},
    			null,
    			wrapAttribute(AwardType.COST, -0.015f));
    	new NostrumQuest("con6-tec3", QuestType.REGULAR, 0,
    			6, // Control
    			3, // Technique
    			0, // Finesse
    			new String[]{"con5-tec3"},
    			null,
    			wrapAttribute(AwardType.REGEN, 0.005f));
    	new NostrumQuest("con6-tec4", QuestType.CHALLENGE, 0,
    			6, // Control
    			4, // Technique
    			0, // Finesse
    			new String[]{"con6-tec3", "con5-tec4"},
    			new ObjectiveKill(EntityGolem.class, "Golem", 30),
    			new IReward[]{new AlterationReward(EAlteration.SUMMON)});
    	new NostrumQuest("con1-tec2", QuestType.REGULAR, 0,
    			1, // Control
    			2, // Technique
    			0, // Finesse
    			new String[]{"con1"},
    			null,
    			wrapAttribute(AwardType.COST, -0.008f));
    	new NostrumQuest("con1-tec3", QuestType.CHALLENGE, 0,
    			1, // Control
    			3, // Technique
    			0, // Finesse
    			new String[]{"con1-tec2"},
    			new ObjectiveSpellCast().numElems(3).requiredElement(EMagicElement.LIGHTNING),
    			wrapAttribute(AwardType.MANA, 0.030f));
    	new NostrumQuest("con1-tec5", QuestType.REGULAR, 0,
    			1, // Control
    			5, // Technique
    			0, // Finesse
    			new String[]{"con1-tec3", "fin1-tec2"},
    			null,
    			wrapAttribute(AwardType.COST, -0.005f));
    	
    	new NostrumQuest("tec1", QuestType.REGULAR, 0,
    			0, // Control
    			1, // Technique
    			0, // Finesse
    			new String[]{"start"},
    			null,
    			wrapAttribute(AwardType.MANA, 0.01f));
    	new NostrumQuest("tec7", QuestType.CHALLENGE, 0,
    			0, // Control
    			7, // Technique
    			0, // Finesse
    			new String[]{"con1-tec5", "fin1-tec5"},
    			new ObjectiveRitual("vani"),
    			new IReward[] {new AlterationReward(EAlteration.ALTER)});

    	new NostrumQuest("fin1", QuestType.REGULAR, 0,
    			0, // Control
    			0, // Technique
    			1, // Finesse
    			new String[]{"start"},
    			null,
    			wrapAttribute(AwardType.REGEN, 0.002f));
    	new NostrumQuest("fin3", QuestType.CHALLENGE, 0,
    			0, // Control
    			0, // Technique
    			3, // Finesse
    			new String[]{"fin1"},
    			new ObjectiveSpellCast().numTriggers(3).requiredAlteration(EAlteration.INFLICT),
    			wrapAttribute(AwardType.REGEN, 0.008f));
    	new NostrumQuest("fin5", QuestType.REGULAR, 0,
    			0, // Control
    			0, // Technique
    			5, // Finesse
    			new String[]{"fin3"},
    			null,
    			wrapAttribute(AwardType.MANA, 0.020f));
    	new NostrumQuest("fin7", QuestType.REGULAR, 0,
    			0, // Control
    			0, // Technique
    			7, // Finesse
    			new String[]{"fin5"},
    			null,
    			wrapAttribute(AwardType.REGEN, 0.075f));
    	new NostrumQuest("fin5-tec2", QuestType.CHALLENGE, 0,
    			0, // Control
    			2, // Technique
    			5, // Finesse
    			new String[]{"fin5", "fin2-tec3"},
    			new ObjectiveSpellCast().requiredShape(AoEShape.instance()),
    			new IReward[] {new AlterationReward(EAlteration.GROWTH)});
    	new NostrumQuest("fin1-tec2", QuestType.REGULAR, 0,
    			0, // Control
    			2, // Technique
    			1, // Finesse
    			new String[]{"fin1"},
    			null,
    			wrapAttribute(AwardType.REGEN, 0.010f));
    	new NostrumQuest("fin1-tec3", QuestType.CHALLENGE, 0,
    			0, // Control
    			3, // Technique
    			1, // Finesse
    			new String[]{"fin1-tec2"},
    			new ObjectiveKill(EntityKoid.class, "Koid", 5),
    			wrapAttribute(AwardType.MANA, 0.025f));
    	new NostrumQuest("fin1-tec5", QuestType.REGULAR, 0,
    			0, // Control
    			5, // Technique
    			1, // Finesse
    			new String[]{"fin1-tec3", "con1-tec2"},
    			null,
    			wrapAttribute(AwardType.REGEN, 0.050f));
    	new NostrumQuest("fin2-tec3", QuestType.REGULAR, 0,
    			0, // Control
    			3, // Technique
    			2, // Finesse
    			new String[]{"fin1-tec3", "fin2-tec5"},
    			null,
    			wrapAttribute(AwardType.COST, -0.010f));
    	new NostrumQuest("fin3-tec3", QuestType.REGULAR, 0,
    			0, // Control
    			3, // Technique
    			3, // Finesse
    			new String[]{"fin2-tec3"},
    			null,
    			wrapAttribute(AwardType.MANA, 0.050f));
    	new NostrumQuest("fin2-tec5", QuestType.REGULAR, 0,
    			0, // Control
    			5, // Technique
    			2, // Finesse
    			new String[]{"fin1-tec5"},
    			null,
    			wrapAttribute(AwardType.REGEN, 0.020f));
    	new NostrumQuest("fin3-tec6", QuestType.CHALLENGE, 0,
    			0, // Control
    			6, // Technique
    			3, // Finesse
    			new String[]{"fin2-tec5"},
    			new ObjectiveRitual("balanced_infusion"),
    			new IReward[] {new AlterationReward(EAlteration.CONJURE)});
    	
    	new NostrumQuest("geogem", QuestType.CHALLENGE, 5,
    			0, // Control
    			0, // Technique
    			0, // Finesse
    			new String[0],
    			new ObjectiveSpellCast().requiredElement(EMagicElement.EARTH),
    			wrapAttribute(AwardType.COST, -0.020f))
    		.offset(-3, 2);
    	
    	new NostrumQuest("geotoken", QuestType.CHALLENGE, 5,
    			0, // Control
    			0, // Technique
    			0, // Finesse
    			new String[] {"geogem"},
    			new ObjectiveRitual("geogem"),
    			wrapAttribute(AwardType.COST, -0.030f))
    		.offset(-4, 2);
    	
    	new NostrumQuest("obelisk", QuestType.CHALLENGE, 10,
    			0, // Control
    			0, // Technique
    			0, // Finesse
    			new String[] {"geotoken"},
    			new ObjectiveSpellCast().requiredElement(EMagicElement.ENDER)
    			.requiredElement(EMagicElement.ENDER)
    			.requiredElement(EMagicElement.ENDER),
    			wrapAttribute(AwardType.MANA, 0.040f))
    		.offset(-5, 6);
    	
    	new NostrumQuest("obelisk2", QuestType.REGULAR, 10,
    			0, // Control
    			0, // Technique
    			0, // Finesse
    			new String[] {"obelisk"},
    			null,
    			wrapAttribute(AwardType.MANA, 0.010f))
    		.offset(-6, 6);
    	
    	new NostrumQuest("recall", QuestType.CHALLENGE, 10,
    			0, // Control
    			0, // Technique
    			0, // Finesse
    			new String[] {"geotoken"},
    			new ObjectiveSpellCast().requiredElement(EMagicElement.WIND),
    			wrapAttribute(AwardType.REGEN, 0.040f))
    		.offset(-5, 8);
    	
    	new NostrumQuest("recall2", QuestType.REGULAR, 10,
    			0, // Control
    			0, // Technique
    			0, // Finesse
    			new String[] {"recall"},
    			null,
    			wrapAttribute(AwardType.REGEN, 0.010f))
    		.offset(-6, 8);
    	
    	new NostrumQuest("boon", QuestType.CHALLENGE, 12,
    			0, // Control
    			0, // Technique
    			0, // Finesse
    			new String[0],
    			new ObjectiveSpellCast().requiredAlteration(EAlteration.RESIST)
    									.requiredAlteration(EAlteration.SUPPORT)
    									.requiredAlteration(EAlteration.GROWTH),
    			wrapAttribute(AwardType.REGEN, 0.100f))
    		.offset(3, 9);
    	
//    	new NostrumQuest("hex", QuestType.CHALLENGE, 14,
//    			0, // Control
//    			0, // Technique
//    			0, // Finesse
//    			new String[] {"boon"},
//    			new ObjectiveSpellCast().requiredAlteration(EAlteration.INFLICT)
//    									.numElems(5),
//    			wrapAttribute(AwardType.REGEN, 0.050f))
//    		.offset(4, 10);
    	
    	new NostrumQuest("enchant", QuestType.REGULAR, 13,
    			0, // Control
    			0, // Technique
    			0, // Finesse
    			new String[] {"boon"},
    			null,
    			wrapAttribute(AwardType.MANA, 0.050f))
    		.offset(4, 11);
    	
//    	new NostrumQuest("con", QuestType.REGULAR, 0,
//    			0, // Control
//    			0, // Technique
//    			0, // Finesse
//    			new String[]{"lvl1"},
//    			null,
//    			wrapAttribute(AwardType.COST, -0.005f));
    	
    }
    
    private static IReward[] wrapAttribute(AwardType type, float val) {
    	return new IReward[]{new AttributeReward(type, val)};
    }
    
    public static boolean getQuestAvailable(EntityPlayer player, NostrumQuest quest) {
    	if (quest.getParentKeys() == null || quest.getParentKeys().length == 0) {
			return canTakeQuest(player, quest);
    	}
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null)
			return false;
		
		for (String parent : quest.getParentKeys()) {
			if (attr.getCompletedQuests().contains(parent))
				return true;
		}
		
		return false;
    }
    
    public static boolean canTakeQuest(EntityPlayer player, NostrumQuest quest) {
    	INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null)
			return false;
		
		return quest.getReqLevel() <= attr.getLevel()
				&& quest.getReqControl() <= attr.getControl()
				&& quest.getReqTechnique() <= attr.getTech()
				&& quest.getReqFinesse() <= attr.getFinesse();
	}
}
