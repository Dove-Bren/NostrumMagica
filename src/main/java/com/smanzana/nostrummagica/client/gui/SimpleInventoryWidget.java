package com.smanzana.nostrummagica.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class SimpleInventoryWidget extends Widget {
	
	public static class SimpleInventoryContainerlet {
		
		protected final int x;
		protected final int y;
		protected final int width;
		protected final int height;
		protected final Rectangle2d invBounds;
		protected final List<Slot> slots;
		
		public SimpleInventoryContainerlet(Consumer<Slot> container, IInventory inventory, ISlotFactory factory, int x, int y, int width, int height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			invBounds = MakeBounds(inventory.getSizeInventory(), x, y, width, height);
			slots = new ArrayList<>(inventory.getSizeInventory());
			
			final int cellsPerRow = Math.max(1, invBounds.getWidth() / POS_SLOT_WIDTH);
			
			for (int i = 0; i < inventory.getSizeInventory(); i++) {
				final int slotX = invBounds.getX() + 1 + ((i%cellsPerRow) * POS_SLOT_WIDTH);
				final int slotY = invBounds.getY() + 1 + ((i/cellsPerRow) * POS_SLOT_WIDTH);
				Slot slot = factory.apply(inventory, i, slotX, slotY);
				slots.add(slot);
				container.accept(slot);
			}
		}
	}

	public static interface ISlotFactory {
		public Slot apply(IInventory inventory, int slotIdx, int x, int y);
	}
	
	protected static class SlotWidget extends ObscurableWidget {
		protected Slot slot;
		
		public SlotWidget(Slot slot, int guiLeft, int guiTop) {
			super(guiLeft + slot.xPos - 1, guiTop + slot.yPos - 1, 18, 18, StringTextComponent.EMPTY);
		}
		
		@Override
		public void renderButton(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			Minecraft.getInstance().getTextureManager().bindTexture(TEXT);
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, this.x, this.y, TEX_SLOT_HOFFSET, TEX_SLOT_VOFFSET, TEX_SLOT_WIDTH, TEX_SLOT_HEIGHT, this.width, this.height, TEX_WIDTH, TEX_HEIGHT);
		}
		
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			return false;
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
	protected static final int POS_SLOTS_BOTTOM_MARGIN = POS_SLOTS_HMARGIN;
	
	protected static final int POS_SLOT_WIDTH = 18;
	
	protected List<SlotWidget> slotWidgets;
	
	public SimpleInventoryWidget(ContainerScreen<? extends Container> gui, SimpleInventoryContainerlet containerlet, ITextComponent name) {
		super(containerlet.x + gui.getGuiLeft(), containerlet.y + gui.getGuiTop(), containerlet.width, containerlet.height, name);
		this.slotWidgets = new ArrayList<>(containerlet.slots.size());
		
		init(containerlet, gui.getGuiLeft(), gui.getGuiTop());
	}
	
	protected static Rectangle2d MakeBounds(int slotCount, int x, int y, int width, int height) {
		// Calculate ideal bounds based on width height and position
		int hOffset = x + POS_SLOTS_HMARGIN;
		int vOffset = y + POS_SLOTS_TOP_MARGIN;
		int slotsWidth = width - (2 * POS_SLOTS_HMARGIN);
		int slotsHeight = height - (POS_SLOTS_TOP_MARGIN + POS_SLOTS_BOTTOM_MARGIN);
		
		// Figure out how many cells we can actually fit in our width
		final int cellsPerRow = Math.max(1, slotsWidth / POS_SLOT_WIDTH);
		
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
		final Rectangle2d guiBounds = new Rectangle2d(containerlet.invBounds.getX() + guiLeft, containerlet.invBounds.getY() + guiTop, containerlet.invBounds.getWidth(), containerlet.invBounds.getHeight());
		for (Slot slot : containerlet.slots) {
			SlotWidget widget = new SlotWidget(slot, guiLeft, guiTop);
			widget.setBounds(guiBounds);
			slotWidgets.add(widget);
		}
	}
	
	@Override
	public void renderButton(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
		final Minecraft mc = Minecraft.getInstance();
		final FontRenderer fontRenderer = mc.fontRenderer;
		
		matrixStackIn.push();
		matrixStackIn.translate(this.x, this.y, 0);
		renderInventoryBackground(matrixStackIn, this.width, this.height, 1f, 1f, 1f, 1f);
		fontRenderer.func_243248_b(matrixStackIn, getMessage(), 8, 6, 4210752); // pos and color copied from ContainerScreen
		matrixStackIn.pop();
		
		// Scrollwheel?
		
		for (SlotWidget widget : slotWidgets) {
			widget.render(matrixStackIn, mouseX, mouseY, partialTicks);
		}
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
	
}
