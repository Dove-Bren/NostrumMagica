package com.smanzana.nostrummagica.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Optional;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attributes.AttributeMagicResist;
import com.smanzana.nostrummagica.blocks.NostrumMagicaFlower;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.entity.tasks.EntityAIFlierDiveTask;
import com.smanzana.nostrummagica.entity.tasks.EntityAIOrbitEntityGeneric;
import com.smanzana.nostrummagica.entity.tasks.EntityAIStayHomeTask;
import com.smanzana.nostrummagica.entity.tasks.EntityAITemptGeneric;
import com.smanzana.nostrummagica.items.NostrumSkillItem;
import com.smanzana.nostrummagica.items.NostrumSkillItem.SkillItemType;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EntityLux extends EntityAnimal implements ILoreTagged, ITameableEntity {
	
	protected static final double LUX_HOME_DISTANCE_SQ = 144;
	protected static final double LUX_HOME_FORGET_DISTANCE_SQ = 400;
	protected static final DataParameter<Optional<BlockPos>> HOME  = EntityDataManager.<Optional<BlockPos>>createKey(EntityLux.class, DataSerializers.OPTIONAL_BLOCK_POS);
	protected static final DataParameter<Optional<UUID>> OWNER = EntityDataManager.<Optional<UUID>>createKey(EntityLux.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	protected static final DataParameter<ItemStack> POLLINATED_ITEM = EntityDataManager.<ItemStack>createKey(EntityLux.class, DataSerializers.ITEM_STACK);
	protected static final DataParameter<Integer> COMMUNITY_SCORE = EntityDataManager.<Integer>createKey(EntityLux.class, DataSerializers.VARINT);
	
	// For display
	protected static final DataParameter<Boolean> ROOSTING = EntityDataManager.<Boolean>createKey(EntityLux.class, DataSerializers.BOOLEAN);
	
	public static final String LoreKey = "nostrum__lux";
	
	private int idleCooldown;
	private long swingStartTicks; // client only
	
	public EntityLux(World worldIn) {
		super(worldIn);
		this.setSize(.5F, .5F);
		this.setNoGravity(true);
		this.noClip = true;
		this.moveHelper = new LuxMoveHelper(this);
		
		idleCooldown = NostrumMagica.rand.nextInt(20 * 30) + (20 * 10);
	}
	
	public EntityLux(World worldIn, BlockPos homePos) {
		this(worldIn);
		this.setHomePosAndDistance(homePos, (int) LUX_HOME_DISTANCE_SQ);
		this.setHome(homePos);
	}
	
	public EntityLux(World worldIn, LivingEntity owner) {
		this(worldIn);
		this.setOwner(owner);
	}
	
	protected void initEntityAI() {
		int priority = 1;
		this.tasks.addTask(priority++, new EntityAIFlierDiveTask<EntityLux>(this, 5.0, 20 * 3, 16, true));
		this.tasks.addTask(priority++, new EntityAIOrbitEntityGeneric<EntityLux>(this, null, 4, 20 * 10) {
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
		this.tasks.addTask(priority++, new EntityAIOrbitEntityGeneric<EntityLux>(this, null, 3, 20 * 10) {
			@Override
			public boolean shouldExecute() {
				LivingEntity owner = getOwner();
				if (owner == null) {
					return false;
				}
				
				return super.shouldExecute();
			}
			
			@Override
			protected LivingEntity getOrbitTarget() {
				return getOwner();
			}
		});
		
		// At night, sleep in trees
		this.tasks.addTask(priority++, new AIRoostTask(this, true));
		
		// If player nearby with a flower, be tempted!
		this.tasks.addTask(priority++, new EntityAITemptGeneric(this, 1.1D, ReagentItem.instance(), false) {
			@Override
			protected boolean isTempting(ItemStack stack) {
				ReagentType type = ReagentItem.findType(stack);
				return type == ReagentType.BLACK_PEARL || type == ReagentType.CRYSTABLOOM;
			}
			
			@Override
			protected void moveToTemptingPlayer(CreatureEntity tempted, PlayerEntity player) {
				if (this.temptedEntity.getDistanceSq(this.temptingPlayer) < 6.25D) {
					//this.temptedEntity.getMoveHelper(). no such thing as stop
				} else {
					this.temptedEntity.getMoveHelper().setMoveTo(player.posX, player.posY, player.posZ, 0.3D);
				}
			}
		});
		// TODO
		
		// If we go too far, go back home!
		this.tasks.addTask(priority++, new EntityAIStayHomeTask<EntityLux>(this, 1D, (LUX_HOME_DISTANCE_SQ * .8)));
		
		// Daily idle tasks. First, look for interesting flowers
		this.tasks.addTask(priority++, new AIFlyToRandomFeature(this, 20 * 10) {

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
		this.tasks.addTask(priority++, new AIRandomFly(this));
		
		priority = 1;
		this.targetTasks.addTask(priority++, new EntityAIHurtByTarget(this, true, new Class[] {EntityLux.class}));
	}
	
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.15D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(4.0D);
		this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(0.0D);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(30.0);
		this.getEntityAttribute(AttributeMagicResist.instance()).setBaseValue(0.0D);
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D);
	}

	protected void playStepSound(BlockPos pos, Block blockIn)
	{
		this.playSound(SoundEvents.BLOCK_GLASS_STEP, 0.15F, 1.0F);
	}

	protected SoundEvent getHurtSound()
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

	public float getEyeHeight()
	{
		return this.getHeight() * 0.5F;
	}

	public boolean attackEntityAsMob(Entity entityIn)
	{
		boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float)((int)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()));

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
	public void onUpdate() {
		super.onUpdate();
		
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
							posX, posY + height/2, posZ,
							0.05, 40, 10,
							new Vec3d(0, -.1, 0),
							null
							).color(.3f, .7f - darken, 1f - darken, .9f - darken));
				}
			} else {
				if (rand.nextBoolean() && rand.nextBoolean() && rand.nextBoolean() && rand.nextBoolean()) { // 1/16
					// darken if community  score is high
					final float darken = (getCommunityScore() >= 50 ? .2f : 0f);
					NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
							1,
							posX, posY + height/2, posZ,
							1, 15, 0,
							this.getEntityId()
							).color(.4f, .2f - darken, 1f - darken, .4f - darken));
				}
			}
		} else {
			// Check if we're far from home and forget it if so
			if (this.getHome() != null) {
				if (this.getDistanceSq(this.getHome()) > LUX_HOME_FORGET_DISTANCE_SQ) {
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
							posX - 32, posY - 32, posZ - 32, posX + 32, posY + 32, posZ + 32
							);
					final int count = world.getEntitiesWithinAABB(EntityLux.class, bb).size();
					this.incrCommunityScore(count);
					
					idleCooldown = rand.nextInt(20 * 30) + (20 * 10); 
				}
			}
		}
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
	protected void entityInit() {
		super.entityInit();
		
		this.dataManager.register(HOME, Optional.absent());
		this.dataManager.register(OWNER, Optional.absent());
		this.dataManager.register(POLLINATED_ITEM, ItemStack.EMPTY);
		this.dataManager.register(COMMUNITY_SCORE, 0);
		this.dataManager.register(ROOSTING, false);
	}
	
	protected void setHome(BlockPos home) {
		this.dataManager.set(HOME, Optional.fromNullable(home));
		this.setHomePosAndDistance(home == null ? BlockPos.ORIGIN : home, (int) LUX_HOME_DISTANCE_SQ);
	}
	
	public BlockPos getHome() {
		return this.dataManager.get(HOME).orNull();
	}
	
	@Override
	public void readEntityFromNBT(CompoundNBT compound) {
		super.readEntityFromNBT(compound);
		if (compound.contains("home", NBT.TAG_LONG)) {
			setHome(BlockPos.fromLong(compound.getLong("home")));
		} else {
			setHome(null);
		}
		
		if (compound.contains("owner", NBT.TAG_COMPOUND)) {
			setOwner(compound.getUniqueId("owner"));
		} else {
			setOwner((UUID)null);
		}
		
		if (compound.contains("pollinated_item", NBT.TAG_COMPOUND)) {
			setPollinatedItem(new ItemStack(compound.getCompound("pollinated_item")));
		} else {
			setPollinatedItem(ItemStack.EMPTY);
		}
		
		setCommunityScore(compound.getInt("community"));
		
		// Note: roosting is not persisted
	}
	
	@Override
	public void writeEntityToNBT(CompoundNBT compound) {
		super.writeEntityToNBT(compound);
		
		BlockPos homePos = this.getHome();
		if (homePos != null) {
			compound.putLong("home", homePos.toLong());
		}
		if (getOwnerId() != null) {
			compound.setUniqueId("owner", getOwnerId());
		}
		if (!getPollinatedItem().isEmpty()) {
			compound.put("pollinated_item", getPollinatedItem().serializeNBT());
		}
		compound.putInt("community", getCommunityScore());
	}
	
	@Override
	public boolean writeToNBTOptional(CompoundNBT compound) {
		return super.writeToNBTOptional(compound);
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
	
	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
		if (wasRecentlyHit && this.getHome() == null) {
			// Research scroll
			int chances = 1 + lootingModifier;
			if (rand.nextInt(300) < chances) {
				this.entityDropItem(NostrumSkillItem.getItem(SkillItemType.RESEARCH_SCROLL_SMALL, 1), 0);
			}
		}
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ENTITY;
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		this.playEffect(ParticleTypes.CRIT);
		return super.attackEntityFrom(source, amount);
	}
	
	private void playEffect(ParticleTypes ParticleTypes) {
		
		for (int i = 0; i < 15; ++i) {
			double d0 = this.rand.nextGaussian() * 0.02D;
			double d1 = this.rand.nextGaussian() * 0.02D;
			double d2 = this.rand.nextGaussian() * 0.02D;
			this.world.addParticle(ParticleTypes, this.posX + (double)(this.rand.nextFloat() * this.getWidth * 2.0F) - (double)this.getWidth, this.posY + 0.5D + (double)(this.rand.nextFloat() * this.getHeight()), this.posZ + (double)(this.rand.nextFloat() * this.getWidth * 2.0F) - (double)this.getWidth, d0, d1, d2, new int[0]);
		}
	}
	
	// Adapted from the wisp move helper
	static protected class LuxMoveHelper extends EntityMoveHelper {
		private final EntityLux parentEntity;
		private int courseChangeCooldown;

		public LuxMoveHelper(EntityLux wisp) {
			super(wisp);
			this.parentEntity = wisp;
		}

		public void onUpdateMoveHelper() {
			if (this.action == EntityMoveHelper.Action.MOVE_TO) {
				double d0 = this.posX - this.parentEntity.posX;
				double d1 = this.posY - this.parentEntity.posY;
				double d2 = this.posZ - this.parentEntity.posZ;
				double d3 = d0 * d0 + d1 * d1 + d2 * d2;

				d3 = (double)MathHelper.sqrt(d3);
				
				if (Math.abs(d3) < .01) {
					this.parentEntity.getMotion().x = 0;
					this.parentEntity.getMotion().y = 0;
					this.parentEntity.getMotion().z = 0;
					this.action = EntityMoveHelper.Action.WAIT;
					return;
				} else if (courseChangeCooldown-- <= 0) {
					courseChangeCooldown = this.parentEntity.getRNG().nextInt(5) + 10;
					float basespeed = (float) this.parentEntity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
					//speed *= 3f;
					this.parentEntity.getMotion().x = (d0 / d3) * basespeed * speed;
					this.parentEntity.getMotion().y = (d1 / d3) * basespeed  * speed;
					this.parentEntity.getMotion().z = (d2 / d3) * basespeed  * speed;
					
					float f9 = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
					this.entity.rotationYaw = this.limitAngle(this.entity.rotationYaw, f9, 90.0F);
				}
			}
		}
	}
	
	// Copied from EntityFlying class
	@Override
	public void travel(float strafe, float vertical, float forward) {
		if (this.isInWater()) {
			this.moveRelative(strafe, vertical, forward, 0.02F);
			this.move(MoverType.SELF, this.getMotion().x, this.getMotion().y, this.getMotion().z);
			this.getMotion().x *= 0.800000011920929D;
			this.getMotion().y *= 0.800000011920929D;
			this.getMotion().z *= 0.800000011920929D;
		} else if (this.isInLava()) {
			this.moveRelative(strafe, vertical, forward, 0.02F);
			this.move(MoverType.SELF, this.getMotion().x, this.getMotion().y, this.getMotion().z);
			this.getMotion().x *= 0.5D;
			this.getMotion().y *= 0.5D;
			this.getMotion().z *= 0.5D;
		} else {
			float f = 0.91F;

			if (this.onGround) {
				final BlockPos pos = new BlockPos(MathHelper.floor(this.posX), MathHelper.floor(this.getBoundingBox().minY) - 1, MathHelper.floor(this.posZ));
				final BlockState state = world.getBlockState(pos);
				f = state.getBlock().getSlipperiness(state, world, pos, this) * 0.91F;
			}

			float f1 = 0.16277136F / (f * f * f);
			this.moveRelative(strafe, vertical, forward, this.onGround ? 0.1F * f1 : 0.02F);
			f = 0.91F;

			if (this.onGround) {
				final BlockPos pos = new BlockPos(MathHelper.floor(this.posX), MathHelper.floor(this.getBoundingBox().minY) - 1, MathHelper.floor(this.posZ));
				final BlockState state = world.getBlockState(pos);
				f = state.getBlock().getSlipperiness(state, world, pos, this) * 0.91F;
			}

			this.move(MoverType.SELF, this.getMotion().x, this.getMotion().y, this.getMotion().z);
			this.getMotion().x *= (double)f;
			this.getMotion().y *= (double)f;
			this.getMotion().z *= (double)f;
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
	public boolean getCanSpawnHere() {
		BlockPos blockpos = new BlockPos(this.posX, this.getBoundingBox().minY, this.posZ);

		return this.world.getLightFor(EnumSkyBlock.SKY, blockpos) >= 8;
	}
	
	static class AIRandomFly extends Goal {
		private final EntityLux parentEntity;

		public AIRandomFly(EntityLux wisp) {
			this.parentEntity = wisp;
			this.setMutexBits(1);
		}

		/**
		 * Returns whether the Goal should begin execution.
		 */
		public boolean shouldExecute() {
			EntityMoveHelper entitymovehelper = this.parentEntity.getMoveHelper();

			if (!entitymovehelper.isUpdating()) {
				return true;
			} else {
				double d0 = entitymovehelper.getX() - this.parentEntity.posX;
				double d1 = entitymovehelper.getY() - this.parentEntity.posY;
				double d2 = entitymovehelper.getZ() - this.parentEntity.posZ;
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
				final double d1 = this.parentEntity.posY + (double)((random.nextFloat() * 2.0F - 1.0F) * radius);
				
				// check acceptable y first
				if (d1 <= 0 || d1 > parentEntity.world.getHeight()) {
					continue;
				}
				
				final double d0 = this.parentEntity.posX + (double)((random.nextFloat() * 2.0F - 1.0F) * radius);
				final double d2 = this.parentEntity.posZ + (double)((random.nextFloat() * 2.0F - 1.0F) * radius);
				
				// Check specific spot
				MutableBlockPos cursor = new MutableBlockPos();
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
				
				this.parentEntity.getMoveHelper().setMoveTo(d0, d1, d2, 0.3D);
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
			this.setMutexBits(1);
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
					if (parentEntity.world.getTotalWorldTime() - lastWakeTicks > 20 * 3) {
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
			if (lastAttemptTicks < 0 || parentEntity.world.getTotalWorldTime() - lastAttemptTicks > 5 * 20) {
				//EntityMoveHelper entitymovehelper = this.parentEntity.getMoveHelper();
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
				final double dist = parentEntity.getPositionVector().squareDistanceTo(roostPos.getX() + .5, roostPos.getY() - (parentEntity.getHeight()), roostPos.getZ() + .5);
				if (dist < .015) {
					if (dist > 0.0) {
						parentEntity.setPosition(roostPos.getX() + .5, roostPos.getY() - (parentEntity.getHeight()), roostPos.getZ() + .5);
					}
					
					parentEntity.getMotion().x = parentEntity.getMotion().y = parentEntity.getMotion().z = 0;
					parentEntity.startRoosting();
				} else if (!parentEntity.getMoveHelper().isUpdating()) {
					this.parentEntity.getMoveHelper().setMoveTo(
							roostPos.getX() + .5,
							roostPos.getY() - (parentEntity.getHeight()),
							roostPos.getZ() + .5,
							0.3D);
				}
				
				return true;
			}
			
			return false;
		}

		/**
		 * Execute a one shot task or start executing a continuous task
		 */
		public void startExecuting() {
			lastAttemptTicks = lastWakeTicks = parentEntity.world.getTotalWorldTime();
			
			// Try to find roost location
			roostPos = this.getRoostLocation(parentEntity);
			if (roostPos != null) {
				this.parentEntity.getMoveHelper().setMoveTo(
						roostPos.getX() + .5,
						roostPos.getY() - (parentEntity.getHeight()),
						roostPos.getZ() + .5,
						0.3D);
			}
		}
		
		@Override
		public void resetTask() {
			super.resetTask();
			roostPos = null;
			lastAttemptTicks = -1;
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
			this.setMutexBits(1);
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
			
			if (parentEntity.world.getTotalWorldTime() - lastAttemptTicks < 20 * 5) {
				// too soon
				return false;
			}
			
			EntityMoveHelper entitymovehelper = this.parentEntity.getMoveHelper();

			if (!entitymovehelper.isUpdating()) {
				return true;
			} else {
				double d0 = entitymovehelper.getX() - this.parentEntity.posX;
				double d1 = entitymovehelper.getY() - this.parentEntity.posY;
				double d2 = entitymovehelper.getZ() - this.parentEntity.posZ;
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
			lastAttemptTicks = parentEntity.world.getTotalWorldTime();
			
			targetPos = this.getNearbyFeature(parentEntity);
			if (targetPos != null) {
				parentEntity.moveHelper.setMoveTo(targetPos.getX() + .5, targetPos.getY() + .5, targetPos.getZ() + .5, .3);
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
		public void updateTask() {
			if (running && targetPos != null) {
				if (!parentEntity.moveHelper.isUpdating()) {
					parentEntity.moveHelper.setMoveTo(targetPos.getX() + .5, targetPos.getY() + .5, targetPos.getZ() + .5, .3);
				}
				
				if (parentEntity.getPositionVector().squareDistanceTo(
						targetPos.getX() + .5,
						targetPos.getY() + .5,
						targetPos.getZ() + .5) < .05) {
					this.onArrive(parentEntity, targetPos);
					// use 'lastAttemptTicks' to effectively make sure we dont' try again for 'delay' ticks
					lastAttemptTicks = parentEntity.world.getTotalWorldTime() + this.delay;
					running = false;
				}
			}
		}
	}
	
	@Override
	public UUID getOwnerId() {
		return this.dataManager.get(OWNER).orNull();
	}

	@Override
	public boolean isEntityTamed() {
		return getOwner() != null;
	}

	@Override
	public LivingEntity getOwner() {
		UUID ownerID = getOwnerId();
		LivingEntity owner = null;
		if (ownerID != null) {
			List<LivingEntity> ids = this.world.getEntities(LivingEntity.class, (ent) -> {
				return ent != null && ent.getUniqueID().equals(ownerID);
			});
			if (ids != null && ids.size() > 0) {
				owner = ids.get(0);
			}
		}
		return owner;
	}

	@Override
	public boolean isEntitySitting() {
		return false;
	}
	
	public void setOwner(@Nullable LivingEntity owner) {
		setOwner(owner == null ? null : owner.getUniqueID());
	}
	
	public void setOwner(@Nullable UUID ownerID) {
		this.dataManager.set(OWNER, Optional.fromNullable(ownerID));
	}
	
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
			swingStartTicks = world.getTotalWorldTime();
		}
		
		final long SWING_TICKS = 20 * 2;
		final long now = world.getTotalWorldTime();
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
		MutableBlockPos cursor = new MutableBlockPos();
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
						|| state.getBlock() instanceof BlockFlower));
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
		MutableBlockPos cursor = new MutableBlockPos();
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
				ItemStack reagentStack = new ItemStack(
						NostrumMagicaFlower.instance().getItemDropped(state, rand, 0),
						1,
						NostrumMagicaFlower.instance().damageDropped(state)
						);
				setPollinatedItem(reagentStack);
			}
			
			((ServerWorld) world).addParticle(ParticleTypes.VILLAGER_HAPPY,
					posX,
					posY + height / 2,
					posZ,
					5,
					.25,
					.25,
					.25,
					0,
					new int[0]);
		}
	}
	
	@Nullable
	protected static final BlockState resolvePlantable(ItemStack stack) {
		if (stack.isEmpty()) {
			return null;
		}
		
		if (stack.getItem() instanceof ReagentItem) {
			switch (ReagentItem.findType(stack)) {
			case BLACK_PEARL:
				return NostrumMagicaFlower.instance().getState(NostrumMagicaFlower.Type.MIDNIGHT_IRIS);
			case CRYSTABLOOM:
				return NostrumMagicaFlower.instance().getState(NostrumMagicaFlower.Type.CRYSTABLOOM);
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
		MutableBlockPos cursor = new MutableBlockPos();
		cursor.setPos(this.getPosition());
		while (cursor.getY() > 0) {
			BlockState state = world.getBlockState(cursor);
			if (
				state == null
				|| state.getBlock().isAir(state, world, cursor)
				|| !state.isOpaqueCube()
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
				&& flowerState.getBlock() instanceof BlockBush
				&& ((BlockBush) flowerState.getBlock()).canPlaceBlockAt(world, cursor.toImmutable())) {
			world.setBlockState(cursor.toImmutable(), flowerState);
			
			((ServerWorld) world).addParticle(ParticleTypes.VILLAGER_HAPPY,
					cursor.getX() + .5,
					cursor.getY() + .5,
					cursor.getZ() + .5,
					5,
					.25,
					.25,
					.25,
					0,
					new int[0]);
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
		if (this.getOwnerId() == null && this.getHome() == null) {
			this.setHome(this.getPosition());
		}
	}
	
	protected void stopRoosting() {
		this.setRoosting(false);
	}
	
	protected void doBreed() {
		world.addEntity(new EntityLux(world));
	}
	
	protected void attemptBreed() {
		final int score = getCommunityScore();
		if (score < 50 && (score <= 0 || rand.nextInt(score) == 0)) {
			this.setCommunityScore(50);
			doBreed();
		}
	}

	@Override
	public EntityAgeable createChild(EntityAgeable ageable) {
		return null;
	}
}
