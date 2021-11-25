package com.smanzana.nostrummagica.client.effects;

public enum ClientEffectIcon {

	SHIELD("shield", false),
	TING1("ting1", false),
	TING2("ting2", false),
	TING3("ting3", false),
	TING4("ting4", false),
	TING5("ting5", false),
	CYL("cyl", true),
	SHELL("shell", true),
	ARROWU("arrow_up", false),
	ARROWD("arrow_down", false),
	ORB_CLOUDY("orb_cloudy", true),
	ORB_SCALED("orb_scaled", true),
	ORB_PURE("orb_pure", true),
	THORN_0("thorn0", true),
	THORN_1("thorn1", true),
	THORN_2("thorn2", true),
	THORN_3("thorn3", true),
	THORN_4("thorn4", true),
	ARROW_SLASH("arrow_slash", false)
	;
	
	private String key;
	private boolean isObj;
	
	private ClientEffectIcon(String key, boolean isObj) {
		this.key = key;
		this.isObj = isObj;
	}
	
	public String getKey() {
		return key;
	}
	
	public String getModelKey() {
		return key + (isObj ? ".obj" : "");
	}
}
