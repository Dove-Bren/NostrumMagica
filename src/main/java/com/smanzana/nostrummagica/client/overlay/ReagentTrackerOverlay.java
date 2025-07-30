package com.smanzana.nostrummagica.client.overlay;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.listener.ClientPlayerListener;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;

public class ReagentTrackerOverlay implements IIngameOverlay {
	
	private static final class ReagentCount {
		public final ReagentType type;
		public int lastCount;
		public int changeThisInterval;
		
		protected ItemStack cacheStack = ItemStack.EMPTY;
		
		public ReagentCount(ReagentType type) {
			this.type = type;
		}
		
		public ItemStack getIcon() {
			if (cacheStack.isEmpty()) {
				cacheStack = ReagentItem.CreateStack(type, 1);
			}
			return cacheStack;
		}
	}
	
	private final Map<ReagentType, ReagentCount> counts = new EnumMap<>(Maps.toMap(List.of(ReagentType.values()), ReagentCount::new));
	
	private int reagentIndex; // Contols reagent HUD element fade in and out
	private static final int reagentFadeDur = 60;
	private static final int reagentFadeDelay = 3 * 60;
	
	private boolean HUDToggle;
	
	public ReagentTrackerOverlay() {
		HUDToggle = false;
	}
	
	public void toggleTracker() {
		this.HUDToggle = !this.HUDToggle;
		this.fadeInReagents();
	}
	
	protected void fadeInReagents() {
		// Either start a fade in, don't touch one that's happening, or reset fade out duration
		if (reagentIndex >= reagentFadeDelay + reagentFadeDur) {
			// Fully faded out. Start new
			reagentIndex = -reagentFadeDur;
		} else if (reagentIndex < 0) {
			; // Fading in already
		} else {
			// Reset to fully opaque and in delay time
			reagentIndex = 0;
		}
	}
	
	protected boolean reagentsFadeIsVisible() {
		// Are we visible at all?
		return reagentIndex < reagentFadeDelay + reagentFadeDur;
	}
	
	protected float reagentsFadeCurrentAlpha() {
		// if -, fading in
		// if 0-reagentFadeDelay, opaque
		// else fading out
		if (reagentIndex < 0) {
			return (float) (reagentFadeDur + reagentIndex) / (float) reagentFadeDur;
		} else if (reagentIndex > reagentFadeDelay) {
			return 1f - ((float) (reagentIndex - reagentFadeDelay) / (float) reagentFadeDur);
		} else {
			return 1f;
		}
	}
	
	protected void tickReagentCounts(Player player) {
		boolean changedThisTick = false;
		EnumMap<ReagentType, Integer> tickCounts = new EnumMap<>(ReagentType.class);
		final ReagentType[] values = ReagentType.values();
		for (ReagentType type : values) {
			tickCounts.put(type, NostrumMagica.getReagentCount(player, type));
		}
		
		// Detect changes
		for (ReagentType type : values) {
			if (counts.get(type).lastCount != tickCounts.get(type)) {
				changedThisTick = true;
				break;
			}
		}
		
		if (changedThisTick) {
			for (ReagentType type : values) {
				final ReagentCount count = counts.get(type);
				final int countThisTick = tickCounts.get(type);
				count.changeThisInterval = countThisTick - count.lastCount;
				count.lastCount = countThisTick;
			}
			this.fadeInReagents();
		}
		
		// Also tick fade anim
		if (this.reagentIndex < reagentFadeDelay + reagentFadeDur) {
			this.reagentIndex++;
		}
	}
	
