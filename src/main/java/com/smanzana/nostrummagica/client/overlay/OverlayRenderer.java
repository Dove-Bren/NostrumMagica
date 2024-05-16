package com.smanzana.nostrummagica.client.overlay;

import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;

import com.google.common.base.Predicate;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.DungeonAir;
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
import com.smanzana.nostrummagica.client.render.layer.LayerAetherCloak;
import com.smanzana.nostrummagica.client.render.layer.LayerDragonFlightWings;
import com.smanzana.nostrummagica.client.render.layer.LayerManaArmor;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.entity.dragon.ITameDragon;
import com.smanzana.nostrummagica.items.HookshotItem;
import com.smanzana.nostrummagica.items.HookshotItem.HookshotType;
import com.smanzana.nostrummagica.items.IRaytraceOverlay;
import com.smanzana.nostrummagica.items.MagicArmor;
import com.smanzana.nostrummagica.items.NostrumItems;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy.EffectData;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy.SpecialEffect;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.components.SpellAction;
import com.smanzana.nostrummagica.spells.components.Transmutation;
import com.smanzana.nostrummagica.spells.components.triggers.SeekingBulletTrigger;
import com.smanzana.nostrummagica.utils.RayTrace;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.block.BlockState;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class OverlayRenderer extends AbstractGui {

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
	private static final int GUI_TARGET_CROSSHAIR_OFFSETY = 57;
	private static final int GUI_TARGET_CROSSHAIR_WIDTH = 7;
	private static final int GUI_CONTINGENCY_ICON_OFFSETX = 22;
	private static final int GUI_CONTINGENCY_ICON_OFFSETY = 37;
	private static final int GUI_CONTINGENCY_ICON_LENGTH = 18;

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
		Minecraft mc = Minecraft.getInstance();
		ClientPlayerEntity player = mc.player;
		MainWindow window = event.getWindow();
		
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
				RayTraceResult result = RayTrace.raytrace(player.world, player, player.getEyePosition(event.getPartialTicks()),
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
					if (result.getType() == Type.ENTITY) {
						// Already filtered in raytrace predicate
						hit = true;
						entity = true;
					} else if (result.getType() == Type.BLOCK) {
						BlockState state = player.world.getBlockState(RayTrace.blockPosFromResult(result));
						if (state != null && HookshotItem.CanBeHooked(type, state)) {
							hit = true;
						}
					}
					
					if (hit) {
						event.setCanceled(true);
						renderHookshotCrosshair(event.getMatrixStack(), player, window, entity);
					}
				}
				
			}
		}
	}
	
	@SubscribeEvent
	public void onRender(RenderGameOverlayEvent.Post event) {
		Minecraft mc = Minecraft.getInstance();
		ClientPlayerEntity player = mc.player;
		MainWindow window = event.getWindow();
		MatrixStack matrixStackIn = event.getMatrixStack();
		
		if (event.getType() == ElementType.EXPERIENCE) {
			// We do mana stuff in experience layer
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr == null || !attr.isUnlocked()) {
				return;
			}
			
			renderSpellSlide(matrixStackIn, player, window, attr);
			
			if (mc.player.isCreative()
					|| mc.player.isSpectator()) {
				return;
			}
			
			// Orbs
			if (ModConfig.config.displayManaOrbs()) {
				renderManaOrbs(matrixStackIn, player, window, attr);
			}
			
			// Mana bar
			if (ModConfig.config.displayManaBar()) {
				renderManaBar(matrixStackIn, player, window, attr);
			}
		} else if (event.getType() == ElementType.ARMOR) {
			if (ModConfig.config.displayArmorOverlay()) {
				renderArmorOverlay(matrixStackIn, player, window);
			}
		} else if (event.getType() == ElementType.FOOD) {
			if (ModConfig.config.displayShieldHearts()) {
				renderShieldOverlay(matrixStackIn, player, window);
			}
		} else if (event.getType() == ElementType.CROSSHAIRS) {
			//if (ModConfig.config.displayShieldHearts())
			{
				ItemStack held = player.getHeldItemMainhand();
				if (held.isEmpty() || !(held.getItem() instanceof IRaytraceOverlay) || !((IRaytraceOverlay) held.getItem()).shouldTrace(player.world, player, held)) {
					held = player.getHeldItemOffhand();
					if (held.isEmpty() || !(held.getItem() instanceof IRaytraceOverlay) || !((IRaytraceOverlay) held.getItem()).shouldTrace(player.world, player, held)) {
						held = ItemStack.EMPTY;
					}
				}
				
				if (!held.isEmpty()) {
					RayTraceResult result = RayTrace.raytraceApprox(player.world, player, player.getEyePosition(event.getPartialTicks()),
							player.rotationPitch, player.rotationYaw, SeekingBulletTrigger.MAX_DIST,
							new Predicate<Entity>() {
	
								@Override
								public boolean apply(Entity arg0) {
									return arg0 != null && arg0 != player && arg0 instanceof LivingEntity;
								}
						
					}, .5);
					if (result != null && RayTrace.entFromRaytrace(result) != null) {
						renderCrosshairTargetOverlay(matrixStackIn, player, window);
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
					renderContingencyShield(matrixStackIn, player, window, 0, offsetX, timer);
					offsetX++;
				}
				data = NostrumMagica.magicEffectProxy.getData(player, SpecialEffect.CONTINGENCY_MANA);
				if (data != null) {
					if (data.getAmt() == 0) {
						data.amt(player.ticksExisted);
					}
					float timer = (float) (nowTicks - (int) data.getAmt()) / (float) data.getCount();
					timer = 1f - timer;
					renderContingencyShield(matrixStackIn, player, window, 1, offsetX, timer);
					offsetX++;
				}
				data = NostrumMagica.magicEffectProxy.getData(player, SpecialEffect.CONTINGENCY_HEALTH);
				if (data != null) {
					if (data.getAmt() == 0) {
						data.amt(player.ticksExisted);
					}
					float timer = (float) (nowTicks - (int) data.getAmt()) / (float) data.getCount();
					timer = 1f - timer;
					renderContingencyShield(matrixStackIn, player, window, 2, offsetX, timer);
					offsetX++;
				}
				data = NostrumMagica.magicEffectProxy.getData(player, SpecialEffect.CONTINGENCY_FOOD);
				if (data != null) {
					if (data.getAmt() == 0) {
						data.amt(player.ticksExisted);
					}
					float timer = (float) (nowTicks - (int) data.getAmt()) / (float) data.getCount();
					timer = 1f - timer;
					renderContingencyShield(matrixStackIn, player, window, 3, offsetX, timer);
					offsetX++;
				}
			}
		} else if (event.getType() == ElementType.VIGNETTE) {
			if (player == null || player.world == null) {
				return;
			}
			
			final int h = (int) player.getEyeHeight();
			BlockState inBlock = player.world.getBlockState(new BlockPos(player.getPosX(), player.getPosY() + h, player.getPosZ()));
			if (inBlock.getBlock() instanceof DungeonAir) {
				// Render dungeon air overlay
				{
					final Matrix4f transform = matrixStackIn.getLast().getMatrix();
					Tessellator tessellator = Tessellator.getInstance();
					BufferBuilder bufferbuilder = tessellator.getBuffer();
					RenderSystem.enableBlend();
					RenderSystem.disableTexture();
					//GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
					RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
					final float depth = -91f;
					bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
					bufferbuilder.pos(transform, 0, event.getWindow().getScaledHeight(), depth).color(.3f, 0, .3f, .2f).endVertex();
					bufferbuilder.pos(transform, event.getWindow().getScaledWidth(), event.getWindow().getScaledHeight(), depth).color(.3f, 0, .3f, .2f).endVertex();
					bufferbuilder.pos(transform, event.getWindow().getScaledWidth(), 0, depth).color(.3f, 0, .3f, .2f).endVertex();
					bufferbuilder.pos(transform, 0, 0, depth).color(.3f, 0, .3f, .2f).endVertex();
					bufferbuilder.finishDrawing();
					WorldVertexBufferUploader.draw(bufferbuilder);
					RenderSystem.enableTexture();
				}
			}
		}
	}
	
	private void renderOrbsInternal(MatrixStack matrixStackIn, int whole, int pieces, int x, int y, float red, float green, float blue, float alpha) {
		int i = 0;
		Minecraft mc = Minecraft.getInstance();
		mc.getTextureManager().bindTexture(GUI_ICONS);
		
		for (; i < whole; i++) {
			// Draw a single partle orb
//			this.blit(x - (8 * (i + 1)),
//					y,	36, 16, GUI_ORB_WIDTH, GUI_ORB_HEIGHT);
			RenderFuncs.blit(matrixStackIn, x - (8 * (i + 1)),
					y,	36, 16, GUI_ORB_WIDTH, GUI_ORB_HEIGHT, red, green, blue, alpha);
		}
		
		if (pieces != 0) {
			// Draw a single partle orb
			RenderFuncs.blit(matrixStackIn, x - (8 * (i + 1)),
					y, GUI_ORB_WIDTH * pieces, 16, GUI_ORB_WIDTH, GUI_ORB_HEIGHT, red, green, blue, alpha);
			i++;
		}
		
		for (; i < 10; i++) {
			RenderFuncs.blit(matrixStackIn, x - (8 * (i + 1)),
					y,	0, 16, GUI_ORB_WIDTH, GUI_ORB_HEIGHT, red, green, blue, alpha);
		}
	}
	
	private void renderManaOrbs(MatrixStack matrixStackIn, ClientPlayerEntity player, MainWindow window, INostrumMagic attr) {
		int hudXAnchor = window.getScaledWidth() / 2 + 89;
		int hudYAnchor = window.getScaledHeight() - 49;
		
		hudYAnchor -= ModConfig.config.getManaSphereOffset() * 10;
		
		if (player.areEyesInFluid(FluidTags.WATER) || player.getAir() < player.getMaxAir()) {
			hudYAnchor -= 10;
		}
		
		int wiggleOffset = 0;
		if (wiggleIndex > 0)
			wiggleOffset = OverlayRenderer.wiggleOffsets[wiggleIndex-- % 12];
		
		int totalMana = 0;
		int totalMaxMana = 0;
		
		// render background
		renderOrbsInternal(matrixStackIn, 10, 0, hudXAnchor + wiggleOffset, hudYAnchor, .4f, .4f, .4f, .4f);

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
				
				renderOrbsInternal(matrixStackIn, whole, pieces, hudXAnchor + wiggleOffset, hudYAnchor, 1f, .2f, .2f, 1f);
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
			
			Minecraft mc = Minecraft.getInstance();
			mc.getTextureManager().bindTexture(GUI_ICONS);
			
			matrixStackIn.push();
			matrixStackIn.translate(0, 0, -.5f); // Behind
			matrixStackIn.translate(hudXAnchor + wiggleOffset - 1, hudYAnchor + 3, 0);
			
			matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(rot));
			matrixStackIn.translate(-1, -10, 0);
			//blit(0, 0, GUI_WING_SIZE, GUI_WING_OFFSETY, GUI_WING_SIZE, GUI_WING_SIZE, red, green, blue, alpha); // -10, 10, 
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0, GUI_WING_SIZE,
					GUI_WING_OFFSETY, -GUI_WING_SIZE, GUI_WING_SIZE, 10, 10, 256f, 256f,
					1f, 1f, 1f, 1f - ratio);
			matrixStackIn.pop();
			
			matrixStackIn.push();
			matrixStackIn.translate(0, 0, -.5f); // Behind
			matrixStackIn.translate(hudXAnchor + wiggleOffset - 76, hudYAnchor + 3, 0);
			
			matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(-rot));
			matrixStackIn.translate(-10, -10, 0);
			//blit(0, 0, 0, offsetX, offsetY, width, height, texWidth, texHeight, red, green, blue, alpha); // -10, 10, 
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0, 0,
					GUI_WING_OFFSETY, GUI_WING_SIZE, GUI_WING_SIZE, 10, 10, 256f, 256,
					1f, 1f, 1f, 1f - ratio);
			matrixStackIn.pop();
			
			
		}
		
		// Render player mana on top
		if (attr.getMaxMana() > 0)
		{
			int playerMana = attr.getMana();
			int playerMaxMana = attr.getMaxMana();
			float ratio = (float) playerMana / (float) playerMaxMana;
			
			
			int parts = Math.round(40 * ratio);
			int whole = parts / 4;
			int pieces = parts % 4;
			
			//0094FF
			renderOrbsInternal(matrixStackIn, whole, pieces, hudXAnchor + wiggleOffset, hudYAnchor, 0f, .8f, 1f, 1f);
			
			totalMana += playerMana;
			totalMaxMana += playerMaxMana;
		}

		if (ModConfig.config.displayManaText()) {
			Minecraft mc = Minecraft.getInstance();
			int centerx = hudXAnchor - (5 * 8);
			String str = totalMana + "/" + totalMaxMana;
			int width = mc.fontRenderer.getStringWidth(str);
			mc.fontRenderer.drawString(matrixStackIn,
					str, centerx - width/2, hudYAnchor + 1, 0xFFFFFFFF);
		}
		
	}
	
	private void renderManaBar(MatrixStack matrixStackIn, ClientPlayerEntity player, MainWindow window, INostrumMagic attr) {
		Minecraft mc = Minecraft.getInstance();
		int hudXAnchor = window.getScaledWidth() - (10 + GUI_BAR_WIDTH);
		int hudYAnchor = 10 + (GUI_BAR_HEIGHT);
		int displayHeight = (int) ((float) GUI_BAR_HEIGHT * Math.max(0f, Math.min(1f, (float) attr.getMana() / (float) attr.getMaxMana())));
		
		RenderSystem.enableBlend();
		mc.getTextureManager().bindTexture(GUI_ICONS);
		this.blit(matrixStackIn, hudXAnchor, hudYAnchor - displayHeight, GUI_BAR_OFFSETX + GUI_BAR_WIDTH, (GUI_BAR_HEIGHT - displayHeight), GUI_BAR_WIDTH, displayHeight);
		this.blit(matrixStackIn, hudXAnchor, hudYAnchor - GUI_BAR_HEIGHT, GUI_BAR_OFFSETX, 0, GUI_BAR_WIDTH, GUI_BAR_HEIGHT);
		
		if (ModConfig.config.displayXPBar()) {
			displayHeight = (int) ((float) GUI_BAR_HEIGHT * Math.min(1f, Math.max(0f, attr.getXP() / attr.getMaxXP())));
			this.blit(matrixStackIn, hudXAnchor, hudYAnchor - displayHeight, GUI_BAR_OFFSETX + GUI_BAR_WIDTH + GUI_BAR_WIDTH, (GUI_BAR_HEIGHT - displayHeight), GUI_BAR_WIDTH, displayHeight);
		}
		RenderSystem.disableBlend();
		
		if (ModConfig.config.displayManaText()) {
			FontRenderer fonter = mc.fontRenderer;
			int centerx = hudXAnchor + (int) (.5 * GUI_BAR_WIDTH);
			String str = "" + attr.getMana();
			int width = fonter.getStringWidth(str);
			fonter.drawString(matrixStackIn, 
					str, centerx - width/2, hudYAnchor - (int) (.66 * GUI_BAR_HEIGHT), 0xFFFFFFFF);
			
			str = "-";
			width = fonter.getStringWidth(str);
			fonter.drawString(matrixStackIn, 
					str, centerx - width/2, hudYAnchor - ((int) (.66 * GUI_BAR_HEIGHT) - fonter.FONT_HEIGHT), 0xFFFFFFFF);
			
			str = "" + attr.getMaxMana();
			width = fonter.getStringWidth(str);
			fonter.drawString(matrixStackIn, 
					str, centerx - width/2, hudYAnchor - ((int) (.66 * GUI_BAR_HEIGHT) - (2 * fonter.FONT_HEIGHT)), 0xFFFFFFFF);
		}
	}
	
	private void renderSpellSlide(MatrixStack matrixStackIn, ClientPlayerEntity player, MainWindow window, INostrumMagic attr) {
		// Bottom left spell slide
		// Spell name
		Minecraft mc = Minecraft.getInstance();
		Spell current = NostrumMagica.getCurrentSpell(mc.player);
		boolean xp = ModConfig.config.displayXPText();
		if (current != null || xp) {
			FontRenderer fonter = mc.fontRenderer;
			final int iconSize = 16;
			final int iconMargin = 2;
			final int textOffset = iconSize + (2 * iconMargin);
			final int textMargin = 5;
			int slideHeight = iconSize + (2 * iconMargin);
			
			if (xp) {
				slideHeight = Math.max(slideHeight, fonter.FONT_HEIGHT * 2 + 9);
			}
			
			String text = (current == null ? "" : current.getName());
			
			RenderFuncs.drawRect(matrixStackIn, textOffset, window.getScaledHeight() - slideHeight, 120, window.getScaledHeight(), 0x50606060);
			
			// Draw icon
			if (current != null) {
				RenderFuncs.drawRect(matrixStackIn, 0, window.getScaledHeight() - slideHeight, textOffset, window.getScaledHeight(), 0xFF202020);
				
//				GlStateManager.color4f(1f, 1f, 1f, 1f);
				final int drawY = (window.getScaledHeight() - (slideHeight + iconSize) / 2);
				SpellIcon.get(current.getIconIndex()).render(Minecraft.getInstance(), matrixStackIn, iconMargin, drawY, iconSize, iconSize);
			}
			
			// Draw name (and maybe xp)
			
			if (xp) {
				// Height is based on this height. Just draw.
				fonter.drawString(matrixStackIn, text, textOffset + textMargin, window.getScaledHeight() - (fonter.FONT_HEIGHT + iconMargin), 0xFF000000);
				fonter.drawString(matrixStackIn, String.format("%.02f%%", 100f * attr.getXP() / attr.getMaxXP()),
						textOffset + textMargin, window.getScaledHeight() - (fonter.FONT_HEIGHT * 2 + 6), 0xFF000000);
			} else {
				// Draw in center
				final int drawY = (window.getScaledHeight() - (slideHeight + fonter.FONT_HEIGHT) / 2);
				fonter.drawString(matrixStackIn, text, textOffset + textMargin, drawY, 0xFF000000);
			}
		}
	}
	
	private void renderArmorOverlay(MatrixStack matrixStackIn, ClientPlayerEntity player, MainWindow window) {
		// Clone calc of left y offset, since it's not passed through
		int left_height = 39;
		ModifiableAttributeInstance attrMaxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        float healthMax = (float)attrMaxHealth.getValue();
        float absorb = MathHelper.ceil(player.getAbsorptionAmount());
		int healthRows = MathHelper.ceil((healthMax + absorb) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (healthRows - 2), 3);

        left_height += (healthRows * rowHeight);
        if (rowHeight != 10) left_height += 10 - rowHeight;
        int left = window.getScaledWidth() / 2 - 91;
        int top = window.getScaledHeight() - left_height;
		
        matrixStackIn.push();
		RenderSystem.enableBlend();
		
		int level = player.getTotalArmorValue();//ForgeHooks.getTotalArmorValue(player);
		level -= 20;
		
		// Stretch the last 5 to 100% out to fill the whole bar
		level = Math.min(20, level * 4);
        for (int i = 0; i < level; i += 2)
        {
            RenderFuncs.blit(matrixStackIn, left, top, 34, 9, 9, 3, 0.1f, .2f, 1f, .8f);
            left += 8;
        }
		
		RenderSystem.disableBlend();
		matrixStackIn.pop();
	}
	
	private void renderShieldOverlay(MatrixStack matrixStackIn, ClientPlayerEntity player, MainWindow window) {
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
        int left = window.getScaledWidth() / 2 - 91;
        int top = window.getScaledHeight() - left_height;
        int whole = (int) Math.ceil(physical) / 2;
        boolean half = Math.ceil(physical) % 2 == 1;
        
        if (physical > 0 || magical > 0) {
        	Minecraft mc = Minecraft.getInstance();
	        matrixStackIn.push();
			RenderSystem.enableBlend();
			RenderSystem.color4f(1f, 1f, 1f, 1f); // ?
			mc.getTextureManager().bindTexture(GUI_ICONS);
			
	        for (int i = 0; i < whole; i++)
	        {
	            blit(matrixStackIn, left, top, GUI_SHIELD_PHYS_OFFSETX, GUI_SHIELD_OFFSETY, 9, 9);
	            left += 8;
	        }
	        
	        if (half) {
	        	blit(matrixStackIn, left, top, GUI_SHIELD_PHYS_OFFSETX + 9, GUI_SHIELD_OFFSETY, 5, 9);
	        }
	        
	        // Repeat for magic
	        whole = (int) Math.ceil(magical) / 2;
	        half = Math.ceil(magical) % 2 == 1;
	        left = window.getScaledWidth() / 2 - 91;
	        
	        for (int i = 0; i < whole; i++)
	        {
	            blit(matrixStackIn, left, top, GUI_SHIELD_MAG_OFFSETX, GUI_SHIELD_OFFSETY, 9, 9);
	            left += 8;
	        }
	        
	        if (half) {
	        	blit(matrixStackIn, left, top, GUI_SHIELD_MAG_OFFSETX, GUI_SHIELD_OFFSETY + 9, 5, 9);
	        }
			
//	        GlStateManager.color4f(1f, 1f, 1f, 1f);
			RenderSystem.disableBlend();
			matrixStackIn.pop();
        }
	}
	
	private void renderHookshotCrosshair(MatrixStack matrixStackIn, ClientPlayerEntity player, MainWindow scaledResolution, boolean entity) {
		final float red;
		final float green;
		final float blue;
		final float alpha;
		matrixStackIn.push();
		RenderSystem.enableBlend();
		if (entity) {
			red = .8f;
			green = .8f;
			blue = .8f;
			alpha = .8f;
		} else {
			red = .5f;
			green = .5f;
			blue = .5f;
			alpha = .7f;
		}
		Minecraft mc = Minecraft.getInstance();
		mc.getTextureManager().bindTexture(GUI_ICONS);
		
		final int period = 30;
		final float frac = (float) (player.world.getGameTime() % period) / period;
		final double radius = 6.0 + 2.0 * (Math.sin(frac * Math.PI * 2));
		
		final float rotOffset;
		if (entity) {
			rotOffset = 360.0f * ((float) (player.world.getGameTime() % period) / period);
		} else {
			rotOffset = 0f;
		}
		float rot;
		
		for (int i = 0; i < 3; i++) {
			rot = rotOffset + (360.0f / 3) * i;
			matrixStackIn.push();
			matrixStackIn.translate(scaledResolution.getScaledWidth() / 2, scaledResolution.getScaledHeight() / 2, 0);
			
			matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(rot));
			matrixStackIn.translate(0, -radius, 0);
			
			if (player.isSneaking()) {
				matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(180f));
				matrixStackIn.translate(-GUI_HOOKSHOT_CROSSHAIR_WIDTH / 2, 0, 0);
			} else {
				matrixStackIn.translate(-GUI_HOOKSHOT_CROSSHAIR_WIDTH / 2, -GUI_HOOKSHOT_CROSSHAIR_WIDTH, 0);
			}
			
			RenderFuncs.blit(matrixStackIn, 0, 0,
					GUI_HOOKSHOT_CROSSHAIR_OFFSETX, 0, GUI_HOOKSHOT_CROSSHAIR_WIDTH, GUI_HOOKSHOT_CROSSHAIR_WIDTH,
					red, green, blue, alpha);
			
			matrixStackIn.pop();
		}
		
		RenderSystem.disableBlend();
		matrixStackIn.pop();
	}
	
	private void renderCrosshairTargetOverlay(MatrixStack matrixStackIn, ClientPlayerEntity player, MainWindow scaledResolution) {
		matrixStackIn.push();
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		Minecraft mc = Minecraft.getInstance();
		mc.getTextureManager().bindTexture(GUI_ICONS);
		
		matrixStackIn.translate(scaledResolution.getScaledWidth() / 2, scaledResolution.getScaledHeight() / 2, 0);
		
		matrixStackIn.translate(-GUI_TARGET_CROSSHAIR_WIDTH / 2, -(GUI_TARGET_CROSSHAIR_WIDTH / 2), 0);
		
		matrixStackIn.translate(0, -1, 0);
		
		RenderFuncs.blit(matrixStackIn, 0, 0,
				0, GUI_TARGET_CROSSHAIR_OFFSETY, GUI_TARGET_CROSSHAIR_WIDTH, GUI_TARGET_CROSSHAIR_WIDTH,
				.5f, .5f, .5f, .9f);
		
		RenderSystem.disableBlend();
		matrixStackIn.pop();
	}
	
	private void renderContingencyShield(MatrixStack matrixStackIn, ClientPlayerEntity player, MainWindow scaledResolution, int typeOffset, int xoffset, float timer) {
		Minecraft mc = Minecraft.getInstance();
		final int left = (scaledResolution.getScaledWidth() / 2 + 91) + 10 + (xoffset * GUI_CONTINGENCY_ICON_LENGTH);
		final int top = scaledResolution.getScaledHeight() - (2 + GUI_CONTINGENCY_ICON_LENGTH);
		final float borderScale = 1.07f;
		final BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		final int width = GUI_CONTINGENCY_ICON_LENGTH; // for readability
		final int height = GUI_CONTINGENCY_ICON_LENGTH; // for readability
		
		if (timer <= 0) {
			return;
		}
		
		matrixStackIn.push();
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		mc.getTextureManager().bindTexture(GUI_ICONS);
		
		matrixStackIn.translate(left, top, 0);
		
		matrixStackIn.push();
		matrixStackIn.translate(-.5f, -.5f, 0);
		matrixStackIn.scale(borderScale, borderScale, borderScale);
		{
			final Matrix4f transform = matrixStackIn.getLast().getMatrix();
			buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR_TEX);
			
			final float uMin = ((float) (GUI_CONTINGENCY_ICON_OFFSETX + (3 * GUI_CONTINGENCY_ICON_LENGTH))) / 256f;
			final float uMax = ((float) (GUI_CONTINGENCY_ICON_OFFSETX + (4 * GUI_CONTINGENCY_ICON_LENGTH))) / 256f;
			final float vMin = ((float) (GUI_CONTINGENCY_ICON_OFFSETY - GUI_CONTINGENCY_ICON_LENGTH)) / 256f;
			final float vMax = ((float) (GUI_CONTINGENCY_ICON_OFFSETY)) / 256f;
			
			final int fullTris = Math.min(4, (int) (timer / .25f));
			// x, y, z, u, v
			final float[][] coords = new float[][] {
				new float[]{0,		0, 		0, uMin, vMin}, // top left
				new float[]{0,		height, 0, uMin, vMax}, // bottom left
				new float[]{width, 	height,	0, uMax, vMax}, // bottom right
				new float[]{width, 	0,		0, uMax, vMin}, // top right
				new float[]{0,		0, 		0, uMin, vMin}, // top left
			};
			
			// triangle fans. 1 per quarter. auto draw around
			
			buffer.pos(transform, width/2f, height/2f, 0).color(1f, .25f, .3f, 1f).tex((uMin + uMax) / 2f, (vMin + vMax) / 2f).endVertex();
			
			for (int i = 0; i <= fullTris; i++) {
				buffer.pos(transform, coords[i][0], coords[i][1], coords[i][2]).color(1f, .25f, .3f, 1f).tex(coords[i][3], coords[i][4]).endVertex();
			}
			
			// draw partial
			if (fullTris < 4) {
				final float partial = (timer - (fullTris * .25f)) * 4;
				buffer
					.pos(transform, coords[fullTris][0] * (1f-partial) + coords[fullTris+1][0] * partial,
						 coords[fullTris][1] * (1f-partial) + coords[fullTris+1][1] * partial,
						 coords[fullTris][2] * (1f-partial) + coords[fullTris+1][2] * partial)
					.color(1f, .25f, .3f, 1f)
					.tex(coords[fullTris][3] * (1f-partial) + coords[fullTris+1][3] * partial,
						 coords[fullTris][4] * (1f-partial) + coords[fullTris+1][4] * partial)
					.endVertex();
			}
			
			
			Tessellator.getInstance().draw();
		}
		
