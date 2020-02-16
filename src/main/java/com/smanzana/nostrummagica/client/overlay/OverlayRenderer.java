package com.smanzana.nostrummagica.client.overlay;

import java.util.Collection;

import com.google.common.base.Predicate;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.SpellIcon;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.entity.ITameDragon;
import com.smanzana.nostrummagica.items.HookshotItem;
import com.smanzana.nostrummagica.items.HookshotItem.HookshotType;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy.EffectData;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy.SpecialEffect;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.utils.RayTrace;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
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
	private static final int GUI_WING_OFFSETY = 37;
	private static final int GUI_WING_SIZE = 20;
	private static final int GUI_SHIELD_PHYS_OFFSETX = 45;
	private static final int GUI_SHIELD_MAG_OFFSETX = 63;
	private static final int GUI_SHIELD_OFFSETY = 17;
	private static final int GUI_HOOKSHOT_CROSSHAIR_OFFSETX = 82;
	private static final int GUI_HOOKSHOT_CROSSHAIR_WIDTH = 10;
	
	private int wiggleIndex; // set to multiples of 12 for each wiggle
	private static final int wiggleOffsets[] = {0, 1, 1, 2, 1, 1, 0, -1, -1, -2, -1, -1};
	
	private int wingIndex; // Controls mana wing animation. Set to -wingAnimDur to play backwards.
	private static final int wingAnimDur = 20; 
	
	public OverlayRenderer() {
		MinecraftForge.EVENT_BUS.register(this);
		wiggleIndex = 0;
		wingIndex = 0;
	}
	
	@SubscribeEvent
	public void onRender(RenderGameOverlayEvent.Pre event) {
		EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
		ScaledResolution scaledRes = event.getResolution();
		
		if (event.getType() == ElementType.CROSSHAIRS) {
			if (ModConfig.config.displayHookshotCrosshair()) {
				ItemStack hookshot = player.getHeldItemMainhand();
				if (hookshot == null || !(hookshot.getItem() instanceof HookshotItem)) {
					hookshot = player.getHeldItemOffhand();
				}
				
				if (hookshot == null || !(hookshot.getItem() instanceof HookshotItem)) {
					return;
				}
				
				HookshotType type = HookshotItem.GetType(hookshot);
				RayTraceResult result = RayTrace.raytrace(player.worldObj, player.getPositionEyes(event.getPartialTicks()),
						player.rotationPitch, player.rotationYaw, (float) HookshotItem.GetMaxDistance(type),
						new Predicate<Entity>() {

							@Override
							public boolean apply(Entity arg0) {
								return arg0 != null && arg0 != player && HookshotItem.CanBeHooked(type, arg0);
							}
					
				});
				
				if (result != null) {
					boolean hit = false;
					boolean entity = false;
					if (result.typeOfHit == Type.ENTITY) {
						// Already filtered in raytrace predicate
						hit = true;
						entity = true;
					} else if (result.typeOfHit == Type.BLOCK) {
						IBlockState state = player.worldObj.getBlockState(result.getBlockPos());
						if (state != null && HookshotItem.CanBeHooked(type, state)) {
							hit = true;
						}
					}
					
					if (hit) {
						event.setCanceled(true);
						renderHookshotCrosshair(player, scaledRes, entity);
					}
				}
				
			}
		}
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
			
			renderSpellSlide(player, scaledRes, attr);
			
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
		} else if (event.getType() == ElementType.ARMOR) {
			if (ModConfig.config.displayArmorOverlay()) {
				renderArmorOverlay(player, scaledRes);
			}
		} else if (event.getType() == ElementType.FOOD) {
			if (ModConfig.config.displayShieldHearts()) {
				renderShieldOverlay(player, scaledRes);
			}
		}
		
	}
	
	private void renderOrbsInternal(int whole, int pieces, int x, int y) {
		int i = 0;
		Minecraft.getMinecraft().getTextureManager().bindTexture(GUI_ICONS);
		for (; i < whole; i++) {
			// Draw a single partle orb
			this.drawTexturedModalRect(x - (8 * (i + 1)),
					y,	36, 16, GUI_ORB_WIDTH, GUI_ORB_HEIGHT);
		}
		
		if (pieces != 0) {
			// Draw a single partle orb
			this.drawTexturedModalRect(x - (8 * (i + 1)),
					y, GUI_ORB_WIDTH * pieces, 16, GUI_ORB_WIDTH, GUI_ORB_HEIGHT);
			i++;
		}
		
		for (; i < 10; i++) {
			this.drawTexturedModalRect(x - (8 * (i + 1)),
					y,	0, 16, GUI_ORB_WIDTH, GUI_ORB_HEIGHT);
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
		
		int totalMana = 0;
		int totalMaxMana = 0;
		
		// render background
		GlStateManager.color(.4f, .4f, .4f, 1f);
		renderOrbsInternal(10, 0, hudXAnchor + wiggleOffset, hudYAnchor);

		// Render dragon mana first, if available
		Collection<ITameDragon> dragons = NostrumMagica.getNearbyTamedDragons(player, 32, true);
		
		boolean hasDragon = false;
		if (dragons != null && !dragons.isEmpty()) {
			int dragonMana = 0;
			int dragonMaxMana = 0;
			
			for (ITameDragon dragon : dragons) {
				if (dragon.sharesMana(player)) {
					totalMana += dragon.getMana();
					totalMaxMana += dragon.getMaxMana();
					
					dragonMana += dragon.getMana();
					dragonMaxMana += dragon.getMaxMana();
				}
			}
			
			// Make sure at least ONE is willing to share mana with us
			if (dragonMaxMana > 0) {
				hasDragon = true;
				float ratio = (float) dragonMana / (float) dragonMaxMana;
				int parts = Math.round(40 * ratio);
				int whole = parts / 4;
				int pieces = parts % 4;
				
				// If we just found a dragon, start anim
				if (this.wingIndex == 0) {
					startWingAnim(true);
				}
				
				GlStateManager.pushMatrix();
				GlStateManager.pushAttrib();
				
				GlStateManager.color(1f, .2f, .2f, 1f);
				renderOrbsInternal(whole, pieces, hudXAnchor + wiggleOffset, hudYAnchor);
				
				GlStateManager.popAttrib();
				GlStateManager.popMatrix();
			}
		}
		
		// If no dragon is sharing mana and we're not playing wing animation, start the furl anim
		if (!hasDragon && wingIndex == wingAnimDur) {
			startWingAnim(false);
		}
		
		if (wingIndex != 0) {
			final int index = wingIndex;
			if (wingIndex < wingAnimDur) {
				wingIndex++;
			}
			
			final float ratio = ((float) (20 - Math.abs(index)) / 20f);
			final float rot = ratio * 120.0f;
			
			Minecraft.getMinecraft().getTextureManager().bindTexture(GUI_ICONS);
			
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0, -.5); // Behind
			GlStateManager.translate(hudXAnchor + wiggleOffset - 1, hudYAnchor + 3, 0);
			
			GlStateManager.rotate(rot, 0, 0, 1f);
			GlStateManager.translate(-1, -10, 0);
			GlStateManager.color(1f, 1f, 1f, 1f - ratio);
			drawScaledCustomSizeModalRect(0, 0, GUI_WING_SIZE, GUI_WING_OFFSETY,
					-GUI_WING_SIZE, GUI_WING_SIZE, 10, 10, 256f, 256f);
			GlStateManager.popMatrix();
			
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0, -.5); // Behind
			GlStateManager.translate(hudXAnchor + wiggleOffset - 76, hudYAnchor + 3, 0);
			
			GlStateManager.rotate(-rot, 0, 0, 1f);
			GlStateManager.translate(-10, -10, 0);
			GlStateManager.color(1f, 1f, 1f, 1f - ratio);
			drawScaledCustomSizeModalRect(0, 0, 0, GUI_WING_OFFSETY,
					GUI_WING_SIZE, GUI_WING_SIZE, 10, 10, 256f, 256f);
			GlStateManager.popMatrix();
			
			
		}
		
		// Render player mana on top
		{
			int playerMana = attr.getMana();
			int playerMaxMana = attr.getMaxMana();
			float ratio = (float) playerMana / (float) playerMaxMana;
			
			
			int parts = Math.round(40 * ratio);
			int whole = parts / 4;
			int pieces = parts % 4;
			
			//0094FF
			GlStateManager.color(0f, .8f, 1f, 1f);
			renderOrbsInternal(whole, pieces, hudXAnchor + wiggleOffset, hudYAnchor);
			
			totalMana += playerMana;
			totalMaxMana += playerMaxMana;
		}

		if (ModConfig.config.displayManaText()) {
			int centerx = hudXAnchor - (5 * 8);
			String str = totalMana + "/" + totalMaxMana;
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
			FontRenderer fonter = Minecraft.getMinecraft().fontRendererObj;
			final int iconSize = 16;
			final int iconMargin = 2;
			final int textOffset = iconSize + (2 * iconMargin);
			final int textMargin = 5;
			int slideHeight = iconSize + (2 * iconMargin);
			
			if (xp) {
				slideHeight = Math.max(slideHeight, fonter.FONT_HEIGHT * 2 + 9);
			}
			
			String text = (current == null ? "" : current.getName());
			
			Gui.drawRect(textOffset, scaledRes.getScaledHeight() - slideHeight, 120, scaledRes.getScaledHeight(), 0x50606060);
			
			// Draw icon
			if (current != null) {
				Gui.drawRect(0, scaledRes.getScaledHeight() - slideHeight, textOffset, scaledRes.getScaledHeight(), 0xFF202020);
				
				GlStateManager.color(1f, 1f, 1f, 1f);
				final int drawY = (scaledRes.getScaledHeight() - (slideHeight + iconSize) / 2);
				SpellIcon.get(current.getIconIndex()).render(Minecraft.getMinecraft(), iconMargin, drawY, iconSize, iconSize);
			}
			
			// Draw name (and maybe xp)
			
			if (xp) {
				// Height is based on this height. Just draw.
				fonter.drawString(text, textOffset + textMargin, scaledRes.getScaledHeight() - (fonter.FONT_HEIGHT + iconMargin), 0xFF000000);
				fonter.drawString(String.format("%.02f%%", 100f * attr.getXP() / attr.getMaxXP()),
						textOffset + textMargin, scaledRes.getScaledHeight() - (fonter.FONT_HEIGHT * 2 + 6), 0xFF000000);
			} else {
				// Draw in center
				final int drawY = (scaledRes.getScaledHeight() - (slideHeight + fonter.FONT_HEIGHT) / 2);
				fonter.drawString(text, textOffset + textMargin, drawY, 0xFF000000);
			}
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
	
	private void renderShieldOverlay(EntityPlayerSP player, ScaledResolution scaledRes) {
		double physical = 0;
		double magical = 0;
		EffectData data = NostrumMagica.magicEffectProxy.getData(player, SpecialEffect.SHIELD_PHYSICAL);
		if (data != null) {
			physical = data.getAmt();
		}
		data = NostrumMagica.magicEffectProxy.getData(player, SpecialEffect.SHIELD_MAGIC);
		if (data != null) {
			magical = data.getAmt();
		}
		
		// Clone calc of left y offset, since it's not passed through
		int left_height = 39;
        int left = scaledRes.getScaledWidth() / 2 - 91;
        int top = scaledRes.getScaledHeight() - left_height;
        int whole = (int) Math.ceil(physical) / 2;
        boolean half = Math.ceil(physical) % 2 == 1;
        
        if (physical > 0 || magical > 0) {
	        GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.color(1f, 1f, 1f, 1f);
			Minecraft.getMinecraft().getTextureManager().bindTexture(GUI_ICONS);
			
	        for (int i = 0; i < whole; i++)
	        {
	            drawTexturedModalRect(left, top, GUI_SHIELD_PHYS_OFFSETX, GUI_SHIELD_OFFSETY, 9, 9);
	            left += 8;
	        }
	        
	        if (half) {
	        	drawTexturedModalRect(left, top, GUI_SHIELD_PHYS_OFFSETX + 9, GUI_SHIELD_OFFSETY, 5, 9);
	        }
	        
	        // Repeat for magic
	        whole = (int) Math.ceil(magical) / 2;
	        half = Math.ceil(magical) % 2 == 1;
	        left = scaledRes.getScaledWidth() / 2 - 91;
	        
	        for (int i = 0; i < whole; i++)
	        {
	            drawTexturedModalRect(left, top, GUI_SHIELD_MAG_OFFSETX, GUI_SHIELD_OFFSETY, 9, 9);
	            left += 8;
	        }
	        
	        if (half) {
	        	drawTexturedModalRect(left, top, GUI_SHIELD_MAG_OFFSETX + 9, GUI_SHIELD_OFFSETY, 5, 9);
	        }
			
	        GlStateManager.color(1f, 1f, 1f, 1f);
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
        }
	}
	
	private void renderHookshotCrosshair(EntityPlayerSP player, ScaledResolution scaledResolution, boolean entity) {
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		if (entity) {
			GlStateManager.color(.8f, .8f, .8f, .8f);
		} else {
			GlStateManager.color(.5f, .5f, .5f, .7f);
		}
		Minecraft.getMinecraft().getTextureManager().bindTexture(GUI_ICONS);
		
		final int period = 30;
		final float frac = (float) (player.worldObj.getTotalWorldTime() % period) / period;
		final double radius = 6.0 + 2.0 * (Math.sin(frac * Math.PI * 2));
		
		final float rotOffset;
		if (entity) {
			rotOffset = 360.0f * ((float) (player.worldObj.getTotalWorldTime() % period) / period);
		} else {
			rotOffset = 0f;
		}
		float rot;
		
		for (int i = 0; i < 3; i++) {
			rot = rotOffset + (360.0f / 3) * i;
			GlStateManager.pushMatrix();
			GlStateManager.translate(scaledResolution.getScaledWidth() / 2, scaledResolution.getScaledHeight() / 2, 0);
			
			GlStateManager.rotate(rot, 0, 0, 1);
			GlStateManager.translate(0, -radius, 0);
			
			if (player.isSneaking()) {
				GlStateManager.rotate(180f, 0, 0, 1);
				GlStateManager.translate(-GUI_HOOKSHOT_CROSSHAIR_WIDTH / 2, 0, 0);
			} else {
				GlStateManager.translate(-GUI_HOOKSHOT_CROSSHAIR_WIDTH / 2, -GUI_HOOKSHOT_CROSSHAIR_WIDTH, 0);
			}
			
			drawTexturedModalRect(0, 0,
					GUI_HOOKSHOT_CROSSHAIR_OFFSETX, 0, GUI_HOOKSHOT_CROSSHAIR_WIDTH, GUI_HOOKSHOT_CROSSHAIR_WIDTH);
			
			GlStateManager.popMatrix();
		}
		
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}
	
	public void startManaWiggle(int wiggleCount) {
		this.wiggleIndex = 12 * wiggleCount;
	}
	
	public void startWingAnim(boolean forward) {
		if (forward) {
			this.wingIndex = 1;
		} else {
			this.wingIndex = -wingAnimDur;
		}
	}
	
}
