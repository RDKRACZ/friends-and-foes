package com.faboslav.friendsandfoes;

import com.faboslav.friendsandfoes.config.FriendsAndFoesConfig;
import com.faboslav.friendsandfoes.config.omegaconfig.OmegaConfig;
import com.faboslav.friendsandfoes.init.*;
import com.faboslav.friendsandfoes.util.ServerTickDeltaCounter;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FriendsAndFoes
{
	private static final FriendsAndFoesConfig CONFIG = OmegaConfig.register(FriendsAndFoesConfig.class);
	private static final Logger LOGGER = LoggerFactory.getLogger(FriendsAndFoes.MOD_ID);
	public static final String MOD_ID = "friendsandfoes";
	public static final ServerTickDeltaCounter serverTickDeltaCounter = new ServerTickDeltaCounter(20.0F, 0L);

	public static Identifier makeID(String path) {
		return new Identifier(
			MOD_ID,
			path
		);
	}

	public static String makeStringID(String name) {
		return MOD_ID + ":" + name;
	}

	public static FriendsAndFoesConfig getConfig() {
		return CONFIG;
	}

	public static Logger getLogger() {
		return LOGGER;
	}

	public static void initRegisters() {
		ModBlocks.initRegister();
		ModCriteria.init();
		ModEntity.initRegister();
		ModItems.initRegister();
		ModSounds.initRegister();
		ModVillagerProfessions.initRegister();
		ModStructureFeatures.initRegister();
	}

	public static void initCustomRegisters() {
		ModBlocks.init();
		ModEntity.init();
		ModItems.init();
		ModBlockEntityTypes.init();
		ModSounds.init();
		ModPointOfInterestTypes.init();
		ModVillagerProfessions.init();
		ModStructureFeatures.init();
	}
}
