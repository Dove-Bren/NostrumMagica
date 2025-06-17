package com.smanzana.nostrummagica.client.overlay;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.EMagicTier;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.SpellComponentIcon;
import com.smanzana.nostrummagica.client.gui.commonwidget.ITooltip;
import com.smanzana.nostrummagica.client.gui.commonwidget.Tooltip;
import com.smanzana.nostrummagica.client.listener.ClientPlayerListener;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EElementalMastery;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Incantation;
import com.smanzana.nostrummagica.spell.component.shapes.NostrumSpellShapes;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.InputEvent.RawMouseEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Renders an overlay to screen (when enabled) that allows players to use their mouse to select
 * incantation components
 */
public class IncantSelectionOverlay implements IIngameOverlay {
	
	private static final double FADE_MS = 300 - 200;//QUICKPRESS_MS;
	private static final double QUICKPRESS_MS = 200;
	
	private static final Component noneTitle = new TextComponent("None");
	private static final Component nextTitle = new TextComponent("Next");
	private static final ITooltip nextTooltip = Tooltip.create(new TextComponent("View the next page of shapes"));
	
	private static final Component prevTitle = new TextComponent("Previous");
	private static final ITooltip prevTooltip = Tooltip.create(new TextComponent("View the previous page of shapes"));
	private static final ITooltip noneTooltip = Tooltip.create(new TextComponent("Do not use an alteration, manifesting a raw representation of the element"));
	private static final ITooltip noneShapeTooltip = Tooltip.create(new TextComponent("Do not use a second shape"));
	private static final Component terminalText = new TextComponent("Terminal").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_PURPLE);
	
	protected boolean enabled;
	protected long showTime; // For behavior like quick-press
	protected long fadeTime; // for animating
	
	// Selection variables
	private @Nullable SpellShape shape;
	private @Nullable SpellShape shape2;
	private @Nullable EMagicElement element;
	private @Nullable EAlteration alteration;
	
	private @Nullable Incantation lastIncantation;
	
	private final List<SelectionStage> stages;
	
