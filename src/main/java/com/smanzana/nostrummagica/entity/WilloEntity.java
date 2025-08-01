package com.smanzana.nostrummagica.entity;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.entity.tasks.OrbitEntityGenericGoal;
import com.smanzana.nostrummagica.entity.tasks.PanicGenericGoal;
import com.smanzana.nostrummagica.entity.tasks.SpellAttackGoal;
import com.smanzana.nostrummagica.item.InfusedGemItem;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.loretag.ELoreCategory;
import com.smanzana.nostrummagica.loretag.IEntityLoreTagged;
import com.smanzana.nostrummagica.loretag.ILoreSupplier;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.serializer.MagicElementDataSerializer;
import com.smanzana.nostrummagica.serializer.WilloStatusSerializer;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.MagicDamageSource;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.component.SpellShapePart;
import com.smanzana.nostrummagica.spell.component.shapes.NostrumSpellShapes;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;
import com.smanzana.nostrummagica.util.DimensionUtils;
import com.smanzana.nostrummagica.util.SpellUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags;

public class WilloEntity extends Monster implements ILoreSupplier, IElementalEntity {
	
	public static enum WilloStatus {
		NEUTRAL,
		PANIC,
		AGGRO,
	}
	
	public static final String ID = "entity_willo";
	
	protected static final double MAX_WISP_DISTANCE_SQ = 144;
	protected static final EntityDataAccessor<EMagicElement> ELEMENT = SynchedEntityData.<EMagicElement>defineId(WilloEntity.class, MagicElementDataSerializer.instance);
	protected static final EntityDataAccessor<WilloStatus> STATUS = SynchedEntityData.<WilloStatus>defineId(WilloEntity.class, WilloStatusSerializer.instance);
	
	private int idleCooldown;
	
	public WilloEntity(EntityType<? extends WilloEntity> type, Level worldIn) {
		super(type, worldIn);
		this.setNoGravity(true);
		this.moveControl = new WispMoveHelper(this);
		this.xpReward = 20;
		
		idleCooldown = NostrumMagica.rand.nextInt(20 * 30) + (20 * 10);
	}
	
	protected void registerGoals() {
		int priority = 1;
		this.goalSelector.addGoal(priority++, new SpellAttackGoal<WilloEntity>(this, 20, 4, true, (willo) -> {
			return willo.getTarget() != null && willo.getStatus() != WilloStatus.PANIC;
		}, new Spell[0]){
			@Override
			public Spell pickSpell(Spell[] spells, WilloEntity wisp) {
				// Ignore empty array and use spell from the wisp
				return getSpellToUse();
			}
		});
		this.goalSelector.addGoal(priority++, new PanicGenericGoal<WilloEntity>(this, 3.0, (e) -> {
			return WilloEntity.this.getStatus() == WilloStatus.PANIC;
		}));
		this.goalSelector.addGoal(priority++, new OrbitEntityGenericGoal<WilloEntity>(this, null, 3.0, 6 * 20, 2.0, 3 * 20, 2, (e) -> {
			return e.getTarget() != null
					&& WilloEntity.this.getStatus() == WilloStatus.AGGRO;
		}, (e) -> {
			return e.getTarget() != null
					&& WilloEntity.this.getStatus() == WilloStatus.AGGRO;
		}) {
			@Override
			protected LivingEntity getOrbitTarget() {
				return WilloEntity.this.getTarget();
			}
		});
		this.goalSelector.addGoal(priority++, new AIRandomFly(this));
		this.goalSelector.addGoal(priority++, new LookAtPlayerGoal(this, Player.class, 60f));
		this.goalSelector.addGoal(priority++, new RandomLookAroundGoal(this));
		
		priority = 1;
		this.targetSelector.addGoal(priority++, new HurtByTargetGoal(this).setAlertOthers(WilloEntity.class));
		this.targetSelector.addGoal(priority++, new NearestAttackableTargetGoal<Player>(this, Player.class, 10, true, false, null));
	}
	
	public static final AttributeSupplier.Builder BuildAttributes() {
		return Monster.createMonsterAttributes()
			.add(Attributes.MOVEMENT_SPEED, 0.2D)
			.add(Attributes.MAX_HEALTH, 10.0D)
			.add(Attributes.ARMOR, 4.0D)
			.add(Attributes.FOLLOW_RANGE, 30.0)
			.add(NostrumAttributes.magicResist, 0.0D);
	}

