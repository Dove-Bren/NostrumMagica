package com.smanzana.nostrummagica.client.overlay;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;

import com.google.common.base.Predicate;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
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
import com.smanzana.nostrummagica.client.render.LayerAetherCloak;
import com.smanzana.nostrummagica.client.render.LayerCustomElytra;
import com.smanzana.nostrummagica.client.render.LayerDragonFlightWings;
import com.smanzana.nostrummagica.client.render.LayerManaArmor;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.entity.IEntityPet;
import com.smanzana.nostrummagica.entity.dragon.ITameDragon;
import com.smanzana.nostrummagica.items.MagicArmor;
import com.smanzana.nostrummagica.items.HookshotItem;
import com.smanzana.nostrummagica.items.HookshotItem.HookshotType;
import com.smanzana.nostrummagica.items.IRaytraceOverlay;
import com.smanzana.nostrummagica.items.NostrumItems;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy.EffectData;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy.SpecialEffect;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.pet.PetInfo;
import com.smanzana.nostrummagica.pet.PetInfo.PetAction;
import com.smanzana.nostrummagica.pet.PetInfo.SecondaryFlavor;
import com.smanzana.nostrummagica.pet.PetPlacementMode;
import com.smanzana.nostrummagica.pet.PetTargetMode;
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
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
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
	
	private static final ResourceLocation GUI_PET_ICONS = new ResourceLocation(NostrumMagica.MODID, "textures/gui/pet_icons.png");
	//private static final int GUI_PET_ICONS_DIMS = 256;
	private static final int GUI_PET_ICON_DIMS = 32;
	private static final int GUI_PET_ICON_TARGET_HOFFSET = 0;
	private static final int GUI_PET_ICON_TARGET_VOFFSET = 0;
	private static final int GUI_PET_ICON_PLACEMENT_HOFFSET = 0;
	private static final int GUI_PET_ICON_PLACEMENT_VOFFSET = GUI_PET_ICON_DIMS;
	
	private int wiggleIndex; // set to multiples of 12 for each wiggle
	private static final int wiggleOffsets[] = {0, 1, 1, 2, 1, 1, 0, -1, -1, -2, -1, -1};
	
	private int wingIndex; // Controls mana wing animation. Set to -wingAnimDur to play backwards.
	private static final int wingAnimDur = 20;
	
	private int petTargetIndex; // Controls displaying pet target icon (fade in/out 50%)
	private int petTargetAnimDur = 80;
	private int petPlacementIndex; // Controls displaying pet placement icon (fade in/out at 50%)
	private int petPlacementAnimDur = 80;
	
	public OverlayRenderer() {
		MinecraftForge.EVENT_BUS.register(this);
		wiggleIndex = 0;
		wingIndex = 0;
		petTargetIndex = -1;
		petPlacementIndex = -1;
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
						renderHookshotCrosshair(player, window, entity);
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
		
		if (event.getType() == ElementType.EXPERIENCE) {
			// We do mana stuff in experience layer
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr == null || !attr.isUnlocked()) {
				return;
			}
			
			renderSpellSlide(player, window, attr);
			
			if (mc.player.isCreative()
					|| mc.player.isSpectator()) {
				return;
			}
			
			// Orbs
			if (ModConfig.config.displayManaOrbs()) {
				renderManaOrbs(player, window, attr);
			}
			
			// Mana bar
			if (ModConfig.config.displayManaBar()) {
				renderManaBar(player, window, attr);
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
				xOffset = window.getScaledWidth() - (2 + healthbarWidth);
				
				List<ITameDragon> dragons = NostrumMagica.getNearbyTamedDragons(player, 32, true);
				Collections.sort(dragons, (left, right) -> {
					return ((LivingEntity) (left)).getUniqueID().compareTo(((LivingEntity) right).getUniqueID());
				});
				for (ITameDragon dragon : dragons) {
					if (dragon instanceof LivingEntity) {
						renderHealthbarOrb(player, window, (LivingEntity) dragon, xOffset, y, scale);
						y += healthbarHeight + 2;
					}
				}
			}
			
			// Pet info
			if (ModConfig.config.displayPetHealthbars()) {
				healthbarWidth = (int) (GUI_HEALTHBAR_BOX_BACK_WIDTH * scale);
				healthbarHeight = (int) (GUI_HEALTHBAR_BOX_BACK_HEIGHT * scale);
				xOffset = window.getScaledWidth() - (2 + healthbarWidth);
				final boolean hideDragons = ModConfig.config.displayDragonHealthbars();
				for (LivingEntity tamed : NostrumMagica.getTamedEntities(player)) {
					if (hideDragons && tamed instanceof ITameDragon) {
						continue;
					}
					renderHealthbarBox(player, window, tamed, xOffset, y, scale);
					y += healthbarHeight;
				}
			}
		} else if (event.getType() == ElementType.ARMOR) {
			if (ModConfig.config.displayArmorOverlay()) {
				renderArmorOverlay(player, window);
			}
		} else if (event.getType() == ElementType.FOOD) {
			if (ModConfig.config.displayShieldHearts()) {
				renderShieldOverlay(player, window);
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
						renderCrosshairTargetOverlay(player, window);
					}
				}
			}
			
			final float ticks = player.ticksExisted + event.getPartialTicks();
			if (petTargetIndex >= 0) {
				PetTargetMode mode = NostrumMagica.instance.getPetCommandManager().getTargetMode(player);
				renderPetActionTargetMode(player, window, mode, (ticks - petTargetIndex) / (float) petTargetAnimDur);
				
				if (ticks >= petTargetIndex + petTargetAnimDur) {
					petTargetIndex = -1;
				}
			}
			
			if (petPlacementIndex >= 0) {
				PetPlacementMode mode = NostrumMagica.instance.getPetCommandManager().getPlacementMode(player);
				renderPetActionPlacementMode(player, window, mode, (ticks - petPlacementIndex) / (float) petPlacementAnimDur);
				
				if (ticks >= petPlacementIndex + petPlacementAnimDur) {
					petPlacementIndex = -1;
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
					renderContingencyShield(player, window, 0, offsetX, timer);
					offsetX++;
				}
				data = NostrumMagica.magicEffectProxy.getData(player, SpecialEffect.CONTINGENCY_MANA);
				if (data != null) {
					if (data.getAmt() == 0) {
						data.amt(player.ticksExisted);
					}
					float timer = (float) (nowTicks - (int) data.getAmt()) / (float) data.getCount();
					timer = 1f - timer;
					renderContingencyShield(player, window, 1, offsetX, timer);
					offsetX++;
				}
				data = NostrumMagica.magicEffectProxy.getData(player, SpecialEffect.CONTINGENCY_HEALTH);
				if (data != null) {
					if (data.getAmt() == 0) {
						data.amt(player.ticksExisted);
					}
					float timer = (float) (nowTicks - (int) data.getAmt()) / (float) data.getCount();
					timer = 1f - timer;
					renderContingencyShield(player, window, 2, offsetX, timer);
					offsetX++;
				}
				data = NostrumMagica.magicEffectProxy.getData(player, SpecialEffect.CONTINGENCY_FOOD);
				if (data != null) {
					if (data.getAmt() == 0) {
						data.amt(player.ticksExisted);
					}
					float timer = (float) (nowTicks - (int) data.getAmt()) / (float) data.getCount();
					timer = 1f - timer;
					renderContingencyShield(player, window, 3, offsetX, timer);
					offsetX++;
				}
			}
		} else if (event.getType() == ElementType.VIGNETTE) {
			if (player == null || player.world == null) {
				return;
			}
			
			final int h = (int) player.getEyeHeight();
			BlockState inBlock = player.world.getBlockState(new BlockPos(player.posX, player.posY + h, player.posZ));
			if (inBlock.getBlock() instanceof DungeonAir) {
				// Render dungeon air overlay
				{
					Tessellator tessellator = Tessellator.getInstance();
					BufferBuilder bufferbuilder = tessellator.getBuffer();
					GlStateManager.enableBlend();
					GlStateManager.disableTexture();
					//GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
					GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
					GlStateManager.color4f(.3f, 0, .3f, .2f);
					final double depth = -91D;
					bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
					bufferbuilder.pos(0, event.getWindow().getScaledHeight(), depth).endVertex();
					bufferbuilder.pos(event.getWindow().getScaledWidth(), event.getWindow().getScaledHeight(), depth).endVertex();
					bufferbuilder.pos(event.getWindow().getScaledWidth(), 0, depth).endVertex();
					bufferbuilder.pos(0, 0, depth).endVertex();
					tessellator.draw();
					GlStateManager.enableTexture();
					GlStateManager.color4f(1f, 1f, 1f, 1f);
				}
			}
		}
	}
	
	private void renderOrbsInternal(int whole, int pieces, int x, int y) {
		int i = 0;
		Minecraft mc = Minecraft.getInstance();
		mc.getTextureManager().bindTexture(GUI_ICONS);
		for (; i < whole; i++) {
			// Draw a single partle orb
//			this.blit(x - (8 * (i + 1)),
//					y,	36, 16, GUI_ORB_WIDTH, GUI_ORB_HEIGHT);
			this.blit(x - (8 * (i + 1)),
					y,	36, 16, GUI_ORB_WIDTH, GUI_ORB_HEIGHT);
		}
		
		if (pieces != 0) {
			// Draw a single partle orb
			this.blit(x - (8 * (i + 1)),
					y, GUI_ORB_WIDTH * pieces, 16, GUI_ORB_WIDTH, GUI_ORB_HEIGHT);
			i++;
		}
		
		for (; i < 10; i++) {
			this.blit(x - (8 * (i + 1)),
					y,	0, 16, GUI_ORB_WIDTH, GUI_ORB_HEIGHT);
		}
		
	}
	
	private void renderManaOrbs(ClientPlayerEntity player, MainWindow window, INostrumMagic attr) {
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
		GlStateManager.color4f(.4f, .4f, .4f, 1f);
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
				GlStateManager.pushLightingAttributes();
				
				GlStateManager.color4f(1f, .2f, .2f, 1f);
				renderOrbsInternal(whole, pieces, hudXAnchor + wiggleOffset, hudYAnchor);
				
				GlStateManager.popAttributes();
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
			
			Minecraft mc = Minecraft.getInstance();
			mc.getTextureManager().bindTexture(GUI_ICONS);
			
			GlStateManager.pushMatrix();
			GlStateManager.translatef(0, 0, -.5f); // Behind
			GlStateManager.translatef(hudXAnchor + wiggleOffset - 1, hudYAnchor + 3, 0);
			
			GlStateManager.rotatef(rot, 0, 0, 1f);
			GlStateManager.translatef(-1, -10, 0);
			GlStateManager.color4f(1f, 1f, 1f, 1f - ratio);
			//blit(0, 0, GUI_WING_SIZE, GUI_WING_OFFSETY, GUI_WING_SIZE, GUI_WING_SIZE); // -10, 10, 
			RenderFuncs.drawScaledCustomSizeModalRect(0, 0, GUI_WING_SIZE, GUI_WING_OFFSETY,
					-GUI_WING_SIZE, GUI_WING_SIZE, 10, 10, 256f, 256f);
			GlStateManager.popMatrix();
			
			GlStateManager.pushMatrix();
			GlStateManager.translatef(0, 0, -.5f); // Behind
			GlStateManager.translatef(hudXAnchor + wiggleOffset - 76, hudYAnchor + 3, 0);
			
			GlStateManager.rotatef(-rot, 0, 0, 1f);
			GlStateManager.translatef(-10, -10, 0);
			GlStateManager.color4f(1f, 1f, 1f, 1f - ratio);
			//blit(0, 0, 0, offsetX, offsetY, width, height, texWidth, texHeight); // -10, 10, 
			RenderFuncs.drawScaledCustomSizeModalRect(0, 0, 0, GUI_WING_OFFSETY,
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
			GlStateManager.color4f(0f, .8f, 1f, 1f);
			renderOrbsInternal(whole, pieces, hudXAnchor + wiggleOffset, hudYAnchor);
			
			totalMana += playerMana;
			totalMaxMana += playerMaxMana;
		}

		if (ModConfig.config.displayManaText()) {
			Minecraft mc = Minecraft.getInstance();
			int centerx = hudXAnchor - (5 * 8);
			String str = totalMana + "/" + totalMaxMana;
			int width = mc.fontRenderer.getStringWidth(str);
			mc.fontRenderer.drawString(
					str, centerx - width/2, hudYAnchor + 1, 0xFFFFFFFF);
		}
		
	}
	
	private void renderManaBar(ClientPlayerEntity player, MainWindow window, INostrumMagic attr) {
		Minecraft mc = Minecraft.getInstance();
		int hudXAnchor = window.getScaledWidth() - (10 + GUI_BAR_WIDTH);
		int hudYAnchor = 10 + (GUI_BAR_HEIGHT);
		int displayHeight = (int) ((float) GUI_BAR_HEIGHT * Math.max(0f, Math.min(1f, (float) attr.getMana() / (float) attr.getMaxMana())));
		
		GlStateManager.enableBlend();
		mc.getTextureManager().bindTexture(GUI_ICONS);
		this.blit(hudXAnchor, hudYAnchor - displayHeight, GUI_BAR_OFFSETX + GUI_BAR_WIDTH, (GUI_BAR_HEIGHT - displayHeight), GUI_BAR_WIDTH, displayHeight);
		this.blit(hudXAnchor, hudYAnchor - GUI_BAR_HEIGHT, GUI_BAR_OFFSETX, 0, GUI_BAR_WIDTH, GUI_BAR_HEIGHT);
		
		if (ModConfig.config.displayXPBar()) {
			displayHeight = (int) ((float) GUI_BAR_HEIGHT * Math.min(1f, Math.max(0f, attr.getXP() / attr.getMaxXP())));
			this.blit(hudXAnchor, hudYAnchor - displayHeight, GUI_BAR_OFFSETX + GUI_BAR_WIDTH + GUI_BAR_WIDTH, (GUI_BAR_HEIGHT - displayHeight), GUI_BAR_WIDTH, displayHeight);
		}
		GlStateManager.disableBlend();
		
		if (ModConfig.config.displayManaText()) {
			FontRenderer fonter = mc.fontRenderer;
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
	
	private void renderSpellSlide(ClientPlayerEntity player, MainWindow window, INostrumMagic attr) {
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
			
			RenderFuncs.drawRect(textOffset, window.getScaledHeight() - slideHeight, 120, window.getScaledHeight(), 0x50606060);
			
			// Draw icon
			if (current != null) {
				RenderFuncs.drawRect(0, window.getScaledHeight() - slideHeight, textOffset, window.getScaledHeight(), 0xFF202020);
				
				GlStateManager.color4f(1f, 1f, 1f, 1f);
				final int drawY = (window.getScaledHeight() - (slideHeight + iconSize) / 2);
				SpellIcon.get(current.getIconIndex()).render(Minecraft.getInstance(), iconMargin, drawY, iconSize, iconSize);
			}
			
			// Draw name (and maybe xp)
			
			if (xp) {
				// Height is based on this height. Just draw.
				fonter.drawString(text, textOffset + textMargin, window.getScaledHeight() - (fonter.FONT_HEIGHT + iconMargin), 0xFF000000);
				fonter.drawString(String.format("%.02f%%", 100f * attr.getXP() / attr.getMaxXP()),
						textOffset + textMargin, window.getScaledHeight() - (fonter.FONT_HEIGHT * 2 + 6), 0xFF000000);
			} else {
				// Draw in center
				final int drawY = (window.getScaledHeight() - (slideHeight + fonter.FONT_HEIGHT) / 2);
				fonter.drawString(text, textOffset + textMargin, drawY, 0xFF000000);
			}
		}
	}
	
	private void renderArmorOverlay(ClientPlayerEntity player, MainWindow window) {
		// Clone calc of left y offset, since it's not passed through
		int left_height = 39;
		IAttributeInstance attrMaxHealth = player.getAttribute(SharedMonsterAttributes.MAX_HEALTH);
        float healthMax = (float)attrMaxHealth.getValue();
        float absorb = MathHelper.ceil(player.getAbsorptionAmount());
		int healthRows = MathHelper.ceil((healthMax + absorb) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (healthRows - 2), 3);

        left_height += (healthRows * rowHeight);
        if (rowHeight != 10) left_height += 10 - rowHeight;
        int left = window.getScaledWidth() / 2 - 91;
        int top = window.getScaledHeight() - left_height;
		
        GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.color4f(0.1f, .2f, 1f, .8f);
		
		int level = player.getTotalArmorValue();//ForgeHooks.getTotalArmorValue(player);
		level -= 20;
		
		// Stretch the last 5 to 100% out to fill the whole bar
		level = Math.min(20, level * 4);
        for (int i = 0; i < level; i += 2)
        {
            blit(left, top, 34, 9, 9, 3);
            left += 8;
        }
		
        GlStateManager.color4f(1f, 1f, 1f, 1f);
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}
	
	private void renderShieldOverlay(ClientPlayerEntity player, MainWindow window) {
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
	        GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.color4f(1f, 1f, 1f, 1f);
			mc.getTextureManager().bindTexture(GUI_ICONS);
			
	        for (int i = 0; i < whole; i++)
	        {
	            blit(left, top, GUI_SHIELD_PHYS_OFFSETX, GUI_SHIELD_OFFSETY, 9, 9);
	            left += 8;
	        }
	        
	        if (half) {
	        	blit(left, top, GUI_SHIELD_PHYS_OFFSETX + 9, GUI_SHIELD_OFFSETY, 5, 9);
	        }
	        
	        // Repeat for magic
	        whole = (int) Math.ceil(magical) / 2;
	        half = Math.ceil(magical) % 2 == 1;
	        left = window.getScaledWidth() / 2 - 91;
	        
	        for (int i = 0; i < whole; i++)
	        {
	            blit(left, top, GUI_SHIELD_MAG_OFFSETX, GUI_SHIELD_OFFSETY, 9, 9);
	            left += 8;
	        }
	        
	        if (half) {
	        	blit(left, top, GUI_SHIELD_MAG_OFFSETX, GUI_SHIELD_OFFSETY + 9, 5, 9);
	        }
			
	        GlStateManager.color4f(1f, 1f, 1f, 1f);
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
        }
	}
	
	private void renderHookshotCrosshair(ClientPlayerEntity player, MainWindow scaledResolution, boolean entity) {
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		if (entity) {
			GlStateManager.color4f(.8f, .8f, .8f, .8f);
		} else {
			GlStateManager.color4f(.5f, .5f, .5f, .7f);
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
			GlStateManager.pushMatrix();
			GlStateManager.translatef(scaledResolution.getScaledWidth() / 2, scaledResolution.getScaledHeight() / 2, 0);
			
			GlStateManager.rotatef(rot, 0, 0, 1);
			GlStateManager.translated(0, -radius, 0);
			
			if (player.isSneaking()) {
				GlStateManager.rotatef(180f, 0, 0, 1);
				GlStateManager.translatef(-GUI_HOOKSHOT_CROSSHAIR_WIDTH / 2, 0, 0);
			} else {
				GlStateManager.translatef(-GUI_HOOKSHOT_CROSSHAIR_WIDTH / 2, -GUI_HOOKSHOT_CROSSHAIR_WIDTH, 0);
			}
			
			blit(0, 0,
					GUI_HOOKSHOT_CROSSHAIR_OFFSETX, 0, GUI_HOOKSHOT_CROSSHAIR_WIDTH, GUI_HOOKSHOT_CROSSHAIR_WIDTH);
			
			GlStateManager.popMatrix();
		}
		
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}
	
	private void renderCrosshairTargetOverlay(ClientPlayerEntity player, MainWindow scaledResolution) {
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.color4f(.5f, .5f, .5f, .9f);
		Minecraft mc = Minecraft.getInstance();
		mc.getTextureManager().bindTexture(GUI_ICONS);
		
		GlStateManager.translatef(scaledResolution.getScaledWidth() / 2, scaledResolution.getScaledHeight() / 2, 0);
		
		GlStateManager.translatef(-GUI_TARGET_CROSSHAIR_WIDTH / 2, -(GUI_TARGET_CROSSHAIR_WIDTH / 2), 0);
		
		blit(0, 0,
				0, GUI_TARGET_CROSSHAIR_OFFSETY, GUI_TARGET_CROSSHAIR_WIDTH, GUI_TARGET_CROSSHAIR_WIDTH);
		
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}
	
	private void renderPetActionTargetMode(ClientPlayerEntity player, MainWindow scaledResolution, PetTargetMode mode, float prog) {
		Minecraft mc = Minecraft.getInstance();
		final float alpha;
		if (prog < .2f) {
			alpha = prog / .2f;
		} else if (prog >= .8f) {
			alpha = (1f-prog) / .2f;
		} else {
			alpha = 1f;
		}
		
		final int u = GUI_PET_ICON_TARGET_HOFFSET + (mode.ordinal() * GUI_PET_ICON_DIMS);
		final int v = GUI_PET_ICON_TARGET_VOFFSET; // + (mode.ordinal() * GUI_PET_ICON_DIMS);
		
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.color4f(1f, 1f, 1f, alpha * .6f);
		mc.getTextureManager().bindTexture(GUI_PET_ICONS);
		
		GlStateManager.translatef(scaledResolution.getScaledWidth() / 2, scaledResolution.getScaledHeight() / 2, 0);
		GlStateManager.scalef(.5f, .5f, .5f);
		GlStateManager.translatef(1, 1, 0);
		
		blit(0, 0, u, v, GUI_PET_ICON_DIMS, GUI_PET_ICON_DIMS);
		
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}
	
	private void renderPetActionPlacementMode(ClientPlayerEntity player, MainWindow scaledResolution, PetPlacementMode mode, float prog) {
		Minecraft mc = Minecraft.getInstance();
		final float alpha;
		if (prog < .2f) {
			alpha = prog / .2f;
		} else if (prog >= .8f) {
			alpha = (1f-prog) / .2f;
		} else {
			alpha = 1f;
		}
		final int u = GUI_PET_ICON_PLACEMENT_HOFFSET + (mode.ordinal() * GUI_PET_ICON_DIMS);
		final int v = GUI_PET_ICON_PLACEMENT_VOFFSET; // + (mode.ordinal() * GUI_PET_ICON_DIMS);
		
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.color4f(1f, 1f, 1f, alpha * .6f);
		mc.getTextureManager().bindTexture(GUI_PET_ICONS);
		
		GlStateManager.translatef(scaledResolution.getScaledWidth() / 2, scaledResolution.getScaledHeight() / 2, 0);
		GlStateManager.scalef(.5f, .5f, .5f);
		GlStateManager.translatef(-(GUI_PET_ICON_DIMS + 1), 1, 0);
		
		blit(0, 0, u, v, GUI_PET_ICON_DIMS, GUI_PET_ICON_DIMS);
		
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}
	
	private void renderContingencyShield(ClientPlayerEntity player, MainWindow scaledResolution, int typeOffset, int xoffset, float timer) {
		Minecraft mc = Minecraft.getInstance();
		final int left = (scaledResolution.getScaledWidth() / 2 + 91) + 10 + (xoffset * GUI_CONTINGENCY_ICON_LENGTH);
		final int top = scaledResolution.getScaledHeight() - (2 + GUI_CONTINGENCY_ICON_LENGTH);
		final double borderScale = 1.07;
		final BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		final int width = GUI_CONTINGENCY_ICON_LENGTH; // for readability
		final int height = GUI_CONTINGENCY_ICON_LENGTH; // for readability
		
		if (timer <= 0) {
			return;
		}
		
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		mc.getTextureManager().bindTexture(GUI_ICONS);
		
		GlStateManager.translatef(left, top, 0);
		
		GlStateManager.pushMatrix();
		GlStateManager.color4f(1f, .25f, .3f, 1f);
		GlStateManager.translatef(-.5f, -.5f, 0);
		GlStateManager.scaled(borderScale, borderScale, borderScale);
		buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_TEX);
		
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
		
