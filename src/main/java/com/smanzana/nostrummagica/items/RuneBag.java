package com.smanzana.nostrummagica.items;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.NostrumGui;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class RuneBag extends Item implements ILoreTagged {

	private static final String NBT_VACUUM = "vacuum";
	private static final String NBT_ITEMS = "items";
	
	public static final int SLOTS = 27;
	
	public static final String ID = "rune_bag";
	
	private RuneBag() {
		super(NostrumItems.PropUnstackable());
	}
	
	public static void setItem(ItemStack bag, @Nonnull ItemStack item, int pos) {
		if (pos > SLOTS - 1)
			return;
		
		if (!bag.isEmpty() && bag.getItem() instanceof RuneBag) {
			if (!bag.hasTag())
				bag.setTag(new CompoundNBT());
			
			CompoundNBT nbt = bag.getTag();
			CompoundNBT items = nbt.getCompound(NBT_ITEMS);
			if (item.isEmpty())
				items.remove(pos + "");
			else {
				CompoundNBT compound = new CompoundNBT();
				item.write(compound);
				items.put(pos + "", compound);
			}
			
			nbt.put(NBT_ITEMS, items);
			bag.setTag(nbt);
		}
	}

	// returns what it couldn't fit
	public static ItemStack addItem(ItemStack bag, ItemStack inputItem) {
		if (inputItem.isEmpty())
			return inputItem;
		
		RuneInventory inv = (RuneInventory) NostrumItems.runeBag.asInventory(bag);
		return inv.addItem(inputItem);
	}
	
	public static @Nonnull ItemStack getItem(ItemStack bag, int pos) {
		if (pos > SLOTS - 1)
			return ItemStack.EMPTY;
		
		if (!bag.isEmpty() && bag.getItem() instanceof RuneBag) {
			if (!bag.hasTag())
				return ItemStack.EMPTY;
			
			CompoundNBT items = bag.getTag().getCompound(NBT_ITEMS);
			if (items.contains(pos + "", NBT.TAG_COMPOUND))
				return ItemStack.read(items.getCompound(pos + ""));
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
			if (!stack.hasTag())
				stack.setTag(new CompoundNBT());
			
			return stack.getTag().getBoolean(NBT_VACUUM);
		}
		
		return false;
	}
	
	public static boolean toggleVacuumEnabled(ItemStack stack) {
		if (!stack.isEmpty() && stack.getItem() instanceof RuneBag) {
			boolean enabled = isVacuumEnabled(stack);
			stack.getTag().putBoolean(NBT_VACUUM, !enabled);
			return !enabled;
		}
		
		return false;
	}
	
	public static void setVacuumEnabled(ItemStack stack, boolean set) {
		if (!stack.isEmpty() && stack.getItem() instanceof RuneBag) {
			stack.getTag().putBoolean(NBT_VACUUM, set);
		}
	}
	
	public RuneInventory asInventory(ItemStack bag) {
		return new RuneInventory(bag);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
		playerIn.openGui(NostrumMagica.instance, NostrumGui.runeBagID, worldIn,
				(int) playerIn.posX, (int) playerIn.posY, (int) playerIn.posZ);
		
		return new ActionResult<ItemStack>(ActionResultType.SUCCESS, playerIn.getHeldItem(hand));
    }
	
	public static class RuneInventory extends Inventory {

		private static final int MAX_COUNT = 1;
		
		private @Nonnull ItemStack stack;
		
		public RuneInventory(ItemStack stack) {
			super(SLOTS);
			
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
