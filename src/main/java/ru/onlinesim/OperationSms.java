package ru.onlinesim;

/** One SMS of an operation, from {@link Operation#msgList()}. */
public final class OperationSms extends Model {
    private String service;
    private String msg;
    private String text;
    private String code;

    private OperationSms() {}

    public String service() {
        return service;
    }

    public String msg() {
        return msg;
    }

    public String text() {
        return text;
    }

    public String code() {
        return code;
    }
}
