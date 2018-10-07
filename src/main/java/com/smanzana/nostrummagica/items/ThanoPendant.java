package com.smanzana.nostrummagica.items;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Thano Pendant. Stores XP, makes casting reagentless
 * @author Skyler
 *
 */
public class ThanoPendant extends Item implements ILoreTagged, ISpellArmor {

	public static final String ID = "pendant_whole";
	private static final String NBT_THANOS_XP = "thanos_xp";
	private static final int THANOS_XP_PER = 10;
	private static final int MAX_THANOS_XP = 5 * THANOS_XP_PER;
	
	private static ThanoPendant instance = null;
	public static ThanoPendant instance() {
		if (instance == null)
			instance = new ThanoPendant();
		
		return instance;
	}
	
	public ThanoPendant() {
		super();
		this.setUnlocalizedName(ID);
		this.setMaxDamage(MAX_THANOS_XP / THANOS_XP_PER);
		this.setMaxStackSize(1);
		this.setCreativeTab(NostrumMagica.creativeTab);
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
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
		if (stack == null)
			return;
		
		tooltip.add(I18n.format("item.info.thanos.desc", (Object[]) null));
		int charges = thanosGetWholeCharges(stack);
		tooltip.add(ChatFormatting.GREEN + I18n.format("item.info.thanos.charges", new Object[] {charges}));
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
		if (stack == null)
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
		if (stack == null)
			return;
		
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null)
			nbt = new NBTTagCompound();
		
		nbt.setInteger(NBT_THANOS_XP, xp);
		stack.setTagCompound(nbt);
		
		int count = thanosGetWholeCharges(stack);
		int max = MAX_THANOS_XP / THANOS_XP_PER;
		stack.setItemDamage(max - count);
	}
	
	public static int thanosGetXP(ItemStack stack) {
		if (stack == null || !stack.hasTagCompound())
			return 0;
		
		NBTTagCompound nbt = stack.getTagCompound();
		return nbt.getInteger(NBT_THANOS_XP);
	}

	@Override
	public void apply(EntityLivingBase caster, SpellCastSummary summary, ItemStack stack) {
		if (stack == null)
			return;
		
		if (summary.getReagentCost() <= 0) {
			return;
		}
		
		int charges = thanosGetWholeCharges(stack);
		if (charges > 0) {
			if (!(caster instanceof EntityPlayer) || !((EntityPlayer) caster).isCreative()) {
				thanosSpendCharge(stack);
			}
			summary.addReagentCost(-1f);
		}
	}
	
}
