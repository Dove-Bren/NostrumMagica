package com.smanzana.nostrummagica.items;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.NostrumGui;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class RuneBag extends Item implements ILoreTagged {

	private static final String NBT_VACUUM = "vacuum";
	private static final String NBT_ITEMS = "items";
	private static RuneBag instance = null;
	
	public static final int SLOTS = 27;
	
	public static RuneBag instance() {
		if (instance == null)
			instance = new RuneBag();
		
		return instance;
	}
	
	public static final String id = "rune_bag";
	
	private RuneBag() {
		super();
		this.setUnlocalizedName(id);
		this.setRegistryName(NostrumMagica.MODID, id);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setMaxStackSize(1);
	}
	
	public static void setItem(ItemStack bag, @Nonnull ItemStack item, int pos) {
		if (pos > SLOTS - 1)
			return;
		
		if (!bag.isEmpty() && bag.getItem() instanceof RuneBag) {
			if (!bag.hasTagCompound())
				bag.setTagCompound(new NBTTagCompound());
			
			NBTTagCompound nbt = bag.getTagCompound();
			NBTTagCompound items = nbt.getCompoundTag(NBT_ITEMS);
			if (item.isEmpty())
				items.removeTag(pos + "");
			else {
				NBTTagCompound compound = new NBTTagCompound();
				item.writeToNBT(compound);
				items.setTag(pos + "", compound);
			}
			
			nbt.setTag(NBT_ITEMS, items);
			bag.setTagCompound(nbt);
		}
	}

	// returns what it couldn't fit
	public static ItemStack addItem(ItemStack bag, ItemStack inputItem) {
		if (inputItem.isEmpty())
			return inputItem;
		
		RuneInventory inv = (RuneInventory) RuneBag.instance().asInventory(bag);
		return inv.addItem(inputItem);
	}
	
	public static @Nonnull ItemStack getItem(ItemStack bag, int pos) {
		if (pos > SLOTS - 1)
			return ItemStack.EMPTY;
		
		if (!bag.isEmpty() && bag.getItem() instanceof RuneBag) {
			if (!bag.hasTagCompound())
				return ItemStack.EMPTY;
			
			NBTTagCompound items = bag.getTagCompound().getCompoundTag(NBT_ITEMS);
			if (items.hasKey(pos + "", NBT.TAG_COMPOUND))
				return new ItemStack(items.getCompoundTag(pos + ""));
			else
				return ItemStack.EMPTY;
		}
		
		return null;
	}
	
	/**
	 * Returns either null (no bag) or an array of size SLOTS with either
	 * items or ItemStack.EMPTY
	 * @param bag
	 * @return
	 */
	public static ItemStack[] getItems(ItemStack bag) {
		if (bag.isEmpty())
			return null;
		
		ItemStack ret[] = new ItemStack[SLOTS];
		
		for (int i = 0; i < SLOTS; i++)
			ret[i] = getItem(bag, i);
		
		return ret;
	}
	
	public static boolean isVacuumEnabled(ItemStack stack) {
		if (!stack.isEmpty() && stack.getItem() instanceof RuneBag) {
			if (!stack.hasTagCompound())
				stack.setTagCompound(new NBTTagCompound());
			
			return stack.getTagCompound().getBoolean(NBT_VACUUM);
		}
		
		return false;
	}
	
	public static boolean toggleVacuumEnabled(ItemStack stack) {
		if (!stack.isEmpty() && stack.getItem() instanceof RuneBag) {
			boolean enabled = isVacuumEnabled(stack);
			stack.getTagCompound().setBoolean(NBT_VACUUM, !enabled);
			return !enabled;
		}
		
		return false;
	}
	
	public static void setVacuumEnabled(ItemStack stack, boolean set) {
		if (!stack.isEmpty() && stack.getItem() instanceof RuneBag) {
			stack.getTagCompound().setBoolean(NBT_VACUUM, set);
		}
	}
	
	public RuneInventory asInventory(ItemStack bag) {
		return new RuneInventory(bag);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		playerIn.openGui(NostrumMagica.instance, NostrumGui.runeBagID, worldIn,
				(int) playerIn.posX, (int) playerIn.posY, (int) playerIn.posZ);
		
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, playerIn.getHeldItem(hand));
    }
	
	public static class RuneInventory extends InventoryBasic {

		private static final int MAX_COUNT = 1;
		
		private @Nonnull ItemStack stack;
		
		public RuneInventory(ItemStack stack) {
			super("Rune Bag", false, SLOTS);
			
			this.stack = stack;
			
			int i = 0;
			for (ItemStack reg : RuneBag.getItems(stack)) {
				this.setInventorySlotContents(i++, reg);
			}
		}
		
	    /**
	     * Try to add the item to the invntory.
	     * Return what won't fit.
	     */
	    public @Nonnull ItemStack addItem(ItemStack stack) {
	    	ItemStack itemstack = stack.copy();

	    	if (!(stack.getItem() instanceof SpellRune))
	    		return itemstack;

	    	for (int i = 0; i < this.getSizeInventory(); ++i) {
	            ItemStack itemstack1 = this.getStackInSlot(i);

	            if (itemstack1.isEmpty()) {
	                this.setInventorySlotContents(i, itemstack);
	                this.markDirty();
	                return ItemStack.EMPTY;
	            }
	        }

	        return itemstack;
	    }

	    @Override
	    public void markDirty() {
	    	// Bleed our changes out to the itemstack
	    	if (!stack.isEmpty()) {
	    		for (int i = 0; i < this.getSizeInventory(); i++) {
	    			RuneBag.setItem(stack, this.getStackInSlot(i), i);
	    		}
	    	} else {
	    		System.out.println("no item base");
	    	}
	    	
	    	super.markDirty();
	    }
	    
	    /**
	     * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
	     */
	    public int getInventoryStackLimit()
	    {
	        return MAX_COUNT;
	    }
	    
	    /**
	     * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot. For
	     * guis use Slot.isItemValid
	     */
	    public boolean isItemValidForSlot(int index, ItemStack stack)
	    {
	        return stack.getItem() instanceof SpellRune;
	    }

		
	}

	@Override
	public String getLoreKey() {
		return "nostrum_rune_bag";
	}

	@Override
	public String getLoreDisplayName() {
		return "Rune Bag";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("Rune bags provide a nice place to put all of those shiny runes!");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("Rune bags provide a nice place to put all of those shiny runes!", "The bag can be configured to automatically take runes when they are picked up.", "Holding shift while picking up items will ignore the vacuum feature.");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
}
