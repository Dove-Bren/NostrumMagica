package com.smanzana.nostrummagica.client.overlay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attribute.IPrintableAttribute;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;
import com.smanzana.nostrummagica.block.dungeon.DungeonAirBlock;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.SpellIcon;
import com.smanzana.nostrummagica.client.listener.ClientPlayerListener;
import com.smanzana.nostrummagica.client.render.effect.CursedFireEffectRenderer;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.entity.dragon.ITameDragon;
import com.smanzana.nostrummagica.inventory.EquipmentSetRegistry;
import com.smanzana.nostrummagica.item.SpellTomePage;
import com.smanzana.nostrummagica.item.api.IRaytraceOverlay;
import com.smanzana.nostrummagica.item.equipment.HookshotItem;
import com.smanzana.nostrummagica.item.equipment.HookshotItem.HookshotType;
import com.smanzana.nostrummagica.item.set.EquipmentSet;
import com.smanzana.nostrummagica.listener.ClientChargeManager.ClientSpellCharge;
import com.smanzana.nostrummagica.listener.MagicEffectProxy.EffectData;
import com.smanzana.nostrummagica.listener.MagicEffectProxy.SpecialEffect;
import com.smanzana.nostrummagica.spell.RegisteredSpell;
import com.smanzana.nostrummagica.spell.SpellCasting;
import com.smanzana.nostrummagica.spell.SpellCooldownTracker;
import com.smanzana.nostrummagica.spell.SpellCooldownTracker.Cooldowns;
import com.smanzana.nostrummagica.spell.SpellCooldownTracker.SpellCooldown;
import com.smanzana.nostrummagica.util.ColorUtil;
import com.smanzana.nostrummagica.util.RayTrace;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.ChatFormatting;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class OverlayRenderer extends GuiComponent {
	
	private static final ResourceLocation GUI_ICONS = new ResourceLocation(NostrumMagica.MODID, "textures/gui/icons.png");
	private static final ResourceLocation GUI_CAST_CENTER = new ResourceLocation(NostrumMagica.MODID, "textures/gui/cast_swirl_hand.png");
	private static final ResourceLocation GUI_CAST_TAIL = new ResourceLocation(NostrumMagica.MODID, "textures/gui/cast_swirl_tail.png");
	private static final ResourceLocation OVERLAY_FROSTBITE = new ResourceLocation("textures/misc/powder_snow_outline.png"); // copied from vanilla, which has it private
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
	private static final int GUI_CAST_SWIRL_WIDTH = 48;
	private static final int GUI_CAST_SWIRL_HEIGHT = 48;
	private static final int GUI_SPELLCOOLDOWN_ICON_HOFFSET = 11;
	private static final int GUI_SPELLCOOLDOWN_ICON_VOFFSET = 57;
	private static final int GUI_SPELLCOOLDOWN_ICON_WIDTH = 16;
	private static final int GUI_SPELLCOOLDOWN_ICON_HEIGHT = 4;
	
	
	protected IIngameOverlay hookshotOverlay;
	protected IIngameOverlay cursedFireOverlay;
	protected IIngameOverlay spellSlideOverlay;
	protected ReagentTrackerOverlay reagentTrackerOverlay;
	protected IIngameOverlay manaOrbOverlay;
	protected IIngameOverlay manaBarOverlay;
	protected IIngameOverlay armorOverlay;
	protected IIngameOverlay magicResistOverlay;
	protected IIngameOverlay shieldHeartOverlay;
	protected IIngameOverlay traceOverlay;
	protected IIngameOverlay contingencyOverlay;
	protected IIngameOverlay mysticAirOverlay;
	protected IIngameOverlay spellChargeOverlay;
	protected IncantSelectionOverlay incantationSelectOverlay;
	protected IIngameOverlay spellCooldownOverlay;
	protected IIngameOverlay frostbiteOverlay;
	
	private int wiggleIndex; // set to multiples of 12 for each wiggle
	private static final int wiggleOffsets[] = {0, 1, 1, 2, 1, 1, 0, -1, -1, -2, -1, -1};
	
	private int wingIndex; // Controls mana wing animation. Set to -wingAnimDur to play backwards.
	private static final int wingAnimDur = 20;
	
	public OverlayRenderer() {
		MinecraftForge.EVENT_BUS.register(this);
		wiggleIndex = 0;
		wingIndex = 0;
	}
	
	public void registerLayers() {
		hookshotOverlay = OverlayRegistry.registerOverlayBelow(ForgeIngameGui.CROSSHAIR_ELEMENT, "NostrumMagica::hookshotOverlay", this::renderHookshotOverlay);
		cursedFireOverlay = OverlayRegistry.registerOverlayBottom("NostrumMagica::cursedFireOverlay", this::renderCursedFireOverlay);
		spellSlideOverlay = OverlayRegistry.registerOverlayAbove(ForgeIngameGui.EXPERIENCE_BAR_ELEMENT, "NostrumMagica::spellSlideOverlay", this::renderSpellSlide);
		reagentTrackerOverlay = new ReagentTrackerOverlay(); OverlayRegistry.registerOverlayAbove(ForgeIngameGui.EXPERIENCE_BAR_ELEMENT, "NostrumMagica::reagentTrackerOverlay", this.reagentTrackerOverlay);
		manaOrbOverlay = OverlayRegistry.registerOverlayAbove(ForgeIngameGui.EXPERIENCE_BAR_ELEMENT, "NostrumMagica::manaOrbOverlay", this::renderManaOrbOverlay);
		manaBarOverlay = OverlayRegistry.registerOverlayAbove(ForgeIngameGui.EXPERIENCE_BAR_ELEMENT, "NostrumMagica::manaBarOverlay", this::renderManaBarOverlay);
		armorOverlay = OverlayRegistry.registerOverlayAbove(ForgeIngameGui.ARMOR_LEVEL_ELEMENT, "NostrumMagica::armorOverlay", this::renderArmorOverlay);
		magicResistOverlay = OverlayRegistry.registerOverlayAbove(ForgeIngameGui.ARMOR_LEVEL_ELEMENT, "NostrumMagica::magicResistOverlay", this::renderMagicResistOverlay);
		shieldHeartOverlay = OverlayRegistry.registerOverlayAbove(ForgeIngameGui.PLAYER_HEALTH_ELEMENT, "NostrumMagica::shieldHeartOverlay", this::renderShieldOverlay);
		
		traceOverlay = OverlayRegistry.registerOverlayAbove(ForgeIngameGui.CROSSHAIR_ELEMENT, "NostrumMagica::traceOverlay", this::renderTraceOverlay);
		contingencyOverlay = OverlayRegistry.registerOverlayAbove(ForgeIngameGui.POTION_ICONS_ELEMENT, "NostrumMagica::contingencyOverlay", this::renderContingencyOverlay);
		mysticAirOverlay = OverlayRegistry.registerOverlayAbove(ForgeIngameGui.VIGNETTE_ELEMENT, "NostrumMagica::mysticAirOverlay", this::renderMysticAirOverlay);
		spellChargeOverlay = OverlayRegistry.registerOverlayAbove(ForgeIngameGui.CROSSHAIR_ELEMENT, "NostrumMagica::spellChargeOverlay", this::renderSpellChargeOverlay);
		incantationSelectOverlay = new IncantSelectionOverlay(); OverlayRegistry.registerOverlayTop("NostrumMagica::incantationSelectOverlay", incantationSelectOverlay);
		spellCooldownOverlay = OverlayRegistry.registerOverlayAbove(ForgeIngameGui.CROSSHAIR_ELEMENT, "NostrumMagica::spellCooldownOverlay", this::renderSpellCooldownOverlay);
		frostbiteOverlay = OverlayRegistry.registerOverlayAbove(ForgeIngameGui.FROSTBITE_ELEMENT, "", this::renderFrostbiteOverlay);
	}
	
	private void renderMysticAirOverlay(ForgeIngameGui gui, PoseStack matrixStackIn, float partialTicks, int width, int height) {
		final Minecraft mc = Minecraft.getInstance();
		final LocalPlayer player = mc.player;
		if (player == null || player.level == null) {
			return;
		}
		
		final int h = (int) player.getEyeHeight();
		BlockState inBlock = player.level.getBlockState(new BlockPos(player.getX(), player.getY() + h, player.getZ()));
		if (inBlock.getBlock() instanceof DungeonAirBlock airBlock) {
			final float[] color = ColorUtil.ARGBToColor(airBlock.getOverlayColor(inBlock));
			
			// Render dungeon air overlay
			{
				final Matrix4f transform = matrixStackIn.last().pose();
				Tesselator tessellator = Tesselator.getInstance();
				BufferBuilder bufferbuilder = tessellator.getBuilder();
				RenderSystem.enableBlend();
				RenderSystem.disableTexture();
				//GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
				final float depth = -91f;
				RenderSystem.setShader(GameRenderer::getPositionColorShader);
				bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
				bufferbuilder.vertex(transform, 0, height, depth).color(color[0], color[1], color[2], color[3]).endVertex();
				bufferbuilder.vertex(transform, width, height, depth).color(color[0], color[1], color[2], color[3]).endVertex();
				bufferbuilder.vertex(transform, width, 0, depth).color(color[0], color[1], color[2], color[3]).endVertex();
				bufferbuilder.vertex(transform, 0, 0, depth).color(color[0], color[1], color[2], color[3]).endVertex();
				bufferbuilder.end();
				BufferUploader.end(bufferbuilder);
				RenderSystem.enableTexture();
			}
		}
	}
	
	private void renderContingencyOverlay(ForgeIngameGui gui, PoseStack matrixStackIn, float partialTicks, int width, int height) {
		// TODO config option
		{
			final Minecraft mc = Minecraft.getInstance();
			final LocalPlayer player = mc.player;
			final int nowTicks = player.tickCount;
			int offsetX = 0;
			EffectData data = NostrumMagica.magicEffectProxy.getData(player, SpecialEffect.CONTINGENCY_DAMAGE);
			if (data != null) {
				if (data.getAmt() == 0) {
					data.amt(player.tickCount);
				}
				float timer = (float) (nowTicks - (int) data.getAmt()) / (float) data.getCount();
				timer = 1f - timer;
				renderContingencyShield(matrixStackIn, player, width, height, 0, offsetX, timer);
				offsetX++;
			}
			data = NostrumMagica.magicEffectProxy.getData(player, SpecialEffect.CONTINGENCY_MANA);
			if (data != null) {
				if (data.getAmt() == 0) {
					data.amt(player.tickCount);
				}
				float timer = (float) (nowTicks - (int) data.getAmt()) / (float) data.getCount();
				timer = 1f - timer;
				renderContingencyShield(matrixStackIn, player, width, height, 1, offsetX, timer);
				offsetX++;
			}
			data = NostrumMagica.magicEffectProxy.getData(player, SpecialEffect.CONTINGENCY_HEALTH);
			if (data != null) {
				if (data.getAmt() == 0) {
					data.amt(player.tickCount);
				}
				float timer = (float) (nowTicks - (int) data.getAmt()) / (float) data.getCount();
				timer = 1f - timer;
				renderContingencyShield(matrixStackIn, player, width, height, 2, offsetX, timer);
				offsetX++;
			}
			data = NostrumMagica.magicEffectProxy.getData(player, SpecialEffect.CONTINGENCY_FOOD);
			if (data != null) {
				if (data.getAmt() == 0) {
					data.amt(player.tickCount);
				}
				float timer = (float) (nowTicks - (int) data.getAmt()) / (float) data.getCount();
				timer = 1f - timer;
				renderContingencyShield(matrixStackIn, player, width, height, 3, offsetX, timer);
				offsetX++;
			}
		}
	}
	
	private void renderTraceOverlay(ForgeIngameGui gui, PoseStack matrixStackIn, float partialTicks, int width, int height) {
		final Minecraft mc = Minecraft.getInstance();
		final LocalPlayer player = mc.player;
		ItemStack held = player.getMainHandItem();
		if (held.isEmpty() || !(held.getItem() instanceof IRaytraceOverlay) || !((IRaytraceOverlay) held.getItem()).shouldTrace(player.level, player, held)) {
			held = player.getOffhandItem();
			if (held.isEmpty() || !(held.getItem() instanceof IRaytraceOverlay) || !((IRaytraceOverlay) held.getItem()).shouldTrace(player.level, player, held)) {
				held = ItemStack.EMPTY;
			}
		}
		
		if (!held.isEmpty()) {
			final double range = ((IRaytraceOverlay) held.getItem()).getTraceRange(player.level, player, held);
			HitResult result = RayTrace.raytraceApprox(player.level, player, player.getEyePosition(partialTicks),
					player.getXRot(), player.getYRot(), (float) range,
					new Predicate<Entity>() {

						@Override
						public boolean apply(Entity arg0) {
							return arg0 != null && arg0 != player && arg0 instanceof LivingEntity;
						}
				
			}, .5);
			Entity highlightEnt = RayTrace.entFromRaytrace(result);
			if (highlightEnt != null) {
				renderCrosshairTargetOverlay(matrixStackIn, player, width, height);
			}
		}
	}
	
	private void renderHookshotOverlay(ForgeIngameGui gui, PoseStack matrixStackIn, float partialTicks, int width, int height) {
		OverlayRegistry.enableOverlay(ForgeIngameGui.CROSSHAIR_ELEMENT, true);
		if (ModConfig.config.displayHookshotCrosshair()) {
			final Minecraft mc = Minecraft.getInstance();
			final LocalPlayer player = mc.player;
			ItemStack hookshot = player.getMainHandItem();
			if (hookshot.isEmpty() || !(hookshot.getItem() instanceof HookshotItem)) {
				hookshot = player.getOffhandItem();
			}
			
			if (hookshot.isEmpty() || !(hookshot.getItem() instanceof HookshotItem)) {
				return;
			}
			
			HookshotType type = HookshotItem.GetType(hookshot);
			HitResult result = RayTrace.raytrace(player.level, player, player.getEyePosition(partialTicks),
					player.getXRot(), player.getYRot(), (float) HookshotItem.GetMaxDistance(type),
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
					BlockState state = player.level.getBlockState(RayTrace.blockPosFromResult(result));
					if (state != null && HookshotItem.CanBeHooked(type, state)) {
						hit = true;
					}
				}
				
				if (hit) {
					OverlayRegistry.enableOverlay(ForgeIngameGui.CROSSHAIR_ELEMENT, false);
					renderHookshotCrosshair(matrixStackIn, player, width, height, entity);
				}
			}
			
		}
	}
	
	private void renderOrbsInternal(PoseStack matrixStackIn, int whole, int pieces, int x, int y, float red, float green, float blue, float alpha) {
		int i = 0;
		RenderSystem.setShaderTexture(0, GUI_ICONS);
		
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
	
	private void renderManaOrbOverlay(ForgeIngameGui gui, PoseStack matrixStackIn, float partialTicks, int width, int height) {
		final Minecraft mc = Minecraft.getInstance();
		final LocalPlayer player = mc.player;
		final INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		
		if (attr != null && attr.isUnlocked()
				&& ModConfig.config.displayManaOrbs()
				&& !player.isCreative()
				&& !player.isSpectator()) {
		
			int hudXAnchor = width / 2 + 89;
			int hudYAnchor = height - 49;
			
			hudYAnchor -= ModConfig.config.getManaSphereOffset() * 10;
			
			if (player.isEyeInFluid(FluidTags.WATER) || player.getAirSupply() < player.getMaxAirSupply()) {
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
				
				RenderSystem.setShaderTexture(0, GUI_ICONS);
				
				matrixStackIn.pushPose();
				matrixStackIn.translate(0, 0, -.5f); // Behind
				matrixStackIn.translate(hudXAnchor + wiggleOffset - 1, hudYAnchor + 3, 0);
				
				matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(rot));
				matrixStackIn.translate(-1, -10, 0);
				//blit(0, 0, GUI_WING_SIZE, GUI_WING_OFFSETY, GUI_WING_SIZE, GUI_WING_SIZE, red, green, blue, alpha); // -10, 10, 
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0, GUI_WING_SIZE,
						GUI_WING_OFFSETY, -GUI_WING_SIZE, GUI_WING_SIZE, 10, 10, 256f, 256f,
						1f, 1f, 1f, 1f - ratio);
				matrixStackIn.popPose();
				
				matrixStackIn.pushPose();
				matrixStackIn.translate(0, 0, -.5f); // Behind
				matrixStackIn.translate(hudXAnchor + wiggleOffset - 76, hudYAnchor + 3, 0);
				
				matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(-rot));
				matrixStackIn.translate(-10, -10, 0);
				//blit(0, 0, 0, offsetX, offsetY, width, height, texWidth, texHeight, red, green, blue, alpha); // -10, 10, 
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0, 0,
						GUI_WING_OFFSETY, GUI_WING_SIZE, GUI_WING_SIZE, 10, 10, 256f, 256,
						1f, 1f, 1f, 1f - ratio);
				matrixStackIn.popPose();
				
				
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
				int centerx = hudXAnchor - (5 * 8);
				String str = totalMana + "/" + totalMaxMana;
				int strWidth = mc.font.width(str);
				mc.font.draw(matrixStackIn,
						str, centerx - strWidth/2, hudYAnchor + 1, 0xFFFFFFFF);
			}
		}
	}
	
	private void renderManaBarOverlay(ForgeIngameGui gui, PoseStack matrixStackIn, float partialTicks, int width, int height) {
		final Minecraft mc = Minecraft.getInstance();
		final LocalPlayer player = mc.player;
		final INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		
		if (attr != null && attr.isUnlocked()
				&& ModConfig.config.displayManaBar()
				) {
		
			int hudXAnchor = width - (10 + GUI_BAR_WIDTH);
			int hudYAnchor = 10 + (GUI_BAR_HEIGHT);
			int displayHeight = (int) ((float) GUI_BAR_HEIGHT * Math.max(0f, Math.min(1f, (float) attr.getMana() / (float) attr.getMaxMana())));
			
			RenderSystem.enableBlend();
			RenderSystem.setShaderTexture(0, GUI_ICONS);
			this.blit(matrixStackIn, hudXAnchor, hudYAnchor - displayHeight, GUI_BAR_OFFSETX + GUI_BAR_WIDTH, (GUI_BAR_HEIGHT - displayHeight), GUI_BAR_WIDTH, displayHeight);
			this.blit(matrixStackIn, hudXAnchor, hudYAnchor - GUI_BAR_HEIGHT, GUI_BAR_OFFSETX, 0, GUI_BAR_WIDTH, GUI_BAR_HEIGHT);
			
			if (ModConfig.config.displayXPBar()) {
				displayHeight = (int) ((float) GUI_BAR_HEIGHT * Math.min(1f, Math.max(0f, attr.getXP() / attr.getMaxXP())));
				this.blit(matrixStackIn, hudXAnchor, hudYAnchor - displayHeight, GUI_BAR_OFFSETX + GUI_BAR_WIDTH + GUI_BAR_WIDTH, (GUI_BAR_HEIGHT - displayHeight), GUI_BAR_WIDTH, displayHeight);
			}
			RenderSystem.disableBlend();
			
			if (ModConfig.config.displayManaText()) {
				Font fonter = mc.font;
				int centerx = hudXAnchor + (int) (.5 * GUI_BAR_WIDTH);
				String str = "" + attr.getMana();
				int strWidth = fonter.width(str);
				fonter.draw(matrixStackIn, 
						str, centerx - strWidth/2, hudYAnchor - (int) (.66 * GUI_BAR_HEIGHT), 0xFFFFFFFF);
				
				str = "-";
				strWidth = fonter.width(str);
				fonter.draw(matrixStackIn, 
						str, centerx - strWidth/2, hudYAnchor - ((int) (.66 * GUI_BAR_HEIGHT) - fonter.lineHeight), 0xFFFFFFFF);
				
				str = "" + attr.getMaxMana();
				strWidth = fonter.width(str);
				fonter.draw(matrixStackIn, 
						str, centerx - strWidth/2, hudYAnchor - ((int) (.66 * GUI_BAR_HEIGHT) - (2 * fonter.lineHeight)), 0xFFFFFFFF);
			}
		}
	}
	
	private void renderSpellLoadoutSlot(PoseStack matrixStackIn, @Nullable RegisteredSpell spell, String binding, int width, int height) {
		// Probably going to draw icon, and spell name real small on top of it?
		RenderFuncs.drawRect(matrixStackIn, 0, 0, width, height, 0xFF000000);
		RenderFuncs.drawRect(matrixStackIn, 1, 1, width-1, height-1, 0x80404040);
		if (spell != null) {
			final Minecraft mc = Minecraft.getInstance();
			SpellIcon.get(spell.getIconIndex()).render(mc, matrixStackIn, 1, 1, width-2, height-2);
			
			// Name
			RenderFuncs.drawRect(matrixStackIn, 1, 1, width-1, (mc.font.lineHeight / 4) + 2, 0x80000000);
			String name = spell.getName();
			if (name.length() > 11) {
				name = name.substring(0, 9) + "...";
			}
			matrixStackIn.pushPose();
			matrixStackIn.translate(width/2, 1, 0);
			matrixStackIn.scale(.25f, .25f, 1f);
			mc.font.draw(matrixStackIn, name, -mc.font.width(name)/2, 0, 0xFFFFFFFF);
			matrixStackIn.popPose();
			
			// Keybind
			if (binding != null && binding.length() > 0) {
				RenderFuncs.drawRect(matrixStackIn, 1, height-1, width-1, height - (mc.font.lineHeight / 4) -2, 0x80000000);
				if (binding.length() > 11) {
					binding = binding.substring(0, 9) + "...";
				}
				matrixStackIn.pushPose();
				matrixStackIn.translate(width/2, (height - mc.font.lineHeight/4) - 1, 0);
				matrixStackIn.scale(.25f, .25f, 1f);
				mc.font.draw(matrixStackIn, binding, -mc.font.width(binding)/2, 0, 0xFFFFFFFF);
				matrixStackIn.popPose();
			}
			
			// Fade if can't cast it?
			
			// Show cooldown
			final SpellCooldown cooldown = NostrumMagica.instance.getSpellCooldownTracker(mc.player.level).getSpellCooldown(mc.player, spell);
			if (cooldown != null && cooldown.endTicks > mc.player.tickCount) {
				final float prog = Math.max(0f, (float) (mc.player.tickCount - cooldown.startTicks) / (cooldown.endTicks - cooldown.startTicks));
				final int progPixels = (int) ((height-2) * prog);
				RenderFuncs.drawRect(matrixStackIn, 1, 1 + progPixels, width-1, height-1, 0xDDDDDDDD);
			}
		}
	}
	
	private String getBindingForSlot(int slot) {
		String binding = null;
		ClientPlayerListener listener = (ClientPlayerListener) NostrumMagica.playerListener;
		@Nullable KeyMapping key = null;
		switch (slot) {
		case 0:
			key = listener.getBindingCast1();
			break;
		case 1:
			key = listener.getBindingCast2();
			break;
		case 2:
			key = listener.getBindingCast3();
			break;
		case 3:
			key = listener.getBindingCast4();
			break;
		case 4:
			key = listener.getBindingCast5();
			break;
		}
		
		if (key != null) {
			binding = key.getTranslatedKeyMessage().getString();
		}
		return binding;
	}
	
	private void renderSpellSlide(ForgeIngameGui gui, PoseStack matrixStackIn, float partialTicks, int width, int height) {
		final Minecraft mc = Minecraft.getInstance();
		final INostrumMagic attr = NostrumMagica.getMagicWrapper(mc.player);
		if (attr != null && attr.isUnlocked()) {
			// Bottom left spell slide
			// Spell name
			RegisteredSpell[] current = NostrumMagica.getCurrentSpellLoadout(mc.player);
			if (current != null && current.length != 0) {
				final int slotSize = 16;
				int slideHeight = slotSize + (4);
				
				final int slideWidthInSlots = Math.max(3, current.length);
				final int slideWidth = slotSize * slideWidthInSlots;
				
				// XP bar
				RenderFuncs.drawRect(matrixStackIn, 0, height - 4, slideWidth, height, 0xFF000000);
				RenderFuncs.drawRect(matrixStackIn, 1, height - 3, slideWidth - 1, height - 1, 0xFF808080);
				final int xpXOffset = Math.round((attr.getXP() / attr.getMaxXP()) * (slideWidth-2));
				if (xpXOffset > 0) {
					RenderFuncs.drawRect(matrixStackIn, 1, height - 3, 1 + xpXOffset, height - 1, 0xFFFFFF22);
				}
				
				// Spells
				final int xOffset = (slideWidthInSlots > current.length) ? ((slideWidthInSlots - current.length) * slotSize) / 2 : 0;
				final int yOffset = height - slideHeight;
				if (slideWidthInSlots > current.length) {
					RenderFuncs.drawGradientRect(matrixStackIn, 0, yOffset + (slideHeight/4), slideWidth, height - 4,
							0x00000000, 0x00000000, 0x80000000, 0x80000000);
				}
				for (int i = 0; i < current.length; i++) {
					matrixStackIn.pushPose();
					matrixStackIn.translate(xOffset + (i * slotSize), yOffset, 0);
					renderSpellLoadoutSlot(matrixStackIn, current[i], getBindingForSlot(i), slotSize, slotSize);
					matrixStackIn.popPose();
				}
			}
		}
	}
	
	private void renderMagicResistOverlay(ForgeIngameGui gui, PoseStack matrixStackIn, float partialTicks, int width, int height) {
		if (ModConfig.config.displayArmorOverlay() && gui.shouldDrawSurvivalElements()) {
			final Minecraft mc = Minecraft.getInstance();
			final LocalPlayer player = mc.player;
			
			int left_height = gui.left_height - 10;
	        int left = width / 2 - 91;
	        int top = height - left_height;
			
	        matrixStackIn.pushPose();
			RenderSystem.enableBlend();
			
			int level = (int) (player.getAttributeValue(NostrumAttributes.magicResist));
			level = Math.min(100, level); // cap at 100
			
			// Spread across 20 levels
			level /= 5;
			
			// have to render each half differently
	        for (int i = 0; i < level; i++)
	        {
	        	if (i % 2 == 0) {
	        		RenderFuncs.blit(matrixStackIn, left, top, 34, 9, 4, 3, 0.1f, .2f, 1f, .8f);
	        	} else {
	        		RenderFuncs.blit(matrixStackIn, left, top, 38, 9, 4, 3, .1f, .2f, 1f, .8f);
	        	}
	            left += 4;
	        }
			
			RenderSystem.disableBlend();
			matrixStackIn.popPose();
		}
	}
	
	private void renderArmorOverlay(ForgeIngameGui gui, PoseStack matrixStackIn, float partialTicks, int width, int height) {
		if (ModConfig.config.displayArmorOverlay() && gui.shouldDrawSurvivalElements()) {
			final Minecraft mc = Minecraft.getInstance();
			final LocalPlayer player = mc.player;
			
			int left_height = gui.left_height - 10;
	        int left = width / 2 - 91;
	        int top = height - left_height;
			
	        matrixStackIn.pushPose();
			RenderSystem.enableBlend();
			
			int level = player.getArmorValue();//ForgeHooks.getTotalArmorValue(player);
			level -= 20;
			
			// Stretch the last 5 to 100% out to fill the whole bar
			level = Math.min(20, level * 4);
	        for (int i = 0; i < level; i += 2)
	        {
	        	RenderFuncs.blit(matrixStackIn, left + 2, top + 4, 34 + 2, 9 + 4, 1, 4, 1f, .2f, .1f, .8f);
	        	RenderFuncs.blit(matrixStackIn, left + 6, top + 4, 34 + 6, 9 + 4, 1, 4, 1f, .2f, 1f, .8f);
	            left += 8;
	        }
			
			RenderSystem.disableBlend();
			matrixStackIn.popPose();
		}
	}
	
	private void renderShieldOverlay(ForgeIngameGui gui, PoseStack matrixStackIn, float partialTicks, int width, int height) {
		if (ModConfig.config.displayShieldHearts() && gui.shouldDrawSurvivalElements()) {
			final Minecraft mc = Minecraft.getInstance();
			final LocalPlayer player = mc.player;
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
	        int left = width / 2 - 91;
	        int top = height - left_height;
	        int whole = (int) Math.ceil(physical) / 2;
	        boolean half = Math.ceil(physical) % 2 == 1;
	        
	        if (physical > 0 || magical > 0) {
		        matrixStackIn.pushPose();
				RenderSystem.enableBlend();
				RenderSystem.setShaderTexture(0, GUI_ICONS);
				
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
		        left = width / 2 - 91;
		        
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
				matrixStackIn.popPose();
	        }
		}
	}
	
	private void renderHookshotCrosshair(PoseStack matrixStackIn, LocalPlayer player, int width, int height, boolean entity) {
		final float red;
		final float green;
		final float blue;
		final float alpha;
		matrixStackIn.pushPose();
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
		RenderSystem.setShaderTexture(0, GUI_ICONS);
		
		final int period = 30;
		final float frac = (float) (player.level.getGameTime() % period) / period;
		final double radius = 6.0 + 2.0 * (Math.sin(frac * Math.PI * 2));
		
		final float rotOffset;
		if (entity) {
			rotOffset = 360.0f * ((float) (player.level.getGameTime() % period) / period);
		} else {
			rotOffset = 0f;
		}
		float rot;
		
		for (int i = 0; i < 3; i++) {
			rot = rotOffset + (360.0f / 3) * i;
			matrixStackIn.pushPose();
			matrixStackIn.translate(width / 2, height / 2, 0);
			
			matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(rot));
			matrixStackIn.translate(0, -radius, 0);
			
			if (player.isShiftKeyDown()) {
				matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(180f));
				matrixStackIn.translate(-GUI_HOOKSHOT_CROSSHAIR_WIDTH / 2, 0, 0);
			} else {
				matrixStackIn.translate(-GUI_HOOKSHOT_CROSSHAIR_WIDTH / 2, -GUI_HOOKSHOT_CROSSHAIR_WIDTH, 0);
			}
			
			RenderFuncs.blit(matrixStackIn, 0, 0,
					GUI_HOOKSHOT_CROSSHAIR_OFFSETX, 0, GUI_HOOKSHOT_CROSSHAIR_WIDTH, GUI_HOOKSHOT_CROSSHAIR_WIDTH,
					red, green, blue, alpha);
			
			matrixStackIn.popPose();
		}
		
		RenderSystem.disableBlend();
		matrixStackIn.popPose();
	}
	
	private void renderCrosshairTargetOverlay(PoseStack matrixStackIn, LocalPlayer player, int width, int height) {
		matrixStackIn.pushPose();
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.setShaderTexture(0, GUI_ICONS);
		
		matrixStackIn.translate(width / 2, height / 2, 0);
		
		matrixStackIn.translate(-GUI_TARGET_CROSSHAIR_WIDTH / 2, -(GUI_TARGET_CROSSHAIR_WIDTH / 2), 0);
		
		matrixStackIn.translate(0, -1, 0);
		
		RenderFuncs.blit(matrixStackIn, 0, 0,
				0, GUI_TARGET_CROSSHAIR_OFFSETY, GUI_TARGET_CROSSHAIR_WIDTH, GUI_TARGET_CROSSHAIR_WIDTH,
				.5f, .5f, .5f, .9f);
		
		RenderSystem.disableBlend();
		matrixStackIn.popPose();
	}
	
	private void renderContingencyShield(PoseStack matrixStackIn, LocalPlayer player, int guiWidth, int guiHeight, int typeOffset, int xoffset, float timer) {
		final int left = (guiWidth / 2 + 91) + 10 + (xoffset * GUI_CONTINGENCY_ICON_LENGTH);
		final int top = guiHeight - (2 + GUI_CONTINGENCY_ICON_LENGTH);
		final float borderScale = 1.07f;
		final BufferBuilder buffer = Tesselator.getInstance().getBuilder();
		final int width = GUI_CONTINGENCY_ICON_LENGTH; // for readability
		final int height = GUI_CONTINGENCY_ICON_LENGTH; // for readability
		
		if (timer <= 0) {
			return;
		}
		
		matrixStackIn.pushPose();
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
		RenderSystem.setShaderTexture(0, GUI_ICONS);
		
		matrixStackIn.translate(left, top, 0);
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(-.5f, -.5f, 0);
		matrixStackIn.scale(borderScale, borderScale, borderScale);
		{
			final Matrix4f transform = matrixStackIn.last().pose();
			buffer.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR_TEX);
			
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
			
			buffer.vertex(transform, width/2f, height/2f, 0).color(1f, .25f, .3f, 1f).uv((uMin + uMax) / 2f, (vMin + vMax) / 2f).endVertex();
			
			for (int i = 0; i <= fullTris; i++) {
				buffer.vertex(transform, coords[i][0], coords[i][1], coords[i][2]).color(1f, .25f, .3f, 1f).uv(coords[i][3], coords[i][4]).endVertex();
			}
			
			// draw partial
			if (fullTris < 4) {
				final float partial = (timer - (fullTris * .25f)) * 4;
				buffer
					.vertex(transform, coords[fullTris][0] * (1f-partial) + coords[fullTris+1][0] * partial,
						 coords[fullTris][1] * (1f-partial) + coords[fullTris+1][1] * partial,
						 coords[fullTris][2] * (1f-partial) + coords[fullTris+1][2] * partial)
					.color(1f, .25f, .3f, 1f)
					.uv(coords[fullTris][3] * (1f-partial) + coords[fullTris+1][3] * partial,
						 coords[fullTris][4] * (1f-partial) + coords[fullTris+1][4] * partial)
					.endVertex();
			}
			
			
			Tesselator.getInstance().end();
		}
		
