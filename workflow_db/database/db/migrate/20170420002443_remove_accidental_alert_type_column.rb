class RemoveAccidentalAlertTypeColumn < ActiveRecord::Migration
  def self.up
    remove_column :workflow_alert_mapreduce_jobs, :alert_type
    remove_column :workflow_alert_workflow_executions, :alert_type
  end

  def self.down
    add_column :workflow_alert_mapreduce_jobs, :alert_type, :integer
    add_column :workflow_alert_workflow_executions, :alert_type, :integer
  end
end
