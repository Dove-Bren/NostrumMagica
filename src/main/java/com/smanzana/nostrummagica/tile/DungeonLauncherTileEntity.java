package com.smanzana.nostrummagica.tile;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.PutterBlock;
import com.smanzana.nostrummagica.entity.MagicDamageProjectileEntity;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.entity.TileProxyTriggerEntity;
import com.smanzana.nostrummagica.item.InfusedGemItem;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.util.Inventories;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TippedArrowItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

public class DungeonLauncherTileEntity extends BlockEntity implements TickableBlockEntity {

	private static final String NBT_INVENTORY = "inventory";
	protected static final int DEFAULT_COOLDOWN = 20 * 3;
	
	private final SimpleContainer inventory;
	
	private int cooldownTicks;
	private int ticksExisted;
	
	public DungeonLauncherTileEntity(BlockPos pos, BlockState state) {
		super(NostrumTileEntities.DungeonLauncherTileType, pos, state);
		this.inventory = new SimpleContainer(9) {
			@Override
			public void setChanged() {
				DungeonLauncherTileEntity.this.setChanged();
			}
		};
		cooldownTicks = 0;
	}
	
	public Container getInventory() {
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
	
	protected boolean canSeeEntity(LivingEntity entity) {
		return !entity.isSpectator() && (entity instanceof Player || entity instanceof Mob) && !(entity instanceof TileProxyTriggerEntity);
	}
	
	protected boolean checkForEntity() {
		Direction direction = level.getBlockState(this.worldPosition).getValue(PutterBlock.FACING);
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
		List<LivingEntity> ents = level.getEntitiesOfClass(LivingEntity.class, Shapes.block().bounds().move(worldPosition).expandTowards(dx, dy, dz).inflate(gx, gy, gz), this::canSeeEntity);
		return ents != null && !ents.isEmpty();
	}
	
	protected void setCooldown() {
		setCooldown(DEFAULT_COOLDOWN);
	}
	
	protected void setCooldown(int cooldown) {
		this.cooldownTicks = this.ticksExisted + cooldown;
	}
	
	protected Vec3 getFirePos() {
		final float startDistance = 1f;
		final BlockPos pos = this.getBlockPos();
		final Direction direction = level.getBlockState(pos).getValue(PutterBlock.FACING);
		switch (direction) {
		case DOWN:
			return new Vec3(pos.getX() + .5, pos.getY() - startDistance, pos.getZ() + .5);
		case EAST:
		default:
			return new Vec3(pos.getX() + 1 + startDistance, pos.getY() + .5, pos.getZ() + .5);
		case NORTH:
			return new Vec3(pos.getX() + .5, pos.getY() + .5, pos.getZ() - startDistance);
		case SOUTH:
			return new Vec3(pos.getX() + .5, pos.getY() + .5, pos.getZ() + 1 + startDistance);
		case UP:
			return new Vec3(pos.getX() + .5, pos.getY() + 1 + startDistance, pos.getZ() + .5);
		case WEST:
			return new Vec3(pos.getX() - startDistance, pos.getY() + .5, pos.getZ() + .5);
		}
	}
	
	protected float getFireSpeed(Projectile projectile) {
		float speed = 1.4f; // base
		if (projectile instanceof MagicDamageProjectileEntity) {
			// Change speed based on element
			switch (((MagicDamageProjectileEntity) projectile).getElement()) {
			case PHYSICAL:
			default:
				; // No change
				break;
			case WIND:
			case ENDER:
				speed += .2f; // faster
				break;
			case FIRE:
			case LIGHTNING:
				speed -= .2f; // slower
				break;
			case EARTH:
			case ICE:
				speed -= .4f; // very slow
				break;
			}
		}
		return speed;
	}
	
	protected float getFireInaccuracy(Projectile projectile) {
		return 1f;
	}
	
	protected void fire() {
		final Vec3 source = getFirePos();
		Projectile projectile = this.makeProjectile(getLevel(), source.x(), source.y(), source.z());
		if (projectile != null) {
			final Vec3 direction = source.subtract(Vec3.atCenterOf(this.getBlockPos())).normalize();
			projectile.shoot(direction.x(), direction.y(), direction.z(), getFireSpeed(projectile), getFireInaccuracy(projectile));
			getLevel().addFreshEntity(projectile);
			playFireEffect(projectile, direction);
		}
	}
	
	protected void playFireEffect(Projectile projectile, Vec3 direction) {
		projectile.level.playSound(null, this.getBlockPos(), SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1f, 1f);
		((ServerLevel) projectile.level).sendParticles(ParticleTypes.SMOKE, projectile.getX(), projectile.getY(), projectile.getZ(),
				10, direction.x(), direction.y(), direction.z(), .2f);
	}
	
	protected @Nullable Projectile makeProjectile(Level world, double x, double y, double z) {
		ItemStack stack = getRandomHeldItem();
		if (stack.isEmpty()) {
			return null;
		}
		
		if (stack.getItem() instanceof InfusedGemItem) {
			EMagicElement element = InfusedGemItem.GetElement(stack);
			return makeElementalProjectile(world, x, y, z, element);
		} else if (stack.getItem() instanceof TippedArrowItem) {
			Arrow arrow = new Arrow(world, x, y, z);
			arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
			arrow.setEffectsFromItem(stack);
			return arrow; 
		} else if (stack.getItem() instanceof ArrowItem) {
			Arrow arrow = new Arrow(world, x, y, z);
			arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
			return arrow;
		}
		
		NostrumMagica.logger.debug("Could not decide how to launch projectile for: " + stack.getItem());
		return null;
	}
	
	protected MagicDamageProjectileEntity makeElementalProjectile(Level world, double x, double y, double z, EMagicElement element) {
		MagicDamageProjectileEntity proj = new MagicDamageProjectileEntity(NostrumEntityTypes.magicDamageProjectile, world);
		proj.setPos(x, y, z);
		proj.setElement(element);
		proj.setDamage(4f);
		return proj;
	}
	
	protected ItemStack getRandomHeldItem() {
		List<ItemStack> heldItems = new ArrayList<>(9);
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack stack = inventory.getItem(i);
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
	public CompoundTag save(CompoundTag nbt) {
		nbt = super.save(nbt);
		
		nbt.put(NBT_INVENTORY, Inventories.serializeInventory(inventory));
		
		return nbt;
	}
	
	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		
		if (nbt == null)
			return;
		
		Inventories.deserializeInventory(inventory, nbt.get(NBT_INVENTORY));
	}

	@Override
	public void tick() {
		ticksExisted++;
		if (level == null || level.isClientSide) {
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

	public boolean trigger() {
		//if (canFire()) {
			fire();
			setCooldown();
			return true;
		//}
		
		//return false;
	}
}