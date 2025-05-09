package com.smanzana.nostrummagica.spelltome;

public class SpellCastSummary {

	private final float baseXP;
	private final int baseCost;
	private final int baseCastTicks;
	
	private float reagentCost;
	private float xpRate;
	private float costRate;
	private float efficiencyRate;
	private int weightDiscount;
	private float castSpeedRate;
	
	public SpellCastSummary(int cost, float xp, int castTicks) {
		this.baseCost = cost;
		this.baseXP = xp;
		this.baseCastTicks = castTicks;
		this.reagentCost
			= this.xpRate
			= this.costRate
			= this.efficiencyRate
			= this.castSpeedRate
			= 1.0f;
		this.weightDiscount = 0;
	}
	
	public void addXPRate(float diff) {
		this.xpRate += diff;
	}
	
	public void addCostRate(float diff) {
		this.costRate += diff;
	}
	
	public void addReagentCost(float diff) {
		this.reagentCost += diff;
	}
	
	public void addEfficiency(float diff) {
		this.efficiencyRate += diff;
	}
	
	public void addWeightDiscount(int diff) {
		this.weightDiscount += diff;
	}
	
	/**
	 * 
	 * @param diff negative numbers make it faster. -.2f is 20% reduced cast time
	 */
	public void addCastSpeedRate(float diff) {
		this.castSpeedRate += diff;
	}

	public float getBaseXP() {
		return baseXP;
	}

	public int getBaseCost() {
		return baseCost;
	}

	public float getReagentCost() {
		return reagentCost;
	}

	public float getXpRate() {
		return xpRate;
	}

	public float getCostRate() {
		return costRate;
	}
	
	public float getEfficiency() {
		return this.efficiencyRate;
	}
	
	public int getWeightDiscount() {
		return this.weightDiscount;
	}
	
	public float getCastSpeedRate() {
		return this.castSpeedRate;
	}
	
	public float getFinalXP() {
		return baseXP * xpRate;
	}
	
	public int getFinalCost() {
		return (int) ((float) baseCost * costRate);
	}
	
	public int getFinalCastTicks() {
		return (int) ((float) baseCastTicks * castSpeedRate);
	}
	
}
