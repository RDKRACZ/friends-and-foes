package com.faboslav.friendsandfoes.entity.ai.pathing;

import com.faboslav.friendsandfoes.entity.passive.GlareEntity;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.world.World;

public class GlareNavigation extends BirdNavigation
{
	int lastPathNodeBaseLightLevel = 1;

	public GlareNavigation(GlareEntity glare, World world) {
		super(glare, world);
	}

	protected void adjustPath() {
		super.adjustPath();

		GlareEntity glare = (GlareEntity) this.entity;
		World world = this.world;
		boolean isGlareTamed = glare.isTamed();
		boolean isSkyVisible = world.isSkyVisible(glare.getBlockPos());
		int glareBlockPosBaseLightLevel = world.getBaseLightLevel(glare.getBlockPos(), 0);
		boolean isGlareInDarkSpot = glareBlockPosBaseLightLevel <= 3;

		this.lastPathNodeBaseLightLevel = glareBlockPosBaseLightLevel;

		for (int i = 0; i < this.currentPath.getLength(); ++i) {
			PathNode pathNode = this.currentPath.getNode(i);
			int pathNodeBaseLightLevel = world.getBaseLightLevel(pathNode.getBlockPos(), 0);

			if (isGlareTamed || isSkyVisible) {
				continue;
			}

			if (
				isGlareInDarkSpot
				&& pathNodeBaseLightLevel >= this.lastPathNodeBaseLightLevel
			) {
				this.lastPathNodeBaseLightLevel = pathNodeBaseLightLevel;
				continue;
			}

			boolean isPathNodeDarkSpot = pathNodeBaseLightLevel == 0;

			if (isPathNodeDarkSpot) {
				this.currentPath.setLength(i);
				return;
			}
		}
	}
}
