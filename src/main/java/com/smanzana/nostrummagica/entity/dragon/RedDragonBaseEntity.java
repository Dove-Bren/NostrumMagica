package com.smanzana.nostrummagica.entity.dragon;

import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap.MutableAttribute;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public abstract class RedDragonBaseEntity extends FlyingDragonEntity {

	private static final DataParameter<Boolean> DRAGON_SLASH =
			EntityDataManager.<Boolean>defineId(RedDragonBaseEntity.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> DRAGON_BITE =
			EntityDataManager.<Boolean>defineId(RedDragonBaseEntity.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> DRAGON_CASTING =
			EntityDataManager.<Boolean>defineId(RedDragonBaseEntity.class, DataSerializers.BOOLEAN);
	
	public static long ANIM_SLASH_DUR = 500;
	
	public static long ANIM_BITE_DUR = 250;
	
	// Time in MS when we last slashed.
	private long slashTime;
	
	private long biteTime;
	
	private long castTime;
	
	public RedDragonBaseEntity(EntityType<? extends RedDragonBaseEntity> type, World worldIn) {
		super(type, worldIn);
        this.noCulling = true;
        this.noPhysics = false;
	}
	
	@Override
	public void onSyncedDataUpdated(DataParameter<?> key) {
		super.onSyncedDataUpdated(key);
		if (key == DRAGON_SLASH) {
			if (this.entityData.get(DRAGON_SLASH)) {
				this.slashTime = System.currentTimeMillis();
			}
		} else if (key == DRAGON_BITE) {
			if (this.entityData.get(DRAGON_BITE)) {
				this.biteTime = System.currentTimeMillis();
			}
		} else if (key == DRAGON_CASTING) {
			if (this.entityData.get(DRAGON_CASTING)) {
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
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DRAGON_SLASH, false);
		this.entityData.define(DRAGON_BITE, false);
		this.entityData.define(DRAGON_CASTING, false);
	}
	
	public boolean isCasting() {
		return this.entityData.get(DRAGON_CASTING).booleanValue();
	}
	
	protected void setCasting(boolean isCasting) {
		this.entityData.set(DRAGON_CASTING, isCasting);
		
	}
	
	public void slash(LivingEntity target) {
		this.entityData.set(DRAGON_SLASH, Boolean.TRUE);
		
		this.doHurtTarget(target);
	}
	
	public void bite(LivingEntity target) {
		this.entityData.set(DRAGON_BITE, Boolean.TRUE);
		
		NostrumMagicaSounds.DRAGON_BITE.play(this);
		
		this.biteDamageInternal(target);
	}
	
	private void biteDamageInternal(LivingEntity target) {
		float f = (float)this.getAttribute(Attributes.ATTACK_DAMAGE).getValue();
		
		// Dragons do 20 damage while on the ground, and 16 when flying
		if (!this.isFlying()) {
			f *= 2.0;
		} else {
			f *= 1.6;
		}
		
		int i = 0; // knockback

		i = 2;

		boolean flag = target.hurt(DamageSource.mobAttack(this), f);

		if (flag)
		{
			if (i > 0)
			{
				target.knockback((float)i * 0.5F, (double)MathHelper.sin(this.yRot * 0.017453292F), (double)(-MathHelper.cos(this.yRot * 0.017453292F)));
				this.setDeltaMovement(this.getDeltaMovement().multiply(.6, 1, .6));
			}

			if (target instanceof PlayerEntity)
			{
				PlayerEntity entityplayer = (PlayerEntity)target;
				ItemStack itemstack1 = entityplayer.isUsingItem() ? entityplayer.getUseItem() : ItemStack.EMPTY;

				if (!itemstack1.isEmpty() && itemstack1.isShield(entityplayer))
				{
					float f1 = 0.5F;

					if (this.random.nextFloat() < f1)
					{
						// Note: Vanilla puts the attacking item on cooldown here instead of the blocking one?
						entityplayer.getCooldowns().addCooldown(itemstack1.getItem(), 100);
						this.level.broadcastEntityEvent(entityplayer, (byte)30);
					}
				}
			}
		}
	}
	
	@Override
	public void tick() {
		super.tick();
		
		//System.out.println("(" + this.getPosX() + ", " + this.getPosZ() + ")");
		
		long now = System.currentTimeMillis();
		
		if (this.entityData.get(DRAGON_SLASH)) {
			if (now - slashTime >= ANIM_SLASH_DUR) {
				this.entityData.set(DRAGON_SLASH, Boolean.FALSE);
			}
		}
		
		if (this.entityData.get(DRAGON_BITE)) {
			if (now - biteTime >= ANIM_BITE_DUR) {
				this.entityData.set(DRAGON_BITE, Boolean.FALSE);
			}
		}
	}
	
	protected static final MutableAttribute BuildBaseRedDragonAttributes() {
		return FlyingDragonEntity.BuildBaseFlyingAttributes();
	}
	
}
