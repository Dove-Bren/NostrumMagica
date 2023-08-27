package com.smanzana.nostrummagica.client.effects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.tiles.AltarTileEntity;
import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifier;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.utils.NonNullEnumMap;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientEffectRitual extends ClientEffect {

	private static final int DURATION_TIER3 = 20 * 6;
	private static final int DURATION_TIER2 = 20 * 6;
	private static final int DURATION_TIER1 = 20 * 2;
	
	private static NonNullEnumMap<ReagentType, ItemStack> itemCache = null;
	
	protected EMagicElement element = EMagicElement.PHYSICAL;
	protected ItemStack center = ItemStack.EMPTY; // Either .empty() or present
	protected NonNullList<ItemStack> extras = null; // can be null or size 4 with .empty() for blank altars
	protected ReagentType[] reagents = null; // Either [1] or [4]
	protected ItemStack output = ItemStack.EMPTY; // either .empty() or actual output
	
	protected ClientEffectRitual(int duration, Vec3d origin,
			EMagicElement element, ItemStack center, NonNullList<ItemStack> extras, ReagentType[] reagents, ItemStack output) {
		super(origin, null, duration);
		this.center = center;
		this.extras = extras;
		this.reagents = reagents;
		this.output = output;
		this.element = element;
	}
	
	@Override
	public void onStart() {
		Minecraft mc = Minecraft.getInstance();
		TileEntity te = mc.world.getTileEntity(new BlockPos(origin).down());
		if (te != null && te instanceof AltarTileEntity) {
			((AltarTileEntity) te).hideItem(true);
		}
		
		if (itemCache == null) {
			itemCache = new NonNullEnumMap<>(ReagentType.class, ItemStack.EMPTY);
			for (ReagentType type : ReagentType.values()) {
				itemCache.put(type, ReagentItem.instance().getReagent(type, 1));
			}
		}
	}
	
	@Override
	public void onEnd() {
		Minecraft mc = Minecraft.getInstance();
		TileEntity te = mc.world.getTileEntity(new BlockPos(origin).down());
		if (te != null && te instanceof AltarTileEntity) {
			((AltarTileEntity) te).hideItem(false);
		}
	}
	
	protected static ItemStack getReagentItem(ReagentType type) {
		return itemCache.get(type);
	}
	
	public ClientEffectRitual(Vec3d origin, EMagicElement element, ItemStack center, NonNullList<ItemStack> extras, ReagentType[] reagents, ItemStack output) {
		this(DURATION_TIER3, origin, element, center, extras, reagents, output);
	}
	
	public ClientEffectRitual(Vec3d origin, EMagicElement element, ItemStack center, ReagentType[] reagents, ItemStack output) {
		this(DURATION_TIER2, origin, element, center, null, reagents, output);
	}
	
	public ClientEffectRitual(Vec3d origin, EMagicElement element, ReagentType reagent, ItemStack output) {
		this(DURATION_TIER1, origin, element, ItemStack.EMPTY, null, new ReagentType[] {reagent}, output);
	}
	
	/**
	 * Creates an effect. Looks at which parameters are present and how long arrays are to deduce which type
	 * @param origin
	 * @param center
	 * @param extras
	 * @param reagents
	 * @param output
	 * @return
	 */
	public static ClientEffectRitual Create(Vec3d origin, EMagicElement element, ItemStack center, @Nullable NonNullList<ItemStack> extras, ReagentType[] reagents, ItemStack output) {
		if (center.isEmpty()) {
			return new ClientEffectRitual(origin, element, reagents[0], output);
		} else if (extras == null) {
			return new ClientEffectRitual(origin, element, center, reagents, output);
		} else {
			return new ClientEffectRitual(origin, element, center, extras, reagents, output);
		}
	}
	
	protected void drawFloatingItem(Minecraft mc, float adjProgress, float partialTicks, Vec3d pos, @Nonnull ItemStack stack) {
		if (stack.isEmpty()) {
			return;
		}
		
		final float scale = .75f;
		//final float rotPeriod = .5f;
		//final float rot = 360f * ((adjProgress % rotPeriod) / rotPeriod); // make rotate?
		final float rot = (float) (2.0 * ((double) System.currentTimeMillis() / 50.0));
		
		GlStateManager.pushMatrix();
		GlStateManager.translated(pos.x, pos.y, pos.z);
		GlStateManager.rotatef(rot, 0, 1f, 0);
		
		GlStateManager.scalef(scale, scale, scale);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.disableLighting();
		GlStateManager.enableAlphaTest();
		GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);

		RenderHelper.enableStandardItemLighting();
		
		RenderFuncs.ItemRenderer(stack);
		
		GlStateManager.popMatrix();
		
		if (NostrumMagica.rand.nextBoolean()
				&& NostrumMagica.rand.nextBoolean()) {
			NostrumParticles.GLOW_ORB.spawn(mc.player.world, (new SpawnParams(
					1, origin.x + pos.x, origin.y + pos.y - .15, origin.z + pos.z, .1, 15, 5,
					new Vec3d(0, -.01, 0), null
					)).color(.4f, .3f, .2f, .4f));
			
			//int count, double spawnX, double spawnY, double spawnZ, double spawnJitterRadius, int lifetime, int lifetimeJitter, 
			//Vec3d velocity, boolean unused
		}
	}
	
	protected void drawCandleTrail(Minecraft mc, float adjProgress, float partialTicks, Vec3d pos, @Nullable ReagentType type) {
		
		if (type != null) {
			final float scale = .75f;
			final float rotPeriod = .5f;
			final float rot = 360f * ((adjProgress % rotPeriod) / rotPeriod); // make rotate?
			
			GlStateManager.pushMatrix();
			GlStateManager.translated(pos.x, pos.y, pos.z);
			GlStateManager.rotatef(rot, 0, 1f, 0);
			
			GlStateManager.scalef(scale, scale, scale);
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlStateManager.disableLighting();
			GlStateManager.enableAlphaTest();
			GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
			RenderHelper.enableStandardItemLighting();
			
			RenderFuncs.ItemRenderer(getReagentItem(type));
			// TODO 1.14 check
			
			GlStateManager.popMatrix();
		}
		
		if (
				NostrumMagica.rand.nextBoolean()
				)
		{
			NostrumParticles.GLOW_ORB.spawn(mc.player.world, (new SpawnParams(
					1, origin.x + pos.x, origin.y + pos.y, origin.z + pos.z, 0, 45, 15,
					new Vec3d(0, -.02, 0), null
					)).color(.4f, .3f, .2f, .4f));
			
			//int count, double spawnX, double spawnY, double spawnZ, double spawnJitterRadius, int lifetime, int lifetimeJitter, 
			//Vec3d velocity, boolean unused
		}
	}
	
	protected void drawRevealCloud(Minecraft mc, float adjProgress, float partialTicks, Vec3d pos) {
		NostrumParticles.GLOW_ORB.spawn(mc.player.world, (new SpawnParams(
				10, origin.x + pos.x, origin.y + pos.y, origin.z + pos.z, 0, 40, 20,
				Vec3d.ZERO, new Vec3d(.1, .1, .1)
				)).color(0x40000000 | (this.element.getColor() & 0x00FFFFFF)));
	}
	
	protected void drawPhase1(ClientEffectRenderDetail detail, Minecraft mc, float adjProgress, float partialTicks) {
		// For tier 2 or tier 3 rituals:
		// Reagents don't render but instead make a trail of smoke that comes in to the center (in a circle?)
		// Center item slowly raises and sparkles
		// Extra items slowly float (also in a circle?) to the center
		if (center != null && !center.isEmpty()) {
			//Center item
			{
				final double yDiff = (adjProgress * .5);
				drawFloatingItem(mc, adjProgress, partialTicks, new Vec3d(0, yDiff + .35, 0), center);
			}
			
			// Extras
			if (extras != null) {
//				final int offsetXs[] = {-4, 0, 0, 4};
//				final int offsetZs[] = {0, -4, 4, 0};
				final double angles[] = {Math.PI, -Math.PI / 2.0, Math.PI / 2.0 , 0};
				final double magnitude = 4;
				
				for (int i = 0; i < 4; i++) {
					if (i >= extras.size()) {
						continue;
					}
					ItemStack stack = extras.get(i);
					if (stack.isEmpty()) {
						continue;
					}
					
					// X and Z just interpolate to center
					// Y we'll do a parabola
//					final double x = offsetXs[i] * (1f - adjProgress);
//					final double z = offsetZs[i] * (1f - adjProgress);
//					final double y = .35 + Math.sin(Math.PI * adjProgress);
					
					//final double dist = Math.sqrt(Math.pow(offsetXs[i] * (1f - adjProgress), 2) + Math.pow(offsetZs[i] * (1f - adjProgress), 2));
					final double dist = magnitude * (1f - adjProgress);
					final double angle = angles[i] + (Math.PI * adjProgress);
					
					final double y = .35 + Math.sin(Math.PI * adjProgress) + (.5 * adjProgress);
					final double x = Math.cos(angle) * dist;
					final double z = Math.sin(angle) * dist;
					
					drawFloatingItem(mc, adjProgress, partialTicks, new Vec3d(
							x,
							y,
							z
						),
						stack);
				}
			}
			
			// Reagents
			{
				// (-2,-2), (-2, 2), (2, -2), (2, 2)
				// 225 degrees, 135, -45, 45
				final double angles[] = {-Math.PI * (6.0/8.0), Math.PI * (6.0/8.0), Math.PI * (-2.0/8.0), Math.PI * (2.0/8.0)};
				final double magnitude = Math.sqrt(4 + 4);
				
				for (int i = 0; i < 4; i++) {
					if (i >= reagents.length) {
						continue;
					}
					final ReagentType type = reagents[i];
					
					// X and Z just interpolate to center
					// Y we'll do a parabola
//					final double x = offsetXs[i] * (1f - adjProgress);
//					final double z = offsetZs[i] * (1f - adjProgress);
//					final double y = .35 + Math.sin(Math.PI * adjProgress);
					
					//final double dist = Math.sqrt(Math.pow(offsetXs[i] * (1f - adjProgress), 2) + Math.pow(offsetZs[i] * (1f - adjProgress), 2));
					final double dist = magnitude * (1f - adjProgress);
					final double angle = angles[i] + (Math.PI * adjProgress);
					
					final double y = .35 + Math.sin(Math.PI * adjProgress) + (.5 * adjProgress);
					final double x = Math.cos(angle) * dist;
					final double z = Math.sin(angle) * dist;
					
					drawCandleTrail(mc, adjProgress, partialTicks, new Vec3d(x, y, z), type);
//					drawFloatingItem(mc, adjProgress, partialTicks, new Vec3d(
//							x,
//							y,
//							z
//						),
//						stack);
				}
			}
			
			// Ambient particles
			{
				// Float up (early) or start speeding in
				final double range = 4;
				final Vec3d pos = Vec3d.ZERO;
				if (adjProgress < .85f) {
					NostrumParticles.FILLED_ORB.spawn(mc.player.world, (new SpawnParams(
							4, origin.x + pos.x, origin.y + pos.y, origin.z + pos.z, range, 30, 20,
							new Vec3d(0, .01, 0), new Vec3d(.1, .1, .1)
							)).color(0x40000000 | (this.element.getColor() & 0x00FFFFFF)));
				} else {
					final double yDiff = .35 + .5;
					NostrumParticles.FILLED_ORB.spawn(mc.player.world, (new SpawnParams(
							4, origin.x + pos.x, origin.y + pos.y, origin.z + pos.z, range, 20, 0,
							origin.add(0, yDiff, 0)
							)).color(0x40000000 | (this.element.getColor() & 0x00FFFFFF)).dieOnTarget(true));
				}
				
			}
		}
		
		// For tier 1 ritual:
		// Smoke trail comes up in 4 trails and spirals up for a while
		else {
			final float yawPeriod = 1f;
			final float hDistMax = 3f;
			
			final float rotYawProg = (adjProgress % yawPeriod) / yawPeriod;
			final float hDist = adjProgress * hDistMax;
			final float rotYawRad = (float) (Math.PI * 2 * rotYawProg);
			final float RadDiff = (float) (Math.PI * 2) / 4f;
			
			//final ReagentType type = reagents[0];
			
			for (int i = 0; i < 4; i++) {
				Vec3d pos = new Vec3d(
						hDist * Math.cos(rotYawRad + (i * RadDiff)),
						hDist * 2,
						hDist * Math.sin(rotYawRad + (i * RadDiff))
						);
				drawCandleTrail(mc, adjProgress, partialTicks, pos, null);
			}
		}
	}
	
	protected void drawPhase2(ClientEffectRenderDetail detail, Minecraft mc, float adjProgress, float partialTicks) {
		// For tier 2 and tier 3 rituals:
		// 
		if (center != null && !center.isEmpty()) {
			// If item output, draw that and lower it to the platform
			if (this.output != null && !this.output.isEmpty()) {
				final double yDiff = ((1.0-adjProgress) * .65);
				drawFloatingItem(mc, adjProgress/4f, partialTicks, new Vec3d(0, yDiff + .2, 0), output);
			}
			// Else cause an explosion of particles that just move outward
			else {
				drawRevealCloud(mc, adjProgress, partialTicks, new Vec3d(0, 1.15, 0));
			}
			
			// Ambient particles
			{
				final double range = 4;
				final Vec3d pos = Vec3d.ZERO;
				NostrumParticles.FILLED_ORB.spawn(mc.player.world, (new SpawnParams(
						4, origin.x + pos.x, origin.y + pos.y, origin.z + pos.z, range, 30, 20,
						origin.add(0, .35, 0)
						)).color(0x40000000 | (this.element.getColor() & 0x00FFFFFF)));
			}
		}
		
		// For tier 1 ritual:
		// Make particals dive back into the candle
		else {
			final float yawPeriod = 1f;
			final float hDistMax = 3f;
			
			final float rotYawProg = ((1f + adjProgress/4f) % yawPeriod) / yawPeriod; // Stay at same rotation things ended on
			final float hDist = (1-adjProgress) * hDistMax;
			final float rotYawRad = (float) (Math.PI * 2 * rotYawProg);
			final float RadDiff = (float) (Math.PI * 2) / 4f;
			
			//final ReagentType type = reagents[0];
			
			for (int i = 0; i < 4; i++) {
				Vec3d pos = new Vec3d(
						hDist * Math.cos(rotYawRad + (i * RadDiff)),
						hDist * 2,
						hDist * Math.sin(rotYawRad + (i * RadDiff))
						);
				drawCandleTrail(mc, adjProgress, partialTicks, pos, null);
			}
			if (adjProgress > .5f) {
				drawRevealCloud(mc, adjProgress, partialTicks, Vec3d.ZERO);
			}
		}
	}
	
	@Override
	protected void drawForm(ClientEffectRenderDetail detail, Minecraft mc, float progress, float partialTicks) {
		if (!this.modifiers.isEmpty())
			for (ClientEffectModifier mod : modifiers) {
				mod.apply(detail, progress, partialTicks);
			}
		
		// Draw items, if there, or smoke from reagents, and old center until it's 'done' (.90) and then reveal result.
		if (progress < .8f) {
			drawPhase1(detail, mc, progress/.8f, partialTicks);
		} else {
			drawPhase2(detail, mc, (progress-.8f)/.2f, partialTicks);
		}
	}

}
