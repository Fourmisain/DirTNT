package fourmisain.dirtnt.config;

import com.google.common.collect.ImmutableSet;
import net.minecraft.util.Identifier;

import java.util.Set;

public class DirTntConfig {

	public Set<Identifier> dirtTypes = ImmutableSet.of(
		Identifier.ofVanilla("dirt"),
		Identifier.ofVanilla("stone"),
		Identifier.ofVanilla("sand"),
		Identifier.ofVanilla("gravel")
	);

	public boolean enableAll = false;

}
