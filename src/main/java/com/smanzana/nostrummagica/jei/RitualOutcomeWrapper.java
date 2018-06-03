package com.smanzana.nostrummagica.jei;

import com.smanzana.nostrummagica.rituals.outcomes.IRitualOutcome;

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
