package com.smanzana.nostrummagica.tile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.block.ParadoxMirrorBlock;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.util.TargetLocation;
import com.smanzana.nostrummagica.util.WorldUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ParadoxMirrorTileEntity extends BlockEntity implements TickableBlockEntity/*, IAetherInfusableTileEntity*/ {
	
	private @Nullable BlockPos linkedPosition;
	
	// Transient list of EntityItems we've created when receiving items so we don't keep sending
	// them back and forth. Just uses regular equality.
	private List<ItemEntity> receivedEntities;
	
	// Transient display variables
	private int cooldownTicks;
	
	public ParadoxMirrorTileEntity(BlockPos pos, BlockState state) {
		super(NostrumBlockEntities.ParadoxMirror, pos, state);
		cooldownTicks = 0;
		receivedEntities = new ArrayList<>();
	}
	
	public @Nullable BlockPos getLinkedPosition() {
		return linkedPosition;
	}
	
	public void setLinkedPosition(@Nullable BlockPos linkedPosition) {
		this.linkedPosition = linkedPosition;
		dirty();
	}
	
	public boolean isInCooldown() {
		return cooldownTicks > 0;
	}
	
	public void setCooldown(int ticks) {
		this.cooldownTicks = ticks;
	}
	
	public Direction getFacing() {
		BlockState state = getLevel().getBlockState(getBlockPos());
		return state.getValue(ParadoxMirrorBlock.FACING);
	}
	
	private static final String NBT_LINKED_POS = "linked_pos";
	
	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		if (linkedPosition != null) {
			nbt.put(NBT_LINKED_POS, NbtUtils.writeBlockPos(linkedPosition));
		}
		
		// Could save and load cooldown but client won't use it and I don't care if people save/load over and over to transfer fast
	}
	
	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		
		if (nbt == null)
			return;
			
		if (nbt.contains(NBT_LINKED_POS, Tag.TAG_LONG)) {
			// Legacy!
			linkedPosition = WorldUtil.blockPosFromLong1_12_2(nbt.getLong(NBT_LINKED_POS));
		} else if (nbt.contains(NBT_LINKED_POS)) {
			linkedPosition = NbtUtils.readBlockPos(nbt.getCompound(NBT_LINKED_POS));
		} else {
			linkedPosition = null;
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
	}
	
	private void dirty() {
		level.sendBlockUpdated(worldPosition, this.level.getBlockState(worldPosition), this.level.getBlockState(worldPosition), 3);
		setChanged();
	}

	@Override
	public void tick() {
		if (level != null && !level.isClientSide) {
			checkItemEntityList(); // every tick
			
			if (cooldownTicks > 0) {
				this.cooldownTicks--;
			}
			
			if (!isInCooldown()) {
				if (grabAndSend()) {
					this.setCooldown(20 * 3);
				}
			}
		}
	}
	
	protected @Nullable ParadoxMirrorTileEntity getLinkedMirror() {
		BlockPos pos = this.getLinkedPosition(); 
		if (pos == null) {
			return null;
		}
		
		if (!NostrumMagica.isBlockLoaded(getLevel(), pos)) {
			return null;
		}
		
		BlockState blockstate = getLevel().getBlockState(pos);
		if (blockstate == null || blockstate.getBlock() != NostrumBlocks.paradoxMirror) {
			return null;
		}
		
		BlockEntity te = getLevel().getBlockEntity(pos);
		if (te == null || !(te instanceof ParadoxMirrorTileEntity)) {
			return null;
		}
		
		
		return (ParadoxMirrorTileEntity) te;
	}
	
	protected boolean grabAndSend() {
		@Nullable ParadoxMirrorTileEntity remoteTile = getLinkedMirror();
		if (remoteTile == null) {
			return false;
		}
		
		@Nullable ItemEntity entity = findNearbyItem();
		if (entity == null) {
			return false;
		}
		
		// Have item, cooldown is good, and are linked! Consume and send!
		@Nonnull ItemStack stack = entity.getItem();
		entity.discard();
		
		remoteTile.receiveAndSpawnItem(stack, this.getBlockPos());
		return true;
	}
	
	protected void checkItemEntityList() {
		Iterator<ItemEntity> it = receivedEntities.iterator();
		while (it.hasNext()) {
			ItemEntity ent = it.next();
			if (ent == null || !ent.isAlive() || ent.distanceToSqr(worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5) > 4) {
				it.remove();
			}
		}
	}
	
	public void receiveAndSpawnItem(@Nonnull ItemStack stack, BlockPos fromPos) {
		Vec3 spawnLoc = getSpawnLocation();
		Vec3 spawnVelocity = getSpawnVelocity();
		ItemEntity entity = new ItemEntity(getLevel(), spawnLoc.x, spawnLoc.y, spawnLoc.z, stack);
		entity.setDeltaMovement(spawnVelocity);
		entity.hurtMarked = true;
		
		receivedEntities.add(entity);
		getLevel().addFreshEntity(entity);
		
		playReceiveEffect(entity, fromPos);
	}
	
	protected Vec3 getSpawnLocation() {
		BlockPos pos = this.getBlockPos();
		double x = pos.getX() + .5;
		double y = pos.getY() + .4;
		double z = pos.getZ() + .5;
		
		// Adjust so item starts closer to mirror
		final double offset = .25;
		switch (this.getFacing()) {
		case DOWN:
		case UP:
		default:
		case NORTH: 
			z += offset;
			break;
		case EAST:
			x -= offset;
			break;
		case SOUTH:
			z -= offset;
			break;
		case WEST:
			x += offset;
			break;
		
		}
		
		return new Vec3(x, y, z);
	}
	
	protected Vec3 getSpawnVelocity() {
		return Vec3.atLowerCornerOf(getFacing().getNormal()).scale(.1);
	}
	
	protected @Nullable ItemEntity findNearbyItem() {
		AABB bb = new AABB(0, 0, 0, 1, 1, 1).move(this.getBlockPos());
		for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, bb)) {
			// Make sure entity isn't in the list of entities we've created
			if (receivedEntities.contains(entity)) {
				continue;
			}
			
			// Try to filter out deleted entities (like ones sucked up by hoppers)
			// or ones with empty itemstacks
			if (!entity.isAlive()
					|| entity.getItem() == null
					|| entity.getItem().isEmpty()
					) {
				continue;
			}
			
			return entity;
		}
		
		return null;
	}
	
	public boolean tryPushItem(@Nonnull ItemStack stack) {
		if (this.isInCooldown()) {
			return false;
		}
		
		@Nullable ParadoxMirrorTileEntity remoteTile = getLinkedMirror();
		if (remoteTile == null) {
			return false;
		}
		
		remoteTile.receiveAndSpawnItem(stack, this.getBlockPos());
		return true;
	}
	
	protected void playReceiveEffect(ItemEntity entity, BlockPos fromPos) {
		if (level.isClientSide) {
			return;
		}
		
		Vec3 spawnPos = this.getSpawnLocation();
		
		NostrumParticles.GLOW_ORB.spawn(getLevel(), new SpawnParams(
				2, // spawn count
				spawnPos.x, spawnPos.y, spawnPos.z, // spawn position (center)
				.2, // spawn position jitter
				40, 20, // lifetime (base + jitter)
				new TargetLocation(entity, true) // entity id to follow
				).color(EMagicElement.ENDER.getColor()));
		
//		NostrumParticles.GLOW_ORB.spawn(getWorld(), new SpawnParams(
//				5, // spawn count
//				spawnPos.x, spawnPos.y, spawnPos.z, // spawn position (center)
//				.1, // spawn position jitter
//				30, 20, // lifetime (base + jitter)
//				getSpawnVelocity().scale(.7), new Vector3d(.05, .05, .05)
//				).color(EMagicElement.ENDER.getColor())
//				.gravity(-.025f));
	}
	
}