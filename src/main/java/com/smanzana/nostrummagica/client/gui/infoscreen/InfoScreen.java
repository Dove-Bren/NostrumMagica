package com.smanzana.nostrummagica.client.gui.infoscreen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class InfoScreen extends GuiScreen {
	
	protected static final ResourceLocation background = new ResourceLocation(NostrumMagica.MODID + ":textures/gui/container/infoscreen.png");
	
	protected static final int TEXT_WHOLE_WIDTH = 64;
	protected static final int TEXT_WHOLE_HEIGHT = 64;
	protected static final int TEXT_BUTTON_TAB_VOFFSET = 24;
	
	protected static final int POS_TABS_HEIGHT = 36;
	protected static final int POS_BUTTONS_HEIGHT = 28;
	protected static final int POS_SUBSCREEN_VOFFSET = POS_TABS_HEIGHT;
	
	private INostrumMagic attribute;
	private List<TabButton> tabs;
	private List<InfoButton> buttons;
	private IInfoSubScreen subscreen;
	private List<ISubScreenButton> subscreenButtons;
	
	protected int globButtonID = 0;
	
	private int scrollY = 0;
	
	private @Nullable String startKey;
	
	public InfoScreen(INostrumMagic attribute, @Nullable String startKey) {
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
//			this.buttonList.add(butt);
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
	public void initGui() {
		tabs = new LinkedList<>();
		buttons = new LinkedList<>();
		subscreenButtons = new LinkedList<>();
		subscreen = null;
		scrollY = 0;
		
		InfoScreenTab.init();
		
		// Populate tabs
		for (InfoScreenTabs tab : InfoScreenTabs.values()) {
			InfoScreenTab inst = InfoScreenTab.get(tab);
			if (inst == null || !inst.isVisible(this.attribute))
				continue;
			TabButton butt = new TabButton(globButtonID++,
					2 + (tabs.size() * (2 + TabButton.TEXT_BUTTON_TAB_WIDTH)), 2,
					this.attribute, inst);
			if (butt.buttons == null || butt.buttons.isEmpty())
				continue;
			tabs.add(butt);
			this.buttonList.add(butt);
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
	public void onResize(Minecraft mcIn, int w, int h) {
		// Save our current spot
		
		super.onResize(mcIn, w, h);
	}
	
	@Override	
	public void updateScreen() {
		;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float p_73863_3_) {

		Gui.drawRect(0, 0, width, height, 0xFF000000);
		
		if (this.subscreen != null) {
			
			// Figure out draw offset depending on how many buttons there are
			final int maxHorizontal = this.width / (InfoButton.BUTTON_WIDTH + 2);
			int xOffset = 0;
			int yOffset = POS_BUTTONS_HEIGHT + POS_BUTTONS_HEIGHT;
			if (buttons.size() > maxHorizontal * 2) {
				xOffset = (2 + InfoButton.BUTTON_WIDTH) * 5;
				yOffset = 10;
			}
			
			yOffset += POS_SUBSCREEN_VOFFSET;
			
			this.subscreen.draw(attribute, mc, xOffset, yOffset, width - xOffset, height - yOffset, mouseX, mouseY);
		}
		
		// Do buttons and other parent stuff
		for (int i = 0; i < this.buttonList.size(); ++i) {
			((GuiButton)this.buttonList.get(i)).drawButton(this.mc, mouseX, mouseY);
		}
		
		// Mask out any partial buttons or buttons that are above button line, since we support scrolling
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, 500);
		if (scrollY > 0) {
			Gui.drawRect(0, 0, width, POS_SUBSCREEN_VOFFSET, 0xFF000000);
		}
		
		for (int i = 0; i < this.tabs.size(); ++i) {
			((GuiButton)this.tabs.get(i)).drawButton(this.mc, mouseX, mouseY);
		}
		GlStateManager.popMatrix();

		for (int j = 0; j < this.labelList.size(); ++j) {
			((GuiLabel)this.labelList.get(j)).drawLabel(this.mc, mouseX, mouseY);
		}
		
		// Only show sub buttons if mouseY is lower than button  vertical offset
		if (mouseY > POS_SUBSCREEN_VOFFSET) {
			for (int i = 0; i < this.buttonList.size(); ++i) {
				((GuiButton)this.buttonList.get(i)).drawButtonForegroundLayer(mouseX, mouseY);
				//this.buttonList.get(0).drawButtonForegroundLayer(mouseX, mouseY);
			}
		}
		
		for (int i = 0; i < this.tabs.size(); ++i) {
			((GuiButton)this.tabs.get(i)).drawButtonForegroundLayer(mouseX, mouseY);
		}
		
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return true;
	}
	
	private void selectTab(TabButton tabButton) {
		this.subscreen = null;
		this.subscreenButtons.clear();
		activateButtons(tabButton.getButtons());
	}
	
	private void selectScreen(InfoButton button) {
		// Since we allow scrolling, disallow clicks that are above the button location
		this.subscreen = ((InfoButton) button).getScreen(attribute);
		this.subscreenButtons.clear();
		Collection<ISubScreenButton> screenbutts = subscreen.getButtons();
		if (screenbutts != null && !screenbutts.isEmpty())
			this.subscreenButtons.addAll(screenbutts);
		
		if (!this.subscreenButtons.isEmpty()) {
			int i = 0;
			for (ISubScreenButton butt : subscreenButtons) {
				butt.xPosition = i;
				i += butt.width + 2;
				butt.yPosition = this.height - 15;
			}
		}
		
		this.buttonList.clear();
		this.buttonList.addAll(this.tabs);
		this.buttonList.addAll(this.buttons);
		this.buttonList.addAll(this.subscreenButtons);
	}
	
	@Override
	public void actionPerformed(GuiButton button) {
		if (!button.visible)
			return;
		
		if (button instanceof InfoButton) {
			selectScreen((InfoButton) button);
		} else if (button instanceof TabButton) {
			selectTab((TabButton) button);
		} else if (button instanceof ISubScreenButton) {
			((ISubScreenButton) button).onClick(attribute);
		}

	}
	
	private void activateButtons(List<InfoButton> buttons) {
		if (this.buttons != null) {
			for (InfoButton button : this.buttons) {
				button.visible = false;
			}
		}
		this.buttons = buttons;
		this.buttonList.clear();
		this.buttonList.addAll(this.tabs);
		this.buttonList.addAll(this.buttons);
		this.buttonList.addAll(this.subscreenButtons);
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
		for (InfoButton button : this.buttons) {
			button.visible = true;
			button.xPosition = i++ * (InfoButton.BUTTON_WIDTH + 2);
			button.yPosition = (j * (InfoButton.BUTTON_WIDTH + 2)) + (POS_TABS_HEIGHT + 2);
			
			if (i >= cutoff) {
				i = 0;
				j++;
			}
		}
		
		scrollY = 0;
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
		final int lastScroll = scrollY;
		scrollY -= dx * 20;
		
		// Cap scroll Y to the amount of y overflow we have from buttons
		final int maxHorizontal = this.width / (InfoButton.BUTTON_WIDTH + 2);
		if (buttons.size() <= maxHorizontal * 2) {
			// Wrapped against top
			scrollY = 0;
		} else {
			final int rows = 1 + (buttons.size() / 5);
			final float visible = ((float) (height - (POS_TABS_HEIGHT + 2)) / (float) (InfoButton.BUTTON_WIDTH + 2));
			if ((int) visible < rows) {
				float overflow = (InfoButton.BUTTON_WIDTH + 2) * ((float) rows - visible);
				scrollY = (int) Math.ceil(Math.max(0, Math.min(overflow, scrollY)));
			} else {
				scrollY = 0;
			}
		}
		
		if (lastScroll != scrollY) {
			for (InfoButton button : this.buttons) {
				button.yPosition -= (scrollY - lastScroll);
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	class TabButton extends GuiButton {
		
		private static final int TEXT_BUTTON_TAB_WIDTH = 32;
        private InfoScreenTab tab;
        private List<InfoButton> buttons;
        private List<String> desc;

        public TabButton(int parButtonId, int parPosX, int parPosY, 
        		INostrumMagic attr, InfoScreenTab tab) {
            super(parButtonId, parPosX, parPosY, TEXT_BUTTON_TAB_WIDTH, TEXT_BUTTON_TAB_WIDTH, "");
            this.tab = tab;
            this.buttons = tab.getButtons(globButtonID++, attr);
            
            if (this.buttons == null || this.buttons.isEmpty() || !tab.isVisible(attr))
            	this.visible = false;
            
            desc = new ArrayList<>();
            String name = tab.tab.name();
            if (name.contains("_")) {
            	name = name.substring(name.indexOf('_') + 1);
            }
            desc.add(name.substring(0, 1).toUpperCase() + name.toLowerCase().substring(1));
        }
        
        public List<InfoButton> getButtons() {
        	return buttons;
        }

        /**
         * Draws this button to the screen.
         */
        @Override
        public void drawButton(Minecraft mc, int parX, int parY) {
            if (visible)
            {
            	final int itemLength = 18;
            	float tint = 1f;
            	if (parX >= xPosition 
                  && parY >= yPosition 
                  && parX < xPosition + width 
                  && parY < yPosition + height) {
            		tint = .75f;
            	}
                
                GL11.glColor4f(tint, tint, tint, 1f);
                mc.getTextureManager().bindTexture(background);
                GlStateManager.enableBlend();
                Gui.drawModalRectWithCustomSizedTexture(xPosition, yPosition, 0, TEXT_BUTTON_TAB_VOFFSET,
                		TEXT_BUTTON_TAB_WIDTH, TEXT_BUTTON_TAB_WIDTH, TEXT_WHOLE_WIDTH, TEXT_WHOLE_HEIGHT);
                GlStateManager.disableBlend();
                
                RenderHelper.enableGUIStandardItemLighting();
                int x = xPosition + (TEXT_BUTTON_TAB_WIDTH - itemLength) / 2;
                int y = yPosition + (TEXT_BUTTON_TAB_WIDTH - itemLength) / 2;
                mc.getRenderItem().renderItemIntoGUI(tab.getIcon(), x, y);
            }
        }
        
        @Override
    	public void drawButtonForegroundLayer(int mouseX, int mouseY) {
    		if (mouseX >= this.xPosition && mouseY > this.yPosition
    			&& mouseX <= this.xPosition + this.width
    			&& mouseY <= this.yPosition + this.height) {
    			Minecraft mc = Minecraft.getMinecraft();
    			GuiUtils.drawHoveringText(desc,
    					mouseX,
    					mouseY,
    					mc.displayWidth,
    					mc.displayHeight,
    					100,
    					mc.fontRendererObj);
    		}
    	}
    }
	
}
