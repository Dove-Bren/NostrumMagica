package com.smanzana.nostrummagica.item;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.item.equipment.SpellTome;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.spelltome.enhancement.SpellTomeEnhancement;
import com.smanzana.nostrummagica.spelltome.enhancement.SpellTomeEnhancementWrapper;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SpellPlate extends Item implements ILoreTagged {

	/*
	 * Base capacity
	 * Bonus enhancements
	 */
	
	private static final String NBT_CAPACITY = "capacity";
	private static final String NBT_SLOTS = "slots";
	private static final String NBT_ENHANCEMENTS = "enhancements";
	private static final String NBT_ENHANCEMENT_LEVEL = "";
	private static final String NBT_ENHANCEMENT_TYPE = "";
	
	public static final String ID_PREFIX = "spelltomeplate_";
	
	private final SpellTome.TomeStyle style;
	
	public SpellPlate(SpellTome.TomeStyle style) {
		super(NostrumItems.PropTomeUnstackable().rarity(Rarity.UNCOMMON));
		this.style = style;
	}
	
	public SpellTome.TomeStyle getStyle() {
		return style;
	}
	
	public static SpellPlate GetPlateForStyle(SpellTome.TomeStyle style) {
		SpellPlate plate = null;
		switch (style) {
		case ADVANCED:
			plate = NostrumItems.spellPlateAdvanced;
			break;
		case COMBAT:
			plate = NostrumItems.spellPlateCombat;
			break;
		case DEATH:
			plate = NostrumItems.spellPlateDeath;
			break;
		case LIVING:
			plate = NostrumItems.spellPlateLiving;
			break;
		case MUTED:
			plate = NostrumItems.spellPlateMuted;
			break;
		case NOVICE:
			plate = NostrumItems.spellPlateNovice;
			break;
		case SPOOKY:
			plate = NostrumItems.spellPlateSpooky;
			break;
		}
		return plate;
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
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		final int capacity = getCapacity(stack);
		final int slots = getSlots(stack);
		tooltip.add(new TranslatableComponent("info.tome.slots", slots));
		if (capacity > 0) {
			tooltip.add(new TranslatableComponent("info.tome.capacity", capacity));
		}
		
		List<SpellTomeEnhancementWrapper> enhancements = getEnhancements(stack);
		if (enhancements != null && !enhancements.isEmpty()) {
			tooltip.add(new TextComponent(""));
			for (SpellTomeEnhancementWrapper enhance : enhancements) {
				tooltip.add(
						new TranslatableComponent(enhance.getEnhancement().getNameFormat())
						.append(new TextComponent(" " + toRoman(enhance.getLevel())))
				);
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
		if (stack .isEmpty() || !(stack.getItem() instanceof SpellPlate))
			return 0;
		
		if (!stack.hasTag())
			return 0;
		
		return stack.getTag().getInt(NBT_CAPACITY);
	}
	
	public static int getSlots(ItemStack stack) {
		if (stack .isEmpty() || !(stack.getItem() instanceof SpellPlate))
			return 0;
		
		if (!stack.hasTag())
			return 2; // Default for ones made before slots were introduced
		
		return stack.getTag().getInt(NBT_SLOTS);
	}
	
	public static List<SpellTomeEnhancementWrapper> getEnhancements(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof SpellPlate))
			return null;
		
		if (!stack.hasTag())
			return null;
		
		List<SpellTomeEnhancementWrapper> enhancements = new LinkedList<>();
		ListTag list = stack.getTag().getList(NBT_ENHANCEMENTS, Tag.TAG_COMPOUND);
		if (list == null || list.isEmpty())
			return enhancements;
		
		for (int i = 0; i < list.size(); i++) {
			CompoundTag tag = list.getCompound(i);
			String key = tag.getString(NBT_ENHANCEMENT_TYPE);
			SpellTomeEnhancement enhance = SpellTomeEnhancement.lookupEnhancement(key);
			if (enhance == null)
				continue;
			int level = tag.getInt(NBT_ENHANCEMENT_LEVEL);
			if (level == 0)
				continue;
			enhancements.add(new SpellTomeEnhancementWrapper(enhance, level));
		}
		
		return enhancements;
	}
	
	public static void setCapacity(ItemStack stack, int capacity) {
		CompoundTag tag = stack.getTag();
		if (tag == null) {
			tag = new CompoundTag();
		}
		
		tag.putInt(NBT_CAPACITY, capacity);
		
		stack.setTag(tag);
	}
	
	public static void setSlots(ItemStack stack, int slots) {
		CompoundTag tag = stack.getTag();
		if (tag == null) {
			tag = new CompoundTag();
		}
		
		tag.putInt(NBT_SLOTS, slots);
		
		stack.setTag(tag);
	}
	
	public static void setEnhancements(ItemStack stack, List<SpellTomeEnhancementWrapper> enhancements) {
		CompoundTag tag = stack.getTag();
		if (tag == null) {
			tag = new CompoundTag();
		}
		
		if (enhancements != null && enhancements.size() > 0) {
			ListTag list = new ListTag();
			for (SpellTomeEnhancementWrapper enhance : enhancements) {
				CompoundTag subtag = new CompoundTag();
				subtag.putString(NBT_ENHANCEMENT_TYPE, enhance.getEnhancement().getTitleKey());
				subtag.putInt(NBT_ENHANCEMENT_LEVEL, enhance.getLevel());
				list.add(subtag);
			}
			
			tag.put(NBT_ENHANCEMENTS, list);
		} else {
			tag.remove(NBT_ENHANCEMENTS);
		}
		
		stack.setTag(tag);
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
	@Override
	public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
		if (this.allowdedIn(group)) {
			ItemStack stack = new ItemStack(this);
			setCapacity(stack, 5);
			setSlots(stack, 2);
			items.add(stack);
			
			stack = new ItemStack(this);
			setCapacity(stack, 10);
			setSlots(stack, 5);
			items.add(stack);
		}
	}
}
