package com.smanzana.nostrummagica.integration.musica.tracks;

import com.smanzana.musica.music.IMusicTrack;
import com.smanzana.musica.music.MusicSound;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy.SpecialEffect;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.Optional;

@Optional.Interface(iface="com.smanzana.musica.IMusicTrack", modid="musica")
public class NostrumSorceryBattleTrack implements IMusicTrack {
	
	protected boolean didIntro;
	
	protected final MusicSound soundIntro;
	protected final MusicSound soundLoop;
	
	public NostrumSorceryBattleTrack() {
		didIntro = false;
		
		soundIntro = IMusicTrack.SoundFromEvent(NostrumMagicaSounds.MUSIC_DUNGEON2_BATTLE_INTRO.getEvent());
		soundLoop = IMusicTrack.SoundFromEvent(NostrumMagicaSounds.MUSIC_DUNGEON2_BATTLE_LOOP.getEvent());
		
		// Make loop loop natively
		soundLoop.setRepeat(true, 0);
	}

	@Override
	public boolean shouldPlay(EntityPlayerSP player) {
		if (player == null || player.world == null || player.dimension != ModConfig.config.sorceryDimensionIndex()) {
			return false;
		}
		
		return NostrumMagica.magicEffectProxy.getData(player, SpecialEffect.TARGETED) != null;
	}
	
	@Override
	public boolean shouldLoop(EntityPlayerSP player) {
		return shouldPlay(player);
	}

	@Override
	public MusicSound getSound(EntityPlayerSP player) {
		if (!didIntro) {
			didIntro = true;
			return soundIntro;
		}
		return soundLoop;
	}
	
	@Override
	public void onStop(EntityPlayerSP player) {
		this.didIntro = false;
	}

}
