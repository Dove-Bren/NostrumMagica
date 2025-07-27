package com.smanzana.nostrummagica.client.render;

import com.mojang.blaze3d.vertex.DefaultedVertexConsumer;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

/**
 * Wrapper around a RenderTypeBuffer that ONLY renders the outline.
 * This class is nearly a duplicate of OutlineLayerBuffer except that is used
 * when the outline AND the entity should be rendered, whereas this is for only the outline.
 * @author Skyler
 *
 */
public class CustomFilledColorTypeBuffer implements MultiBufferSource {
	
	public static final CustomFilledColorTypeBuffer Instance() {
		if (INSTANCE == null) {
			INSTANCE = new CustomFilledColorTypeBuffer(Minecraft.getInstance().renderBuffers().bufferSource(), 0f, 0f, 0f, 1f);
		}
		return INSTANCE;
	}
	private static CustomFilledColorTypeBuffer INSTANCE = null;
	
	
	private final MultiBufferSource.BufferSource bufferIn;
	private int red;
	private int green;
	private int blue;
	private int alpha;
	
	public CustomFilledColorTypeBuffer(MultiBufferSource.BufferSource bufferIn, float red, float green, float blue, float alpha) {
		this.bufferIn = bufferIn;
		
		this.color(red, green, blue, alpha);
	}
	
	public CustomFilledColorTypeBuffer color(float red, float green, float blue, float alpha) {
		this.red = (int) (red * 255);
		this.green = (int) (green * 255);
		this.blue = (int) (blue * 255);
		this.alpha = (int) (alpha * 255);
		return this;
	}

	@Override
	public VertexConsumer getBuffer(RenderType type) {
		return new ColoredBuffer(bufferIn.getBuffer(type), this.red, this.green, this.blue, this.alpha);
	}
	
	public void finish() {
		this.bufferIn.endBatch();
	}
	
	protected static final class ColoredBuffer extends DefaultedVertexConsumer {
		// builder vars
		private double x;
		private double y;
		private double z;
		private float u;
		private float v;
		
		
		
		private final VertexConsumer builder;
		private int overlayUV;
		private int lightmapUV;
		private float normalX;
		private float normalY;
		private float normalZ;
		public ColoredBuffer(VertexConsumer builder, int red, int green, int blue, int alpha) {
			this.builder = builder;
			this.defaultColor(red, green, blue, alpha);
		}

		@Override
		public VertexConsumer vertex(double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
			return this;
		}

		@Override
		public VertexConsumer color(int red, int green, int blue, int alpha) {
			return this;
		}

		@Override
		public VertexConsumer uv(float u, float v) {
			this.u = u;
			this.v = v;
			return this;
		}

		@Override
		public VertexConsumer overlayCoords(int u, int v) {
			//this.uv2(p_85970_ & '\uffff', p_85970_ >> 16 & '\uffff');
			this.overlayUV = (u & 0xFFFF) | ((v << 16) & 0xFFFF0000);
			return this;
		}

		@Override
		public VertexConsumer uv2(int u, int v) {
			this.lightmapUV = (u & 0xFFFF) | ((v << 16) & 0xFFFF0000);
			return this;
		}

		@Override
		public VertexConsumer normal(float x, float y, float z) {
			normalX = x;
			normalY = y;
			normalZ = z;
			return this;
		}

		public void vertex(float x, float y, float z, float red, float green, float blue, float alpha, float texU, float texV, int overlayUV, int lightmapUV, float normalX, float normalY, float normalZ) {
			builder.vertex((double)x, (double)y, (double)z)
				.color(this.defaultR, this.defaultG, this.defaultB, this.defaultA)
				.uv(texU, texV)
				.overlayCoords(overlayUV)
				.uv2(lightmapUV)
				.normal(normalX, normalY, normalZ)
			.endVertex();
		}

		public void endVertex() {
			this.vertex((float)this.x, (float)this.y, (float)this.z, this.defaultR, this.defaultG, this.defaultB, this.defaultA, this.u, this.v,
					this.overlayUV, this.lightmapUV,
					this.normalX, this.normalY, this.normalZ
					);
		}
	}

}
