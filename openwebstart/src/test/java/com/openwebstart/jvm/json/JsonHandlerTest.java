package com.openwebstart.jvm.json;

import com.openwebstart.mimetype.MimeType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonHandlerTest {

    @Test
    void checkJsonConversion() {

        //given
        PersonModel model = new PersonModel();
        model.setAge("20");
        model.setName("mark");

        //when
        String handler = JsonHandler.getInstance().toJson(model);

        //than
        assertTrue(handler.contains("mark"));
        assertTrue(handler.contains("20"));

    }




}
