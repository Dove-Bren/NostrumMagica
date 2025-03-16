package com.smanzana.nostrummagica.client.gui.container;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.smanzana.nostrummagica.client.gui.container.SimpleInventoryWidget.IHiddenSlotFactory;
import com.smanzana.nostrummagica.util.Rectangle;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class SimpleInventoryContainerlet {
	
	protected static final int POS_SLOTS_HMARGIN = 8;
	protected static final int POS_SLOTS_TOP_MARGIN = 18;
	protected static final int POS_SLOTS_BOTTOM_MARGIN = 18;
	
	protected static final int POS_SLOT_WIDTH = 18;
	
	protected static final int POS_SCROLLBAR_RMARGIN = 6;
	protected static final int POS_SCROLLBAR_WIDTH = 10;
	protected static final int POS_SCROLLBAR_TMARGIN = POS_SLOTS_TOP_MARGIN;
	protected static final int POS_SCROLLBAR_BMARGIN = POS_SLOTS_BOTTOM_MARGIN;
	
	protected final int x;
	protected final int y;
	protected final int width;
	protected final int height;
	protected final Component title;
	protected final Rectangle invBounds;
	protected final List<HideableSlot> slots;
	protected final int spilloverRows;
	
	public SimpleInventoryContainerlet(Consumer<Slot> container, Container inventory, IHiddenSlotFactory factory, int x, int y, int width, int height) {
		this(container, inventory, factory, x, y, width, height, TextComponent.EMPTY);
	}
	
	public SimpleInventoryContainerlet(Consumer<Slot> container, Container inventory, IHiddenSlotFactory factory, int x, int y, int width, int height, Component title) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.title = title;
		invBounds = MakeBounds(inventory.getContainerSize(), x, y, width, height);
		slots = new ArrayList<>(inventory.getContainerSize());
		
		final int cellsPerRow = Math.max(1, invBounds.getWidth() / SimpleInventoryContainerlet.POS_SLOT_WIDTH);
		
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			final int slotX = invBounds.getX() + 1 + ((i%cellsPerRow) * SimpleInventoryContainerlet.POS_SLOT_WIDTH);
			final int slotY = invBounds.getY() + 1 + ((i/cellsPerRow) * SimpleInventoryContainerlet.POS_SLOT_WIDTH);
			HideableSlot slot = factory.apply(inventory, i, slotX, slotY);
			slots.add(slot);
			container.accept(slot);
		}
		
		this.spilloverRows = ((inventory.getContainerSize() + cellsPerRow-1) / cellsPerRow)
				- (invBounds.getHeight() / SimpleInventoryContainerlet.POS_SLOT_WIDTH);
	}
	
	protected static Rectangle MakeBounds(int slotCount, int x, int y, int width, int height) {
		// Calculate ideal bounds based on width height and position to start
		int hOffset = x + POS_SLOTS_HMARGIN;
		int vOffset = y + POS_SLOTS_TOP_MARGIN;
		int slotsWidth = width - (2 * POS_SLOTS_HMARGIN);
		int slotsHeight = height - (POS_SLOTS_TOP_MARGIN + POS_SLOTS_BOTTOM_MARGIN);
		
		// Figure out how many cells we can actually fit in our width
		int cellsPerRow = Math.max(1, slotsWidth / SimpleInventoryContainerlet.POS_SLOT_WIDTH);
		
		// See if that means we'll have a scrollbar or not
		if ( ((slotCount + cellsPerRow - 1) / cellsPerRow) * SimpleInventoryContainerlet.POS_SLOT_WIDTH > slotsHeight) {
			// Reduce width to make room for scrollbar
			slotsWidth -= POS_SCROLLBAR_WIDTH;
			cellsPerRow = Math.max(1, slotsWidth / SimpleInventoryContainerlet.POS_SLOT_WIDTH);
		}
		
		// Figure out if that leaves extra space on the side to divy up
		final int realTakenWidth = cellsPerRow * SimpleInventoryContainerlet.POS_SLOT_WIDTH;
		final int leftoverWidth = slotsWidth - realTakenWidth;
		
		// Shrink to match the smaller width
		if (leftoverWidth > 0) {
			hOffset += (leftoverWidth+1) / 2;
			slotsWidth -= leftoverWidth;
		}
		
		return new Rectangle(hOffset, vOffset, slotsWidth, slotsHeight);
	}
}