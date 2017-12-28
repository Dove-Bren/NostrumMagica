package com.smanzana.nostrummagica.sound;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public enum NostrumMagicaSounds {
	
	MELT_METAL("spell.melt_metal", SoundCategory.AMBIENT),
	SHIELD_ABSORB("spell.shield.absorb", SoundCategory.AMBIENT),
	SHIELD_BREAK("spell.shield.break", SoundCategory.AMBIENT),
	GOLEM_HURT("mob.golem.hurt", SoundCategory.NEUTRAL),
	GOLEM_IDLE("mob.golem.idle", SoundCategory.NEUTRAL),
	DAMAGE_FIRE("spell.damage.fire", SoundCategory.HOSTILE),
	DAMAGE_ICE("spell.damage.ice", SoundCategory.HOSTILE),
	DAMAGE_PHYSICAL("spell.damage.physical", SoundCategory.HOSTILE),
	DAMAGE_LIGHTNING("spell.damage.lightning", SoundCategory.HOSTILE),
	DAMAGE_EARTH("spell.damage.earth", SoundCategory.HOSTILE),
	DAMAGE_WIND("spell.damage.wind", SoundCategory.HOSTILE),
	DAMAGE_ENDER("spell.damage.ender", SoundCategory.HOSTILE),
	STATUS_BUFF1("spell.buff1", SoundCategory.AMBIENT),
	STATUS_BUFF2("spell.buff2", SoundCategory.AMBIENT),
	STATUS_DEBUFF1("spell.debuff1", SoundCategory.AMBIENT),
	STATUS_DEBUFF2("spell.debuff2", SoundCategory.AMBIENT),
	SHIELD_APPLY("spell.shield.cast", SoundCategory.AMBIENT),
	CAST_LAUNCH("spell.cast.launch", SoundCategory.AMBIENT),
	CAST_CONTINUE("spell.cast.continue", SoundCategory.AMBIENT),
	CAST_FAIL("spell.cast.fail", SoundCategory.AMBIENT),
	LEVELUP("player.levelup", SoundCategory.AMBIENT),
	UI_TICK("ui.tick", SoundCategory.AMBIENT, .4f),
	AMBIENT_WOOSH("ambient.woosh", SoundCategory.AMBIENT);
	
	private ResourceLocation resource;
	private SoundCategory category;
	private SoundEvent event;
	private float volume;
	
	private NostrumMagicaSounds(String suffix, SoundCategory category) {
		this(suffix, category, 1.0f);
	}
	
	private NostrumMagicaSounds(String suffix, SoundCategory category, float volume) {
		this.resource = new ResourceLocation(NostrumMagica.MODID, suffix);
		this.category = category;
		this.event = new SoundEvent(resource);
		this.volume = volume;
	}
	
	public ResourceLocation getLocation() {
		return this.resource;
	}
	
	public void play(Entity at) {
		play(at.worldObj, at.getPositionVector());
	}
	
	public void play(World world, Vec3d at) {
		play(world, at.xCoord, at.yCoord, at.zCoord);
	}
	
	public void play(World world, double x, double y, double z) {
		world.playSound(x, y, z,
				event, category,
				volume, 0.8f + (NostrumMagica.rand.nextFloat() * 0.4f), false);
		System.out.println("Played sound " + this.name());
	}
	
	public static void registerSounds() {
		int idOffset = SoundEvent.REGISTRY.getKeys().size();
		
		for (NostrumMagicaSounds sound : values()) {
			SoundEvent.REGISTRY.register(idOffset++, sound.resource, sound.event);
		}
		
	}

	public SoundEvent getEvent() {
		return event;
	}
	
}
