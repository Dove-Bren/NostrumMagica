package com.smanzana.nostrummagica.utils;

import java.lang.reflect.Field;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.RainType;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.client.model.pipeline.VertexBufferConsumer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

@SuppressWarnings("deprecation")
@OnlyIn(Dist.CLIENT)
public final class RenderFuncs {
	
	private static final MutableBlockPos cursor = new MutableBlockPos(); // If there are ever threads at play, this will not work
	
	public static final void RenderBlockOutline(PlayerEntity player, World world, Vec3d pos, BlockState blockState, float partialTicks) {
		GlStateManager.enableBlend();
		GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.lineWidth(2.0F);
		GlStateManager.disableTexture();
		GlStateManager.depthMask(false);

		if (blockState.getMaterial() != Material.AIR) {
			double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTicks;
			double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks;
			double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks;
			
			WorldRenderer.drawVoxelShapeParts(blockState.getShape(world, new BlockPos(pos), ISelectionContext.forEntity(player)), -d0, -d1, -d2, 0.0F, 0.0F, 0.0F, 0.4F);
			
		}

		GlStateManager.depthMask(true);
		GlStateManager.enableTexture();
		GlStateManager.disableBlend();
	}
	
	public static final void RenderBlockOutline(PlayerEntity player, World world, BlockPos pos, BlockState blockState, float partialTicks) {
		RenderBlockOutline(player, world, new Vec3d(pos), blockState, partialTicks);
	}
	
	public static void RenderModelWithColor(IBakedModel model, int color) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(7, DefaultVertexFormats.ITEM);
		
		RenderModelWithColor(model, color, buffer);
		tessellator.draw();
	}
	
	private static final Vector3f Vec3fZero = new Vector3f();
	
	public static void RenderModelWithColor(IBakedModel model, int color, BufferBuilder buffer) {
		RenderModelWithColor(model, color, buffer, Vec3fZero);
	}
	
	private static final Matrix4f M4fZero = new Matrix4f();
	
	public static void RenderModelWithColor(IBakedModel model, int color, BufferBuilder buffer, Vector3f offset) {
		RenderModelWithColor(model, color, buffer, offset, M4fZero);
	}
	
	public static void RenderModelWithColor(IBakedModel model, int color, BufferBuilder buffer, Vector3f offset, Matrix4f transform) {
		GlStateManager.pushMatrix();

		Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);

		// TODO provide blockstate?
		for (Direction enumfacing : Direction.values()) {
			renderQuads(model.getQuads((BlockState) null, enumfacing, NostrumMagica.rand, EmptyModelData.INSTANCE), offset, new VertexBufferConsumer(buffer), buffer,
					transform, 1f, color);
		}
		
		renderQuads(model.getQuads((BlockState) null, null, NostrumMagica.rand, EmptyModelData.INSTANCE), offset, new VertexBufferConsumer(buffer), buffer,
				transform, 1f, color);

		GlStateManager.popMatrix();
	}

