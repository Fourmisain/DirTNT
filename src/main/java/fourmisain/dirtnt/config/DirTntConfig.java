package fourmisain.dirtnt.config;

import com.google.common.collect.ImmutableSet;
import net.minecraft.resources.Identifier;

import java.util.Set;

public class DirTntConfig {

	public Set<Identifier> dirtTypes = ImmutableSet.of(
		Identifier.withDefaultNamespace("dirt"),
		Identifier.withDefaultNamespace("stone"),
		Identifier.withDefaultNamespace("sand"),
		Identifier.withDefaultNamespace("gravel")
	);

	public boolean enableAll = false;

}
