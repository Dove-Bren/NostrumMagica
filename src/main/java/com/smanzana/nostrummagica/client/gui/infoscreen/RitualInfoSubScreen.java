package com.smanzana.nostrummagica.client.gui.infoscreen;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrummagica.blocks.Candle;
import com.smanzana.nostrummagica.blocks.NostrumBlocks;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.items.InfusedGemItem;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.math.vector.Vector3f;

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
		
		chalk = NostrumBlocks.chalk.getDefaultState();
		candle = NostrumBlocks.candle.getDefaultState().with(
				Candle.LIT, true);
		altar = NostrumBlocks.altar.getDefaultState();
		
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
	public void draw(INostrumMagic attr, Minecraft mc, MatrixStack matrixStackIn, int x, int y, int width, int height, int mouseX, int mouseY) {
		
		infopage = (desc != null && !desc.isEmpty());
		
		matrixStackIn.push();
		
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
		
		RenderSystem.viewport((int) (x * mc.getMainWindow().getGuiScaleFactor()), 0, (int) (width * mc.getMainWindow().getGuiScaleFactor()), (int) (height * mc.getMainWindow().getGuiScaleFactor()));
		RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, false);
        //GlStateManager.clearDepth(1.0D);
	    //System.out.println(GL11.glGetInteger(GL11.GL_DEPTH_FUNC) + "");
	    //GL11.glDepthFunc(GL11.GL_LEQUAL);
	    
	    // We've changed the viewport. Numbers are relative to whole view now
	    int adjustedWidth = mc.getMainWindow().getScaledWidth();
	    int adjustedHeight = mc.getMainWindow().getScaledHeight();
	    
	    if (infopage) {
	    	matrixStackIn.translate(-(width / 4), 0, 0);
	    }
	    
	    matrixStackIn.translate(adjustedWidth / 2, (adjustedHeight * .6), -50);
		matrixStackIn.scale(scale, scale, -scale);

		matrixStackIn.rotate(Vector3f.XP.rotationDegrees(tilt));
		matrixStackIn.rotate(Vector3f.YP.rotationDegrees(angle));
		matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(180f));
		matrixStackIn.translate(-.5, 0, -.5);
		
		switch (tier) {
		case 0: {
			drawBlock(matrixStackIn, mc, chalk, 1, 0, 1);
			drawBlock(matrixStackIn, mc, chalk, 1, 0, 0);
			drawBlock(matrixStackIn, mc, chalk, 1, 0, -1);
			drawBlock(matrixStackIn, mc, chalk, 0, 0, -1);
			drawBlock(matrixStackIn, mc, chalk, 0, 0, 1);
			drawBlock(matrixStackIn, mc, chalk, -1, 0, 1);
			drawBlock(matrixStackIn, mc, chalk, -1, 0, 0);
			drawBlock(matrixStackIn, mc, chalk, -1, 0, -1);
			drawCandle(matrixStackIn, mc, 0, 0, 0, ritual.getTypes()[0]);
		}
		break;
		case 2: {
			drawBlock(matrixStackIn, mc, chalk, 1, 0, 1);
			drawBlock(matrixStackIn, mc, chalk, 1, 0, 2);
			drawBlock(matrixStackIn, mc, chalk, 1, 0, 3);
			drawBlock(matrixStackIn, mc, chalk, 2, 0, 3);
			drawBlock(matrixStackIn, mc, chalk, 2, 0, 1);
			drawBlock(matrixStackIn, mc, chalk, 3, 0, 1);
			drawBlock(matrixStackIn, mc, chalk, 3, 0, 2);

			drawBlock(matrixStackIn, mc, chalk, -1, 0, 1);
			drawBlock(matrixStackIn, mc, chalk, -1, 0, 2);
			drawBlock(matrixStackIn, mc, chalk, -1, 0, 3);
			drawBlock(matrixStackIn, mc, chalk, -2, 0, 3);
			drawBlock(matrixStackIn, mc, chalk, -2, 0, 1);
			drawBlock(matrixStackIn, mc, chalk, -3, 0, 1);
			drawBlock(matrixStackIn, mc, chalk, -3, 0, 2);

			drawBlock(matrixStackIn, mc, chalk, 1, 0, -1);
			drawBlock(matrixStackIn, mc, chalk, 1, 0, -2);
			drawBlock(matrixStackIn, mc, chalk, 1, 0, -3);
			drawBlock(matrixStackIn, mc, chalk, 2, 0, -3);
			drawBlock(matrixStackIn, mc, chalk, 2, 0, -1);
			drawBlock(matrixStackIn, mc, chalk, 3, 0, -1);
			drawBlock(matrixStackIn, mc, chalk, 3, 0, -2);

			drawBlock(matrixStackIn, mc, chalk, -1, 0, -1);
			drawBlock(matrixStackIn, mc, chalk, -1, 0, -2);
			drawBlock(matrixStackIn, mc, chalk, -1, 0, -3);
			drawBlock(matrixStackIn, mc, chalk, -2, 0, -3);
			drawBlock(matrixStackIn, mc, chalk, -2, 0, -1);
			drawBlock(matrixStackIn, mc, chalk, -3, 0, -1);
			drawBlock(matrixStackIn, mc, chalk, -3, 0, -2);
			
			drawBlock(matrixStackIn, mc, chalk, 0, 0, -2);
			drawBlock(matrixStackIn, mc, chalk, 0, 0, 2);
			drawBlock(matrixStackIn, mc, chalk, -2, 0, 0);
			drawBlock(matrixStackIn, mc, chalk, 2, 0, 0);
			
			drawCandle(matrixStackIn, mc, -2, 0, -2, ritual.getTypes()[0]);
			drawCandle(matrixStackIn, mc, 2, 0, -2, ritual.getTypes()[1]);
			drawCandle(matrixStackIn, mc, -2, 0, 2, ritual.getTypes()[2]);
			drawCandle(matrixStackIn, mc, 2, 0, 2, ritual.getTypes()[3]);

			drawAltar(matrixStackIn, mc, 0, 0, 0, ritual.getCenterItem());
			drawAltar(matrixStackIn, mc, -4, 0, 0, ritual.getExtraItems().get(0));
			drawAltar(matrixStackIn, mc, 0, 0, 4, ritual.getExtraItems().get(1));
			drawAltar(matrixStackIn, mc, 0, 0, -4, ritual.getExtraItems().get(2));
			drawAltar(matrixStackIn, mc, 4, 0, 0, ritual.getExtraItems().get(3));
		}
		break; // No fallthrough cause cna't figure out depth
		case 1: {
			drawBlock(matrixStackIn, mc, chalk, 1, 0, 1);
			drawBlock(matrixStackIn, mc, chalk, 1, 0, 2);
			drawBlock(matrixStackIn, mc, chalk, 1, 0, 3);
			drawBlock(matrixStackIn, mc, chalk, 2, 0, 3);
			drawBlock(matrixStackIn, mc, chalk, 2, 0, 1);
			drawBlock(matrixStackIn, mc, chalk, 3, 0, 1);
			drawBlock(matrixStackIn, mc, chalk, 3, 0, 2);

			drawBlock(matrixStackIn, mc, chalk, -1, 0, 1);
			drawBlock(matrixStackIn, mc, chalk, -1, 0, 2);
			drawBlock(matrixStackIn, mc, chalk, -1, 0, 3);
			drawBlock(matrixStackIn, mc, chalk, -2, 0, 3);
			drawBlock(matrixStackIn, mc, chalk, -2, 0, 1);
			drawBlock(matrixStackIn, mc, chalk, -3, 0, 1);
			drawBlock(matrixStackIn, mc, chalk, -3, 0, 2);

			drawBlock(matrixStackIn, mc, chalk, 1, 0, -1);
			drawBlock(matrixStackIn, mc, chalk, 1, 0, -2);
			drawBlock(matrixStackIn, mc, chalk, 1, 0, -3);
			drawBlock(matrixStackIn, mc, chalk, 2, 0, -3);
			drawBlock(matrixStackIn, mc, chalk, 2, 0, -1);
			drawBlock(matrixStackIn, mc, chalk, 3, 0, -1);
			drawBlock(matrixStackIn, mc, chalk, 3, 0, -2);

			drawBlock(matrixStackIn, mc, chalk, -1, 0, -1);
			drawBlock(matrixStackIn, mc, chalk, -1, 0, -2);
			drawBlock(matrixStackIn, mc, chalk, -1, 0, -3);
			drawBlock(matrixStackIn, mc, chalk, -2, 0, -3);
			drawBlock(matrixStackIn, mc, chalk, -2, 0, -1);
			drawBlock(matrixStackIn, mc, chalk, -3, 0, -1);
			drawBlock(matrixStackIn, mc, chalk, -3, 0, -2);
			
			drawBlock(matrixStackIn, mc, chalk, 0, 0, -2);
			drawBlock(matrixStackIn, mc, chalk, 0, 0, 2);
			drawBlock(matrixStackIn, mc, chalk, -2, 0, 0);
			drawBlock(matrixStackIn, mc, chalk, 2, 0, 0);
			
			drawCandle(matrixStackIn, mc, -2, 0, -2, ritual.getTypes()[0]);
			drawCandle(matrixStackIn, mc, 2, 0, -2, ritual.getTypes()[1]);
			drawCandle(matrixStackIn, mc, -2, 0, 2, ritual.getTypes()[2]);
			drawCandle(matrixStackIn, mc, 2, 0, 2, ritual.getTypes()[3]);

			drawAltar(matrixStackIn, mc, 0, 0, 0, ritual.getCenterItem());
		}
		break;
		}
		
		matrixStackIn.push();
		matrixStackIn.translate(.5, 2.5, .5);
		matrixStackIn.rotate(Vector3f.YP.rotationDegrees(20 * angle));
		RenderSystem.enableBlend();
		RenderFuncs.ItemRenderer(InfusedGemItem.getGem(ritual.getElement(), 1), matrixStackIn);
		matrixStackIn.pop();

