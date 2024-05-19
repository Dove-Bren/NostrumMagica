package com.smanzana.nostrummagica.entity.plantboss;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.blocks.DungeonBlock;
import com.smanzana.nostrummagica.blocks.PoisonWaterBlock;
import com.smanzana.nostrummagica.blocks.TeleportRune;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.entity.AggroTable;
import com.smanzana.nostrummagica.entity.IMultiPartEntity;
import com.smanzana.nostrummagica.entity.MultiPartEntityPart;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.fluids.FluidPoisonWater;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.serializers.FloatArraySerializer;
import com.smanzana.nostrummagica.serializers.OptionalMagicElementDataSerializer;
import com.smanzana.nostrummagica.serializers.PlantBossTreeTypeSerializer;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.SpellPart;
import com.smanzana.nostrummagica.spells.SpellPartProperties;
import com.smanzana.nostrummagica.spells.components.MagicDamageSource;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.FieldTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.MagicCutterTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.MortarTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ProjectileTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.SeekingBulletTrigger;
import com.smanzana.nostrummagica.utils.Entities;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Plane;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.BossInfo;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerBossInfo;

public class EntityPlantBoss extends MobEntity implements ILoreTagged, IMultiPartEntity {
	
	public static enum BattleState {
		IDLE, // Not doing anything specific but throwing out attacks and looking mad
		SHIELDING, // Leaves enclose the body and only one element is vulnerable
		BULLET_SEEDING, // Spinning in a circle firing bullet seeds
		POLLINATING, // Releasing seeking bullets with ROOT
		SWEEPING, // Shield self (partially?) and summon sweeping brambles to attack
		BOMBING, // Releasing a large seed bomb that puts an AoE on a platform
	}

	public static enum Phase {
		NORMAL(1f, // 100% - 70%
				new BattleState[]{BattleState.POLLINATING},
				new BattleState[]{BattleState.SHIELDING},
				new BattleState[]{BattleState.BOMBING}
				),
		ANGRY(.7f, // 70% - 40%
				new BattleState[]{BattleState.POLLINATING, BattleState.SWEEPING},
				new BattleState[]{BattleState.SHIELDING},
				new BattleState[]{BattleState.BOMBING, BattleState.BOMBING},
				new BattleState[]{BattleState.BULLET_SEEDING}
				),
		FURIOUS(.4f, // 40% - 0%
				new BattleState[]{BattleState.POLLINATING, BattleState.SWEEPING},
				new BattleState[]{BattleState.POLLINATING, BattleState.BOMBING, BattleState.SWEEPING},
				new BattleState[]{BattleState.SHIELDING},
				new BattleState[]{BattleState.BOMBING},
				new BattleState[]{BattleState.BULLET_SEEDING, BattleState.BULLET_SEEDING},
				new BattleState[]{BattleState.POLLINATING, BattleState.BULLET_SEEDING}
				),
		;
		
		private final float maxHPPercent;
		private final BattleState[][] stateSequences;
		
		private Phase(float maxHP, BattleState[] ... sequences) {
			this.maxHPPercent = maxHP;
			this.stateSequences = sequences;
		}
		
		public static final Phase getPhaseFromHealth(float healthPercent) {
			for (int i = values().length - 1; i >= 0; i--) {
				Phase phase = values()[i];
				if (phase.maxHPPercent >= healthPercent) {
					return phase;
				}
			}
			
			return Phase.NORMAL;
		}
		
		public BattleState[] getRandomSequence(@Nullable Random rand) {
			if (rand == null) rand = new Random();
			return stateSequences[rand.nextInt(stateSequences.length)];
		}
	}
	
	public static enum PlantBossTreeType {
		NORMAL,
		COVERED,
		ELEMENTAL,
	}
	
	public static final String ID = "entity_plant_boss";
	public static final int NumberOfLeaves = 8;
	
	private static Spell[] IdleSpells = null;
	
	protected static Spell[] GetIdleSpells() {
		if (IdleSpells == null) {
			List<Spell> spells = new ArrayList<>();
			
			spells.add(makeSpell("Leaf Blade",
					MagicCutterTrigger.instance(),
					new SpellPartProperties(1, true),
					SingleShape.instance(),
					EMagicElement.WIND,
					2,
					EAlteration.RUIN,
					null
					));
			spells.add(makeSpell("Spore",
					SeekingBulletTrigger.instance(),
					new SpellPartProperties(0, true),
					SingleShape.instance(),
					EMagicElement.LIGHTNING,
					1,
					EAlteration.INFLICT,
					null
					));
			
			
			IdleSpells = spells.toArray(new Spell[0]);
		}
		
		return IdleSpells;
	}
	
	private static Spell SeedBombSpell = null;
	protected static Spell GetSeedBombSpell() {
		if (SeedBombSpell == null) {
			SeedBombSpell = makeSpell("Seed Bomb",
					MortarTrigger.instance(),
					new SpellPartProperties(0, true),
					FieldTrigger.instance(),
					new SpellPartProperties(2, false),
					SingleShape.instance(),
					EMagicElement.EARTH,
					1,
					null,
					null
					);
		}
		
		return SeedBombSpell;
	}
	
	private static Spell PollenSpell = null;
	protected static Spell GetPollenSpell() {
		if (PollenSpell == null) {
			PollenSpell = makeSpell("Pollinate",
					SeekingBulletTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.EARTH,
					1,
					EAlteration.INFLICT,
					null
					);
		}
		
		return PollenSpell;
	}
	
	private static Spell BulletSeedSpell = null;
	protected static Spell GetBulletSeedSpell() {
		if (BulletSeedSpell == null) {
			BulletSeedSpell = makeSpell("Bullet Seed",
					ProjectileTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.EARTH,
					3,
					null,
					null
					);
		}
		
		return BulletSeedSpell;
	}
	
	private static Spell makeSpell(
			String name,
			Object ... objects) {
		Spell spell = Spell.CreateAISpell(name);
		
		for (int i = 0; i < objects.length; i++) {
			Object o = objects[i];
			if (o instanceof SpellTrigger) {
				SpellTrigger trigger = (SpellTrigger) o;
				final SpellPartProperties param;
				
				// Peek at next
				if (objects.length > i + 1
						&& objects[i+1] instanceof SpellPartProperties) {
					param = (SpellPartProperties) objects[++i];
				} else {
					param = new SpellPartProperties(0, false); // matches SpellPart constructor with no second param
				}
				
				spell.addPart(new SpellPart(trigger, param));
			} else if (o instanceof SpellShape) {
				SpellShape shape = (SpellShape) o;
				EMagicElement element = (EMagicElement) objects[++i];
				Integer level = (Integer) objects[++i];
				EAlteration alt = (EAlteration) objects[++i];
				SpellPartProperties param = (SpellPartProperties) objects[++i];
				
				if (param == null) {
					param = new SpellPartProperties(0, false);
				}
				
				spell.addPart(new SpellPart(shape, element, level, alt, param));
			}
		}
		
		return spell;
	}
	