	@Override
	public void render(ForgeIngameGui gui, PoseStack matrixStackIn, float partialTicks, int guiWidth, int guiHeight) {
		final Minecraft mc = Minecraft.getInstance();
		final LocalPlayer player = mc.player;
		
		tickReagentCounts(player);
		
		final ReagentHUDMode mode = ModConfig.config.displayReagentMode();
		switch (mode) {
		case ALWAYS:
			; // fall through
			break;
		case HOLD: {
				ClientPlayerListener listener = (ClientPlayerListener) NostrumMagica.playerListener;
				if (!listener.getHUDKey().isDown()) {
					return;
				}
			}
			break;
		case TOGGLE:
			if (this.HUDToggle) {
				return;
			}
			break;
		case ONCHANGE:
			if (!reagentsFadeIsVisible()) {
				return;
			}
			break;
		}
		
		// We are visible and should render
		
		final int configX = ModConfig.config.displayReagentXPos();
		final int configY = ModConfig.config.displayReagentYPos();
		final boolean tall = ModConfig.config.displayReagentTall();
		
		final int rows = (ReagentType.values().length+1) / (tall ? 1 : 2);
		final int margin = 3;
		
		final int width = (tall ? 24 : 48) + 2*margin;
		final int height = (rows * 8) + 2*margin;
		
		final int xOffset;
		final int yOffset;
		final Direction hang; // N being up
		final boolean offsetAuto = (configX == -1 && configY == -1);
		if (offsetAuto) {
			final int estimatedSpellSlideWidth = 16 * 5;
			final int estimatedLeftTaken = estimatedSpellSlideWidth + 120 + width;
			if (guiWidth/2 < estimatedLeftTaken) {
				xOffset = 0;
				yOffset = guiHeight - (height + 28);
				hang = Direction.WEST;
			} else {
				xOffset = estimatedSpellSlideWidth + 10;
				yOffset = guiHeight - height;
				hang = Direction.SOUTH;
			}
		} else {
			if (configX == -1) {
				xOffset = guiWidth - width;
			} else {
				xOffset = configX > 0 ? configX : (guiWidth - configX);
			}
			if (configY == -1) {
				yOffset = guiHeight - height;
			} else {
				yOffset = configY > 0 ? configY : (guiHeight - configY);
			}
			
			hang = configX == -1 ? Direction.EAST
					: configX == 0 ? Direction.WEST
					: configY == -1 ? Direction.SOUTH
					: Direction.NORTH;
		}
		
		final float fadeProg;
		if (mode == ReagentHUDMode.ONCHANGE) {
			fadeProg = reagentsFadeCurrentAlpha();
		} else {
			fadeProg = 1f;
		}
		
//		Function<Integer, Integer> makeColor = (color) -> {
//			float existingAlpha = (float) (color >> 24) / (255f);
//			return (int) (existingAlpha * alpha * 255) << 24;
//		};
		
		matrixStackIn.pushPose();
		if (fadeProg != 1f) {
			switch (hang) {
			case UP:
			case DOWN:
			case SOUTH:
			default:
				matrixStackIn.translate(0, height * (1f - fadeProg), 0);
				break;
			case EAST:
				matrixStackIn.translate(width * (1f - fadeProg), 0, 0);
				break;
			case NORTH:
				matrixStackIn.translate(0, -height * (1f - fadeProg), 0);
				break;
			case WEST:
				matrixStackIn.translate(-width * (1f - fadeProg), 0, 0);
				break;
			}
		}
		
		final int colorTL, colorTR, colorBL, colorBR;
		final int dark = 0xAA000000;
		final int light = 0x10000000;
		switch (hang) {
		case UP:
		case DOWN:
		case SOUTH:
		default:
			colorTL = colorTR = light;
			colorBL = colorBR = dark;
			break;
		case EAST:
			colorTL = colorBL = light;
			colorTR = colorBR = dark;
			break;
		case NORTH:
			colorTL = colorTR = dark;
			colorBL = colorBR = light;
			break;
		case WEST:
			colorTL = colorBL = dark;
			colorTR = colorBR = light;
			break;
		}
		
		RenderFuncs.drawGradientRect(matrixStackIn, xOffset, yOffset, xOffset + width, yOffset + height,
				colorTL, colorTR,
				colorBL, colorBR);
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(xOffset + margin, yOffset + margin, 0);
		matrixStackIn.scale(.5f, .5f, 1f);
		
		final ReagentType[] types = ReagentType.values();
		for (int i = 0; i < types.length; i++) {
			final ReagentType type = types[i];
			final ReagentCount count = counts.get(type);
			final int x = (i / rows) * 48;
			final int y = (i % rows) * 16;
			final int color = count.changeThisInterval == 0 ? 0xFFFFFFFF
					: (count.changeThisInterval > 0 ? 0xFF40AA40 : 0xFFFFFF40);
			
			RenderFuncs.RenderGUIItem(count.getIcon(), matrixStackIn, x, y, -30);
			RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			mc.font.draw(matrixStackIn, count.lastCount + "", x + 18, y + 5, color);
		}
		matrixStackIn.popPose();
		matrixStackIn.popPose();
	}
}
