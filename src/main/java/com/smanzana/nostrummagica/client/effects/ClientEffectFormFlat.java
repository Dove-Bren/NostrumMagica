package com.smanzana.nostrummagica.client.effects;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

// From made from loading and displaying a billboard texture
@OnlyIn(Dist.CLIENT)
public class ClientEffectFormFlat implements ClientEffectForm {
	
	private final ResourceLocation texture;
	private Vector3d offset;
	
	public ClientEffectFormFlat(ResourceLocation texture) {
		this(texture, 0d, 0d, 0d);
	}
	
	public ClientEffectFormFlat(ResourceLocation texture, double x, double y, double z) {
		this.texture = texture;
		if (x != 0 || y != 0 || z != 0)
			this.offset = new Vector3d(x, y, z);
		else
			this.offset = null;
	}
	
	public ClientEffectFormFlat(ClientEffectIcon icon, double x, double y, double z) {
		this(new ResourceLocation(NostrumMagica.MODID, "textures/effects/" + icon.getKey() + ".png"), x, y, z);
	}

	@Override
	public void draw(MatrixStack matrixStackIn, Minecraft mc, float partialTicks, int color) {
		final float blue = (float) (color & 0xFF) / 255f;
		final float green = (float) ((color >>> 8) & 0xFF) / 255f;
		final float red = (float) ((color >>> 16) & 0xFF) / 255f;
		final float alpha = (float) ((color >>> 24) & 0xFF) / 255f;
		
		final int light = ClientEffectForm.InferLightmap(matrixStackIn, mc);
		
		
		mc.getTextureManager().bindTexture(texture);
		matrixStackIn.push();
		
		if (this.offset != null) {
			matrixStackIn.translate(offset.x, offset.y, offset.z);
		}
		
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ENTITY);

		RenderSystem.disableCull();
		RenderSystem.alphaFunc(GL11.GL_GREATER, 0f);
		RenderSystem.enableDepthTest();
		RenderFuncs.renderSpaceQuad(matrixStackIn, buffer,
				1,
				light, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
		
		tessellator.draw();
		RenderSystem.defaultAlphaFunc();
		RenderSystem.enableCull();
		
		matrixStackIn.pop();
	}
}
