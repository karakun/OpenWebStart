package com.openwebstart.jvm.json;

import com.google.gson.JsonSyntaxException;
import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import com.openwebstart.jvm.runtimes.RemoteJavaRuntime;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class JsonHandlerTest {

    @Test
    void checkModelToJsonConversion() {

        //given
        Person mark = new Person();
        mark.setAge("20");
        mark.setName("mark");

        //when
        final String handler = JsonHandler.getInstance().toJson(mark);

        //then
        assertTrue(handler.contains("mark"));
        assertTrue(handler.contains("20"));

    }


    @Test
    void checkJsonToModelConversion() {
        // given
        final String model = "{\"age\":\"20\",\"name\":\"mark\"}";

        //when
        final Person mark = JsonHandler.getInstance().fromJson(model, Person.class);

        //then
        assertEquals("20", mark.getAge());
        assertEquals("mark", mark.getName());
    }

    @Test
    void checkErrorInJsonToModelConversion() {
        //given
        final String model = "{\"age1\":\"20\",\"name1\":\"mark\"}";

        //when
        Exception exception = assertThrows(JsonSyntaxException.class,
                () -> JsonHandler.getInstance().fromJson(model, String.class)
        );

        //then
        assertEquals("Error in JSON conversion", exception.getMessage());
    }

    @Test
    void checkModelToJsonToModel() {

        //given
        final Person mark = new Person();
        mark.setAge("20");
        mark.setName("mark");

        //when
        String handler = JsonHandler.getInstance().toJson(mark);
        final Person markCopy = JsonHandler.getInstance().fromJson(handler, Person.class);

        //then
        assertEquals("20", markCopy.getAge());
        assertEquals("mark", markCopy.getName());

    }

    // This will verify the LocalJavaRuntime class is getting converted to json object
    @Test
    void CheckConversion1() {
        final String version = "1.0";
        final LocalDateTime currentTime = LocalDateTime.now();
        final LocalJavaRuntime runtime = new LocalJavaRuntime(version, OperationSystem.ARM32, "vendor", Paths.get("/path"), currentTime, Boolean.TRUE, Boolean.FALSE);
        // JavaHome will be written to Json after converting it to URI
        final Path expectedJavaHome = Paths.get(runtime.getJavaHome().toUri());

        //when
        final String handler = JsonHandler.getInstance().toJson(runtime);
        final LocalJavaRuntime runtimecopy = JsonHandler.getInstance().fromJson(handler, LocalJavaRuntime.class);

        //then
        assertEquals(runtime.getVersion(), runtimecopy.getVersion());
        assertEquals(runtime.getLastUsage(), runtimecopy.getLastUsage());
        assertEquals(expectedJavaHome, runtimecopy.getJavaHome());
        assertEquals(runtime.getOperationSystem(), runtimecopy.getOperationSystem());
        assertEquals(runtime.getLastUsage(), runtimecopy.getLastUsage());
        assertEquals(runtime.isActive(), runtimecopy.isActive());
        assertEquals(runtime.isManaged(), runtimecopy.isManaged());

    }

    // This will verify the RemoteJavaRuntime class is getting converted to json object
    @Test
    void CheckConversion2() {
        final String theOneAndOnlyJdkZip = "http://localhost:8090/jvms/jdk.zip";
        final RemoteJavaRuntime runtime = new RemoteJavaRuntime("1.8.145", OperationSystem.ARM32, "adopt", theOneAndOnlyJdkZip);

        //when
        final String handler = JsonHandler.getInstance().toJson(runtime);
        final RemoteJavaRuntime runtimecopy = JsonHandler.getInstance().fromJson(handler, RemoteJavaRuntime.class);

        //then
        assertEquals(runtime.getVersion(), runtimecopy.getVersion());
        assertEquals(runtime.getVendor(), runtimecopy.getVendor());
        assertEquals(runtime.getOperationSystem(), runtimecopy.getOperationSystem());
        assertEquals(runtime.getHref(), runtimecopy.getHref());
    }

    // This will verify the RemoteRuntimeList class is getting converted to json object
    @Test
    void CheckConversion3() {
        final List<RemoteJavaRuntime> runtimes = new CopyOnWriteArrayList<>();
        final String theOneAndOnlyJdkZip = "http://localhost:8090/jvms/jdk.zip";
        runtimes.add(new RemoteJavaRuntime("1.8.145", OperationSystem.ARM32, "adopt", theOneAndOnlyJdkZip));

        final RemoteRuntimeList runtime = new RemoteRuntimeList(runtimes, 5_000);

        //when
        final String handler = JsonHandler.getInstance().toJson(runtime);
        final RemoteRuntimeList runtimecopy = JsonHandler.getInstance().fromJson(handler, RemoteRuntimeList.class);

        //then
        final RemoteJavaRuntime originalRuntime = runtime.getRuntimes().get(0);
        final RemoteJavaRuntime copyRunTime = runtimecopy.getRuntimes().get(0);

        assertEquals(originalRuntime.getVersion(), copyRunTime.getVersion());
        assertEquals(originalRuntime.getVendor(), copyRunTime.getVendor());
        assertEquals(originalRuntime.getOperationSystem(), copyRunTime.getOperationSystem());
        assertEquals(originalRuntime.getHref(), copyRunTime.getHref());
        assertEquals(runtime.getCacheTimeInMillis(), runtimecopy.getCacheTimeInMillis());

    }

}
