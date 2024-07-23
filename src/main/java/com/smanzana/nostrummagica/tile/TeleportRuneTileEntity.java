package com.smanzana.nostrummagica.tile;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.tile.IOrientedTileEntity;
import com.smanzana.autodungeons.world.blueprints.IBlueprint;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.NostrumMagica.NostrumTeleportEvent;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.util.WorldUtil;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants.NBT;

public class TeleportRuneTileEntity extends TileEntity implements IOrientedTileEntity, ITickableTileEntity {
	
	private static final String NBT_OFFSET = "offset";
	
	// How long entities must wait in the teleporation block before they teleport
	public static final int TELEPORT_CHARGE_TIME = 2;
	protected static final Map<UUID, Integer> EntityChargeMap = new HashMap<>();
	
	private BlockPos teleOffset = null;
	
	public TeleportRuneTileEntity() {
		super(NostrumTileEntities.TeleportRuneTileEntityType);
	}
	
	/**
	 * Sets where to teleport to as an offset from the block itself.
	 * OFFSET, not target location. Went ahead and used ints here to make it more obvious.
	 * @param offsetX
	 * @param offsetY
	 * @param offsetZ
	 */
	public void setOffset(int offsetX, int offsetY, int offsetZ, boolean isWorldGen) {
		this.teleOffset = new BlockPos(offsetX, offsetY, offsetZ);
		flush(isWorldGen);
	}
	
	public void setOffset(int offsetX, int offsetY, int offsetZ) {
		setOffset(offsetX, offsetY, offsetZ, false);
	}
	
	public void setTargetPosition(BlockPos target, boolean isWorldGen) {
		if (target == null) {
			this.teleOffset = null;
		} else {
			this.teleOffset = target.subtract(pos);
		}
		flush(isWorldGen);
	}
	
	public void setTargetPosition(BlockPos target) {
		setTargetPosition(target, false);
	}
	
	public @Nullable BlockPos getOffset() {
		return teleOffset;
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
		handleUpdateTag(this.getBlockState(), pkt.getNbtCompound());
	}
	
	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		
		if (teleOffset != null) {
			compound.put(NBT_OFFSET, NBTUtil.writeBlockPos(teleOffset));
		}
		
