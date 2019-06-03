class Workflow::Dashboard < Workflow::BaseModel
  has_many :user_dashboards, :dependent => :destroy
  has_many :dashboard_applications, :dependent => :destroy

  has_many :users, through: :user_dashboard
  has_many :applications, through: :dashboard_application
end
