package com.smanzana.nostrummagica.client.render.tile;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.blocks.ManaArmorerBlock;
import com.smanzana.nostrummagica.blocks.tiles.ManaArmorerTileEntity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
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
		Minecraft mc = Minecraft.getInstance();
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
			
			
			GlStateManager.color4f(1f, 1f, 1f, 1f);
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			
			GlStateManager.pushMatrix();

			// Offset by .5 around rotate to adjust rotation center
			GlStateManager.translatef(x + .5, y, z + .5);
			GlStateManager.rotatef(rProg, 0, 1, 0);
			GlStateManager.translatef(-.5, 0, -.5);
			
			GlStateManager.translatef(0, vAmt, 0);
			
			mc.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
			BufferBuilder buffer = Tessellator.getInstance().getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
			for (Direction facing : Direction.values()) {
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
