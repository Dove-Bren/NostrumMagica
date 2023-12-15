package com.smanzana.nostrummagica.client.render.entity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class ModelOBJ<T extends Entity> extends EntityModel<T> {
	
	private List<RenderObjCapture> children;
	
	protected ModelOBJ() {
		children = null;
	}

	protected abstract ModelResourceLocation[] getEntityModels();
	protected abstract boolean preRender(T entity, int model, BufferBuilder buffer, double x, double y, double z, float entityYaw, float partialTicks, float scale);
	//protected IUnbakedModel retexture(int i, UnbakedOBJModel model) {return model.model;}
	protected int getColor(int i, T entity) {return -1;}

	@Override
	public void render(T entity, float time, float swingProgress,
			float swing, float headAngleY, float headAngleX, float scale) {
		if (null == children) {
			init();
		}

		for (int i = 0; i < children.size(); i++) {
			children.get(i).render(scale, entity, i);
		}
	}
	
	protected void init() {
		ModelResourceLocation[] locs = this.getEntityModels();
		final int len = locs.length;
		this.children = new ArrayList<>(len);
		
		for (int i = 0; i < len; i++) {
			this.children.add(i, new RenderObjCapture(this, locs[i]));
		}
	}
	
	public class RenderObjCapture extends RenderObj {
		
		private T ent;
		private int modelIdx;

		protected RenderObjCapture(EntityModel<T> base, ModelResourceLocation loc) {
			super(base, loc);
		}

		@Override
		protected boolean preRender(BufferBuilder buffer, float scale) {
			return ModelOBJ.this.preRender(ent, this.modelIdx, buffer, ent.posX, ent.posY, ent.posZ, ent.rotationYaw, 0, scale);
		}
		
//		@Override
//		protected IModel retexture(IModel model) {
//			return ModelOBJ.this.retexture(this.modelIdx, model);
//		}
		
		@Override
		protected int getColor() {
			return ModelOBJ.this.getColor(this.modelIdx, ent); 
		}
		
		@OnlyIn(Dist.CLIENT)
		public void render(float scale, T entity, int i) {
			this.ent = entity;
			this.modelIdx = i;
			render(scale);
		}
	}
	
}
