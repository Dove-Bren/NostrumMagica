package com.smanzana.nostrummagica.client.render.tile;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.tiles.NostrumObeliskEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class TileEntityObeliskRenderer extends TileEntityRenderer<NostrumObeliskEntity> {

	public static void init() {
		ClientRegistry.bindTileEntitySpecialRenderer(NostrumObeliskEntity.class,
				new TileEntityObeliskRenderer());
	}
	
	private static final ResourceLocation MODEL_LOC = new ResourceLocation(NostrumMagica.MODID, "models/block/orb_crystal.obj");
	private static final ResourceLocation TEXT_LOC = new ResourceLocation(NostrumMagica.MODID, "textures/entity/golem_ender.png");
	private static IBakedModel model = null;
	private static boolean attemptedLoading = false;
	
	
	public TileEntityObeliskRenderer() {
		
	}
	
	@Override
	public void render(NostrumObeliskEntity te, double x, double y, double z, float partialTicks, int destroyStage) {

		if (!attemptedLoading && model == null) {
			//IModel raw;
			attemptedLoading = true;
//			try {
//				raw = OBJLoader.INSTANCE.loadModel(MODEL_LOC);
//				model = raw.bake(TRSRTransformation.identity(), DefaultVertexFormats.BLOCK,
//						new Function<ResourceLocation, TextureAtlasSprite>() {
//
//					@Override
//					public TextureAtlasSprite apply(ResourceLocation location) {
//						return Minecraft.getInstance().getTextureMapBlocks().getAtlasSprite(location.toString());
//					}
//				});
//			} catch (Exception e) {
//				e.printStackTrace();
//				System.out.println("Failed to load obelisk tile model");
//			}
			model = Minecraft.getInstance().getModelManager().getModel(MODEL_LOC);
		}
		
		if (model == null)
			return;
		
		if (te.isMaster())
			return;
		
		long time = System.currentTimeMillis();
		float rotY = (float) (time % 3000) / 3000f;
		float rotX = (float) (time % 5000) / 5000f;
		
		
		rotY *= 360f;
		rotX *= 360f;
		
		//GlStateManager.pushLightingAttributes();
		GlStateManager.pushMatrix();
		GlStateManager.translated(x + .5, y + .5, z + .5);
		GlStateManager.rotatef(rotY, 0, 1f, 0);
		GlStateManager.rotatef(rotX, 1f, 0, 0);
		
		RenderHelper.disableStandardItemLighting();
		
		this.bindTexture(TEXT_LOC);
		
		World world = te.getWorld();

        int li = 0xF0;
        
        Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelRenderer().renderModelBrightness(
        		model, world.getBlockState(te.getPos()), li, true);

        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
		
	}
	
}
