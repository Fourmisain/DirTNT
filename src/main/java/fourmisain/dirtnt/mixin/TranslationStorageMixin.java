package fourmisain.dirtnt.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import fourmisain.dirtnt.DirTnt;
import fourmisain.dirtnt.block.DirtTntBlock;
import fourmisain.dirtnt.entity.DirtTntEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

import static fourmisain.dirtnt.DirTnt.DIRT_TYPES;

@Mixin(ClientLanguage.class)
public abstract class TranslationStorageMixin {
	@Inject(
		method = "loadFrom(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/List;Z)Lnet/minecraft/client/resources/language/ClientLanguage;",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/Map;copyOf(Ljava/util/Map;)Ljava/util/Map;"
		)
	)
	private static void dirtnt$addDependentTranslations(ResourceManager resourceManager, List<String> definitions, boolean rightToLeft, CallbackInfoReturnable<ClientLanguage> cir,
			@Local Map<String, String> translations) {
		for (Identifier dirtType : DIRT_TYPES) {
			Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(dirtType);
			if (block.isEmpty()) continue;

			DirtTntBlock tntBlock = DirTnt.BLOCK_MAP.get(dirtType);
			EntityType<DirtTntEntity> tntEntity = DirTnt.ENTITY_TYPE_MAP.get(dirtType);

			// auto-gen translations
			String name = translations.get(block.get().getDescriptionId()) + " TNT";
			if (tntBlock  != null) translations.putIfAbsent(tntBlock.getDescriptionId(),  name);
			if (tntEntity != null) translations.putIfAbsent(tntEntity.getDescriptionId(), name);
		}
	}
}
