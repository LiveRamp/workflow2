class AddUserDashboards < ActiveRecord::Migration
  def self.up

    create_table "users" do |t|
      t.string  "username",                  :null => false
      t.string "notification_email", :null => false
    end

    add_index "users", ["username"], :name => "index_users_on_username", :unique => true    

    create_table "dashboards" do |t|
      t.string "name",  :null => false
    end

    add_index "dashboards", ["name"], :name => "index_dashboards_on_name", :unique => true

    create_table "dashboard_applications" do |t|
      t.integer "dashboard_id", :null => false
      t.integer "application_id", :null => false
    end
 
    add_index "dashboard_applications", ["dashboard_id"], :name => "index_dashboard_applications_on_dashboard_id"
    add_index "dashboard_applications", ["application_id"], :name => "index_dashboard_applications_on_application_id"

    create_table "user_dashboards" do |t|
      t.integer "user_id", :null => false
      t.integer "dashboard_id", :null => false
    end
   
    add_index "user_dashboards", ["user_id"], :name => "index_user_dashboards_on_user_id"
    add_index "user_dashboards", ["dashboard_id"], :name => "index_user_dashboards_on_dashboard_id"

  end

  def self.down
    drop_table :users
    drop_table :dashboards
    drop_table :dashboard_applications
    drop_table :user_dashboards
  end
end
