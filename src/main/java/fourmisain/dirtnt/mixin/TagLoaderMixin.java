package fourmisain.dirtnt.mixin;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagLoader;
import net.minecraft.tags.TagLoader.EntryWithSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static fourmisain.dirtnt.DirTnt.*;

@Mixin(TagLoader.class)
public abstract class TagLoaderMixin {
	@Shadow @Final
	private String directory;

	// runs on worker thread
	@Inject(method = "load", at = @At("RETURN"))
	public void addDirtTntTags(ResourceManager resourceManager, CallbackInfoReturnable<Map<Identifier, List<EntryWithSource>>> cir) {
		var builders = cir.getReturnValue();

		if (directory.equals("tags/block")) {
			builders.compute(BlockTags.ENDERMAN_HOLDABLE.location(), addDirtTntEntries());
		} else if (directory.equals("tags/item")) {
			builders.compute(ItemTags.SULFUR_CUBE_SWALLOWABLE.location(), addDirtTntEntries());
			builders.compute(ItemTags.SULFUR_CUBE_ARCHETYPE_EXPLOSIVE.location(), addDirtTntEntries());
		}
	}

	@Unique
	private static BiFunction<Identifier, List<EntryWithSource>, List<EntryWithSource>> addDirtTntEntries() {
		return (k, entries) -> {
			var newEntries = (entries == null ? new ArrayList<EntryWithSource>() : entries);

			for (Identifier dirtType : DIRT_TYPES) {
				Identifier id = getDirtTntBlockId(dirtType);
				newEntries.add(new EntryWithSource(TagEntry.element(id), MOD_ID));
			}

			return newEntries;
		};
	}
}
