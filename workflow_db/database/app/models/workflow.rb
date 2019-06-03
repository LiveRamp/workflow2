module Workflow
  def self.table_name_prefix
    ''
  end

  class BaseModel < ActiveRecord::Base
    self.abstract_class = true

    CONFIG_KEY = "workflow_#{Rails.env}"

    raise "No configuration for #{CONFIG_KEY}. Add it to your database.yml." if !configurations[CONFIG_KEY]
    establish_connection configurations[CONFIG_KEY]
  end
end
