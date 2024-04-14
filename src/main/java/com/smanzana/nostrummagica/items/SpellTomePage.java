package com.smanzana.nostrummagica.items;

import java.util.List;

import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.spelltome.enhancement.SpellTomeEnhancement;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SpellTomePage extends Item implements ILoreTagged {

	private static final String NBT_LEVEL = "nostrum_level";
	private static final String NBT_TYPE = "nostrum_type";
	
	public static final String ID = "spelltome_page";
	
	public SpellTomePage() {
		super(NostrumItems.PropTomeUnstackable().rarity(Rarity.UNCOMMON));
	}
	
	public static ItemStack Create(SpellTomeEnhancement enhancement, int level) {
		ItemStack stack = new ItemStack(NostrumItems.spellTomePage);
		
		CompoundNBT nbt = stack.getTag();
		
		if (nbt == null)
			nbt = new CompoundNBT();
		
		nbt.putInt(NBT_LEVEL, level);
		nbt.putString(NBT_TYPE, enhancement.getTitleKey());
		
		stack.setTag(nbt);
		return stack;
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
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		SpellTomeEnhancement enhance = getEnhancement(stack);
		if (enhance == null)
			return;
		int level = getLevel(stack);
		
		tooltip.add(new TranslationTextComponent(enhance.getNameFormat())
				.append(new StringTextComponent(" " + toRoman(level))));
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
	
	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		if (this.isInGroup(group)) {
			for (SpellTomeEnhancement enhancement : SpellTomeEnhancement.getEnhancements()) {
				items.add(Create(enhancement, 1));
				if (enhancement.getMaxLevel() != 1) {
					items.add(Create(enhancement, enhancement.getMaxLevel()));
				}
			}
		}
	}
	
	public static int getLevel(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof SpellTomePage))
			return 0;
		
		if (!stack.hasTag())
			return 0;
		
		return stack.getTag().getInt(NBT_LEVEL);
	}
	
	public static SpellTomeEnhancement getEnhancement(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof SpellTomePage))
			return null;
		
		if (!stack.hasTag())
			return null;
		
		String key = stack.getTag().getString(NBT_TYPE);
		SpellTomeEnhancement enhance = SpellTomeEnhancement.lookupEnhancement(key);
		return enhance;
	}
}
