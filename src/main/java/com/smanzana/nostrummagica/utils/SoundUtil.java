package com.smanzana.nostrummagica.utils;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class SoundUtil {

	public static final void stopSound(ISound sound) {
		Minecraft.getMinecraft().getSoundHandler().stopSound(sound);
		
		// Vanilla doesn't actually remove the sound from its playing list, so if you try and add it again
		// you crash. Remove from the internal playing map, too.
		try {
			SoundManager handler_sndManager =
					ObfuscationReflectionHelper.getPrivateValue(SoundHandler.class, Minecraft.getMinecraft().getSoundHandler(), "sndManager");
			Map<String, ISound> manager_playingSounds = 
				ObfuscationReflectionHelper.getPrivateValue(SoundManager.class, handler_sndManager, "playingSounds");
			
			Iterator<Entry<String, ISound>> it = manager_playingSounds.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, ISound> entry = it.next();
				if (entry.getValue().equals(sound)) {
					it.remove();
				}
			}
		} catch (Exception e) {
			NostrumMagica.logger.error("Failed to fully cancel running sound: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
}
