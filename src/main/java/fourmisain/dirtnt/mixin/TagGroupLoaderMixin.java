package fourmisain.dirtnt.mixin;

import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagEntry;
import net.minecraft.registry.tag.TagGroupLoader;
import net.minecraft.registry.tag.TagGroupLoader.TrackedEntry;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static fourmisain.dirtnt.DirTnt.*;

@Mixin(TagGroupLoader.class)
public abstract class TagGroupLoaderMixin {
	@Shadow @Final
	private String dataType;

	// runs on worker thread
	@Inject(method = "loadTags", at = @At("RETURN"))
	public void addTntToEndermanHoldableTag(ResourceManager resourceManager, CallbackInfoReturnable<Map<Identifier, List<TrackedEntry>>> cir) {
		if (!dataType.equals("tags/block"))
			return;

		cir.getReturnValue().compute(BlockTags.ENDERMAN_HOLDABLE.id(), (k, entries) -> {
			var newEntries = (entries == null ? new ArrayList<TrackedEntry>() : entries);

			for (Identifier dirtType : DIRT_TYPES) {
				Identifier blockId = getDirtTntBlockId(dirtType);
				newEntries.add(new TrackedEntry(TagEntry.create(blockId), MOD_ID));
			}

			return newEntries;
		});
	}
}
