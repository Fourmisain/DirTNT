package io.github.fourmisain.stitch.impl;

import net.minecraft.client.resource.metadata.AnimationResourceMetadata;

public interface AnimationDataAccess {
	AnimationResourceMetadata getAnimationData();
	void clearAnimationData(); // TODO make use of this to gain back memory after stitching
}
