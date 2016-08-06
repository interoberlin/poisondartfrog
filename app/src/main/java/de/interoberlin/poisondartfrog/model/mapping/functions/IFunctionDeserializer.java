package de.interoberlin.poisondartfrog.model.mapping.functions;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;


public class IFunctionDeserializer implements JsonDeserializer<IFunction> {

    public IFunction deserialize(JsonElement json, Type typeOfT,
                                 JsonDeserializationContext context) throws JsonParseException {

        String type = json.getAsJsonObject().get("type").getAsString();
        Class c = EFunctionType.valueOf(type.toUpperCase()).getC();

        if (c == null)
            throw new RuntimeException("Unknown type: " + type);
        return context.deserialize(json, c);
    }
}
