package com.smanzana.nostrummagica.client.gui.mirror;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.SpellComponentIcon;
import com.smanzana.nostrummagica.client.gui.book.BookScreen;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class MirrorGui extends Screen implements IMirrorScreen {
	
	private static final ResourceLocation RES_BACK_CLOUD = NostrumMagica.Loc("textures/gui/mirror_back_clouds.png");
	private static final ResourceLocation RES_BASE = NostrumMagica.Loc("textures/gui/mirror_base.png");
	
	private static final int TEX_WIDTH = 256;
	private static final int TEX_HEIGHT = 256;
	
	private static final int TEX_FRAME_HOFFSET = 0;
	private static final int TEX_FRAME_VOFFSET = 0;
	private static final int TEX_FRAME_WIDTH = 256;
	private static final int TEX_FRAME_HEIGHT = 128;
	
	private static final int TEX_MAJORTAB_HOFFSET = 0;
	private static final int TEX_MAJORTAB_VOFFSET = 128;
	private static final int TEX_MAJORTAB_WIDTH = 48;
	private static final int TEX_MAJORTAB_HEIGHT = 43;

	private static final int TEX_MAJORTAB_SEL_HOFFSET = TEX_MAJORTAB_HOFFSET + TEX_MAJORTAB_WIDTH;
	private static final int TEX_MAJORTAB_SEL_VOFFSET = TEX_MAJORTAB_VOFFSET;
	
	private static final int TEX_MINORTAB_HOFFSET = 96;
	private static final int TEX_MINORTAB_VOFFSET = 128;
	private static final int TEX_MINORTAB_WIDTH = 35;
	private static final int TEX_MINORTAB_HEIGHT = 28;
	
	private static final int TEX_MINORTAB_HIGH_HOFFSET = TEX_MINORTAB_HOFFSET + TEX_MINORTAB_WIDTH;
	private static final int TEX_MINORTAB_HIGH_VOFFSET = TEX_MINORTAB_VOFFSET;
	
	private static final int TEX_MINORTAB_SEL_HOFFSET = TEX_MINORTAB_HIGH_HOFFSET + TEX_MINORTAB_WIDTH;
	private static final int TEX_MINORTAB_SEL_VOFFSET = TEX_MINORTAB_VOFFSET;
	
	private static final int TEX_MINORTAB_NEW_HOFFSET = TEX_MINORTAB_SEL_HOFFSET + TEX_MINORTAB_WIDTH;
	private static final int TEX_MINORTAB_NEW_VOFFSET = TEX_MINORTAB_VOFFSET;
	private static final int TEX_MINORTAB_NEW_WIDTH = TEX_MINORTAB_WIDTH;
	private static final int TEX_MINORTAB_NEW_HEIGHT = TEX_MINORTAB_HEIGHT;
	
	private static final int POS_HMARGIN = 50;
	private static final int POS_VMARGIN = 30;
	
	private static final int POS_MAJORTAB_WIDTH = 48; // 40 ?
	private static final int POS_MAJORTAB_HEIGHT = 43;
	
	private static final int POS_MINORTAB_WIDTH = 35;
	private static final int POS_MINORTAB_HEIGHT = 28;
	
	//private INostrumMagic attr;
	private final Player player;
	private final @Nullable INostrumMagic attr;
	private final boolean unlocked;
	private final String unlockPrompt;
	
	private final List<MajorTabButton> majorTabs;
	private final List<MinorTabButton> minorTabs;
	private int guiWidth;
	private int guiHeight;
	private IMirrorSubscreen subscreen;
	private @Nullable IMirrorMinorTab lastMinorTab;
	
	private BookScreen currentInfoScreen = null;
	
	public MirrorGui(Player player) {
		super(new TextComponent("Nostrum Mirror"));
		this.player = player;
		this.attr = NostrumMagica.getMagicWrapper(player);
		this.unlocked = attr == null ? false : attr.isUnlocked();
		this.unlockPrompt = getUnlockPrompt(attr);
		majorTabs = new ArrayList<>();
		minorTabs = new ArrayList<>();
	}
	
	private void setScreen(IMirrorSubscreen subscreen) {
		if (subscreen != this.subscreen) {
			// Reset widgets to the base ones
			this.resetWidgets();
			
			lastMinorTab = null;
			
			// Hide old, then show new
			if (this.subscreen != null) {
				this.subscreen.hide(this, player);
			}
			this.subscreen = subscreen;
			this.subscreen.show(this, player, guiWidth, guiHeight, guiLeft(), guiTop());
		}
	}
	
	protected void addMainWidgets() {
		for (MajorTabButton tab : majorTabs) {
			this.addWidget(tab);
		}
	}
	
	@Override
	public void addWidget(AbstractWidget widget) {
		this.addWidget(widget);
	}

	@Override
	public void resetWidgets() {
		this.clearWidgets();
//		this.buttons.clear();
//		this.children.clear();
		
		this.minorTabs.clear();
		
		addMainWidgets();
	}

	@Override
	public void addMinorTab(IMirrorMinorTab tab) {
		MinorTabButton button = new MinorTabButton(this, tab,
				this.guiLeft() + (this.minorTabs.size() * POS_MINORTAB_WIDTH),
				this.guiTop() + this.guiHeight,
				POS_MINORTAB_WIDTH, POS_MINORTAB_HEIGHT
				);
		this.minorTabs.add(button);
		this.addWidget(button);
		
		// If this was first, act like it got clicked
		if (minorTabs.size() == 1) {
			this.onButtonMinorTab(button);
		}
	}

	@Override
	public void showPopupScreen(BookScreen popup) {
		this.currentInfoScreen = popup;
		this.currentInfoScreen.init(this.minecraft, this.width, this.height);
	}
	
	@Override
	public Screen getGuiHelper() {
		return this;
	}
	
	@Override
	public void init() {
		super.init();
		
		// Could keep square?
		this.guiWidth = this.width - (2 * POS_HMARGIN);
		this.guiHeight = this.height - (2 * POS_VMARGIN);
		
		int leftOffset = guiLeft();
		int topOffset = guiTop();
		
		final IMirrorSubscreen existingSubscreen = this.subscreen;
		lastMinorTab = null;
		
		{
			List<IMirrorSubscreen> subscreens = new ArrayList<>(4);
			subscreens.add(new MirrorCharacterSubscreen());
			subscreens.add(new MirrorQuestSubscreen());
			subscreens.add(new MirrorSkillSubscreen());
			subscreens.add(new MirrorResearchSubscreen());
			// Event to get others?

			majorTabs.clear();
			for (IMirrorSubscreen subscreen : subscreens) {
				majorTabs.add(new MajorTabButton(this, subscreen, leftOffset - POS_MAJORTAB_WIDTH, topOffset + (POS_MAJORTAB_HEIGHT * majorTabs.size()), POS_MAJORTAB_WIDTH, POS_MAJORTAB_HEIGHT));
			}
		
			if (existingSubscreen == null) {
				this.setScreen(subscreens.get(0));
			} else {
				boolean found = false;
				for (IMirrorSubscreen subscreen : subscreens) {
					if (subscreen.getName().equals(existingSubscreen.getName())) {
						this.setScreen(subscreen);
						found = true;
						break;
					}
				}
				
				if (!found) {
					this.setScreen(subscreens.get(0));
				}
			}
		}
	}
	
	protected int guiLeft() {
		return (this.width - guiWidth) / 2;
	}
	
	protected int guiTop() {
		return (this.height - guiHeight) / 2;
	}
	
	private void drawLockedScreenBackground(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
		RenderSystem.setShaderTexture(0, RES_BACK_CLOUD);
		RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0, 0, 0, TEX_WIDTH, TEX_HEIGHT, guiWidth, guiHeight, TEX_WIDTH, TEX_HEIGHT);
		
		int y = 0;
		String str = "Magic Not Yet Unlocked";
		int len = this.font.width(str);
		this.font.drawShadow(matrixStackIn, str, (this.guiWidth - len) / 2, guiHeight / 3, 0xFFFFFFFF);
		
		y = font.lineHeight + 2;
		
		len = this.font.width(unlockPrompt);
		this.font.draw(matrixStackIn, unlockPrompt, (this.guiWidth - len) / 2, y + (guiHeight / 3), 0xFFDFD000);
	}
	
	private void drawLockedScreenForeground(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
		if (attr == null) {
			return;
		}
		
		// DRAW ICONS
		EMagicElement element = null; // Which element we know
		SpellShape shape = null;
		
		Map<EMagicElement, Boolean> map = attr.getKnownElements();
		Boolean val;
		for (EMagicElement elem : map.keySet()) {
			val = map.get(elem);
			if (val != null && val) {
				element = elem;
				break;
			}
		}
		
		if (!attr.getShapes().isEmpty())
			shape = attr.getShapes().get(0);
		
		final int width = 24;
		final int space = 32;
		final long cycle = 1500;
		int x = (int) (.5 * guiWidth) + (-width) + (-space / 2);
		int y = (int)(guiHeight * .75f);
		int strLen;
		String str;

//		GlStateManager.enableBlend();
		RenderFuncs.drawRect(matrixStackIn, x - 2, y - 2, x + width + 2, y + width + 2, 0xA0000000);
		float[] color;
		if (element != null)
			color = new float[] {1f, 1f, 1f, 1f};
		else {
			color = new float[] {8f, .5f, .5f, .5f};
			element = EMagicElement.values()[
				  (int) (System.currentTimeMillis() / cycle) % EMagicElement.values().length
			      ];
		}
		SpellComponentIcon.get(element).draw(matrixStackIn, x, y, width, width, color[0], color[1], color[2], color[3]);
		str = I18n.get("element.name", new Object[0]);
		strLen = this.font.width(str);
		this.font.draw(matrixStackIn, str, (x + width / 2) - strLen/2, y - (3 + this.font.lineHeight), 0xFFFFFF);
		
		x += width + space;
		RenderFuncs.drawRect(matrixStackIn, x - 2, y - 2, x + width + 2, y + width + 2, 0xA0000000);
		if (shape != null)
			color = new float[] {1f, 1f, 1f, 1f};
		else {
			color = new float[] {.8f, .5f, .5f, .5f};
			Collection<SpellShape> shapes = SpellShape.getAllShapes();
			SpellShape[] shapeArray = shapes.toArray(new SpellShape[0]);
			shape = shapeArray[
				  (int) (System.currentTimeMillis() / cycle) % shapeArray.length
			      ];
		}
		SpellComponentIcon.get(shape).draw(matrixStackIn, x, y, width, width, color[0], color[1], color[2], color[3]);
		str = I18n.get("shape.name", new Object[0]);
		strLen = this.font.width(str);
		this.font.draw(matrixStackIn, str, (x + width / 2) - strLen/2, y - (3 + this.font.lineHeight), 0xFFFFFF);
	}
	
	private void drawResearchPages(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
		RenderFuncs.drawRect(matrixStackIn, 0, 0, this.width, this.height, 0x60000000);
		currentInfoScreen.render(matrixStackIn, mouseX, mouseY, partialTicks);
	}
	
	private void drawScreenBorder(PoseStack matrixStackIn, float partialTicks) {
		RenderSystem.setShaderTexture(0, RES_BASE);
		RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0,
				TEX_FRAME_HOFFSET, TEX_FRAME_VOFFSET, TEX_FRAME_WIDTH, TEX_FRAME_HEIGHT,
				guiWidth, guiHeight, TEX_WIDTH, TEX_HEIGHT);
	}
	
	@Override
	public void render(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
		final int leftOffset = guiLeft();
		final int topOffset = guiTop();
		
		// Draw fade background first
		RenderFuncs.drawRect(matrixStackIn, 0, 0, this.width, this.height, 0xDD000000);
		
		matrixStackIn.pushPose();
		
		// Setup mask so only central area is drawn in
		{
			matrixStackIn.pushPose();
			matrixStackIn.translate(0, 0, 10);
			RenderSystem.enableDepthTest();
			RenderSystem.depthMask(true);
			RenderSystem.colorMask(false, false, false, false);
			RenderFuncs.drawRect(matrixStackIn, 0, 0, this.width, this.height, 0xFF000000);
			RenderSystem.colorMask(true, true, true, true);
			matrixStackIn.popPose();
			
			RenderSystem.depthFunc(GL11.GL_GEQUAL);
			RenderFuncs.drawRect(matrixStackIn, guiLeft(), guiTop(), guiLeft() + guiWidth, guiTop() + guiHeight, 0xFF000000);
			RenderSystem.depthFunc(GL11.GL_LEQUAL);
		}
		matrixStackIn.translate(leftOffset, topOffset, 0);
		
		if (!unlocked) {
			drawLockedScreenBackground(matrixStackIn, mouseX, mouseY, partialTicks);
		} else {
			
			this.subscreen.drawBackground(this, matrixStackIn, guiWidth, guiHeight, mouseX, mouseY, partialTicks);
		}
		matrixStackIn.popPose();

		if (unlocked) {
			organizeTabs();
			super.render(matrixStackIn, mouseX, mouseY, partialTicks);
		}
		
		// Undo mask and allow free drawing again
		{
			matrixStackIn.pushPose();
			matrixStackIn.translate(0, 0, -800);
			RenderSystem.enableDepthTest();
			RenderSystem.depthMask(true);
			RenderSystem.depthFunc(GL11.GL_GEQUAL);
			RenderSystem.colorMask(false, false, false, false);
			RenderFuncs.drawRect(matrixStackIn, 0, 0, width, height, 0xFF000000);
			RenderSystem.colorMask(true, true, true, true);
			RenderSystem.depthFunc(GL11.GL_LEQUAL);
			matrixStackIn.popPose();
		}

		matrixStackIn.pushPose();
		matrixStackIn.translate(leftOffset, topOffset, 150);
		
//		// Black out surrounding screen
//		int color = 0xFF000000;
//		RenderFuncs.drawRect(matrixStackIn, 0, 0, this.width, topOffset, color);
//		RenderFuncs.drawRect(matrixStackIn, 0, topOffset + GUI_HEIGHT, this.width, this.height, color);
//		RenderFuncs.drawRect(matrixStackIn, 0, 0, leftOffset, this.height, color);
//		RenderFuncs.drawRect(matrixStackIn, leftOffset + TEXT_WIDTH - 1, 0, this.width, this.height, color);

		drawScreenBorder(matrixStackIn, partialTicks);
		
		if (!unlocked) {
			drawLockedScreenForeground(matrixStackIn, mouseX, mouseY, partialTicks);
		} else {
			
			this.subscreen.drawForeground(this, matrixStackIn, guiWidth, guiHeight, mouseX, mouseY, partialTicks);
		}
		matrixStackIn.popPose();
		
		for (GuiEventListener child : this.children()) {
			// Hacky 2
			if (!(child instanceof AbstractWidget widget)) {
				continue;
			}
			
			// Hacky
			if (widget instanceof MajorTabButton || widget instanceof MinorTabButton
					|| (mouseX > guiLeft() && mouseX < guiLeft() + guiWidth && mouseY > guiTop() && mouseY < guiTop() + guiHeight)) {
				widget.renderToolTip(matrixStackIn, mouseX, mouseY);
			}
		}
		
		if (unlocked && currentInfoScreen != null) {
			matrixStackIn.pushPose();
			matrixStackIn.translate(0, 0, 500);
			drawResearchPages(matrixStackIn, mouseX, mouseY, partialTicks);
			matrixStackIn.popPose();
		}
		

	}
	
	private static String getUnlockPrompt(INostrumMagic attr) {
		if (attr.isUnlocked())
			return "";
		
		Map<EMagicElement, Boolean> map = attr.getKnownElements();
		boolean found = false;
		for (EMagicElement elem : EMagicElement.values()) {
			if (map.get(elem) != null && map.get(elem)) {
				found = true;
				break;
			}
		}
		if (!found)
			return "Unlock an element";
		
		return "Unlock at least one shape";
	}
	
	protected boolean ignoreButton(Button button) {
		if (!button.visible) return true;
		if (currentInfoScreen != null) return true;
		
		return false;
	}
	
	protected void onButtonMajorTab(Button button) {
		if (ignoreButton(button)) return;
		
		this.setScreen(((MajorTabButton) button).getSubscreen());
	}
	
	protected void onButtonMinorTab(Button buttonIn) {
		if (ignoreButton(buttonIn)) return;
		
		MinorTabButton button = (MinorTabButton) buttonIn;
		lastMinorTab = button.tab; 
		button.tab.onClick(this, this.subscreen);
	}
	
	private void organizeTabs() {
		// Tabs can pop in and out of visibility. When they do, react to it!
		int count = 0;
		for (MajorTabButton tab : majorTabs) {
			if (tab.subscreen.isVisible(this, this.player)) {
				tab.y = this.guiTop() + (count++ * POS_MAJORTAB_HEIGHT);
			} else {
				tab.y = -500;
			}
		}
		count = 0;
		for (MinorTabButton tab : minorTabs) {
			if (tab.tab.isVisible(this, subscreen)) {
				tab.x = this.guiLeft() + (count++ * POS_MINORTAB_WIDTH);
			} else {
				tab.x = -500;
			}
		}
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		
		if (!unlocked) {
			return false;
		}
		
		if (currentInfoScreen != null) {
			return currentInfoScreen.mouseClicked(mouseX, mouseY, mouseButton);
		}
		
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		// Super gets the first widget at pos and only call it on it. Do the normal iteration that mouseClicked does instead instead.
		for(GuiEventListener iguieventlistener : this.children()) {
			if (iguieventlistener.mouseReleased(mouseX, mouseY, button)) {
				this.setDragging(false);
				return true;
			}
		}

		return false;
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double dx, double dy) {
		if (currentInfoScreen != null) {
			return false;
		}
		
		return super.mouseDragged(mouseX, mouseY, clickedMouseButton, dx, dy);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		// Super gets the first widget at pos and only call it on it. Do the normal iteration that mouseClicked does instead instead.
		for(GuiEventListener iguieventlistener : this.children()) {
			if (iguieventlistener.mouseScrolled(mouseX, mouseY, delta)) {
				this.setDragging(false);
				return true;
			}
		}

		return false;
	}
	
	@Override
	public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
		if (p_keyPressed_1_ == 256 && currentInfoScreen != null) {
			currentInfoScreen = null;
			return true;
		}
		
		return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
	}
	
	private static class MajorTabButton extends Button {
		
		private final MirrorGui gui;
		private final IMirrorSubscreen subscreen;
		
		public MajorTabButton(MirrorGui gui, IMirrorSubscreen subscreen, int x, int y, int width, int height) {
			super(x, y, width, height, TextComponent.EMPTY, (b) -> {
				gui.onButtonMajorTab(b);
			});
			this.gui = gui;
			this.subscreen = subscreen;
		}
		
		public IMirrorSubscreen getSubscreen() {
			return this.subscreen;
		}
		
		@Override
		public void renderButton(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
				final int textureX;
				final int textureY;
				if (this.isHovered()) {
					textureX = TEX_MAJORTAB_HOFFSET;
					textureY = TEX_MAJORTAB_VOFFSET;
				} else {
					textureX = TEX_MAJORTAB_SEL_HOFFSET;
					textureY = TEX_MAJORTAB_SEL_VOFFSET;
				}
				
				matrixStackIn.pushPose();
				matrixStackIn.translate(0, 0, 10); // Hackily make sure to render on top of children widgets and the screen mask
				
				RenderSystem.setShaderTexture(0, RES_BASE);
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, x, y,
						textureX, textureY, TEX_MAJORTAB_WIDTH, TEX_MAJORTAB_HEIGHT, this.width, this.height, TEX_WIDTH, TEX_HEIGHT);
				
				// Now draw icon
				RenderFuncs.RenderGUIItem(getSubscreen().getIcon(), matrixStackIn, x + (width - 16) / 2, y + (height - 16) / 2);
				
				matrixStackIn.popPose();
		}
		
		@Override
		public void renderToolTip(PoseStack matrixStackIn, int mouseX, int mouseY) {
			if (this.isHovered()) { 
				matrixStackIn.pushPose();
				matrixStackIn.translate(0, 0, 250);
				gui.renderTooltip(matrixStackIn, this.subscreen.getName(), mouseX, mouseY);
				matrixStackIn.popPose();
			}
		}
		
		@Override
		public boolean mouseReleased(double mouseX, double mouseY, int button) {
			return false;
		}
	}
	
	private static class MinorTabButton extends Button {
		
		private final MirrorGui gui;
		private final IMirrorMinorTab tab;
		
		public MinorTabButton(MirrorGui gui, IMirrorMinorTab tab, int x, int y, int width, int height) {
			super(x, y, width, height, TextComponent.EMPTY, (b) -> {
				gui.onButtonMinorTab(b);
			});
			this.gui = gui;
			this.tab = tab;
		}
		
		protected boolean isSelected() {
			return gui.lastMinorTab == this.tab;
		}
		
		@Override
		public void renderButton(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			final int textureX;
			final int textureY;
			if (isSelected()) {
				textureX = TEX_MINORTAB_HOFFSET;
				textureY = TEX_MINORTAB_VOFFSET;
			} else if (this.isHovered()) {
				textureX = TEX_MINORTAB_HIGH_HOFFSET;
				textureY = TEX_MINORTAB_HIGH_VOFFSET;
			} else {
				textureX = TEX_MINORTAB_SEL_HOFFSET;
				textureY = TEX_MINORTAB_SEL_VOFFSET;
			}
			
			matrixStackIn.pushPose();
			matrixStackIn.translate(x, y, 10); // Hackily make su re to render on top of children widgets
			
			RenderSystem.setShaderTexture(0, RES_BASE);
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0,
					textureX, textureY, TEX_MINORTAB_WIDTH, TEX_MINORTAB_HEIGHT, this.width, this.height, TEX_WIDTH, TEX_HEIGHT);
			
			// Now draw custom tab stuff like icons
			tab.renderTab(gui, gui.subscreen, matrixStackIn, this.width, this.height);
			
			// Draw new tab if there's something new
			if (tab.hasNewEntry(gui, gui.subscreen)) {
				RenderSystem.setShaderTexture(0, RES_BASE);
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0,
						TEX_MINORTAB_NEW_HOFFSET, TEX_MINORTAB_NEW_VOFFSET, TEX_MINORTAB_NEW_WIDTH, TEX_MINORTAB_NEW_HEIGHT,
						this.width, this.height, TEX_WIDTH, TEX_HEIGHT);
			}
			
			matrixStackIn.popPose();
		}
		
		@Override
		public void renderToolTip(PoseStack matrixStackIn, int mouseX, int mouseY) {
			if (this.isHovered()) {
				gui.renderTooltip(matrixStackIn, this.tab.getName(), mouseX, mouseY);
			}
		}
		
		@Override
		public boolean mouseReleased(double mouseX, double mouseY, int button) {
			return false;
		}
	}

}
