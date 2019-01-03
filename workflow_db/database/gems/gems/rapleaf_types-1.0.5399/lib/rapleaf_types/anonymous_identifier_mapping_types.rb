#
# Autogenerated by Thrift Compiler (0.11.0)
#
# DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
#

require 'thrift'
require File.join File.dirname(__FILE__), 'bang_types'
require File.join File.dirname(__FILE__), 'audience_types'


module Liveramp
  module Types
    module AnonymousIdentifierMapping
      module AimRule
        EXACT = 1
        INDIVIDUAL = 2
        ONE_NAP_FRINGE = 3
        TWO_NAP_FRINGE = 4
        ONE_HOUSEHOLD_FRINGE = 5
        STATISTICAL_HOUSEHOLD_AND_TWO_FRINGE = 6
        STATISTICAL_A = 7
        STATISTICAL_B = 8
        STATISTICAL_C = 9
        STATISTICAL_D = 10
        INDIVIDUAL_MOBILE_DETERMINISTIC = 11
        INDIVIDUAL_ABILITEC = 12
        INDIVIDUAL_ABILITEC_HEID = 13
        INDIVIDUAL_ABILITEC_STATISTICAL_HEID = 14
        HOUSEHOLD_ABILITEC = 15
        HOUSEHOLD_ABILITEC_HEID = 16
        HOUSEHOLD_ABILITEC_STATISTICAL_HEID = 17
        INDIVIDUAL_EXPOSURE_MATCHING = 18
        INDIVIDUAL_EM_MOBILE_DETERMINISTIC = 19
        PARTNER_LINK_ID_SYNCER_INDIVIDUAL = 20
        PARTNER_LINK_ID_SYNCER_HOUSEHOLD = 21
        PARTNER_LINK_MATCHER_INDIVIDUAL = 22
        PARTNER_LINK_MATCHER_HOUSEHOLD = 23
        ZIP4_ABILITEC = 24
        ZIP4_ABILITEC_STATISTICAL_HEID = 25
        INDIVIDUAL_ABILITEC_OFFLINE_PME = 26
        HOUSEHOLD_ABILITEC_OFFLINE_PME = 27
        THIRD_PARTY_GRAPH = 28
        NAME_AND_CITY_ABILITEC = 29
        NAME_AND_CITY_ABILITEC_STATISTICAL_HEID = 30
        THIRD_PARTY_GRAPH_HOUSEHOLD = 31
        INDIVIDUAL_ABILITEC_MOBILE_STATISTICAL = 32
        NAME_AND_CITY_ABILITEC_OFFLINE_PME = 33
        ZIP4_ABILITEC_OFFLINE_PME = 34
        INDIVIDUAL_ABILITEC_DDS = 35
        HOUSEHOLD_ABILITEC_DDS = 36
        INDIVIDUAL_ABILITEC_EU = 37
        THIRD_PARTY_GRAPH_ZIP4 = 38
        THIRD_PARTY_GRAPH_INDIVIDUAL_DDS = 39
        THIRD_PARTY_GRAPH_HOUSEHOLD_DDS = 40
        INDIVIDUAL_ABILITEC_DDS_EU = 41
        HOUSEHOLD_ABILITEC_DDS_EU = 42
        ZIP4_ABILITEC_MOBILE_STATISTICAL = 43
        THIRD_PARTY_GRAPH_EU = 44
        THIRD_PARTY_GRAPH_INDIVIDUAL_DDS_EU = 45
        VALUE_MAP = {1 => "EXACT", 2 => "INDIVIDUAL", 3 => "ONE_NAP_FRINGE", 4 => "TWO_NAP_FRINGE", 5 => "ONE_HOUSEHOLD_FRINGE", 6 => "STATISTICAL_HOUSEHOLD_AND_TWO_FRINGE", 7 => "STATISTICAL_A", 8 => "STATISTICAL_B", 9 => "STATISTICAL_C", 10 => "STATISTICAL_D", 11 => "INDIVIDUAL_MOBILE_DETERMINISTIC", 12 => "INDIVIDUAL_ABILITEC", 13 => "INDIVIDUAL_ABILITEC_HEID", 14 => "INDIVIDUAL_ABILITEC_STATISTICAL_HEID", 15 => "HOUSEHOLD_ABILITEC", 16 => "HOUSEHOLD_ABILITEC_HEID", 17 => "HOUSEHOLD_ABILITEC_STATISTICAL_HEID", 18 => "INDIVIDUAL_EXPOSURE_MATCHING", 19 => "INDIVIDUAL_EM_MOBILE_DETERMINISTIC", 20 => "PARTNER_LINK_ID_SYNCER_INDIVIDUAL", 21 => "PARTNER_LINK_ID_SYNCER_HOUSEHOLD", 22 => "PARTNER_LINK_MATCHER_INDIVIDUAL", 23 => "PARTNER_LINK_MATCHER_HOUSEHOLD", 24 => "ZIP4_ABILITEC", 25 => "ZIP4_ABILITEC_STATISTICAL_HEID", 26 => "INDIVIDUAL_ABILITEC_OFFLINE_PME", 27 => "HOUSEHOLD_ABILITEC_OFFLINE_PME", 28 => "THIRD_PARTY_GRAPH", 29 => "NAME_AND_CITY_ABILITEC", 30 => "NAME_AND_CITY_ABILITEC_STATISTICAL_HEID", 31 => "THIRD_PARTY_GRAPH_HOUSEHOLD", 32 => "INDIVIDUAL_ABILITEC_MOBILE_STATISTICAL", 33 => "NAME_AND_CITY_ABILITEC_OFFLINE_PME", 34 => "ZIP4_ABILITEC_OFFLINE_PME", 35 => "INDIVIDUAL_ABILITEC_DDS", 36 => "HOUSEHOLD_ABILITEC_DDS", 37 => "INDIVIDUAL_ABILITEC_EU", 38 => "THIRD_PARTY_GRAPH_ZIP4", 39 => "THIRD_PARTY_GRAPH_INDIVIDUAL_DDS", 40 => "THIRD_PARTY_GRAPH_HOUSEHOLD_DDS", 41 => "INDIVIDUAL_ABILITEC_DDS_EU", 42 => "HOUSEHOLD_ABILITEC_DDS_EU", 43 => "ZIP4_ABILITEC_MOBILE_STATISTICAL", 44 => "THIRD_PARTY_GRAPH_EU", 45 => "THIRD_PARTY_GRAPH_INDIVIDUAL_DDS_EU"}
        VALID_VALUES = Set.new([EXACT, INDIVIDUAL, ONE_NAP_FRINGE, TWO_NAP_FRINGE, ONE_HOUSEHOLD_FRINGE, STATISTICAL_HOUSEHOLD_AND_TWO_FRINGE, STATISTICAL_A, STATISTICAL_B, STATISTICAL_C, STATISTICAL_D, INDIVIDUAL_MOBILE_DETERMINISTIC, INDIVIDUAL_ABILITEC, INDIVIDUAL_ABILITEC_HEID, INDIVIDUAL_ABILITEC_STATISTICAL_HEID, HOUSEHOLD_ABILITEC, HOUSEHOLD_ABILITEC_HEID, HOUSEHOLD_ABILITEC_STATISTICAL_HEID, INDIVIDUAL_EXPOSURE_MATCHING, INDIVIDUAL_EM_MOBILE_DETERMINISTIC, PARTNER_LINK_ID_SYNCER_INDIVIDUAL, PARTNER_LINK_ID_SYNCER_HOUSEHOLD, PARTNER_LINK_MATCHER_INDIVIDUAL, PARTNER_LINK_MATCHER_HOUSEHOLD, ZIP4_ABILITEC, ZIP4_ABILITEC_STATISTICAL_HEID, INDIVIDUAL_ABILITEC_OFFLINE_PME, HOUSEHOLD_ABILITEC_OFFLINE_PME, THIRD_PARTY_GRAPH, NAME_AND_CITY_ABILITEC, NAME_AND_CITY_ABILITEC_STATISTICAL_HEID, THIRD_PARTY_GRAPH_HOUSEHOLD, INDIVIDUAL_ABILITEC_MOBILE_STATISTICAL, NAME_AND_CITY_ABILITEC_OFFLINE_PME, ZIP4_ABILITEC_OFFLINE_PME, INDIVIDUAL_ABILITEC_DDS, HOUSEHOLD_ABILITEC_DDS, INDIVIDUAL_ABILITEC_EU, THIRD_PARTY_GRAPH_ZIP4, THIRD_PARTY_GRAPH_INDIVIDUAL_DDS, THIRD_PARTY_GRAPH_HOUSEHOLD_DDS, INDIVIDUAL_ABILITEC_DDS_EU, HOUSEHOLD_ABILITEC_DDS_EU, ZIP4_ABILITEC_MOBILE_STATISTICAL, THIRD_PARTY_GRAPH_EU, THIRD_PARTY_GRAPH_INDIVIDUAL_DDS_EU]).freeze
      end

      module Priority
        HIGH = 1
        LOW = 2
        EXPERIMENTAL = 3
        CRITICAL = 4
        VALUE_MAP = {1 => "HIGH", 2 => "LOW", 3 => "EXPERIMENTAL", 4 => "CRITICAL"}
        VALID_VALUES = Set.new([HIGH, LOW, EXPERIMENTAL, CRITICAL]).freeze
      end

      module AimRequester
        TEST = 1
        POTENTIAL_REACH_IMPORT = 2
        POTENTIAL_REACH_REFRESH = 3
        POTENTIAL_REACH_TEST = 4
        BALLPARK = 5
        BALLPARK_TEST = 6
        REFRESHER_NEW_DATA = 7
        REFRESHER_PERIODIC = 8
        REFRESHER_BACKFILL = 9
        REFRESHER_TEST = 10
        MATCHED_REACH_IMPORT = 11
        MATCHED_REACH_CONFIG_CHANGE = 12
        MATCHED_REACH_CLIENT_SIDE = 13
        MATCHED_REACH_BACKLOG = 14
        MATCHED_REACH_TEST = 15
        CUSTOMER_LINK = 16
        CUSTOMER_LINK_TEST = 17
        CID_FP = 18
        CID_FP_TEST = 19
        MAPPING_RECOMP_REQUESTER = 20
        ESTIMATED_REACH_SERVICE = 21
        ESTIMATED_REACH_SERVICE_TEST = 22
        PLIDS_PEL_STORE_UPDATERS = 23
        PL_ID_SYNCER = 24
        PL_MUID_MAPPING_UPDATER = 25
        PL_GRAPH_SAMPLER = 26
        REFRESHER_NEW_SEGMENT = 27
        CID_REFRESH_CRON = 28
        SSA_REFRESH_CRON = 29
        MAPPING_REFRESHER_CRON = 30
        VALUE_MAP = {1 => "TEST", 2 => "POTENTIAL_REACH_IMPORT", 3 => "POTENTIAL_REACH_REFRESH", 4 => "POTENTIAL_REACH_TEST", 5 => "BALLPARK", 6 => "BALLPARK_TEST", 7 => "REFRESHER_NEW_DATA", 8 => "REFRESHER_PERIODIC", 9 => "REFRESHER_BACKFILL", 10 => "REFRESHER_TEST", 11 => "MATCHED_REACH_IMPORT", 12 => "MATCHED_REACH_CONFIG_CHANGE", 13 => "MATCHED_REACH_CLIENT_SIDE", 14 => "MATCHED_REACH_BACKLOG", 15 => "MATCHED_REACH_TEST", 16 => "CUSTOMER_LINK", 17 => "CUSTOMER_LINK_TEST", 18 => "CID_FP", 19 => "CID_FP_TEST", 20 => "MAPPING_RECOMP_REQUESTER", 21 => "ESTIMATED_REACH_SERVICE", 22 => "ESTIMATED_REACH_SERVICE_TEST", 23 => "PLIDS_PEL_STORE_UPDATERS", 24 => "PL_ID_SYNCER", 25 => "PL_MUID_MAPPING_UPDATER", 26 => "PL_GRAPH_SAMPLER", 27 => "REFRESHER_NEW_SEGMENT", 28 => "CID_REFRESH_CRON", 29 => "SSA_REFRESH_CRON", 30 => "MAPPING_REFRESHER_CRON"}
        VALID_VALUES = Set.new([TEST, POTENTIAL_REACH_IMPORT, POTENTIAL_REACH_REFRESH, POTENTIAL_REACH_TEST, BALLPARK, BALLPARK_TEST, REFRESHER_NEW_DATA, REFRESHER_PERIODIC, REFRESHER_BACKFILL, REFRESHER_TEST, MATCHED_REACH_IMPORT, MATCHED_REACH_CONFIG_CHANGE, MATCHED_REACH_CLIENT_SIDE, MATCHED_REACH_BACKLOG, MATCHED_REACH_TEST, CUSTOMER_LINK, CUSTOMER_LINK_TEST, CID_FP, CID_FP_TEST, MAPPING_RECOMP_REQUESTER, ESTIMATED_REACH_SERVICE, ESTIMATED_REACH_SERVICE_TEST, PLIDS_PEL_STORE_UPDATERS, PL_ID_SYNCER, PL_MUID_MAPPING_UPDATER, PL_GRAPH_SAMPLER, REFRESHER_NEW_SEGMENT, CID_REFRESH_CRON, SSA_REFRESH_CRON, MAPPING_REFRESHER_CRON]).freeze
      end

      module AimConfigPrecision
        CROSSDEVICE_EXACT = 0
        CROSSDEVICE_PROBABILISTIC_A = 1
        CROSSDEVICE_PROBABILISTIC_B = 2
        CROSSDEVICE_PROBABILISTIC_C = 3
        CROSSDEVICE_PROBABILISTIC_D = 4
        CROSSDEVICE_EXACT_DEPRECATED = 5
        CROSSDEVICE_ENTITY = 6
        CROSSDEVICE_HOUSEHOLD = 7
        WATERFALL_NO_WATERFALLING = 8
        WATERFALL_INDIVIDUAL = 9
        WATERFALL_NAME_AND_POSTAL = 10
        WATERFALL_HOUSEHOLD = 11
        WATERFALL_ZIP_PLUS_SIX = 12
        WATERFALL_NAME_AND_CITY = 13
        WATERFALL_ZIP_PLUS_FOUR = 14
        WATERFALL_ZIP = 15
        VALUE_MAP = {0 => "CROSSDEVICE_EXACT", 1 => "CROSSDEVICE_PROBABILISTIC_A", 2 => "CROSSDEVICE_PROBABILISTIC_B", 3 => "CROSSDEVICE_PROBABILISTIC_C", 4 => "CROSSDEVICE_PROBABILISTIC_D", 5 => "CROSSDEVICE_EXACT_DEPRECATED", 6 => "CROSSDEVICE_ENTITY", 7 => "CROSSDEVICE_HOUSEHOLD", 8 => "WATERFALL_NO_WATERFALLING", 9 => "WATERFALL_INDIVIDUAL", 10 => "WATERFALL_NAME_AND_POSTAL", 11 => "WATERFALL_HOUSEHOLD", 12 => "WATERFALL_ZIP_PLUS_SIX", 13 => "WATERFALL_NAME_AND_CITY", 14 => "WATERFALL_ZIP_PLUS_FOUR", 15 => "WATERFALL_ZIP"}
        VALID_VALUES = Set.new([CROSSDEVICE_EXACT, CROSSDEVICE_PROBABILISTIC_A, CROSSDEVICE_PROBABILISTIC_B, CROSSDEVICE_PROBABILISTIC_C, CROSSDEVICE_PROBABILISTIC_D, CROSSDEVICE_EXACT_DEPRECATED, CROSSDEVICE_ENTITY, CROSSDEVICE_HOUSEHOLD, WATERFALL_NO_WATERFALLING, WATERFALL_INDIVIDUAL, WATERFALL_NAME_AND_POSTAL, WATERFALL_HOUSEHOLD, WATERFALL_ZIP_PLUS_SIX, WATERFALL_NAME_AND_CITY, WATERFALL_ZIP_PLUS_FOUR, WATERFALL_ZIP]).freeze
      end

      module AimProcess
        MSJ_BUILDER = 1
        PREGEL_BUILDER = 2
        BANG_GRAPH_TRANSLATOR = 3
        EXPOSURE_MATCHING_BUILDER = 4
        PARTNER_LINK_INDIVIDUAL_BUILDER = 5
        PARTNER_LINK_HH_ID_SYNC_BUILDER = 6
        PARTNER_LINK_HH_MATCH_BUILDER = 7
        VALUE_MAP = {1 => "MSJ_BUILDER", 2 => "PREGEL_BUILDER", 3 => "BANG_GRAPH_TRANSLATOR", 4 => "EXPOSURE_MATCHING_BUILDER", 5 => "PARTNER_LINK_INDIVIDUAL_BUILDER", 6 => "PARTNER_LINK_HH_ID_SYNC_BUILDER", 7 => "PARTNER_LINK_HH_MATCH_BUILDER"}
        VALID_VALUES = Set.new([MSJ_BUILDER, PREGEL_BUILDER, BANG_GRAPH_TRANSLATOR, EXPOSURE_MATCHING_BUILDER, PARTNER_LINK_INDIVIDUAL_BUILDER, PARTNER_LINK_HH_ID_SYNC_BUILDER, PARTNER_LINK_HH_MATCH_BUILDER]).freeze
      end

      module ConfigErrorCode
        UNKNOWN_ERROR = 1
        INVALID_IDENTIFIER_POOL_ERROR = 2
        INVALID_AIM_RULE_ERROR = 3
        INVALID_OWNERS_ERROR = 4
        INPUT_EDGE_OR_GRAPH_NOT_EXIST = 5
        VALUE_MAP = {1 => "UNKNOWN_ERROR", 2 => "INVALID_IDENTIFIER_POOL_ERROR", 3 => "INVALID_AIM_RULE_ERROR", 4 => "INVALID_OWNERS_ERROR", 5 => "INPUT_EDGE_OR_GRAPH_NOT_EXIST"}
        VALID_VALUES = Set.new([UNKNOWN_ERROR, INVALID_IDENTIFIER_POOL_ERROR, INVALID_AIM_RULE_ERROR, INVALID_OWNERS_ERROR, INPUT_EDGE_OR_GRAPH_NOT_EXIST]).freeze
      end

      module InternalErrorCode
        UNKNOWN_ERROR = 1
        AIM_CLERK_ERROR = 2
        DB_ERROR = 3
        HDFS_ERROR = 4
        ZK_LOCK_ERROR = 5
        VALUE_MAP = {1 => "UNKNOWN_ERROR", 2 => "AIM_CLERK_ERROR", 3 => "DB_ERROR", 4 => "HDFS_ERROR", 5 => "ZK_LOCK_ERROR"}
        VALID_VALUES = Set.new([UNKNOWN_ERROR, AIM_CLERK_ERROR, DB_ERROR, HDFS_ERROR, ZK_LOCK_ERROR]).freeze
      end

      module AimRequestStatus
        NEW = 1
        PROCESSING = 2
        COMPLETED = 3
        FAILED = 4
        CANCELLED = 5
        SCHEDULED = 6
        VALUE_MAP = {1 => "NEW", 2 => "PROCESSING", 3 => "COMPLETED", 4 => "FAILED", 5 => "CANCELLED", 6 => "SCHEDULED"}
        VALID_VALUES = Set.new([NEW, PROCESSING, COMPLETED, FAILED, CANCELLED, SCHEDULED]).freeze
      end

      module GraphBuilderBatchStatus
        PENDING = 1
        RUNNING = 2
        COMPLETED = 3
        TO_BE_RETRIED = 4
        FAILED = 5
        CANCELLED = 6
        VALUE_MAP = {1 => "PENDING", 2 => "RUNNING", 3 => "COMPLETED", 4 => "TO_BE_RETRIED", 5 => "FAILED", 6 => "CANCELLED"}
        VALID_VALUES = Set.new([PENDING, RUNNING, COMPLETED, TO_BE_RETRIED, FAILED, CANCELLED]).freeze
      end

      class IdentifierPool; end

      class AimRequest; end

      class PrecisionLevel < ::Thrift::Union; end

      class AimRequestConfigurationFlags; end

      class AimRequestConfiguration; end

      class AimRecord; end

      class EdgeDef; end

      class ConfigSpec; end

      class ConfigError < ::Thrift::Exception; end

      class InternalError < ::Thrift::Exception; end

      class AimRequestTimestampPair; end

      class ScheduledGraphBuilderBatch; end

      class OptionalScheduledGraphBuilderBatch; end

      class AimValidateRequestServiceResponse; end

      class AimStorePartitionVersionStat; end

      class IdentifierPool
        include ::Thrift::Struct, ::Thrift::Struct_Union
        IDENTIFIER_TYPE_ID = 1
        POOL_ID = 2
        MAX_DAYS_SINCE_LAST_SYNCED_AT = 3
        MAX_DAYS_SINCE_LAST_SEEN_AT = 4

        FIELDS = {
          IDENTIFIER_TYPE_ID => {:type => ::Thrift::Types::I16, :name => 'identifier_type_id'},
          POOL_ID => {:type => ::Thrift::Types::I64, :name => 'pool_id', :optional => true},
          MAX_DAYS_SINCE_LAST_SYNCED_AT => {:type => ::Thrift::Types::I32, :name => 'max_days_since_last_synced_at', :optional => true},
          MAX_DAYS_SINCE_LAST_SEEN_AT => {:type => ::Thrift::Types::I32, :name => 'max_days_since_last_seen_at', :optional => true}
        }

        def struct_fields; FIELDS; end

        def validate
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field identifier_type_id is unset!') unless @identifier_type_id
        end

        ::Thrift::Struct.generate_accessors self
      end

      class AimRequest
        include ::Thrift::Struct, ::Thrift::Struct_Union
        SOURCE = 1
        TARGET = 2
        PRIORITY = 3
        MAX_AGE_IN_SECS = 4
        RULE = 5
        OWNERS = 6
        REQUESTER = 7
        AIM_CONFIGURATION_ID = 8

        FIELDS = {
          SOURCE => {:type => ::Thrift::Types::STRUCT, :name => 'source', :class => ::Liveramp::Types::AnonymousIdentifierMapping::IdentifierPool},
          TARGET => {:type => ::Thrift::Types::STRUCT, :name => 'target', :class => ::Liveramp::Types::AnonymousIdentifierMapping::IdentifierPool},
          PRIORITY => {:type => ::Thrift::Types::I32, :name => 'priority', :enum_class => ::Liveramp::Types::AnonymousIdentifierMapping::Priority},
          MAX_AGE_IN_SECS => {:type => ::Thrift::Types::I32, :name => 'max_age_in_secs'},
          RULE => {:type => ::Thrift::Types::I32, :name => 'rule', :enum_class => ::Liveramp::Types::AnonymousIdentifierMapping::AimRule},
          OWNERS => {:type => ::Thrift::Types::SET, :name => 'owners', :element => {:type => ::Thrift::Types::I64}},
          REQUESTER => {:type => ::Thrift::Types::I32, :name => 'requester', :optional => true, :enum_class => ::Liveramp::Types::AnonymousIdentifierMapping::AimRequester},
          AIM_CONFIGURATION_ID => {:type => ::Thrift::Types::I64, :name => 'aim_configuration_id', :optional => true}
        }

        def struct_fields; FIELDS; end

        def validate
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field source is unset!') unless @source
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field target is unset!') unless @target
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field priority is unset!') unless @priority
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field max_age_in_secs is unset!') unless @max_age_in_secs
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field rule is unset!') unless @rule
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field owners is unset!') unless @owners
          unless @priority.nil? || ::Liveramp::Types::AnonymousIdentifierMapping::Priority::VALID_VALUES.include?(@priority)
            raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Invalid value of field priority!')
          end
          unless @rule.nil? || ::Liveramp::Types::AnonymousIdentifierMapping::AimRule::VALID_VALUES.include?(@rule)
            raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Invalid value of field rule!')
          end
          unless @requester.nil? || ::Liveramp::Types::AnonymousIdentifierMapping::AimRequester::VALID_VALUES.include?(@requester)
            raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Invalid value of field requester!')
          end
        end

        ::Thrift::Struct.generate_accessors self
      end

      class PrecisionLevel < ::Thrift::Union
        include ::Thrift::Struct_Union
        class << self
          def online_accuracy_level(val)
            PrecisionLevel.new(:online_accuracy_level, val)
          end

          def offline_accuracy_level(val)
            PrecisionLevel.new(:offline_accuracy_level, val)
          end
        end

        ONLINE_ACCURACY_LEVEL = 1
        OFFLINE_ACCURACY_LEVEL = 2

        FIELDS = {
          ONLINE_ACCURACY_LEVEL => {:type => ::Thrift::Types::I32, :name => 'online_accuracy_level', :optional => true, :enum_class => ::Liveramp::Audience::CrossDeviceAccuracyLevel},
          OFFLINE_ACCURACY_LEVEL => {:type => ::Thrift::Types::I32, :name => 'offline_accuracy_level', :optional => true, :enum_class => ::Liveramp::Audience::WaterfallLevel}
        }

        def struct_fields; FIELDS; end

        def validate
          raise(StandardError, 'Union fields are not set.') if get_set_field.nil? || get_value.nil?
          if get_set_field == :online_accuracy_level
            raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Invalid value of field online_accuracy_level!') unless ::Liveramp::Audience::CrossDeviceAccuracyLevel::VALID_VALUES.include?(get_value)
          end
          if get_set_field == :offline_accuracy_level
            raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Invalid value of field offline_accuracy_level!') unless ::Liveramp::Audience::WaterfallLevel::VALID_VALUES.include?(get_value)
          end
        end

        ::Thrift::Union.generate_accessors self
      end

      class AimRequestConfigurationFlags
        include ::Thrift::Struct, ::Thrift::Struct_Union
        PARTNER_LINK = 3

        FIELDS = {
          # This field is now used as isRequirePlaceholderPel. In the futuer a new
# require_place_holder_pel field should be added, and this field can be removed.
# 
          PARTNER_LINK => {:type => ::Thrift::Types::BOOL, :name => 'partner_link', :optional => true}
        }

        def struct_fields; FIELDS; end

        def validate
        end

        ::Thrift::Struct.generate_accessors self
      end

      class AimRequestConfiguration
        include ::Thrift::Struct, ::Thrift::Struct_Union
        SOURCE = 1
        TARGET = 2
        PRIORITY = 3
        PRECISION_LEVEL = 4
        OWNERS = 5
        FLAGS = 6
        MAX_AGE_IN_SECS = 7
        REQUESTER = 8

        FIELDS = {
          SOURCE => {:type => ::Thrift::Types::STRUCT, :name => 'source', :class => ::Liveramp::Types::AnonymousIdentifierMapping::IdentifierPool},
          TARGET => {:type => ::Thrift::Types::STRUCT, :name => 'target', :class => ::Liveramp::Types::AnonymousIdentifierMapping::IdentifierPool},
          PRIORITY => {:type => ::Thrift::Types::I32, :name => 'priority', :enum_class => ::Liveramp::Types::AnonymousIdentifierMapping::Priority},
          PRECISION_LEVEL => {:type => ::Thrift::Types::STRUCT, :name => 'precision_level', :class => ::Liveramp::Types::AnonymousIdentifierMapping::PrecisionLevel},
          OWNERS => {:type => ::Thrift::Types::SET, :name => 'owners', :element => {:type => ::Thrift::Types::I64}},
          FLAGS => {:type => ::Thrift::Types::STRUCT, :name => 'flags', :class => ::Liveramp::Types::AnonymousIdentifierMapping::AimRequestConfigurationFlags},
          MAX_AGE_IN_SECS => {:type => ::Thrift::Types::I32, :name => 'max_age_in_secs', :optional => true},
          REQUESTER => {:type => ::Thrift::Types::I32, :name => 'requester', :optional => true, :enum_class => ::Liveramp::Types::AnonymousIdentifierMapping::AimRequester}
        }

        def struct_fields; FIELDS; end

        def validate
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field source is unset!') unless @source
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field target is unset!') unless @target
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field priority is unset!') unless @priority
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field precision_level is unset!') unless @precision_level
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field owners is unset!') unless @owners
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field flags is unset!') unless @flags
          unless @priority.nil? || ::Liveramp::Types::AnonymousIdentifierMapping::Priority::VALID_VALUES.include?(@priority)
            raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Invalid value of field priority!')
          end
          unless @requester.nil? || ::Liveramp::Types::AnonymousIdentifierMapping::AimRequester::VALID_VALUES.include?(@requester)
            raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Invalid value of field requester!')
          end
        end

        ::Thrift::Struct.generate_accessors self
      end

      # confidence is an integer in range [0, 100].
