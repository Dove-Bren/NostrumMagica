package com.smanzana.nostrummagica.items;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.ClientCastMessage;
import com.smanzana.nostrummagica.spells.Spell;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class SpellScroll extends Item {

	private static final String NBT_SPELL = "nostrum_spell";
	private static SpellScroll instance = null;
	
	public static SpellScroll instance() {
		if (instance == null)
			instance = new SpellScroll();
		
		return instance;
	}
	
	public static final String id = "spell_scroll";
	
	private SpellScroll() {
		super();
		this.setUnlocalizedName(id);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setMaxStackSize(1);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {
		if (!itemStackIn.hasTagCompound())
			return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStackIn);
		
		NBTTagCompound nbt = itemStackIn.getTagCompound();
		
		if (!nbt.hasKey(NBT_SPELL, NBT.TAG_INT))
			return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStackIn);
		
		int id = nbt.getInteger(NBT_SPELL);
		Spell spell = NostrumMagica.spellRegistry.lookup(id);
		if (spell == null)
			return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStackIn);
		
		if (!playerIn.isCreative()) {
			itemStackIn.stackSize--;
		}

		if (worldIn.isRemote) {
			NetworkHandler.getSyncChannel().sendToServer(
	    			new ClientCastMessage(spell, true));
		}
		
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
		
    }
	
	public static void setSpell(ItemStack itemStack, Spell spell) {
		if (itemStack == null || !(itemStack.getItem() instanceof SpellScroll))
			return;
		
		NBTTagCompound nbt = itemStack.getTagCompound();
		
		if (nbt == null)
			nbt = new NBTTagCompound();
		
		nbt.setInteger(NBT_SPELL, spell.getRegistryID());
		
		itemStack.setTagCompound(nbt);
		itemStack.setStackDisplayName(spell.getName());
		itemStack.addEnchantment(Enchantment.getEnchantmentByLocation("power"), 1);
	}
}
