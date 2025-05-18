package com.smanzana.nostrummagica.tile;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.PortalBlock;
import com.smanzana.nostrummagica.util.Location;
import com.smanzana.nostrummagica.util.WorldUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TeleportationPortalTileEntity extends PortalBlock.NostrumPortalTileEntityBase  {

	private static final String NBT_TARGET_LEGACY = "target_pos";
	private static final String NBT_TARGET = "target_loc";
	
	private Location target;
	
	protected TeleportationPortalTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}
	
	public TeleportationPortalTileEntity(BlockPos pos, BlockState state) {
		this(NostrumBlockEntities.TeleportationPortal, pos, state);
	}
	
	protected TeleportationPortalTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Location target) {
		this(type, pos, state);
		this.setTarget(target);
	}
	
	public TeleportationPortalTileEntity(BlockPos pos, BlockState state, Location target) {
		this(NostrumBlockEntities.TeleportationPortal, pos, state, target);
	}
	
	public @Nullable Location getTarget() {
		return target;
	}
	
	public void setTarget(@Nullable Location target) {
		this.target = target;
		this.setChanged();
		
		if (level != null) {
			BlockState state = level.getBlockState(worldPosition);
			level.sendBlockUpdated(worldPosition, state, state, 2);
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public int getColor() {
		Player player = NostrumMagica.instance.proxy.getPlayer();
		if (PortalBlock.getRemainingCharge(player) > 0) {
			return 0x00A00050;
		}
		return 0x00500050;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public float getRotationPeriod() {
		return 3;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public float getOpacity() {
		Player player = NostrumMagica.instance.proxy.getPlayer();
		if (PortalBlock.getRemainingCharge(player) > 0) {
			return 0.5f;
		}
		return .9f;
	}
	
	@Override
	public void load(CompoundTag compound) {
		super.load(compound);
		
		if (compound.contains(NBT_TARGET_LEGACY, Tag.TAG_LONG)) {
			// Legacy!
			target = new Location(WorldUtil.blockPosFromLong1_12_2(compound.getLong(NBT_TARGET_LEGACY)), Level.OVERWORLD);
		} else if (compound.contains(NBT_TARGET_LEGACY)) {
			// Legacy 2!
			target = new Location(NbtUtils.readBlockPos(compound.getCompound(NBT_TARGET_LEGACY)), Level.OVERWORLD);
		} else if (compound.contains(NBT_TARGET)) {
			target = Location.FromNBT(compound.getCompound(NBT_TARGET));
		} else {
			target = null;
		}
	}
	
	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		
		if (target != null) {
			nbt.put(NBT_TARGET, target.toNBT());
		}
	}
	
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.saveWithoutMetadata();
	}
	
	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		super.onDataPacket(net, pkt);
	}
	
}