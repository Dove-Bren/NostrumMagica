package com.smanzana.nostrummagica.client.effects;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

// From made from loading and displaying a billboard texture
@OnlyIn(Dist.CLIENT)
public class ClientEffectFormFlat implements ClientEffectForm {
	
	private final ResourceLocation texture;
	private Vec3 offset;
	
	public ClientEffectFormFlat(ResourceLocation texture) {
		this(texture, 0d, 0d, 0d);
	}
	
	public ClientEffectFormFlat(ResourceLocation texture, double x, double y, double z) {
		this.texture = texture;
		if (x != 0 || y != 0 || z != 0)
			this.offset = new Vec3(x, y, z);
		else
			this.offset = null;
	}
	
	public ClientEffectFormFlat(ClientEffectIcon icon, double x, double y, double z) {
		this(new ResourceLocation(NostrumMagica.MODID, "textures/effects/" + icon.getKey() + ".png"), x, y, z);
	}

	@Override
	public void draw(PoseStack matrixStackIn, Minecraft mc, float partialTicks, int color) {
		final float blue = (float) (color & 0xFF) / 255f;
		final float green = (float) ((color >>> 8) & 0xFF) / 255f;
		final float red = (float) ((color >>> 16) & 0xFF) / 255f;
		final float alpha = (float) ((color >>> 24) & 0xFF) / 255f;
		
		final int light = ClientEffectForm.InferLightmap(matrixStackIn, mc);
		
		
		RenderSystem.setShaderTexture(0, texture);
		matrixStackIn.pushPose();
		
		if (this.offset != null) {
			matrixStackIn.translate(offset.x, offset.y, offset.z);
		}
		
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder buffer = tessellator.getBuilder();
		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);

		RenderSystem.disableCull();
		//RenderSystem.alphaFunc(GL11.GL_GREATER, 0f);
		RenderSystem.enableDepthTest();
		RenderFuncs.renderSpaceQuad(matrixStackIn, buffer,
				1,
				light, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
		
		tessellator.end();
		//RenderSystem.defaultAlphaFunc();
		RenderSystem.enableCull();
		
		matrixStackIn.popPose();
	}
}
