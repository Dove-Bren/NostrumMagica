package com.smanzana.nostrummagica.client.render;

import java.util.Optional;

import com.mojang.blaze3d.vertex.DefaultedVertexConsumer;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

/**
 * Wrapper around a RenderTypeBuffer that ONLY renders the outline.
 * This class is nearly a duplicate of OutlineLayerBuffer except that is used
 * when the outline AND the entity should be rendered, whereas this is for only the outline.
 * @author Skyler
 *
 */
public class CustomOutlineTypeBuffer implements MultiBufferSource {
	
	private final MultiBufferSource.BufferSource bufferIn;
	private final MultiBufferSource.BufferSource outlineBuffer;
	private int red;
	private int green;
	private int blue;
	private int alpha;
	
	public CustomOutlineTypeBuffer(MultiBufferSource.BufferSource bufferIn, float red, float green, float blue, float alpha) {
		this.bufferIn = bufferIn;
		this.outlineBuffer = MultiBufferSource.immediate(new BufferBuilder(256));
		
		this.color(red, green, blue, alpha);
	}
	
	public void color(float red, float green, float blue, float alpha) {
		this.red = (int) (red * 255);
		this.green = (int) (green * 255);
		this.blue = (int) (blue * 255);
		this.alpha = (int) (alpha * 255);
	}

	@Override
	public VertexConsumer getBuffer(RenderType type) {
		// Very nearly a duplicate of OutlineLayerBuffer except we don't delegate to the original non-outline buffer.
		// That means we ONLY render the outline, which is good in that we don't render the entity again.
		
		if (type.isOutline()) {
	         VertexConsumer ivertexbuilder2 = this.outlineBuffer.getBuffer(type);
	         return new ColoredOutline(ivertexbuilder2, this.red, this.green, this.blue, this.alpha);
	      } else {
	         VertexConsumer ivertexbuilder = this.bufferIn.getBuffer(type);
	         Optional<RenderType> optional = type.outline();
	         if (optional.isPresent()) {
	            VertexConsumer ivertexbuilder1 = this.outlineBuffer.getBuffer(optional.get());
	            ColoredOutline outlinelayerbuffer$coloredoutline = new ColoredOutline(ivertexbuilder1, this.red, this.green, this.blue, this.alpha);
	            return outlinelayerbuffer$coloredoutline;
	         } else {
	            return ivertexbuilder;
	         }
	      }
	}
	
	public void finish() {
		this.outlineBuffer.endBatch();
	}
	
	protected static final class ColoredOutline extends DefaultedVertexConsumer {
		private double x;
		private double y;
		private double z;
		private float u;
		private float v;
		final VertexConsumer builder;
		public ColoredOutline(VertexConsumer builder, int red, int green, int blue, int alpha) {
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
			return this;
		}

		@Override
		public VertexConsumer uv2(int u, int v) {
			return this;
		}

		@Override
		public VertexConsumer normal(float x, float y, float z) {
			return this;
		}

		public void vertex(float x, float y, float z, float red, float green, float blue, float alpha, float texU, float texV, int overlayUV, int lightmapUV, float normalX, float normalY, float normalZ) {
			builder.vertex((double)x, (double)y, (double)z).color(this.defaultR, this.defaultG, this.defaultB, this.defaultA).uv(texU, texV).endVertex();
		}

		public void endVertex() {
			builder.vertex(this.x, this.y, this.z).color(this.defaultR, this.defaultG, this.defaultB, this.defaultA).uv(u, v).endVertex();
		}
	}

}
