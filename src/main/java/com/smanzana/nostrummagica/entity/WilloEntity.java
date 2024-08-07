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
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.entity.tasks.OrbitEntityGenericGoal;
import com.smanzana.nostrummagica.entity.tasks.PanicGenericGoal;
import com.smanzana.nostrummagica.entity.tasks.SpellAttackGoal;
import com.smanzana.nostrummagica.item.InfusedGemItem;
import com.smanzana.nostrummagica.item.NostrumItems;
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

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
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
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.util.Constants.NBT;

public class WilloEntity extends MonsterEntity implements ILoreSupplier, IElementalEntity {
	
	public static enum WilloStatus {
		NEUTRAL,
		PANIC,
		AGGRO,
	}
	
	public static final String ID = "entity_willo";
	
	protected static final double MAX_WISP_DISTANCE_SQ = 144;
	protected static final DataParameter<EMagicElement> ELEMENT = EntityDataManager.<EMagicElement>createKey(WilloEntity.class, MagicElementDataSerializer.instance);
	protected static final DataParameter<WilloStatus> STATUS = EntityDataManager.<WilloStatus>createKey(WilloEntity.class, WilloStatusSerializer.instance);
	
	private int idleCooldown;
	
	public WilloEntity(EntityType<? extends WilloEntity> type, World worldIn) {
		super(type, worldIn);
		this.setNoGravity(true);
		this.moveController = new WispMoveHelper(this);
		this.experienceValue = 20;
		
		idleCooldown = NostrumMagica.rand.nextInt(20 * 30) + (20 * 10);
	}
	
	protected void registerGoals() {
		int priority = 1;
		this.goalSelector.addGoal(priority++, new SpellAttackGoal<WilloEntity>(this, 20, 4, true, (willo) -> {
			return willo.getAttackTarget() != null && willo.getStatus() != WilloStatus.PANIC;
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
			return e.getAttackTarget() != null
					&& WilloEntity.this.getStatus() == WilloStatus.AGGRO;
		}, (e) -> {
			return e.getAttackTarget() != null
					&& WilloEntity.this.getStatus() == WilloStatus.AGGRO;
		}) {
			@Override
			protected LivingEntity getOrbitTarget() {
				return WilloEntity.this.getAttackTarget();
			}
		});
		this.goalSelector.addGoal(priority++, new AIRandomFly(this));
		this.goalSelector.addGoal(priority++, new LookAtGoal(this, PlayerEntity.class, 60f));
		this.goalSelector.addGoal(priority++, new LookRandomlyGoal(this));
		
		priority = 1;
		this.targetSelector.addGoal(priority++, new HurtByTargetGoal(this).setCallsForHelp(WilloEntity.class));
		this.targetSelector.addGoal(priority++, new NearestAttackableTargetGoal<PlayerEntity>(this, PlayerEntity.class, 10, true, false, null));
	}
	
	public static final AttributeModifierMap.MutableAttribute BuildAttributes() {
		return MonsterEntity.func_234295_eP_()
			.createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.2D)
			.createMutableAttribute(Attributes.MAX_HEALTH, 10.0D)
			.createMutableAttribute(Attributes.ARMOR, 4.0D)
			.createMutableAttribute(Attributes.FOLLOW_RANGE, 30.0)
			.createMutableAttribute(NostrumAttributes.magicResist, 0.0D);
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
			Vector3d offset = this.getVectorForRotation(0f, this.rotationYawHead).rotateYaw(rand.nextBoolean() ? 90f : -90f).scale(.5)
					.scale(rand.nextFloat() * 3 + 1f);
			NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
					1,
					getPosX() + offset.x,
					getPosY() + getHeight()/2f + offset.y,
					getPosZ() + offset.z,
					0, 40, 0,
					//offset.scale(rand.nextFloat() * .2f),
					new Vector3d(0, -.05, 0),
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
		public InfoScreenTabs getTab() {
			return InfoScreenTabs.INFO_ENTITY;
		}

