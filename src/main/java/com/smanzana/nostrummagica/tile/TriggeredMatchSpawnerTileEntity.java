package com.smanzana.nostrummagica.tile;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;

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
		this.setChanged();
	}
	
	@Override
	protected boolean shouldSpawnMatch(BlockState state) {
		return false; // We spawn when it's time
	}
	
	@Override
	public CompoundTag save(CompoundTag nbt) {
		nbt = super.save(nbt);
		
		nbt.putBoolean(NBT_TRIGGERED, isTriggered());
		
		return nbt;
	}
	
	@Override
	public void load(BlockState state, CompoundTag nbt) {
		super.load(state, nbt);
		
		this.triggered = nbt.getBoolean(NBT_TRIGGERED);
	}
	
	public void triggerSpawn() {
		if (!this.isTriggered()) {
			this.spawnMatch(getBlockState());
			this.setTriggered(true);
		}
	}
}