package com.smanzana.nostrummagica.items;

import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class EssenceItem extends Item implements ILoreTagged {

	public static final String ID_PREFIX = "nostrum_essence_";
	
	public static final String MakeID(EMagicElement element) {
		return ID_PREFIX + element.name().toLowerCase();
	}
	
	protected final EMagicElement element;
	
	public EssenceItem(EMagicElement element) {
		super(NostrumItems.PropBase());
		this.element = element;
	}
	
    public static EMagicElement findType(ItemStack essence) {
    	if (essence == null || !(essence.getItem() instanceof EssenceItem))
    		return null;
    	
    	EssenceItem item = (EssenceItem) essence.getItem();
    	return item.element;
    }
    
    public static Item getEssenceItem(EMagicElement element) {
    	Item essence = null;
    	switch (element) {
		case EARTH:
			essence = NostrumItems.essenceEarth;
			break;
		case ENDER:
			essence = NostrumItems.essenceEnder;
			break;
		case FIRE:
			essence = NostrumItems.essenceFire;
			break;
		case ICE:
			essence = NostrumItems.essenceIce;
			break;
		case LIGHTNING:
			essence = NostrumItems.essenceLightning;
			break;
		case PHYSICAL:
			essence = NostrumItems.essencePhysical;
			break;
		case WIND:
			essence = NostrumItems.essenceWind;
			break;
    	}
    	
    	return essence;
    }
    
    public static ItemStack getEssence(EMagicElement element, int count) {
    	return new ItemStack(getEssenceItem(element), count);
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
	
	public EMagicElement getElement() {
		return this.element;
	}
}
