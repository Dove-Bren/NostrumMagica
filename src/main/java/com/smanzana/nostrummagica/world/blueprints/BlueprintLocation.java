package com.smanzana.nostrummagica.world.blueprints;

import com.smanzana.nostrummagica.util.WorldUtil;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;

public class BlueprintLocation {
	private final Direction facing;
	private final BlockPos pos;
	
	public BlueprintLocation(BlockPos pos, Direction facing) {
		this.pos = pos;
		this.facing = facing;
	}

	public Direction getFacing() {
		return facing;
	}

	public BlockPos getPos() {
		return pos;
	}
	
	@Override
	public String toString() {
		return "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")[" + facing.name() + "]";
	}
	
	private static final String NBT_POS = "pos";
	private static final String NBT_DIR = "facing";
	
	
	public CompoundNBT toNBT() {
		CompoundNBT tag = new CompoundNBT();
		tag.put(NBT_POS, NBTUtil.writeBlockPos(pos));
		tag.putByte(NBT_DIR, (byte) facing.getHorizontalIndex());
		return tag;
	}
	
	public static BlueprintLocation fromNBT(CompoundNBT nbt) {
		final BlockPos pos;
		
		if (nbt.contains(NBT_POS, NBT.TAG_LONG)) {
			// Legacy
			// 1.13/1.14 changed BlockPos.fromLong, so have to use old version
			pos = WorldUtil.blockPosFromLong1_12_2(nbt.getLong(NBT_POS));
		} else {
			pos = NBTUtil.readBlockPos(nbt.getCompound(NBT_POS));
		}
		
		Direction facing = Direction.byHorizontalIndex(nbt.getByte(NBT_DIR));
		return new BlueprintLocation(pos, facing);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof BlueprintLocation) {
			BlueprintLocation other = (BlueprintLocation) o;
			return other.facing == this.facing && other.pos.equals(this.pos);
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.facing.hashCode() * 91 + this.pos.hashCode();
	}
}