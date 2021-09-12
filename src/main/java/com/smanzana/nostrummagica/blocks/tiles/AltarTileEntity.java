package com.smanzana.nostrummagica.blocks.tiles;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.blocks.IAetherInfusableTileEntity;
import com.smanzana.nostrummagica.integration.aetheria.blocks.AetherInfuser.AetherInfuserTileEntity;
import com.smanzana.nostrummagica.items.IAetherInfuserLens;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.Constants.NBT;

public class AltarTileEntity extends TileEntity implements ISidedInventory, IAetherInfusableTileEntity {
	
	private @Nonnull ItemStack stack = ItemStack.EMPTY;
	
	public AltarTileEntity() {
		
	}
	
	public @Nonnull ItemStack getItem() {
		return stack;
	}
	
	public void setItem(@Nonnull ItemStack stack) {
		this.stack = stack;
		dirty();
	}
	
	private static final String NBT_ITEM = "item";
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt = super.writeToNBT(nbt);
		
		if (stack != ItemStack.EMPTY) {
			NBTTagCompound tag = new NBTTagCompound();
			tag = stack.writeToNBT(tag);
			nbt.setTag(NBT_ITEM, tag);
		}
		
		return nbt;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		
		if (nbt == null)
			return;
			
		if (!nbt.hasKey(NBT_ITEM, NBT.TAG_COMPOUND)) {
			stack = ItemStack.EMPTY;
		} else {
			NBTTagCompound tag = nbt.getCompoundTag(NBT_ITEM);
			stack = new ItemStack(tag);
		}
	}
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, 3, this.getUpdateTag());
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		super.onDataPacket(net, pkt);
		handleUpdateTag(pkt.getNbtCompound());
	}
	
	private void dirty() {
		world.markBlockRangeForRenderUpdate(pos, pos);
		world.notifyBlockUpdate(pos, this.world.getBlockState(pos), this.world.getBlockState(pos), 3);
		world.scheduleBlockUpdate(pos, this.getBlockType(),0,0);
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
		ItemStack ret = this.stack.splitStack(count);
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
	public boolean isUsableByPlayer(EntityPlayer player) {
		return true;
	}

	@Override
	public void openInventory(EntityPlayer player) {
		;
	}

	@Override
	public void closeInventory(EntityPlayer player) {
		;
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return index == 0;
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {
		
	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {
		this.stack = ItemStack.EMPTY;
		dirty();
	}

	@Override
	public String getName() {
		return "Altar";
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		return new int[] {0};
	}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
		if (index != 0)
			return false;
		
		return stack.isEmpty();
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
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