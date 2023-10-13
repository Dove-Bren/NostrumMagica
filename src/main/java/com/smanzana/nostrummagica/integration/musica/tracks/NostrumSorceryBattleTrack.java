package com.smanzana.nostrummagica.integration.musica.tracks;

import com.smanzana.musica.music.IMusicTrack;
import com.smanzana.musica.music.MusicSound;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy.EffectData;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy.SpecialEffect;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.world.dimension.NostrumDimensions;

import net.minecraft.client.entity.player.ClientPlayerEntity;

//@Optional.Interface(iface="com.smanzana.musica.IMusicTrack", modid="musica")
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
	public boolean shouldPlay(ClientPlayerEntity player) {
		if (player == null || player.world == null || player.dimension != NostrumDimensions.EmptyDimension) {
			return false;
		}
		
		EffectData data = NostrumMagica.magicEffectProxy.getData(player, SpecialEffect.TARGETED); 
		
		return data != null && data.getCount() > 0; // Clients stick with a non-null data with all 0's when it's over
	}
	
	@Override
	public boolean shouldLoop(ClientPlayerEntity player) {
		return shouldPlay(player);
	}

	@Override
	public MusicSound getSound(ClientPlayerEntity player) {
		if (!didIntro) {
			didIntro = true;
			return soundIntro;
		}
		return soundLoop;
	}
	
	@Override
	public void onStop(ClientPlayerEntity player) {
		this.didIntro = false;
	}

}