# 
      class AimRecord
        include ::Thrift::Struct, ::Thrift::Struct_Union
        SOURCE = 1
        TARGET = 2
        INTERNAL_TARGET = 3
        CONFIDENCE = 4
        TARGET_METADATA = 5
        CONFIG_DIGEST = 6

        FIELDS = {
          SOURCE => {:type => ::Thrift::Types::STRUCT, :name => 'source', :class => ::Liveramp::Types::Bang::AnonymousIdentifier},
          TARGET => {:type => ::Thrift::Types::STRUCT, :name => 'target', :class => ::Liveramp::Types::Bang::AnonymousIdentifier},
          INTERNAL_TARGET => {:type => ::Thrift::Types::STRUCT, :name => 'internal_target', :class => ::Liveramp::Types::Bang::AnonymousIdentifier},
          CONFIDENCE => {:type => ::Thrift::Types::I32, :name => 'confidence'},
          TARGET_METADATA => {:type => ::Thrift::Types::STRUCT, :name => 'target_metadata', :class => ::Liveramp::Types::Bang::AnonymousIdentifierMetadata, :optional => true},
          CONFIG_DIGEST => {:type => ::Thrift::Types::I32, :name => 'config_digest', :optional => true}
        }

        def struct_fields; FIELDS; end

        def validate
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field source is unset!') unless @source
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field target is unset!') unless @target
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field internal_target is unset!') unless @internal_target
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field confidence is unset!') unless @confidence
        end

        ::Thrift::Struct.generate_accessors self
      end

      # Definition of an edge to be used when building paths for a specific source-target config. This