		return compound;
	}
	
	public void read(BlockState state, CompoundNBT compound) {
		super.read(state, compound);
		
		teleOffset = null;
		if (compound.contains(NBT_OFFSET, NBT.TAG_LONG)) {
			// Legacy format! Probably dungeon spawning
			teleOffset = WorldUtil.blockPosFromLong1_12_2(compound.getLong(NBT_OFFSET));
		} else {
			teleOffset = NBTUtil.readBlockPos(compound.getCompound(NBT_OFFSET));
		}
	}
	
	protected void flush(boolean isWorldGen) {
		if (!isWorldGen && world != null && !world.isRemote) {
			BlockState state = world.getBlockState(pos);
			world.notifyBlockUpdate(pos, state, state, 2);
		}
	}
	
	@Override
	public void setSpawnedFromRotation(Direction rotation, boolean isWorldGen) {
		BlockPos orig = this.getOffset();
		if (orig != null) {
			BlockPos out = IBlueprint.ApplyRotation(this.getOffset(), rotation);
			this.setOffset(out.getX(), out.getY(), out.getZ(), isWorldGen);
		} else {
			System.out.println("Null offset?");
		}
	}
	
	@Override
	public void tick() {
		if (world != null && !world.isRemote()) {
			for (Entity ent : scanForEntities()) {
				entityOnTileTick(ent);
			}
		}
	}
	
	protected Collection<Entity> scanForEntities() {
		return world.getEntitiesInAABBexcluding(null, VoxelShapes.fullCube().getBoundingBox().offset(pos), (e) -> { return true; });
	}
	
	protected void entityOnTileTick(Entity entity) {
		// If no charge yet, play startup effects
		if (!hasEntityCharge(entity)) {
			world.playSound(null, pos, SoundEvents.BLOCK_PORTAL_TRIGGER, SoundCategory.BLOCKS, 1f, (4f / (float) TELEPORT_CHARGE_TIME));
		}
		
		incrEntityCharge(entity);
		final int charge = getEntityCharge(entity);
		if (charge >= TELEPORT_CHARGE_TIME * 20) {
			doTeleport(entity);
		} else if (charge > 0) {
			int count = (charge / 20) / TELEPORT_CHARGE_TIME;
			final double rx = NostrumMagica.rand.nextFloat() - .5f;
			final double rz = NostrumMagica.rand.nextFloat() - .5f;
			
			((ServerWorld) world).spawnParticle(ParticleTypes.DRAGON_BREATH, pos.getX() + .5 + rx, pos.getY(), pos.getZ() + .5 + rz, count,
					0, .25, 0, NostrumMagica.rand.nextFloat());
		}
	}
	
	public void doTeleport(Entity entity) {
		 teleportEntity(entity);
		 setEntityInCooldown(entity);
	}
	
	protected void teleportEntity(Entity entityIn) {
		BlockPos offset = getOffset();
		if (offset == null) {
			return;
		}
		
		BlockPos target = pos.add(offset);
		
		NostrumTeleportEvent event = NostrumMagica.fireTeleportAttemptEvent(entityIn, target.getX() + .5, target.getY() + .1, target.getZ() + .5, null);
		if (!event.isCanceled()) {
		
			entityIn.lastTickPosX = entityIn.prevPosX = target.getX() + .5;
			entityIn.lastTickPosY = entityIn.prevPosY = target.getY() + .005;
			entityIn.lastTickPosZ = entityIn.prevPosZ = target.getZ() + .5;
			
			if (!world.isRemote) {
				NostrumMagica.playerListener.registerTimer((type, entity, data) -> {
					//Event type, LivingEntity entity, T data
					entityIn.setPositionAndUpdate(target.getX() + .5, target.getY() + .005, target.getZ() + .5);
		
					double dx = target.getX() + .5;
					double dy = target.getY() + 1;
					double dz = target.getZ() + .5;
					for (int i = 0; i < 10; i++) {
						
						((ServerWorld) world).spawnParticle(ParticleTypes.DRAGON_BREATH,
								dx,
								dy,
								dz,
								10,
								.25,
								.6,
								.25,
								.1
								);
					}
					NostrumMagicaSounds.DAMAGE_ENDER.play(world, dx, dy, dz);
					return true;
				}, 1, 0);
			}
		}
	}
	
	protected static final void setEntityInCooldown(Entity ent) {
		synchronized(EntityChargeMap) {
			EntityChargeMap.put(ent.getUniqueID(), -40);
		}
	}
	
	protected static final void incrEntityCharge(Entity ent) {
		synchronized(EntityChargeMap) {
			final Integer charge = EntityChargeMap.get(ent.getUniqueID());
			if (charge != null && charge < 0) {
				// in cooldown, so avoid doing anything and let it come back up at the same pace
			} else {
				final int newCharge = (charge == null ? 0 : charge) + 2; // 2 because every tick we decay
				EntityChargeMap.put(ent.getUniqueID(), newCharge);
			}
		}
	}
	
	protected static final boolean hasEntityCharge(Entity ent) {
		synchronized(EntityChargeMap) {
			return EntityChargeMap.containsKey(ent.getUniqueID());
		}
	}
	
	protected static final int getEntityCharge(Entity ent) {
		synchronized(EntityChargeMap) {
			final Integer val = EntityChargeMap.get(ent.getUniqueID());
			return val == null ? 0 : val;
		}
	}
	
	public static final void clearChargeMap() {
		synchronized(EntityChargeMap) {
			
		}
		EntityChargeMap.clear();
	}
	
	public static final void tickChargeMap() {
		synchronized(EntityChargeMap) {
			Iterator<UUID> it = EntityChargeMap.keySet().iterator();
			while (it.hasNext()) {
				UUID key = it.next();
				final Integer charge = EntityChargeMap.get(key);
				if (charge == null || (charge == 1 || charge == -1)) {
					it.remove();
				} else {
					EntityChargeMap.put(key, charge + (charge > 0 ? -1 : 1));
				}
			}
		}
	}
}