package com.smanzana.nostrummagica.entity;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.entity.tasks.SpellAttackGoal;
import com.smanzana.nostrummagica.entity.tasks.StayHomeGoal;
import com.smanzana.nostrummagica.item.EssenceItem;
import com.smanzana.nostrummagica.loretag.IEntityLoreTagged;
import com.smanzana.nostrummagica.loretag.ILoreSupplier;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.serializer.MagicElementDataSerializer;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.component.SpellShapePart;
import com.smanzana.nostrummagica.spell.component.shapes.NostrumSpellShapes;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;
import com.smanzana.nostrummagica.util.DimensionUtils;
import com.smanzana.nostrummagica.util.SpellUtils;
import com.smanzana.petcommand.api.entity.ITameableEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WispEntity extends AbstractGolem implements ILoreSupplier, IEnchantableEntity, IElementalEntity {
	
	public static final String ID = "entity_wisp";
	
	protected static final double MAX_WISP_DISTANCE_SQ = 144;
	protected static final EntityDataAccessor<Optional<BlockPos>> HOME  = SynchedEntityData.<Optional<BlockPos>>defineId(WispEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
	protected static final EntityDataAccessor<EMagicElement> ELEMENT = SynchedEntityData.<EMagicElement>defineId(WispEntity.class, MagicElementDataSerializer.instance);
	
	public static final String LoreKey = "nostrum__wisp";
	
	private int idleCooldown;
	private int perilTicks;
	private @Nullable BlockPos perilLoc;

	private Spell defaultSpell;
	
	public WispEntity(EntityType<? extends WispEntity> type, Level worldIn) {
		super(type, worldIn);
		this.setNoGravity(true);
		this.moveControl = new WispMoveHelper(this);
		this.xpReward = 5;
		
		idleCooldown = NostrumMagica.rand.nextInt(20 * 30) + (20 * 10);
		perilTicks = 0;
		this.pickElement();
	}
	
	public WispEntity(EntityType<? extends WispEntity> type, Level worldIn, BlockPos homePos) {
		this(type, worldIn);
		this.restrictTo(homePos, (int) MAX_WISP_DISTANCE_SQ);
		this.setHome(homePos);
		this.xpReward = 0;
	}
	
	protected void registerGoals() {
		int priority = 1;
		this.goalSelector.addGoal(priority++, new AIRandomFly(this));
		this.goalSelector.addGoal(priority++, new SpellAttackGoal<WispEntity>(this, 20, 4, true, (wisp) -> {
			return wisp.getTarget() != null;
		}, new Spell[0]){
			@Override
			public Spell pickSpell(Spell[] spells, WispEntity wisp) {
				// Ignore empty array and use spell from the wisp
				return getSpellToUse();
			}
		});
		if (this.getHome() != null) {
			this.goalSelector.addGoal(priority++, new StayHomeGoal<WispEntity>(this, 1D, (MAX_WISP_DISTANCE_SQ * .8)));
		}
		
		priority = 1;
		this.targetSelector.addGoal(priority++, new HurtByTargetGoal(this).setAlertOthers(WispEntity.class));
		if (level != null && this.random.nextBoolean() && DimensionUtils.IsNether(level)) {
			this.targetSelector.addGoal(priority++, new NearestAttackableTargetGoal<Player>(this, Player.class, 10, true, false, null));
		} else {
			this.targetSelector.addGoal(priority++, new NearestAttackableTargetGoal<Monster>(this, Monster.class, 10, true, false, (mob) -> {
				// Wisps spawned with no home will be neutral
				if (this.getHome() == null) {
					return false;
				}
				
				return (mob instanceof ITameableEntity ? !((ITameableEntity) mob).isEntityTamed()
						: true);
			}));
		}
	}
	
	public static final AttributeSupplier.Builder BuildAttributes(){
		return AbstractGolem.createMobAttributes()
			.add(Attributes.MOVEMENT_SPEED, 0.1D)
			.add(Attributes.MAX_HEALTH, 5.0D)
			.add(Attributes.ARMOR, 0.0D)
			.add(Attributes.FOLLOW_RANGE, 30.0)
			.add(NostrumAttributes.magicResist, 50.0D);
	}

	protected void playStepSound(BlockPos pos, BlockState blockIn) {
		this.playSound(SoundEvents.GLASS_STEP, 0.15F, 1.0F);
	}

	protected SoundEvent getHurtSound(DamageSource source) {
		return NostrumMagicaSounds.CAST_FAIL.getEvent();
	}

	protected SoundEvent getDeathSound() {
		return NostrumMagicaSounds.SHIELD_BREAK.getEvent();
	}

	/**
	 * Returns the volume for the sounds this mob makes.
	 */
	protected float getSoundVolume() {
		return 1F;
	}

	protected float getStandingEyeHeight(Pose pose, EntityDimensions size) {
		return this.getBbHeight() * 0.5F;
	}

	public boolean doHurtTarget(Entity entityIn) {
		boolean flag = entityIn.hurt(DamageSource.mobAttack(this), (float)((int)this.getAttribute(Attributes.ATTACK_DAMAGE).getValue()));

		if (flag)
		{
			this.doEnchantDamageEffects(this, entityIn);
		}

		return flag;
	}

	public InteractionResult /*processInteract*/ mobInteract(Player player, InteractionHand hand, @Nonnull ItemStack stack) {
		return InteractionResult.PASS;
	}

	public boolean canBeLeashed(Player player) {
		return false;
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (idleCooldown > 0) {
			idleCooldown--;
			if (idleCooldown == 0) {
				if (this.getTarget() == null) {
					NostrumMagicaSounds.WISP_IDLE.play(this);
				}
				idleCooldown = NostrumMagica.rand.nextInt(20 * 30) + (20 * 10); 
			}
		}
		
		if (!level.isClientSide && this.getHome() == null && this.isAlive() && this.getHealth() > 0) {
			if (perilLoc == null || !perilLoc.equals(blockPosition())) {
				BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
				cursor.set(blockPosition());
				while (cursor.getY() > 0 && level.isEmptyBlock(cursor)) {
					cursor.move(Direction.DOWN);
				}
				
				final int diff = (int) (getY()) - cursor.getY();
				if (diff > 30) {
					perilLoc = this.blockPosition().immutable();
				}
			}
			
			if (perilLoc != null) {
				perilTicks++;
				if (perilTicks > 20 * 20) {
					// POP
					final EMagicElement elem;
					if (this.defaultSpell != null) {
						elem = this.defaultSpell.getPrimaryElement();
					} else {
						// else random
						elem = EMagicElement.values()[random.nextInt(EMagicElement.values().length)];
					}
					
					this.hurt(DamageSource.DROWN, 4.0F);
					if (!this.isAlive() || this.getHealth() <= 0) {
						this.spawnAtLocation(EssenceItem.getEssence(elem, 1), 0);
					}
				}
			}
		}
		
		if (level.isClientSide) {
			EMagicElement element = this.getElement();
			if (element == null) element = EMagicElement.PHYSICAL;
			int color = element.getColor();
			NostrumParticles.GLOW_ORB.spawn(level, new SpawnParams(
					1, getX(), getY() + getBbHeight()/2f, getZ(), 0, 40, 0,
					new Vec3(random.nextFloat() * .05 - .025, random.nextFloat() * .05 - .025, random.nextFloat() * .05 - .025), null
					).color(color));
		}
	}
	
	@Override
	public ILoreTagged getLoreTag() {
		return WispLoreTag.instance;
	}
	
	public static final class WispLoreTag implements IEntityLoreTagged<WispEntity> {
		
		private static final WispLoreTag instance = new WispLoreTag();
		public static final WispLoreTag instance() {
			return instance;
		}
	
		@Override
		public String getLoreKey() {
			return LoreKey;
		}
	
		@Override
		public String getLoreDisplayName() {
			return "Wisps";
		}
		
		@Override
		public Lore getBasicLore() {
			return new Lore().add("Strange floating vortexes of magical energy");
					
		}
		
		@Override
		public Lore getDeepLore() {
			return new Lore().add("Strange floating vortexes of magical energy.", "They are drawn to mani crystals, and can be passively spawned to protect you and your base.");
		}

		@Override
		public InfoScreenTabs getTab() {
			return InfoScreenTabs.INFO_ENTITY;
		}

		@Override
		public EntityType<WispEntity> getEntityType() {
			return NostrumEntityTypes.wisp;
		}
	}
	
	
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		
		this.entityData.define(HOME, Optional.empty());
		this.entityData.define(ELEMENT, EMagicElement.PHYSICAL);
	}
	
	protected void setHome(BlockPos home) {
		this.entityData.set(HOME, Optional.of(home));
		this.restrictTo(home, (int) MAX_WISP_DISTANCE_SQ);
	}
	
	public BlockPos getHome() {
		return this.entityData.get(HOME).orElse(null);
	}
	
	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		if (compound.contains("home", Tag.TAG_LONG)) {
			setHome(BlockPos.of(compound.getLong("home"))); // Warning: can break if save used across game versions
		}
	}
	
	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		
		BlockPos homePos = this.getHome();
		if (homePos != null) {
			compound.putLong("home", homePos.asLong());
		}
	}
	
	@Override
	public boolean saveAsPassenger(CompoundTag compound) {
		return false; // don't persist
	}
	
	@Override
	protected void checkFallDamage(double y, boolean onGround, BlockState stae, BlockPos pos) {
		
	}
	
	@OnlyIn(Dist.CLIENT)
	public int getBrightnessForRender(float partialTicks)
	{
		return 15728880;
	}

	/**
	 * Gets how bright this entity is.
	 */
	public float getBrightness(float partialTicks)
	{
		return 1.0F;
	}
	
	@Override
	public boolean isWithinRestriction(BlockPos pos) {
		// MobEntity version assumes if distances is not -1 that home is not null
		if (this.getHome() == null) {
			return true;
		} else {
			return super.isWithinRestriction(pos);
		}
	}
	
	@Override
	protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHitIn) {
		super.dropCustomDeathLoot(source, looting, recentlyHitIn);
	}
	
