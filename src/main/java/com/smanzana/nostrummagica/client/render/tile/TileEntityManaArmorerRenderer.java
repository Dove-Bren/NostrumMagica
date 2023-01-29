package com.smanzana.nostrummagica.client.render.tile;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.blocks.ManaArmorerBlock;
import com.smanzana.nostrummagica.blocks.tiles.ManaArmorerTileEntity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class TileEntityManaArmorerRenderer extends TileEntitySpecialRenderer<ManaArmorerTileEntity> {

	public static void init() {
		ClientRegistry.bindTileEntitySpecialRenderer(ManaArmorerTileEntity.class,
				new TileEntityManaArmorerRenderer());
	}
	
	private IBakedModel model;
	
	public TileEntityManaArmorerRenderer() {
		
	}
	
	@Override
	public void render(ManaArmorerTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		Minecraft mc = Minecraft.getMinecraft();
		IBlockState state = ManaArmorerBlock.instance().getDefaultState();
		
		if (model == null) {
			model = mc.getBlockRendererDispatcher().getBlockModelShapes().getModelForState(state);
		}
		
		if (model != null) {
			final double ticks = te.getTicksExisted() + partialTicks;
			
			// Up/down bobble has period of 3 seconds
			final double vPeriod = 3 * 20;
			final double vMag = .025;
			
			final double vProg = (ticks % vPeriod) / vPeriod;
			final double vAmt = Math.sin(vProg * Math.PI * 2) * vMag;
			
			// Rotate around y axis starting by not rotating until mana starts going
			final float rProg = te.getRenderRotation(partialTicks) * 360f;
			
			
			GlStateManager.color(1f, 1f, 1f, 1f);
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			
			GlStateManager.pushMatrix();

			// Offset by .5 around rotate to adjust rotation center
			GlStateManager.translate(x + .5, y, z + .5);
			GlStateManager.rotate(rProg, 0, 1, 0);
			GlStateManager.translate(-.5, 0, -.5);
			
			GlStateManager.translate(0, vAmt, 0);
			
			mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			BufferBuilder buffer = Tessellator.getInstance().getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
			for (EnumFacing facing : EnumFacing.values()) {
				for (BakedQuad quad : model.getQuads(state, facing, 0L)) {
					LightUtil.renderQuadColor(buffer, quad, -1);
				}
			}
			
			for (BakedQuad quad : model.getQuads(state, null, 0L)) {
				LightUtil.renderQuadColor(buffer, quad, -1);
			}
			
			Tessellator.getInstance().draw();
			
			GlStateManager.popMatrix();
		}
	}
	
}
