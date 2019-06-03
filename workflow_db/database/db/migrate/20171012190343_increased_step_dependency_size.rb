class IncreasedStepDependencySize < ActiveRecord::Migration
  def self.up
    execute "ALTER TABLE step_dependencies MODIFY COLUMN id BIGINT AUTO_INCREMENT"
  end

  def self.down
    execute "ALTER TABLE step_dependencies MODIFY COLUMN id INTEGER AUTO_INCREMENT"
  end
end
