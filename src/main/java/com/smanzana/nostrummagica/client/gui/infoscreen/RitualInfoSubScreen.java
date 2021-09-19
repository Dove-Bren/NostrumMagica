package com.smanzana.nostrummagica.client.gui.infoscreen;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.blocks.AltarBlock;
import com.smanzana.nostrummagica.blocks.Candle;
import com.smanzana.nostrummagica.blocks.ChalkBlock;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.items.InfusedGemItem;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.rituals.RitualRecipe;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.oredict.OreDictionary;

public class RitualInfoSubScreen implements IInfoSubScreen {

	private RitualRecipe ritual;
	private IBlockState chalk;
	private IBlockState candle;
	private IBlockState altar;
	
	private List<String> desc;
	
	private static boolean infopage;
	
	public RitualInfoSubScreen(RitualRecipe ritual) {
		this.ritual = ritual;
		
		chalk = ChalkBlock.instance().getDefaultState();
		candle = Candle.instance().getDefaultState().withProperty(
				Candle.LIT, true);
		altar = AltarBlock.instance().getDefaultState();
		
		if (I18n.hasKey("ritual." + ritual.getTitleKey() + ".desc")) {
			String lines = I18n.format("ritual." + ritual.getTitleKey() + ".desc", new Object[0]);
			List<String> desc = new LinkedList<>();
			int pos = lines.indexOf('|');
			while (pos != -1) {
				desc.add(lines.substring(0, pos));
				lines = lines.substring(pos + 1);
				pos = lines.indexOf('|');
			}
			desc.add(lines);
		}
	}
	
