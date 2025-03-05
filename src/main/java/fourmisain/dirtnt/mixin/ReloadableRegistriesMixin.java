package fourmisain.dirtnt.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.llamalad7.mixinextras.sugar.Local;
import fourmisain.dirtnt.DirTnt;
import net.minecraft.loot.LootDataType;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.ReloadableRegistries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

import static fourmisain.dirtnt.DirTnt.DIRT_TYPES;
import static fourmisain.dirtnt.DirTnt.getDirtTntBlockId;

@Mixin(ReloadableRegistries.class)
public abstract class ReloadableRegistriesMixin {
	@Inject(
		method = "method_61240", // prepare lambda
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V"
		)
	)
	private static <T> void addDirTntLootTables(LootDataType<T> type, ResourceManager resourceManager, RegistryOps<JsonElement> ops, CallbackInfoReturnable<MutableRegistry<?>> cir,
			@Local Map<Identifier, T> lootTables) {
		if (!type.registryKey().equals(RegistryKeys.LOOT_TABLE))
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
