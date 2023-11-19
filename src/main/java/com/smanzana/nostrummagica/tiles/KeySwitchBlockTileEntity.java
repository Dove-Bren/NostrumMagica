package com.smanzana.nostrummagica.tiles;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.world.NostrumKeyRegistry.NostrumWorldKey;

import net.minecraft.nbt.CompoundNBT;

public class KeySwitchBlockTileEntity extends SwitchBlockTileEntity implements IWorldKeyHolder {
	
	private NostrumWorldKey key;
	
	public KeySwitchBlockTileEntity() {
		super(NostrumTileEntities.KeySwitchTileEntityType);
		key = new NostrumWorldKey();
	}
	
	public KeySwitchBlockTileEntity(NostrumWorldKey key) {
		this();
		this.key = key;
	}
	
	private static final String NBT_KEY = "switch_key";
	
	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		nbt = super.write(nbt);
		
		nbt.put(NBT_KEY, this.key.asNBT());
		
		return nbt;
	}
	
	@Override
	public void read(CompoundNBT nbt) {
		super.read(nbt);
		
		this.key = NostrumWorldKey.fromNBT(nbt.getCompound(NBT_KEY));
	}
	
	@Override
	public void setWorldKey(NostrumWorldKey key) {
		this.key = key;
		dirty();
	}
	
	@Override
	public boolean hasWorldKey() {
		return true; // Always have one
	}
	
	@Override
	public NostrumWorldKey getWorldKey() {
		return this.key;
	}
	
	@Override
	protected void doTriggerInternal() {
		NostrumMagica.instance.getWorldKeys().addKey(getWorldKey());
	}
}