	@Override
	public void draw(INostrumMagic attr, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY) {
		
		infopage = (desc != null && !desc.isEmpty());
		
		GlStateManager.pushMatrix();
		
		float angle = (float) (Minecraft.getSystemTime() % 40000L) / 40000f;
		angle *= 360.0f;
		float scale = 40f;
		float tilt = 50f;
		int tier = ritual.getTier();
		if (tier == 1) {
			scale = 40;
			tilt = 30f;
		} else if (tier == 2) {
			scale = 32;
			tilt = 20;
		}
		tilt = 30;
		
		ScaledResolution scaledresolution = new ScaledResolution(mc);
	    GL11.glViewport(x * scaledresolution.getScaleFactor(), 0, width * scaledresolution.getScaleFactor(), height * scaledresolution.getScaleFactor());
	    GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        //GlStateManager.clearDepth(1.0D);
	    //System.out.println(GL11.glGetInteger(GL11.GL_DEPTH_FUNC) + "");
	    //GL11.glDepthFunc(GL11.GL_LEQUAL);
	    
	    // We've changed the viewport. Numbers are relative to whole view now
	    int adjustedWidth = scaledresolution.getScaledWidth();
	    int adjustedHeight = scaledresolution.getScaledHeight();
	    
	    if (infopage) {
	    	GlStateManager.translate(-(width / 4), 0, 0);
	    }
	    
	    GlStateManager.translate(adjustedWidth / 2, (adjustedHeight * .6), -50);
		GlStateManager.scale(scale, scale, -scale);

		GlStateManager.rotate(tilt, 1, 0, 0);
		GlStateManager.rotate(angle, 0, 1, 0);
		GlStateManager.rotate(180f, 0, 0, 1);
		GlStateManager.translate(-.5, 0, -.5);
		
		switch (tier) {
		case 0: {
			drawBlock(mc, chalk, 1, 0, 1);
			drawBlock(mc, chalk, 1, 0, 0);
			drawBlock(mc, chalk, 1, 0, -1);
			drawBlock(mc, chalk, 0, 0, -1);
			drawBlock(mc, chalk, 0, 0, 1);
			drawBlock(mc, chalk, -1, 0, 1);
			drawBlock(mc, chalk, -1, 0, 0);
			drawBlock(mc, chalk, -1, 0, -1);
			drawCandle(mc, 0, 0, 0, ritual.getTypes()[0]);
		}
		break;
		case 2: {
			drawBlock(mc, chalk, 1, 0, 1);
			drawBlock(mc, chalk, 1, 0, 2);
			drawBlock(mc, chalk, 1, 0, 3);
			drawBlock(mc, chalk, 2, 0, 3);
			drawBlock(mc, chalk, 2, 0, 1);
			drawBlock(mc, chalk, 3, 0, 1);
			drawBlock(mc, chalk, 3, 0, 2);

			drawBlock(mc, chalk, -1, 0, 1);
			drawBlock(mc, chalk, -1, 0, 2);
			drawBlock(mc, chalk, -1, 0, 3);
			drawBlock(mc, chalk, -2, 0, 3);
			drawBlock(mc, chalk, -2, 0, 1);
			drawBlock(mc, chalk, -3, 0, 1);
			drawBlock(mc, chalk, -3, 0, 2);

			drawBlock(mc, chalk, 1, 0, -1);
			drawBlock(mc, chalk, 1, 0, -2);
			drawBlock(mc, chalk, 1, 0, -3);
			drawBlock(mc, chalk, 2, 0, -3);
			drawBlock(mc, chalk, 2, 0, -1);
			drawBlock(mc, chalk, 3, 0, -1);
			drawBlock(mc, chalk, 3, 0, -2);

			drawBlock(mc, chalk, -1, 0, -1);
			drawBlock(mc, chalk, -1, 0, -2);
			drawBlock(mc, chalk, -1, 0, -3);
			drawBlock(mc, chalk, -2, 0, -3);
			drawBlock(mc, chalk, -2, 0, -1);
			drawBlock(mc, chalk, -3, 0, -1);
			drawBlock(mc, chalk, -3, 0, -2);
			
			drawBlock(mc, chalk, 0, 0, -2);
			drawBlock(mc, chalk, 0, 0, 2);
			drawBlock(mc, chalk, -2, 0, 0);
			drawBlock(mc, chalk, 2, 0, 0);
			
			drawCandle(mc, -2, 0, -2, ritual.getTypes()[0]);
			drawCandle(mc, 2, 0, -2, ritual.getTypes()[1]);
			drawCandle(mc, -2, 0, 2, ritual.getTypes()[2]);
			drawCandle(mc, 2, 0, 2, ritual.getTypes()[3]);

			drawAltar(mc, 0, 0, 0, ritual.getCenterItem());
			drawAltar(mc, -4, 0, 0, ritual.getExtraItems()[0]);
			drawAltar(mc, 0, 0, 4, ritual.getExtraItems()[1]);
			drawAltar(mc, 0, 0, -4, ritual.getExtraItems()[2]);
			drawAltar(mc, 4, 0, 0, ritual.getExtraItems()[3]);
		}
		break; // No fallthrough cause cna't figure out depth
		case 1: {
			drawBlock(mc, chalk, 1, 0, 1);
			drawBlock(mc, chalk, 1, 0, 2);
			drawBlock(mc, chalk, 1, 0, 3);
			drawBlock(mc, chalk, 2, 0, 3);
			drawBlock(mc, chalk, 2, 0, 1);
			drawBlock(mc, chalk, 3, 0, 1);
			drawBlock(mc, chalk, 3, 0, 2);

			drawBlock(mc, chalk, -1, 0, 1);
			drawBlock(mc, chalk, -1, 0, 2);
			drawBlock(mc, chalk, -1, 0, 3);
			drawBlock(mc, chalk, -2, 0, 3);
			drawBlock(mc, chalk, -2, 0, 1);
			drawBlock(mc, chalk, -3, 0, 1);
			drawBlock(mc, chalk, -3, 0, 2);

			drawBlock(mc, chalk, 1, 0, -1);
			drawBlock(mc, chalk, 1, 0, -2);
			drawBlock(mc, chalk, 1, 0, -3);
			drawBlock(mc, chalk, 2, 0, -3);
			drawBlock(mc, chalk, 2, 0, -1);
			drawBlock(mc, chalk, 3, 0, -1);
			drawBlock(mc, chalk, 3, 0, -2);

			drawBlock(mc, chalk, -1, 0, -1);
			drawBlock(mc, chalk, -1, 0, -2);
			drawBlock(mc, chalk, -1, 0, -3);
			drawBlock(mc, chalk, -2, 0, -3);
			drawBlock(mc, chalk, -2, 0, -1);
			drawBlock(mc, chalk, -3, 0, -1);
			drawBlock(mc, chalk, -3, 0, -2);
			
			drawBlock(mc, chalk, 0, 0, -2);
			drawBlock(mc, chalk, 0, 0, 2);
			drawBlock(mc, chalk, -2, 0, 0);
			drawBlock(mc, chalk, 2, 0, 0);
			
			drawCandle(mc, -2, 0, -2, ritual.getTypes()[0]);
			drawCandle(mc, 2, 0, -2, ritual.getTypes()[1]);
			drawCandle(mc, -2, 0, 2, ritual.getTypes()[2]);
			drawCandle(mc, 2, 0, 2, ritual.getTypes()[3]);

			drawAltar(mc, 0, 0, 0, ritual.getCenterItem());
		}
		break;
		}
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(.5, 2.5, .5);
		GlStateManager.rotate(20 * angle, 0, 1, 0);
		GlStateManager.enableBlend();
		mc.getRenderItem().renderItem(
				InfusedGemItem.instance().getGem(ritual.getElement(), 1), TransformType.GROUND);
		GlStateManager.popMatrix();

	    GL11.glViewport(0, 0, mc.displayWidth, mc.displayHeight);
	    GL11.glMatrixMode(GL11.GL_PROJECTION);
	    GL11.glLoadIdentity();
	    GL11.glOrtho(0.0D, scaledresolution.getScaledWidth_double(), scaledresolution.getScaledHeight_double(), 0.0D, 1000.0D, 3000.0D);
	    GL11.glMatrixMode(GL11.GL_MODELVIEW);
	    GL11.glLoadIdentity();
		
		GlStateManager.popMatrix();
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, 1000);
		String title = I18n.format("ritual." + ritual.getTitleKey() + ".name", new Object[0]);
		int len = mc.fontRenderer.getStringWidth(title);
		mc.fontRenderer.drawStringWithShadow(title, x + (width / 2) + (-len / 2), y, 0xFFFFFFFF);
		GlStateManager.popMatrix();
		
