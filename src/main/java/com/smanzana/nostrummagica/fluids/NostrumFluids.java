package com.smanzana.nostrummagica.fluids;

import net.minecraftforge.fluids.Fluid;

public enum NostrumFluids {
	POISON_WATER(new FluidPoisonWater(false)),
	POISON_WATER_UNBREAKABLE(new FluidPoisonWater(true)),
	;
	
	protected final Fluid fluid;
	
	private NostrumFluids(Fluid fluid) {
		this.fluid = fluid;
	}
	
	public Fluid getFluid() {
		return this.fluid;
	}
}
