package com.smanzana.nostrummagica.entity.dragon;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.entity.IElementalEntity;
import com.smanzana.nostrummagica.entity.IMultiPartEntity;
import com.smanzana.nostrummagica.entity.IMultiPartEntityPart;
import com.smanzana.nostrummagica.entity.MultiPartEntityPart;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.entity.tasks.SpellAttackGoal;
import com.smanzana.nostrummagica.entity.tasks.dragon.DragonAggroTableGoal;
import com.smanzana.nostrummagica.entity.tasks.dragon.DragonFlyEvasionGoal;
import com.smanzana.nostrummagica.entity.tasks.dragon.DragonFlyRandomGoal;
import com.smanzana.nostrummagica.entity.tasks.dragon.DragonLandGoal;
import com.smanzana.nostrummagica.entity.tasks.dragon.DragonMeleeAttackGoal;
import com.smanzana.nostrummagica.entity.tasks.dragon.DragonNearestAttackableTargetGoal;
import com.smanzana.nostrummagica.entity.tasks.dragon.DragonSummonShadowAttackGoal;
import com.smanzana.nostrummagica.entity.tasks.dragon.DragonTakeoffLandGoal;
import com.smanzana.nostrummagica.loretag.IEntityLoreTagged;
import com.smanzana.nostrummagica.loretag.ILoreSupplier;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.serializer.RedDragonBodyPartTypeSerializer;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.component.shapes.NostrumSpellShapes;
import com.smanzana.nostrummagica.util.SpellUtils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
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
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.BossInfo;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerBossInfo;
import net.minecraftforge.common.util.Constants.NBT;

public class RedDragonEntity extends RedDragonBaseEntity implements IMultiPartEntity, IElementalEntity, ILoreSupplier {

	public static enum DragonBodyPartType {
		BODY("body", 2.5f, 3f, Vector3d.ZERO),
		REAR("rear", 2.5f, 3, new Vector3d(0, 0, -2.5f)),
		HEAD("head", .5f, 2f, new Vector3d(0, 3.0, 1.5f)),
		//TAIL("tail", .5f, .5f, new Vector3d(0, 0, 3.0)),
		//WING_LEFT("wing_left", 1f, 1f, new Vector3d(-2, 0, 1.0)),
		//WING_RIGHT("wing_right", 2f, .5f, new Vector3d(2, 0, 1.0)),
		;
		
		private final String name;
		private final float width;
		private final float height;
		private final Vector3d offset;
		