	protected static final DataParameter<Float[]> LEAF_PITCHES = EntityDataManager.<Float[]>createKey(EntityPlantBoss.class, FloatArraySerializer.instance);
	protected static final DataParameter<Optional<EMagicElement>> WEAK_ELEMENT = EntityDataManager.<Optional<EMagicElement>>createKey(EntityPlantBoss.class, OptionalMagicElementDataSerializer.instance);
	protected static final DataParameter<PlantBossTreeType> TREE_TYPE = EntityDataManager.<PlantBossTreeType>createKey(EntityPlantBoss.class, PlantBossTreeTypeSerializer.instance);
	protected static final DataParameter<Optional<UUID>> BODY_ID = EntityDataManager.createKey(EntityPlantBoss.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	
	private final ServerBossInfo bossInfo = (ServerBossInfo) new ServerBossInfo(this.getDisplayName(), BossInfo.Color.GREEN, BossInfo.Overlay.NOTCHED_10).setDarkenSky(true);
	private final PlantBossLeafLimb[] limbs;
	private final MultiPartEntityPart<EntityPlantBoss>[] parts;
	protected float eyeHeight;
	private @Nullable PlantBossBody bodyCache = null;
	
	private Map<BattleState, BattleStateTask> stateTasks;
	
	private BlockPos arenaMin;
	private BlockPos arenaMax;
	private BlockPos[] pillars;
	
	// State machine
	private AggroTable<LivingEntity> aggroTable;
	private @Nullable BattleState[] currentSequence;
	private int currentSequenceIndex;
	
	private @Nullable EMagicElement weakElement;
	
	// Animation
	protected int curlTicks = 0; // 0 - no anim, 1+, curling, -1- - uncurling
	protected int curlDuration = 0;
	protected boolean curlLeaveFrontOpen;
	
	@SuppressWarnings("unchecked")
	public EntityPlantBoss(EntityType<? extends EntityPlantBoss> type, World worldIn) {
		super(type, worldIn);
        this.ignoreFrustumCheck = true;
        this.experienceValue = 1250;
        this.entityCollisionReduction = 1f;
		
        this.parts = new MultiPartEntityPart[NumberOfLeaves + 1];
		this.limbs = new PlantBossLeafLimb[NumberOfLeaves];
        
		this.aggroTable = new AggroTable<>((ent) -> {
			return EntityPlantBoss.this.getEntitySenses().canSee(ent);
		});
		
		this.stateTasks = new EnumMap<>(BattleState.class);
		fillStateTasks(this.stateTasks);
		
		this.eyeHeight = this.getHeight() * .85f;
	}
	
	protected void fillStateTasks(Map<BattleState, BattleStateTask> map) {
		for (BattleState state : BattleState.values()) {
			BattleStateTask task = null;
			switch (state) { // for warning when new ones are added
			case IDLE:
				task = new BattleStateTaskIdle(this, 20 * 8, 20 * 2,
						.05f, 20 * 1, 40 * 1, EntityPlantBoss.GetIdleSpells());
				
				// reset idle task since it's our first
				task.startTask();
				
				break;
			case BOMBING:
				task = new BattleStateTaskBombing(this, 20 * 3, 20 * 2);
				break;
			case POLLINATING:
				task = new BattleStateTaskPollinating(this, 20 * 2, 20 * 1);
				break;
			case BULLET_SEEDING:
				task = new BattleStateTaskBulletSeeding(this, 1, 20 * 10);
				break;
			case SHIELDING:
				task = new BattleStateTaskShielding(this, 20 * 30, 20 * 5, 50f);
				break;
			case SWEEPING:
				task = new BattleStateTaskSweeping(this, 2, 20 * 2, 20 * 5);
				break;
			}
			map.put(state, task);
		}
	}
	
	@Override
	protected void registerData() {
		super.registerData();
		this.dataManager.register(LEAF_PITCHES, new Float[NumberOfLeaves]);
		this.dataManager.register(WEAK_ELEMENT, Optional.empty());
		this.dataManager.register(TREE_TYPE, PlantBossTreeType.NORMAL);
		this.dataManager.register(BODY_ID, Optional.empty());
	}
	
	public static final AttributeModifierMap.MutableAttribute BuildAttributes() {
		return MobEntity.func_233666_p_()
	        .createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.00D)
	        .createMutableAttribute(Attributes.MAX_HEALTH, 800.0D)
	        .createMutableAttribute(Attributes.ATTACK_DAMAGE, 10.0D)
	        .createMutableAttribute(Attributes.ARMOR, 18.0D)
	        .createMutableAttribute(Attributes.ATTACK_SPEED, 0.5D)
	        .createMutableAttribute(Attributes.FOLLOW_RANGE, 8D)
	        .createMutableAttribute(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }
	
	@Override
	protected void registerGoals() {
		super.registerGoals();
	}
	
	@Override
	public boolean canDespawn(double distanceToClosestPlayer) {
		return false;
	}
	
	@Override
	public boolean canAttack(EntityType<?> type) {
		return true;
	}
	
	@Override
	public boolean isNonBoss() {
		return false;
	}
	
	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
	}
	
	@Override
	public void writeAdditional(CompoundNBT compound) {
    	super.writeAdditional(compound);
	}
	
	protected void spawnLimbs() {
        bodyCache = new PlantBossBody(this);
        parts[0] = bodyCache;
        dataManager.set(BODY_ID, Optional.of(bodyCache.getUniqueID()));
		
		for (int i = 0; i < NumberOfLeaves; i++) {
			limbs[i] = new PlantBossLeafLimb(this, i);
			parts[i+1] = limbs[i];
		}
		
		// Add children to world
		for (MultiPartEntityPart<EntityPlantBoss> part : this.parts) {
			// Need to make sure to set a near-ish position for the entities or they may not be added tto the world
			part.setPosition(this.getPosX(), this.getPosY(), this.getPosZ());
			this.world.addEntity(part);
		}
	}
	
