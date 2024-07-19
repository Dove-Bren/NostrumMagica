package com.smanzana.nostrummagica.client.gui.infoscreen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.SpellComponentIcon;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EElementalMastery;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.component.SpellAction;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

public abstract class PersonalSubScreen implements IInfoSubScreen {

	protected INostrumMagic attr;
	
	public PersonalSubScreen(INostrumMagic attr) {
		this.attr = attr;
	}
	
	// Screen with information about unlocking the magic system, for newbs
	public static class PersonalDiscoveryScreen extends PersonalSubScreen {
		
		public PersonalDiscoveryScreen(INostrumMagic attr) {
			super(attr);
		}
		
		@Override
		public void draw(INostrumMagic attr, Minecraft mc, MatrixStack matrixStackIn, int x, int y, int width, int height, int mouseX, int mouseY) {
			
			String desc;
			if (attr.isUnlocked()) {
				desc = I18n.format("info.discovery.spells", new Object[0]);
			} else {
				SpellShape shape = null;
				EMagicElement element = null;
				if (!attr.getShapes().isEmpty())
					shape = attr.getShapes().get(0);
				
				Map<EMagicElement, Boolean> map = attr.getKnownElements();
				for (EMagicElement elem : map.keySet()) {
					Boolean val;
					val = map.get(elem);
					if (val != null && val) {
						element = elem;
						break;
					}
				}
				
				if (shape != null || element != null) {
					desc = I18n.format("info.discovery.starting2", new Object[0]);
				} else {
					desc = I18n.format("info.discovery.starting", new Object[0]);
				}
				
				// Draw rotating icons
				final int iconWidth = 24;
				final int space = 32;
				final long cycle = 1500;
				int drawx = x + (int) (.5 * width) + (-iconWidth / 2) + (-space) + (-iconWidth);
				int drawy = y + height - (iconWidth + 20);
				int strLen;
				String str;
				float color[];

				RenderSystem.enableBlend();
				RenderFuncs.drawRect(matrixStackIn, drawx - 2, drawy - 2, drawx + iconWidth + 2, drawy + iconWidth + 2, 0xA0000000);
				if (element != null)
					color = new float[] {1f, 1f, 1f, 1f};
				else {
					color = new float[] {.8f, .5f, .5f, .5f};
					element = EMagicElement.values()[
		                  (int) (System.currentTimeMillis() / cycle) % EMagicElement.values().length
					      ];
				}
				SpellComponentIcon.get(element).draw(matrixStackIn, drawx, drawy, iconWidth, iconWidth, color[0], color[1], color[2], color[3]);
				str = I18n.format("element.name", new Object[0]);
				strLen = mc.fontRenderer.getStringWidth(str);
				mc.fontRenderer.drawString(matrixStackIn, str, (drawx + iconWidth / 2) - strLen/2, drawy - (3 + mc.fontRenderer.FONT_HEIGHT), 0xFFFFFF);
				
				drawx += iconWidth + space;
				RenderFuncs.drawRect(matrixStackIn, drawx - 2, drawy - 2, drawx + iconWidth + 2, drawy + iconWidth + 2, 0xA0000000);
				if (shape != null)
					color = new float[] {1f, 1f, 1f, 1f};
				else {
					color = new float[] {.8f, .5f, .5f, .5f};
					Collection<SpellShape> shapes = SpellShape.getAllShapes();
					SpellShape[] shapeArray = shapes.toArray(new SpellShape[0]);
					shape = shapeArray[
		                  (int) (System.currentTimeMillis() / cycle) % shapeArray.length
					      ];
				}
				SpellComponentIcon.get(shape).draw(matrixStackIn, drawx, drawy, iconWidth, iconWidth, color[0], color[1], color[2], color[3]);
				str = I18n.format("shape.name", new Object[0]);
				strLen = mc.fontRenderer.getStringWidth(str);
				mc.fontRenderer.drawString(matrixStackIn, str, (drawx + iconWidth / 2) - strLen/2, drawy - (3 + mc.fontRenderer.FONT_HEIGHT), 0xFFFFFF);
				
			}
			
			RenderFuncs.drawSplitString(matrixStackIn, mc.fontRenderer, desc, x + 5, y + 20, width - 10, 0xFFFFFFFF);
			int len;
			desc = I18n.format("info.discovery.name", (Object[])null);
			len = mc.fontRenderer.getStringWidth(desc);
			mc.fontRenderer.drawStringWithShadow(matrixStackIn, desc, x + ((width - len) / 2), y + 5, 0xFFFFFFFF);
		}

