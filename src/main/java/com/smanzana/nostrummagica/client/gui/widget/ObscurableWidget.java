package com.smanzana.nostrummagica.client.gui.widget;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.text.ITextComponent;

public abstract class ObscurableWidget extends FixedWidget {

	protected @Nullable Rectangle2d bounds;
	protected boolean hidden;
	
	public ObscurableWidget(int x, int y, int width, int height, ITextComponent label) {
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
	
	public void setBounds(Rectangle2d bounds) {
		this.bounds = bounds;
	}
	
	public void setBounds(int x, int y, int width, int height) {
		setBounds(new Rectangle2d(x, y, width, height));
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
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		// Check bounds
		this.visible = !isHidden() && inBounds();
		super.render(matrixStack, mouseX, mouseY, partialTicks);
	}
	
}
