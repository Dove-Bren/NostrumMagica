package com.smanzana.nostrummagica.entity.boss.primalmage;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.smanzana.autodungeons.util.WorldUtil;
import com.smanzana.autodungeons.util.WorldUtil.IBlockWalker;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.block.dungeon.SummonGhostBlock;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.client.particles.ParticleTargetBehavior.TargetBehavior;
import com.smanzana.nostrummagica.entity.AggroTable;
import com.smanzana.nostrummagica.serializer.BlockPosListSerializer;
import com.smanzana.nostrummagica.serializer.OptionalMagicElementDataSerializer;
import com.smanzana.nostrummagica.serializer.PrimalMagePoseSerializer;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.MagicDamageSource;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.SpellCastProperties;
import com.smanzana.nostrummagica.spell.component.shapes.NostrumSpellShapes;
import com.smanzana.nostrummagica.util.SpellUtils;
import com.smanzana.nostrummagica.util.TargetLocation;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.SpellcasterIllager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class PrimalMageEntity extends SpellcasterIllager implements PowerableMob {
	
	protected static enum BattleState {
		INACTIVE(true, true), // Waiting for activation
		ACTIVATING(true, true), // ACTIVATING
		RECOVERING(true, false), // Recovering from being downed recently (fly up for easy pathing)
		FLOATING_LAZY_MOVE(false, false), // Is floating to a new location (vulnerable)
		FLOATING_SAFE_MOVE(true, false), // Is floating to a new location (INVULN)
		FLOATING_HOVER(false, false), // Is floating in a location, but not attacking and vulnerable
		FLOATING_BARRAGE(true, false), // Is floating in a location, and casting an attack and INVULN
		FLOATING_SHIELD_CHARGING(false, false), // is floating in a location, and charging a big attack that is interruptable
		MEGA_CHARGE(true, true),
		FALLEN(false, true), // Is on ground and vulnerable for some amount of hits
		;
		
		public final boolean isInvuln;
		public final boolean gravity;
		
		private BattleState(boolean invuln, boolean gravity) {
			this.isInvuln = invuln;
			this.gravity = gravity;
		}
	}
	
	public static enum BattlePose {
		INACTIVE(true), // 
		ACTIVATING(true), // 
		RECOVERING(true), //
		FLOATING_VULN(false), //
		FLOATING_INVULN(true), //
		CASTING_VULN(false), //
		CASTING_INVLUN(true), //
		FALLEN(false)
		;
		
		public final boolean isInvuln;
		
		private BattlePose(boolean invuln) {
			this.isInvuln = invuln;
		}
	}
	
	protected static final EMagicElement[] PRIMAL_ELEMENTS = {EMagicElement.FIRE, EMagicElement.ICE, EMagicElement.WIND, EMagicElement.EARTH};
	
	protected static final Map<EMagicElement, Spell> SPELL_BARRAGE = new EnumMap<>(EMagicElement.class);
	protected static final Map<EMagicElement, Spell> SPELL_CHARGE_BLAST = new EnumMap<>(EMagicElement.class);
	protected static final Map<EMagicElement, Spell> SPELL_MEGA_BLAST = new EnumMap<>(EMagicElement.class);
	
	{
		for (EMagicElement elem : PRIMAL_ELEMENTS) {
			SPELL_BARRAGE.put(elem, SpellUtils.MakeSpell("primalmage_barrage_%s".formatted(elem.getBareName()), NostrumSpellShapes.Mortar, NostrumSpellShapes.Burst, elem, 2, EAlteration.HARM));
			SPELL_CHARGE_BLAST.put(elem, SpellUtils.MakeSpell("primalmage_charge_%s".formatted(elem.getBareName()), NostrumSpellShapes.Barrage, elem, 2, EAlteration.RUIN));
			SPELL_MEGA_BLAST.put(elem, SpellUtils.MakeSpell("primalmage_mega_%s".formatted(elem.getBareName()), NostrumSpellShapes.AI, elem, 3, EAlteration.RUIN, elem, 1, EAlteration.INFLICT));
		}
	}
	
	protected static final EMagicElement RandomPrimalElement(Random rand) {
		return PRIMAL_ELEMENTS[rand.nextInt(PRIMAL_ELEMENTS.length)];
	}

	public static final String ID = "primal_mage";
	
	protected static final EntityDataAccessor<BattlePose> BATTLE_POSE = SynchedEntityData.defineId(PrimalMageEntity.class, PrimalMagePoseSerializer.instance);
	protected static final EntityDataAccessor<List<BlockPos>> CHARGE_LOCATIONS = SynchedEntityData.defineId(PrimalMageEntity.class, BlockPosListSerializer.instance);
	protected static final EntityDataAccessor<Optional<EMagicElement>> SHIELD_ELEMENT = SynchedEntityData.defineId(PrimalMageEntity.class, OptionalMagicElementDataSerializer.instance);
	protected static final EntityDataAccessor<Optional<EMagicElement>> CHARGE_ELEMENT = SynchedEntityData.defineId(PrimalMageEntity.class, OptionalMagicElementDataSerializer.instance);
	
	private final ServerBossEvent bossInfo = (ServerBossEvent) new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_10).setDarkenScreen(true);
	
	// Persisted information
	protected BlockPos homeBlock;
	
	// Arena information
	private PrimalMageArena arena;
	
	// Server-side transient variables
	private AggroTable<LivingEntity> aggroTable; // null on client
	protected BattleState battleState;
	protected int stateTicks; // tick count when state was started
	protected float stateStartHealth;
	protected int stateSubTicks; // counter for logic per each state
	private BlockPos floatTarget;
	private int floatCounter;
	private @Nullable EMagicElement floatElement;
	
	// Animation variables (client side)
	private BattlePose lastPose;
	private int poseTicks; // ticks we've been in the same pose
	
	public PrimalMageEntity(EntityType<? extends PrimalMageEntity> type, Level level) {
		super(type, level);
		
		this.noCulling = true;
        this.xpReward = 1250;
		
        if (!level.isClientSide()) {
			this.aggroTable = new AggroTable<>((ent) -> {
				return (PrimalMageEntity.this.arena != null && arena.getBounds().intersects(ent.getBoundingBox()))
						|| PrimalMageEntity.this.getSensing().hasLineOfSight(ent);
			});
        }
		this.battleState = BattleState.INACTIVE;
	}
	
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(BATTLE_POSE, BattlePose.INACTIVE);
		this.entityData.define(CHARGE_LOCATIONS, new ArrayList<>());
		this.entityData.define(SHIELD_ELEMENT, Optional.empty());
		this.entityData.define(CHARGE_ELEMENT, Optional.empty());
	}

	public static final AttributeSupplier.Builder BuildAttributes() {
		return Mob.createMobAttributes()
		        .add(Attributes.MOVEMENT_SPEED, 0.00D)
		        .add(Attributes.MAX_HEALTH, 300.0D)
		        .add(Attributes.ATTACK_DAMAGE, 2.0D)
		        .add(Attributes.ATTACK_KNOCKBACK, 1.0)
		        .add(Attributes.ARMOR, 4.0D)
		        .add(Attributes.ATTACK_SPEED, 0.5D)
		        .add(Attributes.FOLLOW_RANGE, 8D)
		        .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
	}
	
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		if (tag.contains("origin_block")) {
			this.homeBlock = NbtUtils.readBlockPos(tag.getCompound("origin_block"));
		}
	}

	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		if (this.homeBlock != null) {
			tag.put("origin_block", NbtUtils.writeBlockPos(this.homeBlock));
		}
	}
	
	@Override
	protected void customServerAiStep() {
		super.customServerAiStep();
		
		this.bossInfo.setProgress(this.getHealth() / this.getMaxHealth());
	}
	
	@Override
	public void startSeenByPlayer(ServerPlayer player) {
		super.startSeenByPlayer(player);
		if (isActivated()) {
			this.bossInfo.addPlayer(player);
		}
	}

	@Override
	public void stopSeenByPlayer(ServerPlayer player) {
		super.stopSeenByPlayer(player);
		this.bossInfo.removePlayer(player);
	}
	
	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}
	
	@Override
	public boolean canAttackType(EntityType<?> type) {
		return true;
	}
	
	@Override
	public boolean canChangeDimensions() {
		return false;
	}

	@Override
	protected SoundEvent getCastingSoundEvent() {
		return SoundEvents.EVOKER_CAST_SPELL;
	}

	@Override
	public void applyRaidBuffs(int p_37844_, boolean p_37845_) {
		;
	}

	@Override
	public SoundEvent getCelebrateSound() {
		return SoundEvents.EVOKER_CELEBRATE;
	}
	
	@Override
	public AbstractIllager.IllagerArmPose getArmPose() {
		return super.getArmPose();
	}
	
	@Override
	public boolean isCastingSpell() {
		final BattlePose pose = this.getBattlePose();
		return pose == BattlePose.CASTING_INVLUN || pose == BattlePose.CASTING_VULN
				|| pose == BattlePose.INACTIVE || pose == BattlePose.ACTIVATING;
	}

	@Override
	public boolean isPowered() {
		return this.getBattlePose().isInvuln;
	}
	
	@Override
	protected void registerGoals() {
		; // no AI goals
	}
	
	protected BattleState getBattleState() {
		return this.battleState;
	}
	
	public boolean isActivated() {
		return this.getBattleState() != BattleState.INACTIVE;
	}
	
	public BattlePose getBattlePose() {
		return this.entityData.get(BATTLE_POSE);
	}
	
	protected void setBattlePose(BattlePose pose) {
		this.entityData.set(BATTLE_POSE, pose);
	}
	
	public int getTicksInPose() {
		// This often gets called before the firsttick  where it's changed.
		// TODO respond to actual data changing instead of caching like thisto fix.
		if (this.lastPose != this.getBattlePose()) {
			this.lastPose = this.getBattlePose();
			this.poseTicks = this.tickCount;
		}
		
		return this.tickCount - this.poseTicks;
	}
	
	protected void setChargeLocations(List<BlockPos> locations) {
		this.entityData.set(CHARGE_LOCATIONS, ImmutableList.copyOf(locations));
	}
	
	public List<BlockPos> getChargeLocations() {
		return this.entityData.get(CHARGE_LOCATIONS);
	}
	
	protected void addToChargeLocations(BlockPos pos) {
		final List<BlockPos> newList = new ArrayList<>(this.getChargeLocations());
		newList.add(pos);
		setChargeLocations(newList);
	}
	
	protected void clearChargeLocations() {
		setChargeLocations(new ArrayList<>());
	}
	
	public Optional<EMagicElement> getShieldElement() {
		return this.entityData.get(SHIELD_ELEMENT);
	}
	
	protected void setShieldElement(@Nullable EMagicElement element) {
		this.entityData.set(SHIELD_ELEMENT, Optional.ofNullable(element));
	}
	
	public Optional<EMagicElement> getChargeElement() {
		return this.entityData.get(CHARGE_ELEMENT);
	}
	
	protected void setChargeElement(@Nullable EMagicElement element) {
		this.entityData.set(CHARGE_ELEMENT, Optional.ofNullable(element));
	}
	
	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
		super.onSyncedDataUpdated(data);
		if (BATTLE_POSE.equals(data)) {
			this.refreshDimensions();
		}
	}
	
	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (source.getEntity() == this) {
			return false;
		}
		
		// early exception for out of world, especially for kill command
		if (source == DamageSource.OUT_OF_WORLD) {
			return super.hurt(source, amount);
		}
		
		if (!isActivated()) {
			return false;
		}
		
		if (this.getBattleState().isInvuln) {
			return false;
		}
		
		if (this.getShieldElement().isPresent()) {
			final @Nullable EMagicElement element;
			if (source instanceof MagicDamageSource) {
				element = ((MagicDamageSource) source).getElement();
			} else {
				element = null;
			}
			if (element == this.getShieldElement().get().getOpposite()) {
				// Break shield, and fall
				this.setShieldElement(null);
				this.setBattleState(BattleState.FALLEN);
				this.level.playSound(null, this, SoundEvents.EVOKER_DEATH, getSoundSource(), 1f, 1f);
				this.level.playSound(null, this, SoundEvents.GLASS_BREAK, getSoundSource(), 1f, 1f);
				return super.hurt(source, amount);
			}
			
			// else
			return false;
		}
		
		// else just vulnerable, so let through and make fall if not already there
		this.setBattleState(BattleState.FALLEN);
		this.level.playSound(null, this, SoundEvents.EVOKER_HURT, getSoundSource(), 1f, 1f);
		return super.hurt(source, amount);
	}
	
	@Override
	public boolean causeFallDamage(float damage, float distMult, DamageSource dmgType) {
		return false;
	}
	
	@Override
	public boolean isPickable() {
		return true; // Wants parts to be collided with, not main entity
		//return super.canBeCollidedWith();
	}
	
	@Override
	public boolean isPushable() {
		return this.isActivated();
	}
	
	@Override
	public void push(Entity pusher) {
		if (!this.isActivated()) {
			return;
		}
		
		super.push(pusher);
	}
	
	@Override
	public void push(double x, double y, double z) {
		if (this.isActivated()) {
			super.push(x, y, z);
		}
	}
	
	@Override
	public EntityDimensions getDimensions(Pose entPose) {
		final EntityDimensions baseDims = super.getDimensions(entPose);
		
		// Some of our states want to override the bounding box
		final BattlePose pose = this.getBattlePose();
		if (pose == BattlePose.CASTING_VULN) {
			return new EntityDimensions(baseDims.width + 2, baseDims.height + .5f, baseDims.fixed);
		} else if (pose == BattlePose.FALLEN) {
			return new EntityDimensions(baseDims.width + 1, baseDims.height, baseDims.fixed);
		}
		return baseDims;
	}
	
	protected void updatePose() {
		BattlePose pose = BattlePose.FLOATING_VULN;
		switch (this.battleState) {
		case INACTIVE:
			pose = BattlePose.INACTIVE;
			break;
		case ACTIVATING:
			pose = BattlePose.ACTIVATING;
			break;
		case RECOVERING:
			pose = BattlePose.RECOVERING;
			break;
		case FLOATING_LAZY_MOVE:
			pose = BattlePose.FLOATING_VULN;
			break;
		case FLOATING_SAFE_MOVE:
			pose = BattlePose.FLOATING_INVULN;
			break;
		case FLOATING_HOVER:
			pose = BattlePose.FLOATING_VULN;
			break;
		case FLOATING_BARRAGE:
			pose = BattlePose.CASTING_INVLUN;
			break;
		case FLOATING_SHIELD_CHARGING:
			pose = BattlePose.CASTING_VULN;
			break;
		case MEGA_CHARGE:
			pose = BattlePose.CASTING_INVLUN;
			break;
		case FALLEN:
			pose = BattlePose.FALLEN;
			break;
		}
		this.setBattlePose(pose);
	}
	
	protected void setBattleState(BattleState state) {
		if (state != this.battleState) {
			final BattleState oldState = this.battleState;
			this.battleState = state;
			this.stateTicks = this.tickCount;
			this.stateStartHealth = this.getHealth();
			this.updatePose();
			
			onStateChange(oldState, state);
		}
	}
	
	protected void onStateChange(BattleState oldState, BattleState newState) {
		this.stateSubTicks = 0;
		this.setNoGravity(!newState.gravity);
	}
	
	protected void setNewFloatTarget(boolean wantElemental) {
		if (wantElemental) {
			final EMagicElement wantElement = RandomPrimalElement(this.random);
			BlockPos wantPos = arena.getElementalCrystalFloatPos(wantElement);
			if (wantPos != null) {
				this.floatElement = wantElement;
				this.floatTarget = wantPos;
				return;
			}
		}
		
		// If not elemental or elemental failed, fall back to a random non-elemental
		this.floatElement = null;
		BlockPos pillar;
		int attempts = 10;
		do {
			pillar = this.arena.getRandomPillarFloatPos(getRandom());
		}
		while (pillar == this.floatTarget && attempts-- > 0);
		
		this.floatTarget = pillar;
	}
	
	protected int getInvulnFloatCountForPhase() {
		final float perc = this.getHealth() / this.getMaxHealth();
		final int base = 0;
		return base + (int) ((1-perc) * 8);
	}
	
	protected boolean shouldSafeMoveForPhase() {
		final float perc = this.getHealth() / this.getMaxHealth();
		return perc < .95f;
	}
	
	protected boolean shouldMegaChargeOnRecoverForPhase() {
		final float perc = this.getHealth() / this.getMaxHealth();
		return perc < .8f;
	}
	
	protected BattleState getNextFloatStartState() {
		// Either safe or unsafe, depending on health
		if (shouldSafeMoveForPhase()) {
			return BattleState.FLOATING_SAFE_MOVE;
		} else {
			return BattleState.FLOATING_LAZY_MOVE;
		}
	}
	
	protected BattleState getNextFloatEndState() {
		// base on how many times we've floated invuln, and current hp
		final int threshold = this.getInvulnFloatCountForPhase();
		if (this.floatCounter++ < threshold) {
			// invuln
			return BattleState.FLOATING_BARRAGE;
		} else {
			// vuln
			// Reset counter so that if player misses it, they have to wait again
			this.floatCounter = (floatCounter-1) / 2;
			if (this.getHealth() / this.getMaxHealth() > .95f) {
				// just a lazy float
				return BattleState.FLOATING_HOVER;
			} else {
				return BattleState.FLOATING_SHIELD_CHARGING;
			}
		}
	}
	
	protected void activate() {
		if (!this.isActivated()) {
			// 'activate' boss bar for all players nearby
			if (!getLevel().isClientSide()) {
				for (Player player : ((ServerLevel) getLevel()).getPlayers(p -> p.distanceToSqr(this) < 900)) {
					this.bossInfo.addPlayer((ServerPlayer) player);
				}
				this.setBattleState(BattleState.ACTIVATING);
			}
		}
		
	}
	
	protected void resetCrystal(EMagicElement element, BlockPos start) {
		if (start == null) {
			return;
		}
		
		final BlockState originState = level.getBlockState(start);
		if (originState.getBlock() instanceof SummonGhostBlock) {
			return; // doesn't need resetting
		}
		
		WorldUtil.WalkConnectedBlocks(level, start, new IBlockWalker() {
			@Override
			public boolean canVisit(BlockGetter world, BlockPos startPos, BlockState startState, BlockPos pos,
					BlockState state, int distance) {
				return startState == state;
			}

			@Override
			public IBlockWalker.WalkResult walk(BlockGetter world, BlockPos startPos, BlockState startState, BlockPos pos,
					BlockState state, int distance, int walkCount, Consumer<BlockPos> addBlock) {
				level.setBlock(pos, NostrumBlocks.elementalStone(element).defaultBlockState(), 2);
				SummonGhostBlock.WrapBlock(level, pos, element);
				return IBlockWalker.WalkResult.CONTINUE;
			}
		}, 256);
	}
	
	protected void resetArena() {
		// for each element, find crystal and then reset it
		// by walking all connected and wrapping htem, and then setting elem after
		for (EMagicElement elem : PRIMAL_ELEMENTS) {
			resetCrystal(elem, arena.getElementalCrystal(elem));
		}
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (this.level.isClientSide()) {
			this.clientTick();
		} else {
			this.aggroTable.decayTick();
			if (this.homeBlock == null) {
				this.homeBlock = getOnPos(); // Persisted so should only happen once per spawning
			}
			if (this.arena == null) { // not persisted, so will happen every time reloladed
				this.arena = PrimalMageArena.Capture(getLevel(), this.homeBlock);
			}
			
			for (Player player : this.getLevel().getNearbyPlayers(TargetingConditions.forCombat(), this, arena.getBounds())) {
				this.aggroTable.addDamage(player, .2f);
			}
			
			switch (this.battleState) {
			case INACTIVE:
				this.inactiveTick();
				break;
			case ACTIVATING:
				this.activatingTick();
				break;
			case RECOVERING:
				this.recoveringTick();
				break;
			case FLOATING_LAZY_MOVE:
				this.floatingMoveLazyTick();
				break;
			case FLOATING_SAFE_MOVE:
				this.floatingMoveSafeTick();
				break;
			case FLOATING_HOVER:
				this.floatingHoverTick();
				break;
			case FLOATING_BARRAGE:
				this.floatingBarrageTick();
				break;
			case FLOATING_SHIELD_CHARGING:
				this.floatingShieldedChargeTick();
				break;
			case MEGA_CHARGE:
				this.megaChargeTick();
				break;
			case FALLEN:
				this.fallenTick();
				break;
			}
		}
	}
	
	protected void inactiveTick() {

		// While inactive, we look at elemental top blocks and see if they are summoned yet.
		if (this.arena == null) {
			return;
		}
		
		if (!this.getOnPos().equals(this.homeBlock)) {
			// Presumably reloaded. Likely because player died and ran back.
			// Will mean server crashes in b oss fight reset, but that might be
			// better than loading in to an active boss fight
			if (NostrumMagica.isBlockLoaded(level, this.homeBlock)) {
				resetArena();
				this.setPos(Vec3.atBottomCenterOf(this.homeBlock.above()));
				this.setHealth(this.getMaxHealth());
			}
			
			return;
		}
		
		if (this.stateSubTicks++ < 20) {
			return;
		}
		
		stateSubTicks = 0;
		
		List<BlockPos> activePositions = new ArrayList<>(4);
		for (EMagicElement element : EMagicElement.values()) {
			BlockPos pos = this.arena.getElementalCrystal(element);
			if (pos != null && !(this.getLevel().getBlockState(pos).getBlock() instanceof SummonGhostBlock)) {
				activePositions.add(pos);
			}
		}
		
		this.setChargeLocations(activePositions);
		
		if (activePositions.size() >= 4) {
			this.activate();
			return;
		}
	}
	
	protected void recoveringTick() {
		if (this.stateSubTicks++ > 60) {
			if (this.shouldMegaChargeOnRecoverForPhase()) {
				this.setBattleState(BattleState.MEGA_CHARGE);
			} else {
				this.setBattleState(this.getNextFloatStartState());
			}
		} else {
			// Float upwards to above our home block
			final int targY = (int) (this.arena.bounds.maxY - 2);
			final BlockPos targ = (this.homeBlock == null ? this.blockPosition() : this.homeBlock).atY(targY);
			final Vec3 diff = Vec3.atCenterOf(targ).subtract(position());
			this.setDeltaMovement(this.getDeltaMovement().scale(.6).add(diff.normalize().scale(.1)));
			this.hasImpulse = true;
		}
	}
	
	protected boolean floatingMoveTickBase(boolean wantElemental) {
		final @Nullable LivingEntity target = this.aggroTable.getMainTarget();
		if (target != null) {
			this.getLookControl().setLookAt(target, 40, 20);
		}
		
		// If we have a target and we're not at it, float towards it
		if (stateSubTicks++ == 0) {
			this.setNewFloatTarget(wantElemental);
			return false;
		} else if (this.distanceToSqr(Vec3.atCenterOf(this.floatTarget)) > 1) {
			final Vec3 diff = Vec3.atCenterOf(this.floatTarget).subtract(position());
			this.setDeltaMovement(this.getDeltaMovement().scale(.6).add(diff.normalize().scale(.1)));
			this.hasImpulse = true;
			return false;
		} else if (this.stateSubTicks < 40) {
			// within space of it, so just float there
			return false;
		} else {
			return true;
		}
	}
	
	protected void floatingMoveLazyTick() {
		if (floatingMoveTickBase(false)) {
			this.stateSubTicks = 0; // in case next state is the same
			this.setBattleState(getNextFloatEndState());
		}
	}
	
	protected void floatingMoveSafeTick() {
		if (floatingMoveTickBase(true)) {
			this.stateSubTicks = 0; // in case next state is the same
			this.setBattleState(getNextFloatEndState());
		}
	}
	
	protected void floatingHoverTickBase(float period, float radius) {
		final float progRad = (this.stateSubTicks / period) * 2 * Mth.PI;
		final float xOffset = Mth.cos(progRad) * radius;
		final float zOffset = Mth.sin(progRad) * radius;
		final float yOffset = Mth.sin(progRad + (Mth.PI/2f)) * .25f;
		
		this.setDeltaMovement(Vec3.ZERO);
		this.setPos(this.floatTarget.getX() + .5 + xOffset, floatTarget.getY() + .5 + yOffset, floatTarget.getZ() + .5 + zOffset);
	}
	
	protected void floatingHoverTick() {
		if (this.stateSubTicks++ > 60) {
			this.setBattleState(getNextFloatStartState());
			return;
		}
		floatingHoverTickBase(60f, 1.5f);
	}
	
	protected void castBarrage(EMagicElement element) {
		LivingEntity target = this.aggroTable.getMainTarget();
		if (target != null) {
			this.setTarget(target);
			this.lookAt(target, 360f, 180f);
			SPELL_BARRAGE.get(element).cast(this, SpellCastProperties.makeWithTarget(1f, target));
			this.level.playSound(null, this, SoundEvents.PILLAGER_CELEBRATE, getSoundSource(), 1f, 1f);
		}
	}
	
	protected void floatingBarrageTick() {
		if (this.stateSubTicks++ > 80) {
			EMagicElement elem = this.floatElement;
			if (elem == null) elem = RandomPrimalElement(this.random);
			this.castBarrage(elem);
			this.setBattleState(getNextFloatStartState());
			return;
		}
		floatingHoverTickBase(60f, 1.5f);
		
		if (this.tickCount % 20 == 0) {
			this.level.playSound(null, this, SoundEvents.VILLAGER_TRADE, getSoundSource(), 1f, 1f);
		}
	}
	
	protected void castChargeBlast(EMagicElement element) {
		LivingEntity target = this.aggroTable.getMainTarget();
		if (target != null) {
			this.setTarget(target);
			this.lookAt(target, 360f, 180f);
			SPELL_CHARGE_BLAST.get(element).cast(this, SpellCastProperties.makeWithTarget(1f, target));
			
			this.level.playSound(null, this, SoundEvents.WITCH_CELEBRATE, getSoundSource(), 1f, .75f);
		}
	}
	
	protected void floatingShieldedChargeTick() {
		if (this.stateSubTicks++ == 0) {
			EMagicElement elem = this.floatElement;
			if (elem == null) elem = RandomPrimalElement(this.random);
			this.setShieldElement(elem);
		} else if (this.stateSubTicks > 180) {
			this.castChargeBlast(this.getShieldElement().get());
			this.setShieldElement(null);
			this.setBattleState(getNextFloatStartState());
			return;
		}
		floatingHoverTickBase(100f, .75f);
		
		if (this.tickCount % 20 == 0) {
			this.level.playSound(null, this, SoundEvents.EVOKER_AMBIENT, getSoundSource(), 1f, 1f);
		}
	}
	
	protected void castMegaBlast(EMagicElement element) {
		// For all players that are in the arena OR line of sight (since arena has ledges)
		// that ARENT on the right elemental pillar, blast
		final Spell spell = SPELL_MEGA_BLAST.get(element);
		for (Entity ent : this.getLevel().getEntities(this, arena.bounds.inflate(10), (e) -> 
			e instanceof LivingEntity
				&& (arena.bounds.intersects(e.getBoundingBox()) || PrimalMageEntity.this.getSensing().hasLineOfSight(e))
				&& (!arena.isOnElementalPlatform(e.position(), element.getOpposite()))
		)) {
			LivingEntity living = (LivingEntity) ent;
			this.setTarget(living);
			this.lookAt(living, 360f, 180f);
			spell.cast(this, SpellCastProperties.makeWithTarget(1f, living));
		}
	}
	
	protected void megaChargeTick() {
		if (this.stateSubTicks++ == 0) {
			this.setChargeElement(RandomPrimalElement(this.random));
		} else if (this.stateSubTicks > 140) {
			this.castMegaBlast(this.getChargeElement().get());
			
			// vfx
			{
				NostrumParticles.FILLED_ORB.spawn(level, new SpawnParams(2000, getX(), getY() + this.getBbHeight() / 2, getZ(), 0,
						100, 40, new Vec3(0, .2, 0), new Vec3(1, .4, 1)
						).gravity(true).color(this.getChargeElement().get().getColor()).setTargetBehavior(TargetBehavior.JOIN));
			}
			
			this.playSound(SoundEvents.ENDER_DRAGON_GROWL, 1f, 1f);
			
			this.setBattleState(getNextFloatStartState());
		} else {
			NostrumParticles.FILLED_ORB.spawn(level, new SpawnParams(100, getX(), getY() + this.getBbHeight() / 2, getZ(), 5,
					40, 20, new TargetLocation(this, true)
					).color(this.getChargeElement().get().getColor()).setTargetBehavior(TargetBehavior.ORBIT_LAZY));
			
			if (this.tickCount % 20 == 0) {
				this.level.playSound(null, this, SoundEvents.ILLUSIONER_CAST_SPELL, getSoundSource(), 1f, 1f);
			}
		}
	}
	
	protected void activatingTick() {
		if (this.stateSubTicks++ > 80) {
			this.setBattleState(getNextFloatStartState());
			
			NostrumParticles.FILLED_ORB.spawn(level, new SpawnParams(100, getX(), getY() + this.getBbHeight() / 2, getZ(), .25,
					30, 10, new Vec3(0, .2, 0), new Vec3(.2, .05, .2)
					).gravity(true).color(0xA0E5E52D).setTargetBehavior(TargetBehavior.JOIN));
			
			this.playSound(SoundEvents.WITHER_SPAWN, 1f, 1f);
			
			this.clearChargeLocations();
		}
	}
	
	protected void fallenTick() {
		if (this.stateSubTicks++ > 120 || this.stateStartHealth - this.getHealth() > (.1f * this.getMaxHealth())) {
			this.setBattleState(BattleState.RECOVERING);
			this.level.playSound(null, this, SoundEvents.VILLAGER_NO, getSoundSource(), 1f, 1f);
		}
	}
	
	protected void clientTick() {
		if (this.lastPose != this.getBattlePose()) {
			this.lastPose = this.getBattlePose();
			this.poseTicks = this.tickCount;
		}
		
		final BattlePose pose = this.getBattlePose();
		
		if (pose == BattlePose.ACTIVATING) {
			// effects?
			NostrumParticles.FILLED_ORB.spawn(level, new SpawnParams(1 + (stateSubTicks++ / 20), getX(), getY() + this.getBbHeight() / 2, getZ(), 3,
					30, 10, new TargetLocation(this, true)
					).color(0xA0E5E52D).setTargetBehavior(TargetBehavior.JOIN));
		} else {
			stateSubTicks = 0;
		}
		
//		if (pose == BattlePose.FALLEN) {
//			this.setBoundingBox(this.getBoundingBoxForPose(Pose.STANDING).inflate(.5, 0, .5));
//		} else if (pose == BattlePose.CASTING_VULN) {
//			this.setBoundingBox(this.getBoundingBoxForPose(Pose.STANDING).inflate(1, .5, 1));
//		} else {
//			this.setBoundingBox(this.getBoundingBoxForPose(Pose.STANDING));
//		}
	}

}
