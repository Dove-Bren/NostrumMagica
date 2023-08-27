package com.smanzana.nostrummagica.client.gui.infoscreen;

import java.util.List;

import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraftforge.fml.client.config.GuiUtils;

public abstract class InfoButton extends AbstractButton {

	protected static final int BUTTON_WIDTH = 18;
	
	protected final InfoScreen screen;
	
	public InfoButton(InfoScreen screen, int x, int y) {
		super(x, y, BUTTON_WIDTH, BUTTON_WIDTH, "");
		this.screen = screen;
	}

	public abstract IInfoSubScreen getScreen(INostrumMagic attr);
	
	public abstract List<String> getDescription();
	
	@Override
	public void renderButton(int mouseX, int mouseY, float partialTicks) {
		super.renderButton(mouseX, mouseY, partialTicks);
		
		if (mouseX >= this.x && mouseY > this.y
			&& mouseX <= this.x + this.width
			&& mouseY <= this.y + this.height) {
			Minecraft mc = Minecraft.getInstance();
			GuiUtils.drawHoveringText(getDescription(),
					mouseX,
					mouseY,
					mc.mainWindow.getWidth(),
					mc.mainWindow.getHeight(),
					100,
					mc.fontRenderer);
		}
	}
	
	@Override
	public void onPress() {
		screen.selectScreen(this);
	}
}
