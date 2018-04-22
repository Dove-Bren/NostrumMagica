package com.smanzana.nostrummagica.client.effects;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.math.Vec3d;

// From made from loading a model and rendering it
public class ClientEffectFormBasic implements ClientEffectForm {
	
	private IBakedModel model;
	private Vec3d offset;
	
	public ClientEffectFormBasic(String key) {
		this(key, 0d, 0d, 0d);
	}
	
	public ClientEffectFormBasic(String key, double x, double y, double z) {
		if (x != 0 || y != 0 || z != 0)
			this.offset = new Vec3d(x, y, z);
		else
			this.offset = null;
		
		BlockRendererDispatcher renderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		
		this.model = renderer.getBlockModelShapes().getModelManager()
				.getModel(new ModelResourceLocation(
				NostrumMagica.MODID + ":effects/" + key, "normal"));
	}
	
	public ClientEffectFormBasic(ClientEffectIcon icon, double x, double y, double z) {
		this(icon.getKey(), x, y, z);
	}

	@Override
	public void draw(Minecraft mc, float partialTicks, int color) {
		if (this.offset != null) {
			GlStateManager.translate(offset.xCoord, offset.yCoord, offset.zCoord);
		}
		
		ClientEffectForm.drawModel(model, color);
	}
}
