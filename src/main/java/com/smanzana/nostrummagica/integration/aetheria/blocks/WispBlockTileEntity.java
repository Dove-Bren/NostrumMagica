package com.smanzana.nostrummagica.integration.aetheria.blocks;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import com.smanzana.nostrumaetheria.api.blocks.AetherTickingTileEntity;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntityWisp;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.integration.aetheria.AetheriaProxy;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.utils.ContainerUtil.IAutoContainerInventory;
import com.smanzana.nostrummagica.utils.Inventories;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WispBlockTileEntity extends AetherTickingTileEntity implements IAutoContainerInventory {

	private static final String NBT_INVENTORY = "inventory";
	private static final String NBT_PARTIAL = "partial";
	
	// Synced+saved
	private @Nonnull ItemStack scroll;
	private @Nonnull ItemStack reagent;
	private float reagentPartial;
	private boolean activated;
	
	// Transient
	private List<EntityWisp> wisps; // on server
	private int numWisps;
	
	// TODO add progression. Maybe insert essences or mani crystals?
	// TODO add some cool like mani crystal generation. That'd be neat :)
	// TODO part of progression: be able to spawn more wisps!
	private static final int MAX_WISPS = 3;
	private static final float REAGENT_PER_SECOND = (1f / 120f);  // 1 per 2 minutes
	
	private static final int MAX_AETHER = 5000;
	private static final int AETHER_PER_TICK = 2;
	
	private int ticksExisted;
	
	public WispBlockTileEntity() {
		super(AetheriaProxy.WispBlockTileEntityType, 0, MAX_AETHER);
		scroll = ItemStack.EMPTY;
		reagent = ItemStack.EMPTY;
		reagentPartial = 0f;
		wisps = new LinkedList<>();
		ticksExisted = 0;
		activated = false;
		this.setAutoSync(5);
		this.compWrapper.configureInOut(true, false);
	}
	
	public ItemStack getScroll() {
		return scroll;
	}
	
	public boolean setScroll(ItemStack item) {
		if (!item.isEmpty() && !this.scroll.isEmpty())
			return false;
		
		if (!isItemValidForSlot(0, item)) {
			return false;
		}
		
		this.setInventorySlotContents(0, item);
		return true;
	}
	
	public ItemStack getReagent() {
		return reagent;
	}
	
	public boolean setReagent(ItemStack item) {
		if (!item.isEmpty() && !this.reagent.isEmpty())
			return false;
		
		if (!isItemValidForSlot(1, item)) {
			return false;
		}
		
		this.setInventorySlotContents(1, item);
		return true;
	}
	
	public float getPartialReagent() {
		return reagentPartial;
	}
	
	public int getWispCount() {
		return this.world.isRemote ? this.numWisps : this.wisps.size();
	}
	
	public int getMaxWisps() {
		return MAX_WISPS;
	}
	
	private void dirtyAndUpdate() {
		if (world != null) {
			world.notifyBlockUpdate(pos, this.world.getBlockState(pos), this.world.getBlockState(pos), 3);
			markDirty();
		}
	}
	
	// Cleans up any wisps as soon as we deactivate
	public void deactivate() {
		this.activated = false;
		
		for (EntityWisp wisp : this.wisps) {
			wisp.remove();
		}
		wisps.clear();
		
		dirtyAndUpdate();
	}
	
	private void activate() {
		this.activated = true;
		dirtyAndUpdate();
	}
	
	private void spawnWisp() {
		
		BlockPos spawnPos = null;
		
		// Try to find a safe place to spawn the wisp
		int attempts = 20;
		do {
			spawnPos = this.pos.add(
					NostrumMagica.rand.nextInt(10) - 5,
					NostrumMagica.rand.nextInt(5),
					NostrumMagica.rand.nextInt(10) - 5);
		} while (!world.isAirBlock(spawnPos) && attempts-- >= 0);
		
		if (world.isAirBlock(spawnPos)) {
			EntityWisp wisp = new EntityWisp(NostrumEntityTypes.wisp, this.world, this.pos);
			wisp.setPosition(spawnPos.getX() + .5, spawnPos.getY(), spawnPos.getZ() + .5);
			this.wisps.add(wisp);
			this.world.addEntity(wisp);
			//this.dirtyAndUpdate();
		}
	}

	@Override
	public void tick() {
		super.tick();
		ticksExisted++;
		
		if (world.isRemote) {
			return;
		}
		
		Iterator<EntityWisp> it = wisps.iterator();
		while (it.hasNext()) {
			EntityWisp wisp = it.next();
			if (!wisp.isAlive()) {
				it.remove();
				//this.dirtyAndUpdate();
			}
		}
		
		if (!activated) {
			if (!this.getScroll().isEmpty()
					&& (!this.getReagent().isEmpty() || this.reagentPartial >= REAGENT_PER_SECOND)
					/*&& (this.getOnlyMyAether(null) > AETHER_PER_TICK)*/) {
				activate();
			} else {
				return;
			}
		}
		
		// If no scroll is present, deactivate
		if (this.getScroll().isEmpty()) {
			deactivate();
			return;
		}
		
		// Passively burn reagents. If there are none, kill all wisps and deactivate
		if (ticksExisted % 20 == 0 && !wisps.isEmpty()) {
			float debt = REAGENT_PER_SECOND;
			if (reagentPartial < debt) {
				// Not enough partial. Try to consume from reagent stack.
				// If not there, next bit of logic will turn reagentPartial negative and
				// know to deactivate
				if (getReagent() != null && getReagent().getCount() > 0) {
					reagentPartial += 1f;
					if (getReagent().getCount() > 1) {
						getReagent().shrink(1);
					} else {
						setReagent(ItemStack.EMPTY);
					}
				}
			}
			
			// Regardless of if we have enough, subtract debt
			reagentPartial -= debt;
			
			// If negative, we didn't have enough to run for another tick! Deactivate!
			if (reagentPartial < 0) {
				deactivate();
			} else {
				// Update client
				//this.dirtyAndUpdate();
			}
		}
		
		// Every tick, consume aether
		if (!wisps.isEmpty()) {
			final int debt = AETHER_PER_TICK * getWispCount();
			if (this.compWrapper.getHandlerIfPresent().drawAether(null, debt) != debt) {
				// Didn't have enough. Deactivate!
				deactivate();
			} else {
				// Try to fill up what we just spent
				this.compWrapper.getHandlerIfPresent().fillAether(1000);
			}
		}
		
		if (!activated) {
			return;
		}
		
		// If not at max wisps, maybe spawn one every once in a while
		if (ticksExisted % (20 * 3) == 0 && wisps.size() < getMaxWisps()) {
			if (NostrumMagica.rand.nextInt(10) == 0) {
				spawnWisp();
			}
		}
	}
	
	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		nbt = super.write(nbt);
		
		if (nbt == null)
			nbt = new CompoundNBT();
		
		if (reagentPartial != 0f)
			nbt.putFloat(NBT_PARTIAL, reagentPartial);
		
		nbt.put(NBT_INVENTORY, Inventories.serializeInventory(this));
		
//		if (scroll != null)
//			nbt.put("scroll", scroll.serializeNBT());
//		
//		if (reagent != null)
//			nbt.put("reagent", reagent.serializeNBT());
//		
//		
//		
//		if (activated) {
//			nbt.putBoolean("active", activated);
//			nbt.putInt("wisps", wisps.size());
//		}
		
		return nbt;
	}
	
	@Override
	public void read(CompoundNBT nbt) {
		super.read(nbt);
		
		if (nbt == null)
			return;
		
		Inventories.deserializeInventory(this, nbt.get(NBT_INVENTORY));
		this.reagentPartial = nbt.getFloat(NBT_PARTIAL);
		
//		this.scroll = ItemStack.loadItemStackFromNBT(nbt.getCompound("scroll"));
//		this.reagent = ItemStack.loadItemStackFromNBT(nbt.getCompound("reagent"));
//		this.activated = nbt.getBoolean("active");
//		this.numWisps = nbt.getInt("wisps");
	}
	
	@Override
	public void setWorld(World world) {
		super.setWorld(world);
		
		if (!world.isRemote) {
			this.compWrapper.setAutoFill(true);
		}
	}
	
	@Override
	public int getSizeInventory() {
		return 2;
	}
	
	@Override
	public ItemStack getStackInSlot(int index) {
		if (index < 0 || index >= getSizeInventory())
			return ItemStack.EMPTY;
		
		if (index == 0) {
			return scroll;
		} else {
			return reagent;
		}
	}
	
	@Override
	public ItemStack decrStackSize(int index, int count) {
		if (index < 0 || index >= getSizeInventory()) {
			return ItemStack.EMPTY;
		}
		
		ItemStack inSlot = getStackInSlot(index);
		if (inSlot.isEmpty()) {
			return ItemStack.EMPTY;
		}
		
		ItemStack stack;
		if (inSlot.getCount() <= count) {
			stack = inSlot;
			inSlot = ItemStack.EMPTY;
		} else {
			stack = inSlot.copy();
			stack.setCount(count);
			inSlot.shrink(count);
		}
		
		if (inSlot.isEmpty()) {
			setInventorySlotContents(index, inSlot);
		}
		
		this.dirtyAndUpdate();
		return stack;
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		if (index < 0 || index >= getSizeInventory())
			return ItemStack.EMPTY;
		
		ItemStack stack;
		if (index == 0) {
			stack = scroll;
			scroll = ItemStack.EMPTY;
		} else {
			stack = reagent;
			reagent = ItemStack.EMPTY;
		}
		
		this.dirtyAndUpdate();
		return stack;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		if (!isItemValidForSlot(index, stack))
			return;
		
		if (index == 0) {
			scroll = stack;
		} else {
			reagent = stack;
		}
		
		this.dirtyAndUpdate();
	}
	
	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUsableByPlayer(PlayerEntity player) {
		return true;
	}

	@Override
	public void openInventory(PlayerEntity player) {
	}

	@Override
	public void closeInventory(PlayerEntity player) {
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		if (index < 0 || index >= getSizeInventory())
			return false;
		
		if (index == 0) {
			return (stack.isEmpty() || (stack.getItem() instanceof SpellScroll && SpellScroll.getSpell(stack) != null));
		} else {
			return (stack.isEmpty() || stack.getItem() instanceof ReagentItem);
		}
		
	}
	
	private static final int partialToInt(float progress) {
		return Math.round(progress * 10000);
	}
	
	private static final float intToPartial(int value) {
		return (float) value / 10000f;
	}

	@Override
	public int getField(int id) {
		if (id == 0) {
			return partialToInt(this.reagentPartial);
		} else if (id == 1) {
			return this.activated ? 1 : 0;
		} else if (id == 2) {
			return world.isRemote ? numWisps : wisps.size();
		}
		return 0;
	}

	@Override
	public void setField(int id, int value) {
		if (id == 0) {
			this.reagentPartial = intToPartial(value);
		} else if (id == 1) {
			this.activated = (value != 0);
		} else if (id == 2) {
			this.numWisps = value;
		}
	}

	@Override
	public int getFieldCount() {
		return 3;
	}

	@Override
	public void clear() {
		for (int i = 0; i < getSizeInventory(); i++) {
			removeStackFromSlot(i);
		}
	}

	@Override
	public boolean isEmpty() {
		return !scroll.isEmpty() || !reagent.isEmpty();
	}
}
