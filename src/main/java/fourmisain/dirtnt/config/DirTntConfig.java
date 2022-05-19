package fourmisain.dirtnt.config;

import net.minecraft.util.Identifier;

import java.util.Set;

public class DirTntConfig {

    public Set<Identifier> dirtTypes = Set.of(
			new Identifier("minecraft", "dirt"),
			new Identifier("minecraft", "stone"),
			new Identifier("minecraft", "sand"),
			new Identifier("minecraft", "gravel")
    );

}
