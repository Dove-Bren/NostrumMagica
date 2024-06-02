package com.smanzana.nostrummagica.integration.jei;

import com.smanzana.nostrummagica.ritual.outcome.IRitualOutcome;

public class RitualOutcomeWrapper {

	private IRitualOutcome outcome;

	public RitualOutcomeWrapper(IRitualOutcome outcome) {
		super();
		this.outcome = outcome;
	}
	
	public IRitualOutcome getOutcome() {
		return outcome;
	}
	
}
