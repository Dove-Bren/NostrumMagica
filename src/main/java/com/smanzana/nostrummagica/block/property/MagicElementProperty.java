package com.smanzana.nostrummagica.block.property;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.world.level.block.state.properties.EnumProperty;

public class MagicElementProperty extends EnumProperty<EMagicElement> {

	protected MagicElementProperty(String name, Collection<EMagicElement> values) {
		super(name, EMagicElement.class, values);
	}
	
	public static MagicElementProperty create(String name) {
      return create(name, (element) -> {
         return true;
      });
   }

   public static MagicElementProperty create(String name, Predicate<EMagicElement> p_61548_) {
      return create(name, Arrays.stream(EMagicElement.values()).filter(p_61548_).collect(Collectors.toList()));
   }

   public static MagicElementProperty create(String name, EMagicElement... allowedValues) {
      return create(name, Lists.newArrayList(allowedValues));
   }

   public static MagicElementProperty create(String name, Collection<EMagicElement> allowedValues) {
      return new MagicElementProperty(name, allowedValues);
   }

}
