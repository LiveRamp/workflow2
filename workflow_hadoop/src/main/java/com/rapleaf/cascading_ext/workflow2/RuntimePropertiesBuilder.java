package com.rapleaf.cascading_ext.workflow2;

import java.util.Collections;

import com.liveramp.commons.collections.properties.NestedProperties;
import com.liveramp.java_support.workflow.ActionId;

public interface RuntimePropertiesBuilder {
  public NestedProperties build(ActionId step, FsActionContext context);

  public static class None implements RuntimePropertiesBuilder {

    @Override
    public NestedProperties build(ActionId step, FsActionContext context) {
      return new NestedProperties(Collections.emptyMap(), false);
    }
  }
}
