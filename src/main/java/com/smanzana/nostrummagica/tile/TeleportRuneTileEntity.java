package com.smanzana.nostrummagica.tile;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.api.block.entity.IOrientedTileEntity;
import com.smanzana.autodungeons.util.WorldUtil;
import com.smanzana.autodungeons.world.blueprints.IBlueprint;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.NostrumMagica.NostrumTeleportEvent;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;

public class TeleportRuneTileEntity extends BlockEntity implements IOrientedTileEntity, TickableBlockEntity {
	
	private static final String NBT_OFFSET = "offset";
	
	// How long entities must wait in the teleporation block before they teleport
	public static final int TELEPORT_CHARGE_TIME = 2;
	protected static final Map<UUID, Integer> EntityChargeMap = new HashMap<>();
	
	private BlockPos teleOffset = null;
	
	public TeleportRuneTileEntity(BlockPos pos, BlockState state) {
		super(NostrumBlockEntities.TeleportRune, pos, state);
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
		this.setChanged();
		flush(isWorldGen);
	}
	
	public void setOffset(int offsetX, int offsetY, int offsetZ) {
		setOffset(offsetX, offsetY, offsetZ, false);
	}
	
	public void setTargetPosition(BlockPos target, boolean isWorldGen) {
		if (target == null) {
			this.teleOffset = null;
		} else {
			this.teleOffset = target.subtract(worldPosition);
		}
		this.setChanged();
		flush(isWorldGen);
	}
	
	public void setTargetPosition(BlockPos target) {
		setTargetPosition(target, false);
	}
	
	public @Nullable BlockPos getOffset() {
		return teleOffset;
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
	
	@Override
	public void saveAdditional(CompoundTag compound) {
		super.saveAdditional(compound);
		
		if (teleOffset != null) {
			compound.put(NBT_OFFSET, NbtUtils.writeBlockPos(teleOffset));
		}
	}
	
	@Override
	public void load(CompoundTag compound) {
		super.load(compound);
		
		teleOffset = null;
		if (compound.contains(NBT_OFFSET, Tag.TAG_LONG)) {
			// Legacy format! Probably dungeon spawning
			teleOffset = WorldUtil.blockPosFromLong1_12_2(compound.getLong(NBT_OFFSET));
		} else {
			teleOffset = NbtUtils.readBlockPos(compound.getCompound(NBT_OFFSET));
		}
	}
	
	protected void flush(boolean isWorldGen) {
		if (!isWorldGen && level != null && !level.isClientSide) {
			BlockState state = level.getBlockState(worldPosition);
			level.sendBlockUpdated(worldPosition, state, state, 2);
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
		if (level != null && !level.isClientSide()) {
			for (Entity ent : scanForEntities()) {
				entityOnTileTick(ent);
			}
		}
	}
	
	protected Collection<Entity> scanForEntities() {
		return level.getEntities((Entity) null, Shapes.block().bounds().move(worldPosition), (e) -> { return true; });
	}
	
	protected void entityOnTileTick(Entity entity) {
		// If no charge yet, play startup effects
		if (!hasEntityCharge(entity)) {
			level.playSound(null, worldPosition, SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 1f, (4f / (float) TELEPORT_CHARGE_TIME));
		}
		
		incrEntityCharge(entity);
		final int charge = getEntityCharge(entity);
		if (charge >= TELEPORT_CHARGE_TIME * 20) {
			doTeleport(entity);
		} else if (charge > 0) {
			int count = (charge / 20) / TELEPORT_CHARGE_TIME;
			final double rx = NostrumMagica.rand.nextFloat() - .5f;
			final double rz = NostrumMagica.rand.nextFloat() - .5f;
			
			((ServerLevel) level).sendParticles(ParticleTypes.DRAGON_BREATH, worldPosition.getX() + .5 + rx, worldPosition.getY(), worldPosition.getZ() + .5 + rz, count,
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
		
		BlockPos target = worldPosition.offset(offset);
		
		NostrumTeleportEvent event = NostrumMagica.fireTeleportAttemptEvent(entityIn, target.getX() + .5, target.getY() + .1, target.getZ() + .5, null);
		if (!event.isCanceled()) {
		
			entityIn.xOld = entityIn.xo = target.getX() + .5;
			entityIn.yOld = entityIn.yo = target.getY() + .005;
			entityIn.zOld = entityIn.zo = target.getZ() + .5;
			
			if (!level.isClientSide) {
				NostrumMagica.playerListener.registerTimer((type, entity, data) -> {
					//Event type, LivingEntity entity, T data
					entityIn.teleportTo(target.getX() + .5, target.getY() + .005, target.getZ() + .5);
		
					double dx = target.getX() + .5;
					double dy = target.getY() + 1;
					double dz = target.getZ() + .5;
					for (int i = 0; i < 10; i++) {
						
						((ServerLevel) level).sendParticles(ParticleTypes.DRAGON_BREATH,
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
					NostrumMagicaSounds.DAMAGE_ENDER.play(level, dx, dy, dz);
					return true;
				}, 1, 0);
			}
		}
	}
	
	protected static final void setEntityInCooldown(Entity ent) {
		synchronized(EntityChargeMap) {
			EntityChargeMap.put(ent.getUUID(), -40);
		}
	}
	
	protected static final void incrEntityCharge(Entity ent) {
		synchronized(EntityChargeMap) {
			final Integer charge = EntityChargeMap.get(ent.getUUID());
			if (charge != null && charge < 0) {
				// in cooldown, so avoid doing anything and let it come back up at the same pace
			} else {
				final int newCharge = (charge == null ? 0 : charge) + 2; // 2 because every tick we decay
				EntityChargeMap.put(ent.getUUID(), newCharge);
			}
		}
	}
	
	protected static final boolean hasEntityCharge(Entity ent) {
		synchronized(EntityChargeMap) {
			return EntityChargeMap.containsKey(ent.getUUID());
		}
	}
	
	protected static final int getEntityCharge(Entity ent) {
		synchronized(EntityChargeMap) {
			final Integer val = EntityChargeMap.get(ent.getUUID());
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