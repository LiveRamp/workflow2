#
# Autogenerated by Thrift Compiler (0.11.0)
#
# DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
#

require 'thrift'
require File.join File.dirname(__FILE__), 'util_types'


module Liveramp
  module IdlBilling
    module IdlBillingConfigStatus
      DISABLED = 0
      TEST = 1
      ENABLED = 2
      VALUE_MAP = {0 => "DISABLED", 1 => "TEST", 2 => "ENABLED"}
      VALID_VALUES = Set.new([DISABLED, TEST, ENABLED]).freeze
    end

  end
end