package com.liveramp.cascading_ext.resource.storage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;

import org.codehaus.plexus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liveramp.cascading_ext.resource.InMemoryStorage;
import com.liveramp.cascading_ext.resource.Storage;
import com.liveramp.commons.util.serialization.JavaObjectSerializationHandler;
import com.liveramp.commons.util.serialization.SerializationHandler;

public class FileSystemStorage implements Storage {
  private static final Logger LOG = LoggerFactory.getLogger(FileSystemStorage.class);

  private final SerializationHandler handler;
  private final InMemoryStorage cache;
  private File resourceRoot;

  public FileSystemStorage(String resourceRoot) {
    this.handler = new JavaObjectSerializationHandler();
    this.cache = new InMemoryStorage();
    this.resourceRoot = new File(resourceRoot);
  }

  private File getFile(String name) {
    return new File(resourceRoot, name);
  }

  @Override
  public <T> void store(String name, T object) {
    cache.store(name, object);
    try {

      File file = getFile(name);
      if (file.exists()) {
        FileUtils.forceDelete(file);
      }

      byte[] serialized;

      if (object instanceof Serializable) {
        serialized = handler.serialize(object);
      } else {
        throw new RuntimeException("Could not serialize resource '" + name + "' with value " + object);
      }

      FileUtils.forceMkdir(file.getParentFile());

      FileOutputStream output = new FileOutputStream(file);
      output.write(serialized);
      output.close();

      LOG.info("Stored resource '" + name + "' to " + file.toString());

    } catch (Throwable t) {
      throw new RuntimeException("Unable to store resource '" + name + "' and value " + object, t);
    }

  }

  @Override
  public <T> T retrieve(String name) {
    if (cache.isStored(name)) {
      LOG.info("Retrieved resource '" + name + "' from the cache");
      return (T)cache.retrieve(name);
    }

    try {

      File file = getFile(name);
      if (file.exists()) {

        byte[] data = Files.readAllBytes(file.toPath());

        LOG.info("Retrieved resource '" + name + "' from path " + file.getAbsolutePath().toString());

        return (T)handler.deserialize(data);

      } else {

        LOG.info("Failed to find Resource'" + name + "' at path " + file.getAbsoluteFile().toString());

        return null;

      }

    } catch (Throwable t) {
      throw new RuntimeException("Unable to retrieve resource with name " + name, t);
    }

  }

  @Override
  public boolean
  isStored(String name) {
    if (cache.isStored(name)) {
      return true;
    }

    try {
      return getFile(name).exists();
    } catch (Throwable t) {
      throw new RuntimeException("Unable to determine whether resource with name'" + name + "' is stored or not", t);
    }

  }

  public static class Factory implements Storage.Factory<String> {

    @Override
    public FileSystemStorage forResourceRoot(String root) throws IOException {
      return new FileSystemStorage(root);
    }

  }

}