	protected void searchForBody() {
		Optional<UUID> bodyID = this.dataManager.get(BODY_ID);
		if (bodyID.isPresent()) {
			Entity e = Entities.FindEntity(this.world, bodyID.get());
			if (e != null && e instanceof PlantBossBody) {
				bodyCache = (PlantBossBody) e;
			}
		}
	}
	
	protected void positionLeaves(PlantBossLeafLimb[] limbs) {
		for (PlantBossLeafLimb limb : limbs) {
			final float yawProg = ((float) limb.getLeafIndex() / (float) limbs.length); // 0 to 1
			final double limbRot = Math.PI * 2 * yawProg
								//+ (this.rotationYawHead * Math.PI / 180.0) // don't rotate
								;
			final double radius = this.getBody().getWidth() * (limb.getLeafIndex() % 2 == 0 ? 1.25 : 1.5);
			
			final double x = this.getPosX()
					+ Math.cos(limbRot) * radius;
			final double z = this.getPosZ() + Math.sin(limbRot) * radius;
			
			final float pitch = calcTargetLeafPitch(limb);
			limb.setLocationAndAngles(x, getPosY(), z, yawProg * 360f, pitch);
			
			this.setLeafPitch(limb.getLeafIndex(), pitch);
		}
	}
	
	protected float calcTargetLeafPitch(PlantBossLeafLimb limb) {
		// 0 unless curling/curled.
		final float pitch;
		if (this.curlTicks == 0
				|| (this.curlLeaveFrontOpen && limb.getLeafIndex() == 2) // front leaf and want it down
				) {
			// not curling
			pitch = 0;
		} else {
			// curling.
			final int elapsedTicks = Math.abs(this.curlTicks);
			final float prog = Math.max(0f, Math.min(1f, (float) elapsedTicks / (float) this.curlDuration));
			
			pitch = 90f * prog;
		}
		
		return pitch;
	}
	
	protected void spawnTreeParticles() {
		//		NostrumParticles.GLOW_ORB.spawn(this.world, new NostrumParticles.SpawnParams(
		//		1,
		//		this.getPosX(), this.getPosY() + this.getHeight() + 4, this.getPosZ(), 3,
		//		30, 10,
		//		this.getPositionVec().add(0, this.getHeight() + 1.7, 0)
		//		).color(this.getTreeElement().getColor()));
		
		//NostrumParticles.GLOW_ORB.spawn(this.world, new NostrumParticles.SpawnParams(
		//		1,
		//		this.getPosX(), this.getPosY() + this.getHeight() + 1.70, this.getPosZ(), .25,
		//		40, 20,
		//		new Vector3d(0, .2, 0), new Vector3d(.2, .1, .2)
		//		).gravity(true).color(this.getTreeElement().getColor()));
		
		NostrumParticles.LIGHTNING_STATIC.spawn(this.world, new NostrumParticles.SpawnParams(
				1,
				this.getPosX(), this.getPosY() + this.getHeight() + 1, this.getPosZ(), 1,
				40, 20,
				new Vector3d(0, .05, 0), Vector3d.ZERO
				).color(this.getTreeElement().getColor()));
	}
	
	protected void spawnWardParticles(int count) {
		NostrumParticles.WARD.spawn(this.world, new NostrumParticles.SpawnParams(
				count * 10,
				this.getPosX(), this.getPosY() + (this.getHeight() / 2), this.getPosZ(), this.getWidth() * 1.5,
				40, 10,
				new Vector3d(0, 0, 0), Vector3d.ZERO
				));
	}
	
	protected void spawnWardParticles(@Nonnull Vector3d at, int count) {
		// Calculate vector away from ent to where it got attacked
		Vector3d bounceDir = at.subtract(this.getPositionVec()).normalize();
		
		NostrumParticles.WARD.spawn(this.world, new NostrumParticles.SpawnParams(
				count * 5,
				at.x, at.y, at.z, .25,
				10, 5,
				bounceDir.scale(.01), new Vector3d(.0025, .0025, .0025)
				));
	}
	
	protected void clientTick() {
		if (this.getTreeType() == PlantBossTreeType.ELEMENTAL) {
			spawnTreeParticles();
		}
	}
	
	protected void curlTick() {
		if (this.curlTicks != 0 && this.curlTicks < this.curlDuration) {
			this.curlTicks++;
		}
	}
	
	@Override
	public void onAddedToWorld() {
		super.onAddedToWorld();
		
		if (!this.world.isRemote()) {
			// Can't spawn here, as chunk we're in may not be finished loading
			//spawnLimbs();
		}
	}
	
	@Override
	public void tick() {
		super.tick();
		
		this.aggroTable.decayTick();
		
//		for (MultiPartEntityPart part : this.parts) {
//			part.tick();
//		}
		
		this.rotationYawHead += .1f;
		
		curlTick();
		
		if (this.world.isRemote) {
			if (this.bodyCache == null) {
				searchForBody();
			}
			this.clientTick();
		} else {
			if (this.getBody() == null) {
				spawnLimbs();
			}
			
			getBody().setLocationAndAngles(getPosX(), getPosY(), getPosZ(), rotationYaw, rotationPitch);
			positionLeaves(limbs);
			this.tickStateMachine();
		}
		
		
	}
	
	protected void tickStateMachine() {
		BattleState curState = this.getCurrentState();
		BattleStateTask curTask = this.stateTasks.get(curState);
		
		curTask.update();
		if (curTask.isDone()) {
			curTask.stopTask();
			
			if (advanceStateSequence()) {
				// state has changed to idle
				;
			} else if (curState == BattleState.IDLE) {
				// Idle is done. Start a new sequence?
				startStateSequence(pickStateSequence());
			} else {
				// Current sequence continues
				;
			}
			
			// Regardless of sequence, reset the new task
			curState = this.getCurrentState();
			curTask = this.stateTasks.get(curState);
			
			curTask.startTask();
		}
	}
	
	// Advanced curernt state sequence. Returns true if a non-null sequence just finished.
	protected boolean advanceStateSequence() {
		if (this.currentSequence != null
				&& this.currentSequenceIndex >= this.currentSequence.length - 1) {
			this.currentSequenceIndex = 0;
			this.currentSequence = null;
			return true;
		} else {
			this.currentSequenceIndex++;
		}
		
		return false;
	}
	
	protected void startStateSequence(BattleState[] sequence) {
		this.currentSequence = sequence;
		this.currentSequenceIndex = 0;
	}
	
