package com.smanzana.nostrummagica.entity.tasks;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import com.smanzana.nostrummagica.entity.dragon.EntityDragon;
import com.smanzana.nostrummagica.entity.dragon.EntityShadowDragonRed;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;

public class DragonSummonShadowAttack<T extends EntityDragon> extends EntityAIBase {
	
	protected T dragon;
	protected int delay;
	protected int odds;
	protected Set<EntityLivingBase> pool;
	
	protected int attackTicks;
	
	public DragonSummonShadowAttack(T dragon, int delay, int odds) {
		this.delay = delay;
		this.odds = odds;
		this.dragon = dragon;
		this.pool = new HashSet<>();
		
		this.setMutexBits(0);
	}
	
	@Override
	public boolean shouldExecute() {
		this.attackTicks = Math.max(0, this.attackTicks-1);
		
		if (dragon.isDead)
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
		for (EntityLivingBase targ : this.pool) {
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
	public boolean continueExecuting() {
		return false;
	}

	@Override
	public void startExecuting() {
		if (this.pool.isEmpty())
			return;
		
		final double MaxRange = 64.0D * 64.0D;
		
		// update pool based on distance
		Iterator<EntityLivingBase> it = pool.iterator();
		while (it.hasNext()) {
			EntityLivingBase targ = it.next();
			if (targ.isDead || targ.getDistanceSqToEntity(dragon) > MaxRange) {
				it.remove();
			}
		}

		if (this.pool.isEmpty())
			return;
		
		Random rand = dragon.getRNG();
		for (EntityLivingBase targ : this.pool) {
			EntityShadowDragonRed ent = new EntityShadowDragonRed(targ.worldObj, targ);
			ent.setPosition(targ.posX + 5.0 * (rand.nextDouble() - .5D), targ.posY, targ.posZ + 5.0 * (rand.nextDouble() - .5D));
			targ.worldObj.spawnEntityInWorld(ent);
		}
		
		NostrumMagicaSounds.DRAGON_DEATH.play(dragon.worldObj, dragon.posX, dragon.posY, dragon.posZ);
		
		attackTicks = this.delay;
	}
	
	@Override
	public void updateTask() {
		
	}
	
	public void addToPool(EntityLivingBase ent) {
		this.pool.add(ent);
	}
}
