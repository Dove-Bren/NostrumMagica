package com.smanzana.nostrummagica.serializer;

import com.smanzana.nostrummagica.entity.boss.primalmage.PrimalMageEntity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;

public final class PrimalMagePoseSerializer implements EntityDataSerializer<PrimalMageEntity.BattlePose> {
	
	public static PrimalMagePoseSerializer instance = new PrimalMagePoseSerializer();
	
	private PrimalMagePoseSerializer() {
		;
	}
	
	@Override
	public void write(FriendlyByteBuf buf, PrimalMageEntity.BattlePose value) {
		buf.writeEnum(value);
	}

	@Override
	public PrimalMageEntity.BattlePose read(FriendlyByteBuf buf)  {
		return buf.readEnum(PrimalMageEntity.BattlePose.class);
	}

	@Override
	public EntityDataAccessor<PrimalMageEntity.BattlePose> createAccessor(int id) {
		return new EntityDataAccessor<>(id, this);
	}

	@Override
	public PrimalMageEntity.BattlePose copy(PrimalMageEntity.BattlePose value) {
		return value;
	}
}