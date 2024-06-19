package com.smanzana.nostrummagica.tile;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.ModificationTableBlock;
import com.smanzana.nostrummagica.item.SpellRune;
import com.smanzana.nostrummagica.item.SpellScroll;
import com.smanzana.nostrummagica.item.SpellTome;
import com.smanzana.nostrummagica.item.SpellTomePage;
import com.smanzana.nostrummagica.item.equipment.CasterWandItem;
import com.smanzana.nostrummagica.item.equipment.WarlockSword;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.SpellShapePartProperties;
import com.smanzana.nostrummagica.spelltome.enhancement.SpellTomeEnhancementWrapper;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants.NBT;

public class ModificationTableTileEntity extends TileEntity implements IInventory {

	private static final String NBT_INV = "modtable";
	
	/**
	 * Inventory:
	 *   0 - Center Icon
	 *   1 - Input Slot
	 */
	
	private @Nonnull ItemStack slots[];
	
	public ModificationTableTileEntity() {
		super(NostrumTileEntities.ModificationTableEntityType);
		slots = new ItemStack[getSizeInventory()];
		for (int i = 0; i < slots.length; i++) {
			slots[i] = ItemStack.EMPTY;
		}
	}
	
	public  @Nonnull ItemStack getMainSlot() {
		return this.getStackInSlot(0);
	}
	
	public @Nonnull ItemStack getInputSlot() {
		return this.getStackInSlot(1);
	}
	
	@Override
	public int getSizeInventory() {
		return 2;
	}

	@Override
	public  @Nonnull ItemStack getStackInSlot(int index) {
		if (index < 0 || index >= getSizeInventory())
			return ItemStack.EMPTY;
		
		return slots[index];
	}

	@Override
	public @Nonnull ItemStack decrStackSize(int index, int count) {
		if (index < 0 || index >= getSizeInventory() || slots[index].isEmpty())
			return ItemStack.EMPTY;
		
		@Nonnull ItemStack stack;
		if (slots[index].getCount() <= count) {
			stack = slots[index];
			slots[index] = ItemStack.EMPTY;
		} else {
			stack = slots[index].split(count);
		}
		
		this.markDirty();
		
		return stack;
	}

	@Override
	public @Nonnull ItemStack removeStackFromSlot(int index) {
		if (index < 0 || index >= getSizeInventory())
			return ItemStack.EMPTY;
		
		ItemStack stack = slots[index];
		slots[index] = ItemStack.EMPTY;
		
		this.markDirty();
		return stack;
	}

	@Override
	public void setInventorySlotContents(int index, @Nonnull ItemStack stack) {
		if (!isItemValidForSlot(index, stack))
			return;
		
		slots[index] = stack;
		this.markDirty();
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public boolean isUsableByPlayer(PlayerEntity player) {
		return true;
	}

	@Override
	public void openInventory(PlayerEntity player) {
	}

	@Override
	public void closeInventory(PlayerEntity player) {
	}

	@Override
	public boolean isItemValidForSlot(int index, @Nonnull ItemStack stack) {
		if (index < 0 || index >= getSizeInventory())
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
	public void clear() {
		for (int i = 0; i < getSizeInventory(); i++)
			removeStackFromSlot(i);
	}
	
	
	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		nbt = super.write(nbt);
		CompoundNBT compound = new CompoundNBT();
		
		for (int i = 0; i < getSizeInventory(); i++) {
			if (getStackInSlot(i).isEmpty())
				continue;
			
			CompoundNBT tag = new CompoundNBT();
			compound.put(i + "", getStackInSlot(i).write(tag));
		}
		
		if (nbt == null)
			nbt = new CompoundNBT();
		
		nbt.put(NBT_INV, compound);
		return nbt;
	}
	
	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		
		if (nbt == null || !nbt.contains(NBT_INV, NBT.TAG_COMPOUND))
			return;
		this.clear();
		CompoundNBT items = nbt.getCompound(NBT_INV);
		for (String key : items.keySet()) {
			int id;
			try {
				id = Integer.parseInt(key);
			} catch (NumberFormatException e) {
				NostrumMagica.logger.error("Failed reading SpellTable inventory slot: " + key);
				continue;
			}
			
			ItemStack stack = ItemStack.read(items.getCompound(key));
			this.setInventorySlotContents(id, stack);
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
				this.setInventorySlotContents(1, ItemStack.EMPTY);
			}
		} else if (stack.getItem() instanceof SpellRune) {
			this.setInventorySlotContents(1, ItemStack.EMPTY);
			SpellRune.setPieceParam(stack, new SpellShapePartProperties(valF, valB));
		} else if (stack.getItem() instanceof SpellScroll) {
			Spell spell = SpellScroll.GetSpell(stack);
			if (spell != null) {
				spell.setIcon((int) valF);
				this.setInventorySlotContents(1, ItemStack.EMPTY);
			}
		} else if (stack.getItem() instanceof WarlockSword) {
			this.setInventorySlotContents(1, ItemStack.EMPTY);
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
				this.setInventorySlotContents(1, stack); // Put wand in input slot so there's a visible change
				
				// Create a scroll for the old spell, if there was one
				if (currentSpell != null) {
					ItemStack scroll = SpellScroll.create(currentSpell);
					scroll.setDamage(SpellScroll.getMaxDurability(scroll) - 1);
					this.setInventorySlotContents(0, scroll);
				} else {
					this.setInventorySlotContents(0, ItemStack.EMPTY);
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