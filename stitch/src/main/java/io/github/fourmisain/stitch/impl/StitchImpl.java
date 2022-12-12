package io.github.fourmisain.stitch.impl;

import io.github.fourmisain.stitch.api.SpriteRecipe;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/*
 * 1.19.3 rewrote texture stiching:
 *
 * The main logic is found in SpriteLoader.method_47661, it is called for each atlas, so for vanilla, 10 times.
 * It is a 3 step async process of:
 *  1. loading a List<Supplier<SpriteContents>> for the given atlas id
 *  2. executing each supplier asynchronously, resulting in a List<SpriteContents>
 *  3. adding the finished SpriteContents to the TextureStitcher and calling stitch()
 *
 * The threading makes modification difficult. Since these steps are chained method calls, we can't write
 * a simple (non-@Overwrite) Mixin to insert a step inbetween steps 2 and 3 to read the given List<SpriteContents>,
 * generate our new SpriteContents asynchronously and return a new list.
 *
 * Instead, this version of Stitch tries to do it's work inside method_47663, before the actual stitching is taking place.
 *
 * Issue: method_47663 does not take the resource manager and atlas identifier from method_47661.
 * To get around this we add a dummy Supplier<SpriteContents> which produces a DummySpriteContents, containing our parameters.
 * We of course remove that dummy before stitching again.
 * Search for "step 1" through "step 4" for how this is done exactly.
 *
 * Issue: method_47663 is run on an executor worker but we need to wait until our sprites are generated, which means we
 * are blocking this worker from working. This is both unoptimal and potentially dangerous - if there's more atlases than
 * workers, we've got ourselves a deadlock.
 *
 * This latter issue has no obvious solution. The idea of simply using an @Overwrite for method_47661 might be the right call afterall...
 *
 * Also note that this version of Stitch currently does not deal with recursive depencies, it only does a single vanilla -> generated pass.
 */
public class StitchImpl {
	public static final String MOD_ID = "stitch";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	/** atlas id -> sprite id -> recipe */
	public static final Map<Identifier, Map<Identifier, SpriteRecipe>> atlasRecipes = new LinkedHashMap<>();

	// passed state
	public static final ThreadLocal<Identifier> atlasId = ThreadLocal.withInitial(() -> null);
	public static final ThreadLocal<ResourceManager> resourceManager = ThreadLocal.withInitial(() -> null);
	public static final ThreadLocal<Executor> executor = ThreadLocal.withInitial(() -> null); // for SpriteLoaderMixins
}
