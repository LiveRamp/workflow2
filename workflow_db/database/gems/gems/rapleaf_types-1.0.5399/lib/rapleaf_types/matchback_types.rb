#
# Autogenerated by Thrift Compiler (0.11.0)
#
# DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
#

require 'thrift'

module Rapleaf
  module Types
    module Matchback
      module AutoMatchbackRequestStep
        MATCHBACK = 1
        QA = 2
        DELIVERY = 3
        VALUE_MAP = {1 => "MATCHBACK", 2 => "QA", 3 => "DELIVERY"}
        VALID_VALUES = Set.new([MATCHBACK, QA, DELIVERY]).freeze
      end

      module AutoMatchbackRequestStatus
        PENDING = 1
        RUNNING = 2
        FAILED = 3
        COMPLETE = 4
        WAITING_FOR_DATA = 5
        CANCELLED = 6
        VALUE_MAP = {1 => "PENDING", 2 => "RUNNING", 3 => "FAILED", 4 => "COMPLETE", 5 => "WAITING_FOR_DATA", 6 => "CANCELLED"}
        VALID_VALUES = Set.new([PENDING, RUNNING, FAILED, COMPLETE, WAITING_FOR_DATA, CANCELLED]).freeze
      end

      module MatchbackRequestStatus
        PENDING = 1
        RUNNING = 2
        FAILED = 3
        COMPLETED = 4
        CANCELLED = 5
        WAITING_FOR_DATA = 6
        VALUE_MAP = {1 => "PENDING", 2 => "RUNNING", 3 => "FAILED", 4 => "COMPLETED", 5 => "CANCELLED", 6 => "WAITING_FOR_DATA"}
        VALID_VALUES = Set.new([PENDING, RUNNING, FAILED, COMPLETED, CANCELLED, WAITING_FOR_DATA]).freeze
      end

      class AttributionData; end

      class AttributionDataList; end

      class ImpressionData; end

      class AttributionData
        include ::Thrift::Struct, ::Thrift::Struct_Union
        APPEND = 1
        DOLLARS = 2
        QUANTITY = 3
        FILENAME = 4
        LINENUMBER = 5

        FIELDS = {
          APPEND => {:type => ::Thrift::Types::STRING, :name => 'append'},
          DOLLARS => {:type => ::Thrift::Types::STRING, :name => 'dollars'},
          QUANTITY => {:type => ::Thrift::Types::STRING, :name => 'quantity'},
          FILENAME => {:type => ::Thrift::Types::STRING, :name => 'filename', :optional => true},
          LINENUMBER => {:type => ::Thrift::Types::I32, :name => 'linenumber', :optional => true}
        }

        def struct_fields; FIELDS; end

        def validate
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field append is unset!') unless @append
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field dollars is unset!') unless @dollars
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field quantity is unset!') unless @quantity
        end

        ::Thrift::Struct.generate_accessors self
      end

      class AttributionDataList
        include ::Thrift::Struct, ::Thrift::Struct_Union
        DATA = 1

        FIELDS = {
          DATA => {:type => ::Thrift::Types::LIST, :name => 'data', :element => {:type => ::Thrift::Types::STRUCT, :class => ::Rapleaf::Types::Matchback::AttributionData}}
        }

        def struct_fields; FIELDS; end

        def validate
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field data is unset!') unless @data
        end

        ::Thrift::Struct.generate_accessors self
      end

      class ImpressionData
        include ::Thrift::Struct, ::Thrift::Struct_Union
        IMPRESSIONS_BY_CAMPAIGN_ID = 1

        FIELDS = {
          IMPRESSIONS_BY_CAMPAIGN_ID => {:type => ::Thrift::Types::MAP, :name => 'impressions_by_campaign_id', :key => {:type => ::Thrift::Types::STRING}, :value => {:type => ::Thrift::Types::I32}}
        }

        def struct_fields; FIELDS; end

        def validate
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field impressions_by_campaign_id is unset!') unless @impressions_by_campaign_id
        end

        ::Thrift::Struct.generate_accessors self
      end

    end
  end
end