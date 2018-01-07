package com.smanzana.nostrummagica.items;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.NostrumGui;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.lore.ILoreTagged;
import com.smanzana.nostrummagica.lore.Lore;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ReagentBag extends Item implements ILoreTagged {

	private static final String NBT_VACUUM = "vacuum";
	private static final String NBT_ITEMS = "items";
	private static ReagentBag instance = null;
	
	public static final int SLOTS = 9;
	
	public static ReagentBag instance() {
		if (instance == null)
			instance = new ReagentBag();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.addRecipe(new ItemStack(instance), "GLG", "LRL", "LLL",
				'L', Items.LEATHER,
				'G', Items.GOLD_INGOT,
				'R', ReagentItem.instance());
	}
	
	public static final String id = "reagent_bag";
	
	private ReagentBag() {
		super();
		this.setUnlocalizedName(id);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setMaxStackSize(1);
	}
	
	public static int getReagentCount(ItemStack bag, ReagentType type) {
		if (bag == null || !bag.hasTagCompound())
			return 0;
		
		int count = 0;
		for (ItemStack item : getItems(bag)) {
			if (item == null)
				continue;
			if (!(item.getItem() instanceof ReagentItem))
				continue;
			
			if (ReagentItem.instance().getTypeFromMeta(item.getMetadata())
					== type)
				count += item.stackSize;
		}
		
		return count;
	}
	
	public static void setItem(ItemStack bag, ItemStack item, int pos) {
		if (pos > SLOTS - 1)
			return;
		
		if (bag != null && bag.getItem() instanceof ReagentBag) {
			if (!bag.hasTagCompound())
				bag.setTagCompound(new NBTTagCompound());
			
			NBTTagCompound nbt = bag.getTagCompound();
			NBTTagCompound items = nbt.getCompoundTag(NBT_ITEMS);
			if (item == null)
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
		if (inputItem == null)
			return inputItem;
		
		ItemStack existing[] = getItems(bag);
		if (existing == null)
			return inputItem;
		
		int remaining = inputItem.stackSize;
		int original = remaining;
		for (int i = 0; i < SLOTS; i++) {
			ItemStack item = existing[i];
			if (item == null)
				continue;
			
			if (item.getMetadata() == inputItem.getMetadata()) {
				remaining -= 64 - item.stackSize;
				if (remaining >= 0)
					item.stackSize = 64;
				else
					item.stackSize = 64 + remaining;
				
				if (remaining <= 0)
					break;
			}
		}
		
		if (remaining > 0) {
			// Could'nt fit into existing stacks. Just use first empty one
			for (int i = 0; i < SLOTS; i++) {
				if (existing[i] == null) {
					existing[i] = inputItem.copy();
					remaining -= 64;
					if (remaining >= 0)
						existing[i].stackSize = 64;
					else
						existing[i].stackSize = 64 + remaining;
					
					if (remaining <= 0)
						break;
				}
			}
		}
		
		if (original != remaining) {
			for (int i = 0; i < SLOTS; i++) {
				setItem(bag, existing[i], i);
			}
		}
		
		if (remaining > 0) {
			inputItem.stackSize = remaining;
			return inputItem;
		}
		
		return null;
	}
	
	// removes as much as we can and returns waht we couldn't.
	public static int removeCount(ItemStack bag, ReagentType type, int total) {
		if (bag == null)
			return total;
		
		ItemStack existing[] = getItems(bag);
		if (existing == null)
			return total;
		
		int remaining = total;
		int original = remaining;
		
		for (int i = 0; i < SLOTS; i++) {
			ItemStack item = existing[i];
			if (item == null)
				continue;
			
			if (ReagentItem.instance().getTypeFromMeta(item.getMetadata())
					== type) {
				if (item.stackSize > remaining) {
					item.stackSize -= remaining;
					remaining = 0;
					break;
				} else {
					remaining -= item.stackSize;
					existing[i] = null;
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
	
	public static ItemStack getItem(ItemStack bag, int pos) {
		if (pos > SLOTS - 1)
			return null;
		
		if (bag != null && bag.getItem() instanceof ReagentBag) {
			if (!bag.hasTagCompound())
				return null;
			
			NBTTagCompound items = bag.getTagCompound().getCompoundTag(NBT_ITEMS);
			if (items.hasKey(pos + "", NBT.TAG_COMPOUND))
				return ItemStack.loadItemStackFromNBT(items.getCompoundTag(pos + ""));
			else
				return null;
		}
		
		return null;
	}
	
	/**
	 * Returns either null (no bag) or an array of size SLOTS with either
	 * items or nulls
	 * @param bag
	 * @return
	 */
	public static ItemStack[] getItems(ItemStack bag) {
		if (bag == null)
			return null;
		
		ItemStack ret[] = new ItemStack[SLOTS];
		
		for (int i = 0; i < SLOTS; i++)
			ret[i] = getItem(bag, i);
		
		return ret;
	}
	
	public static boolean isVacuumEnabled(ItemStack stack) {
		if (stack != null && stack.getItem() instanceof ReagentBag) {
			if (!stack.hasTagCompound())
				stack.setTagCompound(new NBTTagCompound());
			
			return stack.getTagCompound().getBoolean(NBT_VACUUM);
		}
		
		return false;
	}
	
	public static boolean toggleVacuumEnabled(ItemStack stack) {
		if (stack != null && stack.getItem() instanceof ReagentBag) {
			boolean enabled = isVacuumEnabled(stack);
			stack.getTagCompound().setBoolean(NBT_VACUUM, !enabled);
			return !enabled;
		}
		
		return false;
	}
	
	public static void setVacuumEnabled(ItemStack stack, boolean set) {
		if (stack != null && stack.getItem() instanceof ReagentBag) {
			stack.getTagCompound().setBoolean(NBT_VACUUM, set);
		}
	}
	
	public IInventory asInventory(ItemStack bag) {
		IInventory inv = new ReagentInventory(bag);
		
		
		
		return inv;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {
		playerIn.openGui(NostrumMagica.instance, NostrumGui.reagentBagID, worldIn,
				(int) playerIn.posX, (int) playerIn.posY, (int) playerIn.posZ);
		
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
    }
	
	public static class ReagentInventory extends InventoryBasic {

		private static final int MAX_COUNT = 256;
		
		private ItemStack stack;
		//private ItemStack[] inventory;
		
		public ReagentInventory(ItemStack stack) {
			super("Reagent Bag", false, SLOTS);
			
			this.stack = stack;
			
			int i = 0;
			for (ItemStack reg : ReagentBag.getItems(stack)) {
				this.setInventorySlotContents(i++, reg);
			}
			
			//this. = ReagentBag.getItems(stack);
		}
		
//		/**
//	     * Returns the stack in the given slot.
//	     */
//	    public ItemStack getStackInSlot(int index) {
//	        return index >= 0 && index < this.inventory.length ? this.inventory[index] : null;
//	    }

//	    /**
//	     * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
//	     */
//	    public ItemStack decrStackSize(int index, int count) {
//	        ItemStack itemstack = ItemStackHelper.getAndSplit(this.inventory, index, count);
//
//	        if (itemstack != null)
//	        {
//	            this.markDirty();
//	        }
//
//	        return itemstack;
//	    }

	    /**
	     * Try to add the item to the invntory.
	     * Return what won't fit.
	     */
	    public ItemStack addItem(ItemStack stack) {
	        ItemStack itemstack = stack.copy();
	        
	        if (!(stack.getItem() instanceof ReagentItem))
	        	return itemstack;

	        return super.addItem(stack);
	    }

//	    /**
//	     * Removes a stack from the given slot and returns it.
//	     */
//	    public ItemStack removeStackFromSlot(int index) {
//	        if (this.reagents[index] != null)
//	        {
//	            ItemStack itemstack = this.reagents[index];
//	            this.reagents[index] = null;
//	            markDirty();
//	            return itemstack;
//	        }
//	        else
//	        {
//	            return null;
//	        }
//	    }
	    
	    @Override
	    public void markDirty() {
	    	// Bleed our changes out to the itemstack
	    	if (stack != null) {
	    		for (int i = 0; i < this.getSizeInventory(); i++) {
	    			ReagentBag.setItem(stack, this.getStackInSlot(i), i);
	    		}
	    	} else {
	    		System.out.println("no item base");
	    	}
	    	
	    	super.markDirty();
	    }

//	    /**
//	     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
//	     */
//	    public void setInventorySlotContents(int index, @Nullable ItemStack stack) {
//	    	// We return 'true' to 'can this go here' for all reagents.
//	    	// When we get here, though, we actually don't respect the index they
//	    	// say and instead put it where it should go
//	    	
//	    	
////	    	if (stack == null) {
////	    		if (index >= reagents.length)
////		    		return;
////	    		this.reagents[index] = null;
////	    		markDirty();
////	    	} else {
////	    		addItem(stack); // We don't do strict setting here
////	    	}
//	    	
//	        this.reagents[index] = stack;
//
//	        if (stack != null && stack.stackSize > MAX_COUNT)
//	        {
//	            stack.stackSize = MAX_COUNT;
//	        }
//
//	        this.markDirty();
//	    }
	    
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

//	    public void clear()
//	    {
//	        for (int i = 0; i < this.reagents.length; ++i)
//	        {
//	            this.inventory[i] = null;
//	        }
//	    }
		
		
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
		return new Lore().add("Reagent bags provide extra storage for reagents.", "The bag can be configured to automatically take reagents when they are picked up.", "Reagent bags are searched when casting spells that take reagents.");
	}
}
