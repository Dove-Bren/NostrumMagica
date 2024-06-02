package com.smanzana.nostrummagica.client.gui.container;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.widget.IMoveableWidget;
import com.smanzana.nostrummagica.client.gui.widget.ObscurableWidget;
import com.smanzana.nostrummagica.client.gui.widget.ParentWidget;
import com.smanzana.nostrummagica.client.gui.widget.ScrollbarWidget;
import com.smanzana.nostrummagica.client.gui.widget.ScrollbarWidget.IScrollbarListener;
import com.smanzana.nostrummagica.util.ColorUtil;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class SimpleInventoryWidget extends ParentWidget implements IScrollbarListener {
	
	public static class SimpleInventoryContainerlet {
		
		protected final int x;
		protected final int y;
		protected final int width;
		protected final int height;
		protected final ITextComponent title;
		protected final Rectangle2d invBounds;
		protected final List<HideableSlot> slots;
		protected final int spilloverRows;
		
		public SimpleInventoryContainerlet(Consumer<Slot> container, IInventory inventory, IHiddenSlotFactory factory, int x, int y, int width, int height) {
			this(container, inventory, factory, x, y, width, height, StringTextComponent.EMPTY);
		}
		
		public SimpleInventoryContainerlet(Consumer<Slot> container, IInventory inventory, IHiddenSlotFactory factory, int x, int y, int width, int height, ITextComponent title) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.title = title;
			invBounds = MakeBounds(inventory.getSizeInventory(), x, y, width, height);
			slots = new ArrayList<>(inventory.getSizeInventory());
			
			final int cellsPerRow = Math.max(1, invBounds.getWidth() / POS_SLOT_WIDTH);
			
			for (int i = 0; i < inventory.getSizeInventory(); i++) {
				final int slotX = invBounds.getX() + 1 + ((i%cellsPerRow) * POS_SLOT_WIDTH);
				final int slotY = invBounds.getY() + 1 + ((i/cellsPerRow) * POS_SLOT_WIDTH);
				HideableSlot slot = factory.apply(inventory, i, slotX, slotY);
				slots.add(slot);
				container.accept(slot);
			}
			
			this.spilloverRows = ((inventory.getSizeInventory() + cellsPerRow-1) / cellsPerRow)
					- (invBounds.getHeight() / POS_SLOT_WIDTH);
		}
	}

	public static interface IHiddenSlotFactory {
		public HideableSlot apply(IInventory inventory, int slotIdx, int x, int y);
	}
	
	protected static class SlotWidget extends ObscurableWidget implements IMoveableWidget {
		
		protected final HideableSlot slot;
		protected final int startX;
		protected final int startY;
		
		public SlotWidget(HideableSlot slot, int guiLeft, int guiTop) {
			super(guiLeft + slot.xPos - 1, guiTop + slot.yPos - 1, 18, 18, StringTextComponent.EMPTY);
			this.startX = this.x;
			this.startY = this.y;
			this.slot = slot;
		}
		
		@Override
		public void renderButton(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			Minecraft.getInstance().getTextureManager().bindTexture(TEXT);
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, this.x, this.y, TEX_SLOT_HOFFSET, TEX_SLOT_VOFFSET, TEX_SLOT_WIDTH, TEX_SLOT_HEIGHT, this.width, this.height, TEX_WIDTH, TEX_HEIGHT);
		}
		
		@Override
		public void setBounds(Rectangle2d bounds) {
			super.setBounds(bounds);
			this.slot.setHidden(!this.inBounds());
		}
		
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			return false;
		}

		@Override
		public void setPosition(int x, int y) {
			final int diffX = (x - this.x);
			final int diffY = (y - this.y);
			this.x = x;
			this.y = y;
			this.slot.xPos += diffX;
			this.slot.yPos += diffY;
			
			this.slot.setHidden(!this.inBounds());
		}

		@Override
		public void offset(int x, int y) {
			this.setPosition(this.x + x, this.y + y);
		}

		@Override
		public void resetPosition() {
			this.setPosition(startX, startY);
		}

		@Override
		public int getStartingX() {
			return startX;
		}

		@Override
		public int getStartingY() {
			return startY;
		}
	}
	
	private static final ResourceLocation TEXT = NostrumMagica.Loc("textures/gui/container/inventory_widget.png");
	
	private static final int TEX_WIDTH = 32;
	private static final int TEX_HEIGHT = 32;
	
	private static final int TEX_BORDER_TL_HOFFSET = 0;
	private static final int TEX_BORDER_TL_VOFFSET = 0;
	private static final int TEX_BORDER_TL_WIDTH = 4;
	private static final int TEX_BORDER_TL_HEIGHT = 4;
	
	private static final int TEX_BORDER_TR_HOFFSET = 5;
	private static final int TEX_BORDER_TR_VOFFSET = 0;
	private static final int TEX_BORDER_TR_WIDTH = 4;
	private static final int TEX_BORDER_TR_HEIGHT = 4;
	
	private static final int TEX_BORDER_BL_HOFFSET = 0;
	private static final int TEX_BORDER_BL_VOFFSET = 5;
	private static final int TEX_BORDER_BL_WIDTH = 4;
	private static final int TEX_BORDER_BL_HEIGHT = 4;
	
	private static final int TEX_BORDER_BR_HOFFSET = 5;
	private static final int TEX_BORDER_BR_VOFFSET = 5;
	private static final int TEX_BORDER_BR_WIDTH = 4;
	private static final int TEX_BORDER_BR_HEIGHT = 4;
	
	private static final int TEX_BORDER_TOP_HOFFSET = 4;
	private static final int TEX_BORDER_TOP_VOFFSET = 0;
	private static final int TEX_BORDER_TOP_WIDTH = 1;
	private static final int TEX_BORDER_TOP_HEIGHT = 4;
	
	private static final int TEX_BORDER_BOTTOM_HOFFSET = 4;
	private static final int TEX_BORDER_BOTTOM_VOFFSET = 5;
	private static final int TEX_BORDER_BOTTOM_WIDTH = 1;
	private static final int TEX_BORDER_BOTTOM_HEIGHT = 4;
	
	private static final int TEX_BORDER_LEFT_HOFFSET = 0;
	private static final int TEX_BORDER_LEFT_VOFFSET = 4;
	private static final int TEX_BORDER_LEFT_WIDTH = 4;
	private static final int TEX_BORDER_LEFT_HEIGHT = 1;
	
	private static final int TEX_BORDER_RIGHT_HOFFSET = 5;
	private static final int TEX_BORDER_RIGHT_VOFFSET = 4;
	private static final int TEX_BORDER_RIGHT_WIDTH = 4;
	private static final int TEX_BORDER_RIGHT_HEIGHT = 1;
	
	private static final int TEX_CENTER_HOFFSET = 4;
	private static final int TEX_CENTER_VOFFSET = 4;
	private static final int TEX_CENTER_WIDTH = 1;
	private static final int TEX_CENTER_HEIGHT = 1;
	
	private static final int TEX_SLOT_HOFFSET = 0;
	private static final int TEX_SLOT_VOFFSET = 9;
	private static final int TEX_SLOT_WIDTH = 18;
	private static final int TEX_SLOT_HEIGHT = 18;
	
	protected static final int POS_SLOTS_HMARGIN = 8;
	protected static final int POS_SLOTS_TOP_MARGIN = 18;
	protected static final int POS_SLOTS_BOTTOM_MARGIN = 18;
	
	protected static final int POS_SLOT_WIDTH = 18;
	
	protected static final int POS_SCROLLBAR_RMARGIN = 6;
	protected static final int POS_SCROLLBAR_WIDTH = 10;
	protected static final int POS_SCROLLBAR_TMARGIN = POS_SLOTS_TOP_MARGIN;
	protected static final int POS_SCROLLBAR_BMARGIN = POS_SLOTS_BOTTOM_MARGIN;
	
	protected final SimpleInventoryContainerlet containerlet;
	protected List<SlotWidget> slotWidgets;
	protected Rectangle2d guiBounds;
	protected float[] color = {1f, 1f, 1f, 1f};
	
	public SimpleInventoryWidget(ContainerScreen<? extends Container> gui, SimpleInventoryContainerlet containerlet) {
		super(containerlet.x + gui.getGuiLeft(), containerlet.y + gui.getGuiTop(), containerlet.width, containerlet.height, containerlet.title);
		this.containerlet = containerlet;
		this.slotWidgets = new ArrayList<>(containerlet.slots.size());
		
		init(containerlet, gui.getGuiLeft(), gui.getGuiTop());
	}
	
	protected static Rectangle2d MakeBounds(int slotCount, int x, int y, int width, int height) {
		// Calculate ideal bounds based on width height and position to start
		int hOffset = x + POS_SLOTS_HMARGIN;
		int vOffset = y + POS_SLOTS_TOP_MARGIN;
		int slotsWidth = width - (2 * POS_SLOTS_HMARGIN);
		int slotsHeight = height - (POS_SLOTS_TOP_MARGIN + POS_SLOTS_BOTTOM_MARGIN);
		
		// Figure out how many cells we can actually fit in our width
		int cellsPerRow = Math.max(1, slotsWidth / POS_SLOT_WIDTH);
		
		// See if that means we'll have a scrollbar or not
		if ( ((slotCount + cellsPerRow - 1) / cellsPerRow) * POS_SLOT_WIDTH > slotsHeight) {
			// Reduce width to make room for scrollbar
			slotsWidth -= POS_SCROLLBAR_WIDTH;
			cellsPerRow = Math.max(1, slotsWidth / POS_SLOT_WIDTH);
		}
		
		// Figure out if that leaves extra space on the side to divy up
		final int realTakenWidth = cellsPerRow * POS_SLOT_WIDTH;
		final int leftoverWidth = slotsWidth - realTakenWidth;
		
		// Shrink to match the smaller width
		if (leftoverWidth > 0) {
			hOffset += (leftoverWidth+1) / 2;
			slotsWidth -= leftoverWidth;
		}
		
		return new Rectangle2d(hOffset, vOffset, slotsWidth, slotsHeight);
	}
	
	protected void init(SimpleInventoryContainerlet containerlet, int guiLeft, int guiTop) {
		this.slotWidgets.clear();
		guiBounds = new Rectangle2d(containerlet.invBounds.getX() + guiLeft, containerlet.invBounds.getY() + guiTop, containerlet.invBounds.getWidth(), containerlet.invBounds.getHeight());
		for (HideableSlot slot : containerlet.slots) {
			SlotWidget widget = new SlotWidget(slot, guiLeft, guiTop);
			widget.setBounds(guiBounds);
			slotWidgets.add(widget);
			this.addChild(widget);
		}
		
		if (containerlet.spilloverRows > 0 ) {
			ScrollbarWidget scrollbar = new ScrollbarWidget(this,
					guiLeft + containerlet.x + containerlet.width - (POS_SCROLLBAR_WIDTH + POS_SCROLLBAR_RMARGIN),
					guiTop + containerlet.y + POS_SCROLLBAR_TMARGIN,
					POS_SCROLLBAR_WIDTH,
					containerlet.height - (POS_SCROLLBAR_TMARGIN + POS_SCROLLBAR_BMARGIN)
					);
			scrollbar.setScrollRate(1f/containerlet.spilloverRows);
			this.addChild(scrollbar);
		}
	}
	
	public void setColor(int color) {
		float[] colors = ColorUtil.ARGBToColor(color);
		setColor(colors[0], colors[1], colors[2], colors[3]);
	}
	
	public void setColor(float red, float green, float blue, float alpha) {
		this.color = new float[] {red, green, blue, alpha};
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	protected boolean isValidClickButton(int button) {
		return false;
	}
	
	@Override
	public void renderButton(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
		matrixStackIn.push();
		matrixStackIn.translate(this.x, this.y, 0);
		renderInventoryBackground(matrixStackIn, this.width, this.height, color[0], color[1], color[2], color[3]);
		matrixStackIn.pop();
	}
	
	@Override
	protected void renderForeground(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
		final Minecraft mc = Minecraft.getInstance();
		final FontRenderer fontRenderer = mc.fontRenderer;
		
		matrixStackIn.push();
		matrixStackIn.translate(this.x, this.y, 0);
		renderInventoryOverlay(matrixStackIn, this.width, this.height, guiBounds, color[0], color[1], color[2], color[3]);
		fontRenderer.func_243248_b(matrixStackIn, getMessage(), 8, 6, 4210752); // pos and color copied from ContainerScreen
		matrixStackIn.pop();
	}
	
	protected void renderInventoryBackground(MatrixStack matrixStackIn, int width, int height, float red, float green, float blue, float alpha) {
		Minecraft.getInstance().getTextureManager().bindTexture(TEXT);
		
		// Note: hardcoding border size to be 4
		RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0, TEX_BORDER_TL_HOFFSET, TEX_BORDER_TL_VOFFSET, TEX_BORDER_TL_WIDTH, TEX_BORDER_TL_HEIGHT, 4, 4, TEX_WIDTH, TEX_HEIGHT, red, green, blue, alpha);
		RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, width-4, 0, TEX_BORDER_TR_HOFFSET, TEX_BORDER_TR_VOFFSET, TEX_BORDER_TR_WIDTH, TEX_BORDER_TR_HEIGHT, 4, 4, TEX_WIDTH, TEX_HEIGHT, red, green, blue, alpha);
		RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, height-4, TEX_BORDER_BL_HOFFSET, TEX_BORDER_BL_VOFFSET, TEX_BORDER_BL_WIDTH, TEX_BORDER_BL_HEIGHT, 4, 4, TEX_WIDTH, TEX_HEIGHT, red, green, blue, alpha);
		RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, width-4, height-4, TEX_BORDER_BR_HOFFSET, TEX_BORDER_BR_VOFFSET, TEX_BORDER_BR_WIDTH, TEX_BORDER_BR_HEIGHT, 4, 4, TEX_WIDTH, TEX_HEIGHT, red, green, blue, alpha);
		
		RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 4, 0, TEX_BORDER_TOP_HOFFSET, TEX_BORDER_TOP_VOFFSET, TEX_BORDER_TOP_WIDTH, TEX_BORDER_TOP_HEIGHT, width-8, 4, TEX_WIDTH, TEX_HEIGHT, red, green, blue, alpha);
		RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 4, height-4, TEX_BORDER_BOTTOM_HOFFSET, TEX_BORDER_BOTTOM_VOFFSET, TEX_BORDER_BOTTOM_WIDTH, TEX_BORDER_BOTTOM_HEIGHT, width-8, 4, TEX_WIDTH, TEX_HEIGHT, red, green, blue, alpha);
		RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 4, TEX_BORDER_LEFT_HOFFSET, TEX_BORDER_LEFT_VOFFSET, TEX_BORDER_LEFT_WIDTH, TEX_BORDER_LEFT_HEIGHT, 4, height-8, TEX_WIDTH, TEX_HEIGHT, red, green, blue, alpha);
		RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, width-4, 4, TEX_BORDER_RIGHT_HOFFSET, TEX_BORDER_RIGHT_VOFFSET, TEX_BORDER_RIGHT_WIDTH, TEX_BORDER_RIGHT_HEIGHT, 4, height-8, TEX_WIDTH, TEX_HEIGHT, red, green, blue, alpha);
		
		RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 4, 4, TEX_CENTER_HOFFSET, TEX_CENTER_VOFFSET, TEX_CENTER_WIDTH, TEX_CENTER_HEIGHT, width-8, height-8, TEX_WIDTH, TEX_HEIGHT, red, green, blue, alpha);
	}
	
	protected void renderInventoryOverlay(MatrixStack matrixStackIn, int width, int height, Rectangle2d hollow, float red, float green, float blue, float alpha) {
		Minecraft.getInstance().getTextureManager().bindTexture(TEXT);
		
//		final int innerOffset = 4;
//		final int topY = hollow.getY() - 1;
//		final int bottomY = hollow.getY() + hollow.getHeight();
//		
//		// Two rectangles.
//		// Top one from 0,0 to width,topY
//		// Bottom one from 0,bottomY to width,height
//		// Except where there are 0's are the inner offsets and where width/height are are that - innerOffset...
//		matrixStackIn.push();
//		matrixStackIn.translate(innerOffset, innerOffset, 0);
//		width -= 2 * innerOffset;
//		height -= 2 * innerOffset;
//		
////		RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0, TEX_CENTER_HOFFSET, TEX_CENTER_VOFFSET, TEX_CENTER_WIDTH, TEX_CENTER_HEIGHT,
////				width, topY, TEX_WIDTH, TEX_HEIGHT, red, green, blue, alpha);
////		RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, bottomY, TEX_CENTER_HOFFSET, TEX_CENTER_VOFFSET, TEX_CENTER_WIDTH, TEX_CENTER_HEIGHT,
////				width - 0, height - bottomY, TEX_WIDTH, TEX_HEIGHT, red, green, blue, alpha);
//		
//		matrixStackIn.pop();
	}

	@Override
	public void handleScroll(float scroll) {
		if (this.containerlet.spilloverRows > 0) {
			final int yOffset = (int) Math.round(this.containerlet.spilloverRows * scroll) * POS_SLOT_WIDTH;
			
			for (SlotWidget widget : this.slotWidgets) {
				widget.setPosition(widget.getStartingX(), widget.getStartingY() - yOffset);
			}
		}
	}
}
