package com.openwebstart.jvm.json;

import com.google.gson.JsonSyntaxException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;


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
    void checkErrorInJsonToModelConversion() {
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

    @Test
    void checkModelToJsonToModel() {

        //given
        Person mark = new Person();
        mark.setAge("20");
        mark.setName("mark");

        //when
        String handler = JsonHandler.getInstance().toJson(mark);
        mark =  JsonHandler.getInstance().fromJson(handler,Person.class);

        //then
        assertEquals("20",mark.getAge());
        assertEquals("mark",mark.getName());

    }


}
