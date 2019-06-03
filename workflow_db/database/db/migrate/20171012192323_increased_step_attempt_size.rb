class IncreasedStepAttemptSize < ActiveRecord::Migration
  def self.up
    execute "ALTER TABLE step_attempts MODIFY COLUMN id BIGINT AUTO_INCREMENT"

    change_column :step_dependencies, :step_attempt_id, :integer, :limit => 8
    change_column :step_dependencies, :dependency_attempt_id, :integer, :limit => 8
    change_column :step_attempt_datastores, :step_attempt_id, :integer, :limit => 8
    change_column :mapreduce_jobs, :step_attempt_id, :integer, :limit => 8
    change_column :step_statistics, :step_attempt_id, :integer, :limit => 8

  end

  def self.down
    execute "ALTER TABLE step_attempts MODIFY COLUMN id INTEGER AUTO_INCREMENT"

    change_column :step_dependencies, :step_attempt_id, :integer, :limit => 4
    change_column :step_dependencies, :dependency_attempt_id, :integer, :limit => 4
    change_column :step_attempt_datastores, :step_attempt_id, :integer, :limit => 4
    change_column :mapreduce_jobs, :step_attempt_id, :integer, :limit => 4
    change_column :step_statistics, :step_attempt_id, :integer, :limit => 4

  end
end
