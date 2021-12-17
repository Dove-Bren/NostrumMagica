package com.smanzana.nostrummagica.integration.musica.tracks;

import com.smanzana.musica.music.IMusicTrack;
import com.smanzana.musica.music.MusicSound;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.Optional;

@Optional.Interface(iface="com.smanzana.musica.IMusicTrack", modid="musica")
public class NostrumSorceryBackgroundTrack implements IMusicTrack {
	
	protected int introStep; // 0 for nothing so far. 1 for intro. 2 for low.
	
	protected final MusicSound soundIntro;
	protected final MusicSound soundLow;
	protected final MusicSound soundLowAdv;
	protected final MusicSound soundHigh;
	protected final MusicSound soundHighAdv;
	
	public NostrumSorceryBackgroundTrack() {
		introStep = 0;
		
		soundIntro = IMusicTrack.SoundFromEvent(NostrumMagicaSounds.MUSIC_DUNGEON2_INTRO.getEvent());
		soundLow = IMusicTrack.SoundFromEvent(NostrumMagicaSounds.MUSIC_DUNGEON2_LOW.getEvent());
		soundLowAdv = IMusicTrack.SoundFromEvent(NostrumMagicaSounds.MUSIC_DUNGEON2_LOW_ADV.getEvent());
		soundHigh = IMusicTrack.SoundFromEvent(NostrumMagicaSounds.MUSIC_DUNGEON2_HIGH.getEvent());
		soundHighAdv = IMusicTrack.SoundFromEvent(NostrumMagicaSounds.MUSIC_DUNGEON2_HIGH_ADV.getEvent());
	}

	@Override
	public boolean shouldPlay(EntityPlayerSP player) {
		return player != null && player.dimension == ModConfig.config.sorceryDimensionIndex();
	}
	
	@Override
	public boolean shouldLoop(EntityPlayerSP player) {
		return shouldPlay(player);
	}

	@Override
	public MusicSound getSound(EntityPlayerSP player) {
		switch (introStep) {
		case 0:
			introStep++;
			return soundIntro;
		case 1:
			introStep++;
			return soundLow;
		case 2:
			introStep++;
			return soundLowAdv;
		case 3:
		default:
			// Pick one of the highs and then go back down to adv
			introStep = 2;
			return NostrumMagica.rand.nextBoolean() ? soundHigh : soundHighAdv;
		}
	}
	
	@Override
	public void onStop(EntityPlayerSP player) {
		this.introStep = 0;
	}

}