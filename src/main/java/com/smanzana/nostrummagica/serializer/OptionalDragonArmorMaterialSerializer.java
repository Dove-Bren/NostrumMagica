package com.smanzana.nostrummagica.serializer;

import java.util.Optional;

import com.smanzana.nostrummagica.item.armor.DragonArmor;
import com.smanzana.nostrummagica.item.armor.DragonArmor.DragonArmorMaterial;

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
			buf.writeEnum(value.get());
		}
	}

	@Override
	public Optional<DragonArmor.DragonArmorMaterial> read(PacketBuffer buf)  {
		return buf.readBoolean() ? Optional.of(buf.readEnum(DragonArmor.DragonArmorMaterial.class)) : Optional.empty();
	}

	@Override
	public DataParameter<Optional<DragonArmor.DragonArmorMaterial>> createAccessor(int id) {
		return new DataParameter<>(id, this);
	}

	@Override
	public Optional<DragonArmorMaterial> copy(Optional<DragonArmorMaterial> value) {
		return Optional.ofNullable(value.orElse(null));
	}
}