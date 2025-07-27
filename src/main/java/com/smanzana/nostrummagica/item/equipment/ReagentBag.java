package com.smanzana.nostrummagica.item.equipment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.CandleBlock;
import com.smanzana.nostrummagica.client.gui.container.ReagentBagGui;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.loretag.ELoreCategory;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.ritual.AltarRitualLayout;
import com.smanzana.nostrummagica.ritual.IRitualIngredients;
import com.smanzana.nostrummagica.ritual.RitualRecipe;
import com.smanzana.nostrummagica.ritual.RitualRegistry;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.tile.PedestalBlockEntity;
import com.smanzana.nostrummagica.tile.CandleTileEntity;
import com.smanzana.nostrummagica.util.Inventories;
import com.smanzana.nostrummagica.util.Inventories.ItemStackArrayWrapper;
import com.smanzana.nostrummagica.util.Inventories.IterableInventoryWrapper;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ReagentBag extends Item implements ILoreTagged {

	private static final String NBT_VACUUM = "vacuum";
	private static final String NBT_ITEMS = "items";
	
	public static final int SLOTS = 9;
	
	public static final String ID = "reagent_bag";
	
	public ReagentBag() {
		super(NostrumItems.PropEquipment());
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
			
			CompoundTag items = bag.getTag().getCompound(NBT_ITEMS);
			if (items.contains(pos + "", Tag.TAG_COMPOUND))
				return ItemStack.of(items.getCompound(pos + ""));
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
				stack.setTag(new CompoundTag());
			
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
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
		int pos = Inventories.getPlayerHandSlotIndex(playerIn.getInventory(), hand);
		NostrumMagica.Proxy.openContainer(playerIn, ReagentBagGui.BagContainer.Make(pos));
		
		return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, playerIn.getItemInHand(hand));
    }
	
	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (context.getPlayer().isShiftKeyDown()) {
			if (context.getLevel().isClientSide()) {
				return InteractionResult.SUCCESS;
			}
			
			// If sneaking, try and do container fast add.
			final BlockPos pos = context.getClickedPos();
			final BlockEntity te = context.getLevel().getBlockEntity(pos);
			if (te != null) {
				
				// First, check if it's a ritual setup and see about auto-inserting reagents
				if (autoFillRitual(context.getLevel(), pos, te, context.getPlayer(), context.getItemInHand())) {
					return InteractionResult.SUCCESS;
				}
				
				// If not that, try to insert into container
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
	
	public static class ReagentInventory extends SimpleContainer {

		private static final int MAX_COUNT = 127;
		
		private @Nonnull ItemStack stack;
		//private ItemStack[] inventory;
		
		public ReagentInventory(ItemStack stack) {
			super(SLOTS);
			
			this.stack = stack;
			
			int i = 0;
			for (ItemStack reg : ReagentBag.getItems(stack)) {
				this.setItem(i++, reg);
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

	    	for (int i = 0; i < this.getContainerSize(); ++i) {
	            ItemStack itemstack1 = this.getItem(i);

	            if (itemstack1.isEmpty()) {
	                this.setItem(i, itemstack);
	                this.setChanged();
	                return ItemStack.EMPTY;
	            }

	            if (ItemStack.isSame(itemstack1, itemstack)) {
	                int j = this.getMaxStackSize();
	                int k = Math.min(itemstack.getCount(), j - itemstack1.getCount());

	                if (k > 0) {
	                    itemstack1.grow(k);
	                    itemstack.shrink(k);

	                    if (itemstack.getCount() <= 0) {
	                        this.setChanged();
	                        return ItemStack.EMPTY;
	                    }
	                }
	            }
	        }

	        if (itemstack.getCount() != stack.getCount()) {
	            this.setChanged();
	        }

	        return itemstack;
	    }

	    @Override
	    public void setChanged() {
	    	// Bleed our changes out to the itemstack
	    	if (!stack.isEmpty()) {
	    		for (int i = 0; i < this.getContainerSize(); i++) {
	    			ReagentBag.setItem(stack, this.getItem(i), i);
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
	public ELoreCategory getCategory() {
		return ELoreCategory.ITEM;
	}
	
	protected static final boolean autoFillRitual(Level world, BlockPos pos, BlockEntity te, Player player, ItemStack bag) {
		if (te instanceof PedestalBlockEntity && !((PedestalBlockEntity) te).getItem().isEmpty()) {
			// Capture current actual layout
			AltarRitualLayout layout = AltarRitualLayout.Capture(world, pos, EMagicElement.NEUTRAL);
			if (!layout.hasTierBlocks(1)) { // only for tier 2+ (encoded as 1+)
				return false;
			}
			
			final IRitualIngredients wrapper = new IRitualIngredients() {
				@Override
				public boolean hasTierBlocks(int tier) {
					return layout.hasTierBlocks(tier);
				}

				@Override
				public boolean hasCenterItem(Ingredient ingredient) {
					return layout.hasCenterItem(ingredient);
				}

				@Override
				public boolean hasReagents(Iterable<ReagentType> reagents) {
					return true; // Actual reason for wrapper: lie about reagents
				}

				@Override
				public boolean hasExtraItems(Iterable<Ingredient> ingredients) {
					return layout.hasExtraItems(ingredients);
				}

				@Override
				public boolean hasElement(EMagicElement element) {
					return true; // lie since we don't know
				}
			};
			
			// Find all matching rituals
			List<RitualRecipe> matches = RitualRegistry.instance().getRegisteredRituals().stream()
					.filter(r -> r.matches(player, world, pos, wrapper))
					.collect(Collectors.toList());
			
			// If only one, easy to figure out what to do.
			// If multiple, cycle through matching rituals
			final RitualRecipe match;
			final List<ItemStack> reagents;
			if (matches.isEmpty()) {
				return false;
			} else if (matches.size() > 1) {
				// if full reagents aren't already out, give a nice message explaning what's going on
				if (layout.getReagentItems(world, pos).size() < 4) {
					player.sendMessage(new TextComponent("Matched multiple rituals. Filling for first. Use again to cycle."), Util.NIL_UUID);
					match = matches.get(0);
				} else {
					// pull all reagents back to put fresh ones next. Figure out which recipe we're set up for.
					final IRitualIngredients elementWrapper = new IRitualIngredients() {
						@Override
						public boolean hasTierBlocks(int tier) {
							return layout.hasTierBlocks(tier);
						}

						@Override
						public boolean hasCenterItem(Ingredient ingredient) {
							return layout.hasCenterItem(ingredient);
						}

						@Override
						public boolean hasReagents(Iterable<ReagentType> reagents) {
							return layout.hasReagents(reagents); // actually check reagents but still lie about element
						}

						@Override
						public boolean hasExtraItems(Iterable<Ingredient> ingredients) {
							return layout.hasExtraItems(ingredients);
						}

						@Override
						public boolean hasElement(EMagicElement element) {
							return true; // lie since we don't know
						}
					};
					
					Iterator<RitualRecipe> it = matches.iterator();
					RitualRecipe next = null;
					while (it.hasNext()) {
						RitualRecipe ritual = it.next();
						if (ritual.matches(player, world, pos, elementWrapper)) {
							// found the true match
							if (it.hasNext()) {
								next = it.next();
							}
						}
					}
					if (next == null) {
						next = matches.get(0);
					}
					
					match = next;
				}
					
				// Eat up reagents
				AltarRitualLayout.VisitTier2Candles(world, pos, (w, candlePos) -> {
					BlockState state = world.getBlockState(candlePos);
					if (state == null || !(state.getBlock() instanceof CandleBlock)) {
						return;
					}
					// Candle TE can exist or not
					BlockEntity candleTE = world.getBlockEntity(candlePos);
					if (candleTE != null && candleTE instanceof CandleTileEntity && ((CandleTileEntity) candleTE).getReagentType() != null) {
						ReagentType type = ((CandleTileEntity) candleTE).getReagentType();
						addItem(bag, ReagentItem.CreateStack(type, 1));
						CandleBlock.extinguish(world, candlePos, state, false);
					}
				});
				
				// Just ate up reagents so don't use what's on the layout
				reagents = new ArrayList<>();
			} else {
				// only one
				match = matches.get(0);
				reagents = layout.getReagentItems(world, pos);
			}
			
			// Only one matched! What reagents does it need? Try to add them
			List<ReagentType> requiredTypes = Lists.newArrayList(match.getTypes());
			
			// subtract what's already there
			for (ItemStack reagent : reagents) {
				if (reagent.isEmpty()) {
					continue;
				}
				
				ReagentType type = ReagentItem.FindType(reagent);
				if (type != null) {
					requiredTypes.remove(type);
				}
			}
			
			// For remainder, try to add
			for (ReagentType missing : requiredTypes) {
				if (removeCount(bag, missing, 1) == 0) {
					// removed a reagent. Place it
					boolean[] found = {false};
					AltarRitualLayout.VisitTier2Candles(world, pos, (w, candlePos) -> {
						if (!found[0]) {
							BlockState state = world.getBlockState(candlePos);
							if (state == null || !(state.getBlock() instanceof CandleBlock)) {
								return;
							}
							// Candle TE can exist or not
							BlockEntity candleTE = world.getBlockEntity(candlePos);
							if (candleTE != null && candleTE instanceof CandleTileEntity && ((CandleTileEntity) candleTE).getReagentType() != null) {
								return;
							}
							
							CandleBlock.setReagent(world, candlePos, world.getBlockState(candlePos), missing);
							found[0] = true;
						}
					});
					
					if (!found[0]) {
						// Couldn't find candle, so... drop it?
						ItemEntity item = new ItemEntity(world, pos.getX() + .5, pos.getY() + 1.5, pos.getZ() + .5, ReagentItem.CreateStack(missing, 1));
						world.addFreshEntity(item);
					}
				}
			}
			
			return true;
		}
		
		return false;
	}
}
