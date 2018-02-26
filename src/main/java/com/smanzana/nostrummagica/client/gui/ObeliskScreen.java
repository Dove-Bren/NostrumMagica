package com.smanzana.nostrummagica.client.gui;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.NostrumObelisk;
import com.smanzana.nostrummagica.blocks.NostrumObelisk.NostrumObeliskEntity;
import com.smanzana.nostrummagica.config.ModConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ObeliskScreen extends GuiScreen {
	
	protected static final ResourceLocation background = new ResourceLocation(NostrumMagica.MODID + ":textures/gui/container/obelisk.png");
	
	protected static final int TEXT_BACK_WIDTH = 192;
	protected static final int TEXT_BACK_PAN = 64;
	protected static final int TEXT_BACK_HEIGHT = 64;
	protected static final int TEXT_WHOLE_WIDTH = 256;
	protected static final int TEXT_WHOLE_HEIGHT = 256;
	protected static final int TEXT_ICON_LENGTH = 14;
	
	private static final String NO_DEST_KEY = "info.obelisk.none";

	private static int zoomScaleFactor = 8;
	
	private NostrumObeliskEntity tileEntity;
	private int xOffset;
	private int yOffset;
	private float scale;
	private DestinationButton centralButton;
	private List<DestinationButton> floatingButtons;
	private List<DestinationButton> listButtons;
	private boolean drawList;
	
	private int mouseClickX;
	private int mouseClickY;
	private int mouseClickXOffset; //xoffset at time of click
	private int mouseClickYOffset; //yoffset at time of click
	private String errorString;
	
	public ObeliskScreen(NostrumObeliskEntity tileEntity) {
		this.tileEntity = tileEntity;
		drawList = ModConfig.config.getObeliskList();
		
		errorString = I18n.format(NO_DEST_KEY, new Object[0]);
		this.floatingButtons = new LinkedList<>();
		this.listButtons = new LinkedList<>();
		this.scale = 1.0f;
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
		button.xPosition = getScaled(button.pos.getX()) - xOffset;
		button.yPosition = getScaled(button.pos.getZ()) - yOffset;
	}
	
	private String getTitleString(BlockPos pos) {
		return "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
	}
	
	@Override
	public void initGui() {
		if (tileEntity.getTargets().isEmpty())
			return;
		
		float xDiv = this.drawList ? (2f/3f) : .5f;
		this.xOffset = getScaled(tileEntity.getPos().getX()) - (int) (this.width * (xDiv));
		this.yOffset = getScaled(tileEntity.getPos().getZ()) - (this.height / 2);
		int id = 0;
		
		this.centralButton = new DestinationButton(id++, 0, 0, tileEntity.getPos(), true, false, "", true);
		
		int listY = 0;
		for (BlockPos pos : tileEntity.getTargets()) {
			boolean valid = NostrumObelisk.isValidTarget(tileEntity.getWorld(), tileEntity.getPos(), pos);
			
			if (drawList) {
				listButtons.add(
						new DestinationButton(id++, 10, 50 + (listY++ * 20), pos, false, true, getTitleString(pos), valid));
			}
			DestinationButton button = new DestinationButton(id++, 0, 0, pos, false, false, getTitleString(pos), valid);
			floatingButtons.add(button);
		}
		
		this.buttonList.add(centralButton);
		this.buttonList.addAll(floatingButtons);
		this.buttonList.addAll(listButtons);
		
		this.updateButtons();
	}
	
	@Override	
	public void updateScreen() {
		;
	}
	
	@Override
	public void drawScreen(int parWidth, int parHeight, float p_73863_3_) {

		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		Minecraft.getMinecraft().getTextureManager().bindTexture(background);
		
		double time = (float) ((double) Minecraft.getSystemTime() / 15000);
		int panX = (int) (Math.sin(time) * TEXT_BACK_PAN);
		
		Gui.drawScaledCustomSizeModalRect(0, 0, (TEXT_BACK_PAN / 2) + panX, 0, TEXT_BACK_WIDTH, TEXT_BACK_HEIGHT, width, height, TEXT_WHOLE_WIDTH, TEXT_WHOLE_HEIGHT);
		
		if (this.centralButton == null) {
			// No targets. Draw error string
			this.fontRendererObj.drawSplitString(errorString, (this.width - (this.width / 2)) / 2, this.height / 2 - 100, this.width / 2, 0xFFFFFF);
			return;
		}
		
		if (drawList) {
			GlStateManager.pushAttrib();
			drawRect(0, 0, this.width / 3, this.height, 0xFF304060);
			GlStateManager.popAttrib();
		}
		
		this.fontRendererObj.drawString("<" + xOffset + "," + yOffset + ">", 5, 5, 0xFFFFFF);
		this.fontRendererObj.drawString("Scale: " + this.scale, 5, 20, 0xFFFFFFFF);
		
		for (DestinationButton other : floatingButtons)
		{
			renderLine(centralButton, other);
		}
		
		// Do buttons and other parent stuff
		super.drawScreen(parWidth, parHeight, p_73863_3_);
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return true;
	}
	
	@Override
	public void handleMouseInput() throws IOException {
		int dWheel = Mouse.getDWheel();
		if (dWheel != 0) {
			int mx = Mouse.getEventX() * this.width / this.mc.displayWidth;
	        int my = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
			handleMouseScroll(dWheel > 0 ? 1 : -1, mx, my);
		} else
			super.handleMouseInput();
	}
	
	protected void handleMouseScroll(int dx, int mouseX, int mouseY) {
		double diff = .05 * (scale / .5);
		float oldScale = (scale * (float) zoomScaleFactor);
		float newScale = (float) Math.max(0.01f, Math.min(scale - dx * diff, 2.0));
		panWithScroll(oldScale, (newScale * (float) zoomScaleFactor), mouseX, mouseY);
		this.scale = newScale;
		updateButtons();
	}
	
	// Change offsets to match new scale, so mouseX and Y are in the same spot
	private void panWithScroll(float scaleOld, float scaleNew, int mouseX, int mouseY) {
		float diffOrig = (1f/scaleNew) - (1f/scaleOld);
		float diff = diffOrig;
		
		// X
		int worldPos = unscale(xOffset + mouseX);
		diff *= worldPos;
		xOffset = (int) diff + xOffset;

		diff = diffOrig;
		// Y
		worldPos = unscale(yOffset + mouseY);
		diff *= worldPos;
		yOffset = (int) diff + yOffset;
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		mouseClickX = mouseX;
		mouseClickY = mouseY;
		mouseClickXOffset = xOffset;
		mouseClickYOffset = yOffset;
		
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		
	}
	
	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		xOffset = mouseClickXOffset + (mouseClickX - mouseX);
		yOffset = mouseClickYOffset + (mouseClickY - mouseY);
		
		updateButtons();
		
		super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
	}
	
	private void updateButtons() {
		if (!floatingButtons.isEmpty()) {
			for (DestinationButton button : floatingButtons) {
				translateButton(button);
			}
			translateButton(this.centralButton);
		}
		
	}
	
	private void renderLine(DestinationButton center, DestinationButton other) {
		GlStateManager.pushMatrix();
		GlStateManager.pushAttrib();
		GlStateManager.translate(TEXT_ICON_LENGTH / 2, TEXT_ICON_LENGTH / 2, 0);
		VertexBuffer buf = Tessellator.getInstance().getBuffer();
		//GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        //GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 0.6f);
        buf.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        buf.pos(center.xPosition, center.yPosition, 0).endVertex();
        buf.pos(other.xPosition, other.yPosition, 0).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.enableTexture2D();
        //GlStateManager.disableBlend();
		
        GlStateManager.popAttrib();
		GlStateManager.popMatrix();
	}
	
	@Override
	public void actionPerformed(GuiButton button) {
		if (!button.visible)
			return;
		
		if (button == this.centralButton)
			return;

		DestinationButton butt = (DestinationButton) button;
		if (!butt.isValid)
			return;
		
		NostrumMagica.proxy.requestObeliskTransportation(tileEntity.getPos(), ((DestinationButton) button).pos);		
		Minecraft.getMinecraft().displayGuiScreen(null);
	}
	
	@SideOnly(Side.CLIENT)
    static class DestinationButton extends GuiButton
    {
        private final BlockPos pos;
        private final boolean isListed;
        private final String title;
        private final boolean isCenter;
        private final boolean isValid;

        public DestinationButton(int parButtonId, int parPosX, int parPosY, 
              BlockPos pos, boolean isCenter, boolean isListed, String title,
              boolean isValid)
        {
            super(parButtonId, parPosX, parPosY, 23, 13, "");
            this.pos = pos;
            this.isListed = isListed;
            this.title = title;
            this.isCenter = isCenter;
            this.isValid = isValid;
        }

        /**
         * Draws this button to the screen.
         */
        @Override
        public void drawButton(Minecraft mc, int parX, int parY)
        {
            if (visible)
            {
                int textureX = 0;
                int textureY = TEXT_BACK_HEIGHT;
                if (isCenter) {
                	textureX = 2 * TEXT_ICON_LENGTH;
                } else {
                	if (parX >= xPosition 
                      && parY >= yPosition 
                      && parX < xPosition + width 
                      && parY < yPosition + height) {
                		textureX = TEXT_ICON_LENGTH;
                	}
                }
                
                float val = isValid ? 1.0f : .6f;
                GL11.glColor4f(val, 1.0f, val, val);
                mc.getTextureManager().bindTexture(background);
                GlStateManager.enableBlend();
                Gui.drawModalRectWithCustomSizedTexture(xPosition, yPosition, textureX, textureY,
        				TEXT_ICON_LENGTH, TEXT_ICON_LENGTH, TEXT_WHOLE_WIDTH, TEXT_WHOLE_HEIGHT);
                GlStateManager.disableBlend();
                
                if (!isCenter) {
                	// Draw the name below
                	FontRenderer fonter = mc.fontRendererObj;
                	int textWidth = fonter.getStringWidth(title);
                	int buttonWidth = TEXT_ICON_LENGTH;
                	int color = isValid ? 0xB0B0B0 : 0xB05050;
                	if (isListed) {
                		// Draw to the right
                		fonter.drawString(title, xPosition + buttonWidth + 5, yPosition + ((buttonWidth - fonter.FONT_HEIGHT + 1) / 2), color);
                	} else {
                		// Draw above
                		int x = xPosition + (buttonWidth / 2) - (textWidth / 2);
                		fonter.drawString(title, x, yPosition - (5 + fonter.FONT_HEIGHT), color);
                	}
                	
                }
                
            }
        }
    }
	
}
