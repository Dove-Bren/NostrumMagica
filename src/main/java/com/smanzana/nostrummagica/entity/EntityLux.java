package com.smanzana.nostrummagica.entity;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attributes.AttributeMagicResist;
import com.smanzana.nostrummagica.blocks.NostrumBlocks;
import com.smanzana.nostrummagica.blocks.NostrumMagicaFlower;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.entity.tasks.EntityAIFlierDiveTask;
import com.smanzana.nostrummagica.entity.tasks.EntityAIOrbitEntityGeneric;
import com.smanzana.nostrummagica.entity.tasks.EntityAIStayHomeTask;
import com.smanzana.nostrummagica.entity.tasks.GenericTemptGoal;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.loretag.ILoreSupplier;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.block.BlockState;
import net.minecraft.block.BushBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
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
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;

public class EntityLux extends AnimalEntity implements ILoreSupplier/*, ITameableEntity*/ {
	
	public static final String ID = "entity_lux";
	
	protected static final double LUX_HOME_DISTANCE_SQ = 144;
	protected static final double LUX_HOME_FORGET_DISTANCE_SQ = 400;
	protected static final DataParameter<Optional<BlockPos>> HOME  = EntityDataManager.<Optional<BlockPos>>createKey(EntityLux.class, DataSerializers.OPTIONAL_BLOCK_POS);
	//protected static final DataParameter<Optional<UUID>> OWNER = EntityDataManager.<Optional<UUID>>createKey(EntityLux.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	protected static final DataParameter<ItemStack> POLLINATED_ITEM = EntityDataManager.<ItemStack>createKey(EntityLux.class, DataSerializers.ITEMSTACK);
	protected static final DataParameter<Integer> COMMUNITY_SCORE = EntityDataManager.<Integer>createKey(EntityLux.class, DataSerializers.VARINT);
	
	// For display
	protected static final DataParameter<Boolean> ROOSTING = EntityDataManager.<Boolean>createKey(EntityLux.class, DataSerializers.BOOLEAN);
	
	public static final String LoreKey = "nostrum__lux";
	
	private int idleCooldown;
	private long swingStartTicks; // client only
	
	public EntityLux(EntityType<? extends EntityLux> type, World worldIn) {
		super(type, worldIn);
		this.setNoGravity(true);
		this.noClip = true;
		this.moveController = new LuxMoveHelper(this);
		
		idleCooldown = NostrumMagica.rand.nextInt(20 * 30) + (20 * 10);
	}
	
