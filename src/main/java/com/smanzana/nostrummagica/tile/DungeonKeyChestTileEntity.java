package com.smanzana.nostrummagica.tile;

import java.util.Random;

import com.smanzana.autodungeons.AutoDungeons;
import com.smanzana.autodungeons.tile.IWorldKeyHolder;
import com.smanzana.autodungeons.world.WorldKey;
import com.smanzana.nostrummagica.block.dungeon.DungeonKeyChestBlock;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;

public class DungeonKeyChestTileEntity extends TileEntity implements IWorldKeyHolder {
	
	private WorldKey key;
	private boolean triggered;
	
	// Ticks when opened, for client animation
	private long openTicks = -1;
	
	public DungeonKeyChestTileEntity() {
		super(NostrumTileEntities.DungeonKeyChestTileEntityType);
		key = new WorldKey();
		triggered = false;
	}
	
	public DungeonKeyChestTileEntity(WorldKey key) {
		this();
		this.key = key;
	}
	
	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(this.worldPosition, 3, this.getUpdateTag());
	}

	@Override
	public CompoundNBT getUpdateTag() {
		return this.save(new CompoundNBT());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		super.onDataPacket(net, pkt);
		handleUpdateTag(this.getBlockState(), pkt.getTag());
	}
	
	protected void dirty() {
		level.sendBlockUpdated(worldPosition, this.level.getBlockState(worldPosition), this.level.getBlockState(worldPosition), 3);
		setChanged();
	}
	
	private static final String NBT_KEY = "switch_key";
	private static final String NBT_TRIGGERED = "triggered";
	
	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		nbt = super.save(nbt);
		
		nbt.put(NBT_KEY, this.key.asNBT());
		nbt.putBoolean(NBT_TRIGGERED, this.isTriggered());
		
		return nbt;
	}
	
	@Override
	public void load(BlockState state, CompoundNBT nbt) {
		super.load(state, nbt);
		
		this.key = WorldKey.fromNBT(nbt.getCompound(NBT_KEY));
		this.triggered = nbt.getBoolean(NBT_TRIGGERED);
	}
	
	public boolean isTriggered() {
		return this.triggered;
	}
	
	public void setTriggered(boolean triggered) {
		this.triggered = triggered;
		this.dirty();
	}
	
	@Override
	public void setWorldKey(WorldKey key) {
		setWorldKey(key, false);
	}
	
	public void setWorldKey(WorldKey key, boolean isWorldGen) {
		this.key = key;
		if (!isWorldGen) {
			dirty();
		}
	}
	
	@Override
	public boolean hasWorldKey() {
		return true; // Always have one
	}
	
	@Override
	public WorldKey getWorldKey() {
		return this.key;
	}
	
	public boolean isLarge() {
		return getBlockState().getBlock() instanceof DungeonKeyChestBlock.Large;
	}
	
	public Vector3d getCenterOffset() {
		final VoxelShape shape;
		if (isLarge()) {
			shape = ((DungeonKeyChestBlock.Large) this.getBlockState().getBlock()).getWholeShape(getBlockState(), level, worldPosition, ISelectionContext.empty());
		} else {
			shape = this.getBlockState().getShape(level, worldPosition);
		}
		return new Vector3d(
				(shape.max(Axis.X) - shape.min(Axis.X)) / 2 + shape.min(Axis.X),
				shape.max(Axis.Y),
				(shape.max(Axis.Z) - shape.min(Axis.Z)) / 2 + shape.min(Axis.Z)
				);
	}
	
	public void open(PlayerEntity player) {
		if (this.level.isClientSide() || this.isTriggered()) {
			return;
		}
		
		this.setTriggered(true);
		AutoDungeons.GetWorldKeys().addKey(getWorldKey());
		this.level.blockEvent(worldPosition, getBlockState().getBlock(), 0, 0);
		level.playSound(null, worldPosition, SoundEvents.CHEST_OPEN, SoundCategory.BLOCKS, .5f, .8f);
		
		NostrumMagicaSounds fanfare = isLarge() ? NostrumMagicaSounds.AMBIENT_WOOSH2 : NostrumMagicaSounds.AMBIENT_WOOSH3;
		fanfare.play(level, worldPosition);
	}
	
	@Override
	public boolean triggerEvent(int id, int type) {
		if (id == 0) {
			if (this.level != null && this.level.isClientSide()) {
				openTicks = this.level.getGameTime();
				final boolean large = isLarge();
				final Random rand = level.random;
				final VoxelShape shape;
				if (large) {
					shape = ((DungeonKeyChestBlock.Large) this.getBlockState().getBlock()).getWholeShape(getBlockState(), level, worldPosition, ISelectionContext.empty());
				} else {
					shape = this.getBlockState().getShape(level, worldPosition);
				}
				final double x = worldPosition.getX() + shape.min(Axis.X);
				final double z = worldPosition.getZ() + shape.min(Axis.Z);
				final double y = worldPosition.getY() + shape.max(Axis.Y);
				for (int i = 0; i < (large ? 40 : 20); i++) {
					final double xOffset = rand.nextFloat() * (shape.max(Axis.X) - shape.min(Axis.X));
					final double zOffset = rand.nextFloat() * (shape.max(Axis.Z) - shape.min(Axis.Z));
					
					NostrumParticles.GLOW_ORB.spawn(this.level, new SpawnParams(
							1, x + xOffset, y, z + zOffset, 0,
							40, 20,
							new Vector3d(0, .05, 0), new Vector3d(.005, .045, .005)
							).color(.5f, 1f, 1f, .25f));
				}
			}
			return true;
		}
		return super.triggerEvent(id, type);
	}
	
	public long getOpenTicks() {
		return this.openTicks;
	}
	
	// Could imagine an interface like this for dungeons instead of just blueprints that gets the
	// dungeon instance passed through
//	@Override
//	public void onDungeonSpawn(UUID dungeonID, UUID roomID, boolean isWorldGen) {
//		this.key = dungeon.key;
//	}
}