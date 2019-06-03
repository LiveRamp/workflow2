package com.rapleaf.cascading_ext.workflow2;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.junit.Test;

import cascading.tuple.Fields;

import com.rapleaf.cascading_ext.datastore.TupleDataStoreImpl;

import static com.rapleaf.cascading_ext.workflow2.test.WorkflowTestUtils.execute;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ActionIT extends WorkflowTestCase {
  public class ExampleAction extends Action {
    public ExampleAction() throws IOException {
      super("example");
      creates(new TupleDataStoreImpl("example dir 1", getTestRoot(), "/dir1", new Fields()));
      createsTemporary(new TupleDataStoreImpl("example dir 2", getTestRoot(), "/dir2", new Fields()));
      readsFrom(new TupleDataStoreImpl("example dir 3",getTestRoot(),  "/dir3", new Fields()));
    }

    @Override
    protected void execute() throws Exception {
    }
  }

  @Test
  public void testDeletesCreatesAndTemp() throws Exception {
    Path dir1Path = new Path(getTestRoot() + "/dir1");
    getFS().mkdirs(dir1Path);
    Path dir2Path = new Path(getTestRoot() + "/dir2");
    getFS().mkdirs(dir2Path);
    Path dir3Path = new Path(getTestRoot() + "/dir3");
    getFS().mkdirs(dir3Path);

    execute(new ExampleAction());

    assertFalse("dir2 should be deleted", getFS().exists(dir2Path));
    assertTrue("dir3 should exist", getFS().exists(dir3Path));
  }
}
