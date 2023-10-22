package com.smanzana.nostrummagica.client.effects;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

// From made from loading a model and rendering it
@OnlyIn(Dist.CLIENT)
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
		
		BlockRendererDispatcher renderer = Minecraft.getInstance().getBlockRendererDispatcher();
		
		this.model = renderer.getBlockModelShapes().getModelManager()
				.getModel(RenderFuncs.makeDefaultModelLocation(new ResourceLocation(
				NostrumMagica.MODID + ":effect/" + key)));
	}
	
	public ClientEffectFormBasic(ClientEffectIcon icon, double x, double y, double z) {
		this(icon.getKey(), x, y, z);
	}

	@Override
	public void draw(Minecraft mc, float partialTicks, int color) {
		if (this.offset != null) {
			GlStateManager.translated(offset.x, offset.y, offset.z);
		}
		
		GlStateManager.enableBlend();
		ClientEffectForm.drawModel(model, color);
		GlStateManager.disableBlend();
	}
}
