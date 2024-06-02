package com.smanzana.nostrummagica.integration.musica.tracks;

import javax.annotation.Nullable;

import com.smanzana.musica.music.IMusicTrack;
import com.smanzana.musica.music.MusicSound;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.util.DimensionUtils;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;

//@Optional.Interface(iface="com.smanzana.musica.IMusicTrack", modid="musica")
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
	public boolean shouldPlay(ClientPlayerEntity player) {
		return player != null && DimensionUtils.IsOverworld(DimensionUtils.GetDimension(player));
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
		} else {
			if (startPos == null) {
				this.startPos = player.getPosition();
				return soundLow;
			}
			
			if (player.getDistanceSq(startPos.getX() + .5, startPos.getZ() + .5, startPos.getZ() + .5) > 144) {
				return soundHigh;
			} else {
				return soundLow;
			}
		}
	}
	
	@Override
	public void onStop(ClientPlayerEntity player) {
		this.didIntro = false; // Reset
		this.startPos = null;
	}

}
