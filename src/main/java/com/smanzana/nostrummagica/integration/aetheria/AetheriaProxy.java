package com.smanzana.nostrummagica.integration.aetheria;

import com.smanzana.nostrumaetheria.api.proxy.APIProxy;

public class AetheriaProxy {
	public static Object makeBurnable(int burnTicks, float aether) {
		return APIProxy.makeBurnable(burnTicks, aether);
	}
	
}
