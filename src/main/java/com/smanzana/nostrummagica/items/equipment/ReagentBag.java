package com.smanzana.nostrummagica.items.equipment;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.container.ReagentBagGui;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.items.NostrumItems;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.utils.Inventories;

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

public class ReagentBag extends Item implements ILoreTagged {

	private static final String NBT_VACUUM = "vacuum";
	private static final String NBT_ITEMS = "items";
	
	public static final int SLOTS = 9;
	
	public static final String ID = "reagent_bag";
	
	public ReagentBag() {
		super(NostrumItems.PropUnstackable());
	}
	
	public static int getReagentCount(ItemStack bag, ReagentType type) {
		if (bag.isEmpty() || !bag.hasTag())
			return 0;
		
		int count = 0;
		for (ItemStack item : getItems(bag)) {
			if (item.isEmpty())
				continue;
			if (!(item.getItem() instanceof ReagentItem))
				continue;
			
			ReagentItem reagentItem = (ReagentItem) item.getItem();
			if (reagentItem.getType() == type)
				count += item.getCount();
		}
		
		return count;
	}
	
	public static void setItem(ItemStack bag, ItemStack item, int pos) {
		if (pos > SLOTS - 1)
			return;
		
		if (!bag.isEmpty() && bag.getItem() instanceof ReagentBag) {
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
		
		ReagentInventory inv = (ReagentInventory) NostrumItems.reagentBag.asInventory(bag);
		return inv.addItem(inputItem);
	}
	
	// removes as much as we can and returns waht we couldn't.
	public static int removeCount(ItemStack bag, ReagentType type, int total) {
		if (bag.isEmpty())
			return total;
		
		ItemStack existing[] = getItems(bag);
		if (existing == null)
			return total;
		
		int remaining = total;
		int original = remaining;
		
		for (int i = 0; i < SLOTS; i++) {
			ItemStack item = existing[i];
			ReagentType existingType = ReagentItem.FindType(item);
			if (item.isEmpty() || null == existingType)
				continue;
				
			if (existingType == type) {
				if (item.getCount() > remaining) {
					item.shrink(remaining);
					remaining = 0;
					break;
				} else {
					remaining -= item.getCount();
					existing[i] = ItemStack.EMPTY;
				}
			}
		}
		
		if (remaining != original) {
			for (int i = 0; i < SLOTS; i++) {
				setItem(bag, existing[i], i);
			}
		}
		
		return remaining;
		
	}
	
	public static @Nonnull ItemStack getItem(ItemStack bag, int pos) {
		if (pos > SLOTS - 1)
			return ItemStack.EMPTY;
		
		if (!bag.isEmpty() && bag.getItem() instanceof ReagentBag) {
			if (!bag.hasTag())
				return ItemStack.EMPTY;
			
			CompoundNBT items = bag.getTag().getCompound(NBT_ITEMS);
			if (items.contains(pos + "", NBT.TAG_COMPOUND))
				return ItemStack.read(items.getCompound(pos + ""));
			else
				return ItemStack.EMPTY;
		}
		
		return ItemStack.EMPTY;
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
		if (!stack.isEmpty() && stack.getItem() instanceof ReagentBag) {
			if (!stack.hasTag())
				stack.setTag(new CompoundNBT());
			
			return stack.getTag().getBoolean(NBT_VACUUM);
		}
		
		return false;
	}
	
	public static boolean toggleVacuumEnabled(ItemStack stack) {
		if (!stack.isEmpty() && stack.getItem() instanceof ReagentBag) {
			boolean enabled = isVacuumEnabled(stack);
			stack.getTag().putBoolean(NBT_VACUUM, !enabled);
			return !enabled;
		}
		
		return false;
	}
	
	public static void setVacuumEnabled(ItemStack stack, boolean set) {
		if (!stack.isEmpty() && stack.getItem() instanceof ReagentBag) {
			stack.getTag().putBoolean(NBT_VACUUM, set);
		}
	}
	
	public ReagentInventory asInventory(ItemStack bag) {
		return new ReagentInventory(bag);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
		int pos = Inventories.getPlayerHandSlotIndex(playerIn.inventory, Hand.MAIN_HAND);
		ItemStack inHand = playerIn.getHeldItemMainhand();
		if (inHand.isEmpty()) {
			inHand = playerIn.getHeldItemOffhand();
			pos = Inventories.getPlayerHandSlotIndex(playerIn.inventory, Hand.OFF_HAND);
		}
		NostrumMagica.instance.proxy.openContainer(playerIn, ReagentBagGui.BagContainer.Make(pos));
		
		return new ActionResult<ItemStack>(ActionResultType.SUCCESS, playerIn.getHeldItem(hand));
    }
	
	public static class ReagentInventory extends Inventory {

		private static final int MAX_COUNT = 127;
		
		private @Nonnull ItemStack stack;
		//private ItemStack[] inventory;
		
		public ReagentInventory(ItemStack stack) {
			super(SLOTS);
			
			this.stack = stack;
			
			int i = 0;
			for (ItemStack reg : ReagentBag.getItems(stack)) {
				this.setInventorySlotContents(i++, reg);
			}
			
			//this. = ReagentBag.getItems(stack);
		}
		
	    /**
	     * Try to add the item to the invntory.
	     * Return what won't fit.
	     */
	    public ItemStack addItem(ItemStack stack) {
	    	ItemStack itemstack = stack.copy();

	    	if (!(stack.getItem() instanceof ReagentItem))
	    		return itemstack;

	    	for (int i = 0; i < this.getSizeInventory(); ++i) {
	            ItemStack itemstack1 = this.getStackInSlot(i);

	            if (itemstack1.isEmpty()) {
	                this.setInventorySlotContents(i, itemstack);
	                this.markDirty();
	                return ItemStack.EMPTY;
	            }

	            if (ItemStack.areItemsEqual(itemstack1, itemstack)) {
	                int j = this.getInventoryStackLimit();
	                int k = Math.min(itemstack.getCount(), j - itemstack1.getCount());

	                if (k > 0) {
	                    itemstack1.grow(k);
	                    itemstack.shrink(k);

	                    if (itemstack.getCount() <= 0) {
	                        this.markDirty();
	                        return ItemStack.EMPTY;
	                    }
	                }
	            }
	        }

	        if (itemstack.getCount() != stack.getCount()) {
	            this.markDirty();
	        }

	        return itemstack;
	    }

	    @Override
	    public void markDirty() {
	    	// Bleed our changes out to the itemstack
	    	if (!stack.isEmpty()) {
	    		for (int i = 0; i < this.getSizeInventory(); i++) {
	    			ReagentBag.setItem(stack, this.getStackInSlot(i), i);
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
	        return stack.getItem() instanceof ReagentItem;
	    }

		
	}

	@Override
	public String getLoreKey() {
		return "nostrum_reagent_bag";
	}

	@Override
	public String getLoreDisplayName() {
		return "Reagent Bag";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("Reagent bags provide extra storage for reagents.");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("Reagent bags provide extra storage for reagents.", "The bag can be configured to automatically take reagents when they are picked up.", "Reagent bags are searched when casting spells that take reagents.", "Holding shift while picking up items will ignore the vacuum feature.");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_REAGENTS;
	}
}
