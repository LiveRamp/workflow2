#
# Autogenerated by Thrift Compiler (0.11.0)
#
# DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
#

require 'thrift'
require File.join File.dirname(__FILE__), 'new_person_data_types'
require File.join File.dirname(__FILE__), 'bang_types'
require File.join File.dirname(__FILE__), 'parc_types'
require File.join File.dirname(__FILE__), 'importer_types'


module Liveramp
  module Importer
    module IdentifierOnly
      class OfflineOrAnonymousIdentifier < ::Thrift::Union; end

      class IdentifierAndKey; end

      class IdentifierOnlyPiiImportRecord; end

      class OfflineOrAnonymousIdentifier < ::Thrift::Union
        include ::Thrift::Struct_Union
        class << self
          def pin(val)
            OfflineOrAnonymousIdentifier.new(:pin, val)
          end

          def anonymous_identifier(val)
            OfflineOrAnonymousIdentifier.new(:anonymous_identifier, val)
          end
        end

        PIN = 1
        ANONYMOUS_IDENTIFIER = 2

        FIELDS = {
          PIN => {:type => ::Thrift::Types::STRUCT, :name => 'pin', :class => ::Rapleaf::Types::NewPersonData::PIN, :optional => true},
          ANONYMOUS_IDENTIFIER => {:type => ::Thrift::Types::STRUCT, :name => 'anonymous_identifier', :class => ::Liveramp::Types::Bang::AnonymousIdentifier, :optional => true}
        }

        def struct_fields; FIELDS; end

        def validate
          raise(StandardError, 'Union fields are not set.') if get_set_field.nil? || get_value.nil?
        end

        ::Thrift::Union.generate_accessors self
      end

      class IdentifierAndKey
        include ::Thrift::Struct, ::Thrift::Struct_Union
        IDENTIFIER = 1
        KEY = 2

        FIELDS = {
          IDENTIFIER => {:type => ::Thrift::Types::STRUCT, :name => 'identifier', :class => ::Liveramp::Importer::IdentifierOnly::OfflineOrAnonymousIdentifier},
          KEY => {:type => ::Thrift::Types::STRING, :name => 'key'}
        }

        def struct_fields; FIELDS; end

        def validate
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field identifier is unset!') unless @identifier
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field key is unset!') unless @key
        end

        ::Thrift::Struct.generate_accessors self
      end

      class IdentifierOnlyPiiImportRecord
        include ::Thrift::Struct, ::Thrift::Struct_Union
        METADATA = 1
        IDENTIFIERS = 2
        KEY_TO_UNPARSED_DATA = 3
        KEY_TO_ANONYMIZED_STRIPPED_VALUES = 4
        LEGACY_AUDIENCE_KEY = 5
        KEY_TO_IDENTIFIERS = 6
        DATA_TOKENS = 7

        FIELDS = {
          METADATA => {:type => ::Thrift::Types::STRUCT, :name => 'metadata', :class => ::Liveramp::Types::PARC::RecordMetadata},
          # Deprecated. Use key-to_identifiers instead.
# 
          IDENTIFIERS => {:type => ::Thrift::Types::SET, :name => 'identifiers', :element => {:type => ::Thrift::Types::STRUCT, :class => ::Rapleaf::Types::NewPersonData::PIN}},
          KEY_TO_UNPARSED_DATA => {:type => ::Thrift::Types::MAP, :name => 'key_to_unparsed_data', :key => {:type => ::Thrift::Types::STRING}, :value => {:type => ::Thrift::Types::STRING}},
          KEY_TO_ANONYMIZED_STRIPPED_VALUES => {:type => ::Thrift::Types::MAP, :name => 'key_to_anonymized_stripped_values', :key => {:type => ::Thrift::Types::STRING}, :value => {:type => ::Thrift::Types::STRING}},
          LEGACY_AUDIENCE_KEY => {:type => ::Thrift::Types::STRING, :name => 'legacy_audience_key', :binary => true, :optional => true},
          KEY_TO_IDENTIFIERS => {:type => ::Thrift::Types::MAP, :name => 'key_to_identifiers', :key => {:type => ::Thrift::Types::STRING}, :value => {:type => ::Thrift::Types::STRUCT, :class => ::Rapleaf::Types::NewPersonData::PIN}, :optional => true},
          DATA_TOKENS => {:type => ::Thrift::Types::LIST, :name => 'data_tokens', :element => {:type => ::Thrift::Types::STRUCT, :class => ::Rapleaf::Types::Importer::RecordToken}, :optional => true}
        }

        def struct_fields; FIELDS; end

        def validate
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field metadata is unset!') unless @metadata
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field identifiers is unset!') unless @identifiers
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field key_to_unparsed_data is unset!') unless @key_to_unparsed_data
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field key_to_anonymized_stripped_values is unset!') unless @key_to_anonymized_stripped_values
        end

        ::Thrift::Struct.generate_accessors self
      end

    end
  end
end