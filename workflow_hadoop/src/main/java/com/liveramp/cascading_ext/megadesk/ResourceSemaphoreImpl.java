package com.liveramp.cascading_ext.megadesk;

import java.util.List;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreV2;
import org.apache.curator.framework.recipes.locks.Lease;

public class ResourceSemaphoreImpl implements ResourceSemaphore {


  private final List<InterProcessSemaphoreV2> semaphores = Lists.newArrayList();
  private final Multimap<InterProcessSemaphoreV2, Lease> leasesPerSemaphore = HashMultimap.create();

  public static final String READER_LOCK_BASE_PATH = "/clockwork/datastore_reader_locks/";
  private final String path;
  private final String name;

  public ResourceSemaphoreImpl(CuratorFramework framework,
                               String resourceUniqueIdentifer, String name) {
    this(Lists.newArrayList(framework), resourceUniqueIdentifer, name);
  }

  public ResourceSemaphoreImpl(List<CuratorFramework> frameworks,
                               String resourceUniqueIdentifer, String name) {
    this.name = name;
    this.path = READER_LOCK_BASE_PATH + resourceUniqueIdentifer;

    for (CuratorFramework framework : frameworks) {
      this.semaphores.add(new InterProcessSemaphoreV2(framework, path, Integer.MAX_VALUE));
    }

    for (InterProcessSemaphoreV2 semaphore : semaphores) {
      initNode(semaphore);
    }

  }

  private void initNode(InterProcessSemaphoreV2 semaphore) {
    try {
      Lease acquire = semaphore.acquire();
      semaphore.getParticipantNodes();
      semaphore.returnLease(acquire);
      //semaphore.getParticipantNodes();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void lock() {
    try {

      for (InterProcessSemaphoreV2 semaphore : semaphores) {
        Lease lease = semaphore.acquire();
        leasesPerSemaphore.put(semaphore, lease);
      }

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void release() {
    try {

      for (InterProcessSemaphoreV2 semaphore : semaphores) {
        semaphore.returnAll(leasesPerSemaphore.get(semaphore));
        leasesPerSemaphore.removeAll(semaphore);
      }

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean hasReaders() {
    try {

      for (InterProcessSemaphoreV2 semaphore : semaphores) {
        if (semaphore.getParticipantNodes().size() > 0) {
          return true;
        }
      }

      return false;

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String toString() {
    return "ResourceSemaphoreImpl{" +
        "path='" + path + '\'' +
        ", name='" + name + '\'' +
        '}';
  }
}
