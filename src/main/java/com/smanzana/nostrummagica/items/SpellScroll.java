package com.smanzana.nostrummagica.items;

import java.util.List;

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

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SpellScroll extends Item implements ILoreTagged, IRaytraceOverlay {

	private static final String NBT_SPELL = "nostrum_spell";
	private static final String NBT_DURABILITY = "max_uses";
	//private static final String NBT_WAKE_START = "nostrum_timer";
	//private static final String NBT_TYPE = "nostrum_type";
	//private static final int WAKE_TIME = 20 * 60 * 5;
	private static SpellScroll instance = null;
	
	public static SpellScroll instance() {
		if (instance == null)
			instance = new SpellScroll();
		
		return instance;
	}
	
	public static final String id = "spell_scroll";
	
	public static void init() {
		
		//GameRegistry.addRecipe(new ActivatedRecipe());
		
	}
	
	private SpellScroll() {
		super();
		this.setUnlocalizedName(id);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setMaxStackSize(1);
		this.setHasSubtypes(true);
		this.setMaxDamage(100);
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
//		int i = getNestedScrollMeta(stack);
//		
//		switch (i) {
//		case 1: return "item.spell_scroll_activated";
//		case 2: return "item.spell_scroll_awakened";
//		}
//		
		
		return this.getUnlocalizedName();
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {
		
		if (playerIn.isSneaking()) {
			// Open scroll screen
			playerIn.openGui(NostrumMagica.instance,
					NostrumGui.scrollID, worldIn,
					(int) playerIn.posX, (int) playerIn.posY, (int) playerIn.posZ);
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
		}
		
		if (itemStackIn == null)
			return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStackIn);
		
//		if (getNestedScrollMeta(itemStackIn) != 0)
//			return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStackIn);
		
		if (!itemStackIn.hasTagCompound())
			return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStackIn);
		
		NBTTagCompound nbt = itemStackIn.getTagCompound();
		
		if (!nbt.hasKey(NBT_SPELL, NBT.TAG_INT))
			return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStackIn);
		
		Spell spell = getSpell(itemStackIn);
		if (spell == null)
			return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStackIn);
		
		if (!playerIn.isCreative()) {
			//itemStackIn.stackSize--;
			itemStackIn.damageItem(1, playerIn);
		}

		if (worldIn.isRemote) {
			NetworkHandler.getSyncChannel().sendToServer(
	    			new ClientCastMessage(spell, true, 0));
		}
		
		if (itemStackIn.getItemDamage() > itemStackIn.getMaxDamage()) {
			// Going to break
			NostrumMagica.getSpellRegistry().evict(spell);
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
		nbt.setInteger(NBT_DURABILITY, GetMaxUses(spell));
		
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
		Spell spell = NostrumMagica.getSpellRegistry().lookup(id);
		
		if (spell == null) {
			if (NostrumMagica.proxy.isServer()) {
				NostrumMagica.logger.error("Failed to lookup spell in scroll with id " + id);
			} else {
				NostrumMagica.logger.info("Requesting spell " + id
						 + " from the server...");
					NetworkHandler.getSyncChannel().sendToServer(
			    			new SpellRequestMessage(new int[] {id}));
			}
		}
			
		return spell;
	}
	
	public static int getMaxDurability(ItemStack itemStack) {
		if (itemStack == null || !(itemStack.getItem() instanceof SpellScroll))
			return 1;
		
		NBTTagCompound nbt = itemStack.getTagCompound();		
		if (nbt == null || !nbt.hasKey(NBT_DURABILITY, NBT.TAG_INT))
			return 15; // old default
		
		return Math.max(1, nbt.getInteger(NBT_DURABILITY));
	}
	
//	public static int getNestedScrollMeta(ItemStack scroll) {
//		byte ret = 0;
//		
//		if (scroll != null && scroll.hasTagCompound()) {
//			NBTTagCompound nbt = scroll.getTagCompound();
//			ret = nbt.getByte(NBT_TYPE);
//		}
//		
//		return ret;
//	}
//	
//	public static void setNestedScrollMeta(ItemStack scroll, byte meta) {
//		if (scroll == null)
//			return;
//		
//		NBTTagCompound nbt = scroll.getTagCompound();
//		if (nbt == null)
//			nbt = new NBTTagCompound();
//		
//		nbt.setByte(NBT_TYPE, meta);
//	}
	
	public static ItemStack create(Spell spell) {
		ItemStack scroll = new ItemStack(instance(), 1);
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
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		if (stack == null)
			return;
		
//		int meta = getNestedScrollMeta(stack);
//		if (meta == 1) {
//			tooltip.add(ChatFormatting.DARK_BLUE + "Activated");
//		} else if (meta == 2) {
//			tooltip.add(ChatFormatting.DARK_GREEN + "Awakened");
//			tooltip.add(ChatFormatting.GRAY + "Use on an altar with a bound spell tome");
//			tooltip.add(ChatFormatting.GRAY + "to begin binding");
//		}
	}
	
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
		
//		if (!worldIn.isRemote) {
//			if (stack == null || getNestedScrollMeta(stack) != 1)
//				return;
//			
//			NBTTagCompound nbt;
//			if (!stack.hasTagCompound())
//				nbt = new NBTTagCompound();
//			else
//				nbt = stack.getTagCompound();
//			
//			long start = nbt.getLong(NBT_WAKE_START);
//			long worldtime = worldIn.getMinecraftServer().getTickCounter();
//			if (start == 0) {
//				nbt.setLong(NBT_WAKE_START, worldtime);
//				stack.setTagCompound(nbt);
//				return;
//			}
//			
//			if (worldtime > start + WAKE_TIME) {
//				setNestedScrollMeta(stack, (byte) 2);
//				if (!worldIn.isRemote) {
//					NostrumMagicaSounds.DAMAGE_ENDER.play(worldIn, entityIn.posX, entityIn.posY, entityIn.posZ);
//				}
//			}
//		}
	}
	
//	private static class ActivatedRecipe extends ShapedRecipes {
//
//		public ActivatedRecipe() {
//			super(3, 3, new ItemStack[] {
//				new ItemStack(Items.DIAMOND),
//				new ItemStack(Items.ENDER_PEARL),
//				new ItemStack(Items.DIAMOND),
//				new ItemStack(Items.ENDER_PEARL),
//				new ItemStack(instance(), 1, OreDictionary.WILDCARD_VALUE),
//				new ItemStack(Items.ENDER_PEARL),
//				new ItemStack(Items.DIAMOND),
//				new ItemStack(Items.ENDER_PEARL),
//				new ItemStack(Items.DIAMOND),
//			}, new ItemStack(instance(), 1, 1));
//			
//			RecipeSorter.register(NostrumMagica.MODID + ":ScrollRecipe_activated",
//					this.getClass(), Category.SHAPED, "after:minecraft:shaped");
//		}
//		
//		@Override
//		public ItemStack getCraftingResult(InventoryCrafting inv) {
//			// Clone input scroll and set meta
//			ItemStack scroll = inv.getStackInSlot(4);
//			Spell spell = getSpell(scroll);
//			if (spell == null) {
//				return null;
//			}
//			
//			scroll = scroll.copy();
//			scroll.setItemDamage(0);
//			setNestedScrollMeta(scroll, (byte) 1);
//			return scroll;
//		}
//	}

	@Override
	public boolean shouldTrace(World world, EntityPlayer player, ItemStack stack) {
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