	protected BattleState[] pickStateSequence() {
		// If no targets, only go to idle
		if (this.getRandomTarget() == null) {
			return null;
		} else {
			BattleState[][] sequences = getCurrentPhase().stateSequences;
			return sequences[rand.nextInt(sequences.length)];
		}
	}
	
	protected @Nonnull BattleState getCurrentState() {
		// When not running a sequence, just stay idle
		if (this.currentSequence == null) {
			return BattleState.IDLE;
		}
		
		return this.currentSequence[this.currentSequenceIndex];
	}
	
	protected Phase getCurrentPhase() {
		return Phase.getPhaseFromHealth(this.getHealth() / this.getMaxHealth());
	}
	
	@Override
	protected void updateAITasks() {
		super.updateAITasks();
		
		this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
	}
	
	@Override
	public void addTrackingPlayer(ServerPlayerEntity player) {
		super.addTrackingPlayer(player);
		this.bossInfo.addPlayer(player);
	}

	@Override
	public void removeTrackingPlayer(ServerPlayerEntity player) {
		super.removeTrackingPlayer(player);
		this.bossInfo.removePlayer(player);
	}
	
	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		super.notifyDataManagerChange(key);
//		if (this.world != null && this.world.isRemote) {
//			if (key == LEAF_PITCHES) {
//				Float[] pitches = dataManager.get(LEAF_PITCHES);
//				for (int i = 0; i < NumberOfLeaves; i++) {
//					final float pitch = (pitches.length > i && pitches[i] != null) ? pitches[i] : 0f;
//					this.getLeafLimb(i).setPitch(pitch);
//				}
//			}
//		}
	}
	
	@Override
	public String getLoreKey() {
		return "nostrum__plant_boss";
	}

	@Override
	public String getLoreDisplayName() {
		return "Nettler";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("");
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("");
	}
	
	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ENTITY;
	}
	
