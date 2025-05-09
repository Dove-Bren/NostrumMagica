package com.smanzana.nostrummagica.client.gui.widget;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.client.gui.SpellComponentIcon;
import com.smanzana.nostrummagica.client.gui.commonwidget.ChildButtonWidget;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;

public abstract class SpellComponentButton<T> extends ChildButtonWidget<SpellComponentButton<T>> {
	
	@FunctionalInterface
	public static interface ComponentOnPress<T> {
		public void onPress(SpellComponentButton<T> button, T value);
	}
	
	private final T component;

	@SuppressWarnings("unchecked")
	public SpellComponentButton(Screen parent, int x, int y, int width, int height, @Nonnull T component, ComponentOnPress<T> onPress) {
		super(parent, x, y, width, height, TextComponent.EMPTY, (b) -> {
			onPress.onPress((SpellComponentButton<T>)b, component);
		});
		
		this.component = component;
	}
	
	protected SpellComponentIcon getIcon(@Nonnull T value) {
		if (value instanceof SpellShape shape) {
			return SpellComponentIcon.get(shape);
		} else if (value instanceof EMagicElement element) {
			return SpellComponentIcon.get(element);
		} else {
			return SpellComponentIcon.get((EAlteration) value);
		}
	}
	
	@Override
	protected void renderButtonIcon(PoseStack matrixStackIn, int iconX, int iconY, int iconWidth, int iconHeight, float partialTicks) {
		super.renderButtonIcon(matrixStackIn, iconX, iconY, iconWidth, iconHeight, partialTicks);
		
		SpellComponentIcon icon = this.getIcon(component);
		icon.draw(matrixStackIn, iconX+1, iconY+1, iconWidth-2, iconHeight-2);
	}
	
	public static class SpellShapeButton extends SpellComponentButton<SpellShape> {
		public SpellShapeButton(Screen parent, int x, int y, int width, int height, SpellShape shape, ComponentOnPress<SpellShape> onPress) {
			super(parent, x, y, width, height, shape, onPress);
		}
	}
	
	public static class ElementButton extends SpellComponentButton<EMagicElement> {
		public ElementButton(Screen parent, int x, int y, int width, int height, EMagicElement element, ComponentOnPress<EMagicElement> onPress) {
			super(parent, x, y, width, height, element, onPress);
		}
	}
	
	public static class AlterationButton extends SpellComponentButton<EAlteration> {
		public AlterationButton(Screen parent, int x, int y, int width, int height, EAlteration alteration, ComponentOnPress<EAlteration> onPress) {
			super(parent, x, y, width, height, alteration, onPress);
		}
	}

}
