package com.smanzana.nostrummagica.client.gui.mirror;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.widget.MoveableObscurableWidget;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.ClientPurchaseSkillMessage;
import com.smanzana.nostrummagica.progression.skill.Skill;
import com.smanzana.nostrummagica.util.ColorUtil;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class MirrorSkillSubscreen extends PanningMirrorSubscreen {
	
	private static final ResourceLocation RES_ICONS = NostrumMagica.Loc("textures/gui/mirror_skills.png");
	private static final ResourceLocation RES_BACK = NostrumMagica.Loc("textures/gui/mirror_back_skills.png");
	
	private final ITextComponent name;
	private final ItemStack icon;
	private final Map<Skill, SkillButton> buttons;
	
	private PlayerEntity player;
	private INostrumMagic attr;
	private int width;
	private int height;
	
	public MirrorSkillSubscreen() {
		name = new TranslationTextComponent("mirror.tab.skill.name");
		icon = new ItemStack(NostrumItems.spellTomeCombat, 1);
		buttons = new HashMap<>();
	}
	
	@Override
	public ITextComponent getName() {
		return name;
	}
	
	@Override
	public ItemStack getIcon() {
		return icon;
	}
	
	@Override
	public boolean isVisible(IMirrorScreen parent, PlayerEntity player) {
		return true;
	}
	
	@Override
	public void show(IMirrorScreen parent, PlayerEntity player, int width, int height, int guiLeft, int guiTop) {
		this.player = player;
		attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null) {
			return;
		}
		
		this.width = width;
		this.height = height;
		
		buttons.clear();
		final Rectangle2d bounds = new Rectangle2d(guiLeft, guiTop, width, height);
		for (Skill skill : Skill.allSkills()) {
			final int buttonX = guiLeft + (width/2) + (skill.getPlotX() * GRID_SCALE);
			final int buttonY = guiTop + (height/2) + (-skill.getPlotY() * GRID_SCALE);
			
			SkillButton button = new SkillButton(this, skill,
					buttonX, buttonY,
					POS_BUTTON_WIDTH, POS_BUTTON_HEIGHT);
			button.setBounds(bounds);
			parent.addWidget(button);
			buttons.put(skill, button);
		}
		
		setButtonPositions();
		
		// Add parent last so it's below everything
		super.show(parent, player, width, height, guiLeft, guiTop);
	}

	@Override
	public void hide(IMirrorScreen parent, PlayerEntity player) {
		; // Not sure there's much to do. Parent will clean things up for us
	}

	@Override
	public void drawBackground(IMirrorScreen parent, MatrixStack matrixStackIn, int width, int height, int mouseX, int mouseY, float partialTicks) {
		float extra = .1f * (float) Math.sin((double) System.currentTimeMillis() / 1500.0);
		float inv = .1f - extra;
		
		Minecraft.getInstance().getTextureManager().bindTexture(RES_BACK);
		RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0,
				0, 0, TEX_BACK_WIDTH, TEX_BACK_HEIGHT,
				width, height, TEX_BACK_WIDTH, TEX_BACK_HEIGHT,
				.9f + extra, 1f, .8f + inv, 1f);
	}

	@Override
	public void drawForeground(IMirrorScreen parent, MatrixStack matrixStackIn, int width, int height, int mouseX, int mouseY, float partialTicks) {
		final Minecraft mc = Minecraft.getInstance();
		final FontRenderer font = mc.fontRenderer;
		matrixStackIn.push();
		
		matrixStackIn.translate(width/2, 20, 0);
		RenderFuncs.drawGradientRect(matrixStackIn, -40, 0, 40, 20,
				0xFF000000, 0xFF000000,
				0x40000000, 0x40000000);
		
		matrixStackIn.translate(0, 8, 0);
		
		String str = "Skillpoints: " + attr.getSkillPoints();
		int strWidth = font.getStringWidth(str);
		matrixStackIn.push();
		matrixStackIn.scale(.75f, .75f, 1f);
		font.drawString(matrixStackIn, str, -strWidth/2, 0, 0xFFFFFFFF);
		matrixStackIn.pop();
		
		matrixStackIn.translate(0, font.FONT_HEIGHT, 0);
		
		matrixStackIn.pop();
	}
	
	@Override
	protected void onPan(int panX, int panY, float scale) {
		setButtonPositions();
	}
	
	@Override
	protected void onZoom(int panX, int panY, float scale) {
		setButtonPositions();
	}
	
	protected void setButtonPositions() {
		for (SkillButton button : buttons.values()) {
			final int offsetX = button.getStartingX() - (width / 2);
			final int offsetY = button.getStartingY() - (height / 2);
			
			
			final int scaledX = (width/2) + (int) ((offsetX + this.getPanX()) * this.getPanScale());
			final int scaledY = (height/2) + (int) ((offsetY + this.getPanY()) * this.getPanScale());
			button.setPosition(scaledX, scaledY);
		}
	}
	
	protected void onSkillButton(SkillButton button) {
		//if (ignoreButton(button)) return;
		
		final Skill skill = button.skill;
		final INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		
		if (button.state == SkillState.AVAILABLE && attr != null && attr.getSkillPoints() > 0) {
			// do on client too for instant feedback
			attr.addSkill(skill);
			
			NetworkHandler.sendToServer(
				new ClientPurchaseSkillMessage(skill)	
				);
			
			button.updateState();
		}
	}
	
	protected static enum SkillState {
		UNAVAILABLE,
		AVAILABLE,
		OWNED
	}
	
	private static class SkillButton extends MoveableObscurableWidget {
		
		private final MirrorSkillSubscreen subscreen;
		private final Skill skill;
		
		private final List<ITextComponent> tooltip;
		private final float fontScale = 0.75f;
		
		private SkillState state;
		
		public SkillButton(MirrorSkillSubscreen subscreen, Skill skill, int x, int y, int width, int height) {
			super(x, y, width, height, StringTextComponent.EMPTY);
			this.subscreen = subscreen;
			this.skill = skill;
			this.tooltip = genTooltip();
			updateState();
		}
		
		protected void updateState() {
			if (subscreen.attr.hasSkill(skill)) {
				this.state = SkillState.OWNED;
			} else if (!skill.isHidden(subscreen.player) && subscreen.attr.getSkillPoints() > 0) {
				this.state = SkillState.AVAILABLE;
			} else {
				this.state = SkillState.UNAVAILABLE;
			}
		}
		
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (!this.isHidden()) {
				return super.mouseClicked(mouseX, mouseY, button);
			}
			
			return false;
		}
		
		@Override
		public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
			return false;
		}
		
		@Override
		public boolean mouseReleased(double mouseX, double mouseY, int button) {
			return false;
		}
		
		@Override
		public void onClick(double mouseX, double mouseY) {
			subscreen.onSkillButton(this);
		}
		
		public void drawTreeLines(MatrixStack matrixStackIn, Minecraft mc) {
			if (skill.getParentKey() != null) {
				Skill parent = Skill.lookup(this.skill.getParentKey());
				if (parent == null)
					return;
				
				SkillButton other = subscreen.buttons.get(parent);
				if (other != null && !other.isHidden())
					renderLine(matrixStackIn, other);
			}
		}
		
		private void renderLine(MatrixStack matrixStackIn, SkillButton other) {
			matrixStackIn.push();
//			GlStateManager.pushLightingAttributes();
			matrixStackIn.translate(width / 2, height / 2, 0);
			
			final Matrix4f transform = matrixStackIn.getLast().getMatrix();
			
			BufferBuilder buf = Tessellator.getInstance().getBuffer();
			RenderSystem.enableBlend();
			RenderSystem.disableTexture();
			RenderSystem.lineWidth(3f);
	        //GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//	        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 0.6f);
	        buf.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
	        buf.pos(transform, x, y, 0).color(1f, 1f, 1f, .6f).endVertex();
	        buf.pos(transform, other.x, other.y, 0).color(1f, 1f, 1f, .6f).endVertex();
	        Tessellator.getInstance().draw();
	        RenderSystem.enableTexture();
//	        GlStateManager.enableTexture();
	        RenderSystem.disableBlend();
			RenderSystem.lineWidth(1f);
			
//	        GlStateManager.popAttributes();
	        matrixStackIn.pop();
		}
		
		@Override
		public void render(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			// Render tree lines even if we're out of bounds
			if (!this.isHidden()) {
				updateState();
				matrixStackIn.push();
				matrixStackIn.translate(0, 0, 0);
				drawTreeLines(matrixStackIn, Minecraft.getInstance());
				matrixStackIn.translate(0, 0, 1);
				super.render(matrixStackIn, mouseX, mouseY, partialTicks);
				matrixStackIn.pop();
			}
		}
		
		@Override
		public void renderButton(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			int u = TEX_ICON_BUTTON_HOFFSET;
			int v = TEX_ICON_BUTTON_VOFFSET;
			float[] color = ColorUtil.ARGBToColor(skill.getCategory().getColor());
			if (this.state == SkillState.UNAVAILABLE) {
				color[0] *= .2f;
				color[1] *= .2f;
				color[2] *= .2f;
			} else if (this.state == SkillState.OWNED) {
				u = TEX_ICON_BUTTON_OWNED_HOFFSET;
				v = TEX_ICON_BUTTON_OWNED_VOFFSET;
			}
			
			if (this.isHovered()) {
				color[0] *= .8f;
				color[1] *= .8f;
				color[2] *= .8f;
			}
			
			RenderSystem.enableBlend();
			Minecraft.getInstance().getTextureManager().bindTexture(RES_ICONS);
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, x, y,
					u, v, TEX_ICON_BUTTON_WIDTH, TEX_ICON_BUTTON_HEIGHT,
					this.width, this.height, TEX_ICON_WIDTH, TEX_ICON_HEIGHT,
					color[0], color[1], color[2], color[3]);

			// Icon
			matrixStackIn.push();
			RenderHelper.enableStandardItemLighting();
			// RenderGuiItem moves 100 forward. Blocks render several z deep.
			// Squish to 8 deep, and shift back
			matrixStackIn.scale(1f, 1f, .4f);
			matrixStackIn.translate(x + (width/2), y + (height / 2), -90);
			matrixStackIn.scale(.75f, .75f, 1f);
			RenderFuncs.RenderGUIItem(skill.getIcon(), matrixStackIn, (- 16) / 2, (- 16) / 2);
			RenderSystem.enableDepthTest();
			RenderHelper.disableStandardItemLighting();
			
			matrixStackIn.pop();
		}
		
		@Override
		public void renderToolTip(MatrixStack matrixStackIn, int mouseX, int mouseY) {
			if (this.isHovered()) { 
				final Minecraft mc = Minecraft.getInstance();
				final FontRenderer font = mc.fontRenderer;
				matrixStackIn.push();
				matrixStackIn.scale(fontScale, fontScale, 1f);
				matrixStackIn.translate((int) (mouseX / fontScale) - mouseX, (int) (mouseY / fontScale) - mouseY, 0);
				GuiUtils.drawHoveringText(matrixStackIn, tooltip, mouseX, mouseY, subscreen.width, subscreen.height, 400, font);
				matrixStackIn.pop();
			}
		}
		
		private List<ITextComponent> genTooltip() {
			List<ITextComponent> tooltip = new LinkedList<>();
			tooltip.add(((TextComponent) skill.getName()).mergeStyle(TextFormatting.BLUE));
			tooltip.addAll(skill.getDescription());
			
			if (this.subscreen.attr.hasSkill(skill)) {
				tooltip.add(new StringTextComponent(" "));
				tooltip.add(new StringTextComponent("Owned").mergeStyle(TextFormatting.BOLD, TextFormatting.DARK_GREEN));
			}
			
			return tooltip;
		}
		
		public boolean isHidden() {
			return skill.isHidden(subscreen.player) || !skill.meetsRequirements(subscreen.player);
		}
	}
	
	private static final int TEX_BACK_WIDTH = 256;
	private static final int TEX_BACK_HEIGHT = 256;
	
	private static final int TEX_ICON_WIDTH = 64;
	private static final int TEX_ICON_HEIGHT = 64;
	
	private static final int TEX_ICON_BUTTON_HOFFSET = 0;
	private static final int TEX_ICON_BUTTON_VOFFSET = 0;
	private static final int TEX_ICON_BUTTON_WIDTH = 32;
	private static final int TEX_ICON_BUTTON_HEIGHT = 32;
	
	private static final int TEX_ICON_BUTTON_OWNED_HOFFSET = TEX_ICON_BUTTON_HOFFSET + TEX_ICON_BUTTON_WIDTH;
	private static final int TEX_ICON_BUTTON_OWNED_VOFFSET = TEX_ICON_BUTTON_VOFFSET;
	
	private static final int POS_BUTTON_WIDTH = 32;
	private static final int POS_BUTTON_HEIGHT = 32;
	
	private static final int GRID_SCALE = POS_BUTTON_WIDTH + 8;

}
