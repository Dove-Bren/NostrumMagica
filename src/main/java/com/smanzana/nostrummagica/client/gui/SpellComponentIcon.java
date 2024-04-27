package com.smanzana.nostrummagica.client.gui;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.spells.components.shapes.AoEShape;
import com.smanzana.nostrummagica.spells.components.shapes.ChainShape;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.AITargetTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.AtFeetTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.AuraTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.BeamTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.CasterTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.DamagedTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.DelayTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.FieldTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.FoodTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.HealthTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.MagicCutterTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.MagicCyclerTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ManaTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.MortarTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.OtherTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ProjectileTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ProximityTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.SeekingBulletTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.SelfTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.TouchTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.WallTrigger;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

// Nice wrapper to rendering spell component icons
@OnlyIn(Dist.CLIENT)
public class SpellComponentIcon {

	private static Map<EMagicElement, SpellComponentIcon> elementCache = new EnumMap<>(EMagicElement.class);
	private static Map<EAlteration, SpellComponentIcon> alterationCache = new EnumMap<>(EAlteration.class);
	private static Map<String, SpellComponentIcon> shapeCache = new HashMap<>();
	private static Map<String, SpellComponentIcon> triggerCache = new HashMap<>(); 
	
	public static SpellComponentIcon get(EMagicElement element) {
		SpellComponentIcon icon = elementCache.get(element);
		if (icon == null) {
			icon = new SpellComponentIcon(element);
			elementCache.put(element, icon);
		}
		
		return icon;
	}
	
	public static SpellComponentIcon get(EAlteration alteration) {
		SpellComponentIcon icon = alterationCache.get(alteration);
		if (icon == null) {
			icon = new SpellComponentIcon(alteration);
			alterationCache.put(alteration, icon);
		}
		
		return icon;
	}
	
	public static SpellComponentIcon get(SpellShape shape) {
		String name = shape.getShapeKey();
		SpellComponentIcon icon = shapeCache.get(name);
		
		if (icon == null) {
			icon = new SpellComponentIcon(shape);
			shapeCache.put(shape.getShapeKey(), icon);
		}
		
		return icon;
	}
	
	public static SpellComponentIcon get(SpellTrigger trigger) {
		String name = trigger.getTriggerKey();
		SpellComponentIcon icon = triggerCache.get(name);
		
		if (icon == null) {
			icon = new SpellComponentIcon(trigger);
			triggerCache.put(trigger.getTriggerKey(), icon);
		}
		
		return icon;
	}
	
	private static ResourceLocation iconSheet = new ResourceLocation(
			NostrumMagica.MODID, "textures/gui/icons.png");
	private static int uWidthElement = 32;
	private static int uWidthAlteration = 32;
	private static int uWidthTrigger = 32;
	private static int uWidthShape = 32;
	private static int vOffsetAlteration = 64;
	private static int vOffsetElement = 96;
	private static int vOffsetTrigger = 160;
	private static int vOffsetShape = 224; 
	
	private int offsetU;
	private int offsetV;
	private int width;
	private int height;
	private ResourceLocation model;
	
	private SpellComponentIcon(EMagicElement element) {
		int ord;
		if (element == null)
			element = EMagicElement.PHYSICAL;
		
		ord = element.ordinal();
		
		offsetV = vOffsetElement;
		offsetU = uWidthElement * ord;
		width = uWidthElement;
		height = 32;
		
		model = new ResourceLocation(NostrumMagica.MODID,
				"textures/models/symbol/element_" + element.name().toLowerCase() + ".png");
	}
	
	private SpellComponentIcon(EAlteration alteration) {
		int ord;
		if (alteration == null)
			alteration = EAlteration.INFLICT;
		
		ord = alteration.ordinal();
		
		offsetV = vOffsetAlteration;
		offsetU = uWidthAlteration * ord;
		width = uWidthAlteration;
		height = 32;
		
		model = new ResourceLocation(NostrumMagica.MODID,
				"textures/models/symbol/alteration_" + alteration.name().toLowerCase() + ".png");
	}
	
