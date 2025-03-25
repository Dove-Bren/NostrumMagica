package com.smanzana.nostrummagica.client.gui;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.autodungeons.util.DimensionUtils;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.tile.ObeliskTileEntity;
import com.smanzana.nostrummagica.tile.ObeliskTileEntity.NostrumObeliskTarget;
import com.smanzana.nostrummagica.util.Location;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import com.mojang.math.Matrix4f;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ObeliskScreen extends Screen {
	
	protected static final ResourceLocation background = new ResourceLocation(NostrumMagica.MODID + ":textures/gui/container/obelisk.png");
	
	protected static final int TEXT_BACK_WIDTH = 192;
	protected static final int TEXT_BACK_PAN = 64;
	protected static final int TEXT_BACK_HEIGHT = 64;
	protected static final int TEXT_WHOLE_WIDTH = 256;
	protected static final int TEXT_WHOLE_HEIGHT = 256;
	protected static final int TEXT_ICON_LENGTH = 14;
	protected static final int TEXT_REMOVE_BUTT_HOFFSET = 70;
	protected static final int TEXT_REMOVE_BUTT_VOFFSET = 64;
	protected static final int TEXT_REMOVE_BUTT_LENT = 14;
	
	private static final String NO_DEST_KEY = "info.obelisk.none";

	private static int zoomScaleFactor = 8;
	
	private ObeliskTileEntity tileEntity;
	private int xOffset;
	private int yOffset;
	private float scale;
	private DestinationButton centralButton;
	private List<DestinationButton> floatingButtons;
	private List<DestinationButton> listButtons;
	private List<RemoveButton> listRemoveButtons;
	private boolean drawList;
	
	private double mouseClickX;
	private double mouseClickY;
	private double mouseClickXOffset; //xoffset at time of click
	private double mouseClickYOffset; //yoffset at time of click
	private String errorString;
	private final Minecraft mc;
	
	public ObeliskScreen(ObeliskTileEntity tileEntity) {
		super(new TextComponent("Obelisk Screen"));
		this.tileEntity = tileEntity;
		drawList = ModConfig.config.getObeliskList();
		
		errorString = I18n.get(NO_DEST_KEY, new Object[0]);
		this.floatingButtons = new ArrayList<>();
		this.listButtons = new ArrayList<>();
		this.listRemoveButtons = new ArrayList<>();
		this.scale = 1.0f;
		mc = Minecraft.getInstance();
	}
	
	private int getScaled(int worldXZ) {
		return (int) ((float) worldXZ / (scale * (float) zoomScaleFactor));
	}
	
	private int unscale(int screenXZ) {
		return (int) ((float) screenXZ * (scale * (float) zoomScaleFactor));
	}
	
	private void translateButton(DestinationButton button) {
		// raw x is getScaled(button.pos.x)
		// Render x is button.pos.x - te.x SCALED
		button.x = getScaled(button.loc.getPos().getX()) - xOffset;
		button.y = getScaled(button.loc.getPos().getZ()) - yOffset;
	}
	
	@Override
	public void init() {
		if (tileEntity.getTargets().isEmpty())
			return;
		
		// Set initial scale
		{
			// How much space do we have to use? If we're drawing the list, we only get 2/3 of space.
			// Divide answer by 2 because we want the max extent to fix on half of the screen around origin
			float minScreenSide = Math.min(this.height, this.width * (this.drawList ? (2f/3f) : 1f)) / 2f;
			
			// we want furthest vert/horizontal distance to fit on the screen by default
			float furthestDim = 1; // in blocks
			for (NostrumObeliskTarget target: tileEntity.getTargets()) {
				final BlockPos targPos = target.getLocation().getPos();
				final int xDiff = Math.abs(targPos.getX() - tileEntity.getBlockPos().getX());
				final int yDiff = Math.abs(targPos.getZ() - tileEntity.getBlockPos().getZ());
				if (furthestDim < xDiff) {
					furthestDim = xDiff;
				}
				if (furthestDim < yDiff) {
					furthestDim = yDiff;
				}
			}
			
			// Ideally furthest fits in 80% of the actual length. 1/.8 = 1.25.
			// So what scale would make (furthestDim) fit on 80% of screen side length?
			// xDistOnScreen = xDist / (scale * zoomScaleFactor)
			// (.8 * minScreenSide) = (furthestDim) / ({ideal scale} * zoomScaleFactor)
			// .8 * minScreenSide * {ideal scale} * zoomScaleFactor = (furthestDim)
			// {ideal scale} = (furthestDim) / (.8 * minScreenSide * zoomScaleFactor)
			final float idealScale = furthestDim / (.8f * minScreenSide * zoomScaleFactor);
			
			scale = Math.max(0.01f, Math.min(2.0f, idealScale));
		}
		
		float xDiv = this.drawList ? (2f/3f) : .5f;
		this.xOffset = getScaled(tileEntity.getBlockPos().getX()) - (int) (this.width * (xDiv));
		this.yOffset = getScaled(tileEntity.getBlockPos().getZ()) - (this.height / 2);
		
		this.centralButton = new DestinationButton(this, 0, 0, new Location(tileEntity.getLevel(), tileEntity.getBlockPos()), -1, true, false, "", true, false, null);
		
		final Location selectedLoc = tileEntity.getCurrentTarget();
		
		int listY = 0;
		int index = 0;
		for (NostrumObeliskTarget target: tileEntity.getTargets()) {
			boolean valid = tileEntity.canAffordTeleport(target.getLocation());
			//boolean valid = true; // Would be cool, but need some communication
			// between the server and client to get a list of actual valid ones,
			// since the client doesn't have those chunks loaded and returns
			// null or air blocks when fetching blockstates
			
			final boolean selected = selectedLoc != null && selectedLoc.equals(target.getLocation());
			
			// Always add list button
			listButtons.add(
					new DestinationButton(this, 10, 50 + (listY * 20), target.getLocation(), index, false, true, target.getTitle(), valid, selected, null));
			listRemoveButtons.add(
					new RemoveButton(this, (this.width / 3) - 16, 50 + (listY * 20), index));
			listY++;
			
			// Only add float button if in same dimension
			if (DimensionUtils.DimEquals(tileEntity.getLevel().dimension(), target.getLocation().getDimension())) {
				DestinationButton button = new DestinationButton(this, 0, 0, target.getLocation(), index, false, false, target.getTitle(), valid, selected, this.centralButton);
				floatingButtons.add(button);
			}
			index++;
		}
		
		this.addButton(centralButton);
		for (AbstractWidget w : floatingButtons) addButton(w);
		for (AbstractWidget w : listButtons) addButton(w);
		for (AbstractWidget w : listRemoveButtons) addButton(w);
		
		for (DestinationButton butt : listButtons) {
			butt.visible = drawList;
		}
		for (RemoveButton butt : listRemoveButtons) {
			butt.visible = drawList;
		}
		
		this.updateButtons();
	}
	
	@Override	
	public void tick() {
		;
	}
	
	@Override
	public void render(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {

		//GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.setShaderTexture(0, background);
		
		double time = (float) ((double) System.currentTimeMillis() / 15000.0);
		int panX = (int) (Math.sin(time) * TEXT_BACK_PAN);
		
		RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0, (TEXT_BACK_PAN / 2) + panX, 0, TEXT_BACK_WIDTH, TEXT_BACK_HEIGHT, width, height, TEXT_WHOLE_WIDTH, TEXT_WHOLE_HEIGHT,
				1f, 1f, 1f, 1f);
		
		if (this.centralButton == null) {
			// No targets. Draw error string
			RenderFuncs.drawSplitString(matrixStackIn, this.font, errorString, (this.width - (this.width / 2)) / 2, this.height / 2 - 100, this.width / 2, 0xFFFFFF);
			return;
		}
		
		for (DestinationButton other : floatingButtons)
		{
			renderLine(matrixStackIn, centralButton, other, partialTicks);
		}
		
		// Do buttons
		centralButton.render(matrixStackIn, mouseX, mouseY, partialTicks);
		for (DestinationButton butt : floatingButtons) {
			butt.render(matrixStackIn, mouseX, mouseY, partialTicks);
		}
		
		if (drawList) {
//			GlStateManager.pushLightingAttributes();
			RenderFuncs.drawRect(matrixStackIn, 0, 0, this.width / 3, this.height, 0xFF304060);
//			GlStateManager.popAttributes();
			
			int left = (this.width / 3) - 14;
			boolean mouseover = (mouseX >= left && mouseX <= left + 14 && mouseY <= 14);
			RenderSystem.setShaderTexture(0, background);
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, left, 0, 42 + (mouseover ? 14 : 0), 78, 14, 14, 14, 14, TEXT_WHOLE_WIDTH, TEXT_WHOLE_HEIGHT,
					1f, 1f, 1f, 1f);
		} else {
			int left = 0;
			boolean mouseover = (mouseX >= left && mouseX <= left + 14 && mouseY <= 14);
			RenderSystem.setShaderTexture(0, background);
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, left, 0, 42 + (mouseover ? 14 : 0), 64, 14, 14, 14, 14, TEXT_WHOLE_WIDTH, TEXT_WHOLE_HEIGHT,
					1f, 1f, 1f, 1f);
		}
		
		//GlStateManager.color4f(1f, 1f, 1f, 1f);
		//GlStateManager.enableBlend();
		//GlStateManager.disableLighting();
		this.font.draw(matrixStackIn, "<" + xOffset + "," + yOffset + ">", 35, 5, 0xFFFFFFFF);
		this.font.draw(matrixStackIn, "Scale: " + this.scale, 35, 20, 0xFFFFFFFF);
		
		for (DestinationButton butt : listButtons) {
			butt.render(matrixStackIn, mouseX, mouseY, partialTicks);
		}
		
		for (RemoveButton butt : listRemoveButtons) {
			butt.render(matrixStackIn, mouseX, mouseY, partialTicks);
		}
		
	}
	
	@Override
	public boolean isPauseScreen() {
		return true;
	}
	
