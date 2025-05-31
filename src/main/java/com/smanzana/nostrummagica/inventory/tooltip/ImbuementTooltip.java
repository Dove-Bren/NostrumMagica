package com.smanzana.nostrummagica.inventory.tooltip;

import com.smanzana.nostrummagica.spell.ItemImbuement;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class ImbuementTooltip implements TooltipComponent {
	
	public final ItemImbuement imbuement;
	
	public ImbuementTooltip(ItemImbuement imbuement) {
		this.imbuement = imbuement;
	}
}