//	private static void renderQuadsColor(BufferBuilder buffer, List<BakedQuad> quads, int color) {
//
//		int i = 0;
//		for (int j = quads.size(); i < j; ++i) {
//			BakedQuad bakedquad = quads.get(i);
//
//			if (bakedquad.hasTintIndex()) {
//				if (EntityRenderer.anaglyphEnable) {
//					color = TextureUtil.anaglyphColor(color);
//				}
//
//				color = color | -16777216;
//			}
//
//			net.minecraftforge.client.model.pipeline.LightUtil.renderQuadColor(buffer, bakedquad, color);
//		}
//	}
	
	/**
	 * Taken from https://github.com/Cadiboo/Example-Mod/blob/5fe80fde8a41cd571593c02897b06b5822e9a738/src/main/java/io/github/cadiboo/examplemod/client/ClientUtil.java#L240
	 * Renders a collection of BakedQuads into the BufferBuilder given. This method allows you to render any model in game in the FastTESR, be it a block model or an item model.
	 * Alternatively a custom list of quads may be constructed at runtime to render things like text.
	 * Drawbacks: doesn't transform normals as they are not guaranteed to be present in the buffer. Not relevant for a FastTESR but may cause issues with Optifine's shaders.
	 *
	 * @param quads      the iterable of BakedQuads. This may be any iterable object.
	 * @param baseOffset the base position offset for the rendering. This position will not be transformed by the model matrix.
	 * @param pipeline   the vertex consumer object. It is a parameter for optimization reasons. It may simply be constructed as new VertexBufferConsumer(buffer) and may be reused indefinately in the scope of the render pass.
	 * @param buffer     the buffer to upload vertices to.
	 * @param transform  the model matrix that is used to transform quad vertices.
	 * @param brightness the brightness of the model. The packed lightmap coordinate system is pretty complex and a lot of parameters are not necessary here so only the dominant one is implemented.
	 * @param color      the color of the quad. This is a color multiplier in the ARGB format.
	 */
	public static void renderQuads(Iterable<BakedQuad> quads, Vector3f baseOffset, VertexBufferConsumer pipeline, BufferBuilder buffer, Matrix4f transform, float brightness, int color) {
		// Get the raw int buffer of the buffer builder object.
		IntBuffer intBuf = getIntBuffer(buffer);

		// Iterate the iterable
		for (BakedQuad quad : quads) {
			// Push the quad to the consumer so it can be uploaded onto the buffer.
			LightUtil.putBakedQuad(pipeline, quad);

			// After the quad has been uploaded the buffer contains enough info to apply the model matrix transformation.
			// Getting the vertex size for the given format.
			int vertexSize = buffer.getVertexFormat().getIntegerSize();

			// Getting the offset for the current quad.
			int quadOffset = (buffer.getVertexCount() - 4) * vertexSize;

			// Each quad is made out of 4 vertices, so looping 4 times.
			for (int k = 0; k < 4; ++k) {
				// Getting the offset for the current vertex.
				int vertexIndex = quadOffset + k * vertexSize;

				// Grabbing the position vector from the buffer.
				float vertX = Float.intBitsToFloat(intBuf.get(vertexIndex));
				float vertY = Float.intBitsToFloat(intBuf.get(vertexIndex + 1));
				float vertZ = Float.intBitsToFloat(intBuf.get(vertexIndex + 2));
				Vector4f vert = new Vector4f(vertX, vertY, vertZ, 1);

				// Transforming it by the model matrix.
				Vector4f copy = new Vector4f(vert);
				transform.transform(copy, vert);

				// Uploading the difference back to the buffer. Have to use the helper function since the provided putX methods upload the data for a quad, not a vertex and this data is vertex-dependent.
				putPositionForVertex(buffer, intBuf, vertexIndex, new Vector3f(vert.x - vertX, vert.y - vertY, vert.z - vertZ));
				//putColor4ForVertex(buffer, intBuf, vertexIndex, color);
				//buffer.putColor4(color);
				{
					for (int i = 0; i < 4; ++i) {
						int idx = buffer.getColorIndex(i+1);
						int r = color >> 16 & 255;
						int g = color >> 8 & 255;
						int b = color & 255;
						int a = color >> 24 & 255;
						{
							IntBuffer rawIntBuffer = getIntBuffer(buffer);
							if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
								rawIntBuffer.put(idx, a << 24 | b << 16 | g << 8 | r);
							} else {
								rawIntBuffer.put(idx, r << 24 | g << 16 | b << 8 | a);
							}
							//System.out.print(",");
						}
						//buffer.putColorRGBA(idx, r, g, b);
					}
				}
				//buffer.putColorRGBA(vertexIndex, red, green, blue, alpha);
			}

			// Uploading the origin position to the buffer. This is an addition operation.
			buffer.putPosition(baseOffset.x, baseOffset.y, baseOffset.z);

			// Constructing the most basic packed lightmap data with a mask of 0x00FF0000.
			//int bVal = ((byte) (brightness * 255)) << 16;

//			// Uploading the brightness to the buffer.
//			buffer.putBrightness4(bVal, bVal, bVal, bVal);

			// Uploading the color multiplier to the buffer
			//buffer.putColor4(color); Vanilla sucks and now ignores the alpha bit
			//buffer.putColor4(argb);
			
			//putColor4(buffer, intBuf,  color);
		}
	}
	
	/**
	 * A setter for the vertex-based positions for a given BufferBuilder object.
	 *
	 * @param buffer the buffer to set the positions in.
	 * @param intBuf the raw int buffer.
	 * @param offset the offset for the int buffer, in ints.
	 * @param pos    the position to add to the buffer.
	 */
	public static void putPositionForVertex(BufferBuilder buffer, IntBuffer intBuf, int offset, Vector3f pos) {
		// Getting the old position data in the buffer currently.
		float ox = Float.intBitsToFloat(intBuf.get(offset));
		float oy = Float.intBitsToFloat(intBuf.get(offset + 1));
		float oz = Float.intBitsToFloat(intBuf.get(offset + 2));

		// Converting the new data to ints.
		int x = Float.floatToIntBits(pos.x + ox);
		int y = Float.floatToIntBits(pos.y + oy);
		int z = Float.floatToIntBits(pos.z + oz);

		// Putting the data into the buffer
		intBuf.put(offset, x);
		intBuf.put(offset + 1, y);
		intBuf.put(offset + 2, z);
	}
	
	
	public static void putColor4(BufferBuilder buffer, IntBuffer intBuf, int colorARGB) {
		// got to get to RGBA
		int color = colorARGB << 8
				| ((colorARGB >> 24) & 255);
		
		if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
			// swap color around
			color = Integer.reverse(color);
		}
		for (int i = 0; i < 4; ++i) {
			int colorIdx = buffer.getColorIndex(i + 1);
			intBuf.put(colorIdx, color);
		}
	}
	
	public static void putColor4ForVertex(BufferBuilder buffer, IntBuffer intBuf, int offset, int color) {
		int colorIdx = buffer.getColorIndex(offset);
		intBuf.put(colorIdx, color);
	}
	
	private static final Field bufferBuilder_rawIntBuffer = ObfuscationReflectionHelper.findField(BufferBuilder.class, "field_178999_b");
	
	/**
	 * A getter for the rawIntBuffer field value of the BufferBuilder.
	 *
	 * @param buffer the buffer builder to get the buffer from
	 * @return the rawIntbuffer component
	 */
	@Nonnull
	public static IntBuffer getIntBuffer(BufferBuilder buffer) {
		try {
			return (IntBuffer) bufferBuilder_rawIntBuffer.get(buffer);
		} catch (IllegalAccessException exception) {
			// Some other mod messed up and reset the access flag of the field.
			CrashReport crashReport = new CrashReport("An impossible error has occurred!", exception);
			crashReport.makeCategory("Reflectively Accessing BufferBuilder#rawIntBuffer");
			throw new ReportedException(crashReport);
		}
	}
	
	public static void renderWeather(BlockPos at, float partialTicks, boolean snow) {
		//throw new RuntimeException("Not finished implementing");
		final Minecraft mc = Minecraft.getInstance();
		//enableLightmap();
		disableLightmap();
		Entity entity = mc.getRenderViewEntity();
		World world = mc.world;
//		int entPosX = MathHelper.floor_double(entity.posX);
		int entPosY = MathHelper.floor(entity.posY);
//		int entPosZ = MathHelper.floor_double(entity.posZ);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder vertexbuffer = tessellator.getBuffer();
		GlStateManager.disableCull();
		GlStateManager.normal3f(0.0F, 1.0F, 0.0F);
		GlStateManager.enableBlend();
		GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.color4f(1f, 1f, 1f, 1f);
		GlStateManager.disableTexture();
		GlStateManager.enableTexture();
		double entPosDX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)partialTicks;
		double entPosDY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks;
		double entPosDZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)partialTicks;
		int entPosDYFloor = MathHelper.floor(entPosDY);
		int radius = 5;

		if (mc.gameSettings.fancyGraphics)
		{
			radius = 10;
		}

		float f1 = (float)getRendererUpdateCount() + partialTicks;
		vertexbuffer.setTranslation(-entPosDX, -entPosDY, -entPosDZ);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

		Biome biome = world.getBiome(at);

		if (biome.getPrecipitation() != RainType.NONE)
		{
			int percipWorldY = world.getHeight(Heightmap.Type.MOTION_BLOCKING, at).getY();
			int percipMinY = entPosY - radius;
			int percipMaxY = entPosY + radius;

			if (percipMinY < percipWorldY)
			{
				percipMinY = percipWorldY;
			}

			if (percipMaxY < percipWorldY)
			{
				percipMaxY = percipWorldY;
			}

			int lightSampleY = percipWorldY;

			if (percipWorldY < entPosDYFloor)
			{
				lightSampleY = entPosDYFloor;
			}

			if (percipMinY != percipMaxY)
			{
				mc.getTextureManager().bindTexture(SNOW_TEXTURES);
				vertexbuffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
				if (!snow)
				{
					mc.getTextureManager().bindTexture(RAIN_TEXTURES);
					double d5 = -((double)(getRendererUpdateCount() + 0 * 0 * 3121 + 0 * 45238971 + 0 * 0 * 418711 + 0 * 13761 & 31) + (double)partialTicks) / 32.0D * (3.0D + 0);
					double d6 = (double)((float)at.getX() + 0.5F) - entity.posX;
					double d7 = (double)((float)at.getZ() + 0.5F) - entity.posZ;
					float f3 = MathHelper.sqrt(d6 * d6 + d7 * d7) / (float)radius;
					float f4 = ((1.0F - f3 * f3) * 0.5F + 0.5F) * .5f;
					cursor.setPos(at.getX(), lightSampleY, at.getZ());
					int j3 = world.getCombinedLight(cursor, 0);
					int k3 = j3 >> 16 & 65535;
					int l3 = j3 & 65535;
					vertexbuffer.pos((double)at.getX() - 0 + 0.5D, (double)percipMaxY, (double)at.getZ() - 0 + 0.5D).tex(0.0D, (double)percipMinY * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f4).lightmap(k3, l3).endVertex();
					vertexbuffer.pos((double)at.getX() + 0 + 0.5D, (double)percipMaxY, (double)at.getZ() + 0 + 0.5D).tex(1.0D, (double)percipMinY * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f4).lightmap(k3, l3).endVertex();
					vertexbuffer.pos((double)at.getX() + 0 + 0.5D, (double)percipMinY, (double)at.getZ() + 0 + 0.5D).tex(1.0D, (double)percipMaxY * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f4).lightmap(k3, l3).endVertex();
					vertexbuffer.pos((double)at.getX() - 0 + 0.5D, (double)percipMinY, (double)at.getZ() - 0 + 0.5D).tex(0.0D, (double)percipMaxY * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f4).lightmap(k3, l3).endVertex();
				}
				else
				{
					double d8 = (double)(-((float)(getRendererUpdateCount() & 511) + partialTicks) / 512.0F);
					double d9 = 0 + (double)f1 * 0.01D * (double)((float) .5D);
					double d10 = 0 + (double)(f1 * (float) .5D) * 0.001D;
					double d11 = (double)((float)at.getX() + 0.5F) - entity.posX;
					double d12 = (double)((float)at.getZ() + 0.5F) - entity.posZ;
					float f6 = MathHelper.sqrt(d11 * d11 + d12 * d12) / (float)radius;
					float f5 = ((1.0F - f6 * f6) * 0.3F + 0.5F) * .5f;
					cursor.setPos(at.getX(), lightSampleY, at.getZ());
					int i4 = (world.getCombinedLight(cursor, 0) * 3 + 15728880) / 4;
					int j4 = i4 >> 16 & 65535;
					int k4 = i4 & 65535;
					vertexbuffer.pos((double)at.getX() - 0 + 0.5D, (double)percipMaxY, (double)at.getZ() - 0 + 0.5D).tex(0.0D + d9, (double)percipMinY * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5).lightmap(j4, k4).endVertex();
					vertexbuffer.pos((double)at.getX() + 0 + 0.5D, (double)percipMaxY, (double)at.getZ() + 0 + 0.5D).tex(1.0D + d9, (double)percipMinY * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5).lightmap(j4, k4).endVertex();
					vertexbuffer.pos((double)at.getX() + 0 + 0.5D, (double)percipMinY, (double)at.getZ() + 0 + 0.5D).tex(1.0D + d9, (double)percipMaxY * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5).lightmap(j4, k4).endVertex();
					vertexbuffer.pos((double)at.getX() - 0 + 0.5D, (double)percipMinY, (double)at.getZ() - 0 + 0.5D).tex(0.0D + d9, (double)percipMaxY * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5).lightmap(j4, k4).endVertex();
				}
				tessellator.draw();
			}
		}
		
		// End of old double loop

		vertexbuffer.setTranslation(0.0D, 0.0D, 0.0D);
		GlStateManager.enableCull();
		GlStateManager.disableBlend();
		GlStateManager.alphaFunc(516, 0.1F);
		//disableLightmap();
	}
	
	public static void disableLightmap() {
		Minecraft mc = Minecraft.getInstance();
		mc.gameRenderer.disableLightmap();
//		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
//		GlStateManager.disableTexture();
//		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}

	public static void enableLightmap() {
		Minecraft mc = Minecraft.getInstance();
		mc.gameRenderer.enableLightmap();
//		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
//		GlStateManager.matrixMode(5890);
//		GlStateManager.loadIdentity();
//		GlStateManager.scalef(0.00390625F, 0.00390625F, 0.00390625F);
//		GlStateManager.translatef(8.0F, 8.0F, 8.0F);
//		GlStateManager.matrixMode(5888);
//		Minecraft.getInstance().getTextureManager().bindTexture(getLocationLightMap());
//		GlStateManager.glTexParameteri(3553, 10241, 9729);
//		GlStateManager.glTexParameteri(3553, 10240, 9729);
//		GlStateManager.glTexParameteri(3553, 10242, 10496);
//		GlStateManager.glTexParameteri(3553, 10243, 10496);
//		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
//		GlStateManager.enableTexture();
//		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }
	
	private static @Nullable GameRenderer cachedRenderer; // Renderer pulled and modified to expose internal maps and resources
//	private static @Nullable Field cachedLightMapField; // Pulled from renderer above
	private static @Nullable Field cachedRendererUpdateCountField; // Pulled from renderer above
	private static final ResourceLocation RAIN_TEXTURES = new ResourceLocation("textures/environment/rain.png");
	private static final ResourceLocation SNOW_TEXTURES = new ResourceLocation("textures/environment/snow.png");
	
	private static final GameRenderer getCachedRenderer() {
		Minecraft mc = Minecraft.getInstance();
		final GameRenderer cur = mc.gameRenderer;
		if (cur != cachedRenderer) {
			// Refresh cache
			NostrumMagica.logger.info("Refreshing entity renderer cache");
			cachedRenderer = cur;
			//cachedLightMapField = ObfuscationReflectionHelper.findField(EntityRenderer.class, "field_110922_T"); //"locationLightMap");
			cachedRendererUpdateCountField = ObfuscationReflectionHelper.findField(GameRenderer.class, "field_78529_t"); //"rendererUpdateCount");
			//cachedLightMapField.setAccessible(true); // done for us in reflection helper
		}
		
		return cachedRenderer;
	}
//	
//	private static final @Nullable ResourceLocation getLocationLightMap() {
//		final EntityRenderer renderer = getCachedRenderer(); // Also sets up field
//		try {
//			return (ResourceLocation) cachedLightMapField.get(renderer);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
	
	private static final int getRendererUpdateCount() {
		final GameRenderer renderer = getCachedRenderer(); // Also sets up field
		try {
			return (int) cachedRendererUpdateCountField.get(renderer);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return 0;
	}
	
//	private static @Nullable RenderGlobal cachedRenderGlobal = null;
//	private static @Nullable Field cachedRenderGlobal_outlineFrameBuffer = null;
//	private static @Nullable Field cachedRenderGlobal_outlineShader = null;
//	
//	private static final RenderGlobal getCachedRenderGlobal() {
//		if (cachedRenderGlobal == null) {
//			// Pull fields and methods from new Global
//			try {
//				cachedRenderGlobal_outlineFrameBuffer = ObfuscationReflectionHelper.findField(RenderGlobal.class, "field_175015_z"); // "entityOutlineFramebuffer"
//				cachedRenderGlobal_outlineShader = ObfuscationReflectionHelper.findField(RenderGlobal.class, "field_174991_A"); // "entityOutlineShader"
//			} catch (Exception e) {
//				NostrumMagica.logger.error("Failed to get renderer fields. Highlighting will not work!");
//				cachedRenderGlobal_outlineFrameBuffer = null;
//				cachedRenderGlobal_outlineShader = null;
//			}
//		}
//		cachedRenderGlobal = Minecraft.getInstance().renderGlobal;
//			
//		return cachedRenderGlobal;
//	}
	
	public static final void renderEntityOutline(Entity e, float partialTicks) {
		if (!e.world.isRemote) {
			return;
		}
		
		e.setGlowing(true); // Best we can do?
		
//		RenderGlobal global = getCachedRenderGlobal();
//		
//		if (global == null || cachedRenderGlobal_outlineFrameBuffer == null) {
//			e.setGlowing(true);
//			return; // Best we can do?
//		}
		
//		final Minecraft mc = Minecraft.getInstance();
//		
//		GlStateManager.depthFunc(519);
//		GlStateManager.disableFog();
//		GlStateManager.color4f(1f, 1f, 0f, 1f);
//		
//		try {
//			final RenderManager renderManager = ObfuscationReflectionHelper.getPrivateValue(RenderGlobal.class, global, "field_175010_j"); // "renderManager"
//			
//			//this.entityOutlineFramebuffer.bindFramebuffer(false);
//			((Framebuffer)(cachedRenderGlobal_outlineFrameBuffer.get(global))).bindFramebuffer(false);
//			RenderHelper.disableStandardItemLighting();
//			//this.renderManager.setRenderOutlines(true);
//			renderManager.setRenderOutlines(true);
//			
//			//this.renderManager.renderEntityStatic(list1.get(j), partialTicks, false);
//			renderManager.renderEntityStatic(e, partialTicks, false);
//			
//			//this.renderManager.setRenderOutlines(false);
//			renderManager.setRenderOutlines(false);
//			RenderHelper.enableStandardItemLighting();
//			GlStateManager.depthMask(false);
//			//this.entityOutlineShader.render(partialTicks);
//			((ShaderGroup)cachedRenderGlobal_outlineShader.get(global)).render(partialTicks);
//			
//			ObfuscationReflectionHelper.setPrivateValue(RenderGlobal.class, global, true, "field_184386_ad"); // entityOutlinesRendered
//			
//		} catch (Exception exception) {
//			System.out.print(".");
//		}
//		
//		GlStateManager.enableLighting();
//		GlStateManager.depthMask(true);
////		GlStateManager.enableFog();
//		GlStateManager.enableBlend();
//		GlStateManager.enableColorMaterial();
//		GlStateManager.depthFunc(515);
//		GlStateManager.enableDepth();
//		GlStateManager.enableAlphaTest();
//
//		mc.getFramebuffer().bindFramebuffer(false);
//		global.renderEntityOutlineFramebuffer();
//		mc.getFramebuffer().bindFramebuffer(false);
//		GlStateManager.enableBlend();
//		GlStateManager.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
	}
	
	/**
	 * Renders an item. Basically a wrapper for rendering classes.
	 * Making now because transform type is deprecated but required :P and I'd rather have one warning than a bunch.
	 * @param world
	 * @param stack
	 */
	public static void ItemRenderer(ItemStack stack) {
		Minecraft.getInstance().getItemRenderer()
			.renderItem(stack, TransformType.GROUND);
	}
	
	/**
	 * Render an item with default blending and lighting.
	 * @param world
	 * @param stack
	 */
	public static void renderItemStandard(ItemStack stack) {
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.disableLighting();
		GlStateManager.enableAlphaTest();
		GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);

		RenderHelper.enableStandardItemLighting();
		
		ItemRenderer(stack);
	}
	
	public static void drawModalRectWithCustomSizedTexture(int x, int y, float u, float v, int width, int height, float textureWidth, float textureHeight) {
		float f = 1.0F / textureWidth;
		float f1 = 1.0F / textureHeight;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferbuilder.pos((double)x, (double)(y + height), 0.0D).tex((double)(u * f), (double)((v + (float)height) * f1)).endVertex();
		bufferbuilder.pos((double)(x + width), (double)(y + height), 0.0D).tex((double)((u + (float)width) * f), (double)((v + (float)height) * f1)).endVertex();
		bufferbuilder.pos((double)(x + width), (double)y, 0.0D).tex((double)((u + (float)width) * f), (double)(v * f1)).endVertex();
		bufferbuilder.pos((double)x, (double)y, 0.0D).tex((double)(u * f), (double)(v * f1)).endVertex();
		tessellator.draw();
	}
	
	public static void drawRect(int minX, int minY, int maxX, int maxY, int colorRGBA) {
		AbstractGui.fill(minX, minY, maxX, maxY, colorRGBA);
	}

	// Called fillGradient
//	public static void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
//		float f = (float)(startColor >> 24 & 255) / 255.0F;
//		float f1 = (float)(startColor >> 16 & 255) / 255.0F;
//		float f2 = (float)(startColor >> 8 & 255) / 255.0F;
//		float f3 = (float)(startColor & 255) / 255.0F;
//		float f4 = (float)(endColor >> 24 & 255) / 255.0F;
//		float f5 = (float)(endColor >> 16 & 255) / 255.0F;
//		float f6 = (float)(endColor >> 8 & 255) / 255.0F;
//		float f7 = (float)(endColor & 255) / 255.0F;
//		GlStateManager.disableTexture2D();
//		GlStateManager.enableBlend();
//		GlStateManager.disableAlpha();
//		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//		GlStateManager.shadeModel(7425);
//		Tessellator tessellator = Tessellator.getInstance();
//		BufferBuilder bufferbuilder = tessellator.getBuffer();
//		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
//		bufferbuilder.pos((double)right, (double)top, (double)this.zLevel).color(f1, f2, f3, f).endVertex();
//		bufferbuilder.pos((double)left, (double)top, (double)this.zLevel).color(f1, f2, f3, f).endVertex();
//		bufferbuilder.pos((double)left, (double)bottom, (double)this.zLevel).color(f5, f6, f7, f4).endVertex();
//		bufferbuilder.pos((double)right, (double)bottom, (double)this.zLevel).color(f5, f6, f7, f4).endVertex();
//		tessellator.draw();
//		GlStateManager.shadeModel(7424);
//		GlStateManager.disableBlend();
//		GlStateManager.enableAlpha();
//		GlStateManager.enableTexture2D();
//    }
}
