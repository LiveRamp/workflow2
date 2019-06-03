package com.liveramp.cascading_ext.resource;

import java.io.IOException;

public interface RootDeterminer<RESOURCE_ROOT> {
  RESOURCE_ROOT getResourceRoot(long version, String versionType) throws IOException;
}
