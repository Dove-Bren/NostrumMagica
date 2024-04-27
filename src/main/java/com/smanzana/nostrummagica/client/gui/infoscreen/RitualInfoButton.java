package com.smanzana.nostrummagica.client.gui.infoscreen;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

public class RitualInfoButton extends InfoButton {

	private RitualRecipe ritual;
	
	public RitualInfoButton(InfoScreen screen, RitualRecipe ritual) {
		super(screen, 0, 0);
		this.ritual = ritual;
	}

	@Override
	public IInfoSubScreen getScreen(INostrumMagic attr) {
		return new RitualInfoSubScreen(ritual);
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		float tint = 1f;
		if (mouseX >= this.x 
			&& mouseY >= this.y 
			&& mouseX < this.x + width 
			&& mouseY < this.y + height) {
			tint = .75f;
		}
		
		Minecraft mc = Minecraft.getInstance();
		GL11.glColor4f(tint, tint, tint, 1f);
		mc.getTextureManager().bindTexture(InfoScreen.background);
		GlStateManager.enableBlend();
		RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, this.x, this.y, 0,
				0, width,
				height, InfoScreen.TEXT_WHOLE_WIDTH, InfoScreen.TEXT_WHOLE_HEIGHT);
		GlStateManager.disableBlend();
		
		final int itemLength = 16;
		
		@Nonnull ItemStack iconStack = ritual.getIcon();
		if (!iconStack.isEmpty()) {
			ItemRenderer ItemRenderer = mc.getItemRenderer();
			int x = this.x + (width - itemLength) / 2;
			int y = this.y + (height - itemLength) / 2;
			
			ItemRenderer.renderItemIntoGUI(iconStack, x, y);
		}
	}
	
	private List<String> desc = new ArrayList<>(1);
	@Override
	public List<String> getDescription() {
		if (desc.isEmpty())
			desc.add(I18n.format("ritual." + ritual.getTitleKey() + ".name", new Object[0]));
		
		return desc;
	}
}