//	@Override
//	public void handleMouseInput() throws IOException {
//		int dWheel = Mouse.getDWheel();
//		if (dWheel != 0) {
//			int mx = Mouse.getEventX() * this.width / this.mc.displayWidth;
//	        int my = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
//			handleMouseScroll(dWheel > 0 ? 1 : -1, mx, my);
//		} else
//			super.handleMouseInput();
//	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double dx) {
		double diff = .05 * (scale / .5);
		float oldScale = (scale * (float) zoomScaleFactor);
		float newScale = (float) Math.max(0.01f, Math.min(scale - dx * diff, 2.0));
		panWithScroll(oldScale, (newScale * (float) zoomScaleFactor), mouseX, mouseY);
		this.scale = newScale;
		updateButtons();
		return true;
	}
	
	// Change offsets to match new scale, so mouseX and Y are in the same spot
	private void panWithScroll(float scaleOld, float scaleNew, double mouseX, double mouseY) {
		float diffOrig = (1f/scaleNew) - (1f/scaleOld);
		float diff = diffOrig;
		
		// X
		int worldPos = unscale((int) (xOffset + mouseX));
		diff *= worldPos;
		xOffset = (int) diff + xOffset;

		diff = diffOrig;
		// Y
		worldPos = unscale((int) (yOffset + mouseY));
		diff *= worldPos;
		yOffset = (int) diff + yOffset;
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		if (mouseButton == 0) {
			int left;
			if (drawList) {
				left = (this.width / 3) - 14;
			} else {
				left = 0;
			}
			
			boolean mouseover = (mouseX >= left && mouseX <= left + 14 && mouseY <= 14);
			if (mouseover) {
				// click the list button
				drawList = !drawList;
				for (DestinationButton butt : listButtons) {
					butt.visible = drawList;
				}
				for (RemoveButton butt : listRemoveButtons) {
					butt.visible = drawList;
				}
				centralButton.playDownSound(this.mc.getSoundManager());
				this.updateButtons();
				return true;
			} else if (drawList && mouseX < this.width / 3) {
				; //return true;
			} else {
				mouseClickX = mouseX;
				mouseClickY = mouseY;
				mouseClickXOffset = xOffset;
				mouseClickYOffset = yOffset;
				// fall through
			}
		}
		
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_) {
		if (mouseClickX > 0) {
			mouseClickX = -1;
			mouseClickY = -1;
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double idk, double idk2) {
		if (clickedMouseButton == 0 && mouseClickX > 0 && mouseClickY >= 0) {
			xOffset = (int) (mouseClickXOffset + (mouseClickX - mouseX));
			yOffset = (int) (mouseClickYOffset + (mouseClickY - mouseY));
			
			updateButtons();
			return true;
		}
		
		return super.mouseDragged(mouseX, mouseY, clickedMouseButton, idk, idk2);
	}
	
	private void updateButtons() {
		if (!floatingButtons.isEmpty()) {
			for (DestinationButton button : floatingButtons) {
				translateButton(button);
			}
			translateButton(this.centralButton);
		}
		
	}
	
	private void renderLine(PoseStack matrixStackIn, DestinationButton center, DestinationButton other, float partialTicks) {
		matrixStackIn.pushPose();
		//GlStateManager.pushLightingAttributes();
		matrixStackIn.translate(TEXT_ICON_LENGTH / 2, TEXT_ICON_LENGTH / 2, 0);
		final Matrix4f transform = matrixStackIn.last().pose();
		
		final float red, green, blue, alpha;
		final int segments;
		final int highlightInterval;
		if (other.isSelected) {
			red = .6f;
			green = .6f;
			blue = .7f;
			alpha = 1f;
			segments = 20;
			highlightInterval = 5;
		} else {
			red = .3f;
			green = .3f;
			blue = .3f;
			alpha = .8f;
			segments = 2;
			highlightInterval = 0;
		}
		
		final int highlightIdxOffset = (int) ((System.currentTimeMillis() / 200) % segments);
		{
			// figure out offset based on time to animate
		}
		
		final float diffX = other.x - center.x;
		final float diffY = other.y - center.y;
		final float diffPerX = diffX / (segments-1);
		final float diffPerY = diffY / (segments-1);
		
		
		BufferBuilder buf = Tesselator.getInstance().getBuilder();
		RenderSystem.enableBlend();
		RenderSystem.disableTexture();
		RenderSystem.lineWidth(3f);
		RenderSystem.enableDepthTest();
        //GlStateManager.disableTexture();
        //GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        //GlStateManager.color4f(1.0f, 1.0f, 1.0f, 0.6f);
        buf.begin(GL11.GL_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i < segments; i++) {
        	final float X = center.x + (diffPerX * i);
        	final float Y = center.y + (diffPerY * i);
        	
        	final float r, g, b;
        	if (highlightInterval != 0 && i % highlightInterval == highlightIdxOffset % highlightInterval) {
        		r = red;
        		g = green;
        		b = blue + .3f;
        	} else {
        		r = red;
        		g = green;
        		b = blue;
        	}
        	
	        buf.vertex(transform, X, Y, 0).color(r, g, b, alpha).endVertex();
        }
        Tesselator.getInstance().end();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
		
//        GlStateManager.popAttributes();
        matrixStackIn.popPose();
	}
	
	protected void onDestinationClicked(Button button) {
		if (!button.visible)
			return;
		
		if (button == this.centralButton)
			return;

		DestinationButton butt = (DestinationButton) button;
		if (!butt.isValid)
			return;
		
		NostrumMagica.instance.proxy.setObeliskIndex(tileEntity.getBlockPos(), butt.obeliskIndex);
		Minecraft.getInstance().setScreen(null);
	}
	
	protected void onRemoveClicked(RemoveButton button) {
		NostrumMagica.instance.proxy.removeObeliskIndex(tileEntity.getBlockPos(), button.obeliskIndex);
		Minecraft.getInstance().setScreen(null);
	}
	
	static class RemoveButton extends Button {
		private final int obeliskIndex;
		
		public RemoveButton(ObeliskScreen screen, int x, int y, int index) {
			super(x, y, 13, 13, TextComponent.EMPTY, (b) -> {
				screen.onRemoveClicked((RemoveButton) b);
			});
			this.obeliskIndex = index;		
		}
		
		@Override
		public void renderButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
			final Minecraft mc = Minecraft.getInstance();
			final float sat = (this.isHovered() ? 1f : .8f);
			
			mc.getTextureManager().bind(background);
            RenderSystem.enableBlend();
            RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStack, x, y,
            		TEXT_REMOVE_BUTT_HOFFSET, TEXT_REMOVE_BUTT_VOFFSET,
            		TEXT_REMOVE_BUTT_LENT, TEXT_REMOVE_BUTT_LENT, TEXT_WHOLE_WIDTH, TEXT_WHOLE_HEIGHT,
    				sat, sat, sat, alpha);
            RenderSystem.disableBlend();
		}
	}
	
	@OnlyIn(Dist.CLIENT)
    static class DestinationButton extends Button
    {
        private final Location loc;
        private final int obeliskIndex;
        private final boolean isListed;
        private final String title;
        private final boolean isCenter;
        private final boolean isValid;
        private final boolean isSelected;
        private final @Nullable DestinationButton parentButton;
        
        public boolean isHovered;

        public DestinationButton(ObeliskScreen screen, int parPosX, int parPosY, 
        		Location loc, int index, boolean isCenter, boolean isListed, String title,
              boolean isValid, boolean isSelected, @Nullable DestinationButton parentButton) {
            super(parPosX, parPosY, 13, 13, TextComponent.EMPTY, (b) -> {
            	screen.onDestinationClicked(b);
            });
            this.loc = loc;
            this.obeliskIndex = index;
            this.isListed = isListed;
            this.title = title;
            this.isCenter = isCenter;
            this.isValid = isValid;
            this.isSelected = isSelected;
            this.parentButton = parentButton;
            
            isHovered = false;
        }
        
        /**
         * Draws this button to the screen.
         */
        @Override
        public void render(PoseStack matrixStackIn, int parX, int parY, float partialTicks) {
            if (visible) {
                final Minecraft mc = Minecraft.getInstance();
                isHovered = parX >= x 
                        && parY >= y 
                        && parX < x + width 
                        && parY < y + height;
                int textureX = 0;
                int textureY = TEXT_BACK_HEIGHT;
                if (isCenter) {
                	textureX += TEXT_ICON_LENGTH;
                }
                
                
                float alpha, red, green, blue;
                
                if (isSelected) {
                	red = .4f;
                	green = .6f;
                	blue = .4f;
                	alpha = 1f;
                } else if (isCenter) {
                	red = .4f;
                	green = .6f;
                	blue = .8f;
                	alpha = 1f;
                } else if (isValid) {
                	red = .6f;
                	green = .2f;
                	blue = .6f;
                	alpha = 1f;
                } else {
                	red = .8f;
                	green = .3f;
                	blue = .35f;
                	alpha = .8f;
                }
                
                
                if (isHovered && !isCenter && !isSelected) {
                	red += .2f;
                	green += .2f;
                	blue += .2f;
                }
                
                
                mc.getTextureManager().bind(background);
                RenderSystem.enableBlend();
                RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, x, y, textureX,
        				textureY, TEXT_ICON_LENGTH, TEXT_ICON_LENGTH, TEXT_WHOLE_WIDTH, TEXT_WHOLE_HEIGHT,
        				red, green, blue, alpha);
                RenderSystem.disableBlend();
                
                if (!isCenter) {
                	// Draw the name below
                	Font fonter = mc.font;
                	int textWidth = fonter.width(title);
                	int buttonWidth = TEXT_ICON_LENGTH;
                	int color = isValid ? 0xB0B0B0 : 0xB05050;
                	if (isListed) {
                		// Draw to the right
                		fonter.draw(matrixStackIn, title, x + buttonWidth + 5, y + ((buttonWidth - fonter.lineHeight + 1) / 2), color);
                		if (isSelected) {
	                		matrixStackIn.pushPose();
	                		matrixStackIn.translate(x + (buttonWidth/2), y + buttonWidth - 1, 0);
	                		matrixStackIn.scale(.5f, .5f, 1f);
	                		fonter.draw(matrixStackIn, "Active", -14, 0, color);
	                		matrixStackIn.popPose();
                		}
                	} else {
                		// Draw above
                		int xPos = x + (buttonWidth / 2) - (textWidth / 2);
                		fonter.draw(matrixStackIn, title, xPos, y - (2 + fonter.lineHeight), color);
                		
                		if (isSelected) {
	                		matrixStackIn.pushPose();
	                		matrixStackIn.translate(x + (buttonWidth/2), y - 2, 0);
	                		matrixStackIn.scale(.5f, .5f, 1f);
	                		fonter.draw(matrixStackIn, "Active", -14, 0, color);
	                		matrixStackIn.popPose();
                		}
                	}
                } else {
                	final String title = "This Obelisk";
                	Font fonter = mc.font;
                	int textWidth = fonter.width(title);
                	int xPos = x + (TEXT_ICON_LENGTH / 2) - (textWidth / 2);
            		fonter.draw(matrixStackIn, title, xPos, y - (2 + fonter.lineHeight), 0xB0B0B0);
                }
                
            }
        }
    }
	
}
