package com.smanzana.nostrummagica.blocks.tiles;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.world.blueprints.IOrientedTileEntity;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprint;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;

public class TeleportRuneTileEntity extends TileEntity implements IOrientedTileEntity {
	
	private static final String NBT_OFFSET = "offset";
	
	private BlockPos teleOffset = null;
	
	public TeleportRuneTileEntity() {
		super();
	}
	
	/**
	 * Sets where to teleport to as an offset from the block itself.
	 * OFFSET, not target location. Went ahead and used ints here to make it more obvious.
	 * @param offsetX
	 * @param offsetY
	 * @param offsetZ
	 */
	public void setOffset(int offsetX, int offsetY, int offsetZ) {
		this.teleOffset = new BlockPos(offsetX, offsetY, offsetZ);
		flush();
	}
	
	public void setTargetPosition(BlockPos target) {
		if (target == null) {
			this.teleOffset = null;
		} else {
			this.teleOffset = target.subtract(pos);
		}
		flush();
	}
	
	public @Nullable BlockPos getOffset() {
		return teleOffset;
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
	
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		
		if (teleOffset != null) {
			compound.setLong(NBT_OFFSET, teleOffset.toLong());
		}
		
		return compound;
	}
	
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		
		teleOffset = null;
		if (compound.hasKey(NBT_OFFSET, NBT.TAG_LONG)) {
			teleOffset = BlockPos.fromLong(compound.getLong(NBT_OFFSET));
		}
	}
	
	protected void flush() {
		if (world != null && !world.isRemote) {
			IBlockState state = world.getBlockState(pos);
			world.notifyBlockUpdate(pos, state, state, 2);
		}
	}
	
	@Override
	public void setSpawnedFromRotation(EnumFacing rotation) {
		BlockPos out = RoomBlueprint.applyRotation(this.getOffset(), rotation);
		this.setOffset(out.getX(), out.getY(), out.getZ());
	}
}