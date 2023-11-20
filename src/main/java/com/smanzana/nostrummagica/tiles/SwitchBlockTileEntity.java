package com.smanzana.nostrummagica.tiles;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.blocks.ITriggeredBlock;
import com.smanzana.nostrummagica.entity.EntitySwitchTrigger;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.utils.WorldUtil;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprint;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;

public class SwitchBlockTileEntity extends TileEntity implements ITickableTileEntity, IOrientedTileEntity {
	
	public static enum SwitchHitType {
		ANY,
		MAGIC,
	}
	
	public static enum SwitchTriggerType {
		ONE_TIME,
		REPEATABLE,
		TIMED,
	}
	
	private static final long REPEAT_COOLDOWN_TICKS = 20;
	
	private SwitchBlockTileEntity.SwitchHitType hitType;
	private SwitchBlockTileEntity.SwitchTriggerType triggerType;
	private BlockPos triggerOffset;
	private long triggerWorldTicks;
	private long cooldownTicks;
	private LivingEntity triggerEntity;
	
	protected SwitchBlockTileEntity(TileEntityType<? extends SwitchBlockTileEntity> tileType) {
		super(tileType);
		hitType = SwitchHitType.ANY;
		triggerType = SwitchTriggerType.ONE_TIME;
		triggerOffset = new BlockPos(0, -2, 0);
		triggerEntity = null;
		triggerWorldTicks = 0;
		cooldownTicks = 0;
	}
	
	public SwitchBlockTileEntity() {
		this(NostrumTileEntities.SwitchBlockTileEntityType);
	}
	
	public SwitchBlockTileEntity(SwitchBlockTileEntity.SwitchHitType type, BlockPos pos) {
		this();
		
		this.hitType = type;
		this.triggerOffset = pos;
	}
	
	public SwitchBlockTileEntity(SwitchBlockTileEntity.SwitchHitType hitType, SwitchBlockTileEntity.SwitchTriggerType triggerType, BlockPos pos) {
		this();
		
		this.hitType = hitType;
		this.triggerType = triggerType;
	}
	
	private static final String NBT_HIT_TYPE = "switch_type";
	private static final String NBT_TRIGGER_TYPE = "switch_trigger_type";
	private static final String NBT_OFFSET = "switch_offset";
	private static final String NBT_TRIGGER_TICKS = "trigger_ticks";
	private static final String NBT_COOOLDOWN_TICKS = "trigger_cooldown_ticks";
	
	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		nbt = super.write(nbt);
		
		nbt.putInt(NBT_HIT_TYPE, this.hitType.ordinal());
		nbt.putInt(NBT_TRIGGER_TYPE, this.triggerType.ordinal());
		nbt.put(NBT_OFFSET, NBTUtil.writeBlockPos(this.triggerOffset));
		nbt.putLong(NBT_TRIGGER_TICKS, this.triggerWorldTicks);
		nbt.putLong(NBT_COOOLDOWN_TICKS, cooldownTicks);
		
