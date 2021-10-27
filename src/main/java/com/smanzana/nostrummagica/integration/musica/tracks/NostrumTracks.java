package com.smanzana.nostrummagica.integration.musica.tracks;

import com.smanzana.musica.music.MusicPlayer;

public class NostrumTracks {

	public static void registerTracks() {
		MusicPlayer.instance().registerTrack(new NostrumOverworldDungeonTrack(), 1);
		MusicPlayer.instance().registerTrack(new NostrumSorceryBackgroundTrack(), 1);
		MusicPlayer.instance().registerOverlay(new NostrumSorceryBattleTrack(), 1);
	}
	
}
