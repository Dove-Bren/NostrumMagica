package com.smanzana.nostrummagica.tiles;

import java.util.UUID;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.LockedChest;
import com.smanzana.nostrummagica.blocks.NostrumBlocks;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.utils.Inventories;
import com.smanzana.nostrummagica.world.NostrumKeyRegistry.NostrumWorldKey;

import net.minecraft.block.Block;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class LockedChestEntity extends TileEntity implements ITickableTileEntity, IInventory, IWorldKeyHolder, IUniqueDungeonTileEntity {

	private static final String NBT_INV = "inventory";
	private static final String NBT_LOCK = "lockkey";
	private static final String NBT_COLOR = "color";
	
	private final Inventory inventory;
	private NostrumWorldKey lockKey;
	private DyeColor color;
	private int ticksExisted;
	
	public LockedChestEntity() {
		super(NostrumTileEntities.LockedChestEntityType);
		inventory = new Inventory(27);
		lockKey = new NostrumWorldKey();
		color = DyeColor.RED;
	}
	
	private void dirty() {
		world.notifyBlockUpdate(pos, this.world.getBlockState(pos), this.world.getBlockState(pos), 3);
		markDirty();
	}
	
	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		nbt = super.write(nbt);
		
		nbt.put(NBT_INV, Inventories.serializeInventory(inventory));
		nbt.put(NBT_LOCK, lockKey.asNBT());
		nbt.putString(NBT_COLOR, color.name());
		
		return nbt;
	}
	
	@Override
	public void read(CompoundNBT nbt) {
		super.read(nbt);
		
		if (nbt == null)
			return;
		
		Inventories.deserializeInventory(inventory, nbt.get(NBT_INV));
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
		handleUpdateTag(pkt.getNbtCompound());
	}

	@Override
	public void tick() {
		ticksExisted++;
		
		if (world != null && !world.isRemote()) {
			if (ticksExisted % 20 == 0) {
				boolean worldUnlockable = world.getBlockState(pos).get(LockedChest.UNLOCKABLE);
				boolean tileUnlockable = NostrumMagica.instance.getWorldKeys().hasKey(lockKey); 
				if (worldUnlockable != tileUnlockable) {
					world.setBlockState(pos, world.getBlockState(pos).with(LockedChest.UNLOCKABLE, tileUnlockable), 3);
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
			player.sendMessage(new TranslationTextComponent("info.locked_chest.nokey"));
			NostrumMagicaSounds.HOOKSHOT_TICK.play(player.world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
		}
	}
	
	protected void unlock() {
		final Direction facing = world.getBlockState(pos).get(LockedChest.FACING);
		this.world.setBlockState(pos, Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, facing), 3);
		fillChestEntity(world.getTileEntity(pos));
		
		final double flySpeed = .125;
		NostrumParticles.WARD.spawn(world, new SpawnParams(
				50, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, .75,
				40, 10,
				new Vector3d(0, .1, 0), new Vector3d(flySpeed, flySpeed / 2, flySpeed)
				).gravity(.075f));
		NostrumMagicaSounds.LORE.play(world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
		
		//int count, double spawnX, double spawnY, double spawnZ, double spawnJitterRadius, int lifetime, int lifetimeJitter, 
		//Vector3d velocity, Vector3d velocityJitter
	}
	
	protected void fillChestEntity(TileEntity entity) {
		if (entity != null && entity instanceof ChestTileEntity) {
			ChestTileEntity chest = (ChestTileEntity) entity;
			
			chest.clear();
			
			final int sharedSlotCount = Math.min(this.getSizeInventory(), chest.getSizeInventory());
			
			int i = 0;
			for (; i < sharedSlotCount; i++) {
				chest.setInventorySlotContents(i, this.removeStackFromSlot(i));
			}
			
			// For any leftover items, drop on ground
			for (; i < this.getSizeInventory(); i++) {
				Block.spawnAsEntity(world, pos, this.removeStackFromSlot(i));
			}
		}
	}
	
	public void setContents(IInventory chest) {
		this.clear();
		final int sharedSlotCount = Math.min(this.getSizeInventory(), chest.getSizeInventory());
		
		int i = 0;
		for (; i < sharedSlotCount; i++) {
			this.setInventorySlotContents(i, chest.removeStackFromSlot(i));
		}
		
		// For any leftover items, drop on ground
		for (; i < this.getSizeInventory(); i++) {
			Block.spawnAsEntity(world, pos, chest.removeStackFromSlot(i));
		}
	}
	
	@Override
	public void clear() {
		this.inventory.clear();
	}

	@Override
	public int getSizeInventory() {
		return this.inventory.getSizeInventory();
	}

	@Override
	public boolean isEmpty() {
		return this.inventory.isEmpty();
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		return this.inventory.getStackInSlot(index);
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		return this.inventory.decrStackSize(index, count);
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		return this.inventory.removeStackFromSlot(index);
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		this.inventory.setInventorySlotContents(index, stack);
	}

	@Override
	public boolean isUsableByPlayer(PlayerEntity player) {
		return this.inventory.isUsableByPlayer(player);
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
	
	public static final boolean LockChest(World world, BlockPos pos, NostrumWorldKey key) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof ChestTileEntity) {
			ChestTileEntity chest = (ChestTileEntity) te;
			final Direction facing = world.getBlockState(pos).get(ChestBlock.FACING);
			Inventory invCopy = new Inventory(chest.getSizeInventory());
			
			for (int i = 0; i < invCopy.getSizeInventory(); i++) {
				invCopy.setInventorySlotContents(i, chest.removeStackFromSlot(i));
			}
			
			world.setBlockState(pos, NostrumBlocks.lockedChest.getDefaultState().with(LockedChest.FACING, facing), 3);
			
			LockedChestEntity lockedChest = (LockedChestEntity) world.getTileEntity(pos);
			lockedChest.setContents(invCopy);
			lockedChest.setWorldKey(key);
			return true;
		} else {
			return false;
		}
	}
}