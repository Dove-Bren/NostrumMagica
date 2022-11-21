package com.smanzana.nostrummagica.entity.tasks.arcanewolf;

import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf.ArcaneWolfElementalType;
import com.smanzana.nostrummagica.potions.NaturesBlessingPotion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.potion.PotionEffect;

public class ArcaneWolfAINatureTask extends EntityAIBase {

	protected final EntityArcaneWolf wolf;
	
	// Cooldown, done as compared to wolf.ticksExisted
	protected int cooldownTicks = 0;
	protected final int manaCost;
	
	public ArcaneWolfAINatureTask(EntityArcaneWolf wolf, int manaCost) {
		this.wolf = wolf;
		this.manaCost = manaCost;
		
		this.setMutexBits(0); // Can execute with any! Nice!
	}
	
	@Override
	public boolean shouldExecute() {
		return !wolf.isDead
				&& !wolf.isSitting()
				&& wolf.getOwner() != null
				&& wolf.ticksExisted >= cooldownTicks
				&& wolf.getMana() >= manaCost
				&& wolf.getElementalType() == ArcaneWolfElementalType.NATURE;
	}
	
	protected List<EntityLivingBase> getTargets(EntityArcaneWolf wolf) {
		EntityLivingBase owner = wolf.getOwner();
		List<EntityLivingBase> tames = NostrumMagica.getTamedEntities(owner);
		tames.add(owner);
		tames.removeIf((e) -> { return e.getDistance(wolf) > 15;});
		return tames;
	}
	
	protected boolean applyTo(EntityArcaneWolf wolf, EntityLivingBase target) {
		// Nature keeps the "Nature's blessing" status effect constant
		PotionEffect effect = target.getActivePotionEffect(NaturesBlessingPotion.instance());
		if (effect == null || effect.getDuration() < 11 * 20) {
			target.addPotionEffect(new PotionEffect(NaturesBlessingPotion.instance(), 20 * 30, 0));
		}
		
		return effect == null; // Only charge for applying the first time
	}
	
	@Override
	public void startExecuting() {
		int backoff = 5;
		List<EntityLivingBase> targets = this.getTargets(wolf);
		for (EntityLivingBase target : targets) {
			if (applyTo(wolf, target)) {
				wolf.addMana(-manaCost);
				NostrumParticles.FILLED_ORB.spawn(wolf.world, new SpawnParams(
						1, wolf.posX, wolf.posY + wolf.height/2, wolf.posZ, 0, 20, 0, target.getEntityId()
						).color(ArcaneWolfElementalType.NATURE.getColor()));
			}
		}
		
		cooldownTicks = wolf.ticksExisted + backoff;
	}

}