package fourmisain.dirtnt.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.llamalad7.mixinextras.sugar.Local;
import fourmisain.dirtnt.DirTnt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.storage.loot.LootDataType;

import static fourmisain.dirtnt.DirTnt.DIRT_TYPES;
import static fourmisain.dirtnt.DirTnt.getDirtTntBlockId;

@Mixin(ReloadableServerRegistries.class)
public abstract class ReloadableRegistriesMixin {
	@Inject(
		method = "method_61240", // prepare lambda
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V"
		)
	)
	private static <T> void addDirTntLootTables(LootDataType<T> type, ResourceManager resourceManager, RegistryOps<JsonElement> ops, CallbackInfoReturnable<WritableRegistry<?>> cir,
			@Local Map<Identifier, T> lootTables) {
		if (!type.registryKey().equals(Registries.LOOT_TABLE))
			return;

		for (Identifier dirtType : DIRT_TYPES) {
			Identifier id = getDirtTntBlockId(dirtType);
			Identifier tableId = DirTnt.id("blocks/" + id.getPath());

			try (Reader reader = new StringReader(DirTnt.getLootTableJson(id))) {
				type.codec().parse(ops, JsonParser.parseReader(reader)).ifSuccess(value -> {
					lootTables.putIfAbsent(tableId, value); // ignore if it exists already
				}).ifError(error -> DirTnt.LOGGER.error("Couldn't parse loot table '{}': {}", tableId, error));
			} catch (IllegalArgumentException | IOException | JsonParseException e) {
				DirTnt.LOGGER.error("Couldn't parse loot table '{}'", tableId, e);
			}
		}
	}
}
