class IncreaseAttemptDatastoreIdSize < ActiveRecord::Migration
  def self.up
    execute "SET sql_log_bin=0"
    execute "ALTER TABLE step_attempt_datastores MODIFY COLUMN workflow_attempt_datastore_id BIGINT not null"
  end

  def self.down
  end
end
