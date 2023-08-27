package com.smanzana.nostrummagica.client.gui.book;

public interface IClickableBookPage extends IBookPage {

	/**
	 * 
	 * @param parent
	 * @param d
	 * @param e
	 * @param button LWJGL button number
	 * @return
	 */
	public boolean onClick(BookScreen parent, double d, double e, int button);
	
}
