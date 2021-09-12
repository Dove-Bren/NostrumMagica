package com.smanzana.nostrummagica.items;

import java.util.List;

import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.spelltome.enhancement.SpellTomeEnhancement;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;

public class SpellTomePage extends Item implements ILoreTagged {

	private static final String NBT_LEVEL = "nostrum_level";
	private static final String NBT_TYPE = "nostrum_type";
	private static SpellTomePage instance = null;
	
	public static SpellTomePage instance() {
		if (instance == null)
			instance = new SpellTomePage();
		
		return instance;
	}
	
	public static void init() {
		RecipeSorter.register("SpellTomeUpgradeRecipe", TomePageRecipe.class, Category.SHAPELESS, "");
		GameRegistry.addRecipe(new TomePageRecipe());
	}
	
	public static final String id = "spelltome_page";
	
	private SpellTomePage() {
		super();
		this.setUnlocalizedName(id);
		//this.setCreativeTab(NostrumMagica.creativeTab); // set as icon for tab
		this.setMaxStackSize(1);
	}
	
	public static ItemStack getItemstack(SpellTomeEnhancement enhancement, int level) {
		ItemStack stack = new ItemStack(instance);
		
		NBTTagCompound nbt = stack.getTagCompound();
		
		if (nbt == null)
			nbt = new NBTTagCompound();
		
		nbt.setInteger(NBT_LEVEL, level);
		nbt.setString(NBT_TYPE, enhancement.getTitleKey());
		
		stack.setTagCompound(nbt);
		return stack;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
		// For all registered enhancements, create an item in the creative tab
		for (SpellTomeEnhancement enhancement : SpellTomeEnhancement.getEnhancements()) {
			subItems.add(getItemstack(enhancement, enhancement.getMaxLevel()));
		}
	}

	@Override
	public String getLoreKey() {
		return "nostrum_spelltome_page";
	}

	@Override
	public String getLoreDisplayName() {
		return "Spelltome Pages";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("These pages hold some sort of power, but they don't seem to apply to regular weapons and tools.");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("These pages modify spells cast from a spell tome.", "Pages can be added during spell tome creation to add their effects to the tome.");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		SpellTomeEnhancement enhance = getEnhancement(stack);
		if (enhance == null)
			return;
		int level = getLevel(stack);
		
		tooltip.add(I18n.format(enhance.getNameFormat(), new Object[0]) + " " + toRoman(level));
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

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_TOMES;
	}
	
	public static int getLevel(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof SpellTomePage))
			return 0;
		
		if (!stack.hasTagCompound())
			return 0;
		
		return stack.getTagCompound().getInteger(NBT_LEVEL);
	}
	
	public static SpellTomeEnhancement getEnhancement(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof SpellTomePage))
			return null;
		
		if (!stack.hasTagCompound())
			return null;
		
		String key = stack.getTagCompound().getString(NBT_TYPE);
		SpellTomeEnhancement enhance = SpellTomeEnhancement.lookupEnhancement(key);
		return enhance;
	}
	
	private static class TomePageRecipe implements IRecipe {

		@Override
		public boolean matches(InventoryCrafting inv, World worldIn) {
			SpellTomeEnhancement enhancement = null;
			int count = 0;
			
			for (int i = 0; i < inv.getSizeInventory(); i++) {
				ItemStack stack = inv.getStackInSlot(i);
				if (stack.isEmpty())
					continue;
				
				if (!(stack.getItem() instanceof SpellTomePage))
					return false;
				
				SpellTomeEnhancement cur = SpellTomePage.getEnhancement(stack);
				if (cur == null)
					return false;
				
				if (enhancement != null && enhancement != cur)
					return false;
				
				int level = SpellTomePage.getLevel(stack);
				if (0 == level)
					return false;
				
				if (level >= cur.getMaxLevel())
					return false;
				
				enhancement = cur;
				count++;
			}
			
			return count > 1;
		}

		@Override
		public ItemStack getCraftingResult(InventoryCrafting inv) {
			SpellTomeEnhancement enhancement = null;
			int sum = 0;
			int count = 0;
			
			for (int i = 0; i < inv.getSizeInventory(); i++) {
				ItemStack stack = inv.getStackInSlot(i);
				if (stack == null)
					continue;
				
				if (!(stack.getItem() instanceof SpellTomePage))
					return ItemStack.EMPTY;
				
				SpellTomeEnhancement cur = SpellTomePage.getEnhancement(stack);
				if (cur == null)
					return ItemStack.EMPTY;
				
				if (enhancement != null && enhancement != cur)
					return ItemStack.EMPTY;
				
				int level = SpellTomePage.getLevel(stack);
				if (0 == level)
					return ItemStack.EMPTY;
				
				// Optimization to disallow adding when you are already max level
				if (level >= cur.getMaxLevel())
					return ItemStack.EMPTY;
				
				enhancement = cur;
				sum += level;
				count++;
			}
			
			if (count < 2 || enhancement == null)
				return ItemStack.EMPTY;
			
			// Check level
			int level = 1 + (int) (Math.log(sum) / Math.log(2)); // Log base 2, floored
			if (level > enhancement.getMaxLevel())
				return ItemStack.EMPTY;
			
			return getItemstack(enhancement, level);
		}

		@Override
		public int getRecipeSize() {
			return 4;
		}

		@Override
		public ItemStack getRecipeOutput() {
			return SpellTomePage.getItemstack(SpellTomeEnhancement.EFFICIENCY, 1);
		}

		@Override
		public ItemStack[] getRemainingItems(InventoryCrafting inv) {
			return new ItemStack[inv.getSizeInventory()];
		}
		
	}
}