//	@Override
//	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
//		int count = this.rand.nextInt(3) + 1;
//		count += lootingModifier;
//		
//		this.entityDropItem(NostrumResourceItem.getItem(ResourceType.EVIL_THISTLE, count), 0);
//		
//		count = 1 + lootingModifier / 2;
//		this.entityDropItem(NostrumResourceItem.getItem(ResourceType.MANA_LEAF, 1), 0);
//	}
	
	@Override
	public boolean attackEntityFromPart(MultiPartEntityPart<?> plantPart, DamageSource source, float damage) {
		if (plantPart == this.bodyCache || this.getWeakElement() != null) {
			return this.attackEntityFrom(source, damage);
		} else {
			// Leaves take no damage
			return true; 
		}
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (source.getTrueSource() == this) {
			return false;
		}
		
		if (source == DamageSource.IN_WALL) {
			//this.moveForced(this.getPositionVec().add(0, 1, 0));
			return false;
		}
		
		amount = Math.min(amount, 10f);
		
		if (this.getWeakElement() != null) {
			// Only let attacks of the right element through
			final @Nullable EMagicElement element;
			if (source instanceof MagicDamageSource) {
				element = ((MagicDamageSource) source).getElement();
			} else {
				element = null;
			}
			
			if (element != this.getWeakElement()) {
				if (!world.isRemote()) {
					if (source.getDamageLocation() != null) {
						 spawnWardParticles(source.getDamageLocation(), 1);
					} else {
						spawnWardParticles(1);
					}
				}
				
				return true;
			}
		}
		
		if (!this.world.isRemote && source.getTrueSource() != null) {
			Entity ent = source.getTrueSource();
			if (ent instanceof LivingEntity && ent != this) {
				this.aggroTable.addDamage((LivingEntity) ent, amount);
			}
		}
		
		return super.attackEntityFrom(source, amount);
	}
	
	@Override
	public World getWorld() {
		return this.world;
	}
	
	@Override
	@Nullable
	public Entity[] getEnityParts() {
		return this.parts;
	}
	
	@Override
	public boolean canBeCollidedWith() {
		return false; // Wants parts to be collided with, not main entity
		//return super.canBeCollidedWith();
	}
	
	@Override
	public boolean canBePushed() {
		return false;
	}
	
	public boolean isPartOfMe(Entity ent) {
		if (this.getParts() != null) {
			for (Entity part : this.getParts()) {
				if (part != null && part.equals(ent)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	@Override
	protected void collideWithEntity(Entity entityIn) {
		if (entityIn == this || isPartOfMe(entityIn)) {
			return;
		}
		
		if (entityIn instanceof PlayerEntity && ((PlayerEntity) entityIn).isSpectator()) {
			return;
		}
		
		pushEntity(entityIn);
	}
	
	public @Nullable PlantBossLeafLimb getLeafLimb(int index) {
		if (index < this.limbs.length) {
			return limbs[index];
		}
		return null;
	}
	
	// Get the leaf's intended pitch according to the sync'ed data manager list
	protected float getLeafPitch(int index) {
		Float[] pitches = this.dataManager.get(LEAF_PITCHES);
		return (index < pitches.length &&  pitches[index] != null)
				? pitches[index]
				: 0f;
	}
	
	protected void setLeafPitch(int index, float pitch) {
		Float[] pitches = this.dataManager.get(LEAF_PITCHES);
		if (pitches == null) {
			pitches = new Float[NumberOfLeaves];
			Arrays.fill(pitches, -1f);
		}
		if (pitches[index] == null || pitches[index] != pitch) {
			pitches = pitches.clone();
			pitches[index] = pitch;
			this.dataManager.set(LEAF_PITCHES, pitches);
		}
	}
	
	// Can be null on client when still looking for matching body piece
	public @Nullable PlantBossBody getBody() {
		return this.bodyCache;
	}
	
	public EMagicElement getTreeElement() {
		return getWeakElement();
	}
	
	public PlantBossTreeType getTreeType() {
		return this.dataManager.get(TREE_TYPE);
	}
	
	protected void setTreeType(@Nonnull PlantBossTreeType type) {
		this.dataManager.set(TREE_TYPE, type);
	}
	
	protected @Nullable LivingEntity getRandomTarget() {
		@Nullable LivingEntity target = aggroTable.getMainTarget();
		if (target == null) {
			// Just try nearby entities
			AxisAlignedBB searchBox = this.getBoundingBox().grow(16, 8, 16);
			List<Entity> ents = this.world.getEntitiesInAABBexcluding(this, searchBox, (e) -> {
				return (e instanceof LivingEntity)
						&& (!(e instanceof PlayerEntity) || !((PlayerEntity) e).isCreative())
						&& (this.canEntityBeSeen(e));
				});
			
			if (ents.isEmpty()) {
				target = null;
			} else {
				target = (LivingEntity) ents.get(this.rand.nextInt(ents.size()));
			}
		}
		
		return target;
	}
	
	protected List<LivingEntity> getAllTargets() {
		return aggroTable.getAllTracked();
	}
	
	protected boolean isStillTargetable(@Nonnull LivingEntity target) {
		return this.getEntitySenses().canSee(target);
	}
	
	protected void setCasting(@Nullable Spell spell) {
		; // TODO
	}
	
	public boolean isCasting() {
		return false; // TODO
	}
	
	public @Nullable Spell getCastingSpell() {
		return null; // TODO
	}
	
	protected void discoverArena() {
		BlockPos.Mutable cursor = new BlockPos.Mutable();
		int remaining;
		
		cursor.setPos(this.getPosX(), this.getPosY(), this.getPosZ()).move(Direction.DOWN);
		remaining = 20;
		while (remaining-- > 0 && isArenaBlock(world.getBlockState(cursor))) {
			cursor.move(Direction.NORTH);
		}
		final int minZ = cursor.getZ() + 1;
		
		cursor.setPos(this.getPosX(), this.getPosY(), this.getPosZ()).move(Direction.DOWN);
		remaining = 20;
		while (remaining-- > 0 && isArenaBlock(world.getBlockState(cursor))) {
			cursor.move(Direction.SOUTH);
		}
		final int maxZ = cursor.getZ() - 1;
		
		cursor.setPos(this.getPosX(), this.getPosY(), this.getPosZ()).move(Direction.DOWN);
		remaining = 20;
		while (remaining-- > 0 && isArenaBlock(world.getBlockState(cursor))) {
			cursor.move(Direction.EAST);
		}
		final int maxX = cursor.getX() - 1;
		
		cursor.setPos(this.getPosX(), this.getPosY(), this.getPosZ()).move(Direction.DOWN);
		remaining = 20;
		while (remaining-- > 0 && isArenaBlock(world.getBlockState(cursor))) {
			cursor.move(Direction.WEST);
		}
		final int minX = cursor.getX() + 1;
		
		this.arenaMin = new BlockPos(minX, cursor.getY(), minZ);
		this.arenaMax = new BlockPos(maxX, cursor.getY(), maxZ);
	}
	
	protected void detectArena() {
		if (this.arenaMin == null) {
			this.discoverArena();
		}
	}
	
	protected BlockPos getArenaMin() {
		detectArena();
		return this.arenaMin;
	}
	
	protected BlockPos getArenaMax() {
		detectArena();
		return this.arenaMax;
	}
	
	public @Nullable BlockPos getRandomPillar() {
		detectArena();
		
		if (this.pillars == null) {
			this.pillars = scanForPillars();
		}
		
		if (this.pillars == null || this.pillars.length == 0) {
			return null;
		}
		
		return pillars[rand.nextInt(pillars.length)];
	}
	
	protected BlockPos[] scanForPillars() {
		List<BlockPos> pillars = new ArrayList<>();
		BlockPos.Mutable cursor = new BlockPos.Mutable();
		
		for (int x = arenaMin.getX(); x <= arenaMax.getX(); x++)
		for (int z = arenaMin.getZ(); z <= arenaMax.getZ(); z++) {
			
			if (Math.abs(x - (int) getPosX()) <= 4
					&& Math.abs(z - (int) getPosZ()) <= 4) {
				continue;
			}
			
			cursor.setPos(x, arenaMax.getY(), z);
			if (isPillarCenter(world, cursor)) {
				pillars.add(cursor.toImmutable());
			}
		}
		
		return pillars.toArray(new BlockPos[0]);
	}
	
	protected boolean isPillarCenter(World world, BlockPos pos) {
		// Lazy; just check if center of a 3x1x3 of pillar blocks
		BlockPos.Mutable cursor = new BlockPos.Mutable();
		int[] xs = new int[] {-1, 0, 1};
		int[] zs = new int[] {-1, 0, 1};
		for (int x : xs)
		for (int z : zs) {
			cursor.setPos(pos.getX() + (x),
					pos.getY(), 
					pos.getZ() + (z)
					);
			BlockState state = world.getBlockState(cursor);
			if (!isPillarBlock(state)) {
				return false;
			}
		}
		
		return true;
	}
	
	protected boolean isPillarBlock(BlockState state) {
		return state.getBlock() instanceof DungeonBlock
				|| state.getBlock() == Blocks.GLOWSTONE;
		//return !(state.getBlock() instanceof FluidPoisonWater.FluidPoisonWaterBlock);
	}
	
	protected boolean isArenaBlock(BlockState state) {
		return isPillarBlock(state)
				|| state.getBlock() instanceof TeleportRune // Since main arena has one hidden underneath
				|| state.getBlock() instanceof PoisonWaterBlock
				|| isArenaBlock(state.getFluidState())
				;
		
	}
	
	protected boolean isArenaBlock(FluidState state) {
		return state.getFluid() instanceof FluidPoisonWater;
	}
	
	@Override
	protected float getStandingEyeHeight(Pose pose, EntitySize size) {
		if (this.eyeHeight == 0) {
			return size.height * .85f;
		}
		return this.eyeHeight;
	}
	
	protected void setEyeHeight(float height) {
		if (height <= 0) {
			height = this.getHeight() * .85f;
		}
		
		this.eyeHeight = height;
		this.recalculateSize(); // Prompts final parent field to refresh
	}
	
	protected void curlLeaves(int curlLength, boolean leaveFrontOpen) {
		// Only start curling if we aren't already curled/curling
		if (this.curlTicks == 0) {
			this.curlDuration = curlLength;
			this.curlTicks = 1;
		} else if (this.curlTicks < 0) {
			// was uncurling. Switch back to curling matching progress
			final float prog = (float) Math.abs(this.curlTicks) / (float) this.curlDuration;
			this.curlDuration = curlLength;
			this.curlTicks = Math.max(1, Math.round(curlLength * prog));
		}
		this.curlLeaveFrontOpen = leaveFrontOpen;
	}
	
	protected void uncurlLeaves(int uncurlLength) {
		this.curlDuration = uncurlLength;
		this.curlTicks = -uncurlLength;
	}
	
	protected void setWeakElement(@Nullable EMagicElement element) {
		this.dataManager.set(WEAK_ELEMENT, Optional.ofNullable(element));
	}
	
	public @Nullable EMagicElement getWeakElement() {
		return this.dataManager.get(WEAK_ELEMENT).orElse(null);
	}
	
	protected void pushEntity(Entity e) {
		Vector3d awayDir = e.getPositionVec().subtract(this.getPositionVec());
		double dist = awayDir.lengthSquared();
		double force = Math.min(.1, Math.max(.5, .1 * (16.0 / dist)));
		
		awayDir = awayDir.normalize().scale(force);
		e.addVelocity(awayDir.x, awayDir.y, awayDir.z);
	}
	
	public static class PlantBossBody extends MultiPartEntityPart<EntityPlantBoss> {
		
		public static final String ID = EntityPlantBoss.ID + ".body";
		
		public PlantBossBody(EntityType<? extends PlantBossBody> type, World world) {
			super(type, world, "PlantBoss_Body", 3, 3);
		}
		
		public PlantBossBody(EntityPlantBoss parent) {
			super(NostrumEntityTypes.plantBossBody, parent, "PlantBoss_Body", 3, 3);
		}
	}
	
	protected void spawnBramble(Direction side) {
		this.detectArena();
		
		final BlockPos start;
		final int width;
		final int dist;
		
		int dx = arenaMax.getX() - arenaMin.getX();
		int dz = arenaMax.getZ() - arenaMin.getZ();
		
		switch (side) {
		default:
		case NORTH:
			start = new BlockPos((int) this.getPosX(), this.getPosY(), arenaMax.getZ() + 1);
			width = dx;
			dist = dz + 2;
			break;
		case SOUTH:
			start = new BlockPos((int) this.getPosX(), this.getPosY(), arenaMin.getZ() - 1);
			width = dx;
			dist = dz + 2;
			break;
		case EAST:
			start = new BlockPos(arenaMin.getX() - 1, this.getPosY(), (int) this.getPosZ());
			width = dz;
			dist = dx + 2;
			break;
		case WEST:
			start = new BlockPos(arenaMax.getX() + 1, this.getPosY(), (int) this.getPosZ());
			width = dz;
			dist = dx + 2;
			break;
		}
		
		EntityPlantBossBramble bramble = new EntityPlantBossBramble(NostrumEntityTypes.plantBossBramble, world, this, width);
		bramble.setPosition(start.getX() + .5, start.getY(), start.getZ() + .5);
		bramble.setMotion(side, dist);
		world.addEntity(bramble);
	}
	
	public static class PlantBossLeafLimb extends MultiPartEntityPart<EntityPlantBoss> {
		
		public static final String ID = EntityPlantBoss.ID + ".leaf";
		
		protected static final DataParameter<Integer> INDEX = EntityDataManager.createKey(PlantBossLeafLimb.class, DataSerializers.VARINT);

		protected EntityPlantBoss plant;
		protected float effectivePitch;
		
		public PlantBossLeafLimb(EntityType<?> type, World world) {
			super(type, world, "PlantBoss_Leaf_Client", 4, 4f / 16f);
			this.plant = null;
		}
		
		public PlantBossLeafLimb(EntityPlantBoss parent, int index) {
			super(NostrumEntityTypes.plantBossLeaf, parent, "PlantBoss_Leaf_" + index, 4, 4f / 16f);
			this.effectivePitch = 0f;
			this.plant = parent;
			this.setLeafIndex(index);
		}
		
		public int getLeafIndex() {
			return this.dataManager.get(INDEX);
		}
		
		protected void setLeafIndex(int index) {
			this.dataManager.set(INDEX, index);
		}
		
		public float getYawOffset() {
			final float yawProg = ((float) getLeafIndex() / (float) EntityPlantBoss.NumberOfLeaves); // 0 to 1
			
			return yawProg * 360f;
		}
		
		public float getPitch() {
			return this.effectivePitch;
		}
		
		private void setPitch(float pitch) {
			this.rotationPitch = pitch;
			this.effectivePitch = pitch;
			
			final double pitchDiffForTopRad = 0.152680255;
			
			// Adjust size
			// Cheat knowing that the 'top' of the box is the heighest point for pitch 0-90
			final double newWidth = Math.cos(pitch / 180 * Math.PI) * 4;
			final double newHeight = Math.sin((pitch / 180 * Math.PI) + pitchDiffForTopRad) * 4;
			this.setSize((float) newWidth, (float) newHeight);
		}
		
		private float widthCache = 0;
		private float heightCache = 0;
		@Override
		public AxisAlignedBB getBoundingBox() {
			if (this.getWidth() != widthCache || this.getHeight() != heightCache) {
				this.widthCache = getWidth();
				this.heightCache = getHeight();
				
				// change BB to match pitch...
				AxisAlignedBB bb = this.getBoundingBox();
				final double centerZ = (bb.minZ + bb.maxZ) / 2;
				this.setBoundingBox(new AxisAlignedBB(
						bb.minX, bb.minY, centerZ - 2,
						bb.maxX, bb.maxY, centerZ + 2
						));
			}
			
			return super.getBoundingBox();
		}
		
		@Override
		public void tick() {
			super.tick();
			
			if (this.world != null && world.isRemote) {
				if (this.plant == null) {
					this.plant = this.getParent(); // Look up parent by ID
				}
			}
			
			// If pitch has changed in parent data manager, act on it!
			if (this.plant != null) {
				final float pitch = plant.getLeafPitch(this.getLeafIndex()); 
				if (pitch != this.effectivePitch) {
					this.setPitch(pitch);
				}
			}
			// else count down and disappear self?
		}
		
		@Override
		public boolean canBeCollidedWith() {
			return true;
		}
		
		@Override
		protected void registerData() {
			super.registerData();
			super.dataManager.register(INDEX, 0);
		}
	}
	
	protected abstract class BattleStateTask {
		
		protected final EntityPlantBoss parent;
		protected final BattleState state;
		
		// How long the task should last in ticks. If <0, this is not used and task must be completed some other way.
		protected final int durationTicks;
		
		// If > 0, random value added to duration on reset.
		// We will add somewhere between [-durationTicksJitter, durationTicksJitter] ticks;
		protected final int durationTicksJitter;
		
		
		// Transient current state variables
		protected int runTicks; // Duration for this run
		protected int elapsedTicks; // Number of ticks this run has received
		
		public BattleStateTask(EntityPlantBoss parent, BattleState state) {
			this(parent, state, -1);
		}
		
		public BattleStateTask(EntityPlantBoss parent, BattleState state, int durationTicks) {
			this(parent, state, durationTicks, -1);
		}
		
		public BattleStateTask(EntityPlantBoss parent, BattleState state, int durationTicks, int durationTicksJitter) {
			this.parent = parent;
			this.state = state;
			this.durationTicks = durationTicks;
			this.durationTicksJitter = durationTicksJitter;
		}
		
		public boolean isDone() {
			if (runTicks >= 0) {
				return elapsedTicks >= runTicks;
			}
			
			return false;
		}
		
		public void update() {
			this.elapsedTicks++;
		}
		
		public void startTask() {
			this.elapsedTicks = 0;
			
			if (durationTicks == 0) {
				runTicks = 0;
			} else if (durationTicks > 0) {
				if (this.durationTicksJitter > 0) {
					final int adj = parent.rand.nextInt(durationTicksJitter * 2) - durationTicksJitter;
					runTicks = Math.max(0, this.durationTicks + adj);
				} else {
					runTicks = this.durationTicks;
				}
			} else {
				runTicks = -1;
			}
		}
		
		public void stopTask() {
			;
		}
		
		protected final void doSpellCast(@Nonnull LivingEntity target, @Nonnull Spell spell, float castHeight) {
			@Nullable LivingEntity oldTarget = parent.getAttackTarget();
			if (target != null) {
				parent.faceEntity(target, 360f, 180f);
				parent.setAttackTarget(target);
			}
			
			//deductMana(spell, entity);
			final float eyeHeight = parent.getEyeHeight();
			if (castHeight > 0) {
				parent.setEyeHeight(castHeight);
			}
			spell.cast(parent, 1);
			if (castHeight > 0) {
				parent.setEyeHeight(eyeHeight);
			}
			parent.setAttackTarget(oldTarget);
		}
	}
	
	protected class BattleStateTaskIdle extends BattleStateTask {
		
		protected final Spell[] spells;
		private final float castChance;
		private final int castDuration;
		private final int castCooldown;
		
		protected @Nullable Spell castingSpell;
		protected @Nullable LivingEntity castingTarget;
		protected int castTicks; // 0 means nothing. 1-castDuration means casting. -castCooldown to -1 is cooling down.
		
		public BattleStateTaskIdle(EntityPlantBoss parent, int duration, int durationJitter, float castChance, int castDuration, int castCooldown, Spell ... spells) {
			super(parent, BattleState.IDLE, duration, durationJitter);
			this.spells = spells;
			this.castChance = castChance;
			this.castDuration = castDuration;
			this.castCooldown = castCooldown;
		}
		
		protected void doCastTick() {
			if (castTicks != 0) {
				castTicks++;
				
				if (castTicks == 0) {
					// cooldown done
				} else if (castTicks > castDuration) {
					// Casting time done. Cast!
					finishCastingSpell(this.castingSpell, this.castingTarget, parent.getRandomTarget());
					
					// Set in cooldown
					castTicks = -castCooldown;
					
					// Reset casting params
					this.castingSpell = null;
					this.castingTarget = null;
				}
			}
		}
		
		@Override
		public void update() {
			super.update();
			
			// While idling, we sometimes cast spells
			if (this.castTicks == 0
					&& parent.rand.nextFloat() < castChance) {
				Spell spell = chooseSpell();
				LivingEntity target = parent.getRandomTarget();
				if (spell != null && target != null) {
					startCastingSpell(spell, target);
				}
			}
			
			doCastTick(); // Do spell cast above so 0 casting time or cooldown works
			
//			int todo;
//			{
//				if (!world.isRemote && this.elapsedTicks % 20 == 0) {
//					parent.spawnBramble(Direction.EAST);
//				}
//			}
		}
		
		protected @Nullable Spell chooseSpell() {
			return (spells != null && spells.length > 0)
					? spells[parent.rand.nextInt(spells.length)]
					: null;
		}
		
		protected void startCastingSpell(@Nonnull Spell spell, @Nonnull LivingEntity target) {
			this.castingSpell = spell;
			this.castingTarget = target;
			
			this.castTicks = 1;
			
			parent.setCasting(spell);
		}
		
		protected void finishCastingSpell(@Nonnull Spell spell, @Nonnull LivingEntity target, @Nullable LivingEntity altTarget) {
			
			if (!target.isAlive() || !parent.isStillTargetable(target)) {
				target = altTarget;
			}
			
			this.doSpellCast(target, spell, -1f);
			
			parent.setCasting(null);
		}
		
		@Override
		public boolean isDone() {
			if (!super.isDone()) {
				return false;
			}
			
			// Timer is done but are we casting still?
			return castTicks == 0;
		}
		
		@Override
		public void stopTask() {
			super.stopTask();
			parent.setCasting(null); // Just in case
		}
	}

	protected class BattleStateTaskBombing extends BattleStateTask {
		
		private final int chargeTicks;
		protected final int cooldownTicks;
		
		public BattleStateTaskBombing(EntityPlantBoss parent, int chargeTicks, int cooldownTicks) {
			super(parent, BattleState.BOMBING, chargeTicks + cooldownTicks);
			this.chargeTicks = chargeTicks;
			this.cooldownTicks = cooldownTicks;
		}
		
		@Override
		public void update() {
			if (this.elapsedTicks == chargeTicks) {
				// Charge finished. Release the bomb!
				fireBomb();
			}
				
			super.update();
		}
		
		protected Spell getBombSpell() {
			return EntityPlantBoss.GetSeedBombSpell();
		}
		
		protected void fireBomb() {
			Spell spell = this.getBombSpell();
			if (spell != null) {
				// Pick a platform
				BlockPos pillar = parent.getRandomPillar();
				if (pillar != null) {
					// Face platform
					{
						double d0 = (pillar.getX() + .5) - parent.getPosX();
						double d2 = (pillar.getZ() + .5) - parent.getPosZ();
						double d1 = (pillar.getY() + 1) - (parent.getPosY() + parent.getEyeHeight());
						
						double d3 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);
						float f = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
						float f1 = (float)(-(MathHelper.atan2(d1, d3) * (180D / Math.PI)));
						parent.rotationPitch = f1;
						parent.rotationYaw = f;
					}
					
					doSpellCast(null, spell, -1);
				}
			}
		}
		
		@Override
		public void startTask() {
			super.startTask();
			
			parent.curlLeaves(20, false);
		}
		
		@Override
		public void stopTask() {
			super.stopTask();
			
			parent.uncurlLeaves(30);
		}
		
		@Override
		public boolean isDone() {
			return super.isDone();
		}
	}
	
	protected class BattleStateTaskPollinating extends BattleStateTask {
		
		private final int chargeTicks;
		public final int cooldownTicks;
		
		public BattleStateTaskPollinating(EntityPlantBoss parent, int chargeTicks, int cooldownTicks) {
			super(parent, BattleState.POLLINATING, chargeTicks + cooldownTicks);
			this.chargeTicks = chargeTicks;
			this.cooldownTicks = cooldownTicks;
		}
		
		@Override
		public void update() {
			if (this.elapsedTicks == chargeTicks) {
				// Charge finished. Release the pollen!
				firePollen();
			}
				
			super.update();
		}
		
		protected Spell getPollenSpell() {
			return EntityPlantBoss.GetPollenSpell();
		}
		
		protected void firePollen() {
			Spell spell = this.getPollenSpell();
			List<LivingEntity> targets = parent.getAllTargets();
			if (spell != null && targets != null && !targets.isEmpty()) {
				
				for (LivingEntity target : targets) {
					doSpellCast(target, spell, -1);
				}
			}
		}
		
		@Override
		public void startTask() {
			super.startTask();
			
			parent.curlLeaves(15, false);
		}
		
		@Override
		public void stopTask() {
			super.stopTask();
			
			parent.uncurlLeaves(15);
		}
		
		@Override
		public boolean isDone() {
			return super.isDone();
		}
	}
	
	protected class BattleStateTaskBulletSeeding extends BattleStateTask {
		
		protected final int rotations;
		protected final int periodTicks;

		protected float startingYaw;
		
		public BattleStateTaskBulletSeeding(EntityPlantBoss parent, int rotations, int periodTicks) {
			super(parent, BattleState.BULLET_SEEDING, periodTicks * rotations);
			this.rotations = rotations;
			this.periodTicks = periodTicks;
		}
		
		@Override
		public void update() {
			super.update();
			
			final float yaw = getYaw(this.startingYaw, this.elapsedTicks, this.periodTicks);
			parent.setRotationYawHead(yaw);
			parent.setPositionAndRotation(parent.getPosX(), parent.getPosY(), parent.getPosZ(), yaw, 0);
			//parent.setRotation(yaw, 0f);
			
			if (this.elapsedTicks % 5 == 0) {
				fireBullet();
			}
		}
		
		protected float getYaw(float startYaw, int elapsed, int period) {
			return startingYaw + ((float) (elapsed % period) / (float) period) * 360f;
		}
		
		protected Spell getBulletSpell() {
			return EntityPlantBoss.GetBulletSeedSpell();
		}
		
		protected void fireBullet() {
			Spell spell = this.getBulletSpell();
			if (spell != null) {
				doSpellCast(null, spell, .5f);
			}
		}
		
		@Override
		public void startTask() {
			super.startTask();
			
			this.startingYaw = (parent.rotationYaw + parent.rand.nextFloat() * 360f) % 360f;
			parent.curlLeaves(10, false); // want to be true but need to lower and raise leaves
		}
		
		@Override
		public void stopTask() {
			super.stopTask();
			
			parent.uncurlLeaves(10);
		}
		
		@Override
		public boolean isDone() {
			return super.isDone();
		}
	}
	
	protected class BattleStateTaskShielding extends BattleStateTask {
		
		protected final float maxDamage;

		protected float startingHealth;
		protected EMagicElement element;
		
		public BattleStateTaskShielding(EntityPlantBoss parent, int maxDuration, int maxDurationJitter, float maxDamage) {
			super(parent, BattleState.SHIELDING, maxDuration, maxDurationJitter);
			this.maxDamage = maxDamage;
		}
		
		protected EMagicElement chooseElement() {
			// Just random
			return EMagicElement.values()[parent.rand.nextInt(EMagicElement.values().length)];
		}
		
		@Override
		public void update() {
			super.update();
		}
		
		@Override
		public void startTask() {
			super.startTask();
			
			this.startingHealth = parent.getHealth();
			this.element = chooseElement();
			
			parent.curlLeaves(10, false);
			parent.setWeakElement(element);
			parent.setTreeType(PlantBossTreeType.ELEMENTAL);
		}
		
		@Override
		public void stopTask() {
			super.stopTask();
			
			parent.uncurlLeaves(10);
			parent.setWeakElement(null);
			parent.setTreeType(PlantBossTreeType.NORMAL);
		}
		
		@Override
		public boolean isDone() {
			// Check if max damage has been exceeded
			if (this.startingHealth - parent.getHealth() >= maxDamage) {
				return true;
			}
			
			return super.isDone();
		}
	}
	
	protected class BattleStateTaskSweeping extends BattleStateTask {
		
		protected final int spawnCount;
		protected final int spawnDelay;
		//protected final int waitTicks;

		protected int numSpawned;
		
		public BattleStateTaskSweeping(EntityPlantBoss parent, int spawnCount, int spawnDelay, int waitTicks) {
			super(parent, BattleState.SWEEPING, (spawnCount * spawnDelay) + waitTicks);
			this.spawnCount = spawnCount;
			this.spawnDelay = spawnDelay;
		}
		
		protected Direction getSpawnDirection() {
			// Could remember which we did and not do the same again...
			// but for now, just random
			return Plane.HORIZONTAL.random(parent.rand);
		}
		
		protected void spawnBramble(Direction direction) {
			parent.spawnBramble(direction);
		}
		
		protected void spawnBramble() {
			spawnBramble(getSpawnDirection());
		}
		
		@Override
		public void update() {
			super.update();
			
			// Spawn brambles if appropriate
			if (this.numSpawned < this.spawnCount) {
				if ((this.elapsedTicks - 1) % this.spawnDelay == 0) {
					this.spawnBramble();
					this.numSpawned++;
				}
			}
		}
		
		@Override
		public void startTask() {
			super.startTask();
			
			this.numSpawned = 0;
			
			// parent.curlLeaves(10, false);
		}
		
		@Override
		public void stopTask() {
			super.stopTask();
			
			// parent.uncurlLeaves(10);
		}
		
		@Override
		public boolean isDone() {
			return super.isDone();
		}
	}
	
	//broke()
	{
		/**
		 * Bramble class needs registered and a renderer and a model.
		 * And then we need to summon it. And collison might not be working.
		 * 
		 * I want to take a second and make the boss discover the reaches of the arena. That can speed up the pillar search, too.
		 * And then use that to set the .getWidth()/height of the brambles.
		 * 
		 * Also brambles need a direction to travel in and a max distance
		 */
	}
}
