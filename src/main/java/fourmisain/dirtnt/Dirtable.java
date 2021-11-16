package fourmisain.dirtnt;

import net.minecraft.util.Identifier;

public interface Dirtable {
	void makeDirty(Identifier dirtType);
	Identifier getDirtType();

	default boolean isDirty() {
		return getDirtType() != null;
	}
}
