package com.smanzana.nostrummagica.client.gui.infoscreen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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
	protected static final int POS_SUBSCREEN_VOFFSET = POS_TABS_HEIGHT + POS_BUTTONS_HEIGHT + POS_BUTTONS_HEIGHT;
	
	private INostrumMagic attribute;
	private List<GuiButton> tabs;
	private List<InfoButton> buttons;
	private IInfoSubScreen subscreen;
	private List<ISubScreenButton> subscreenButtons;
	
	protected int globButtonID = 0;
	
	public InfoScreen(INostrumMagic attribute) {
		this.attribute = attribute;
	}
	
	@Override
	public void initGui() {
		tabs = new LinkedList<>();
		buttons = new LinkedList<>();
		subscreenButtons = new LinkedList<>();
		subscreen = null;
		
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
	}
	
	@Override	
	public void updateScreen() {
		;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		Gui.drawRect(0, 0, width, height, 0xFF000000);
		
		if (this.subscreen != null) {
			this.subscreen.draw(attribute, mc, 0, POS_SUBSCREEN_VOFFSET, width, height - POS_SUBSCREEN_VOFFSET, mouseX, mouseY);
		}
		
		// Do buttons and other parent stuff
		for (int i = 0; i < this.buttonList.size(); ++i) {
			((GuiButton)this.buttonList.get(i)).drawButton(this.mc, mouseX, mouseY, partialTicks);
		}

		for (int j = 0; j < this.labelList.size(); ++j) {
			((GuiLabel)this.labelList.get(j)).drawLabel(this.mc, mouseX, mouseY);
		}
		
		for (int i = 0; i < this.buttonList.size(); ++i) {
			this.buttonList.get(i).drawButtonForegroundLayer(mouseX, mouseY);
		}
		
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return true;
	}
	
	@Override
	public void actionPerformed(GuiButton button) {
		if (!button.visible)
			return;
		
		if (button instanceof InfoButton) {
			this.subscreen = ((InfoButton) button).getScreen(attribute);
			this.subscreenButtons.clear();
			Collection<ISubScreenButton> screenbutts = subscreen.getButtons();
			if (screenbutts != null && !screenbutts.isEmpty())
				this.subscreenButtons.addAll(screenbutts);
			
			if (!this.subscreenButtons.isEmpty()) {
				int i = 0;
				for (ISubScreenButton butt : subscreenButtons) {
					butt.x = i;
					i += butt.width + 2;
					butt.y = this.height - 15;
				}
			}
			
			this.buttonList.clear();
			this.buttonList.addAll(this.tabs);
			this.buttonList.addAll(this.buttons);
			this.buttonList.addAll(this.subscreenButtons);
		} else if (button instanceof TabButton) {
			activateButtons(((TabButton) button).getButtons());
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
		int cuttoff = this.width / (InfoButton.BUTTON_WIDTH + 2);
		for (InfoButton button : this.buttons) {
			button.visible = true;
			button.x = i++ * (InfoButton.BUTTON_WIDTH + 2);
			button.y = (j * (InfoButton.BUTTON_WIDTH + 2)) + (POS_TABS_HEIGHT + 2);
			
			if (i >= cuttoff) {
				i = 0;
				j++;
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
        public void drawButton(Minecraft mc, int parX, int parY, float PartialTicks) {
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
                
                GL11.glColor4f(tint, tint, tint, 1f);
                mc.getTextureManager().bindTexture(background);
                GlStateManager.enableBlend();
                Gui.drawModalRectWithCustomSizedTexture(x, y, 0, TEXT_BUTTON_TAB_VOFFSET,
                		TEXT_BUTTON_TAB_WIDTH, TEXT_BUTTON_TAB_WIDTH, TEXT_WHOLE_WIDTH, TEXT_WHOLE_HEIGHT);
                GlStateManager.disableBlend();
                
                RenderHelper.enableGUIStandardItemLighting();
                int newx = x + (TEXT_BUTTON_TAB_WIDTH - itemLength) / 2;
                int newy = y + (TEXT_BUTTON_TAB_WIDTH - itemLength) / 2;
                mc.getRenderItem().renderItemIntoGUI(tab.getIcon(), newx, newy);
            }
        }
        
        @Override
    	public void drawButtonForegroundLayer(int mouseX, int mouseY) {
    		if (mouseX >= this.x && mouseY > this.y
    			&& mouseX <= this.x + this.width
    			&& mouseY <= this.y + this.height) {
    			Minecraft mc = Minecraft.getMinecraft();
    			GuiUtils.drawHoveringText(desc,
    					mouseX,
    					mouseY,
    					mc.displayWidth,
    					mc.displayHeight,
    					100,
    					mc.fontRenderer);
    		}
    	}
    }
	
}
