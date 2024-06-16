package com.smanzana.nostrummagica.tile;

import java.util.UUID;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.dungeon.LockedDoor;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.world.NostrumKeyRegistry.NostrumWorldKey;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;

public class LockedDoorTileEntity extends TileEntity implements ITickableTileEntity, IWorldKeyHolder, IUniqueDungeonTileEntity {

	private static final String NBT_LOCK = "lockkey";
	private static final String NBT_COLOR = "color";
	
	private NostrumWorldKey lockKey;
	private DyeColor color;
	private int ticksExisted;
	
	public LockedDoorTileEntity() {
		super(NostrumTileEntities.LockedDoorType);
		lockKey = new NostrumWorldKey();
		color = DyeColor.GRAY;
	}
	
	private void dirty() {
		world.notifyBlockUpdate(pos, this.world.getBlockState(pos), this.world.getBlockState(pos), 3);
		markDirty();
	}
	
	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		nbt = super.write(nbt);
		
		nbt.put(NBT_LOCK, lockKey.asNBT());
		nbt.putString(NBT_COLOR, color.name());
		
		return nbt;
	}
	
	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		
		if (nbt == null)
			return;
		
		lockKey = NostrumWorldKey.fromNBT(nbt.getCompound(NBT_LOCK));
		try {
			color = DyeColor.valueOf(nbt.getString(NBT_COLOR).toUpperCase());
		} catch (Exception e) {
			color = DyeColor.RED;
		}
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

	@Override
	public void tick() {
		ticksExisted++;
		
		if (world != null && !world.isRemote()) {
			if (ticksExisted % 20 == 0) {
				boolean worldUnlockable = world.getBlockState(pos).get(LockedDoor.UNLOCKABLE);
				boolean tileUnlockable = NostrumMagica.instance.getWorldKeys().hasKey(lockKey); 
				if (worldUnlockable != tileUnlockable) {
					world.setBlockState(pos, world.getBlockState(pos).with(LockedDoor.UNLOCKABLE, tileUnlockable), 3);
				}
			}
		}
	}
	
	public void attemptUnlock(PlayerEntity player) {
		if (player.isCreative()
				|| NostrumMagica.instance.getWorldKeys().consumeKey(lockKey)
				) {
			unlock();
		} else {
			player.sendMessage(new TranslationTextComponent("info.locked_door.nokey"), Util.DUMMY_UUID);
			NostrumMagicaSounds.HOOKSHOT_TICK.play(player.world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
		}
	}
	
	protected void unlock() {
		BlockState state = world.getBlockState(pos);
		((LockedDoor) state.getBlock()).clearDoor(world, pos, state);
		
		final double flySpeed = .125;
		NostrumParticles.WARD.spawn(world, new SpawnParams(
				50, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, .75,
				40, 10,
				new Vector3d(0, .1, 0), new Vector3d(flySpeed, flySpeed / 2, flySpeed)
				).gravity(.075f));
		NostrumMagicaSounds.LORE.play(world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
	}
	
	@Override
	public boolean hasWorldKey() {
		return this.getWorldKey() != null;
	}

	@Override
	public NostrumWorldKey getWorldKey() {
		return this.lockKey;
	}

	@Override
	public void setWorldKey(NostrumWorldKey key) {
		this.lockKey = key;
		this.dirty();
	}
	
	public void setColor(DyeColor color) {
		this.color = color;
		this.dirty();
	}
	
	public DyeColor getColor() {
		return this.color;
	}
	
	@Override
	public void onDungeonSpawn(UUID dungeonID, UUID roomID, boolean isWorldGen) {
		// TODO: should this use dungeon ID? Or even let it be configurable?
		// Sorcery dungeon is one big room, and I feel like MOST of my uses of this
		// will want unique-per-room keys?
		// Ehh well the whole points is that things don't have to be close to eachother, so maybe
		// that's wrong?
		final NostrumWorldKey newKey = this.lockKey.mutateWithID(roomID);
		if (isWorldGen) {
			this.lockKey = newKey;
		} else {
			this.setWorldKey(newKey);
		}
	}
	
	private BlockPos bottomStash = null;
	public BlockPos getBottomCenterPos() {
		if (bottomStash == null) {
			bottomStash = LockedDoor.FindBottomCenterPos(getWorld(), getPos());
		}
		return bottomStash;
	}
	
	public Direction getFace() {
		return this.getBlockState().get(LockedDoor.HORIZONTAL_FACING);
	}
	
	private MutableBoundingBox boundsStach = null;
	public MutableBoundingBox getDoorBounds() {
		if (boundsStach == null) {
			boundsStach = LockedDoor.FindDisplayBounds(getWorld(), getPos());
		}
		return boundsStach;
	}
}