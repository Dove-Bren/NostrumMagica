package com.smanzana.nostrummagica.capabilities;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.ParticleTargetBehavior.TargetBehavior;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Default implementation of the IManaArmor interface
 * @author Skyler
 *
 */
public class ManaArmor implements IManaArmor {
	
	private static final String NBT_HAS_ARMOR = "has_armor";
	private static final String NBT_MANA_COST = "mana_cost";
	
	private boolean hasArmor;
	private int manaCost;
	
	private final LivingEntity entity;
	
	public ManaArmor(LivingEntity entity) {
		hasArmor = false;
		manaCost = 0;
		this.entity = entity;
	}

	@Override
	public boolean hasArmor() {
		return hasArmor;
	}
	
	@Override
	public void setHasArmor(boolean hasArmor, int manaCost) {
		if (this.hasArmor != hasArmor || manaCost != this.getManaCost()) {
			@Nullable INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
			if (attr != null && attr.isUnlocked()) {
				attr.addReservedMana(-this.getManaCost()); // Add back whatever we've taken
				if (hasArmor) {
					// Deduct new reserve
					attr.addReservedMana(manaCost);
				}
			}
		}
		this.hasArmor = hasArmor;
		this.manaCost = manaCost;
	}
	
	@Override
	public int getManaCost() {
		return this.manaCost;
	}

	@Override
	public void deserialize(boolean hasArmor, int manaCost) {
		this.hasArmor = hasArmor;
		this.manaCost = manaCost;
	}

	@Override
	public void copy(IManaArmor cap) {
		this.deserialize(cap.hasArmor(), cap.getManaCost());
	}
	
	@Override
	public boolean canHandle(Entity hurtEntity, DamageSource source, float amount) {
		// I want to filter out things that damage creative players, but not all mods are good about that...
		if (source == DamageSource.OUT_OF_WORLD) {
			return false;
		}
		
		// Known for sure ignored types:
		if (source == DamageSource.DROWN
				|| source == DamageSource.FALL
				|| source == DamageSource.IN_WALL
				|| source == DamageSource.STARVE
				|| source == DamageSource.WITHER) {
			return false;
		}
		
		// Don't handle if amount is already 0
		if (amount <= 0f) {
			return false;
		}
		
		// Handle if armor is on even if we don't have en ough mana to cover it
		@Nullable INostrumMagic attr = NostrumMagica.getMagicWrapper(hurtEntity);
		return this.hasArmor() && attr != null && attr.getMana() > 0;
	}

	@Override
	public float handle(Entity hurtEntity, DamageSource source, float originalAmount) {
		// Assume we can afford it and don't bother checking cost
		final float amount = 0f;
		
		spawnEffects(hurtEntity, source, originalAmount, amount);
		
		// Deduct cost.
		// If no mana left, turn armor off!
		if (deductCost(hurtEntity, source, originalAmount, amount)) {
			removeArmor(hurtEntity);
		}
		
		return amount;
	}
	
	protected void spawnEffects(Entity hurtEntity, DamageSource source, float originalAmount, float finalAmount) {
		NostrumParticles.WARD.spawn(hurtEntity.level, new NostrumParticles.SpawnParams(
				4, hurtEntity.getX(), hurtEntity.getY() + (hurtEntity.getBbHeight()/2), hurtEntity.getZ(), .75, 30, 0,
				Vec3.ZERO, new Vec3(.0, -.01, .0)
				//hurtEntity.getEntityId()
				)
					.color(0x602244FF)
					.setTargetBehavior(TargetBehavior.ORBIT));
	}
	
	protected boolean deductCost(Entity hurtEntity, DamageSource source, float originalAmount, float finalAmount) {
		@Nullable INostrumMagic attr = NostrumMagica.getMagicWrapper(hurtEntity);
		if (attr != null) {
			final int cost = calcManaCost(hurtEntity, source, originalAmount);
			attr.addMana(-cost);
		}
		
		return attr.getMana() <= 0;
	}
	
	protected void spawnBreakEffects(Entity hurtEntity) {
		NostrumMagicaSounds.SHIELD_BREAK.play(hurtEntity);
		NostrumParticles.WARD.spawn(hurtEntity.level, new NostrumParticles.SpawnParams(
				20, hurtEntity.getX(), hurtEntity.getY() + (hurtEntity.getBbHeight()/2), hurtEntity.getZ(), .75, 30, 0,
				new Vec3(.0, -.01, .0), new Vec3(.01, 0, .01) 
				//hurtEntity.getEntityId()
				)
					.gravity(true)
					.color(0x602244FF));
	}
	
	protected void removeArmor(Entity exhaustedEntity) {
		this.setHasArmor(false, 0);
		spawnBreakEffects(exhaustedEntity);
		if (exhaustedEntity instanceof ServerPlayer) {
			NostrumMagica.Proxy.syncPlayer((ServerPlayer) exhaustedEntity);
		}
	}
	
	protected int calcManaCost(Entity hurtEntity, DamageSource source, float amount) {
		if (amount <= 0) {
			return 0;
		} else if (amount <= 10) {
			return 10 * (int) Math.ceil(amount / 5f); // 10 mana per 5 reduction. 4 damage is 10 mana. 14 damage is 15 mana.
		} else {
			return 25; 
		}
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		
		nbt.putBoolean(NBT_HAS_ARMOR, hasArmor());
		nbt.putInt(NBT_MANA_COST, getManaCost());
		
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag tag) {
		deserialize(tag.getBoolean(NBT_HAS_ARMOR),
			tag.getInt(NBT_MANA_COST));
	}
}
