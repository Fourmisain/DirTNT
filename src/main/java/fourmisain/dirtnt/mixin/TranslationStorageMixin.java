package fourmisain.dirtnt.mixin;

import fourmisain.dirtnt.DirTnt;
import fourmisain.dirtnt.block.DirtTntBlock;
import fourmisain.dirtnt.entity.DirtTntEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.resource.language.LanguageDefinition;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static fourmisain.dirtnt.DirTnt.DIRT_TYPES;

@Environment(value= EnvType.CLIENT)
@Mixin(TranslationStorage.class)
public abstract class TranslationStorageMixin {
	@Inject(
			method = "load(Lnet/minecraft/resource/ResourceManager;Ljava/util/List;)Lnet/minecraft/client/resource/language/TranslationStorage;",
			at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap;copyOf(Ljava/util/Map;)Lcom/google/common/collect/ImmutableMap;", remap = false),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private static void dirtnt$addDependentTranslations(ResourceManager resourceManager, List<LanguageDefinition> definitions, CallbackInfoReturnable<TranslationStorage> cir, Map<String, String> translations) {
		for (Identifier dirtType : DIRT_TYPES) {
			Optional<Block> block = Registries.BLOCK.getOrEmpty(dirtType);
			if (block.isEmpty()) continue;

			DirtTntBlock tntBlock = DirTnt.BLOCK_MAP.get(dirtType);
			EntityType<DirtTntEntity> tntEntity = DirTnt.ENTITY_TYPE_MAP.get(dirtType);

			// auto-gen translations
			String name = translations.get(block.get().getTranslationKey()) + " TNT";
			if (tntBlock  != null) translations.putIfAbsent(tntBlock.getTranslationKey(),  name);
			if (tntEntity != null) translations.putIfAbsent(tntEntity.getTranslationKey(), name);
		}
	}
}
