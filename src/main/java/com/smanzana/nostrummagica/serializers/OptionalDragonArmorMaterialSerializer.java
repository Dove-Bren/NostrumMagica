package com.smanzana.nostrummagica.serializers;

import com.google.common.base.Optional;
import com.smanzana.nostrummagica.items.DragonArmor;
import com.smanzana.nostrummagica.items.DragonArmor.DragonArmorMaterial;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.IDataSerializer;

public final class OptionalDragonArmorMaterialSerializer implements IDataSerializer<Optional<DragonArmor.DragonArmorMaterial>> {
	
	public static final OptionalDragonArmorMaterialSerializer instance = new OptionalDragonArmorMaterialSerializer();
	
	private OptionalDragonArmorMaterialSerializer() {
		;
	}
	
	@Override
	public void write(PacketBuffer buf, Optional<DragonArmor.DragonArmorMaterial> value) {
		buf.writeBoolean(value.isPresent());
		if (value.isPresent()) {
			buf.writeEnumValue(value.get());
		}
	}

	@Override
	public Optional<DragonArmor.DragonArmorMaterial> read(PacketBuffer buf)  {
		return buf.readBoolean() ? Optional.of(buf.readEnumValue(DragonArmor.DragonArmorMaterial.class)) : Optional.absent();
	}

	@Override
	public DataParameter<Optional<DragonArmor.DragonArmorMaterial>> createKey(int id) {
		return new DataParameter<>(id, this);
	}

	@Override
	public Optional<DragonArmorMaterial> copyValue(Optional<DragonArmorMaterial> value) {
		return Optional.fromNullable(value.orNull());
	}
}