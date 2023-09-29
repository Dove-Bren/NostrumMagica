package com.smanzana.nostrummagica.tiles;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.world.blueprints.IOrientedTileEntity;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprint;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;

public class TeleportRuneTileEntity extends TileEntity implements IOrientedTileEntity {
	
	private static final String NBT_OFFSET = "offset";
	
	private BlockPos teleOffset = null;
	
	public TeleportRuneTileEntity() {
		super(NostrumTileEntities.TeleportRuneTileEntityType);
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
	
	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		
		if (teleOffset != null) {
			compound.putLong(NBT_OFFSET, teleOffset.toLong());
		}
		
		return compound;
	}
	
	public void read(CompoundNBT compound) {
		super.read(compound);
		
		teleOffset = null;
		if (compound.contains(NBT_OFFSET, NBT.TAG_LONG)) {
			teleOffset = BlockPos.fromLong(compound.getLong(NBT_OFFSET));
		}
	}
	
	protected void flush() {
		if (world != null && !world.isRemote) {
			BlockState state = world.getBlockState(pos);
			world.notifyBlockUpdate(pos, state, state, 2);
		}
	}
	
	@Override
	public void setSpawnedFromRotation(Direction rotation) {
		BlockPos orig = this.getOffset();
		if (orig != null) {
			BlockPos out = RoomBlueprint.applyRotation(this.getOffset(), rotation);
			this.setOffset(out.getX(), out.getY(), out.getZ());
		} else {
			System.out.println("Null offset?");
		}
	}
}