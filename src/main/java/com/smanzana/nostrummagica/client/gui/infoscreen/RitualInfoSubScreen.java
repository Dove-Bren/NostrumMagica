package com.smanzana.nostrummagica.client.gui.infoscreen;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.AltarBlock;
import com.smanzana.nostrummagica.blocks.Candle;
import com.smanzana.nostrummagica.blocks.ChalkBlock;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.items.InfusedGemItem;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.data.EmptyModelData;

@SuppressWarnings("deprecation")
public class RitualInfoSubScreen implements IInfoSubScreen {

	private RitualRecipe ritual;
	private BlockState chalk;
	private BlockState candle;
	private BlockState altar;
	
	private List<String> desc;
	
	private static boolean infopage;
	
	public RitualInfoSubScreen(RitualRecipe ritual) {
		this.ritual = ritual;
		
		chalk = ChalkBlock.instance().getDefaultState();
		candle = Candle.instance().getDefaultState().with(
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
		
		float angle = (float) (System.currentTimeMillis() % 40000L) / 40000f;
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
		
	    GL11.glViewport((int) (x * mc.mainWindow.getGuiScaleFactor()), 0, (int) (width * mc.mainWindow.getGuiScaleFactor()), (int) (height * mc.mainWindow.getGuiScaleFactor()));
	    GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        //GlStateManager.clearDepth(1.0D);
	    //System.out.println(GL11.glGetInteger(GL11.GL_DEPTH_FUNC) + "");
	    //GL11.glDepthFunc(GL11.GL_LEQUAL);
	    
	    // We've changed the viewport. Numbers are relative to whole view now
	    int adjustedWidth = mc.mainWindow.getScaledWidth();
	    int adjustedHeight = mc.mainWindow.getScaledHeight();
	    
	    if (infopage) {
	    	GlStateManager.translatef(-(width / 4), 0, 0);
	    }
	    
	    GlStateManager.translated(adjustedWidth / 2, (adjustedHeight * .6), -50);
		GlStateManager.scalef(scale, scale, -scale);

		GlStateManager.rotatef(tilt, 1, 0, 0);
		GlStateManager.rotatef(angle, 0, 1, 0);
		GlStateManager.rotatef(180f, 0, 0, 1);
		GlStateManager.translated(-.5, 0, -.5);
		
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
			drawAltar(mc, -4, 0, 0, ritual.getExtraItems().get(0));
			drawAltar(mc, 0, 0, 4, ritual.getExtraItems().get(1));
			drawAltar(mc, 0, 0, -4, ritual.getExtraItems().get(2));
			drawAltar(mc, 4, 0, 0, ritual.getExtraItems().get(3));
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
		GlStateManager.translated(.5, 2.5, .5);
		GlStateManager.rotatef(20 * angle, 0, 1, 0);
		GlStateManager.enableBlend();
		mc.getItemRenderer().renderItem(
				InfusedGemItem.instance().getGem(ritual.getElement(), 1), TransformType.GROUND);
		GlStateManager.popMatrix();

//	    GL11.glViewport(0, 0, mc.displayWidth, mc.displayHeight);
//	    GL11.glMatrixMode(GL11.GL_PROJECTION);
//	    GL11.glLoadIdentity();
//	    GL11.glOrtho(0.0D, scaledresolution.getScaledWidth_double(), scaledresolution.getScaledHeight_double(), 0.0D, 1000.0D, 3000.0D);
//	    GL11.glMatrixMode(GL11.GL_MODELVIEW);
//	    GL11.glLoadIdentity();
		
		 GL11.glViewport(0, 0, mc.mainWindow.getWidth(), mc.mainWindow.getHeight());
	    GL11.glMatrixMode(GL11.GL_PROJECTION);
	    GL11.glLoadIdentity();
	    GL11.glOrtho(0.0D, mc.mainWindow.getScaledWidth(), mc.mainWindow.getScaledHeight(), 0.0D, 1000.0D, 3000.0D);
	    GL11.glMatrixMode(GL11.GL_MODELVIEW);
	    GL11.glLoadIdentity();
		
		GlStateManager.popMatrix();
		
		GlStateManager.pushMatrix();
		GlStateManager.translatef(0, 0, 1000);
		String title = I18n.format("ritual." + ritual.getTitleKey() + ".name", new Object[0]);
		int len = mc.fontRenderer.getStringWidth(title);
		mc.fontRenderer.drawStringWithShadow(title, x + (width / 2) + (-len / 2), y, 0xFFFFFFFF);
		GlStateManager.popMatrix();
		
		if (infopage) {
			RenderFuncs.drawRect(x + (int) (width * .75), y, x + width, y + height, 0xFF203050);
			
			int i = 0;
			for (String line : desc) {
				mc.fontRenderer.drawSplitString(line, x + (int) (width * .75) + 5, i + y + 10, (width / 4) - 5, 0xFFFFFFFF);
				i += mc.fontRenderer.FONT_HEIGHT * (mc.fontRenderer.getStringWidth(line) / ((width / 4) - 5));
			}
		}
		
	}
	
	private void drawBlock(Minecraft mc, BlockState state, double x, double y, double z) {
		IBakedModel model = mc.getBlockRendererDispatcher().getBlockModelShapes().getModel(state);
		if (model == null)
			return;
		
		//GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
		
		GlStateManager.pushMatrix();
		GlStateManager.enableDepthTest();
		//GlStateManager.translatef(x, y, z);
		GlStateManager.enableCull();
		GlStateManager.enableRescaleNormal();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.depthMask(true);
		mc.gameRenderer.disableLightmap(); // used to be mc.entityRenderer.... TODO
		
		GlStateManager.disableLighting();
		GlStateManager.enableTexture();
		GlStateManager.enableAlphaTest();
		//mc.getTextureManager().bindTexture(new ResourceLocation(NostrumMagica.MODID, "textures/blocks/ceramic_generic.png"));
		mc.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		try {
			mc.getBlockRendererDispatcher().getBlockModelRenderer()
				.renderModelFlat(mc.world, model, state, new BlockPos(x, y, z), buffer, false, NostrumMagica.rand, 55, EmptyModelData.INSTANCE);
			
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
			GlStateManager.translated(x + .5, y + 1, z + .5);
			mc.getItemRenderer().renderItem(
					ReagentItem.instance().getReagent(reagent, 1), TransformType.GROUND);
			GlStateManager.popMatrix();
		}
	}
	
	private void drawAltar(Minecraft mc, double x, double y, double z, @Nonnull ItemStack item) {
		drawBlock(mc, altar, x, y, z);
		if (!item.isEmpty()) {
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.translated(x + .5, y + 1.5, z + .5);
			mc.getItemRenderer().renderItem(
					item, TransformType.GROUND);
			GlStateManager.popMatrix();
		}
	}

	@Override
	public Collection<ISubScreenButton> getButtons() {
		return null;
	}

}
