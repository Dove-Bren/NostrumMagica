package com.smanzana.nostrummagica.config;

/**
 * Receives updates about config when the config is changed, given the class
 * registered itself with the ModConfig
 * @author Skyler
 *
 */
public interface IConfigWatcher {

	public void onConfigUpdate(ModConfig config);
	
}
