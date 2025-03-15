package com.smanzana.nostrummagica.world.dungeon;

import java.util.Random;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.world.blueprints.BlueprintLocation;
import com.smanzana.autodungeons.world.dungeon.Dungeon;
import com.smanzana.autodungeons.world.dungeon.DungeonRoomInstance;
import com.smanzana.autodungeons.world.dungeon.room.DungeonStartRoom;
import com.smanzana.autodungeons.world.dungeon.room.IDungeonRoomRef;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class NostrumDungeon extends Dungeon {
	
	public NostrumDungeon(String tag, DungeonStartRoom starting, IDungeonRoomRef<?> ending, int minPath, int randPath) {
		super(tag, starting, ending, minPath, randPath);
	}
	
	public NostrumDungeon(String tag, DungeonStartRoom starting, IDungeonRoomRef<?> ending) {
		this(tag, starting, ending, 2, 3);
	}
	
	@Override
	protected void spawnDungeonParticles(World world, PlayerEntity player) {
		Random rand = player.level.random;
		final float range = 15;
		for (int i = 0; i < 15; i++) {
			NostrumParticles.GLOW_ORB.spawn(player.level, new SpawnParams(
				1, player.getX() + (rand.nextGaussian() * range), player.getY() + (rand.nextGaussian() * 4), player.getZ() + (rand.nextGaussian() * range), .5,
				80, 30,
				new Vector3d(0, .025, 0), new Vector3d(.01, .0125, .01)
				).color(color));
		}
	}
	
	@Override
	protected void spawnLargeKey(DungeonRoomInstance room, IWorld world, BlueprintLocation keyLocation) {
		// Technically, this spawns at two positions and could go out of bounds
		NostrumBlocks.largeDungeonKeyChest.makeDungeonChest(world, keyLocation.getPos(), keyLocation.getFacing(), room.getDungeonInstance());
	}
	
	@Override
	protected void spawnLargeDoor(DungeonRoomInstance room, IWorld world, BlueprintLocation doorLocation) {
		// Relying on there already being a door... could make large chest do the same?
		NostrumBlocks.largeDungeonDoor.overrideDungeonKey(world, doorLocation.getPos(), room.getDungeonInstance());
		
		// could if/else and use existing if it's th ere. same with large key?
	}
	
	@Override
	protected void spawnSmallKey(DungeonRoomInstance room, IWorld world, BlueprintLocation keyLocation) {
		NostrumBlocks.smallDungeonKeyChest.makeDungeonChest(world, keyLocation.getPos(), keyLocation.getFacing(), room.getDungeonInstance());
	}
	
	@Override
	protected void spawnSmallDoor(DungeonRoomInstance room, IWorld world, BlueprintLocation smallDoor, @Nullable MutableBoundingBox bounds) {
		NostrumBlocks.smallDungeonDoor.spawnDungeonDoor(world, smallDoor.getPos(), smallDoor.getFacing(), bounds, room.getDungeonInstance());
	}
}
