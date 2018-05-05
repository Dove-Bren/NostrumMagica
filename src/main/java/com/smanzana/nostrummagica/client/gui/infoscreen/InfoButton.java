package com.smanzana.nostrummagica.client.gui.infoscreen;

import java.util.List;

import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.client.config.GuiUtils;

public abstract class InfoButton extends GuiButton {

	protected static final int BUTTON_WIDTH = 18;
	
	public InfoButton(int buttonId, int x, int y) {
		super(buttonId, x, y, BUTTON_WIDTH, BUTTON_WIDTH, "");
	}

	public abstract IInfoSubScreen getScreen(INostrumMagic attr);
	
	public abstract List<String> getDescription();
	
	@Override
	public void drawButtonForegroundLayer(int mouseX, int mouseY) {
		if (mouseX >= this.xPosition && mouseY > this.yPosition
			&& mouseX <= this.xPosition + this.width
			&& mouseY <= this.yPosition + this.height) {
			Minecraft mc = Minecraft.getMinecraft();
			GuiUtils.drawHoveringText(getDescription(),
					mouseX,
					mouseY,
					mc.displayWidth,
					mc.displayHeight,
					100,
					mc.fontRendererObj);
		}
	}
}
