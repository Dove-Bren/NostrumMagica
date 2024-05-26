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
import com.smanzana.nostrummagica.attributes.NostrumAttributes;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.effects.NostrumEffects;
import com.smanzana.nostrummagica.entity.tasks.EntityAIStayHomeTask;
import com.smanzana.nostrummagica.entity.tasks.EntitySpellAttackTask;
import com.smanzana.nostrummagica.items.EssenceItem;
import com.smanzana.nostrummagica.loretag.ILoreSupplier;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.serializers.MagicElementDataSerializer;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.spells.components.legacy.ChainShape;
import com.smanzana.nostrummagica.spells.components.legacy.LegacySpell;
import com.smanzana.nostrummagica.spells.components.legacy.LegacySpellPart;
import com.smanzana.nostrummagica.spells.components.legacy.LegacySpellShape;
import com.smanzana.nostrummagica.spells.components.legacy.SingleShape;
import com.smanzana.nostrummagica.spells.components.legacy.triggers.BeamTrigger;
import com.smanzana.nostrummagica.spells.components.legacy.triggers.MagicCutterTrigger;
import com.smanzana.nostrummagica.spells.components.legacy.triggers.ProjectileTrigger;
import com.smanzana.nostrummagica.spells.components.legacy.triggers.SeekingBulletTrigger;
import com.smanzana.nostrummagica.utils.DimensionUtils;
import com.smanzana.petcommand.api.entity.ITameableEntity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;

public class EntityWisp extends GolemEntity implements ILoreSupplier, IEnchantableEntity {
	
	public static final String ID = "entity_wisp";
	
	protected static final double MAX_WISP_DISTANCE_SQ = 144;
	protected static final DataParameter<Optional<BlockPos>> HOME  = EntityDataManager.<Optional<BlockPos>>createKey(EntityWisp.class, DataSerializers.OPTIONAL_BLOCK_POS);
	protected static final DataParameter<EMagicElement> ELEMENT = EntityDataManager.<EMagicElement>createKey(EntityWisp.class, MagicElementDataSerializer.instance);
	
	public static final String LoreKey = "nostrum__wisp";
	
	private int idleCooldown;
	private int perilTicks;
	private @Nullable BlockPos perilLoc;

	private LegacySpell defaultSpell;
	
	public EntityWisp(EntityType<? extends EntityWisp> type, World worldIn) {
		super(type, worldIn);
		this.setNoGravity(true);
		this.moveController = new WispMoveHelper(this);
		this.experienceValue = 5;
		
		idleCooldown = NostrumMagica.rand.nextInt(20 * 30) + (20 * 10);
		perilTicks = 0;
		this.pickElement();
	}
	
	public EntityWisp(EntityType<? extends EntityWisp> type, World worldIn, BlockPos homePos) {
		this(type, worldIn);
		this.setHomePosAndDistance(homePos, (int) MAX_WISP_DISTANCE_SQ);
		this.setHome(homePos);
		this.experienceValue = 0;
	}
	
	protected void registerGoals() {
		int priority = 1;
		this.goalSelector.addGoal(priority++, new AIRandomFly(this));
		this.goalSelector.addGoal(priority++, new EntitySpellAttackTask<EntityWisp>(this, 20, 4, true, (wisp) -> {
			return wisp.getAttackTarget() != null;
		}, new LegacySpell[0]){
			@Override
			public LegacySpell pickSpell(LegacySpell[] spells, EntityWisp wisp) {
				// Ignore empty array and use spell from the wisp
				return getSpellToUse();
			}
		});
		if (this.getHome() != null) {
			this.goalSelector.addGoal(priority++, new EntityAIStayHomeTask<EntityWisp>(this, 1D, (MAX_WISP_DISTANCE_SQ * .8)));
		}
		
		priority = 1;
		this.targetSelector.addGoal(priority++, new HurtByTargetGoal(this).setCallsForHelp(EntityWisp.class));
		if (world != null && this.rand.nextBoolean() && DimensionUtils.IsNether(world)) {
			this.targetSelector.addGoal(priority++, new NearestAttackableTargetGoal<PlayerEntity>(this, PlayerEntity.class, 10, true, false, null));
		} else {
			this.targetSelector.addGoal(priority++, new NearestAttackableTargetGoal<MonsterEntity>(this, MonsterEntity.class, 10, true, false, (mob) -> {
				// Wisps spawned with no home will be neutral
				if (this.getHome() == null) {
					return false;
				}
				
				return (mob instanceof ITameableEntity ? !((ITameableEntity) mob).isEntityTamed()
						: true);
			}));
		}
	}
	
