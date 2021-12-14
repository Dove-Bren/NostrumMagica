package com.smanzana.nostrummagica.pet;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.EntityLivingBase;

public class PetInfo {
	
	public static enum SecondaryFlavor {
		/**
		 * A bad thing, but expected to slowly grow as times goes on.
		 * For example, 'fatigue'.
		 * "Full" is very bad.
		 */
		GRADUAL_BAD(0xFF4BAD20, 0xFFB51921),
		
		/**
		 * A bad thing. Like permanent-deathliness or something
		 */
		BAD(0xFFB51921, 0xFFB51921),
		
		/**
		 * Like XP. A good thing, but expected to be growing regularly
		 */
		PROGRESS(0xFFFFB2FF, 0xFFFFB2FF),
		
		/**
		 * A good thing. Like health, but not health. Like mana?
		 */
		GOOD(0xFF4BAD20, 0xFF4BAD20),
		
		/**
		 * A good thing that's expected to slowly grow or drop. For example, energy.
		 * "Full" is great.
		 */
		GRADUAL_GOOD(0xFFB51921, 0xFF4BAD20);
		
		protected final float emptyR;
		protected final float emptyG;
		protected final float emptyB;
		protected final float emptyA;
		
		protected final float diffR;
		protected final float diffG;
		protected final float diffB;
		protected final float diffA;
		
		// ARGB
		private SecondaryFlavor(int empty, int full) {
			emptyR = (float) ((empty >> 16) & 0xFF) / 256f;
			emptyG = (float) ((empty >> 8) & 0xFF) / 256f;
			emptyB = (float) ((empty >> 0) & 0xFF) / 256f;
			emptyA = (float) ((empty >> 24) & 0xFF) / 256f;
			
			float fullR = (float) ((full >> 16) & 0xFF) / 256f;
			float fullG = (float) ((full >> 8) & 0xFF) / 256f;
			float fullB = (float) ((full >> 0) & 0xFF) / 256f;
			float fullA = (float) ((full >> 24) & 0xFF) / 256f;
			
			diffR = fullR - emptyR;
			diffG = fullG - emptyG;
			diffB = fullB - emptyB;
			diffA = fullA - emptyA;
		}
		
		public float colorR(float perc) {
			return emptyR + (perc * diffR);
		}
		
		public float colorG(float perc) {
			return emptyG + (perc * diffG);
		}
		
		public float colorB(float perc) {
			return emptyB + (perc * diffB);
		}
		
		public float colorA(float perc) {
			return emptyA + (perc * diffA);
		}
	}
	
	public static enum PetAction {
		SITTING,
		ATTACKING,
		IDLING,
		WORKING,
		WAITING
	}

	private double currentHp;
	private double maxHp;
	private double hpPercent; // out of 1.0

	private double currentSecondary;
	private double maxSecondary;
	private double secondaryPercent; // out of 1.0
	
	private SecondaryFlavor flavor;
	private PetAction action;
	
	private int refCount;
	
	protected PetInfo() {
		refCount = 0;
	}
	
	protected void set(double hp, double maxHp, double secondary, double maxSecondary, SecondaryFlavor flavor, PetAction action) {
		this.currentHp = hp;
		this.maxHp = maxHp;
		this.hpPercent = maxHp > 0 ? (hp / maxHp) : 0;
		
		this.currentSecondary = secondary;
		this.maxSecondary = maxSecondary;
		this.secondaryPercent = maxSecondary > 0 ? (secondary / maxSecondary) : 0;
		
		this.flavor = flavor == null ? SecondaryFlavor.PROGRESS : flavor;
		this.action = action == null ? PetAction.WAITING : action;
	}
	
	protected void set(double hp, double maxHp, double secondary, double maxSecondary) {
		set(hp, maxHp, secondary, maxSecondary, null, null);
	}
	
	protected void set(double hp, double maxHp, PetAction action) {
		set(hp, maxHp, 0, 0, null, action);
	}
	
	protected void set(double hp, double maxHp) {
		set(hp, maxHp, 0, 0);
	}
	
	protected PetInfo addRef() {
		refCount++;
		return this;
	}
	
	protected boolean removeRef() {
		refCount--;
		if (refCount < 0) {
			throw new RuntimeException("Invalid number of removes");
		}
		
		return refCount == 0;
	}
	
