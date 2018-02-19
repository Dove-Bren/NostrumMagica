package com.smanzana.nostrummagica;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.smanzana.nostrummagica.capabilities.AttributeProvider;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.command.CommandTestConfig;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.items.InfusedGemItem;
import com.smanzana.nostrummagica.items.NostrumResourceItem;
import com.smanzana.nostrummagica.items.NostrumResourceItem.ResourceType;
import com.smanzana.nostrummagica.items.ReagentBag;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.items.SeekerIdol;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy;
import com.smanzana.nostrummagica.listeners.PlayerListener;
import com.smanzana.nostrummagica.proxy.CommonProxy;
import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.rituals.RitualRegistry;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeEnchantItem;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeMark;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomePotionEffect;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeRecall;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeSpawnItem;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.SpellRegistry;
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
    	
    	proxy.preinit();
    	
    	spellRegistry = new SpellRegistry();
    	RitualRegistry.instance();
    	
    	registerDefaultRituals();
    	
    	File dir = new File(event.getSuggestedConfigurationFile().getParentFile(), "NostrumMagica");
    	if (!dir.exists())
    		dir.mkdirs();
    	spellRegistryFile = new File(dir, "spells.dat"); 
    	loadSpellRegistry(spellRegistryFile);
    	seekerRegistryFile = new File(dir, "dungloc.dat"); 
    	//loadSeekerRegistry(spellRegistryFile);
    	
    	new ModConfig(new Configuration(event.getSuggestedConfigurationFile()));
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
		
		recipe = RitualRecipe.createTier1("ritual.buff.luck.name", null,
				ReagentType.GRAVE_DUST,
				new OutcomePotionEffect(Potion.getPotionFromResourceLocation("luck"), 0, 20 * 20));
		
		RitualRegistry.instance().addRitual(recipe);
		
		recipe = RitualRecipe.createTier2("ritual.enchant.infinity.name", null,
				new ReagentType[] {ReagentType.GRAVE_DUST, ReagentType.GRAVE_DUST, ReagentType.GRAVE_DUST, ReagentType.CRYSTABLOOM},
				new ItemStack(Items.BOW),
				new OutcomeEnchantItem(Enchantments.INFINITY, 1));
		RitualRegistry.instance().addRitual(recipe);
		
		ItemStack enderpearl = new ItemStack(Items.ENDER_PEARL);
		recipe = RitualRecipe.createTier3("ritual.mark.name", EMagicElement.WIND,
				new ReagentType[] {ReagentType.GRAVE_DUST, ReagentType.MANI_DUST, ReagentType.MANI_DUST, ReagentType.CRYSTABLOOM},
				InfusedGemItem.instance().getGem(EMagicElement.EARTH, 1),
				new ItemStack[] {enderpearl, enderpearl, new ItemStack(Items.COMPASS), new ItemStack(Items.MAP, 1, OreDictionary.WILDCARD_VALUE)},
				new OutcomeMark());
		RitualRegistry.instance().addRitual(recipe);
		
		recipe = RitualRecipe.createTier1("ritual.recall.name", EMagicElement.LIGHTNING,
				ReagentType.SKY_ASH,
				new OutcomeRecall());
		RitualRegistry.instance().addRitual(recipe);
		
		// medium crystal -- tier 2. Small crystal, reagents, basic crystal
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier2("ritual.kani.name", null,
					new ReagentType[] {ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST, ReagentType.GINSENG, ReagentType.GRAVE_DUST},
					NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1),
					new OutcomeSpawnItem(NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1)))
				);
		
		// large crystal -- tier 3. Medium crystal, 4 medium crystals, reagents, basic crystal
		ItemStack crystal = NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1);
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("ritual.vani.name", null,
					new ReagentType[] {ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST, ReagentType.BLACK_PEARL, ReagentType.CRYSTABLOOM},
					crystal,
					new ItemStack[] {crystal, crystal, crystal, crystal},
					new OutcomeSpawnItem(NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1)))
				);
		
//		RitualRegistry.instance().addRitual(
//				RitualRecipe.createTier2("ritual.form_obelisk.name", EMagicElement.ENDER,
//					new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.SKY_ASH, ReagentType.SPIDER_SILK},
//					center, outcome)
//				);
	}
}
