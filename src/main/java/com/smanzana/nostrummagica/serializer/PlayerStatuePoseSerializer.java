package com.smanzana.nostrummagica.serializer;

import com.smanzana.nostrummagica.entity.boss.playerstatue.PlayerStatueEntity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;

public final class PlayerStatuePoseSerializer implements EntityDataSerializer<PlayerStatueEntity.BattlePose> {
	
	public static PlayerStatuePoseSerializer instance = new PlayerStatuePoseSerializer();
	
	private PlayerStatuePoseSerializer() {
		;
	}
	
	@Override
	public void write(FriendlyByteBuf buf, PlayerStatueEntity.BattlePose value) {
		buf.writeEnum(value);
	}

	@Override
	public PlayerStatueEntity.BattlePose read(FriendlyByteBuf buf)  {
		return buf.readEnum(PlayerStatueEntity.BattlePose.class);
	}

	@Override
	public EntityDataAccessor<PlayerStatueEntity.BattlePose> createAccessor(int id) {
		return new EntityDataAccessor<>(id, this);
	}

	@Override
	public PlayerStatueEntity.BattlePose copy(PlayerStatueEntity.BattlePose value) {
		return value;
	}
}