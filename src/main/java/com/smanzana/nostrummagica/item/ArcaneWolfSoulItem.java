package com.smanzana.nostrummagica.item;

import java.util.List;
import java.util.UUID;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ArcaneWolfSoulItem extends PetSoulItem {
	
	public static final String ID = "arcane_wolf_soul_item";
	private static final String NBT_MANA = "mana";
	
	public ArcaneWolfSoulItem() {
		super(NostrumItems.PropUnstackable().rarity(Rarity.EPIC));
	}

	@Override
	public String getLoreKey() {
		return "arcane_wolf_soul_items";
	}

	@Override
	public String getLoreDisplayName() {
		return "Arcane Wolf Souls";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("This beautiful flower holds within it the fierce-yet-loving soul of your Arcane Wolf.",
				"The wolf soul is powerful, but incomplete. More magical energy is needed to bring the wolf back alive...");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("This beautiful flower holds within it the fierce-yet-loving soul of your Arcane Wolf.",
				"The wolf soul must be given mana before it can be used to bring the wolf back to life");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ENTITY;
	}

	@Override
	protected void setWorldID(LivingEntity pet, UUID worldID) {
		if (pet instanceof ArcaneWolfEntity) {
			((ArcaneWolfEntity) pet).setWorldID(worldID);
		}
	}

	@Override
	protected void beforePetRespawn(LivingEntity pet, Level world, Vec3 pos, ItemStack stack) {
		// Wolves spawn at 50% health and 0% mana
		if (pet instanceof ArcaneWolfEntity) {
			ArcaneWolfEntity wolf = ((ArcaneWolfEntity) pet);
			final int mana = wolf.getMana();
			final float maxHP = wolf.getMaxHealth();
			
			wolf.setHealth(Math.max(1, maxHP/2));
			wolf.addMana(-mana);
		}
		
		setMana(stack, 0);
	}
	
	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player playerIn, LivingEntity target, InteractionHand hand) {
		if (!playerIn.isCreative()) {
			return InteractionResult.PASS;
		}
		
		if (playerIn.level.isClientSide) {
			return InteractionResult.SUCCESS;
		}
		
		if (target instanceof ArcaneWolfEntity) {
			ItemStack newStack = MakeSoulItem((ArcaneWolfEntity) target);
			target.spawnAtLocation(newStack, 1);
			return InteractionResult.SUCCESS;
		}
		
		return InteractionResult.PASS;
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
		final ItemStack held = playerIn.getItemInHand(hand);
		if (getMana(held) >= getMaxMana(held)) {
			return new InteractionResultHolder<ItemStack>(InteractionResult.PASS, held);
		}
		
		playerIn.startUsingItem(hand);
		return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, held);
	}
	
	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BOW;
	}
	
	@Override
	public int getUseDuration(ItemStack stack) {
		return 20;
	}
	
	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity entityLiving) {
		if (worldIn.isClientSide) {
			return stack;
		}
		
		// Try to add mana into it
		INostrumMagic attr = NostrumMagica.getMagicWrapper(entityLiving);
		if (attr != null) {
			int avail = Math.min(200, attr.getMana());
			int used = addMana(stack, avail);
			if (used > 0) {
				attr.addMana(-used);
			}
		}
		
		return stack;
	}
	
	@Override
	public void onUsingTick(ItemStack stack, LivingEntity player, int count) {
		if (player.level.isClientSide) {
			// On client, spawn particles
			if (NostrumMagica.rand.nextBoolean()) {
				Vec3 offset;
				final float rotation;
				final Minecraft mc = Minecraft.getInstance();
				if (player == NostrumMagica.instance.proxy.getPlayer() && mc.options.getCameraType() == CameraType.FIRST_PERSON) {
					offset = new Vec3(-.1, player.getEyeHeight() -.05, .2);
					rotation = -player.getYRot() % 360f;
				} else {
					offset = new Vec3(-.375, player.getEyeHeight() -.05, .825);
					if (player == NostrumMagica.instance.proxy.getPlayer()) {
						rotation = -player.yBodyRot % 360f;
					} else {
						rotation = -player.getYRot() % 360f;
					}
				}
				
				final float rotRad = (float) ((rotation / 360f) * 2 * Math.PI);
				offset = offset.yRot(rotRad);
				offset = offset.add(player.position());
				// Need to adjust while watching to go to hand
				NostrumParticles.FILLED_ORB.spawn(player.level, new SpawnParams(
						1, offset.x, offset.y, offset.z, 1.0, 20, 0,
						offset
						).color(1f, .4f, .8f, 1f).dieOnTarget(true));
			}
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		
		String name = getPetName(stack);
		if (name == null || name.isEmpty()) {
			name = "Unknown Pet";
		}
		tooltip.add(new TextComponent(name).withStyle(ChatFormatting.DARK_RED));
		tooltip.add(new TextComponent(getMana(stack) + " / " + getMaxMana(stack)).withStyle(ChatFormatting.BLUE));
	}
	
	@Override
	public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entityItem) {
		if (entityItem.level.isClientSide) {
			// Particles!
			if (NostrumMagica.rand.nextBoolean()) {
				NostrumParticles.GLOW_ORB.spawn(entityItem.level, new SpawnParams(
						1, entityItem.getX(), entityItem.getY() + .5, entityItem.getZ(), .25, 30, 10,
						new Vec3(0, .05, 0), new Vec3(.025, 0, .025)
						).color(.3f, .6f, 0f, 0f));
			}
		}
		
		return false;
	}
	
	public int getMana(ItemStack stack) {
		CompoundTag nbt = stack.getTag();
		if (nbt == null) {
			nbt = new CompoundTag();
			stack.setTag(nbt);
		}
		
		return nbt.getInt(NBT_MANA);
	}
	
	public void setMana(ItemStack stack, int mana) {
		mana = Math.min(mana, getMaxMana(stack));
		
		CompoundTag nbt = stack.getTag();
		if (nbt == null) {
			nbt = new CompoundTag();
			stack.setTag(nbt);
		}
		
		nbt.putInt(NBT_MANA, mana);
	}
	
	/**
	 * Attempts to add some mana into the item.
	 * Returns how much was actually used.
	 * @param stack
	 * @param availableMana
	 * @return
	 */
	public int addMana(ItemStack stack, int availableMana) {
		final int mana = getMana(stack);
		final int room = getMaxMana(stack) - mana;
		if (room <= 0 || availableMana == 0) {
			return 0;
		} else if (room >= availableMana) {
			setMana(stack, mana + availableMana);
			return availableMana;
		} else {
			setMana(stack, mana + room);
			return room;
		}
	}
	
	public int getMaxMana(ItemStack stack) {
		return 500; // Could make it dynamic
	}
	
	public static ItemStack MakeSoulItem(ArcaneWolfEntity wolf) {
		return MakeSoulItem(wolf, true);
	}
	
	public static ItemStack MakeSoulItem(ArcaneWolfEntity wolf, boolean register) {
		if (register) {
			NostrumItems.arcaneWolfSoulItem.setWorldID(wolf, NostrumMagica.instance.getPetSoulRegistry().registerPet(wolf));
			NostrumMagica.instance.getPetSoulRegistry().snapshotPet(wolf);
		}
		
		ItemStack stack = new ItemStack(NostrumItems.arcaneWolfSoulItem);
		NostrumItems.arcaneWolfSoulItem.setPet(stack, wolf);
		
		return stack;
	}

	@Override
	public boolean canSpawnEntity(Level world, LivingEntity spawner, Vec3 pos, ItemStack stack) {
		if (this.getMana(stack) < this.getMaxMana(stack)) {
			spawner.sendMessage(new TranslatableComponent("info.respawn_soulbound_dragon.fail.mana", new Object[0]), Util.NIL_UUID);
			return false;
		}
		
		return true;
	}
}
