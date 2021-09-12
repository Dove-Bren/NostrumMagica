package com.smanzana.nostrummagica.items;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.NostrumGui;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
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
//		GameRegistry.addRecipe(new ItemStack(instance), "GLG", "LRL", "LLL",
//				'L', Items.LEATHER,
//				'G', Items.GOLD_INGOT,
//				'R', ReagentItem.instance());
	}
	
	public static final String id = "reagent_bag";
	
	private ReagentBag() {
		super();
		this.setUnlocalizedName(id);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setMaxStackSize(1);
	}
	
	public static int getReagentCount(ItemStack bag, ReagentType type) {
		if (bag.isEmpty() || !bag.hasTagCompound())
			return 0;
		
		int count = 0;
		for (ItemStack item : getItems(bag)) {
			if (item.isEmpty())
				continue;
			if (!(item.getItem() instanceof ReagentItem))
				continue;
			
			if (ReagentItem.getTypeFromMeta(item.getMetadata())
					== type)
				count += item.getCount();
		}
		
		return count;
	}
	
	public static void setItem(ItemStack bag, ItemStack item, int pos) {
		if (pos > SLOTS - 1)
			return;
		
		if (!bag.isEmpty() && bag.getItem() instanceof ReagentBag) {
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
		
		ReagentInventory inv = (ReagentInventory) ReagentBag.instance().asInventory(bag);
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
			if (item.isEmpty())
				continue;
			
			if (ReagentItem.getTypeFromMeta(item.getMetadata())
					== type) {
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
			if (!bag.hasTagCompound())
				return ItemStack.EMPTY;
			
			NBTTagCompound items = bag.getTagCompound().getCompoundTag(NBT_ITEMS);
			if (items.hasKey(pos + "", NBT.TAG_COMPOUND))
				return new ItemStack(items.getCompoundTag(pos + ""));
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
			if (!stack.hasTagCompound())
				stack.setTagCompound(new NBTTagCompound());
			
			return stack.getTagCompound().getBoolean(NBT_VACUUM);
		}
		
		return false;
	}
	
	public static boolean toggleVacuumEnabled(ItemStack stack) {
		if (!stack.isEmpty() && stack.getItem() instanceof ReagentBag) {
			boolean enabled = isVacuumEnabled(stack);
			stack.getTagCompound().setBoolean(NBT_VACUUM, !enabled);
			return !enabled;
		}
		
		return false;
	}
	
	public static void setVacuumEnabled(ItemStack stack, boolean set) {
		if (!stack.isEmpty() && stack.getItem() instanceof ReagentBag) {
			stack.getTagCompound().setBoolean(NBT_VACUUM, set);
		}
	}
	
	public ReagentInventory asInventory(ItemStack bag) {
		return new ReagentInventory(bag);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		playerIn.openGui(NostrumMagica.instance, NostrumGui.reagentBagID, worldIn,
				(int) playerIn.posX, (int) playerIn.posY, (int) playerIn.posZ);
		
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, playerIn.getHeldItem(hand));
    }
	
	public static class ReagentInventory extends InventoryBasic {

		private static final int MAX_COUNT = 127;
		
		private @Nonnull ItemStack stack;
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
