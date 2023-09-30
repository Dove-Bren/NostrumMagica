package com.smanzana.nostrummagica.network.messages;

import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.pet.PetPlacementMode;
import com.smanzana.nostrummagica.pet.PetTargetMode;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Client has issued a pet command.
 * May be about one pet or all pets.
 * @author Skyler
 *
 */
public class PetCommandMessage {

	public static void handle(PetCommandMessage message, Supplier<NetworkEvent.Context> ctx) {
		final ServerPlayerEntity sp = ctx.get().getSender();
		ctx.get().setPacketHandled(true);
		// Can't call from network threads because manager doesn't sync entity target modification
		
		ctx.get().enqueueWork(() -> {
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
					NostrumMagica.instance.getPetCommandManager().commandAllStopAttacking(sp);
				//}
				break;
			case ATTACK:
				if (target == null) {
					NostrumMagica.logger.error("Received pet attack command with no target");
					break;
				}
				
				if (pet == null) {
					NostrumMagica.instance.getPetCommandManager().commandAllToAttack(sp, target);
				} else {
					NostrumMagica.instance.getPetCommandManager().commandToAttack(sp, pet, target);
				}
				break;
			case SET_PLACEMENT_MODE:
				if (message.placementMode == null) {
					NostrumMagica.logger.error("Received pet placement mode with null mode");
					break;
				}
				
				NostrumMagica.instance.getPetCommandManager().setPlacementMode(sp, message.placementMode);
				break;
			case SET_TARGET_MODE:
				if (message.targetMode == null) {
					NostrumMagica.logger.error("Received pet target mode with null mode");
					break;
				}
				
				NostrumMagica.instance.getPetCommandManager().setTargetMode(sp, message.targetMode);
				break;
			}
		});
	}
	
	public static enum PetCommandMessageType {
		STOP,
		ATTACK,
		SET_PLACEMENT_MODE,
		SET_TARGET_MODE,
	}
	
	protected final PetCommandMessageType type;
	protected final @Nullable UUID petUUID;
	protected final @Nullable UUID targetUUID;
	protected final @Nullable PetPlacementMode placementMode;
	protected final @Nullable PetTargetMode targetMode;

	
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
	
	public static PetCommandMessage decode(PacketBuffer buf) {
		final PetCommandMessageType type;
		final @Nullable UUID petUUID;
		final @Nullable UUID targetUUID;
		final @Nullable PetPlacementMode placementMode;
		final @Nullable PetTargetMode targetMode;
		
		type = buf.readEnumValue(PetCommandMessageType.class);
		petUUID = buf.readBoolean() ? buf.readUniqueId() : null;
		targetUUID = buf.readBoolean() ? buf.readUniqueId() : null;
		placementMode = buf.readBoolean() ? buf.readEnumValue(PetPlacementMode.class) : null;
		targetMode = buf.readBoolean() ? buf.readEnumValue(PetTargetMode.class) : null;
		
		return new PetCommandMessage(type, petUUID, targetUUID, placementMode, targetMode);
	}

	public static void encode(PetCommandMessage msg, PacketBuffer buf) {
		buf.writeEnumValue(msg.type);
		
		buf.writeBoolean(msg.petUUID != null);
		if (msg.petUUID != null) {
			buf.writeUniqueId(msg.petUUID);
		}
		
		buf.writeBoolean(msg.targetUUID != null);
		if (msg.targetUUID != null) {
			buf.writeUniqueId(msg.targetUUID);
		}
		
		buf.writeBoolean(msg.placementMode != null);
		if (msg.placementMode != null) {
			buf.writeEnumValue(msg.placementMode);
		}
		
		buf.writeBoolean(msg.targetMode != null);
		if (msg.targetMode != null) {
			buf.writeEnumValue(msg.targetMode);
		}
	}
	
}
