#
# Autogenerated by Thrift Compiler (0.11.0)
#
# DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
#

require 'thrift'

module Liveramp
  module Om
    module DaemonExt
      class StringError; end

      class VoidResult; end

      class StringError
        include ::Thrift::Struct, ::Thrift::Struct_Union
        ERROR = 1

        FIELDS = {
          ERROR => {:type => ::Thrift::Types::STRING, :name => 'error'}
        }

        def struct_fields; FIELDS; end

        def validate
          raise ::Thrift::ProtocolException.new(::Thrift::ProtocolException::UNKNOWN, 'Required field error is unset!') unless @error
        end

        ::Thrift::Struct.generate_accessors self
      end

      class VoidResult
        include ::Thrift::Struct, ::Thrift::Struct_Union

        FIELDS = {

        }

        def struct_fields; FIELDS; end

        def validate
        end

        ::Thrift::Struct.generate_accessors self
      end

    end
  end
end