package com.smanzana.nostrummagica.entity.tasks.dragon;

import java.util.Arrays;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.entity.dragon.DragonEntity;
import com.smanzana.nostrummagica.entity.dragon.DragonGambit;
import com.smanzana.nostrummagica.entity.dragon.ITameDragon;
import com.smanzana.nostrummagica.spell.Spell;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

// Extend with some way to get gambits and spells, silly!
public abstract class DragonGambittedSpellAttackGoal<T extends DragonEntity & ITameDragon> extends Goal {
	
	protected T dragon;
	protected int delay;
	protected int odds;
	
	protected int attackTicks;
	protected LivingEntity currentTarget;
	protected int[] tickStorage;
	
	public DragonGambittedSpellAttackGoal(T dragon, int delay, int odds) {
		this.dragon = dragon;
		this.delay = delay;
		this.odds = odds;
		
		//this.setMutexBits(0);
	}
	
	/**
	 * Return an array of gambits to use.
	 * This array should map 1-1 to what's returned with getSpells.
	 * That is, getGambits()[i] should correspond to getSpells()[i]
	 * @return
	 */
	public abstract DragonGambit[] getGambits();
	
	/**
	 * Return an array of spells to use.
	 * @return
	 */
	public abstract Spell[] getSpells();
	
	public abstract LivingEntity getTarget(T dragon);
	
	@Override
	public boolean canUse() {
		this.attackTicks = Math.max(0, this.attackTicks-1);
		
		if (!dragon.isAlive())
			return false;
		
		if (dragon.isEntitySitting())
			return false;
		
		currentTarget = getTarget(dragon);
		if (currentTarget == null) {
			return false;
		}
		
		if (!dragon.getSensing().hasLineOfSight(currentTarget)){
			return false;
		}
		
		if (this.attackTicks == 0) {
			if (odds > 0 && dragon.getRandom().nextInt(odds) == 0) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public boolean canContinueToUse() {
		return false;
	}
	
	// Actually has the logic of using gambits!
	private Spell getSpellToCast(Spell[] spells, DragonGambit[] gambits) {
		Spell spell = null;
		
		// Fix up tickStorage in case things are returning weirdly
		if (this.tickStorage == null) {
			this.tickStorage = new int[spells.length];
		} else if (this.tickStorage.length < spells.length) {
			this.tickStorage = Arrays.copyOf(this.tickStorage, spells.length);
		}
		
		for (int i = 0; spell == null && i < spells.length; i++) {
			Spell cur = spells[i];
			DragonGambit gambit = gambits[i];
			if (cur == null) {
				break;
			}
			
			if (cur.getManaCost() > dragon.getMana()) {
				continue;
			}
			
			int nowTicks = dragon.level.getServer().getTickCount();
			
			switch (gambit) {
			case ALWAYS:
				spell = cur;
				break;
			case FREQUENT:
				// Check if we're 20 seconds after last, or we've wrapped an int.
				if (tickStorage[i] + (20 * 20) < nowTicks || tickStorage[i] > nowTicks) {
					spell = cur;
				}
				break;
			case HEALTH_CRITICAL:
				if (currentTarget.getHealth() / currentTarget.getMaxHealth() <= 0.15f) {
					spell = cur;
				}
				break;
			case HEALTH_LOW:
				if (currentTarget.getHealth() / currentTarget.getMaxHealth() <= 0.40f) {
					spell = cur;
				}
				break;
			case MANA_LOW:
				if (currentTarget instanceof ITameDragon) {
					ITameDragon d = (ITameDragon) currentTarget;
					if ((float) d.getMana() / (float) d.getMaxMana() <= 0.40f) {
						spell = cur;
					}
				} else if (currentTarget instanceof Entity) {
					INostrumMagic attr = NostrumMagica.getMagicWrapper((Entity) currentTarget);
					if (attr != null && attr.isUnlocked()
							&& (float) attr.getMana() / (float) attr.getMaxMana() <= 0.40f) {
						spell = cur;
					}
				}
				break;
			case OCCASIONAL:
				// Check if we're 20 seconds after last, or we've wrapped an int.
				if (tickStorage[i] + (20 * 60) < nowTicks || tickStorage[i] > nowTicks) {
					spell = cur;
				}
				break;
			}
			
			// Set tick storage
			if (spell != null) {
				this.tickStorage[i] = nowTicks;
			}
			
			// TODO this data is stale when spells move around.. but that won't happen TOO fast anyways.
		}
		
		return spell;
	}

	@Override
	public void start() {
		Spell[] spells = getSpells();
		DragonGambit[] gambits = getGambits();
		
		if (spells == null || spells.length == 0) {
			return;
		}
		
		if (gambits == null || gambits.length == 0) {
			return;
		}
		
		if (gambits.length != spells.length) {
			NostrumMagica.logger.error("Gambit and spell lengths do not match");
			return;
		}
		
		Spell spell = getSpellToCast(spells, gambits);
		
		if (spell == null) {
			return;
		}
		
		dragon.lookAt(currentTarget, 360f, 180f);
		
		spell.cast(dragon, 1);
		attackTicks = this.delay;
	}
	
	@Override
	public void tick() {
		
	}
}
