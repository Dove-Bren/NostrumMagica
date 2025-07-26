package com.smanzana.nostrummagica.entity.boss.shadowdragon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.client.particles.ParticleTargetBehavior.TargetBehavior;
import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.entity.AggroTable;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.entity.TameLightning;
import com.smanzana.nostrummagica.entity.golem.MagicGolemEntity;
import com.smanzana.nostrummagica.serializer.OptionalMagicElementDataSerializer;
import com.smanzana.nostrummagica.serializer.ShadowDragonPoseSerializer;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.MagicDamageSource;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.SpellCastProperties;
import com.smanzana.nostrummagica.util.TargetLocation;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ShadowDragonEntity extends Mob implements PowerableMob {
	
	public static enum BattleState {
		INACTIVE(true, true), // Waiting for activation
		ACTIVATING(true, false), // ACTIVATING
		FALLEN(false, true), // Struck by the player and falling down, vulnerable. Includings falling over animations 
		RECOVERING(false, false), // Getting back up from being toppled. Invlunerable during this stage
		FOLLOW_PLAYER_PASSIVE(false, false), // Is moving after the player, revealed and vulnerable
		FOLLOW_PLAYER_SHADOW(true, false), // Is moving after the player, but is shadowed but passive
		
		DIVE(true, false), // Doing a dive attack at the player
		
		MOVE_TO_CENTER(true, false), // Moving to center to do a charge attack
		
		CHARGE_ARENA_ATTACK(true, false), // Returning to center and charging up a wave of attacks, possible with some arena denial
		CHARGE_ARENA_SUMMON(true, false), // Returning to center and charging up a summon spell
		
		HIDE_FOR_SUMMONS(true, false),
		;
		
		public final boolean isInvuln;
		public final boolean gravity;
		
		private BattleState(boolean invuln, boolean gravity) {
			this.isInvuln = invuln;
			this.gravity = gravity;
		}
	}
	
	/**
	 * What animation pose the entity is in.
	 */
	public static enum BattlePose {
		INACTIVE(false, true),
		FALLEN(false, false),
		ACTIVATING(false, true),
		RECOVERING(false, true),
		FLOAT_SHADOW(true, false),
		FLOAT_REVEALED(false, false),
		CHARGING(false, true),
		ROARING(false, true),
		HIDDEN(false, false),
		DIVING(false, true),
		;
		
		public final boolean isEthereal;
		public final boolean isInvuln;
		
		private BattlePose(boolean isEthereal, boolean isInvuln) {
			this.isEthereal = isEthereal;
			this.isInvuln = isInvuln;
		}
		
		//float_hidden for summon phase
		// charging for summon phase or arena denial phase
	}
	
	private static record ChargeAttackPhase(ShadowDragonAttackPattern pattern, Spell spell, float power) {}
	
	public static final String ID = "shadow_dragon_boss";
	
	private static final Component TEXT_ETHEREAL = new TranslatableComponent("info.shadow_dragoon.ethereal.no_hit");
	
	protected static final EntityDataAccessor<BattlePose> BATTLE_POSE = SynchedEntityData.defineId(ShadowDragonEntity.class, ShadowDragonPoseSerializer.instance);
	protected static final EntityDataAccessor<Optional<EMagicElement>> REVEALED_ELEMENT = SynchedEntityData.defineId(ShadowDragonEntity.class, OptionalMagicElementDataSerializer.instance);
	
	private final ServerBossEvent bossInfo = (ServerBossEvent) new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_20).setDarkenScreen(true);
	
	// Persisted information
	protected BlockPos homeBlock;
	
	// Arena information
	private ShadowDragonArena arena;
	
	// Server-side transient variables
	private AggroTable<LivingEntity> aggroTable; // null on client
	protected BattleState battleState;
	private final ShadowDragonSpells spells;
	private final List<Entity> summonedEntities;
	private float lastHealthThresholdPassed = 1f;
	private int diveCount;
	
	// State sub variables, expected to be reset when battle state changes
	protected int stateTicks; // tick count when state was started
	protected float stateStartHealth;
	protected int stateSubTicks; // counter for logic per each state
	protected int stateSubTimer; // Specialty timer for each state to use
	protected Vec3 stateTargetPos;
	protected ChargeAttackPhase attackPhase;
	
	// Animation variables (client side)
	private BattlePose lastPose;
	private int poseTicks; // ticks we've been in the same pose

	public ShadowDragonEntity(EntityType<? extends ShadowDragonEntity> type, Level worldIn) {
		super(type, worldIn);
        this.noCulling = true;
        this.xpReward = 0;
		
        if (!worldIn.isClientSide()) {
			this.aggroTable = new AggroTable<>((ent) -> {
				return ShadowDragonEntity.this.getSensing().hasLineOfSight(ent);
			});
        }
		this.battleState = BattleState.INACTIVE;
		this.spells = ShadowDragonSpells.Instance();
		this.summonedEntities = new ArrayList<>(16);
	}
	
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(BATTLE_POSE, BattlePose.INACTIVE);
		this.entityData.define(REVEALED_ELEMENT, Optional.empty());
	}
	
	public static final AttributeSupplier.Builder BuildAttributes() {
		return Mob.createMobAttributes()
		        .add(Attributes.MOVEMENT_SPEED, 0.40D)
		        .add(Attributes.MAX_HEALTH, 500.0D)
		        .add(Attributes.ATTACK_DAMAGE, 10.0D)
		        .add(Attributes.ATTACK_KNOCKBACK, 4.0)
		        .add(Attributes.ARMOR, 18.0D)
		        .add(Attributes.ATTACK_SPEED, 0.5D)
		        .add(Attributes.FOLLOW_RANGE, 8D)
		        .add(Attributes.KNOCKBACK_RESISTANCE, 100.0);
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
	public boolean isPowered() {
		return this.getBattlePose().isInvuln;
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
	
	public @Nullable EMagicElement getRevealedElement() {
		return this.entityData.get(REVEALED_ELEMENT).orElse(null);
	}
	
	protected void setRevealedElement(@Nullable EMagicElement element) {
		this.entityData.set(REVEALED_ELEMENT, Optional.ofNullable(element));
	}
	
	public boolean isDiving() {
		return this.getBattlePose() == BattlePose.DIVING;
	}
	
	public boolean isEthereal() {
		return this.getBattlePose().isEthereal;
	}
	
	public boolean isHidden() {
		return this.getBattlePose() == BattlePose.HIDDEN;
	}
	
	public boolean isRoaring() {
		return this.getBattlePose() == BattlePose.ROARING;
	}
	
	public boolean isCharging() {
		return this.getBattlePose() == BattlePose.CHARGING;
	}
	
	protected float getStateHealthChange() {
		return (this.stateStartHealth - this.getHealth()) / this.getMaxHealth();
	}
	
	@Override
	public boolean isInvisible() {
		return isHidden() || super.isInvisible();
	}
	
	@Override
	public boolean isInvisibleTo(Player player) {
		return isHidden() || super.isInvisibleTo(player);
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
		
		if (source == DamageSource.IN_WALL) {
			return false;
		}
		
		if (!isActivated()) {
			return false;
		}
		
		if (this.isEthereal()) {
			NostrumMagicaSounds.WING_FLAP.play(this);
			if (source.getEntity() instanceof Player player) {
				player.sendMessage(TEXT_ETHEREAL, Util.NIL_UUID);
			}
			return false;
		}
		
		if (this.getBattleState().isInvuln) {
			return false;
		}
		
		if (this.getRevealedElement() != null) {
			// Make physical do basically nothing if elementally charge
			EMagicElement attackElem = null;
			if (source instanceof MagicDamageSource magicSource) {
				attackElem = magicSource.getElement();
			}
			if (attackElem != this.getRevealedElement().getOpposite()) {
				amount *= attackElem == null ? .2f : 1f;
			} else {
				amount *= 1.5f;
				this.setRevealedElement(null);
				this.addEffect(new MobEffectInstance(NostrumEffects.rend, 20 * 30, 1));
				this.addEffect(new MobEffectInstance(NostrumEffects.magicRend, 20 * 30, 2));
				if (!level.isClientSide() && level instanceof ServerLevel server) {
					server.sendParticles(ParticleTypes.BUBBLE_POP, this.getX(), getY() + this.getBbHeight() / 2f, getZ(), 100, 0, 0, 0, .2);
				}
			}
		}
		
		// Cap damage
		amount = Math.min(amount, this.getMaxHealth() / 10);
		
//		// else just vulnerable, so let through and make fall if not already there
//		this.setBattleState(BattleState.FALLEN);
		this.level.playSound(null, this, SoundEvents.ENDER_DRAGON_HURT, getSoundSource(), 1f, 1f);
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
		if (!this.isActivated() || this.isHidden() || this.getBattleState() == BattleState.FALLEN) {
			return;
		}
		
		damageEntity(pusher);
		
		super.push(pusher);
	}
	
	@Override
	public void push(double x, double y, double z) {
		if (this.isActivated()) {
			super.push(x, y, z);
		}
	}
	
	protected void damageEntity(Entity entity) {
		this.doHurtTarget(entity);
	}
	
	@Override
	public EntityDimensions getDimensions(Pose entPose) {
		final EntityDimensions baseDims = super.getDimensions(entPose);
		
		// Some of our states want to override the bounding box
		final BattlePose pose = this.getBattlePose();
		if (pose == BattlePose.FALLEN) {
			return new EntityDimensions(baseDims.width + 1, baseDims.height, baseDims.fixed);
		}
//		else if (pose == BattlePose.CASTING_VULN) {
//			return new EntityDimensions(baseDims.width + 2, baseDims.height + .5f, baseDims.fixed);
//		}
		return baseDims;
	}

	@Override
	public int getMaxHeadXRot() {
		return 80;
	}

	@Override
	public int getMaxHeadYRot() {
		return 75;
	}

	@Override
	public int getHeadRotSpeed() {
		return 30;
	}
	
	@Override
	protected float tickHeadTurn(float curYaw, float change) {
		curYaw = this.yBodyRot;
		change = 0;
		return 0;//super.tickHeadTurn(curYaw, change);
	}
	
	protected BattlePose getPoseForState(BattleState state) {
		BattlePose pose = BattlePose.FLOAT_REVEALED;
		switch (state) {
		case INACTIVE:
			pose = BattlePose.INACTIVE;
			break;
		case ACTIVATING:
			pose = BattlePose.ACTIVATING;
			break;
		case RECOVERING:
			pose = BattlePose.RECOVERING;
			break;
		case FALLEN:
			pose = BattlePose.FALLEN;
			break;
		case FOLLOW_PLAYER_PASSIVE:
			pose = BattlePose.FLOAT_REVEALED;
			break;
		case FOLLOW_PLAYER_SHADOW:
			pose = BattlePose.FLOAT_SHADOW;
			break;
		case CHARGE_ARENA_ATTACK:
			pose = BattlePose.ROARING;
			break;
		case CHARGE_ARENA_SUMMON:
			pose = BattlePose.CHARGING;
			break;
		case HIDE_FOR_SUMMONS:
			pose = BattlePose.HIDDEN;
			break;
		case DIVE:
			pose = BattlePose.DIVING;
			break;
		case MOVE_TO_CENTER:
			pose = BattlePose.FLOAT_SHADOW;
			break;
		}
		return pose;
	}
	
	protected void updatePose() {
		BattlePose pose = this.getPoseForState(this.battleState);
		this.setBattlePose(pose);
	}
	
	protected void setBattleState(BattleState state) {
		if (state != this.battleState) {
			final BattleState oldState = this.battleState;
			this.battleState = state;
			this.updatePose();
			
			onStateChange(oldState, state);
		}
	}
	
	protected void onStateChange(BattleState oldState, BattleState newState) {
		this.stateTicks = this.tickCount;
		this.stateStartHealth = this.getHealth();
		this.stateSubTicks = 0;
		this.stateSubTimer = 0;
		this.setNoGravity(!newState.gravity);
		
		final boolean oldEthereal = getPoseForState(oldState).isEthereal;
		final boolean newEthereal = getPoseForState(newState).isEthereal;
		if (oldEthereal != newEthereal) {
			if (newEthereal) {
				this.doEtherealEffects();
			} else {
				this.doSolidifyEffects();
			}
		}
		
		final boolean oldHasLights = (oldState == BattleState.FOLLOW_PLAYER_PASSIVE || oldState == BattleState.FOLLOW_PLAYER_SHADOW || oldState == BattleState.DIVE);
		final boolean newHasLights = (newState == BattleState.FOLLOW_PLAYER_PASSIVE || newState == BattleState.FOLLOW_PLAYER_SHADOW || newState == BattleState.DIVE);
		if (oldHasLights != newHasLights) {
			if (newHasLights) {
				final float healthRatio = this.getHealth() / this.getMaxHealth();
				if (healthRatio > .9f) {
					arena.activateLights();
				} else if (healthRatio > .5f) {
					// Leave two elements active
					arena.deactivateLights();
					EMagicElement elem1 = EMagicElement.getRandom(random);
					while (elem1 == EMagicElement.NEUTRAL || elem1 == EMagicElement.WIND || elem1 == EMagicElement.EARTH) {
						elem1 = EMagicElement.getRandom(random);
					}
					
					EMagicElement elem2 = EMagicElement.getRandom(random);
					while (elem2 == EMagicElement.NEUTRAL || elem2 == EMagicElement.WIND || elem2 == EMagicElement.EARTH || elem2 == elem1) {
						elem2 = EMagicElement.getRandom(random);
					}
					arena.activateLight(elem1);
					arena.activateLight(elem2);
				} else {
					arena.deactivateLights();
					EMagicElement elem1 = EMagicElement.getRandom(random);
					while (elem1 == EMagicElement.NEUTRAL || elem1 == EMagicElement.WIND || elem1 == EMagicElement.EARTH) {
						elem1 = EMagicElement.getRandom(random);
					}
					arena.activateLight(elem1);
				}
			} else {
				arena.deactivateLights();
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
				this.arena.clearExtraBlocks();
			}
		}
		
	}
	
	protected boolean hasArenaChallengePhase(float healthRatio) {
		return healthRatio <= .8f;
	}
	
	protected int getRandomArenaChallengeDuration(float healthRatio) {
		return 200 // 10 seconds
				+ (int) (20 * ((.8f - healthRatio) / .1f)) // Get how much we've lost from .8, and then add a second for every 10%
				;
	}
	
	protected boolean hasSummonChallengePhase(float healthRatio) {
		return healthRatio <= .5f;
	}
	
	protected int getSummonChallengeCount(float healthRatio) {
		return 2
				+ (int) (1 * ((.5f - healthRatio) / .1f)) // extra 1 for every 10% beyond 50%
				;
	}
	
	protected boolean hasShadowFollowPhase(float healthRatio) {
		return healthRatio <= .95f;
	}
	
	protected BattleState getNextPhaseState(boolean wantChargeState) {
		final float healthRatio = this.getHealth() / this.getMaxHealth();
		
		// If wantChargeState, we want to do a charging challenge state instead of a follow state.
		// So first pick a charging state, if we have any at this point
		if (wantChargeState) {
			if (this.hasSummonChallengePhase(healthRatio) || this.hasArenaChallengePhase(healthRatio)) {
				return BattleState.MOVE_TO_CENTER;
			}
		}
		
		// Else want/have to do a follow phase. Pick the appropriate start state for health
		if (hasShadowFollowPhase(healthRatio)) {
			return BattleState.FOLLOW_PLAYER_SHADOW;
		}
		
		// else
		return BattleState.FOLLOW_PLAYER_PASSIVE;
	}
	
	protected ChargeAttackPhase getNextAttackPhase() {
		// Just random?
		
		@SuppressWarnings("unchecked")
		Supplier<ChargeAttackPhase>[] phases = new Supplier[]{
				() -> new ChargeAttackPhase(new ShadowDragonAttackPattern.FloorRings(arena), spells.randomBombSpell(random), 1f),
				() -> new ChargeAttackPhase(new ShadowDragonAttackPattern.FloorWave(arena), spells.randomBombSpell(random), 1f),
				() -> new ChargeAttackPhase(ShadowDragonAttackPattern.FloorChunks.Random(arena), spells.randomBombSpell(random), 1f),
				() -> new ChargeAttackPhase(ShadowDragonAttackPattern.FloorChunks.Sequential(arena), spells.randomBombSpell(random), 1f)
		};
		
		return phases[random.nextInt(phases.length)].get();
	}
	
	protected void doEtherealEffects() {
		if (!level.isClientSide() && level instanceof ServerLevel server) {
			server.sendParticles(ParticleTypes.POOF, this.getX(), getY() + this.getBbHeight() / 2f, getZ(), 20, 0, 0, 0, .2);
		}
	}
	
	protected void doSolidifyEffects() {
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.SKELETON_CONVERTED_TO_STRAY, SoundSource.HOSTILE, 1f, 1f);
	}
	
	protected void castSpellAt(Spell spell, float efficiency, LivingEntity target) {
		this.setTarget(target);
		this.lookAt(target, 360f, 180f);
		spell.cast(this, SpellCastProperties.makeWithTarget(efficiency, target));
	}
	
	protected void castSpellAt(Spell spell, float efficiency, Vec3 targetPos) {
		this.lookControl.setLookAt(targetPos.x, targetPos.y, targetPos.z, 360f, 180f);
		
		{
			double d0 = (targetPos.x + .5) - getX();
			double d2 = (targetPos.z + .5) - getZ();
			double d1 = (targetPos.y + 1) - (getY() + getEyeHeight());
			
			double d3 = (double)Math.sqrt(d0 * d0 + d2 * d2);
			float f = (float)(Math.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
			float f1 = (float)(-(Math.atan2(d1, d3) * (180D / Math.PI)));
			setXRot(f1);
			setYRot(f);
		}
		
		spell.cast(this, SpellCastProperties.makeSimple(efficiency));
	}
	
	protected void doSummonEffects(Entity summon) {
		TameLightning bolt = new TameLightning(NostrumEntityTypes.tameLightning, level, summon.getX(), summon.getY(), summon.getZ());
		bolt.setEntityToIgnore((LivingEntity) summon);
		bolt.setDamage(0);
		((ServerLevel) level).addFreshEntity(bolt);
	}
	
	protected void discardSummons() {
		for (Entity e : this.summonedEntities) {
			if (e.isAlive()) {
				e.discard();
				doSummonEffects(e);
			}
		}
		this.summonedEntities.clear();
	}
	
	protected void tickSummons() {
		Iterator<Entity> it = this.summonedEntities.iterator();
		while (it.hasNext()) {
			if (!it.next().isAlive()) {
				it.remove();
			}
		}
	}
	
	protected void addSummon(Entity e) {
		this.summonedEntities.add(e);
	}
	
	protected boolean hasSummons() {
		return !this.summonedEntities.isEmpty();
	}
	
	protected Entity spawnSummon(float healthRatio, BlockPos spot) {
		// Always just summon elemental golems?
		EntityType<?> summonType = null;
		EMagicElement elem = EMagicElement.getRandom(random);
		switch (elem) {
		case EARTH:
			summonType = NostrumEntityTypes.golemEarth;
			break;
		case ENDER:
			summonType = NostrumEntityTypes.golemEnder;
			break;
		case FIRE:
			summonType = NostrumEntityTypes.golemFire;
			break;
		case ICE:
			summonType = NostrumEntityTypes.golemIce;
			break;
		case LIGHTNING:
			summonType = NostrumEntityTypes.golemLightning;
			break;
		case NEUTRAL:
			summonType = NostrumEntityTypes.golemNeutral;
			break;
		case WIND:
			summonType = NostrumEntityTypes.golemWind;
			break;
		}
		
		Entity summon = summonType.create(level);
		summon.setPos(spot.getX() + .5, spot.getY(), spot.getZ() + .5);
		
		if (summon instanceof MagicGolemEntity golem) { // TODO expand this
			golem.setOwnerUUID(this.getUUID());
		}
		
		level.addFreshEntity(summon);
		
		return summon;
	}
	
	@Override
	public void tickDeath() {
		++this.deathTime;
		this.setNoGravity(true);
		
		this.move(MoverType.SELF, new Vec3(0, 0.025, 0));
		
		if (this.deathTime == 100 && !this.level.isClientSide()) {
			this.arena.activateLights();
			ExperienceOrb.award((ServerLevel) level, position(), this.xpReward);
			
			this.level.broadcastEntityEvent(this, (byte)60);
			this.remove(Entity.RemovalReason.KILLED);
		}
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (this.level.isClientSide()) {
			this.clientTick();
		}
	}
	
	@Override
	protected void customServerAiStep() {
		this.bossInfo.setProgress(this.getHealth() / this.getMaxHealth());
		
		this.aggroTable.decayTick();
		this.tickSummons();
		if (this.homeBlock == null) {
			this.homeBlock = getOnPos(); // Persisted so should only happen once per spawning
		}
		if (this.arena == null) { // not persisted, so will happen every time reloladed
			this.arena = ShadowDragonArena.Capture(getLevel(), this.homeBlock);
			this.arena.resetArena();
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
		case FALLEN:
			this.fallenTick();
			break;
		case RECOVERING:
			this.recoveringTick();
			break;
		case FOLLOW_PLAYER_PASSIVE:
			this.followPlayerPassiveTick();
			break;
		case FOLLOW_PLAYER_SHADOW:
			this.followPlayerShadowTick();
			break;
		case DIVE:
			this.diveAttackTick();
			break;
		case CHARGE_ARENA_ATTACK:
			this.arenaAttackTick();
			break;
		case CHARGE_ARENA_SUMMON:
			this.summonChargeTick();
			break;
		case HIDE_FOR_SUMMONS:
			this.hideTick();
			break;
		case MOVE_TO_CENTER:
			this.moveToCenterTick();
			break;
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
			NostrumParticles.FILLED_ORB.spawn(level, new SpawnParams(1 + (this.getTicksInPose() / 20), getX(), getY() + this.getBbHeight() / 2, getZ(), 3,
					30, 10, new TargetLocation(this, true)
					).color(0xA0E5E52D).setTargetBehavior(TargetBehavior.JOIN));
		} else if (this.isCharging()) {
			NostrumParticles.FILLED_ORB.spawn(level, new SpawnParams(10, getX(), getY() + this.getBbHeight() / 2, getZ(), 5,
					40, 20, new TargetLocation(this, true)
					).color(EMagicElement.LIGHTNING.getColor()).setTargetBehavior(TargetBehavior.ORBIT_LAZY));
		} else if (this.isRoaring()) {
			NostrumParticles.GLOW_ORB.spawn(level, new SpawnParams(5, getX(), getY() + this.getBbHeight(), getZ(), .25,
					60, 40, new Vec3(0, .15, 0), new Vec3(.1, .1, .1)
					).color(EMagicElement.ENDER.getColor()));
		}
		
//		if (this.isDeadOrDying() && this.deathTime <= 40 && this.deathTime % 20 == 0) {
//			NostrumParticles.LIGHT_EXPLOSION.spawn(level, new SpawnParams(40, getX(), getY() + this.getBbHeight() / 2, getZ(), .125, 100, 20, new Vec3(0, .01, 0), Vec3.ZERO));
//		}
		if (this.isDeadOrDying()) {
			final int period = Math.max(5, 20 - (this.deathTime / 5));
			if (this.deathTime % period == 0) {
				level.addParticle(ParticleTypes.EXPLOSION, getX() + random.nextGaussian() * 1, getY() + random.nextGaussian() * 1, getZ() + random.nextGaussian() * 1, 0, 0, 0);
				level.playLocalSound(getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE, this.getSoundSource(), .5f, .8f, false);
			}
		}
	}
	
	protected void inactiveTick() {
		
		if (!this.getOnPos().equals(this.homeBlock)) {
			// Presumably reloaded. Likely because player died and ran back.
			// Will mean server crashes in b oss fight reset, but that might be
			// better than loading in to an active boss fight
			if (NostrumMagica.isBlockLoaded(level, this.homeBlock)) {
//				if (this.arena != null) {
//					this.arena.resetArena(); We do this every time the arena is captured
//				}
				this.setPos(Vec3.atBottomCenterOf(this.homeBlock.above()));
				this.setHealth(this.getMaxHealth());
			}
			
			return;
		}
		
		// Look for correctly-configured lights to start activating
		if (this.arena.areLightsUnblocked()) {
			this.activate();
		}
		
	}
	
	protected void activatingTick() {
		if (this.stateSubTicks++ > 80) {
			this.setBattleState(BattleState.FOLLOW_PLAYER_PASSIVE);
			
			NostrumParticles.FILLED_ORB.spawn(level, new SpawnParams(100, getX(), getY() + this.getBbHeight() / 2, getZ(), .25,
					30, 10, new Vec3(0, .2, 0), new Vec3(.2, .05, .2)
					).gravity(true).color(0xA0E5E52D).setTargetBehavior(TargetBehavior.JOIN));
			
			this.playSound(SoundEvents.WITHER_SPAWN, 1f, 1f);
		}
	}
	
	protected void recoveringTick() {
		
	}
	
	protected void fallenTick() {
		if (this.lastHealthThresholdPassed > .5f && this.getHealth() / this.getMaxHealth() <= .5f) {
			this.lastHealthThresholdPassed = .5f;
			this.setRevealedElement(null);
			this.setBattleState(BattleState.MOVE_TO_CENTER);
		} else if (this.lastHealthThresholdPassed > .8f && this.getHealth() / this.getMaxHealth() <= .8f) {
			this.lastHealthThresholdPassed = .8f;
			this.setRevealedElement(null);
			this.setBattleState(BattleState.MOVE_TO_CENTER);
		} else if (this.stateSubTicks++ > 100 || this.getStateHealthChange() > .1f) {
			this.setBattleState(this.getNextPhaseState(false));
		}
	}
	
	protected boolean followPlayerGenericTick(LivingEntity target) {
		final Vec3 targPos = target.position();
		boolean needsMove = false;
		
		final double yDistIdeal = 4;
		final double hDistIdeal = 5;
		
		// Treat y and xz different, and provide a consistent y level compared to the player's
		final double idealY = targPos.y + yDistIdeal;
		if (Math.abs(idealY - this.getY()) > .2) {
			needsMove = true;
		}
		
		if (!needsMove) {
			// Eval horizontal
			final double hDistSqr = Mth.square(this.getX() - targPos.x) + Mth.square(this.getZ() - targPos.z);
			if (hDistSqr > Mth.square(hDistIdeal + 1.5)) {
				needsMove = true;
			}
		}
		
		if (needsMove) {
			final Vec3 idealPos;
			final Vec3 diffToMe = targPos.subtract(position());
			
			final Vec3 flatDiff = new Vec3(diffToMe.x, 0, diffToMe.z).normalize().scale(-hDistIdeal);
			
			idealPos = targPos.add(flatDiff).add(0, yDistIdeal, 0);
			
			final Vec3 diffToIdeal = idealPos.subtract(position());
			this.setDeltaMovement(this.getDeltaMovement().scale(.8).add(diffToIdeal.normalize().scale(.1)));
			this.hasImpulse = true;
		} else {
			this.setDeltaMovement(this.getDeltaMovement().scale(.8));
			this.hasImpulse = true;
		}
		
		this.getLookControl().setLookAt(target, 10, 80);
		return needsMove;
	}
	
	protected void followPlayerPassiveTick() {
		if (this.stateSubTicks++ == 0) {
			// Decide how long we'll stay in this state
			stateSubTimer = 20 * this.random.nextInt(4) + 120;
			if (this.diveCount <= 0) {
				diveCount = this.random.nextInt(4) + 2;
			}
		} else if (this.lastHealthThresholdPassed > .5f && this.getHealth() / this.getMaxHealth() <= .5f) {
			this.lastHealthThresholdPassed = .5f;
			this.setRevealedElement(null);
			this.setBattleState(BattleState.MOVE_TO_CENTER);
		} else if (this.lastHealthThresholdPassed > .8f && this.getHealth() / this.getMaxHealth() <= .8f) {
			this.lastHealthThresholdPassed = .8f;
			this.setRevealedElement(null);
			this.setBattleState(BattleState.MOVE_TO_CENTER);
		} else if (this.stateSubTicks >= stateSubTimer || this.getStateHealthChange() > .1f) {
			if (this.getStateHealthChange() > .1f) {
				diveCount--; // Speed to more intense attacks
			}
			this.setRevealedElement(null);
			this.setBattleState(BattleState.DIVE);
		} else {
			final @Nullable LivingEntity target = this.aggroTable.getMainTarget();
			if (target != null) {
				this.followPlayerGenericTick(target);
			}
			
			if (this.stateSubTicks % 20 == 0 && random.nextInt(2) == 0) {
				this.playSound(SoundEvents.PIGLIN_BRUTE_AMBIENT, 1f, .2f);
			}
		}
	}

	protected void followPlayerShadowTick() {
		if (this.stateSubTicks++ == 0) {
			// Decide how long we'll stay in this state
			stateSubTimer = 20 * this.random.nextInt(4) + 80;
			if (this.diveCount <= 0) {
				diveCount = this.random.nextInt(4) + 2;
			}
		} else if (this.stateSubTicks >= stateSubTimer || this.getStateHealthChange() > .1f) {
			if (this.getStateHealthChange() > .1f) {
				diveCount--; // Speed to more intense attacks
			}
			this.setBattleState(BattleState.DIVE);
		} else if (this.arena.getLaserElementBelow(this) != null) {
			this.setRevealedElement(this.arena.getLaserElementBelow(this));
			this.setBattleState(BattleState.FOLLOW_PLAYER_PASSIVE);
		} else {
			final @Nullable LivingEntity target = this.aggroTable.getMainTarget();
			if (target != null) {
				this.followPlayerGenericTick(target);
			}
			
			if (this.stateSubTicks % 20 == 0 && random.nextInt(2) == 0) {
				this.playSound(SoundEvents.PIGLIN_BRUTE_AMBIENT, 1f, .2f);
			}
		}
	}

	protected void diveAttackTick() {
		final @Nullable LivingEntity target = this.aggroTable.getMainTarget();
		if (target == null) {
			this.setBattleState(this.getNextPhaseState(--this.diveCount <= 0));
			return;
		}
		
		if (this.stateSubTicks++ == 0) {
			// Capture target location
			stateTargetPos = target.position().add(0, target.getBbHeight() / 2f, 0);
			level.playSound(null, this, SoundEvents.ENDER_DRAGON_AMBIENT, getSoundSource(), 1f, 1f);
		} else if (stateTargetPos != null) { // use null for target pos to mark that we've dove and are coming out of the dive
			this.lookControl.setLookAt(stateTargetPos.x, stateTargetPos.y, stateTargetPos.z, 90f, 90f);
			Vec3 diff = stateTargetPos.subtract(position());
			if (stateSubTicks < 10) {
				// Reel back
				this.setDeltaMovement(diff.normalize().scale(-.1));
			} if (stateSubTicks > 60) {
				// fail safe for if we get stuck
				stateTargetPos = null;
				stateSubTicks = 1;
			} else {
				final Vec3 forwardVec = diff.normalize().scale(.1).multiply(1, 2, 1);
				if (stateSubTicks == 10) {
					// just started swooping
					this.setDeltaMovement(forwardVec);
				} else {
					this.setDeltaMovement(this.getDeltaMovement().scale(.8f).add(forwardVec));
				}
				
				if (diff.lengthSqr() < .25) {
					// Reached it, so stop swooping
					stateTargetPos = null;
					stateSubTicks = 1;
				}
			}
			this.hasImpulse = true;
		} else if (stateSubTicks < 20) { // target is null and ticks are high
			// arch back up from dive by just slightly adding to y motion
			if (this.getDeltaMovement().y < .1) {
				this.setDeltaMovement(this.getDeltaMovement().add(0, .025, 0));
				this.hasImpulse = true;
			}
		} else {
			this.setBattleState(this.getNextPhaseState(--this.diveCount <= 0));
		}
	}
	
	protected boolean arenaGotoCenterTick() {
		BlockPos targPos = arena.getCenterPillarFloatPos();
		final Vec3 centerPos = Vec3.atCenterOf(targPos);
		final Vec3 diff = centerPos.subtract(position());
		if (diff.lengthSqr() < .25) {
			this.setDeltaMovement(Vec3.ZERO);
			return true;
		} else {
			this.lookControl.setLookAt(centerPos);
			this.setDeltaMovement(this.getDeltaMovement().scale(.8f).add(diff.normalize().scale(.1)));
			this.hasImpulse = true;
			return false;
		}
	}
	
	protected void moveToCenterTick() {
		if (!arenaGotoCenterTick()) {
			// Still moving to center
		} else {
			// Decide which attack state to switch to and switch to it
			final float healthRatio = this.getHealth() / this.getMaxHealth();
			
			// Only two charging states. First roll for more difficult one. Then always take non-difficult, if we have it.
			// (check dive count > 0 which would indicate we came here early, like on health boundary check)
			if ((diveCount > 0 || random.nextBoolean()) && this.hasSummonChallengePhase(healthRatio)) {
				this.setBattleState(BattleState.CHARGE_ARENA_SUMMON);
				return;
			}
			
			if (this.hasArenaChallengePhase(healthRatio)) {
				this.setBattleState(BattleState.CHARGE_ARENA_ATTACK);
				return;
			}
			
			// else something has went wrong...
			NostrumMagica.logger.warn("Shadow dragon AI ran into a case where no challenge was available");
			this.setBattleState(this.getNextPhaseState(false));
		}
	}

	protected void arenaAttackTick() {
		if (stateSubTicks++ == 0) {
			this.attackPhase = getNextAttackPhase();
			if (this.stateSubTimer == 0) {
				// Figure out how many attack cycles we want...
				final float healthMissingBeyondStart = (1f - (this.getHealth() / this.getMaxHealth())) - .2f; // arena challeng starts at 80%, so ignore 20% of damage
				this.stateSubTimer = 1 + (int) (healthMissingBeyondStart / .1f);
				
			}
			this.playSound(SoundEvents.ENDER_DRAGON_AMBIENT, 1f, .25f);
		} else if (stateSubTicks < 40) {
			; // charging
		} else {
			final int tickInterval = 20; // could be based on damage
			if (stateSubTicks % tickInterval == 0) {
				//final int spawnCallIdx = ((stateSubTicks - 40) % tickInterval);
				final float healthMissingBeyondStart = (1f - (this.getHealth() / this.getMaxHealth())) - .2f; // arena challeng starts at 80%, so ignore 20% of damage
				int batchesToDispatch;
				
				if (attackPhase.pattern().supportsMultiBatching()) {
					batchesToDispatch = 1 + (int)(healthMissingBeyondStart / .1f);
				
					// Cap batches base on total batch count from pattern
					final int batchesPerPattern = this.attackPhase.pattern().getTargetCount() / attackPhase.pattern().getRecommendedCountPerDifficulty();
					if (batchesToDispatch > batchesPerPattern / 2) {
						batchesToDispatch = batchesPerPattern / 2;
					}
				} else {
					batchesToDispatch = 1;
				}
				
				boolean doneCasting = false;
				if (attackPhase.pattern().getRecommendedCountPerDifficulty() <= 0) {
					doneCasting = true;
				} else {
					for (int i = 0; i < batchesToDispatch * attackPhase.pattern().getRecommendedCountPerDifficulty(); i++) {
						Vec3 targ = attackPhase.pattern().getNextTarget();
						if (targ == null) {
							/////// This is where we are done with an attack pattern and need to change!
							doneCasting = true;
							break;
						}
						
						this.castSpellAt(attackPhase.spell(), attackPhase.power(), targ);
					}
				}
				
				if (doneCasting) {
					if (--stateSubTimer > 0) {
						// Stay in arena attack, but reset
						stateSubTicks = 0;
					} else {
						this.setBattleState(this.getNextPhaseState(false));
					}
				}
				
				if (random.nextInt(4) == 0) {
					this.playSound(SoundEvents.ENDER_DRAGON_AMBIENT, 1f, .5f);
				}
			}
		}
	}
	
	protected void summonChargeTick() {
		if (stateSubTicks++ == 0) {
			final float healthRatio = this.getHealth() / this.getMaxHealth();
			this.stateSubTimer = this.getSummonChallengeCount(healthRatio);;
			this.playSound(SoundEvents.ENDER_DRAGON_GROWL, 1f, .75f);
		} else if (stateSubTicks % 20 == 0) {
			// Spawn one summon
			{
				final float healthRatio = this.getHealth() / this.getMaxHealth();
				BlockPos spot = this.arena.getRandomPillarPos(random);
				Entity summon = this.spawnSummon(healthRatio, spot.above());
				doSummonEffects(summon);
				this.addSummon(summon);
			}
			
			if (--stateSubTimer <= 0) {
				this.playSound(SoundEvents.ENDER_DRAGON_GROWL, 1f, .75f);
				this.setBattleState(BattleState.HIDE_FOR_SUMMONS);
			}
		}
	}

	protected void hideTick() {
		final @Nullable LivingEntity target = this.aggroTable.getMainTarget();
		if (target != null) {
			this.followPlayerGenericTick(target);
		}
		
		if (stateSubTicks++ > 20 * 60 * 3) {
			// have a 3 minute timer...
			this.discardSummons();
			this.setBattleState(this.getNextPhaseState(false));
		} else if (!this.hasSummons()) {
			// Summons beat, so... fall?
			this.setBattleState(BattleState.FALLEN);
			this.playSound(SoundEvents.ENDER_DRAGON_HURT, 1f, .5f);
		} else {
			if (this.stateSubTicks % 20 == 0 && random.nextInt(4) == 0) {
				this.playSound(SoundEvents.PIGLIN_BRUTE_AMBIENT, 1f, .2f);
			}
		}
	}
	
}