	private void validate() {
		// No getters should be called if refcount is 0
		if (refCount <= 0) {
			throw new RuntimeException("PetInfo was released but is still being used");
		}
	}
	
	public void release() {
		release(this);
	}
	
	public double getCurrentHp() {
		validate();
		return currentHp;
	}

	public double getMaxHp() {
		validate();
		return maxHp;
	}

	public double getHpPercent() {
		validate();
		return hpPercent;
	}

	public double getCurrentSecondary() {
		validate();
		return currentSecondary;
	}

	public double getMaxSecondary() {
		validate();
		return maxSecondary;
	}

	public double getSecondaryPercent() {
		validate();
		return secondaryPercent;
	}
	
	public SecondaryFlavor getSecondaryFlavor() {
		validate();
		return this.flavor;
	}
	
	public PetAction getPetAction() {
		validate();
		return this.action;
	}


	/**************************************************
	 * 
	 *    Pooling methods
	 * 
	 **************************************************/
	
	private static final List<PetInfo> availableInfos = new ArrayList<>();
	private static final int ChunkSize = 32;
	
	public static final PetInfo claim() {
		synchronized(availableInfos) {
			if (availableInfos.isEmpty()) {
				// Imagine if I allocated an array of PetInfos and then indexed into those
				// like a C mem pool that grabbed contiguous chunks lol
				for (int i = 0; i < ChunkSize; i++) {
					availableInfos.add(new PetInfo());
				}
			}
			
			return availableInfos.remove(availableInfos.size() - 1).addRef();
		}
	}
	
	public static final PetInfo claim(double hp, double maxHp, double secondary, double maxSecondary, SecondaryFlavor flavor, PetAction action) {
		PetInfo info = claim();
		info.set(hp, maxHp, secondary, maxSecondary, flavor, action);
		return info;
	}
	
	public static final PetInfo claim(double hp, double maxHp, double secondary, double maxSecondary) {
		PetInfo info = claim();
		info.set(hp, maxHp, secondary, maxSecondary);
		return info;
	}
	
	public static final PetInfo claim(double hp, double maxHp, PetAction action) {
		PetInfo info = claim();
		info.set(hp, maxHp, action);
		return info;
	}
	
	public static final PetInfo claim(double hp, double maxHp) {
		PetInfo info = claim();
		info.set(hp, maxHp);
		return info;
	}
	
	public static final void release(PetInfo info) {
		// if pooled is false, they shouldn't call this... but oh well.
		if (info.removeRef()) {
			synchronized(availableInfos) {
				availableInfos.add(info);
			}
		}
	}
	
	public static PetInfo Wrap(EntityLivingBase entity) {
		return claim(entity.getHealth(), entity.getMaxHealth());
	}
	
	
	
	/**************************************************
	 * 
	 *    Managed methods
	 * 
	 **************************************************/
	
	public static class ManagedPetInfo extends PetInfo {

		protected ManagedPetInfo() {
			super();
			super.addRef();
		}
		
		@Override
		public void set(double hp, double maxHp, double secondary, double maxSecondary, SecondaryFlavor flavor, PetAction action) {
			super.set(hp, maxHp, secondary, maxSecondary, flavor, action);
		}
		
		@Override
		public void set(double hp, double maxHp, double secondary, double maxSecondary) {
			super.set(hp, maxHp, secondary, maxSecondary);
		}
		
		@Override
		public void set(double hp, double maxHp, PetAction action) {
			super.set(hp, maxHp, action);
		}
		
		@Override
		public void set(double hp, double maxHp) {
			super.set(hp, maxHp, 0, 0);
		}
		
		@Override
		protected boolean removeRef() {
			return false;
		}
		
		@Override
		protected PetInfo addRef() {
			return this;
		}
	}
	
	/**
	 * Create a PetInfo that isn't pooled or managed by the pool.
	 * There's no refcounting or pooling mechanics. Just use and let GC when applicable.
	 * If you are calling this very frequently and the lifespan of the infos is short,
	 * consider using the pooling calls (#claim(), #release())
	 * @return
	 */
	public static ManagedPetInfo createManaged() {
		return new ManagedPetInfo();
	}
}
