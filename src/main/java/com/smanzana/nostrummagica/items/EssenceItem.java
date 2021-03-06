package com.smanzana.nostrummagica.items;

import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

public class EssenceItem extends Item implements ILoreTagged {

	public static final String ID = "nostrum_essence";
	
	private static EssenceItem instance = null;
	public static EssenceItem instance() {
		if (instance == null)
			instance = new EssenceItem();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.addShapelessRecipe(InfusedGemItem.instance().getGem(null, 1),
				getEssence(EMagicElement.EARTH, 1),
				getEssence(EMagicElement.ENDER, 1),
				getEssence(EMagicElement.FIRE, 1),
				getEssence(EMagicElement.ICE, 1),
				getEssence(EMagicElement.LIGHTNING, 1),
				getEssence(EMagicElement.PHYSICAL, 1),
				getEssence(EMagicElement.WIND, 1));
		
		EMagicElement[] all = EMagicElement.values();
		EMagicElement last = all[all.length - 1];
		for (EMagicElement element : all) {
			GameRegistry.addShapelessRecipe(getEssence(element, 1),
					getEssence(last, 1),
					new ItemStack(instance(), 1, OreDictionary.WILDCARD_VALUE));
			last = element;
		}
	}
	
	public EssenceItem() {
		super();
		this.setUnlocalizedName(ID);
		this.setMaxDamage(0);
		this.setMaxStackSize(64);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setHasSubtypes(true);
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		int i = stack.getMetadata();
		
		String suffix = getNameFromMeta(i);
		
		return this.getUnlocalizedName() + "." + suffix;
	}
	
	/**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
    @SideOnly(Side.CLIENT)
    @Override
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
    	for (EMagicElement element: EMagicElement.values()) {
    		subItems.add(new ItemStack(itemIn, 1, element.ordinal()));
    	}
	}
    
    public static EMagicElement findType(ItemStack essence) {
    	if (essence == null || !(essence.getItem() instanceof EssenceItem))
    		return null;
    	
    	for (EMagicElement type : EMagicElement.values()) {
    		if (type.ordinal() == essence.getMetadata())
    			return type;
    	}
    	
    	return null;
    }
    
    public static String getNameFromMeta(int meta) {
    	String suffix = "unknown";
		
    	EMagicElement type = getTypeFromMeta(meta);
    	if (type != null)
    		suffix = type.name().toLowerCase();
    	
		return suffix;
    }
    
    public static EMagicElement getTypeFromMeta(int meta) {
    	EMagicElement ret = null;
    	for (EMagicElement type : EMagicElement.values()) {
			if (type.ordinal() == meta) {
				ret = type;
				break;
			}
		}
    	
    	return ret;
    }
    
    public static ItemStack getEssence(EMagicElement element, int count) {
    	int meta = element.ordinal();
    	return new ItemStack(instance(), count, meta);
    }
    
	@Override
	public String getLoreKey() {
		return "nostrum_essence_item";
	}

	@Override
	public String getLoreDisplayName() {
		return "Magical Essences";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("Magical Essences contain the unprocessed energies of an element.");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("Magical Essences are materializations of the raw power of an element.", "Essences are used to create runes.", "They are dropped from many different enemies, but most easily from Koids.");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
}
