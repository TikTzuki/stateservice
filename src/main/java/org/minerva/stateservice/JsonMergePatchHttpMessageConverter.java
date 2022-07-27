package org.minerva.stateservice;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;

import javax.json.Json;
import javax.json.JsonMergePatch;
import javax.json.JsonReader;
import javax.json.JsonWriter;

@Component
public class JsonMergePatchHttpMessageConverter extends AbstractHttpMessageConverter<JsonMergePatch> {
    public JsonMergePatchHttpMessageConverter() {
        super(MediaType.valueOf("application/merge-patch+json"));
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return JsonMergePatch.class.isAssignableFrom(clazz);
    }

    @Override
    @NotNull
    protected JsonMergePatch readInternal(Class<? extends JsonMergePatch> clazz, HttpInputMessage inputMessage) throws HttpMessageNotReadableException {
        try (JsonReader reader = Json.createReader(inputMessage.getBody())) {
            return Json.createMergePatch(reader.readValue());
        } catch (Exception e) {
            throw new HttpMessageNotReadableException(e.getMessage(), inputMessage);
        }
    }

    @Override
    protected void writeInternal(JsonMergePatch mergePatch, HttpOutputMessage outputMessage) throws HttpMessageNotWritableException {
        try (JsonWriter writer = Json.createWriter(outputMessage.getBody())) {
            writer.write(mergePatch.toJsonValue());
        } catch (Exception e) {
            throw new HttpMessageNotWritableException(e.getMessage(), e);
        }
    }
}
