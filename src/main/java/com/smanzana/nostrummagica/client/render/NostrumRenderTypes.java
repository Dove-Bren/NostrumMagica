package com.smanzana.nostrummagica.client.render;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrummagica.client.render.entity.ModelSwitchTrigger;
import com.smanzana.nostrummagica.client.render.entity.RenderHookShot;
import com.smanzana.nostrummagica.client.render.tile.TileEntityLockedChestRenderer;
import com.smanzana.nostrummagica.client.render.tile.TileEntityPortalRenderer;
import com.smanzana.nostrummagica.client.render.tile.TileEntityProgressionDoorRenderer;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
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
	
	private static final String Name(String suffix) {
		return "nostrumrender_" + suffix;
	}
	
	// Could make func that took a texture and returns a render type for flat, unlit icons
	
	static {
		// Set up states that we use. Pull some from RenderState itself, and make some custom ones/ones not worth the effort to pull out.
		//final RenderState.TextureState BLOCKATLAS_MIPMAP = new RenderState.TextureState(AtlasTexture.LOCATION_BLOCKS_TEXTURE, false, true);
		
		//final RenderState.TransparencyState LIGHTNING_TRANSPARENCY = ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "field_228512_d_");
		final RenderState.TransparencyState TRANSLUCENT_TRANSPARENCY = ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "field_228515_g_");
		final RenderState.TransparencyState NO_TRANSPARENCY = ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "field_228510_b_");

		final RenderState.LayerState VIEW_OFFSET_Z_LAYERING = ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "field_239235_M_");

		final RenderState.TargetState ITEM_ENTITY_TARGET = ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "field_241712_U_");
		
		final boolean ENABLE_DEPTH_WRITING = true;
	    final boolean ENABLE_COLOUR_COMPONENTS_WRITING = true;
	    final RenderState.WriteMaskState WRITE_TO_DEPTH_AND_COLOR
	            = new RenderState.WriteMaskState(ENABLE_DEPTH_WRITING, ENABLE_COLOUR_COMPONENTS_WRITING);
	    
	    final RenderState.CullState NO_CULL = new RenderState.CullState(false);
	    
	    final RenderState.DepthTestState DEPTH_EQUAL = new RenderState.DepthTestState("==", GL11.GL_EQUAL);
	    final RenderState.DepthTestState NO_DEPTH_TEST = new RenderState.DepthTestState("none", GL11.GL_ALWAYS);
	    
	    final RenderState.LightmapState NO_LIGHTING = new RenderState.LightmapState(false);
	    final RenderState.LightmapState LIGHTMAP_ENABLED = new RenderState.LightmapState(true);
	    
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
		
		glState = RenderType.State.getBuilder()
				.texture(new RenderState.TextureState(LayerManaArmor.TEXTURE_ARMOR, true, false))
				.transparency(TRANSLUCENT_TRANSPARENCY)
				.lightmap(NO_LIGHTING)
				.layer(VIEW_OFFSET_Z_LAYERING)
				.writeMask(WRITE_TO_DEPTH_AND_COLOR)
				.texturing(MANAARMOR_GLINT)
				// depth test?
			.build(false);
		MANA_ARMOR = RenderType.makeType(Name("manaarmor"), DefaultVertexFormats.ENTITY, GL11.GL_QUADS, 128, glState);
		
		glState = RenderType.State.getBuilder()
				.texture(new RenderState.TextureState(ModelSwitchTrigger.TEXT, false, true))
				.transparency(TRANSLUCENT_TRANSPARENCY)
				.lightmap(NO_LIGHTING)
				.layer(VIEW_OFFSET_Z_LAYERING)
				.writeMask(WRITE_TO_DEPTH_AND_COLOR)
			.build(false);
		SWITCH_TRIGGER_BASE = RenderType.makeType(Name("switch_trigger_base"), DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_TRIANGLES, 64, glState);
		
		glState = RenderType.State.getBuilder()
				.texture(new RenderState.TextureState(ModelSwitchTrigger.CAGE_TEXT, false, true))
				.transparency(TRANSLUCENT_TRANSPARENCY)
				.lightmap(NO_LIGHTING)
				.layer(VIEW_OFFSET_Z_LAYERING)
				.depthTest(DEPTH_EQUAL)
				.writeMask(WRITE_TO_DEPTH_AND_COLOR)
			.build(false);
		SWITCH_TRIGGER_CAGE = RenderType.makeType(Name("switch_trigger_cage"), DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_TRIANGLES, 64, glState);
		
		glState = RenderType.State.getBuilder()
				.texture(new RenderState.TextureState(TileEntityPortalRenderer.TEX_LOC, false, true))
				.transparency(TRANSLUCENT_TRANSPARENCY)
				.lightmap(NO_LIGHTING)
				.cull(NO_CULL)
				.depthTest(NO_DEPTH_TEST)
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
				.transparency(NO_TRANSPARENCY)
				.lightmap(LIGHTMAP_ENABLED)
				.cull(NO_CULL)
			.build(false);
		LOCKEDCHEST_CHAIN = RenderType.makeType(Name("lockedchest_chain"), DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP, GL11.GL_QUADS, 64, glState);
	}
}