		@Override
		public Collection<Widget> getWidgets(int x, int y, int width, int height) {
			return null;
		}
		
	}
	
	// Screen where all of your stats and what they equate to are displayed
	public static class PersonalStatsScreen extends PersonalSubScreen {
		
		// Display wrapper that supports tooltips! Whoo!
		private class StatLabel {
			
			private String label;
			private List<ITextComponent> tooltip;
			private int x;
			private int y;
			private int width;
			private int height;
			
			public StatLabel(String key, int x, int y) {
				label = I18n.format(key + ".name", (Object[]) null) + ": ";
				parseTooltip(key);
				
				Minecraft mc = Minecraft.getInstance();
				this.width = mc.fontRenderer.getStringWidth(label);
				this.height = mc.fontRenderer.FONT_HEIGHT;
				this.x = x;
				this.y = y;
			}
			
			private void parseTooltip(String key) {
				String raw = I18n.format(key + ".desc", (Object[]) null).trim();
				tooltip = new ArrayList<>();
				int index = raw.indexOf('|');
				while (index != -1) {
					String sub = raw.substring(0, index).trim();
					tooltip.add(new StringTextComponent(sub));
					raw = raw.substring(index + 1).trim();
					index = raw.indexOf('|');
				}
				
				tooltip.add(new StringTextComponent(raw));
			}
			
			public void draw(MatrixStack matrixStackIn, Minecraft mc, int offsetx, int offsety, int width, int height) {
				mc.fontRenderer.drawString(matrixStackIn, label, offsetx + x, offsety + y, 0xFFFFFFFF);
			}
			
			public void drawOverlay(MatrixStack matrixStackIn, Minecraft mc, int offsetx, int offsety, int width, int height, int mouseX, int mouseY) {
				if (mouseX >= offsetx + x && mouseX <= x + offsetx + this.width
						&& mouseY >= offsety + y && mouseY <= y + offsety + this.height) {
					GuiUtils.drawHoveringText(matrixStackIn, tooltip, mouseX, mouseY, width, height, 150, mc.fontRenderer);
					RenderHelper.disableStandardItemLighting();
				}
			}
			
		}
		
		private List<StatLabel> labels;
		private int valueOffsetPrimary;
		private int valueOffsetDerived;
		
		public PersonalStatsScreen(INostrumMagic attr) {
			super(attr);
			labels = null;
		}
		
