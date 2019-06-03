package com.rapleaf.cascading_ext.workflow2.options;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class DefaultHostnameProvider implements HostnameProvider {
  @Override
  public String getHostname() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    }
  }
}