		@Override
		public EntityType<WilloEntity> getEntityType() {
			return NostrumEntityTypes.willo;
		}
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
			this.world.addParticle(particle, this.getPosX() + (double)(this.rand.nextFloat() * this.getWidth() * 2.0F) - (double)this.getWidth(), this.getPosY() + 0.5D + (double)(this.rand.nextFloat() * this.getHeight()), this.getPosZ() + (double)(this.rand.nextFloat() * this.getWidth() * 2.0F) - (double)this.getWidth(), d0, d1, d2);
		}
	}
	
	@Override
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
		private final WilloEntity parentEntity;
		private int courseChangeCooldown;

		public WispMoveHelper(WilloEntity wisp) {
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
					this.parentEntity.setMotion(Vector3d.ZERO);
					this.action = MovementController.Action.WAIT;
					return;
				} else if (this.isNotColliding(this.getX(), this.getY(), this.getZ(), d3)) {
					float basespeed = (float) this.parentEntity.getAttribute(Attributes.MOVEMENT_SPEED).getValue();
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
		if (!super.canSpawn(world, spawnReason)) { // checks light level
			return false;
		}
		
		// Want to use dimension key but not available with IWorldReadyer
		RegistryKey<Biome> biomeKey = RegistryKey.getOrCreateKey(Registry.BIOME_KEY, world.getBiome(this.getPosition()).getRegistryName());
		
//		if (!DimensionUtils.IsOverworld(world) && !DimensionUtils.IsNether(world)) {
//			return false;
//		}
		if (BiomeDictionary.hasType(biomeKey, BiomeDictionary.Type.END)) {
			return false;
		}
		
		return true;
	}
	
	static class AIRandomFly extends Goal {
		private final WilloEntity parentEntity;
		private int delayTicks = 0;

		public AIRandomFly(WilloEntity wisp) {
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
			final Vector3d center = (parentEntity.getAttackTarget() == null ? parentEntity.getPositionVec() : parentEntity.getAttackTarget().getPositionVec());
			final float range = (parentEntity.getAttackTarget() == null ? 16f : 8f);
			double d0 = center.x + (double)((random.nextFloat() * 2.0F - 1.0F) * range);
			double d1 = center.y + (double)((random.nextFloat() * 2.0F - 1.0F) * range);
			double d2 = center.z + (double)((random.nextFloat() * 2.0F - 1.0F) * range);
			
			// Adjust to above ground
			double height = random.nextInt(4) + 2;
			BlockPos.Mutable cursor = new BlockPos.Mutable();
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
			
			// Physical
			putSpell("Physic Blast",
					NostrumSpellShapes.Projectile,
					EMagicElement.PHYSICAL,
					2,
					null);
			putSpell("Shield",
					NostrumSpellShapes.Burst,
					EMagicElement.PHYSICAL,
					1,
					EAlteration.RESIST);
			putSpell("Shield",
					EMagicElement.PHYSICAL,
					2,
					EAlteration.SUPPORT);
			putSpell("Weaken",
					NostrumSpellShapes.Projectile,
					NostrumSpellShapes.Burst,
					EMagicElement.PHYSICAL,
					1,
					EAlteration.INFLICT);
			putSpell("Weaken II",
					NostrumSpellShapes.SeekingBullet,
					EMagicElement.PHYSICAL,
					2,
					EAlteration.INFLICT);
			putSpell("Summon Pets",
					EMagicElement.PHYSICAL,
					2,
					EAlteration.SUMMON);
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
					NostrumSpellShapes.Beam,
					EMagicElement.LIGHTNING,
					1,
					EAlteration.CONJURE);
			putSpell("Lightning Ball I",
					NostrumSpellShapes.Projectile,
					NostrumSpellShapes.Burst,
					EMagicElement.LIGHTNING,
					3,
					null);
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
					EAlteration.CONJURE);
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
					EMagicElement.PHYSICAL,
					3,
					EAlteration.SUMMON);

			// Ice
			putSpell("Magic Aegis",
					EMagicElement.ICE,
					1,
					EAlteration.SUPPORT);
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
			putSpell("Dispel",
					NostrumSpellShapes.SeekingBullet,
					EMagicElement.ICE,
					1,
					EAlteration.RESIST);
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
					EAlteration.SUPPORT);
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
					EAlteration.CONJURE);
			putSpell("Invisibility",
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
	public ILivingEntityData onInitialSpawn(IServerWorld world, DifficultyInstance difficulty, SpawnReason reason, @Nullable ILivingEntityData livingdata, @Nullable CompoundNBT dataTag) {
		final EMagicElement elem = EMagicElement.values()[NostrumMagica.rand.nextInt(EMagicElement.values().length)];
		setElement(elem);
		return super.onInitialSpawn(world, difficulty, reason, livingdata, dataTag);
	}
	
	public static boolean canSpawnExtraCheck(EntityType<WilloEntity> type, IServerWorld world, SpawnReason reason, BlockPos pos, Random rand) {
		// Do extra checks in the nether, which has a smaller pool of spawns and so weight 1 is bigger than intended
		
		if (DimensionUtils.IsNether(world.getWorld())) {
			return world.getDifficulty() != Difficulty.PEACEFUL && rand.nextInt(35) == 0 && canSpawnOn(type, world, reason, pos, rand);
		} else {
			return true;
		}
	}
}