//		blit(0, 0,
//				GUI_CONTINGENCY_ICON_OFFSETX + (3 * GUI_CONTINGENCY_ICON_LENGTH),
//				GUI_CONTINGENCY_ICON_OFFSETY - GUI_CONTINGENCY_ICON_LENGTH, GUI_CONTINGENCY_ICON_LENGTH, GUI_CONTINGENCY_ICON_LENGTH);
		GlStateManager.popMatrix();

		GlStateManager.color4f(.5f, .5f, .5f, 1f);
		blit(0, 0,
				GUI_CONTINGENCY_ICON_OFFSETX + (typeOffset * GUI_CONTINGENCY_ICON_LENGTH),
				GUI_CONTINGENCY_ICON_OFFSETY, GUI_CONTINGENCY_ICON_LENGTH, GUI_CONTINGENCY_ICON_LENGTH);
		
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}
	
	private void renderHealthbarOrb(ClientPlayerEntity player, MainWindow window, LivingEntity pet, int xoffset, int yoffset, float scale) {
		Minecraft mc = Minecraft.getInstance();
		
		// Render back, scaled bar + middle 'goods', and then foreground. Easy.
		// For center, render:
		// 1) healthbar
		// 2) pet head/icon
		// 3) pet status icon
		FontRenderer fonter = mc.fontRenderer;
//		final boolean sitting = (pet instanceof EntityTameable ? ((EntityTameable) pet).isSitting()
//				: pet instanceof IEntityTameable ? ((IEntityTameable) pet).isSitting()
//				: false);
//		final boolean attacking = (pet instanceof MobEntity ? ((MobEntity) pet).getAttackTarget() != null : false);
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
		
		mc.getTextureManager().bindTexture(GUI_HEALTHBARS);
		
		GlStateManager.pushMatrix();
		
		GlStateManager.translatef(xoffset, yoffset, 0);
		GlStateManager.scalef(scale, scale, 1);
		
		GlStateManager.enableBlend();
		GlStateManager.color4f(1f, 1f, 1f, 1f);
		
		// Draw background
		GlStateManager.pushMatrix();
		GlStateManager.translatef(0, 0, -100);
		this.fillGradient(GUI_HEALTHBAR_ORB_NAME_HOFFSET, GUI_HEALTHBAR_ORB_NAME_VOFFSET,
				GUI_HEALTHBAR_ORB_NAME_WIDTH, GUI_HEALTHBAR_ORB_NAME_HEIGHT,
				0x50000000, 0xA0000000); //nameplate background
		blit(0, 0,
				0, GUI_HEALTHBAR_ORB_BACK_HEIGHT, GUI_HEALTHBAR_ORB_BACK_WIDTH, GUI_HEALTHBAR_ORB_BACK_HEIGHT);
		GlStateManager.popMatrix();
		
		// Draw middle
		GlStateManager.pushMatrix();
		// 	-> Health bar
		blit(
				GUI_HEALTHBAR_ORB_HEALTH_BAR_INNER_HOFFSET + Math.round(GUI_HEALTHBAR_ORB_HEALTH_WIDTH * (1f-health)),
				GUI_HEALTHBAR_ORB_HEALTH_BAR_INNER_VOFFSET,
				GUI_HEALTHBAR_ORB_HEALTH_BAR_HOFFSET + Math.round(GUI_HEALTHBAR_ORB_HEALTH_WIDTH * (1f-health)),
				GUI_HEALTHBAR_ORB_HEALTH_BAR_VOFFSET,
				GUI_HEALTHBAR_ORB_HEALTH_WIDTH - Math.round(GUI_HEALTHBAR_ORB_HEALTH_WIDTH * (1f-health)),
				GUI_HEALTHBAR_ORB_HEALTH_HEIGHT);
		//	-> Secondary bar
		if (!hasSecondaryBar) {
			GlStateManager.color4f(.7f, .9f, .7f, 1f);
			secondaryMeter = 1f;
		} else {
			GlStateManager.color4f(flavor.colorR(secondaryMeter),
					flavor.colorG(secondaryMeter),
					flavor.colorB(secondaryMeter),
					flavor.colorA(secondaryMeter));
		}
		blit(
				GUI_HEALTHBAR_ORB_SECONDARY_BAR_INNER_HOFFSET + Math.round(GUI_HEALTHBAR_ORB_SECONDARY_WIDTH * (1f-secondaryMeter)),
				GUI_HEALTHBAR_ORB_SECONDARY_BAR_INNER_VOFFSET,
				GUI_HEALTHBAR_ORB_SECONDARY_BAR_HOFFSET + Math.round(GUI_HEALTHBAR_ORB_SECONDARY_WIDTH * (1f-secondaryMeter)),
				GUI_HEALTHBAR_ORB_SECONDARY_BAR_VOFFSET,
				GUI_HEALTHBAR_ORB_SECONDARY_WIDTH - Math.round(GUI_HEALTHBAR_ORB_SECONDARY_WIDTH * (1f-secondaryMeter)),
				GUI_HEALTHBAR_ORB_SECONDARY_HEIGHT);
	
		GlStateManager.color4f(1f, 1f, 1f, 1f);

		//	-> Icon
		InventoryScreen.drawEntityOnScreen(GUI_HEALTHBAR_ORB_ENTITY_HOFFSET, GUI_HEALTHBAR_ORB_ENTITY_VOFFSET, GUI_HEALTHBAR_ORB_ENTITY_WIDTH, 0, 0, pet);
		mc.getTextureManager().bindTexture(GUI_HEALTHBARS);
		GlStateManager.color4f(1f, 1f, 1f, 1f);
		
		//	-> Status
		GlStateManager.translatef(0, 0, 100);
		GlStateManager.pushMatrix();
		GlStateManager.scalef(.6f, .6f, .6f);
		GlStateManager.translatef(0, 0, 0);
		if (action == PetAction.ATTACKING) {
			blit(GUI_HEALTHBAR_ICON_INTERNAL_HOFFSET, GUI_HEALTHBAR_ICON_INTERNAL_VOFFSET,
					GUI_HEALTHBAR_ICON_HOFFSET, GUI_HEALTHBAR_ICON_ATTACK_VOFFSET, GUI_HEALTHBAR_ICON_LENGTH, GUI_HEALTHBAR_ICON_LENGTH);
		} else if (action == PetAction.SITTING) {
			blit(GUI_HEALTHBAR_ICON_INTERNAL_HOFFSET, GUI_HEALTHBAR_ICON_INTERNAL_VOFFSET,
					GUI_HEALTHBAR_ICON_HOFFSET, GUI_HEALTHBAR_ICON_STAY_VOFFSET, GUI_HEALTHBAR_ICON_LENGTH, GUI_HEALTHBAR_ICON_LENGTH);
		} else if (action == PetAction.WORKING) {
			blit(GUI_HEALTHBAR_ICON_INTERNAL_HOFFSET, GUI_HEALTHBAR_ICON_INTERNAL_VOFFSET,
					GUI_HEALTHBAR_ICON_HOFFSET, GUI_HEALTHBAR_ICON_WORK_VOFFSET, GUI_HEALTHBAR_ICON_LENGTH, GUI_HEALTHBAR_ICON_LENGTH);
		}
		GlStateManager.popMatrix();
		
		//	-> Name
		final String name = pet.hasCustomName() ? pet.getCustomName().getFormattedText() : pet.getName().getFormattedText();
		final int nameLen = fonter.getStringWidth(name);
		//final float fontScale = (1f/scale) * .6f;
		final float fontScale = scale * 2.4f;
		GlStateManager.pushMatrix();
		GlStateManager.scalef(fontScale, fontScale, fontScale);
		fonter.drawString(name, 123 - (nameLen), 25 - (fonter.FONT_HEIGHT + 2), 0xFFFFFFFF);
		mc.getTextureManager().bindTexture(GUI_HEALTHBARS);
		GlStateManager.popMatrix();
		
		GlStateManager.popMatrix();
		
		// Draw foreground
		GlStateManager.enableBlend();
		GlStateManager.pushMatrix();
		GlStateManager.translatef(0, 0, 100);
		blit(0, 0,
				0, 0, GUI_HEALTHBAR_ORB_BACK_WIDTH, GUI_HEALTHBAR_ORB_BACK_HEIGHT);
		GlStateManager.popMatrix();
		
		GlStateManager.popMatrix();
	}
	
	private void renderHealthbarBox(ClientPlayerEntity player, MainWindow window, LivingEntity pet, int xoffset, int yoffset, float scale) {
		Minecraft mc = Minecraft.getInstance();
		
		// Render back, scaled bar + middle 'goods', and then foreground. Easy.
		// For center, render:
		// 1) healthbar
		// 2) pet head/icon
		// 3) pet status icon
		FontRenderer fonter = mc.fontRenderer;
		
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
//		final boolean attacking = (pet instanceof MobEntity ? ((MobEntity) pet).getAttackTarget() != null : false);
		final PetAction action = info.getPetAction();
		
		info.release();
		info = null;
		
		mc.getTextureManager().bindTexture(GUI_HEALTHBARS);
		
		GlStateManager.pushMatrix();
		
		GlStateManager.translatef(xoffset, yoffset, 0);
		GlStateManager.scalef(scale, scale, 1);
		
		GlStateManager.enableBlend();
		GlStateManager.color4f(1f, 1f, 1f, 1f);
		
		// Draw background
		GlStateManager.pushMatrix();
		GlStateManager.translatef(0, 0, -100);
//		this.drawGradientRect(GUI_HEALTHBAR_ORB_NAME_HOFFSET, GUI_HEALTHBAR_ORB_NAME_VOFFSET,
//				GUI_HEALTHBAR_ORB_NAME_WIDTH, GUI_HEALTHBAR_ORB_NAME_HEIGHT,
//				0x50000000, 0xA0000000); //nameplate background
		blit(0, 0,
				0, GUI_HEALTHBAR_BOX_BACK_VOFFSET + GUI_HEALTHBAR_BOX_BACK_HEIGHT, GUI_HEALTHBAR_BOX_BACK_WIDTH, GUI_HEALTHBAR_BOX_BACK_HEIGHT);
		GlStateManager.popMatrix();
		
		// Draw middle
		GlStateManager.pushMatrix();
		// 	-> Health bar
		blit(
				GUI_HEALTHBAR_BOX_HEALTH_BAR_INNER_HOFFSET + Math.round(GUI_HEALTHBAR_BOX_HEALTH_WIDTH * (1f-health)),
				GUI_HEALTHBAR_BOX_HEALTH_BAR_INNER_VOFFSET,
				GUI_HEALTHBAR_BOX_HEALTH_BAR_HOFFSET + Math.round(GUI_HEALTHBAR_BOX_HEALTH_WIDTH * (1f-health)),
				GUI_HEALTHBAR_BOX_HEALTH_BAR_VOFFSET,
				GUI_HEALTHBAR_BOX_HEALTH_WIDTH - Math.round(GUI_HEALTHBAR_BOX_HEALTH_WIDTH * (1f-health)),
				GUI_HEALTHBAR_BOX_HEALTH_HEIGHT);
		//	-> Secondary bar
		if (!hasSecondaryBar) {
			GlStateManager.color4f(.7f, .9f, .7f, 1f);
			secondaryMeter = 1f;
		} else {
			GlStateManager.color4f(flavor.colorR(secondaryMeter),
					flavor.colorG(secondaryMeter),
					flavor.colorB(secondaryMeter),
					flavor.colorA(secondaryMeter));
		}
		blit(
				GUI_HEALTHBAR_BOX_SECONDARY_BAR_INNER_HOFFSET + Math.round(GUI_HEALTHBAR_BOX_SECONDARY_WIDTH * (1f-secondaryMeter)),
				GUI_HEALTHBAR_BOX_SECONDARY_BAR_INNER_VOFFSET,
				GUI_HEALTHBAR_BOX_SECONDARY_BAR_HOFFSET + Math.round(GUI_HEALTHBAR_BOX_SECONDARY_WIDTH * (1f-secondaryMeter)),
				GUI_HEALTHBAR_BOX_SECONDARY_BAR_VOFFSET,
				GUI_HEALTHBAR_BOX_SECONDARY_WIDTH - Math.round(GUI_HEALTHBAR_BOX_SECONDARY_WIDTH * (1f-secondaryMeter)),
				GUI_HEALTHBAR_BOX_SECONDARY_HEIGHT);
	
		GlStateManager.color4f(1f, 1f, 1f, 1f);
		
		//		-> Status
		GlStateManager.translatef(0, 0, 100);
		GlStateManager.pushMatrix();
		GlStateManager.scalef(.6f, .6f, .6f);
		GlStateManager.translatef(0, 0, 0);
		if (action == PetAction.ATTACKING) {
			blit(282, 6,
					GUI_HEALTHBAR_ICON_HOFFSET, GUI_HEALTHBAR_ICON_ATTACK_VOFFSET, GUI_HEALTHBAR_ICON_LENGTH, GUI_HEALTHBAR_ICON_LENGTH);
		} else if (action == PetAction.SITTING) {
			blit(282, 6,
					GUI_HEALTHBAR_ICON_HOFFSET, GUI_HEALTHBAR_ICON_STAY_VOFFSET, GUI_HEALTHBAR_ICON_LENGTH, GUI_HEALTHBAR_ICON_LENGTH);
		} else if (action == PetAction.WORKING) {
			blit(282, 6,
					GUI_HEALTHBAR_ICON_HOFFSET, GUI_HEALTHBAR_ICON_WORK_VOFFSET, GUI_HEALTHBAR_ICON_LENGTH, GUI_HEALTHBAR_ICON_LENGTH);
		}
		GlStateManager.popMatrix();

		//	-> Name
		final String name = pet.hasCustomName() ? pet.getCustomName().getFormattedText() : pet.getName().getFormattedText();
		final int nameLen = fonter.getStringWidth(name);
		//final float fontScale = (1f/scale) * .6f;
		final float fontScale = scale * 2.4f;
		GlStateManager.pushMatrix();
		GlStateManager.scalef(fontScale, fontScale, fontScale);
		fonter.drawStringWithShadow(name, 135 - (nameLen), 14 - (fonter.FONT_HEIGHT + 2), 0xFFFFFFFF);
		mc.getTextureManager().bindTexture(GUI_HEALTHBARS);
		GlStateManager.popMatrix();
		
		GlStateManager.popMatrix();
		
		// Draw foreground
		GlStateManager.enableBlend();
		GlStateManager.pushMatrix();
		GlStateManager.translatef(0, 0, 100);
		blit(0, 0,
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
	
	public void changePetTargetIcon() {
		Minecraft mc = Minecraft.getInstance();
		final ClientPlayerEntity player = mc.player;
		if (petTargetIndex < 0) {
			// Brand new animation
			petTargetIndex = player.ticksExisted;
		} else if (player.ticksExisted - petTargetIndex > petTargetAnimDur/2) {
			// Reset to halfway point
			petTargetIndex = player.ticksExisted - petTargetAnimDur/2;
		} else {
			; // Fading in, leave alone and just swap out the icon
		}
	}
	
	public void changePetPlacementIcon() {
		Minecraft mc = Minecraft.getInstance();
		final ClientPlayerEntity player = mc.player;
		if (petPlacementIndex < 0) {
			// Brand new animation
			petPlacementIndex = player.ticksExisted;
		} else if (player.ticksExisted - petPlacementIndex > petPlacementAnimDur/2) {
			// Reset to halfway point
			petPlacementIndex = player.ticksExisted - petPlacementAnimDur/2;
		} else {
			; // Fading in, leave alone and just swap out the icon
		}
	}
	
	private void renderLoreIcon(Boolean loreIsDeep) {
		Minecraft mc = Minecraft.getInstance();
		GlStateManager.enableBlend();
		GlStateManager.color4f(.6f, .6f, .6f, .6f);
		mc.getItemRenderer().renderItemIntoGUI(new ItemStack(NostrumItems.spellScroll), 0, 0);
		GlStateManager.color4f(1f, 1f, 1f, 1f);
		
		if (loreIsDeep != null) {
			final int u = (160 + (loreIsDeep ? 0 : 32));
			mc.getTextureManager().bindTexture(GUI_ICONS);
			RenderFuncs.drawScaledCustomSizeModalRect(8, 8, u, 0, 32, 32, 8, 8, 256, 256);
		}
	}
	
	private void renderEnchantableIcon() {
		Minecraft mc = Minecraft.getInstance();
		GlStateManager.enableBlend();
		GlStateManager.color4f(1f, 1f, 1f, 1f);
		mc.getTextureManager().bindTexture(GUI_ICONS);
		RenderFuncs.drawScaledCustomSizeModalRect(6, 6, 192, 32, 32, 32, 12, 12, 256, 256);
		GlStateManager.color4f(1f, 1f, 1f, 1f);
	}
	
	private void renderConfigurableIcon() {
		Minecraft mc = Minecraft.getInstance();
		GlStateManager.enableBlend();
		GlStateManager.color4f(1f, 1f, 1f, 1f);
		mc.getTextureManager().bindTexture(GUI_ICONS);
		RenderFuncs.drawScaledCustomSizeModalRect(8, 8, 160, 32, 32, 32, 8, 8, 256, 256);
		GlStateManager.color4f(1f, 1f, 1f, 1f);
	}
	
	private void renderTransmutableIcon() {
		Minecraft mc = Minecraft.getInstance();
		GlStateManager.enableBlend();
		GlStateManager.color4f(1f, 1f, 1f, 1f);
		mc.getTextureManager().bindTexture(GUI_ICONS);
		RenderFuncs.drawScaledCustomSizeModalRect(8, 8, 224, 32, 32, 32, 8, 8, 256, 256);
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
			
			GlStateManager.pushMatrix();
			GlStateManager.translatef(event.getX() + event.getWidth() - 4, event.getY() + event.getHeight() - 6, 50);
			renderLoreIcon(hasFullLore);
			GlStateManager.popMatrix();
		}
		
		// Enchantable?
		if (SpellAction.isEnchantable(stack)) {
			GlStateManager.pushMatrix();
			GlStateManager.translatef(event.getX() + event.getWidth() - 8, event.getY() - 16, 50);
			renderEnchantableIcon();
			GlStateManager.popMatrix();
		}
		
		// Configurable?
		if (ModificationTable.IsModifiable(stack)) {
			GlStateManager.pushMatrix();
			GlStateManager.translatef(event.getX() - 15, event.getY() + event.getHeight() - 8, 50);
			renderConfigurableIcon();
			GlStateManager.popMatrix();
		}
		
		// Transmutable?
		if (Transmutation.IsTransmutable(stack.getItem())) {
			GlStateManager.pushMatrix();
			GlStateManager.translatef(event.getX() - 15, event.getY() - 16, 50);
			renderTransmutableIcon();
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
//		INostrumMagic attr = NostrumMagica.getMagicWrapper(mc.player);
//		if (attr == null || !attr.isUnlocked()) {
//			return;
//		}
//		
//		ILoreTagged tag = (ILoreTagged) stack.getItem();
//		renderLoreIcon(attr.hasFullLore(tag));
	}
	
	protected void renderRoots(LivingEntity entity) {
		if (entity.ticksExisted % 4 == 0) {
			EffectData data = NostrumMagica.magicEffectProxy.getData(entity, SpecialEffect.ROOTED);
			if (data != null && data.getCount() != 0) {
				final ClientEffect effect = new ClientEffectAnimated(entity.getPositionVector(), 1000L,
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
	
	private boolean renderRecurseMarker = false;
	
	@SubscribeEvent
	public void onEntityRender(RenderLivingEvent.Post<LivingEntity, EntityModel<LivingEntity>> event) {
		if (!renderRecurseMarker) {
			final LivingEntity entity = event.getEntity();
			//final float partialTicks = event.getPartialRenderTick();
			renderRecurseMarker = true;
			{
				renderRoots(entity);
				
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
				mc.gameSettings.thirdPersonView != 0
				|| (mc.getRenderViewEntity() instanceof LivingEntity && ((LivingEntity)mc.getRenderViewEntity()).isSleeping())
				);
		
		if (!shouldRenderMe) {
			// Normal render didn't happen. Do rooted render manually instead.
			renderRoots(NostrumMagica.instance.proxy.getPlayer());
		}
	}
	
	private static final Map<PlayerRenderer, Boolean> InjectedSet = new WeakHashMap<>();
	
	@SubscribeEvent
	public void onPlayerRender(RenderPlayerEvent.Post event) {
		if (!InjectedSet.containsKey(event.getRenderer())) {
			InjectedSet.put(event.getRenderer(), true);
			
			// EnderIO injects custom cape layer so that capes don't render if an elytra-like item is present. We won't bother.
			// Instead, we just inject a layer for our custom elytras, and another for dragon-flight wings
			event.getRenderer().addLayer(new LayerCustomElytra<>(event.getRenderer()));
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
	public void onBlockHighlight(DrawBlockHighlightEvent event) {
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
//			final BlockPos blockpos = new BlockPos(player.posX, player.posY + player.getEyeHeight(), player.posZ);
//			final BlockState state = player.world.getBlockState(blockpos);
//			final Block block = state.getBlock();
//			final IFluidState fluidState = state.getFluidState();
//
//			if (block instanceof PoisonWaterBlock || (fluidState != null && fluidState.getFluid() instanceof FluidPoisonWater)) {
//				
//				
//				
//				BlockFluidBase fblock = (BlockFluidBase) block;
//				Vec3d fogColor = fblock.getFogColor(player.world, blockpos, state, player,
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
//					GlStateManager.pushMatrix();
//					float f7 = -player.rotationYaw / 64.0F;
//					float f8 = player.rotationPitch / 64.0F;
//					vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
//					vertexbuffer.pos(-1.0D, -1.0D, -0.5D).tex(4.0F + f7, 4.0F + f8).endVertex();
//					vertexbuffer.pos(1.0D, -1.0D, -0.5D).tex(0.0F + f7, 4.0F + f8).endVertex();
//					vertexbuffer.pos(1.0D, 1.0D, -0.5D).tex(0.0F + f7, 0.0F + f8).endVertex();
//					vertexbuffer.pos(-1.0D, 1.0D, -0.5D).tex(4.0F + f7, 0.0F + f8).endVertex();
//					tessellator.draw();
//					GlStateManager.popMatrix();
//					GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
//					GlStateManager.disableBlend();
//					
//					event.setCanceled(true);
//				}
//			}
//		}
	}
	
}
