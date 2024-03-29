package com.smanzana.nostrummagica.entity.dragon;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.entity.IMultiPartEntity;
import com.smanzana.nostrummagica.entity.IMultiPartEntityPart;
import com.smanzana.nostrummagica.entity.MultiPartEntityPart;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.entity.tasks.EntitySpellAttackTask;
import com.smanzana.nostrummagica.entity.tasks.dragon.DragonAIAggroTable;
import com.smanzana.nostrummagica.entity.tasks.dragon.DragonAINearestAttackableTarget;
import com.smanzana.nostrummagica.entity.tasks.dragon.DragonFlyEvasionTask;
import com.smanzana.nostrummagica.entity.tasks.dragon.DragonFlyRandomTask;
import com.smanzana.nostrummagica.entity.tasks.dragon.DragonLandTask;
import com.smanzana.nostrummagica.entity.tasks.dragon.DragonMeleeAttackTask;
import com.smanzana.nostrummagica.entity.tasks.dragon.DragonSummonShadowAttack;
import com.smanzana.nostrummagica.entity.tasks.dragon.DragonTakeoffLandTask;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.serializers.RedDragonBodyPartTypeSerializer;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.spells.components.shapes.AoEShape;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.AITargetTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.DamagedTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.OtherTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ProjectileTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.SelfTrigger;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.GiantEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.horse.HorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BossInfo;
import net.minecraft.world.ServerBossInfo;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class EntityDragonRed extends EntityDragonRedBase implements IMultiPartEntity {

	public static enum DragonBodyPartType {
		BODY("body", 2.5f, 3f, Vec3d.ZERO),
		REAR("rear", 2.5f, 3, new Vec3d(0, 0, -2.5f)),
		HEAD("head", .5f, 2f, new Vec3d(0, 3.0, 1.5f)),
		//TAIL("tail", .5f, .5f, new Vec3d(0, 0, 3.0)),
		//WING_LEFT("wing_left", 1f, 1f, new Vec3d(-2, 0, 1.0)),
		//WING_RIGHT("wing_right", 2f, .5f, new Vec3d(2, 0, 1.0)),
		;
		
		private final String name;
		private final float width;
		private final float height;
		private final Vec3d offset;
		
		private DragonBodyPartType(String name, float width, float height, Vec3d offset) {
			this.name = name;
			this.width = width;
			this.height = height;
			this.offset = offset;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		public float getWidth() {
			return width;
		}
		
		public float getHeight() {
			return height;
		}
		
		public Vec3d getPartOffset() {
			return offset;
		}
	}
	
	private static enum DragonPhase {
		GROUNDED_PHASE,
		FLYING_PHASE,
		RAMPAGE_PHASE,
	}
	
	public static final String ID = "entity_dragon_red";
	
	private static final DataParameter<Integer> DRAGON_PHASE =
			EntityDataManager.<Integer>createKey(EntityDragonRed.class, DataSerializers.VARINT);
	
	private static final String DRAGON_SERIAL_PHASE_TOK = "DragonPhase";

	private static Spell DSPELL_Fireball;
	private static Spell DSPELL_Fireball2;
	private static Spell DSPELL_Speed;
	private static Spell DSPELL_Shield;
	private static Spell DSPELL_Weaken;
	private static Spell DSPELL_Curse;
	
	private static final int DRAGON_CAST_TIME = 20 * 3;
	
	private static Spell makeSpell(
			String name,
			Object ... objects) {
		Spell spell = new Spell(name, true);
		
		for (int i = 0; i < objects.length; i++) {
			Object o = objects[i];
			if (o instanceof SpellTrigger) {
				SpellTrigger trigger = (SpellTrigger) o;
				spell.addPart(new SpellPart(trigger));
			} else if (o instanceof SpellShape) {
				SpellShape shape = (SpellShape) o;
				EMagicElement element = (EMagicElement) objects[++i];
				Integer level = (Integer) objects[++i];
				EAlteration alt = (EAlteration) objects[++i];
				SpellPartParam param = (SpellPartParam) objects[++i];
				
				if (param == null) {
					param = new SpellPartParam(0, false);
				}
				
				spell.addPart(new SpellPart(shape, element, level, alt, param));
			}
		}
		
		return spell;
	}
	
	private static void initSpells() {
		
		if (DSPELL_Fireball == null) {
			DSPELL_Fireball = makeSpell("Fireball",
					ProjectileTrigger.instance(),
					AoEShape.instance(),
					EMagicElement.FIRE,
					2,
					null,
					new SpellPartParam(3, false));
			DSPELL_Fireball2 = makeSpell("Fireball2",
					ProjectileTrigger.instance(),
					AoEShape.instance(),
					EMagicElement.FIRE,
					3,
					null,
					new SpellPartParam(3, false));
			DSPELL_Speed = makeSpell("Speed",
					SelfTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.WIND,
					1,
					EAlteration.SUPPORT,
					null);
			DSPELL_Shield = makeSpell("Shield",
					SelfTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.EARTH,
					2,
					EAlteration.SUPPORT,
					null);
			DSPELL_Weaken = makeSpell("Weaken",
					AITargetTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.PHYSICAL,
					2,
					EAlteration.INFLICT,
					null);
			DSPELL_Curse = makeSpell("Curse",
					SelfTrigger.instance(),
					DamagedTrigger.instance(),
					OtherTrigger.instance(),
					DamagedTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.PHYSICAL,
					2,
					null,
					null,
					SingleShape.instance(),
					EMagicElement.PHYSICAL,
					1,
					EAlteration.INFLICT,
					null);
		}
	}
	
	private final ServerBossInfo bossInfo = (ServerBossInfo)(new ServerBossInfo(this.getDisplayName(), BossInfo.Color.RED, BossInfo.Overlay.NOTCHED_10)).setDarkenSky(true);
	
	// AI. First array is indexed by the phase. Second is just a collection of tasks.
	private Goal[][] flyingAI;
	private Goal[][] groundedAI;
	
	private Goal[] lastAI;
	
	private DragonSummonShadowAttack<EntityDragonRed> shadowAttack;
	private DragonFlyEvasionTask evasionTask;
	private DragonAIAggroTable<EntityDragonRed, LivingEntity> aggroTable;
	
	private Map<DragonBodyPartType, DragonBodyPart> bodyParts;
	
	public EntityDragonRed(EntityType<? extends EntityDragonRed> type, World worldIn) {
		super(type, worldIn);
		this.stepHeight = 2;
		this.ignoreFrustumCheck = true;
		this.experienceValue = 1000;
		this.noClip = false;
		
		bodyParts = new EnumMap<>(DragonBodyPartType.class);
	}
	
	public static final float GetBodyWidth() {
		return DragonBodyPartType.BODY.getWidth();
	}
	
	public static final float GetBodyHeight() {
		return DragonBodyPartType.BODY.getHeight();
	}
	
	private DragonPhase getPhase() {
		return DragonPhase.values()[this.dataManager.get(DRAGON_PHASE).intValue()];
	}
	
	private void setPhase(DragonPhase phase) {
		this.dataManager.set(DRAGON_PHASE, phase.ordinal());
	}
	
	private void onPhaseChange() {
		DragonPhase phase = this.getPhase();
		if (phase == DragonPhase.FLYING_PHASE) {
			if (!this.isTryingToLand() && !this.isFlying()) {
				// Conveniently will set up AI
				this.startFlying();
			}
			NostrumMagicaSounds.DRAGON_DEATH.play(this);
		} else if (phase == DragonPhase.RAMPAGE_PHASE) {
			NostrumMagicaSounds.DRAGON_DEATH.play(this);
			// No convenience. Set up AI depending on if we're flying or not
			if (!this.world.isRemote) {
				if (this.isFlying()) {
					this.setFlyingAI();
				} else {
					this.setGroundedAI();
				}
			}
		}
	}
	
	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		super.notifyDataManagerChange(key);
		if (key == DRAGON_PHASE) {
			onPhaseChange();
		}
	}
	
	private void initBaseAI() {
		
		initSpells();
        shadowAttack = new DragonSummonShadowAttack<EntityDragonRed>(this, 5 * 60, 10);
        evasionTask = new DragonFlyEvasionTask(this, 1.0D);
        aggroTable = new DragonAIAggroTable<>(this, true);
		
		// Order on these are priority numbers!
		flyingAI = new Goal[][] {
			// PHASE_GROUNDED
			new Goal[] {},
			new Goal[] {
				shadowAttack,
        		new DragonLandTask(this),
        		new DragonMeleeAttackTask(this, 1.0D, true),
        		new DragonSpellAttackTask(this, (5 * 5), 10, true, null, DRAGON_CAST_TIME, DSPELL_Fireball),
        		evasionTask,
        		//new DragonFlyStrafeTask<EntityDragonRed>(this, 20),
        		new DragonTakeoffLandTask(this),
        		new DragonFlyRandomTask(this),
			},
			new Goal[] {
				shadowAttack,
				new DragonLandTask(this),
        		new DragonMeleeAttackTask(this, 1.0D, true),
        		new DragonTakeoffLandTask(this),
				new DragonSpellAttackTask(this, (5 * 5), 12, true, null, DRAGON_CAST_TIME, DSPELL_Fireball2),
				new DragonSpellAttackTask(this, (5 * 12), 10, false, null, DRAGON_CAST_TIME, DSPELL_Speed, DSPELL_Shield),
				new DragonSpellAttackTask(this, (5 * 10), 20, false, null, DRAGON_CAST_TIME, DSPELL_Weaken),
				new DragonSpellAttackTask(this, (5 * 45), 20, true, null, DRAGON_CAST_TIME, DSPELL_Curse),
        		evasionTask,
        		//new DragonFlyStrafeTask<EntityDragonRed>(this, 20),
        		new DragonFlyRandomTask(this),
			}
        };
        groundedAI = new Goal[][] {
        	new Goal[] {
    			shadowAttack,
        		new DragonMeleeAttackTask(this, 1.0D, true),
        		new WaterAvoidingRandomWalkingGoal(this, 1.0D, 30)
        	},
        	new Goal[] {
    			shadowAttack,
        		new DragonSpellAttackTask(this, (5 * 5), 20, true, null, DRAGON_CAST_TIME, DSPELL_Fireball),
        		new DragonTakeoffLandTask(this),
    			new DragonMeleeAttackTask(this, 1.0D, true),
        		new WaterAvoidingRandomWalkingGoal(this, 1.0D, 30)
        	},
        	new Goal[] {
    			shadowAttack,
        		new DragonTakeoffLandTask(this),
    			new DragonMeleeAttackTask(this, 1.0D, true),
    			new DragonSpellAttackTask(this, (5 * 5), 12, true, null, DRAGON_CAST_TIME, DSPELL_Fireball2),
				new DragonSpellAttackTask(this, (5 * 12), 10, false, null, DRAGON_CAST_TIME, DSPELL_Speed, DSPELL_Shield),
				new DragonSpellAttackTask(this, (5 * 10), 20, false, null, DRAGON_CAST_TIME, DSPELL_Weaken),
				new DragonSpellAttackTask(this, (5 * 45), 20, true, null, DRAGON_CAST_TIME, DSPELL_Curse),
        		new WaterAvoidingRandomWalkingGoal(this, 1.0D, 30)
        	}
        		
        };
		
//      this.goalSelector.addGoal(1, new SwimGoal(this));
//		this.goalSelector.addGoal(4, new WaterAvoidingRandomWalkingGoal(this, 1.0D, 30));
        this.targetSelector.addGoal(1, aggroTable);
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(3, new DragonAINearestAttackableTarget<PlayerEntity>(this, PlayerEntity.class, true));
		this.targetSelector.addGoal(4, new DragonAINearestAttackableTarget<ZombieEntity>(this, ZombieEntity.class, true));
		this.targetSelector.addGoal(5, new DragonAINearestAttackableTarget<SheepEntity>(this, SheepEntity.class, true));
		this.targetSelector.addGoal(6, new DragonAINearestAttackableTarget<CowEntity>(this, CowEntity.class, true));
		this.targetSelector.addGoal(7, new DragonAINearestAttackableTarget<PigEntity>(this, PigEntity.class, true));
		this.targetSelector.addGoal(8, new DragonAINearestAttackableTarget<VillagerEntity>(this, VillagerEntity.class, true));
		this.targetSelector.addGoal(9, new DragonAINearestAttackableTarget<HorseEntity>(this, HorseEntity.class, true));
		this.targetSelector.addGoal(10, new DragonAINearestAttackableTarget<GiantEntity>(this, GiantEntity.class, true));
		this.targetSelector.addGoal(11, new DragonAINearestAttackableTarget<PolarBearEntity>(this, PolarBearEntity.class, true));
	}
	
	@Override
	protected void setFlyingAI() {
		DragonPhase phase = this.getPhase();
		// Remove grounded
		if (lastAI != null) {
			for (Goal ai : lastAI) {
				this.goalSelector.removeGoal(ai);
			}
		}
		
		lastAI = this.flyingAI[phase.ordinal()];
		
		if (lastAI != null && lastAI.length > 0) {
			for (int i = 0; i < lastAI.length; i++) {
				this.goalSelector.addGoal(i, lastAI[i]);
			}
		}
	}
	
	@Override
	protected void setGroundedAI() {
		DragonPhase phase = this.getPhase();
		
		// Remove flying
		if (lastAI != null) {
			for (Goal ai : lastAI) {
				this.goalSelector.removeGoal(ai);
			}
		}
		
		lastAI = this.groundedAI[phase.ordinal()];
		
		if (lastAI != null && lastAI.length > 0) {
			for (int i = 0; i < lastAI.length; i++) {
				this.goalSelector.addGoal(i, lastAI[i]);
			}
		}
	}
	
	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.initBaseAI();
		
		if (isFlying()) {
			this.setFlyingAI();
		} else {
			this.setGroundedAI();
		}
	}
	
	@Override
	protected void registerAttributes() {
		super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.33D);
        this.getAttribute(SharedMonsterAttributes.FLYING_SPEED).setBaseValue(3D);
        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(1000.0D);
        this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(15.0D);
        this.getAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(15.0D);
        this.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_SPEED);
        this.getAttribute(SharedMonsterAttributes.ATTACK_SPEED).setBaseValue(0.5D);
        this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64D);
    }
	
	@Override
	protected void registerData() {
		super.registerData();
		this.dataManager.register(DRAGON_PHASE, DragonPhase.GROUNDED_PHASE.ordinal());
	}
	
	protected void spawnBodyParts() {
		for (DragonBodyPartType partType : DragonBodyPartType.values()) {
			DragonBodyPart part = new DragonBodyPart(partType, this);
			bodyParts.put(partType, part);
			
			part.setPosition(this.posX, this.posY, this.posZ);
			this.world.addEntity(part);
		}
	}
	
	@Override
	public void onAddedToWorld() {
		super.onAddedToWorld();
	}
	
	@Override
	public boolean canDespawn(double nearestPlayer) {
		return false;
	}
	
	public boolean canAttackClass(Class <? extends LivingEntity > cls) {
		return true;
	}
	
	@Override
	public boolean isNonBoss() {
		return false;
	}
	
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);

		if (compound.contains(DRAGON_SERIAL_PHASE_TOK, NBT.TAG_ANY_NUMERIC)) {
        	int i = compound.getByte(DRAGON_SERIAL_PHASE_TOK);
            this.setPhase(DragonPhase.values()[i]);
        }
		
		if (!this.world.isRemote) {
			this.registerGoals(); // TODO this seems bad
		}
	}
	
	public void writeAdditional(CompoundNBT compound) {
    	super.writeAdditional(compound);
    	compound.putByte(DRAGON_SERIAL_PHASE_TOK, (byte)this.getPhase().ordinal());
	}
	
	protected void updateParts() {
		final float progress = getRotationYawHead();
		final double rotRad = Math.PI * (progress / -180.0);
		for (DragonBodyPartType type : DragonBodyPartType.values()) {
			DragonBodyPart part = this.bodyParts.get(type);
			
			if (part == null) {
				continue; // Client, and hasn't attached yet?
			}
			
			Vec3d offset = type.getPartOffset();
			part.setLocationAndAngles(
					this.posX + (Math.cos(rotRad) * offset.x) + (Math.sin(rotRad) * offset.z),
					this.posY + offset.y,
					this.posZ + (Math.sin(rotRad) * offset.x) + (Math.cos(rotRad) * offset.z),
					this.rotationYaw, this.rotationPitch);
			part.tick();
		}
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (!this.world.isRemote() && this.bodyParts.get(DragonBodyPartType.BODY) == null) {
			spawnBodyParts();
		}
		
		DragonPhase phase = this.getPhase();
		if (phase == DragonPhase.GROUNDED_PHASE) {
			float ratio = this.getHealth() / this.getMaxHealth();
			if (ratio <= 0.70f) {
				this.setPhase(DragonPhase.FLYING_PHASE);
			}
		} else if (phase == DragonPhase.FLYING_PHASE) {
			float ratio = this.getHealth() / this.getMaxHealth();
			if (ratio <= 0.40f) {
				this.setPhase(DragonPhase.RAMPAGE_PHASE);
			}
		}
		
		if (world.isRemote) {
			if (this.isFlying() && !this.getWingFlapping()) {
				if ((this.posY > this.prevPosY) || (this.getMotion().x + this.getMotion().z < .2)) {
					this.flapWing(this.getMotion().x + this.getMotion().z < .2 ? .5f : 1f);
				}
			}
		}
		
		updateParts();
		
		if (this.world.isRemote && this.isCasting()) {
//			NostrumParticles.FILLED_ORB.spawn(this.world, new NostrumParticles.SpawnParams(5,
//					posX, posY + this.getHeight() / 2, posZ,
//					5,
//					30, 5,
//					new Vec3d(0, .25, 0), Vec3d.ZERO)
//					.color(0xFFFF0022));
			NostrumParticles.FILLED_ORB.spawn(this.world, new NostrumParticles.SpawnParams(5,
					posX, posY + this.getHeight() / 2, posZ,
					5,
					30, 5,
					this.getEntityId())
					.color(0xFFAA0022)
					.dieOnTarget(true));
		}
	}
	
	@Override
	protected void updateAITasks() {
		super.updateAITasks();
		
		this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
	}
	
	public void addTrackingPlayer(ServerPlayerEntity player) {
		super.addTrackingPlayer(player);
		this.bossInfo.addPlayer(player);
	}

	public void removeTrackingPlayer(ServerPlayerEntity player) {
		super.removeTrackingPlayer(player);
		this.bossInfo.removePlayer(player);
	}

	@Override
	public String getLoreKey() {
		return "nostrum__dragon_red";
	}

	@Override
	public String getLoreDisplayName() {
		return "Red Dragons";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("Red Dragons are greedy creatures. They often live in abandoned castles, and have a strong fondness to anything that's shiny.");
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("Red Dragons are greedy creatures. They often live in abandoned castles, and have a strong fondness to anything that's shiny.", "According to some reports, Red Dragons are the only ones which are hatched from eggs.", "Nothing is known about what such eggs would look like.");
	}
	
