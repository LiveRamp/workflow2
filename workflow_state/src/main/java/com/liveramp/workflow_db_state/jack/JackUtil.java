package com.liveramp.workflow_db_state.jack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import com.rapleaf.jack.AttributesWithId;
import com.rapleaf.jack.ModelWithId;

public class JackUtil {

  public static <E extends ModelWithId> List<E> sortDescending(Collection<E> items) {
    ArrayList<E> list = Lists.newArrayList(items);

    Collections.sort(list,
        (o1, o2) -> new CompareToBuilder()
            .append(o2.getId(), o1.getId())
            .build());

    return list;
  }

  public static <K, T extends ModelWithId> Multimap<K, T> by(Collection<T> models, Enum field) {
    return by(models, field.name());
  }

  private static <K, T extends ModelWithId> Multimap<K, T> by(Collection<T> models, String field) {

    Multimap<K, T> byField = HashMultimap.create();
    for (T model : models) {
      byField.put((K)model.getField(field), model);
    }

    return byField;
  }

  public static <T extends ModelWithId> Map<Long, T> byId(Collection<T> models) {
    Map<Long, T> byId = Maps.newHashMap();
    for (T model : models) {
      byId.put(model.getId(), model);
    }
    return byId;
  }

  public static JSONArray toJSON(Collection<AttributesWithId> models,
                                 Map<? extends Enum, Class<? extends Enum>> enumTranslation,
                                 Object defaultValue) {
    JSONArray array = new JSONArray();
    for (AttributesWithId model : models) {
      array.put(toJSON(model, enumTranslation, defaultValue));
    }
    return array;
  }

  public static JSONObject toJSON(com.rapleaf.jack.AttributesWithId model,
                                  Map<? extends Enum, Class<? extends Enum>> enumTranslation,
                                  Object defaultValue) {
    try {

      JSONObject obj = new JSONObject();
      obj.put("id", model.getId());

      for (Enum anEnum : model.getFieldSet()) {
        String fieldName = anEnum.name();
        Object field = model.getField(fieldName);

        if (field == null) {
          obj.put(fieldName, defaultValue);
        } else {
          if (enumTranslation.containsKey(anEnum)) {
            obj.put(fieldName, enumTranslation.get(anEnum).getEnumConstants()[(Integer)field].name());
          } else {
            obj.put(fieldName, field);
          }
        }
      }

      return obj;

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
