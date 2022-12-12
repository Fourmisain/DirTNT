package fourmisain.dirtnt.config;

import com.google.common.collect.ImmutableSet;
import net.minecraft.util.Identifier;

import java.util.Set;

public class DirTntConfig {

    public Set<Identifier> dirtTypes = ImmutableSet.of(
			new Identifier("minecraft", "dirt"),
			new Identifier("minecraft", "stone"),
			new Identifier("minecraft", "sand"),
			new Identifier("minecraft", "gravel")
    );

	public boolean enableAll = false;

}
