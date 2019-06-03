# Load the rails application
require File.expand_path('../application', __FILE__)

require 'thrift'
require 'workflow_constants'
require 'mysql2'
require 'active_record/connection_adapters/mysql2_adapter'

# Initialize the rails application
WorkflowDb::Application.initialize!

require File.expand_path('../../lib/patches/57key.rb', __FILE__)