	public SpellComponentIcon(SpellTrigger trigger) {
		int u = 0;
		int v = 0;
		if (trigger instanceof TouchTrigger) {
			; // actually is 0 0
		} else if (trigger instanceof ProximityTrigger) {
			u = 1;
		} else if (trigger instanceof ProjectileTrigger) {
			u = 2;
		} else if (trigger instanceof BeamTrigger) {
			u = 3;
		} else if (trigger instanceof DelayTrigger) {
			u = 4;
		} else if (trigger instanceof AITargetTrigger) {
			u = 5;
		} else if (trigger instanceof HealthTrigger) {
			u = 6;
		} else if (trigger instanceof ManaTrigger) {
			u = 7;
		} else if (trigger instanceof FoodTrigger) {
			u = 0;
			v = 1;
		} else if (trigger instanceof SelfTrigger) {
			v = 1;
			u = 1;
		} else if (trigger instanceof DamagedTrigger) {
			u = 2;
			v = 1;
		} else if (trigger instanceof OtherTrigger) {
			u = 3;
			v = 1;
		} else if (trigger instanceof MagicCutterTrigger) {
			u = 4;
			v = 1;
		} else if (trigger instanceof MagicCyclerTrigger) {
			u = 5;
			v = 1;
		} else if (trigger instanceof SeekingBulletTrigger) {
			u = 6;
			v = 1;
		} else if (trigger instanceof WallTrigger) {
			u = 3;
			v = 2;
		} else if (trigger instanceof MortarTrigger) {
			u = 4;
			v = 2;
		} else if (trigger instanceof FieldTrigger) {
			u = 5;
			v = 2;
		} else if (trigger instanceof AtFeetTrigger) {
			u = 6;
			v = 2;
		} else if (trigger instanceof AuraTrigger) {
			u = 7;
			v = 2;
		} else if (trigger instanceof CasterTrigger) {
			u = 7;
			v = 1;
		} else {
			System.out.println("Trigger doesn't have set uv: " + trigger);
		}
		
		offsetV = vOffsetTrigger + (uWidthTrigger * v);
		offsetU = uWidthTrigger * u;
		width = uWidthTrigger;
		height = uWidthTrigger;
		
		model = new ResourceLocation(NostrumMagica.MODID,
				"textures/models/symbol/trigger_" + trigger.getTriggerKey().toLowerCase() + ".png");
	}
	
	public SpellComponentIcon(SpellShape shape) {
		int u = 0;
		if (shape instanceof SingleShape) {
			; // actually is 0 0
		} else if (shape instanceof AoEShape) {
			u = 1;
		} else if (shape instanceof ChainShape) {
			u = 2;
		}
		
		offsetV = vOffsetShape;
		offsetU = uWidthShape * u;
		width = uWidthShape;
		height = uWidthShape;
		

		
		model = new ResourceLocation(NostrumMagica.MODID,
				"textures/models/symbol/shape_" + shape.getShapeKey().toLowerCase() + ".png");
	}
	
	public void draw(AbstractGui parent, FontRenderer fonter, int xOffset, int yOffset, int width, int height) {
		GL11.glPushMatrix();
		
		

//		if (useIconSheet)
//		{
//		//GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
//			float scaleU = (float) width / (float) this.width;
//			float scaleV = (float) height / (float) this.height;
//			
//			xOffset *= 1f / scaleU;
//			yOffset *= 1f / scaleV;
//			
//			GL11.glScalef(scaleU, scaleV, 0f); // Idk which it is!
//			//GlStateManager.color4f(1f, 1f, 1f, 1f);
//		
//			Minecraft.getInstance().getTextureManager().bindTexture(iconSheet);
//		
//			parent.blit(xOffset, yOffset, offsetU, offsetV,
//					this.width, this.height);
//		}
//		else
		{
			Minecraft.getInstance().getTextureManager().bindTexture(this.getModelLocation());
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, xOffset, yOffset, 0, 0, this.width, this.height, width, height, this.width, this.height);
		}
		
		GL11.glPopMatrix();
	}
	
	public ResourceLocation getModelLocation() {
		return model;
	}
	
}
