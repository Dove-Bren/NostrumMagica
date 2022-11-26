package com.smanzana.nostrummagica.entity.plantboss;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Optional;
import com.smanzana.nostrummagica.blocks.DungeonBlock;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.entity.AggroTable;
import com.smanzana.nostrummagica.fluids.FluidPoisonWater;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.serializers.FloatArraySerializer;
import com.smanzana.nostrummagica.serializers.OptionalMagicElementDataSerializer;
import com.smanzana.nostrummagica.serializers.PlantBossTreeTypeSerializer;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.components.MagicDamageSource;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.FieldTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.MagicCutterTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.MortarTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ProjectileTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.SeekingBulletTrigger;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.World;

public class EntityPlantBoss extends EntityMob implements ILoreTagged, IEntityMultiPart {
	
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
	
	public static final int NumberOfLeaves = 8;
	
	private static Spell[] IdleSpells = null;
	
	protected static Spell[] GetIdleSpells() {
		if (IdleSpells == null) {
			List<Spell> spells = new ArrayList<>();
			
			spells.add(makeSpell("Leaf Blade",
					MagicCutterTrigger.instance(),
					new SpellPartParam(1, true),
					SingleShape.instance(),
					EMagicElement.WIND,
					2,
					EAlteration.RUIN,
					null
					));
			spells.add(makeSpell("Spore",
					SeekingBulletTrigger.instance(),
					new SpellPartParam(0, true),
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
					new SpellPartParam(0, true),
					FieldTrigger.instance(),
					new SpellPartParam(2, false),
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
		Spell spell = new Spell(name, true);
		
		for (int i = 0; i < objects.length; i++) {
			Object o = objects[i];
			if (o instanceof SpellTrigger) {
				SpellTrigger trigger = (SpellTrigger) o;
				final SpellPartParam param;
				
				// Peek at next
				if (objects.length > i + 1
						&& objects[i+1] instanceof SpellPartParam) {
					param = (SpellPartParam) objects[++i];
				} else {
					param = new SpellPartParam(0, false); // matches SpellPart constructor with no second param
				}
				
				spell.addPart(new SpellPart(trigger, param));
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
	
	protected static final DataParameter<Float[]> LEAF_PITCHES = EntityDataManager.<Float[]>createKey(EntityPlantBoss.class, FloatArraySerializer.instance);
	protected static final DataParameter<Optional<EMagicElement>> WEAK_ELEMENT = EntityDataManager.<Optional<EMagicElement>>createKey(EntityPlantBoss.class, OptionalMagicElementDataSerializer.instance);
	protected static final DataParameter<PlantBossTreeType> TREE_TYPE = EntityDataManager.<PlantBossTreeType>createKey(EntityPlantBoss.class, PlantBossTreeTypeSerializer.instance);
	
	private final BossInfoServer bossInfo = (BossInfoServer)(new BossInfoServer(this.getDisplayName(), BossInfo.Color.GREEN, BossInfo.Overlay.NOTCHED_10)).setDarkenSky(true);
	private final PlantBossLeafLimb[] limbs;
	private final PlantBossBody body;
	private final MultiPartEntityPart[] parts;
	protected float eyeHeight;
	
	private Map<BattleState, BattleStateTask> stateTasks;
	
	private BlockPos arenaMin;
	private BlockPos arenaMax;
	private BlockPos[] pillars;
	
	// State machine
	private AggroTable<EntityLivingBase> aggroTable;
	private @Nullable BattleState[] currentSequence;
	private int currentSequenceIndex;
	
	private @Nullable EMagicElement weakElement;
	
	// Animation
	protected int curlTicks = 0; // 0 - no anim, 1+, curling, -1- - uncurling
	protected int curlDuration = 0;
	protected boolean curlLeaveFrontOpen;
	
	public EntityPlantBoss(World worldIn) {
		super(worldIn);
		this.setSize(7, 4); // Has to be large enough to enclose all parts. Body is 3x3
        this.ignoreFrustumCheck = true;
        this.experienceValue = 1250;
        this.entityCollisionReduction = 1f;
		
        this.parts = new MultiPartEntityPart[NumberOfLeaves + 1];
        body = new PlantBossBody(this);
        parts[0] = body;
        
		this.limbs = new PlantBossLeafLimb[NumberOfLeaves];
		for (int i = 0; i < NumberOfLeaves; i++) {
			limbs[i] = new PlantBossLeafLimb(this, i);
			parts[i+1] = limbs[i];
		}
		
		this.aggroTable = new AggroTable<>((ent) -> {
			return EntityPlantBoss.this.getEntitySenses().canSee(ent);
		});
		
		this.stateTasks = new EnumMap<>(BattleState.class);
		fillStateTasks(this.stateTasks);
		
		this.eyeHeight = this.height * .85f;
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
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(LEAF_PITCHES, new Float[NumberOfLeaves]);
		this.dataManager.register(WEAK_ELEMENT, Optional.absent());
		this.dataManager.register(TREE_TYPE, PlantBossTreeType.NORMAL);
	}
	
	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.00D);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(800.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(12.0D);
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_SPEED);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).setBaseValue(0.5D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(8D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0);
    }
	
	@Override
	protected void initEntityAI() {
		super.initEntityAI();
	}
	
	@Override
	protected boolean canDespawn() {
		return false;
	}
	
	public boolean canAttackClass(Class <? extends EntityLivingBase > cls) {
		return true;
	}
	
	public boolean isNonBoss() {
		return false;
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
    	super.writeEntityToNBT(compound);
	}
	
	protected void positionLeaves(PlantBossLeafLimb[] limbs) {
		for (PlantBossLeafLimb limb : limbs) {
			final float yawProg = ((float) limb.index / (float) limbs.length); // 0 to 1
			final double limbRot = Math.PI * 2 * yawProg
								//+ (this.rotationYawHead * Math.PI / 180.0) // don't rotate
								;
			final double radius = this.getBody().width * (limb.index % 2 == 0 ? 1.25 : 1.5);
			
			final double x = this.posX
					+ Math.cos(limbRot) * radius;
			final double z = this.posZ + Math.sin(limbRot) * radius;
			
			final float pitch = calcTargetLeafPitch(limb);
			limb.setLocationAndAngles(x, posY, z, yawProg * 360f, pitch);
			
			this.setLeafPitch(limb.index, pitch);
		}
	}
	
	protected float calcTargetLeafPitch(PlantBossLeafLimb limb) {
		// 0 unless curling/curled.
		final float pitch;
		if (this.curlTicks == 0
				|| (this.curlLeaveFrontOpen && limb.index == 2) // front leaf and want it down
				) {
			// not curling
			pitch = 0;
		} else {
			// curling.
			final int elapsedTicks = Math.abs(this.curlTicks);
			final float prog = Math.max(0f, Math.min(1f, (float) elapsedTicks / (float) this.curlDuration));
			//System.out.println("[" + limb.index + "] Pitch calced to: " + prog);
			
			pitch = 90f * prog;
		}
		
		return pitch;
	}
	
	protected void spawnTreeParticles() {
		//		NostrumParticles.GLOW_ORB.spawn(this.world, new NostrumParticles.SpawnParams(
		//		1,
		//		this.posX, this.posY + this.height + 4, this.posZ, 3,
		//		30, 10,
		//		this.getPositionVector().addVector(0, this.height + 1.7, 0)
		//		).color(this.getTreeElement().getColor()));
		
		//NostrumParticles.GLOW_ORB.spawn(this.world, new NostrumParticles.SpawnParams(
		//		1,
		//		this.posX, this.posY + this.height + 1.70, this.posZ, .25,
		//		40, 20,
		//		new Vec3d(0, .2, 0), new Vec3d(.2, .1, .2)
		//		).gravity(true).color(this.getTreeElement().getColor()));
		
		NostrumParticles.LIGHTNING_STATIC.spawn(this.world, new NostrumParticles.SpawnParams(
				1,
				this.posX, this.posY + this.height + 1, this.posZ, 1,
				40, 20,
				new Vec3d(0, .05, 0), Vec3d.ZERO
				).color(this.getTreeElement().getColor()));
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
	public void onUpdate() {
		super.onUpdate();
		
		this.aggroTable.decayTick();
		
		this.rotationYawHead += .1f;
		
		curlTick();
		
		getBody().setLocationAndAngles(posX, posY, posZ, rotationYaw, rotationPitch);
		
		if (this.world.isRemote) {
			this.clientTick();
		} else {
			positionLeaves(limbs);
			this.tickStateMachine();
		}
		
		for (MultiPartEntityPart part : this.parts) {
			part.onUpdate();
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
		BattleState[][] sequences = getCurrentPhase().stateSequences;
		return sequences[rand.nextInt(sequences.length)];
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
	public void addTrackingPlayer(EntityPlayerMP player) {
		super.addTrackingPlayer(player);
		this.bossInfo.addPlayer(player);
	}

	@Override
	public void removeTrackingPlayer(EntityPlayerMP player) {
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
	
	@Override
	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
		
	}
	
	@Override
	public boolean attackEntityFromPart(MultiPartEntityPart plantPart, DamageSource source, float damage) {
		if (plantPart == this.body || this.getWeakElement() != null) {
			return this.attackEntityFrom(source, damage);
		} else {
			return false; // Leaves take no damage
		}
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (source.getTrueSource() == this) {
			return false;
		}
		
		amount = Math.min(amount, 20f);
		
		if (this.getWeakElement() != null) {
			// Only let attacks of the right element through
			final @Nullable EMagicElement element;
			if (source instanceof MagicDamageSource) {
				element = ((MagicDamageSource) source).getElement();
			} else {
				element = null;
			}
			
			if (element != this.getWeakElement()) {
				return false;
			}
		}
		
		if (!this.world.isRemote && source.getTrueSource() != null) {
			Entity ent = source.getTrueSource();
			if (ent instanceof EntityLivingBase && ent != this) {
				this.aggroTable.addDamage((EntityLivingBase) ent, amount);
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
	public Entity[] getParts() {
		return this.parts;
	}
	
	@Override
	public boolean canBeCollidedWith() {
		return false;
		//return super.canBeCollidedWith();
	}
	
	@Override
	public boolean canBePushed() {
		return false;
	}
	
	@Override
	protected void collideWithEntity(Entity entityIn) {
		return; // Don't push others away
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
	
	public PlantBossBody getBody() {
		return this.body;
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
	
	protected @Nullable EntityLivingBase getRandomTarget() {
		return aggroTable.getMainTarget();
	}
	
	protected List<EntityLivingBase> getAllTargets() {
		return aggroTable.getAllTracked();
	}
	
	protected boolean isStillTargetable(@Nonnull EntityLivingBase target) {
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
		MutableBlockPos cursor = new MutableBlockPos();
		int remaining;
		
		cursor.setPos(this).move(EnumFacing.DOWN);
		remaining = 20;
		while (remaining-- > 0 && isArenaBlock(world.getBlockState(cursor))) {
			cursor.move(EnumFacing.NORTH);
		}
		final int minZ = cursor.getZ() + 1;
		
		cursor.setPos(this).move(EnumFacing.DOWN);
		remaining = 20;
		while (remaining-- > 0 && isArenaBlock(world.getBlockState(cursor))) {
			cursor.move(EnumFacing.SOUTH);
		}
		final int maxZ = cursor.getZ() - 1;
		
		cursor.setPos(this).move(EnumFacing.DOWN);
		remaining = 20;
		while (remaining-- > 0 && isArenaBlock(world.getBlockState(cursor))) {
			cursor.move(EnumFacing.EAST);
		}
		final int maxX = cursor.getX() - 1;
		
		cursor.setPos(this).move(EnumFacing.DOWN);
		remaining = 20;
		while (remaining-- > 0 && isArenaBlock(world.getBlockState(cursor))) {
			cursor.move(EnumFacing.WEST);
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
	
	public BlockPos getRandomPillar() {
		detectArena();
		
		if (this.pillars == null) {
			this.pillars = scanForPillars();
		}
		
		return pillars[rand.nextInt(pillars.length)];
	}
	
	protected BlockPos[] scanForPillars() {
		List<BlockPos> pillars = new ArrayList<>();
		MutableBlockPos cursor = new MutableBlockPos();
		
		for (int x = arenaMin.getX(); x <= arenaMax.getX(); x++)
		for (int z = arenaMin.getZ(); z <= arenaMax.getZ(); z++) {
			
			if (Math.abs(x - (int) posX) <= 4
					&& Math.abs(z - (int) posZ) <= 4) {
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
		MutableBlockPos cursor = new MutableBlockPos();
		int[] xs = new int[] {-1, 0, 1};
		int[] zs = new int[] {-1, 0, 1};
		for (int x : xs)
		for (int z : zs) {
			cursor.setPos(pos.getX() + (x),
					pos.getY(), 
					pos.getZ() + (z)
					);
			IBlockState state = world.getBlockState(cursor);
			if (!isPillarBlock(state)) {
				return false;
			}
		}
		
		return true;
	}
	
	protected boolean isPillarBlock(IBlockState state) {
		return state.getBlock() instanceof DungeonBlock;
		//return !(state.getBlock() instanceof FluidPoisonWater.FluidPoisonWaterBlock);
	}
	
	protected boolean isArenaBlock(IBlockState state) {
		return isPillarBlock(state)
				|| state.getBlock() instanceof FluidPoisonWater.FluidPoisonWaterBlock;
	}
	
	@Override
	public float getEyeHeight() {
		return this.eyeHeight;
	}
	
	protected void setEyeHeight(float height) {
		if (height <= 0) {
			height = this.height * .85f;
		}
		
		this.eyeHeight = height;
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
		this.dataManager.set(WEAK_ELEMENT, Optional.fromNullable(element));
	}
	
	public @Nullable EMagicElement getWeakElement() {
		return this.dataManager.get(WEAK_ELEMENT).orNull();
	}
	
	public static class PlantBossBody extends MultiPartEntityPart {
		public PlantBossBody(EntityPlantBoss parent) {
			super(parent, "PlantBoss_Body", 3, 3);
		}
	}
	
	protected void spawnBramble(EnumFacing side) {
		this.detectArena();
		
		final BlockPos start;
		final int width;
		final int dist;
		
		int dx = arenaMax.getX() - arenaMin.getX();
		int dz = arenaMax.getZ() - arenaMin.getZ();
		
		switch (side) {
		default:
		case NORTH:
			start = new BlockPos((int) this.posX, this.posY, arenaMax.getZ() + 1);
			width = dx;
			dist = dz + 2;
			break;
		case SOUTH:
			start = new BlockPos((int) this.posX, this.posY, arenaMin.getZ() - 1);
			width = dx;
			dist = dz + 2;
			break;
		case EAST:
			start = new BlockPos(arenaMin.getX() - 1, this.posY, (int) this.posZ);
			width = dz;
			dist = dx + 2;
			break;
		case WEST:
			start = new BlockPos(arenaMax.getX() + 1, this.posY, (int) this.posZ);
			width = dz;
			dist = dx + 2;
			break;
		}
		
		EntityPlantBossBramble bramble = new EntityPlantBossBramble(world, this, width);
		bramble.setPosition(start.getX() + .5, start.getY(), start.getZ() + .5);
		bramble.setMotion(side, dist);
		world.spawnEntity(bramble);
	}
	
	public static class PlantBossLeafLimb extends MultiPartEntityPart {

		protected final int index;
		protected final EntityPlantBoss plant;
		protected float effectivePitch;
		
		public PlantBossLeafLimb(EntityPlantBoss parent, int index) {
			super(parent, "PlantBoss_Leaf_" + index, 4, 4f / 16f);
			this.index = index;
			this.effectivePitch = 0f;
			this.plant = parent;
		}
		
		public float getYawOffset() {
			final float yawProg = ((float) index / (float) EntityPlantBoss.NumberOfLeaves); // 0 to 1
			
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
		public AxisAlignedBB getEntityBoundingBox() {
			if (this.width != widthCache || this.height != heightCache) {
				this.widthCache = width;
				this.heightCache = height;
				
				// change BB to match pitch...
				AxisAlignedBB bb = this.getEntityBoundingBox();
				final double centerZ = (bb.minZ + bb.maxZ) / 2;
				this.setEntityBoundingBox(new AxisAlignedBB(
						bb.minX, bb.minY, centerZ - 2,
						bb.maxX, bb.maxY, centerZ + 2
						));
			}
			
			return super.getEntityBoundingBox();
		}
		
		@Override
		public void onUpdate() {
			super.onUpdate();
			
			// If pitch has changed in parent data manager, act on it!
			final float pitch = plant.getLeafPitch(this.index); 
			if (pitch != this.effectivePitch) {
				this.setPitch(pitch);
			}
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
			
			System.out.println("Starting task for " + this.state.name());
		}
		
		public void stopTask() {
			;
		}
		
		protected final void doSpellCast(@Nonnull EntityLivingBase target, @Nonnull Spell spell, float castHeight) {
			@Nullable EntityLivingBase oldTarget = parent.getAttackTarget();
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
		protected @Nullable EntityLivingBase castingTarget;
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
				EntityLivingBase target = parent.getRandomTarget();
				if (spell != null && target != null) {
					startCastingSpell(spell, target);
				}
			}
			
			doCastTick(); // Do spell cast above so 0 casting time or cooldown works
			
//			int todo;
//			{
//				if (!world.isRemote && this.elapsedTicks % 20 == 0) {
//					parent.spawnBramble(EnumFacing.EAST);
//				}
//			}
		}
		
		protected @Nullable Spell chooseSpell() {
			return (spells != null && spells.length > 0)
					? spells[parent.rand.nextInt(spells.length)]
					: null;
		}
		
		protected void startCastingSpell(@Nonnull Spell spell, @Nonnull EntityLivingBase target) {
			this.castingSpell = spell;
			this.castingTarget = target;
			
			this.castTicks = 1;
			
			parent.setCasting(spell);
		}
		
		protected void finishCastingSpell(@Nonnull Spell spell, @Nonnull EntityLivingBase target, @Nullable EntityLivingBase altTarget) {
			
			if (target.isDead || !parent.isStillTargetable(target)) {
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
				
				// Face platform
				{
					double d0 = (pillar.getX() + .5) - parent.posX;
					double d2 = (pillar.getZ() + .5) - parent.posZ;
					double d1 = (pillar.getY() + 1) - (parent.posY + parent.getEyeHeight());
					
					double d3 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);
					float f = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
					float f1 = (float)(-(MathHelper.atan2(d1, d3) * (180D / Math.PI)));
					parent.rotationPitch = f1;
					parent.rotationYaw = f;
				}
				
				doSpellCast(null, spell, -1);
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
			List<EntityLivingBase> targets = parent.getAllTargets();
			if (spell != null && targets != null && !targets.isEmpty()) {
				
				for (EntityLivingBase target : targets) {
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
			parent.setPositionAndRotation(parent.posX, parent.posY, parent.posZ, yaw, 0);
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
		
		protected EnumFacing getSpawnDirection() {
			// Could remember which we did and not do the same again...
			// but for now, just random
			return EnumFacing.HORIZONTALS[parent.rand.nextInt(EnumFacing.HORIZONTALS.length)];
		}
		
		protected void spawnBramble(EnumFacing direction) {
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
		 * And then use that to set the width/height of the brambles.
		 * 
		 * Also brambles need a direction to travel in and a max distance
		 */
	}
}
