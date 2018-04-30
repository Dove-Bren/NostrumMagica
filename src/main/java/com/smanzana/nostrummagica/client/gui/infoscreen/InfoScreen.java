package com.smanzana.nostrummagica.client.gui.infoscreen;

import java.util.LinkedList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.entity.EntityGolemPhysical;
import com.smanzana.nostrummagica.loretag.LoreRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
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
	
	protected int globButtonID = 0;
	
	public InfoScreen(INostrumMagic attribute) {
		this.attribute = attribute;
		
		LoreRegistry.instance().register(new EntityGolemPhysical(Minecraft.getMinecraft().theWorld));
		
		
	}
	
	@Override
	public void initGui() {
		tabs = new LinkedList<>();
		buttons = new LinkedList<>();
		subscreen = null;
		
		InfoScreenTab.init();
		
		// Populate tabs
		for (InfoScreenTabs tab : InfoScreenTabs.values()) {
			InfoScreenTab inst = InfoScreenTab.get(tab);
			if (inst == null)
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
	public void drawScreen(int parWidth, int parHeight, float p_73863_3_) {

		Gui.drawRect(0, 0, width, height, 0xFF000000);
		
		if (this.subscreen != null) {
			this.subscreen.draw(attribute, mc, 0, POS_SUBSCREEN_VOFFSET, width, height - POS_SUBSCREEN_VOFFSET);
		}
		
		// Do buttons and other parent stuff
		super.drawScreen(parWidth, parHeight, p_73863_3_);
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
		} else if (button instanceof TabButton) {
			activateButtons(((TabButton) button).getButtons());
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
		int i = 0;
		int j = 0;
		int cuttoff = this.width / (InfoButton.BUTTON_WIDTH + 2);
		for (InfoButton button : this.buttons) {
			button.visible = true;
			button.xPosition = i++ * (InfoButton.BUTTON_WIDTH + 2);
			button.yPosition = (j * (InfoButton.BUTTON_WIDTH + 2)) + (POS_TABS_HEIGHT + 2);
			
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

        public TabButton(int parButtonId, int parPosX, int parPosY, 
        		INostrumMagic attr, InfoScreenTab tab) {
            super(parButtonId, parPosX, parPosY, TEXT_BUTTON_TAB_WIDTH, TEXT_BUTTON_TAB_WIDTH, "");
            this.tab = tab;
            this.buttons = tab.getButtons(globButtonID++, attr);
            
            if (this.buttons == null || this.buttons.isEmpty())
            	this.visible = false;
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
    }
	
}
