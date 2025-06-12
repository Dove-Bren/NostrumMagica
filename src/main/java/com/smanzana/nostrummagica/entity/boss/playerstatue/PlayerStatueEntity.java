package com.smanzana.nostrummagica.entity.boss.playerstatue;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.client.particles.ParticleTargetBehavior.TargetBehavior;
import com.smanzana.nostrummagica.entity.AggroTable;
import com.smanzana.nostrummagica.serializer.PlayerStatuePoseSerializer;
import com.smanzana.nostrummagica.util.TargetLocation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class PlayerStatueEntity extends Mob implements PowerableMob {
	
	public static enum BattleState {
		INACTIVE, // Waiting for activation
		ACTIVATING, // ACTIVATING
		JUMPING, // Is moving around and jumping to attack the player. Each jump reduces any current armor
		TOPPLED, // Struck by the player and falling down, vulnerable. Includings falling over animations 
		RECOVERING, // Getting back up from being toppled. Invlunerable during this stage
	}
	
	/**
	 * What animation pose the entity is in.
	 * Not intended to be a direct match of battle state (although as of writing it has become that)
	 * This is communicated to the client, and the client counts how many ticks it's been in the same pose
	 * for animating. It is not used on the server and doesn't control entity logic.
	 */
	public static enum BattlePose {
		INACTIVE,
		UPRIGHT,
		TOPPLED,
		ACTIVATING,
		RECOVERING,
	}

	public static final String ID = "player_statue";
	
	protected static final EntityDataAccessor<BattlePose> BATTLE_POSE = SynchedEntityData.<BattlePose>defineId(PlayerStatueEntity.class, PlayerStatuePoseSerializer.instance);
	protected static final EntityDataAccessor<Integer> SHIELD_CHARGES = SynchedEntityData.<Integer>defineId(PlayerStatueEntity.class, EntityDataSerializers.INT);
	
	private final ServerBossEvent bossInfo = (ServerBossEvent) new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.NOTCHED_6).setDarkenScreen(true);
	
	private boolean activated;
	private AggroTable<LivingEntity> aggroTable; // null on client
	protected BattleState battleState;
	protected int stateTicks; // tick count when state was started
	protected float stateStartHealth;
	protected int stateSubTicks; // counter for logic per each state
	
	// Arena information
	private AABB arenaBounds;
	
	// Animation variables (client side)
	private BattlePose lastPose;
	private int poseTicks; // ticks we've been in the same pose
	
	public PlayerStatueEntity(EntityType<? extends PlayerStatueEntity> type, Level worldIn) {
		super(type, worldIn);
        this.noCulling = true;
        this.xpReward = 1250;
		
        if (!worldIn.isClientSide()) {
			this.aggroTable = new AggroTable<>((ent) -> {
				return PlayerStatueEntity.this.getSensing().hasLineOfSight(ent);
			});
        }
		this.battleState = BattleState.INACTIVE;
	}
	
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(BATTLE_POSE, BattlePose.UPRIGHT);
		this.entityData.define(SHIELD_CHARGES, 0);
	}
	
	public static final AttributeSupplier.Builder BuildAttributes() {
		return Mob.createMobAttributes()
	        .add(Attributes.MOVEMENT_SPEED, 0.00D)
	        .add(Attributes.MAX_HEALTH, 200.0D)
	        .add(Attributes.ATTACK_DAMAGE, 8.0D)
	        .add(Attributes.ATTACK_KNOCKBACK, 3.0)
	        .add(Attributes.ARMOR, 8.0D)
	        .add(Attributes.ATTACK_SPEED, 0.5D)
	        .add(Attributes.FOLLOW_RANGE, 8D)
	        .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }
	
	@Override
	protected void customServerAiStep() {
		super.customServerAiStep();
		
		this.bossInfo.setProgress(this.getHealth() / this.getMaxHealth());
	}
	
	@Override
	public void startSeenByPlayer(ServerPlayer player) {
		super.startSeenByPlayer(player);
		if (activated) {
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
	public void tick() {
		super.tick();
		
		if (this.level.isClientSide()) {
			this.clientTick();
		} else {
			this.aggroTable.decayTick();
			if (this.arenaBounds == null) {
				this.arenaBounds = discoverArena();
			}
			
			for (Player player : this.getLevel().getNearbyPlayers(TargetingConditions.forCombat(), this, arenaBounds)) {
				this.aggroTable.addDamage(player, .2f);
			}
			
			switch (this.battleState) {
			case INACTIVE:
				this.inactiveTick();
				break;
			case JUMPING:
				this.jumpingTick();
				break;
			case RECOVERING:
				this.recoveringTick();
				break;
			case TOPPLED:
				this.toppledTick();
				break;
			case ACTIVATING:
				this.activatingTick();
				break;
			}
		}
	}
	
	protected boolean attackerIsBelow(Entity attacker) {
		return attacker.getEyeY() < this.getY()
				&& this.getDeltaMovement().y < 0 // on the way back down
				;
	}
	
	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (source.getEntity() == this) {
			return false;
		}
		
		if (source == DamageSource.IN_WALL || source == DamageSource.FALL) {
			//this.moveForced(this.getPositionVec().add(0, 1, 0));
			return false;
		}
		
		// If not activated, active now if directly hit by player
		if (!activated && !source.isProjectile() && source.getEntity() != null && source.getEntity() instanceof Player) {
			this.activate();
			return false;
		}
		
		// Have to be in vulnerable, and attacks must be from below
		if (!isVulnerableAnywhere() && !isVulnerableBelow()) {
			return false;
		}
		
		if (isVulnerableBelow() && source.getEntity() != null && !attackerIsBelow(source.getEntity())) {
			return false;
		}
		
		///// END maybe not eligible checks
		if (this.getShieldCharges() > 0) {
			this.consumeShieldCharge();
			return false;
		}
		
		// armor check and consumption
		
		if (source.getEntity() == null || !(source.getEntity() instanceof Player)
				|| !((Player) source.getEntity()).isCreative()) {
			amount = Math.min(amount, 10f);
		}
		
		if (!this.level.isClientSide && source.getEntity() != null) {
			Entity ent = source.getEntity();
			if (ent instanceof LivingEntity && ent != this) {
				this.aggroTable.addDamage((LivingEntity) ent, amount);
			}
			
			if (isVulnerableBelow()) {
				this.setBattleState(BattleState.TOPPLED);
			}
		}
		
		return super.hurt(source, amount);
	}
	
	@Override
	public boolean causeFallDamage(float damage, float distMult, DamageSource dmgType) {
		if (!level.isClientSide()) {
			// extra particles. Hasto be server side :(
			//new BlockParticleOption(ParticleTypes.BLOCK, p_20992_).setPos(p_20993_)
			final BlockPos pos = new BlockPos(getX(), getY() - .2, getZ());
			final BlockState state = level.getBlockState(pos);
			((ServerLevel)this.level).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state).setPos(pos), this.getX(), this.getY() + .2f, this.getZ(), 300, 0.0D, 0.0D, 0.0D, (double)50f);
			
			
		}
		return super.causeFallDamage(damage, distMult, dmgType);
	}
	
	@Override
	public boolean isPickable() {
		return true; // Wants parts to be collided with, not main entity
		//return super.canBeCollidedWith();
	}
	
	@Override
	public boolean isPushable() {
		return true;
	}
	
	protected boolean entityBeneath(Entity other) {
		return this.getY() > other.getY() + other.getBbHeight() / 2f;
	}
	
	@Override
	public void push(Entity pusher) {
		if (this.isAlive() && entityBeneath(pusher) && this.getDeltaMovement().y < 0) {
			
			damageEntity(pusher);
		}
		return;
	}
	
	@Override
	protected void doPush(Entity toPush) {
		if (this.isAlive() && entityBeneath(toPush) && this.getDeltaMovement().y < 0) {
			damageEntity(toPush);
		}
		return;
	}
	
	protected void damageEntity(Entity target) {
		if (this.doHurtTarget(target)) {
			this.playSound(SoundEvents.ANVIL_LAND, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
			//target.hurt(DamageSource.mobAttack(this), this.getAttackDamage());
		}
	}
	
	protected float getAttackDamage() {
		return (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
	}
	
	@Override
	public void playerTouch(Player player) {
		super.playerTouch(player);
	}
	
	public BattlePose getBattlePose() {
		return this.entityData.get(BATTLE_POSE);
	}
	
	protected void setBattlePose(BattlePose pose) {
		this.entityData.set(BATTLE_POSE, pose);
	}
	
	public int getShieldCharges() {
		return this.entityData.get(SHIELD_CHARGES);
	}
	
	protected void setShieldCharges(int charges) {
		this.entityData.set(SHIELD_CHARGES, charges);
	}
	
	protected void consumeShieldCharge() {
		this.setShieldCharges(Math.max(0, this.getShieldCharges() - 1));
		if (this.getShieldCharges() <= 0) {
			level.playSound(null, this, SoundEvents.GLASS_BREAK, getSoundSource(), 1f, 1f);
		} else {
			level.playSound(null, this, SoundEvents.CHAIN_BREAK, getSoundSource(), 1f, 1f);
			((ServerLevel) level).sendParticles(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), 10, 0.2D, 0.2D, 0.2D, (double)0f);
		}
	}

	@Override
	public boolean isPowered() {
		return this.getShieldCharges() > 0;
	}
	
	
	protected void updatePose() {
		BattlePose pose = BattlePose.UPRIGHT;
		switch (this.battleState) {
		case JUMPING:
			pose = BattlePose.UPRIGHT;
			break;
		case RECOVERING:
			pose = BattlePose.RECOVERING;
			break;
		case TOPPLED:
			pose = BattlePose.TOPPLED;
			break;
		case ACTIVATING:
			pose = BattlePose.ACTIVATING;
			break;
		case INACTIVE:
			pose = BattlePose.INACTIVE;
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
		if (newState == BattleState.RECOVERING) {
			// Shield
			this.setShieldCharges(getShieldChargesForPhase());
			this.playSound(SoundEvents.CHAIN_PLACE, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
		} else if (oldState == BattleState.INACTIVE) {
			this.setShieldCharges(getShieldChargesForPhase());
		}
	}
	
	protected void activate() {
		if (!this.activated) {
			this.activated = true;
			// 'activate' boss bar for all players nearby
			if (!getLevel().isClientSide()) {
				for (Player player : ((ServerLevel) getLevel()).getNearbyPlayers(TargetingConditions.forNonCombat(), this, this.getBoundingBox().inflate(30))) {
					this.bossInfo.addPlayer((ServerPlayer) player);
				}
				this.setBattleState(BattleState.ACTIVATING);
			}
		}
		
	}
	
	public boolean isVulnerableAnywhere() {
		return this.battleState == BattleState.TOPPLED;
	}
	
	public boolean isVulnerableBelow() {
		return this.battleState == BattleState.JUMPING;
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
	
	protected void jump(Vec3 target) {
		// target is where we want to end up.
		// We jump in the right direction with a too-high horizontal speed, but stop ourselves horizontally
		// when we get there
		this.jumpFromGround();
		
		// Add extra vertical here,, and horizontal component
		final Vec3 motion = this.getDeltaMovement();
		final Vec3 posDiff = target.subtract(position()).multiply(1, 0, 1).normalize().scale(.5f);
		this.setDeltaMovement(motion.x + posDiff.x, motion.y + .4, motion.z + posDiff.z);
		
		this.playSound(SoundEvents.GRINDSTONE_USE, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
	}
	
	/**
	 * Calculate how many shield charges to use for current hp %
	 * @return
	 */
	protected int getShieldChargesForPhase() {
		final float perc = this.getHealth() / this.getMaxHealth();
		final int base = 0;
		return base + (int) ((1-perc) * 8);
	}
	
	protected AABB discoverArena() {
		// Assume we are on ground. Find the block below us and then find the horizontal extents of it
		
		// do a simplified walk where we assume it's a rectangle and that if we go east/south/west/north from our c urrent space, we'll
		// accurately detect the bounds
		final int minX;
		final int maxX;
		final int minZ;
		final int maxZ;
		final int minY;
		final int maxY;
		final MutableBlockPos cursor = new MutableBlockPos();
		
		cursor.set(getOnPos());
		while (true) {
			cursor.move(Direction.SOUTH); // +z
			if (!level.isEmptyBlock(cursor.above())) {
				break;
			}
		}
		maxZ = cursor.getZ() - 1; // -1 cause where it's at is where it failed
		
		cursor.set(getOnPos());
		while (true) {
			cursor.move(Direction.NORTH); // -z
			if (!level.isEmptyBlock(cursor.above())) {
				break;
			}
		}
		minZ = cursor.getZ() + 1;
		
		cursor.set(getOnPos());
		while (true) {
			cursor.move(Direction.EAST); // +x
			if (!level.isEmptyBlock(cursor.above())) {
				break;
			}
		}
		maxX = cursor.getX() - 1;
		
		cursor.set(getOnPos());
		while (true) {
			cursor.move(Direction.WEST); // -x
			if (!level.isEmptyBlock(cursor.above())) {
				break;
			}
		}
		minX = cursor.getX() + 1;
		
		minY = this.getOnPos().getY();
		cursor.set(getOnPos());
		while (true) {
			cursor.move(Direction.UP); // +y
			if (!level.isEmptyBlock(cursor) && !level.getBlockState(cursor).getCollisionShape(level, cursor).isEmpty()) {
				break;
			}
		}
		maxY = cursor.getY() - 1;
		
		
		return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
	}
	
	protected @Nullable Vec3 getJumpTarget() {
		LivingEntity target = this.aggroTable.getMainTarget();
		if (target == null) {
			return null;
		}
		
		if (this.arenaBounds.intersects(target.getBoundingBox())) {
			final Vec3 pos = target.position();
			return new Vec3(pos.x, getY(), pos.z);
		}
		return null;
	}
	
	protected void turnTowardsMotion() {
		final Vec3 move = position().subtract(xo, yo, zo);
		final float yaw = (float)(Mth.atan2(move.x, move.z) * (double)(180F / (float)Math.PI));
//		this.setXRot((float)(Mth.atan2(vec3.y, d0) * (double)(180F / (float)Math.PI)));
		this.setYRot(-yaw);
	}
	
	protected void inactiveTick() {
		if (this.activated) {
			this.setBattleState(BattleState.JUMPING);
		} else {
			// when we load, this may not be true
			this.setBattlePose(BattlePose.INACTIVE);
		}
	}
	
	protected void jumpingTick() {
		// if mid-jump, don't tick subtick count but do slow down if we are over target
		if (!this.isOnGround()) {
			Vec3 jumpTarg = this.getJumpTarget();
			if (jumpTarg != null) {
				if (Math.abs(this.getX() - jumpTarg.x) < .25 && Math.abs(this.getZ() - jumpTarg.z) < .25) {
					final Vec3 motion = this.getDeltaMovement();
					this.setDeltaMovement(motion.x * .2, motion.y, motion.z * .2);
				}
			}
		} else if (this.stateSubTicks++ < 20) {
			; // do nothing
		} else {
			this.stateSubTicks = 0;
			Vec3 jumpTarg = this.getJumpTarget();
			if (jumpTarg != null) {
				this.jump(jumpTarg);
				this.lookControl.setLookAt(jumpTarg);
			}
		}
	}
	
	protected void toppledTick() {
		if (this.stateSubTicks++ > 100 || this.stateStartHealth - this.getHealth() > (.1f * this.getMaxHealth())) {
			this.setBattleState(BattleState.RECOVERING);
		}
	}
	
	protected void recoveringTick() {
		if (this.stateSubTicks++ == 0) {
			this.jumpFromGround();
		} else if (this.isOnGround()) {
			this.setBattleState(BattleState.JUMPING);
		}
	}
	
	protected void activatingTick() {
		if (this.stateSubTicks++ > 80) {
			this.setBattleState(BattleState.JUMPING);
			
			NostrumParticles.FILLED_ORB.spawn(level, new SpawnParams(100, getX(), getY() + this.getBbHeight() / 2, getZ(), .25,
					30, 10, new Vec3(0, .2, 0), new Vec3(.2, .05, .2)
					).gravity(true).color(0xA0E5E52D).setTargetBehavior(TargetBehavior.JOIN));
			
			this.playSound(SoundEvents.WITHER_SPAWN, 1f, 1f);
		}
	}
	
	protected void clientTick() {
		if (this.lastPose != this.getBattlePose()) {
			this.lastPose = this.getBattlePose();
			this.poseTicks = this.tickCount;
		}
		
		turnTowardsMotion();
		
		if (this.getBattlePose() == BattlePose.ACTIVATING) {
			// effects?
			NostrumParticles.FILLED_ORB.spawn(level, new SpawnParams(1 + (stateSubTicks++ / 20), getX(), getY() + this.getBbHeight() / 2, getZ(), 3,
					30, 10, new TargetLocation(this, true)
					).color(0xA0E5E52D).setTargetBehavior(TargetBehavior.JOIN));
		}
	}
}
