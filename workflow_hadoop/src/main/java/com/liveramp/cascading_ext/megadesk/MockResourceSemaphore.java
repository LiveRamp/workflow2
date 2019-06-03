package com.liveramp.cascading_ext.megadesk;

public class MockResourceSemaphore implements ResourceSemaphore {
  @Override
  public void lock() {

  }

  @Override
  public void release() {

  }

  @Override
  public boolean hasReaders() {
    return false;
  }
}
