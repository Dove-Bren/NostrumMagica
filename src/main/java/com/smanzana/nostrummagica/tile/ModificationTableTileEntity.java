package com.smanzana.nostrummagica.tile;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.ModificationTableBlock;
import com.smanzana.nostrummagica.item.SpellScroll;
import com.smanzana.nostrummagica.item.SpellTome;
import com.smanzana.nostrummagica.item.SpellTomePage;
import com.smanzana.nostrummagica.item.equipment.CasterWandItem;
import com.smanzana.nostrummagica.item.equipment.WarlockSword;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spelltome.enhancement.SpellTomeEnhancementWrapper;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ModificationTableTileEntity extends BlockEntity implements Container {

	private static final String NBT_INV = "modtable";
	
	/**
	 * Inventory:
	 *   0 - Center Icon
	 *   1 - Input Slot
	 */
	
	private @Nonnull ItemStack slots[];
	
	public ModificationTableTileEntity(BlockPos pos, BlockState state) {
		super(NostrumTileEntities.ModificationTableEntityType, pos, state);
		slots = new ItemStack[getContainerSize()];
		for (int i = 0; i < slots.length; i++) {
			slots[i] = ItemStack.EMPTY;
		}
	}
	
	public  @Nonnull ItemStack getMainSlot() {
		return this.getItem(0);
	}
	
	public @Nonnull ItemStack getInputSlot() {
		return this.getItem(1);
	}
	
	@Override
	public int getContainerSize() {
		return 2;
	}

	@Override
	public  @Nonnull ItemStack getItem(int index) {
		if (index < 0 || index >= getContainerSize())
			return ItemStack.EMPTY;
		
		return slots[index];
	}

	@Override
	public @Nonnull ItemStack removeItem(int index, int count) {
		if (index < 0 || index >= getContainerSize() || slots[index].isEmpty())
			return ItemStack.EMPTY;
		
		@Nonnull ItemStack stack;
		if (slots[index].getCount() <= count) {
			stack = slots[index];
			slots[index] = ItemStack.EMPTY;
		} else {
			stack = slots[index].split(count);
		}
		
		this.setChanged();
		
		return stack;
	}

	@Override
	public @Nonnull ItemStack removeItemNoUpdate(int index) {
		if (index < 0 || index >= getContainerSize())
			return ItemStack.EMPTY;
		
		ItemStack stack = slots[index];
		slots[index] = ItemStack.EMPTY;
		
		this.setChanged();
		return stack;
	}

	@Override
	public void setItem(int index, @Nonnull ItemStack stack) {
		if (!canPlaceItem(index, stack))
			return;
		
		slots[index] = stack;
		this.setChanged();
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public void startOpen(Player player) {
	}

	@Override
	public void stopOpen(Player player) {
	}

	@Override
	public boolean canPlaceItem(int index, @Nonnull ItemStack stack) {
		if (index < 0 || index >= getContainerSize())
			return false;
		
		if (stack.isEmpty())
			return true;
		
		if (index == 0) {
			return ModificationTableBlock.IsModifiable(stack);
		}
		
		if (index == 1)
			return true;
		
		return false;
	}

	@Override
	public void clearContent() {
		for (int i = 0; i < getContainerSize(); i++)
			removeItemNoUpdate(i);
	}
	
	
	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		CompoundTag compound = new CompoundTag();
		
		for (int i = 0; i < getContainerSize(); i++) {
			if (getItem(i).isEmpty())
				continue;
			
			CompoundTag tag = new CompoundTag();
			compound.put(i + "", getItem(i).save(tag));
		}
		
		if (nbt == null)
			nbt = new CompoundTag();
		
		nbt.put(NBT_INV, compound);
	}
	
	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		
		if (nbt == null || !nbt.contains(NBT_INV, Tag.TAG_COMPOUND))
			return;
		this.clearContent();
		CompoundTag items = nbt.getCompound(NBT_INV);
		for (String key : items.getAllKeys()) {
			int id;
			try {
				id = Integer.parseInt(key);
			} catch (NumberFormatException e) {
				NostrumMagica.logger.error("Failed reading SpellTable inventory slot: " + key);
				continue;
			}
			
			ItemStack stack = ItemStack.of(items.getCompound(key));
			this.setItem(id, stack);
		}
	}
	
	// Submit current staged modifications
	public void modify(boolean valB, float valF) {
		ItemStack stack = this.getMainSlot();
		if (stack.getItem() instanceof SpellTome) {
			if (this.getInputSlot() != null && this.getInputSlot().getItem() instanceof SpellTomePage) {
				SpellTome.addEnhancement(stack, new SpellTomeEnhancementWrapper( 
						SpellTomePage.getEnhancement(this.getInputSlot()),
						SpellTomePage.getLevel(this.getInputSlot())));
				int mods = Math.max(0, SpellTome.getModifications(stack) - 1);
				SpellTome.setModifications(stack, mods);
				this.setItem(1, ItemStack.EMPTY);
			}
		} else if (stack.getItem() instanceof SpellScroll) {
			Spell spell = SpellScroll.GetSpell(stack);
			if (spell != null) {
				spell.setIcon((int) valF);
				this.setItem(1, ItemStack.EMPTY);
			}
		} else if (stack.getItem() instanceof WarlockSword) {
			this.setItem(1, ItemStack.EMPTY);
			WarlockSword.addCapacity(stack, 2);
		} else if (stack.getItem() instanceof CasterWandItem) {
			final Spell currentSpell = CasterWandItem.GetSpell(stack);
			
			// Make sure if we're going to try and put a spell in that it's not empty
			if (!this.getInputSlot().isEmpty()) {
				if (SpellScroll.GetSpell(getInputSlot()) == null) {
					return;
				}
			}
			
			final Spell scrollSpell;
			if (this.getInputSlot().isEmpty()) {
				scrollSpell = null;
			} else {
				scrollSpell = SpellScroll.GetSpell(this.getInputSlot());
			}
			if (scrollSpell == null || CasterWandItem.CanStoreSpell(stack, scrollSpell)) {
				CasterWandItem.SetSpell(stack, scrollSpell);
				this.setItem(1, stack); // Put wand in input slot so there's a visible change
				
				// Create a scroll for the old spell, if there was one
				if (currentSpell != null) {
					ItemStack scroll = SpellScroll.create(currentSpell);
					scroll.setDamageValue(SpellScroll.getMaxDurability(scroll) - 1);
					this.setItem(0, scroll);
				} else {
					this.setItem(0, ItemStack.EMPTY);
				}
			}
		}
		
	}

	@Override
	public boolean isEmpty() {
		for (@Nonnull ItemStack stack : slots) {
			if (!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}
}