class CopyWorkflowModels < ActiveRecord::Migration
  def self.up

    create_table "applications" do |t|
      t.string  "name",                  :null => false
      t.integer "app_type", :limit => 4
    end

    add_index "applications", ["app_type"], :name => "index_applications_on_app_type", :unique => true
    add_index "applications", ["name"], :name => "index_applications_on_name", :unique => true

    create_table "application_counter_summaries" do |t|
      t.integer "application_id", :limit => 4
      t.string  "group"
      t.string  "name"
      t.integer "value",          :limit => 8
      t.date    "date"
    end

    add_index "application_counter_summaries", ["application_id", "date", "group", "name"], :name => "application_id_date_group_name_idx", :unique => true
    add_index "application_counter_summaries", ["date", "group", "name"], :name => "date_group_name_idx"

    create_table "workflow_executions" do |t|
      t.integer  "app_type",         :limit => 4
      t.string   "name",                          :null => false
      t.string   "scope_identifier"
      t.integer  "status",           :limit => 4, :null => false
      t.datetime "start_time"
      t.datetime "end_time"
      t.integer  "application_id",   :limit => 4
      t.string   "pool_override"
    end

    add_index "workflow_executions", ["app_type", "start_time"], :name => "app_type_start_time_idx"
    add_index "workflow_executions", ["application_id", "scope_identifier", "start_time"], :name => "application_id_scope_identifier_start_time_idx"
    add_index "workflow_executions", ["application_id", "scope_identifier", "status"], :name => "application_id_scope_identifier_status_idx"
    add_index "workflow_executions", ["end_time"], :name => "end_time_idx"
    add_index "workflow_executions", ["name", "scope_identifier", "start_time"], :name => "name_scope_start_time_idx"
    add_index "workflow_executions", ["name", "scope_identifier", "status"], :name => "name_scope_status"
    add_index "workflow_executions", ["start_time"], :name => "start_time_idx"

    create_table "workflow_attempts" do |t|
      t.integer  "workflow_execution_id", :limit => 4, :null => false
      t.string   "system_user",                        :null => false
      t.string   "shutdown_reason"
      t.string   "priority",                           :null => false
      t.string   "pool",                               :null => false
      t.string   "host",                               :null => false
      t.datetime "start_time"
      t.datetime "end_time"
      t.integer  "status",                :limit => 4
      t.datetime "last_heartbeat"
      t.string   "launch_dir"
      t.string   "launch_jar"
      t.string   "error_email"
      t.string   "info_email"
      t.string   "scm_remote"
      t.string   "commit_revision"
      t.string   "description" 
    end

    add_index "workflow_attempts", ["end_time"], :name => "index_workflow_attempts_on_end_time"
    add_index "workflow_attempts", ["status"], :name => "index_workflow_attempts_on_status"
    add_index "workflow_attempts", ["workflow_execution_id"], :name => "workflow_execution_id_index"

    create_table "workflow_attempt_configured_notifications" do |t|
      t.integer "workflow_attempt_id",        :limit => 8, :null => false
      t.integer "configured_notification_id", :limit => 8, :null => false
    end

    add_index "workflow_attempt_configured_notifications", ["configured_notification_id"], :name => "workflow_attempt_configured_notification_notification_idx"
    add_index "workflow_attempt_configured_notifications", ["workflow_attempt_id"], :name => "workflow_attempt_configured_notification_attempt_idx"

    create_table "step_attempts" do |t|
      t.integer  "workflow_attempt_id", :limit => 4,     :null => false
      t.string   "step_token",                           :null => false
      t.datetime "start_time"
      t.datetime "end_time"
      t.integer  "step_status",         :limit => 4,     :null => false
      t.string   "failure_cause"
      t.string   "failure_trace",       :limit => 10000
      t.string   "action_class",                         :null => false
      t.string   "status_message"
    end

    add_index "step_attempts", ["end_time"], :name => "index_step_attempts_on_end_time"
    add_index "step_attempts", ["workflow_attempt_id", "step_token"], :name => "workflow_attempt_token", :unique => true

    create_table "workflow_attempt_datastores"  do |t|
      t.integer "workflow_attempt_id", :limit => 4, :null => false
      t.string  "name",                             :null => false
      t.string  "path",                             :null => false
      t.string  "class_name",                       :null => false
    end

    add_index "workflow_attempt_datastores", ["workflow_attempt_id"], :name => "workflow_attempt_id_index"


    create_table "mapreduce_jobs" do |t|
      t.integer "step_attempt_id",        :limit => 4
      t.string  "job_identifier",                      :null => false
      t.string  "job_name",                            :null => false
      t.string  "tracking_url",                        :null => false
      t.integer "avg_map_duration",       :limit => 8
      t.integer "median_map_duration",    :limit => 8
      t.integer "max_map_duration",       :limit => 8
      t.integer "min_map_duration",       :limit => 8
      t.integer "stdev_map_duration",     :limit => 8
      t.integer "avg_reduce_duration",    :limit => 8
      t.integer "median_reduce_duration", :limit => 8
      t.integer "max_reduce_duration",    :limit => 8
      t.integer "min_reduce_duration",    :limit => 8
      t.integer "stdev_reduce_duration",  :limit => 8
    end

    add_index "mapreduce_jobs", ["step_attempt_id"], :name => "step_attempt_id_index"

    create_table "step_dependencies" do |t|
      t.integer "step_attempt_id",       :limit => 4, :null => false
      t.integer "dependency_attempt_id", :limit => 4, :null => false
    end

    add_index "step_dependencies", ["dependency_attempt_id"], :name => "dependency_attempt_id_index"
    add_index "step_dependencies", ["step_attempt_id"], :name => "step_attempt_id_index"


    create_table "step_statistics"  do |t|
      t.integer "step_attempt_id", :limit => 4, :null => false
      t.string  "name",                         :null => false
      t.integer "value",           :limit => 8, :null => false
    end

    add_index "step_statistics", ["step_attempt_id"], :name => "index_step_statistics_on_step_attempt_id"

    create_table "step_attempt_datastores"  do |t|
      t.integer "step_attempt_id",               :limit => 4, :null => false
      t.integer "workflow_attempt_datastore_id", :limit => 4, :null => false
      t.integer "ds_action",                     :limit => 4, :null => false
    end

    add_index "step_attempt_datastores", ["step_attempt_id"], :name => "step_attempt_id_index"
    add_index "step_attempt_datastores", ["workflow_attempt_datastore_id"], :name => "workflow_attempt_datastore_id_index"


    create_table "mapreduce_counters"  do |t|
      t.integer "mapreduce_job_id", :limit => 4, :null => false
      t.string  "group",                         :null => false
      t.string  "name",                          :null => false
      t.integer "value",            :limit => 8, :null => false
    end

    add_index "mapreduce_counters", ["group", "name"], :name => "mapreduce_counter_group_name_index"
    add_index "mapreduce_counters", ["mapreduce_job_id"], :name => "index_mapreduce_counters_on_mapreduce_job_id"

    execute "ALTER TABLE mapreduce_counters MODIFY COLUMN id BIGINT AUTO_INCREMENT"


    create_table "mapreduce_job_task_exceptions"  do |t|
      t.integer "mapreduce_job_id", :limit => 4
      t.string  "task_attempt_id"
      t.text    "exception"
    end

    add_index "mapreduce_job_task_exceptions", ["mapreduce_job_id"], :name => "index_mapreduce_job_task_exceptions_on_mapreduce_job_id"

    execute "ALTER TABLE mapreduce_job_task_exceptions MODIFY COLUMN id BIGINT AUTO_INCREMENT"

    create_table "configured_notifications"  do |t|
      t.integer "workflow_runner_notification", :limit => 4, :null => false
      t.string  "email"
      t.boolean "provided_alerts_handler"
    end

    create_table "workflow_execution_configured_notifications"  do |t|
      t.integer "workflow_execution_id",      :limit => 8, :null => false
      t.integer "configured_notification_id", :limit => 8, :null => false
    end

    add_index "workflow_execution_configured_notifications", ["configured_notification_id"], :name => "workflow_execution_configured_notification_notification_idx"
    add_index "workflow_execution_configured_notifications", ["workflow_execution_id"], :name => "workflow_execution_configured_notification_execution_idx"

    create_table "application_configured_notifications" do |t|
      t.integer "application_id",             :limit => 8, :null => false
      t.integer "configured_notification_id", :limit => 8, :null => false
    end

    add_index "application_configured_notifications", ["application_id"], :name => "application_configured_notification_application_idx"
    add_index "application_configured_notifications", ["configured_notification_id"], :name => "application_configured_notification_notification_idx"


  end

  def self.down
  end
end