//	    GL11.glViewport(0, 0, mc.displayWidth, mc.displayHeight);
//	    GL11.glMatrixMode(GL11.GL_PROJECTION);
//	    GL11.glLoadIdentity();
//	    GL11.glOrtho(0.0D, scaledresolution.getScaledWidth_double(), scaledresolution.getScaledHeight_double(), 0.0D, 1000.0D, 3000.0D);
//	    GL11.glMatrixMode(GL11.GL_MODELVIEW);
//	    GL11.glLoadIdentity();
		
		RenderSystem.viewport(0, 0, mc.getMainWindow().getWidth(), mc.getMainWindow().getHeight());
		RenderSystem.matrixMode(GL11.GL_PROJECTION);
		RenderSystem.loadIdentity();
		RenderSystem.ortho(0.0D, mc.getMainWindow().getScaledWidth(), mc.getMainWindow().getScaledHeight(), 0.0D, 1000.0D, 3000.0D);
		RenderSystem.matrixMode(GL11.GL_MODELVIEW);
		RenderSystem.loadIdentity();
		
		matrixStackIn.pop();
		
		matrixStackIn.push();
		matrixStackIn.translate(0, 0, 1000);
		String title = I18n.format("ritual." + ritual.getTitleKey() + ".name", new Object[0]);
		int len = mc.fontRenderer.getStringWidth(title);
		mc.fontRenderer.drawStringWithShadow(matrixStackIn, title, x + (width / 2) + (-len / 2), y, 0xFFFFFFFF);
		matrixStackIn.pop();
		
		if (infopage) {
			RenderFuncs.drawRect(matrixStackIn, x + (int) (width * .75), y, x + width, y + height, 0xFF203050);
			
			int i = 0;
			for (String line : desc) {
				mc.fontRenderer.drawString(matrixStackIn, line, x + (int) (width * .75) + 5, i + y + 10, /*(width / 4) - 5,*/ 0xFFFFFFFF);
				i += mc.fontRenderer.FONT_HEIGHT * (mc.fontRenderer.getStringWidth(line) / ((width / 4) - 5));
			}
		}
		
	}
	
	private void drawBlock(MatrixStack matrixStackIn, Minecraft mc, BlockState state, double x, double y, double z) {
		//GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
		
		final int combinedLight = 15728880; // Sampled from GameRenderer
		final int combinedOverlay = OverlayTexture.NO_OVERLAY;
		
		matrixStackIn.push();
		RenderSystem.enableDepthTest();
		//matrixStackIn.translate(x, y, z);
		RenderSystem.enableCull();
		RenderSystem.enableRescaleNormal();
		RenderHelper.disableStandardItemLighting();
		RenderSystem.depthMask(true);
		//mc.gameRenderer.disableLightmap(); // used to be mc.entityRenderer.... TODO
		
		RenderSystem.disableLighting();
		RenderSystem.enableTexture();
		RenderSystem.enableAlphaTest();
		//mc.getTextureManager().bindTexture(new ResourceLocation(NostrumMagica.MODID, "textures/block/ceramic_generic.png"));
		mc.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		
		IRenderTypeBuffer.Impl typebuffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
		try {
			RenderFuncs.RenderBlockState(state, matrixStackIn, typebuffer, combinedLight, combinedOverlay);
		} catch (Exception e) {
			
		}
		typebuffer.finish();
		
		matrixStackIn.pop();
	}
	
	private void drawCandle(MatrixStack matrixStackIn, Minecraft mc, double x, double y, double z, ReagentType reagent) {
		drawBlock(matrixStackIn, mc, candle, x, y, z);
		if (reagent != null) {
			matrixStackIn.push();
			RenderSystem.enableBlend();
			matrixStackIn.translate(x + .5, y + 1, z + .5);
			RenderFuncs.ItemRenderer(
					ReagentItem.CreateStack(reagent, 1), matrixStackIn);
			matrixStackIn.pop();
		}
	}
	
	private void drawAltar(MatrixStack matrixStackIn, Minecraft mc, double x, double y, double z, @Nonnull Ingredient item) {
		drawBlock(matrixStackIn, mc, altar, x, y, z);
		if (!item.hasNoMatchingItems()) {
			final ItemStack[] matches = item.getMatchingStacks();
			final int ingIndex = (int) ((System.currentTimeMillis() / 1000L) % matches.length);
			
			matrixStackIn.push();
			RenderSystem.enableBlend();
			matrixStackIn.translate(x + .5, y + 1.5, z + .5);
			RenderFuncs.ItemRenderer(
					matches[ingIndex], matrixStackIn);
			matrixStackIn.pop();
		}
	}

	@Override
	public Collection<ISubScreenButton> getButtons() {
		return null;
	}

}
