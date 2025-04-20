package com.smanzana.nostrummagica.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class TriggeredMatchSpawnerTileEntity extends MatchSpawnerTileEntity {
	
	private static final String NBT_TRIGGERED = "triggered";
	
	protected boolean triggered;
	
	public TriggeredMatchSpawnerTileEntity(BlockPos pos, BlockState state) {
		super(NostrumTileEntities.TriggeredMatchSpawnerTileEntityType, pos, state);
	}
	
	public boolean isTriggered() {
		return this.triggered;
	}
	
	protected void setTriggered(boolean triggered) {
		this.triggered = triggered;
		this.setChanged();
	}
	
	@Override
	protected boolean shouldSpawnMatch(BlockState state) {
		return false; // We spawn when it's time
	}
	
	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		
		nbt.putBoolean(NBT_TRIGGERED, isTriggered());
	}
	
	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		
		this.triggered = nbt.getBoolean(NBT_TRIGGERED);
	}
	
	public void triggerSpawn() {
		if (!this.isTriggered()) {
			this.spawnMatch(getBlockState());
			this.setTriggered(true);
		}
	}
}