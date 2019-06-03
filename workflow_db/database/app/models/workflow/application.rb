class Workflow::Application < Workflow::BaseModel
  has_many :workflow_execution, :dependent => :restrict_with_exception
  has_many :application_configured_notification, :dependent => :destroy
  has_many :application_counter_summary, :dependent => :destroy
  has_many :dashboard_applications, :dependent => :destroy
  has_many :dashboards, through: :dashboard_application
end
