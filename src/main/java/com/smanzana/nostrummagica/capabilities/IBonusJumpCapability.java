package com.smanzana.nostrummagica.capabilities;

public interface IBonusJumpCapability {
	
	public int getCount();
	
	public void incrCount();
	
	public void resetCount();
	
	public void copy(IBonusJumpCapability source);

}
