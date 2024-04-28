package com.smanzana.nostrummagica.client.gui.infoscreen;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class SubscreenInfoButton extends InfoButton {

	private IInfoSubScreen screen;
	private @Nonnull ItemStack icon;
	private String descKey;
	
	public SubscreenInfoButton(InfoScreen mainScreen, String key, IInfoSubScreen screen, @Nonnull ItemStack icon) {
		super(mainScreen, 0, 0);
		this.screen = screen;
		this.icon = icon;
		this.descKey = key;
	}

	@Override
	public IInfoSubScreen getScreen(INostrumMagic attr) {
		return screen;
	}

	@Override
	public void render(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
		float tint = 1f;
		if (mouseX >= this.x
			&& mouseY >= this.y 
			&& mouseX < this.x + width 
			&& mouseY < this.y + height) {
			tint = .75f;
		}
		
		Minecraft.getInstance().getTextureManager().bindTexture(InfoScreen.background);
		RenderSystem.enableBlend();
		RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, this.x, this.y, 0,
				0, width,
				height, InfoScreen.TEXT_WHOLE_WIDTH, InfoScreen.TEXT_WHOLE_HEIGHT,
				tint, tint, tint, 1f);
		RenderSystem.disableBlend();
		
		final int itemLength = 16;
		
		if (!icon.isEmpty()) {
			int x = this.x + (width - itemLength) / 2;
			int y = this.y + (height - itemLength) / 2;
			Minecraft.getInstance().getItemRenderer().renderItemIntoGUI(icon, x, y);
		}
	}
	
	private List<ITextComponent> desc = new ArrayList<>(1);
	@Override
	public List<ITextComponent> getDescription() {
		if (desc.isEmpty())
			desc.add(new TranslationTextComponent("info." + descKey + ".name", new Object[0]));
		
		return desc;
	}
}
