class AddUniqueJoinIndices < ActiveRecord::Migration
  def self.up

    add_index "dashboard_applications", ["application_id", "dashboard_id"], :name => "index_dashboard_applications_on_application_id_and_dashboard_id", :unique => true
    add_index "user_dashboards", ["user_id", "dashboard_id"], :name => "index_user_dashboards_on_user_id_and_dashboard_id", :unique => true

  end

  def self.down

    remove_index "dashboard_applications", :name => "index_dashboard_applications_on_application_id_and_dashboard_id"
    remove_index "user_dashboards", :name => "index_user_dashboards_on_user_id_and_dashboard_id"

  end
end
