package com.smanzana.nostrummagica.tile;

import javax.annotation.Nonnull;

import com.smanzana.nostrumaetheria.api.blocks.IAetherInfusableTileEntity;
import com.smanzana.nostrumaetheria.api.blocks.IAetherInfuserTileEntity;
import com.smanzana.nostrumaetheria.api.item.IAetherInfuserLens;

import net.minecraft.block.BlockState;
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
	
	public void setItemNoDirty(@Nonnull ItemStack stack) {
		// Set the item without marking the TE as dirty. Useful during world gen, where
		// the chunk is going to be saved after generation anyways.
		this.stack = stack;
	}
	
	public boolean isHidingItem() {
		return hideItem;
	}
	
	public void hideItem(boolean hide) {
		this.hideItem = hide;
	}
	
	private static final String NBT_ITEM = "item";
	
	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		nbt = super.save(nbt);
		
		if (stack != ItemStack.EMPTY) {
			CompoundNBT tag = new CompoundNBT();
			tag = stack.save(tag);
			nbt.put(NBT_ITEM, tag);
		}
		
		return nbt;
	}
	
	@Override
	public void load(BlockState state, CompoundNBT nbt) {
		super.load(state, nbt);
		
		if (nbt == null)
			return;
			
		if (!nbt.contains(NBT_ITEM, NBT.TAG_COMPOUND)) {
			stack = ItemStack.EMPTY;
		} else {
			CompoundNBT tag = nbt.getCompound(NBT_ITEM);
			stack = ItemStack.of(tag);
		}
	}
	
	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(this.worldPosition, 3, this.getUpdateTag());
	}

	@Override
	public CompoundNBT getUpdateTag() {
		return this.save(new CompoundNBT());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		super.onDataPacket(net, pkt);
		handleUpdateTag(this.getBlockState(), pkt.getTag());
	}
	
	private void dirty() {
		level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
		setChanged();
	}

	@Override
	public int getContainerSize() {
		return 1;
	}

	@Override
	public ItemStack getItem(int index) {
		if (index > 0)
			return ItemStack.EMPTY;
		return this.stack;
	}

	@Override
	public ItemStack removeItem(int index, int count) {
		if (index > 0)
			return ItemStack.EMPTY;
		ItemStack ret = this.stack.split(count);
		if (this.stack.getCount() == 0)
			this.stack = ItemStack.EMPTY;
		this.dirty();
		return ret;
	}

	@Override
	public ItemStack removeItemNoUpdate(int index) {
		if (index > 0)
			return ItemStack.EMPTY;
		ItemStack ret = this.stack;
		this.stack = ItemStack.EMPTY;
		dirty();
		return ret;
	}

	@Override
	public void setItem(int index, ItemStack stack) {
		if (index > 0)
			return;
		this.stack = stack;
		dirty();
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	@Override
	public boolean stillValid(PlayerEntity player) {
		return true;
	}

	@Override
	public void startOpen(PlayerEntity player) {
		;
	}

	@Override
	public void stopOpen(PlayerEntity player) {
		;
	}

	@Override
	public boolean canPlaceItem(int index, ItemStack stack) {
		return index == 0;
	}

	@Override
	public void clearContent() {
		this.stack = ItemStack.EMPTY;
		dirty();
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		return new int[] {0};
	}

	@Override
	public boolean canPlaceItemThroughFace(int index, ItemStack itemStackIn, Direction direction) {
		if (index != 0)
			return false;
		
		return stack.isEmpty();
	}

	@Override
	public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
		return index == 0 && !stack.isEmpty();
	}

	@Override
	public boolean isEmpty() {
		return stack.isEmpty();
	}

	@Override
	public boolean canAcceptAetherInfuse(IAetherInfuserTileEntity source, int maxAether) {
		return !stack.isEmpty() && stack.getItem() instanceof IAetherInfuserLens;
	}

	@Override
	public int acceptAetherInfuse(IAetherInfuserTileEntity source, int maxAether) {
		final IAetherInfuserLens infusable = ((IAetherInfuserLens) stack.getItem());
		final int leftover;
		if (infusable.canAcceptAetherInfuse(stack, worldPosition, source, maxAether)) {
			leftover = infusable.acceptAetherInfuse(stack, worldPosition, source, maxAether);
		} else {
			leftover = maxAether;
		}
		return leftover;
	}
	
}