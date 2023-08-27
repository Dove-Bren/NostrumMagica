package com.smanzana.nostrummagica.network.messages;

import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.pet.PetPlacementMode;
import com.smanzana.nostrummagica.pet.PetTargetMode;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Client has issued a pet command.
 * May be about one pet or all pets.
 * @author Skyler
 *
 */
public class PetCommandMessage implements IMessage {

	public static class Handler implements IMessageHandler<PetCommandMessage, IMessage> {

		@Override
		public IMessage onMessage(PetCommandMessage message, MessageContext ctx) {
			final ServerPlayerEntity sp = ctx.getServerHandler().player;
			
			// Can't call from network threads because manager doesn't sync entity target modification
			
			sp.getServerWorld().runAsync(() -> {
				final @Nullable LivingEntity target;
				final @Nullable MobEntity pet;
				
				if (message.targetUUID != null) {
					Entity e = NostrumMagica.getEntityByUUID(sp.world, message.targetUUID);
					if (e instanceof LivingEntity) {
						target = (LivingEntity) e;
					} else {
						target = null;
					}
				} else {
					target = null;
				}
				
				if (message.petUUID != null) {
					Entity e = NostrumMagica.getEntityByUUID(sp.world, message.petUUID);
					if (e instanceof MobEntity) {
						pet = (MobEntity) e;
					} else {
						pet = null;
					}
				} else {
					pet = null;
				}
				
				switch (message.type) {
				case STOP:
					//if (pet == null) {
						NostrumMagica.getPetCommandManager().commandAllStopAttacking(sp);
					//}
					break;
				case ATTACK:
					if (target == null) {
						NostrumMagica.logger.error("Received pet attack command with no target");
						break;
					}
					
					if (pet == null) {
						NostrumMagica.getPetCommandManager().commandAllToAttack(sp, target);
					} else {
						NostrumMagica.getPetCommandManager().commandToAttack(sp, pet, target);
					}
					break;
				case SET_PLACEMENT_MODE:
					if (message.placementMode == null) {
						NostrumMagica.logger.error("Received pet placement mode with null mode");
						break;
					}
					
					NostrumMagica.getPetCommandManager().setPlacementMode(sp, message.placementMode);
					break;
				case SET_TARGET_MODE:
					if (message.targetMode == null) {
						NostrumMagica.logger.error("Received pet target mode with null mode");
						break;
					}
					
					NostrumMagica.getPetCommandManager().setTargetMode(sp, message.targetMode);
					break;
				}
			});
			
			return null;
		}
	}
	
	public static enum PetCommandMessageType {
		STOP,
		ATTACK,
		SET_PLACEMENT_MODE,
		SET_TARGET_MODE,
	}
	
	protected PetCommandMessageType type;
	protected @Nullable UUID petUUID;
	protected @Nullable UUID targetUUID;
	protected @Nullable PetPlacementMode placementMode;
	protected @Nullable PetTargetMode targetMode;

	
	public PetCommandMessage() {
		this(PetCommandMessageType.STOP, null, null, null, null);
	}
	
	private PetCommandMessage(PetCommandMessageType type,
			@Nullable UUID petUUID,
			@Nullable UUID targetUUID,
			@Nullable PetPlacementMode placement,
			@Nullable PetTargetMode target) {
		this.type = type;
		this.petUUID = petUUID;
		this.targetUUID = targetUUID;
		this.placementMode = placement;
		this.targetMode = target;
	}
	
	public static PetCommandMessage AllStop() {
		return new PetCommandMessage(PetCommandMessageType.STOP, null, null, null, null);
	}
	
	public static PetCommandMessage PetStop(LivingEntity pet) {
		return new PetCommandMessage(PetCommandMessageType.STOP, pet.getUniqueID(), null, null, null);
	}
	
	public static PetCommandMessage AllAttack(LivingEntity target) {
		return new PetCommandMessage(PetCommandMessageType.ATTACK, null, target.getUniqueID(), null, null);
	}
	
	public static PetCommandMessage PetAttack(LivingEntity pet, LivingEntity target) {
		return new PetCommandMessage(PetCommandMessageType.ATTACK, pet.getUniqueID(), target.getUniqueID(), null, null);
	}
	
	public static PetCommandMessage AllPlacementMode(PetPlacementMode mode) {
		return new PetCommandMessage(PetCommandMessageType.SET_PLACEMENT_MODE, null, null, mode, null);
	}
	
	public static PetCommandMessage AllTargetMode(PetTargetMode mode) {
		return new PetCommandMessage(PetCommandMessageType.SET_TARGET_MODE, null, null, null, mode);
	}
	
	private static final String NBT_TYPE = "type";
	private static final String NBT_PET_ID = "petID";
	private static final String NBT_TARGET_ID = "targetID";
	private static final String NBT_PLACEMENT_MODE = "placementMode";
	private static final String NBT_TARGET_MODE = "targetMode";
	
	@Override
	public void fromBytes(ByteBuf buf) {
		CompoundNBT tag = ByteBufUtils.readTag(buf);
		try {
			this.type = PetCommandMessageType.valueOf(tag.getString(NBT_TYPE).toUpperCase());
		} catch (Exception e) {
			e.printStackTrace();
			this.type = PetCommandMessageType.STOP;
			this.petUUID = null;
			this.targetUUID = null;
			return;
		}
		
		if (tag.hasUniqueId(NBT_PET_ID)) {
			this.petUUID = tag.getUniqueId(NBT_PET_ID);
		} else {
			this.petUUID = null;
		}
		
		if (tag.hasUniqueId(NBT_TARGET_ID)) {
			this.targetUUID = tag.getUniqueId(NBT_TARGET_ID);
		} else {
			this.targetUUID = null;
		}
		
		if (tag.contains(NBT_PLACEMENT_MODE)) {
			try {
				this.placementMode = PetPlacementMode.valueOf(tag.getString(NBT_PLACEMENT_MODE).toUpperCase());
			} catch (Exception e) {
				e.printStackTrace();
				this.placementMode = PetPlacementMode.FREE;
			}
		} else {
			this.placementMode = PetPlacementMode.FREE;
		}
		
		if (tag.contains(NBT_TARGET_MODE)) {
			try {
				this.targetMode = PetTargetMode.valueOf(tag.getString(NBT_TARGET_MODE).toUpperCase());
			} catch (Exception e) {
				e.printStackTrace();
				this.targetMode = PetTargetMode.FREE;
			}
		} else {
			this.targetMode = PetTargetMode.FREE;
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		CompoundNBT tag = new CompoundNBT();
		
		tag.putString(NBT_TYPE, this.type.name());
		if (petUUID != null) {
			tag.setUniqueId(NBT_PET_ID, petUUID);
		}
		if (targetUUID != null) {
			tag.setUniqueId(NBT_TARGET_ID, targetUUID);
		}
		if (placementMode != null) {
			tag.putString(NBT_PLACEMENT_MODE, this.placementMode.name());
		}
		if (targetMode != null) {
			tag.putString(NBT_TARGET_MODE, this.targetMode.name());
		}
		
		ByteBufUtils.writeTag(buf, tag);
	}
	
}
