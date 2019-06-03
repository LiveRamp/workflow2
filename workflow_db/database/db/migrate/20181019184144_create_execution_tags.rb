class CreateExecutionTags < ActiveRecord::Migration
  def self.up
    create_table :execution_tags do |t|
      t.integer :workflow_execution_id, :limit => 8, :null => false
      t.text :tag, :null => false
      t.text :value, :null => false
    end
    add_index :execution_tags, [:tag, :value], :length => {:tag => 32, :value => 64}
    add_index :execution_tags, [:workflow_execution_id]
  end

  def self.down
    drop_table :execution_tags
  end
end
