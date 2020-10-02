package com.openwebstart.jvm.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import  com.openwebstart.jvm.json.Person;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.runtimes.RemoteJavaRuntime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JsonHandlerTest {

    @Test
    void checkModelToJsonConversion() {

        //given
        Person mark = new Person();
        mark.setAge("20");
        mark.setName("mark");

        //when
        String handler = JsonHandler.getInstance().toJson(mark);

        //then
        assertTrue(handler.contains("mark"));
        assertTrue(handler.contains("20"));

    }


    @Test
    void checkJsonToModelConversion() {
        // given
        String model = "{\"age\":\"20\",\"name\":\"mark\"}";

        //when
        Person mark =  JsonHandler.getInstance().fromJson(model,Person.class);

        //then
        assertEquals("20",mark.getAge());
        assertEquals("mark",mark.getName());
    }

    @Test
    void CheckErrorInJsonToModelConversion() {
        //given
        String model = "{\"age1\":\"20\",\"name1\":\"mark\"}";

        //when
        Exception exception = assertThrows(JsonSyntaxException.class, () -> {
            JsonHandler.getInstance().fromJson(model,String.class);
        });

        exception.getMessage();

        //then
        assertEquals("Error in JSON conversion",exception.getMessage());
    }




}