		return nbt;
	}
	
	@Override
	public void read(CompoundNBT nbt) {
		super.read(nbt);
		
		int ord = nbt.getInt(NBT_HIT_TYPE);
		for (SwitchBlockTileEntity.SwitchHitType type : SwitchHitType.values()) {
			if (type.ordinal() == ord) {
				this.hitType = type;
				break;
			}
		}
		
		ord = nbt.getInt(NBT_TRIGGER_TYPE);
		for (SwitchBlockTileEntity.SwitchTriggerType type : SwitchTriggerType.values()) {
			if (type.ordinal() == ord) {
				this.triggerType = type;
				break;
			}
		}
		
		if (nbt.contains(NBT_OFFSET, NBT.TAG_LONG)) {
			this.triggerOffset = WorldUtil.blockPosFromLong1_12_2(nbt.getLong(NBT_OFFSET));
		} else {
			this.triggerOffset = NBTUtil.readBlockPos(nbt.getCompound(NBT_OFFSET));
		}
		
		this.triggerWorldTicks = nbt.getLong(NBT_TRIGGER_TICKS);
		this.cooldownTicks = nbt.getLong(NBT_COOOLDOWN_TICKS);
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
		handleUpdateTag(pkt.getNbtCompound());
	}
	
	protected void dirty() {
		world.notifyBlockUpdate(pos, this.world.getBlockState(pos), this.world.getBlockState(pos), 3);
		markDirty();
	}
	
	public SwitchBlockTileEntity.SwitchHitType getSwitchHitType() {
		return this.hitType;
	}
	
	public void setHitType(SwitchBlockTileEntity.SwitchHitType type) {
		this.hitType = type;
		dirty();
	}
	
	public SwitchBlockTileEntity.SwitchTriggerType getSwitchTriggerType() {
		return this.triggerType;
	}
	
	public void setTriggerType(SwitchBlockTileEntity.SwitchTriggerType type) {
		this.triggerType = type;
		dirty();
	}
	
	public void setOffset(BlockPos newOffset) {
		setOffset(newOffset, false);
	}
	
	public void setOffset(BlockPos newOffset, boolean isWorldGen) {
		this.triggerOffset = newOffset.toImmutable();
		if (!isWorldGen) {
			dirty();
		}
	}
	
	public void offsetTo(BlockPos targ) {
		this.setOffset(targ.subtract(this.getPos()));
	}
	
	public BlockPos getOffset() {
		return this.triggerOffset;
	}
	
	public void setCooldownTicks(long cooldown) {
		this.cooldownTicks = cooldown;
		this.dirty();
	}
	
	public long getTotalCooldownTicks() {
		return this.cooldownTicks;
	}
	
	public long getCurrentCooldownTicks() {
		if (isTriggered()) {
			final long worldTicks = world.getGameTime();
			final long elapsed = worldTicks - this.triggerWorldTicks;
			return Math.max(0, this.getTotalCooldownTicks() - elapsed);
		}
		
		return 0;
	}
	
	@Nullable
	public LivingEntity getTriggerEntity() {
		return this.triggerEntity;
	}
	
	public boolean isTriggered() {
		return this.triggerWorldTicks != 0;
	}
	
	protected void oneTimeTick(long gameTicks) {
		; // Nothing to do
	}
	
	protected void repeatableTick(long gameTicks) {
		// Stay triggered for a while and then become triggerable again
		if (this.triggerWorldTicks != 0 && gameTicks - this.triggerWorldTicks >= REPEAT_COOLDOWN_TICKS) {
			this.triggerWorldTicks = 0;
			this.dirty();
		}
	}
	
	protected void timedTick(long gameTicks) {
		// Stay triggered for the duration of the timer. At the end, trigger again, and then become
		// triggerable again
		if (this.triggerWorldTicks != 0 && gameTicks - this.triggerWorldTicks >= this.getTotalCooldownTicks()) {
			this.doTriggerInternal();
			this.triggerWorldTicks = 0;
			this.dirty();
		}
	}
	
	@Override
	public void tick() {
		if (world.isRemote) {
			return;
		}
		
		// Create entity here if it doesn't exist
		BlockPos blockUp = pos.up();
		if (triggerEntity == null || !triggerEntity.isAlive() || triggerEntity.world != this.world
				|| triggerEntity.getDistanceSq(blockUp.getX() + .5, blockUp.getY(), blockUp.getZ() + .5) > 1.5) {
			// Entity is dead OR is too far away
			if (triggerEntity != null && !triggerEntity.isAlive()) {
				triggerEntity.remove();
			}
			
			triggerEntity = makeTriggerEntity(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
			world.addEntity(triggerEntity);
		}
		
		// Do tick logic based on type
		final long gameTicks = world.getGameTime();
		switch (this.triggerType) {
		case ONE_TIME:
			oneTimeTick(gameTicks);
			break;
		case REPEATABLE:
			repeatableTick(gameTicks);
			break;
		case TIMED:
			timedTick(gameTicks);
			break;
		}
	}
	
	protected EntitySwitchTrigger makeTriggerEntity(double x, double y, double z) {
		EntitySwitchTrigger ent = new EntitySwitchTrigger(NostrumEntityTypes.switchTrigger, this.world);
		ent.setPosition(x, y, z);
		return ent;
	}
	
	protected void doTriggerInternal() {
		
		BlockPos triggerPos = this.getPos().add(this.getOffset());
		BlockState state = world.getBlockState(triggerPos);
		if (state == null || !(state.getBlock() instanceof ITriggeredBlock)) {
			return;
		}
		
		((ITriggeredBlock) state.getBlock()).trigger(world, triggerPos, state, this.getPos());
	}
	
	public void trigger(boolean isMagic) {
		if (!this.isTriggered()) {
			if (hitType == SwitchHitType.ANY || isMagic) {
				this.triggerWorldTicks = world.getGameTime();
				NostrumMagicaSounds.DAMAGE_ICE.play(world, pos.getX() + .5, pos.getY(), pos.getZ() + .5);
				this.dirty();
				doTriggerInternal();
			} else {
				// Wrong input type
				NostrumMagicaSounds.CAST_FAIL.play(world, pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5);
			}
		}
	}

	@Override
	public void setSpawnedFromRotation(Direction rotation, boolean isWorldGen) {
		this.setOffset(RoomBlueprint.applyRotation(this.getOffset(), rotation), isWorldGen);
	}
}