class AddExecutorStatus < ActiveRecord::Migration
  def self.up

    create_table "background_workflow_executor_infos" do |t|
      t.string :host, :null => false
      t.integer :status, :null => false
      t.datetime :last_heartbeat, :null => false
    end

    add_index :background_workflow_executor_infos, :host

    add_column :background_step_attempt_infos, :background_workflow_executor_info_id, :integer

    add_index :background_step_attempt_infos, :background_workflow_executor_info_id, :name =>
        'idx_bg_step_attempt_infos_on_bg_workflow_ex_info_id'

  end

  def self.down
  end
end
