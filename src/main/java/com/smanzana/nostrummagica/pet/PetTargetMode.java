package com.smanzana.nostrummagica.pet;

public enum PetTargetMode {
	FREE, // Follow regular target rules for mob
	DEFENSIVE, // Attack only when we or owner is attacked
	AGGRESSIVE, // Attack anything that might turn aggressive
	PASSIVE, // Wait for command
}
