package ru.onlinesim;

/** Options of {@link FreeApi#list(FreeListOptions)}. Unset options are not sent. Immutable: every setter returns a copy. */
public final class FreeListOptions {
    private static final FreeListOptions NONE = new FreeListOptions(null, null, null, null);

    private final Integer country;
    private final String number;
    private final Integer page;
    private final Integer count;

    private FreeListOptions(Integer country, String number, Integer page, Integer count) {
        this.country = country;
        this.number = number;
        this.page = page;
        this.count = count;
    }

    /** @return no options set (server defaults) */
    public static FreeListOptions defaults() {
        return NONE;
    }

    /**
     * @param country country code; server default 44
     * @return a copy with the option set
     */
    public FreeListOptions country(int country) {
        return new FreeListOptions(country, number, page, count);
    }

    /**
     * @param number number without the country code, as the keys of {@link FreeList#numbers()}
     * @return a copy with the option set
     */
    public FreeListOptions number(String number) {
        return new FreeListOptions(country, number, page, count);
    }

    /**
     * @param page page of {@link FreeList#messages()}
     * @return a copy with the option set
     */
    public FreeListOptions page(int page) {
        return new FreeListOptions(country, number, page, count);
    }

    /**
     * @param count SMS per page, 10–500
     * @return a copy with the option set
     */
    public FreeListOptions count(int count) {
        return new FreeListOptions(country, number, page, count);
    }

    Object[] query() {
        return new Object[] {"country", country, "number", number, "page", page, "count", count};
    }
}
