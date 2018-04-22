package com.smanzana.nostrummagica.client.effects;

public enum ClientEffectIcon {

	SHIELD("shield", false),
	TING1("ting1", false),
	TING2("ting2", false),
	TING3("ting3", false),
	TING4("ting4", false),
	TING5("ting5", false),
	CYL("cyl", true),
	SHELL("shell", true);
	
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
