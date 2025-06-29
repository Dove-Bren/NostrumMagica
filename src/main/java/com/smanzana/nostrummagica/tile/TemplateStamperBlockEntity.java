package com.smanzana.nostrummagica.tile;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.api.block.entity.IOrientedTileEntity;
import com.smanzana.autodungeons.world.blueprints.Blueprint;
import com.smanzana.autodungeons.world.blueprints.Blueprint.LoadContext;
import com.smanzana.autodungeons.world.blueprints.BlueprintLocation;
import com.smanzana.autodungeons.world.blueprints.IBlueprint;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TemplateStamperBlockEntity extends BlockEntity implements IOrientedTileEntity {
	
	private static final String NBT_OFFSET = "offset";
	private static final String NBT_TEMPLATE = "template";
	
	private BlueprintLocation spawnOffset;
	private Blueprint blueprint;
	
	protected TemplateStamperBlockEntity(BlockEntityType<? extends TemplateStamperBlockEntity> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		this.spawnOffset = new BlueprintLocation(new BlockPos(0, 2, 0), Direction.NORTH);
		this.blueprint = null;
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
	
	protected void spawnBlueprint() {
		final Blueprint blueprint = this.getBlueprint();
		final BlueprintLocation location = this.getOffset();
		if (blueprint != null) {
			blueprint.spawn(level, this.getBlockPos().offset(location.getPos()), location.getFacing());
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
	}
	
	@Override
	public void load(CompoundTag compound) {
		super.load(compound);
		
		this.spawnOffset = BlueprintLocation.fromNBT(compound.getCompound(NBT_OFFSET));
		this.blueprint = null;
		if (compound.contains(NBT_TEMPLATE)) {
			this.blueprint = Blueprint.FromNBT(new LoadContext("TemplateStamperEnt load()"), compound.getCompound(NBT_TEMPLATE));
		}
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
		this.spawnBlueprint();
	}
}