//		blit(0, 0,
//				GUI_CONTINGENCY_ICON_OFFSETX + (3 * GUI_CONTINGENCY_ICON_LENGTH),
//				GUI_CONTINGENCY_ICON_OFFSETY - GUI_CONTINGENCY_ICON_LENGTH, GUI_CONTINGENCY_ICON_LENGTH, GUI_CONTINGENCY_ICON_LENGTH);
		matrixStackIn.popPose();

		RenderFuncs.blit(matrixStackIn, 0, 0,
				GUI_CONTINGENCY_ICON_OFFSETX + (typeOffset * GUI_CONTINGENCY_ICON_LENGTH),
				GUI_CONTINGENCY_ICON_OFFSETY, GUI_CONTINGENCY_ICON_LENGTH, GUI_CONTINGENCY_ICON_LENGTH,
				.5f, .5f, .5f, 1f);
		
		RenderSystem.disableBlend();
		matrixStackIn.popPose();
	}
	
	private void renderCursedFireOverlay(ForgeIngameGui gui, PoseStack matrixStackIn, float partialTicks, int width, int height) {
		final Minecraft mc = Minecraft.getInstance();
		final LocalPlayer player = mc.player;
		if (player.getEffect(NostrumEffects.cursedFire) != null) {
		
			matrixStackIn.pushPose();
			matrixStackIn.translate(width *.2f, height * .95f, -500);
			
			matrixStackIn.pushPose();
			matrixStackIn.scale(width, width, 1);
			CursedFireEffectRenderer.renderFire(matrixStackIn, mc.renderBuffers().bufferSource(), CursedFireEffectRenderer.TEX_FIRE_0.sprite(), RenderFuncs.BrightPackedLight, 1f, 1f, 1f, 1f, 1f);
			matrixStackIn.popPose();
			
			matrixStackIn.pushPose();
			matrixStackIn.translate(width*.6f, 0, 0);
			matrixStackIn.scale(width, width, 1);
			CursedFireEffectRenderer.renderFire(matrixStackIn, mc.renderBuffers().bufferSource(), CursedFireEffectRenderer.TEX_FIRE_1.sprite(), RenderFuncs.BrightPackedLight, 1f, 1f, 1f, 1f, 1f);
			matrixStackIn.popPose();
			
			mc.renderBuffers().bufferSource().endBatch();
			matrixStackIn.popPose();
		}
	}
	
	private void renderSpellChargeOverlay(ForgeIngameGui gui, PoseStack matrixStackIn, float partialTicks, int width, int height) {
		ClientPlayerListener listener = (ClientPlayerListener) NostrumMagica.playerListener;
		final ClientSpellCharge charge = listener.getChargeManager().getCurrentCharge();
		if (charge != null) {
			final float progress = listener.getChargeManager().getChargePercent();
			final float[] colorBase = ColorUtil.ARGBToColor(charge.charge.spell().getPrimaryElement().getColor());
			
			final int iconwidth = 16;
			final int iconheight = 16; 
			
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
			
			matrixStackIn.pushPose();
			matrixStackIn.translate(width/2, (height/2) + 12, 0);
			
			RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			RenderSystem.setShaderTexture(0, NostrumMagica.Loc("textures/gui/cast_swirl_background.png"));
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, -iconwidth/2, -iconheight/2,
					0, 0, GUI_CAST_SWIRL_WIDTH, GUI_CAST_SWIRL_HEIGHT, iconwidth, iconheight, GUI_CAST_SWIRL_WIDTH, GUI_CAST_SWIRL_HEIGHT,
					.8f, .8f, .8f, .5f);
			
			RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			RenderSystem.setShaderTexture(0, GUI_CAST_CENTER);
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, -iconwidth/2, -iconheight/2,
					0, 0, GUI_CAST_SWIRL_WIDTH, GUI_CAST_SWIRL_HEIGHT, iconwidth, iconheight, GUI_CAST_SWIRL_WIDTH, GUI_CAST_SWIRL_HEIGHT,
					colorBase[0] * .5f, colorBase[1] * .5f, colorBase[2] * .5f, 1f);
			
			RenderSystem.setShaderTexture(0, GUI_CAST_TAIL);
			
			final double glowPeriod = 1000;
			final float glowProg = (float)((System.currentTimeMillis() % glowPeriod) / glowPeriod);
			
			Function<Float, float[]> color = (f) -> {
				final float thisProg = (f - glowProg) % 1f;
				final float glow = .4f + .4f * Mth.sin(thisProg * 2 * Mth.PI);
				return new float[] {colorBase[0] * glow, colorBase[1] * glow, colorBase[2] * glow, 1f};
			};
			RenderFuncs.drawRadialProgressQuadImmediate(matrixStackIn, -iconwidth/2, -iconheight/2, 0, iconwidth, iconheight, 0, 1, 0, 1, progress, color);
			
			final float elapsedTicks = (float) charge.charge.duration() - ((float) listener.getChargeManager().getRemainingTicks());
			
			// Flash cast info for first part
			if (elapsedTicks < 40) {
				final float infoAlpha = Mth.sin(Mth.PI * (Math.min(40f, elapsedTicks) / 40f));
				
				final int overcharge = charge.charge.overchargeCount();
				if (overcharge == 0) { // No overcharge, so start info about rate
					
				
					// Rate
					final float chargeSpeed = charge.chargeSpeed;//-(SpellCasting.CalculateHandsSpellCastModifier(NostrumMagica.Proxy.getPlayer()) - 1f);
					if (chargeSpeed != 0f) {
						matrixStackIn.pushPose();
						matrixStackIn.translate(0, 12, 0);
						matrixStackIn.scale(.5f, .5f, 1f);
						final String rate = String.format("%+.0f%%", chargeSpeed * 100);
						final int len = gui.getFont().width(rate);
						gui.getFont().draw(matrixStackIn, rate, -len / 2, 0, RenderFuncs.ARGBFade(0xFFFFFFFF, infoAlpha));
						matrixStackIn.popPose();
					
					
						RenderSystem.setShaderColor(1f, 1f, 0f, infoAlpha);
						// Items
						if (!charge.mainhandItem.isEmpty() && !SpellCasting.ItemAllowsCasting(charge.mainhandItem, EquipmentSlot.MAINHAND)) {
							matrixStackIn.pushPose();
							matrixStackIn.translate(4, 20, 0);
							matrixStackIn.scale(.25f, .25f, .25f);
							RenderFuncs.RenderGUIItem(charge.mainhandItem, matrixStackIn, -8, -8, -1);
							matrixStackIn.popPose();
						}
						
						if (!charge.offhandItem.isEmpty() && !SpellCasting.ItemAllowsCasting(charge.offhandItem, EquipmentSlot.OFFHAND)) {
							matrixStackIn.pushPose();
							matrixStackIn.translate(-4, 20, 0);
							matrixStackIn.scale(.25f, .25f, .25f);
							RenderFuncs.RenderGUIItem(charge.offhandItem, matrixStackIn, -8, -8, -1);
							matrixStackIn.popPose();
						}
					}
					RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
				} else {
					// Overcharging. Don't redisplay speed info, but show info about overcharging
					matrixStackIn.pushPose();
					matrixStackIn.translate(0, 12, 0);
					matrixStackIn.scale(.5f, .5f, 1f);
					final String rate = "Charge %s".formatted(SpellTomePage.toRoman(overcharge + 1));
					final int len = gui.getFont().width(rate);
					gui.getFont().draw(matrixStackIn, rate, -len / 2, 0, RenderFuncs.ARGBFade(0xFFFFFFFF, infoAlpha));
					matrixStackIn.popPose();
				}
				
			}
			
			matrixStackIn.popPose();
		}
	}
	
	private void renderSpellCooldownOverlay(ForgeIngameGui gui, PoseStack matrixStackIn, float partialTicks, int width, int height) {
		final Player player = NostrumMagica.Proxy.getPlayer();
		final SpellCooldownTracker tracker = NostrumMagica.instance.getSpellCooldownTracker(player.getLevel());
		final Cooldowns cooldowns = tracker.getCooldowns(player);
		if (cooldowns != null && cooldowns.getGlobalCooldown().endTicks > player.tickCount) {
			final int totalCooldownTicks = cooldowns.getGlobalCooldown().endTicks - cooldowns.getGlobalCooldown().startTicks;
			final int ticksElapsed = player.tickCount - cooldowns.getGlobalCooldown().startTicks;
			final float cooldownProg = (float) ticksElapsed / (float) totalCooldownTicks;
			final Minecraft mc = Minecraft.getInstance();
			
			final boolean hasAttackCooldown = mc.options.attackIndicator == AttackIndicatorStatus.CROSSHAIR && player.getAttackStrengthScale(0.0F) < 1f;
			final int y = (height / 2 - 7 + 16) + (hasAttackCooldown ? 8 : 0);
			final int x = width / 2 - 8;
			
			final int w = Math.round((float)GUI_SPELLCOOLDOWN_ICON_WIDTH * cooldownProg);
			
			RenderSystem.enableBlend();
			RenderSystem.setShaderTexture(0, GUI_ICONS);
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			this.blit(matrixStackIn, x, y, GUI_SPELLCOOLDOWN_ICON_HOFFSET, GUI_SPELLCOOLDOWN_ICON_VOFFSET, GUI_SPELLCOOLDOWN_ICON_WIDTH, GUI_SPELLCOOLDOWN_ICON_HEIGHT);
			this.blit(matrixStackIn, x, y, GUI_SPELLCOOLDOWN_ICON_HOFFSET + GUI_SPELLCOOLDOWN_ICON_WIDTH, GUI_SPELLCOOLDOWN_ICON_VOFFSET, w, GUI_SPELLCOOLDOWN_ICON_HEIGHT);
			RenderSystem.defaultBlendFunc();
		}
	}
	
	private void renderFrostbiteOverlay(ForgeIngameGui gui, PoseStack matrixStackIn, float partialTicks, int width, int height) {
		final Player player = NostrumMagica.Proxy.getPlayer();
		final MobEffectInstance effect = player.getEffect(NostrumEffects.frostbite);
		if (effect != null && effect.getDuration() > 0) {
			final float alpha = effect.getDuration() > 20 ? 1f : ((float)effect.getDuration()/20f);
			gui.setupOverlayRenderState(true, false);
			//gui.renderTextureOverlay(OVERLAY_FROSTBITE, alpha); private :(
			{
				RenderSystem.disableDepthTest();
				RenderSystem.depthMask(false);
				RenderSystem.defaultBlendFunc();
				RenderSystem.setShader(GameRenderer::getPositionTexShader);
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
				RenderSystem.setShaderTexture(0, OVERLAY_FROSTBITE);
				Tesselator tesselator = Tesselator.getInstance();
				BufferBuilder bufferbuilder = tesselator.getBuilder();
				bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
				bufferbuilder.vertex(0.0D, (double)height, -90.0D).uv(0.0F, 1.0F).endVertex();
				bufferbuilder.vertex((double)width, (double)height, -90.0D).uv(1.0F, 1.0F).endVertex();
				bufferbuilder.vertex((double)width, 0.0D, -90.0D).uv(1.0F, 0.0F).endVertex();
				bufferbuilder.vertex(0.0D, 0.0D, -90.0D).uv(0.0F, 0.0F).endVertex();
				tesselator.end();
				RenderSystem.depthMask(true);
				RenderSystem.enableDepthTest();
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			}
		}
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
	
	public void toggleHUD() {
		this.reagentTrackerOverlay.toggleTracker();
	}
	
	public void enableIncantationSelection() {
		if (!this.incantationSelectOverlay.isEnabled()) {
			this.incantationSelectOverlay.enableSelection(true);
		}
	}
	
	private static ItemStack EquipmentSetCacheKey = ItemStack.EMPTY;
	private static List<EquipmentSet> EquipmentSetCacheSets = new ArrayList<>();
	
	protected @Nullable List<EquipmentSet> GetSetsForItem(ItemStack stack) {
		if (ItemStack.matches(stack, EquipmentSetCacheKey)) {
			return EquipmentSetCacheSets;
		}
		
		EquipmentSetCacheSets = new ArrayList<>();
		for (EquipmentSet set : EquipmentSetRegistry.GetAllSets()) {
			if (set.isSetItem(stack)) {
				EquipmentSetCacheSets.add(set);
			}
		}
		
		EquipmentSetCacheKey = stack.copy();
		return EquipmentSetCacheSets;
	}
	
	@SubscribeEvent
	public void onTooltipConstruct(ItemTooltipEvent event) {
		// if a set item, insert set text
		List<EquipmentSet> sets = GetSetsForItem(event.getItemStack());
		if (!sets.isEmpty()) {
			final Minecraft mc = Minecraft.getInstance();
			final Player player = mc.player;
			
			List<Component> lines = event.getToolTip();
			for (EquipmentSet set : sets) {
				lines.addAll(1, makeSetLines(event.getItemStack(), player, set));
			}
		}
	}
	
	protected List<Component> makeSetLines(ItemStack stack, Player player, EquipmentSet set) {
		List<Component> lines = new ArrayList<>();
		final boolean showFull = Screen.hasShiftDown();
		final int count = NostrumMagica.itemSetListener.getActiveSetCount(player, set);
		final int maxCount = set.getFullSetCount();
		final String countStr = (showFull ? " (Complete)" : String.format(" (%d/%d)", count, maxCount));
		
		lines.add(set.getName().copy().withStyle(ChatFormatting.DARK_PURPLE)
				.append(new TextComponent(countStr).withStyle((showFull || count >= maxCount) ? ChatFormatting.GOLD : ChatFormatting.DARK_AQUA)));

		// If player is wearing the set, display bonuses
		if (showFull || NostrumMagica.itemSetListener.getCurrentSets(player).contains(set)) {
			final Multimap<Attribute, AttributeModifier> attribs;
			if (showFull) {
				attribs = set.getFullSetBonuses();
			} else {
				// Show current
				attribs = NostrumMagica.itemSetListener.getActiveSetBonus(player, set);
			}

			if (attribs != null && !attribs.isEmpty()) {
				for (Entry<Attribute, AttributeModifier> entry : attribs.entries()) {
					AttributeModifier modifier = entry.getValue();
					
					if (entry.getKey() instanceof IPrintableAttribute) {
						Component line = ((IPrintableAttribute) entry.getKey()).formatModifier(modifier);
						if (line != null) {
							lines.add(line);
						}
					} else {
						Component line = IPrintableAttribute.formatAttributeValueVanilla(entry.getKey(), modifier);
						if (line != null) {
							lines.add(line);
						}
					}
				}
			}
			
			List<Component> extras = set.getExtraBonuses(showFull ? maxCount : count);
			if (extras != null && !extras.isEmpty()) {
				extras.forEach(t -> lines.add(t.copy().withStyle(ChatFormatting.DARK_AQUA)));
			}
		}
		return lines;
	}
}
