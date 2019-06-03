class IncreasedConfiguredNotificationIdSize < ActiveRecord::Migration
  def self.up
    execute "ALTER TABLE configured_notifications MODIFY COLUMN id BIGINT AUTO_INCREMENT"
  end

  def self.down
    execute "ALTER TABLE configured_notifications MODIFY COLUMN id INTEGER AUTO_INCREMENT"
  end
end
