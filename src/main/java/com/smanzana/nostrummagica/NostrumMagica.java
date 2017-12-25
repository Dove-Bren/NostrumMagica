package com.smanzana.nostrummagica;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.smanzana.nostrummagica.capabilities.AttributeProvider;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.items.SpellTome;
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
    
    @SidedProxy(clientSide="com.smanzana.nostrummagica.proxy.ClientProxy", serverSide="com.smanzana.nostrummagica.proxy.CommonProxy")
    public static CommonProxy proxy;
    
    public static CreativeTabs creativeTab;
    public static Logger logger = LogManager.getLogger(MODID);
    public static PlayerListener playerListener;
    
    public static SpellRegistry spellRegistry;
    private File spellRegistryFile; 
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
    }
    
    @EventHandler
    public void preinit(FMLPreInitializationEvent event) {
    	playerListener = new PlayerListener();
    	spellRegistry = new SpellRegistry();
    	
    	spellRegistryFile = new File(event.getSuggestedConfigurationFile().getParentFile(),
    			"spells.dat");
    	loadSpellRegistry(spellRegistryFile);
    	
    	NostrumMagica.creativeTab = new CreativeTabs(MODID){
	    	@Override
	        @SideOnly(Side.CLIENT)
	        public Item getTabIconItem(){
	    		return SpellTome.instance();
	        }
	    };
	    SpellTome.instance().setCreativeTab(NostrumMagica.creativeTab);
    	
    	proxy.preinit();
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
    	return e.getCapability(AttributeProvider.CAPABILITY, null);
    }
    
    private static int potionID = 65;
    public static void registerPotion(Potion potion, ResourceLocation loc) {
    	while (Potion.getPotionById(potionID) != null)
    		potionID++;
    	Potion.REGISTRY.register(potionID, loc, potion);
    }
    
    public static Spell getCurrentSpell(EntityPlayer player) {
    	
    }
    
    public static List<Spell> getSpells(EntityPlayer entity) {
    	if (entity == null)
    		return null;
    	
    	
    	
    	for (ItemStack stack : entity.inventory.mainInventory)
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
