package com.smanzana.nostrummagica.items;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.spelltome.enhancement.SpellTomeEnhancement;
import com.smanzana.nostrummagica.spelltome.enhancement.SpellTomeEnhancementWrapper;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SpellPlate extends Item implements ILoreTagged {

	/*
	 * Base capacity
	 * Bonus enhancements
	 */
	
	private static final String NBT_CAPACITY = "capacity";
	private static final String NBT_ENHANCEMENTS = "enhancements";
	private static final String NBT_ENHANCEMENT_LEVEL = "";
	private static final String NBT_ENHANCEMENT_TYPE = "";
	
	private static SpellPlate instance = null;
	
	public static SpellPlate instance() {
		if (instance == null)
			instance = new SpellPlate();
		
		return instance;
	}
	
	public static final String id = "spellTomePlate";
	
	private SpellPlate() {
		super();
		this.setUnlocalizedName(id);
		this.setCreativeTab(NostrumMagica.enhancementTab); // set as icon for tab
		this.setMaxStackSize(1);
	}
	
	public static ItemStack getItemstack(int type, int capacity,
			SpellTomeEnhancementWrapper ... enhancements) {
		type = type % SpellTome.MAX_TOME_COUNT;
		ItemStack stack = new ItemStack(instance, 1, type);
		
		NBTTagCompound nbt = stack.getTagCompound();
		
		if (nbt == null)
			nbt = new NBTTagCompound();
		
		nbt.setInteger(NBT_CAPACITY, capacity);
		if (enhancements != null && enhancements.length > 0) {
			NBTTagList list = new NBTTagList();
			for (SpellTomeEnhancementWrapper enhance : enhancements) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setString(NBT_ENHANCEMENT_TYPE, enhance.getEnhancement().getTitleKey());
				tag.setInteger(NBT_ENHANCEMENT_LEVEL, enhance.getLevel());
				list.appendTag(tag);
			}
			
			nbt.setTag(NBT_ENHANCEMENTS, list);
		}
		
		stack.setTagCompound(nbt);
		return stack;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
		if (this.getCreativeTab() != tab)
    		return;
    	
		for (int i = 0; i < SpellTome.MAX_TOME_COUNT; i++) {
			subItems.add(getItemstack(i, 10));
		}
	}

	@Override
	public String getLoreKey() {
		return "nostrum_spelltome_plate";
	}

	@Override
	public String getLoreDisplayName() {
		return "Spelltome Plates";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("Sturdy wood plates perfect for binding as a tome.");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("These plates are used to create spell tomes. Take them and a handful of Spelltome Pages to your ritual altar!");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
		int capacity = getCapacity(stack);
		tooltip.add(I18n.format("info.tome.capacity", new Object[] {capacity}));
		
		List<SpellTomeEnhancementWrapper> enhancements = getEnhancements(stack);
		if (enhancements != null && !enhancements.isEmpty()) {
			tooltip.add("");
			for (SpellTomeEnhancementWrapper enhance : enhancements) {
				tooltip.add(I18n.format(
						enhance.getEnhancement().getNameFormat(), new Object[0])
						+ " " + toRoman(enhance.getLevel()));
			}
		}
	}
	
	public static String toRoman(int num) {
		if (num <= 0)
			return "0";
		
		switch (num) {
		case 1:
			return "I";
		case 2:
			return "II";
		case 3:
			return "III";
		case 4:
			return "IV";
		case 5:
			return "V";
		case 6:
			return "VI";
		case 7:
			return "VII";
		case 8:
			return "VIII";
		case 9:
			return "IX";
		case 10:
			return "X";
		case 11:
			return "XI";
		case 12:
			return "XII";
		case 13:
			return "XIII";
		case 14:
			return "XIV";
		case 15:
			return "XV";
		case 16:
			return "XVI";
		case 17:
			return "XVII";
		case 18:
			return "XVII";
		case 19:
			return "XIX";
		case 20:
			return "XX";
		case 21:
			return "XXI";
		case 22:
			return "XXII";
		case 23:
			return "XXIII";
		case 24:
			return "XXIV";
		case 25:
			return "XXV";
		case 26:
			return "XXVI";
		case 27:
			return "XXVII";
		case 28:
			return "XXVIII";
		case 29:
			return "XXIX";
		case 30:
			return "XXX";
		}
		
		return "?";
	}
	
	public static int getCapacity(ItemStack stack) {
		if (stack == null || !(stack.getItem() instanceof SpellPlate))
			return 0;
		
		if (!stack.hasTagCompound())
			return 0;
		
		return stack.getTagCompound().getInteger(NBT_CAPACITY);
	}
	
	/*
	 * NBTTagCompound nbt = stack.getTagCompound();
		
		if (nbt == null)
			nbt = new NBTTagCompound();
		
		nbt.setInteger(NBT_CAPACITY, capacity);
		if (enhancements != null && enhancements.length > 0) {
			NBTTagList list = new NBTTagList();
			for (SpellTomeEnhancementWrapper enhance : enhancements) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setString(NBT_ENHANCEMENT_TYPE, enhance.getEnhancement().getTitleKey());
				tag.setInteger(NBT_ENHANCEMENT_LEVEL, enhance.getLevel());
				list.appendTag(tag);
			}
			
			nbt.setTag(NBT_ENHANCEMENTS, list);
		}
		
		stack.setTagCompound(nbt);
	 */
	
	public static List<SpellTomeEnhancementWrapper> getEnhancements(ItemStack stack) {
		if (stack == null || !(stack.getItem() instanceof SpellPlate))
			return null;
		
		if (!stack.hasTagCompound())
			return null;
		
		List<SpellTomeEnhancementWrapper> enhancements = new LinkedList<>();
		NBTTagList list = stack.getTagCompound().getTagList(NBT_ENHANCEMENTS, NBT.TAG_COMPOUND);
		if (list == null || list.hasNoTags())
			return enhancements;
		
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound tag = list.getCompoundTagAt(i);
			String key = tag.getString(NBT_ENHANCEMENT_TYPE);
			SpellTomeEnhancement enhance = SpellTomeEnhancement.lookupEnhancement(key);
			if (enhance == null)
				continue;
			int level = tag.getInteger(NBT_ENHANCEMENT_LEVEL);
			if (level == 0)
				continue;
			enhancements.add(new SpellTomeEnhancementWrapper(enhance, level));
		}
		
		return enhancements;
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
}
