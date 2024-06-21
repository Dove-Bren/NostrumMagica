package com.smanzana.nostrummagica.tile;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.PutterBlock;
import com.smanzana.nostrummagica.entity.MagicDamageProjectileEntity;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.item.InfusedGemItem;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.util.Inventories;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TippedArrowItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class DungeonLauncherTileEntity extends TileEntity implements ITickableTileEntity {

	private static final String NBT_INVENTORY = "inventory";
	protected static final int DEFAULT_COOLDOWN = 20 * 3;
	
	private final Inventory inventory;
	
	private int cooldownTicks;
	private int ticksExisted;
	
	public DungeonLauncherTileEntity() {
		super(NostrumTileEntities.DungeonLauncherTileType);
		this.inventory = new Inventory(9) {
			@Override
			public void markDirty() {
				DungeonLauncherTileEntity.this.markDirty();
			}
		};
		cooldownTicks = 0;
	}
	
	public IInventory getInventory() {
		return inventory;
	}
	
	public int getCooldownTicksRemaining() {
		return Math.max(0, cooldownTicks - ticksExisted);
	}
	
	public boolean canFire() {
		return getCooldownTicksRemaining() == 0;
	}
	
	protected boolean isDisabled() {
		return false; // Could let redstone or triggers disable it. Child classes might want to?
	}
	
	protected int getRange() {
		return 10;
	}
	
	protected boolean checkForEntity() {
		Direction direction = world.getBlockState(this.pos).get(PutterBlock.FACING);
		final int range = getRange();
		int dx = 0;
		int dy = 0;
		int dz = 0;
		int gx = 0;
		int gy = 0;
		int gz = 0;
		switch (direction) {
		case DOWN:
			dy = -range;
			gx = 1;
			gz = 1;
			break;
		case EAST:
		default:
			dx = range;
			gy = 1;
			gz = 1;
			break;
		case NORTH:
			dz = -range;
			gx = 1;
			gy = 1;
			break;
		case SOUTH:
			dz = range;
			gx = 1;
			gy = 1;
			break;
		case UP:
			dy = range;
			gx = 1;
			gz = 1;
			break;
		case WEST:
			dx = -range;
			gy = 1;
			gz = 1;
			break;
		}
		List<LivingEntity> ents = world.getEntitiesWithinAABB(LivingEntity.class, VoxelShapes.fullCube().getBoundingBox().offset(pos).expand(dx, dy, dz).grow(gx, gy, gz));
		return ents != null && !ents.isEmpty();
	}
	
	protected void setCooldown() {
		setCooldown(DEFAULT_COOLDOWN);
	}
	
	protected void setCooldown(int cooldown) {
		this.cooldownTicks = this.ticksExisted + cooldown;
	}
	
	protected Vector3d getFirePos() {
		final float startDistance = 1f;
		final BlockPos pos = this.getPos();
		final Direction direction = world.getBlockState(pos).get(PutterBlock.FACING);
		switch (direction) {
		case DOWN:
			return new Vector3d(pos.getX() + .5, pos.getY() - startDistance, pos.getZ() + .5);
		case EAST:
		default:
			return new Vector3d(pos.getX() + 1 + startDistance, pos.getY() + .5, pos.getZ() + .5);
		case NORTH:
			return new Vector3d(pos.getX() + .5, pos.getY() + .5, pos.getZ() - startDistance);
		case SOUTH:
			return new Vector3d(pos.getX() + .5, pos.getY() + .5, pos.getZ() + 1 + startDistance);
		case UP:
			return new Vector3d(pos.getX() + .5, pos.getY() + 1 + startDistance, pos.getZ() + .5);
		case WEST:
			return new Vector3d(pos.getX() - startDistance, pos.getY() + .5, pos.getZ() + .5);
		}
	}
	
	protected float getFireSpeed(ProjectileEntity projectile) {
		return 1.4f;
	}
	
	protected float getFireInaccuracy(ProjectileEntity projectile) {
		return 1f;
	}
	
	protected void fire() {
		final Vector3d source = getFirePos();
		ProjectileEntity projectile = this.makeProjectile(getWorld(), source.getX(), source.getY(), source.getZ());
		if (projectile != null) {
			final Vector3d direction = source.subtract(Vector3d.copyCentered(this.getPos())).normalize();
			projectile.shoot(direction.getX(), direction.getY(), direction.getZ(), getFireSpeed(projectile), getFireInaccuracy(projectile));
			getWorld().addEntity(projectile);
		}
	}
	
	protected @Nullable ProjectileEntity makeProjectile(World world, double x, double y, double z) {
		ItemStack stack = getRandomHeldItem();
		if (stack.isEmpty()) {
			return null;
		}
		
		if (stack.getItem() instanceof InfusedGemItem) {
			EMagicElement element = InfusedGemItem.GetElement(stack);
			return makeElementalProjectile(world, x, y, z, element);
		} else if (stack.getItem() instanceof TippedArrowItem) {
			ArrowEntity arrow = new ArrowEntity(world, x, y, z);
			arrow.pickupStatus = AbstractArrowEntity.PickupStatus.DISALLOWED;
			arrow.setPotionEffect(stack);
			return arrow; 
		} else if (stack.getItem() instanceof ArrowItem) {
			ArrowEntity arrow = new ArrowEntity(world, x, y, z);
			arrow.pickupStatus = AbstractArrowEntity.PickupStatus.DISALLOWED;
			return arrow;
		}
		
		NostrumMagica.logger.debug("Could not decide how to launch projectile for: " + stack.getItem());
		return null;
	}
	
	protected MagicDamageProjectileEntity makeElementalProjectile(World world, double x, double y, double z, EMagicElement element) {
		MagicDamageProjectileEntity proj = new MagicDamageProjectileEntity(NostrumEntityTypes.magicDamageProjectile, world);
		proj.setPosition(x, y, z);
		proj.setElement(element);
		proj.setDamage(4f);
		return proj;
	}
	
	protected ItemStack getRandomHeldItem() {
		List<ItemStack> heldItems = new ArrayList<>(9);
		for (int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack.isEmpty()) {
				continue;
			}
			heldItems.add(stack);
		}
		
		if (heldItems.isEmpty()) {
			return ItemStack.EMPTY;
		} else {
			return heldItems.get(NostrumMagica.rand.nextInt(heldItems.size()));
		}
	}
	
	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		nbt = super.write(nbt);
		
		nbt.put(NBT_INVENTORY, Inventories.serializeInventory(inventory));
		
		return nbt;
	}
	
	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		
		if (nbt == null)
			return;
		
		Inventories.deserializeInventory(inventory, nbt.get(NBT_INVENTORY));
	}

	@Override
	public void tick() {
		ticksExisted++;
		if (world == null || world.isRemote) {
			return;
		}
		
		if (isDisabled()) {
			return;
		}
		
		if (!canFire()) {
			return;
		}
		
		if (checkForEntity()) {
			// Fire!
			fire();
			setCooldown();
		}
	}
}