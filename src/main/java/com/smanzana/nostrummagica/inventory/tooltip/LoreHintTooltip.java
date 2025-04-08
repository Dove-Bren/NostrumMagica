package com.smanzana.nostrummagica.inventory.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class LoreHintTooltip implements TooltipComponent {
	
	public static enum LoreLevel {
		NONE,
		BASIC,
		FULL;
	}
	
	public final LoreLevel level;
	
	public LoreHintTooltip(LoreLevel level) {
		this.level = level;
	}
}
