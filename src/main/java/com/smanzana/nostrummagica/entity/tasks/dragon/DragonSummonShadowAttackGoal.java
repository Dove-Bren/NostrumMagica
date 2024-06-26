package com.smanzana.nostrummagica.entity.tasks.dragon;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.entity.dragon.DragonEntity;
import com.smanzana.nostrummagica.entity.dragon.ShadowRedDragonEntity;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;

public class DragonSummonShadowAttackGoal<T extends DragonEntity> extends Goal {
	
	protected T dragon;
	protected int delay;
	protected int odds;
	protected Set<LivingEntity> pool;
	
	protected int attackTicks;
	
	public DragonSummonShadowAttackGoal(T dragon, int delay, int odds) {
		this.delay = delay;
		this.odds = odds;
		this.dragon = dragon;
		this.pool = new HashSet<>();
		
		//this.setMutexBits(0);
	}
	
	@Override
	public boolean shouldExecute() {
		this.attackTicks = Math.max(0, this.attackTicks-1);
		
		if (!dragon.isAlive())
			return false;
		
		if (pool.isEmpty())
			return false;
		
		if (this.attackTicks != 0) {
			return false;
		}

		if (odds > 0 && dragon.getRNG().nextInt(odds) != 0) {
			return false;
		}
		
		boolean found = false;
		for (LivingEntity targ : this.pool) {
			if (dragon.getEntitySenses().canSee(targ)) {
				found = true;
				break;
			}
		}
		
		if (!found) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean shouldContinueExecuting() {
		return false;
	}

	@Override
	public void startExecuting() {
		if (this.pool.isEmpty())
			return;
		
		final double MaxRange = 64.0D * 64.0D;
		
		// update pool based on distance
		Iterator<LivingEntity> it = pool.iterator();
		while (it.hasNext()) {
			LivingEntity targ = it.next();
			if (!targ.isAlive() || targ.getDistanceSq(dragon) > MaxRange) {
				it.remove();
			}
		}

		if (this.pool.isEmpty())
			return;
		
		Random rand = dragon.getRNG();
		for (LivingEntity targ : this.pool) {
			ShadowRedDragonEntity ent = new ShadowRedDragonEntity(NostrumEntityTypes.shadowDragonRed, targ.world, targ);
			ent.setPosition(targ.getPosX() + 5.0 * (rand.nextDouble() - .5D), targ.getPosY(), targ.getPosZ() + 5.0 * (rand.nextDouble() - .5D));
			targ.world.addEntity(ent);
		}
		
		NostrumMagicaSounds.DRAGON_DEATH.play(dragon.world, dragon.getPosX(), dragon.getPosY(), dragon.getPosZ());
		
		attackTicks = this.delay;
	}
	
	@Override
	public void tick() {
		
	}
	
	public void addToPool(LivingEntity ent) {
		this.pool.add(ent);
	}
}
