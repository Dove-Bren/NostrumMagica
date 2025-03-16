package com.smanzana.nostrummagica.client.gui.widget;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

public abstract class ObscurableWidget extends FixedWidget {

	protected @Nullable Rect2i bounds;
	protected boolean hidden;
	
	public ObscurableWidget(int x, int y, int width, int height, Component label) {
		super(x, y, width, height, label);
		bounds = null;
		hidden = false;
	}
	
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	
	public boolean isHidden() {
		return this.hidden;
	}
	
	public void setBounds(Rect2i bounds) {
		this.bounds = bounds;
	}
	
	public void setBounds(int x, int y, int width, int height) {
		setBounds(new Rect2i(x, y, width, height));
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	protected boolean inBounds() {
		if (this.bounds != null) {
			final int maxX = this.x + this.width;
			final int maxY = this.y + this.height;
			final int boundsMaxX = bounds.getX() + bounds.getWidth();
			final int boundsMaxY = bounds.getY() + bounds.getHeight();
			return x < boundsMaxX
					&& maxX > bounds.getX()
					&& y < boundsMaxY
					&& maxY > bounds.getY();
		}
		
		return true;
	}
	
	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		// Check bounds
		this.visible = !isHidden() && inBounds();
		super.render(matrixStack, mouseX, mouseY, partialTicks);
	}
	
}
