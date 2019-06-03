package com.liveramp.cascading_ext.resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

import com.google.common.collect.Maps;

public class InMemoryStorage implements Storage {

  private Map<String, Object> map = Maps.newHashMap();

  public void reset() {
    map = Maps.newHashMap();
  }

  @Override
  public <T> void store(String name, T object) {
    Serializable serializableObject = clone((Serializable)object);
    map.put(name, serializableObject);
  }

  @Override
  public <T> T retrieve(String name) {
    return (T)map.get(name);
  }

  @Override
  public boolean isStored(String name) {
    return map.containsKey(name);
  }

  private Serializable clone(Serializable s) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      new ObjectOutputStream(baos).writeObject(s);
      baos.close();
      ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
      Serializable result = (Serializable)ois.readObject();
      ois.close();
      return result;
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public static class Factory implements Storage.Factory<Void>{

    @Override
    public InMemoryStorage forResourceRoot(Void aVoid) throws IOException {
      return new InMemoryStorage();
    }
  }
}
