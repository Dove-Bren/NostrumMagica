package com.smanzana.nostrummagica.tile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.block.dungeon.LaserBlock;
import com.smanzana.nostrummagica.capabilities.CapabilityHandler;
import com.smanzana.nostrummagica.capabilities.ILaserReactive;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class LaserBlockEntity extends BlockEntity implements TickableBlockEntity, ILaserReactive {
	
	private static final String NBT_ELEMENT = "element";
	private static final String NBT_TOGGLE_MODE = "toggle_mode";
	private static final String NBT_DEFAULT_ENABLED = "default_enabled";
	private static final String NBT_LASER_LENGTH = "laser_length";
	
	private static final String NBT_MODE_CHANGE_FLAG = "mode_change";
	
	public static final int ANIM_TICKS = 10;
	
	// Persisted data
	protected @Nonnull EMagicElement element;
	protected boolean toggleMode;
	protected boolean enabled;
	protected int laserLength; // in blocks
	
	protected int enabledTicks;
	protected boolean animateLevelChange;
	protected int chargeTicks; // for when being charged from something else
	
	public LaserBlockEntity(BlockPos pos, BlockState state) {
		super(NostrumBlockEntities.Laser, pos, state);
		this.element = EMagicElement.PHYSICAL;
		this.toggleMode = false;
		this.laserLength = 0;
		this.enabled = false;
	}
	
	public void setElement(EMagicElement element) {
		this.element = element;
		this.dirty();
	}
	
	public void setToggleMode(boolean toggleMode) {
		this.toggleMode = toggleMode;
		this.dirty();
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		this.dirty();
	}
	
	protected void updateSegmentLength(int length) {
		if (length != this.laserLength) {
			this.laserLength = length;
			this.dirty();
		}
	}
	
	public EMagicElement getElement() {
		return this.element;
	}
	
	public boolean getToggleMode() {
		return this.toggleMode;
	}
	
	public boolean getDefaultState() {
		return this.enabled;
	}
	
	public int getLaserWholeSegments() {
		return this.laserLength;
	}
	
	public float getSubLengthProgress(float partialTicks) {
		final float mult = this.getEnabled() ? 1 : -1;
		final float age = (enabledTicks + (partialTicks * mult));
		return Mth.clamp(age / ANIM_TICKS, 0f, 1f);
	}
	
	public float getLaserAnimLength(float partialTicks) {
		final int wholeLengths = this.getLaserWholeSegments();
		float subProgress = getSubLengthProgress(partialTicks);
		return wholeLengths + subProgress;
//		if (subProgress <= 0f) {
//			return wholeLengths + (this.getEnabled() ? 0 : -1);
//		} else {
//			return (wholeLengths - 1) + subProgress;
//		}
	}
	
	@Override
	public AABB getRenderBoundingBox() {
		return INFINITE_EXTENT_AABB;
	}
	
	public boolean getEnabled() {
		return enabled;
	}
	
	public Direction getDirection() {
		return this.getBlockState().getValue(LaserBlock.FACING);
	}
	
	protected void resetAnimation(boolean enabled) {
		if (!enabled) {
			this.enabledTicks = ANIM_TICKS - 1;
		} else {
			this.enabledTicks = 0;
		}
	}
	
	private void dirty() {
		level.sendBlockUpdated(worldPosition, this.level.getBlockState(worldPosition), this.level.getBlockState(worldPosition), 3);
		setChanged();
	}
	
	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		nbt.put(NBT_ELEMENT, this.element.toNBT());
		nbt.putBoolean(NBT_TOGGLE_MODE, toggleMode);
		nbt.putBoolean(NBT_DEFAULT_ENABLED, enabled);
		nbt.putInt(NBT_LASER_LENGTH, this.laserLength);
	}
	
	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		
		if (nbt == null)
			return;
		
		this.element = EMagicElement.FromNBT(nbt.get(NBT_ELEMENT));
		this.toggleMode = nbt.getBoolean(NBT_TOGGLE_MODE);
		this.enabled = nbt.getBoolean(NBT_DEFAULT_ENABLED);
		this.laserLength = nbt.getInt(NBT_LASER_LENGTH);
		
		// hard set to no anim, and let update packet handler set animation if there is one
		//this.enabledTicks = getEnabled() ? ANIM_TICKS : 0;
	}
	
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag base = this.saveWithId();
		
		// For updates, we also may signal that our output changed for animation
		if (this.animateLevelChange) {
			base.putBoolean(NBT_MODE_CHANGE_FLAG, true);
		}
		
		return base;
	}
	
	@Override
	public void handleUpdateTag(CompoundTag tag) {
		super.handleUpdateTag(tag);
		this.animateLevelChange = tag.getBoolean(NBT_MODE_CHANGE_FLAG);
		if (this.animateLevelChange) {
			resetAnimation(this.getEnabled());
		}
	}
	
	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		CompoundTag compoundtag = pkt.getTag();
		if (compoundtag != null) {
			this.handleUpdateTag(compoundtag);
		}
	}
	
	protected void onEnabledChange(boolean enabled) {
		// make sure blockstate is updated
		level.setBlock(worldPosition, getBlockState().setValue(LaserBlock.ENABLED, enabled), 3);
	}
	
	protected void addSegment() {
		this.updateSegmentLength(this.getLaserWholeSegments() + 1);
		this.enabledTicks = 0;
	}
	
	protected void setSegmentsTo(int newCount) {
		final boolean shrank = newCount < this.laserLength;
		this.updateSegmentLength(newCount);
		this.enabledTicks = shrank ? ANIM_TICKS-1 : 0;
	}
	
	protected LaserHitResult passThroughBlock(BlockState state, BlockPos pos, BlockEntity ent) {
		// Check if block is laser reactive, and if so give it a chance to react and specify behavior
		@Nullable ILaserReactive react = this.getLaserReactivity(state, ent, getDirection().getOpposite());
		if (react != null) {
			return react.laserPassthroughTick(level, pos, state, getBlockPos(), getElement());
		}
		
		if (state.isAir() || state.propagatesSkylightDown(level, pos)) {
			return LaserHitResult.PASSTHROUGH;
		}
		
		return LaserHitResult.BLOCK;
	}
	
	protected boolean validateLength(List<BlockPos> others) {
		// return true if we find a problem and adjust our length
		MutableBlockPos cursor = new MutableBlockPos(this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ());
		final Direction dir = this.getDirection();
		final Set<BlockPos> seen = new HashSet<>();
		
		cursor.move(dir);
		for (int i = 0; i < this.getLaserWholeSegments(); i++) {
			seen.add(cursor.immutable());
			others.remove(cursor.immutable());
			final LaserHitResult result = passThroughBlock(level.getBlockState(cursor), cursor.immutable(), level.getBlockEntity(cursor)); 
			if (result.stopLaser()) {
				this.setSegmentsTo(i);
				return true;
			}
			
			// blockAllowed visits any laser-reactive blocks. We want to also affect nearby blocks for places we don't stop at
			for (int x = -1; x <= 1; x++)
			for (int y = -1; y <= 1; y++)
			for (int z = -1; z <= 1; z++) {
				final BlockPos otherPos = cursor.immutable().offset(x, y, z);
				if (seen.add(otherPos)) {
					others.add(otherPos);
				}
			}
			
			cursor.move(dir);
		}
		
		return false;
	}
	
	protected boolean attemptGrow() {
		if (this.getLaserWholeSegments() > 16) {
			return false; // CAP
		}
		
		BlockPos toPos = this.worldPosition.relative(getDirection(), this.getLaserWholeSegments() + 1);
		BlockState state = level.getBlockState(toPos);
		BlockEntity ent = level.getBlockEntity(toPos);
		final LaserHitResult result = this.passThroughBlock(state, toPos, ent);
		if (!result.stopLaser()) {
			this.addSegment();
			this.animateLevelChange = true;
			return true;
		}
		
		return false;
	}
	
	protected boolean attemptShrink() {
		if (this.getLaserWholeSegments() == 0) {
			return false;
		}
		
		this.setSegmentsTo(this.getLaserWholeSegments() - 1);
		this.animateLevelChange = true;
		return true;
	}
	
	protected @Nullable ILaserReactive getLaserReactivity(BlockState state, @Nullable BlockEntity entity, Direction direction) {
		@Nullable ILaserReactive reactive = null;
		if (entity != null) {
			reactive = entity.getCapability(CapabilityHandler.CAPABILITY_LASER_REACTIVE, direction).orElse(null);
		}
		
		if (reactive == null && state.getBlock() instanceof ILaserReactive reactiveBlock) {
			reactive = reactiveBlock;
		}
		return reactive;
	}

	@Override
	public void tick() {
		this.animateLevelChange = false;
		
		if (this.getEnabled()) {
			if (this.enabledTicks < ANIM_TICKS) {
				this.enabledTicks++;
			}
		} else {
			if (this.enabledTicks > 0) {
				this.enabledTicks--;
			}
		}
		
		if (level.isClientSide) {
			;
		} else {
			List<BlockPos> nearbyBlocks = new ArrayList<>();
			if (this.validateLength(nearbyBlocks)) {
				// made an adjustment and updated tick count
			} else {
				final boolean enabled = this.getEnabled();
				if (enabled && this.enabledTicks == ANIM_TICKS) {
					attemptGrow();
					this.enabledTicks = 0;
				} else if (!enabled && this.enabledTicks == 0) {
					attemptShrink();
					this.enabledTicks = ANIM_TICKS-1;
				}
			}
			
			// Visit nearby blocks
			for (BlockPos pos : nearbyBlocks) {
				BlockState state = level.getBlockState(pos);
				BlockEntity ent = level.getBlockEntity(pos);
				@Nullable ILaserReactive react = this.getLaserReactivity(state, ent, getDirection().getOpposite());
				if (react != null) {
					react.laserNearbyTick(level, pos, state, this.getBlockPos(), getElement(), pos.distManhattan(getBlockPos()));
				}
			}
			
			this.chargeTicks = Math.max(0, chargeTicks-1);
			if (!this.toggleMode && chargeTicks <= 0 && this.enabled) {
				// Not permanently on, so disable
				this.enabled = false;
			}
		}
	}
	
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		if (cap == CapabilityHandler.CAPABILITY_LASER_REACTIVE) {
			return LazyOptional.of(() -> this).cast();
		}
		
		return super.getCapability(cap, side);
	}

	@Override
	public LaserHitResult laserPassthroughTick(LevelAccessor laserLevel, BlockPos pos, BlockState state, BlockPos laserPos, EMagicElement element) {
		if (!this.enabled) {
			this.enabled = true;
			this.setElement(element);
			this.chargeTicks = 5;
		}
		
		if (this.getElement() == element) {
			this.chargeTicks++;
			return LaserHitResult.PASSTHROUGH;
		} else {
			return LaserHitResult.BLOCK;
		}
	}

	@Override
	public void laserNearbyTick(LevelAccessor laserLevel, BlockPos pos, BlockState state, BlockPos laserPos, EMagicElement element, int beamDistance) {
		; // do nothing
	}
	
}