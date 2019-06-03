
/**
 * Autogenerated by Jack
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 */
package com.liveramp.databases.workflow_db.iface;

import com.liveramp.databases.workflow_db.models.ResourceRoot;
import com.liveramp.databases.workflow_db.query.ResourceRootQueryBuilder;
import com.liveramp.databases.workflow_db.query.ResourceRootDeleteBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.List;

import com.rapleaf.jack.IModelPersistence;

public interface IResourceRootPersistence extends IModelPersistence<ResourceRoot> {
  ResourceRoot create(final String name, final Long created_at, final Long updated_at, final String scope_identifier, final Long version, final String version_type) throws IOException;
  ResourceRoot create() throws IOException;

  ResourceRoot createDefaultInstance() throws IOException;
  List<ResourceRoot> findByName(String value)  throws IOException;
  List<ResourceRoot> findByCreatedAt(Long value)  throws IOException;
  List<ResourceRoot> findByUpdatedAt(Long value)  throws IOException;
  List<ResourceRoot> findByScopeIdentifier(String value)  throws IOException;
  List<ResourceRoot> findByVersion(Long value)  throws IOException;
  List<ResourceRoot> findByVersionType(String value)  throws IOException;

  ResourceRootQueryBuilder query();

  ResourceRootDeleteBuilder delete();
}
