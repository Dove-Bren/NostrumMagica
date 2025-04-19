package com.smanzana.nostrummagica.client.gui.tooltip;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraftforge.client.event.RenderTooltipEvent;

/**
 * ClientTooltipComponent that wants to render on top of the tooltip instead of as a regular component, and therefore
 * needs information about the tooltip being rendered.
 */
public abstract class AbsoluteTooltipComponent implements ClientTooltipComponent {

	protected int mouseX;
	protected int mouseY;
	protected int tooltipWidth;
	protected int tooltipHeight;
	
	public AbsoluteTooltipComponent() {
		
	}
	
	protected void setTooltipDimensions(int mouseX, int mouseY, int tooltipWidth, int tooltipHeight) {
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.tooltipWidth = tooltipWidth;
		this.tooltipHeight = tooltipHeight;
	}
	
	public static final void CaptureTooltipDimensions(RenderTooltipEvent.Color event) {
		// Capturing this in this event came from looking at Botania code which similarly wants to paint on the tooltip and
		// wants to know the dimensions of the tooltip itself, which the render methods do not provide.
		int tooltipWidth = 0;
		int tooltipHeight = event.getComponents().size() == 1 ? -2 : 0; // copied from renderTooltipInternal
		List<AbsoluteTooltipComponent> listeners = new ArrayList<>(1);
		for (ClientTooltipComponent comp : event.getComponents()) {
			tooltipWidth = Math.max(tooltipWidth, comp.getWidth(event.getFont()));
			tooltipHeight += comp.getHeight();
			if (comp instanceof AbsoluteTooltipComponent tooltip) {
				listeners.add(tooltip);
			}
		}
		
		for (AbsoluteTooltipComponent tooltip : listeners) {
			tooltip.setTooltipDimensions(event.getX(), event.getY(), tooltipWidth, tooltipHeight);
		}
	}
	
}
