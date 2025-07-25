package com.smanzana.nostrummagica.tile;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.api.block.entity.IOrientedTileEntity;
import com.smanzana.autodungeons.world.blueprints.Blueprint;
import com.smanzana.autodungeons.world.blueprints.Blueprint.LoadContext;
import com.smanzana.autodungeons.world.blueprints.BlueprintLocation;
import com.smanzana.autodungeons.world.blueprints.IBlueprint;
import com.smanzana.nostrummagica.util.DimensionUtils;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TemplateStamperBlockEntity extends BlockEntity implements IOrientedTileEntity {
	
	private static final String NBT_OFFSET = "offset";
	private static final String NBT_TEMPLATE = "template";
	private static final String NBT_TRIGGERED_BEFORE = "triggered_before";
	private static final String NBT_ONE_TIME = "one_time";
	private static final String NBT_SHOW_HINT = "show_hint";
	
	private static final Component HINT_TEXT = new TranslatableComponent("info.template_stamper.hint");
	
	private BlueprintLocation spawnOffset;
	private Blueprint blueprint;
	private boolean triggeredBefore;
	private boolean oneTime;
	private boolean showHint;
	
	protected TemplateStamperBlockEntity(BlockEntityType<? extends TemplateStamperBlockEntity> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		this.spawnOffset = new BlueprintLocation(new BlockPos(0, 2, 0), Direction.NORTH);
		this.blueprint = null;
		this.triggeredBefore = false;
		this.oneTime = false;
		this.showHint = false;
	}
	
	public TemplateStamperBlockEntity(BlockPos pos, BlockState state) {
		this(NostrumBlockEntities.TemplateStamper, pos, state);
	}
	
	// Calculates the offset to the given pos and saves it
	public void setSpawnPoint(int triggerX, int triggerY, int triggerZ, Direction direction, boolean isWorldGen) {
		setSpawnPoint(new BlockPos(triggerX, triggerY, triggerZ), direction, isWorldGen);
	}
	
	// Calculates the offset to the given pos and saves it
	public void setSpawnPoint(BlockPos triggerPoint, Direction direction, boolean isWorldGen) {
		this.setOffset(new BlueprintLocation(triggerPoint.subtract(this.getBlockPos()), direction), isWorldGen);
	}
	
	// Calculates the offset to the given pos and saves it. Assume direction from relative direction.
	public void setSpawnPoint(BlockPos triggerPoint, boolean isWorldGen) {
		final BlockPos diff = triggerPoint.subtract(this.getBlockPos());
		final Direction direction = Direction.getNearest(diff.getX(), diff.getY(), diff.getZ());
		this.setOffset(new BlueprintLocation(diff, direction), isWorldGen);
	}
	
	public void setOffset(BlueprintLocation offset, boolean isWorldGen) {
		this.spawnOffset = offset;
		flush(isWorldGen);
	}
	
	public BlueprintLocation getOffset() {
		return spawnOffset;
	}
	
	public void setBlueprint(Blueprint blueprint) {
		this.blueprint = blueprint;
		flush(false);
	}
	
	public @Nullable Blueprint getBlueprint() {
		return this.blueprint;
	}
	
	public void resetTriggered() {
		this.triggeredBefore = false;
		flush(false);
	}
	
	public boolean hasBeenTriggered() {
		return this.triggeredBefore;
	}
	
	public void setOneTimeOnly(boolean oneTime) {
		this.oneTime = oneTime;
		flush(false);
	}
	
	public boolean isOneTimeOnly() {
		return this.oneTime;
	}
	
	public void setShowHint(boolean showHint) {
		this.showHint = showHint;
		flush(false);
	}
	
	public boolean showsHint() {
		return this.showHint;
	}
	
	protected void spawnBlueprint() {
		final Blueprint blueprint = this.getBlueprint();
		final BlueprintLocation location = this.getOffset();
		if (blueprint != null) {
			blueprint.spawn(level, this.getBlockPos().offset(location.getPos()), location.getFacing());
		}
	}
	
	protected void showHintToNearby() {
		for (Player player : ((ServerLevel) level).getPlayers(p -> DimensionUtils.IsSorceryDim(p.getLevel()) && p.distanceToSqr(Vec3.atCenterOf(getBlockPos())) < 2500)) {
			player.sendMessage(HINT_TEXT, Util.NIL_UUID);
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
	
	@Override
	public void saveAdditional(CompoundTag compound) {
		super.saveAdditional(compound);
		
		compound.put(NBT_OFFSET, spawnOffset.toNBT());
		if (blueprint != null) {
			compound.put(NBT_TEMPLATE, blueprint.toNBT());
		}
		compound.putBoolean(NBT_ONE_TIME, oneTime);
		compound.putBoolean(NBT_SHOW_HINT, showHint);
		compound.putBoolean(NBT_TRIGGERED_BEFORE, triggeredBefore);
	}
	
	@Override
	public void load(CompoundTag compound) {
		super.load(compound);
		
		this.spawnOffset = BlueprintLocation.fromNBT(compound.getCompound(NBT_OFFSET));
		this.blueprint = null;
		if (compound.contains(NBT_TEMPLATE)) {
			this.blueprint = Blueprint.FromNBT(new LoadContext("TemplateStamperEnt load()"), compound.getCompound(NBT_TEMPLATE));
		}
		this.oneTime = compound.getBoolean(NBT_ONE_TIME);
		this.showHint = compound.getBoolean(NBT_SHOW_HINT);
		this.triggeredBefore = compound.getBoolean(NBT_TRIGGERED_BEFORE);
	}
	
	protected void flush(boolean isWorldGen) {
		if (!isWorldGen && level != null && !level.isClientSide) {
			this.setChanged();
			BlockState state = level.getBlockState(worldPosition);
			level.sendBlockUpdated(worldPosition, state, state, 2);
		}
	}
	
	@Override
	public void setSpawnedFromRotation(Direction rotation, boolean isWorldGen) {
		//this.setOffset(this.spawnOffset.withRotation(rotation), isWorldGen);
		
		Direction doorDir = spawnOffset.getFacing();
		int times = (rotation.get2DDataValue() + 2) % 4;
		while (times-- > 0) {
			doorDir = doorDir.getClockWise();
		}
		
		this.setOffset(new BlueprintLocation(IBlueprint.ApplyRotation(spawnOffset.getPos(), rotation), doorDir), isWorldGen);
	}
	
	public void trigger(BlockPos triggerSource) {
		if (!this.oneTime || !this.triggeredBefore) {
			this.spawnBlueprint();
			if (this.showHint) {
				showHintToNearby();
			}
			if (!this.triggeredBefore) {
				this.triggeredBefore = true;
				this.flush(false);
			}
		}
	}
}