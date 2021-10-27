package com.smanzana.nostrummagica.integration.musica;

public class MusicaProxy {

	private boolean enabled;
	
	public MusicaProxy() {
		this.enabled = false;
	}
	
	public void enable() {
		this.enabled = true;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void preInit() {
		if (!isEnabled()) {
			return;
		}
	}
	
	public void init() {
		if (!isEnabled()) {
			return;
		}
		
	}
	
	public void postInit() {
		if (!isEnabled()) {
			return;
		}
		
	}
}
