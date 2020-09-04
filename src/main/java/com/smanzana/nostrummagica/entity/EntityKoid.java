package com.smanzana.nostrummagica.entity;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.entity.tasks.KoidTask;
import com.smanzana.nostrummagica.items.EssenceItem;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityKoid extends EntityMob implements ILoreTagged {

	private static final DataParameter<Integer> KOID_VARIANT =
			EntityDataManager.<Integer>createKey(EntityKoid.class, DataSerializers.VARINT);
	
	private KoidTask kTask;
	private int idleCooldown;
	
	public EntityKoid(World worldIn) {
		this(worldIn, EMagicElement.values()[NostrumMagica.rand.nextInt(
				EMagicElement.values().length)]);
	}
	
    protected EntityKoid(World worldIn, EMagicElement element) {
        super(worldIn);
        this.setSize(0.5F, .8F);
        
        this.setElement(element);
        idleCooldown = NostrumMagica.rand.nextInt(20 * 30) + (20 * 10);
    }
    
    protected void initEntityAI() {
        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(4, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, true));
    }
    
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.33D);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(2.0D);
    }

    protected void playStepSound(BlockPos pos, Block blockIn)
    {
        this.playSound(SoundEvents.ENTITY_HUSK_STEP, 0.15F, 1.0F);
    }

    protected SoundEvent getHurtSound()
    {
        return SoundEvents.ENTITY_HUSK_HURT;
    }

    protected SoundEvent getDeathSound()
    {
    	return SoundEvents.ENTITY_HUSK_DEATH;
    }

    /**
     * Returns the volume for the sounds this mob makes.
     */
    protected float getSoundVolume()
    {
        return 0.7F;
    }

    public float getEyeHeight()
    {
        return this.height * 0.8F;
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

    public boolean processInteract(EntityPlayer player, EnumHand hand, @Nullable ItemStack stack)
    {
        return false;
    }

    /**
     * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
     * the animal type)
     */
    public boolean isBreedingItem(@Nullable ItemStack stack)
    {
        return false;
    }

    /**
     * Returns true if the mob is currently able to mate with the specified mob.
     */
    public boolean canMateWith(EntityAnimal otherAnimal)
    {
        return false;
    }

    public boolean shouldAttackEntity(EntityLivingBase target, EntityLivingBase owner)
    {
        return target != this;
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
				if (this.getAttackTarget() == null)
					NostrumMagicaSounds.CAST_FAIL.play(this);
				idleCooldown = NostrumMagica.rand.nextInt(20 * 30) + (20 * 10); 
			}
		}
	}
	
	public static String LoreKey = "nostrum__koid";
	
	@Override
	public String getLoreKey() {
		return LoreKey;
	}

	@Override
	public String getLoreDisplayName() {
		return "Koids";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("Koids are strange wisps of energy that have over time become attuned to the elements.");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("Koids are created when too much of one element of energy gathers in a location.", "Koids hate other koids, golems, and players.", "Koids have a chance of dropping elemental runes.");
	}
	
	public EMagicElement getElement() {
		return EMagicElement.values()[
              this.dataManager.get(KOID_VARIANT).intValue()];
	}
	
	public void setElement(EMagicElement element) {
		this.dataManager.set(KOID_VARIANT, element.ordinal());
		setCombatTask();
	}
	
	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(KOID_VARIANT, EMagicElement.PHYSICAL.ordinal());
	}
	
	public void setCombatTask() {
		if (this.worldObj != null && !this.worldObj.isRemote) {
			if (kTask != null)
				this.tasks.removeTask(kTask);
			
			kTask = new KoidTask(this);
	        this.tasks.addTask(2, kTask);
		}
	}
	
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);

        if (compound.hasKey("KoidType", NBT.TAG_ANY_NUMERIC)) {
        	int i = compound.getByte("KoidType");
            this.setElement(EMagicElement.values()[i]);
        }

        this.setCombatTask();
	}
	
	public void writeEntityToNBT(NBTTagCompound compound) {
    	super.writeEntityToNBT(compound);
        compound.setByte("KoidType", (byte)this.getElement().ordinal());
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
		int count = this.rand.nextInt(2);
		count += lootingModifier;
		
		this.entityDropItem(EssenceItem.getEssence(
				this.getElement(),
				count), 0);
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ENTITY;
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (source.isProjectile()) {
			amount *= 0.25f;
		}
		
		return super.attackEntityFrom(source, amount);
	}
	
	@Override
	protected boolean isValidLightLevel() {
		return super.isValidLightLevel(); // Like a regular mob.
	}
}
