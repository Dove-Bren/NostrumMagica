package com.smanzana.nostrummagica.serializer;

import com.smanzana.nostrummagica.item.armor.DragonArmor;
import com.smanzana.nostrummagica.item.armor.DragonArmor.DragonArmorMaterial;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.IDataSerializer;

public final class DragonArmorMaterialSerializer implements IDataSerializer<DragonArmor.DragonArmorMaterial> {
	
	public static final DragonArmorMaterialSerializer instance = new DragonArmorMaterialSerializer();
	
	private DragonArmorMaterialSerializer() {
		
	}
	
	@Override
	public void write(PacketBuffer buf, DragonArmor.DragonArmorMaterial value) {
		buf.writeEnum(value);
	}

	@Override
	public DragonArmor.DragonArmorMaterial read(PacketBuffer buf)  {
		return buf.readEnum(DragonArmor.DragonArmorMaterial.class);
	}

	@Override
	public DataParameter<DragonArmor.DragonArmorMaterial> createAccessor(int id) {
		return new DataParameter<>(id, this);
	}

	@Override
	public DragonArmorMaterial copy(DragonArmorMaterial value) {
		return value;
	}
}