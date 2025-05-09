package com.smanzana.nostrummagica.client.gui.mirror;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.client.gui.commonwidget.FixedWidget;

import net.minecraft.world.entity.player.Player;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;

public abstract class PanningMirrorSubscreen implements IMirrorSubscreen {
	
	private int panX;
	private int panY;
	private float scale;
	
	// Panning behavior vars
	private boolean mouseClicked; // Are we currently panning
	private double mouseClickX; // Where on the screen did we click?
	private double mouseClickY;
	private int mouseClickPanX; // Where were we panned when we clicked?
	private int mouseClickPanY;
	
	public PanningMirrorSubscreen() {
		panX = 0;
		panY = 0;
		scale = 1f;
	}
	
	protected void resetPan() {
		panX = 0;
		panY = 0;
		onPan(panX, panY, scale);
	}
	
	protected void resetScale() {
		scale = 1f;
		onZoom(panX, panY, scale);
	}
	
	protected int getPanX() {
		return panX;
	}
	
	protected int getPanY() {
		return panY;
	}
	
	protected float getPanScale() {
		return scale;
	}
	
	private void updatePan(double mouseX, double mouseY) {
		if (mouseClicked) {
			double diffX = mouseX - mouseClickX;
			double diffY = mouseY - mouseClickY;
			
			// Adjust to scale and add to pan
			panX = mouseClickPanX + (int) (diffX / scale);
			panY = mouseClickPanY + (int) (diffY / scale);
			onPan(panX, panY, scale);
		}
	}
	
	private void startPanning(double mouseX, double mouseY) {
		if (!mouseClicked) {
			mouseClicked = true;
			mouseClickX = mouseX;
			mouseClickY = mouseY;
			mouseClickPanX = panX;
			mouseClickPanY = panY;
		}
	}
	
	private void endPanning(double mouseX, double mouseY) {
		if (mouseClicked) {
			updatePan(mouseX, mouseY);
			mouseClicked = false;
		}
	}
	
	private void onPan(double mouseX, double mouseY) {
		if (mouseClicked) {
			updatePan(mouseX, mouseY);
		}
	}
	
	private void onZoom(double delta) {
		this.scale = (float) Math.max(.25f, Math.min(2f, scale + (delta * .25)));
		onZoom(panX, panY, scale);
	}
	
	protected abstract void onPan(int panX, int panY, float zoomScale);
	
	protected abstract void onZoom(int panX, int panY, float zoomScale);
	
	@Override
	public void show(IMirrorScreen parent, Player player, int width, int height, int guiLeft, int guiTop) {
		// Add a widget to capture mouse events as the last thing so buttons on top get clicked first.
		// AKA call super last!
		parent.addWidget(new PanWidget(this, guiLeft, guiTop, width, height));
	}
	
	private static final class PanWidget extends FixedWidget {
		
		private final PanningMirrorSubscreen subscreen;
		
		public PanWidget(PanningMirrorSubscreen subscreen, int x, int y, int width, int height) {
			super(x, y, width, height, TextComponent.EMPTY);
			this.subscreen = subscreen;
		}
		
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
			subscreen.startPanning(mouseX, mouseY);
			return true;
		}
		
		@Override
		public boolean mouseReleased(double mouseX, double mouseY, int button) {
			subscreen.endPanning(mouseX, mouseY);
			return true;
		}
		
		@Override
		public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double dx, double dy) {
			subscreen.onPan(mouseX, mouseY);
			return true;
		}
		
		@Override
		public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
			subscreen.onZoom(delta);
			return true;
		}
		
		@Override
		public void render(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			; // don't render anything
		}

		@Override
		public void updateNarration(NarrationElementOutput p_169152_) {
			super.defaultButtonNarrationText(p_169152_);
		}
	}

}
