class AddBackgroundModels < ActiveRecord::Migration
  def self.up

    create_table "background_step_attempt_infos" do |t|
      t.integer "step_attempt_id", :limit => 8, :null => false
      t.binary "serialized_context", :null => false
      t.date "next_execute_check", :null => false
      t.integer "execute_check_cooldown_seconds", :null => false
      t.string "claimed_by_worker"
    end

    create_table "background_attempt_infos" do |t|
      t.integer "workflow_attempt_id", :limit => 8, :null => false
      t.string "resource_manager_factory"
      t.string "resource_manager_version_class"
    end

    add_index "background_step_attempt_infos", "step_attempt_id", :unique => true
    execute "ALTER TABLE background_step_attempt_infos MODIFY COLUMN id BIGINT AUTO_INCREMENT"

    add_index "background_attempt_infos", "workflow_attempt_id", :unique => true
    execute "ALTER TABLE background_attempt_infos MODIFY COLUMN id BIGINT AUTO_INCREMENT"

    # TODO free-form property model

  end

  def self.down
    drop_table :step_contexts
  end

end
