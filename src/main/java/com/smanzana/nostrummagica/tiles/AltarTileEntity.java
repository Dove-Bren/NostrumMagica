package com.smanzana.nostrummagica.tiles;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.blocks.IAetherInfusableTileEntity;
import com.smanzana.nostrummagica.integration.aetheria.blocks.AetherInfuserTileEntity;
import com.smanzana.nostrummagica.items.IAetherInfuserLens;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.Constants.NBT;

public class AltarTileEntity extends TileEntity implements ISidedInventory, IAetherInfusableTileEntity {
	
	private @Nonnull ItemStack stack = ItemStack.EMPTY;
	
	// Transient display variables
	private boolean hideItem;
	
	public AltarTileEntity() {
		super(NostrumTileEntities.AltarTileEntityType);
	}
	
	public @Nonnull ItemStack getItem() {
		return stack;
	}
	
	public void setItem(@Nonnull ItemStack stack) {
		this.stack = stack;
		dirty();
	}
	
	public boolean isHidingItem() {
		return hideItem;
	}
	
	public void hideItem(boolean hide) {
		this.hideItem = hide;
	}
	
	private static final String NBT_ITEM = "item";
	
	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		nbt = super.write(nbt);
		
		if (stack != ItemStack.EMPTY) {
			CompoundNBT tag = new CompoundNBT();
			tag = stack.write(tag);
			nbt.put(NBT_ITEM, tag);
		}
		
		return nbt;
	}
	
	@Override
	public void read(CompoundNBT nbt) {
		super.read(nbt);
		
		if (nbt == null)
			return;
			
		if (!nbt.contains(NBT_ITEM, NBT.TAG_COMPOUND)) {
			stack = ItemStack.EMPTY;
		} else {
			CompoundNBT tag = nbt.getCompound(NBT_ITEM);
			stack = ItemStack.read(tag);
		}
	}
	
	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(this.pos, 3, this.getUpdateTag());
	}

	@Override
	public CompoundNBT getUpdateTag() {
		return this.write(new CompoundNBT());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		super.onDataPacket(net, pkt);
		handleUpdateTag(pkt.getNbtCompound());
	}
	
	private void dirty() {
		world.notifyBlockUpdate(this.getPos(), this.getBlockState(), this.getBlockState(), 3);
		markDirty();
	}

	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		if (index > 0)
			return ItemStack.EMPTY;
		return this.stack;
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		if (index > 0)
			return ItemStack.EMPTY;
		ItemStack ret = this.stack.split(count);
		if (this.stack.getCount() == 0)
			this.stack = ItemStack.EMPTY;
		this.dirty();
		return ret;
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		if (index > 0)
			return ItemStack.EMPTY;
		ItemStack ret = this.stack;
		this.stack = ItemStack.EMPTY;
		dirty();
		return ret;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		if (index > 0)
			return;
		this.stack = stack;
		dirty();
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
		;
	}

	@Override
	public void closeInventory(PlayerEntity player) {
		;
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return index == 0;
	}

	@Override
	public void clear() {
		this.stack = ItemStack.EMPTY;
		dirty();
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		return new int[] {0};
	}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, Direction direction) {
		if (index != 0)
			return false;
		
		return stack.isEmpty();
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
		return index == 0 && !stack.isEmpty();
	}

	@Override
	public boolean isEmpty() {
		return stack.isEmpty();
	}

	@Override
	public boolean canAcceptAetherInfuse(AetherInfuserTileEntity source, int maxAether) {
		return !stack.isEmpty() && stack.getItem() instanceof IAetherInfuserLens;
	}

	@Override
	public int acceptAetherInfuse(AetherInfuserTileEntity source, int maxAether) {
		final IAetherInfuserLens infusable = ((IAetherInfuserLens) stack.getItem());
		final int leftover;
		if (infusable.canAcceptAetherInfuse(stack, pos, source, maxAether)) {
			leftover = infusable.acceptAetherInfuse(stack, pos, source, maxAether);
		} else {
			leftover = maxAether;
		}
		return leftover;
	}
	
}