		private DragonBodyPartType(String name, float width, float height, Vector3d offset) {
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
		
		public Vector3d getPartOffset() {
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
			EntityDataManager.<Integer>createKey(RedDragonEntity.class, DataSerializers.VARINT);
	
	private static final String DRAGON_SERIAL_PHASE_TOK = "DragonPhase";

	private static Spell DSPELL_Fireball;
	private static Spell DSPELL_Fireball2;
	private static Spell DSPELL_Speed;
	private static Spell DSPELL_Shield;
	private static Spell DSPELL_Weaken;
	private static Spell DSPELL_Curse;
	
	private static final int DRAGON_CAST_TIME = 20 * 3;
	
	private static void initSpells() {
		
		if (DSPELL_Fireball == null) {
			DSPELL_Fireball = SpellUtils.MakeSpell("Fireball",
					NostrumSpellShapes.Projectile,
					NostrumSpellShapes.Burst,
					NostrumSpellShapes.Burst.makeProps(3),
					EMagicElement.FIRE,
					2,
					null
					);
			DSPELL_Fireball2 = SpellUtils.MakeSpell("Fireball2",
					NostrumSpellShapes.Projectile,
					NostrumSpellShapes.Burst,
					NostrumSpellShapes.Burst.makeProps(3),
					EMagicElement.FIRE,
					3,
					null);
			DSPELL_Speed = SpellUtils.MakeSpell("Speed",
					EMagicElement.WIND,
					1,
					EAlteration.SUPPORT
					);
			DSPELL_Shield = SpellUtils.MakeSpell("Shield",
					EMagicElement.EARTH,
					2,
					EAlteration.SUPPORT
					);
			DSPELL_Weaken = SpellUtils.MakeSpell("Weaken",
					NostrumSpellShapes.AI,
					EMagicElement.PHYSICAL,
					2,
					EAlteration.INFLICT);
			DSPELL_Curse = SpellUtils.MakeSpell("Curse",
					NostrumSpellShapes.OnDamage,
					NostrumSpellShapes.Delay,
					EMagicElement.PHYSICAL,
					2,
					null,
					EMagicElement.PHYSICAL,
					1,
					EAlteration.INFLICT);
		}
	}
	
	private final ServerBossInfo bossInfo = (ServerBossInfo)(new ServerBossInfo(this.getDisplayName(), BossInfo.Color.RED, BossInfo.Overlay.NOTCHED_10)).setDarkenSky(true);
	
	// AI. First array is indexed by the phase. Second is just a collection of tasks.
	private Goal[][] flyingAI;
	private Goal[][] groundedAI;
	
	private Goal[] lastAI;
	
	private DragonSummonShadowAttackGoal<RedDragonEntity> shadowAttack;
	private DragonFlyEvasionGoal evasionTask;
	private DragonAggroTableGoal<RedDragonEntity, LivingEntity> aggroTable;
	
	private Map<DragonBodyPartType, DragonBodyPart> bodyParts;
	
	public RedDragonEntity(EntityType<? extends RedDragonEntity> type, World worldIn) {
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
        shadowAttack = new DragonSummonShadowAttackGoal<RedDragonEntity>(this, 5 * 60, 10);
        evasionTask = new DragonFlyEvasionGoal(this, 1.0D);
        aggroTable = new DragonAggroTableGoal<>(this, true);
		
		// Order on these are priority numbers!
		flyingAI = new Goal[][] {
			// PHASE_GROUNDED
			new Goal[] {},
			new Goal[] {
				shadowAttack,
        		new DragonLandGoal(this),
        		new DragonMeleeAttackGoal(this, 1.0D, true),
        		new DragonSpellAttackTask(this, (5 * 5), 10, true, null, DRAGON_CAST_TIME, DSPELL_Fireball),
        		evasionTask,
        		//new DragonFlyStrafeTask<EntityDragonRed>(this, 20),
        		new DragonTakeoffLandGoal(this),
        		new DragonFlyRandomGoal(this),
			},
			new Goal[] {
				shadowAttack,
				new DragonLandGoal(this),
        		new DragonMeleeAttackGoal(this, 1.0D, true),
        		new DragonTakeoffLandGoal(this),
				new DragonSpellAttackTask(this, (5 * 5), 12, true, null, DRAGON_CAST_TIME, DSPELL_Fireball2),
				new DragonSpellAttackTask(this, (5 * 12), 10, false, null, DRAGON_CAST_TIME, DSPELL_Speed, DSPELL_Shield),
				new DragonSpellAttackTask(this, (5 * 10), 20, false, null, DRAGON_CAST_TIME, DSPELL_Weaken),
				new DragonSpellAttackTask(this, (5 * 45), 20, true, null, DRAGON_CAST_TIME, DSPELL_Curse),
        		evasionTask,
        		//new DragonFlyStrafeTask<EntityDragonRed>(this, 20),
        		new DragonFlyRandomGoal(this),
			}
        };
        groundedAI = new Goal[][] {
        	new Goal[] {
    			shadowAttack,
        		new DragonMeleeAttackGoal(this, 1.0D, true),
        		new WaterAvoidingRandomWalkingGoal(this, 1.0D, 30)
        	},
        	new Goal[] {
    			shadowAttack,
        		new DragonSpellAttackTask(this, (5 * 5), 20, true, null, DRAGON_CAST_TIME, DSPELL_Fireball),
        		new DragonTakeoffLandGoal(this),
    			new DragonMeleeAttackGoal(this, 1.0D, true),
        		new WaterAvoidingRandomWalkingGoal(this, 1.0D, 30)
        	},
        	new Goal[] {
    			shadowAttack,
        		new DragonTakeoffLandGoal(this),
    			new DragonMeleeAttackGoal(this, 1.0D, true),
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
		this.targetSelector.addGoal(3, new DragonNearestAttackableTargetGoal<PlayerEntity>(this, PlayerEntity.class, true));
		this.targetSelector.addGoal(4, new DragonNearestAttackableTargetGoal<ZombieEntity>(this, ZombieEntity.class, true));
		this.targetSelector.addGoal(5, new DragonNearestAttackableTargetGoal<SheepEntity>(this, SheepEntity.class, true));
		this.targetSelector.addGoal(6, new DragonNearestAttackableTargetGoal<CowEntity>(this, CowEntity.class, true));
		this.targetSelector.addGoal(7, new DragonNearestAttackableTargetGoal<PigEntity>(this, PigEntity.class, true));
		this.targetSelector.addGoal(8, new DragonNearestAttackableTargetGoal<VillagerEntity>(this, VillagerEntity.class, true));
		this.targetSelector.addGoal(9, new DragonNearestAttackableTargetGoal<HorseEntity>(this, HorseEntity.class, true));
		this.targetSelector.addGoal(10, new DragonNearestAttackableTargetGoal<GiantEntity>(this, GiantEntity.class, true));
		this.targetSelector.addGoal(11, new DragonNearestAttackableTargetGoal<PolarBearEntity>(this, PolarBearEntity.class, true));
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
	
	public static final AttributeModifierMap.MutableAttribute BuildAttributes() {
		return RedDragonBaseEntity.BuildBaseRedDragonAttributes()
	        .createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.33D)
	        .createMutableAttribute(Attributes.FLYING_SPEED, 3D)
	        .createMutableAttribute(Attributes.MAX_HEALTH, 1000.0D)
	        .createMutableAttribute(Attributes.ATTACK_DAMAGE, 15.0D)
	        .createMutableAttribute(Attributes.ARMOR, 15.0D)
	        .createMutableAttribute(Attributes.ATTACK_SPEED, 0.5D)
	        .createMutableAttribute(Attributes.FOLLOW_RANGE, 64D);
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
			
			part.setPosition(this.getPosX(), this.getPosY(), this.getPosZ());
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
			
			Vector3d offset = type.getPartOffset();
			part.setLocationAndAngles(
					this.getPosX() + (Math.cos(rotRad) * offset.x) + (Math.sin(rotRad) * offset.z),
					this.getPosY() + offset.y,
					this.getPosZ() + (Math.sin(rotRad) * offset.x) + (Math.cos(rotRad) * offset.z),
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
				if ((this.getPosY() > this.prevPosY) || (this.getMotion().x + this.getMotion().z < .2)) {
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
//					new Vector3d(0, .25, 0), Vector3d.ZERO)
//					.color(0xFFFF0022));
			NostrumParticles.FILLED_ORB.spawn(this.world, new NostrumParticles.SpawnParams(5,
					getPosX(), getPosY() + this.getHeight() / 2, getPosZ(),
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
	public ILoreTagged getLoreTag() {
		return RedDragonLore.instance();
	}
	
	public static final class RedDragonLore implements IEntityLoreTagged<RedDragonEntity> {
		
		private static RedDragonLore instance = null;
		public static RedDragonLore instance() {
			if (instance == null) {
				instance = new RedDragonLore();
			}
			return instance;
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

		@Override
		public InfoScreenTabs getTab() {
			return InfoScreenTabs.INFO_ENTITY;
		}

		@Override
		public EntityType<RedDragonEntity> getEntityType() {
			return NostrumEntityTypes.dragonRed;
		}
	}

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
	public Entity[] getEnityParts() {
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
	
	private class DragonSpellAttackTask extends SpellAttackGoal<RedDragonEntity> {

		public DragonSpellAttackTask(RedDragonEntity entity, int delay, int odds, boolean needsTarget, Predicate<RedDragonEntity> predicate,
				int castTime, Spell ... spells) {
			super(entity, delay, odds, needsTarget, predicate, castTime, spells);
		}
		
		@Override
		public void resetTask() {
			super.resetTask();
			RedDragonEntity.this.setCasting(false);
		}
		
		@Override
		public void startExecuting() {
			super.startExecuting();
			RedDragonEntity.this.setCasting(true);
		}
	}
	
	public static class DragonBodyPart extends MultiPartEntityPart<RedDragonEntity> {
		
		public static final String ID = RedDragonEntity.ID + ".body_part";
		
		protected static final DataParameter<DragonBodyPartType> TYPE = EntityDataManager.createKey(DragonBodyPart.class, RedDragonBodyPartTypeSerializer.instance);
		protected @Nullable RedDragonEntity parent;
		
		public DragonBodyPart(DragonBodyPartType type, RedDragonEntity parent) {
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

	@Override
	public EMagicElement getElement() {
		return EMagicElement.FIRE;
	}

}
