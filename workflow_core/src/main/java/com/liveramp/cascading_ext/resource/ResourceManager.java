package com.liveramp.cascading_ext.resource;

import org.apache.commons.lang.NotImplementedException;

public interface ResourceManager extends ResourceDeclarer {

  <T> Resource<T> resource(T object);

  <T> Resource<T> resource(T object, String name);

  <T> void write(WriteResource<T> resource, T value);

  <T> T read(ReadResource<T> resource);


  public class NotImplemented extends ResourceDeclarer.NotImplemented implements ResourceManager {

    @Override
    public <T> T manage(T context) {
      throw new NotImplementedException();
    }

    @Override
    public <T> Resource<T> resource(T object) {
      throw new NotImplementedException();
    }

    @Override
    public <T> Resource<T> resource(T object, String name) {
      throw new NotImplementedException();
    }

    @Override
    public <T> void write(WriteResource<T> resource, T value) {
      throw new NotImplementedException();
    }

    @Override
    public <T> T read(ReadResource<T> resource) {
      throw new NotImplementedException();
    }

  }

}
