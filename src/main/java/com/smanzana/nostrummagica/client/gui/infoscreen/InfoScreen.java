package com.smanzana.nostrummagica.client.gui.infoscreen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.IForegroundRenderable;
import com.smanzana.nostrummagica.client.gui.StackableScreen;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class InfoScreen extends StackableScreen {
	
	protected static final ResourceLocation background = new ResourceLocation(NostrumMagica.MODID + ":textures/gui/container/infoscreen.png");
	
	protected static final int TEXT_WHOLE_WIDTH = 256;
	protected static final int TEXT_WHOLE_HEIGHT = 256;
	protected static final int TEXT_BUTTON_TAB_VOFFSET = 24;
	
	protected static final int POS_TABS_HEIGHT = 36;
	protected static final int POS_BUTTONS_HEIGHT = 28;
	protected static final int POS_SUBSCREEN_VOFFSET = 8;
	
	private INostrumMagic attribute;
	private List<TabButton> tabs;
	private List<InfoButton> infoButtons;
	private IInfoSubScreen subscreen;
	private List<AbstractWidget> subscreenButtons;
	
	protected int globButtonID = 0;
	
	private int scrollY = 0;
	
	private @Nullable String startKey;
	
	public InfoScreen(INostrumMagic attribute, @Nullable String startKey) {
		super();
		this.attribute = attribute;
		this.startKey = startKey;
		//this.start = start;
		
//		tabs = new LinkedList<>();
//		buttons = new LinkedList<>();
//		subscreenButtons = new LinkedList<>();
//		subscreen = null;
//		scrollY = 0;
//		
//		InfoScreenTab.init();
//		
//		// Populate tabs
//		for (InfoScreenTabs tab : InfoScreenTabs.values()) {
//			InfoScreenTab inst = InfoScreenTab.get(tab);
//			if (inst == null || !inst.isVisible(this.attribute))
//				continue;
//			TabButton butt = new TabButton(globButtonID++,
//					2 + (tabs.size() * (2 + TabButton.TEXT_BUTTON_TAB_WIDTH)), 2,
//					this.attribute, inst);
//			if (butt.buttons == null || butt.buttons.isEmpty())
//				continue;
//			tabs.add(butt);
//			this.buttons.add(butt);
//		}
//		
//		// Open up startup location, if one was provided
//		if (start != null) {
//			String indexString = start.getInfoScreenKey();
//			for (TabButton tabButton : tabs) {
//				InfoButton page = tabButton.tab.lookup(indexString);
//				if (page != null) {
//					selectTab(tabButton);
//					selectScreen(page);
//					break;
//				}
//			}
//		}
	}
	
	public InfoScreen(INostrumMagic attribute, @Nullable InfoScreenIndexed start) {
		this(attribute, (start == null ? null : start.getInfoScreenKey()));
	}
	
	@Override
	public void init() {
		tabs = new LinkedList<>();
		infoButtons = new LinkedList<>();
		subscreenButtons = new LinkedList<>();
		subscreen = null;
		scrollY = 0;
		
		InfoScreenTab.init();
		
		// Populate tabs
		for (InfoScreenTabs tab : InfoScreenTabs.values()) {
			InfoScreenTab inst = InfoScreenTab.get(tab);
			if (inst == null || !inst.isVisible(this.attribute))
				continue;
			TabButton butt = new TabButton(
					2 + (tabs.size() * (2 + TabButton.TEXT_BUTTON_TAB_WIDTH)), 2,
					this.attribute, inst);
			if (butt.buttons == null || butt.buttons.isEmpty())
				continue;
			tabs.add(butt);
			this.addRenderableWidget(butt);
		}
		
		// Open up startup location, if one was provided
		if (startKey != null) {
			for (TabButton tabButton : tabs) {
				InfoButton page = tabButton.tab.lookup(startKey);
				if (page != null) {
					selectTab(tabButton);
					selectScreen(page);
					break;
				}
			}
		}
	}
	
	@Override
	public void resize(Minecraft mcIn, int w, int h) {
		// Save our current spot
		
		super.resize(mcIn, w, h);
	}
	
	@Override	
	public void tick() {
		;
	}
	
	@Override
	public void render(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {

		RenderFuncs.drawRect(matrixStackIn, 0, 0, width, height, 0xFF000000);
		
		// Figure out draw offset depending on how many buttons there are
		final int maxHorizontal = this.width / (InfoButton.BUTTON_WIDTH + 2);
		int xOffset = 0;
		int yOffset = POS_BUTTONS_HEIGHT + POS_BUTTONS_HEIGHT;
		if (infoButtons.size() > maxHorizontal * 2) {
			xOffset = (2 + InfoButton.BUTTON_WIDTH) * 5;
			yOffset = 10;
		}
		
		yOffset += POS_SUBSCREEN_VOFFSET;
		
		if (this.subscreen != null) {
			this.subscreen.draw(attribute, minecraft, matrixStackIn, xOffset, yOffset, width - xOffset, height - yOffset, mouseX, mouseY);
		}
		
		// Do buttons and other parent stuff
		super.render(matrixStackIn, mouseX, mouseY, partialTicks);
//		for (int i = 0; i < this.buttons.size(); ++i) {
//			this.buttons.get(i).render(matrixStackIn, mouseX, mouseY, partialTicks);
//		}
		
		// Mask out any partial buttons or buttons that are above button line, since we support scrolling
		matrixStackIn.pushPose();
		matrixStackIn.translate(0, 0, 500);
		if (scrollY > 0) {
			RenderFuncs.drawRect(matrixStackIn, 0, 0, width, POS_SUBSCREEN_VOFFSET, 0xFF000000);
		}
		
		for (int i = 0; i < this.tabs.size(); ++i) {
			((AbstractButton)this.tabs.get(i)).render(matrixStackIn, mouseX, mouseY, partialTicks);
		}
		matrixStackIn.popPose();

//		for (int j = 0; j < this.labelList.size(); ++j) {
//			((GuiLabel)this.labelList.get(j)).drawLabel(this.minecraft, mouseX, mouseY);
//		}
		
		if (this.subscreen != null) {
			this.subscreen.drawForeground(attribute, minecraft, matrixStackIn, xOffset, yOffset, width - xOffset, height - yOffset, mouseX, mouseY);
		}
		
		// Only show sub buttons if mouseY is lower than button  vertical offset
		if (mouseY > POS_SUBSCREEN_VOFFSET) {
			for (int i = 0; i < this.renderables.size(); ++i) {
				Widget w = this.renderables.get(i);
				if (w instanceof IForegroundRenderable) {
					((IForegroundRenderable)this.renderables.get(i)).renderForeground(matrixStackIn, mouseX, mouseY, partialTicks);
				}
			}
		}
		
		for (int i = 0; i < this.tabs.size(); ++i) {
			Widget w = this.renderables.get(i);
			if (w instanceof IForegroundRenderable) {
				((IForegroundRenderable)this.renderables.get(i)).renderForeground(matrixStackIn, mouseX, mouseY, partialTicks);
			}
		}
		
	}
	
	@Override
	public boolean isPauseScreen() {
		return true;
	}
	
	private void selectTab(TabButton tabButton) {
		this.subscreen = null;
		this.subscreenButtons.clear();
		activateButtons(tabButton.getButtons());
	}
	
	public void selectScreen(InfoButton button) {
		// Since we allow scrolling, disallow clicks that are above the button location
		this.subscreen = ((InfoButton) button).getScreen(attribute);
		this.subscreenButtons.clear();
		
		{
			// Figure out draw offset depending on how many buttons there are
			final int maxHorizontal = this.width / (InfoButton.BUTTON_WIDTH + 2);
			int xOffset = 0;
			int yOffset = POS_BUTTONS_HEIGHT + POS_BUTTONS_HEIGHT;
			if (infoButtons.size() > maxHorizontal * 2) {
				xOffset = (2 + InfoButton.BUTTON_WIDTH) * 5;
				yOffset = 10;
			}
			
			yOffset += POS_SUBSCREEN_VOFFSET;
						
			Collection<AbstractWidget> screenbutts = subscreen.getWidgets(xOffset, yOffset, width - xOffset, height - yOffset);
			if (screenbutts != null && !screenbutts.isEmpty())
				this.subscreenButtons.addAll(screenbutts);
		}
		
		if (!this.subscreenButtons.isEmpty() && this.subscreenButtons.stream().allMatch(b -> b.x == 0 && b.y == 0)) {
			int i = 0;
			for (AbstractWidget butt : subscreenButtons) {
				butt.x = i;
				i += butt.getWidth() + 2;
				butt.y = this.height - 15;
			}
		}
		
		this.clearWidgets();
//		this.buttons.clear();
//		this.children.clear();
		
		for (AbstractWidget w : this.tabs) { this.addRenderableWidget(w); }
		for (AbstractWidget w : this.infoButtons) { this.addRenderableWidget(w); }
		for (AbstractWidget w : this.subscreenButtons) { this.addRenderableWidget(w); }
//		this.buttons.addAll(this.tabs);
//		this.buttons.addAll(this.infoButtons);
//		this.buttons.addAll(this.subscreenButtons);
	}
	
	private void activateButtons(List<InfoButton> buttons) {
		if (this.infoButtons != null) {
			for (InfoButton button : this.infoButtons) {
				button.visible = false;
			}
		}
		this.infoButtons = buttons;
		this.clearWidgets();
//		this.buttons.clear();
//		this.children.clear();
		
		for (AbstractWidget w : this.tabs) { this.addRenderableWidget(w); }
		for (AbstractWidget w : this.infoButtons) { this.addRenderableWidget(w); }
		for (AbstractWidget w : this.subscreenButtons) { this.addRenderableWidget(w); }
//		this.buttons.addAll(this.tabs);
//		this.buttons.addAll(this.infoButtons);
//		this.buttons.addAll(this.subscreenButtons);
		int i = 0;
		int j = 0;
		
		// Note: Logic about how to wrap buttons duplicated when figuring out offset for ritual display
		final int maxHorizontal = this.width / (InfoButton.BUTTON_WIDTH + 2);
		int cutoff;
		if (buttons.size() <= maxHorizontal * 2) {
			cutoff = this.width / (InfoButton.BUTTON_WIDTH + 2); // Wrapped against top
		} else {
			cutoff = 5;
		}
		for (InfoButton button : this.infoButtons) {
			button.visible = true;
			button.x = i++ * (InfoButton.BUTTON_WIDTH + 2);
			button.y = (j * (InfoButton.BUTTON_WIDTH + 2)) + (POS_TABS_HEIGHT + 2);
			
			if (i >= cutoff) {
				i = 0;
				j++;
			}
		}
		
		scrollY = 0;
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double dx) {
		final int maxHorizontal = this.width / (InfoButton.BUTTON_WIDTH + 2);
		if (infoButtons.size() <= maxHorizontal * 2) {
			return super.mouseScrolled(mouseX, mouseY, dx);
		}
		
		this.handleMouseScroll(dx > 0 ? 1 : -1, (int) mouseX, (int) mouseY);
		return true;
	}
	
	protected void handleMouseScroll(int dx, int mouseX, int mouseY) {
		final int lastScroll = scrollY;
		scrollY -= dx * 20;
		
		// Cap scroll Y to the amount of y overflow we have from buttons
		final int maxHorizontal = this.width / (InfoButton.BUTTON_WIDTH + 2);
		if (infoButtons.size() <= maxHorizontal * 2) {
			// Wrapped against top
			scrollY = 0;
		} else {
			final int rows = 1 + (this.renderables.size() / 5);
			final float visible = ((float) (height - (POS_TABS_HEIGHT + 2)) / (float) (InfoButton.BUTTON_WIDTH + 2));
			if ((int) visible < rows) {
				float overflow = (InfoButton.BUTTON_WIDTH + 2) * ((float) rows - visible);
				scrollY = (int) Math.ceil(Math.max(0, Math.min(overflow, scrollY)));
			} else {
				scrollY = 0;
			}
		}
		
		if (lastScroll != scrollY) {
			for (InfoButton button : this.infoButtons) {
				button.y -= (scrollY - lastScroll);
			}
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	class TabButton extends AbstractButton implements IForegroundRenderable {
		
		private static final int TEXT_BUTTON_TAB_WIDTH = 32;
        private InfoScreenTab tab;
        private List<InfoButton> buttons;
        private List<Component> desc;

        public TabButton(int parPosX, int parPosY, 
        		INostrumMagic attr, InfoScreenTab tab) {
            super(parPosX, parPosY, TEXT_BUTTON_TAB_WIDTH, TEXT_BUTTON_TAB_WIDTH, TextComponent.EMPTY);
            this.tab = tab;
            this.buttons = tab.getButtons(InfoScreen.this, attr);
            
            if (this.buttons == null || this.buttons.isEmpty() || !tab.isVisible(attr))
            	this.visible = false;
            
            desc = new ArrayList<>();
            String name = tab.tab.name();
            if (name.contains("_")) {
            	name = name.substring(name.indexOf('_') + 1);
            }
            desc.add(new TextComponent(name.substring(0, 1).toUpperCase() + name.toLowerCase().substring(1)));
        }
        
        public List<InfoButton> getButtons() {
        	return buttons;
        }

        /**
         * Draws this button to the screen.
         */
        @Override
        public void render(PoseStack matrixStackIn, int parX, int parY, float partialTicks) {
            if (visible)
            {
            	final int itemLength = 18;
            	float tint = 1f;
            	if (parX >= x 
                  && parY >= y 
                  && parX < x + width 
                  && parY < y + height) {
            		tint = .75f;
            	}
                
            	RenderSystem.setShaderTexture(0, background);
                RenderSystem.enableBlend();
                RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, this.x, this.y, 0,
                		TEXT_BUTTON_TAB_VOFFSET, TEXT_BUTTON_TAB_WIDTH, TEXT_BUTTON_TAB_WIDTH, TEXT_WHOLE_WIDTH, TEXT_WHOLE_HEIGHT,
                		tint, tint, tint, 1f);
                RenderSystem.disableBlend();
                
                //RenderHelper.enableStandardItemLighting();
                int x = this.x + (TEXT_BUTTON_TAB_WIDTH - itemLength) / 2;
                int y = this.y + (TEXT_BUTTON_TAB_WIDTH - itemLength) / 2;
                RenderFuncs.RenderGUIItem(tab.getIcon(), matrixStackIn, x, y);
                
                //drawButtonForegroundLayer(parX, parY);
            }
        }
        
    	public void renderForeground(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
    		if (mouseX >= this.x && mouseY > this.y
    			&& mouseX <= this.x + this.width
    			&& mouseY <= this.y + this.height) {
    			Minecraft minecraft = Minecraft.getInstance();
    			matrixStackIn.pushPose();
    			matrixStackIn.translate(0, 0, 500);
    			InfoScreen.this.renderTooltip(matrixStackIn, desc, Optional.empty(),
    					mouseX,
    					mouseY,
    					minecraft.font);
    			matrixStackIn.popPose();
    		}
    	}

		@Override
		public void onPress() {
			InfoScreen.this.selectTab(this);
		}

		@Override
		public void updateNarration(NarrationElementOutput p_169152_) {
			this.defaultButtonNarrationText(p_169152_);
		}
    }
	
}