# is internal to OI.
      class EdgeDef
        include ::Thrift::Struct, ::Thrift::Struct_Union
        SOURCE = 1
        TARGET = 2
        TYPE = 3

        FIELDS = {
          SOURCE => {:type => ::Thrift::Types::STRUCT, :name => 'source', :class => ::Liveramp::Types::AnonymousIdentifierMapping::IdentifierPool},
          TARGET => {:type => ::Thrift::Types::STRUCT, :name => 'target', :class => ::Liveramp::Types::AnonymousIdentifierMapping::IdentifierPool},
          TYPE => {:type => ::Thrift::Types::I32, :name => 'type', :enum_class => ::Liveramp::Types::Bang::BangEdgeType}
        }

        def struct_fields; FIELDS; end

        def validate
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field source is unset!') unless @source
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field target is unset!') unless @target
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field type is unset!') unless @type
          unless @type.nil? || ::Liveramp::Types::Bang::BangEdgeType::VALID_VALUES.include?(@type)
            raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Invalid value of field type!')
          end
        end

        ::Thrift::Struct.generate_accessors self
      end

      # Full specification of what should be used to create a mapping between a source and target identifier.
# 
      class ConfigSpec
        include ::Thrift::Struct, ::Thrift::Struct_Union
        PATH_DEFS = 1
        OWNERS = 2

        FIELDS = {
          # All possible paths connecting a source to a target. By definition, they all have the same
# source and target identifiers. It's only the vertices in the middle that vary.
# 
          PATH_DEFS => {:type => ::Thrift::Types::SET, :name => 'path_defs', :element => {:type => ::Thrift::Types::LIST, :element => {:type => ::Thrift::Types::STRUCT, :class => ::Liveramp::Types::AnonymousIdentifierMapping::EdgeDef}}},
          OWNERS => {:type => ::Thrift::Types::SET, :name => 'owners', :element => {:type => ::Thrift::Types::I64}}
        }

        def struct_fields; FIELDS; end

        def validate
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field path_defs is unset!') unless @path_defs
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field owners is unset!') unless @owners
        end

        ::Thrift::Struct.generate_accessors self
      end

      class ConfigError < ::Thrift::Exception
        include ::Thrift::Struct, ::Thrift::Struct_Union
        CONFIG_ERROR_CODE = 1
        REASON = 2

        FIELDS = {
          CONFIG_ERROR_CODE => {:type => ::Thrift::Types::I32, :name => 'config_error_code', :enum_class => ::Liveramp::Types::AnonymousIdentifierMapping::ConfigErrorCode},
          REASON => {:type => ::Thrift::Types::STRING, :name => 'reason', :optional => true}
        }

        def struct_fields; FIELDS; end

        def validate
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field config_error_code is unset!') unless @config_error_code
          unless @config_error_code.nil? || ::Liveramp::Types::AnonymousIdentifierMapping::ConfigErrorCode::VALID_VALUES.include?(@config_error_code)
            raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Invalid value of field config_error_code!')
          end
        end

        ::Thrift::Struct.generate_accessors self
      end

      class InternalError < ::Thrift::Exception
        include ::Thrift::Struct, ::Thrift::Struct_Union
        INTERNAL_ERROR_CODE = 1
        REASON = 2

        FIELDS = {
          INTERNAL_ERROR_CODE => {:type => ::Thrift::Types::I32, :name => 'internal_error_code', :enum_class => ::Liveramp::Types::AnonymousIdentifierMapping::InternalErrorCode},
          REASON => {:type => ::Thrift::Types::STRING, :name => 'reason', :optional => true}
        }

        def struct_fields; FIELDS; end

        def validate
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field internal_error_code is unset!') unless @internal_error_code
          unless @internal_error_code.nil? || ::Liveramp::Types::AnonymousIdentifierMapping::InternalErrorCode::VALID_VALUES.include?(@internal_error_code)
            raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Invalid value of field internal_error_code!')
          end
        end

        ::Thrift::Struct.generate_accessors self
      end

      class AimRequestTimestampPair
        include ::Thrift::Struct, ::Thrift::Struct_Union
        AIMREQUEST = 1
        TIMESTAMP = 2

        FIELDS = {
          AIMREQUEST => {:type => ::Thrift::Types::STRUCT, :name => 'aimRequest', :class => ::Liveramp::Types::AnonymousIdentifierMapping::AimRequest},
          TIMESTAMP => {:type => ::Thrift::Types::I64, :name => 'timestamp'}
        }

        def struct_fields; FIELDS; end

        def validate
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field aimRequest is unset!') unless @aimRequest
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field timestamp is unset!') unless @timestamp
        end

        ::Thrift::Struct.generate_accessors self
      end

      class ScheduledGraphBuilderBatch
        include ::Thrift::Struct, ::Thrift::Struct_Union
        BATCH_ID = 1
        AIM_REQUESTS = 2

        FIELDS = {
          BATCH_ID => {:type => ::Thrift::Types::I64, :name => 'batch_id'},
          AIM_REQUESTS => {:type => ::Thrift::Types::SET, :name => 'aim_requests', :element => {:type => ::Thrift::Types::STRUCT, :class => ::Liveramp::Types::AnonymousIdentifierMapping::AimRequest}}
        }

        def struct_fields; FIELDS; end

        def validate
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field batch_id is unset!') unless @batch_id
        end

        ::Thrift::Struct.generate_accessors self
      end

      class OptionalScheduledGraphBuilderBatch
        include ::Thrift::Struct, ::Thrift::Struct_Union
        BATCH = 1

        FIELDS = {
          BATCH => {:type => ::Thrift::Types::STRUCT, :name => 'batch', :class => ::Liveramp::Types::AnonymousIdentifierMapping::ScheduledGraphBuilderBatch, :optional => true}
        }

        def struct_fields; FIELDS; end

        def validate
        end

        ::Thrift::Struct.generate_accessors self
      end

      # Wraps ConfigError so as to allow null value which would