//	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
//		if (wasRecentlyHit && this.getHome() == null) {
//			int chance = 1 + (2 * lootingModifier);
//			if (this.rand.nextInt(100) < chance) {
//				this.entityDropItem(NostrumResourceItem.getItem(ResourceType.WISP_PEBBLE, 1), 0);
//			}
//			
//			// Research scroll
//			int chances = 1 + lootingModifier;
//			if (rand.nextInt(200) < chances) {
//				this.entityDropItem(NostrumSkillItem.getItem(SkillItemType.RESEARCH_SCROLL_SMALL, 1), 0);
//			}
//		}
//	}
	
	@Override
	public boolean hurt(DamageSource source, float amount) {
		this.playEffect(ParticleTypes.CRIT);
		return super.hurt(source, amount);
	}
	
	private void playEffect(ParticleOptions particle) {
		
		for (int i = 0; i < 15; ++i) {
			double d0 = this.random.nextGaussian() * 0.02D;
			double d1 = this.random.nextGaussian() * 0.02D;
			double d2 = this.random.nextGaussian() * 0.02D;
			this.level.addParticle(particle, this.getX() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(), this.getY() + 0.5D + (double)(this.random.nextFloat() * this.getBbHeight()), this.getZ() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(), d0, d1, d2);
		}
	}
	
	@Override
	public EMagicElement getElement() {
		return this.entityData.get(ELEMENT);
	}
	
	protected void setElement(EMagicElement element) {
		this.entityData.set(ELEMENT, element);
	}
	
	protected void pickElement() {
		// Pick a random element
		EMagicElement elem = EMagicElement.getRandom(getRandom());
		this.setElement(elem);
	}
	
	protected Spell getDefaultSpell() {
		init();
		return getRandSpell(getRandom(), this.getElement());
	}
	
	protected Spell getSpellToUse() {
		if (this.defaultSpell == null || this.defaultSpell.getPrimaryElement() != this.getElement()) {
			this.defaultSpell = getDefaultSpell();
		}
		
		Spell spell = this.defaultSpell;
		
		return spell;
	}
	
	@Override
	public int getMaxSpawnClusterSize() {
		return 1;
	}
	
	// Adapted from the wisp move helper
	static protected class WispMoveHelper extends MoveControl {
		private final WispEntity parentEntity;
		private int courseChangeCooldown;

		public WispMoveHelper(WispEntity wisp) {
			super(wisp);
			this.parentEntity = wisp;
		}

		@Override
		public void tick() {
			if (this.operation == MoveControl.Operation.MOVE_TO) {
				double d0 = this.getWantedX() - this.parentEntity.getX();
				double d1 = this.getWantedY() - this.parentEntity.getY();
				double d2 = this.getWantedZ() - this.parentEntity.getZ();
				double d3 = d0 * d0 + d1 * d1 + d2 * d2;
				
				if (d3 < 6) {
					this.operation = MoveControl.Operation.WAIT;
					return;
				}

				if (this.courseChangeCooldown-- <= 0) {
					this.courseChangeCooldown += this.parentEntity.getRandom().nextInt(5) + 2;
					d3 = (double)Math.sqrt(d3);

					if (this.isNotColliding(this.getWantedX(), this.getWantedY(), this.getWantedZ(), d3)) {
						this.parentEntity.setDeltaMovement(this.parentEntity.getDeltaMovement().add(
								d0 / d3 * 0.005D,
								d1 / d3 * 0.005D,
								d2 / d3 * 0.005D
								));
					} else {
						this.operation = MoveControl.Operation.WAIT;
					}
				}
			}
		}

		/**
		 * Checks if entity bounding box is not colliding with terrain
		 */
		private boolean isNotColliding(double x, double y, double z, double p_179926_7_) {
			double d0 = (x - this.parentEntity.getX()) / p_179926_7_;
			double d1 = (y - this.parentEntity.getY()) / p_179926_7_;
			double d2 = (z - this.parentEntity.getZ()) / p_179926_7_;
			AABB axisalignedbb = this.parentEntity.getBoundingBox();

			for (int i = 1; (double)i < p_179926_7_; ++i) {
				axisalignedbb = axisalignedbb.move(d0, d1, d2);

				if (!this.parentEntity.level.noCollision(this.parentEntity, axisalignedbb)) {
					return false;
				}
			}

			return true;
		}
	}
	
	// Copied from EntityFlying class
	@Override
	public void travel(Vec3 how) {
		if (this.isInWater()) {
			this.moveRelative(0.02F, how);
			this.move(MoverType.SELF, this.getDeltaMovement());
			this.setDeltaMovement(this.getDeltaMovement().scale(0.8));
		} else if (this.isInLava()) {
			this.moveRelative(0.02F, how);
			this.move(MoverType.SELF, this.getDeltaMovement());
			this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
		} else {
			float f = 0.91F;

			if (this.onGround) {
				//f = this.world.getBlockState(new BlockPos(MathHelper.floor(this.getPosX()), MathHelper.floor(this.getBoundingBox().minY) - 1, MathHelper.floor(this.getPosZ()))).getBlock().slipperiness * 0.91F;
				BlockPos underPos = new BlockPos(Mth.floor(this.getX()), Mth.floor(this.getBoundingBox().minY) - 1, Mth.floor(this.getZ()));
				BlockState underState = this.level.getBlockState(underPos);
				f = underState.getFriction(this.level, underPos, this) * 0.91F;
			}

			float f1 = 0.16277136F / (f * f * f);
			this.moveRelative(this.onGround ? 0.1F * f1 : 0.02F, how);
			f = 0.91F;

			if (this.onGround) {
				//f = this.world.getBlockState(new BlockPos(MathHelper.floor(this.getPosX()), MathHelper.floor(this.getBoundingBox().minY) - 1, MathHelper.floor(this.getPosZ()))).getBlock().slipperiness * 0.91F;
				BlockPos underPos = new BlockPos(Mth.floor(this.getX()), Mth.floor(this.getBoundingBox().minY) - 1, Mth.floor(this.getZ()));
				BlockState underState = this.level.getBlockState(underPos);
				f = underState.getFriction(this.level, underPos, this) * 0.91F;
			}

			this.move(MoverType.SELF, this.getDeltaMovement());
			this.setDeltaMovement(this.getDeltaMovement().scale(f));
		}

		this.calculateEntityAnimation(this, false);
	}

	/**
	 * returns true if this entity is by a ladder, false otherwise
	 */
	@Override
	public boolean onClimbable() {
		return false;
	}
	
	@Override
	public boolean checkSpawnRules(LevelAccessor world, MobSpawnType spawnReason) {
		if (!super.checkSpawnRules(world, spawnReason)) {
			return false;
		}
		
		return true;
	}
	
	public static boolean canSpawnExtraCheck(EntityType<WispEntity> type, ServerLevelAccessor world, MobSpawnType reason, BlockPos pos, Random rand) {
		AABB bb = new AABB(pos).inflate(40);
		if (DimensionUtils.IsOverworld(world.getLevel())) {
			if (world.getMaxLocalRawBrightness(pos) > .75f) {
				return false;
			}
	
			List<WispEntity> wisps = world.getEntitiesOfClass(WispEntity.class, bb, null);
			return wisps.size() < 20;
		} else {
			// Nether has smaller pool, so make it harder to spawn there than simply using weight (cause a weight of 1 is still too large)
			if (DimensionUtils.IsNether(world.getLevel())) {
				if (world.getDifficulty() == Difficulty.PEACEFUL || rand.nextInt(30) != 0 || pos.getY() > 120) {
					return false;
				}
			}
			
			// Other dimensions, just check nearby wisp count
			List<WispEntity> wisps = world.getEntitiesOfClass(WispEntity.class, bb, null);
			
			return wisps.size() < 1;
		}
	}
	
	static class AIRandomFly extends Goal {
		private final WispEntity parentEntity;

		public AIRandomFly(WispEntity wisp) {
			this.parentEntity = wisp;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		/**
		 * Returns whether the Goal should begin execution.
		 */
		public boolean canUse() {
			MoveControl MovementController = this.parentEntity.getMoveControl();

			if (!MovementController.hasWanted()) {
				return true;
			} else {
				double d0 = MovementController.getWantedX() - this.parentEntity.getX();
				double d1 = MovementController.getWantedY() - this.parentEntity.getY();
				double d2 = MovementController.getWantedZ() - this.parentEntity.getZ();
				double d3 = d0 * d0 + d1 * d1 + d2 * d2;
				return d3 < 1.0D || d3 > 3600.0D;
			}
		}

		/**
		 * Returns whether an in-progress Goal should continue executing
		 */
		@Override
		public boolean canContinueToUse() {
			return false;
		}

		/**
		 * Execute a one shot task or start executing a continuous task
		 */
		public void start() {
			Random random = this.parentEntity.getRandom();
			double d0 = this.parentEntity.getX() + (double)((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
			double d1 = this.parentEntity.getY() + (double)((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
			double d2 = this.parentEntity.getZ() + (double)((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
			this.parentEntity.getMoveControl().setWantedPosition(d0, d1, d2, 0.3D);
		}
	}
	
	private static Map<EMagicElement, List<Spell>> defaultSpells;
	
	private static @Nullable Spell getRandSpell(Random rand, EMagicElement element) {
		List<Spell> list = defaultSpells.get(element);
		if (list == null || list.isEmpty()) {
			return null;
		}
		
		return list.get(rand.nextInt(list.size()));
	}
	
	private static void putSpell(Spell spell, EMagicElement element) {
		List<Spell> list = defaultSpells.get(element);
		if (list == null) {
			list = new ArrayList<>();
			defaultSpells.put(element, list);
		}
		
		list.add(spell);
	}
	
	private static void putSpell(String name,
			SpellShape shape,
			EMagicElement element,
			int power,
			EAlteration alteration) {
		Spell spell = Spell.CreateAISpell(name);
		spell.addPart(new SpellShapePart(shape));
		spell.addPart(new SpellEffectPart(element, power, alteration));
		
		putSpell(spell, element);
	}
	
	private static void init() {
		if (defaultSpells == null) {
			defaultSpells = new EnumMap<>(EMagicElement.class);
			
			Spell spell;
			
			// Physical
			putSpell("Physic Blast",
					NostrumSpellShapes.Projectile,
					EMagicElement.PHYSICAL,
					1,
					null);
//			putSpell("Shield",
//					SelfTrigger.instance(),
//					AoEShape.instance(),
//					EMagicElement.PHYSICAL,
//					1,
//					EAlteration.RESIST);
//			putSpell("Weaken",
//					AITargetTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.PHYSICAL,
//					1,
//					EAlteration.INFLICT);
//			putSpell("Weaken II",
//					AITargetTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.PHYSICAL,
//					2,
//					EAlteration.INFLICT);
//			putSpell("Crush",
//					NostrumSpellShapes.Projectile,
//					SingleShape.instance(),
//					EMagicElement.PHYSICAL,
//					1,
//					null);
//			putSpell("Bone Crusher",
//					NostrumSpellShapes.Projectile,
//					SingleShape.instance(),
//					EMagicElement.PHYSICAL,
//					2,
//					null);
			
			// Lightning
			putSpell(SpellUtils.MakeSpell("Lightning Ball I",
					NostrumSpellShapes.Projectile,
					NostrumSpellShapes.Chain,
					EMagicElement.LIGHTNING,
					1,
					null), EMagicElement.LIGHTNING);
//			putSpell("Magic Shell",
//					SelfTrigger.instance(),
//					AoEShape.instance(),
//					EMagicElement.LIGHTNING,
//					1,
//					EAlteration.RESIST);
//			putSpell("Bolt",
//					BeamTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.LIGHTNING,
//					1,
//					EAlteration.CONJURE);
//			putSpell("Shock",
//					AITargetTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.LIGHTNING,
//					1,
//					EAlteration.INFLICT);
//			putSpell("Lightning Ball I",
//					NostrumSpellShapes.Projectile,
//					ChainShape.instance(),
//					EMagicElement.LIGHTNING,
//					1,
//					null);
//			putSpell("Lightning Ball II",
//					NostrumSpellShapes.Projectile,
//					SingleShape.instance(),
//					EMagicElement.LIGHTNING,
//					2,
//					null);
			
			// Fire
			putSpell("Burn",
					NostrumSpellShapes.SeekingBullet,
					EMagicElement.FIRE,
					1,
					EAlteration.CONJURE);
//			spell = new Spell("Fireball", true);
//			spell.addPart(new SpellPart(NostrumSpellShapes.Projectile));
//			spell.addPart(new SpellPart(AoEShape.instance(), EMagicElement.FIRE,
//					1, null, new SpellPartParam(3, false)));
//			defaultSpells.add(spell);
//			putSpell("Flare",
//					NostrumSpellShapes.Projectile,
//					SingleShape.instance(),
//					EMagicElement.FIRE,
//					3,
//					null);

			// Ice
			spell = SpellUtils.MakeSpell("Frostbite",
					NostrumSpellShapes.Projectile,
					EMagicElement.ICE, 1, EAlteration.INFLICT,
					EMagicElement.ICE, 1, null);
			putSpell(spell, EMagicElement.ICE);
//			putSpell("Magic Aegis",
//					SelfTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.ICE,
//					2,
//					EAlteration.SUPPORT);
//			putSpell("Ice Shard",
//					NostrumSpellShapes.Projectile,
//					SingleShape.instance(),
//					EMagicElement.ICE,
//					1,
//					null);
//			spell = new Spell("Group Frostbite", true);
//			spell.addPart(new SpellPart(NostrumSpellShapes.Projectile));
//			spell.addPart(new SpellPart(AoEShape.instance(), EMagicElement.ICE,
//					1, EAlteration.INFLICT, new SpellPartParam(5, false)));
//			defaultSpells.add(spell);
//			
//			putSpell("Hand Of Cold",
//					MagicCyclerTrigger.instance(),
//					AoEShape.instance(),
//					EMagicElement.ICE,
//					2,
//					EAlteration.RUIN);
			
			// Earth
			putSpell("Rock Fling",
					NostrumSpellShapes.Projectile,
					EMagicElement.EARTH,
					1,
					null);
//			putSpell("Earth Aegis",
//					SelfTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.EARTH,
//					2,
//					EAlteration.SUPPORT);
//			putSpell("Earth Aegis II",
//					SelfTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.EARTH,
//					3,
//					EAlteration.SUPPORT);
//			putSpell("Roots",
//					AITargetTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.EARTH,
//					3,
//					EAlteration.INFLICT);
//			putSpell("Rock Fling",
//					NostrumSpellShapes.Projectile,
//					SingleShape.instance(),
//					EMagicElement.EARTH,
//					1,
//					null);
//			putSpell("Earth Bash",
//					NostrumSpellShapes.Projectile,
//					SingleShape.instance(),
//					EMagicElement.EARTH,
//					2,
//					null);
			
			// Wind
			putSpell("Wind Slash",
					NostrumSpellShapes.Projectile,
					EMagicElement.WIND,
					1,
					null);
//			putSpell("Gust",
//					SelfTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.WIND,
//					2,
//					EAlteration.RESIST);
//			putSpell("Poison",
//					AITargetTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.WIND,
//					1,
//					EAlteration.INFLICT);
//			putSpell("Wind Slash",
//					MagicCutterTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.WIND,
//					1,
//					null);
//			putSpell("Wind Ball I",
//					NostrumSpellShapes.Projectile,
//					SingleShape.instance(),
//					EMagicElement.WIND,
//					2,
//					null);
//			putSpell("Wind Ball II",
//					NostrumSpellShapes.Projectile,
//					SingleShape.instance(),
//					EMagicElement.WIND,
//					3,
//					null);
			
			// Ender
			putSpell("Ender Beam",
					NostrumSpellShapes.Beam,
					EMagicElement.ENDER,
					1,
					null);
//			spell = new Spell("Blinker", true);
//			spell.addPart(new SpellPart(SelfTrigger.instance()));
//			spell.addPart(new SpellPart(DamagedTrigger.instance()));
//			spell.addPart(new SpellPart(OtherTrigger.instance()));
//			spell.addPart(new SpellPart(SingleShape.instance(), EMagicElement.ENDER,
//					2, EAlteration.GROWTH));
//			defaultSpells.add(spell);
//			putSpell("Blindness",
//					NostrumSpellShapes.Projectile,
//					AoEShape.instance(),
//					EMagicElement.ENDER,
//					1,
//					EAlteration.INFLICT);
//			putSpell("Random Teleport",
//					NostrumSpellShapes.Projectile,
//					SingleShape.instance(),
//					EMagicElement.ENDER,
//					1,
//					EAlteration.CONJURE);
		}
	}

	@Override
	public boolean canEnchant(Entity entity, EMagicElement element, int power) {
		return true;
	}

	@Override
	public boolean attemptEnchant(Entity entity, EMagicElement element, int power) {
		@Nullable EntityType<?> specialType = getSpecialTransformType(this.getElement(), element);
		if (specialType != null) {
			// Transform into the given entity
			Entity ent = specialType.create(level);
			ent.copyPosition(this);
			if (this.hasCustomName()) {
				ent.setCustomName(this.getCustomName());
				ent.setCustomNameVisible(this.isCustomNameVisible());
			}
			level.addFreshEntity(ent);
			this.discard();
			return true;
		}
		
		if (element == this.getElement()) {
			// Get a buff
			this.addEffect(new MobEffectInstance(NostrumEffects.magicBoost, 20 * 30, Math.max(0, power - 1)));
		} else {
			this.setElement(element);
		}
		return true;
	}
	
	protected static @Nullable EntityType<?> getSpecialTransformType(EMagicElement from, EMagicElement to) {
		// Fire -> Ender = Sprite
		if (EMagicElement.FIRE == from && EMagicElement.ENDER == to) {
			return NostrumEntityTypes.sprite;
		}
		
		// Earth -> Wind = Lux
		if (EMagicElement.EARTH == from && EMagicElement.WIND == to) {
			return NostrumEntityTypes.lux;
		}
		
		return null;
	}
}
