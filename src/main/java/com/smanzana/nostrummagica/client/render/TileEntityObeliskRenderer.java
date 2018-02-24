package com.smanzana.nostrummagica.client.render;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.NostrumObelisk;
import com.smanzana.nostrummagica.blocks.NostrumObelisk.NostrumObeliskEntity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class TileEntityObeliskRenderer extends TileEntitySpecialRenderer<NostrumObeliskEntity> {

	public static void init() {
		ClientRegistry.bindTileEntitySpecialRenderer(NostrumObeliskEntity.class,
				new TileEntityObeliskRenderer());
	}
	
	private static final ResourceLocation MODEL_LOC = new ResourceLocation(NostrumMagica.MODID, "models/block/orb_crystal.obj");
	private static final ResourceLocation TEXT_LOC = new ResourceLocation(NostrumMagica.MODID, "textures/entity/golem_ender.png");
	private static IBakedModel model = null;
	private static boolean attemptedLoading = false;
	
	private IBlockState blockState;
	
	public TileEntityObeliskRenderer() {
		blockState = NostrumObelisk.instance().getTileState();
	}
	
	@Override
	public void renderTileEntityAt(NostrumObeliskEntity te, double x, double y, double z, float partialTicks, int destroyStage) {

		if (!attemptedLoading && model == null) {
			IModel raw;
			attemptedLoading = true;
			try {
				raw = OBJLoader.INSTANCE.loadModel(MODEL_LOC);
				model = raw.bake(TRSRTransformation.identity(), DefaultVertexFormats.BLOCK,
						(location) -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()));
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Failed to load obelisk tile model");
			}
		}
		
		if (model == null)
			return;
		
		if (te.isMaster())
			return;
		
		long time = Minecraft.getSystemTime();
		float rotY = 5.0f * (time / 50 + partialTicks);
		float rotX = 3.0f * (time / 40 + partialTicks);
		
		//GlStateManager.pushAttrib();
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + .5, y + .5, z + .5);
		GlStateManager.rotate(rotY, 0, 1f, 0);
		GlStateManager.rotate(rotX, 1f, 0, 0);
		
		GlStateManager.disableLighting();
		//GlStateManager.enableAlpha();
		//GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		RenderHelper.disableStandardItemLighting();
		
		this.bindTexture(TEXT_LOC);
		
		Tessellator tessellator = Tessellator.getInstance();
		World world = te.getWorld();
        // Translate back to local view coordinates so that we can do the acual rendering here
        GlStateManager.translate(-te.getPos().getX(), -te.getPos().getY(), -te.getPos().getZ());

        int li = getWorld().getCombinedLight(te.getPos(), 15728640);
        int i1 = li % 65536;
        int j1 = li / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) i1, (float) j1);
        
        tessellator.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(
                world,
                model,
                blockState,
                te.getPos(),
                Tessellator.getInstance().getBuffer(),
                false);
        tessellator.draw();

        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
		//GlStateManager.popAttrib();
		
	}
	
}
