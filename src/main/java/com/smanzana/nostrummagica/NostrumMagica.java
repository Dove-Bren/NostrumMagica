package com.smanzana.nostrummagica;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.smanzana.nostrummagica.capabilities.AttributeProvider;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy;
import com.smanzana.nostrummagica.listeners.PlayerListener;
import com.smanzana.nostrummagica.proxy.CommonProxy;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.SpellRegistry;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = NostrumMagica.MODID, version = NostrumMagica.VERSION)
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
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
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
    	
    	System.out.println("Suggested: " + event.getSuggestedConfigurationFile().getAbsolutePath());
    	File dir = new File(event.getSuggestedConfigurationFile().getParentFile(), "NostrumMagica");
    	if (!dir.exists())
    		dir.mkdirs();
    	spellRegistryFile = new File(dir, "spells.dat"); 
    	loadSpellRegistry(spellRegistryFile);
    }
    
    @EventHandler
    public void postinit(FMLPostInitializationEvent event) {
    	proxy.postinit();
    }
    
    @EventHandler
    public void shutdown(FMLServerStoppingEvent event) {
    	saveSpellRegistry(spellRegistryFile);
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
}
