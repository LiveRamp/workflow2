package com.liveramp.workflow2.workflow_state.resources;

import java.io.IOException;
import java.util.List;

import org.joda.time.DateTime;

import com.liveramp.commons.Accessors;
import com.liveramp.databases.workflow_db.IWorkflowDb;
import com.liveramp.databases.workflow_db.models.ResourceRoot;
import com.rapleaf.jack.queries.QueryOrder;

public class BaseDbStorageRootDeterminer {
  public static synchronized ResourceRoot getResourceRoot(long version, String versionType, IWorkflowDb workflowDb) throws IOException {

    List<ResourceRoot> results = workflowDb.resourceRoots().query()
        .version(version)
        .versionType(versionType)
        .orderById(QueryOrder.DESC)
        .limit(1)
        .find();

    if (!results.isEmpty()) {
      return Accessors.only(results);
    }

    //  we can start from scratch with a version
    long time = DateTime.now().getMillis();
    ResourceRoot root = workflowDb.resourceRoots().create()
        .setVersion(version)
        .setVersionType(versionType)
        .setCreatedAt(time)
        .setUpdatedAt(time);

    root.save();

    return root;
  }
}
