class AddWorkflowAlertsTables < ActiveRecord::Migration
  def self.up

    create_table "workflow_alerts" do |t|
      t.string "alert_class"
      t.string "message"
    end

    create_table "workflow_alert_mapreduce_jobs" do |t|
      t.integer "workflow_alert_id", :null => false
      t.integer "mapreduce_job_id", :limit => 4, :null => false
      t.integer "alert_type", :limit => 4, :null => false
    end

    add_index "workflow_alert_mapreduce_jobs", ["workflow_alert_id", "mapreduce_job_id"], :name => "index_wf_alerts_and_mr_jobs", :unique => true
    add_index "workflow_alert_mapreduce_jobs", ["mapreduce_job_id"], :name => "index_mr_jobs", :unique => true

    create_table "workflow_alert_workflow_executions" do |t|
      t.integer "workflow_alert_id", :null => false
      t.integer "workflow_execution_id", :limit => 4, :null => false
      t.integer "alert_type", :limit => 4, :null => false
    end

    add_index "workflow_alert_workflow_executions", ["workflow_alert_id", "workflow_execution_id"], :name => "index_wf_alerts_and_executions", :unique => true
    add_index "workflow_alert_workflow_executions", ["workflow_execution_id"], :name => "index_wf_executions", :unique => true

  end

  def self.down
    drop_table :workflow_alerts
    drop_table :workflow_alert_mapreduce_jobs
    drop_table :workflow_alert_workflow_executions
  end
end
