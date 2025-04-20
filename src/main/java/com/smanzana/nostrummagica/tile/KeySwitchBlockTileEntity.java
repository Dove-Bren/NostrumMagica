package com.smanzana.nostrummagica.tile;

import java.util.UUID;

import com.smanzana.autodungeons.AutoDungeons;
import com.smanzana.autodungeons.tile.IUniqueBlueprintTileEntity;
import com.smanzana.autodungeons.tile.IWorldKeyHolder;
import com.smanzana.autodungeons.world.WorldKey;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.entity.KeySwitchTriggerEntity;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class KeySwitchBlockTileEntity extends EntityProxiedTileEntity<KeySwitchTriggerEntity> implements IWorldKeyHolder, IUniqueBlueprintTileEntity {
	
	private WorldKey key;
	private DyeColor color;
	private boolean triggered;
	
	public KeySwitchBlockTileEntity(BlockPos pos, BlockState state) {
		super(NostrumTileEntities.KeySwitchTileEntityType, pos, state);
		key = new WorldKey();
		color = DyeColor.RED;
		triggered = false;
	}
	
	public KeySwitchBlockTileEntity(WorldKey key, BlockPos pos, BlockState state) {
		this(pos, state);
		this.key = key;
	}
	
	private static final String NBT_KEY = "switch_key";
	private static final String NBT_COLOR = "color";
	private static final String NBT_TRIGGERED = "triggered";
	
	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		nbt.put(NBT_KEY, this.key.asNBT());
		nbt.putString(NBT_COLOR, this.color.name());
		nbt.putBoolean(NBT_TRIGGERED, this.isTriggered());
	}
	
	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		
		this.key = WorldKey.fromNBT(nbt.getCompound(NBT_KEY));
		try {
			this.color = DyeColor.valueOf(nbt.getString(NBT_COLOR).toUpperCase());
		} catch (Exception e) {
			this.color = DyeColor.RED;
		}
		this.triggered = nbt.getBoolean(NBT_TRIGGERED);
	}
	
	public boolean isTriggered() {
		return this.triggered;
	}
	
	public void setTriggered(boolean triggered) {
		this.triggered = triggered;
		this.dirty();
	}
	
	public void setColor(DyeColor color) {
		this.color = color;
		this.dirty();
	}
	
	public DyeColor getColor() {
		return this.color;
	}
	
	@Override
	public void setWorldKey(WorldKey key) {
		this.key = key;
		dirty();
	}
	
	@Override
	public boolean hasWorldKey() {
		return true; // Always have one
	}
	
	@Override
	public WorldKey getWorldKey() {
		return this.key;
	}
	
	@Override
	protected KeySwitchTriggerEntity makeTriggerEntity(Level world, double x, double y, double z) {
		KeySwitchTriggerEntity ent = new KeySwitchTriggerEntity(NostrumEntityTypes.keySwitchTrigger, world);
		ent.setPos(x, y, z);
		return ent;
	}
	
	@Override
	public void trigger(LivingEntity entity, DamageSource source, float damage) {
		if (this.level.isClientSide() || this.isTriggered()) {
			return;
		}
		
		this.setTriggered(true);
		AutoDungeons.GetWorldKeys().addKey(getWorldKey());
		level.setBlockAndUpdate(worldPosition, Blocks.AIR.defaultBlockState());

		for (ServerPlayer player : ((ServerLevel) level).getPlayers((p) -> {
			return p.distanceToSqr(worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5) < 900;
		})) {
			player.sendMessage(new TranslatableComponent("info.world_key.gotkey"), Util.NIL_UUID);
		}
		
		NostrumMagicaSounds.AMBIENT_WOOSH2.play(level, worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5);
		NostrumParticles.GLOW_ORB.spawn(level, new SpawnParams(
				30, worldPosition.getX() + .5, worldPosition.getY() + 1.5, worldPosition.getZ() + .5, 0,
				50, 10,
				Vec3.ZERO, new Vec3(.075, .05, .075)
				).gravity(-.1f).color(this.getColor().getTextColor() | 0xAA000000));
	}
	
	@Override
	public void onRoomBlueprintSpawn(UUID dungeonID, UUID roomID, boolean isWorldGen) {
		// TODO: should this use dungeon ID? Or even let it be configurable?
		// Sorcery dungeon is one big room, and I feel like MOST of my uses of this
		// will want unique-per-room keys?
		// Ehh well the whole points is that things don't have to be close to eachother, so maybe
		// that's wrong?
		final WorldKey newKey = this.key.mutateWithID(roomID);
		if (isWorldGen) {
			this.key = newKey;
		} else {
			this.setWorldKey(newKey);
		}
	}
}