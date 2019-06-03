package com.rapleaf.cascading_ext.workflow2;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.collect.Sets;
import org.junit.Test;

import com.liveramp.cascading_ext.resource.Resource;
import com.liveramp.cascading_ext.resource.ResourceManager;
import com.liveramp.cascading_ext.resource.WriteResource;
import com.liveramp.workflow.state.WorkflowDbPersistenceFactory;
import com.liveramp.workflow2.workflow_hadoop.ResourceManagers;

import com.rapleaf.cascading_ext.workflow2.options.HadoopWorkflowOptions;

import static org.junit.Assert.*;

public class WorkflowRunnersIT extends WorkflowTestCase {


  @Test
  public void test() throws IOException {

    AtomicLong result = new AtomicLong();

    WorkflowRunners.run(
        new WorkflowDbPersistenceFactory(),
        WorkflowRunnersIT.class.getName(),
        HadoopWorkflowOptions.test().setResourceManager(ResourceManagers.defaultResourceManager()),
        initialized -> {

          ResourceManager manager = initialized.getManager();
          Resource<Long> resource = manager.emptyResource("resource");

          Step step = new Step(new SetResource("step", 1L, resource));

          return Sets.newHashSet(step);

        },
        initialized -> {

          ResourceManager manager = initialized.getManager();
          Long number = manager.read(manager.getReadPermission(manager.<Long>findResource("resource")));

          result.set(number);
        }

    );

    assertEquals(1L, result.get());

  }


  public static class SetResource extends Action {

    private final WriteResource<Long> resource;
    private final Long value;

    public SetResource(String checkpointToken, Long value, Resource<Long> resource) {
      super(checkpointToken);
      this.value = value;
      this.resource = creates(resource);
    }

    @Override
    protected void execute() throws Exception {
      set(resource, value);
    }
  }

}