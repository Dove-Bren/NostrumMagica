package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.autodungeons.world.blueprints.BlueprintLocation;
import com.smanzana.autodungeons.world.dungeon.DungeonInstance;
import com.smanzana.autodungeons.world.dungeon.room.DungeonExitData;
import com.smanzana.autodungeons.world.dungeon.room.DungeonRoomExit;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.block.dungeon.SingleSpawnerBlock;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.equipment.SpellScroll;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.RegisteredSpell;
import com.smanzana.nostrummagica.spell.SpellType;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.component.SpellShapePart;
import com.smanzana.nostrummagica.spell.component.shapes.NostrumSpellShapes;
import com.smanzana.nostrummagica.tile.AltarTileEntity;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeons;
import com.smanzana.nostrummagica.world.dungeon.NostrumOverworldDungeon;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneWallTorchBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class RoomLectern extends StaticRoom {
	
	public static final ResourceLocation ID = NostrumMagica.Loc("room_lectern");
	
	public RoomLectern() {
		super(ID, -18, -1, 0, 2, 4, 6,
				// Floor
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				// Layer 1
				"XXXXXXXXXXXXXXXXXXCXX",
				"X                 C X",
				"X QQQT            C X",
				"XGQQQTCCCCCCCCCCCCC X",
				"X QQQT            C X",
				"X                 C X",
				"XXXXXXXXXXXXXXXXXXCXX",
				// Layer 2
				"XXXXXXXXXXXXXXXXXX XX",
				"X        S          X",
				"X                  WX",
				"X  L                X",
				"X                  WX",
				"X        N          X",
				"XXXXXXXXXXXXXXXXXX XX",
				// Layer 3
				"XXXXXXXXXXXXXXXXXXXXX",
				"X                   X",
				"XE                  X",
				"X                   X",
				"XE                  X",
				"X                   X",
				"XXXXXXXXXXXXXXXXXXXXX",
				// Layer 4
				"XXXXXXXXXXXXXXXXXXXXX",
				"X                   X",
				"X                   X",
				"X                   X",
				"X                   X",
				"X                   X",
				"XXXXXXXXXXXXXXXXXXXXX",
				// Layer 5
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXX",
				'X', NostrumBlocks.lightDungeonBlock,
				'W', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.defaultBlockState().setValue(RedstoneWallTorchBlock.FACING, Direction.WEST)),
				'E', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.defaultBlockState().setValue(RedstoneWallTorchBlock.FACING, Direction.EAST)),
				'N', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.defaultBlockState().setValue(RedstoneWallTorchBlock.FACING, Direction.NORTH)),
				'S', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.defaultBlockState().setValue(RedstoneWallTorchBlock.FACING, Direction.SOUTH)),
				'C', new StaticBlockState(Blocks.RED_CARPET),
				'T', new StaticBlockState(Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST).setValue(StairBlock.HALF, Half.BOTTOM).setValue(StairBlock.SHAPE, StairsShape.STRAIGHT)),
				'Q', Blocks.QUARTZ_BLOCK,
				'G', new StaticBlockState(NostrumBlocks.singleSpawner.getState(SingleSpawnerBlock.Type.GOLEM_FIRE)),
				'L', NostrumBlocks.altar,
				' ', null);
		// T STAIR, Q QUARTZ, L PEDESTAL+SPELL,  NW norsou, g golem
	}

	@Override
	public int getNumExits() {
		return 1;
	}

	@Override
	public List<DungeonRoomExit> getExits(BlueprintLocation start) {
		List<DungeonRoomExit> list = new ArrayList<>();
		
		BlockPos exit = new BlockPos(0, 0, 6);
		
		list.add(new DungeonRoomExit(NostrumOverworldDungeon.asRotated(start, exit, Direction.NORTH), DungeonExitData.EMPTY));
		
		return list;
	}

	@Override
	public int getDifficulty() {
		return 4;
	}

	@Override
	public boolean hasEnemies() {
		return true;
	}

	@Override
	public boolean hasTraps() {
		return false;
	}

	@Override
	public boolean supportsDoor() {
		return false;
	}

	@Override
	public boolean supportsKey() {
		return false;
	}

	@Override
	public BlueprintLocation getKeyLocation(BlueprintLocation start) {
		return null;
	}
	
	@Override
	public boolean supportsTreasure() {
		return false;
	}

	@Override
	public List<BlueprintLocation> getTreasureLocations(BlueprintLocation start) {
		return new LinkedList<>();
	}
	
	static private String benePrefixs[] = {
			"Kiss of",
			"Boon of",
			"Blessing of",
			"Grace of",
			"",
			"",
	};
	
	static private String negeDamagePrefixes[] = {
			"Wrath of",
			"Fist of",
			"Anger of",
			"Spit of",
			"Burst of",
			"Fury of",
			"Blade of",
			"",
	};
	
	static private String negeStatusPrefixes[] = {
			"Will of",
			"Disgrace of",
			"Burden of",
			"Curse of",
			"Scorn of",
	};
	
	static private String genSpellName(Random rand, EMagicElement elem, boolean harmful, boolean status)
	{
		StringBuilder builder = new StringBuilder();
		if (harmful) {
			if (status) {
				builder.append(negeStatusPrefixes[rand.nextInt(negeStatusPrefixes.length)]);
			} else {
				builder.append(negeDamagePrefixes[rand.nextInt(negeDamagePrefixes.length)]);
			}
		} else {
			builder.append(benePrefixs[rand.nextInt(benePrefixs.length)]);
		}
		
		String elemStr;
		switch (elem) {
		case EARTH:
			elemStr = "The Earth";
			break;
		case ENDER:
			elemStr = "The End";
			break;
		case FIRE:
			elemStr = "The Inferno";
			break;
		case ICE:
			elemStr = "The Ice";
			break;
		case LIGHTNING:
			elemStr = "The Lightning";
			break;
		case PHYSICAL:
			elemStr = "Metal";
			break;
		case WIND:
			elemStr = "The Storm";
			break;
		default:
			elemStr = "";
			break;
		
		}
		
		builder.append(" ");
		builder.append(elemStr);
		
		
		return builder.toString();
	}
	
	static private RegisteredSpell genSpell(Random rand) {
		EMagicElement element;
		boolean harmful = false;
		boolean status = false;
		
		// Determine what kind of spell it's going to be!
		element = EMagicElement.values()[rand.nextInt(EMagicElement.values().length)];
		harmful = rand.nextBoolean();
		if (harmful) {
			status = rand.nextBoolean();
		}
		
		// Build the spell
		RegisteredSpell spell = RegisteredSpell.MakeAndRegister(genSpellName(rand, element, harmful, status), SpellType.Crafted, 75, 3);
		if (harmful) {
			
			int roll = rand.nextInt(10);
			if (roll < 1) {
				spell.addPart(new SpellShapePart(NostrumSpellShapes.Beam));
			} else if (roll < 2) {
				spell.addPart(new SpellShapePart(NostrumSpellShapes.Chain));
			} else if (roll < 4) {
				spell.addPart(new SpellShapePart(NostrumSpellShapes.Projectile));
				spell.addPart(new SpellShapePart(NostrumSpellShapes.Burst));
			} else if (roll < 6) {
				spell.addPart(new SpellShapePart(NostrumSpellShapes.Touch));
			} else if (roll < 8) {
				spell.addPart(new SpellShapePart(NostrumSpellShapes.Projectile));
			} else {
				spell.addPart(new SpellShapePart(NostrumSpellShapes.Ring));
			}
			
			int effects = 1;
			if (rand.nextBoolean() && rand.nextBoolean())
				effects++;
			
			for (int i = 0; i < effects; i++) {
				int potency = 1;
				if (rand.nextBoolean())
					potency++;
				
				EAlteration alt = null;
				if (status) {
					alt = EAlteration.INFLICT;
				}
				
				spell.addPart(new SpellEffectPart(element, potency, alt));
			}
		} else {
			boolean self = rand.nextBoolean();
			if (!self) {
				spell.addPart(new SpellShapePart(NostrumSpellShapes.Projectile));
			}
			
			// Roll for chaining or bursting
			if (rand.nextBoolean() && rand.nextBoolean()) {
				if (rand.nextBoolean()) {
					spell.addPart(new SpellShapePart(NostrumSpellShapes.Chain));
				} else {
					spell.addPart(new SpellShapePart(NostrumSpellShapes.Burst));
				}
			}
			
			int effects = 1;
			if (rand.nextBoolean() && rand.nextBoolean())
				effects++;
			if (rand.nextBoolean() && rand.nextBoolean())
				effects++;
			
			List<EAlteration> alts = Lists.newArrayList(
					EAlteration.RESIST,
					EAlteration.SUPPORT,
					EAlteration.SUMMON
					);
			if (element != EMagicElement.FIRE) {
				alts.add(EAlteration.GROWTH);
			}
			
			for (int i = 0; i < effects; i++) {
			
				int potency = 1;
				if (rand.nextInt(6) == 0)
					potency = 2;
				spell.addPart(new SpellEffectPart(element, potency, alts.get(rand.nextInt(alts.size()))));
			}
		}
		
		return spell;
	}
	
	@Override
	public void spawn(LevelAccessor world, BlueprintLocation start, @Nullable BoundingBox bounds, DungeonInstance dungeonInstance, UUID roomID)
	{
		super.spawn(world, start, bounds, dungeonInstance, roomID);
		
		// Fill out lectern!
		BlockPos offset = new BlockPos(-15, 1, 3);
		BlueprintLocation point = NostrumOverworldDungeon.asRotated(start, offset, start.getFacing());
		BlockPos pos = point.getPos();
		
		if (bounds == null || bounds.isInside(pos)) {
			BlockEntity ent = world.getBlockEntity(pos);
			if (null == ent)
			{
				System.out.println("Could not find lectern! (" + pos.getX() + " " + pos.getY() + " " + pos.getZ());
				world.setBlock(pos, Blocks.BEDROCK.defaultBlockState(), 2);
			}
			else
			{
				AltarTileEntity te = (AltarTileEntity) ent;
				ItemStack scroll = new ItemStack(NostrumItems.spellScroll, 1);
				RegisteredSpell spell = genSpell(world.getRandom());
				SpellScroll.setSpell(scroll, spell);
				scroll.setDamageValue(NostrumMagica.rand.nextInt(10));
				
				if (world instanceof WorldGenRegion) {
					te.setItemNoDirty(scroll);
				} else {
					te.setItem(scroll);
				}
			}
		}
	}

	@Override
	public List<String> getRoomTags() {
		return Lists.newArrayList(NostrumDungeons.TAG_DRAGON, NostrumDungeons.TAG_PLANTBOSS, NostrumDungeons.TAG_PORTAL);
	}

	@Override
	public String getRoomName() {
		return "Magic Lectern";
	}
}
