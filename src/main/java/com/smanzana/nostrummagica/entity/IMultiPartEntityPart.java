package com.smanzana.nostrummagica.entity;

import javax.annotation.Nonnull;

public interface IMultiPartEntityPart<T extends IMultiPartEntity> {

	public @Nonnull T getParent();
	
}
