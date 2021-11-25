package com.smanzana.nostrummagica.sound;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
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
	STATUS_DEBUFF3("spell.debuff3", SoundCategory.AMBIENT),
	SHIELD_APPLY("spell.shield.cast", SoundCategory.AMBIENT),
	CAST_LAUNCH("spell.cast.launch", SoundCategory.AMBIENT),
	CAST_CONTINUE("spell.cast.continue", SoundCategory.AMBIENT),
	CAST_FAIL("spell.cast.fail", SoundCategory.AMBIENT),
	LEVELUP("player.levelup", SoundCategory.AMBIENT),
	LORE("player.lore.get", SoundCategory.AMBIENT),
	UI_TICK("ui.tick", SoundCategory.AMBIENT, .4f),
	SUCCESS_QUEST("ui.success.quest", SoundCategory.AMBIENT),
	SUCCESS_RESEARCH("ui.success.research", SoundCategory.AMBIENT),
	UI_RESEARCH("ui.research.progress", SoundCategory.PLAYERS),
	AMBIENT_WOOSH("ambient.woosh", SoundCategory.AMBIENT),
	AMBIENT_WOOSH2("ambient.woosh2", SoundCategory.AMBIENT),
	DRAGON_IDLE("mob.dragon.idle", SoundCategory.HOSTILE),
	DRAGON_LAND_HURT("mob.dragon.land_hurt", SoundCategory.HOSTILE),
	DRAGON_WATER_HURT("mob.dragon.water_hurt", SoundCategory.HOSTILE),
	DRAGON_BITE("mob.dragon.attack", SoundCategory.HOSTILE),
	DRAGON_DEATH("mob.dragon.death", SoundCategory.HOSTILE),
	HOOKSHOT_FIRE("item.hookshot.shoot", SoundCategory.PLAYERS),
	HOOKSHOT_TICK("item.hookshot.tick", SoundCategory.PLAYERS),
	WISP_HURT("mob.wisp.hurt", SoundCategory.NEUTRAL),
	WISP_DEATH("mob.wisp.death", SoundCategory.NEUTRAL),
	WISP_IDLE("mob.wisp.idle", SoundCategory.NEUTRAL),
	PORTAL("ambient.portal", SoundCategory.AMBIENT),
	WING_FLAP("entity.wing.flap", SoundCategory.PLAYERS),
	LUX_HURT("mob.lux.hurt", SoundCategory.NEUTRAL),
	LUX_DEATH("mob.lux.death", SoundCategory.NEUTRAL),
	LUX_IDLE("mob.lux.idle", SoundCategory.NEUTRAL),
	HEAVY_STRIKE("spell.heavy_strike", SoundCategory.HOSTILE, .4f),
	MUSIC_DUNGEON1_INTRO("music.dungeon1.intro", SoundCategory.MUSIC),
	MUSIC_DUNGEON1_LOW("music.dungeon1.low", SoundCategory.MUSIC),
	MUSIC_DUNGEON1_HIGH("music.dungeon1.high", SoundCategory.MUSIC),
	MUSIC_DUNGEON2_INTRO("music.dungeon2.intro", SoundCategory.MUSIC),
	MUSIC_DUNGEON2_LOW("music.dungeon2.low", SoundCategory.MUSIC),
	MUSIC_DUNGEON2_LOW_ADV("music.dungeon2.low_adv", SoundCategory.MUSIC),
	MUSIC_DUNGEON2_HIGH("music.dungeon2.high", SoundCategory.MUSIC),
	MUSIC_DUNGEON2_HIGH_ADV("music.dungeon2.high_adv", SoundCategory.MUSIC),
	MUSIC_DUNGEON2_BATTLE_INTRO("music.dungeon2.battle.intro", SoundCategory.MUSIC),
	MUSIC_DUNGEON2_BATTLE_LOOP("music.dungeon2.battle.loop", SoundCategory.MUSIC)
	;
	
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
		event.setRegistryName(resource);
		this.volume = volume;
	}
	
	public ResourceLocation getLocation() {
		return this.resource;
	}
	
	public void play(Entity at) {
		play(null, at.world, at.getPositionVector());
	}
	
	public void play(EntityPlayer at) {
		play(at, at.world, at.getPositionVector());
	}
	
	public void play(EntityPlayer player, World world, Vec3d at) {
		play(player, world, at.x, at.y, at.z);
	}
	
	public void play(World world, double x, double y, double z) {
		play(null, world, x, y, z);
	}
	
	public void playClient(Entity at) {
		playClient(at.world, at.posX, at.posY, at.posZ);
	}
	
	public void playClient(World world, double x, double y, double z) {
		world.playSound(x, y, z, event, category, volume, .8f + (NostrumMagica.rand.nextFloat() * 0.4f), false);
	}
	
	public void play(EntityPlayer player, World world, double x, double y, double z) {
		world.playSound(player, x, y, z,
				event, category,
				volume, 0.8f + (NostrumMagica.rand.nextFloat() * 0.4f));
	}
	
	public SoundEvent getEvent() {
		return event;
	}
	
}