# indicate that the request was valid.
      class AimValidateRequestServiceResponse
        include ::Thrift::Struct, ::Thrift::Struct_Union
        CONFIGERROR = 1

        FIELDS = {
          CONFIGERROR => {:type => ::Thrift::Types::STRUCT, :name => 'configError', :class => ::Liveramp::Types::AnonymousIdentifierMapping::ConfigError, :optional => true}
        }

        def struct_fields; FIELDS; end

        def validate
        end

        ::Thrift::Struct.generate_accessors self
      end

      class AimStorePartitionVersionStat
        include ::Thrift::Struct, ::Thrift::Struct_Union
        SOURCE_IDENTIFIER_TYPE_ID = 1
        SOURCE_POOL_ID = 2
        TARGET_IDENTIFIER_TYPE_ID = 3
        TARGET_POOL_ID = 4
        CONFIG_DIGEST = 5
        VERSION = 6
        TOTAL_RECORD_COUNT = 7
        UNIQ_SOURCE_COUNT = 8

        FIELDS = {
          SOURCE_IDENTIFIER_TYPE_ID => {:type => ::Thrift::Types::I16, :name => 'source_identifier_type_id'},
          SOURCE_POOL_ID => {:type => ::Thrift::Types::I64, :name => 'source_pool_id'},
          TARGET_IDENTIFIER_TYPE_ID => {:type => ::Thrift::Types::I16, :name => 'target_identifier_type_id'},
          TARGET_POOL_ID => {:type => ::Thrift::Types::I64, :name => 'target_pool_id'},
          CONFIG_DIGEST => {:type => ::Thrift::Types::I32, :name => 'config_digest'},
          VERSION => {:type => ::Thrift::Types::I32, :name => 'version'},
          TOTAL_RECORD_COUNT => {:type => ::Thrift::Types::I64, :name => 'total_record_count'},
          UNIQ_SOURCE_COUNT => {:type => ::Thrift::Types::I64, :name => 'uniq_source_count'}
        }

        def struct_fields; FIELDS; end

        def validate
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field source_identifier_type_id is unset!') unless @source_identifier_type_id
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field source_pool_id is unset!') unless @source_pool_id
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field target_identifier_type_id is unset!') unless @target_identifier_type_id
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field target_pool_id is unset!') unless @target_pool_id
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field config_digest is unset!') unless @config_digest
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field version is unset!') unless @version
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field total_record_count is unset!') unless @total_record_count
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field uniq_source_count is unset!') unless @uniq_source_count
        end

        ::Thrift::Struct.generate_accessors self
      end

    end
  end
end