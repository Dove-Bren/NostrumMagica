package com.smanzana.nostrummagica.item;

import java.util.List;
import java.util.UUID;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.entity.dragon.EntityTameDragonRed;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DragonSoulItem extends PetSoulItem {
	
	public static final String ID = "dragon_soul_item";
	private static final String NBT_MANA = "mana";
	
	public DragonSoulItem() {
		super(NostrumItems.PropUnstackable().rarity(Rarity.EPIC));
	}

	@Override
	public String getLoreKey() {
		return "dragon_soul_items";
	}

	@Override
	public String getLoreDisplayName() {
		return "Dragon Souls";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("An intense energy radiates from this hunk of crystaline flesh as it glows.",
				"Inside is the soul of the dragon you obtained it from.",
				"The dragon soul is powerful, but incomplete. More magical energy is needed to bring the dragon back alive...");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("An intense energy radiates from this hunk of crystaline flesh as it glows.",
				"Inside is the soul of the dragon you obtained it from.",
				"The dragon soul must be given mana before it can be used to bring the dragon back to life");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_DRAGONS;
	}

	@Override
	protected void setWorldID(LivingEntity pet, UUID worldID) {
		if (pet instanceof EntityTameDragonRed) { // TODO new base class lol
			((EntityTameDragonRed) pet).setWorldID(worldID);
		}
	}

	@Override
	protected void beforePetRespawn(LivingEntity pet, World world, Vector3d pos, ItemStack stack) {
		// Dragons spawn at 50% health and 0% mana
		if (pet instanceof EntityTameDragonRed) { // TODO new base class lol
			EntityTameDragonRed dragon = ((EntityTameDragonRed) pet);
			final int mana = dragon.getMana();
			final float maxHP = dragon.getMaxHealth();
			
			dragon.setHealth(Math.max(1, maxHP/2));
			dragon.addMana(-mana);
		}
		
		setMana(stack, 0);
	}
	
	@Override
	public ActionResultType itemInteractionForEntity(ItemStack stack, PlayerEntity playerIn, LivingEntity target, Hand hand) {
		if (!playerIn.isCreative()) {
			return ActionResultType.PASS;
		}
		
		if (playerIn.world.isRemote) {
			return ActionResultType.SUCCESS;
		}
		
		if (target instanceof EntityTameDragonRed) {
			ItemStack newStack = MakeSoulItem((EntityTameDragonRed) target);
			target.entityDropItem(newStack, 1);
			return ActionResultType.SUCCESS;
		}
		
		return ActionResultType.PASS;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
		final ItemStack held = playerIn.getHeldItem(hand);
		if (getMana(held) >= getMaxMana(held)) {
			return new ActionResult<ItemStack>(ActionResultType.PASS, held);
		}
		
		playerIn.setActiveHand(hand);
		return new ActionResult<ItemStack>(ActionResultType.SUCCESS, held);
	}
	
	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.BOW;
	}
	
	@Override
	public int getUseDuration(ItemStack stack) {
		return 20;
	}
	
	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, LivingEntity entityLiving) {
		if (worldIn.isRemote) {
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
		if (player.world.isRemote) {
			// On client, spawn particles
			if (NostrumMagica.rand.nextBoolean()) {
				Vector3d offset;
				final float rotation;
				if (player == NostrumMagica.instance.proxy.getPlayer() && Minecraft.getInstance().gameSettings.getPointOfView() == PointOfView.FIRST_PERSON) {
					offset = new Vector3d(-.1, player.getEyeHeight() -.05, .2);
					rotation = -player.rotationYaw % 360f;
				} else {
					offset = new Vector3d(-.375, player.getEyeHeight() -.05, .825);
					if (player == NostrumMagica.instance.proxy.getPlayer()) {
						rotation = -player.renderYawOffset % 360f;
					} else {
						rotation = -player.rotationYaw % 360f;
					}
				}
				
				final float rotRad = (float) ((rotation / 360f) * 2 * Math.PI);
				offset = offset.rotateYaw(rotRad);
				offset = offset.add(player.getPositionVec());
				// Need to adjust while watching to go to hand
				NostrumParticles.FILLED_ORB.spawn(player.world, new SpawnParams(
						1, offset.x, offset.y, offset.z, 1.0, 20, 0,
						offset
						).color(1f, .4f, .8f, 1f).dieOnTarget(true));
			}
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		
		String name = getPetName(stack);
		if (name == null || name.isEmpty()) {
			name = "Unknown Pet";
		}
		tooltip.add(new StringTextComponent(name).mergeStyle(TextFormatting.DARK_RED));
		tooltip.add(new StringTextComponent(getMana(stack) + " / " + getMaxMana(stack)).mergeStyle(TextFormatting.BLUE));
	}
	
	@Override
	public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entityItem) {
		if (entityItem.world.isRemote) {
			// Particles!
			if (NostrumMagica.rand.nextBoolean()) {
				NostrumParticles.GLOW_ORB.spawn(entityItem.world, new SpawnParams(
						1, entityItem.getPosX(), entityItem.getPosY() + .5, entityItem.getPosZ(), .25, 30, 10,
						new Vector3d(0, .05, 0), new Vector3d(.025, 0, .025)
						).color(.3f, .6f, 0f, 0f));
			}
		}
		
		return false;
	}
	
	public int getMana(ItemStack stack) {
		CompoundNBT nbt = stack.getTag();
		if (nbt == null) {
			nbt = new CompoundNBT();
			stack.setTag(nbt);
		}
		
		return nbt.getInt(NBT_MANA);
	}
	
	public void setMana(ItemStack stack, int mana) {
		mana = Math.min(mana, getMaxMana(stack));
		
		CompoundNBT nbt = stack.getTag();
		if (nbt == null) {
			nbt = new CompoundNBT();
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
		return 3000; // Could make it dynamic
	}
	
	public static ItemStack MakeSoulItem(EntityTameDragonRed dragon) {
		return MakeSoulItem(dragon, true);
	}
	
	public static ItemStack MakeSoulItem(EntityTameDragonRed dragon, boolean register) {
		if (register) {
			NostrumItems.dragonSoulItem.setWorldID(dragon, NostrumMagica.instance.getPetSoulRegistry().registerPet(dragon));
			NostrumMagica.instance.getPetSoulRegistry().snapshotPet(dragon);
		}
		
		ItemStack stack = new ItemStack(NostrumItems.dragonSoulItem);
		NostrumItems.dragonSoulItem.setPet(stack, dragon);
		
		return stack;
	}

	@Override
	public boolean canSpawnEntity(World world, LivingEntity spawner, Vector3d pos, ItemStack stack) {
		if (this.getMana(stack) < this.getMaxMana(stack)) {
			spawner.sendMessage(new TranslationTextComponent("info.respawn_soulbound_dragon.fail.mana", new Object[0]), Util.DUMMY_UUID);
			return false;
		}
		
		return true;
	}
}
