class WorkflowAlertsBiggerMessages < ActiveRecord::Migration
  def self.up
    remove_index "workflow_alert_mapreduce_jobs", name: "index_wf_alerts_and_mr_jobs"
    remove_index "workflow_alert_mapreduce_jobs", name: "index_mr_jobs"
    remove_index "workflow_alert_workflow_executions", name: "index_wf_alerts_and_executions"
    remove_index "workflow_alert_workflow_executions", name: "index_wf_executions"

    add_index "workflow_alert_mapreduce_jobs", ["workflow_alert_id", "mapreduce_job_id"], :name => "index_wf_alerts_and_mr_jobs", :unique => false
    add_index "workflow_alert_mapreduce_jobs", ["mapreduce_job_id"], :name => "index_mr_jobs", :unique => false
    add_index "workflow_alert_workflow_executions", ["workflow_alert_id", "workflow_execution_id"], :name => "index_wf_alerts_and_executions", :unique => false
    add_index "workflow_alert_workflow_executions", ["workflow_execution_id"], :name => "index_wf_executions", :unique => false

    change_column "workflow_alerts", "message", :text
  end

  def self.down
    change_column "workflow_alerts", "message", :string
  end
end
