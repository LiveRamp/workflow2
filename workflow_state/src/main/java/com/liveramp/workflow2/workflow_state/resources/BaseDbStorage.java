package com.liveramp.workflow2.workflow_state.resources;

import java.io.IOException;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liveramp.cascading_ext.resource.InMemoryStorage;
import com.liveramp.cascading_ext.resource.JavaObjectStorageSerializer;
import com.liveramp.cascading_ext.resource.StorageSerializer;
import com.liveramp.commons.Accessors;
import com.liveramp.commons.util.NumberUtil;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.databases.workflow_db.models.ResourceRecord;
import com.liveramp.databases.workflow_db.models.ResourceRoot;
import com.rapleaf.jack.queries.QueryOrder;

public class BaseDbStorage {

  private enum SERIALIZATION_TYPE {JAVA, JSON}

  public static final int CHAR_SIZE = 4;
  private static final int KILOBYTE = 1024;
  private static final int MEGABYTE = 1024 * KILOBYTE;
  public static final int MAX_OBJECT_SIZE = MEGABYTE;

  private final StorageSerializer javaSerializer;
  private static final Logger LOG = LoggerFactory.getLogger(DbStorage.class);
  private final InMemoryStorage cache;
  private ResourceRoot root;

  public BaseDbStorage(ResourceRoot root) {
    this.javaSerializer = new JavaObjectStorageSerializer();
    this.cache = new InMemoryStorage();
    this.root = root;
  }

  public synchronized <T> void store(String name, T object, IWorkflowDb workflowDb) {

    if (object == null) {
      throw new IllegalArgumentException("DbStore cannot store null objects!");
    }

    SERIALIZATION_TYPE type = getSerializationType(name, workflowDb);
    String serialized = serialize(object, type);

    if (serialized.length() * CHAR_SIZE > MAX_OBJECT_SIZE) {
      throw new RuntimeException("Resource size over limit. Name: " + name + ", size: " + serialized.length() * CHAR_SIZE + ", limit: " + MAX_OBJECT_SIZE + ", object: " + object);
    }

    cache.store(name, object);

    try {
      switch (type) {
        case JAVA:
          workflowDb.resourceRecords().create(name, NumberUtil.safeLongToInt(root.getId()), serialized, System.currentTimeMillis(), null);
          break;
        case JSON:
          workflowDb.resourceRecords().create(name, NumberUtil.safeLongToInt(root.getId()), serialized, System.currentTimeMillis(), object.getClass().getCanonicalName());
          break;
        default:
          throw new RuntimeException("Only Java and Json serialization supported.");
      }
      LOG.info("Stored resource '" + name + "' to root " + root.toString());
    } catch (IOException e) {
      throw new RuntimeException("Failed to store resource '" + name + "'", e);
    }
  }

  private SERIALIZATION_TYPE getSerializationType(String name, IWorkflowDb workflowDb) {
    if (isStored(name, workflowDb)) {
      try {
        if (getResource(workflowDb, name, root).getClassPath() == null) {
          return SERIALIZATION_TYPE.JAVA;
        }
      } catch (IOException ioe) {
        throw new RuntimeException(ioe);
      }
    }

    return SERIALIZATION_TYPE.JSON;
  }


  public static ResourceRecord getResource(IWorkflowDb workflowDb, String name, ResourceRoot root) throws IOException {
    return Accessors.first(workflowDb.resourceRecords().query()
        .resourceRootId(NumberUtil.safeLongToInt(root.getId()))
        .name(name)
        .orderById(QueryOrder.DESC)
        .limit(1)
        .find()
    );
  }

  public static boolean hasResource(IWorkflowDb worklfowDb, String name, ResourceRoot root) throws IOException {
    return !worklfowDb.resourceRecords().query()
        .resourceRootId(NumberUtil.safeLongToInt(root.getId()))
        .name(name)
        .orderById(QueryOrder.DESC)
        .limit(1)
        .find().isEmpty();
  }

  public synchronized <T> T retrieve(String name, IWorkflowDb workflowDb) {
    if (cache.isStored(name)) {
      LOG.info("Retrieved resource '" + name + "' from the cache");
      return (T)cache.retrieve(name);
    }

    try {
      if (isStored(name, workflowDb)) {
        ResourceRecord resourceRecord = getResource(workflowDb, name, root);
        LOG.info("Retrieved resource '" + name + "' from root " + root.toString());
        return deserialize(resourceRecord);
      } else {
        return null;
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to retrieve resource '" + name + "'", e);
    }
  }

  public synchronized boolean isStored(String name, IWorkflowDb workflowDb) {
    if (cache.isStored(name)) {
      return true;
    }
    try {
      if (root != null) {
        return hasResource(workflowDb, name, root);
      } else {
        return false;
      }
    } catch (Throwable t) {
      throw new RuntimeException("Failed to determine whether resource '" + name + "' is stored", t);
    }
  }

  private <T> String serialize(T object, SERIALIZATION_TYPE type) {
    switch (type) {
      case JAVA:
        return javaSerialize(object);
      case JSON:
        return jsonSerialize(object);
      default:
        throw new RuntimeException("Only Java and Json serialization supported.");
    }
  }

  private <T> String jsonSerialize(T object) {
    return JsonWriter.objectToJson(object);
  }

  private <T> String javaSerialize(T object) {
    return javaSerializer.serialize(object);
  }

  private <T> T deserialize(ResourceRecord record) {
    if (record.getClassPath() == null) {
      return javaDeserialize(record);
    } else {
      return jsonDeserialize(record);
    }
  }

  private <T> T javaDeserialize(ResourceRecord record) {
    return (T)javaSerializer.deSerialize(record.getJson());
  }

  private <T> T jsonDeserialize(ResourceRecord record) {
    return (T)JsonReader.jsonToJava(record.getJson());
  }
}
