package com.rapleaf.cascading_ext.workflow2;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.Before;

import cascading.tuple.Fields;

import com.liveramp.cascading_ext.fs.TrashHelper;
import com.liveramp.databases.workflow_db.DatabasesImpl;

import com.rapleaf.cascading_ext.datastore.TupleDataStore;
import com.rapleaf.cascading_ext.datastore.TupleDataStoreImpl;

import static org.junit.Assert.fail;

public class WorkflowTestCase  {

  protected final String TEST_ROOT;

  public WorkflowTestCase() {
    TEST_ROOT = "/tmp/tests/" + "/" + this.getClass().getName() + "_AUTOGEN/";
  }

  public FileSystem getFS() throws IOException {
    return new Path(TEST_ROOT).getFileSystem(new Configuration());
  }

  @Before
  public final void setUpTestRoot() throws IOException {
    FileUtils.deleteDirectory(new File(this.TEST_ROOT));
    TrashHelper.deleteUsingTrashIfEnabled(new Path(TEST_ROOT).getFileSystem(new Configuration()), new Path(this.TEST_ROOT));
  }

  @Before
  public void deleteFixtures() throws Exception {
    new DatabasesImpl().getWorkflowDb().deleteAll();
    new com.liveramp.databases.workflow_db.DatabasesImpl().getWorkflowDb().deleteAll();
  }

  public String getTestRoot() {
    return TEST_ROOT;
  }

  protected Exception getException(Callable run) {
    try {
      run.call();
      fail("Should have thrown an exception!");
      throw new RuntimeException("won't get here");
    } catch (Exception e) {
      return e;
    }
  }


  public TupleDataStore tupleStore(String relPath, Fields fields) throws IOException {
    return new TupleDataStoreImpl("test", getTestRoot(), relPath, fields);
  }

}
