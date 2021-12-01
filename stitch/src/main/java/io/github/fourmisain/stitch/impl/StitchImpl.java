package io.github.fourmisain.stitch.impl;

import io.github.fourmisain.stitch.api.SpriteRecipe;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;

public class StitchImpl {
	public static final Logger LOGGER = LogManager.getLogger("stitch");

	/** atlas id -> sprite id -> recipe */
	public static Map<Identifier, Map<Identifier, SpriteRecipe>> atlasRecipes = new LinkedHashMap<>();
}
