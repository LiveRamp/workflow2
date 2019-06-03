package com.liveramp.workflow2.workflow_hadoop;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import com.liveramp.cascading_ext.resource.Storage;
import com.liveramp.commons.collections.map.MapBuilder;
import com.liveramp.databases.workflow_db.DatabasesImpl;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.rapleaf.cascading_ext.workflow2.WorkflowTestCase;

import static com.liveramp.commons.test.TestUtils.assertCollectionEquivalent;

public abstract class BaseTestStorage<STORAGE extends Storage.Factory<ROOT>, ROOT> extends WorkflowTestCase {

  protected static final Map<String, Integer> map = new MapBuilder<String, Integer>()
      .of("x", 1).put("y", 2).put("z", 3).get();
  protected static final Set<Long> set = Sets.newHashSet(Long.MAX_VALUE, Long.MIN_VALUE, -1L, 0L);
  protected static final String MAP_NAME = "MapResource";
  protected static final String SET_NAME = "SetResource";

  private STORAGE factory;

  protected abstract STORAGE createStorage();
  protected abstract ROOT createRoot() throws IOException;

  @Before
  public void prepare() {
    factory = createStorage();
  }

  @Test
  public void testStoreAndRetrieve() throws IOException {
    Storage storage = factory.forResourceRoot(createRoot());
    storage.store(MAP_NAME, map);
    Map<String, Integer> retrievedMap = storage.retrieve(MAP_NAME);
    assertCollectionEquivalent(map.entrySet(), retrievedMap.entrySet());
  }

  @SuppressWarnings("Duplicates")
  @Test
  public void testReinitialize() throws IOException {
    Storage storage = factory.forResourceRoot(createRoot());
    storage.store(MAP_NAME, map);
    assert(storage.isStored(MAP_NAME));
    assert(storage.retrieve(MAP_NAME) != null);
    Storage storage2 = factory.forResourceRoot(createRoot());
    assert(!storage2.isStored(MAP_NAME));
    assert(storage2.retrieve(MAP_NAME) == null);
  }

  @Test
  public void testStoreAndRetrieve2() throws IOException {
    Storage storage = factory.forResourceRoot(createRoot());
    storage.store(SET_NAME, set);
    Set<Long> retrievedSet = storage.retrieve(SET_NAME);
    assertCollectionEquivalent(retrievedSet, set);
  }

  @SuppressWarnings("Duplicates")
  @Test
  public void testPersistence() throws IOException {
    Storage storage = factory.forResourceRoot(createRoot());
    storage.store(SET_NAME, set);
    createStorage();
    assert(storage.isStored(SET_NAME));
    Set<Long> retrievedSet = storage.retrieve(SET_NAME);
    assertCollectionEquivalent(retrievedSet, set);
  }

}