//	private final SelectionStage elementStage;
//	private final SelectionStage shapeStage1;
//	private final SelectionStage shapeStage2;
//	private final SelectionStage alterationStage;
	
	private int currentStageIdx = -1;
	
	
	// Tooltip stuff
	private @Nullable WheelSlice<?> hovered;
	private long hoverTime;
	
	private final Minecraft mc;
	
	public IncantSelectionOverlay() {
		MinecraftForge.EVENT_BUS.register(this);
		this.mc = Minecraft.getInstance();
		
		this.stages = new ArrayList<>();
	}
	
	// Should these be here?
	protected boolean hasFullShapeSelection(INostrumMagic attr) {
		// With current unlock order, this is as soon as you unlock any 4th shape which will mean
		// it's just the same as shape.size() > 3. Room for progression by expanding it but then players
		// might not be able to try out their new shape.
		return attr.getTier().isGreaterOrEqual(EMagicTier.KANI) && attr.getShapes().size() > 3;
	}
	
	protected boolean hasDoubleShapeSelection(INostrumMagic attr) {
		return attr.getTier().isGreaterOrEqual(EMagicTier.VANI) && hasFullShapeSelection(attr);
	}
	
	protected boolean hasAlterationSelection(INostrumMagic attr) {
		return attr.getTier().isGreaterOrEqual(EMagicTier.MANI) && attr.getAlterations() != null && attr.getAlterations().containsValue(Boolean.TRUE);
	}
	
	protected boolean isEnabled() {
		return enabled;
	}
	
	protected void onEnable() {
		initSlices();
		
		this.showTime = System.currentTimeMillis();
		resetFadeTimer();
		mc.mouseHandler.releaseMouse();
	}
	
	protected void onDisable() {
		resetFadeTimer();
//		resetSelection(); dont reset here; wait for fade out
		mc.mouseHandler.grabMouse();
		this.showTime = 0;
	}
	
	public void enableSelection(boolean enabled) {
		final boolean wasEnabled = isEnabled();
		this.enabled = enabled;
		
		if (wasEnabled != enabled) {
			if (enabled) {
				onEnable();
			} else {
				onDisable();
			}
		}
	}
	
	protected boolean isQuickPressTime() {
		return System.currentTimeMillis() - this.showTime < QUICKPRESS_MS;
	}
	
	protected void resetFadeTimer() {
		// Use any previous in-progress fade to smooth this out
		final long diff = System.currentTimeMillis() - fadeTime;
		if (diff < FADE_MS) {
			fadeTime = System.currentTimeMillis() - (long)(FADE_MS - diff);
		} else {
			fadeTime = System.currentTimeMillis() + (long)QUICKPRESS_MS;
		}
	}
	
	protected void resetSelection() {
		element = null;
		shape = null;
		shape2 = null;
		alteration = null;
	}
	
	protected void submitSelection() {
		ClientPlayerListener listener = (ClientPlayerListener) NostrumMagica.playerListener;
		this.lastIncantation = new Incantation(this.shape, this.shape2, this.element, this.alteration);
		listener.startIncantationCast(this.lastIncantation);
		this.enableSelection(false);
		listener.getTutorial().onIncantationFormed();
	}
	
	protected void onKeyRelease() {
		ClientPlayerListener listener = (ClientPlayerListener) NostrumMagica.playerListener;
		if (isQuickPressTime() && this.lastIncantation != null) {
			// Recast previous incantation
			listener.startIncantationCast(this.lastIncantation);
			listener.getTutorial().onQuickIncant();
		} else {
			listener.getTutorial().onIncantationFormAborted();
		}
		
		this.enableSelection(false);
	}
	
	protected @Nullable SelectionStage getCurrentStage() {
		return this.currentStageIdx >= 0 && currentStageIdx < stages.size()
				? stages.get(currentStageIdx)
				: null;
	}
	
	protected @Nullable SelectionStagePage getCurrentPage() {
		SelectionStage stage = getCurrentStage();
		if (stage != null) {
			return stage.pages[stage.pageIdx];
		}
		return null;
	}
	
	protected void advanceStage() {
		advanceStage(false);
	}
	
	protected void advanceStage(boolean skipNext) {
		this.currentStageIdx += (skipNext ? 2 : 1);
		if (currentStageIdx >= this.stages.size()) {
			this.submitSelection();
		}
	}
	
	protected void resetStage() {
		this.currentStageIdx = 0;
	}
	
	protected @Nullable WheelSlice<?> getSelection(int width, int height, int mouseX, int mouseY) {
		if (!this.isEnabled()) {
			return null;
		}
		
		@Nullable SelectionStagePage stage = this.getCurrentPage();
		if (stage == null) {
			return null;
		}
		
		final int mouseOffsetX = mouseX - (width/2);
		final int mouseOffsetY = mouseY - (height/2);
		
		final double angleRad = (Mth.atan2(mouseOffsetY, mouseOffsetX) + (Math.PI * 2)) % (Math.PI * 2);
		final float anglePerc = (float) (angleRad / (Math.PI * 2));
		final float dist = Mth.sqrt(mouseOffsetX * mouseOffsetX + mouseOffsetY * mouseOffsetY);
		
		if (dist < 30) {
			return null;
		}
		
		for (WheelSlice<?> slice : stage.slices) {
			if (slice == null) {
				continue;
			}
			
			final float angleDiff = Math.min(Math.abs(anglePerc - slice.rotationPerc()), Math.abs(anglePerc - (1f+slice.rotationPerc())));
			if (angleDiff < slice.width()) {
				return slice;
			}
		}
		
		return null; // can this happen? Should return last slice?
	}
	
	protected SelectionStage makeElementStage(Player player, @Nullable INostrumMagic attr) {
		// elements are even split of the circle
		final int count = EMagicElement.values().length;
		WheelSlice<?>[] elementSlices = new WheelSlice[count];
		
		final float perSlice = (1f / (float) count);
		for (int i = 0; i < count; i++) {
			final float prog = (.75f + (i * perSlice)) % 1; // start at 75% around which is up
			final EMagicElement elem = EMagicElement.values()[i];
			
			if (attr == null || !attr.isUnlocked() || !attr.getElementalMastery(elem).isGreaterOrEqual(EElementalMastery.NOVICE)) {
				elementSlices[i] = WheelSlice.Hidden(prog, perSlice/2f);
			} else {
				elementSlices[i] = new WheelSlice<>(elem, SpellComponentIcon.get(elem), elem.getDisplayName(), () -> this.getElementTooltip(elem), prog, perSlice/2f, this::setElement, false);
			}
		}
		return new SelectionStage().setPages(new SelectionStagePage(elementSlices));
	}
	
	protected SelectionStage makeAlterationStage(Player player, @Nullable INostrumMagic attr) {
		// Alterations are even, with "NO ALTERATION" being on top
		
		final int count = EAlteration.values().length + 1;
		WheelSlice<?>[] alterationSlices = new WheelSlice[count];
		final Predicate<EAlteration> check = (a) -> attr != null && attr.isUnlocked() && (a == null || attr.getAlterations().getOrDefault(a, Boolean.FALSE));
		
		final float perSlice = (1f / (float) count);
		for (int i = 0; i < count; i++) {
			final float prog = (.75f + (i * perSlice)) % 1; // start at 75% around which is up
			final EAlteration alter = i == 0 ? null : EAlteration.values()[i-1];
			
			if (!check.test(alter)) {
				alterationSlices[i] = WheelSlice.Hidden(prog, perSlice/2f);
			} else {
				alterationSlices[i] = new WheelSlice<>(alter, alter == null ? null : SpellComponentIcon.get(alter),
						alter == null ? noneTitle : alter.getDisplayName(),
						() -> getAlterationTooltip(alter),		
						prog, perSlice/2f, this::setAlteration, false);
			}
		}
		
		return new SelectionStage().setPages(new SelectionStagePage(alterationSlices));
	}
	
	protected SelectionStage makeFullShapeStage(Player player, @Nullable INostrumMagic attr, boolean isSecondStage) {
		// Shapes are mostly even, with a few called out specifically.
		final SpellShape[] specials = {NostrumSpellShapes.Projectile, NostrumSpellShapes.Touch, NostrumSpellShapes.Self};
		
		// Specials are bigger than the others
		final float specialWidth = .1125f;
		
		// TODO fix holding down R. Require a key-up to re-show menu.
		
		Comparator<SpellShape> compare = (a, b) -> {
			final int aIdx = (a.getWeight(a.getDefaultProperties())) * 100
					//+ (a.getManaCost(a.getDefaultProperties())) * 10
					;
			final int bIdx = (b.getWeight(a.getDefaultProperties())) * 100
					//+ (b.getManaCost(a.getDefaultProperties())) * 10
					;
			
			if (aIdx != bIdx) {
				return aIdx - bIdx;
			}
			
			// If all equal, sort by name?
			return a.getShapeKey().compareToIgnoreCase(b.getShapeKey());
		};
		
		List<SpellShape> shapes = Lists.newArrayList(specials);
		Set<SpellShape> seen = Sets.newHashSet(specials);
		SpellShape.getAllShapes().stream().filter(SpellShape::canIncant).filter(seen::add).sorted(compare).forEach(shapes::add);
		Set<SpellShape> known = (attr != null && attr.isUnlocked()) ? Set.copyOf(attr.getShapes()) : new HashSet<>();
		
		final int countPerPage = 11;
		final int count = shapes.size();
		final int specialPerPage = specials.length + 2;
		final int standardPerPage = countPerPage - specialPerPage;
		final int numPages = (((count-specials.length) + standardPerPage-1) / standardPerPage);
		SelectionStagePage[] shapePages = new SelectionStagePage[numPages];
		
		final float standardWidth = ((1f - (specialWidth * specials.length)) / (float) (countPerPage-specials.length));
		
		final int buttonsIdx = (((countPerPage - specials.length)-1) / 2) + specials.length;
		
		for (int page = 0; page < numPages; page++) {
			WheelSlice<?>[] curSlices = new WheelSlice[countPerPage];
			for (int i = 0; i < countPerPage; i++) {
				final SpellShape shape;
				final float prog;
				final float sliceWidth;
				// note that 75% around is up
				if (i < specials.length) {
					final float progStart = (.75f + (-specialWidth * ((specials.length-1)/2f)));
					prog = (progStart + (i * specialWidth)) % 1;
					sliceWidth = specialWidth;
					shape = shapes.get(i);
				} else {
					final int subi = (i-specials.length);
					final float progStart = (.75f + (specialWidth*specials.length) / 2f) + (standardWidth / 2); 
					prog = (progStart + (subi * standardWidth)) % 1;
					sliceWidth = standardWidth;
					
					if (i < buttonsIdx || i > buttonsIdx + 1) {
						int shapeIdx = specials.length + (standardPerPage)*page + subi;
						if (i > buttonsIdx) {
							shapeIdx -= 2;
						}
						shape = shapeIdx < shapes.size() ? shapes.get(shapeIdx) : null;
					} else {
						shape = null;
					}
				}
				
				if (i == buttonsIdx && page < numPages - 1) {
					// next button
					curSlices[i] = new WheelSlice<>(Boolean.TRUE, null, nextTitle, nextTooltip, prog, sliceWidth/2f, this::movePage, true);
				} else if (i == buttonsIdx + 1 && page > 0) {
					// prev button
					curSlices[i] = new WheelSlice<>(Boolean.FALSE, null, prevTitle, prevTooltip, prog, sliceWidth/2f, this::movePage, true);
				} else if (shape == null) {
					curSlices[i] = null; // no slice
				} else if (!known.contains(shape)) {
					curSlices[i] = WheelSlice.Hidden(prog, sliceWidth/2f);
				} else if (isSecondStage && shape == this.shape) { // shape selected in first stage
					curSlices[i] = new WheelSlice<>((SpellShape) shape, null, noneTitle, noneShapeTooltip, prog, sliceWidth/2f, isSecondStage ? this::setSecondShape : this::setFirstShape, true);
				} else {
					curSlices[i] = new WheelSlice<>(shape, SpellComponentIcon.get(shape), shape.getDisplayName(), () -> this.getShapeTooltip(shape), prog, sliceWidth/2f, isSecondStage ? this::setSecondShape : this::setFirstShape, i < specials.length);
				}
			}
			shapePages[page] = new SelectionStagePage(curSlices);
		}
		return new SelectionStage().setPages(shapePages);
	}
	
	protected SelectionStage makePrimaryShapeStage(Player player, @Nullable INostrumMagic attr, boolean isSecondStage) {
		// Primary Shapes!
		final SpellShape[] primaries = {NostrumSpellShapes.Projectile, NostrumSpellShapes.Touch, NostrumSpellShapes.Self};
		Set<SpellShape> known = (attr != null && attr.isUnlocked()) ? Set.copyOf(attr.getShapes()) : new HashSet<>();
		WheelSlice<?>[] slices = new WheelSlice[primaries.length];
		
		final float perSlice = (1f / (float) primaries.length);
		for (int i = 0; i < primaries.length; i++) {
			final float prog = (.75f + (i * perSlice)) % 1; // start at 75% around which is up
			final SpellShape shape = primaries[i];
			
			if (!known.contains(shape)) {
				slices[i] = WheelSlice.Hidden(prog, perSlice/2f);
			} else if (isSecondStage && shape == this.shape) { // shape selected in first stage
				slices[i] = new WheelSlice<>((SpellShape) shape, null, noneTitle, noneShapeTooltip, prog, perSlice/2f, isSecondStage ? this::setSecondShape : this::setFirstShape, false);
			} else {
				slices[i] = new WheelSlice<>(shape, SpellComponentIcon.get(shape), shape.getDisplayName(), () -> this.getShapeTooltip(shape), prog, perSlice/2f, isSecondStage ? this::setSecondShape : this::setFirstShape, false);
			}
		}
		
		return new SelectionStage().setPages(new SelectionStagePage(slices));
	}
	
	protected void initSlices() {
		// Remake slices each time to allow attr to change
		Player player = NostrumMagica.Proxy.getPlayer();
		final @Nullable INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		
		stages.clear();
		
		// First stage is shapes
		if (hasFullShapeSelection(attr)) {
			stages.add(makeFullShapeStage(player, attr, false));
		} else {
			stages.add(makePrimaryShapeStage(player, attr, false));
		}
		if (hasDoubleShapeSelection(attr)) {
			if (hasFullShapeSelection(attr)) {
				stages.add(makeFullShapeStage(player, attr, true));
			} else {
				stages.add(makePrimaryShapeStage(player, attr, true));
			}
		}
		
		// elements are second
		stages.add(makeElementStage(player, attr));
		
//		// Shapes
//		this.shapeStage1.setPages(this.makeShapePages(player, attr, false));
//		this.shapeStage2.setPages(this.makeShapePages(player, attr, true));
		
		// Alterations last
		if (hasAlterationSelection(attr)) {
			stages.add(makeAlterationStage(player, attr));
		}
		
		this.resetStage();
	}
	
	protected void setElement(EMagicElement element, boolean isRight) {
		this.element = element;
		
		// If no alterations are discovered, submit now
		Player player = NostrumMagica.Proxy.getPlayer();
		final @Nullable INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		final boolean alterPickAllowed = attr != null
				&& attr.getAlterations().values().stream().filter(Objects::nonNull).anyMatch(Boolean::booleanValue)
				// TODO and tier/skill check
				;
		if (isRight || !alterPickAllowed) {
			this.advanceStage(true);
		} else {
			this.advanceStage();
		}
	}
	
	protected void setAlteration(EAlteration alteration, boolean isRight) {
		this.alteration = alteration;
		this.advanceStage();
	}
	
	protected void setFirstShape(SpellShape shape, boolean isRight) {
		// This used to do the work to figure out if it should show the second shape stage, but now that's all determined
		// when creating stages. This just needs to advance.
		this.shape = shape;
		this.advanceStage(isRight);
	}
	
	protected void setSecondShape(SpellShape shape, boolean isRight) {
		// This used to do the work to figure out if it should show the second shape stage, but now that's all determined
		// when creating stages. This just needs to advance.
		this.shape2 = shape;
		this.advanceStage(false); // can't skip element
	}
	
	protected List<Component> getShapeTooltip(SpellShape shape) {
		final int mana = shape.getManaCost(shape.getDefaultProperties());
		final int weight = shape.getWeight(shape.getDefaultProperties());
		
		List<Component> tooltip = new ArrayList<>(shape.getTooltip());
		tooltip.add(new TextComponent(" "));
		if (shape.getAttributes(shape.getDefaultProperties()).terminal) {
			tooltip.add(terminalText);
		}
		tooltip.add(makeManaLine(mana));
		tooltip.add(makeWeightLine(weight));
		return tooltip;
	}
	
	protected List<Component> getElementTooltip(EMagicElement element) {
		return element.getTooltip();
	}
	
	protected List<Component> getAlterationTooltip(@Nullable EAlteration alteration) {
		if (alteration == null) {
			return noneTooltip.get();
		}
		
		final int mana = alteration.getCost();
		final int weight = alteration.getWeight();
		
		List<Component> tooltip = new ArrayList<>(alteration.getTooltip());
		tooltip.add(new TextComponent(" "));
		tooltip.add(makeManaLine(mana));
		tooltip.add(makeWeightLine(weight));
		return tooltip;
	}
	
	protected Component makeManaLine(int mana) {
		return new TextComponent(mana + "").append(new TextComponent(" Mana").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
	}
	
	protected Component makeWeightLine(int weight) {
		return new TextComponent(weight + "").append(new TextComponent(" Weight").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
	}
	
	protected void movePage(Boolean isNext, Boolean isRight) {
		final @Nullable SelectionStage stage = this.getCurrentStage();
		if (stage != null) {
			stage.movePage(isNext);
		}
	}
	
	protected float getFadeProgress() {
		final long diff = Math.max(0, System.currentTimeMillis() - fadeTime);
		final float progRaw = (float)((double) diff / FADE_MS);
		final float prog = (isEnabled() ? progRaw : 1f - progRaw);
		return Mth.clamp(prog, 0f, 1f);
	}
	
	@SubscribeEvent
	public void onMouseRaw(RawMouseEvent event) {
		if (isEnabled() && event.getAction() == GLFW.GLFW_PRESS) {
			event.setCanceled(true);
			final boolean isLeft = event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT;
			final boolean isRight = event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
			if (isLeft || isRight) {
				final int width = mc.getWindow().getGuiScaledWidth();
				final int height = mc.getWindow().getGuiScaledHeight();
				final double wScale = (double) width/ (double)mc.getWindow().getScreenWidth();
				final double hScale = (double) height / (double)mc.getWindow().getScreenHeight();
				
				final int mouseX = (int)(mc.mouseHandler.xpos() * wScale);
				final int mouseY = (int)(mc.mouseHandler.ypos() * hScale);
				
				final @Nullable WheelSlice<?> current = this.getSelection(width, height, mouseX, mouseY);
				if (current != null) {
					current.click(isRight);
				}
				
			}
		}
	}

	public void render(ForgeIngameGui gui, PoseStack matrixStackIn, float partialTicks, int width, int height) {
		ClientPlayerListener listener = (ClientPlayerListener) NostrumMagica.playerListener;
		if (/*this.isEnabled() || */this.getFadeProgress() > 0f) {
			final float fade = this.getFadeProgress();
			Player player = NostrumMagica.Proxy.getPlayer();
			final @Nullable INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr != null) {
				final float radius = 90f;
				
				final double wScale = (double)mc.getWindow().getGuiScaledWidth() / (double)mc.getWindow().getScreenWidth();
				final double hScale = (double)mc.getWindow().getGuiScaledHeight() / (double)mc.getWindow().getScreenHeight();
				
				final int mouseX = (int)(mc.mouseHandler.xpos() * wScale);
				final int mouseY = (int)(mc.mouseHandler.ypos() * hScale);
			
				matrixStackIn.pushPose();
				matrixStackIn.translate(width/2, (height/2), 10);
				renderWheel(matrixStackIn, partialTicks, radius, fade, width, height, mouseX, mouseY);
				matrixStackIn.popPose();
				
				final WheelSlice<?> selected = this.getSelection(width, height, mouseX, mouseY);
				if (selected != this.hovered) {
					this.hovered = selected;
					this.hoverTime = System.currentTimeMillis();
				} else if (this.hovered != null && System.currentTimeMillis() - this.hoverTime > 1000) {
					this.renderWheelSliceTooltip(this.hovered, matrixStackIn, partialTicks, mouseX, mouseY);
				}
			}
		}
		
		// This will also be what turns itself off
		if (this.isEnabled() && !listener.getBindingIncant().isDown()) {
			onKeyRelease();
		}
		
		if (!this.isEnabled() && this.getFadeProgress() <= 0f) {
			resetSelection();
		}
	}
	
	protected void renderWheel(PoseStack matrixStackIn, float partialTicks, float radius, float fadeAlpha, int screenwidth, int screenheight, int mouseX, int mouseY) {
		renderWheelBackground(matrixStackIn, partialTicks, radius, fadeAlpha);
		
		SelectionStagePage stage = this.getCurrentPage();
		final WheelSlice<?> selected = this.getSelection(screenwidth, screenheight, mouseX, mouseY);
		if (stage != null) {
			for (WheelSlice<?> slice : stage.slices) {
				if (slice == null) {
					continue;
				}
				renderWheelSlice(slice, matrixStackIn, partialTicks, radius, fadeAlpha, selected == slice);
			}
		}
		
		renderWheelForeground(matrixStackIn, partialTicks, radius, fadeAlpha);
	}
	
	protected void renderWheelBackground(PoseStack matrixStackIn, float partialTicks, float radius, float fadeAlpha) {
		
		matrixStackIn.pushPose();
		BufferBuilder buffer = Tesselator.getInstance().getBuilder();
		buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.disableTexture();
		RenderSystem.disableCull();
		
		RenderFuncs.drawEllipse(radius, radius, 32, matrixStackIn, buffer, 0, .7f, .3f, .7f, fadeAlpha * .7f);
		
		Tesselator.getInstance().end();
		
		RenderSystem.enableCull();
		matrixStackIn.popPose();
	}
	
	protected void renderWheelForeground(PoseStack matrixStackIn, float partialTicks, float radius, float fadeAlpha) {
		matrixStackIn.pushPose();
		BufferBuilder buffer = Tesselator.getInstance().getBuilder();
		buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.disableTexture();
		RenderSystem.disableCull();
		RenderSystem.enableBlend();
		
		RenderFuncs.drawEllipse(30, 30, 32, matrixStackIn, buffer, 0, .6f, .5f, .6f, fadeAlpha);
		
		Tesselator.getInstance().end();
		
		final boolean twoShapes = (this.shape != null && this.shape2 != null);
		if (this.shape != null) {
			matrixStackIn.pushPose();
			if (twoShapes) {
				matrixStackIn.translate(-12, -12, 0);
			} else {
				matrixStackIn.translate(-12, -3, 0);
			}
			SpellComponentIcon.get(this.shape).draw(matrixStackIn, -6, -6, 12, 12, 1f, 1f, 1f, fadeAlpha);
			matrixStackIn.translate(0, 7, 0);
			matrixStackIn.scale(.5f, .5f, 1f);
			final int len = mc.font.width(this.shape.getDisplayName());
			mc.font.draw(matrixStackIn, this.shape.getDisplayName(), -len/2, 0, RenderFuncs.ARGBFade(0xFFFFFFFF, fadeAlpha));
			matrixStackIn.popPose();
		}
		
		if (this.shape2 != null) {
			matrixStackIn.pushPose();
			matrixStackIn.translate(12, -12, 0);
			SpellComponentIcon.get(this.shape2).draw(matrixStackIn, -6, -6, 12, 12, 1f, 1f, 1f, fadeAlpha);
			matrixStackIn.translate(0, 7, 0);
			matrixStackIn.scale(.5f, .5f, 1f);
			final int len = mc.font.width(this.shape2.getDisplayName());
			mc.font.draw(matrixStackIn, this.shape2.getDisplayName(), -len/2, 0, RenderFuncs.ARGBFade(0xFFFFFFFF, fadeAlpha));
			matrixStackIn.popPose();
		}
		
		if (this.element != null) {
			matrixStackIn.pushPose();
			if (twoShapes) {
				matrixStackIn.translate(0, 12, 0);
			} else {
				matrixStackIn.translate(12, -3, 0);
			}
			SpellComponentIcon.get(this.element).draw(matrixStackIn, -6, -6, 12, 12, 1f, 1f, 1f, fadeAlpha);
			matrixStackIn.translate(0, 7, 0);
			matrixStackIn.scale(.5f, .5f, 1f);
			final int len = mc.font.width(this.element.getDisplayName());
			mc.font.draw(matrixStackIn, this.element.getDisplayName(), -len/2, 0, RenderFuncs.ARGBFade(0xFFFFFFFF, fadeAlpha));
			matrixStackIn.popPose();
		}
		
		RenderSystem.enableCull();
		matrixStackIn.popPose();
	}
	
	protected void renderWheelSliceTooltip(WheelSlice<?> slice, PoseStack matrixStackIn, float partialTicks, int mouseX, int mouseY) {
		final List<Component> components = slice.tooltip() == null ? null : slice.tooltip().get();
		if (components != null && !components.isEmpty()) {
			final int width = 150;
			int height = 20;
			
			RenderSystem.enableDepthTest();
			RenderSystem.depthMask(true);
			
			matrixStackIn.pushPose();
			matrixStackIn.translate(mouseX + 10, mouseY + -10, 400);
			
			if (mc.getWindow().getGuiScaledWidth() - mouseX < width + 5) {
				matrixStackIn.translate(-width + -10 + -20, 0, 0);
			}
			
			// Draw text first higher to figure out width/height
			{
				matrixStackIn.pushPose();
				matrixStackIn.translate(2, 2, 0);
				
				int y = 0;
				for (Component component : components) {
					for (FormattedCharSequence line : mc.font.split(component, width-4)) {
						mc.font.draw(matrixStackIn, line, 0, y, 0xFFC0C0C0);
						y += mc.font.lineHeight + (y == 0 ? 5 : 2);
					}
				}
				
				if (y > height) {
					height = y;
				}
				
				matrixStackIn.popPose();
			}
			
			RenderSystem.enableDepthTest();
			RenderSystem.depthMask(true);
			
			matrixStackIn.translate(0, 0, -1);
			RenderFuncs.drawRect(matrixStackIn, -1, -1, width+1, height+1, 0xFF000000);
			RenderFuncs.drawRect(matrixStackIn, 0, 0, width, height, 0xFF404040);
			
			matrixStackIn.popPose();
		}
	}
	
	protected void renderWheelSlice(WheelSlice<?> slice, PoseStack matrixStackIn, float partialTicks, float radius, float fadeAlpha, boolean highlight) {
		final float leftX = Mth.cos((slice.rotationPerc + slice.width()) * 2 * Mth.PI) * radius;
		final float leftY = Mth.sin((slice.rotationPerc + slice.width()) * 2 * Mth.PI) * radius;
		
		final float rightX = Mth.cos((slice.rotationPerc - slice.width()) * 2 * Mth.PI) * radius;
		final float rightY = Mth.sin((slice.rotationPerc - slice.width()) * 2 * Mth.PI) * radius;
		
		if (slice.decorate()) {
			final float sat = (0f);
			{
				Matrix4f transform = matrixStackIn.last().pose();
				
				final float leftSmallX = Mth.cos((slice.rotationPerc + slice.width()) * 2 * Mth.PI) * 29.5f;
				final float leftSmallY = Mth.sin((slice.rotationPerc + slice.width()) * 2 * Mth.PI) * 29.5f;
				
				final float midSmallX = Mth.cos((slice.rotationPerc) * 2 * Mth.PI) * 35f;
				final float midSmallY = Mth.sin((slice.rotationPerc) * 2 * Mth.PI) * 35f;
				
				final float rightSmallX = Mth.cos((slice.rotationPerc - slice.width()) * 2 * Mth.PI) * 29.5f;
				final float rightSmallY = Mth.sin((slice.rotationPerc - slice.width()) * 2 * Mth.PI) * 29.5f;
			
				RenderSystem.setShader(GameRenderer::getPositionColorShader);
				RenderSystem.disableTexture();
				RenderSystem.disableCull();
				RenderSystem.enableBlend();
				BufferBuilder buffer = Tesselator.getInstance().getBuilder();
				buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
				
				buffer.vertex(transform, midSmallX, midSmallY, 0).color(sat, sat, sat, 0).endVertex();
				buffer.vertex(transform, rightSmallX, rightSmallY, 0).color(sat, sat, sat, fadeAlpha).endVertex();
				buffer.vertex(transform, leftSmallX, leftSmallY, 0).color(sat, sat, sat, fadeAlpha).endVertex();
				
				Tesselator.getInstance().end();
				
				RenderSystem.enableCull();
			}
		}
		
		// Border lines
		{
			RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
			RenderSystem.disableCull();
			RenderSystem.enableBlend();
			BufferBuilder buffer = Tesselator.getInstance().getBuilder();
			buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
			
			RenderFuncs.renderLine(matrixStackIn, buffer, new Vec3(1f, 1f, 0), new Vec3(leftX, leftY, 0), 4, OverlayTexture.NO_OVERLAY, 0, 0f, 0f, 0f, fadeAlpha);
			RenderFuncs.renderLine(matrixStackIn, buffer, new Vec3(1f, 1f, 0), new Vec3(rightX, rightY, 0), 4, OverlayTexture.NO_OVERLAY, 0, 0f, 0f, 0f, fadeAlpha);
			
			Tesselator.getInstance().end();
			RenderSystem.enableCull();
		}
		
		matrixStackIn.pushPose();
		
		final float armRadius = radius * .75f;
		final float offsetX = Mth.cos(slice.rotationPerc * 2 * Mth.PI) * armRadius;
		final float offsetY = Mth.sin(slice.rotationPerc * 2 * Mth.PI) * armRadius;
		
		matrixStackIn.translate(offsetX, offsetY, 0);
		if (slice.isHidden()) {
			if (slice.name() != null) {
				matrixStackIn.pushPose();
				matrixStackIn.scale(.5f, .5f, .5f);
				final int len = mc.font.width(slice.name());
				mc.font.draw(matrixStackIn, slice.name(), -len/2, -mc.font.lineHeight / 2, RenderFuncs.ARGBFade(0xFFFFFFFF, fadeAlpha));
				matrixStackIn.popPose();
			}
		} else {
			matrixStackIn.translate(0, -5, 0);
			if (slice.icon() != null) {
				slice.icon().draw(matrixStackIn, -8, -8, 16, 16, 1f, 1f, 1f, fadeAlpha);
			}
			
			if (slice.name() != null) {
				matrixStackIn.pushPose();
				matrixStackIn.translate(0, 10, 0);
				matrixStackIn.scale(.5f, .5f, .5f);
				{
					RenderSystem.enableBlend();
					final int len = mc.font.width(slice.name());
					mc.font.draw(matrixStackIn, slice.name(), -len/2, 0, RenderFuncs.ARGBFade(0xFFFFFFFF, fadeAlpha));
//					int y = 0;
//					for (FormattedCharSequence line : mc.font.split(slice.name(), 40)) {
//						final int len = mc.font.width(line);
//						mc.font.draw(matrixStackIn, line, -len/2, y, 0xFFC0C0C0);
//						y += mc.font.lineHeight + (y == 0 ? 5 : 2);
//					}
				}
				matrixStackIn.popPose();
			}
		}
		
		matrixStackIn.popPose();
		
		if (highlight) {
			final float sat = (slice.isHidden() ? .3f : 1f);
			{
				Matrix4f transform = matrixStackIn.last().pose();
			
				RenderSystem.setShader(GameRenderer::getPositionColorShader);
				RenderSystem.disableTexture();
				RenderSystem.disableCull();
				RenderSystem.enableBlend();
				BufferBuilder buffer = Tesselator.getInstance().getBuilder();
				buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
				
				buffer.vertex(transform, 0, 0, 0).color(sat, sat, sat, .5f * fadeAlpha).endVertex();
				buffer.vertex(transform, rightX, rightY, 0).color(sat, sat, sat, 0f).endVertex();
				buffer.vertex(transform, leftX, leftY, 0).color(sat, sat, sat, 0f).endVertex();
				
				Tesselator.getInstance().end();
				
				RenderSystem.enableCull();
			}
		}
	}
	
	private static final record WheelSlice<T>(T val, @Nullable SpellComponentIcon icon, Component name, ITooltip tooltip, float rotationPerc, float width, BiConsumer<T, Boolean> onClick, boolean decorate) {
		private static final Component HiddenName = new TextComponent("?");
		private static final ITooltip HiddenTooltip = Tooltip.create(new TextComponent("An undiscovered component"));
		
		
		public static WheelSlice<Object> Hidden(float rotationPerc, float width) {
			return new WheelSlice<>(null, null, HiddenName, HiddenTooltip, rotationPerc, width, null, false);
		}
		
		public void click(boolean isRight) {
			if (this.onClick != null) {
				this.onClick().accept(val, isRight);
			}
		}

		public boolean isHidden() {
			return this.icon == null && onClick == null;
		}
		
	}
	
	private static final class SelectionStage {
		public SelectionStagePage[] pages;
		public int pageIdx = 0;
		
		public SelectionStage() {
			
		}
		
		public SelectionStage setPages(SelectionStagePage ...pages) {
			this.pages = pages;
			if (this.pageIdx >= pages.length) {
				pageIdx = pages.length - 1;
			}
			return this;
		}
		
		public void movePage(Boolean isNext) {
			this.pageIdx = Mth.clamp(this.pageIdx + (isNext ? 1 : -1), 0, getPageCount());
		}

		public int getPageCount() {
			return pages.length;
		}
	}
	
	private static final class SelectionStagePage {
		public final WheelSlice<?>[] slices;
		
		public SelectionStagePage(WheelSlice<?> ...slices) {
			this.slices = slices;
		}
	}
	
}
