class WorkflowAlertsAutoIncrement < ActiveRecord::Migration
  def self.up
    execute "ALTER TABLE workflow_alerts MODIFY COLUMN id BIGINT AUTO_INCREMENT";
  end

  def self.down
    change_column :workflow_alerts, :id, :integer, :limit => 8
  end
end
