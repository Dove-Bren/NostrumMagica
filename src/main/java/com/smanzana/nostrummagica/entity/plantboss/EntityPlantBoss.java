package com.smanzana.nostrummagica.entity.plantboss;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.World;

public class EntityPlantBoss extends EntityMob implements ILoreTagged, IEntityMultiPart {
	
	public static enum PlantBossTreeType {
		NORMAL,
		COVERED,
		ELEMENTAL,
	}
	
	public static final int NumberOfLeaves = 8;
	
	
	private final BossInfoServer bossInfo = (BossInfoServer)(new BossInfoServer(this.getDisplayName(), BossInfo.Color.GREEN, BossInfo.Overlay.NOTCHED_10)).setDarkenSky(true);
	private final PlantBossLeafLimb[] limbs;
	
	public EntityPlantBoss(World worldIn) {
		super(worldIn);
		this.setSize(3, 3);
        this.ignoreFrustumCheck = true;
        this.experienceValue = 1250;
		
		this.limbs = new PlantBossLeafLimb[NumberOfLeaves];
		for (int i = 0; i < NumberOfLeaves; i++) {
			limbs[i] = new PlantBossLeafLimb(this, i);
		}
	}
	
	@Override
	protected void entityInit() {
		super.entityInit();
		//this.dataManager.register(DRAGON_PHASE, DragonPhase.GROUNDED_PHASE.ordinal());
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

//		if (compound.hasKey(DRAGON_SERIAL_PHASE_TOK, NBT.TAG_ANY_NUMERIC)) {
//        	int i = compound.getByte(DRAGON_SERIAL_PHASE_TOK);
//            this.setPhase(DragonPhase.values()[i]);
//        }
//		
//		if (!this.world.isRemote) {
//			this.initEntityAI();
//		}
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
    	super.writeEntityToNBT(compound);
//    	compound.setByte(DRAGON_SERIAL_PHASE_TOK, (byte)this.getPhase().ordinal());
	}
	
	protected void positionLeaves(PlantBossLeafLimb[] limbs) {
		for (PlantBossLeafLimb limb : limbs) {
			final float yawProg = ((float) limb.index / (float) limbs.length); // 0 to 1
			final double limbRot = Math.PI * 2 * yawProg
								+ (this.rotationYawHead * Math.PI / 180.0);
			final double radius = this.width * (limb.index % 2 == 0 ? 1.25 : 1.5);
			
			final double x = this.posX
					+ Math.cos(limbRot) * radius;
			final double z = this.posZ + Math.sin(limbRot) * radius;
			
			final float pitch = yawProg * 90f;
			limb.setLocationAndAngles(x, posY, z, yawProg * 360f, pitch);
		}
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		
		this.rotationYawHead += .1f;
		
		positionLeaves(limbs);
		
		{
			if (this.world.isRemote) {
				if (this.getTreeType() == PlantBossTreeType.ELEMENTAL) {
//					NostrumParticles.GLOW_ORB.spawn(this.world, new NostrumParticles.SpawnParams(
//							1,
//							this.posX, this.posY + this.height + 4, this.posZ, 3,
//							30, 10,
//							this.getPositionVector().addVector(0, this.height + 1.7, 0)
//							).color(this.getTreeElement().getColor()));
					
//					NostrumParticles.GLOW_ORB.spawn(this.world, new NostrumParticles.SpawnParams(
//							1,
//							this.posX, this.posY + this.height + 1.70, this.posZ, .25,
//							40, 20,
//							new Vec3d(0, .2, 0), new Vec3d(.2, .1, .2)
//							).gravity(true).color(this.getTreeElement().getColor()));
					
					NostrumParticles.LIGHTNING_STATIC.spawn(this.world, new NostrumParticles.SpawnParams(
							1,
							this.posX, this.posY + this.height + 1, this.posZ, 1,
							40, 20,
							new Vec3d(0, .05, 0), Vec3d.ZERO
							).color(this.getTreeElement().getColor()));
				}
			}
		}
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
		// TODO
		return this.attackEntityFrom(source, damage);
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (!this.world.isRemote && source.getTrueSource() != null) {
			Entity ent = source.getTrueSource();
			if (ent instanceof EntityLivingBase && ent != this) {
				//this.shadowAttack.addToPool((EntityLivingBase) ent);
				//this.aggroTable.addDamage((EntityLivingBase) ent, amount);
			}
			
			//this.evasionTask.reset();
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
		return this.limbs;
	}
	
	@Override
	public boolean canBeCollidedWith() {
		return super.canBeCollidedWith();
	}
	
	@Override
	public boolean canBePushed() {
		return false;
	}
	
	public @Nullable PlantBossLeafLimb getLeafLimb(int index) {
		if (index < this.limbs.length) {
			return limbs[index];
		}
		return null;
	}
	
	public EMagicElement getTreeElement() {
		return EMagicElement.PHYSICAL;
	}
	
	public PlantBossTreeType getTreeType() {
		return PlantBossTreeType.COVERED;
	}
	
	public static class PlantBossLeafLimb extends MultiPartEntityPart {

		protected final int index;
		
		public PlantBossLeafLimb(EntityPlantBoss parent, int index) {
			super(parent, "PlantBoss_Leaf_" + index, 4, 4f / 16f);
			this.index = index;
		}
		
		public float getYawOffset() {
			final float yawProg = ((float) index / (float) EntityPlantBoss.NumberOfLeaves); // 0 to 1
			
			return yawProg * 360f;
		}
	}
}
