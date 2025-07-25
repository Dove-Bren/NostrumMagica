package com.smanzana.nostrummagica.tile;

import java.util.UUID;

import com.smanzana.autodungeons.AutoDungeons;
import com.smanzana.autodungeons.api.block.entity.IUniqueBlueprintTileEntity;
import com.smanzana.autodungeons.api.block.entity.IWorldKeyHolder;
import com.smanzana.autodungeons.world.WorldKey;
import com.smanzana.autodungeons.world.dungeon.DungeonInstance;
import com.smanzana.nostrummagica.block.dungeon.LockedDoorBlock;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;

public class LockedDoorTileEntity extends BlockEntity implements TickableBlockEntity, IWorldKeyHolder, IUniqueBlueprintTileEntity {

	private static final String NBT_LOCK = "lockkey";
	private static final String NBT_COLOR = "color";
	
	private WorldKey lockKey;
	private DyeColor color;
	private int ticksExisted;
	
	protected LockedDoorTileEntity(BlockEntityType<? extends LockedDoorTileEntity> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		lockKey = new WorldKey();
		color = DyeColor.GRAY;
	}
	
	public LockedDoorTileEntity(BlockPos pos, BlockState state) {
		this(NostrumBlockEntities.LockedDoor, pos, state);
	}
	
	private void dirty() {
		level.sendBlockUpdated(worldPosition, this.level.getBlockState(worldPosition), this.level.getBlockState(worldPosition), 3);
		setChanged();
	}
	
	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		nbt.put(NBT_LOCK, lockKey.asNBT());
		nbt.putString(NBT_COLOR, color.name());
	}
	
	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		
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
	
	public void attemptUnlock(Player player) {
		if (player.isCreative()
				|| AutoDungeons.GetWorldKeys().consumeKey(lockKey)
				) {
			unlock();
		} else {
			player.sendMessage(new TranslatableComponent("info.locked_door.nokey"), Util.NIL_UUID);
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
				new Vec3(0, .1, 0), new Vec3(flySpeed, flySpeed / 2, flySpeed)
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
	public void onRoomBlueprintSpawn(DungeonInstance dungeonInstance, UUID roomID, boolean isWorldGen) {
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
	
	private BoundingBox boundsStach = null;
	public BoundingBox getDoorBounds() {
		if (boundsStach == null) {
			boundsStach = LockedDoorBlock.FindDisplayBounds(getLevel(), getBlockPos());
		}
		return boundsStach;
	}
}