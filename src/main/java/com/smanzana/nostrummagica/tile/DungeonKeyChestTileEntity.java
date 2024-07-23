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
		return new SUpdateTileEntityPacket(this.pos, 3, this.getUpdateTag());
	}

	@Override
	public CompoundNBT getUpdateTag() {
		return this.write(new CompoundNBT());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		super.onDataPacket(net, pkt);
		handleUpdateTag(this.getBlockState(), pkt.getNbtCompound());
	}
	
	protected void dirty() {
		world.notifyBlockUpdate(pos, this.world.getBlockState(pos), this.world.getBlockState(pos), 3);
		markDirty();
	}
	
	private static final String NBT_KEY = "switch_key";
	private static final String NBT_TRIGGERED = "triggered";
	
	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		nbt = super.write(nbt);
		
		nbt.put(NBT_KEY, this.key.asNBT());
		nbt.putBoolean(NBT_TRIGGERED, this.isTriggered());
		
		return nbt;
	}
	
	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		
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
			shape = ((DungeonKeyChestBlock.Large) this.getBlockState().getBlock()).getWholeShape(getBlockState(), world, pos, ISelectionContext.dummy());
		} else {
			shape = this.getBlockState().getShape(world, pos);
		}
		return new Vector3d(
				(shape.getEnd(Axis.X) - shape.getStart(Axis.X)) / 2 + shape.getStart(Axis.X),
				shape.getEnd(Axis.Y),
				(shape.getEnd(Axis.Z) - shape.getStart(Axis.Z)) / 2 + shape.getStart(Axis.Z)
				);
	}
	
	public void open(PlayerEntity player) {
		if (this.world.isRemote() || this.isTriggered()) {
			return;
		}
		
		this.setTriggered(true);
		AutoDungeons.GetWorldKeys().addKey(getWorldKey());
		this.world.addBlockEvent(pos, getBlockState().getBlock(), 0, 0);
		world.playSound(null, pos, SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, .5f, .8f);
		
		NostrumMagicaSounds fanfare = isLarge() ? NostrumMagicaSounds.AMBIENT_WOOSH2 : NostrumMagicaSounds.AMBIENT_WOOSH3;
		fanfare.play(world, pos);
	}
	
	@Override
	public boolean receiveClientEvent(int id, int type) {
		if (id == 0) {
			if (this.world != null && this.world.isRemote()) {
				openTicks = this.world.getGameTime();
				final boolean large = isLarge();
				final Random rand = world.rand;
				final VoxelShape shape;
				if (large) {
					shape = ((DungeonKeyChestBlock.Large) this.getBlockState().getBlock()).getWholeShape(getBlockState(), world, pos, ISelectionContext.dummy());
				} else {
					shape = this.getBlockState().getShape(world, pos);
				}
				final double x = pos.getX() + shape.getStart(Axis.X);
				final double z = pos.getZ() + shape.getStart(Axis.Z);
				final double y = pos.getY() + shape.getEnd(Axis.Y);
				for (int i = 0; i < (large ? 40 : 20); i++) {
					final double xOffset = rand.nextFloat() * (shape.getEnd(Axis.X) - shape.getStart(Axis.X));
					final double zOffset = rand.nextFloat() * (shape.getEnd(Axis.Z) - shape.getStart(Axis.Z));
					
					NostrumParticles.GLOW_ORB.spawn(this.world, new SpawnParams(
							1, x + xOffset, y, z + zOffset, 0,
							40, 20,
							new Vector3d(0, .05, 0), new Vector3d(.005, .045, .005)
							).color(.5f, 1f, 1f, .25f));
				}
			}
			return true;
		}
		return super.receiveClientEvent(id, type);
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