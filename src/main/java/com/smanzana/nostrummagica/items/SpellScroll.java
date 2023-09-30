package com.smanzana.nostrummagica.items;

import java.util.List;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.NostrumGui;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.ClientCastMessage;
import com.smanzana.nostrummagica.network.messages.SpellRequestMessage;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.SeekingBulletTrigger;
import com.smanzana.nostrummagica.utils.ItemStacks;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;

public class SpellScroll extends Item implements ILoreTagged, IRaytraceOverlay {

	private static final String NBT_SPELL = "nostrum_spell";
	private static final String NBT_DURABILITY = "max_uses";
	//private static final String NBT_WAKE_START = "nostrum_timer";
	//private static final String NBT_TYPE = "nostrum_type";
	//private static final int WAKE_TIME = 20 * 60 * 5;
	public static final String ID = "spell_scroll";
	
	public SpellScroll() {
		super(NostrumItems.PropUnstackable().rarity(Rarity.UNCOMMON).maxDamage(100));
	}
	
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
		final @Nonnull ItemStack itemStackIn = playerIn.getHeldItem(hand);
		
		if (playerIn.isSneaking()) {
			// Open scroll screen
			final int spellId = itemStackIn.getTag() == null ? 0 : itemStackIn.getTag().getInt(NBT_SPELL);
			playerIn.openGui(NostrumMagica.instance,
					NostrumGui.scrollID, worldIn,
					spellId, (int) playerIn.posY, (int) playerIn.posZ);
			return new ActionResult<ItemStack>(ActionResultType.SUCCESS, itemStackIn);
		}
		
		if (itemStackIn.isEmpty())
			return new ActionResult<ItemStack>(ActionResultType.PASS, itemStackIn);
		
		if (!itemStackIn.hasTag())
			return new ActionResult<ItemStack>(ActionResultType.PASS, itemStackIn);
		
		CompoundNBT nbt = itemStackIn.getTag();
		
		if (!nbt.contains(NBT_SPELL, NBT.TAG_INT))
			return new ActionResult<ItemStack>(ActionResultType.PASS, itemStackIn);
		
		Spell spell = getSpell(itemStackIn);
		if (spell == null)
			return new ActionResult<ItemStack>(ActionResultType.PASS, itemStackIn);
		
		if (!playerIn.isCreative()) {
			//itemStackIn.stackSize--;
			ItemStacks.damageItem(itemStackIn, playerIn, hand, 1);
		}

		if (worldIn.isRemote) {
			NetworkHandler.sendToServer(
	    			new ClientCastMessage(spell, true, 0));
		}
		
		if (itemStackIn.getDamage() > itemStackIn.getMaxDamage() // Old way, I think never happens?
				|| itemStackIn.isEmpty()) {
			// Going to break
			NostrumMagica.instance.getSpellRegistry().evict(spell);
		}
		
		return new ActionResult<ItemStack>(ActionResultType.SUCCESS, itemStackIn);
		
    }
	
	public static void setSpell(ItemStack itemStack, Spell spell) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellScroll))
			return;
		
		CompoundNBT nbt = itemStack.getTag();
		
		if (nbt == null)
			nbt = new CompoundNBT();
		
		nbt.putInt(NBT_SPELL, spell.getRegistryID());
		nbt.putInt(NBT_DURABILITY, GetMaxUses(spell));
		
		itemStack.setTag(nbt);
		itemStack.setDisplayName(new StringTextComponent(spell.getName()));
		itemStack.addEnchantment(Enchantments.POWER, 1);
	}
	
	public static Spell getSpell(ItemStack itemStack) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellScroll))
			return null;
		
		CompoundNBT nbt = itemStack.getTag();		
		if (nbt == null)
			return null;
		
		int id = nbt.getInt(NBT_SPELL);
		Spell spell = NostrumMagica.instance.getSpellRegistry().lookup(id);
		
		if (spell == null) {
			if (NostrumMagica.instance.proxy.isServer()) {
				NostrumMagica.logger.error("Failed to lookup spell in scroll with id " + id);
			} else {
				NostrumMagica.logger.info("Requesting spell " + id
						 + " from the server...");
					NetworkHandler.sendToServer(
			    			new SpellRequestMessage(new int[] {id}));
			}
		}
			
		return spell;
	}
	
	public static int getMaxDurability(ItemStack itemStack) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellScroll))
			return 1;
		
		CompoundNBT nbt = itemStack.getTag();		
		if (nbt == null || !nbt.contains(NBT_DURABILITY, NBT.TAG_INT))
			return 15; // old default
		
		return Math.max(1, nbt.getInt(NBT_DURABILITY));
	}
	
//	public static int getNestedScrollMeta(ItemStack scroll) {
//		byte ret = 0;
//		
//		if (!scroll.isEmpty() && scroll.hasTag()) {
//			CompoundNBT nbt = scroll.getTag();
//			ret = nbt.getByte(NBT_TYPE);
//		}
//		
//		return ret;
//	}
//	
//	public static void setNestedScrollMeta(ItemStack scroll, byte meta) {
//		if (scroll.isEmpty())
//			return;
//		
//		CompoundNBT nbt = scroll.getTag();
//		if (nbt == null)
//			nbt = new CompoundNBT();
//		
//		nbt.setByte(NBT_TYPE, meta);
//	}
	
	public static ItemStack create(Spell spell) {
		ItemStack scroll = new ItemStack(NostrumItems.spellScroll, 1);
		setSpell(scroll, spell);
		return scroll;
	}
	
	/**
	 * Figure out how many uses to give the provided spell.
	 * More complex spells have less uses per scroll.
	 */
	protected static final int GetMaxUses(Spell spell) {
		final int count = (spell == null ? 0 : spell.getComponentCount());
		if (count <= 2) {
			return 100;
		} else if (count <= 4) {
			return 50;
		} else if (count <= 6) {
			return 35;
		} else {
			return 20;
		}
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

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_SPELLS;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		;
	}
	
//	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
//		super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
//		
////		if (!worldIn.isRemote) {
////			if (stack.isRemote() || getNestedScrollMeta(stack) != 1)
////				return;
////			
////			CompoundNBT nbt;
////			if (!stack.hasTag())
////				nbt = new CompoundNBT();
////			else
////				nbt = stack.getTag();
////			
////			long start = nbt.getLong(NBT_WAKE_START);
////			long worldtime = worldIn.getMinecraftServer().getTickCounter();
////			if (start == 0) {
////				nbt.putLong(NBT_WAKE_START, worldtime);
////				stack.setTag(nbt);
////				return;
////			}
////			
////			if (worldtime > start + WAKE_TIME) {
////				setNestedScrollMeta(stack, (byte) 2);
////				if (!worldIn.isRemote) {
////					NostrumMagicaSounds.DAMAGE_ENDER.play(worldIn, entityIn.posX, entityIn.posY, entityIn.posZ);
////				}
////			}
////		}
//	}

	@Override
	public boolean shouldTrace(World world, PlayerEntity player, ItemStack stack) {
		Spell spell = getSpell(stack);
		SpellTrigger firstTrigger = null;
		if (spell != null && !spell.getSpellParts().isEmpty()) {
			firstTrigger = spell.getSpellParts().get(0).getTrigger();
		}
		
		return firstTrigger != null && firstTrigger instanceof SeekingBulletTrigger;
	}
	
	@Override
	public int getMaxDamage(ItemStack stack) {
		return getMaxDurability(stack);
	}
}
