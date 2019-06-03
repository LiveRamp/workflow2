package com.liveramp.cascading_ext.megadesk;

import java.util.Collection;

import com.rapleaf.cascading_ext.datastore.DataStore;

public class MockStoreReaderLockProvider implements StoreReaderLockProvider {

  @Override
  public IStoreReaderLocker create() {
    return new MockStoreReaderLocker();
  }

  public static class MockStoreReaderLocker implements IStoreReaderLocker {

    @Override
    public ResourceSemaphore createLock(DataStore store) {
      return new MockResourceSemaphore();
    }

    @Override
    public ResourceSemaphore createLock(String store) {
      return new MockResourceSemaphore();
    }

    @Override
    public ILockManager createManager(Collection<DataStore> datastores) {
      return new MockLockManager();
    }

    @Override
    public void shutdown() {

    }
  }

  public static class MockLockManager implements ILockManager {

    @Override
    public ILockManager lockProcessStart() {
      return this;
    }

    @Override
    public ILockManager lockConsumeStart() {
      return this;
    }

    @Override
    public ILockManager lock() {
      return this;
    }

    @Override
    public void release() {

    }
  }

}
