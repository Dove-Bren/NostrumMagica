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
import com.smanzana.nostrummagica.attributes.AttributeMagicResist;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.entity.tasks.EntityAIOrbitEntityGeneric;
import com.smanzana.nostrummagica.entity.tasks.EntityAIPanicGeneric;
import com.smanzana.nostrummagica.entity.tasks.EntitySpellAttackTask;
import com.smanzana.nostrummagica.items.InfusedGemItem;
import com.smanzana.nostrummagica.items.NostrumItems;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.serializers.MagicElementDataSerializer;
import com.smanzana.nostrummagica.serializers.WilloStatusSerializer;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.components.MagicDamageSource;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.spells.components.shapes.AoEShape;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.BeamTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.DamagedTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.MagicCutterTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.MagicCyclerTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.OtherTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ProjectileTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.SeekingBulletTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.SelfTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.WallTrigger;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;

public class EntityWillo extends MonsterEntity implements ILoreTagged {
	
	public static enum WilloStatus {
		NEUTRAL,
		PANIC,
		AGGRO,
	}
	
	public static final String ID = "entity_willo";
	
	protected static final double MAX_WISP_DISTANCE_SQ = 144;
	protected static final DataParameter<EMagicElement> ELEMENT = EntityDataManager.<EMagicElement>createKey(EntityWillo.class, MagicElementDataSerializer.instance);
	protected static final DataParameter<WilloStatus> STATUS = EntityDataManager.<WilloStatus>createKey(EntityWillo.class, WilloStatusSerializer.instance);
	
	public static final String LoreKey = "nostrum__willo";
	
	private int idleCooldown;
	
	public EntityWillo(EntityType<? extends EntityWillo> type, World worldIn) {
		super(type, worldIn);
		this.setNoGravity(true);
		this.moveController = new WispMoveHelper(this);
		this.experienceValue = 20;
		
		idleCooldown = NostrumMagica.rand.nextInt(20 * 30) + (20 * 10);
	}
	
