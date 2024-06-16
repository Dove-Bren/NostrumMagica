package com.smanzana.nostrummagica.client.render;

import java.util.OptionalDouble;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrummagica.client.model.ModelSwitchTrigger;
import com.smanzana.nostrummagica.client.render.entity.RenderHookShot;
import com.smanzana.nostrummagica.client.render.layer.LayerManaArmor;
import com.smanzana.nostrummagica.client.render.tile.TileEntityLockedChestRenderer;
import com.smanzana.nostrummagica.client.render.tile.TileEntityPortalRenderer;
import com.smanzana.nostrummagica.client.render.tile.TileEntityProgressionDoorRenderer;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class NostrumRenderTypes {

	
	public static final RenderType HOOKSHOT_CHAIN;
	public static final RenderType MANA_ARMOR;
	public static final RenderType SWITCH_TRIGGER_BASE;
	public static final RenderType SWITCH_TRIGGER_CAGE;
	public static final RenderType NOSTRUM_PORTAL;
	public static final RenderType PROGRESSION_DOOR_LOCK;
	public static final RenderType LOCKEDCHEST_LOCK;
	public static final RenderType LOCKEDCHEST_CHAIN;
	public static final RenderType SPELLSHAPE_QUADS;
	public static final RenderType SPELLSHAPE_ORB_CHAIN;
	public static final RenderType SPELLSHAPE_LINES;
	public static final RenderType SPELLSHAPE_LINES_THICK;
	
	private static final String Name(String suffix) {
		return "nostrumrender_" + suffix;
	}
	
	// Could make func that took a texture and returns a render type for flat, unlit icons
	
	// Set up states that we use. Pull some from RenderState itself, and make some custom ones/ones not worth the effort to pull out.
	//private static final RenderState.TextureState BLOCKATLAS_MIPMAP = new RenderState.TextureState(AtlasTexture.LOCATION_BLOCKS_TEXTURE, false, true);
	
	//private static final RenderState.TransparencyState LIGHTNING_TRANSPARENCY = ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "field_228512_d_");
	private static final RenderState.TransparencyState TRANSLUCENT_TRANSPARENCY = ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "field_228515_g_");
	//private static final RenderState.TransparencyState NO_TRANSPARENCY = ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "field_228510_b_");

	private static final RenderState.LayerState VIEW_OFFSET_Z_LAYERING = ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "field_239235_M_");

	private static final RenderState.TargetState ITEM_ENTITY_TARGET = ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "field_241712_U_");
	
	private static final RenderState.WriteMaskState WRITE_TO_DEPTH_AND_COLOR = new RenderState.WriteMaskState(true, true);
	private static final RenderState.WriteMaskState WRITE_NO_DEPTH_BUT_COLOR = new RenderState.WriteMaskState(true, false);
    
	private static final RenderState.CullState NO_CULL = new RenderState.CullState(false);
    
	//private static final RenderState.DepthTestState DEPTH_EQUAL = new RenderState.DepthTestState("==", GL11.GL_EQUAL);
    
	private static final RenderState.LightmapState NO_LIGHTING = new RenderState.LightmapState(false);
	private static final RenderState.LightmapState LIGHTMAP_ENABLED = new RenderState.LightmapState(true);
	private static final RenderState.OverlayState OVERLAY_ENABLED = new RenderState.OverlayState(true);
	
	private static final RenderState.DiffuseLightingState DIFFUSE_LIGHTING_ENABLED = new RenderState.DiffuseLightingState(true);
	
	private static final RenderState.AlphaState DEFAULT_ALPHA = new RenderState.AlphaState(0.003921569F);
	private static final RenderState.AlphaState CUTOUT_ALPHA = new RenderState.AlphaState(.5f);
	
	//private static final RenderState.FogState NO_FOG = new RenderState.FogState("no_fog", () -> {}, () -> {});
	
	private static final RenderState.LineState LINE_3 = new RenderState.LineState(OptionalDouble.of(3f));
	private static final RenderState.LineState LINE_10 = new RenderState.LineState(OptionalDouble.of(10f));
	
    private static final RenderState.TexturingState MANAARMOR_GLINT = new RenderState.TexturingState("nostrum_manaarmor_glint", () -> {
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
    
    private static final RenderState.TexturingState SPELLSHAPE_TEXTURING = new RenderState.TexturingState("spellshape_glint", () -> {
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
		

	static {
	    // Define render types
		RenderType.State glState;
		
		glState = RenderType.State.getBuilder()
				.texture(new RenderState.TextureState(RenderHookShot.CHAIN_TEXTURE, false, true))
				.cull(NO_CULL)
				.lightmap(NO_LIGHTING)
				.layer(VIEW_OFFSET_Z_LAYERING)
				.target(ITEM_ENTITY_TARGET)
				.alpha(CUTOUT_ALPHA)
			.build(false);
		HOOKSHOT_CHAIN = RenderType.makeType(Name("hookshot_chain"), DefaultVertexFormats.POSITION_TEX, GL11.GL_QUADS, 128, glState);
		
		glState = RenderType.State.getBuilder()
				.texture(new RenderState.TextureState(LayerManaArmor.TEXTURE_ARMOR, true, false))
				.transparency(TRANSLUCENT_TRANSPARENCY)
				.lightmap(NO_LIGHTING)
				.layer(VIEW_OFFSET_Z_LAYERING)
				.writeMask(WRITE_NO_DEPTH_BUT_COLOR)
				.texturing(MANAARMOR_GLINT)
				// depth test?
			.build(false);
		MANA_ARMOR = RenderType.makeType(Name("manaarmor"), DefaultVertexFormats.ENTITY, GL11.GL_QUADS, 128, glState);
		
		glState = RenderType.State.getBuilder()
				.texture(new RenderState.TextureState(SpellShapeRenderer.TEXTURE_BLOCK, true, false))
				.transparency(TRANSLUCENT_TRANSPARENCY)
				.lightmap(NO_LIGHTING)
				.layer(VIEW_OFFSET_Z_LAYERING)
				.writeMask(WRITE_NO_DEPTH_BUT_COLOR)
				.texturing(SPELLSHAPE_TEXTURING)
				// depth test?
			.build(false);
		SPELLSHAPE_QUADS = RenderType.makeType(Name("spellshape"), DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_QUADS, 128, glState);
		
		glState = RenderType.State.getBuilder()
				.texture(new RenderState.TextureState(SpellShapeRenderer.TEXTURE_FLOW, true, false))
				.transparency(TRANSLUCENT_TRANSPARENCY)
				.lightmap(NO_LIGHTING)
				.layer(VIEW_OFFSET_Z_LAYERING)
				.writeMask(WRITE_NO_DEPTH_BUT_COLOR)
				.cull(NO_CULL)
				//.texturing(SPELLSHAPE_TEXTURING)
				// depth test?
			.build(false);
		SPELLSHAPE_ORB_CHAIN = RenderType.makeType(Name("spellshape_chain"), DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_QUADS, 128, glState);
		
		glState = RenderType.State.getBuilder()
				//.texture(new RenderState.TextureState(SpellShapeRenderer.TEXTURE_FLOW, true, false))
				.transparency(TRANSLUCENT_TRANSPARENCY)
				.lightmap(NO_LIGHTING)
				.layer(VIEW_OFFSET_Z_LAYERING)
				.writeMask(WRITE_NO_DEPTH_BUT_COLOR)
				.texturing(SPELLSHAPE_TEXTURING)
				.line(LINE_3)
				// depth test?
			.build(false);
		SPELLSHAPE_LINES = RenderType.makeType(Name("spellshape_lines"), DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_LINES, 32, glState);
		
		glState = RenderType.State.getBuilder()
				//.texture(new RenderState.TextureState(SpellShapeRenderer.TEXTURE_FLOW, true, false))
				.transparency(TRANSLUCENT_TRANSPARENCY)
				.lightmap(NO_LIGHTING)
				.layer(VIEW_OFFSET_Z_LAYERING)
				.writeMask(WRITE_NO_DEPTH_BUT_COLOR)
				.texturing(SPELLSHAPE_TEXTURING)
				.line(LINE_10)
				// depth test?
			.build(false);
		SPELLSHAPE_LINES_THICK = RenderType.makeType(Name("spellshape_lines_thick"), DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_LINES, 32, glState);
		
		glState = RenderType.State.getBuilder()
				.texture(new RenderState.TextureState(ModelSwitchTrigger.TEXT, false, true))
				.transparency(TRANSLUCENT_TRANSPARENCY)
				.lightmap(NO_LIGHTING)
				.layer(VIEW_OFFSET_Z_LAYERING)
				.writeMask(WRITE_NO_DEPTH_BUT_COLOR)
			.build(false);
		SWITCH_TRIGGER_BASE = RenderType.makeType(Name("switch_trigger_base"), DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_TRIANGLES, 64, glState);
		
		glState = RenderType.State.getBuilder()
				.texture(new RenderState.TextureState(ModelSwitchTrigger.CAGE_TEXT, false, true))
				.transparency(TRANSLUCENT_TRANSPARENCY)
				.lightmap(NO_LIGHTING)
				.layer(VIEW_OFFSET_Z_LAYERING)
				//.depthTest(DEPTH_EQUAL)
				.writeMask(WRITE_NO_DEPTH_BUT_COLOR)
			.build(false);
		SWITCH_TRIGGER_CAGE = RenderType.makeType(Name("switch_trigger_cage"), DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_TRIANGLES, 64, glState);
		
		glState = RenderType.State.getBuilder()
				.texture(new RenderState.TextureState(TileEntityPortalRenderer.TEX_LOC, false, true))
				.transparency(TRANSLUCENT_TRANSPARENCY)
				.lightmap(NO_LIGHTING)
				.cull(NO_CULL)
			.build(false);
		NOSTRUM_PORTAL = RenderType.makeType(Name("nostrum_portal"), DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP, GL11.GL_TRIANGLES, 64, glState);
		
		glState = RenderType.State.getBuilder()
				.texture(new RenderState.TextureState(TileEntityProgressionDoorRenderer.TEX_GEM_LOC, false, true))
				.transparency(TRANSLUCENT_TRANSPARENCY)
				.lightmap(LIGHTMAP_ENABLED)
				.layer(VIEW_OFFSET_Z_LAYERING)
				.writeMask(WRITE_TO_DEPTH_AND_COLOR)
			.build(false);
		PROGRESSION_DOOR_LOCK = RenderType.makeType(Name("prog_door_lock"), DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP, GL11.GL_TRIANGLES, 64, glState);
		
		
		glState = RenderType.State.getBuilder()
				.texture(new RenderState.TextureState(TileEntityLockedChestRenderer.TEXT_LOCK_LOC, false, true))
				.transparency(TRANSLUCENT_TRANSPARENCY)
				.lightmap(LIGHTMAP_ENABLED)
				.cull(NO_CULL)
			.build(false);
		LOCKEDCHEST_LOCK = RenderType.makeType(Name("lockedchest_lock"), DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP, GL11.GL_QUADS, 32, glState);
		
		glState = RenderType.State.getBuilder()
				.texture(new RenderState.TextureState(TileEntityLockedChestRenderer.TEXT_CHAINLINK_LOC, false, true))
				.transparency(TRANSLUCENT_TRANSPARENCY)
				.lightmap(LIGHTMAP_ENABLED)
				.cull(NO_CULL)
				.writeMask(WRITE_NO_DEPTH_BUT_COLOR)
			.build(false);
		LOCKEDCHEST_CHAIN = RenderType.makeType(Name("lockedchest_chain"), DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP, GL11.GL_QUADS, 64, glState);
	}
	
	public static final RenderType GetIconType(ResourceLocation texture) {
		RenderType.State glState;
		
		glState = RenderType.State.getBuilder()
				.texture(new RenderState.TextureState(texture, false, true))
				.cull(NO_CULL)
				.lightmap(LIGHTMAP_ENABLED)
				.transparency(TRANSLUCENT_TRANSPARENCY)
				//.layer(VIEW_OFFSET_Z_LAYERING)
				//.target(ITEM_ENTITY_TARGET)
				//.writeMask(WRITE_TO_DEPTH_AND_COLOR)
			.build(false);
		return RenderType.makeType(Name("flaticon"), DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP, GL11.GL_QUADS, 32, glState);
	}
	
	public static final RenderType GetBlendedEntity(ResourceLocation texture, boolean affectsOutline) {
		// This based on RenderType.getEntityTranslucent(locationIn) but with no depth buffer writing
		RenderType.State glState;
		
//		RenderType.State rendertype$state = RenderType.State.getBuilder()
//				.texture(new RenderState.TextureState(LocationIn, false, false))
//				.transparency(TRANSLUCENT_TRANSPARENCY)
//				.diffuseLighting(DIFFUSE_LIGHTING_ENABLED)
//				.alpha(DEFAULT_ALPHA).cull(CULL_DISABLED)
//				.lightmap(LIGHTMAP_ENABLED)
//				.overlay(OVERLAY_ENABLED)
//			.build(outlineIn);
		
		RenderSystem.defaultBlendFunc();
		
		glState = RenderType.State.getBuilder()
				.texture(new RenderState.TextureState(texture, false, false))
				.transparency(new RenderState.TransparencyState("translucent_transparency", () -> {
				      RenderSystem.enableBlend();
				      RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
				   }, () -> {
				      RenderSystem.disableBlend();
				      RenderSystem.defaultBlendFunc();
				   }))
				.diffuseLighting(DIFFUSE_LIGHTING_ENABLED)
				.alpha(DEFAULT_ALPHA).cull(NO_CULL)
				.lightmap(LIGHTMAP_ENABLED)
				.overlay(OVERLAY_ENABLED)
				.writeMask(WRITE_NO_DEPTH_BUT_COLOR)
			.build(affectsOutline);
		return RenderType.makeType(Name("nostrum_blendedentity"), DefaultVertexFormats.ENTITY, GL11.GL_QUADS, 256, glState);
	}
	
	public static final RenderType GetBlendedEntity(ResourceLocation texture) {
		return GetBlendedEntity(texture, true);
	}
	
}