		if (infopage) {
			Gui.drawRect(x + (int) (width * .75), y, x + width, y + height, 0xFF203050);
			
			int i = 0;
			for (String line : desc) {
				mc.fontRenderer.drawSplitString(line, x + (int) (width * .75) + 5, i + y + 10, (width / 4) - 5, 0xFFFFFFFF);
				i += mc.fontRenderer.FONT_HEIGHT * (mc.fontRenderer.getStringWidth(line) / ((width / 4) - 5));
			}
		}
		
	}
	
	private void drawBlock(Minecraft mc, IBlockState state, double x, double y, double z) {
		IBakedModel model = mc.getBlockRendererDispatcher().getBlockModelShapes().getModelForState(state);
		if (model == null)
			return;
		
		//GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
		
		GlStateManager.pushMatrix();
		GlStateManager.enableDepth();
		//GlStateManager.translate(x, y, z);
		GlStateManager.enableCull();
		GlStateManager.enableRescaleNormal();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.depthMask(true);
		mc.entityRenderer.disableLightmap();
		
		GlStateManager.disableLighting();
		GlStateManager.enableTexture2D();
		GlStateManager.enableAlpha();
		//mc.getTextureManager().bindTexture(new ResourceLocation(NostrumMagica.MODID, "textures/blocks/ceramic_generic.png"));
		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		try {
			mc.getBlockRendererDispatcher().getBlockModelRenderer()
				.renderModelFlat(mc.world, model, state, new BlockPos(x, y, z), buffer, false, 55);
			
		} catch (Exception e) {
			
		}
		Tessellator.getInstance().draw();
		
		GlStateManager.popMatrix();
	}
	
	private void drawCandle(Minecraft mc, double x, double y, double z, ReagentType reagent) {
		drawBlock(mc, candle, x, y, z);
		if (reagent != null) {
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.translate(x + .5, y + 1, z + .5);
			mc.getRenderItem().renderItem(
					ReagentItem.instance().getReagent(reagent, 1), TransformType.GROUND);
			GlStateManager.popMatrix();
		}
	}
	
	private void drawAltar(Minecraft mc, double x, double y, double z, @Nonnull ItemStack item) {
		drawBlock(mc, altar, x, y, z);
		if (!item.isEmpty()) {
			if (item.getMetadata() == OreDictionary.WILDCARD_VALUE) {
				item = item.copy();
				item.setItemDamage(0);
			}
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.translate(x + .5, y + 1.5, z + .5);
			mc.getRenderItem().renderItem(
					item, TransformType.GROUND);
			GlStateManager.popMatrix();
		}
	}

	@Override
	public Collection<ISubScreenButton> getButtons() {
		return null;
	}

}
