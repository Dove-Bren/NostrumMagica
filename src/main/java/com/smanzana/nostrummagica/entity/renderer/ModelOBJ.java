package com.smanzana.nostrummagica.entity.renderer;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class ModelOBJ extends ModelBase {

	private RenderObjCapture[] children;
	
	protected ModelOBJ() {
		children = null;
	}

	protected abstract ResourceLocation[] getEntityModels();
	protected abstract boolean preRender(Entity entity, int model, BufferBuilder buffer, double x, double y, double z, float entityYaw, float partialTicks);
	protected IModel retexture(int i, IModel model) {return model;}
	protected int getColor(int i, Entity entity) {return -1;}

	@Override
	public void render(Entity entity, float time, float swingProgress,
			float swing, float headAngleY, float headAngleX, float scale) {
	//public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
		if (null == children) {
			init();
		}

		for (int i = 0; i < children.length; i++) {
			children[i].render(scale, entity, i);
		}
	}
	
	protected void init() {
		ResourceLocation[] locs = this.getEntityModels();
		final int len = locs.length;
		this.children = new RenderObjCapture[len];
		
		for (int i = 0; i < len; i++) {
			this.children[i] = new RenderObjCapture(this, locs[i]);
		}
	}
	
	public class RenderObjCapture extends RenderObj {
		
		private Entity ent;
		private int modelIdx;

		protected RenderObjCapture(ModelBase base, ResourceLocation loc) {
			super(base, loc);
		}

		@Override
		protected boolean preRender(BufferBuilder buffer) {
			return ModelOBJ.this.preRender(ent, this.modelIdx, buffer, ent.posX, ent.posY, ent.posZ, ent.rotationYaw, 0);
		}
		
		@Override
		protected IModel retexture(IModel model) {
			return ModelOBJ.this.retexture(this.modelIdx, model);
		}
		
		@Override
		protected int getColor() {
			return ModelOBJ.this.getColor(this.modelIdx, ent); 
		}
		
		@SideOnly(Side.CLIENT)
		public void render(float scale, Entity entity, int i) {
			this.ent = entity;
			this.modelIdx = i;
			render(scale);
		}
	}
	
}
