$:.push File.expand_path('../lib', __FILE__)
#require 'workflow_db/version'
require 'workflow_db/paths'

Gem::Specification.new do |s|
  s.name        = 'workflow_db'
  s.version     = '1.0.0'
  s.authors     = ['Dev Tools']
  s.email       = ['dev-tools@liveramp.com']
  s.has_rdoc    = false,
  s.summary     = 'ActiveRecord models and libraries for LiveRamp WorkflowDb database'
  s.description = s.summary
  s.files       = Dir['lib/**/*', 'app/models/**/*', 'db/**/*']

  s.require_paths = WorkflowDb::PATHS
  s.add_dependency('auto_strip_attributes')
  s.add_dependency('net-ldap')
  s.add_dependency('net-ssh', '~> 2.4')
  s.add_dependency('ancestry', '~> 2.0.0')
  s.add_dependency('workflow2_types', '1.0.4')
end
