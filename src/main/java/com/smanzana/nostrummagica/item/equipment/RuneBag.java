package com.smanzana.nostrummagica.item.equipment;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.container.RuneBagGui;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.SpellRune;
import com.smanzana.nostrummagica.loretag.ELoreCategory;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.util.Inventories;
import com.smanzana.nostrummagica.util.Inventories.ItemStackArrayWrapper;
import com.smanzana.nostrummagica.util.Inventories.IterableInventoryWrapper;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class RuneBag extends Item implements ILoreTagged {

	private static final String NBT_VACUUM = "vacuum";
	private static final String NBT_ITEMS = "items";
	
	public static final int SLOTS = 27;
	
	public static final String ID = "rune_bag";
	
	public RuneBag() {
		super(NostrumItems.PropEquipment());
	}
	
	public static void setItem(ItemStack bag, @Nonnull ItemStack item, int pos) {
		if (pos > SLOTS - 1)
			return;
		
		if (!bag.isEmpty() && bag.getItem() instanceof RuneBag) {
			if (!bag.hasTag())
				bag.setTag(new CompoundTag());
			
			CompoundTag nbt = bag.getTag();
			CompoundTag items = nbt.getCompound(NBT_ITEMS);
			if (item.isEmpty())
				items.remove(pos + "");
			else {
				CompoundTag compound = new CompoundTag();
				item.save(compound);
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
			
			CompoundTag items = bag.getTag().getCompound(NBT_ITEMS);
			if (items.contains(pos + "", Tag.TAG_COMPOUND))
				return ItemStack.of(items.getCompound(pos + ""));
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
				stack.setTag(new CompoundTag());
			
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
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
		int pos = Inventories.getPlayerHandSlotIndex(playerIn.getInventory(), InteractionHand.MAIN_HAND);
		ItemStack inHand = playerIn.getMainHandItem();
		if (inHand.isEmpty()) {
			inHand = playerIn.getOffhandItem();
			pos = Inventories.getPlayerHandSlotIndex(playerIn.getInventory(), InteractionHand.OFF_HAND);
		}
		NostrumMagica.Proxy.openContainer(playerIn, RuneBagGui.BagContainer.Make(pos));
		
		return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, playerIn.getItemInHand(hand));
    }
	
	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (context.getPlayer().isShiftKeyDown()) {
			// If sneaking, try and do container fast add.
			final BlockPos pos = context.getClickedPos();
			final BlockEntity te = context.getLevel().getBlockEntity(pos);
			if (te != null) {
				ItemStack[] contents = getItems(context.getItemInHand());
				if (Inventories.attemptAddToTile(new IterableInventoryWrapper(new ItemStackArrayWrapper(contents)), context.getLevel().getBlockState(pos), te, context.getClickedFace())) {
					// Update contents
					for (int i = 0; i < contents.length; i++) {
						setItem(context.getItemInHand(), contents[i], i);
					}
					return InteractionResult.SUCCESS;
				}
			}
			
			// Fall through to default behavior if we fail
		}
		
		return super.useOn(context);
	}
	
	public static class RuneInventory extends SimpleContainer {

		private static final int MAX_COUNT = 64;
		
		private @Nonnull ItemStack stack;
		
		public RuneInventory(ItemStack stack) {
			super(SLOTS);
			
			this.stack = stack;
			
			int i = 0;
			for (ItemStack reg : RuneBag.getItems(stack)) {
				this.setItem(i++, reg);
			}
		}
		
	    /**
	     * Try to add the item to the invntory.
	     * Return what won't fit.
	     */
	    public @Nonnull ItemStack addItem(ItemStack stack) {
	    	return Inventories.addItem(this, stack);
//	    	ItemStack itemstack = stack.copy();
//
//	    	if (!(stack.getItem() instanceof SpellRune))
//	    		return itemstack;
//
//	    	for (int i = 0; i < this.getSizeInventory(); ++i) {
//	            ItemStack itemstack1 = this.getStackInSlot(i);
//
//	            if (itemstack1.isEmpty()) {
//	                this.setInventorySlotContents(i, itemstack);
//	                this.markDirty();
//	                return ItemStack.EMPTY;
//	            }
//	        }
//
//	        return itemstack;
	    }

	    @Override
	    public void setChanged() {
	    	// Bleed our changes out to the itemstack
	    	if (!stack.isEmpty()) {
	    		for (int i = 0; i < this.getContainerSize(); i++) {
	    			RuneBag.setItem(stack, this.getItem(i), i);
	    		}
	    	} else {
	    		System.out.println("no item base");
	    	}
	    	
	    	super.setChanged();
	    }
	    
	    /**
	     * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
	     */
	    public int getMaxStackSize()
	    {
	        return MAX_COUNT;
	    }
	    
	    /**
	     * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot. For
	     * guis use Slot.isItemValid
	     */
	    public boolean canPlaceItem(int index, ItemStack stack)
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
	public ELoreCategory getCategory() {
		return ELoreCategory.ITEM;
	}
}