	public EntityLux(EntityType<? extends EntityLux> type, World worldIn, BlockPos homePos) {
		this(type, worldIn);
		this.setHomePosAndDistance(homePos, (int) LUX_HOME_DISTANCE_SQ);
		this.setHome(homePos);
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
	
//	public EntityLux(EntityType<? extends EntityLux> type, World worldIn, LivingEntity owner) {
//		this(type, worldIn);
//		this.setOwner(owner);
//	}
	
	protected void registerGoals() {
		int priority = 1;
		this.goalSelector.addGoal(priority++, new EntityAIFlierDiveTask<EntityLux>(this, 5.0, 20 * 3, 16, true));
		this.goalSelector.addGoal(priority++, new EntityAIOrbitEntityGeneric<EntityLux>(this, null, 4, 20 * 10) {
			@Override
			public boolean shouldExecute() {
				if (this.ent == null || this.ent.getAttackTarget() == null) {
					return false;
				}
				
				return super.shouldExecute();
			}
			
			@Override
			protected LivingEntity getOrbitTarget() {
				return this.ent.getAttackTarget();
			}
		});
//		this.goalSelector.addGoal(priority++, new EntityAIOrbitEntityGeneric<EntityLux>(this, null, 3, 20 * 10) {
//			@Override
//			public boolean shouldExecute() {
//				LivingEntity owner = getOwner();
//				if (owner == null) {
//					return false;
//				}
//				
//				return super.shouldExecute();
//			}
//			
//			@Override
//			protected LivingEntity getOrbitTarget() {
//				return getOwner();
//			}
//		});
		
		// At night, sleep in trees
		this.goalSelector.addGoal(priority++, new AIRoostTask(this, true));
		
		// If player nearby with a flower, be tempted!
		this.goalSelector.addGoal(priority++, new GenericTemptGoal(this, 1.1D, false, Ingredient.EMPTY) {
			@Override
			protected boolean isTempting(ItemStack stack) {
				return !stack.isEmpty()
						&& (NostrumTags.Items.ReagentCrystabloom.contains(stack.getItem()) || NostrumTags.Items.ReagentBlackPearl.contains(stack.getItem()));
			}
			
			@Override
			public void moveToclosestPlayer(CreatureEntity tempted, PlayerEntity player) {
				if (tempted.getDistanceSq(player) < 6.25D) {
					//this.temptedEntity.getMoveHelper(). no such thing as stop
				} else {
					tempted.getMoveHelper().setMoveTo(player.getPosX(), player.getPosY(), player.getPosZ(), 1D);
				}
			}
		});
		// TODO
		
		// If we go too far, go back home!
		this.goalSelector.addGoal(priority++, new EntityAIStayHomeTask<EntityLux>(this, 1D, (LUX_HOME_DISTANCE_SQ * .8)));
		
		// Daily idle tasks. First, look for interesting flowers
		this.goalSelector.addGoal(priority++, new AIFlyToRandomFeature(this, 20 * 10) {

			@Override
			public boolean shouldExecute() {
				// Don't even try if we're already full
				if (!getPollinatedItem().isEmpty()) {
					return false;
				}
				
				return super.shouldExecute();
			}
			
			@Override
			protected BlockPos getNearbyFeature(EntityLux lux) {
				return findNearbyFlowers();
			}

			@Override
			protected void onArrive(EntityLux lux, BlockPos pos) {
				BlockState state = lux.world.getBlockState(pos);
				lux.onFlowerVisit(pos, state);
			}
			
		});
		
		// else just fly randomly
		this.goalSelector.addGoal(priority++, new AIRandomFly(this));
		
		priority = 1;
		this.targetSelector.addGoal(priority++, new HurtByTargetGoal(this).setCallsForHelp(EntityLux.class));
	}
	
	public static final AttributeModifierMap.MutableAttribute BuildAttributes(){
		return AnimalEntity.func_233666_p_()
			.createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.2D)
			.createMutableAttribute(Attributes.MAX_HEALTH, 4.0D)
			.createMutableAttribute(Attributes.ARMOR, 0.0D)
			.createMutableAttribute(Attributes.FOLLOW_RANGE, 30.0)
			.createMutableAttribute(AttributeMagicResist.instance(), 0.0D)
			.createMutableAttribute(Attributes.ATTACK_DAMAGE, 1.0D);
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
		
		if (world.isRemote) {
			ItemStack stack = this.getPollinatedItem();
			if (stack.isEmpty()) {
				// 'drip' particles every once in a while
				if (rand.nextBoolean() && rand.nextBoolean() && rand.nextBoolean() && rand.nextBoolean()
						&& rand.nextBoolean() && rand.nextBoolean()) { // 1/64
					// darken if community  score is high
					final float darken = (getCommunityScore() >= 50 ? .2f : 0f);
					NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
							1,
							getPosX(), getPosY() + getHeight()/2, getPosZ(),
							0.05, 40, 10,
							new Vector3d(0, -.1, 0),
							null
							).color(.3f, .7f - darken, 1f - darken, .9f - darken));
				}
			} else {
				if (rand.nextBoolean() && rand.nextBoolean() && rand.nextBoolean() && rand.nextBoolean()) { // 1/16
					// darken if community  score is high
					final float darken = (getCommunityScore() >= 50 ? .2f : 0f);
					NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
							1,
							getPosX(), getPosY() + getHeight()/2, getPosZ(),
							1, 15, 0,
							this.getEntityId()
							).color(.4f, .2f - darken, 1f - darken, .4f - darken));
				}
			}
		} else {
			// Check if we're far from home and forget it if so
			if (this.getHome() != null) {
				
				if (this.getHome().distanceSq(this.getPosition()) > LUX_HOME_FORGET_DISTANCE_SQ) {
					this.setHome(null);
				}
			}
			
			if (idleCooldown > 0) {
				idleCooldown--;
				if (idleCooldown == 0) {
					if (this.getAttackTarget() == null) {
						NostrumMagicaSounds.LUX_IDLE.play(this);
					}
					
					// If pollinated, drop item occasionally
					ItemStack stack = this.getPollinatedItem();
					if (!stack.isEmpty() && rand.nextInt(10) == 0) {
						this.onPollinationComplete(stack);
						this.setPollinatedItem(ItemStack.EMPTY);
					}
					
					// Poll for nearby lux and update community score
					final AxisAlignedBB bb = new AxisAlignedBB(
							getPosX() - 32, getPosY() - 32, getPosZ() - 32, getPosX() + 32, getPosY() + 32, getPosZ() + 32
							);
					final int count = world.getEntitiesWithinAABB(EntityLux.class, bb).size();
					this.incrCommunityScore(count);
					
					idleCooldown = rand.nextInt(20 * 30) + (20 * 10); 
				}
			}
		}
	}
	
	@Override
	public ILoreTagged getLoreTag() {
		return LuxLoreTag.instance;
	}
	
	public static final class LuxLoreTag implements ILoreTagged {
		
		private static final LuxLoreTag instance = new LuxLoreTag(); 
		public static final LuxLoreTag instance() {
			return instance;
		}
	
		@Override
		public String getLoreKey() {
			return LoreKey;
		}
	
		@Override
		public String getLoreDisplayName() {
			return "Lux";
		}
		
		@Override
		public Lore getBasicLore() {
			return new Lore().add("Simple floating chunks of magical energy.", "They seem docile, love flowers, and only attack once attacked themselves.");
					
		}
		
		@Override
		public Lore getDeepLore() {
			return new Lore().add("Simple floating chunks of magical energy.", "They seem docile, and only attack once attacked themselves.", "The magical dust that drops off them might be collectable...");
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
		//this.dataManager.register(OWNER, Optional.empty());
		this.dataManager.register(POLLINATED_ITEM, ItemStack.EMPTY);
		this.dataManager.register(COMMUNITY_SCORE, 0);
		this.dataManager.register(ROOSTING, false);
	}
	
	protected void setHome(BlockPos home) {
		this.dataManager.set(HOME, Optional.ofNullable(home));
		this.setHomePosAndDistance(home, (int) LUX_HOME_DISTANCE_SQ);
	}
	
	public BlockPos getHome() {
		return this.dataManager.get(HOME).orElse(null);
	}
	
	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		if (compound.contains("home", NBT.TAG_LONG)) {
			setHome(BlockPos.fromLong(compound.getLong("home"))); // Warning: can break if save used across game versions
		} else {
			setHome(null);
		}
		
//		if (compound.contains("owner", NBT.TAG_COMPOUND)) {
//			setOwner(compound.getUniqueId("owner"));
//		} else {
//			setOwner((UUID)null);
//		}
		
		if (compound.contains("pollinated_item", NBT.TAG_COMPOUND)) {
			setPollinatedItem(ItemStack.read(compound.getCompound("pollinated_item")));
		} else {
			setPollinatedItem(ItemStack.EMPTY);
		}
		
		setCommunityScore(compound.getInt("community"));
		
		// Note: roosting is not persisted
	}
	
	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		
		BlockPos homePos = this.getHome();
		if (homePos != null) {
			compound.putLong("home", homePos.toLong());
		}
