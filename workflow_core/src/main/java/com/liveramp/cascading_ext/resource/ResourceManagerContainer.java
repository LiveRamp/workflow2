package com.liveramp.cascading_ext.resource;

import java.io.IOException;

public class ResourceManagerContainer<RESOURCE_ROOT> implements ResourceManager {

  private final ResourceDeclarer declarer;
  private final Tagger tagger;

  private final Storage storage;

  public ResourceManagerContainer(
      ResourceDeclarer declarer,
      Tagger tagger,
      Storage storage) {
    this.declarer = declarer;
    this.storage = storage;
    this.tagger = tagger;
  }

  @Override
  public <T> Resource<T> resource(T object) {
    if (!(object instanceof TaggedObject)) {
      throw new RuntimeException("Cannot manage " + object + " without calling manage() on the context.");
    }
    TaggedObject taggedObject = (TaggedObject)object;
    ContainerObject containerObject = (ContainerObject)object;
    String id = taggedObject.getID();

    return createResource(id, (T)containerObject.getOriginal());
  }

  @Override
  public <T> Resource<T> resource(T object, String name) {
    String id = tagger.determineTag(name, object);
    return createResource(id, object);
  }

  @Override
  public <T> void write(WriteResource<T> resource, T value) {
    storage.store(resource.getId(), value);
  }

  @Override
  public <T> T read(ReadResource<T> resource) {
    String id = resource.getId();
    return storage.retrieve(id);
  }

  private <T> Resource<T> createResource(String id, T object) {
    Resource<T> resource = emptyResource(id);

    if (!storage.isStored(id)) {
      storage.store(id, object);
    }

    return resource;
  }


  @Override
  public ResourceManager create(long version, String versionType) throws IOException {
    return declarer.create(version, versionType);
  }

  @Override
  public <T> T manage(T context) {
    return declarer.manage(context);
  }

  @Override
  public <T> Resource<T> emptyResource(String name) {
    return declarer.emptyResource(name);
  }

  @Override
  public <T> Resource<T> findResource(String name) {
    return declarer.findResource(name);
  }

  @Override
  public <T> ReadResource<T> getReadPermission(Resource<T> resource) {
    return declarer.getReadPermission(resource);
  }

  @Override
  public <T> WriteResource<T> getWritePermission(Resource<T> resource) {
    return declarer.getWritePermission(resource);
  }

  //  mainly for testing
  public Storage getStorage() {
    return storage;
  }
}
