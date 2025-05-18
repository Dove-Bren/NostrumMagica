package com.smanzana.nostrummagica.tile;

import java.util.Random;

import com.smanzana.nostrummagica.block.CandleBlock;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CandleTileEntity extends BlockEntity implements TickableBlockEntity, IReagentProviderTile {
	
	private static final String NBT_TYPE = "type";
	private static Random rand = new Random();
	private ReagentType type;
	private int lifeTicks;
	
	public CandleTileEntity(ReagentType type, BlockPos pos, BlockState state) {
		this(pos, state);
		this.type = type;
	}
	
	public CandleTileEntity(BlockPos pos, BlockState state) {
		super(NostrumBlockEntities.Candle, pos, state);
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
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		nbt.putString(NBT_TYPE, serializeType(type));
	}
	
	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		
		if (nbt == null || !nbt.contains(NBT_TYPE, Tag.TAG_STRING))
			return;
		
		this.type = parseType(nbt.getString(NBT_TYPE));
	}
	
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.saveWithoutMetadata();
	}
	
	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		super.onDataPacket(net, pkt);
		//handleUpdateTag(pkt.getTag());
	}
	
	private void dirty() {
		level.sendBlockUpdated(worldPosition, this.level.getBlockState(worldPosition), this.level.getBlockState(worldPosition), 3);
		setChanged();
	}
	
	@Override
	public void tick() {
		// If no enhancing block is present, tick down life ticks to eventually consume reagent
		if (!isEnhanced()) {
			this.lifeTicks = Math.max(-1, this.lifeTicks-1);
		}
		
		if (this.lifeTicks == 0 && !level.isClientSide) {
			BlockState state = level.getBlockState(this.worldPosition);
			if (state == null)
				return;
			
			CandleBlock.extinguish(level, this.worldPosition, state, false);
		}
	}
	
	public void setReagentType(ReagentType type) {
		this.type = type;
		this.dirty();
	}
	
	protected boolean isEnhanced() {
		return CandleBlock.IsCandleEnhanced(getLevel(), getBlockPos());
	}

	@Override
	public ReagentType getPresentReagentType(BlockEntity provider, Level world, BlockPos pos) {
		return this.getReagentType();
	}

	@Override
	public boolean consumeReagentType(BlockEntity provider, Level world, BlockPos pos, ReagentType type) {
		if (this.getReagentType() == null || (type != null && type != this.getReagentType())) {
			return false;
		}
		
		CandleBlock.extinguish(world, pos, getBlockState());
		return true;
	}
}