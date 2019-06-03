package com.rapleaf.cascading_ext.workflow2.options;

public class FixedHostnameProvider implements HostnameProvider {
  @Override
  public String getHostname() {
    return "fake-host.example.com";
  }
}
