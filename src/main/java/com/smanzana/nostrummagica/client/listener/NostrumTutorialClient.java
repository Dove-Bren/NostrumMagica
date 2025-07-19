package com.smanzana.nostrummagica.client.listener;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.progression.tutorial.NostrumTutorial;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.KeybindComponent;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * Kind of like the vanilla tutorial class, but nothing is persisted and just shows up when events say they should.
 * Unlike the vanilla, it is not a simple set of steps. Instead, there can be one active tutorial and when it finishes
 * no other one is automatically advanced to.
 */
public class NostrumTutorialClient {
	
	public enum ClientTutorial {
	
		CAST_SPELL(NostrumTutorial.CAST_SPELL, TEXT_FIRSTCAST_TITLE, TEXT_FIRSTCAST_TEXT1, TutorialToast.Icons.MOVEMENT_KEYS),
		FORM_INCANTATION(NostrumTutorial.FORM_INCANTATION, TEXT_FORMINCANT_TITLE, TEXT_FORMINCANT_TEXT1, TutorialToast.Icons.MOUSE),
		QUICK_INCANT(NostrumTutorial.QUICK_INCANT, TEXT_QUICKINCANT_TITLE, TEXT_QUICKINCANT_TEXT1, TutorialToast.Icons.MOVEMENT_KEYS),
		OVERCHARGE(NostrumTutorial.OVERCHARGE, TEXT_OVERCHARGE_TITLE, TEXT_OVERCHARGE_TEXT1, TutorialToast.Icons.MOVEMENT_KEYS),
		;
		
		private final NostrumTutorial base;
		private final Component title;
		private final Component text;
		private final TutorialToast.Icons icon;
		
		private ClientTutorial(NostrumTutorial base, Component title, Component text, TutorialToast.Icons icon) {
			this.base = base;
			this.title = title;
			this.text = text;
			this.icon = icon;
		}
		
		public NostrumTutorial getTutorialType() {
			return base;
		}
		
	}
	
	private final static Map<NostrumTutorial, ClientTutorial> ClientLookup = new EnumMap<>(NostrumTutorial.class);
	
	{
		for (ClientTutorial ctut : ClientTutorial.values()) {
			ClientLookup.put(ctut.getTutorialType(), ctut);
		}
	}
	
	private static final Component TEXT_FIRSTCAST_TITLE = new TranslatableComponent("tutorial.nostrummagica.firstcast");
	private static final Component TEXT_FIRSTCAST_TEXT1 = new TranslatableComponent("tutorial.nostrummagica.firstcast.text1", new KeybindComponent("key.castslow.desc"));
	private static final Component TEXT_FIRSTCAST_TEXT2 = new TranslatableComponent("tutorial.nostrummagica.firstcast.text2");
	
	private static final Component TEXT_FORMINCANT_TITLE = new TranslatableComponent("tutorial.nostrummagica.form_incant");
	private static final Component TEXT_FORMINCANT_TEXT1 = new TranslatableComponent("tutorial.nostrummagica.form_incant.text1", new KeybindComponent("key.castslow.desc"));
	private static final Component TEXT_FORMINCANT_TEXT2 = new TranslatableComponent("tutorial.nostrummagica.form_incant.text2");
	
	private static final Component TEXT_QUICKINCANT_TITLE = new TranslatableComponent("tutorial.nostrummagica.quick_incant");
	private static final Component TEXT_QUICKINCANT_TEXT1 = new TranslatableComponent("tutorial.nostrummagica.quick_incant.text", new KeybindComponent("key.castslow.desc"));
	
	private static final Component TEXT_OVERCHARGE_TITLE = new TranslatableComponent("tutorial.nostrummagica.overcharge");
	private static final Component TEXT_OVERCHARGE_TEXT1 = new TranslatableComponent("tutorial.nostrummagica.overcharge.text1", new KeybindComponent("key.castslow.desc"));
	private static final Component TEXT_OVERCHARGE_TEXT2 = new TranslatableComponent("tutorial.nostrummagica.overcharge.text2", new KeybindComponent("key.castslow.desc"));
	private static final Component TEXT_OVERCHARGE_TEXT3 = new TranslatableComponent("tutorial.nostrummagica.overcharge.text3");

	protected @Nullable ClientTutorial activeTutorial; 
	protected @Nullable TutorialToast activeToast;
	private int stageIdx; // state with meaning depending on which tutorial it's for
	
	private final Minecraft mc;
	
	public NostrumTutorialClient() {
		this.mc = Minecraft.getInstance();
	}
	
	public @Nullable ClientTutorial getActiveTutorial() {
		return this.activeTutorial;
	}
	
	public void setTutorial(@Nullable NostrumTutorial tutorial) {
		this.setTutorial(tutorial == null ? null : ClientLookup.get(tutorial));
	}
	
	public void setTutorial(@Nullable ClientTutorial tutorial) {
		if (tutorial == this.activeTutorial) {
			return;
		}
		
		if (this.activeTutorial != null) {
			onTutorialEnd(this.activeTutorial);
		}
		this.activeTutorial = tutorial;
		if (this.activeTutorial != null) {
			onTutorialStart(this.activeTutorial);
		}
	}
	
