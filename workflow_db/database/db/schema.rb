# encoding: UTF-8
# This file is auto-generated from the current state of the database. Instead
# of editing this file, please use the migrations feature of Active Record to
# incrementally modify your database, and then regenerate this schema definition.
#
# Note that this schema.rb definition is the authoritative source for your
# database schema. If you need to create the application database on another
# system, you should be using db:schema:load, not running all the migrations
# from scratch. The latter is a flawed and unsustainable approach (the more migrations
# you'll amass, the slower it'll run and the greater likelihood for issues).
#
# It's strongly recommended to check this file into your version control system.

ActiveRecord::Schema.define(:version => 20190204201620) do

  create_table "application_configured_notifications", :force => true do |t|
    t.integer "application_id",             :limit => 8, :null => false
    t.integer "configured_notification_id", :limit => 8, :null => false
  end

  add_index "application_configured_notifications", ["application_id"], :name => "application_configured_notification_application_idx"
  add_index "application_configured_notifications", ["configured_notification_id"], :name => "application_configured_notification_notification_idx"

  create_table "application_counter_summaries", :force => true do |t|
    t.integer "application_id"
    t.string  "group"
    t.string  "name"
    t.integer "value",          :limit => 8
    t.date    "date"
  end

  add_index "application_counter_summaries", ["application_id", "date", "group", "name"], :name => "application_id_date_group_name_idx", :unique => true
  add_index "application_counter_summaries", ["date", "group", "name"], :name => "date_group_name_idx"

  create_table "applications", :force => true do |t|
    t.string  "name",     :null => false
    t.integer "app_type"
  end

  add_index "applications", ["app_type"], :name => "index_applications_on_app_type", :unique => true
  add_index "applications", ["name"], :name => "index_applications_on_name", :unique => true

  create_table "background_attempt_infos", :force => true do |t|
    t.integer "workflow_attempt_id",            :limit => 8, :null => false
    t.string  "resource_manager_factory"
    t.string  "resource_manager_version_class"
  end

  add_index "background_attempt_infos", ["workflow_attempt_id"], :name => "index_background_attempt_infos_on_workflow_attempt_id", :unique => true

  create_table "background_step_attempt_infos", :force => true do |t|
    t.integer "step_attempt_id",                      :limit => 8, :null => false
    t.binary  "serialized_context",                                :null => false
    t.date    "next_execute_check",                                :null => false
    t.integer "execute_check_cooldown_seconds",                    :null => false
    t.string  "claimed_by_worker"
    t.integer "background_workflow_executor_info_id"
  end

  add_index "background_step_attempt_infos", ["background_workflow_executor_info_id"], :name => "idx_bg_step_attempt_infos_on_bg_workflow_ex_info_id"
  add_index "background_step_attempt_infos", ["step_attempt_id"], :name => "index_background_step_attempt_infos_on_step_attempt_id", :unique => true

  create_table "background_workflow_executor_infos", :force => true do |t|
    t.string   "host",           :null => false
    t.integer  "status",         :null => false
    t.datetime "last_heartbeat", :null => false
  end

  add_index "background_workflow_executor_infos", ["host"], :name => "index_background_workflow_executor_infos_on_host"

  create_table "configured_notifications", :force => true do |t|
    t.integer "workflow_runner_notification", :null => false
    t.string  "email"
    t.boolean "provided_alerts_handler"
  end

  create_table "dashboard_applications", :force => true do |t|
    t.integer "dashboard_id",   :null => false
    t.integer "application_id", :null => false
  end

  add_index "dashboard_applications", ["application_id", "dashboard_id"], :name => "index_dashboard_applications_on_application_id_and_dashboard_id", :unique => true
  add_index "dashboard_applications", ["application_id"], :name => "index_dashboard_applications_on_application_id"
  add_index "dashboard_applications", ["dashboard_id"], :name => "index_dashboard_applications_on_dashboard_id"

  create_table "dashboards", :force => true do |t|
    t.string "name", :null => false
  end

  add_index "dashboards", ["name"], :name => "index_dashboards_on_name", :unique => true

  create_table "execution_tags", :force => true do |t|
    t.integer "workflow_execution_id", :limit => 8, :null => false
    t.text    "tag",                                :null => false
    t.text    "value",                              :null => false
  end

  add_index "execution_tags", ["tag", "value"], :name => "index_execution_tags_on_tag_and_value", :length => {"tag"=>32, "value"=>64}
  add_index "execution_tags", ["workflow_execution_id"], :name => "index_execution_tags_on_workflow_execution_id"

  create_table "mapreduce_counters", :force => true do |t|
    t.integer "mapreduce_job_id",              :null => false
    t.string  "group",                         :null => false
    t.string  "name",                          :null => false
    t.integer "value",            :limit => 8, :null => false
  end

  add_index "mapreduce_counters", ["group", "name"], :name => "mapreduce_counter_group_name_index"
  add_index "mapreduce_counters", ["mapreduce_job_id"], :name => "index_mapreduce_counters_on_mapreduce_job_id"

  create_table "mapreduce_job_task_exceptions", :force => true do |t|
    t.integer "mapreduce_job_id"
    t.string  "task_attempt_id"
    t.text    "exception"
    t.string  "host_url"
  end

  add_index "mapreduce_job_task_exceptions", ["mapreduce_job_id"], :name => "index_mapreduce_job_task_exceptions_on_mapreduce_job_id"

  create_table "mapreduce_jobs", :force => true do |t|
    t.integer "step_attempt_id",        :limit => 8
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
    t.integer "tasks_sampled"
    t.integer "tasks_failed_in_sample"
  end

  add_index "mapreduce_jobs", ["step_attempt_id"], :name => "step_attempt_id_index"

  create_table "resource_records", :force => true do |t|
    t.string   "name",                                 :null => false
    t.integer  "resource_root_id",                     :null => false
    t.text     "json",             :limit => 16777215, :null => false
    t.datetime "created_at"
    t.string   "class_path"
  end

  add_index "resource_records", ["resource_root_id", "name"], :name => "index_resource_records_on_resource_root_id_and_name"

  create_table "resource_roots", :force => true do |t|
    t.string   "name"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.string   "scope_identifier"
    t.integer  "version",          :limit => 8
    t.string   "version_type"
  end

  add_index "resource_roots", ["name"], :name => "name_index"
  add_index "resource_roots", ["version_type", "version"], :name => "resource_root_version_type_idx", :unique => true

  create_table "step_attempt_datastores", :force => true do |t|
    t.integer "step_attempt_id",               :limit => 8, :null => false
    t.integer "workflow_attempt_datastore_id", :limit => 8, :null => false
    t.integer "ds_action",                                  :null => false
  end

  add_index "step_attempt_datastores", ["step_attempt_id"], :name => "step_attempt_id_index"
  add_index "step_attempt_datastores", ["workflow_attempt_datastore_id"], :name => "workflow_attempt_datastore_id_index"

  create_table "step_attempts", :force => true do |t|
    t.integer  "workflow_attempt_id",                  :null => false
    t.string   "step_token",                           :null => false
    t.datetime "start_time"
    t.datetime "end_time"
    t.integer  "step_status",                          :null => false
    t.string   "failure_cause"
    t.string   "failure_trace",       :limit => 10000
    t.string   "action_class",                         :null => false
    t.string   "status_message"
  end

  add_index "step_attempts", ["end_time"], :name => "index_step_attempts_on_end_time"
  add_index "step_attempts", ["workflow_attempt_id", "step_token"], :name => "workflow_attempt_token", :unique => true

  create_table "step_dependencies", :force => true do |t|
    t.integer "step_attempt_id",       :limit => 8, :null => false
    t.integer "dependency_attempt_id", :limit => 8, :null => false
  end

  add_index "step_dependencies", ["dependency_attempt_id"], :name => "dependency_attempt_id_index"
  add_index "step_dependencies", ["step_attempt_id"], :name => "step_attempt_id_index"

  create_table "step_statistics", :force => true do |t|
    t.integer "step_attempt_id", :limit => 8, :null => false
    t.string  "name",                         :null => false
    t.integer "value",           :limit => 8, :null => false
  end

  add_index "step_statistics", ["step_attempt_id"], :name => "index_step_statistics_on_step_attempt_id"

  create_table "user_dashboards", :force => true do |t|
    t.integer "user_id",      :null => false
    t.integer "dashboard_id", :null => false
  end

  add_index "user_dashboards", ["dashboard_id"], :name => "index_user_dashboards_on_dashboard_id"
  add_index "user_dashboards", ["user_id", "dashboard_id"], :name => "index_user_dashboards_on_user_id_and_dashboard_id", :unique => true
  add_index "user_dashboards", ["user_id"], :name => "index_user_dashboards_on_user_id"

  create_table "users", :force => true do |t|
    t.string "username",           :null => false
    t.string "notification_email", :null => false
  end

  add_index "users", ["username"], :name => "index_users_on_username", :unique => true

  create_table "workflow_alert_mapreduce_jobs", :force => true do |t|
    t.integer "workflow_alert_id", :limit => 8, :null => false
    t.integer "mapreduce_job_id",  :limit => 8, :null => false
  end

  add_index "workflow_alert_mapreduce_jobs", ["mapreduce_job_id"], :name => "index_mr_jobs"
  add_index "workflow_alert_mapreduce_jobs", ["workflow_alert_id", "mapreduce_job_id"], :name => "index_wf_alerts_and_mr_jobs"

  create_table "workflow_alert_workflow_executions", :force => true do |t|
    t.integer "workflow_alert_id",     :limit => 8, :null => false
    t.integer "workflow_execution_id", :limit => 8, :null => false
  end

  add_index "workflow_alert_workflow_executions", ["workflow_alert_id", "workflow_execution_id"], :name => "index_wf_alerts_and_executions"
  add_index "workflow_alert_workflow_executions", ["workflow_execution_id"], :name => "index_wf_executions"

  create_table "workflow_alerts", :force => true do |t|
    t.string "alert_class"
    t.text   "message"
  end

  create_table "workflow_attempt_configured_notifications", :force => true do |t|
    t.integer "workflow_attempt_id",        :limit => 8, :null => false
    t.integer "configured_notification_id", :limit => 8, :null => false
  end

  add_index "workflow_attempt_configured_notifications", ["configured_notification_id"], :name => "workflow_attempt_configured_notification_notification_idx"
  add_index "workflow_attempt_configured_notifications", ["workflow_attempt_id"], :name => "workflow_attempt_configured_notification_attempt_idx"

  create_table "workflow_attempt_datastores", :force => true do |t|
    t.integer "workflow_attempt_id", :null => false
    t.string  "name",                :null => false
    t.string  "path",                :null => false
    t.string  "class_name",          :null => false
  end

  add_index "workflow_attempt_datastores", ["workflow_attempt_id"], :name => "workflow_attempt_id_index"

  create_table "workflow_attempts", :force => true do |t|
    t.integer  "workflow_execution_id", :null => false
    t.string   "system_user",           :null => false
    t.string   "shutdown_reason"
    t.string   "priority",              :null => false
    t.string   "pool",                  :null => false
    t.string   "host",                  :null => false
    t.datetime "start_time"
    t.datetime "end_time"
    t.integer  "status"
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

  create_table "workflow_execution_configured_notifications", :force => true do |t|
    t.integer "workflow_execution_id",      :limit => 8, :null => false
    t.integer "configured_notification_id", :limit => 8, :null => false
  end

  add_index "workflow_execution_configured_notifications", ["configured_notification_id"], :name => "workflow_execution_configured_notification_notification_idx"
  add_index "workflow_execution_configured_notifications", ["workflow_execution_id"], :name => "workflow_execution_configured_notification_execution_idx"

  create_table "workflow_executions", :force => true do |t|
    t.integer  "app_type"
    t.string   "name",             :null => false
    t.string   "scope_identifier"
    t.integer  "status",           :null => false
    t.datetime "start_time"
    t.datetime "end_time"
    t.integer  "application_id"
    t.string   "pool_override"
  end

  add_index "workflow_executions", ["app_type", "start_time"], :name => "app_type_start_time_idx"
  add_index "workflow_executions", ["application_id", "scope_identifier", "start_time"], :name => "application_id_scope_identifier_start_time_idx"
  add_index "workflow_executions", ["application_id", "scope_identifier", "status"], :name => "application_id_scope_identifier_status_idx"
  add_index "workflow_executions", ["end_time"], :name => "end_time_idx"
  add_index "workflow_executions", ["name", "scope_identifier", "start_time"], :name => "name_scope_start_time_idx"
  add_index "workflow_executions", ["name", "scope_identifier", "status"], :name => "name_scope_status"
  add_index "workflow_executions", ["start_time"], :name => "start_time_idx"

end
