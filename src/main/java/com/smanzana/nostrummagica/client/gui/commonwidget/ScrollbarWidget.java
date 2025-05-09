package com.smanzana.nostrummagica.client.gui.commonwidget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

public class ScrollbarWidget extends ObscurableChildWidget<ScrollbarWidget> {
	
	public static interface IScrollbarListener {
		/**
		 * Called when the scrollbar has changed the scroll level.
		 * @param scroll How far we are scrolled, from 0 (not scrolled) to 1 (fully scrolled)
		 */
		public void handleScroll(float scroll);
	}
	
	protected final IScrollbarListener listener;
	
	protected float scrollRate;
	
	protected boolean pressed;
	protected float scroll;
	
	public ScrollbarWidget(IScrollbarListener listener, int x, int y, int width, int height) {
		this(listener, x, y, width, height, TextComponent.EMPTY);
	}
	
	public ScrollbarWidget(IScrollbarListener listener, int x, int y, int width, int height, Component title) {
		super(x, y, width, height, title);
		this.listener = listener;
		this.pressed = false;
		this.scroll = 0f;
		this.scrollRate = .125f;
	}
	
	public void setEnabled(boolean enabled) {
		this.active = enabled;
	}
	
	public void setScrollRate(float rate) {
		this.scrollRate = rate;
		this.scroll = 0f;
	}
	
	protected int getYForScroll(float scroll) {
		final int yMargin = 2;
		final int scrollRange = this.height - ((yMargin * 2) + (POS_SCROLLBAR_HEIGHT));
		return (int) (scroll * scrollRange);
	}
	
	protected float getScrollForY(double mouseY) {
		final int yMargin = 2;
		final int scrollRange = this.height - ((yMargin * 2) + (POS_SCROLLBAR_HEIGHT));
		mouseY = mouseY - (y + yMargin + POS_SCROLLBAR_HEIGHT/2);
		return Math.max(0, Math.min(1f, (float) mouseY / (float) scrollRange));
	}
	
	protected void updateScroll(double mouseY) {
		this.scroll = getScrollForY(mouseY);
		this.listener.handleScroll(scroll);
	}
	
	@Override
	public void onClick(double mouseX, double mouseY) {
		if (this.active) {
			this.pressed = true;
		}
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int state) {
		if (this.pressed) {
			this.pressed = false;
			updateScroll(mouseY);
			return true;
		}
		
		return super.mouseReleased(mouseX, mouseY, state);
	}
	
	// This even doesn't fire under normal circumstances
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double dx, double dy) {
		if (pressed) {
			updateScroll(mouseY);
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, clickedMouseButton, dx, dy);
	}
	
	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		if (pressed) {
			updateScroll(mouseY);
		}
		super.mouseMoved(mouseX, mouseY);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double dx) {
		if (!active) {
			return super.mouseScrolled(mouseX, mouseY, dx);
		}
		
		final float amt = (float) -dx * scrollRate;
		this.scroll = Math.min(1f, Math.max(0, scroll + amt));
		this.listener.handleScroll(scroll);
		
		return true;
	}
	
	private static final ResourceLocation TEXT = new ResourceLocation(NostrumMagica.MODID, "textures/gui/misc_widget.png");
	
	private static final int TEX_WIDTH = 64;
	private static final int TEX_HEIGHT = 64;
	
	private static final int TEX_SCROLLBAR_HOFFSET = 0;
	private static final int TEX_SCROLLBAR_VOFFSET = 0;
	private static final int TEX_SCROLLBAR_WIDTH = 6;
	private static final int TEX_SCROLLBAR_HEIGHT = 14;
	
	private static final int POS_SCROLLBAR_WIDTH = 6;
	private static final int POS_SCROLLBAR_HEIGHT = 14;

	@Override
	public void renderButton(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
		final int xMargin = 2;
		final int yMargin = 2;
		matrixStackIn.pushPose();
		matrixStackIn.translate(x, y, 0);
		
		// Draw track
		this.drawTrack(matrixStackIn, this.width, this.height);
		
		// Draw scrollbar
		final int barPos;
		if (this.pressed) {
			// Snap to mouse
			int barPosRaw = Math.max(yMargin + (POS_SCROLLBAR_HEIGHT/2),
					Math.min(this.height - (yMargin + (POS_SCROLLBAR_HEIGHT/2)),
							mouseY - y));
			// Adjust so that the bar is in the middle
			barPos = barPosRaw - (POS_SCROLLBAR_HEIGHT/2);
		} else {
			// base on scroll
			barPos = yMargin + getYForScroll(this.scroll);
		}
		
		if (this.active) {
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		} else {
			RenderSystem.setShaderColor(.2f, .2f, .2f, 1f);
		}
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(xMargin, barPos, 0);
		this.drawScrollbar(matrixStackIn, POS_SCROLLBAR_WIDTH, POS_SCROLLBAR_HEIGHT);
		matrixStackIn.popPose();
		
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		
		matrixStackIn.popPose();
	}
	
	protected void drawScrollbar(PoseStack matrixStackIn, int width, int height) {
		RenderSystem.setShaderTexture(0, TEXT);
		blit(matrixStackIn, 0, 0, width, height, TEX_SCROLLBAR_HOFFSET, TEX_SCROLLBAR_VOFFSET, TEX_SCROLLBAR_WIDTH, TEX_SCROLLBAR_HEIGHT, TEX_WIDTH, TEX_HEIGHT);
	}
	
	protected void drawTrack(PoseStack matrixStackIn, int width, int height) {
		GuiComponent.fill(matrixStackIn, 0, 0, width, height, 0xFF000000);
	}

	@Override
	public void updateNarration(NarrationElementOutput p_169152_) {
		// TODO Auto-generated method stub
		
	}

}
