class AddResourceTables < ActiveRecord::Migration
  def self.up

    create_table "resource_roots" do |t|
      t.string   "name"
      t.datetime "created_at"
      t.datetime "updated_at"
      t.string   "scope_identifier"
      t.integer  "version",          :limit => 8
      t.string   "version_type"
    end

    add_index "resource_roots", ["name"], :name => "name_index"
    add_index "resource_roots", %w(version_type version), :name => "resource_root_version_type_idx", :unique => true

    create_table "resource_records" do |t|
      t.string   "name",                                 :null => false
      t.integer  "resource_root_id", :limit => 4,        :null => false
      t.text     "json",             :limit => 16777215, :null => false
      t.datetime "created_at"
      t.string   "class_path"
    end

    add_index "resource_records", %w(resource_root_id name), :name => "index_resource_records_on_resource_root_id_and_name"

  end

  def self.down




  end
end
