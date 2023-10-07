package com.smanzana.nostrummagica.entity.dragon;

import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public abstract class EntityDragonRedBase extends EntityDragonFlying {

	private static final DataParameter<Boolean> DRAGON_SLASH =
			EntityDataManager.<Boolean>createKey(EntityDragonRedBase.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> DRAGON_BITE =
			EntityDataManager.<Boolean>createKey(EntityDragonRedBase.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> DRAGON_CASTING =
			EntityDataManager.<Boolean>createKey(EntityDragonRedBase.class, DataSerializers.BOOLEAN);
	
	public static long ANIM_SLASH_DUR = 500;
	
	public static long ANIM_BITE_DUR = 250;
	
	// Time in MS when we last slashed.
	private long slashTime;
	
	private long biteTime;
	
	private long castTime;
	
	public EntityDragonRedBase(EntityType<? extends EntityDragonRedBase> type, World worldIn) {
		super(type, worldIn);
        this.ignoreFrustumCheck = true;
        this.noClip = false;
	}
	
	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		super.notifyDataManagerChange(key);
		if (key == DRAGON_SLASH) {
			if (this.dataManager.get(DRAGON_SLASH)) {
				this.slashTime = System.currentTimeMillis();
			}
		} else if (key == DRAGON_BITE) {
			if (this.dataManager.get(DRAGON_BITE)) {
				this.biteTime = System.currentTimeMillis();
			}
		} else if (key == DRAGON_CASTING) {
			if (this.dataManager.get(DRAGON_CASTING)) {
				castTime = System.currentTimeMillis();
			}
		}
	}
	
	public long getLastSlashTime() {
		return this.slashTime;
	}
	
	public long getLastBiteTime() {
		return this.biteTime;
	}
	
	public long getLastCastTime() {
		return this.castTime;
	}
	
	@Override
	protected void registerData() { int unused; // TODO
		super.entityInit();
		this.dataManager.register(DRAGON_SLASH, false);
		this.dataManager.register(DRAGON_BITE, false);
		this.dataManager.register(DRAGON_CASTING, false);
	}
	
	public boolean isCasting() {
		return this.dataManager.get(DRAGON_CASTING).booleanValue();
	}
	
	protected void setCasting(boolean isCasting) {
		this.dataManager.set(DRAGON_CASTING, isCasting);
		
	}
	
	public void slash(LivingEntity target) {
		this.dataManager.set(DRAGON_SLASH, Boolean.TRUE);
		
		this.attackEntityAsMob(target);
	}
	
	public void bite(LivingEntity target) {
		this.dataManager.set(DRAGON_BITE, Boolean.TRUE);
		
		NostrumMagicaSounds.DRAGON_BITE.play(this);
		
		this.biteDamageInternal(target);
	}
	
	private void biteDamageInternal(LivingEntity target) {
		float f = (float)this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
		
		// Dragons do 20 damage while on the ground, and 16 when flying
		if (!this.isFlying()) {
			f *= 2.0;
		} else {
			f *= 1.6;
		}
		
		int i = 0;

		i = 2;

		boolean flag = target.attackEntityFrom(DamageSource.causeMobDamage(this), f);

		if (flag)
		{
			if (i > 0)
			{
				target.knockBack(this, (float)i * 0.5F, (double)MathHelper.sin(this.rotationYaw * 0.017453292F), (double)(-MathHelper.cos(this.rotationYaw * 0.017453292F)));
				this.getMotion().x *= 0.6D;
				this.getMotion().z *= 0.6D;
			}

			if (target instanceof PlayerEntity)
			{
				PlayerEntity entityplayer = (PlayerEntity)target;
				ItemStack itemstack1 = entityplayer.isHandActive() ? entityplayer.getActiveItemStack() : ItemStack.EMPTY;

				if (!itemstack1.isEmpty() && itemstack1.getItem() == Items.SHIELD)
				{
					float f1 = 0.5F;

					if (this.rand.nextFloat() < f1)
					{
						entityplayer.getCooldownTracker().setCooldown(Items.SHIELD, 100);
						this.world.setEntityState(entityplayer, (byte)30);
					}
				}
			}
		}
	}
	
	@Override
	public void tick() {
		super.tick();
		
		//System.out.println("(" + this.posX + ", " + this.posZ + ")");
		
		long now = System.currentTimeMillis();
		
		if (this.dataManager.get(DRAGON_SLASH)) {
			if (now - slashTime >= ANIM_SLASH_DUR) {
				this.dataManager.set(DRAGON_SLASH, Boolean.FALSE);
			}
		}
		
		if (this.dataManager.get(DRAGON_BITE)) {
			if (now - biteTime >= ANIM_BITE_DUR) {
				this.dataManager.set(DRAGON_BITE, Boolean.FALSE);
			}
		}
	}
	
}
