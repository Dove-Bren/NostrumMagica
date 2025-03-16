package com.smanzana.nostrummagica.serializer;

import java.util.Optional;

import com.smanzana.nostrummagica.item.armor.DragonArmor;
import com.smanzana.nostrummagica.item.armor.DragonArmor.DragonArmorMaterial;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;

public final class OptionalDragonArmorMaterialSerializer implements EntityDataSerializer<Optional<DragonArmor.DragonArmorMaterial>> {
	
	public static final OptionalDragonArmorMaterialSerializer instance = new OptionalDragonArmorMaterialSerializer();
	
	private OptionalDragonArmorMaterialSerializer() {
		;
	}
	
	@Override
	public void write(FriendlyByteBuf buf, Optional<DragonArmor.DragonArmorMaterial> value) {
		buf.writeBoolean(value.isPresent());
		if (value.isPresent()) {
			buf.writeEnum(value.get());
		}
	}

	@Override
	public Optional<DragonArmor.DragonArmorMaterial> read(FriendlyByteBuf buf)  {
		return buf.readBoolean() ? Optional.of(buf.readEnum(DragonArmor.DragonArmorMaterial.class)) : Optional.empty();
	}

	@Override
	public EntityDataAccessor<Optional<DragonArmor.DragonArmorMaterial>> createAccessor(int id) {
		return new EntityDataAccessor<>(id, this);
	}

	@Override
	public Optional<DragonArmorMaterial> copy(Optional<DragonArmorMaterial> value) {
		return Optional.ofNullable(value.orElse(null));
	}
}