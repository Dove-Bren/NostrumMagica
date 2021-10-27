package com.smanzana.nostrummagica.integration.musica.tracks;

import javax.annotation.Nullable;

import com.smanzana.musica.music.IMusicTrack;
import com.smanzana.musica.music.MusicSound;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Optional;

@Optional.Interface(iface="com.smanzana.musica.IMusicTrack", modid="musica")
public class NostrumOverworldDungeonTrack implements IMusicTrack {
	
	protected boolean didIntro;
	protected @Nullable BlockPos startPos;
	
	protected final MusicSound soundIntro;
	protected final MusicSound soundLow;
	protected final MusicSound soundHigh;
	
	public NostrumOverworldDungeonTrack() {
		didIntro = false;
		
		soundIntro = IMusicTrack.SoundFromEvent(NostrumMagicaSounds.MUSIC_DUNGEON1_INTRO.getEvent());
		soundLow = IMusicTrack.SoundFromEvent(NostrumMagicaSounds.MUSIC_DUNGEON1_LOW.getEvent());
		soundHigh = IMusicTrack.SoundFromEvent(NostrumMagicaSounds.MUSIC_DUNGEON1_HIGH.getEvent());
	}

	@Override
	public boolean shouldPlay(EntityPlayerSP player) {
		return player != null && player.dimension == 1;
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
		} else {
			if (startPos == null) {
				this.startPos = player.getPosition();
				return soundLow;
			}
			
			if (player.getDistanceSq(startPos) > 144) {
				return soundHigh;
			} else {
				return soundLow;
			}
		}
	}
	
	@Override
	public void onStop(EntityPlayerSP player) {
		this.didIntro = false; // Reset
		this.startPos = null;
	}

}
