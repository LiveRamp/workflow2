package com.liveramp.resource_db_manager;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import com.liveramp.cascading_ext.resource.JavaObjectStorageSerializer;
import com.liveramp.cascading_ext.resource.ReadResource;
import com.liveramp.cascading_ext.resource.Resource;
import com.liveramp.cascading_ext.resource.ResourceManager;
import com.liveramp.cascading_ext.resource.Storage;
import com.liveramp.cascading_ext.resource.StorageSerializer;
import com.liveramp.commons.Accessors;
import com.liveramp.commons.collections.map.MapBuilder;
import com.liveramp.databases.workflow_db.DatabasesImpl;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.databases.workflow_db.models.ResourceRoot;
import com.liveramp.workflow.test.types.Email;
import com.liveramp.workflow2.workflow_state.resources.DbResourceManager;
import com.liveramp.workflow2.workflow_state.resources.DbStorage;

import static com.liveramp.commons.test.TestUtils.assertCollectionEquivalent;
import static org.junit.Assert.assertEquals;

public class DbStorageIT extends ResourceDbManagerTestCase {
  private final static Email email1 = new Email("thomas@kielbus.com");
  private final static Email email2 = new Email("alice@kielbus.com");
  private final static Email email3 = new Email("alice2@gmail.com");
  private final static List<Integer> listOfIntegers = Lists.newArrayList(1, 2, 3, 4);
  private final static List<Long> listOfLongs = Lists.newArrayList(1L, 2L, 3L, 4L);

  protected final IWorkflowDb workflowDb = new DatabasesImpl().getWorkflowDb();
  private DbStorage.Factory factory;

  protected DbStorage.Factory createStorage() {
    return new DbStorage.Factory(new DbResourceManager.WorkflowDbFactory.Default());
  }

  @Before
  public void prepare() {
    factory = createStorage();
  }

  protected static final Map<String, Integer> map = MapBuilder
      .of("x", 1).put("y", 2).put("z", 3).get();
  protected static final Set<Long> set = Sets.newHashSet(Long.MAX_VALUE, Long.MIN_VALUE, -1L, 0L);
  protected static final String MAP_NAME = "MapResource";
  protected static final String SET_NAME = "SetResource";
  protected static final String NAME = "Some name";

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

  protected ResourceRoot createRoot() throws IOException {
    ResourceRoot root = workflowDb.resourceRoots().create()
        .setName(NAME)
        .setCreatedAt(System.currentTimeMillis())
        .setUpdatedAt(System.currentTimeMillis());
    root.save();
    return root;
  }

  @Test
  public void testSwitchingBetweenJavaAndJsonSerialization() throws IOException {
    ResourceManager resourceManager = DbResourceManager.create(new DbResourceManager.WorkflowDbFactory.Default())
        .create(0L, "Test");

    ResourceRoot resourceRoot = Accessors.first(workflowDb.resourceRoots().findAll());

    final StorageSerializer javaSerializer = new JavaObjectStorageSerializer();
    workflowDb.resourceRecords().create("email2", resourceRoot.getIntId(), javaSerializer.serialize(email2));

    Resource<Email> resource1 = resourceManager.resource(email1, "email1");
    Resource<Email> resource2 = resourceManager.resource(email2, "email2");

    ReadResource<Email> readResource1 = resourceManager.getReadPermission(resource1);
    ReadResource<Email> readResource2 = resourceManager.getReadPermission(resource2);

    Email readEmail1 = resourceManager.read(readResource1);
    Email readEmail2 = resourceManager.read(readResource2);

    resourceManager.write(resourceManager.getWritePermission(resource2), email3);

    Email readEmail3 = resourceManager.read(readResource2);

    assertEquals(email1, readEmail1);
    assertEquals(email2, readEmail2);
    assertEquals(email3, readEmail3);
  }

  @Test
  public void testJsonForListsOfNumbers() throws IOException {
    ResourceManager resourceManager = DbResourceManager.create(new DbResourceManager.WorkflowDbFactory.Default())
        .create(0L, "Test");

    Resource<List<Integer>> listOfIntegersResource = resourceManager.resource(listOfIntegers, "list-of-integers");
    assertCollectionEquivalent(listOfIntegers, resourceManager.read(resourceManager.getReadPermission(listOfIntegersResource)));

    Resource<List<Long>> listOfLongsResource = resourceManager.resource(listOfLongs, "list-of-longs");
    assertCollectionEquivalent(listOfLongs, resourceManager.read(resourceManager.getReadPermission(listOfLongsResource)));
  }
}