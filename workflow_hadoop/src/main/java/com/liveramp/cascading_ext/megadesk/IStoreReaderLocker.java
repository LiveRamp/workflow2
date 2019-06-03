package com.liveramp.cascading_ext.megadesk;

import java.util.Collection;

import com.rapleaf.cascading_ext.datastore.DataStore;

public interface IStoreReaderLocker {

  public ResourceSemaphore createLock(DataStore store);

  public ResourceSemaphore createLock(String store);

  public ILockManager createManager(Collection<DataStore> datastores);

  public void shutdown();

}
