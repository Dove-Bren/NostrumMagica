package com.smanzana.nostrummagica.client.listener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
public class NostrumTutorial {

	public static enum Tutorial {
		CAST_SPELL(TEXT_FIRSTCAST_TITLE, TEXT_FIRSTCAST_TEXT1, TutorialToast.Icons.MOVEMENT_KEYS),
		FORM_INCANTATION(TEXT_FORMINCANT_TITLE, TEXT_FORMINCANT_TEXT1, TutorialToast.Icons.MOUSE),
		QUICK_INCANT(TEXT_QUICKINCANT_TITLE, TEXT_QUICKINCANT_TEXT1, TutorialToast.Icons.MOVEMENT_KEYS),
		;
		
		private final Component title;
		private final Component text;
		private final TutorialToast.Icons icon;
		
		private Tutorial(Component title, Component text, TutorialToast.Icons icon) {
			this.title = title;
			this.text = text;
			this.icon = icon;
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
	
	protected @Nullable Tutorial activeTutorial; 
	protected @Nullable TutorialToast activeToast;
	private int stageIdx; // state with meaning depending on which tutorial it's for
	
	private final Minecraft mc;
	
	public NostrumTutorial() {
		this.mc = Minecraft.getInstance();
	}
	
	public @Nullable Tutorial getActiveTutorial() {
		return this.activeTutorial;
	}
	
	public void setTutorial(@Nullable Tutorial tutorial) {
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
		if (this.activeTutorial == Tutorial.CAST_SPELL && this.stageIdx == 0) {
			this.updateToastProgress(.5f);
			this.updateToastMessage(TEXT_FIRSTCAST_TEXT2);
			this.stageIdx = 1;
		}
	}
	
	public void onIncantationFormStarted() {
		if (this.activeTutorial == Tutorial.FORM_INCANTATION && this.stageIdx == 0) {
			this.updateToastProgress(.5f);
			this.updateToastMessage(TEXT_FORMINCANT_TEXT2);
			this.stageIdx = 1;
		}
	}
	
	public void onIncantationFormed() {
		if (this.activeTutorial == Tutorial.FORM_INCANTATION && this.stageIdx == 1) {
			this.updateToastProgress(1f);
			this.stageIdx = 2;
			
			//this.setTutorial(null);
			// For now chain these two together
			this.setTutorial(Tutorial.QUICK_INCANT);
		}
	}
	
	public void onIncantationFormAborted() {
		if (this.activeTutorial == Tutorial.FORM_INCANTATION && this.stageIdx == 1) {
			// Go back
			this.updateToastProgress(0f);
			this.updateToastMessage(TEXT_FORMINCANT_TEXT1);
			this.stageIdx = 0;
		}
	}
	
	public void onIncantationCastFinished() {
		if (this.activeTutorial == Tutorial.CAST_SPELL && this.stageIdx == 1) {
			this.updateToastProgress(1f);
			this.stageIdx = 2;
			this.setTutorial(null);
		}
	}
	
	public void onQuickIncant() {
		if (this.activeTutorial == Tutorial.QUICK_INCANT && this.stageIdx == 0) {
			this.updateToastProgress(1f);
			this.stageIdx = 1;
			this.setTutorial(null);
		}
	}
	
	public void onChargeCancel() {
		if (this.activeTutorial == Tutorial.CAST_SPELL && this.stageIdx == 1) {
			// GO BACK
			this.updateToastProgress(0f);
			this.updateToastMessage(TEXT_FIRSTCAST_TEXT1);
			this.stageIdx = 0;
		}
	}
	
	
	// Internal helpers
	protected void onTutorialStart(@Nonnull Tutorial tutorial) {
		this.activeToast = makeToast(tutorial);
		mc.getToasts().addToast(this.activeToast);
		stageIdx = 0;
	}
	
	protected void onTutorialEnd(@Nonnull Tutorial tutorial) {
		if (this.activeToast != null) {
			this.activeToast.hide();
			this.activeToast = null;
		}
	}
	
	protected TutorialToast makeToast(@Nonnull Tutorial tutorial) {
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
