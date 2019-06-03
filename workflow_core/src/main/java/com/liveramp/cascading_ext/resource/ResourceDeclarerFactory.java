package com.liveramp.cascading_ext.resource;

import java.io.IOException;

public interface ResourceDeclarerFactory {
  public ResourceDeclarer create() throws IOException;
}
