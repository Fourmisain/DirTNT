package fourmisain.dirtnt.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagLoader;
import net.minecraft.tags.TagLoader.EntryWithSource;

import static fourmisain.dirtnt.DirTnt.*;

@Mixin(TagLoader.class)
public abstract class TagGroupLoaderMixin {
	@Shadow @Final
	private String directory;

	// runs on worker thread
	@Inject(method = "load", at = @At("RETURN"))
	public void addTntToEndermanHoldableTag(ResourceManager resourceManager, CallbackInfoReturnable<Map<Identifier, List<EntryWithSource>>> cir) {
		if (!directory.equals("tags/block"))
			return;

		cir.getReturnValue().compute(BlockTags.ENDERMAN_HOLDABLE.location(), (k, entries) -> {
			var newEntries = (entries == null ? new ArrayList<EntryWithSource>() : entries);

			for (Identifier dirtType : DIRT_TYPES) {
				Identifier blockId = getDirtTntBlockId(dirtType);
				newEntries.add(new EntryWithSource(TagEntry.element(blockId), MOD_ID));
			}

			return newEntries;
		});
	}
}
