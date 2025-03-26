package com.smanzana.nostrummagica.client.gui.container;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.widget.IMoveableWidget;
import com.smanzana.nostrummagica.client.gui.widget.ObscurableWidget;
import com.smanzana.nostrummagica.client.gui.widget.ParentWidget;
import com.smanzana.nostrummagica.client.gui.widget.ScrollbarWidget;
import com.smanzana.nostrummagica.client.gui.widget.ScrollbarWidget.IScrollbarListener;
import com.smanzana.nostrummagica.util.ColorUtil;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class SimpleInventoryWidget extends ParentWidget implements IScrollbarListener {
	
	public static interface IHiddenSlotFactory {
		public HideableSlot apply(Container inventory, int slotIdx, int x, int y);
	}
	
	protected static class SlotWidget extends ObscurableWidget implements IMoveableWidget {
		
		protected final HideableSlot slot;
		protected final int startX;
		protected final int startY;
		
		public SlotWidget(HideableSlot slot, int guiLeft, int guiTop) {
			super(guiLeft + slot.x - 1, guiTop + slot.y - 1, 18, 18, TextComponent.EMPTY);
			this.startX = this.x;
			this.startY = this.y;
			this.slot = slot;
		}
		
		@Override
		public void renderButton(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			RenderSystem.setShaderTexture(0, TEXT);
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, this.x, this.y, TEX_SLOT_HOFFSET, TEX_SLOT_VOFFSET, TEX_SLOT_WIDTH, TEX_SLOT_HEIGHT, this.width, this.height, TEX_WIDTH, TEX_HEIGHT);
		}
		
		@Override
		public void setBounds(Rect2i bounds) {
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
			this.slot.x += diffX;
			this.slot.y += diffY;
			
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

		@Override
		public void updateNarration(NarrationElementOutput p_169152_) {
			// TODO Auto-generated method stub
			
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
	
	protected final SimpleInventoryContainerlet containerlet;
	protected List<SlotWidget> slotWidgets;
	protected Rect2i guiBounds;
	protected float[] color = {1f, 1f, 1f, 1f};
	
	public SimpleInventoryWidget(AbstractContainerScreen<? extends AbstractContainerMenu> gui, SimpleInventoryContainerlet containerlet) {
		super(containerlet.x + gui.getGuiLeft(), containerlet.y + gui.getGuiTop(), containerlet.width, containerlet.height, containerlet.title);
		this.containerlet = containerlet;
		this.slotWidgets = new ArrayList<>(containerlet.slots.size());
		
		init(containerlet, gui.getGuiLeft(), gui.getGuiTop());
	}
	
	protected void init(SimpleInventoryContainerlet containerlet, int guiLeft, int guiTop) {
		this.slotWidgets.clear();
		guiBounds = new Rect2i(containerlet.invBounds.getX() + guiLeft, containerlet.invBounds.getY() + guiTop, containerlet.invBounds.getWidth(), containerlet.invBounds.getHeight());
		for (HideableSlot slot : containerlet.slots) {
			SlotWidget widget = new SlotWidget(slot, guiLeft, guiTop);
			widget.setBounds(guiBounds);
			slotWidgets.add(widget);
			this.addChild(widget);
		}
		
		if (containerlet.spilloverRows > 0 ) {
			ScrollbarWidget scrollbar = new ScrollbarWidget(this,
					guiLeft + containerlet.x + containerlet.width - (SimpleInventoryContainerlet.POS_SCROLLBAR_WIDTH + SimpleInventoryContainerlet.POS_SCROLLBAR_RMARGIN),
					guiTop + containerlet.y + SimpleInventoryContainerlet.POS_SCROLLBAR_TMARGIN,
					SimpleInventoryContainerlet.POS_SCROLLBAR_WIDTH,
					containerlet.height - (SimpleInventoryContainerlet.POS_SCROLLBAR_TMARGIN + SimpleInventoryContainerlet.POS_SCROLLBAR_BMARGIN)
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
	public void renderButton(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
		matrixStackIn.pushPose();
		matrixStackIn.translate(this.x, this.y, 0);
		renderInventoryBackground(matrixStackIn, this.width, this.height, color[0], color[1], color[2], color[3]);
		matrixStackIn.popPose();
	}
	
	@Override
	protected void renderForeground(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
		final Minecraft mc = Minecraft.getInstance();
		final Font fontRenderer = mc.font;
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(this.x, this.y, 0);
		renderInventoryOverlay(matrixStackIn, this.width, this.height, guiBounds, color[0], color[1], color[2], color[3]);
		fontRenderer.draw(matrixStackIn, getMessage(), 8, 6, 4210752); // pos and color copied from ContainerScreen
		matrixStackIn.popPose();
	}
	
	protected void renderInventoryBackground(PoseStack matrixStackIn, int width, int height, float red, float green, float blue, float alpha) {
		RenderSystem.setShaderTexture(0, TEXT);
		
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
	
	protected void renderInventoryOverlay(PoseStack matrixStackIn, int width, int height, Rect2i hollow, float red, float green, float blue, float alpha) {
		RenderSystem.setShaderTexture(0, TEXT);
		
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
			final int yOffset = (int) Math.round(this.containerlet.spilloverRows * scroll) * SimpleInventoryContainerlet.POS_SLOT_WIDTH;
			
			for (SlotWidget widget : this.slotWidgets) {
				widget.setPosition(widget.getStartingX(), widget.getStartingY() - yOffset);
			}
		}
	}
}
