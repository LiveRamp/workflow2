package com.liveramp.cascading_ext.resource;

import java.io.IOException;

import org.apache.commons.codec.binary.Base64;

import com.liveramp.commons.util.serialization.JavaObjectSerializationHandler;
import com.liveramp.commons.util.serialization.SerializationHandler;

public class JavaObjectStorageSerializer implements StorageSerializer {

  SerializationHandler handler = new JavaObjectSerializationHandler();

  @Override
  public String serialize(Object o) {
    try {
      return Base64.encodeBase64String(handler.serialize(o));
    } catch (IOException e) {
      throw new RuntimeException("Failed to serialize resource " + o, e);
    }
  }

  @Override
  public Object deSerialize(String s) {
    try {
      return handler.deserialize(Base64.decodeBase64(s));
    } catch (IOException e) {
      throw new RuntimeException("Failed to deserialize resource " + s, e);
    }
  }
}
