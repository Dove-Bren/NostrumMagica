package com.smanzana.nostrummagica.spelltome.enhancement;

public class SpellTomeEnhancementWrapper {
	protected SpellTomeEnhancement enhancement;
	protected int level;
	
	public SpellTomeEnhancementWrapper(SpellTomeEnhancement enhancement, int level) {
		this.enhancement = enhancement;
		this.level = level;
	}

	public SpellTomeEnhancement getEnhancement() {
		return enhancement;
	}

	public int getLevel() {
		return level;
	}
}
