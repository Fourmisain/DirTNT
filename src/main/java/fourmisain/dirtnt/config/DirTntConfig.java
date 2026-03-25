package fourmisain.dirtnt.config;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.minecraft.resources.Identifier;

public class DirTntConfig {

	public Set<Identifier> dirtTypes = ImmutableSet.of(
		Identifier.withDefaultNamespace("dirt"),
		Identifier.withDefaultNamespace("stone"),
		Identifier.withDefaultNamespace("sand"),
		Identifier.withDefaultNamespace("gravel")
	);

	public boolean enableAll = false;

}
