package com.smanzana.nostrummagica.tiles;

import java.util.Random;

import com.smanzana.nostrummagica.blocks.Candle;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants.NBT;

public class CandleTileEntity extends TileEntity implements ITickableTileEntity {
	
	private static final String NBT_TYPE = "type";
	private static Random rand = new Random();
	private ReagentType type;
	private int lifeTicks;
	
	public CandleTileEntity(ReagentType type) {
		this();
		this.type = type;
	}
	
	public CandleTileEntity() {
		super(NostrumTileEntities.CandleTileEntityType);
		this.lifeTicks = (20 * 15) + CandleTileEntity.rand.nextInt(20*30);
	}
	
	public ReagentType getReagentType() {
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
	public CompoundNBT write(CompoundNBT nbt) {
		nbt = super.write(nbt);
		
		nbt.putString(NBT_TYPE, serializeType(type));
		
		return nbt;
	}
	
	@Override
	public void read(CompoundNBT nbt) {
		super.read(nbt);
		
		if (nbt == null || !nbt.contains(NBT_TYPE, NBT.TAG_STRING))
			return;
		
		this.type = parseType(nbt.getString(NBT_TYPE));
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
		world.notifyBlockUpdate(pos, this.world.getBlockState(pos), this.world.getBlockState(pos), 3);
		markDirty();
	}
	
	@Override
	public void tick() {
		// If no enhancing block is present, tick down life ticks to eventually consume reagent
		if (!isEnhanced()) {
			this.lifeTicks = Math.max(-1, this.lifeTicks-1);
		}
		
		if (this.lifeTicks == 0 && !world.isRemote) {
			BlockState state = world.getBlockState(this.pos);
			if (state == null)
				return;
			
			Candle.extinguish(world, this.pos, state, false);
		}
	}
	
	public void setReagentType(ReagentType type) {
		this.type = type;
		this.dirty();
	}
	
	protected boolean isEnhanced() {
		return Candle.IsCandleEnhanced(getWorld(), getPos());
	}
}