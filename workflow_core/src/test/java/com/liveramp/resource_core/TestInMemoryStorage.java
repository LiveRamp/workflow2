package com.liveramp.resource_core;

import java.io.IOException;

import org.junit.Test;

import com.liveramp.cascading_ext.resource.InMemoryResourceManager;
import com.liveramp.cascading_ext.resource.ReadResource;
import com.liveramp.cascading_ext.resource.Resource;
import com.liveramp.cascading_ext.resource.ResourceManager;

import static org.junit.Assert.assertEquals;

public class TestInMemoryStorage extends ResourceCoreTestCase {

  @Test
  public void testInMemoryStorage() throws IOException {
    ResourceManager resourceManager = InMemoryResourceManager.create()
        .create(0L, "Test");
    Resource<Integer> res = resourceManager.emptyResource("a");
    resourceManager.write(resourceManager.getWritePermission(res), 1);

    ReadResource<Integer> rp = resourceManager.getReadPermission(res);
    Integer read = resourceManager.read(rp);

    assertEquals(Integer.valueOf(1), read);

  }
}