//		blit(0, 0,
//				GUI_CONTINGENCY_ICON_OFFSETX + (3 * GUI_CONTINGENCY_ICON_LENGTH),
//				GUI_CONTINGENCY_ICON_OFFSETY - GUI_CONTINGENCY_ICON_LENGTH, GUI_CONTINGENCY_ICON_LENGTH, GUI_CONTINGENCY_ICON_LENGTH);
		matrixStackIn.pop();

		RenderFuncs.blit(matrixStackIn, 0, 0,
				GUI_CONTINGENCY_ICON_OFFSETX + (typeOffset * GUI_CONTINGENCY_ICON_LENGTH),
				GUI_CONTINGENCY_ICON_OFFSETY, GUI_CONTINGENCY_ICON_LENGTH, GUI_CONTINGENCY_ICON_LENGTH,
				.5f, .5f, .5f, 1f);
		
		RenderSystem.disableBlend();
		matrixStackIn.pop();
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
	
	private void renderLoreIcon(MatrixStack matrixStackIn, Boolean loreIsDeep) {
		Minecraft mc = Minecraft.getInstance();
		RenderSystem.enableBlend();
		RenderSystem.color4f(.6f, .6f, .6f, .6f);
		RenderSystem.pushMatrix();
		RenderSystem.multMatrix(matrixStackIn.getLast().getMatrix());
		mc.getItemRenderer().renderItemIntoGUI(new ItemStack(NostrumItems.spellScroll), 0, 0); // not using transform!
		RenderSystem.popMatrix();
		RenderSystem.color4f(1f, 1f, 1f, 1f);
		
		if (loreIsDeep != null) {
			final int u = (160 + (loreIsDeep ? 0 : 32));
			mc.getTextureManager().bindTexture(GUI_ICONS);
			
			matrixStackIn.push();
			matrixStackIn.translate(0, 0, 101); // items render z+100
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 8, 8, u, 0, 32, 32, 8, 8, 256, 256);
			matrixStackIn.pop();
		}
	}
	
	private void renderEnchantableIcon(MatrixStack matrixStackIn) {
		Minecraft mc = Minecraft.getInstance();
		RenderSystem.enableBlend();
		mc.getTextureManager().bindTexture(GUI_ICONS);
		RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 6, 6, 192, 32, 32, 32, 12, 12, 256, 256);
	}
	
	private void renderConfigurableIcon(MatrixStack matrixStackIn) {
		Minecraft mc = Minecraft.getInstance();
		RenderSystem.enableBlend();
		mc.getTextureManager().bindTexture(GUI_ICONS);
		RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 8, 8, 160, 32, 32, 32, 8, 8, 256, 256);
	}
	
	private void renderTransmutableIcon(MatrixStack matrixStackIn) {
		Minecraft mc = Minecraft.getInstance();
		RenderSystem.enableBlend();
		mc.getTextureManager().bindTexture(GUI_ICONS);
		RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 8, 8, 224, 32, 32, 32, 8, 8, 256, 256);
	}
	
	@SubscribeEvent
	public void onTooltipRender(RenderTooltipEvent.PostBackground event) {
		ItemStack stack = event.getStack();
		if (stack.isEmpty()) {
			return;
		}

		Minecraft mc = Minecraft.getInstance();
		INostrumMagic attr = NostrumMagica.getMagicWrapper(mc.player);
		if (attr == null || !attr.isUnlocked()) {
			return; // no highlights
		}
		
		final MatrixStack matrixStackIn = event.getMatrixStack();
		
		// Lore icon
		final ILoreTagged tag;
		if (stack.getItem() instanceof BlockItem) {
			if (!(((BlockItem) stack.getItem()).getBlock() instanceof ILoreTagged)) {
				tag = null;
			} else {
				tag = (ILoreTagged) ((BlockItem) stack.getItem()).getBlock();
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
			
			matrixStackIn.push();
			matrixStackIn.translate(event.getX() + event.getWidth() - 4, event.getY() + event.getHeight() - 6, 500);
			renderLoreIcon(matrixStackIn, hasFullLore);
			matrixStackIn.pop();
		}
		
		// Enchantable?
		if (SpellAction.isEnchantable(stack)) {
			matrixStackIn.push();
			matrixStackIn.translate(event.getX() + event.getWidth() - 8, event.getY() - 16, 500);
			renderEnchantableIcon(matrixStackIn);
			matrixStackIn.pop();
		}
		
		// Configurable?
		if (ModificationTable.IsModifiable(stack)) {
			matrixStackIn.push();
			matrixStackIn.translate(event.getX() - 15, event.getY() + event.getHeight() - 8, 500);
			renderConfigurableIcon(matrixStackIn);
			matrixStackIn.pop();
		}
		
		// Transmutable?
		if (Transmutation.IsTransmutable(stack.getItem())) {
			matrixStackIn.push();
			matrixStackIn.translate(event.getX() - 15, event.getY() - 16, 500);
			renderTransmutableIcon(matrixStackIn);
			matrixStackIn.pop();
		}
	}
		
	
	@SubscribeEvent
	public void onTooltipRender(RenderTooltipEvent.PostText event) {
//		ItemStack stack = event.getStack();
//		if (stack.isEmpty() || !(stack.getItem() instanceof ILoreTagged)) {
//			return;
//		}
//		
//		INostrumMagic attr = NostrumMagica.getMagicWrapper(mc.player);
//		if (attr == null || !attr.isUnlocked()) {
//			return;
//		}
//		
//		ILoreTagged tag = (ILoreTagged) stack.getItem();
//		renderLoreIcon(attr.hasFullLore(tag));
	}
	
	protected void renderRoots(MatrixStack matrixStackIn, LivingEntity entity) {
		if (entity.ticksExisted % 4 == 0) {
			EffectData data = NostrumMagica.magicEffectProxy.getData(entity, SpecialEffect.ROOTED);
			if (data != null && data.getCount() != 0) {
				final ClientEffect effect = new ClientEffectAnimated(entity.getPositionVec(), 1000L,
						new ClientEffect[] {
							new ClientEffect(Vector3d.ZERO, new ClientEffectFormBasic(ClientEffectIcon.THORN_0, 0, 0, 0), 1500L),
							new ClientEffect(Vector3d.ZERO, new ClientEffectFormBasic(ClientEffectIcon.THORN_1, 0, 0, 0), 1500L),
							new ClientEffect(Vector3d.ZERO, new ClientEffectFormBasic(ClientEffectIcon.THORN_2, 0, 0, 0), 1500L),
							new ClientEffect(Vector3d.ZERO, new ClientEffectFormBasic(ClientEffectIcon.THORN_3, 0, 0, 0), 1500L),
							new ClientEffect(Vector3d.ZERO, new ClientEffectFormBasic(ClientEffectIcon.THORN_4, 0, 0, 0), 1500L),
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
	
	private boolean renderRecurseMarker = false;
	
	@SubscribeEvent
	public void onEntityRender(RenderLivingEvent.Post<LivingEntity, EntityModel<LivingEntity>> event) {
		if (!renderRecurseMarker) {
			final LivingEntity entity = event.getEntity();
			final MatrixStack matrixStackIn = event.getMatrixStack();
			//final float partialTicks = event.getPartialRenderTick();
			renderRecurseMarker = true;
			{
				renderRoots(matrixStackIn, entity);
				
//				// If selected entity, render with outline
//				if (MinecraftForgeClient.getRenderPass() == 0)
//				if (((ClientProxy) NostrumMagica.instance.proxy).getCurrentPet() == entity) {
//					
////					GlStateManager.depthFunc(GL11.GL_ALWAYS); //519
////					//event.getRenderer().setRenderOutlines(true);
////					final float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
////					event.getRenderer().doRender(entity, event.getX(), event.getY(), event.getZ(), yaw, partialTicks);
////					event.getRenderer().setRenderOutlines(false);
//					
//					//RenderFuncs.renderEntityOutline(entity, partialTicks); Want this to work so we can color and stuff :(
//				}
			}
			renderRecurseMarker = false;
		}
	}
	
	@SubscribeEvent
	public void onRenderLast(RenderWorldLastEvent event) {
		// Copy of what vanilla uses to figure out if it should render the render entity:
		Minecraft mc = Minecraft.getInstance();
		final boolean shouldRenderMe = (
				mc.gameSettings.getPointOfView() != PointOfView.FIRST_PERSON
				|| (mc.getRenderViewEntity() instanceof LivingEntity && ((LivingEntity)mc.getRenderViewEntity()).isSleeping())
				);
		
		if (!shouldRenderMe) {
			// Normal render didn't happen. Do rooted render manually instead.
			renderRoots(event.getMatrixStack(), NostrumMagica.instance.proxy.getPlayer());
		}
	}
	
	private static final Map<PlayerRenderer, Boolean> InjectedSet = new WeakHashMap<>();
	
	@SubscribeEvent
	public void onPlayerRender(RenderPlayerEvent.Post event) {
		if (!InjectedSet.containsKey(event.getRenderer())) {
			InjectedSet.put(event.getRenderer(), true);
			
			// EnderIO injects custom cape layer so that capes don't render if an elytra-like item is present. We won't bother.
			// Instead, we just inject a layer for our custom elytras, and another for dragon-flight wings
			event.getRenderer().addLayer(new LayerDragonFlightWings(event.getRenderer()));
			event.getRenderer().addLayer(new LayerAetherCloak(event.getRenderer()));
			event.getRenderer().addLayer(new LayerManaArmor(event.getRenderer()));
		}

		Minecraft mc = Minecraft.getInstance();
		if (event.getPlayer() != mc.player) {
			// For other players, possibly do armor render ticks
			for (@Nonnull ItemStack equipStack : event.getPlayer().getArmorInventoryList()) {
				if (equipStack.isEmpty() || !(equipStack.getItem() instanceof MagicArmor)) {
					continue;
				}
				
				((MagicArmor) equipStack.getItem()).onArmorTick(equipStack, event.getPlayer().world, event.getPlayer());
			}
		}
	}
	
	@SubscribeEvent
	public void onBlockHighlight(DrawHighlightEvent.HighlightBlock event) {
		if (event.isCanceled() || event.getTarget().getType() != RayTraceResult.Type.BLOCK) {
			return;
		}
		
		BlockState state = event.getInfo().getRenderViewEntity().world.getBlockState(RayTrace.blockPosFromResult(event.getTarget()));
		if (state == null) {
			return;
		}
		
		// Dungeon Air wants no overlay
		if (state.getBlock() instanceof DungeonAir) {
			if (!(event.getInfo().getRenderViewEntity() instanceof PlayerEntity)
					|| ((PlayerEntity) event.getInfo().getRenderViewEntity()).isCreative()) {
				event.setCanceled(true);
				return;
			}
		}
	}
	
	@SubscribeEvent
	public void onBlockOverlay(RenderBlockOverlayEvent event) {
		
//		// Forge overlays aren't set up. Have to do it manually. (Some copied from EnderIO)
//		if (!event.isCanceled() && event.getOverlayType() == OverlayType.WATER) {
//			final PlayerEntity player = event.getPlayer();
//			// the event has the wrong BlockPos (entity center instead of eyes)
//			final BlockPos blockpos = new BlockPos(player.getPosX(), player.getPosY() + player.getEyeHeight(), player.getPosZ());
//			final BlockState state = player.world.getBlockState(blockpos);
//			final Block block = state.getBlock();
//			final IFluidState fluidState = state.getFluidState();
//
//			if (block instanceof PoisonWaterBlock || (fluidState != null && fluidState.getFluid() instanceof FluidPoisonWater)) {
//				
//				
//				
//				BlockFluidBase fblock = (BlockFluidBase) block;
//				Vector3d fogColor = fblock.getFogColor(player.world, blockpos, state, player,
//						player.world.getFogColor(event.getRenderPartialTicks()),
//						event.getRenderPartialTicks());
//				float fogColorRed = (float) fogColor.x;
//				float fogColorGreen = (float) fogColor.y;
//				float fogColorBlue = (float) fogColor.z;
//				
//				final ResourceLocation r = fblock.getFluid().getOverlay();
//				if (r != null) {
//					mc.getTextureManager().bindTexture(
//							new ResourceLocation(r.getResourceDomain(), "textures/" + r.getResourcePath() + ".png")
//							);
//					Tessellator tessellator = Tessellator.getInstance();
//					BufferBuilder vertexbuffer = tessellator.getBuffer();
//					float f = player.getBrightness();
//					GlStateManager.color4f(f * fogColorRed, f * fogColorGreen, f * fogColorBlue, 0.5F);
//					GlStateManager.enableBlend();
//					GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
//							GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//					matrixStackIn.push();
//					float f7 = -player.rotationYaw / 64.0F;
//					float f8 = player.rotationPitch / 64.0F;
//					vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
//					vertexbuffer.pos(-1.0D, -1.0D, -0.5D).tex(4.0F + f7, 4.0F + f8).endVertex();
//					vertexbuffer.pos(1.0D, -1.0D, -0.5D).tex(0.0F + f7, 4.0F + f8).endVertex();
//					vertexbuffer.pos(1.0D, 1.0D, -0.5D).tex(0.0F + f7, 0.0F + f8).endVertex();
//					vertexbuffer.pos(-1.0D, 1.0D, -0.5D).tex(4.0F + f7, 0.0F + f8).endVertex();
//					tessellator.draw();
//					matrixStackIn.pop();
//					GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
//					GlStateManager.disableBlend();
//					
//					event.setCanceled(true);
//				}
//			}
//		}
	}
	
}
