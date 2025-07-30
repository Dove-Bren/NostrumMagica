package com.smanzana.nostrummagica.client.gui.infoscreen;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.SpellComponentIcon;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EElementalMastery;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.SpellEffects;
import com.smanzana.nostrummagica.spell.component.SpellAction;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public abstract class PersonalSubScreen implements IInfoSubScreen {

	protected INostrumMagic attr;
	
	public PersonalSubScreen(INostrumMagic attr) {
		this.attr = attr;
	}
	
	// Screen with what you've unlocked (runes) and tips on where to find others
	public static class PersonalGrowthScreen extends PersonalSubScreen {
		
		public PersonalGrowthScreen(INostrumMagic attr) {
			super(attr);
		}
		
		@Override
		public void draw(INostrumMagic attr, Minecraft mc, PoseStack matrixStackIn, int x, int y, int width, int height, int mouseX, int mouseY) {
			
			if (!attr.isUnlocked()) {
				
				matrixStackIn.pushPose();
				matrixStackIn.translate(x + (width / 2), y, 0);
				
				Component text = new TranslatableComponent("info.magic.locked");
				final int len = mc.font.width(text);
				mc.font.draw(matrixStackIn, text, -len/2, 30, 0xFFFFFFFF);
				matrixStackIn.popPose();
				
				return;
			}
			
			final float known = 1f;
			final float unknown = .15f;
			final int iconWidth = 16;
			int drawX, drawY;
			Component tooltipText = null;
			
			drawX = x + 20;
			drawY = y + 20;
			
			mc.font.draw(matrixStackIn, I18n.get("element.name", (Object[]) null) + "(s):",
					drawX, drawY, 0xFFFFFFFF);
			drawY += 10;
			
			float alpha;
			RenderSystem.enableBlend();
			Map<EMagicElement, Boolean> elementKnow = attr.getKnownElements();
			for (EMagicElement elem : EMagicElement.values()) {
				EElementalMastery mastery = attr.getElementalMastery(elem);
				Boolean know = elementKnow.get(elem);
				if (know != null && know)
					alpha = known;
				else
					alpha = unknown;
				
				RenderFuncs.drawRect(matrixStackIn, drawX, drawY, drawX + iconWidth, drawY + iconWidth, 0xFF406080);
				SpellComponentIcon.get(elem).draw(matrixStackIn, drawX, drawY, iconWidth, iconWidth, 1f, 1f, 1f, alpha);
				
				if (mouseX >= drawX && mouseY >= drawY
						&& mouseX <= drawX + iconWidth && mouseY <= drawY + iconWidth) {
					tooltipText = new TextComponent(elem.getBareName() + ": ")
							.append(mastery.getName());
				}
				
				drawX += 5 + iconWidth;
			}
			
			drawY += 5 + iconWidth;
			drawX = x + 20;
			
//			mc.fontRenderer.drawString(matrixStackIn, I18n.format("trigger.name", (Object[]) null) + "(s):",
//					drawX, drawY, 0xFFFFFFFF);
//			drawY += 10;
			
//			List<SpellTrigger> knownTriggers = attr.getTriggers();
//			for (SpellTrigger trigger : SpellTrigger.getAllTriggers()) {
//				if (knownTriggers.contains(trigger))
//					alpha = known;
//				else
//					alpha = unknown;
//				
//				RenderFuncs.drawRect(matrixStackIn, drawX, drawY, drawX + iconWidth, drawY + iconWidth, 0xFF406080);
//				SpellComponentIcon.get(trigger).draw(mc.currentScreen, matrixStackIn, mc.fontRenderer, drawX, drawY, iconWidth, iconWidth, 1f, 1f, 1f, alpha);
//				
//				if (mouseX >= drawX && mouseY >= drawY
//						&& mouseX <= drawX + iconWidth && mouseY <= drawY + iconWidth) {
//					if (knownTriggers.contains(trigger)) {
//						tooltipText = trigger.getDisplayName();
//					} else {
//						tooltipText = "Unknown Trigger";
//					}
//				}
//				
//				drawX += 5 + iconWidth;
//			}
//			
//			drawY += 5 + iconWidth;
//			drawX = x + 20;
			
			mc.font.draw(matrixStackIn, I18n.get("shape.name", (Object[]) null) + "(s):",
					drawX, drawY, 0xFFFFFFFF);
			drawY += 10;
			
			List<SpellShape> knownShapes = attr.getShapes();
			for (SpellShape shape : SpellShape.getAllShapes()) {
				if (knownShapes.contains(shape))
					alpha = known;
				else
					alpha = unknown;
				
				RenderFuncs.drawRect(matrixStackIn, drawX, drawY, drawX + iconWidth, drawY + iconWidth, 0xFF406080);
				SpellComponentIcon.get(shape).draw(matrixStackIn, drawX, drawY, iconWidth, iconWidth, 1f, 1f, 1f, alpha);
				
				if (mouseX >= drawX && mouseY >= drawY
						&& mouseX <= drawX + iconWidth && mouseY <= drawY + iconWidth) {
					if (knownShapes.contains(shape)) {
						tooltipText = shape.getDisplayName();
					} else {
						tooltipText = new TextComponent("Unknown Shape");
					}
				}
				
				drawX += 5 + iconWidth;
			}
			
			drawY += 5 + iconWidth;
			drawX = x + 20;
			
			mc.font.draw(matrixStackIn, I18n.get("alteration.name", (Object[]) null) + "(s):",
					drawX, drawY, 0xFFFFFFFF);
			drawY += 10;
			
			Map<EAlteration, Boolean> knownAlterations = attr.getAlterations();
			for (EAlteration alteration : EAlteration.values()) {
				Boolean has = knownAlterations.get(alteration);
				if (has != null && has)
					alpha = known;
				else
					alpha = unknown;
				
				RenderFuncs.drawRect(matrixStackIn, drawX, drawY, drawX + iconWidth, drawY + iconWidth, 0xFF406080);
				SpellComponentIcon.get(alteration).draw(matrixStackIn, drawX, drawY, iconWidth, iconWidth, 1f, 1f, 1f, alpha);
				
				if (mouseX >= drawX && mouseY >= drawY
						&& mouseX <= drawX + iconWidth && mouseY <= drawY + iconWidth) {
					if (has != null && has) {
						tooltipText = alteration.getDisplayName();
					} else {
						tooltipText = new TextComponent("Unknown Alteration");
					}
				}
				
				drawX += 5 + iconWidth;
			}
			
			String desc;
			int len;
			desc = I18n.get("info.growth.name", (Object[])null);
			len = mc.font.width(desc);
			mc.font.drawShadow(matrixStackIn, desc, x + ((width - len) / 2), y + 5, 0xFFFFFFFF);
			
			if (tooltipText != null) {
				mc.screen.renderTooltip(matrixStackIn, Lists.newArrayList(tooltipText), Optional.empty(), mouseX, mouseY, mc.font);
			}
		}

		@Override
		public Collection<AbstractWidget> getWidgets(INostrumMagic attr, int x, int y, int width, int height) {
			return null;
		}
		
	}
	
	// Screen with the element + alteration combos you've experienced so far
	public static class PersonalExplorationScreen extends PersonalSubScreen {
		
		private static Map<EAlteration, Map<EMagicElement, SpellAction>> actions = null;
		private List<Row> rows;
		private List<Header> headers;
		
		public PersonalExplorationScreen(INostrumMagic attr) {
			super(attr);
		}
		
		@Override
		public void draw(INostrumMagic attr, Minecraft mc, PoseStack matrixStackIn, int x, int y, int width, int height, int mouseX, int mouseY) {
			
			if (rows == null) {
				rows = new LinkedList<>();
				headers = new LinkedList<>();
				
				int posX = 0;
				int posY = 0;
				final int xOffset = 100;
				final int yOffset = 75;
				
				for (EMagicElement elem : EMagicElement.values()) {
					//drawTable(attr, mc, x + posX * xOffset, y + (posY * yOffset), elem, mouseX, mouseY);
					
					int curY = posY * yOffset;
					
					Header head = new Header(40 + posX * xOffset, curY, elem.getBareName());
					curY += 10;
					headers.add(head);
					
					// add one for no alteration
					rows.add(new Row(posX * xOffset, curY, attr, elem, null));
					curY += 5;
					
					// Add alterations
					for (EAlteration alt : EAlteration.values()) {
						rows.add(new Row(posX * xOffset, curY, attr, elem, alt));
						curY += 5;
					}
					
					posY++;
					if ((posY + 1) * yOffset > height - 20) {
						posY = 0;
						posX++;
					}
				}
			}
			
			matrixStackIn.pushPose();
			matrixStackIn.translate(x + 5, y + 20, 0);
			for (Header head : headers) {
				head.draw(matrixStackIn, mc);
			}
			for (Row row : rows) {
				row.draw(matrixStackIn, mc);
			}
			for (Row row : rows) {
				row.drawOverlay(matrixStackIn, mc, mouseX - (x + 5), mouseY - (y + 20));
			}
			matrixStackIn.popPose();
			
			String desc;
			int len;
			desc = I18n.get("info.exploration.name", (Object[])null);
			len = mc.font.width(desc);
			mc.font.drawShadow(matrixStackIn, desc, x + ((width - len) / 2), y + 5, 0xFFFFFFFF);
		}

		@Override
		public Collection<AbstractWidget> getWidgets(INostrumMagic attr, int x, int y, int width, int height) {
			return null;
		}
		
		private class Header {
			private int x;
			private int y;
			private String name;
			
			public Header(int x, int y, String name) {
				this.x = x;
				this.y = y;
				this.name = name;
			}
			
			public void draw(PoseStack matrixStackIn, Minecraft mc) {
				final float ratio = .8f;
				int len = mc.font.width(name);
				len = (int) (len * ratio);
				matrixStackIn.pushPose();
				matrixStackIn.translate(x, y, 0);
				matrixStackIn.scale(ratio, ratio, 1);
				mc.font.draw(matrixStackIn, name, -len / 2, 0, 0xFFAAAAAA);
				matrixStackIn.popPose();
			}
		}
		
		private class Row {
			
			private int x;
			private int y;
			private int width;
			private String alterationName;
			private String name;
			private String desc;
			private boolean unlocked;
			
			public Row(int x, int y, INostrumMagic attr, EMagicElement element, EAlteration alteration) {
				this.x = x;
				this.y = y;
				this.alterationName = alteration == null ? "  -  " : alteration.getBareName();
				this.width = 0;
				
				SpellAction action = getAction(element, alteration);
				
				if (attr.hasKnowledge(element, alteration)) {
					name = action.getName().getString();
					desc = action.getDescription().getString();
					unlocked = true;
				} else {
					name = "???";
					desc = null;
					unlocked = false;
				}
			}
			
			public void draw(PoseStack matrixStackIn, Minecraft mc) {
				final float ratio = .5f;
				int len = mc.font.width(alterationName + ": ");
				this.width = (int) ((len + mc.font.width(name)) * ratio);
				matrixStackIn.pushPose();
				matrixStackIn.translate(x, y, 0);
				matrixStackIn.scale(ratio, ratio, 0);
				mc.font.draw(matrixStackIn, alterationName + ": ",
						0, 0, 0xFFFFFFFF);
				int color = unlocked ? 0xFF00DD00 : 0xFF0040FF;
				mc.font.draw(matrixStackIn, name,
						len, 0, color);
				matrixStackIn.popPose();
				
				
			}
			
			public void drawOverlay(PoseStack matrixStackIn, Minecraft mc, int mouseX, int mouseY) {
				if (desc == null)
					return;
				
				final float ratio = .5f;
				if (mouseX >= x && mouseY >= y
						&& mouseX <= x + width && mouseY <= y + (int) (mc.font.lineHeight * ratio)) {
					mc.screen.renderTooltip(matrixStackIn, Lists.newArrayList(new TextComponent(desc)), Optional.empty(), mouseX, mouseY, mc.font);
				}
			}
			
		}
		
		private static SpellAction getAction(EMagicElement element, EAlteration alteration) {
			if (actions == null)
				actions = new HashMap<>();
			
			Map<EMagicElement, SpellAction> map = actions.get(alteration);
			if (map == null) {
				map = new EnumMap<>(EMagicElement.class);
				actions.put(alteration, map);
			}
			
			SpellAction action = map.get(element);
			if (action == null) {
				action = SpellEffects.SolveAction(alteration, element, 1);
				map.put(element, action);
			}
			
			return action;
		}
		
	}
	
//	// Screen for defining your personal incantation
//	public static class PersonalIncantationScreen extends PersonalSubScreen {
//		
//		private final List<FixedWidget> widgets;
//		private final InfoScreen parent;
//
//		public PersonalIncantationScreen(INostrumMagic attr, InfoScreen parent) {
//			super(attr);
//			
//			this.widgets = new ArrayList<>();
//			this.parent = parent;
//		}
//
//		@Override
//		public void draw(INostrumMagic attr, Minecraft mc, PoseStack matrixStackIn, int x, int y, int width, int height, int mouseX, int mouseY) {
//			
//			final int middleSpace = 125;
//
//			//final int scrollbarWidth = 8;
//			//final int listWidth = (width / 3) - (scrollbarWidth);
//			final int listHeight = Math.max(50, (height - middleSpace));
//			
//			matrixStackIn.pushPose();
//			matrixStackIn.translate(x, y, 0);
//			
//			String title = "Incantation";
//			int len = mc.font.width(title);
//			mc.font.drawShadow(matrixStackIn, title, width / 2 + (-len / 2), 0, 0xFFFFFFFF);
//			
//			// Selection background
//			GuiComponent.fill(matrixStackIn, (width/2) - 150, (middleSpace/2) - 30, (width/2) + 150, (middleSpace/2) + 30, 0xFF404040);
//			RenderFuncs.drawGradientRect(matrixStackIn,
//					(width/2) + -100 + -12, (middleSpace/2) + -24, (width/2) + -100 + 12, (middleSpace/2), 
//					0xFF000000, 0xFF000000, 0xFF404040, 0xFF404040);
//			
//			RenderFuncs.drawGradientRect(matrixStackIn,
//					(width/2) + 0 + -12, (middleSpace/2) + -24, (width/2) + 0 + 12, (middleSpace/2), 
//					0xFF000000, 0xFF000000, 0xFF404040, 0xFF404040);
//			
//			RenderFuncs.drawGradientRect(matrixStackIn,
//					(width/2) + 100 + -12, (middleSpace/2) + -24, (width/2) + 100 + 12, (middleSpace/2), 
//					0xFF000000, 0xFF000000, 0xFF404040, 0xFF404040);
//			
//			// List backgrounds
//			for (int i = 0; i < 3; i++) {
//				final int lx = 0 + (i * width/3);
//				final int ly = height - listHeight;
//				RenderFuncs.drawGradientRect(matrixStackIn,
//						lx, ly, lx + (width/3), ly + listHeight, 
//						0xFF202030, 0xFF303040, 0xFF202030, 0xFF404050);
//			}
//			
//			matrixStackIn.popPose();
//		}
//		
//		@Override
//		public void drawForeground(INostrumMagic attr, Minecraft mc, PoseStack matrixStackIn, int x, int y, int width, int height, int mouseX, int mouseY) {
//			final int middleSpace = 125;
//
//			//final int scrollbarWidth = 8;
//			//final int listWidth = (width / 3) - (scrollbarWidth);
//			final int listHeight = Math.max(50, (height - middleSpace));
//			GuiComponent.fill(matrixStackIn, x, y + (height + -listHeight + -24), x + width, y + (height + -listHeight), 0xFF000000);
//			
//			String title = "Shapes";
//			int len = mc.font.width(title);
//			mc.font.draw(matrixStackIn, title, x + width / 6 + (-len / 2), y + height + -listHeight + -10, 0xFFFFFFFF);
//			
//			title = "Elements";
//			len = mc.font.width(title);
//			mc.font.draw(matrixStackIn, title, x + width / 2 + (-len / 2), y + height + -listHeight + -10, 0xFFFFFFFF);
//			
//			title = "Alterations";
//			len = mc.font.width(title);
//			mc.font.draw(matrixStackIn, title, x + (5*width) / 6 + (-len / 2), y + height + -listHeight + -10, 0xFFFFFFFF);
//			
//			matrixStackIn.pushPose();
//			matrixStackIn.translate(0, 0, 100);
//			
//			for (FixedWidget widget : widgets) {
//				widget.renderToolTip(matrixStackIn, mouseX, mouseY);
//			}
//			matrixStackIn.popPose();
//		}
//
//		@Override
//		public Collection<AbstractWidget> getWidgets(int x, int y, int width, int height) {
//			widgets.clear();
//			
//			final int middleSpace = 125;
//			
//			widgets.add(new TextWidget(parent, new TextComponent("Shape"), x + (width / 2) + -100, y + (middleSpace/2) + 5, 48, 24).centerHorizontal());
//			widgets.add(new SpellComponentWidget(() -> getActiveShape() == null ? null : SpellComponentIcon.get(getActiveShape()),
//					x + (width / 2) + -100 + -12, y + (middleSpace/2) + -24, 24, 24)
//					.tooltip(this::getShapeTooltip)
//					);
//			
//			widgets.add(new TextWidget(parent, new TextComponent("Element"), x + (width / 2), y + (middleSpace/2) + 5, 48, 24).centerHorizontal());
//			widgets.add(new SpellComponentWidget(() -> getActiveElement() == null ? null : SpellComponentIcon.get(getActiveElement()),
//					x + (width / 2) + -12, y + (middleSpace/2) + -24, 24, 24)
//					.tooltip(this::getElementTooltip)
//					);
//			
//			widgets.add(new TextWidget(parent, new TextComponent("Alteration"), x + (width / 2) + 100, y + (middleSpace/2) + 5, 48, 24).centerHorizontal());
//			widgets.add(new SpellComponentWidget(() -> getActiveAlteration() == null ? null : SpellComponentIcon.get(getActiveAlteration()),
//					x + (width / 2) + 100 + -12, y + (middleSpace/2) + -24, 24, 24)
//					.tooltip(this::getAlterationTooltip)
//					);
//			
//			final int scrollbarWidth = 10;
//			final int listWidth = (width / 3) - (scrollbarWidth);
//			final int listHeight = Math.max(50, (height - middleSpace));
//			
//			final int buttonLen = 24;
//			
//			GridWidget<SpellShapeButton> shapeList = new GridWidget<>(x + 0, y + height - listHeight, listWidth, listHeight, TextComponent.EMPTY);
//			GridWidget<ElementButton> elementList = new GridWidget<>(x + width/3, y + height - listHeight, listWidth, listHeight, TextComponent.EMPTY);
//			GridWidget<AlterationButton> alterationList = new GridWidget<>(x + (2 * width) / 3, y + height - listHeight, listWidth, listHeight, TextComponent.EMPTY);
//			
//			final int margin = 2;
//			final int minSpacing = 0;
//			final int expectedPerRow = listWidth / buttonLen;
//			final int extraSpace = listWidth - ((margin * 2) + (expectedPerRow * buttonLen) + ((expectedPerRow-1) * minSpacing));
//			final int actualSpacing = extraSpace / (expectedPerRow-1);
//			final int actualMargin = margin + (extraSpace - (actualSpacing * (expectedPerRow-1)))/2;
//			shapeList.setMargin(actualMargin).setSpacing(actualSpacing);
//			elementList.setMargin(actualMargin).setSpacing(actualSpacing);
//			alterationList.setMargin(actualMargin).setSpacing(actualSpacing);
//			
//			ScrollbarWidget shapeScroller = new ScrollbarWidget(shapeList, shapeList.x + shapeList.getWidth(), shapeList.y, scrollbarWidth, shapeList.getHeight());
//			shapeList.setScrollbar(shapeScroller);
//			
//			ScrollbarWidget elementScroller = new ScrollbarWidget(elementList, elementList.x + elementList.getWidth(), elementList.y, scrollbarWidth, elementList.getHeight());
//			elementList.setScrollbar(elementScroller);
//			
//			ScrollbarWidget alterationScroller = new ScrollbarWidget(alterationList, alterationList.x + alterationList.getWidth(), alterationList.y, scrollbarWidth, alterationList.getHeight());
//			alterationList.setScrollbar(alterationScroller);
//			
//			List<SpellShapeButton> shapes = new ArrayList<>(32);
//			for (SpellShape shape : attr.getShapes()) {
//				var button = new SpellShapeButton(parent, 0, 0, buttonLen, buttonLen, shape, this::setActiveShape);
//				button.tooltip(Tooltip.create(getShapeTooltip(shape)));
//				shapes.add(button);
//			}
//			shapeList.addChildren(shapes);
//			widgets.add(shapeList);
//			widgets.add(shapeScroller);
//			
//			List<ElementButton> elements = new ArrayList<>(EMagicElement.values().length);
//			for (EMagicElement elem : EMagicElement.values()) {
//				if (!attr.getElementalMastery(elem).isGreaterOrEqual(EElementalMastery.NOVICE)) {
//					continue;
//				}
//				
//				var button = new ElementButton(parent, 0, 0, buttonLen, buttonLen, elem, this::setActiveElement);
//				button.tooltip(Tooltip.create(getElementTooltip(elem)));
//				
//				elements.add(button);
//			}
//			elementList.addChildren(elements);
//			widgets.add(elementList);
//			widgets.add(elementScroller);
//
//			List<AlterationButton> alterations = new ArrayList<>(EAlteration.values().length);
//			for (EAlteration alteration : EAlteration.values()) {
//				if (!attr.getAlterations().getOrDefault(alteration, Boolean.FALSE)) {
//					continue;
//				}
//				
//				var button = new AlterationButton(parent, 0, 0, buttonLen, buttonLen, alteration, this::setActiveAlteration);
//				button.tooltip(Tooltip.create(getAlterationTooltip(alteration)));
//				
//				alterations.add(button);
//			}
//			alterationList.addChildren(alterations);
//			widgets.add(alterationList);
//			widgets.add(alterationScroller);
//			
//			return List.copyOf(widgets);
//		}
//		
//		protected @Nullable SpellShape getActiveShape() {
//			return this.attr.getIncantationShape();
//		}
//		
//		protected void setActiveShape(SpellComponentButton<SpellShape> ignored, @Nullable SpellShape shape) {
//			attr.setIncantationShape(shape);
//			NetworkHandler.sendToServer(new IncantationSelectionMessage(shape));
//		}
//		
//		protected @Nullable EMagicElement getActiveElement() {
//			return attr.getIncantationElement();
//		}
//		
//		protected void setActiveElement(SpellComponentButton<EMagicElement> ignored, @Nullable EMagicElement element) {
//			attr.setIncantationElement(element);
//			NetworkHandler.sendToServer(new IncantationSelectionMessage(element));
//		}
//		
//		protected @Nullable EAlteration getActiveAlteration() {
//			return attr.getIncantationAlteration();
//		}
//		
//		protected void setActiveAlteration(SpellComponentButton<EAlteration> ignored, @Nullable EAlteration alteration) {
//			if (alteration == getActiveAlteration()) {
//				alteration = null;
//			}
//			attr.setIncantationAlteration(alteration);
//			NetworkHandler.sendToServer(new IncantationSelectionMessage(alteration));
//		}
//		
//		protected List<Component> getShapeTooltip(@Nullable SpellShape shape) {
//			if (shape == null) {
//				return null;
//			}
//			
//			return shape.getTooltip();
//		}
//		
//		protected List<Component> getShapeTooltip() {
//			return getShapeTooltip(getActiveShape());
//		}
//		
//		protected List<Component> getElementTooltip(@Nullable EMagicElement element) {
//			if (element == null) {
//				return null;
//			}
//			
//			return element.getTooltip();
//		}
//		
//		protected List<Component> getElementTooltip() {
//			return getElementTooltip(getActiveElement());
//		}
//		
//		protected List<Component> getAlterationTooltip(@Nullable EAlteration alteration) {
//			if (alteration == null) {
//				return null;
//			}
//			
//			return alteration.getTooltip();
//		}
//		
//		protected List<Component> getAlterationTooltip() {
//			return getAlterationTooltip(getActiveAlteration());
//		}
//		
//	}
	
}
