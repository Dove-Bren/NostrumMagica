package com.smanzana.nostrummagica.integration.aetheria.items;

public enum AetherResourceType {
	FLOWER_GINSENG("ginseng_flower"),
	FLOWER_MANDRAKE("mandrake_flower");
	
	private String key;
	
	private AetherResourceType(String key) {
		this.key = key;
	}
	
	public String getUnlocalizedKey() {
		return key;
	}
	
	public String getDescKey() {
		return "item." + key + ".desc";
	}
}
