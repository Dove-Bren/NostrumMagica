package com.smanzana.nostrummagica.blocks;

import java.util.Map;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.NostrumGui;
import com.smanzana.nostrummagica.client.gui.SpellIcon;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
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
		private static final int TEXT_HEIGHT = 242;
		private static final int TEXT_BOTTOM_HOFFSET = 25;
		private static final int TEXT_BOTTOM_VOFFSET = 185;
		private static final int TEXT_CONTENT_HOFFSET = 16;
		private static final int TEXT_CONTENT_VOFFSET = 16;
		private static final int TEXT_CONTENT_WIDTH = 269;
		private static final int TEXT_CONTENT_HEIGHT = 159;
		
		private static final ResourceLocation RES_BACK_CLOUD = new ResourceLocation(
				NostrumMagica.MODID, "textures/gui/container/mirror_back_clouds.png");
		private static final ResourceLocation RES_BACK_CLEAR = new ResourceLocation(
				NostrumMagica.MODID, "textures/gui/container/mirror_back_clear.png");
		private static final ResourceLocation RES_FORE = new ResourceLocation(
				NostrumMagica.MODID, "textures/gui/container/mirror_foreground.png");
		
		private INostrumMagic attr;
		private EntityPlayer player;
		
		public MirrorGui(EntityPlayer player) {
			this.width = TEXT_WIDTH;
			this.height = TEXT_HEIGHT;
			attr = NostrumMagica.getMagicWrapper(player);
			this.player = player;
		}
		
		@Override
		public void drawScreen(int mouseX, int mouseY, float partialTicks) {
			
			int leftOffset = (this.width - TEXT_WIDTH) / 2; //distance from left
			int topOffset = (this.height - TEXT_HEIGHT) / 2;
			
			boolean unlocked = attr != null && attr.isUnlocked();
			if (unlocked)
				Minecraft.getMinecraft().getTextureManager().bindTexture(RES_BACK_CLEAR);
			else
				Minecraft.getMinecraft().getTextureManager().bindTexture(RES_BACK_CLOUD);
			GlStateManager.color(1f, 1f, 1f, 1f);
			Gui.drawScaledCustomSizeModalRect(leftOffset, topOffset, 0, 0, TEXT_WIDTH, TEXT_HEIGHT, TEXT_WIDTH, TEXT_HEIGHT, TEXT_WIDTH, TEXT_HEIGHT);
			
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
			Gui.drawScaledCustomSizeModalRect(leftOffset, topOffset, 0, 0, TEXT_WIDTH, TEXT_HEIGHT, TEXT_WIDTH, TEXT_HEIGHT, TEXT_WIDTH, TEXT_HEIGHT);
			
			// TEXT DRAWING
			if (unlocked) {
				// DRAW STATS
				int y = 2;
				int len;
				int colorKey = 0xFF0ACE00;
				int colorVal = 0xFFE4E5D5;
				String str;
				
				str = "Level " + attr.getLevel();
				len = fontRendererObj.getStringWidth(str);
				this.fontRendererObj.drawString(str, (this.width - len) / 2, topOffset + TEXT_BOTTOM_VOFFSET, 0xFFFFFFFF, true);
				y += fontRendererObj.FONT_HEIGHT + 10;
				
				//leftOffset + TEXT_BOTTOM_HOFFSET, y + topOffset + TEXT_BOTTOM_VOFFSET, colorKey
				Gui.drawRect(leftOffset + TEXT_BOTTOM_HOFFSET - 2, topOffset + TEXT_BOTTOM_VOFFSET + y - 2, leftOffset + TEXT_BOTTOM_HOFFSET + 50 + 2, topOffset + TEXT_BOTTOM_VOFFSET + y + 2 + this.fontRendererObj.FONT_HEIGHT, 0xD0000000);
				str = "Control: ";
				len = fontRendererObj.getStringWidth(str);
				this.fontRendererObj.drawString(str, leftOffset + TEXT_BOTTOM_HOFFSET, y + topOffset + TEXT_BOTTOM_VOFFSET, colorKey);
				this.fontRendererObj.drawString("" + attr.getControl(), leftOffset + len + TEXT_BOTTOM_HOFFSET, y + topOffset + TEXT_BOTTOM_VOFFSET, colorVal);
				y += fontRendererObj.FONT_HEIGHT + 10;
				
				Gui.drawRect(leftOffset + TEXT_BOTTOM_HOFFSET - 2, topOffset + TEXT_BOTTOM_VOFFSET + y - 2, leftOffset + TEXT_BOTTOM_HOFFSET + 50 + 2, topOffset + TEXT_BOTTOM_VOFFSET + y + 2 + this.fontRendererObj.FONT_HEIGHT, 0xD0000000);
				str = "Technique: ";
				len = fontRendererObj.getStringWidth(str);
				this.fontRendererObj.drawString(str, leftOffset + TEXT_BOTTOM_HOFFSET, y + topOffset + (TEXT_BOTTOM_VOFFSET / 2), colorKey);
				this.fontRendererObj.drawString("" + attr.getTech(), leftOffset + len + TEXT_BOTTOM_HOFFSET, y + topOffset + TEXT_BOTTOM_VOFFSET, colorVal);
				y += fontRendererObj.FONT_HEIGHT + 10;
				
				Gui.drawRect(leftOffset + TEXT_BOTTOM_HOFFSET - 2, topOffset + TEXT_BOTTOM_VOFFSET + y - 2, leftOffset + TEXT_BOTTOM_HOFFSET + 50 + 2, topOffset + TEXT_BOTTOM_VOFFSET + y + 2 + this.fontRendererObj.FONT_HEIGHT, 0xD0000000);
				str = "Finess: ";
				len = fontRendererObj.getStringWidth(str);
				this.fontRendererObj.drawString(str, leftOffset + TEXT_BOTTOM_HOFFSET, y + topOffset + (TEXT_BOTTOM_VOFFSET / 2), colorKey);
				this.fontRendererObj.drawString("" + attr.getFinesse(), leftOffset + len + TEXT_BOTTOM_HOFFSET, y + topOffset + TEXT_BOTTOM_VOFFSET, colorVal);
				//y += fontRendererObj.FONT_HEIGHT + 10;
				
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
	}
}
