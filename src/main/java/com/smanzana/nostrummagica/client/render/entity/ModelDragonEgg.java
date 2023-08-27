package com.smanzana.nostrummagica.client.render.entity;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.entity.dragon.EntityDragonEgg;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.entity.Entity;

public class ModelDragonEgg extends ModelBase {

	private ModelRenderer main;
	
	private static final int textureHeight = 32;
	private static final int textureWidth = 32;
	
	private void addCyl(int y, int height, int radius) {
		main.addBox(-radius, y, -radius, radius * 2, -height, radius * 2);
	}
	
	public ModelDragonEgg() {
		main = new ModelRenderer(this, 0, 0);
		int y = 28;
		
		main.setTextureSize(textureWidth, textureHeight);
		main.setRotationPoint(0, 20, 0);
		addCyl(y--, 1, 2);
		addCyl(y--, 1, 4);
		addCyl(y--, 1, 5);
		addCyl(y, 2, 6); y -= 2;
		addCyl(y, 4, 7); y -= 4;
		addCyl(y, 2, 6); y -= 2;
		addCyl(y, 2, 5); y -= 2;
		addCyl(y--, 1, 4);
		addCyl(y--, 1, 3);
		addCyl(y--, 1, 2);
	}
	
	@Override
	public void render(Entity entity, float time, float swingProgress,
			float swing, float headAngleY, float headAngleX, float scale) {
		setRotationAngles(time, swingProgress, swing, headAngleY, headAngleX, scale, entity);
		
		GL11.glPushMatrix();
		
		float modelScale = 0.5f;
		GL11.glScalef(modelScale, modelScale, modelScale);
		
		float coldScale = 0f;
		if (entity instanceof EntityDragonEgg) {
			EntityDragonEgg egg = (EntityDragonEgg) entity;
			coldScale = 1f - (egg.getHeat() / EntityDragonEgg.HEAT_MAX);
		}
		
		GlStateManager.color4f(1f - (coldScale * .4f), 1f - (coldScale * .1f), 1f - (coldScale * .1f), 1f);
		
		main.render(scale);
		
		GL11.glPopMatrix();
	}
}