	// Tutorial events
	public void onStarterIncantationCast() {
		if (this.activeTutorial == ClientTutorial.CAST_SPELL && this.stageIdx == 0) {
			this.updateToastProgress(.5f);
			this.updateToastMessage(TEXT_FIRSTCAST_TEXT2);
			this.stageIdx = 1;
		}
	}
	
	public void onIncantationFormStarted() {
		if (this.activeTutorial == ClientTutorial.FORM_INCANTATION && this.stageIdx == 0) {
			this.updateToastProgress(.5f);
			this.updateToastMessage(TEXT_FORMINCANT_TEXT2);
			this.stageIdx = 1;
		}
	}
	
	public void onIncantationFormed() {
		if (this.activeTutorial == ClientTutorial.FORM_INCANTATION && this.stageIdx == 1) {
			this.updateToastProgress(1f);
			this.stageIdx = 2;
			
			//this.setTutorial(null);
			// For now chain these two together
			this.setTutorial(ClientTutorial.QUICK_INCANT);
			return;
		}
		
		if (this.activeTutorial == ClientTutorial.OVERCHARGE && this.stageIdx == 0) {
			this.updateToastProgress(1f/3f);
			this.updateToastMessage(TEXT_OVERCHARGE_TEXT2);
			this.stageIdx = 1;
			return;
		}
	}
	
	public void onIncantationFormAborted() {
		if (this.activeTutorial == ClientTutorial.FORM_INCANTATION && this.stageIdx == 1) {
			// Go back
			this.updateToastProgress(0f);
			this.updateToastMessage(TEXT_FORMINCANT_TEXT1);
			this.stageIdx = 0;
		}
	}
	
	public void onIncantationCastFinished() {
		if (this.activeTutorial == ClientTutorial.CAST_SPELL && this.stageIdx == 1) {
			this.updateToastProgress(1f);
			this.stageIdx = 2;
			this.setTutorial((ClientTutorial) null);
			return;
		}
		
		if (this.activeTutorial == ClientTutorial.OVERCHARGE && this.stageIdx == 1) {
			// Did not hold it
			this.updateToastProgress(0f/3f);
			this.updateToastMessage(TEXT_OVERCHARGE_TEXT1);
			this.stageIdx = 0;
			return;
		}
		
		if (this.activeTutorial == ClientTutorial.OVERCHARGE && this.stageIdx == 2) {
			this.updateToastProgress(1f);
			this.stageIdx = 3;
			this.setTutorial((ClientTutorial) null);
			return;
		}
	}
	
	public void onQuickIncant() {
		if (this.activeTutorial == ClientTutorial.QUICK_INCANT && this.stageIdx == 0) {
			this.updateToastProgress(1f);
			this.stageIdx = 1;
			this.setTutorial((ClientTutorial) null);
			return;
		}
		
		if (this.activeTutorial == ClientTutorial.OVERCHARGE && this.stageIdx == 0) {
			this.updateToastProgress(1f/3f);
			this.updateToastMessage(TEXT_OVERCHARGE_TEXT2);
			this.stageIdx = 1;
			return;
		}
	}
	
	public void onChargeCancel() {
		if (this.activeTutorial == ClientTutorial.CAST_SPELL && this.stageIdx == 1) {
			// GO BACK
			this.updateToastProgress(0f);
			this.updateToastMessage(TEXT_FIRSTCAST_TEXT1);
			this.stageIdx = 0;
			return;
		}
		
		if (this.activeTutorial == ClientTutorial.OVERCHARGE && (this.stageIdx == 1 || this.stageIdx == 2)) {
			// Did not hold it
			this.updateToastProgress(0f/3f);
			this.updateToastMessage(TEXT_OVERCHARGE_TEXT1);
			this.stageIdx = 0;
			return;
		}
	}
	
	public void onOverchargeStage() {
		if (this.activeTutorial == ClientTutorial.OVERCHARGE && this.stageIdx == 1) {
			this.updateToastProgress(2f/3f);
			this.updateToastMessage(TEXT_OVERCHARGE_TEXT3);
			this.stageIdx = 2;
			return;
		}
	}
	
	
	// Internal helpers
	protected void onTutorialStart(@Nonnull ClientTutorial tutorial) {
		this.activeToast = makeToast(tutorial);
		mc.getToasts().addToast(this.activeToast);
		stageIdx = 0;
	}
	
	protected void onTutorialEnd(@Nonnull ClientTutorial tutorial) {
		if (this.activeToast != null) {
			this.activeToast.hide();
			this.activeToast = null;
		}
	}
	
	protected TutorialToast makeToast(@Nonnull ClientTutorial tutorial) {
		return new TutorialToast(tutorial.icon, tutorial.title, tutorial.text, true);
	}
	
	protected void updateToastProgress(float progress) {
		if (this.activeToast != null) {
			this.activeToast.updateProgress(progress);
		}
	}
	
	protected void updateToastMessage(Component message) {
		if (this.activeToast != null) {
			this.activeToast.message = message;
		}
	}
	
}
