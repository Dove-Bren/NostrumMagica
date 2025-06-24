package com.smanzana.nostrummagica.serializer;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;

/**
 * Doesn't support null Floats. Serializes them as 0.
 * @author Skyler
 *
 */
public class BlockPosListSerializer implements EntityDataSerializer<List<BlockPos>> {

	public static final BlockPosListSerializer instance = new BlockPosListSerializer();
	
	private BlockPosListSerializer() {
		;
	}
	
	@Override
	public void write(FriendlyByteBuf buf, List<BlockPos> value) {
		buf.writeInt(value.size());
		for (BlockPos pos : value) {
			buf.writeBlockPos(pos);
		}
	}

	@Override
	public List<BlockPos> read(FriendlyByteBuf buf)  {
		int len = buf.readInt();
		List<BlockPos> array = new ArrayList<>(len);
		for (int i = 0; i < len; i++) {
			array.set(i, buf.readBlockPos());
		}
		return array;
	}

	@Override
	public EntityDataAccessor<List<BlockPos>> createAccessor(int id) {
		return new EntityDataAccessor<>(id, this);
	}

	@Override
	public List<BlockPos> copy(List<BlockPos> value) {
		return Lists.newArrayList(value);
	}
	
}
