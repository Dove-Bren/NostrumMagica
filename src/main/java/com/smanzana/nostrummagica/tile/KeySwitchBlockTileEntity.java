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

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Util;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class KeySwitchBlockTileEntity extends EntityProxiedTileEntity<KeySwitchTriggerEntity> implements IWorldKeyHolder, IUniqueBlueprintTileEntity {
	
	private WorldKey key;
	private DyeColor color;
	private boolean triggered;
	
	public KeySwitchBlockTileEntity() {
		super(NostrumTileEntities.KeySwitchTileEntityType);
		key = new WorldKey();
		color = DyeColor.RED;
		triggered = false;
	}
	
	public KeySwitchBlockTileEntity(WorldKey key) {
		this();
		this.key = key;
	}
	
	private static final String NBT_KEY = "switch_key";
	private static final String NBT_COLOR = "color";
	private static final String NBT_TRIGGERED = "triggered";
	
	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		nbt = super.write(nbt);
		
		nbt.put(NBT_KEY, this.key.asNBT());
		nbt.putString(NBT_COLOR, this.color.name());
		nbt.putBoolean(NBT_TRIGGERED, this.isTriggered());
		
		return nbt;
	}
	
	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		
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
	protected KeySwitchTriggerEntity makeTriggerEntity(World world, double x, double y, double z) {
		KeySwitchTriggerEntity ent = new KeySwitchTriggerEntity(NostrumEntityTypes.keySwitchTrigger, world);
		ent.setPosition(x, y, z);
		return ent;
	}
	
	@Override
	public void trigger(LivingEntity entity, DamageSource source, float damage) {
		if (this.world.isRemote() || this.isTriggered()) {
			return;
		}
		
		this.setTriggered(true);
		AutoDungeons.GetWorldKeys().addKey(getWorldKey());
		world.setBlockState(pos, Blocks.AIR.getDefaultState());

		for (ServerPlayerEntity player : ((ServerWorld) world).getPlayers((p) -> {
			return p.getDistanceSq(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5) < 900;
		})) {
			player.sendMessage(new TranslationTextComponent("info.world_key.gotkey"), Util.DUMMY_UUID);
		}
		
		NostrumMagicaSounds.AMBIENT_WOOSH2.play(world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
		NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
				30, pos.getX() + .5, pos.getY() + 1.5, pos.getZ() + .5, 0,
				50, 10,
				Vector3d.ZERO, new Vector3d(.075, .05, .075)
				).gravity(-.1f).color(this.getColor().getColorValue() | 0xAA000000));
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