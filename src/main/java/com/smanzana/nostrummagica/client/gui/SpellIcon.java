package com.smanzana.nostrummagica.client.gui;

import java.util.EnumMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SpellIcon {

	private static Map<EMagicElement, SpellIcon> elementCache = new EnumMap<>(EMagicElement.class);
	private static Map<EAlteration, SpellIcon> alterationCache = new EnumMap<>(EAlteration.class);
	
	public static SpellIcon get(EMagicElement element) {
		SpellIcon icon = elementCache.get(element);
		if (icon == null) {
			icon = new SpellIcon(element);
			elementCache.put(element, icon);
		}
		
		return icon;
	}
	
	public static SpellIcon get(EAlteration alteration) {
		SpellIcon icon = alterationCache.get(alteration);
		if (icon == null) {
			icon = new SpellIcon(alteration);
			alterationCache.put(alteration, icon);
		}
		
		return icon;
	}
	
	private static ResourceLocation iconSheet = new ResourceLocation(
			NostrumMagica.MODID, "textures/gui/icons.png");
	private static int uWidthElement = 32;
	private static int uWidthAlteration = 32;
	private static int vOffsetAlteration = 64;
	private static int vOffsetElement = 96;
	
	private int offsetU;
	private int offsetV;
	private int width;
	private int height;
	
	private SpellIcon(EMagicElement element) {
		int ord;
		if (element == null)
			ord = 0;
		else
			ord = element.ordinal();
		
		offsetV = vOffsetElement;
		offsetU = uWidthElement * ord;
		width = uWidthElement;
		height = 32;
	}
	
	private SpellIcon(EAlteration alteration) {
		int ord;
		if (alteration == null)
			ord = 0;
		else
			ord = alteration.ordinal();
		
		offsetV = vOffsetAlteration;
		offsetU = uWidthAlteration * ord;
		width = uWidthAlteration;
		height = 32;
	}
	
	public void draw(Gui parent, FontRenderer fonter, int xOffset, int yOffset, int width, int height) {
		GL11.glPushMatrix();

		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		float scaleU = (float) width / (float) this.width;
		float scaleV = (float) height / (float) this.height;
		
		xOffset *= 1f / scaleU;
		yOffset *= 1f / scaleV;
		
		GL11.glScalef(scaleU, scaleV, 0f); // Idk which it is!
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(iconSheet);
		
		parent.drawTexturedModalRect(xOffset, yOffset, offsetU, offsetV,
				this.width, this.height);
		
		GL11.glPopMatrix();
	}
	
}
