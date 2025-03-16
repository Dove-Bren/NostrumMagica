package com.smanzana.nostrummagica.serializer;

import com.smanzana.nostrummagica.item.armor.DragonArmor;
import com.smanzana.nostrummagica.item.armor.DragonArmor.DragonArmorMaterial;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;

public final class DragonArmorMaterialSerializer implements EntityDataSerializer<DragonArmor.DragonArmorMaterial> {
	
	public static final DragonArmorMaterialSerializer instance = new DragonArmorMaterialSerializer();
	
	private DragonArmorMaterialSerializer() {
		
	}
	
	@Override
	public void write(FriendlyByteBuf buf, DragonArmor.DragonArmorMaterial value) {
		buf.writeEnum(value);
	}

	@Override
	public DragonArmor.DragonArmorMaterial read(FriendlyByteBuf buf)  {
		return buf.readEnum(DragonArmor.DragonArmorMaterial.class);
	}

	@Override
	public EntityDataAccessor<DragonArmor.DragonArmorMaterial> createAccessor(int id) {
		return new EntityDataAccessor<>(id, this);
	}

	@Override
	public DragonArmorMaterial copy(DragonArmorMaterial value) {
		return value;
	}
}