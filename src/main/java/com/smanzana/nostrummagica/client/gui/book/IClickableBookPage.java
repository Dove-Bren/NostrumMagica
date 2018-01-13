package com.smanzana.nostrummagica.client.gui.book;

public interface IClickableBookPage extends IBookPage {

	/**
	 * 
	 * @param parent
	 * @param mouseX
	 * @param mouseY
	 * @param button LWJGL button number
	 * @return
	 */
	public boolean onClick(BookScreen parent, int mouseX, int mouseY, int button);
	
}
