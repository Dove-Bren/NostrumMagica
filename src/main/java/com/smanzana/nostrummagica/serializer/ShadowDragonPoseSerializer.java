package com.smanzana.nostrummagica.serializer;

import com.smanzana.nostrummagica.entity.boss.shadowdragon.ShadowDragonEntity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;

public final class ShadowDragonPoseSerializer implements EntityDataSerializer<ShadowDragonEntity.BattlePose> {
	
	public static ShadowDragonPoseSerializer instance = new ShadowDragonPoseSerializer();
	
	private ShadowDragonPoseSerializer() {
		;
	}
	
	@Override
	public void write(FriendlyByteBuf buf, ShadowDragonEntity.BattlePose value) {
		buf.writeEnum(value);
	}

	@Override
	public ShadowDragonEntity.BattlePose read(FriendlyByteBuf buf)  {
		return buf.readEnum(ShadowDragonEntity.BattlePose.class);
	}

	@Override
	public EntityDataAccessor<ShadowDragonEntity.BattlePose> createAccessor(int id) {
		return new EntityDataAccessor<>(id, this);
	}

	@Override
	public ShadowDragonEntity.BattlePose copy(ShadowDragonEntity.BattlePose value) {
		return value;
	}
}