		@Override
		public void draw(INostrumMagic attr, Minecraft mc, MatrixStack matrixStackIn, int x, int y, int width, int height, int mouseX, int mouseY) {
			
			if (labels == null) {
				int drawX = 0;
				int drawY = 0;
				
				labels = new ArrayList<>();
				
				StatLabel label;
				valueOffsetPrimary = valueOffsetDerived = 0;
				
				
				label = new StatLabel("level", drawX, drawY);
				labels.add(label);
				valueOffsetPrimary = Math.max(valueOffsetPrimary, label.width);
				drawY += 15;
				label = new StatLabel("tier", drawX, drawY);
				labels.add(label);
				valueOffsetPrimary = Math.max(valueOffsetPrimary, label.width);
				drawY += 15;
				
				drawY = 0;
				drawX = width - 200;
				label = new StatLabel("mana", drawX, drawY);
				labels.add(label);
				valueOffsetDerived = Math.max(valueOffsetDerived, label.width);
				drawY += 15;
				label = new StatLabel("manabonus", drawX, drawY);
				labels.add(label);
				valueOffsetDerived = Math.max(valueOffsetDerived, label.width);
				drawY += 15;
				label = new StatLabel("lmc", drawX, drawY);
				labels.add(label);
				valueOffsetDerived = Math.max(valueOffsetDerived, label.width);
				drawY += 15;
				label = new StatLabel("manaregen", drawX, drawY);
				labels.add(label);
				valueOffsetDerived = Math.max(valueOffsetDerived, label.width);
				drawY += 15;
			}
			
			int drawX, drawY;
			
			drawY = y + 20;
			drawX = x + 20;
			
			for (StatLabel label : labels) {
				label.draw(matrixStackIn, mc, drawX, drawY, width, height);
			}
			
			// draw values
			String text;
			int color = 0xFF22FF00;
			drawY = y + 20;
			drawX = x + 20 + valueOffsetPrimary;
			text = String.format("%2d", attr.getLevel());
			mc.fontRenderer.drawString(matrixStackIn, text, drawX, drawY, color);
			drawY += 15;
			
			//text = String.format("%d", attr.getTier().getName());
			mc.fontRenderer.func_243248_b(matrixStackIn, attr.getTier().getName(), drawX, drawY, color);
			drawY += 15;
			
			//text = String.format("3.1%f%%", );
			drawX = x + 20 + (width - 200) + valueOffsetDerived;
			drawY = y + 20;
			
			text = String.format("%4d", attr.getMaxMana());
			mc.fontRenderer.drawString(matrixStackIn, text, drawX, drawY, color);
			drawY += 15;
			
			text = String.format("%+5.1f%%", attr.getManaModifier() * 100f);
			mc.fontRenderer.drawString(matrixStackIn, text, drawX, drawY, color);
			drawY += 15;
			
			text = String.format("%+5.1f%%", attr.getManaCostModifier() * 100f);
			mc.fontRenderer.drawString(matrixStackIn, text, drawX, drawY, color);
			drawY += 15;
			
			text = String.format("%+05.1f%%", attr.getManaRegenModifier() * 100f);
			mc.fontRenderer.drawString(matrixStackIn, text, drawX, drawY, color);
			drawY += 15;
			
			String desc;
			int len;
			desc = I18n.format("info.stats.name", (Object[])null);
			len = mc.fontRenderer.getStringWidth(desc);
			mc.fontRenderer.drawStringWithShadow(matrixStackIn, desc, x + ((width - len) / 2), y + 5, 0xFFFFFFFF);

			
			for (StatLabel label : labels) {
				label.drawOverlay(matrixStackIn, mc, x + 20, y + 20, width, height, mouseX, mouseY);
			}
		}

		@Override
		public Collection<Widget> getWidgets(int x, int y, int width, int height) {
			return null;
		}
		
	}
	
	// Screen with what you've unlocked (runes) and tips on where to find others
	public static class PersonalGrowthScreen extends PersonalSubScreen {
		
		public PersonalGrowthScreen(INostrumMagic attr) {
			super(attr);
		}
		
		@Override
		public void draw(INostrumMagic attr, Minecraft mc, MatrixStack matrixStackIn, int x, int y, int width, int height, int mouseX, int mouseY) {
			
			final float known = 1f;
			final float unknown = .15f;
			final int iconWidth = 16;
			int drawX, drawY;
			ITextComponent tooltipText = null;
			
			drawX = x + 20;
			drawY = y + 20;
			
			mc.fontRenderer.drawString(matrixStackIn, I18n.format("element.name", (Object[]) null) + "(s):",
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
					tooltipText = new StringTextComponent(elem.getName() + ": ")
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
			
			mc.fontRenderer.drawString(matrixStackIn, I18n.format("shape.name", (Object[]) null) + "(s):",
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
						tooltipText = new StringTextComponent("Unknown Shape");
					}
				}
				
				drawX += 5 + iconWidth;
			}
			
			drawY += 5 + iconWidth;
			drawX = x + 20;
			
			mc.fontRenderer.drawString(matrixStackIn, I18n.format("alteration.name", (Object[]) null) + "(s):",
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
						tooltipText = new StringTextComponent(alteration.getName());
					} else {
						tooltipText = new StringTextComponent("Unknown Alteration");
					}
				}
				
