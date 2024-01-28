package root.entities.ci;

import com.google.gson.annotations.SerializedName;

public class FailedTest {
    /*
    {
"test_class": "xx",
"test_function": "xx",
"message": "xx",
}
     */
    @SerializedName("test_class")
    private String testClass;
    @SerializedName("test_function")
    private String testFunction;
    @SerializedName("assertion_line")
    private String lineNumber;
    @SerializedName("exception")
    private String exception;
    @SerializedName("message")
    private String message;

    public String getTestClass() {
        return testClass;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public void setTestClass(String testClass) {
        this.testClass = testClass;
    }

    public String getTestFunction() {
        return testFunction;
    }

    public void setTestFunction(String testFunction) {
        this.testFunction = testFunction;
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
