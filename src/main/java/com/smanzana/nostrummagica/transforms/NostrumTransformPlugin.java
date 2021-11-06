package com.smanzana.nostrummagica.transforms;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;

@MCVersion("1.12.2")
@IFMLLoadingPlugin.SortingIndex(Integer.MAX_VALUE-1)
public class NostrumTransformPlugin implements IFMLLoadingPlugin {

	@Override
	public String[] getASMTransformerClass() {
		return new String[] {"com.smanzana.nostrummagica.transforms.NostrumTransformer"};
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		;
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

}
