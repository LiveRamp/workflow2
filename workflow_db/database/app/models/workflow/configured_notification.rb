class Workflow::ConfiguredNotification < Workflow::BaseModel
  has_many :workflow_execution_configured_notification, :dependent => :destroy
  has_many :workflow_attempt_configured_notification, :dependent => :destroy
  has_many :application_configured_notification, :dependent => :destroy
end