	protected void registerGoals() {
		int priority = 1;
		this.goalSelector.addGoal(priority++, new EntitySpellAttackTask<EntityWillo>(this, 20, 4, true, (willo) -> {
			return willo.getAttackTarget() != null && willo.getStatus() != WilloStatus.PANIC;
		}, new Spell[0]){
			@Override
			public Spell pickSpell(Spell[] spells, EntityWillo wisp) {
				// Ignore empty array and use spell from the wisp
				return getSpellToUse();
			}
		});
		this.goalSelector.addGoal(priority++, new EntityAIPanicGeneric<EntityWillo>(this, 3.0, (e) -> {
			return EntityWillo.this.getStatus() == WilloStatus.PANIC;
		}));
		this.goalSelector.addGoal(priority++, new EntityAIOrbitEntityGeneric<EntityWillo>(this, null, 3.0, 6 * 20, 2.0, 3 * 20, 2, (e) -> {
			return e.getAttackTarget() != null
					&& EntityWillo.this.getStatus() == WilloStatus.AGGRO;
		}, (e) -> {
			return e.getAttackTarget() != null
					&& EntityWillo.this.getStatus() == WilloStatus.AGGRO;
		}) {
			@Override
			protected LivingEntity getOrbitTarget() {
				return EntityWillo.this.getAttackTarget();
			}
		});
		this.goalSelector.addGoal(priority++, new AIRandomFly(this));
		this.goalSelector.addGoal(priority++, new LookAtGoal(this, PlayerEntity.class, 60f));
		this.goalSelector.addGoal(priority++, new LookRandomlyGoal(this));
		
		priority = 1;
		this.targetSelector.addGoal(priority++, new HurtByTargetGoal(this).setCallsForHelp(EntityWillo.class));
		this.targetSelector.addGoal(priority++, new NearestAttackableTargetGoal<PlayerEntity>(this, PlayerEntity.class, 10, true, false, null));
	}
	
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
		this.getAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(4.0D);
		this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(30.0);
		this.getAttribute(AttributeMagicResist.instance()).setBaseValue(0.0D);
	}

	protected void playStepSound(BlockPos pos, BlockState blockIn)
	{
		this.playSound(SoundEvents.BLOCK_GLASS_STEP, 0.15F, 1.0F);
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

	protected float getStandingEyeHeight(Pose pose, EntitySize size)
	{
		return this.getHeight() * 0.5F;
	}

	public boolean attackEntityAsMob(Entity entityIn)
	{
		boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float)((int)this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getValue()));

		if (flag)
		{
			this.applyEnchantments(this, entityIn);
		}

		return flag;
	}

	public boolean processInteract(PlayerEntity player, Hand hand, @Nonnull ItemStack stack)
	{
		return false;
	}

	public boolean canBeLeashedTo(PlayerEntity player)
	{
		return false;
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (idleCooldown > 0) {
			idleCooldown--;
			if (idleCooldown == 0) {
				if (this.getAttackTarget() == null) {
					NostrumMagicaSounds.LUX_IDLE.play(this);
				}
				idleCooldown = NostrumMagica.rand.nextInt(20 * 30) + (20 * 10); 
			}
		}
		
		if (this.getAttackTarget() != null) {
			this.faceEntity(this.getAttackTarget(), 360f, 180f);
		} else {
			if (this.ticksExisted % 20 == 0 && this.getStatus() != WilloStatus.NEUTRAL) {
				this.setStatus(WilloStatus.NEUTRAL);
			}
		}
		
		if (this.getStatus() == WilloStatus.PANIC && this.getRevengeTarget() != null) {
			// Up the time so we panic forever unless target is gone or we can't see them
			if (this.getRevengeTarget().isAlive() && this.getEntitySenses().canSee(this.getRevengeTarget())) {
				this.setRevengeTarget(this.getRevengeTarget()); // Refreshes timer to 100 ticks
			}
		}
		
		if (world.isRemote) {
//			EMagicElement element = this.getElement();
//			int color = element.getColor();
//			Vec3d offset = this.getVectorForRotation(0f, this.rotationYawHead).rotateYaw(rand.nextBoolean() ? 90f : -90f).scale(.5);
//			final double yOffset =  Math.sin(2 * Math.PI * ((double) ticksExisted % 20.0) / 20.0) * (height/2);
//			NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
//					1,
//					posX + offset.x,
//					posY + height/2f + offset.y + yOffset,
//					posZ + offset.z,
//					0, 40, 0,
//					offset.scale(rand.nextFloat() * .2f),
//					false
//					).color(color));
			
			EMagicElement element = this.getElement();
			int color = element.getColor();
			Vec3d offset = this.getVectorForRotation(0f, this.rotationYawHead).rotateYaw(rand.nextBoolean() ? 90f : -90f).scale(.5)
					.scale(rand.nextFloat() * 3 + 1f);
			NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
					1,
					posX + offset.x,
					posY + getHeight()/2f + offset.y,
					posZ + offset.z,
					0, 40, 0,
					//offset.scale(rand.nextFloat() * .2f),
					new Vec3d(0, -.05, 0),
					null
					).color(color));
		}
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
	protected void registerData() {
		super.registerData();
		
		this.dataManager.register(STATUS, WilloStatus.NEUTRAL);
		this.dataManager.register(ELEMENT, EMagicElement.PHYSICAL);
	}
	
	protected void setStatus(WilloStatus status) {
		if (world.isRemote) {
			return;
		}
		
		this.dataManager.set(STATUS, status);
	}
	
	public WilloStatus getStatus() {
		return this.dataManager.get(STATUS);
	}
	
	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		if (compound.contains("element", NBT.TAG_STRING)) {
			try {
				this.dataManager.set(ELEMENT, EMagicElement.valueOf(compound.getString("element").toUpperCase()));
			} catch (Exception e) {
				this.dataManager.set(ELEMENT, EMagicElement.ICE);
			}
		}
