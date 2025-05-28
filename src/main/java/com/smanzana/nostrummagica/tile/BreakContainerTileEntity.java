package com.smanzana.nostrummagica.tile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.util.Inventories;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BreakContainerTileEntity extends BlockEntity implements Container {
	
	private static final String NBT_INVENTORY = "inventory";
	private static final String NBT_LOOT_TABLE = "loot_table";
	private static final String NBT_LOOT_SEED = "loot_seed";
	
	private @Nullable SimpleContainer inventory;
	// OR
	private @Nullable ResourceLocation lootTable;
	private long lootSeed;
	
	public BreakContainerTileEntity(BlockPos pos, BlockState state) {
		super(NostrumBlockEntities.BreakContainer, pos, state);
	}
	
	protected void guaranteeInventory() {
		if (inventory == null) {
			inventory = new SimpleContainer(27);
			inventory.addListener((c) -> this.dirty());
			lootTable = null;
		}
	}
	
	public void addItem(ItemStack stack) {
		guaranteeInventory();
		addItem(stack, true);
	}
	
	public void addItem(ItemStack stack, boolean randomSpot) {
		guaranteeInventory();
		if (randomSpot) {
			setItemInRandomSlot(stack);
		} else {
			Inventories.addItem(this, stack);
		}
	}
	
	public ItemStack getFirstHeldItem() {
		if (this.inventory != null) {
			for (int i = 0; i < this.getContainerSize(); i++) {
				ItemStack stack = this.getItem(i);
				if (!stack.isEmpty()) {
					return stack;
				}
			}
		}
		return ItemStack.EMPTY;
	}
	
	protected boolean setItemInRandomSlot(ItemStack stack) {
		guaranteeInventory();
		List<Integer> spots = new ArrayList<>(27);
		for (int i = 0; i < this.getContainerSize(); i++) {
			ItemStack held = this.getItem(i);
			if (held.isEmpty()) {
				spots.add(i);
			}
		}
		
		if (!spots.isEmpty()) {
			Collections.shuffle(spots);
			this.setItem(spots.get(0), stack);
			return true;
		}
		
		return false;
	}
	
	public boolean isChestMode() {
		if (this.inventory == null && this.lootTable != null) {
			return true;
		}
		
		boolean foundOne = false;
		for (int i = 0; i < this.getContainerSize(); i++) {
			ItemStack stack = this.getItem(i);
			if (!stack.isEmpty()) {
				if (foundOne) {
					// found two!
					return true;
				}
				foundOne = true;
			}
		}
		return false;
	}
	
	private void dirty() {
		if (this.level != null) {
			level.sendBlockUpdated(worldPosition, this.level.getBlockState(worldPosition), this.level.getBlockState(worldPosition), 3);
		}
		setChanged();
	}
	
	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		if (inventory != null) {
			nbt.put(NBT_INVENTORY, Inventories.serializeInventory(inventory));
		}
		if (this.lootTable != null) {
			nbt.putString(NBT_LOOT_TABLE, this.lootTable.toString());
			nbt.putLong(NBT_LOOT_SEED, lootSeed);
		}
	}
	
	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		
		if (nbt == null)
			return;
		
		if (nbt.contains(NBT_INVENTORY)) {
			this.guaranteeInventory();
			Inventories.deserializeInventory(inventory, nbt.get(NBT_INVENTORY));
		}
		
		if (nbt.contains(NBT_LOOT_TABLE)) {
			this.lootTable = ResourceLocation.parse(nbt.getString(NBT_LOOT_TABLE));
			this.lootSeed = nbt.getLong(NBT_LOOT_SEED);
		}
	}
	
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.saveWithId();
	}
	
	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		super.onDataPacket(net, pkt);
		//handleUpdateTag(pkt.getTag());
	}
	
	public boolean handleTrigger() {
		// if we have more than one items (chest mode), we want to create a chest here and return true (we already did everything).
		// Otherwise, we can return false and let the block destroy itself and take care of dropping items.
		if (isChestMode()) {
			Direction face = Direction.NORTH;
			@Nullable Player nearest = getLevel().getNearestPlayer(worldPosition.getX() + .5, worldPosition.getY(), worldPosition.getZ() + .5, 10, false);
			if (nearest != null) {
				face = nearest.getDirection().getOpposite();
				// just in case getDirection changes
				if (face.getAxis().isVertical()) {
					face = Direction.NORTH;
				}
			}
			
			final SimpleContainer copy;
			if (this.inventory != null) {
				copy = new SimpleContainer(this.inventory.getContainerSize());
				for (int i = 0; i < this.inventory.getContainerSize(); i++) {
					copy.setItem(i, inventory.removeItemNoUpdate(i));
				}
				this.inventory.setChanged();
			} else {
				copy = null;
			}
			
			// Set to chest, which will remove us. Make sure to have cleared inventory by this point!
			this.level.setBlock(worldPosition, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, face), 3); // Removes us!
			fillChestEntity(level.getBlockEntity(worldPosition), copy);
			
			// VFX since swapping block won't do it
			{
				((ServerLevel) this.getLevel()).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, this.getBlockState()),
						worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5,
						100,
						.5, .5, .5,
						.15f);
				this.getLevel().playSound(null, worldPosition, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1f, 1f);
			}
			
			return true;
		}
		
		return false;
	}
	
	protected void fillChestEntity(BlockEntity entity, Container source) {
		// No clue what dirrection to use. Maybe try and get nearby player, otherwise default
		if (entity != null && entity instanceof ChestBlockEntity chest) {
			chest.clearContent();
			
			if (source == null) {
				chest.setLootTable(lootTable, lootSeed);
			} else {
				final int sharedSlotCount = Math.min(source.getContainerSize(), chest.getContainerSize());
				
				int i = 0;
				for (; i < sharedSlotCount; i++) {
					chest.setItem(i, source.removeItemNoUpdate(i));
				}
				
				// For any leftover items, drop on ground
				for (; i < source.getContainerSize(); i++) {
					Block.popResource(level, worldPosition, source.removeItemNoUpdate(i));
				}
			}
		}
	}
	
	@Override
	public void clearContent() {
		if (this.inventory != null) {
			this.inventory.clearContent();
		}
		if (this.lootTable != null) {
			this.lootTable = null;
		}
	}

	@Override
	public int getContainerSize() {
		return inventory == null ? 0 : this.inventory.getContainerSize();
	}

	@Override
	public boolean isEmpty() {
		return inventory == null ? true : this.inventory.isEmpty();
	}

	@Override
	public ItemStack getItem(int index) {
		return inventory == null ? null : this.inventory.getItem(index);
	}

	@Override
	public ItemStack removeItem(int index, int count) {
		return inventory == null ? null : this.inventory.removeItem(index, count);
	}

	@Override
	public ItemStack removeItemNoUpdate(int index) {
		return inventory == null ? null : this.inventory.removeItemNoUpdate(index);
	}

	@Override
	public void setItem(int index, ItemStack stack) {
		if (inventory != null) {
			this.inventory.setItem(index, stack);
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return inventory != null && this.inventory.stillValid(player);
	}

	public void setContents(Container chest) {
		this.clearContent();
		this.guaranteeInventory();
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

	public void setLootTableNoUpdate(ResourceLocation lootTable, long lootSeed) {
		this.lootTable = lootTable;
		this.lootSeed = lootSeed;
	}
	
}