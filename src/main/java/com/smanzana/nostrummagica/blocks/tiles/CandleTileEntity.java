package com.smanzana.nostrummagica.blocks.tiles;

import java.util.Random;

import com.smanzana.nostrummagica.blocks.Candle;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.util.Constants.NBT;

public class CandleTileEntity extends TileEntity implements ITickable {
	
	private static final String NBT_TYPE = "type";
	private static Random rand = new Random();
	private ReagentType type;
	private int lifeTicks;
	
	public CandleTileEntity(ReagentType type) {
		this();
		this.type = type;
	}
	
	public CandleTileEntity() {
		this.lifeTicks = (20 * 15) + CandleTileEntity.rand.nextInt(20*30);
	}
	
	public ReagentType getType() {
		return type;
	}
	
	private ReagentType parseType(String serial) {
		for (ReagentType type : ReagentType.values()) {
			if (type.name().equalsIgnoreCase(serial))
				return type;
		}
		
		return null;
	}
	
	private String serializeType(ReagentType type) {
		if (type == null)
			return "null";
		return type.name().toLowerCase();
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt = super.writeToNBT(nbt);
		
		nbt.setString(NBT_TYPE, serializeType(type));
		
		return nbt;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		
		if (nbt == null || !nbt.hasKey(NBT_TYPE, NBT.TAG_STRING))
			return;
		
		this.type = parseType(nbt.getString(NBT_TYPE));
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
	public void update() {
		this.lifeTicks = Math.max(-1, this.lifeTicks-1);
		
		if (this.lifeTicks == 0 && !world.isRemote) {
			IBlockState state = world.getBlockState(this.pos);
			if (state == null)
				return;
			
			Candle.extinguish(world, this.pos, state, false);
		}
	}
	
	public void setReagentType(ReagentType type) {
		this.type = type;
		this.dirty();
	}
}