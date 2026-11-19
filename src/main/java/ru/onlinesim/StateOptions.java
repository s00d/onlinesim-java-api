package ru.onlinesim;

/**
 * Options of {@link NumbersApi#state(StateOptions)} and {@link NumbersApi#stateOne(long, StateOptions)}.
 * Immutable: every setter returns a copy.
 */
public final class StateOptions {
    /** Order of operations. */
    public enum Order {
        /** Oldest first (default). */
        ASC,
        /** Newest first. */
        DESC
    }

    private static final StateOptions DEFAULTS = new StateOptions(true, Order.ASC, false);

    private final boolean messageToCode;
    private final Order orderBy;
    private final boolean msgList;

    private StateOptions(boolean messageToCode, Order orderBy, boolean msgList) {
        this.messageToCode = messageToCode;
        this.orderBy = orderBy;
        this.msgList = msgList;
    }

    /** @return {@code messageToCode = true}, {@code orderBy = ASC}, {@code msgList = false} */
    public static StateOptions defaults() {
        return DEFAULTS;
    }

    /**
     * @param messageToCode {@code true} (default): {@code msg} is the code only; {@code false}: the full SMS text
     * @return a copy with the option set
     */
    public StateOptions messageToCode(boolean messageToCode) {
        return new StateOptions(messageToCode, orderBy, msgList);
    }

    /**
     * @param orderBy order of operations; ignored by {@code stateOne}
     * @return a copy with the option set
     */
    public StateOptions orderBy(Order orderBy) {
        return new StateOptions(messageToCode, orderBy, msgList);
    }

    /**
     * @param msgList {@code true}: {@code msg} is a list of all SMS ({@link Operation#msgList()}); default {@code false}
     * @return a copy with the option set
     */
    public StateOptions msgList(boolean msgList) {
        return new StateOptions(messageToCode, orderBy, msgList);
    }

    /** @return whether {@code msg} is the code only */
    public boolean messageToCode() {
        return messageToCode;
    }

    /** @return order of operations */
    public Order orderBy() {
        return orderBy;
    }

    /** @return whether {@code msg} is a list of all SMS */
    public boolean msgList() {
        return msgList;
    }
}