//		if (getOwnerId() != null) {
//			compound.putUniqueId("owner", getOwnerId());
//		}
		if (!getPollinatedItem().isEmpty()) {
			compound.put("pollinated_item", getPollinatedItem().serializeNBT());
		}
		compound.putInt("community", getCommunityScore());
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
	
//	@Override
//	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
//		if (wasRecentlyHit && this.getHome() == null) {
//			// Research scroll
//			int chances = 1 + lootingModifier;
//			if (rand.nextInt(300) < chances) {
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
	
	// Adapted from the wisp move helper
	static protected class LuxMoveHelper extends MovementController {
		private final EntityLux parentEntity;
		private int courseChangeCooldown;

		public LuxMoveHelper(EntityLux wisp) {
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
				
				if (Math.abs(d3) < .1) {
					this.parentEntity.setMotion(Vector3d.ZERO);
					this.action = MovementController.Action.WAIT;
					return;
				} else if (courseChangeCooldown-- <= 0) {
					float basespeed = (float) this.parentEntity.getAttribute(Attributes.MOVEMENT_SPEED).getValue();
					final double moveSpeed = (basespeed * this.speed * .3f);
					courseChangeCooldown = this.parentEntity.getRNG().nextInt(5) + 10;
					//speed *= 3f;
					this.parentEntity.setMotion(
							(d0 / d3) * moveSpeed,
							(d1 / d3) * moveSpeed,
							(d2 / d3) * moveSpeed
							);
					
					float f9 = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
					this.mob.rotationYaw = this.limitAngle(this.mob.rotationYaw, f9, 90.0F);
				}
			}
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
				this.moveRelative(this.onGround ? 0.1F * f1 : 0.03F, how);
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
		return super.canSpawn(world, spawnReason);
	}
	
	static class AIRandomFly extends Goal {
		private final EntityLux parentEntity;
		private int cooldownTicks;

		public AIRandomFly(EntityLux wisp) {
			this.parentEntity = wisp;
			this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		/**
		 * Returns whether the Goal should begin execution.
		 */
		public boolean shouldExecute() {
			MovementController MovementController = this.parentEntity.getMoveHelper();

			if (cooldownTicks > 0) {
				if (!MovementController.isUpdating()) {
					cooldownTicks--;
				}
				return false;
			} else if (!MovementController.isUpdating()) {
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
			int attempts = 10;
			while (attempts-- > 0) { 
				final double radius = 8.0F;
				final double d1 = this.parentEntity.getPosY() + (double)((random.nextFloat() * 2.0F - 1.0F) * radius);
				
				// check acceptable y first
				if (d1 <= 0 || d1 > parentEntity.world.getHeight()) {
					continue;
				}
				
				final double d0 = this.parentEntity.getPosX() + (double)((random.nextFloat() * 2.0F - 1.0F) * radius);
				final double d2 = this.parentEntity.getPosZ() + (double)((random.nextFloat() * 2.0F - 1.0F) * radius);
				
				// Check specific spot
				BlockPos.Mutable cursor = new BlockPos.Mutable();
				cursor.setPos(d0, d1, d2);
				
				if (!parentEntity.world.isAirBlock(cursor)) {
					continue;
				}
				
				// Check how high above ground that is, and retry if too far up
				int yDiff = 1;
				cursor.move(Direction.DOWN);
				while (cursor.getY() > 0 && parentEntity.world.isAirBlock(cursor)) {
					cursor.move(Direction.DOWN);
					yDiff++;
				}
				
				if (yDiff > 10) {
					continue;
				}
				
				this.parentEntity.getMoveHelper().setMoveTo(d0, d1, d2, 1D);
				cooldownTicks = this.parentEntity.getRNG().nextInt(20 * 5) + 40;
				break;
			}
		}
	}
	
	static class AIRoostTask extends Goal {
		
		private final EntityLux parentEntity;
		private Predicate<EntityLux> predicate;
		private BlockPos roostPos;
		
		private long lastAttemptTicks = -1;
		private long lastWakeTicks = -1;
		
		public static final Predicate<EntityLux> ROOST_AT_NIGHT = (ent) -> {
			return ent.world != null && !ent.world.isDaytime();
		};

		public AIRoostTask(EntityLux lux) {
			this.parentEntity = lux;
			this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
		}
		
		public AIRoostTask(EntityLux lux, boolean atNight) {
			this(lux, ROOST_AT_NIGHT);
			
		}
		
		public AIRoostTask(EntityLux lux, Predicate<EntityLux> shouldRoost) {
			this(lux);
			this.predicate = shouldRoost;
		}
		
		protected boolean shouldRoost() {
			if (parentEntity.getAttackTarget() != null) {
				return false;
			}

			if (predicate != null) {
				// Only check predicate every couple of seconds for delayed waking
				if (lastWakeTicks <= 0) {
					// Trying to start
					return predicate.test(parentEntity);
				} else {
					// random wake jitter from already-running task
					if (parentEntity.world.getGameTime() - lastWakeTicks > 20 * 3) {
						if (parentEntity.rand.nextInt(4) == 0) {
							return predicate.test(parentEntity);
						} else {
							return true; // random snooze
						}
					} else {
						return true; // snooze
					}
				}
			}
			
			return false;
		}
		
		protected @Nullable BlockPos getRoostLocation(EntityLux lux) {
			return lux.findNearbyLeaves();
		}

		/**
		 * Returns whether the Goal should begin execution.
		 */
		public boolean shouldExecute() {
			if (lastAttemptTicks < 0 || parentEntity.world.getGameTime() - lastAttemptTicks > 5 * 20) {
				//MovementController MovementController = this.parentEntity.getMoveHelper();
				return shouldRoost();
			}
			return false;
		}

		/**
		 * Returns whether an in-progress Goal should continue executing
		 */
		@Override
		public boolean shouldContinueExecuting() {
			if (shouldRoost() && roostPos != null) {
				// If roost position is destroyed, bail out
				if (parentEntity.world.isAirBlock(roostPos) // fast simple check
						|| !parentEntity.isGoodLeavesBlock(parentEntity.world.getBlockState(roostPos), roostPos)) {
					return false;
				}
				
				// If already roosting, snap to right position and hold there with no jitter
				final double dist = parentEntity.getPositionVec().squareDistanceTo(roostPos.getX() + .5, roostPos.getY() - (parentEntity.getHeight()), roostPos.getZ() + .5);
				if (dist < .015) {
					if (dist > 0.0) {
						parentEntity.setPosition(roostPos.getX() + .5, roostPos.getY() - (parentEntity.getHeight()), roostPos.getZ() + .5);
					}
					
					parentEntity.setMotion(Vector3d.ZERO);
					parentEntity.startRoosting();
				} else if (!parentEntity.getMoveHelper().isUpdating()) {
					this.parentEntity.getMoveHelper().setMoveTo(
							roostPos.getX() + .5,
							roostPos.getY() - (parentEntity.getHeight()),
							roostPos.getZ() + .5,
							1D);
				}
				
				return true;
			}
			
			if (roostPos != null) {
				lastAttemptTicks = -1;
			}
			return false;
		}

		/**
		 * Execute a one shot task or start executing a continuous task
		 */
		public void startExecuting() {
			lastAttemptTicks = lastWakeTicks = parentEntity.world.getGameTime();
			
			// Try to find roost location
			roostPos = this.getRoostLocation(parentEntity);
			if (roostPos != null) {
				this.parentEntity.getMoveHelper().setMoveTo(
						roostPos.getX() + .5,
						roostPos.getY() - (parentEntity.getHeight()),
						roostPos.getZ() + .5,
						1D);
			}
		}
		
		@Override
		public void resetTask() {
			super.resetTask();
			roostPos = null;
//			lastAttemptTicks = -1; Only reset on success
			lastWakeTicks = -1;
			parentEntity.stopRoosting();
		}
	}
	
	static abstract class AIFlyToRandomFeature extends Goal {
		
		private final EntityLux parentEntity;
		private final long delay;
		
		protected boolean running;
		protected long lastAttemptTicks;
		protected BlockPos targetPos;

		public AIFlyToRandomFeature(EntityLux lux, long successDelay) {
			this.parentEntity = lux;
			this.delay = successDelay;
			this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
			lastAttemptTicks = -1;
		}
		
		/**
		 * Find a nearby interesting feature the entity is interested in.
		 * This is called on a retry timer instead of every frame, so it can be a little more expensive.
		 * @param lux
		 * @return
		 */
		protected abstract BlockPos getNearbyFeature(EntityLux lux);
		
		/**
		 * Called when the task is complete and the entity has arrived.
		 * @param lux
		 * @param pos
		 */
		protected abstract void onArrive(EntityLux lux, BlockPos pos);

		/**
		 * Returns whether the Goal should begin execution.
		 */
		public boolean shouldExecute() {
			
			if (parentEntity.world.getGameTime() - lastAttemptTicks < 20 * 5) {
				// too soon
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
			return running;
		}

		/**
		 * Execute a one shot task or start executing a continuous task
		 */
		public void startExecuting() {
			running = true;
			lastAttemptTicks = parentEntity.world.getGameTime();
			
			targetPos = this.getNearbyFeature(parentEntity);
			if (targetPos != null) {
				parentEntity.moveController.setMoveTo(targetPos.getX() + .5, targetPos.getY() + .5, targetPos.getZ() + .5, 1);
			} else {
				running = false;
			}
		}
		
		@Override
		public void resetTask() {
			super.resetTask();
			running = false;
			targetPos = null;
		}
		
		@Override
		public void tick() {
			if (running && targetPos != null) {
				if (!parentEntity.moveController.isUpdating()) {
					parentEntity.moveController.setMoveTo(targetPos.getX() + .5, targetPos.getY() + .5, targetPos.getZ() + .5, 1);
				}
				
				if (parentEntity.getPositionVec().squareDistanceTo(
						targetPos.getX() + .5,
						targetPos.getY() + .5,
						targetPos.getZ() + .5) < .05) {
					this.onArrive(parentEntity, targetPos);
					// use 'lastAttemptTicks' to effectively make sure we dont' try again for 'delay' ticks
					lastAttemptTicks = parentEntity.world.getGameTime() + this.delay;
					running = false;
				}
			}
		}
	}
	
//	@Override
//	public UUID getOwnerId() {
//		return this.dataManager.get(OWNER).orElse(null);
//	}
//
//	@Override
//	public boolean isEntityTamed() {
//		return getOwner() != null;
//	}
//
//	@Override
//	public LivingEntity getOwner() {
//		UUID ownerID = getOwnerId();
//		LivingEntity owner = null;
//		if (ownerID != null) {
//			List<LivingEntity> ids = this.world.getEntities(LivingEntity.class, (ent) -> {
//				return ent != null && ent.getUniqueID().equals(ownerID);
//			});
//			if (ids != null && ids.size() > 0) {
//				owner = ids.get(0);
//			}
//		}
//		return owner;
//	}
//
//	@Override
//	public boolean isEntitySitting() {
//		return false;
//	}
//	
//	public void setOwner(@Nullable LivingEntity owner) {
//		setOwner(owner == null ? null : owner.getUniqueID());
//	}
//	
//	public void setOwner(@Nullable UUID ownerID) {
//		this.dataManager.set(OWNER, Optional.ofNullable(ownerID));
//	}
	
	public @Nonnull ItemStack getPollinatedItem() {
		return dataManager.get(POLLINATED_ITEM);
	}
	
	public void setPollinatedItem(@Nonnull ItemStack stack) {
		dataManager.set(POLLINATED_ITEM, stack);
	}
	
	public int getCommunityScore() {
		return dataManager.get(COMMUNITY_SCORE);
	}
	
	public void setCommunityScore(int score) {
		this.dataManager.set(COMMUNITY_SCORE, score);
	}
	
	public void incrCommunityScore(int count) {
		if (count != 0) {
			setCommunityScore(getCommunityScore()+count);
		}
	}
	
	public boolean isRoosting() {
		return dataManager.get(ROOSTING);
	}
	
	protected void setRoosting(boolean roosting) {
		dataManager.set(ROOSTING, roosting);
	}
	
	/**
	 * Current swing progress from 0 to 1. [0-1)
	 * @return
	 */
	@Override
	public float getSwingProgress(float partialTicks) {
		if (this.swingStartTicks == 0) {
			swingStartTicks = world.getGameTime();
		}
		
		final long SWING_TICKS = 20 * 2;
		final long now = world.getGameTime();
		final long diff = (now - swingStartTicks) % SWING_TICKS;
		final double curTicks = diff + partialTicks;
		return (float) (curTicks / (double) SWING_TICKS);
	}
	
	protected boolean isLeavesBlock(BlockState state) {
		return state.getMaterial().equals(Material.LEAVES);
	}
	
	protected boolean isGoodLeavesBlock(BlockState state, BlockPos pos) {
		return isLeavesBlock(state)
				&& world.isAirBlock(pos.down());
	}
	
	/**
	 * Find some leaves nearby to sleep under.
	 * Function should apply some jitter so a bunch of Lux that finish attacking don't all go to the same place.
	 * This is not intended to be a super cheap per-tick function.
	 * @return
	 */
	protected @Nullable BlockPos findNearbyLeaves() {
		if (world == null) {
			return null;
		}
		
		// Check home space
		BlockPos homePos = this.getHome();
		if (homePos != null && this.isGoodLeavesBlock(world.getBlockState(homePos), homePos)) {
			return homePos;
		}
		
		// Do longer search nearby
		List<BlockPos> leaves = new ArrayList<>();
		BlockPos center = (homePos == null ? getPosition() : homePos);
		BlockPos.Mutable cursor = new BlockPos.Mutable();
		final int radius = 10;
		for (int x = -radius; x <= radius; x++)
		for (int z = -radius; z <= radius; z++)
		for (int y = -radius; y <= radius; y++) {
			cursor.setPos(center.getX() + x, center.getY() + y, center.getZ() + z);
			
			// Make sure y if suitable
			if (cursor.getY() <= 0 || cursor.getY() > world.getHeight()) {
				continue;
			}
			
			BlockState state = world.getBlockState(cursor);
			if (isGoodLeavesBlock(state, cursor)) {
				leaves.add(cursor.toImmutable());
			}
		}
		
		if (leaves.isEmpty()) {
			return null;
		}
		
		return leaves.get(rand.nextInt(leaves.size()));
	}
	
	protected boolean isFlowersBlock(BlockState state) {
		// We only care about nostrum flowers
		return (state != null
				&& (state.getBlock() instanceof NostrumMagicaFlower
						|| BlockTags.SMALL_FLOWERS.contains(state.getBlock())));
	}
	
	/**
	 * Find some flowers nearby to go and 'sniff'.
	 * Function should apply some jitter so a bunch of Lux that start in the same spot don't pick the same flowers.
	 * This is not intended to be a super cheap per-tick function.
	 * @return
	 */
	protected @Nullable BlockPos findNearbyFlowers() {
		if (world == null) {
			return null;
		}
		
		List<BlockPos> flowers = new ArrayList<>();
		final BlockPos homePos = this.getHome();
		final BlockPos center = (homePos == null ? getPosition() : homePos);
		BlockPos.Mutable cursor = new BlockPos.Mutable();
		final int radius = 10;
		for (int x = -radius; x <= radius; x++)
		for (int z = -radius; z <= radius; z++)
		for (int y = -radius; y <= radius; y++) {
			cursor.setPos(center.getX() + x, center.getY() + y, center.getZ() + z);
			
			// Make sure y if suitable
			if (cursor.getY() <= 0 || cursor.getY() > world.getHeight()) {
				continue;
			}
			
			BlockState state = world.getBlockState(cursor);
			if (isFlowersBlock(state)) {
				flowers.add(cursor.toImmutable());
			}
		}
		
		if (flowers.isEmpty()) {
			return null;
		}
		
		return flowers.get(rand.nextInt(flowers.size()));
	}
	
	protected void onFlowerVisit(BlockPos pos, BlockState state) {
		// Check what kind of flower, and possible become 'pollinated' (possibly)
		if (rand.nextBoolean() && rand.nextBoolean()) {
			if (state != null && state.getBlock() instanceof NostrumMagicaFlower) {
				Item item = ((NostrumMagicaFlower) state.getBlock()).getReagentItem();
				if (item != null) {
					ItemStack reagentStack = new ItemStack(item, 1);
					setPollinatedItem(reagentStack);
				}
			}
			
			((ServerWorld) world).spawnParticle(ParticleTypes.HAPPY_VILLAGER,
					getPosX(),
					getPosY() + getHeight() / 2,
					getPosZ(),
					5,
					.25,
					.25,
					.25,
					0);
		}
	}
	
	@Nullable
	protected static final BlockState resolvePlantable(ItemStack stack) {
		if (stack.isEmpty()) {
			return null;
		}
		
		if (stack.getItem() instanceof ReagentItem) {
			switch (ReagentItem.FindType(stack)) {
			case BLACK_PEARL:
				return NostrumBlocks.midnightIris.getDefaultState();
			case CRYSTABLOOM:
				return NostrumBlocks.crystabloom.getDefaultState();
			case GINSENG:
			case GRAVE_DUST:
			case MANDRAKE_ROOT:
			case MANI_DUST:
			case SKY_ASH:
			case SPIDER_SILK:
			default:
				return null;
			}
		}
		
		return null;
	}
	
	protected void onPollinationComplete(ItemStack stack) {
		// If over bare grass, plant flower. Otherwise, drop
		BlockPos.Mutable cursor = new BlockPos.Mutable();
		cursor.setPos(this.getPosition());
		while (cursor.getY() > 0) {
			BlockState state = world.getBlockState(cursor);
			if (
				state == null
				|| state.getBlock().isAir(state, world, cursor)
				|| !state.isOpaqueCube(world, cursor)
				|| state.getMaterial() == Material.LEAVES
				) {
				cursor.move(Direction.DOWN);
			} else {
				break;
			}
		}
		
		cursor.move(Direction.UP);
		
		final BlockState flowerState = resolvePlantable(stack);
		
		if (flowerState != null
				&& flowerState.getBlock() instanceof BushBlock
				&& ((BushBlock) flowerState.getBlock()).isValidPosition(flowerState, world, cursor.toImmutable())) {
			world.setBlockState(cursor.toImmutable(), flowerState);
			
			((ServerWorld) world).spawnParticle(ParticleTypes.HAPPY_VILLAGER,
					cursor.getX() + .5,
					cursor.getY() + .5,
					cursor.getZ() + .5,
					5,
					.25,
					.25,
					.25,
					0);
		} else {
			this.entityDropItem(stack, 0f);
		}
			
		attemptBreed();
	}
	
	protected void startRoosting() {
		this.setRoosting(true);
		
		// Reset community score as well
		this.setCommunityScore(0);
		
		// And set home if none have been set already and we don't have an owner
		if (/*this.getOwnerId() == null &&*/ this.getHome() == null) {
			this.setHome(this.getPosition());
		}
	}
	
	protected void stopRoosting() {
		this.setRoosting(false);
	}
	
	protected void doBreed() {
		world.addEntity(new EntityLux(NostrumEntityTypes.lux, world));
	}
	
	protected void attemptBreed() {
		final int score = getCommunityScore();
		if (score < 50 && (score <= 0 || rand.nextInt(score) == 0)) {
			this.setCommunityScore(50);
			doBreed();
		}
	}

	@Override
	public AgeableEntity /*createChild*/ func_241840_a(ServerWorld world, AgeableEntity ageable) {
		return null;
	}

}
