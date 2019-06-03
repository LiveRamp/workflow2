class WorkflowExecutionsIntToLong < ActiveRecord::Migration
  def self.up
    change_column :workflow_alert_workflow_executions, :workflow_execution_id, :integer, :limit => 8
    change_column :workflow_alert_mapreduce_jobs, :mapreduce_job_id, :integer, :limit => 8
  end

  def self.down
  end
end
