package com.smanzana.nostrummagica.tile;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AltarTileEntity extends BlockEntity implements WorldlyContainer {
	
	private @Nonnull ItemStack stack = ItemStack.EMPTY;
	
	// Transient display variables
	private boolean hideItem;
	
	public AltarTileEntity(BlockPos pos, BlockState state) {
		super(NostrumBlockEntities.Altar, pos, state);
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
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		if (stack != ItemStack.EMPTY) {
			CompoundTag tag = new CompoundTag();
			tag = stack.save(tag);
			nbt.put(NBT_ITEM, tag);
		}
	}
	
	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		
		if (nbt == null)
			return;
			
		if (!nbt.contains(NBT_ITEM, Tag.TAG_COMPOUND)) {
			stack = ItemStack.EMPTY;
		} else {
			CompoundTag tag = nbt.getCompound(NBT_ITEM);
			stack = ItemStack.of(tag);
		}
	}
	
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.saveWithId(); // force ID so that it's non-empty and will always be read on client
	}
	
	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		super.onDataPacket(net, pkt);
		//handleUpdateTag(pkt.getTag());
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
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public void startOpen(Player player) {
		;
	}

	@Override
	public void stopOpen(Player player) {
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
	
}