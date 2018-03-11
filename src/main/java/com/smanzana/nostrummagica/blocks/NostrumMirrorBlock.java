package com.smanzana.nostrummagica.blocks;

import java.util.Map;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.NostrumGui;
import com.smanzana.nostrummagica.client.gui.SpellIcon;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.ClientSkillUpMessage;
import com.smanzana.nostrummagica.network.messages.ClientSkillUpMessage.Type;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class NostrumMirrorBlock extends BlockHorizontal {
	
	public static final String ID = "mirror_block";
	
	private static NostrumMirrorBlock instance = null;
	public static NostrumMirrorBlock instance() {
		if (instance == null)
			instance = new NostrumMirrorBlock();
		
		return instance;
	}
	
	public NostrumMirrorBlock() {
		super(Material.ROCK, MapColor.OBSIDIAN);
		this.setUnlocalizedName(ID);
		this.setHardness(4.0f);
		this.setResistance(20.0f);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.STONE);
		this.setHarvestLevel("pickaxe", 2);
	}
	
	@Override
	public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return true;
	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
		return false;
	}
	
	@Override
	public boolean isVisuallyOpaque() {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
        return false;
    }
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing enumfacing = EnumFacing.getHorizontal(meta);
		return getDefaultState().withProperty(FACING, enumfacing);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getHorizontalIndex();
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		
		playerIn.openGui(NostrumMagica.instance,
				NostrumGui.mirrorID, worldIn,
				pos.getX(), pos.getY(), pos.getZ());
		
		
		return true;
	}
	
	@SideOnly(Side.CLIENT)
	public static class MirrorGui extends GuiScreen {
		
		private static final int TEXT_WIDTH = 300;
		private static final int TEXT_HEIGHT = 300;
		//private static final int GUI_HEIGHT = 242;
		private static final int TEXT_BOTTOM_HOFFSET = 25;
		private static final int TEXT_BOTTOM_VOFFSET = 170;
		private static final int TEXT_BOTTOM_WIDTH = 251;
		private static final int TEXT_CONTENT_HOFFSET = 16;
		private static final int TEXT_CONTENT_VOFFSET = 16;
		private static final int TEXT_CONTENT_WIDTH = 269;
		private static final int TEXT_CONTENT_HEIGHT = 159;
		private static final int TEXT_BUTTON_VOFFSET = 242;
		private static final int TEXT_BUTTON_LENGTH = 16;
		
		private static final ResourceLocation RES_BACK_CLOUD = new ResourceLocation(
				NostrumMagica.MODID, "textures/gui/container/mirror_back_clouds.png");
		private static final ResourceLocation RES_BACK_CLEAR = new ResourceLocation(
				NostrumMagica.MODID, "textures/gui/container/mirror_back_clear.png");
		private static final ResourceLocation RES_FORE = new ResourceLocation(
				NostrumMagica.MODID, "textures/gui/container/mirror_foreground.png");
		
		private static final int KEY_WIDTH = 70;
		
		private INostrumMagic attr;
		private EntityPlayer player;
		
		private ImproveButton buttonControl;
		private ImproveButton buttonTechnique;
		private ImproveButton buttonFinesse;
		
		public MirrorGui(EntityPlayer player) {
			this.width = TEXT_WIDTH;
			//this.height = GUI_HEIGHT;
			this.height = 242;
			attr = NostrumMagica.getMagicWrapper(player);
			this.player = player;
		}
		
		@Override
		public void initGui() {
			super.initGui();
			int id = 0;
			
			int GUI_HEIGHT = 242;
			int KEY_HEIGHT = 15 + 5;
			int KEY_VOFFSET = 10;
			int leftOffset = (this.width - TEXT_WIDTH) / 2; //distance from left
			int topOffset = (this.height - GUI_HEIGHT) / 2;
			
			buttonControl = new ImproveButton(id++, leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH - (KEY_WIDTH + TEXT_BUTTON_LENGTH),
					topOffset + TEXT_BOTTOM_VOFFSET + KEY_VOFFSET);
			buttonTechnique = new ImproveButton(id++, leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH - (KEY_WIDTH + TEXT_BUTTON_LENGTH),
					topOffset + TEXT_BOTTOM_VOFFSET + KEY_VOFFSET + KEY_HEIGHT);
			buttonFinesse = new ImproveButton(id++, leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH - (KEY_WIDTH + TEXT_BUTTON_LENGTH),
					topOffset + TEXT_BOTTOM_VOFFSET + KEY_VOFFSET + KEY_HEIGHT + KEY_HEIGHT);
			
			this.addButton(buttonControl);
			this.addButton(buttonTechnique);
			this.addButton(buttonFinesse);
			
			refreshButtons();
		}
		
		@Override
		public void drawScreen(int mouseX, int mouseY, float partialTicks) {
			
			int GUI_HEIGHT = 242;
			int KEY_HEIGHT = 15;
			int KEY_VOFFSET = 9;
			int leftOffset = (this.width - TEXT_WIDTH) / 2; //distance from left
			int topOffset = (this.height - GUI_HEIGHT) / 2;
			
			boolean unlocked = attr != null && attr.isUnlocked();
			if (unlocked)
				Minecraft.getMinecraft().getTextureManager().bindTexture(RES_BACK_CLEAR);
			else
				Minecraft.getMinecraft().getTextureManager().bindTexture(RES_BACK_CLOUD);
			GlStateManager.color(1f, 1f, 1f, 1f);
			Gui.drawScaledCustomSizeModalRect(leftOffset, topOffset, 0, 0, TEXT_WIDTH, GUI_HEIGHT, TEXT_WIDTH, GUI_HEIGHT, TEXT_WIDTH, GUI_HEIGHT);
			
			// CONTENT DRAWING
			if (unlocked) {
				
			} else if (attr != null) {
				int y = 0;
				String str = "Magic Not Yet Unlocked";
				int len = this.fontRendererObj.getStringWidth(str);
				this.fontRendererObj.drawString(str, (this.width - len) / 2, topOffset + (TEXT_CONTENT_HEIGHT / 2), 0xFFFFFFFF, true);
				
				y = fontRendererObj.FONT_HEIGHT + 2;
				
				str = getUnlockPrompt(attr);
				len = this.fontRendererObj.getStringWidth(str);
				this.fontRendererObj.drawString(str, (this.width - len) / 2, y + topOffset + (TEXT_CONTENT_HEIGHT / 2), 0xFFFF2000, false);
			}
			
			Minecraft.getMinecraft().getTextureManager().bindTexture(RES_FORE);
			GlStateManager.color(1f, 1f, 1f, 1f);
			Gui.drawScaledCustomSizeModalRect(leftOffset, topOffset, 0, 0, TEXT_WIDTH, GUI_HEIGHT, TEXT_WIDTH, GUI_HEIGHT, TEXT_WIDTH, TEXT_HEIGHT);
			
			// TEXT DRAWING
			if (unlocked) {
				// DRAW STATS
				int y = 2;
				int len;
				int colorKey = 0xFF0A8E0A;
				int colorVal = 0xFFE4E5D5;
				String str;
				
				str = "Level " + attr.getLevel();
				len = fontRendererObj.getStringWidth(str);
				this.fontRendererObj.drawString(str, (this.width - len) / 2, topOffset + TEXT_BOTTOM_VOFFSET, 0xFFFFFFFF, true);
				y += fontRendererObj.FONT_HEIGHT + 10;
				int yTop = y = KEY_VOFFSET + topOffset + TEXT_BOTTOM_VOFFSET;
				
				//leftOffset + TEXT_BOTTOM_HOFFSET, y + topOffset + TEXT_BOTTOM_VOFFSET, colorKey
				// XP, points
				Gui.drawRect(leftOffset + TEXT_BOTTOM_HOFFSET - 2, y, leftOffset + TEXT_BOTTOM_HOFFSET + KEY_WIDTH + 2, y + KEY_HEIGHT, 0xD0000000);
				str = "XP: ";
				len = fontRendererObj.getStringWidth(String.format("%.02f%%", 100f * attr.getXP()/attr.getMaxXP()));
				this.fontRendererObj.drawString(str, leftOffset + TEXT_BOTTOM_HOFFSET, y + (KEY_HEIGHT - fontRendererObj.FONT_HEIGHT) / 2 + 1, colorKey);
				this.fontRendererObj.drawString(String.format("%.02f%%", 100f * attr.getXP()/attr.getMaxXP()), leftOffset + TEXT_BOTTOM_HOFFSET + KEY_WIDTH - (len), y + (KEY_HEIGHT - fontRendererObj.FONT_HEIGHT) / 2 + 1, colorVal);
				y += KEY_HEIGHT + 5;
				
//				Gui.drawRect(leftOffset + TEXT_BOTTOM_HOFFSET - 2, topOffset + TEXT_BOTTOM_VOFFSET + y - 2, leftOffset + TEXT_BOTTOM_HOFFSET + keyWidth + 2, topOffset + TEXT_BOTTOM_VOFFSET + y + this.fontRendererObj.FONT_HEIGHT, 0xD0000000);
//				str = "Technique: ";
//				len = fontRendererObj.getStringWidth("" + attr.getTech());
//				this.fontRendererObj.drawString(str, leftOffset + TEXT_BOTTOM_HOFFSET, y + topOffset + TEXT_BOTTOM_VOFFSET, colorKey);
//				this.fontRendererObj.drawString("" + attr.getTech(), leftOffset + TEXT_BOTTOM_HOFFSET + keyWidth - (len), y + topOffset + TEXT_BOTTOM_VOFFSET, colorVal);
				y += KEY_HEIGHT + 5;
				
				Gui.drawRect(leftOffset + TEXT_BOTTOM_HOFFSET - 2, y, leftOffset + TEXT_BOTTOM_HOFFSET + KEY_WIDTH + 2, y + KEY_HEIGHT, 0xD0000000);
				str = "Skill Points: ";
				len = fontRendererObj.getStringWidth("" + attr.getSkillPoints());
				this.fontRendererObj.drawString(str, leftOffset + TEXT_BOTTOM_HOFFSET, y + (KEY_HEIGHT - fontRendererObj.FONT_HEIGHT) / 2 + 1, colorKey);
				this.fontRendererObj.drawString("" + attr.getSkillPoints(), leftOffset + TEXT_BOTTOM_HOFFSET + KEY_WIDTH - (len), y + (KEY_HEIGHT - fontRendererObj.FONT_HEIGHT) / 2 + 1, colorVal);
				y += KEY_HEIGHT + 5;
				
				// stats
				y = yTop;
				Gui.drawRect(leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH - (2 + KEY_WIDTH), y, leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH + 2, y + KEY_HEIGHT, 0xD0000000);
				str = "Control: ";
				len = fontRendererObj.getStringWidth("" + attr.getControl());
				this.fontRendererObj.drawString(str, leftOffset + TEXT_BOTTOM_WIDTH + TEXT_BOTTOM_HOFFSET - (KEY_WIDTH), y + (KEY_HEIGHT - fontRendererObj.FONT_HEIGHT) / 2 + 1, colorKey);
				this.fontRendererObj.drawString("" + attr.getControl(), leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH - (len), y + (KEY_HEIGHT - fontRendererObj.FONT_HEIGHT) / 2 + 1, colorVal);
				y += KEY_HEIGHT + 5;
				
				Gui.drawRect(leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH - (2 + KEY_WIDTH), y, leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH + 2, y + KEY_HEIGHT, 0xD0000000);
				str = "Technique: ";
				len = fontRendererObj.getStringWidth("" + attr.getTech());
				this.fontRendererObj.drawString(str, leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH - KEY_WIDTH, y + (KEY_HEIGHT - fontRendererObj.FONT_HEIGHT) / 2 + 1, colorKey);
				this.fontRendererObj.drawString("" + attr.getTech(), leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH - (len), y + (KEY_HEIGHT - fontRendererObj.FONT_HEIGHT) / 2 + 1, colorVal);
				y += KEY_HEIGHT + 5;
				
				Gui.drawRect(leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH - (2 + KEY_WIDTH), y, leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH + 2, y + KEY_HEIGHT, 0xD0000000);
				str = "Finess: ";
				len = fontRendererObj.getStringWidth("" + attr.getFinesse());
				this.fontRendererObj.drawString(str, leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH - KEY_WIDTH, y + (KEY_HEIGHT - fontRendererObj.FONT_HEIGHT) / 2 + 1, colorKey);
				this.fontRendererObj.drawString("" + attr.getFinesse(), leftOffset + TEXT_BOTTOM_HOFFSET + TEXT_BOTTOM_WIDTH - (len), y + (KEY_HEIGHT - fontRendererObj.FONT_HEIGHT) / 2 + 1, colorVal);
				y += KEY_HEIGHT + 5;
				
			} else if (attr != null) {
				// DRAW ICONS
				Map<EMagicElement, Boolean> map = attr.getKnownElements();
				Boolean val;
				int x = 0;
				for (EMagicElement element : EMagicElement.values()) {
					Gui.drawRect(leftOffset + TEXT_BOTTOM_HOFFSET + x - 2, topOffset + TEXT_BOTTOM_VOFFSET - 2, leftOffset + TEXT_BOTTOM_HOFFSET + x + 18, topOffset + TEXT_BOTTOM_VOFFSET + 18, 0xD0000000);
					val = map.get(element);
					if (val == null || !val)
						GlStateManager.color(.1f, .1f, .1f, .9f);
					else
						GlStateManager.color(1f, 1f, 1f, 1f);
					
					SpellIcon.get(element).draw(this, fontRendererObj, leftOffset + TEXT_BOTTOM_HOFFSET + x, topOffset + TEXT_BOTTOM_VOFFSET, 16, 16);
					x += 32;
				}
			}
			
			super.drawScreen(mouseX, mouseY, partialTicks);
		}
		
		private static String getUnlockPrompt(INostrumMagic attr) {
			if (attr.isUnlocked())
				return "";
			
			Map<EMagicElement, Boolean> map = attr.getKnownElements();
			for (EMagicElement elem : EMagicElement.values()) {
				if (map.get(elem) == null || !map.get(elem)) {
					return "Unlock all elements";
				}
			}
			
			if (attr.getShapes().isEmpty())
				return "Unlock at least one shape";
			
			return "Unlock at least one trigger";
		}
		
		@Override
		public void actionPerformed(GuiButton button) {
			if (!button.visible)
				return;
			
			if (button == this.buttonControl) {
				attr.takeSkillPoint(); // take a local point so our update makes sense
				NetworkHandler.getSyncChannel().sendToServer(
						new ClientSkillUpMessage(Type.CONTROL)
						);
			} else if (button == this.buttonFinesse) {
				attr.takeSkillPoint(); // take a local point so our update makes sense
				NetworkHandler.getSyncChannel().sendToServer(
						new ClientSkillUpMessage(Type.FINESSE)
						);
			} else if (button == this.buttonTechnique) {
				attr.takeSkillPoint(); // take a local point so our update makes sense
				NetworkHandler.getSyncChannel().sendToServer(
						new ClientSkillUpMessage(Type.TECHNIQUE)
						);
			}
			
			refreshButtons();
		}
		
		private void refreshButtons() {
			if (attr.getSkillPoints() == 0) {
				buttonControl.visible
					= buttonTechnique.visible
					= buttonFinesse.visible
					= false;
			} else {
				buttonControl.visible
				= buttonTechnique.visible
				= buttonFinesse.visible
				= true;
			}
		}
		
		@SideOnly(Side.CLIENT)
	    static class ImproveButton extends GuiButton {
			
			public ImproveButton(int parButtonId, int parPosX, int parPosY) {
				super(parButtonId, parPosX, parPosY, 12, 12, "");
			}
			
			@Override
	        public void drawButton(Minecraft mc, int parX, int parY) {
				if (visible) {
					int textureX = 0;
					int textureY = TEXT_BUTTON_VOFFSET;
                	if (parX >= xPosition 
                      && parY >= yPosition 
                      && parX < xPosition + width 
                      && parY < yPosition + height) {
                		textureX += TEXT_BUTTON_LENGTH;
                	}
	                
	                GL11.glColor4f(1f, 1f, 1f, 1f);
	                mc.getTextureManager().bindTexture(RES_FORE);
	                Gui.drawScaledCustomSizeModalRect(xPosition, yPosition, textureX, textureY,
	        				TEXT_BUTTON_LENGTH, TEXT_BUTTON_LENGTH, this.width, this.height, TEXT_WIDTH, TEXT_HEIGHT);
	            }
	        }
		}
	}
}
