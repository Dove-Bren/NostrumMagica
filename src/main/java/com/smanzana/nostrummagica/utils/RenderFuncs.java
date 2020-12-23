package com.smanzana.nostrummagica.utils;

import java.lang.reflect.Field;
import java.nio.IntBuffer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.client.model.pipeline.VertexBufferConsumer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class RenderFuncs {
	
	private static final MutableBlockPos cursor = new MutableBlockPos(); // If there are ever threads at play, this will not work
	
	public static final void RenderBlockOutline(EntityPlayer player, World world, Vec3d pos, IBlockState blockState, float partialTicks) {
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.glLineWidth(2.0F);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);

		if (blockState.getMaterial() != Material.AIR) {
			double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTicks;
			double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks;
			double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks;
			RenderGlobal.drawSelectionBoundingBox(blockState.getSelectedBoundingBox(world, new BlockPos(pos)).expandXyz(0.0020000000949949026D).offset(-d0, -d1, -d2), 0.0F, 0.0F, 0.0F, 0.4F);
		}

		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}
	
	public static final void RenderBlockOutline(EntityPlayer player, World world, BlockPos pos, IBlockState blockState, float partialTicks) {
		RenderBlockOutline(player, world, new Vec3d(pos), blockState, partialTicks);
	}
	
	public static void RenderModelWithColor(IBakedModel model, int color) {
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer buffer = tessellator.getBuffer();
		buffer.begin(7, DefaultVertexFormats.ITEM);
		
		RenderModelWithColor(model, color, buffer);
		tessellator.draw();
	}
	
	private static final Vector3f Vec3fZero = new Vector3f();
	
	public static void RenderModelWithColor(IBakedModel model, int color, VertexBuffer buffer) {
		RenderModelWithColor(model, color, buffer, Vec3fZero);
	}
	
	private static final Matrix4f M4fZero = new Matrix4f();
	
	public static void RenderModelWithColor(IBakedModel model, int color, VertexBuffer buffer, Vector3f offset) {
		RenderModelWithColor(model, color, buffer, offset, M4fZero);
	}
	
	public static void RenderModelWithColor(IBakedModel model, int color, VertexBuffer buffer, Vector3f offset, Matrix4f transform) {
		GlStateManager.pushMatrix();

		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

		// TODO provide blockstate?
		for (EnumFacing enumfacing : EnumFacing.values()) {
			renderQuads(model.getQuads((IBlockState) null, enumfacing, 0L), offset, new VertexBufferConsumer(buffer), buffer,
					transform, 1f, color);
		}
		
		renderQuads(model.getQuads((IBlockState) null, null, 0L), offset, new VertexBufferConsumer(buffer), buffer,
				transform, 1f, color);

		GlStateManager.popMatrix();
	}

//	private static void renderQuadsColor(VertexBuffer buffer, List<BakedQuad> quads, int color) {
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
	public static void renderQuads(Iterable<BakedQuad> quads, Vector3f baseOffset, VertexBufferConsumer pipeline, VertexBuffer buffer, Matrix4f transform, float brightness, int color) {
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
				vert = Matrix4f.transform(transform, vert, new Vector4f());

				// Uploading the difference back to the buffer. Have to use the helper function since the provided putX methods upload the data for a quad, not a vertex and this data is vertex-dependent.
				putPositionForVertex(buffer, intBuf, vertexIndex, new Vector3f(vert.x - vertX, vert.y - vertY, vert.z - vertZ));
			}

			// Uploading the origin position to the buffer. This is an addition operation.
			buffer.putPosition(baseOffset.x, baseOffset.y, baseOffset.z);

			// Constructing the most basic packed lightmap data with a mask of 0x00FF0000.
			//int bVal = ((byte) (brightness * 255)) << 16;

