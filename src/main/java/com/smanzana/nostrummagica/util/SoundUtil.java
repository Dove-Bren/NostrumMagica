package com.smanzana.nostrummagica.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;

public class SoundUtil {

	public static final void stopSound(SoundInstance sound) {
		Minecraft.getInstance().getSoundManager().stop(sound);
		
		// TODO is this still real?
		
//		// Vanilla doesn't actually remove the sound from its playing list, so if you try and add it again
//		// you crash. Remove from the internal playing map, too.
//		try {
//			SoundManager handler_sndManager =
//					ObfuscationReflectionHelper.getPrivateValue(SoundHandler.class, Minecraft.getInstance().getSoundHandler(), "soundEngine");
//			Map<String, ISound> manager_playingSounds = 
//				ObfuscationReflectionHelper.getPrivateValue(SoundManager.class, handler_sndManager, "field_148629_h"); //"playingSounds");
//			
//			Iterator<Entry<String, ISound>> it = manager_playingSounds.entrySet().iterator();
//			while (it.hasNext()) {
//				Entry<String, ISound> entry = it.next();
//				if (entry.get().equals(sound)) {
//					it.discard();
//				}
//			}
//		} catch (Exception e) {
//			NostrumMagica.logger.error("Failed to fully cancel running sound: " + e.getMessage());
//			e.printStackTrace();
//		}
	}
	
}
