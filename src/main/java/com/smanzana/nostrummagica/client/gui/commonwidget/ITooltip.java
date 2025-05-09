package com.smanzana.nostrummagica.client.gui.commonwidget;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.network.chat.Component;

@FunctionalInterface
public interface ITooltip extends Supplier<List<Component>> {
	
}
