class IncreasedWorkflowAttemptDatastoreSize < ActiveRecord::Migration
  def self.up
    execute "ALTER TABLE workflow_attempt_datastores MODIFY COLUMN id BIGINT AUTO_INCREMENT"
  end

  def self.down
    execute "ALTER TABLE workflow_attempt_datastores MODIFY COLUMN id INTEGER AUTO_INCREMENT"
  end
end
