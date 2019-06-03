class WorkflowAlertLargerInts < ActiveRecord::Migration
  def self.up
    change_column :workflow_alerts, :id, :integer, :limit => 8
    change_column :workflow_alert_workflow_executions, :workflow_alert_id, :integer, :limit => 8
    change_column :workflow_alert_mapreduce_jobs, :workflow_alert_id, :integer, :limit => 8
  end

  def self.down
  end
end
