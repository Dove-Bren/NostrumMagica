package com.smanzana.nostrummagica.client.render;

import java.util.Optional;

import com.mojang.blaze3d.vertex.DefaultColorVertexBuilder;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;

/**
 * Wrapper around a RenderTypeBuffer that ONLY renders the outline.
 * This class is nearly a duplicate of OutlineLayerBuffer except that is used
 * when the outline AND the entity should be rendered, whereas this is for only the outline.
 * @author Skyler
 *
 */
public class CustomOutlineTypeBuffer implements IRenderTypeBuffer {
	
	private final IRenderTypeBuffer.Impl bufferIn;
	private final IRenderTypeBuffer.Impl outlineBuffer;
	private int red;
	private int green;
	private int blue;
	private int alpha;
	
	public CustomOutlineTypeBuffer(IRenderTypeBuffer.Impl bufferIn, float red, float green, float blue, float alpha) {
		this.bufferIn = bufferIn;
		this.outlineBuffer = IRenderTypeBuffer.getImpl(new BufferBuilder(256));
		
		this.color(red, green, blue, alpha);
	}
	
	public void color(float red, float green, float blue, float alpha) {
		this.red = (int) (red * 255);
		this.green = (int) (green * 255);
		this.blue = (int) (blue * 255);
		this.alpha = (int) (alpha * 255);
	}

	@Override
	public IVertexBuilder getBuffer(RenderType type) {
		// Very nearly a duplicate of OutlineLayerBuffer except we don't delegate to the original non-outline buffer.
		// That means we ONLY render the outline, which is good in that we don't render the entity again.
		
		if (type.isColoredOutlineBuffer()) {
	         IVertexBuilder ivertexbuilder2 = this.outlineBuffer.getBuffer(type);
	         return new ColoredOutline(ivertexbuilder2, this.red, this.green, this.blue, this.alpha);
	      } else {
	         IVertexBuilder ivertexbuilder = this.bufferIn.getBuffer(type);
	         Optional<RenderType> optional = type.getOutline();
	         if (optional.isPresent()) {
	            IVertexBuilder ivertexbuilder1 = this.outlineBuffer.getBuffer(optional.get());
	            ColoredOutline outlinelayerbuffer$coloredoutline = new ColoredOutline(ivertexbuilder1, this.red, this.green, this.blue, this.alpha);
	            return outlinelayerbuffer$coloredoutline;
	         } else {
	            return ivertexbuilder;
	         }
	      }
	}
	
	public void finish() {
		this.outlineBuffer.finish();
	}
	
	protected static final class ColoredOutline extends DefaultColorVertexBuilder {
		private double x;
		private double y;
		private double z;
		private float u;
		private float v;
		final IVertexBuilder builder;
		public ColoredOutline(IVertexBuilder builder, int red, int green, int blue, int alpha) {
			this.builder = builder;
			this.setDefaultColor(red, green, blue, alpha);
		}

		@Override
		public IVertexBuilder pos(double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
			return this;
		}

		@Override
		public IVertexBuilder color(int red, int green, int blue, int alpha) {
			return this;
		}

		@Override
		public IVertexBuilder tex(float u, float v) {
			this.u = u;
			this.v = v;
			return this;
		}

		@Override
		public IVertexBuilder overlay(int u, int v) {
			return this;
		}

		@Override
		public IVertexBuilder lightmap(int u, int v) {
			return this;
		}

		@Override
		public IVertexBuilder normal(float x, float y, float z) {
			return this;
		}

		public void addVertex(float x, float y, float z, float red, float green, float blue, float alpha, float texU, float texV, int overlayUV, int lightmapUV, float normalX, float normalY, float normalZ) {
			builder.pos((double)x, (double)y, (double)z).color(this.defaultRed, this.defaultGreen, this.defaultBlue, this.defaultAlpha).tex(texU, texV).endVertex();
		}

		public void endVertex() {
			builder.pos(this.x, this.y, this.z).color(this.defaultRed, this.defaultGreen, this.defaultBlue, this.defaultAlpha).tex(u, v).endVertex();
		}
	}

}