//		if (compound.contains("status", NBT.TAG_STRING)) {
//			try {
//				this.dataManager.set(STATUS, WilloStatus.valueOf(compound.getString("status").toUpperCase()));
//			} catch (Exception e) {
//				this.dataManager.set(STATUS, WilloStatus.NEUTRAL);
//			}
//		}
	}
	
	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		
		compound.putString("element", this.getElement().name());
		//compound.putString("status", this.getStatus().name());
	}
	
	@Override
	public void fall(float distance, float damageMulti) {
		; // No fall damage
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
	protected void dropSpecialItems(DamageSource source, int looting, boolean recentlyHitIn) {
		super.dropSpecialItems(source, looting, recentlyHitIn);
		
		if (recentlyHitIn) {
			// In panic mode, drop elemental gems.
			// In aggro mode, roll for mani gem.
			if (this.getStatus() == WilloStatus.PANIC) {
				final int count = 1 + (int) (Math.floor((float) looting / 2f));
				this.entityDropItem(InfusedGemItem.getGem(this.getElement(), count));
			} else if (this.getStatus() == WilloStatus.AGGRO) {
				final float fltCount = .25f + (looting * .375f); // .25, .625, 1, 1.375
				final int whole = (int) fltCount;
				final float frac = fltCount - whole;
				
				if (whole > 0) {
					this.entityDropItem(new ItemStack(NostrumItems.crystalSmall));
				}
				if (this.rand.nextFloat() < frac) {
					this.entityDropItem(new ItemStack(NostrumItems.crystalSmall));
				}
			}
		}
	}
	
//	@Override
//	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
//		if (wasRecentlyHit) {
////			int chance = 1 + (2 * lootingModifier);
////			if (this.rand.nextInt(100) < chance) {
////				this.entityDropItem(NostrumResourceItem.getItem(ResourceType.WISP_PEBBLE, 1), 0);
////			}
//			
//			// Research scroll
//			int chances = 1 + lootingModifier;
//			if (rand.nextInt(200) < chances) {
//				this.entityDropItem(NostrumSkillItem.getItem(SkillItemType.RESEARCH_SCROLL_SMALL, 1), 0);
//			}
//		}
//	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ENTITY;
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		// Aggro if physical or non-opposing element.
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
			return super.attackEntityFrom(source, amount);
		} else if (source == DamageSource.IN_WALL
				|| source == DamageSource.CRAMMING
				|| source == DamageSource.DROWN
				|| source == DamageSource.OUT_OF_WORLD
				) {
			return super.attackEntityFrom(source, amount);
		} else {
			NostrumMagicaSounds.CAST_FAIL.play(this);
			if (this.getStatus() == WilloStatus.NEUTRAL) {
				this.setStatus(WilloStatus.AGGRO);
			}
			
			return super.attackEntityFrom(source, 0f);
		}
	}
	
	private void playEffect(IParticleData particle) {
		
		for (int i = 0; i < 15; ++i) {
			double d0 = this.rand.nextGaussian() * 0.02D;
			double d1 = this.rand.nextGaussian() * 0.02D;
			double d2 = this.rand.nextGaussian() * 0.02D;
			this.world.addParticle(particle, this.posX + (double)(this.rand.nextFloat() * this.getWidth() * 2.0F) - (double)this.getWidth(), this.posY + 0.5D + (double)(this.rand.nextFloat() * this.getHeight()), this.posZ + (double)(this.rand.nextFloat() * this.getWidth() * 2.0F) - (double)this.getWidth(), d0, d1, d2);
		}
	}
	
	public EMagicElement getElement() {
		return this.dataManager.get(ELEMENT);
	}
	
	protected Spell getSpellToUse() {
		init();
		List<Spell> spells = defaultSpells.get(this.getElement());
		int idx = (this.getStatus() == WilloStatus.NEUTRAL
				? this.rand.nextInt(2)
				: this.rand.nextInt(spells.size()));
		return spells.get(idx);
	}
	
	// Adapted from the wisp move helper
	static protected class WispMoveHelper extends MovementController {
		private final EntityWillo parentEntity;
		private int courseChangeCooldown;

		public WispMoveHelper(EntityWillo wisp) {
			super(wisp);
			this.parentEntity = wisp;
		}

		@Override
		public void tick() {
			if (this.action == MovementController.Action.MOVE_TO) {
				double d0 = this.posX - this.parentEntity.posX;
				double d1 = this.posY - this.parentEntity.posY;
				double d2 = this.posZ - this.parentEntity.posZ;
				double d3 = d0 * d0 + d1 * d1 + d2 * d2;

				d3 = (double)MathHelper.sqrt(d3);
				
//				if (Math.abs(d3) < .5) {
//					this.parentEntity.getMotion().x = 0;
//					this.parentEntity.getMotion().y = 0;
//					this.parentEntity.getMotion().z = 0;
//					this.action = MovementController.Action.WAIT;
//					return;
//				} else if (courseChangeCooldown-- <= 0) {
//					courseChangeCooldown = this.parentEntity.getRNG().nextInt(5) + 10;
//					
//					if (this.isNotColliding(this.posX, this.posY, this.posZ, d3)) {
//						float basespeed = (float) this.parentEntity.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue();
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
					this.parentEntity.setMotion(Vec3d.ZERO);
					this.action = MovementController.Action.WAIT;
					return;
				} else if (this.isNotColliding(this.posX, this.posY, this.posZ, d3)) {
					float basespeed = (float) this.parentEntity.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue();
					//speed *= 3f;
					this.parentEntity.setMotion(
							(d0 / d3) * basespeed * speed,
							(d1 / d3) * basespeed  * speed,
							(d2 / d3) * basespeed  * speed
							);
					
					float f9 = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
					this.mob.rotationYaw = this.limitAngle(this.mob.rotationYaw, f9, 90.0F);
				} else if (courseChangeCooldown-- <= 0) {
					courseChangeCooldown = this.parentEntity.getRNG().nextInt(5) + 10;
					this.action = MovementController.Action.WAIT;
				}
			}
		}

		/**
		 * Checks if entity bounding box is not colliding with terrain
		 */
		private boolean isNotColliding(double x, double y, double z, double p_179926_7_) {
			double d0 = (x - this.parentEntity.posX) / p_179926_7_;
			double d1 = (y - this.parentEntity.posY) / p_179926_7_;
			double d2 = (z - this.parentEntity.posZ) / p_179926_7_;
			AxisAlignedBB axisalignedbb = this.parentEntity.getBoundingBox();

			for (int i = 1; (double)i < p_179926_7_; ++i) {
				axisalignedbb = axisalignedbb.offset(d0, d1, d2);

				if (!this.parentEntity.world.isCollisionBoxesEmpty(this.parentEntity, axisalignedbb)) {
					return false;
				}
			}

			return true;
		}
	}
	
	// Copied from EntityFlying class
		@Override
		public void travel(Vec3d how) {
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
					//f = this.world.getBlockState(new BlockPos(MathHelper.floor(this.posX), MathHelper.floor(this.getBoundingBox().minY) - 1, MathHelper.floor(this.posZ))).getBlock().slipperiness * 0.91F;
					BlockPos underPos = new BlockPos(MathHelper.floor(this.posX), MathHelper.floor(this.getBoundingBox().minY) - 1, MathHelper.floor(this.posZ));
					BlockState underState = this.world.getBlockState(underPos);
					f = underState.getBlock().getSlipperiness(underState, this.world, underPos, this) * 0.91F;
				}

				float f1 = 0.16277136F / (f * f * f);
				this.moveRelative(this.onGround ? 0.1F * f1 : 0.02F, how);
				f = 0.91F;

				if (this.onGround) {
					//f = this.world.getBlockState(new BlockPos(MathHelper.floor(this.posX), MathHelper.floor(this.getBoundingBox().minY) - 1, MathHelper.floor(this.posZ))).getBlock().slipperiness * 0.91F;
					BlockPos underPos = new BlockPos(MathHelper.floor(this.posX), MathHelper.floor(this.getBoundingBox().minY) - 1, MathHelper.floor(this.posZ));
					BlockState underState = this.world.getBlockState(underPos);
					f = underState.getBlock().getSlipperiness(underState, this.world, underPos, this) * 0.91F;
				}

				this.move(MoverType.SELF, this.getMotion());
				this.setMotion(this.getMotion().scale(f));
			}

			this.prevLimbSwingAmount = this.limbSwingAmount;
			double d1 = this.posX - this.prevPosX;
			double d0 = this.posZ - this.prevPosZ;
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
		if (!super.canSpawn(world, spawnReason)) { // checks light level
			return false;
		}
		
		if (this.world.getDimension().getType() != DimensionType.OVERWORLD && this.world.getDimension().getType() != DimensionType.THE_NETHER) {
			return false;
		}
		
		return true;
	}
	
	static class AIRandomFly extends Goal {
		private final EntityWillo parentEntity;
		private int delayTicks = 0;

		public AIRandomFly(EntityWillo wisp) {
			this.parentEntity = wisp;
			this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		/**
		 * Returns whether the Goal should begin execution.
		 */
		public boolean shouldExecute() {
			
			if (delayTicks-- > 0) {
				return false;
			}
			
			MovementController MovementController = this.parentEntity.getMoveHelper();

			if (!MovementController.isUpdating()) {
				return true;
			} else {
				double d0 = MovementController.getX() - this.parentEntity.posX;
				double d1 = MovementController.getY() - this.parentEntity.posY;
				double d2 = MovementController.getZ() - this.parentEntity.posZ;
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
			final Vec3d center = (parentEntity.getAttackTarget() == null ? parentEntity.getPositionVector() : parentEntity.getAttackTarget().getPositionVector());
			final float range = (parentEntity.getAttackTarget() == null ? 16f : 8f);
			double d0 = center.x + (double)((random.nextFloat() * 2.0F - 1.0F) * range);
			double d1 = center.y + (double)((random.nextFloat() * 2.0F - 1.0F) * range);
			double d2 = center.z + (double)((random.nextFloat() * 2.0F - 1.0F) * range);
			
			// Adjust to above ground
			double height = random.nextInt(4) + 2;
			MutableBlockPos cursor = new MutableBlockPos();
			cursor.setPos(d0, d1, d2);
			
			while (cursor.getY() > 0 && parentEntity.world.isAirBlock(cursor)) {
				cursor.move(Direction.DOWN);
			}
			
			while (cursor.getY() < 255 && !parentEntity.world.isAirBlock(cursor)) {
				cursor.move(Direction.UP);
			}
			
			// Try and move `height` up
			for (int i = 0; i < height; i++) {
				if (parentEntity.world.isAirBlock(cursor.up())) {
					cursor.move(Direction.UP);
				} else {
					break;
				}
			}
			
			this.parentEntity.getMoveHelper().setMoveTo(
					cursor.getX() + (d0 % 1.0),
					cursor.getY() + (d1 % 1.0),
					cursor.getZ() + (d2 % 1.0),
					1D);
			
			delayTicks = parentEntity.getStatus() == WilloStatus.PANIC ? 5 : random.nextInt(20 * 10) + 20 * 4;
		}
	}
	
	private static Map<EMagicElement, List<Spell>> defaultSpells;
	
	private static void putSpell(String name,
			SpellTrigger trigger,
			SpellShape shape,
			EMagicElement element,
			int power,
			EAlteration alteration) {
		putSpell(name, trigger, null, shape, element, power, alteration);
	}
	
	private static void putSpell(String name,
			SpellTrigger trigger1,
			SpellTrigger trigger2,
			SpellShape shape,
			EMagicElement element,
			int power,
			EAlteration alteration) {
		Spell spell = new Spell(name, true);
		spell.addPart(new SpellPart(trigger1));
		if (trigger2 != null) {
			spell.addPart(new SpellPart(trigger2));
		}
		spell.addPart(new SpellPart(shape, element, power, alteration));
		
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
			
			// Physical
			putSpell("Physic Blast",
					ProjectileTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.PHYSICAL,
					2,
					null);
			putSpell("Shield",
					SelfTrigger.instance(),
					AoEShape.instance(),
					EMagicElement.PHYSICAL,
					1,
					EAlteration.RESIST);
			putSpell("Shield",
					SelfTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.PHYSICAL,
					2,
					EAlteration.SUPPORT);
			putSpell("Weaken",
					ProjectileTrigger.instance(),
					AoEShape.instance(),
					EMagicElement.PHYSICAL,
					1,
					EAlteration.INFLICT);
			putSpell("Weaken II",
					SeekingBulletTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.PHYSICAL,
					2,
					EAlteration.INFLICT);
			putSpell("Summon Pets",
					SelfTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.PHYSICAL,
					2,
					EAlteration.SUMMON);
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
					SingleShape.instance(),
					EMagicElement.LIGHTNING,
					1,
					EAlteration.RUIN);
			putSpell("Shock",
					SeekingBulletTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.LIGHTNING,
					1,
					EAlteration.INFLICT);
			putSpell("Magic Shell",
					SelfTrigger.instance(),
					AoEShape.instance(),
					EMagicElement.LIGHTNING,
					1,
					EAlteration.RESIST);
			putSpell("Bolt",
					BeamTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.LIGHTNING,
					1,
					EAlteration.CONJURE);
			putSpell("Lightning Ball I",
					ProjectileTrigger.instance(),
					AoEShape.instance(),
					EMagicElement.LIGHTNING,
					3,
					null);
			putSpell("Lightning Ball II",
					ProjectileTrigger.instance(),
					AoEShape.instance(),
					EMagicElement.LIGHTNING,
					2,
					EAlteration.RUIN);
			
			// Fire
			putSpell("Burn",
					SeekingBulletTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.FIRE,
					2,
					EAlteration.CONJURE);
			putSpell("Overheat",
					SeekingBulletTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.FIRE,
					2,
					EAlteration.INFLICT);
			putSpell("Flare",
					ProjectileTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.FIRE,
					3,
					EAlteration.RUIN);
			putSpell("HeatUp",
					SelfTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.FIRE,
					3,
					EAlteration.SUPPORT);
			putSpell("Summon Pets (Fire)",
					SelfTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.PHYSICAL,
					3,
					EAlteration.SUMMON);

			// Ice
			putSpell("Magic Aegis",
					SelfTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.ICE,
					1,
					EAlteration.SUPPORT);
			putSpell("Ice Shard",
					ProjectileTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.ICE,
					2,
					EAlteration.RUIN);
			putSpell("Frostbite",
					SeekingBulletTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.ICE,
					1,
					EAlteration.INFLICT);
			putSpell("Heal",
					SelfTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.ICE,
					2,
					EAlteration.GROWTH);
			putSpell("Dispel",
					SeekingBulletTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.ICE,
					1,
					EAlteration.RESIST);
			putSpell("Hand Of Cold",
					MagicCyclerTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.ICE,
					3,
					EAlteration.RUIN);
			
			// Earth
			putSpell("Rock Fling",
					ProjectileTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.EARTH,
					1,
					EAlteration.RUIN);
			putSpell("Roots",
					SeekingBulletTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.EARTH,
					1,
					EAlteration.INFLICT);
			putSpell("Earth Aegis",
					SelfTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.EARTH,
					2,
					EAlteration.SUPPORT);
			putSpell("Earthen Regen",
					SelfTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.EARTH,
					2,
					EAlteration.GROWTH);
			putSpell("Earth Bash",
					ProjectileTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.EARTH,
					2,
					EAlteration.RUIN);
			putSpell("Summon Pets (Earth)",
					SelfTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.EARTH,
					1,
					EAlteration.SUMMON);
			
			// Wind
			putSpell("Wind Slash",
					MagicCutterTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.WIND,
					1,
					EAlteration.RUIN);
			putSpell("Poison",
					SeekingBulletTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.WIND,
					1,
					EAlteration.INFLICT);
			putSpell("Gust",
					SelfTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.WIND,
					3,
					EAlteration.RESIST);
			putSpell("Wind Wall",
					ProjectileTrigger.instance(),
					WallTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.WIND,
					3,
					null);
			putSpell("Wind Ball I",
					ProjectileTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.WIND,
					1,
					EAlteration.RUIN);
			putSpell("Wind Ball II",
					ProjectileTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.WIND,
					2,
					EAlteration.RUIN);
			
			// Ender
			putSpell("Ender Beam",
					BeamTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.ENDER,
					1,
					EAlteration.RUIN);
			putSpell("Blindness",
					ProjectileTrigger.instance(),
					AoEShape.instance(),
					EMagicElement.ENDER,
					1,
					EAlteration.INFLICT);
			spell = new Spell("Blinker", true);
			spell.addPart(new SpellPart(SelfTrigger.instance()));
			spell.addPart(new SpellPart(DamagedTrigger.instance()));
			spell.addPart(new SpellPart(OtherTrigger.instance()));
			spell.addPart(new SpellPart(SingleShape.instance(), EMagicElement.ENDER,
					2, EAlteration.GROWTH));
			defaultSpells.get(EMagicElement.ENDER).add(spell);
			putSpell("Random Teleport",
					ProjectileTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.ENDER,
					2,
					EAlteration.CONJURE);
			putSpell("Invisibility",
					SelfTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.ENDER,
					3,
					EAlteration.RESIST);
		}
	}
	
	public void setElement(EMagicElement element) {
		dataManager.set(ELEMENT, element);
	}
	
	@Override
	@Nullable
	public ILivingEntityData onInitialSpawn(IWorld world, DifficultyInstance difficulty, SpawnReason reason, @Nullable ILivingEntityData livingdata, @Nullable CompoundNBT dataTag) {
		final EMagicElement elem = EMagicElement.values()[NostrumMagica.rand.nextInt(EMagicElement.values().length)];
		setElement(elem);
		return super.onInitialSpawn(world, difficulty, reason, livingdata, dataTag);
	}
	
	public static boolean canSpawnExtraCheck(EntityType<EntityWillo> type, IWorld world, SpawnReason reason, BlockPos pos, Random rand) {
		// Do extra checks in the nether, which has a smaller pool of spawns and so weight 1 is bigger than intended
		if (world.getDimension().getType() == DimensionType.THE_NETHER) {
			return world.getDifficulty() != Difficulty.PEACEFUL && rand.nextInt(35) == 0 && canSpawnOn(type, world, reason, pos, rand);
		} else {
			return true;
		}
	}
}
