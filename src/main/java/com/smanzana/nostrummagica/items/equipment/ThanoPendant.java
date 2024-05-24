package com.smanzana.nostrummagica.items.equipment;

import java.util.List;

import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.items.ISpellEquipment;
import com.smanzana.nostrummagica.items.NostrumItems;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Thano Pendant. Stores XP, makes casting reagentless
 * @author Skyler
 *
 */
public class ThanoPendant extends Item implements ILoreTagged, ISpellEquipment {

	public static final String ID = "pendant_whole";
	private static final String NBT_THANOS_XP = "thanos_xp";
	private static final int THANOS_XP_PER = 10;
	private static final int MAX_THANOS_XP = 5 * THANOS_XP_PER;
	
	public ThanoPendant() {
		super(NostrumItems.PropUnstackable()
				.rarity(Rarity.UNCOMMON)
				.maxDamage(MAX_THANOS_XP / THANOS_XP_PER));
	}
	
	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}
    
    @Override
	public String getLoreKey() {
		return "nostrum_thanos_pendant";
	}

	@Override
	public String getLoreDisplayName() {
		return "Thano Pendant";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("Thano pendants are formed from fusing together two fragments found in shrines.", "The pendants have a curious ability to store raw power...!");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("Thano pendants are formed from fusing together two fragments found in shrines.", "These pendants absorb XP and provide 'charges' which you can use to cast a spell without using reagents.", "The pendant must be in your mainhand or offhand to use it.", "It will absorb xp from anywhere in your inventory.");
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		tooltip.add(new TranslationTextComponent("item.nostrummagica.info.thanos.desc"));
		int charges = thanosGetWholeCharges(stack);
		tooltip.add(new TranslationTextComponent("item.nostrummagica.info.thanos.charges", charges).mergeStyle(TextFormatting.GREEN));
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
	public static int thanosGetWholeCharges(ItemStack stack) {
		int xp = thanosGetXP(stack);
		return xp / THANOS_XP_PER;
	}
	
	public static void thanosSpendCharge(ItemStack stack) {
		int xp = thanosGetXP(stack);
		if (xp >= THANOS_XP_PER)
			xp -= THANOS_XP_PER;
		
		thanosSetXP(stack, xp);
	}
	
	/**
	 * Returns leftover xp
	 * @param stack
	 * @param xp
	 * @return
	 */
	public static int thanosAddXP(ItemStack stack, int xp) {
		if (stack.isEmpty())
			return xp;
		
		int inPendant = thanosGetXP(stack);
		int space = MAX_THANOS_XP - inPendant;
		int remaining;
		if (space >= xp) {
			inPendant += xp;
			remaining = 0;
		} else {
			inPendant = MAX_THANOS_XP;
			remaining = xp - space;
		}
		
		thanosSetXP(stack, inPendant);
		
		return remaining;
	}
	
	private static void thanosSetXP(ItemStack stack, int xp) {
		if (stack.isEmpty())
			return;
		
		CompoundNBT nbt = stack.getTag();
		if (nbt == null)
			nbt = new CompoundNBT();
		
		nbt.putInt(NBT_THANOS_XP, xp);
		stack.setTag(nbt);
		
		int count = thanosGetWholeCharges(stack);
		int max = MAX_THANOS_XP / THANOS_XP_PER;
		stack.setDamage(max - count);
	}
	
	public static int thanosGetXP(ItemStack stack) {
		if (stack.isEmpty() || !stack.hasTag())
			return 0;
		
		CompoundNBT nbt = stack.getTag();
		return nbt.getInt(NBT_THANOS_XP);
	}

	@Override
	public void apply(LivingEntity caster, SpellCastSummary summary, ItemStack stack) {
		if (stack.isEmpty())
			return;
		
		if (summary.getReagentCost() <= 0) {
			return;
		}
		
		int charges = thanosGetWholeCharges(stack);
		if (charges > 0) {
			if (!(caster instanceof PlayerEntity) || !((PlayerEntity) caster).isCreative()) {
				thanosSpendCharge(stack);
			}
			summary.addReagentCost(-1f);
		}
	}
	
}
