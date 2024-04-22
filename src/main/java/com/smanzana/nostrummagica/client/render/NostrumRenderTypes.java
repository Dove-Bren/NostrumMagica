package com.smanzana.nostrummagica.client.render;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrummagica.client.render.entity.RenderHookShot;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Util;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class NostrumRenderTypes {

	
	public static final RenderType HOOKSHOT_CHAIN;
	public static final RenderType MANA_ARMOR;
	
	private static final String Name(String suffix) {
		return "nostrumrender_" + suffix;
	}
	
	static {
		// Set up states that we use. Pull some from RenderState itself, and make some custom ones/ones not worth the effort to pull out.
		final RenderState.TextureState BLOCKATLAS_MIPMAP = new RenderState.TextureState(AtlasTexture.LOCATION_BLOCKS_TEXTURE, false, true);
		
		final RenderState.TransparencyState LIGHTNING_TRANSPARENCY = ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "field_228512_d_");
		final RenderState.TransparencyState TRANSLUCENT_TRANSPARENCY = ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "field_228515_g_");
		final RenderState.TransparencyState NO_TRANSPARENCY = ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "field_228510_b_");

		final RenderState.LayerState VIEW_OFFSET_Z_LAYERING = ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "field_239235_M_");

		final RenderState.TargetState ITEM_ENTITY_TARGET = ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "field_241712_U_");
		
		final boolean ENABLE_DEPTH_WRITING = true;
	    final boolean ENABLE_COLOUR_COMPONENTS_WRITING = true;
	    final RenderState.WriteMaskState WRITE_TO_DEPTH_AND_COLOR
	            = new RenderState.WriteMaskState(ENABLE_DEPTH_WRITING, ENABLE_COLOUR_COMPONENTS_WRITING);
	    
	    final RenderState.CullState NO_CULL = new RenderState.CullState(false);
	    
	    final RenderState.LightmapState NO_LIGHTING = new RenderState.LightmapState(false);
	    
	    final RenderState.TexturingState MANAARMOR_GLINT = new RenderState.TexturingState("nostrum_manaarmor_glint", () -> {
	    	//setupGlintTexturing(0.16F);
			RenderSystem.matrixMode(GL11.GL_TEXTURE);
			RenderSystem.pushMatrix();
			RenderSystem.loadIdentity();
			final long ms = Util.milliTime();
			final long ticks = ms / (1000/20); // whole ticks
			final long remain = ms % (1000/20); // partial ticks in ms
			
			// old formula for xoffset was "0 + (ageInTicks + partialTicks) * .001"
			// So we wanted to shift .001 unit for every tick
			
			final float offset = (.001f * ticks) + (.000001f * remain);
			RenderSystem.translatef(offset, 0f, 0f);
			RenderSystem.matrixMode(GL11.GL_MODELVIEW);
	    }, () -> {
	    	RenderSystem.matrixMode(GL11.GL_TEXTURE);
	    	RenderSystem.popMatrix();
	    	RenderSystem.matrixMode(GL11.GL_MODELVIEW);
	    });
		
	    
	    // Define render types
		RenderType.State glState;
		
		glState = RenderType.State.getBuilder()
				.texture(new RenderState.TextureState(RenderHookShot.CHAIN_TEXTURE, false, true))
				.cull(NO_CULL)
				.lightmap(NO_LIGHTING)
				.transparency(TRANSLUCENT_TRANSPARENCY)
				.layer(VIEW_OFFSET_Z_LAYERING)
				.target(ITEM_ENTITY_TARGET)
				.writeMask(WRITE_TO_DEPTH_AND_COLOR)
			.build(false);
		HOOKSHOT_CHAIN = RenderType.makeType(Name("hookshot_chain"), DefaultVertexFormats.POSITION_TEX, GL11.GL_QUAD_STRIP, 128, glState);
		
		
		/*
//			GlStateManager.disableBlend();
//			GlStateManager.disableAlphaTest();
//			GlStateManager.enableBlend();
//			GlStateManager.enableAlphaTest();
//			GlStateManager.disableTexture();
//			GlStateManager.enableTexture();
//			GlStateManager.enableLighting();
//			GlStateManager.disableLighting();
//			GlStateManager.disableColorLogicOp();
//			GlStateManager.enableColorMaterial();
//			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
//	
//			this.renderPlayer.bindTexture(TEXTURE_ARMOR);
//			
//			GlStateManager.pushMatrix();
//			GlStateManager.scaled(1.0 + growAmt, 1.0 + growAmt, 1.0 + growAmt);
//			
//			GlStateManager.matrixMode(GL11.GL_TEXTURE);
//			GlStateManager.pushMatrix();
//			GlStateManager.loadIdentity();
//			GlStateManager.translated(0 + (ageInTicks + partialTicks) * .001, 0, 0);
//			
//			GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		 */
		
		//makeType("armor_entity_glint", DefaultVertexFormats.POSITION_TEX, 7, 256, RenderType.State.getBuilder().texture(new RenderState.TextureState(ItemRenderer.RES_ITEM_GLINT, true, false)).writeMask(COLOR_WRITE).cull(CULL_DISABLED).depthTest(DEPTH_EQUAL).transparency(GLINT_TRANSPARENCY).texturing(ENTITY_GLINT_TEXTURING).layer(field_239235_M_).build(false));
		//RenderType.State.getBuilder().texture(new RenderState.TextureState(LocationIn, false, false)).transparency(TRANSLUCENT_TRANSPARENCY).diffuseLighting(DIFFUSE_LIGHTING_ENABLED).alpha(DEFAULT_ALPHA).cull(CULL_DISABLED).lightmap(LIGHTMAP_ENABLED).overlay(OVERLAY_ENABLED).build(outlineIn);
	      //return makeType("entity_translucent", DefaultVertexFormats.ENTITY, 7, 256, true, true, rendertype$state);
		glState = RenderType.State.getBuilder()
				.texture(new RenderState.TextureState(LayerManaArmor.TEXTURE_ARMOR, true, false))
				.transparency(TRANSLUCENT_TRANSPARENCY)
				.lightmap(NO_LIGHTING)
				.layer(VIEW_OFFSET_Z_LAYERING)
				.writeMask(WRITE_TO_DEPTH_AND_COLOR)
				.texturing(MANAARMOR_GLINT)
			.build(false);
		MANA_ARMOR = RenderType.makeType(Name("manaarmor"), DefaultVertexFormats.ENTITY, GL11.GL_QUADS, 128, glState);
	}
}
