class Workflow::User < Workflow::BaseModel
  has_many :user_dashboards, :dependent => :destroy
  has_many :dashboards, through: :user_dashboards
end
