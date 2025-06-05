package com.smanzana.nostrummagica.sound;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public enum NostrumMagicaSounds {
	
	MELT_METAL("spell.melt_metal", SoundSource.AMBIENT),
	SHIELD_ABSORB("spell.shield.absorb", SoundSource.AMBIENT),
	SHIELD_BREAK("spell.shield.break", SoundSource.AMBIENT),
	GOLEM_HURT("mob.golem.hurt", SoundSource.NEUTRAL),
	GOLEM_IDLE("mob.golem.idle", SoundSource.NEUTRAL),
	DAMAGE_FIRE("spell.damage.fire", SoundSource.HOSTILE),
	DAMAGE_ICE("spell.damage.ice", SoundSource.HOSTILE),
	DAMAGE_PHYSICAL("spell.damage.physical", SoundSource.HOSTILE),
	DAMAGE_LIGHTNING("spell.damage.lightning", SoundSource.HOSTILE),
	DAMAGE_EARTH("spell.damage.earth", SoundSource.HOSTILE),
	DAMAGE_WIND("spell.damage.wind", SoundSource.HOSTILE),
	DAMAGE_ENDER("spell.damage.ender", SoundSource.HOSTILE),
	STATUS_BUFF1("spell.buff1", SoundSource.AMBIENT),
	STATUS_BUFF2("spell.buff2", SoundSource.AMBIENT),
	STATUS_DEBUFF1("spell.debuff1", SoundSource.AMBIENT),
	STATUS_DEBUFF2("spell.debuff2", SoundSource.AMBIENT),
	STATUS_DEBUFF3("spell.debuff3", SoundSource.AMBIENT),
	SHIELD_APPLY("spell.shield.cast", SoundSource.AMBIENT),
	CAST_LAUNCH("spell.cast.launch", SoundSource.AMBIENT),
	CAST_CONTINUE("spell.cast.continue", SoundSource.AMBIENT),
	CAST_FAIL("spell.cast.fail", SoundSource.AMBIENT),
	LEVELUP("player.levelup", SoundSource.AMBIENT),
	LORE("player.lore.get", SoundSource.AMBIENT, .5f),
	UI_TICK("ui.tick", SoundSource.AMBIENT, .4f),
	SUCCESS_QUEST("ui.success.quest", SoundSource.AMBIENT),
	SUCCESS_RESEARCH("ui.success.research", SoundSource.AMBIENT),
	UI_RESEARCH("ui.research.progress", SoundSource.PLAYERS),
	AMBIENT_WOOSH("ambient.woosh", SoundSource.AMBIENT),
	AMBIENT_WOOSH2("ambient.woosh2", SoundSource.AMBIENT),
	AMBIENT_WOOSH3("ambient.woosh3", SoundSource.AMBIENT),
	DRAGON_IDLE("mob.dragon.idle", SoundSource.HOSTILE),
	DRAGON_LAND_HURT("mob.dragon.land_hurt", SoundSource.HOSTILE),
	DRAGON_WATER_HURT("mob.dragon.water_hurt", SoundSource.HOSTILE),
	DRAGON_BITE("mob.dragon.attack", SoundSource.HOSTILE),
	DRAGON_DEATH("mob.dragon.death", SoundSource.HOSTILE),
	HOOKSHOT_FIRE("item.hookshot.shoot", SoundSource.PLAYERS),
	HOOKSHOT_TICK("item.hookshot.tick", SoundSource.PLAYERS),
	WISP_HURT("mob.wisp.hurt", SoundSource.NEUTRAL),
	WISP_DEATH("mob.wisp.death", SoundSource.NEUTRAL),
	WISP_IDLE("mob.wisp.idle", SoundSource.NEUTRAL),
	PORTAL("ambient.portal", SoundSource.AMBIENT),
	WING_FLAP("entity.wing.flap", SoundSource.PLAYERS),
	LUX_HURT("mob.lux.hurt", SoundSource.NEUTRAL),
	LUX_DEATH("mob.lux.death", SoundSource.NEUTRAL),
	LUX_IDLE("mob.lux.idle", SoundSource.NEUTRAL),
	HEAVY_STRIKE("spell.heavy_strike", SoundSource.HOSTILE, .4f),
	MUSIC_DUNGEON1_INTRO("music.dungeon1.intro", SoundSource.MUSIC),
	MUSIC_DUNGEON1_LOW("music.dungeon1.low", SoundSource.MUSIC),
	MUSIC_DUNGEON1_HIGH("music.dungeon1.high", SoundSource.MUSIC),
	MUSIC_DUNGEON2_INTRO("music.dungeon2.intro", SoundSource.MUSIC),
	MUSIC_DUNGEON2_LOW("music.dungeon2.low", SoundSource.MUSIC),
	MUSIC_DUNGEON2_LOW_ADV("music.dungeon2.low_adv", SoundSource.MUSIC),
	MUSIC_DUNGEON2_HIGH("music.dungeon2.high", SoundSource.MUSIC),
	MUSIC_DUNGEON2_HIGH_ADV("music.dungeon2.high_adv", SoundSource.MUSIC),
	MUSIC_DUNGEON2_BATTLE_INTRO("music.dungeon2.battle.intro", SoundSource.MUSIC),
	MUSIC_DUNGEON2_BATTLE_LOOP("music.dungeon2.battle.loop", SoundSource.MUSIC),
	BAUBLE_EQUIP("item.bauble.equip", SoundSource.PLAYERS),
	TICK("ambient.tick", SoundSource.BLOCKS),
	TOCK("ambient.tock", SoundSource.BLOCKS),
	BUBBLE_SPRAY("spell.bubble_spray", SoundSource.AMBIENT),
	BUBBLE_POP("spell.bubble_pop", SoundSource.AMBIENT),
	ROCK_SMASH("spell.smash", SoundSource.PLAYERS),
	;
	
	private ResourceLocation resource;
	private SoundSource category;
	private SoundEvent event;
	private float volume;
	
	private NostrumMagicaSounds(String suffix, SoundSource category) {
		this(suffix, category, 1.0f);
	}
	
	private NostrumMagicaSounds(String suffix, SoundSource category, float volume) {
		this.resource = new ResourceLocation(NostrumMagica.MODID, suffix);
		this.category = category;
		this.event = new SoundEvent(resource);
		event.setRegistryName(resource);
		this.volume = volume;
	}
	
	public ResourceLocation getLocation() {
		return this.resource;
	}
	
	public void play(Entity at) {
		play(null, at.level, at.position());
	}
	
	public void play(Player at) {
		play(at, at.level, at.position());
	}
	
	public void play(Player player, Level world, Vec3 at) {
		play(player, world, at.x, at.y, at.z);
	}
	
	public void play(Level world, double x, double y, double z) {
		play(null, world, x, y, z);
	}
	
	public void play(Level world, BlockPos pos) {
		play(world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
	}
	
	public void playClient(Entity at) {
		playClient(at.level, at.getX(), at.getY(), at.getZ());
	}
	
	public void playClient(Level world, double x, double y, double z) {
		world.playLocalSound(x, y, z, event, category, volume, .8f + (NostrumMagica.rand.nextFloat() * 0.4f), false);
	}
	
	public void play(Player player, Level world, double x, double y, double z) {
		world.playSound(player, x, y, z,
				event, category,
				volume, 0.8f + (NostrumMagica.rand.nextFloat() * 0.4f));
	}
	
	public SoundEvent getEvent() {
		return event;
	}
	
}
