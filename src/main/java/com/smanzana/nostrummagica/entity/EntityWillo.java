package com.smanzana.nostrummagica.entity;

import java.util.ArrayList;
import java.util.EnumMap;
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
import com.smanzana.nostrummagica.items.NostrumSkillItem;
import com.smanzana.nostrummagica.items.NostrumSkillItem.SkillItemType;
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

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityWillo extends EntityMob implements ILoreTagged {
	
	public static enum WilloStatus {
		NEUTRAL,
		PANIC,
		AGGRO,
	}
	
	protected static final double MAX_WISP_DISTANCE_SQ = 144;
	protected static final DataParameter<EMagicElement> ELEMENT = EntityDataManager.<EMagicElement>createKey(EntityWillo.class, MagicElementDataSerializer.instance);
	protected static final DataParameter<WilloStatus> STATUS = EntityDataManager.<WilloStatus>createKey(EntityWillo.class, WilloStatusSerializer.instance);
	
	public static final String LoreKey = "nostrum__willo";
	
	private int idleCooldown;
	
	public EntityWillo(World worldIn) {
		super(worldIn);
		this.setSize(.75F, .75F);
		this.setNoGravity(true);
		this.moveHelper = new WispMoveHelper(this);
		
		idleCooldown = NostrumMagica.rand.nextInt(20 * 30) + (20 * 10);
	}
	
	protected void initEntityAI() {
		int priority = 1;
		this.tasks.addTask(priority++, new EntitySpellAttackTask<EntityWillo>(this, 20, 4, true, (willo) -> {
			return willo.getAttackTarget() != null;
		}, new Spell[0]){
			@Override
			public Spell pickSpell(Spell[] spells, EntityWillo wisp) {
				// Ignore empty array and use spell from the wisp
				return getSpellToUse();
			}
		});
		this.tasks.addTask(priority++, new EntityAIPanicGeneric<EntityWillo>(this, 3.0, (e) -> {
			return EntityWillo.this.getStatus() == WilloStatus.PANIC;
		}));
		this.tasks.addTask(priority++, new EntityAIOrbitEntityGeneric<EntityWillo>(this, null, 3.0, 6 * 20, 2.0, 3 * 20, 2, (e) -> {
			return e.getAttackTarget() != null
					&& EntityWillo.this.getStatus() == WilloStatus.AGGRO;
		}, (e) -> {
			return e.getAttackTarget() != null
					&& EntityWillo.this.getStatus() == WilloStatus.AGGRO;
		}) {
			@Override
			protected EntityLivingBase getOrbitTarget() {
				return EntityWillo.this.getAttackTarget();
			}
		});
		this.tasks.addTask(priority++, new AIRandomFly(this));
		this.tasks.addTask(priority++, new EntityAIWatchClosest(this, EntityPlayer.class, 60f));
		this.tasks.addTask(priority++, new EntityAILookIdle(this));
		
		priority = 1;
		this.targetTasks.addTask(priority++, new EntityAIHurtByTarget(this, true, new Class[] {EntityWillo.class}));
		this.targetTasks.addTask(priority++, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, 10, true, false, null));
	}
	
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
		this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(4.0D);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(30.0);
		this.getEntityAttribute(AttributeMagicResist.instance()).setBaseValue(0.0D);
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
		return this.height * 0.5F;
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

	public boolean processInteract(EntityPlayer player, EnumHand hand, @Nonnull ItemStack stack)
	{
		return false;
	}

	public boolean canBeLeashedTo(EntityPlayer player)
	{
		return false;
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		
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
					posY + height/2f + offset.y,
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
	protected void entityInit() {
		super.entityInit();
		
		this.dataManager.register(STATUS, WilloStatus.NEUTRAL);
		this.dataManager.register(ELEMENT, EMagicElement.PHYSICAL);
	}
	
	protected void setStatus(WilloStatus status) {
		if (world.isRemote) {
			return;
		}
		
		System.out.println("Willo status changed to " + status.name());
		this.dataManager.set(STATUS, status);
	}
	
	public WilloStatus getStatus() {
		return this.dataManager.get(STATUS);
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		if (compound.hasKey("element", NBT.TAG_STRING)) {
			try {
				this.dataManager.set(ELEMENT, EMagicElement.valueOf(compound.getString("element").toUpperCase()));
			} catch (Exception e) {
				this.dataManager.set(ELEMENT, EMagicElement.ICE);
			}
		}
//		if (compound.hasKey("status", NBT.TAG_STRING)) {
//			try {
//				this.dataManager.set(STATUS, WilloStatus.valueOf(compound.getString("status").toUpperCase()));
//			} catch (Exception e) {
//				this.dataManager.set(STATUS, WilloStatus.NEUTRAL);
//			}
//		}
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		
		compound.setString("element", this.getElement().name());
		//compound.setString("status", this.getStatus().name());
	}
	
	@Override
	public boolean writeToNBTOptional(NBTTagCompound compound) {
		return super.writeToNBTOptional(compound);
	}
	
	@Override
	public void fall(float distance, float damageMulti) {
		; // No fall damage
	}
	
	@Override
	protected void updateFallState(double y, boolean onGround, IBlockState stae, BlockPos pos) {
		
	}
	
	@SideOnly(Side.CLIENT)
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
		if (wasRecentlyHit) {
//			int chance = 1 + (2 * lootingModifier);
//			if (this.rand.nextInt(100) < chance) {
//				this.entityDropItem(NostrumResourceItem.getItem(ResourceType.WISP_PEBBLE, 1), 0);
//			}
			
			// Research scroll
			int chances = 1 + lootingModifier;
			if (rand.nextInt(200) < chances) {
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
			
			this.playEffect(EnumParticleTypes.CRIT_MAGIC);
			return super.attackEntityFrom(source, amount);
		} else {
			NostrumMagicaSounds.CAST_FAIL.play(this);
			if (this.getStatus() == WilloStatus.NEUTRAL) {
				this.setStatus(WilloStatus.AGGRO);
			}
			
			return super.attackEntityFrom(source, 0f);
		}
	}
	
	private void playEffect(EnumParticleTypes enumparticletypes) {
		
		for (int i = 0; i < 15; ++i) {
			double d0 = this.rand.nextGaussian() * 0.02D;
			double d1 = this.rand.nextGaussian() * 0.02D;
			double d2 = this.rand.nextGaussian() * 0.02D;
			this.world.spawnParticle(enumparticletypes, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + 0.5D + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, d0, d1, d2, new int[0]);
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
	static protected class WispMoveHelper extends EntityMoveHelper {
		private final EntityWillo parentEntity;
		private int courseChangeCooldown;

		public WispMoveHelper(EntityWillo wisp) {
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
				
//				if (Math.abs(d3) < .5) {
//					this.parentEntity.motionX = 0;
//					this.parentEntity.motionY = 0;
//					this.parentEntity.motionZ = 0;
//					this.action = EntityMoveHelper.Action.WAIT;
//					return;
//				} else if (courseChangeCooldown-- <= 0) {
//					courseChangeCooldown = this.parentEntity.getRNG().nextInt(5) + 10;
//					
//					if (this.isNotColliding(this.posX, this.posY, this.posZ, d3)) {
//						float basespeed = (float) this.parentEntity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
//						//speed *= 3f;
//						this.parentEntity.motionX = (d0 / d3) * basespeed * speed;
//						this.parentEntity.motionY = (d1 / d3) * basespeed  * speed;
//						this.parentEntity.motionZ = (d2 / d3) * basespeed  * speed;
//						
//						float f9 = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
//						this.entity.rotationYaw = this.limitAngle(this.entity.rotationYaw, f9, 90.0F);
//					} else {
//						this.action = EntityMoveHelper.Action.WAIT;
//					}
//				}
				
				if (Math.abs(d3) < .5) {
					this.parentEntity.motionX = 0;
					this.parentEntity.motionY = 0;
					this.parentEntity.motionZ = 0;
					this.action = EntityMoveHelper.Action.WAIT;
					return;
				} else if (this.isNotColliding(this.posX, this.posY, this.posZ, d3)) {
					float basespeed = (float) this.parentEntity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
					//speed *= 3f;
					this.parentEntity.motionX = (d0 / d3) * basespeed * speed;
					this.parentEntity.motionY = (d1 / d3) * basespeed  * speed;
					this.parentEntity.motionZ = (d2 / d3) * basespeed  * speed;
					
					float f9 = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
					this.entity.rotationYaw = this.limitAngle(this.entity.rotationYaw, f9, 90.0F);
				} else if (courseChangeCooldown-- <= 0) {
					courseChangeCooldown = this.parentEntity.getRNG().nextInt(5) + 10;
					this.action = EntityMoveHelper.Action.WAIT;
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
			AxisAlignedBB axisalignedbb = this.parentEntity.getEntityBoundingBox();

			for (int i = 1; (double)i < p_179926_7_; ++i) {
				axisalignedbb = axisalignedbb.offset(d0, d1, d2);

				if (!this.parentEntity.world.getCollisionBoxes(this.parentEntity, axisalignedbb).isEmpty()) {
					return false;
				}
			}

			return true;
		}
	}
	
	// Copied from EntityFlying class
	@Override
	public void travel(float strafe, float vertical, float forward) {
		if (this.isInWater()) {
			this.moveRelative(strafe, vertical, forward, 0.02F);
			this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
			this.motionX *= 0.800000011920929D;
			this.motionY *= 0.800000011920929D;
			this.motionZ *= 0.800000011920929D;
		} else if (this.isInLava()) {
			this.moveRelative(strafe, vertical, forward, 0.02F);
			this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
			this.motionX *= 0.5D;
			this.motionY *= 0.5D;
			this.motionZ *= 0.5D;
		} else {
			float f = 0.91F;

			if (this.onGround) {
				//f = this.world.getBlockState(new BlockPos(MathHelper.floor(this.posX), MathHelper.floor(this.getEntityBoundingBox().minY) - 1, MathHelper.floor(this.posZ))).getBlock().slipperiness * 0.91F;
				BlockPos underPos = new BlockPos(MathHelper.floor(this.posX), MathHelper.floor(this.getEntityBoundingBox().minY) - 1, MathHelper.floor(this.posZ));
				IBlockState underState = this.world.getBlockState(underPos);
				f = underState.getBlock().getSlipperiness(underState, this.world, underPos, this) * 0.91F;
			}

			float f1 = 0.16277136F / (f * f * f);
			this.moveRelative(strafe, vertical, forward, this.onGround ? 0.1F * f1 : 0.02F);
			f = 0.91F;

			if (this.onGround) {
				//f = this.world.getBlockState(new BlockPos(MathHelper.floor(this.posX), MathHelper.floor(this.getEntityBoundingBox().minY) - 1, MathHelper.floor(this.posZ))).getBlock().slipperiness * 0.91F;
				BlockPos underPos = new BlockPos(MathHelper.floor(this.posX), MathHelper.floor(this.getEntityBoundingBox().minY) - 1, MathHelper.floor(this.posZ));
				IBlockState underState = this.world.getBlockState(underPos);
				f = underState.getBlock().getSlipperiness(underState, this.world, underPos, this) * 0.91F;
			}

			this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
			this.motionX *= (double)f;
			this.motionY *= (double)f;
			this.motionZ *= (double)f;
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
		if (!super.getCanSpawnHere()) {
			return false;
		}
		
		BlockPos blockpos = new BlockPos(this.posX, this.getEntityBoundingBox().minY, this.posZ);

		if (this.world.getLightFor(EnumSkyBlock.SKY, blockpos) > this.rand.nextInt(32)) {
			return false;
		} else {
			int i = this.world.getLightFromNeighbors(blockpos);

			if (this.world.isThundering()) {
				int j = this.world.getSkylightSubtracted();
				this.world.setSkylightSubtracted(10);
				i = this.world.getLightFromNeighbors(blockpos);
				this.world.setSkylightSubtracted(j);
			}

			return i <= this.rand.nextInt(12);
		}
	}
	
	static class AIRandomFly extends EntityAIBase {
		private final EntityWillo parentEntity;
		private int delayTicks = 0;

		public AIRandomFly(EntityWillo wisp) {
			this.parentEntity = wisp;
			this.setMutexBits(1);
		}

		/**
		 * Returns whether the EntityAIBase should begin execution.
		 */
		public boolean shouldExecute() {
			
			if (delayTicks-- > 0) {
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
		 * Returns whether an in-progress EntityAIBase should continue executing
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
				cursor.move(EnumFacing.DOWN);
			}
			
			while (cursor.getY() < 255 && !parentEntity.world.isAirBlock(cursor)) {
				cursor.move(EnumFacing.UP);
			}
			
			// Try and move `height` up
			for (int i = 0; i < height; i++) {
				if (parentEntity.world.isAirBlock(cursor.up())) {
					cursor.move(EnumFacing.UP);
				} else {
					break;
				}
			}
			
			this.parentEntity.getMoveHelper().setMoveTo(
					cursor.getX() + (d0 % 1.0),
					cursor.getY() + (d1 % 1.0),
					cursor.getZ() + (d2 % 1.0),
					1D);
			
			delayTicks = random.nextInt(20 * 10) + 20 * 4;
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
					3,
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
					2,
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
					3,
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
					2,
					EAlteration.INFLICT);
			putSpell("Heal",
					SelfTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.ICE,
					3,
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
					2,
					EAlteration.RUIN);
			putSpell("Roots",
					SeekingBulletTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.EARTH,
					2,
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
					3,
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
					3,
					EAlteration.RUIN);
			putSpell("Poison",
					SeekingBulletTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.WIND,
					2,
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
					2,
					EAlteration.RUIN);
			putSpell("Wind Ball II",
					ProjectileTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.WIND,
					3,
					EAlteration.RUIN);
			
			// Ender
			putSpell("Ender Beam",
					BeamTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.ENDER,
					2,
					EAlteration.RUIN);
			putSpell("Blindness",
					ProjectileTrigger.instance(),
					AoEShape.instance(),
					EMagicElement.ENDER,
					3,
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
	
	@Override
	@Nullable
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
		System.out.println("Spawning Willo!");
		final EMagicElement elem = EMagicElement.values()[NostrumMagica.rand.nextInt(EMagicElement.values().length)];
		dataManager.set(ELEMENT, elem);
		return super.onInitialSpawn(difficulty, livingdata);
	}
}
