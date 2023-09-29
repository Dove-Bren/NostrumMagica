package com.smanzana.nostrummagica.serializers;

import com.smanzana.nostrummagica.items.DragonArmor;
import com.smanzana.nostrummagica.items.DragonArmor.DragonArmorMaterial;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.IDataSerializer;

public final class DragonArmorMaterialSerializer implements IDataSerializer<DragonArmor.DragonArmorMaterial> {
	
	public static final DragonArmorMaterialSerializer instance = new DragonArmorMaterialSerializer();
	
	private DragonArmorMaterialSerializer() {
		
	}
	
	@Override
	public void write(PacketBuffer buf, DragonArmor.DragonArmorMaterial value) {
		buf.writeEnumValue(value);
	}

	@Override
	public DragonArmor.DragonArmorMaterial read(PacketBuffer buf)  {
		return buf.readEnumValue(DragonArmor.DragonArmorMaterial.class);
	}

	@Override
	public DataParameter<DragonArmor.DragonArmorMaterial> createKey(int id) {
		return new DataParameter<>(id, this);
	}

	@Override
	public DragonArmorMaterial copyValue(DragonArmorMaterial value) {
		return value;
	}
}