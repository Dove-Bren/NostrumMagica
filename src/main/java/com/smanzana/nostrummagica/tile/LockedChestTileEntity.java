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

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class LockedChestTileEntity extends TileEntity implements ITickableTileEntity, IInventory, IWorldKeyHolder, IUniqueBlueprintTileEntity {

	private static final String NBT_INV = "inventory";
	private static final String NBT_LOCK = "lockkey";
	private static final String NBT_COLOR = "color";
	
	private final Inventory inventory;
	private WorldKey lockKey;
	private DyeColor color;
	private int ticksExisted;
	
	public LockedChestTileEntity() {
		super(NostrumTileEntities.LockedChestEntityType);
		inventory = new Inventory(27);
		lockKey = new WorldKey();
		color = DyeColor.RED;
	}
	
	private void dirty() {
		level.sendBlockUpdated(worldPosition, this.level.getBlockState(worldPosition), this.level.getBlockState(worldPosition), 3);
		setChanged();
	}
	
	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		nbt = super.save(nbt);
		
		nbt.put(NBT_INV, Inventories.serializeInventory(inventory));
		nbt.put(NBT_LOCK, lockKey.asNBT());
		nbt.putString(NBT_COLOR, color.name());
		
		return nbt;
	}
	
	@Override
	public void load(BlockState state, CompoundNBT nbt) {
		super.load(state, nbt);
		
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
	
	public void attemptUnlock(PlayerEntity player) {
		if (player.isCreative()
				|| AutoDungeons.GetWorldKeys().consumeKey(lockKey)
				) {
			unlock();
		} else {
			player.sendMessage(new TranslationTextComponent("info.locked_chest.nokey"), Util.NIL_UUID);
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
				new Vector3d(0, .1, 0), new Vector3d(flySpeed, flySpeed / 2, flySpeed)
				).gravity(.075f));
		NostrumMagicaSounds.LORE.play(level, worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5);
		
		//int count, double spawnX, double spawnY, double spawnZ, double spawnJitterRadius, int lifetime, int lifetimeJitter, 
		//Vector3d velocity, Vector3d velocityJitter
	}
	
	protected void fillChestEntity(TileEntity entity) {
		if (entity != null && entity instanceof ChestTileEntity) {
			ChestTileEntity chest = (ChestTileEntity) entity;
			
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
	
	public void setContents(IInventory chest) {
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
	public boolean stillValid(PlayerEntity player) {
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
	
	public static final boolean LockChest(World world, BlockPos pos, WorldKey key) {
		TileEntity te = world.getBlockEntity(pos);
		if (te instanceof ChestTileEntity) {
			ChestTileEntity chest = (ChestTileEntity) te;
			final Direction facing = world.getBlockState(pos).getValue(ChestBlock.FACING);
			Inventory invCopy = new Inventory(chest.getContainerSize());
			
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