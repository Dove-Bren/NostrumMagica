package com.smanzana.nostrummagica.integration.musica;

import com.smanzana.nostrummagica.integration.musica.tracks.NostrumTracks;

public class MusicaClientProxy extends MusicaProxy {

	public MusicaClientProxy() {
		super();
	}
	
	public void preInit() {
		super.preInit();
		
		if (!isEnabled()) {
			return;
		}
	}
	
	public void init() {
		super.init();
		
		if (!isEnabled()) {
			return;
		}
		
	}
	
	public void postInit() {
		super.postInit();
		
		if (!isEnabled()) {
			return;
		}
		
		NostrumTracks.registerTracks();
	}
}
