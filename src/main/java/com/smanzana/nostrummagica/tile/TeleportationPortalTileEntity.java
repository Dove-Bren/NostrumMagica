package com.smanzana.nostrummagica.tile;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.PortalBlock;
import com.smanzana.nostrummagica.util.Location;
import com.smanzana.nostrummagica.util.WorldUtil;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;

public class TeleportationPortalTileEntity extends PortalBlock.NostrumPortalTileEntityBase  {

	private static final String NBT_TARGET_LEGACY = "target_pos";
	private static final String NBT_TARGET = "target_loc";
	
	private Location target;
	
	protected TeleportationPortalTileEntity(BlockEntityType<?> type) {
		super(type);
	}
	
	public TeleportationPortalTileEntity() {
		this(NostrumTileEntities.TeleportationPortalTileEntityType);
	}
	
	protected TeleportationPortalTileEntity(BlockEntityType<?> type, Location target) {
		this(type);
		this.setTarget(target);
	}
	
	public TeleportationPortalTileEntity(Location target) {
		this(NostrumTileEntities.TeleportationPortalTileEntityType, target);
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
	public void load(BlockState state, CompoundTag compound) {
		super.load(state, compound);
		
		if (compound.contains(NBT_TARGET_LEGACY, NBT.TAG_LONG)) {
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
	public CompoundTag save(CompoundTag nbt) {
		nbt = super.save(nbt);
		
		if (target != null) {
			nbt.put(NBT_TARGET, target.toNBT());
		}
		
		return nbt;
	}
	
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return new ClientboundBlockEntityDataPacket(this.worldPosition, 3, this.getUpdateTag());
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.save(new CompoundTag());
	}
	
	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		super.onDataPacket(net, pkt);
		handleUpdateTag(this.getBlockState(), pkt.getTag());
	}
	
}