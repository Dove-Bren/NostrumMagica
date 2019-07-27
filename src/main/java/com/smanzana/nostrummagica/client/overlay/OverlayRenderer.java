package com.smanzana.nostrummagica.client.overlay;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.spells.Spell;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class OverlayRenderer extends Gui {

	private static final ResourceLocation GUI_ICONS = new ResourceLocation(NostrumMagica.MODID, "textures/gui/icons.png");
	private static final int GUI_ORB_WIDTH = 9;
	private static final int GUI_ORB_HEIGHT = 9;
	private static final int GUI_BAR_WIDTH = 17;
	private static final int GUI_BAR_HEIGHT = 62;
	private static final int GUI_BAR_OFFSETX = 96;
	
	private int wiggleIndex; // set to multiples of 12 for each wiggle
	private static final int wiggleOffsets[] = {0, 1, 1, 2, 1, 1, 0, -1, -1, -2, -1, -1};
	
	public OverlayRenderer() {
		MinecraftForge.EVENT_BUS.register(this);
		wiggleIndex = 0;
	}
	
	@SubscribeEvent
	public void onRender(RenderGameOverlayEvent.Post event) {
		EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
		ScaledResolution scaledRes = event.getResolution();
		
		if (event.getType() == ElementType.EXPERIENCE) {
			// We do mana stuff in experience layer
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr == null || !attr.isUnlocked()) {
				return;
			}
			
			if (Minecraft.getMinecraft().thePlayer.isCreative()
					|| Minecraft.getMinecraft().thePlayer.isSpectator()) {
				return;
			}
			
			// Orbs
			if (ModConfig.config.displayManaOrbs()) {
				renderManaOrbs(player, scaledRes, attr);
			}
			
			if (ModConfig.config.displayManaBar()) {
				renderManaBar(player, scaledRes, attr);
			}
			
			renderSpellSlide(player, scaledRes, attr);
		} else if (event.getType() == ElementType.ARMOR) {
			renderArmorOverlay(player, scaledRes);
		}
		
		
		
		
		
		
		
		
	}
	
	private void renderManaOrbs(EntityPlayerSP player, ScaledResolution scaledRes, INostrumMagic attr) {
		int hudXAnchor = scaledRes.getScaledWidth() / 2 + 89;
		int hudYAnchor = scaledRes.getScaledHeight() - 49;
		
		if (player.isInsideOfMaterial(Material.WATER)) {
			hudYAnchor -= 10;
		}
		
		int wiggleOffset = 0;
		if (wiggleIndex > 0)
			wiggleOffset = OverlayRenderer.wiggleOffsets[wiggleIndex-- % 12];
		
		int mana = attr.getMana(),
				maxMana = attr.getMaxMana();
		float ratio = (float) mana / (float) maxMana;
		
		
		int parts = Math.round(40 * ratio);
		int whole = parts / 4;
		int pieces = parts % 4;
		
		int i = 0;
		Minecraft.getMinecraft().getTextureManager().bindTexture(GUI_ICONS);
		for (; i < whole; i++) {
			//this.drawTexturedModalRect(hudXAnchor - (), y, textureX, textureY, width, height);
//				Gui.drawModalRectWithCustomSizedTexture(hudXAnchor - (9 * i), hudYAnchor,
//						64, 0, 16, 16, 256, 256);
			// Draw a single partle orb
			this.drawTexturedModalRect(hudXAnchor - (8 * (i + 1)) + wiggleOffset,
					hudYAnchor,	36, 16, GUI_ORB_WIDTH, GUI_ORB_HEIGHT);
		}
		
		if (pieces != 0) {
			// Draw a single partle orb
			this.drawTexturedModalRect(hudXAnchor - (8 * (i + 1)) + wiggleOffset,
					hudYAnchor, GUI_ORB_WIDTH * pieces, 16, GUI_ORB_WIDTH, GUI_ORB_HEIGHT);
			i++;
		}
		
		for (; i < 10; i++) {
			this.drawTexturedModalRect(hudXAnchor - (8 * (i + 1)) + wiggleOffset,
					hudYAnchor,	0, 16, GUI_ORB_WIDTH, GUI_ORB_HEIGHT);
		}
		
		

		if (ModConfig.config.displayManaText()) {
			int centerx = hudXAnchor - (5 * 8);
			String str = attr.getMana() + "/" + attr.getMaxMana();
			int width = Minecraft.getMinecraft().fontRendererObj.getStringWidth(str);
			Minecraft.getMinecraft().fontRendererObj.drawString(
					str, centerx - width/2, hudYAnchor + 1, 0xFFFFFFFF);
		}
	}
	
	private void renderManaBar(EntityPlayerSP player, ScaledResolution scaledRes, INostrumMagic attr) {
		int hudXAnchor = scaledRes.getScaledWidth() - (10 + GUI_BAR_WIDTH);
		int hudYAnchor = 10 + (GUI_BAR_HEIGHT);
		int displayHeight = (int) ((float) GUI_BAR_HEIGHT * Math.max(0f, Math.min(1f, (float) attr.getMana() / (float) attr.getMaxMana())));
		
		GlStateManager.enableBlend();
		Minecraft.getMinecraft().getTextureManager().bindTexture(GUI_ICONS);
		this.drawTexturedModalRect(hudXAnchor, hudYAnchor - displayHeight, GUI_BAR_OFFSETX + GUI_BAR_WIDTH, (GUI_BAR_HEIGHT - displayHeight), GUI_BAR_WIDTH, displayHeight);
		this.drawTexturedModalRect(hudXAnchor, hudYAnchor - GUI_BAR_HEIGHT, GUI_BAR_OFFSETX, 0, GUI_BAR_WIDTH, GUI_BAR_HEIGHT);
		
		if (ModConfig.config.displayXPBar()) {
			displayHeight = (int) ((float) GUI_BAR_HEIGHT * Math.min(1f, Math.max(0f, attr.getXP() / attr.getMaxXP())));
			this.drawTexturedModalRect(hudXAnchor, hudYAnchor - displayHeight, GUI_BAR_OFFSETX + GUI_BAR_WIDTH + GUI_BAR_WIDTH, (GUI_BAR_HEIGHT - displayHeight), GUI_BAR_WIDTH, displayHeight);
		}
		GlStateManager.disableBlend();
		
		if (ModConfig.config.displayManaText()) {
			FontRenderer fonter = Minecraft.getMinecraft().fontRendererObj;
			int centerx = hudXAnchor + (int) (.5 * GUI_BAR_WIDTH);
			String str = "" + attr.getMana();
			int width = fonter.getStringWidth(str);
			fonter.drawString(
					str, centerx - width/2, hudYAnchor - (int) (.66 * GUI_BAR_HEIGHT), 0xFFFFFFFF);
			
			str = "-";
			width = fonter.getStringWidth(str);
			fonter.drawString(
					str, centerx - width/2, hudYAnchor - ((int) (.66 * GUI_BAR_HEIGHT) - fonter.FONT_HEIGHT), 0xFFFFFFFF);
			
			str = "" + attr.getMaxMana();
			width = fonter.getStringWidth(str);
			fonter.drawString(
					str, centerx - width/2, hudYAnchor - ((int) (.66 * GUI_BAR_HEIGHT) - (2 * fonter.FONT_HEIGHT)), 0xFFFFFFFF);
		}
	}
	
	private void renderSpellSlide(EntityPlayerSP player, ScaledResolution scaledRes, INostrumMagic attr) {
		// Bottom left spell slide
		// Spell name
		Spell current = NostrumMagica.getCurrentSpell(Minecraft.getMinecraft().thePlayer);
		boolean xp = ModConfig.config.displayXPText();
		if (current != null || xp) {
			String text = (current == null ? "" : current.getName());
			int mult = (xp) ? 2 : 1;
			FontRenderer fonter = Minecraft.getMinecraft().fontRendererObj;
			Gui.drawRect(0, scaledRes.getScaledHeight() - (fonter.FONT_HEIGHT * mult + 9), 100, scaledRes.getScaledHeight(), 0x50606060);
			fonter.drawString(text, 5, scaledRes.getScaledHeight() - (fonter.FONT_HEIGHT + 3), 0xFF000000);
			if (xp)
				fonter.drawString(String.format("%.02f%%", 100f * attr.getXP() / attr.getMaxXP()),
						5, scaledRes.getScaledHeight() - (fonter.FONT_HEIGHT * 2 + 6), 0xFF000000);
		}
	}
	
	private void renderArmorOverlay(EntityPlayerSP player, ScaledResolution scaledRes) {
		// Clone calc of left y offset, since it's not passed through
		int left_height = 39;
		IAttributeInstance attrMaxHealth = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        float healthMax = (float)attrMaxHealth.getAttributeValue();
        float absorb = MathHelper.ceiling_float_int(player.getAbsorptionAmount());
		int healthRows = MathHelper.ceiling_float_int((healthMax + absorb) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (healthRows - 2), 3);

        left_height += (healthRows * rowHeight);
        if (rowHeight != 10) left_height += 10 - rowHeight;
        int left = scaledRes.getScaledWidth() / 2 - 91;
        int top = scaledRes.getScaledHeight() - left_height;
		
        GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.color(0.1f, .2f, 1f, .8f);
		
		int level = ForgeHooks.getTotalArmorValue(player);
		level -= 20;
		
		// Stretch the last 5 to 100% out to fill the whole bar
		level = Math.min(20, level * 4);
        for (int i = 0; i < level; i += 2)
        {
            drawTexturedModalRect(left, top, 34, 9, 9, 3);
            left += 8;
        }
		
        GlStateManager.color(1f, 1f, 1f, 1f);
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}
	
	public void startManaWiggle(int wiggleCount) {
		this.wiggleIndex = 12 * wiggleCount;
	}
	
}
