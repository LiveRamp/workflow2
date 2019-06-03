class Workflow::ApplicationConfiguredNotification < Workflow::BaseModel
  belongs_to :configured_notification
  belongs_to :application
end