	public static final AttributeModifierMap.MutableAttribute BuildAttributes(){
		return GolemEntity.func_233666_p_()
			.createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.1D)
			.createMutableAttribute(Attributes.MAX_HEALTH, 5.0D)
			.createMutableAttribute(Attributes.ARMOR, 0.0D)
			.createMutableAttribute(Attributes.FOLLOW_RANGE, 30.0)
			.createMutableAttribute(NostrumAttributes.magicResist, 50.0D);
	}

	protected void playStepSound(BlockPos pos, BlockState blockIn) {
		this.playSound(SoundEvents.BLOCK_GLASS_STEP, 0.15F, 1.0F);
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

	protected float getStandingEyeHeight(Pose pose, EntitySize size) {
		return this.getHeight() * 0.5F;
	}

	public boolean attackEntityAsMob(Entity entityIn) {
		boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float)((int)this.getAttribute(Attributes.ATTACK_DAMAGE).getValue()));

		if (flag)
		{
			this.applyEnchantments(this, entityIn);
		}

		return flag;
	}

	public ActionResultType /*processInteract*/ func_230254_b_(PlayerEntity player, Hand hand, @Nonnull ItemStack stack) {
		return ActionResultType.PASS;
	}

	public boolean canBeLeashedTo(PlayerEntity player) {
		return false;
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (idleCooldown > 0) {
			idleCooldown--;
			if (idleCooldown == 0) {
				if (this.getAttackTarget() == null) {
					NostrumMagicaSounds.WISP_IDLE.play(this);
				}
				idleCooldown = NostrumMagica.rand.nextInt(20 * 30) + (20 * 10); 
			}
		}
		
		if (!world.isRemote && this.getHome() == null && this.isAlive() && this.getHealth() > 0) {
			if (perilLoc == null || !perilLoc.equals(getPosition())) {
				BlockPos.Mutable cursor = new BlockPos.Mutable();
				cursor.setPos(getPosition());
				while (world.isAirBlock(cursor)) {
					cursor.move(Direction.DOWN);
				}
				
				final int diff = (int) (getPosY()) - cursor.getY();
				if (diff > 30) {
					perilLoc = this.getPosition().toImmutable();
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
						elem = EMagicElement.values()[rand.nextInt(EMagicElement.values().length)];
					}
					
					this.attackEntityFrom(DamageSource.DROWN, 4.0F);
					if (!this.isAlive() || this.getHealth() <= 0) {
						this.entityDropItem(EssenceItem.getEssence(elem, 1), 0);
					}
				}
			}
		}
		
		if (world.isRemote) {
			EMagicElement element = this.getElement();
			if (element == null) element = EMagicElement.PHYSICAL;
			int color = element.getColor();
			NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
					1, getPosX(), getPosY() + getHeight()/2f, getPosZ(), 0, 40, 0,
					new Vector3d(rand.nextFloat() * .05 - .025, rand.nextFloat() * .05 - .025, rand.nextFloat() * .05 - .025), null
					).color(color));
		}
	}
	
	@Override
	public ILoreTagged getLoreTag() {
		return WispLoreTag.instance;
	}
	
	public static final class WispLoreTag implements ILoreTagged {
		
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
	}
	
	
	@Override
	protected void registerData() {
		super.registerData();
		
		this.dataManager.register(HOME, Optional.empty());
		this.dataManager.register(ELEMENT, EMagicElement.PHYSICAL);
	}
	
	protected void setHome(BlockPos home) {
		this.dataManager.set(HOME, Optional.of(home));
		this.setHomePosAndDistance(home, (int) MAX_WISP_DISTANCE_SQ);
	}
	
	public BlockPos getHome() {
		return this.dataManager.get(HOME).orElse(null);
	}
	
	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		if (compound.contains("home", NBT.TAG_LONG)) {
			setHome(BlockPos.fromLong(compound.getLong("home"))); // Warning: can break if save used across game versions
		}
	}
	
	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		
		BlockPos homePos = this.getHome();
		if (homePos != null) {
			compound.putLong("home", homePos.toLong());
		}
	}
	
	@Override
	public boolean writeUnlessRemoved(CompoundNBT compound) {
		return false; // don't persist
	}
	
	@Override
	public boolean onLivingFall(float distance, float damageMulti) {
		return false; // No fall damage
	}
	
	@Override
	protected void updateFallState(double y, boolean onGround, BlockState stae, BlockPos pos) {
		
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
	public boolean isWithinHomeDistanceFromPosition(BlockPos pos) {
		// MobEntity version assumes if distances is not -1 that home is not null
		if (this.getHome() == null) {
			return true;
		} else {
			return super.isWithinHomeDistanceFromPosition(pos);
		}
	}
	
	@Override
	protected void dropSpecialItems(DamageSource source, int looting, boolean recentlyHitIn) {
		super.dropSpecialItems(source, looting, recentlyHitIn);
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
	public boolean attackEntityFrom(DamageSource source, float amount) {
		this.playEffect(ParticleTypes.CRIT);
		return super.attackEntityFrom(source, amount);
	}
	
	private void playEffect(IParticleData particle) {
		
		for (int i = 0; i < 15; ++i) {
			double d0 = this.rand.nextGaussian() * 0.02D;
			double d1 = this.rand.nextGaussian() * 0.02D;
			double d2 = this.rand.nextGaussian() * 0.02D;
			this.world.addParticle(particle, this.getPosX() + (double)(this.rand.nextFloat() * this.getWidth() * 2.0F) - (double)this.getWidth(), this.getPosY() + 0.5D + (double)(this.rand.nextFloat() * this.getHeight()), this.getPosZ() + (double)(this.rand.nextFloat() * this.getWidth() * 2.0F) - (double)this.getWidth(), d0, d1, d2);
		}
	}
	
	public EMagicElement getElement() {
		return this.dataManager.get(ELEMENT);
	}
	
	protected void setElement(EMagicElement element) {
		this.dataManager.set(ELEMENT, element);
	}
	
	protected void pickElement() {
		// Pick a random element
		EMagicElement elem = EMagicElement.getRandom(getRNG());
		this.setElement(elem);
	}
	
	protected LegacySpell getDefaultSpell() {
		init();
		return getRandSpell(getRNG(), this.getElement());
	}
	
	protected LegacySpell getSpellToUse() {
		if (this.defaultSpell == null || this.defaultSpell.getPrimaryElement() != this.getElement()) {
			this.defaultSpell = getDefaultSpell();
		}
		
		LegacySpell spell = this.defaultSpell;
		
		return spell;
	}
	
	@Override
	public int getMaxSpawnedInChunk() {
		return 1;
	}
	
	// Adapted from the wisp move helper
	static protected class WispMoveHelper extends MovementController {
		private final EntityWisp parentEntity;
		private int courseChangeCooldown;

		public WispMoveHelper(EntityWisp wisp) {
			super(wisp);
			this.parentEntity = wisp;
		}

		@Override
		public void tick() {
			if (this.action == MovementController.Action.MOVE_TO) {
				double d0 = this.getX() - this.parentEntity.getPosX();
				double d1 = this.getY() - this.parentEntity.getPosY();
				double d2 = this.getZ() - this.parentEntity.getPosZ();
				double d3 = d0 * d0 + d1 * d1 + d2 * d2;
				
				if (d3 < 6) {
					this.action = MovementController.Action.WAIT;
					return;
				}

				if (this.courseChangeCooldown-- <= 0) {
					this.courseChangeCooldown += this.parentEntity.getRNG().nextInt(5) + 2;
					d3 = (double)MathHelper.sqrt(d3);

					if (this.isNotColliding(this.getX(), this.getY(), this.getZ(), d3)) {
						this.parentEntity.setMotion(this.parentEntity.getMotion().add(
								d0 / d3 * 0.005D,
								d1 / d3 * 0.005D,
								d2 / d3 * 0.005D
								));
					} else {
						this.action = MovementController.Action.WAIT;
					}
				}
			}
		}

		/**
		 * Checks if entity bounding box is not colliding with terrain
		 */
		private boolean isNotColliding(double x, double y, double z, double p_179926_7_) {
			double d0 = (x - this.parentEntity.getPosX()) / p_179926_7_;
			double d1 = (y - this.parentEntity.getPosY()) / p_179926_7_;
			double d2 = (z - this.parentEntity.getPosZ()) / p_179926_7_;
			AxisAlignedBB axisalignedbb = this.parentEntity.getBoundingBox();

			for (int i = 1; (double)i < p_179926_7_; ++i) {
				axisalignedbb = axisalignedbb.offset(d0, d1, d2);

				if (!this.parentEntity.world.hasNoCollisions(this.parentEntity, axisalignedbb)) {
					return false;
				}
			}

			return true;
		}
	}
	
	// Copied from EntityFlying class
	@Override
	public void travel(Vector3d how) {
		if (this.isInWater()) {
			this.moveRelative(0.02F, how);
			this.move(MoverType.SELF, this.getMotion());
			this.setMotion(this.getMotion().scale(0.8));
		} else if (this.isInLava()) {
			this.moveRelative(0.02F, how);
			this.move(MoverType.SELF, this.getMotion());
			this.setMotion(this.getMotion().scale(0.5));
		} else {
			float f = 0.91F;

			if (this.onGround) {
				//f = this.world.getBlockState(new BlockPos(MathHelper.floor(this.getPosX()), MathHelper.floor(this.getBoundingBox().minY) - 1, MathHelper.floor(this.getPosZ()))).getBlock().slipperiness * 0.91F;
				BlockPos underPos = new BlockPos(MathHelper.floor(this.getPosX()), MathHelper.floor(this.getBoundingBox().minY) - 1, MathHelper.floor(this.getPosZ()));
				BlockState underState = this.world.getBlockState(underPos);
				f = underState.getBlock().getSlipperiness(underState, this.world, underPos, this) * 0.91F;
			}

			float f1 = 0.16277136F / (f * f * f);
			this.moveRelative(this.onGround ? 0.1F * f1 : 0.02F, how);
			f = 0.91F;

			if (this.onGround) {
				//f = this.world.getBlockState(new BlockPos(MathHelper.floor(this.getPosX()), MathHelper.floor(this.getBoundingBox().minY) - 1, MathHelper.floor(this.getPosZ()))).getBlock().slipperiness * 0.91F;
				BlockPos underPos = new BlockPos(MathHelper.floor(this.getPosX()), MathHelper.floor(this.getBoundingBox().minY) - 1, MathHelper.floor(this.getPosZ()));
				BlockState underState = this.world.getBlockState(underPos);
				f = underState.getBlock().getSlipperiness(underState, this.world, underPos, this) * 0.91F;
			}

			this.move(MoverType.SELF, this.getMotion());
			this.setMotion(this.getMotion().scale(f));
		}

		this.prevLimbSwingAmount = this.limbSwingAmount;
		double d1 = this.getPosX() - this.prevPosX;
		double d0 = this.getPosZ() - this.prevPosZ;
		float f2 = MathHelper.sqrt(d1 * d1 + d0 * d0) * 4.0F;

		if (f2 > 1.0F) {
			f2 = 1.0F;
		}

		this.limbSwingAmount += (f2 - this.limbSwingAmount) * 0.4F;
		this.limbSwing += this.limbSwingAmount;
	}

	/**
	 * returns true if this entity is by a ladder, false otherwise
	 */
	@Override
	public boolean isOnLadder() {
		return false;
	}
	
	@Override
	public boolean canSpawn(IWorld world, SpawnReason spawnReason) {
		if (!super.canSpawn(world, spawnReason)) {
			return false;
		}
		
		return true;
	}
	
	public static boolean canSpawnExtraCheck(EntityType<EntityWisp> type, IServerWorld world, SpawnReason reason, BlockPos pos, Random rand) {
		AxisAlignedBB bb = new AxisAlignedBB(pos).grow(40);
		if (DimensionUtils.IsOverworld(world.getWorld())) {
			if (world.getLight(pos) > .75f) {
				return false;
			}
	
			List<EntityWisp> wisps = world.getEntitiesWithinAABB(EntityWisp.class, bb, null);
			return wisps.size() < 20;
		} else {
			// Nether has smaller pool, so make it harder to spawn there than simply using weight (cause a weight of 1 is still too large)
			if (DimensionUtils.IsNether(world.getWorld())) {
				if (world.getDifficulty() == Difficulty.PEACEFUL || rand.nextInt(30) != 0 || pos.getY() > 120) {
					return false;
				}
			}
			
			// Other dimensions, just check nearby wisp count
			List<EntityWisp> wisps = world.getEntitiesWithinAABB(EntityWisp.class, bb, null);
			
			return wisps.size() < 1;
		}
	}
	
	static class AIRandomFly extends Goal {
		private final EntityWisp parentEntity;

		public AIRandomFly(EntityWisp wisp) {
			this.parentEntity = wisp;
			this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		/**
		 * Returns whether the Goal should begin execution.
		 */
		public boolean shouldExecute() {
			MovementController MovementController = this.parentEntity.getMoveHelper();

			if (!MovementController.isUpdating()) {
				return true;
			} else {
				double d0 = MovementController.getX() - this.parentEntity.getPosX();
				double d1 = MovementController.getY() - this.parentEntity.getPosY();
				double d2 = MovementController.getZ() - this.parentEntity.getPosZ();
				double d3 = d0 * d0 + d1 * d1 + d2 * d2;
				return d3 < 1.0D || d3 > 3600.0D;
			}
		}

		/**
		 * Returns whether an in-progress Goal should continue executing
		 */
		@Override
		public boolean shouldContinueExecuting() {
			return false;
		}

		/**
		 * Execute a one shot task or start executing a continuous task
		 */
		public void startExecuting() {
			Random random = this.parentEntity.getRNG();
			double d0 = this.parentEntity.getPosX() + (double)((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
			double d1 = this.parentEntity.getPosY() + (double)((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
			double d2 = this.parentEntity.getPosZ() + (double)((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
			this.parentEntity.getMoveHelper().setMoveTo(d0, d1, d2, 0.3D);
		}
	}
	
	private static Map<EMagicElement, List<LegacySpell>> defaultSpells;
	
	private static @Nullable LegacySpell getRandSpell(Random rand, EMagicElement element) {
		List<LegacySpell> list = defaultSpells.get(element);
		if (list == null || list.isEmpty()) {
			return null;
		}
		
		return list.get(rand.nextInt(list.size()));
	}
	
	private static void putSpell(LegacySpell spell, EMagicElement element) {
		List<LegacySpell> list = defaultSpells.get(element);
		if (list == null) {
			list = new ArrayList<>();
			defaultSpells.put(element, list);
		}
		
		list.add(spell);
	}
	
	private static void putSpell(String name,
			SpellTrigger trigger,
			LegacySpellShape shape,
			EMagicElement element,
			int power,
			EAlteration alteration) {
		LegacySpell spell = LegacySpell.CreateAISpell(name);
		spell.addPart(new LegacySpellPart(trigger));
		spell.addPart(new LegacySpellPart(shape, element, power, alteration));
		
		putSpell(spell, element);
	}
	
	private static void init() {
		if (defaultSpells == null) {
			defaultSpells = new EnumMap<>(EMagicElement.class);
			
			LegacySpell spell;
			
			// Physical
			putSpell("Physic Blast",
					ProjectileTrigger.instance(),
					SingleShape.instance(),
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
//					ProjectileTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.PHYSICAL,
//					1,
//					null);
//			putSpell("Bone Crusher",
//					ProjectileTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.PHYSICAL,
//					2,
//					null);
			
			// Lightning
			putSpell("Lightning Ball I",
					ProjectileTrigger.instance(),
					ChainShape.instance(),
					EMagicElement.LIGHTNING,
					1,
					null);
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
//					ProjectileTrigger.instance(),
//					ChainShape.instance(),
//					EMagicElement.LIGHTNING,
//					1,
//					null);
//			putSpell("Lightning Ball II",
//					ProjectileTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.LIGHTNING,
//					2,
//					null);
			
			// Fire
			putSpell("Burn",
					SeekingBulletTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.FIRE,
					1,
					EAlteration.CONJURE);
//			spell = new Spell("Fireball", true);
//			spell.addPart(new SpellPart(ProjectileTrigger.instance()));
//			spell.addPart(new SpellPart(AoEShape.instance(), EMagicElement.FIRE,
//					1, null, new SpellPartParam(3, false)));
//			defaultSpells.add(spell);
//			putSpell("Flare",
//					ProjectileTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.FIRE,
//					3,
//					null);

			// Ice
			spell = LegacySpell.CreateAISpell("Frostbite");
			spell.addPart(new LegacySpellPart(ProjectileTrigger.instance()));
			spell.addPart(new LegacySpellPart(SingleShape.instance(), EMagicElement.ICE,
					1, EAlteration.INFLICT));
			spell.addPart(new LegacySpellPart(SingleShape.instance(), EMagicElement.ICE,
					1, null));
			putSpell(spell, EMagicElement.ICE);
//			putSpell("Magic Aegis",
//					SelfTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.ICE,
//					2,
//					EAlteration.SUPPORT);
//			putSpell("Ice Shard",
//					ProjectileTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.ICE,
//					1,
//					null);
//			spell = new Spell("Group Frostbite", true);
//			spell.addPart(new SpellPart(ProjectileTrigger.instance()));
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
					ProjectileTrigger.instance(),
					SingleShape.instance(),
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
//					ProjectileTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.EARTH,
//					1,
//					null);
//			putSpell("Earth Bash",
//					ProjectileTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.EARTH,
//					2,
//					null);
			
			// Wind
			putSpell("Wind Slash",
					MagicCutterTrigger.instance(),
					SingleShape.instance(),
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
//					ProjectileTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.WIND,
//					2,
//					null);
//			putSpell("Wind Ball II",
//					ProjectileTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.WIND,
//					3,
//					null);
			
			// Ender
			putSpell("Ender Beam",
					BeamTrigger.instance(),
					SingleShape.instance(),
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
//					ProjectileTrigger.instance(),
//					AoEShape.instance(),
//					EMagicElement.ENDER,
//					1,
//					EAlteration.INFLICT);
//			putSpell("Random Teleport",
//					ProjectileTrigger.instance(),
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
			Entity ent = specialType.create(world);
			ent.copyLocationAndAnglesFrom(this);
			if (this.hasCustomName()) {
				ent.setCustomName(this.getCustomName());
				ent.setCustomNameVisible(this.isCustomNameVisible());
			}
			world.addEntity(ent);
			this.remove();
			return true;
		}
		
		if (element == this.getElement()) {
			// Get a buff
			this.addPotionEffect(new EffectInstance(NostrumEffects.magicBoost, 20 * 30, Math.max(0, power - 1)));
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
