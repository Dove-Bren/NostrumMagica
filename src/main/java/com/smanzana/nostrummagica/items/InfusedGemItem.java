package com.smanzana.nostrummagica.items;

import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * One for each element, except physical
 * @author Skyler
 *
 */
public class InfusedGemItem extends Item {

	public static final String ID = "nostrum_gem";
	
	private static InfusedGemItem instance = null;
	public static InfusedGemItem instance() {
		if (instance == null)
			instance = new InfusedGemItem();
		
		return instance;
	}
	
	public InfusedGemItem() {
		super();
		this.setUnlocalizedName(ID);
		this.setMaxDamage(0);
		this.setMaxStackSize(16);
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
	 * Returns an itemstack containing the gem type specefied.
	 * If element is null, returns a basic gem.
	 * @param element
	 * @param count
	 * @return
	 */
	public ItemStack getGem(EMagicElement element, int count) {
		int meta = 0;
		if (element != null && element != EMagicElement.PHYSICAL)
			meta = element.ordinal() + 1;
		
		return new ItemStack(this, count, meta);
	}
	
	/**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
    @SideOnly(Side.CLIENT)
    @Override
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
    	subItems.add(new ItemStack(itemIn, 1, 0));
    	for (EMagicElement type : EMagicElement.values()) {
    		if (type == EMagicElement.PHYSICAL)
    			continue;
    		subItems.add(new ItemStack(itemIn, 1, type.ordinal() + 1));
    	}
	}
    
    public int getMetaFromElement(EMagicElement element) {
    	return element.ordinal() + 1;
    }
    
    public String getNameFromMeta(int meta) {
    	String suffix = "basic";
		
    	EMagicElement type = getTypeFromMeta(meta);
    	if (type != null)
    		suffix = type.name().toLowerCase();
    	
		return suffix;
    }
    
    public EMagicElement getTypeFromMeta(int meta) {
    	EMagicElement ret = null;
    	for (EMagicElement type : EMagicElement.values()) {
			if (type.ordinal() + 1 == meta) {
				ret = type;
				break;
			}
		}
    	
    	return ret;
    }
}