	protected void playStepSound(BlockPos pos, BlockState blockIn)
	{
		this.playSound(SoundEvents.GLASS_STEP, 0.15F, 1.0F);
	}

	protected SoundEvent getHurtSound(DamageSource source)
	{
		return NostrumMagicaSounds.LUX_HURT.getEvent();
	}

	protected SoundEvent getDeathSound()
	{
		return NostrumMagicaSounds.LUX_DEATH.getEvent();
	}

	/**
	 * Returns the volume for the sounds this mob makes.
	 */
	protected float getSoundVolume()
	{
		return 1F;
	}

	protected float getStandingEyeHeight(Pose pose, EntityDimensions size)
	{
		return this.getBbHeight() * 0.5F;
	}

	public boolean doHurtTarget(Entity entityIn)
	{
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
					NostrumMagicaSounds.LUX_IDLE.play(this);
				}
				idleCooldown = NostrumMagica.rand.nextInt(20 * 30) + (20 * 10); 
			}
		}
		
		if (this.getTarget() != null) {
			this.lookAt(this.getTarget(), 360f, 180f);
		} else {
			if (this.tickCount % 20 == 0 && this.getStatus() != WilloStatus.NEUTRAL) {
				this.setStatus(WilloStatus.NEUTRAL);
			}
		}
		
		if (this.getStatus() == WilloStatus.PANIC && this.getLastHurtByMob() != null) {
			// Up the time so we panic forever unless target is gone or we can't see them
			if (this.getLastHurtByMob().isAlive() && this.getSensing().hasLineOfSight(this.getLastHurtByMob())) {
				this.setLastHurtByMob(this.getLastHurtByMob()); // Refreshes timer to 100 ticks
			}
		}
		
		if (level.isClientSide) {
//			EMagicElement element = this.getElement();
//			int color = element.getColor();
//			Vector3d offset = this.getVectorForRotation(0f, this.rotationYawHead).rotateYaw(rand.nextBoolean() ? 90f : -90f).scale(.5);
//			final double yOffset =  Math.sin(2 * Math.PI * ((double) ticksExisted % 20.0) / 20.0) * (height/2);
//			NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
//					1,
//					getPosX() + offset.x,
//					getPosY() + height/2f + offset.y + yOffset,
//					getPosZ() + offset.z,
//					0, 40, 0,
//					offset.scale(rand.nextFloat() * .2f),
//					false
//					).color(color));
			
			EMagicElement element = this.getElement();
			int color = element.getColor();
			Vec3 offset = this.calculateViewVector(0f, this.yHeadRot).yRot(random.nextBoolean() ? 90f : -90f).scale(.5)
					.scale(random.nextFloat() * 3 + 1f);
			NostrumParticles.GLOW_ORB.spawn(level, new SpawnParams(
					1,
					getX() + offset.x,
					getY() + getBbHeight()/2f + offset.y,
					getZ() + offset.z,
					0, 40, 0,
					//offset.scale(rand.nextFloat() * .2f),
					new Vec3(0, -.05, 0),
					null
					).color(color));
		}
	}
	
	@Override
	public ILoreTagged getLoreTag() {
		return WilloLoreTag.instance;
	}
	
	public static final class WilloLoreTag implements IEntityLoreTagged<WilloEntity> {
		
		public static final String LoreKey = "nostrum__willo";
		
		private static final WilloLoreTag instance = new WilloLoreTag();
		public static final WilloLoreTag instance() {
			return instance;
		}
	
		@Override
		public String getLoreKey() {
			return LoreKey;
		}

		@Override
		public String getLoreDisplayName() {
			return "Willo";
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
		public ELoreCategory getCategory() {
			return ELoreCategory.ENTITY;
		}

		@Override
		public EntityType<WilloEntity> getEntityType() {
			return NostrumEntityTypes.willo;
		}
	}
	
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		
		this.entityData.define(STATUS, WilloStatus.NEUTRAL);
		this.entityData.define(ELEMENT, EMagicElement.NEUTRAL);
	}
	
	protected void setStatus(WilloStatus status) {
		if (level.isClientSide) {
			return;
		}
		
		this.entityData.set(STATUS, status);
	}
	
	public WilloStatus getStatus() {
		return this.entityData.get(STATUS);
	}
	
	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		if (compound.contains("element", Tag.TAG_STRING)) {
			try {
				this.entityData.set(ELEMENT, EMagicElement.parse(compound.getString("element").toUpperCase()));
			} catch (Exception e) {
				this.entityData.set(ELEMENT, EMagicElement.ICE);
			}
		}
