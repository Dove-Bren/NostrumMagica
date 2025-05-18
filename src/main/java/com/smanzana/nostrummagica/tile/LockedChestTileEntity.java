package com.smanzana.nostrummagica.tile;

import java.util.UUID;

import com.smanzana.autodungeons.AutoDungeons;
import com.smanzana.autodungeons.tile.IUniqueBlueprintTileEntity;
import com.smanzana.autodungeons.tile.IWorldKeyHolder;
import com.smanzana.autodungeons.world.WorldKey;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.block.dungeon.LockedChestBlock;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.util.Inventories;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class LockedChestTileEntity extends BlockEntity implements TickableBlockEntity, Container, IWorldKeyHolder, IUniqueBlueprintTileEntity {

	private static final String NBT_INV = "inventory";
	private static final String NBT_LOCK = "lockkey";
	private static final String NBT_COLOR = "color";
	
	private final SimpleContainer inventory;
	private WorldKey lockKey;
	private DyeColor color;
	private int ticksExisted;
	
	public LockedChestTileEntity(BlockPos pos, BlockState state) {
		super(NostrumBlockEntities.LockedChest, pos, state);
		inventory = new SimpleContainer(27);
		lockKey = new WorldKey();
		color = DyeColor.RED;
	}
	
	private void dirty() {
		level.sendBlockUpdated(worldPosition, this.level.getBlockState(worldPosition), this.level.getBlockState(worldPosition), 3);
		setChanged();
	}
	
	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		nbt.put(NBT_INV, Inventories.serializeInventory(inventory));
		nbt.put(NBT_LOCK, lockKey.asNBT());
		nbt.putString(NBT_COLOR, color.name());
	}
	
	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		
		if (nbt == null)
			return;
		
		Inventories.deserializeInventory(inventory, nbt.get(NBT_INV));
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

	@Override
	public void tick() {
		ticksExisted++;
		
		if (level != null && !level.isClientSide()) {
			if (ticksExisted % 20 == 0) {
				boolean worldUnlockable = level.getBlockState(worldPosition).getValue(LockedChestBlock.UNLOCKABLE);
				boolean tileUnlockable = AutoDungeons.GetWorldKeys().hasKey(lockKey); 
				if (worldUnlockable != tileUnlockable) {
					level.setBlock(worldPosition, level.getBlockState(worldPosition).setValue(LockedChestBlock.UNLOCKABLE, tileUnlockable), 3);
				}
			}
		}
	}
	
	public void attemptUnlock(Player player) {
		if (player.isCreative()
				|| AutoDungeons.GetWorldKeys().consumeKey(lockKey)
				) {
			unlock();
		} else {
			player.sendMessage(new TranslatableComponent("info.locked_chest.nokey"), Util.NIL_UUID);
			NostrumMagicaSounds.HOOKSHOT_TICK.play(player.level, worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5);
		}
	}
	
	protected void unlock() {
		final Direction facing = level.getBlockState(worldPosition).getValue(LockedChestBlock.FACING);
		this.level.setBlock(worldPosition, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), 3);
		fillChestEntity(level.getBlockEntity(worldPosition));
		
		final double flySpeed = .125;
		NostrumParticles.WARD.spawn(level, new SpawnParams(
				50, worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5, .75,
				40, 10,
				new Vec3(0, .1, 0), new Vec3(flySpeed, flySpeed / 2, flySpeed)
				).gravity(.075f));
		NostrumMagicaSounds.LORE.play(level, worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5);
		
		//int count, double spawnX, double spawnY, double spawnZ, double spawnJitterRadius, int lifetime, int lifetimeJitter, 
		//Vector3d velocity, Vector3d velocityJitter
	}
	
	protected void fillChestEntity(BlockEntity entity) {
		if (entity != null && entity instanceof ChestBlockEntity) {
			ChestBlockEntity chest = (ChestBlockEntity) entity;
			
			chest.clearContent();
			
			final int sharedSlotCount = Math.min(this.getContainerSize(), chest.getContainerSize());
			
			int i = 0;
			for (; i < sharedSlotCount; i++) {
				chest.setItem(i, this.removeItemNoUpdate(i));
			}
			
			// For any leftover items, drop on ground
			for (; i < this.getContainerSize(); i++) {
				Block.popResource(level, worldPosition, this.removeItemNoUpdate(i));
			}
		}
	}
	
	public void setContents(Container chest) {
		this.clearContent();
		final int sharedSlotCount = Math.min(this.getContainerSize(), chest.getContainerSize());
		
		int i = 0;
		for (; i < sharedSlotCount; i++) {
			this.setItem(i, chest.removeItemNoUpdate(i));
		}
		
		// For any leftover items, drop on ground
		for (; i < this.getContainerSize(); i++) {
			Block.popResource(level, worldPosition, chest.removeItemNoUpdate(i));
		}
	}
	
	@Override
	public void clearContent() {
		this.inventory.clearContent();
	}

	@Override
	public int getContainerSize() {
		return this.inventory.getContainerSize();
	}

	@Override
	public boolean isEmpty() {
		return this.inventory.isEmpty();
	}

	@Override
	public ItemStack getItem(int index) {
		return this.inventory.getItem(index);
	}

	@Override
	public ItemStack removeItem(int index, int count) {
		return this.inventory.removeItem(index, count);
	}

	@Override
	public ItemStack removeItemNoUpdate(int index) {
		return this.inventory.removeItemNoUpdate(index);
	}

	@Override
	public void setItem(int index, ItemStack stack) {
		this.inventory.setItem(index, stack);
	}

	@Override
	public boolean stillValid(Player player) {
		return this.inventory.stillValid(player);
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
	public void onRoomBlueprintSpawn(UUID dungeonID, UUID roomID, boolean isWorldGen) {
		// TODO: should this use dungeon ID? Or even let it be configurable?
		// Sorcery dungeon is one big room, and I feel like MOST of my uses of this
		// will want unique-per-room keys?
		// Ehh well the whole points is that things don't have to be close to eachother, so maybe
		// that's wrong?
		final WorldKey newKey = this.lockKey.mutateWithID(roomID);
		if (isWorldGen) {
			this.lockKey = newKey;
		} else {
			this.setWorldKey(newKey);
		}
	}
	
	public static final boolean LockChest(Level world, BlockPos pos, WorldKey key) {
		BlockEntity te = world.getBlockEntity(pos);
		if (te instanceof ChestBlockEntity) {
			ChestBlockEntity chest = (ChestBlockEntity) te;
			final Direction facing = world.getBlockState(pos).getValue(ChestBlock.FACING);
			SimpleContainer invCopy = new SimpleContainer(chest.getContainerSize());
			
			for (int i = 0; i < invCopy.getContainerSize(); i++) {
				invCopy.setItem(i, chest.removeItemNoUpdate(i));
			}
			
			world.setBlock(pos, NostrumBlocks.lockedChest.defaultBlockState().setValue(LockedChestBlock.FACING, facing), 3);
			
			LockedChestTileEntity lockedChest = (LockedChestTileEntity) world.getBlockEntity(pos);
			lockedChest.setContents(invCopy);
			lockedChest.setWorldKey(key);
			return true;
		} else {
			return false;
		}
	}
}