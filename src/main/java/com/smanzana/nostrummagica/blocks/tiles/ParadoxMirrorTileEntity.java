package com.smanzana.nostrummagica.blocks.tiles;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.IAetherInfusableTileEntity;
import com.smanzana.nostrummagica.blocks.ParadoxMirrorBlock;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.integration.aetheria.blocks.AetherInfuserTileEntity;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.VoxelShape;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants.NBT;

public class ParadoxMirrorTileEntity extends TileEntity implements ITickableTileEntity, IAetherInfusableTileEntity {
	
	private @Nullable BlockPos linkedPosition;
	
	// Transient list of EntityItems we've created when receiving items so we don't keep sending
	// them back and forth. Just uses regular equality.
	private List<ItemEntity> receivedEntities;
	
	// Transient display variables
	private int cooldownTicks;
	
	public ParadoxMirrorTileEntity() {
		super();
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
		BlockState state = getWorld().getBlockState(getPos());
		return state.get(ParadoxMirrorBlock.FACING);
	}
	
	private static final String NBT_LINKED_POS = "linked_pos";
	
	@Override
	public CompoundNBT writeToNBT(CompoundNBT nbt) {
		nbt = super.writeToNBT(nbt);
		
		if (linkedPosition != null) {
			nbt.putLong(NBT_LINKED_POS, linkedPosition.toLong());
		}
		
		// Could save and load cooldown but client won't use it and I don't care if people save/load over and over to transfer fast
		
		return nbt;
	}
	
	@Override
	public void readFromNBT(CompoundNBT nbt) {
		super.readFromNBT(nbt);
		
		if (nbt == null)
			return;
			
		if (!nbt.contains(NBT_LINKED_POS, NBT.TAG_LONG)) {
			linkedPosition = null;
		} else {
			linkedPosition = BlockPos.fromLong(nbt.getLong(NBT_LINKED_POS));
		}
	}
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, 3, this.getUpdateTag());
	}

	@Override
	public CompoundNBT getUpdateTag() {
		return this.writeToNBT(new CompoundNBT());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		super.onDataPacket(net, pkt);
		handleUpdateTag(pkt.getNbtCompound());
	}
	
	private void dirty() {
		world.markBlockRangeForRenderUpdate(pos, pos);
		world.notifyBlockUpdate(pos, this.world.getBlockState(pos), this.world.getBlockState(pos), 3);
		world.scheduleBlockUpdate(pos, this.getBlockType(),0,0);
		markDirty();
	}

	@Override
	public boolean canAcceptAetherInfuse(AetherInfuserTileEntity source, int maxAether) {
		return this.isInCooldown();
	}

	@Override
	public int acceptAetherInfuse(AetherInfuserTileEntity source, int maxAether) {
		
		// take one aether point and reduce time by up to 4 ticks.  That means -5 ticks per tick or 5x as fast
		if (maxAether > 0) {
			maxAether--;
			this.cooldownTicks -= Math.min(cooldownTicks, 4);
		}
		
		return maxAether;
	}
	
	@Override
	public void tick() {
		if (world != null && !world.isRemote) {
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
		
		if (!NostrumMagica.isBlockLoaded(getWorld(), pos)) {
			return null;
		}
		
		BlockState blockstate = getWorld().getBlockState(pos);
		if (blockstate == null || blockstate.getBlock() != ParadoxMirrorBlock.instance()) {
			return null;
		}
		
		TileEntity te = getWorld().getTileEntity(pos);
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
		entity.setDead();
		
		remoteTile.receiveAndSpawnItem(stack, this.getPos());
		return true;
	}
	
	protected void checkItemEntityList() {
		Iterator<ItemEntity> it = receivedEntities.iterator();
		while (it.hasNext()) {
			ItemEntity ent = it.next();
			if (ent == null || ent.isDead || ent.getDistanceSqToCenter(getPos()) > 4) {
				it.remove();
			}
		}
	}
	
	protected void receiveAndSpawnItem(@Nonnull ItemStack stack, BlockPos fromPos) {
		Vec3d spawnLoc = getSpawnLocation();
		Vec3d spawnVelocity = getSpawnVelocity();
		ItemEntity entity = new ItemEntity(getWorld(), spawnLoc.x, spawnLoc.y, spawnLoc.z, stack);
		entity.getMotion().x = spawnVelocity.x;
		entity.getMotion().y = spawnVelocity.y;
		entity.getMotion().z = spawnVelocity.z;
		entity.velocityChanged = true;
		
		receivedEntities.add(entity);
		getWorld().addEntity(entity);
		
		playReceiveEffect(entity, fromPos);
	}
	
	protected Vec3d getSpawnLocation() {
		BlockPos pos = this.getPos();
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
		
		return new Vec3d(x, y, z);
	}
	
	protected Vec3d getSpawnVelocity() {
		return new Vec3d(getFacing().getDirectionVec()).scale(.1);
	}
	
	protected @Nullable ItemEntity findNearbyItem() {
		VoxelShape bb = Block.makeCuboidShape(0, 0, 0, 1, 1, 1).offset(this.getPos());
		for (ItemEntity entity : world.getEntitiesWithinAABB(ItemEntity.class, bb)) {
			// Make sure entity isn't in the list of entities we've created
			if (receivedEntities.contains(entity)) {
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
		
		remoteTile.receiveAndSpawnItem(stack, this.getPos());
		return true;
	}
	
	protected void playReceiveEffect(ItemEntity entity, BlockPos fromPos) {
		if (getWorld().isRemote) {
			return;
		}
		
		Vec3d spawnPos = this.getSpawnLocation();
		
		NostrumParticles.GLOW_ORB.spawn(getWorld(), new SpawnParams(
				2, // spawn count
				spawnPos.x, spawnPos.y, spawnPos.z, // spawn position (center)
				.2, // spawn position jitter
				40, 20, // lifetime (base + jitter)
				entity.getEntityId() // entity id to follow
				).color(EMagicElement.ENDER.getColor()));
		
//		NostrumParticles.GLOW_ORB.spawn(getWorld(), new SpawnParams(
//				5, // spawn count
//				spawnPos.x, spawnPos.y, spawnPos.z, // spawn position (center)
//				.1, // spawn position jitter
//				30, 20, // lifetime (base + jitter)
//				getSpawnVelocity().scale(.7), new Vec3d(.05, .05, .05)
//				).color(EMagicElement.ENDER.getColor())
//				.gravity(-.025f));
	}
	
}