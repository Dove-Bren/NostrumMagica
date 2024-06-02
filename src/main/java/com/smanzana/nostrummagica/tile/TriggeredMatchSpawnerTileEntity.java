package com.smanzana.nostrummagica.tile;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;

public class TriggeredMatchSpawnerTileEntity extends MatchSpawnerTileEntity {
	
	private static final String NBT_TRIGGERED = "triggered";
	
	protected boolean triggered;
	
	public TriggeredMatchSpawnerTileEntity() {
		super(NostrumTileEntities.TriggeredMatchSpawnerTileEntityType);
	}
	
	public boolean isTriggered() {
		return this.triggered;
	}
	
	protected void setTriggered(boolean triggered) {
		this.triggered = triggered;
		this.markDirty();
	}
	
	@Override
	protected boolean shouldSpawnMatch(BlockState state) {
		return false; // We spawn when it's time
	}
	
	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		nbt = super.write(nbt);
		
		nbt.putBoolean(NBT_TRIGGERED, isTriggered());
		
		return nbt;
	}
	
	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		
		this.triggered = nbt.getBoolean(NBT_TRIGGERED);
	}
	
	public void triggerSpawn() {
		if (!this.isTriggered()) {
			this.spawnMatch(getBlockState());
			this.setTriggered(true);
		}
	}
}