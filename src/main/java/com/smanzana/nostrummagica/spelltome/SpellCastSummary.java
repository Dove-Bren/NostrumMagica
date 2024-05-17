package com.smanzana.nostrummagica.spelltome;

public class SpellCastSummary {

	private final float baseXP;
	private final int baseCost;
	
	private float reagentCost;
	private float xpRate;
	private float costRate;
	private float efficiencyRate;
	private int weightDiscount;
	
	public SpellCastSummary(int cost, float xp) {
		this.baseCost = cost;
		this.baseXP = xp;
		this.reagentCost
			= this.xpRate
			= this.costRate
			= this.efficiencyRate
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
	
	public float getFinalXP() {
		return baseXP * xpRate;
	}
	
	public int getFinalCost() {
		return (int) ((float) baseCost * costRate);
	}
	
}
