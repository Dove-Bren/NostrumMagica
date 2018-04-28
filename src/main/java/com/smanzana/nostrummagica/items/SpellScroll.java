package com.smanzana.nostrummagica.items;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.ClientCastMessage;
import com.smanzana.nostrummagica.network.messages.SpellRequestMessage;
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

public class SpellScroll extends Item implements ILoreTagged {

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
		
		Spell spell = getSpell(itemStackIn);
		if (spell == null)
			return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStackIn);
		
		if (!playerIn.isCreative()) {
			itemStackIn.stackSize--;
		}

		if (worldIn.isRemote) {
			NetworkHandler.getSyncChannel().sendToServer(
	    			new ClientCastMessage(spell, true, 0));
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
	
	public static Spell getSpell(ItemStack itemStack) {
		if (itemStack == null || !(itemStack.getItem() instanceof SpellScroll))
			return null;
		
		NBTTagCompound nbt = itemStack.getTagCompound();		
		if (nbt == null)
			return null;
		
		int id = nbt.getInteger(NBT_SPELL);
		Spell spell = NostrumMagica.spellRegistry.lookup(id);
		
		if (spell == null) {
			NostrumMagica.logger.info("Requesting spell " + id
					 + " from the server...");
				NetworkHandler.getSyncChannel().sendToServer(
		    			new SpellRequestMessage(new int[] {id}));
		}
			
		return spell;
	}

	@Override
	public String getLoreKey() {
		return "nostrum_spell_scroll";
	}

	@Override
	public String getLoreDisplayName() {
		return "Spell Scrolls";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("Spell scrolls are created from blank scrolls, spell runes, and reagents.", "Using a spell scroll will cast the spell on it.");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("Spell scrolls are created from blank scrolls, spell runes, and reagents.", "Using a spell scroll will cast the spell on it.", "Scrolls can be bound to Spell Tomes so that they can be cast over and over at the cost of reagents.");
	}
}
