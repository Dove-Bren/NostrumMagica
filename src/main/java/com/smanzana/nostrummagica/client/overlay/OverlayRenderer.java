package com.smanzana.nostrummagica.client.overlay;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;

import com.google.common.base.Predicate;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.ModificationTable;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.effects.ClientEffect;
import com.smanzana.nostrummagica.client.effects.ClientEffectAnimated;
import com.smanzana.nostrummagica.client.effects.ClientEffectFormBasic;
import com.smanzana.nostrummagica.client.effects.ClientEffectIcon;
import com.smanzana.nostrummagica.client.effects.ClientEffectRenderer;
import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifierGrow;
import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifierShrink;
import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifierTranslate;
import com.smanzana.nostrummagica.client.gui.SpellIcon;
import com.smanzana.nostrummagica.client.render.LayerAetherCloak;
import com.smanzana.nostrummagica.client.render.LayerCustomElytra;
import com.smanzana.nostrummagica.client.render.LayerDragonFlightWings;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.entity.IEntityPet;
import com.smanzana.nostrummagica.entity.PetInfo;
import com.smanzana.nostrummagica.entity.PetInfo.PetAction;
import com.smanzana.nostrummagica.entity.PetInfo.SecondaryFlavor;
import com.smanzana.nostrummagica.entity.dragon.ITameDragon;
import com.smanzana.nostrummagica.items.EnchantedArmor;
import com.smanzana.nostrummagica.items.HookshotItem;
import com.smanzana.nostrummagica.items.HookshotItem.HookshotType;
import com.smanzana.nostrummagica.items.IRaytraceOverlay;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy.EffectData;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy.SpecialEffect;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.components.SpellAction;
import com.smanzana.nostrummagica.spells.components.triggers.SeekingBulletTrigger;
import com.smanzana.nostrummagica.utils.RayTrace;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
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
	private static final int GUI_TARGET_CROSSHAIR_OFFSETY = 58;
	private static final int GUI_TARGET_CROSSHAIR_WIDTH = 7;
	private static final int GUI_CONTINGENCY_ICON_OFFSETX = 22;
	private static final int GUI_CONTINGENCY_ICON_OFFSETY = 37;
	private static final int GUI_CONTINGENCY_ICON_LENGTH = 18;

	private static final ResourceLocation GUI_HEALTHBARS = new ResourceLocation(NostrumMagica.MODID, "textures/gui/healthbars.png");
	private static final int GUI_HEALTHBAR_ORB_BACK_WIDTH = 205;
	private static final int GUI_HEALTHBAR_ORB_BACK_HEIGHT = 56;
	private static final int GUI_HEALTHBAR_ORB_HEALTH_BAR_HOFFSET = 2;
	private static final int GUI_HEALTHBAR_ORB_HEALTH_BAR_VOFFSET = 112;
	private static final int GUI_HEALTHBAR_ORB_HEALTH_BAR_INNER_HOFFSET = 2;
	private static final int GUI_HEALTHBAR_ORB_HEALTH_BAR_INNER_VOFFSET = 29;
	private static final int GUI_HEALTHBAR_ORB_HEALTH_WIDTH = 152;
	private static final int GUI_HEALTHBAR_ORB_HEALTH_HEIGHT = 18;
	private static final int GUI_HEALTHBAR_ORB_SECONDARY_BAR_HOFFSET = 61;
	private static final int GUI_HEALTHBAR_ORB_SECONDARY_BAR_VOFFSET = 129;
	private static final int GUI_HEALTHBAR_ORB_SECONDARY_BAR_INNER_HOFFSET = 61;
	private static final int GUI_HEALTHBAR_ORB_SECONDARY_BAR_INNER_VOFFSET = 45;
	private static final int GUI_HEALTHBAR_ORB_SECONDARY_WIDTH = 105;
	private static final int GUI_HEALTHBAR_ORB_SECONDARY_HEIGHT = 8;
	private static final int GUI_HEALTHBAR_ORB_ENTITY_HOFFSET = 177;
	private static final int GUI_HEALTHBAR_ORB_ENTITY_VOFFSET = 40;
	private static final int GUI_HEALTHBAR_ORB_ENTITY_WIDTH = 12;
	private static final int GUI_HEALTHBAR_ORB_NAME_WIDTH = 160;
	private static final int GUI_HEALTHBAR_ORB_NAME_HEIGHT = 30;
	private static final int GUI_HEALTHBAR_ORB_NAME_HOFFSET = 20;
	private static final int GUI_HEALTHBAR_ORB_NAME_VOFFSET = 12;
	
	private static final int GUI_HEALTHBAR_BOX_BACK_WIDTH = 191;
	private static final int GUI_HEALTHBAR_BOX_BACK_HEIGHT = 25;
	private static final int GUI_HEALTHBAR_BOX_BACK_VOFFSET = 140;
	private static final int GUI_HEALTHBAR_BOX_HEALTH_BAR_HOFFSET = 2;
	private static final int GUI_HEALTHBAR_BOX_HEALTH_BAR_VOFFSET = 191;
	private static final int GUI_HEALTHBAR_BOX_HEALTH_BAR_INNER_HOFFSET = 2;
	private static final int GUI_HEALTHBAR_BOX_HEALTH_BAR_INNER_VOFFSET = 1;
	private static final int GUI_HEALTHBAR_BOX_HEALTH_WIDTH = 165;
	private static final int GUI_HEALTHBAR_BOX_HEALTH_HEIGHT = 18;
	private static final int GUI_HEALTHBAR_BOX_SECONDARY_BAR_HOFFSET = 61;
	private static final int GUI_HEALTHBAR_BOX_SECONDARY_BAR_VOFFSET = 211;
	private static final int GUI_HEALTHBAR_BOX_SECONDARY_BAR_INNER_HOFFSET = 62;
	private static final int GUI_HEALTHBAR_BOX_SECONDARY_BAR_INNER_VOFFSET = 17;
	private static final int GUI_HEALTHBAR_BOX_SECONDARY_WIDTH = 104;
	private static final int GUI_HEALTHBAR_BOX_SECONDARY_HEIGHT = 8;
	
	private static final int GUI_HEALTHBAR_ICON_LENGTH = 32;
	private static final int GUI_HEALTHBAR_ICON_HOFFSET = 207;
	private static final int GUI_HEALTHBAR_ICON_INTERNAL_HOFFSET = 300;
	private static final int GUI_HEALTHBAR_ICON_INTERNAL_VOFFSET = 50;
	private static final int GUI_HEALTHBAR_ICON_STAY_VOFFSET = 0;
	private static final int GUI_HEALTHBAR_ICON_ATTACK_VOFFSET = GUI_HEALTHBAR_ICON_STAY_VOFFSET + GUI_HEALTHBAR_ICON_LENGTH;
	private static final int GUI_HEALTHBAR_ICON_WORK_VOFFSET = GUI_HEALTHBAR_ICON_ATTACK_VOFFSET + GUI_HEALTHBAR_ICON_LENGTH;
	
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
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		ScaledResolution scaledRes = event.getResolution();
		
		if (event.getType() == ElementType.CROSSHAIRS) {
			if (ModConfig.config.displayHookshotCrosshair()) {
				ItemStack hookshot = player.getHeldItemMainhand();
				if (hookshot.isEmpty() || !(hookshot.getItem() instanceof HookshotItem)) {
					hookshot = player.getHeldItemOffhand();
				}
				
				if (hookshot.isEmpty() || !(hookshot.getItem() instanceof HookshotItem)) {
					return;
				}
				
				HookshotType type = HookshotItem.GetType(hookshot);
				RayTraceResult result = RayTrace.raytrace(player.world, player.getPositionEyes(event.getPartialTicks()),
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
						IBlockState state = player.world.getBlockState(result.getBlockPos());
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
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		ScaledResolution scaledRes = event.getResolution();
		
		if (event.getType() == ElementType.EXPERIENCE) {
			// We do mana stuff in experience layer
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr == null || !attr.isUnlocked()) {
				return;
			}
			
			renderSpellSlide(player, scaledRes, attr);
			
			if (Minecraft.getMinecraft().player.isCreative()
					|| Minecraft.getMinecraft().player.isSpectator()) {
				return;
			}
			
			// Orbs
			if (ModConfig.config.displayManaOrbs()) {
				renderManaOrbs(player, scaledRes, attr);
			}
			
			// Mana bar
			if (ModConfig.config.displayManaBar()) {
				renderManaBar(player, scaledRes, attr);
			}
			
			final float scale = 0.5f;
			int y = 75;
			int healthbarWidth;
			int healthbarHeight;
			int xOffset;
			
			// Dragon info
			if (ModConfig.config.displayDragonHealthbars()) {
				healthbarWidth = (int) (GUI_HEALTHBAR_ORB_BACK_WIDTH * scale);
				healthbarHeight = (int) (GUI_HEALTHBAR_ORB_BACK_HEIGHT * scale);
				xOffset = scaledRes.getScaledWidth() - (2 + healthbarWidth);
				
				List<ITameDragon> dragons = NostrumMagica.getNearbyTamedDragons(player, 32, true);
				Collections.sort(dragons, (left, right) -> {
					return ((EntityLivingBase) (left)).getUniqueID().compareTo(((EntityLivingBase) right).getUniqueID());
				});
				for (ITameDragon dragon : dragons) {
					if (dragon instanceof EntityLivingBase) {
						renderHealthbarOrb(player, scaledRes, (EntityLivingBase) dragon, xOffset, y, scale);
						y += healthbarHeight + 2;
					}
				}
			}
			
			// Pet info
			if (ModConfig.config.displayPetHealthbars()) {
				healthbarWidth = (int) (GUI_HEALTHBAR_BOX_BACK_WIDTH * scale);
				healthbarHeight = (int) (GUI_HEALTHBAR_BOX_BACK_HEIGHT * scale);
				xOffset = scaledRes.getScaledWidth() - (2 + healthbarWidth);
				final boolean hideDragons = ModConfig.config.displayDragonHealthbars();
				for (EntityLivingBase tamed : NostrumMagica.getTamedEntities(player)) {
					if (hideDragons && tamed instanceof ITameDragon) {
						continue;
					}
					renderHealthbarBox(player, scaledRes, tamed, xOffset, y, scale);
					y += healthbarHeight;
				}
			}
		} else if (event.getType() == ElementType.ARMOR) {
			if (ModConfig.config.displayArmorOverlay()) {
				renderArmorOverlay(player, scaledRes);
			}
		} else if (event.getType() == ElementType.FOOD) {
			if (ModConfig.config.displayShieldHearts()) {
				renderShieldOverlay(player, scaledRes);
			}
		} else if (event.getType() == ElementType.CROSSHAIRS) {
			//if (ModConfig.config.displayShieldHearts())
			{
				ItemStack held = player.getHeldItemMainhand();
				if (held == null || !(held.getItem() instanceof IRaytraceOverlay) || !((IRaytraceOverlay) held.getItem()).shouldTrace(player.worldObj, player, held)) {
					held = player.getHeldItemOffhand();
					if (held == null || !(held.getItem() instanceof IRaytraceOverlay) || !((IRaytraceOverlay) held.getItem()).shouldTrace(player.worldObj, player, held)) {
						held = null;
					}
				}
				
				if (held != null) {
					RayTraceResult result = RayTrace.raytraceApprox(player.worldObj, player.getPositionEyes(event.getPartialTicks()),
							player.rotationPitch, player.rotationYaw, SeekingBulletTrigger.MAX_DIST,
							new Predicate<Entity>() {
	
								@Override
								public boolean apply(Entity arg0) {
									return arg0 != null && arg0 != player && arg0 instanceof EntityLivingBase;
								}
						
					}, .5);
					if (result != null && result.entityHit != null) {
						renderCrosshairTargetOverlay(player, scaledRes);
					}
				}
			}
		} else if (event.getType() == ElementType.POTION_ICONS) {
			// TODO config option
						{
							final int nowTicks = player.ticksExisted;
							int offsetX = 0;
							EffectData data = NostrumMagica.magicEffectProxy.getData(player, SpecialEffect.CONTINGENCY_DAMAGE);
							if (data != null) {
								if (data.getAmt() == 0) {
									data.amt(player.ticksExisted);
								}
								float timer = (float) (nowTicks - (int) data.getAmt()) / (float) data.getCount();
								timer = 1f - timer;
								renderContingencyShield(player, scaledRes, 0, offsetX, timer);
								offsetX++;
							}
							data = NostrumMagica.magicEffectProxy.getData(player, SpecialEffect.CONTINGENCY_MANA);
							if (data != null) {
								if (data.getAmt() == 0) {
									data.amt(player.ticksExisted);
								}
								float timer = (float) (nowTicks - (int) data.getAmt()) / (float) data.getCount();
								timer = 1f - timer;
								renderContingencyShield(player, scaledRes, 1, offsetX, timer);
								offsetX++;
							}
							data = NostrumMagica.magicEffectProxy.getData(player, SpecialEffect.CONTINGENCY_HEALTH);
							if (data != null) {
								if (data.getAmt() == 0) {
									data.amt(player.ticksExisted);
								}
								float timer = (float) (nowTicks - (int) data.getAmt()) / (float) data.getCount();
								timer = 1f - timer;
								renderContingencyShield(player, scaledRes, 2, offsetX, timer);
								offsetX++;
							}
							data = NostrumMagica.magicEffectProxy.getData(player, SpecialEffect.CONTINGENCY_FOOD);
							if (data != null) {
								if (data.getAmt() == 0) {
									data.amt(player.ticksExisted);
								}
								float timer = (float) (nowTicks - (int) data.getAmt()) / (float) data.getCount();
								timer = 1f - timer;
								renderContingencyShield(player, scaledRes, 3, offsetX, timer);
								offsetX++;
							}
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
			int width = Minecraft.getMinecraft().fontRenderer.getStringWidth(str);
			Minecraft.getMinecraft().fontRenderer.drawString(
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
			FontRenderer fonter = Minecraft.getMinecraft().fontRenderer;
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
		Spell current = NostrumMagica.getCurrentSpell(Minecraft.getMinecraft().player);
		boolean xp = ModConfig.config.displayXPText();
		if (current != null || xp) {
			FontRenderer fonter = Minecraft.getMinecraft().fontRenderer;
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
        float absorb = MathHelper.ceil(player.getAbsorptionAmount());
		int healthRows = MathHelper.ceil((healthMax + absorb) / 2.0F / 10.0F);
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
	        	drawTexturedModalRect(left, top, GUI_SHIELD_MAG_OFFSETX, GUI_SHIELD_OFFSETY + 9, 5, 9);
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
		final float frac = (float) (player.world.getTotalWorldTime() % period) / period;
		final double radius = 6.0 + 2.0 * (Math.sin(frac * Math.PI * 2));
		
		final float rotOffset;
		if (entity) {
			rotOffset = 360.0f * ((float) (player.world.getTotalWorldTime() % period) / period);
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
	
	private void renderCrosshairTargetOverlay(EntityPlayerSP player, ScaledResolution scaledResolution) {
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(.5f, .5f, .5f, .9f);
		Minecraft.getMinecraft().getTextureManager().bindTexture(GUI_ICONS);
		
		GlStateManager.translate(scaledResolution.getScaledWidth() / 2, scaledResolution.getScaledHeight() / 2, 0);
		
		GlStateManager.translate(-GUI_TARGET_CROSSHAIR_WIDTH / 2, -(GUI_TARGET_CROSSHAIR_WIDTH / 2), 0);
		
		drawTexturedModalRect(0, 0,
				0, GUI_TARGET_CROSSHAIR_OFFSETY, GUI_TARGET_CROSSHAIR_WIDTH, GUI_TARGET_CROSSHAIR_WIDTH);
		
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}
	
	private void renderContingencyShield(EntityPlayerSP player, ScaledResolution scaledResolution, int typeOffset, int xoffset, float timer) {
		final int left = (scaledResolution.getScaledWidth() / 2 + 91) + 10 + (xoffset * GUI_CONTINGENCY_ICON_LENGTH);
		final int top = scaledResolution.getScaledHeight() - (2 + GUI_CONTINGENCY_ICON_LENGTH);
		final double borderScale = 1.07;
		final VertexBuffer buffer = Tessellator.getInstance().getBuffer();
		final int width = GUI_CONTINGENCY_ICON_LENGTH; // for readability
		final int height = GUI_CONTINGENCY_ICON_LENGTH; // for readability
		
		if (timer <= 0) {
			return;
		}
		
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		Minecraft.getMinecraft().getTextureManager().bindTexture(GUI_ICONS);
		
		GlStateManager.translate(left, top, 0);
		
		GlStateManager.pushMatrix();
		GlStateManager.color(1f, .25f, .3f, 1f);
		GlStateManager.translate(-.5, -.5, 0);
		GlStateManager.scale(borderScale, borderScale, borderScale);
		buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_TEX);
		
		final float uMin = ((float) (GUI_CONTINGENCY_ICON_OFFSETX + (3 * GUI_CONTINGENCY_ICON_LENGTH))) / 256f;
		final float uMax = ((float) (GUI_CONTINGENCY_ICON_OFFSETX + (4 * GUI_CONTINGENCY_ICON_LENGTH))) / 256f;
		final float vMin = ((float) (GUI_CONTINGENCY_ICON_OFFSETY - GUI_CONTINGENCY_ICON_LENGTH)) / 256f;
		final float vMax = ((float) (GUI_CONTINGENCY_ICON_OFFSETY)) / 256f;
		
		final int fullTris = (int) (timer / .25f);
		// x, y, z, u, v
		final float[][] coords = new float[][] {
			new float[]{0,		0, 		0, uMin, vMin}, // top left
			new float[]{0,		height, 0, uMin, vMax}, // bottom left
			new float[]{width, 	height,	0, uMax, vMax}, // bottom right
			new float[]{width, 	0,		0, uMax, vMin}, // top right
			new float[]{0,		0, 		0, uMin, vMin}, // top left
		};
		
		// triangle fans. 1 per quarter. auto draw around
		
		buffer.pos(width/2f, height/2f, 0).tex((uMin + uMax) / 2f, (vMin + vMax) / 2f).endVertex();
		
		for (int i = 0; i <= fullTris; i++) {
			buffer.pos(coords[i][0], coords[i][1], coords[i][2]).tex(coords[i][3], coords[i][4]).endVertex();
		}
		
		// draw partial
		if (fullTris < 4) {
			final float partial = (timer - (fullTris * .25f)) * 4;
			buffer
				.pos(coords[fullTris][0] * (1f-partial) + coords[fullTris+1][0] * partial,
					 coords[fullTris][1] * (1f-partial) + coords[fullTris+1][1] * partial,
					 coords[fullTris][2] * (1f-partial) + coords[fullTris+1][2] * partial)
				.tex(coords[fullTris][3] * (1f-partial) + coords[fullTris+1][3] * partial,
					 coords[fullTris][4] * (1f-partial) + coords[fullTris+1][4] * partial)
				.endVertex();
		}
		
		
		Tessellator.getInstance().draw();
		
//		drawTexturedModalRect(0, 0,
//				GUI_CONTINGENCY_ICON_OFFSETX + (3 * GUI_CONTINGENCY_ICON_LENGTH),
//				GUI_CONTINGENCY_ICON_OFFSETY - GUI_CONTINGENCY_ICON_LENGTH, GUI_CONTINGENCY_ICON_LENGTH, GUI_CONTINGENCY_ICON_LENGTH);
		GlStateManager.popMatrix();

		GlStateManager.color(.5f, .5f, .5f, 1f);
		drawTexturedModalRect(0, 0,
				GUI_CONTINGENCY_ICON_OFFSETX + (typeOffset * GUI_CONTINGENCY_ICON_LENGTH),
				GUI_CONTINGENCY_ICON_OFFSETY, GUI_CONTINGENCY_ICON_LENGTH, GUI_CONTINGENCY_ICON_LENGTH);
		
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}
	
	private void renderHealthbarOrb(EntityPlayerSP player, ScaledResolution scaledRes, EntityLivingBase pet, int xoffset, int yoffset, float scale) {
		
		// Render back, scaled bar + middle 'goods', and then foreground. Easy.
		// For center, render:
		// 1) healthbar
		// 2) pet head/icon
		// 3) pet status icon
		FontRenderer fonter = Minecraft.getMinecraft().fontRenderer;
//		final boolean sitting = (pet instanceof EntityTameable ? ((EntityTameable) pet).isSitting()
//				: pet instanceof IEntityTameable ? ((IEntityTameable) pet).isSitting()
//				: false);
//		final boolean attacking = (pet instanceof EntityLiving ? ((EntityLiving) pet).getAttackTarget() != null : false);
//		final float health = (float) (Math.max(0, Math.ceil(pet.getHealth())) / Math.max(0.01, Math.ceil(pet.getMaxHealth())));
//		boolean hasSecondaryBar = false;
//		float secondaryMeter = 0f;
//		
//		if (pet instanceof ITameDragon) {
//			ITameDragon dragon = (ITameDragon) pet;
//			hasSecondaryBar = true;
//			secondaryMeter = (float) dragon.getXP() / (float) dragon.getMaxXP();
//		}
		
		PetInfo info;
		if (pet instanceof IEntityPet) {
			IEntityPet iPet = (IEntityPet) pet;
			info = iPet.getPetSummary();
		} else {
			info = PetInfo.Wrap(pet);
		}
		
		final float health = (float) info.getHpPercent();//(float) (Math.max(0, Math.ceil(pet.getHealth())) / Math.max(0.01, Math.ceil(pet.getMaxHealth())));
		final boolean hasSecondaryBar = info.getMaxSecondary() > 0;
		float secondaryMeter = (float) info.getSecondaryPercent();
		final SecondaryFlavor flavor = info.getSecondaryFlavor();
		final PetAction action = info.getPetAction();
		
		info.release();
		info = null;
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(GUI_HEALTHBARS);
		
		GlStateManager.pushMatrix();
		
		GlStateManager.translate(xoffset, yoffset, 0);
		GlStateManager.scale(scale, scale, 1);
		
		GlStateManager.enableBlend();
		GlStateManager.color(1f, 1f, 1f, 1f);
		
		// Draw background
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, -100);
		this.drawGradientRect(GUI_HEALTHBAR_ORB_NAME_HOFFSET, GUI_HEALTHBAR_ORB_NAME_VOFFSET,
				GUI_HEALTHBAR_ORB_NAME_WIDTH, GUI_HEALTHBAR_ORB_NAME_HEIGHT,
				0x50000000, 0xA0000000); //nameplate background
		drawTexturedModalRect(0, 0,
				0, GUI_HEALTHBAR_ORB_BACK_HEIGHT, GUI_HEALTHBAR_ORB_BACK_WIDTH, GUI_HEALTHBAR_ORB_BACK_HEIGHT);
		GlStateManager.popMatrix();
		
		// Draw middle
		GlStateManager.pushMatrix();
		// 	-> Health bar
		drawTexturedModalRect(
				GUI_HEALTHBAR_ORB_HEALTH_BAR_INNER_HOFFSET + Math.round(GUI_HEALTHBAR_ORB_HEALTH_WIDTH * (1f-health)),
				GUI_HEALTHBAR_ORB_HEALTH_BAR_INNER_VOFFSET,
				GUI_HEALTHBAR_ORB_HEALTH_BAR_HOFFSET + Math.round(GUI_HEALTHBAR_ORB_HEALTH_WIDTH * (1f-health)),
				GUI_HEALTHBAR_ORB_HEALTH_BAR_VOFFSET,
				GUI_HEALTHBAR_ORB_HEALTH_WIDTH - Math.round(GUI_HEALTHBAR_ORB_HEALTH_WIDTH * (1f-health)),
				GUI_HEALTHBAR_ORB_HEALTH_HEIGHT);
		//	-> Secondary bar
		if (!hasSecondaryBar) {
			GlStateManager.color(.7f, .9f, .7f, 1f);
			secondaryMeter = 1f;
		} else {
			GlStateManager.color(flavor.colorR(secondaryMeter),
					flavor.colorG(secondaryMeter),
					flavor.colorB(secondaryMeter),
					flavor.colorA(secondaryMeter));
		}
		drawTexturedModalRect(
				GUI_HEALTHBAR_ORB_SECONDARY_BAR_INNER_HOFFSET + Math.round(GUI_HEALTHBAR_ORB_SECONDARY_WIDTH * (1f-secondaryMeter)),
				GUI_HEALTHBAR_ORB_SECONDARY_BAR_INNER_VOFFSET,
				GUI_HEALTHBAR_ORB_SECONDARY_BAR_HOFFSET + Math.round(GUI_HEALTHBAR_ORB_SECONDARY_WIDTH * (1f-secondaryMeter)),
				GUI_HEALTHBAR_ORB_SECONDARY_BAR_VOFFSET,
				GUI_HEALTHBAR_ORB_SECONDARY_WIDTH - Math.round(GUI_HEALTHBAR_ORB_SECONDARY_WIDTH * (1f-secondaryMeter)),
				GUI_HEALTHBAR_ORB_SECONDARY_HEIGHT);
	
		GlStateManager.color(1f, 1f, 1f, 1f);

		//	-> Icon
		GuiInventory.drawEntityOnScreen(GUI_HEALTHBAR_ORB_ENTITY_HOFFSET, GUI_HEALTHBAR_ORB_ENTITY_VOFFSET, GUI_HEALTHBAR_ORB_ENTITY_WIDTH, 0, 0, pet);
		Minecraft.getMinecraft().getTextureManager().bindTexture(GUI_HEALTHBARS);
		GlStateManager.color(1f, 1f, 1f, 1f);
		
		//	-> Status
		GlStateManager.translate(0, 0, 100);
		GlStateManager.pushMatrix();
		GlStateManager.scale(.6f, .6f, .6f);
		GlStateManager.translate(0, 0, 0);
		if (action == PetAction.ATTACKING) {
			drawTexturedModalRect(GUI_HEALTHBAR_ICON_INTERNAL_HOFFSET, GUI_HEALTHBAR_ICON_INTERNAL_VOFFSET,
					GUI_HEALTHBAR_ICON_HOFFSET, GUI_HEALTHBAR_ICON_ATTACK_VOFFSET, GUI_HEALTHBAR_ICON_LENGTH, GUI_HEALTHBAR_ICON_LENGTH);
		} else if (action == PetAction.SITTING) {
			drawTexturedModalRect(GUI_HEALTHBAR_ICON_INTERNAL_HOFFSET, GUI_HEALTHBAR_ICON_INTERNAL_VOFFSET,
					GUI_HEALTHBAR_ICON_HOFFSET, GUI_HEALTHBAR_ICON_STAY_VOFFSET, GUI_HEALTHBAR_ICON_LENGTH, GUI_HEALTHBAR_ICON_LENGTH);
		} else if (action == PetAction.WORKING) {
			drawTexturedModalRect(GUI_HEALTHBAR_ICON_INTERNAL_HOFFSET, GUI_HEALTHBAR_ICON_INTERNAL_VOFFSET,
					GUI_HEALTHBAR_ICON_HOFFSET, GUI_HEALTHBAR_ICON_WORK_VOFFSET, GUI_HEALTHBAR_ICON_LENGTH, GUI_HEALTHBAR_ICON_LENGTH);
		}
		GlStateManager.popMatrix();
		
		//	-> Name
		final String name = pet.hasCustomName() ? pet.getCustomNameTag() : pet.getName();
		final int nameLen = fonter.getStringWidth(name);
		//final float fontScale = (1f/scale) * .6f;
		final float fontScale = scale * 2.4f;
		GlStateManager.pushMatrix();
		GlStateManager.scale(fontScale, fontScale, fontScale);
		fonter.drawString(name, 123 - (nameLen), 25 - (fonter.FONT_HEIGHT + 2), 0xFFFFFFFF);
		Minecraft.getMinecraft().getTextureManager().bindTexture(GUI_HEALTHBARS);
		GlStateManager.popMatrix();
		
		GlStateManager.popMatrix();
		
		// Draw foreground
		GlStateManager.enableBlend();
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, 100);
		drawTexturedModalRect(0, 0,
				0, 0, GUI_HEALTHBAR_ORB_BACK_WIDTH, GUI_HEALTHBAR_ORB_BACK_HEIGHT);
		GlStateManager.popMatrix();
		
		GlStateManager.popMatrix();
	}
	
	private void renderHealthbarBox(EntityPlayerSP player, ScaledResolution scaledRes, EntityLivingBase pet, int xoffset, int yoffset, float scale) {
		
		// Render back, scaled bar + middle 'goods', and then foreground. Easy.
		// For center, render:
		// 1) healthbar
		// 2) pet head/icon
		// 3) pet status icon
		FontRenderer fonter = Minecraft.getMinecraft().fontRenderer;
		
		PetInfo info;
		if (pet instanceof IEntityPet) {
			IEntityPet iPet = (IEntityPet) pet;
			info = iPet.getPetSummary();
		} else {
			info = PetInfo.Wrap(pet);
		}
		
		final float health = (float) info.getHpPercent();//(float) (Math.max(0, Math.ceil(pet.getHealth())) / Math.max(0.01, Math.ceil(pet.getMaxHealth())));
		final boolean hasSecondaryBar = info.getMaxSecondary() > 0;
		float secondaryMeter = (float) info.getSecondaryPercent();
		final SecondaryFlavor flavor = info.getSecondaryFlavor();
//		final boolean sitting = (pet instanceof EntityTameable ? ((EntityTameable) pet).isSitting()
//				: pet instanceof IEntityTameable ? ((IEntityTameable) pet).isSitting()
//				: false);
//		final boolean attacking = (pet instanceof EntityLiving ? ((EntityLiving) pet).getAttackTarget() != null : false);
		final PetAction action = info.getPetAction();
		
		info.release();
		info = null;
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(GUI_HEALTHBARS);
		
		GlStateManager.pushMatrix();
		
		GlStateManager.translate(xoffset, yoffset, 0);
		GlStateManager.scale(scale, scale, 1);
		
		GlStateManager.enableBlend();
		GlStateManager.color(1f, 1f, 1f, 1f);
		
		// Draw background
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, -100);
//		this.drawGradientRect(GUI_HEALTHBAR_ORB_NAME_HOFFSET, GUI_HEALTHBAR_ORB_NAME_VOFFSET,
//				GUI_HEALTHBAR_ORB_NAME_WIDTH, GUI_HEALTHBAR_ORB_NAME_HEIGHT,
//				0x50000000, 0xA0000000); //nameplate background
		drawTexturedModalRect(0, 0,
				0, GUI_HEALTHBAR_BOX_BACK_VOFFSET + GUI_HEALTHBAR_BOX_BACK_HEIGHT, GUI_HEALTHBAR_BOX_BACK_WIDTH, GUI_HEALTHBAR_BOX_BACK_HEIGHT);
		GlStateManager.popMatrix();
		
		// Draw middle
		GlStateManager.pushMatrix();
		// 	-> Health bar
		drawTexturedModalRect(
				GUI_HEALTHBAR_BOX_HEALTH_BAR_INNER_HOFFSET + Math.round(GUI_HEALTHBAR_BOX_HEALTH_WIDTH * (1f-health)),
				GUI_HEALTHBAR_BOX_HEALTH_BAR_INNER_VOFFSET,
				GUI_HEALTHBAR_BOX_HEALTH_BAR_HOFFSET + Math.round(GUI_HEALTHBAR_BOX_HEALTH_WIDTH * (1f-health)),
				GUI_HEALTHBAR_BOX_HEALTH_BAR_VOFFSET,
				GUI_HEALTHBAR_BOX_HEALTH_WIDTH - Math.round(GUI_HEALTHBAR_BOX_HEALTH_WIDTH * (1f-health)),
				GUI_HEALTHBAR_BOX_HEALTH_HEIGHT);
		//	-> Secondary bar
		if (!hasSecondaryBar) {
			GlStateManager.color(.7f, .9f, .7f, 1f);
			secondaryMeter = 1f;
		} else {
			GlStateManager.color(flavor.colorR(secondaryMeter),
					flavor.colorG(secondaryMeter),
					flavor.colorB(secondaryMeter),
					flavor.colorA(secondaryMeter));
		}
		drawTexturedModalRect(
				GUI_HEALTHBAR_BOX_SECONDARY_BAR_INNER_HOFFSET + Math.round(GUI_HEALTHBAR_BOX_SECONDARY_WIDTH * (1f-secondaryMeter)),
				GUI_HEALTHBAR_BOX_SECONDARY_BAR_INNER_VOFFSET,
				GUI_HEALTHBAR_BOX_SECONDARY_BAR_HOFFSET + Math.round(GUI_HEALTHBAR_BOX_SECONDARY_WIDTH * (1f-secondaryMeter)),
				GUI_HEALTHBAR_BOX_SECONDARY_BAR_VOFFSET,
				GUI_HEALTHBAR_BOX_SECONDARY_WIDTH - Math.round(GUI_HEALTHBAR_BOX_SECONDARY_WIDTH * (1f-secondaryMeter)),
				GUI_HEALTHBAR_BOX_SECONDARY_HEIGHT);
	
		GlStateManager.color(1f, 1f, 1f, 1f);
		
		//		-> Status
		GlStateManager.translate(0, 0, 100);
		GlStateManager.pushMatrix();
		GlStateManager.scale(.6f, .6f, .6f);
		GlStateManager.translate(0, 0, 0);
		if (action == PetAction.ATTACKING) {
			drawTexturedModalRect(282, 6,
					GUI_HEALTHBAR_ICON_HOFFSET, GUI_HEALTHBAR_ICON_ATTACK_VOFFSET, GUI_HEALTHBAR_ICON_LENGTH, GUI_HEALTHBAR_ICON_LENGTH);
		} else if (action == PetAction.SITTING) {
			drawTexturedModalRect(282, 6,
					GUI_HEALTHBAR_ICON_HOFFSET, GUI_HEALTHBAR_ICON_STAY_VOFFSET, GUI_HEALTHBAR_ICON_LENGTH, GUI_HEALTHBAR_ICON_LENGTH);
		} else if (action == PetAction.WORKING) {
			drawTexturedModalRect(282, 6,
					GUI_HEALTHBAR_ICON_HOFFSET, GUI_HEALTHBAR_ICON_WORK_VOFFSET, GUI_HEALTHBAR_ICON_LENGTH, GUI_HEALTHBAR_ICON_LENGTH);
		}
		GlStateManager.popMatrix();

		//	-> Name
		final String name = pet.hasCustomName() ? pet.getCustomNameTag() : pet.getName();
		final int nameLen = fonter.getStringWidth(name);
		//final float fontScale = (1f/scale) * .6f;
		final float fontScale = scale * 2.4f;
		GlStateManager.pushMatrix();
		GlStateManager.scale(fontScale, fontScale, fontScale);
		fonter.drawStringWithShadow(name, 135 - (nameLen), 14 - (fonter.FONT_HEIGHT + 2), 0xFFFFFFFF);
		Minecraft.getMinecraft().getTextureManager().bindTexture(GUI_HEALTHBARS);
		GlStateManager.popMatrix();
		
		GlStateManager.popMatrix();
		
		// Draw foreground
		GlStateManager.enableBlend();
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, 100);
		drawTexturedModalRect(0, 0,
				0, GUI_HEALTHBAR_BOX_BACK_VOFFSET, GUI_HEALTHBAR_BOX_BACK_WIDTH, GUI_HEALTHBAR_BOX_BACK_HEIGHT);
		GlStateManager.popMatrix();
		
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
	
	private void renderLoreIcon(Boolean loreIsDeep) {
		
		GlStateManager.enableBlend();
		GlStateManager.color(.6f, .6f, .6f, .6f);
		Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(new ItemStack(SpellScroll.instance()), 0, 0);
		GlStateManager.color(1f, 1f, 1f, 1f);
		
		if (loreIsDeep != null) {
			final int u = (160 + (loreIsDeep ? 0 : 32));
			Minecraft.getMinecraft().getTextureManager().bindTexture(GUI_ICONS);
			Gui.drawScaledCustomSizeModalRect(8, 8, u, 0, 32, 32, 8, 8, 256, 256);
		}
	}
	
	private void renderEnchantableIcon() {
		GlStateManager.enableBlend();
		GlStateManager.color(1f, 1f, 1f, 1f);
		Minecraft.getMinecraft().getTextureManager().bindTexture(GUI_ICONS);
		Gui.drawScaledCustomSizeModalRect(6, 6, 192, 32, 32, 32, 12, 12, 256, 256);
		GlStateManager.color(1f, 1f, 1f, 1f);
	}
	
	private void renderConfigurableIcon() {
		GlStateManager.enableBlend();
		GlStateManager.color(1f, 1f, 1f, 1f);
		Minecraft.getMinecraft().getTextureManager().bindTexture(GUI_ICONS);
		Gui.drawScaledCustomSizeModalRect(8, 8, 160, 32, 32, 32, 8, 8, 256, 256);
		GlStateManager.color(1f, 1f, 1f, 1f);
	}
	
	@SubscribeEvent
	public void onTooltipRender(RenderTooltipEvent.PostBackground event) {
		ItemStack stack = event.getStack();
		if (stack.isEmpty()) {
			return;
		}
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(Minecraft.getMinecraft().player);
		if (attr == null || !attr.isUnlocked()) {
			return; // no highlights
		}
		
		// Lore icon
		final ILoreTagged tag;
		if (stack.getItem() instanceof ItemBlock) {
			if (!(((ItemBlock) stack.getItem()).getBlock() instanceof ILoreTagged)) {
				tag = null;
			} else {
				tag = (ILoreTagged) ((ItemBlock) stack.getItem()).getBlock();
			}
		} else if (!(stack.getItem() instanceof ILoreTagged)) {
			tag = null;
		} else {
			tag = (ILoreTagged) stack.getItem();
		}
		
		if (tag != null) {
			final Boolean hasFullLore;
			if (attr.hasFullLore(tag)) {
				hasFullLore = true;
			} else if (attr.hasLore(tag)) {
				hasFullLore = false;
			} else {
				hasFullLore = null;
			}
			
			GlStateManager.pushMatrix();
			GlStateManager.translate(event.getX() + event.getWidth() - 4, event.getY() + event.getHeight() - 6, 50);
			renderLoreIcon(hasFullLore);
			GlStateManager.popMatrix();
		}
		
		// Enchantable?
		if (SpellAction.isEnchantable(stack)) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(event.getX() + event.getWidth() - 8, event.getY() + event.getHeight() - 24, 50);
			renderEnchantableIcon();
			GlStateManager.popMatrix();
		}
		
		// Configurable?
		if (ModificationTable.IsModifiable(stack)) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(event.getX() - 15, event.getY() + event.getHeight() - 8, 50);
			renderConfigurableIcon();
			GlStateManager.popMatrix();
		}
	}
		
	
	@SubscribeEvent
	public void onTooltipRender(RenderTooltipEvent.PostText event) {
//		ItemStack stack = event.getStack();
//		if (stack.isEmpty() || !(stack.getItem() instanceof ILoreTagged)) {
//			return;
//		}
//		
//		INostrumMagic attr = NostrumMagica.getMagicWrapper(Minecraft.getMinecraft().player);
//		if (attr == null || !attr.isUnlocked()) {
//			return;
//		}
//		
//		ILoreTagged tag = (ILoreTagged) stack.getItem();
//		renderLoreIcon(attr.hasFullLore(tag));
	}
	
	@SubscribeEvent
	public void onEntityRender(RenderLivingEvent.Post<EntityLivingBase> event) {
		if (event.getEntity().ticksExisted % 4 == 0) {
			EffectData data = NostrumMagica.magicEffectProxy.getData(event.getEntity(), SpecialEffect.ROOTED);
			if (data != null && data.getCount() != 0) {
				final ClientEffect effect = new ClientEffectAnimated(event.getEntity().getPositionVector(), 1000L,
						new ClientEffect[] {
							new ClientEffect(Vec3d.ZERO, new ClientEffectFormBasic(ClientEffectIcon.THORN_0, 0, 0, 0), 1500L),
							new ClientEffect(Vec3d.ZERO, new ClientEffectFormBasic(ClientEffectIcon.THORN_1, 0, 0, 0), 1500L),
							new ClientEffect(Vec3d.ZERO, new ClientEffectFormBasic(ClientEffectIcon.THORN_2, 0, 0, 0), 1500L),
							new ClientEffect(Vec3d.ZERO, new ClientEffectFormBasic(ClientEffectIcon.THORN_3, 0, 0, 0), 1500L),
							new ClientEffect(Vec3d.ZERO, new ClientEffectFormBasic(ClientEffectIcon.THORN_4, 0, 0, 0), 1500L),
						},
						new float[] {
							.1f,
							.2f,
							.3f,
							.4f,
							1f
						});
				effect
					.modify(new ClientEffectModifierTranslate(NostrumMagica.rand.nextFloat() - .5f, 0, NostrumMagica.rand.nextFloat() - .5f, 0, NostrumMagica.rand.nextFloat() * 360f))
					.modify(new ClientEffectModifierGrow(.05f, 1f, .1f, 1f, .2f))
					.modify(new ClientEffectModifierShrink(1f, 1f, 1f, .0f, .8f));
				ClientEffectRenderer.instance().addEffect(effect);
			}
		}
	}
	
	private static final Map<RenderPlayer, Boolean> InjectedSet = new WeakHashMap<>();
	
	@SubscribeEvent
	public void onPlayerRender(RenderPlayerEvent.Post event) {
		if (!InjectedSet.containsKey(event.getRenderer())) {
			InjectedSet.put(event.getRenderer(), true);
			
			// EnderIO injects custom cape layer so that capes don't render if an elytra-like item is present. We won't bother.
			// Instead, we just inject a layer for our custom elytras, and another for dragon-flight wings
			event.getRenderer().addLayer(new LayerCustomElytra(event.getRenderer()));
			event.getRenderer().addLayer(new LayerDragonFlightWings(event.getRenderer()));
			event.getRenderer().addLayer(new LayerAetherCloak(event.getRenderer()));
		}
		
		if (event.getEntityPlayer() != Minecraft.getMinecraft().player) {
			// For other players, possibly do armor render ticks
			for (@Nonnull ItemStack equipStack : event.getEntityPlayer().getArmorInventoryList()) {
				if (equipStack.isEmpty() || !(equipStack.getItem() instanceof EnchantedArmor)) {
					continue;
				}
				
				((EnchantedArmor) equipStack.getItem()).onArmorTick(event.getEntityPlayer().world, event.getEntityPlayer(), equipStack);
			}
		}
	}
	
}
