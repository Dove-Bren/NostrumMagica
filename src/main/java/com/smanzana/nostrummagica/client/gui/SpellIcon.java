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
import com.smanzana.nostrummagica.spells.components.triggers.BeamTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.DamagedTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.DelayTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.FoodTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.HealthTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.InstantTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ManaTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ProjectileTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ProximityTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.TouchTrigger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SpellIcon {

	private static Map<EMagicElement, SpellIcon> elementCache = new EnumMap<>(EMagicElement.class);
	private static Map<EAlteration, SpellIcon> alterationCache = new EnumMap<>(EAlteration.class);
	private static Map<String, SpellIcon> shapeCache = new HashMap<>();
	private static Map<String, SpellIcon> triggerCache = new HashMap<>(); 
	
	public static SpellIcon get(EMagicElement element) {
		SpellIcon icon = elementCache.get(element);
		if (icon == null) {
			icon = new SpellIcon(element);
			elementCache.put(element, icon);
		}
		
		return icon;
	}
	
	public static SpellIcon get(EAlteration alteration) {
		SpellIcon icon = alterationCache.get(alteration);
		if (icon == null) {
			icon = new SpellIcon(alteration);
			alterationCache.put(alteration, icon);
		}
		
		return icon;
	}
	
	public static SpellIcon get(SpellShape shape) {
		String name = shape.getShapeKey();
		SpellIcon icon = shapeCache.get(name);
		
		if (icon == null) {
			icon = new SpellIcon(shape);
			shapeCache.put(shape.getShapeKey(), icon);
		}
		
		return icon;
	}
	
	public static SpellIcon get(SpellTrigger trigger) {
		String name = trigger.getTriggerKey();
		SpellIcon icon = triggerCache.get(name);
		
		if (icon == null) {
			icon = new SpellIcon(trigger);
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
	
	private SpellIcon(EMagicElement element) {
		int ord;
		if (element == null)
			ord = 0;
		else
			ord = element.ordinal();
		
		offsetV = vOffsetElement;
		offsetU = uWidthElement * ord;
		width = uWidthElement;
		height = 32;
		
		model = new ResourceLocation(NostrumMagica.MODID,
				"textures/models/symbol/element_" + element.name().toLowerCase() + ".png");
	}
	
	private SpellIcon(EAlteration alteration) {
		int ord;
		if (alteration == null)
			ord = 0;
		else
			ord = alteration.ordinal();
		
		offsetV = vOffsetAlteration;
		offsetU = uWidthAlteration * ord;
		width = uWidthAlteration;
		height = 32;
		
		model = new ResourceLocation(NostrumMagica.MODID,
				"textures/models/symbol/alteration_" + alteration.name().toLowerCase() + ".png");
	}
	
	public SpellIcon(SpellTrigger trigger) {
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
		}
		//else if (trigger instanceof Trigger) {
			
		//}
		else if (trigger instanceof HealthTrigger) {
			u = 6;
		} else if (trigger instanceof ManaTrigger) {
			u = 7;
		} else if (trigger instanceof FoodTrigger) {
			u = 0;
			v = 1;
		} else if (trigger instanceof InstantTrigger) {
			v = 1;
			u = 1;
		} else if (trigger instanceof DamagedTrigger) {
			u = 2;
			v = 1;
		}
		
		offsetV = vOffsetTrigger + (uWidthTrigger * v);
		offsetU = uWidthTrigger * u;
		width = uWidthTrigger;
		height = uWidthTrigger;
		
		model = new ResourceLocation(NostrumMagica.MODID,
				"textures/models/symbol/" + trigger.getTriggerKey().toLowerCase() + ".png");
	}
	
	public SpellIcon(SpellShape shape) {
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
				"textures/models/symbol/" + shape.getShapeKey().toLowerCase() + ".png");
	}
	
	public void draw(Gui parent, FontRenderer fonter, int xOffset, int yOffset, int width, int height) {
		GL11.glPushMatrix();

		//GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		float scaleU = (float) width / (float) this.width;
		float scaleV = (float) height / (float) this.height;
		
		xOffset *= 1f / scaleU;
		yOffset *= 1f / scaleV;
		
		GL11.glScalef(scaleU, scaleV, 0f); // Idk which it is!
		GlStateManager.color(1f, 1f, 1f, 1f);
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(iconSheet);
		
		parent.drawTexturedModalRect(xOffset, yOffset, offsetU, offsetV,
				this.width, this.height);
		
		GL11.glPopMatrix();
	}
	
	public ResourceLocation getModelLocation() {
		return model;
	}
	
}
