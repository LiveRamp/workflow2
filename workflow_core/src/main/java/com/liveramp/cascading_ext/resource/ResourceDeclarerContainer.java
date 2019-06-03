package com.liveramp.cascading_ext.resource;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Set;

import com.google.common.collect.Sets;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;


public class ResourceDeclarerContainer<RESOURCE_ROOT> implements ResourceDeclarer {

  private final Tagger tagger;
  private final RootManager<RESOURCE_ROOT> rootManager;
  private final Set managedIds;

  public ResourceDeclarerContainer(Tagger tagger, RootManager<RESOURCE_ROOT> rootManager) throws IOException {
    this.tagger = tagger;
    this.rootManager = rootManager;

    this.managedIds = Sets.newHashSet();

  }

  @Override
  public ResourceManager create(long version, String versionType) throws IOException {

    Storage storage = rootManager.getStorage(version, versionType);

    return new ResourceManagerContainer<>(this, tagger, storage);
  }

  @Override
  public <T> T manage(T context) {
    return (T)Enhancer.create(context.getClass(), new ContextManagerInterceptor(context));
  }

  @Override
  public <T> Resource<T> emptyResource(String name) {
    if (managedIds.contains(name)) {
      throw new RuntimeException("Cannot create two resources with the same id: " + name);
    }
    managedIds.add(name);
    return new ResourceImpl<T>(name);
  }

  @Override
  public <T> Resource<T> findResource(String name) {
    if (!managedIds.contains(name)) {
      throw new RuntimeException("Resource not found: " + name);
    }
    return new ResourceImpl<T>(name);
  }

  @Override
  public <T> ReadResource<T> getReadPermission(Resource<T> resource) {
    return (ResourceImpl<T>)resource;
  }

  @Override
  public <T> WriteResource<T> getWritePermission(Resource<T> resource) {
    return (ResourceImpl<T>)resource;
  }



  private class ContextManagerInterceptor implements MethodInterceptor {

    final Object originalContext;

    public ContextManagerInterceptor(Object originalContext) {
      this.originalContext = originalContext;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
      if (!method.getReturnType().equals(Void.TYPE)) {
        return createTaggedObject(originalContext, method, args, proxy);
      } else {
        return proxy.invoke(originalContext, args);
      }
    }

    public Object createTaggedObject(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
      Object val = proxy.invoke(obj, args);
      checkSerializableReturnValue(method, val);
      String tag = tagger.determineTag(method.getName(), val);
      return Enhancer.create(val.getClass(), new Class[]{TaggedObject.class, ContainerObject.class}, new TaggingInterceptor(val, tag));
    }

    private void checkSerializableReturnValue(Method method, Object value) {
      if (!(value instanceof Serializable)) {
        throw new IllegalStateException("Method marked as persistent, but does not return a Serializable object. Method: " + method);
      }
    }

    private class TaggingInterceptor implements MethodInterceptor {

      final String tag;
      final Object original;

      public TaggingInterceptor(Object val, String tag) {
        this.original = val;
        this.tag = tag;
      }

      @Override
      public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        if (method.getDeclaringClass().equals(TaggedObject.class)) {
          return tag;
        } else if (method.getDeclaringClass().equals(ContainerObject.class)) {
          return original;
        } else {
          return methodProxy.invoke(original, objects);
        }
      }
    }
  }

  public static class MethodNameTagger implements Tagger {
    @Override
    public String determineTag(String name, Object value) {
      return name;
    }
  }
}