				drawX += 5 + iconWidth;
			}
			
			String desc;
			int len;
			desc = I18n.format("info.growth.name", (Object[])null);
			len = mc.fontRenderer.getStringWidth(desc);
			mc.fontRenderer.drawStringWithShadow(matrixStackIn, desc, x + ((width - len) / 2), y + 5, 0xFFFFFFFF);
			
			if (tooltipText != null) {
				GuiUtils.drawHoveringText(matrixStackIn, Lists.newArrayList(tooltipText), mouseX, mouseY, mc.getMainWindow().getScaledWidth(), mc.getMainWindow().getScaledHeight(), 200, mc.fontRenderer);
				RenderHelper.disableStandardItemLighting();
			}
		}

		@Override
		public Collection<Widget> getWidgets(int x, int y, int width, int height) {
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
		public void draw(INostrumMagic attr, Minecraft mc, MatrixStack matrixStackIn, int x, int y, int width, int height, int mouseX, int mouseY) {
			
			if (rows == null) {
				rows = new LinkedList<>();
				headers = new LinkedList<>();
				
				int posX = 0;
				int posY = 0;
				final int xOffset = 100;
				final int yOffset = 60;
				
				for (EMagicElement elem : EMagicElement.values()) {
					//drawTable(attr, mc, x + posX * xOffset, y + (posY * yOffset), elem, mouseX, mouseY);
					
					int curY = posY * yOffset;
					
					Header head = new Header(40 + posX * xOffset, curY, elem.getName());
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
			
			matrixStackIn.push();
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
			matrixStackIn.pop();
			
			String desc;
			int len;
			desc = I18n.format("info.exploration.name", (Object[])null);
			len = mc.fontRenderer.getStringWidth(desc);
			mc.fontRenderer.drawStringWithShadow(matrixStackIn, desc, x + ((width - len) / 2), y + 5, 0xFFFFFFFF);
		}

		@Override
		public Collection<Widget> getWidgets(int x, int y, int width, int height) {
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
			
			public void draw(MatrixStack matrixStackIn, Minecraft mc) {
				final float ratio = .8f;
				int len = mc.fontRenderer.getStringWidth(name);
				len = (int) (len * ratio);
				matrixStackIn.push();
				matrixStackIn.translate(x, y, 0);
				matrixStackIn.scale(ratio, ratio, 1);
				mc.fontRenderer.drawString(matrixStackIn, name, -len / 2, 0, 0xFFAAAAAA);
				matrixStackIn.pop();
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
				this.alterationName = alteration == null ? "  -  " : alteration.getName();
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
			
			public void draw(MatrixStack matrixStackIn, Minecraft mc) {
				final float ratio = .5f;
				int len = mc.fontRenderer.getStringWidth(alterationName + ": ");
				this.width = (int) ((len + mc.fontRenderer.getStringWidth(name)) * ratio);
				matrixStackIn.push();
				matrixStackIn.translate(x, y, 0);
				matrixStackIn.scale(ratio, ratio, 0);
				mc.fontRenderer.drawString(matrixStackIn, alterationName + ": ",
						0, 0, 0xFFFFFFFF);
				int color = unlocked ? 0xFF00DD00 : 0xFF0040FF;
				mc.fontRenderer.drawString(matrixStackIn, name,
						len, 0, color);
				matrixStackIn.pop();
				
				
			}
			
			public void drawOverlay(MatrixStack matrixStackIn, Minecraft mc, int mouseX, int mouseY) {
				if (desc == null)
					return;
				
				final float ratio = .5f;
				if (mouseX >= x && mouseY >= y
						&& mouseX <= x + width && mouseY <= y + (int) (mc.fontRenderer.FONT_HEIGHT * ratio)) {
					GuiUtils.drawHoveringText(matrixStackIn, Lists.newArrayList(new StringTextComponent(desc)), mouseX, mouseY, mc.getMainWindow().getScaledWidth(), mc.getMainWindow().getScaledHeight(), 200, mc.fontRenderer);
					RenderHelper.disableStandardItemLighting();
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
				action = Spell.solveAction(alteration, element, 1);
				map.put(element, action);
			}
			
			return action;
		}
		
	}
	
}
