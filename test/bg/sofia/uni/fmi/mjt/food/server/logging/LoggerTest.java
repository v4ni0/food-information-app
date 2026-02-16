package bg.sofia.uni.fmi.mjt.food.server.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LoggerTest {
    private Logger logger;
    private Path logFile;


    @BeforeEach
    void setUp() {
        String testFileName = "test-log.txt";
        logger = Logger.createWithCustomFileName(testFileName);
        logFile = Path.of("logs").resolve(testFileName);
    }

    @Test
    void testLogCreatesFile() {
        logger.log("Test message", null);
        assertTrue(Files.exists(logFile), "Log file should be created");
    }

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(logFile)) {
            Files.delete(logFile);
        }
    }


    @Test
    void testLogWithThrowable() throws IOException {
        String message = "Error occurred";
        Exception exception = new RuntimeException("test-exception");

        logger.log(message, exception);

        String content = Files.readString(logFile);
        assertTrue(content.contains(message), "Log should contain the message");
        assertTrue(content.contains("RuntimeException"), "Log should contain exception type");
        assertTrue(content.contains("test-exception"), "Log should contain exception message");
    }

    @Test
    void testLogWithAdditionalInfo() throws IOException {
        String message = "Request failed";
        String additionalInfo = "client1";
        Exception exception = new IOException("message");

        logger.log(message, exception, additionalInfo);

        String content = Files.readString(logFile);
        assertTrue(content.contains(message), "Log should contain the message");
        assertTrue(content.contains("Additional Info: " + additionalInfo),
            "Log should contain additional info");
        assertTrue(content.contains("IOException"), "Log should contain exception type");
        assertTrue(content.contains("message"), "Log should contain exception message");
    }

    @Test
    void testMultipleLogsAppend() throws IOException {
        logger.log("First message", null);
        logger.log("Second message", null);
        logger.log("Third message", null);

        List<String> lines = Files.readAllLines(logFile);
        String content = String.join("\n", lines);

        assertTrue(content.contains("First message"), "Log should contain first message");
        assertTrue(content.contains("Second message"), "Log should contain second message");
        assertTrue(content.contains("Third message"), "Log should contain third message");
    }

    @Test
    void testLogWithStackTrace() throws IOException {
        String message = "Stack trace test";
        Exception exception = new IllegalArgumentException("Invalid argument");

        logger.log(message, exception);

        String content = Files.readString(logFile);
        assertTrue(content.contains("IllegalArgumentException"), "Log should contain exception class");
        assertTrue(content.contains("Invalid argument"), "Log should contain exception message");
    }

    @Test
    void testGetInstanceReturnsSameInstance() {
        Logger instance1 = Logger.getInstance();
        Logger instance2 = Logger.getInstance();
        assertSame(instance1, instance2, "getInstance should return the same instance");
    }


}