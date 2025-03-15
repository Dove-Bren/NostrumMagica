package com.smanzana.nostrummagica.client.gui.book;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.ScrollScreen;
import com.smanzana.nostrummagica.client.gui.SpellIcon;
import com.smanzana.nostrummagica.item.SpellTome;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.ClientTomeDropSpellMessage;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class SpellPreviewPage implements IClickableBookPage {

	private Spell spell;
	private String description;
	private List<ITextComponent> tooltip;
	private final ItemStack tome;
	
	public SpellPreviewPage(ItemStack tome, Spell spell) {
		this.spell = spell;
		tooltip = new ArrayList<>();
		Map<ReagentType, Integer> reagents = spell.getRequiredReagents();
		for (ReagentType type : reagents.keySet()) {
			if (type == null)
				continue;
			Integer count = reagents.get(type);
			if (count == null || count == 0)
				continue;
			
			tooltip.add(new StringTextComponent(count + " " + type.prettyName()));
		}
		tooltip.add(new StringTextComponent("Click for details").withStyle(TextFormatting.GRAY));
		tooltip.add(new StringTextComponent("Shift+Right Click to remove and destroy").withStyle(TextFormatting.DARK_RED));
		
		description = "";
		this.tome = tome;
	}
	
	@Override
	public void draw(BookScreen parent, MatrixStack matrixStackIn, FontRenderer fonter, int xoffset, int yoffset, int width, int height) {

		yoffset += 5;
		height -= 5;
		
		Minecraft mc = Minecraft.getInstance();
		INostrumMagic attr = NostrumMagica.getMagicWrapper(mc.player);
		
		matrixStackIn.pushPose();
		RenderFuncs.drawRect(matrixStackIn, xoffset, yoffset, xoffset + width, yoffset + height, 0x40000000);
		
		// Draw element icon
		//SpellComponentIcon elementIcon = SpellComponentIcon.get(spell.getPrimaryElement());
		SpellIcon icon = SpellIcon.get(spell.getIconIndex());
		int icony = yoffset + (height / 2) + (-12);
		//matrixStackIn.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		icon.render(Minecraft.getInstance(), matrixStackIn, xoffset + 4, icony, 24, 24);
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(xoffset + 32, yoffset + 2, 0);
		matrixStackIn.scale(.7f, .7f, 1);
		
		fonter.draw(matrixStackIn, spell.getName(), 0, 0, spell.getPrimaryElement().getColor());
		yoffset = fonter.lineHeight + 3;
		fonter.draw(matrixStackIn, "Mana: " + spell.getManaCost(), 0, yoffset, 0xFF354AA8);
		yoffset += fonter.lineHeight + 3;
		fonter.draw(matrixStackIn, "Weight: " + spell.getWeight(), 0, yoffset, 0xFF354AA8);
		yoffset += fonter.lineHeight + 3;
		if (attr != null) {
			float xp = spell.getXP(true);
			float perc = xp / attr.getMaxXP();
			perc *= 100f;
			fonter.draw(matrixStackIn, String.format("XP: %.1f (%03.2f%%)", xp, perc)
					, 0, yoffset, 0xFF0A3500);
			yoffset += fonter.lineHeight + 3;
		}
		RenderFuncs.drawSplitString(matrixStackIn, fonter, description, 0,
				yoffset, width, 0xFF000000);
		
		matrixStackIn.popPose();
		matrixStackIn.popPose();
	}

	@Override
	public void overlay(BookScreen parent, MatrixStack matrixStackIn, FontRenderer fonter, int mouseX, int mouseY, int trueX, int trueY) {
		Minecraft mc = Minecraft.getInstance();
		GuiUtils.drawHoveringText(matrixStackIn, tooltip, trueX, trueY, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight(), 200, fonter);
	}

	@Override
	public boolean onClick(BookScreen parent, double mouseX, double mouseY, int button) {
		PlayerEntity player = NostrumMagica.instance.proxy.getPlayer();
		
		if (button == 0) {
			Minecraft.getInstance().setScreen(new ScrollScreen(spell));
		} else if (button == 1 && InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
			// Fake on client
			SpellTome.removeSpell(tome, spell.getRegistryID());
			NostrumMagica.instance.proxy.openBook(player, (SpellTome) tome.getItem(), tome);
			
			NetworkHandler.getSyncChannel()
			.sendToServer(new ClientTomeDropSpellMessage(tome, spell.getRegistryID()));
		}
		return true;
	}
}
