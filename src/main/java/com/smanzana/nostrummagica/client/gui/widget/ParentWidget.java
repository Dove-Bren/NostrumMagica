package com.smanzana.nostrummagica.client.gui.widget;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.text.ITextComponent;

public abstract class ParentWidget extends MoveableObscurableWidget {
	
	protected final List<Widget> children;
	
	public ParentWidget(int x, int y, int width, int height, ITextComponent title) {
		super(x, y, width, height, title);
		children = new ArrayList<>();
	}
	
	public void addChild(Widget child) {
		children.add(child);
	}
	
	protected void clearChildren() {
		children.clear();
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		if (this.visible) {
			for (Widget widget : children) {
				widget.render(matrixStack, mouseX, mouseY, partialTicks);
			}
		}
		renderForeground(matrixStack, mouseX, mouseY, partialTicks);
	}
	
	/**
	 * Render foreground media. Notably, this is called after children have been rendered.
	 * @param matrixStackIn
	 * @param mouseX
	 * @param mouseY
	 * @param partialTicks
	 */
	protected void renderForeground(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
		
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		// Try children first since they're probably on TOP of the widget
		for (Widget widget : children) {
			if (widget.mouseClicked(mouseX, mouseY, button)) {
				return true;
			}
		}
		
		//return super.mouseClicked(mouseX, mouseY, button);
		return false;
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		// Try children first since they're probably on TOP of the widget
		for (Widget widget : children) {
			if (widget.mouseReleased(mouseX, mouseY, button)) {
				// Vanilla is dumb and has 'am I under it' checks in mouse click but not for mouse released. That means
				// things blindly return 'true' to whether they've handled it or not.
				// So instead of caring, call it on every child and ignore return.
				; //return true;
			}
		}
		
		//return super.mouseReleased(mouseX, mouseY, button);
		return false;
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		// Try children first since they're probably on TOP of the widget
		for (Widget widget : children) {
			if (widget.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
				// Like 'mouseReleased', vanilla classes blindly 'handle' thi s. So do all children even if one returns true.
				//return true;
			}
		}
		
		//return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
		return false;
	}
	
	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		// Try children first since they're probably on TOP of the widget
		for (Widget widget : children) {
			widget.mouseMoved(mouseX, mouseY);
		}
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		// Try children first since they're probably on TOP of the widget
		for (Widget widget : children) {
			if (widget.mouseScrolled(mouseX, mouseY, delta)) {
				return true;
			}
		}
		
		//return super.mouseScrolled(mouseX, mouseY, delta);
		return false;
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		// Try children first since they're probably on TOP of the widget
		for (Widget widget : children) {
			if (widget.keyPressed(keyCode, scanCode, modifiers)) {
				return true;
			}
		}
		
		//return super.keyPressed(keyCode, scanCode, modifiers);
		return false;
	}
	
	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		// Try children first since they're probably on TOP of the widget
		for (Widget widget : children) {
			if (widget.keyReleased(keyCode, scanCode, modifiers)) {
				return true;
			}
		}
		
		//return super.keyReleased(keyCode, scanCode, modifiers);
		return false;
	}
	
	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		// Try children first since they're probably on TOP of the widget
		for (Widget widget : children) {
			if (widget.charTyped(codePoint, modifiers)) {
				return true;
			}
		}
		
		//return super.charTyped(codePoint, modifiers);
		return false;
	}
	
	@Override
	public void setPosition(int x, int y) {
		super.setPosition(x, y);
		
		final int xDiff = x - this.getStartingX();
		final int yDiff = y - this.getStartingY();
		
		final Rectangle2d bounds = new Rectangle2d(x, y, this.width, this.height);
		
		for (Widget widget : children) {
			if (widget instanceof ObscurableWidget) {
				((ObscurableWidget) widget).setBounds(bounds);
			}
			if (widget instanceof IMoveableWidget) {
				((IMoveableWidget) widget).offsetFromStart(xDiff, yDiff);
			}
		}
	}

}