//		if (compound.contains("status", Tag.TAG_STRING)) {
//			try {
//				this.dataManager.set(STATUS, WilloStatus.valueOf(compound.getString("status").toUpperCase()));
//			} catch (Exception e) {
//				this.dataManager.set(STATUS, WilloStatus.NEUTRAL);
//			}
//		}
	}
	
	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		
		compound.putString("element", this.getElement().name());
		//compound.putString("status", this.getStatus().name());
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
	protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHitIn) {
		super.dropCustomDeathLoot(source, looting, recentlyHitIn);
		
		if (recentlyHitIn) {
			// In panic mode, drop elemental gems.
			// In aggro mode, roll for mani gem.
			if (this.getStatus() == WilloStatus.PANIC) {
				final int count = 1 + (int) (Math.floor((float) looting / 2f));
				this.spawnAtLocation(InfusedGemItem.getGem(this.getElement(), count));
			} else if (this.getStatus() == WilloStatus.AGGRO) {
				final float fltCount = .25f + (looting * .375f); // .25, .625, 1, 1.375
				final int whole = (int) fltCount;
				final float frac = fltCount - whole;
				
				if (whole > 0) {
					this.spawnAtLocation(new ItemStack(NostrumItems.crystalSmall));
				}
				if (this.random.nextFloat() < frac) {
					this.spawnAtLocation(new ItemStack(NostrumItems.crystalSmall));
				}
			}
		}
	}
	
	@Override
	public boolean hurt(DamageSource source, float amount) {
		// Aggro if neutral or non-opposing element.
		// Panic if opposing element and not already panicked.
		
		
		if (source instanceof MagicDamageSource) {
			final MagicDamageSource magicSource = (MagicDamageSource) source;
			
			// Check element. Panic if opposite element is used
			if (this.getStatus() != WilloStatus.PANIC) {
				if (magicSource.getElement() == this.getElement().getOpposite()) {
					this.setStatus(WilloStatus.PANIC);
				} else {
					if (this.getStatus() == WilloStatus.NEUTRAL) {
						this.setStatus(WilloStatus.AGGRO);
					}
				}
			}
			
			this.playEffect(ParticleTypes.CRIT);
			return super.hurt(source, amount);
		} else if (source == DamageSource.IN_WALL
				|| source == DamageSource.CRAMMING
				|| source == DamageSource.DROWN
				|| source == DamageSource.OUT_OF_WORLD
				) {
			return super.hurt(source, amount);
		} else {
			NostrumMagicaSounds.CAST_FAIL.play(this);
			if (this.getStatus() == WilloStatus.NEUTRAL) {
				this.setStatus(WilloStatus.AGGRO);
			}
			
			return super.hurt(source, 0f);
		}
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
	
	protected Spell getSpellToUse() {
		init();
		List<Spell> spells = defaultSpells.get(this.getElement());
		int idx = (this.getStatus() == WilloStatus.NEUTRAL
				? this.random.nextInt(2)
				: this.random.nextInt(spells.size()));
		return spells.get(idx);
	}
	
	// Adapted from the wisp move helper
	static protected class WispMoveHelper extends MoveControl {
		private final WilloEntity parentEntity;
		private int courseChangeCooldown;

		public WispMoveHelper(WilloEntity wisp) {
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

				d3 = (double)Math.sqrt(d3);
				
//				if (Math.abs(d3) < .5) {
//					this.parentEntity.getMotion().x = 0;
//					this.parentEntity.getMotion().y = 0;
//					this.parentEntity.getMotion().z = 0;
//					this.action = MovementController.Action.WAIT;
//					return;
//				} else if (courseChangeCooldown-- <= 0) {
//					courseChangeCooldown = this.parentEntity.getRNG().nextInt(5) + 10;
//					
//					if (this.isNotColliding(this.getPosX(), this.getPosY(), this.getPosZ(), d3)) {
//						float basespeed = (float) this.parentEntity.getAttribute(Attributes.MOVEMENT_SPEED).getValue();
//						//speed *= 3f;
//						this.parentEntity.getMotion().x = (d0 / d3) * basespeed * speed;
//						this.parentEntity.getMotion().y = (d1 / d3) * basespeed  * speed;
//						this.parentEntity.getMotion().z = (d2 / d3) * basespeed  * speed;
//						
//						float f9 = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
//						this.entity.rotationYaw = this.limitAngle(this.entity.rotationYaw, f9, 90.0F);
//					} else {
//						this.action = MovementController.Action.WAIT;
//					}
//				}
				
				if (Math.abs(d3) < .5) {
					this.parentEntity.setDeltaMovement(Vec3.ZERO);
					this.operation = MoveControl.Operation.WAIT;
					return;
				} else if (this.isNotColliding(this.getWantedX(), this.getWantedY(), this.getWantedZ(), d3)) {
					float basespeed = (float) this.parentEntity.getAttribute(Attributes.MOVEMENT_SPEED).getValue();
					//speed *= 3f;
					this.parentEntity.setDeltaMovement(
							(d0 / d3) * basespeed * speedModifier,
							(d1 / d3) * basespeed  * speedModifier,
							(d2 / d3) * basespeed  * speedModifier
							);
					
					float f9 = (float)(Mth.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
					this.mob.setYRot(this.rotlerp(this.mob.getYRot(), f9, 90.0F));
				} else if (courseChangeCooldown-- <= 0) {
					courseChangeCooldown = this.parentEntity.getRandom().nextInt(5) + 10;
					this.operation = MoveControl.Operation.WAIT;
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
		if (!super.checkSpawnRules(world, spawnReason)) { // checks light level
			return false;
		}
		
		
		// Want to use dimension key but not available with IWorldReadyer
		Holder<Biome> biomeKey = world.getBiome(this.blockPosition());
		
//		if (!DimensionUtils.IsOverworld(world) && !DimensionUtils.IsNether(world)) {
//			return false;
//		}
		if (biomeKey.containsTag(Tags.Biomes.IS_END)) {
			return false;
		}
		
		return true;
	}
	
	static class AIRandomFly extends Goal {
		private final WilloEntity parentEntity;
		private int delayTicks = 0;

		public AIRandomFly(WilloEntity wisp) {
			this.parentEntity = wisp;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		/**
		 * Returns whether the Goal should begin execution.
		 */
		public boolean canUse() {
			
			if (delayTicks-- > 0) {
				return false;
			}
			
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
			final Vec3 center = (parentEntity.getTarget() == null ? parentEntity.position() : parentEntity.getTarget().position());
			final float range = (parentEntity.getTarget() == null ? 16f : 8f);
			double d0 = center.x + (double)((random.nextFloat() * 2.0F - 1.0F) * range);
			double d1 = center.y + (double)((random.nextFloat() * 2.0F - 1.0F) * range);
			double d2 = center.z + (double)((random.nextFloat() * 2.0F - 1.0F) * range);
			
			// Adjust to above ground
			double height = random.nextInt(4) + 2;
			BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
			cursor.set(d0, d1, d2);
			
			while (cursor.getY() > 0 && parentEntity.level.isEmptyBlock(cursor)) {
				cursor.move(Direction.DOWN);
			}
			
			while (cursor.getY() < 255 && !parentEntity.level.isEmptyBlock(cursor)) {
				cursor.move(Direction.UP);
			}
			
			// Try and move `height` up
			for (int i = 0; i < height; i++) {
				if (parentEntity.level.isEmptyBlock(cursor.above())) {
					cursor.move(Direction.UP);
				} else {
					break;
				}
			}
			
			this.parentEntity.getMoveControl().setWantedPosition(
					cursor.getX() + (d0 % 1.0),
					cursor.getY() + (d1 % 1.0),
					cursor.getZ() + (d2 % 1.0),
					1D);
			
			delayTicks = parentEntity.getStatus() == WilloStatus.PANIC ? 5 : random.nextInt(20 * 10) + 20 * 4;
		}
	}
	
	private static Map<EMagicElement, List<Spell>> defaultSpells;
	
	private static void putSpell(String name,
			EMagicElement element,
			int power,
			EAlteration alteration) {
		putSpell(name, null, null, element, power, alteration);
	}
	
	private static void putSpell(String name,
			SpellShape shape,
			EMagicElement element,
			int power,
			EAlteration alteration) {
		putSpell(name, shape, null, element, power, alteration);
	}
	
	private static void putSpell(String name,
			SpellShape shape1,
			SpellShape shape2,
			EMagicElement element,
			int power,
			EAlteration alteration) {
		Spell spell = Spell.CreateAISpell(name);
		if (shape1 != null) {
			spell.addPart(new SpellShapePart(shape1));
		}
		if (shape2 != null) {
			spell.addPart(new SpellShapePart(shape2));
		}
		spell.addPart(new SpellEffectPart(element, power, alteration));
		
		if (!defaultSpells.containsKey(element) || defaultSpells.get(element) == null) {
			defaultSpells.put(element, new ArrayList<>());
		}
		defaultSpells.get(element).add(spell);
	}
	
	private static void init() {
		if (defaultSpells == null) {
			defaultSpells = new EnumMap<>(EMagicElement.class);
			
			Spell spell;
			
			// Note: spell spot 1 and 2 are 'neutral' spells and can be cast on passing-by players.
			// The others are only cast when aggro or panicking
			
			// Neutral
			putSpell("Physic Blast",
					NostrumSpellShapes.Projectile,
					EMagicElement.NEUTRAL,
					2,
					EAlteration.HARM);
			putSpell("Shield",
					NostrumSpellShapes.Burst,
					EMagicElement.NEUTRAL,
					1,
					EAlteration.RESIST);
			putSpell("Shield",
					EMagicElement.NEUTRAL,
					2,
					EAlteration.SUPPORT);
			putSpell("Weaken",
					NostrumSpellShapes.Projectile,
					NostrumSpellShapes.Burst,
					EMagicElement.NEUTRAL,
					1,
					EAlteration.INFLICT);
			putSpell("Weaken II",
					NostrumSpellShapes.SeekingBullet,
					EMagicElement.NEUTRAL,
					2,
					EAlteration.INFLICT);
			putSpell("Summon Pets",
					EMagicElement.NEUTRAL,
					2,
					EAlteration.SUMMON);
//			putSpell("Crush",
//					NostrumSpellShapes.Projectile,
//					SingleShape.instance(),
//					EMagicElement.NEUTRAL,
//					1,
//					null);
//			putSpell("Bone Crusher",
//					NostrumSpellShapes.Projectile,
//					SingleShape.instance(),
//					EMagicElement.NEUTRAL,
//					2,
//					null);
			
			// Lightning
			putSpell("Lightning Ball I",
					NostrumSpellShapes.Projectile,
					EMagicElement.LIGHTNING,
					1,
					EAlteration.RUIN);
			putSpell("Shock",
					NostrumSpellShapes.SeekingBullet,
					EMagicElement.LIGHTNING,
					1,
					EAlteration.INFLICT);
			putSpell("Magic Shell",
					NostrumSpellShapes.Burst,
					EMagicElement.LIGHTNING,
					1,
					EAlteration.RESIST);
			putSpell("Bolt",
					NostrumSpellShapes.Projectile,
					EMagicElement.LIGHTNING,
					1,
					null);
			putSpell("Lightning Ball I",
					NostrumSpellShapes.Projectile,
					NostrumSpellShapes.Burst,
					EMagicElement.LIGHTNING,
					3,
					EAlteration.HARM);
			putSpell("Lightning Ball II",
					NostrumSpellShapes.Projectile,
					NostrumSpellShapes.Burst,
					EMagicElement.LIGHTNING,
					2,
					EAlteration.RUIN);
			
			// Fire
			putSpell("Burn",
					NostrumSpellShapes.SeekingBullet,
					EMagicElement.FIRE,
					2,
					null);
			putSpell("Overheat",
					NostrumSpellShapes.SeekingBullet,
					EMagicElement.FIRE,
					2,
					EAlteration.INFLICT);
			putSpell("Flare",
					NostrumSpellShapes.Projectile,
					EMagicElement.FIRE,
					3,
					EAlteration.RUIN);
			putSpell("HeatUp",
					EMagicElement.FIRE,
					3,
					EAlteration.SUPPORT);
			putSpell("Summon Pets (Fire)",
					EMagicElement.NEUTRAL,
					3,
					EAlteration.SUMMON);

			// Ice
			putSpell("Magic Aegis",
					EMagicElement.ICE,
					1,
					EAlteration.RESIST);
			putSpell("Ice Shard",
					NostrumSpellShapes.Projectile,
					EMagicElement.ICE,
					2,
					EAlteration.RUIN);
			putSpell("Frostbite",
					NostrumSpellShapes.SeekingBullet,
					EMagicElement.ICE,
					1,
					EAlteration.INFLICT);
			putSpell("Heal",
					EMagicElement.ICE,
					2,
					EAlteration.GROWTH);
			putSpell("Hand Of Cold",
					NostrumSpellShapes.Cycler,
					EMagicElement.ICE,
					3,
					EAlteration.RUIN);
			
			// Earth
			putSpell("Rock Fling",
					NostrumSpellShapes.Projectile,
					EMagicElement.EARTH,
					1,
					EAlteration.RUIN);
			putSpell("Roots",
					NostrumSpellShapes.SeekingBullet,
					EMagicElement.EARTH,
					1,
					EAlteration.INFLICT);
			putSpell("Earth Aegis",
					EMagicElement.EARTH,
					2,
					EAlteration.RESIST);
			putSpell("Earthen Regen",
					EMagicElement.EARTH,
					2,
					EAlteration.GROWTH);
			putSpell("Earth Bash",
					NostrumSpellShapes.Projectile,
					EMagicElement.EARTH,
					2,
					EAlteration.RUIN);
			putSpell("Summon Pets (Earth)",
					EMagicElement.EARTH,
					1,
					EAlteration.SUMMON);
			
			// Wind
			putSpell("Wind Slash",
					NostrumSpellShapes.Projectile,
					EMagicElement.WIND,
					1,
					EAlteration.RUIN);
			putSpell("Poison",
					NostrumSpellShapes.SeekingBullet,
					EMagicElement.WIND,
					1,
					EAlteration.INFLICT);
			putSpell("Gust",
					EMagicElement.WIND,
					3,
					EAlteration.RESIST);
			putSpell("Wind Wall",
					NostrumSpellShapes.Projectile,
					NostrumSpellShapes.Wall,
					EMagicElement.WIND,
					3,
					null);
			putSpell("Wind Ball I",
					NostrumSpellShapes.Projectile,
					EMagicElement.WIND,
					1,
					EAlteration.RUIN);
			putSpell("Wind Ball II",
					NostrumSpellShapes.Projectile,
					EMagicElement.WIND,
					2,
					EAlteration.RUIN);
			
			// Ender
			putSpell("Ender Beam",
					NostrumSpellShapes.Beam,
					EMagicElement.ENDER,
					1,
					EAlteration.RUIN);
			putSpell("Blindness",
					NostrumSpellShapes.Projectile,
					NostrumSpellShapes.Burst,
					EMagicElement.ENDER,
					1,
					EAlteration.INFLICT);
			spell = SpellUtils.MakeSpell("Blinker", 
					NostrumSpellShapes.OnDamage,
					EMagicElement.ENDER,
					2, EAlteration.GROWTH);
			defaultSpells.get(EMagicElement.ENDER).add(spell);
			putSpell("Random Teleport",
					NostrumSpellShapes.Projectile,
					EMagicElement.ENDER,
					2,
					null);
			putSpell("Invisibility",
					EMagicElement.ENDER,
					3,
					EAlteration.RESIST);
		}
	}
	
	public void setElement(EMagicElement element) {
		entityData.set(ELEMENT, element);
	}
	
	@Override
	@Nullable
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData livingdata, @Nullable CompoundTag dataTag) {
		final EMagicElement elem = EMagicElement.values()[NostrumMagica.rand.nextInt(EMagicElement.values().length)];
		setElement(elem);
		return super.finalizeSpawn(world, difficulty, reason, livingdata, dataTag);
	}
	
	public static boolean canSpawnExtraCheck(EntityType<WilloEntity> type, ServerLevelAccessor world, MobSpawnType reason, BlockPos pos, Random rand) {
		// Do extra checks in the nether, which has a smaller pool of spawns and so weight 1 is bigger than intended
		if (DimensionUtils.IsNether(world.getLevel())) {
			return world.getDifficulty() != Difficulty.PEACEFUL && rand.nextInt(35) == 0 && checkMobSpawnRules(type, world, reason, pos, rand);
		} else if (DimensionUtils.IsOverworld(world.getLevel())) {
			// Require out a certain distance in overworld
			return pos.distSqr(world.getLevel().getSharedSpawnPos()) > 500 * 500; 
		} else {
			return true;
		}
	}
}