//			// Uploading the brightness to the buffer.
//			buffer.putBrightness4(bVal, bVal, bVal, bVal);

			// Uploading the color multiplier to the buffer
			buffer.putColor4(color);
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
	public static void putPositionForVertex(VertexBuffer buffer, IntBuffer intBuf, int offset, Vector3f pos) {
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
	
	private static final Field bufferBuilder_rawIntBuffer = ReflectionHelper.findField(VertexBuffer.class, "rawIntBuffer", "field_178999_b", "field_178999_b");
	
	/**
	 * A getter for the rawIntBuffer field value of the BufferBuilder.
	 *
	 * @param buffer the buffer builder to get the buffer from
	 * @return the rawIntbuffer component
	 */
	@Nonnull
	public static IntBuffer getIntBuffer(VertexBuffer buffer) {
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
		final Minecraft mc = Minecraft.getMinecraft();
		//enableLightmap();
		disableLightmap();
		Entity entity = mc.getRenderViewEntity();
		World world = mc.theWorld;
//		int entPosX = MathHelper.floor_double(entity.posX);
		int entPosY = MathHelper.floor_double(entity.posY);
//		int entPosZ = MathHelper.floor_double(entity.posZ);
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer vertexbuffer = tessellator.getBuffer();
		GlStateManager.disableCull();
		GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.color(1f, 1f, 1f, 1f);
		GlStateManager.disableTexture2D();
		GlStateManager.enableTexture2D();
		double entPosDX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)partialTicks;
		double entPosDY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks;
		double entPosDZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)partialTicks;
		int entPosDYFloor = MathHelper.floor_double(entPosDY);
		int radius = 5;

		if (mc.gameSettings.fancyGraphics)
		{
			radius = 10;
		}

		float f1 = (float)getRendererUpdateCount() + partialTicks;
		vertexbuffer.setTranslation(-entPosDX, -entPosDY, -entPosDZ);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		Biome biome = world.getBiome(at);

		if (biome.canRain() || biome.getEnableSnow())
		{
			int percipWorldY = world.getPrecipitationHeight(at).getY();
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
					float f3 = MathHelper.sqrt_double(d6 * d6 + d7 * d7) / (float)radius;
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
					float f6 = MathHelper.sqrt_double(d11 * d11 + d12 * d12) / (float)radius;
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
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}

	public static void enableLightmap() {
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.matrixMode(5890);
		GlStateManager.loadIdentity();
		GlStateManager.scale(0.00390625F, 0.00390625F, 0.00390625F);
		GlStateManager.translate(8.0F, 8.0F, 8.0F);
		GlStateManager.matrixMode(5888);
		Minecraft.getMinecraft().getTextureManager().bindTexture(getLocationLightMap());
		GlStateManager.glTexParameteri(3553, 10241, 9729);
		GlStateManager.glTexParameteri(3553, 10240, 9729);
		GlStateManager.glTexParameteri(3553, 10242, 10496);
		GlStateManager.glTexParameteri(3553, 10243, 10496);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }
	
	private static @Nullable EntityRenderer cachedRenderer; // Renderer pulled and modified to expose internal maps and resources
	private static @Nullable Field cachedLightMapField; // Pulled from renderer above
	private static @Nullable Field cachedRendererUpdateCountField; // Pulled from renderer above
	private static final ResourceLocation RAIN_TEXTURES = new ResourceLocation("textures/environment/rain.png");
	private static final ResourceLocation SNOW_TEXTURES = new ResourceLocation("textures/environment/snow.png");
	
	private static final EntityRenderer getCachedRenderer() {
		final EntityRenderer cur = Minecraft.getMinecraft().entityRenderer;
		if (cur != cachedRenderer) {
			// Refresh cache
			NostrumMagica.logger.info("Refreshing entity renderer cache");
			cachedRenderer = cur;
			cachedLightMapField = ReflectionHelper.findField(EntityRenderer.class, "locationLightMap", "field_110922_T");
			cachedRendererUpdateCountField = ReflectionHelper.findField(EntityRenderer.class, "rendererUpdateCount", "field_78529_t");
			//cachedLightMapField.setAccessible(true); // done for us in reflection helper
		}
		
		return cachedRenderer;
	}
	
	private static final @Nullable ResourceLocation getLocationLightMap() {
		final EntityRenderer renderer = getCachedRenderer(); // Also sets up field
		try {
			return (ResourceLocation) cachedLightMapField.get(renderer);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static final int getRendererUpdateCount() {
		final EntityRenderer renderer = getCachedRenderer(); // Also sets up field
		try {
			return (int) cachedRendererUpdateCountField.get(renderer);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return 0;
	}
}
