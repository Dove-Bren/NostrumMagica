package com.smanzana.nostrummagica.util;

import java.util.function.Function;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Wrapper around #TargetLocation that can be sent across the network and then used to be resolved into a TargetLocation
 */
public class NetTargetLocation implements Function<Function<Integer, Entity>, TargetLocation> {

	protected final @Nullable Vec3 targetPos;
	protected final @Nullable Integer targetEntityID;
	
	protected NetTargetLocation(@Nullable Integer targetEntityID, @Nullable Vec3 targetPos) {
		this.targetPos = targetPos;
		this.targetEntityID = targetEntityID;
	}
	
	public NetTargetLocation(TargetLocation target) {
		this(target.targetEntity == null ? null : target.targetEntity.getId(),
				target.targetPos == null ? null : target.targetPos
				);
	}

	@Override
	public TargetLocation apply(Function<Integer, Entity> getEntity) {
		if (this.targetEntityID != null) {
			return new TargetLocation(getEntity.apply(this.targetEntityID), targetPos);
		} else {
			return new TargetLocation(targetPos);
		}
	}
	
	public CompoundTag toNBT() {
		CompoundTag tag = new CompoundTag();
		if (targetPos != null) {
			tag.put("pos", NetUtils.ToNBT(targetPos));
		}
		if (targetEntityID != null) {
			tag.putInt("entID", targetEntityID);
		}
		return tag;
	}
	
	public static final NetTargetLocation FromNBT(CompoundTag tag) {
		final Vec3 pos = tag.contains("pos") ? NetUtils.VecFromNBT(tag.getCompound("pos")) : null;
		final Integer entID = tag.contains("entID") ? tag.getInt("entID") : null;
		return new NetTargetLocation(entID, pos);
	}
	
	public static final Codec<NetTargetLocation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("targetEntID").forGetter((p) -> p.targetEntityID),
			NetUtils.CODEC_VECTOR3D.fieldOf("targetPos").forGetter((p) -> p.targetPos)
		).apply(instance, NetTargetLocation::new));
	
}
