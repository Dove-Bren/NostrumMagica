package com.smanzana.nostrummagica.tile;

import java.util.UUID;

import com.smanzana.autodungeons.AutoDungeons;
import com.smanzana.autodungeons.tile.IUniqueBlueprintTileEntity;
import com.smanzana.autodungeons.tile.IWorldKeyHolder;
import com.smanzana.autodungeons.world.WorldKey;
import com.smanzana.nostrummagica.block.dungeon.LockedDoorBlock;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;

public class LockedDoorTileEntity extends TileEntity implements ITickableTileEntity, IWorldKeyHolder, IUniqueBlueprintTileEntity {

	private static final String NBT_LOCK = "lockkey";
	private static final String NBT_COLOR = "color";
	
	private WorldKey lockKey;
	private DyeColor color;
	private int ticksExisted;
	
	protected LockedDoorTileEntity(TileEntityType<? extends LockedDoorTileEntity> type) {
		super(type);
		lockKey = new WorldKey();
		color = DyeColor.GRAY;
	}
	
	public LockedDoorTileEntity() {
		this(NostrumTileEntities.LockedDoorType);
	}
	
	private void dirty() {
		level.sendBlockUpdated(worldPosition, this.level.getBlockState(worldPosition), this.level.getBlockState(worldPosition), 3);
		setChanged();
	}
	
	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		nbt = super.save(nbt);
		
		nbt.put(NBT_LOCK, lockKey.asNBT());
		nbt.putString(NBT_COLOR, color.name());
		
		return nbt;
	}
	
	@Override
	public void load(BlockState state, CompoundNBT nbt) {
		super.load(state, nbt);
		
		if (nbt == null)
			return;
		
		lockKey = WorldKey.fromNBT(nbt.getCompound(NBT_LOCK));
		try {
			color = DyeColor.valueOf(nbt.getString(NBT_COLOR).toUpperCase());
		} catch (Exception e) {
			color = DyeColor.RED;
		}
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
	
	protected void checkBlockState() {
		boolean worldUnlockable = level.getBlockState(worldPosition).getValue(LockedDoorBlock.UNLOCKABLE);
		boolean tileUnlockable = AutoDungeons.GetWorldKeys().hasKey(lockKey); 
		if (worldUnlockable != tileUnlockable) {
			level.setBlock(worldPosition, level.getBlockState(worldPosition).setValue(LockedDoorBlock.UNLOCKABLE, tileUnlockable), 3);
		}
	}

	@Override
	public void tick() {
		ticksExisted++;
		
		if (level != null && !level.isClientSide()) {
			if (ticksExisted % 20 == 0) {
				checkBlockState();
			}
		}
	}
	
	public void attemptUnlock(PlayerEntity player) {
		if (player.isCreative()
				|| AutoDungeons.GetWorldKeys().consumeKey(lockKey)
				) {
			unlock();
		} else {
			player.sendMessage(new TranslationTextComponent("info.locked_door.nokey"), Util.NIL_UUID);
			NostrumMagicaSounds.HOOKSHOT_TICK.play(player.level, worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5);
		}
	}
	
	protected void unlock() {
		BlockState state = level.getBlockState(worldPosition);
		((LockedDoorBlock) state.getBlock()).clearDoor(level, worldPosition, state);
		
		final double flySpeed = .125;
		NostrumParticles.WARD.spawn(level, new SpawnParams(
				50, worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5, .75,
				40, 10,
				new Vector3d(0, .1, 0), new Vector3d(flySpeed, flySpeed / 2, flySpeed)
				).gravity(.075f));
		NostrumMagicaSounds.LORE.play(level, worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5);
	}
	
	@Override
	public boolean hasWorldKey() {
		return this.getWorldKey() != null;
	}

	@Override
	public WorldKey getWorldKey() {
		return this.lockKey;
	}

	@Override
	public void setWorldKey(WorldKey key) {
		setWorldKey(key, false);
	}
	
	public void setWorldKey(WorldKey key, boolean isWorldGen) {
		this.lockKey = key;
		if (!isWorldGen) {
			this.dirty();
		}
	}
	
	public void setColor(DyeColor color) {
		this.color = color;
		this.dirty();
	}
	
	public DyeColor getColor() {
		return this.color;
	}
	
	@Override
	public void onRoomBlueprintSpawn(UUID dungeonID, UUID roomID, boolean isWorldGen) {
		// TODO: should this use dungeon ID? Or even let it be configurable?
		// Sorcery dungeon is one big room, and I feel like MOST of my uses of this
		// will want unique-per-room keys?
		// Ehh well the whole points is that things don't have to be close to eachother, so maybe
		// that's wrong?
		final WorldKey newKey = this.lockKey.mutateWithID(roomID);
		setWorldKey(newKey, isWorldGen);
	}
	
	private BlockPos bottomStash = null;
	public BlockPos getBottomCenterPos() {
		if (bottomStash == null) {
			bottomStash = LockedDoorBlock.FindBottomCenterPos(getLevel(), getBlockPos());
		}
		return bottomStash;
	}
	
	public Direction getFace() {
		return this.getBlockState().getValue(LockedDoorBlock.FACING);
	}
	
	private MutableBoundingBox boundsStach = null;
	public MutableBoundingBox getDoorBounds() {
		if (boundsStach == null) {
			boundsStach = LockedDoorBlock.FindDisplayBounds(getLevel(), getBlockPos());
		}
		return boundsStach;
	}
}