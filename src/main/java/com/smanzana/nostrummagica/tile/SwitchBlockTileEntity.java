package com.smanzana.nostrummagica.tile;

import com.smanzana.autodungeons.api.block.entity.IOrientedTileEntity;
import com.smanzana.autodungeons.util.WorldUtil;
import com.smanzana.autodungeons.world.blueprints.IBlueprint;
import com.smanzana.nostrummagica.block.ITriggeredBlock;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.entity.SwitchTriggerEntity;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.MagicDamageSource;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SwitchBlockTileEntity extends EntityProxiedTileEntity<SwitchTriggerEntity> implements IOrientedTileEntity {
	
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
	
	protected SwitchBlockTileEntity(BlockEntityType<? extends SwitchBlockTileEntity> tileType, BlockPos pos, BlockState state) {
		super(tileType, pos, state);
		hitType = SwitchHitType.ANY;
		triggerType = SwitchTriggerType.ONE_TIME;
		triggerOffset = new BlockPos(0, -2, 0);
		triggerWorldTicks = 0;
		cooldownTicks = 0;
	}
	
	public SwitchBlockTileEntity(BlockPos pos, BlockState state) {
		this(NostrumBlockEntities.SwitchBlock, pos, state);
	}
	
	public SwitchBlockTileEntity( BlockPos blockEntPos, BlockState state, SwitchBlockTileEntity.SwitchHitType type,BlockPos pos) {
		this(blockEntPos, state);
		
		this.hitType = type;
		this.triggerOffset = pos;
	}
	
	public SwitchBlockTileEntity( BlockPos blockEntPos, BlockState state, SwitchBlockTileEntity.SwitchHitType hitType, SwitchBlockTileEntity.SwitchTriggerType triggerType, BlockPos pos) {
		this(blockEntPos, state);
		
		this.hitType = hitType;
		this.triggerType = triggerType;
	}
	
	private static final String NBT_HIT_TYPE = "switch_type";
	private static final String NBT_TRIGGER_TYPE = "switch_trigger_type";
	private static final String NBT_OFFSET = "switch_offset";
	private static final String NBT_TRIGGER_TICKS = "trigger_ticks";
	private static final String NBT_COOOLDOWN_TICKS = "trigger_cooldown_ticks";
	
	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		
		nbt.putInt(NBT_HIT_TYPE, this.hitType.ordinal());
		nbt.putInt(NBT_TRIGGER_TYPE, this.triggerType.ordinal());
		nbt.put(NBT_OFFSET, NbtUtils.writeBlockPos(this.triggerOffset));
		nbt.putLong(NBT_TRIGGER_TICKS, this.triggerWorldTicks);
		nbt.putLong(NBT_COOOLDOWN_TICKS, cooldownTicks);
	}
	
	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		
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
		
		if (nbt.contains(NBT_OFFSET, Tag.TAG_LONG)) {
			this.triggerOffset = WorldUtil.blockPosFromLong1_12_2(nbt.getLong(NBT_OFFSET));
		} else {
			this.triggerOffset = NbtUtils.readBlockPos(nbt.getCompound(NBT_OFFSET));
		}
		
		this.triggerWorldTicks = nbt.getLong(NBT_TRIGGER_TICKS);
		this.cooldownTicks = nbt.getLong(NBT_COOOLDOWN_TICKS);
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
		this.triggerOffset = newOffset.immutable();
		if (!isWorldGen) {
			dirty();
		}
	}
	
	public void offsetTo(BlockPos targ) {
		this.setOffset(targ.subtract(this.getBlockPos()));
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
			final long worldTicks = level.getGameTime();
			final long elapsed = worldTicks - this.triggerWorldTicks;
			return Math.max(0, this.getTotalCooldownTicks() - elapsed);
		}
		
		return 0;
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
	
	protected long getTickTockPeriod() {
		// If total time is large, use 1 second period. Otherwise, half second
		// OR base it on current time left!
		final long remainingTime = this.getCurrentCooldownTicks();
		
		return remainingTime <= 3 * 20 ? 10 : 20;
	}
	
	protected void timedTick(long gameTicks) {
		// Stay triggered for the duration of the timer. At the end, trigger again, and then become
		// triggerable again
		final long elapsedTicks = gameTicks - this.triggerWorldTicks;
		if (this.triggerWorldTicks != 0 && elapsedTicks >= this.getTotalCooldownTicks()) {
			this.doTriggerInternal();
			this.triggerWorldTicks = 0;
			this.dirty();
			
			NostrumMagicaSounds.DAMAGE_ICE.play(level, worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5);
		}
		
		// Play tick or tock if still going
		
		final long period = getTickTockPeriod();
		if (this.triggerWorldTicks != 0 && elapsedTicks % period == 0) {
			// Figure out if it's a tick (first, third, etc.) or tock (second, fourth, etc.)
			final boolean tick = elapsedTicks % (period * 2) < period;
			// elapsed % 2* period < period
			
			// period of 10
			// 0 < period YES
			// 10 < period NO
			// 0 ....
			
			// period of 20 then 20 after 3 seconds (6 sec timer)
			// 0 < period  YES   // 0
			// 20 < period NO    // 20
			// 0 < period YES    // 40
			// 10 < period NO    // 50
			
			final NostrumMagicaSounds sound = tick ? NostrumMagicaSounds.TICK : NostrumMagicaSounds.TOCK;
			sound.play(level, worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5);
		}
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (level.isClientSide) {
			return;
		}
		
		// Do tick logic based on type
		final long gameTicks = level.getGameTime();
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
	
	@Override
	protected SwitchTriggerEntity makeTriggerEntity(Level world, double x, double y, double z) {
		SwitchTriggerEntity ent = new SwitchTriggerEntity(NostrumEntityTypes.switchTrigger, world);
		ent.setPos(x, y, z);
		return ent;
	}
	
	protected void doTriggerInternal() {
		
		BlockPos triggerPos = this.getBlockPos().offset(this.getOffset());
		BlockState state = level.getBlockState(triggerPos);
		if (state == null || !(state.getBlock() instanceof ITriggeredBlock)) {
			return;
		}
		
		((ITriggeredBlock) state.getBlock()).trigger(level, triggerPos, state, this.getBlockPos());
	}
	
	protected void doTrigger() {
		this.triggerWorldTicks = level.getGameTime();
		NostrumMagicaSounds.DAMAGE_ICE.play(level, worldPosition.getX() + .5, worldPosition.getY(), worldPosition.getZ() + .5);
		this.dirty();
		doTriggerInternal();
	}
	
	@Override
	public void trigger(LivingEntity entity, DamageSource source, float damage) {
		if (!this.isTriggered() && !this.level.isClientSide()) {
			final boolean isMagic = (source instanceof MagicDamageSource);
			if (hitType == SwitchHitType.ANY || isMagic) {
				doTrigger();
			} else {
				// Wrong input type
				NostrumMagicaSounds.CAST_FAIL.play(level, worldPosition.getX() + .5, worldPosition.getY() + 1, worldPosition.getZ() + .5);
			}
		}
	}

	@Override
	public void setSpawnedFromRotation(Direction rotation, boolean isWorldGen) {
		this.setOffset(IBlueprint.ApplyRotation(this.getOffset(), rotation), isWorldGen);
	}
}