//	@Override
//	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
//		this.entityDropItem(new ItemStack(NostrumItems.dragonEggFragment), 0);
//		
//		int count = this.getRNG().nextInt(2 + lootingModifier);
//		if (count != 0) {
//			this.entityDropItem(new ItemStack(NostrumItems.resourceDragonWing, count), 0);
//		}
//		
//		// Research scroll
//		int chances = 20 + (lootingModifier * 2);
//		if (rand.nextInt(100) < chances) {
//			this.entityDropItem(new ItemStack(NostrumItems.skillScrollSmall, 1), 0);
//		}
//	}

	@Override
	public boolean attackEntityFromPart(MultiPartEntityPart<?> dragonPart, DamageSource source, float damage) {
		// could take less or more damage from different sources in different parents
		return this.attackEntityFrom(source, damage);
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (!this.world.isRemote && source.getTrueSource() != null) {
			Entity ent = source.getTrueSource();
			if (ent instanceof LivingEntity && ent != this) {
				this.shadowAttack.addToPool((LivingEntity) ent);
				this.aggroTable.addDamage((LivingEntity) ent, amount);
			}
			
			this.evasionTask.reset();
		}
		
		return super.attackEntityFrom(source, amount);
	}
	
	@Override
	public void bite(LivingEntity target) {
		super.bite(target);
		
		if (!this.world.isRemote) {
			this.evasionTask.reset();
		}
	}

	@Override
	public World getWorld() {
		return this.world;
	}
	
	@Override
	@Nullable
	public Entity[] getParts() {
		return bodyParts.values().toArray(new Entity[0]);
	}
	
	@Override
	public boolean canBeCollidedWith() {
		return super.canBeCollidedWith();
	}
	
	@Override
	public boolean attachClientEntity(IMultiPartEntityPart<?> part) {
		DragonBodyPart bodyPart = (DragonBodyPart) part;
		if (this.bodyParts.containsKey(bodyPart.getDragonPart())) {
			NostrumMagica.logger.warn("Got client attach event for dragon part [" + bodyPart.getDragonPart().name + "] but already have one?");
		} else {
			this.bodyParts.put(bodyPart.getDragonPart(), bodyPart);
			return true;
		}
		
		return false;
	}
	
	private class DragonSpellAttackTask extends EntitySpellAttackTask<EntityDragonRed> {

		public DragonSpellAttackTask(EntityDragonRed entity, int delay, int odds, boolean needsTarget, Predicate<EntityDragonRed> predicate,
				int castTime, Spell ... spells) {
			super(entity, delay, odds, needsTarget, predicate, castTime, spells);
		}
		
		@Override
		public void resetTask() {
			super.resetTask();
			EntityDragonRed.this.setCasting(false);
		}
		
		@Override
		public void startExecuting() {
			super.startExecuting();
			EntityDragonRed.this.setCasting(true);
		}
	}
	
	public static class DragonBodyPart extends MultiPartEntityPart<EntityDragonRed> {
		
		public static final String ID = EntityDragonRed.ID + ".body_part";
		
		protected static final DataParameter<DragonBodyPartType> TYPE = EntityDataManager.createKey(DragonBodyPart.class, RedDragonBodyPartTypeSerializer.instance);
		protected @Nullable EntityDragonRed parent;
		
		public DragonBodyPart(DragonBodyPartType type, EntityDragonRed parent) {
			super(NostrumEntityTypes.dragonRedBodyPart, parent, type.name(), type.getWidth(), type.getHeight());
			this.setType(type);
			this.parent = parent;
		}
		
		public DragonBodyPart(EntityType<? extends DragonBodyPart> type, World world) {
			super(type, world, "DragonPart_Client", 2, 2);
		}
		
		@Override
		public void registerData() {
			super.registerData();
			this.dataManager.register(TYPE, DragonBodyPartType.BODY);
		}
		
		public DragonBodyPartType getDragonPart() {
			return dataManager.get(TYPE);
		}
		
		protected void setType(DragonBodyPartType type) {
			this.dataManager.set(TYPE, type);
		}
		
		@Override
		protected void readAdditional(CompoundNBT compound) {
			super.readAdditional(compound);
		}

		@Override
		protected void writeAdditional(CompoundNBT compound) {
			super.writeAdditional(compound);
		}
	}

}
