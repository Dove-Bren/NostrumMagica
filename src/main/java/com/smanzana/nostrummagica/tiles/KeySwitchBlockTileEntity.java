package com.smanzana.nostrummagica.tiles;

import java.util.UUID;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.entity.EntityKeySwitchTrigger;
import com.smanzana.nostrummagica.entity.EntitySwitchTrigger;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.world.NostrumKeyRegistry.NostrumWorldKey;

import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.Vec3d;

public class KeySwitchBlockTileEntity extends SwitchBlockTileEntity implements IWorldKeyHolder, IUniqueDungeonTileEntity {
	
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
	protected EntitySwitchTrigger makeTriggerEntity(double x, double y, double z) {
		EntityKeySwitchTrigger ent = new EntityKeySwitchTrigger(NostrumEntityTypes.keySwitchTrigger, this.world);
		ent.setPosition(x, y, z);
		return ent;
	}
	
	@Override
	protected void doTriggerInternal() {
		NostrumMagica.instance.getWorldKeys().addKey(getWorldKey());
		world.setBlockState(pos, Blocks.AIR.getDefaultState());
		
		NostrumMagicaSounds.AMBIENT_WOOSH2.play(world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
		NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
				30, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, 1,
				50, 10,
				Vec3d.ZERO, new Vec3d(.1, .1, .1)
				).gravity(-.075f));
	}
	
	@Override
	public void onDungeonSpawn(UUID dungeonID, UUID roomID, boolean isWorldGen) {
		// TODO: should this use dungeon ID? Or even let it be configurable?
		// Sorcery dungeon is one big room, and I feel like MOST of my uses of this
		// will want unique-per-room keys?
		// Ehh well the whole points is that things don't have to be close to eachother, so maybe
		// that's wrong?
		final NostrumWorldKey newKey = this.key.mutateWithID(roomID);
		if (isWorldGen) {
			this.key = newKey;
		} else {
			this.setWorldKey(newKey);
		}
	}
}