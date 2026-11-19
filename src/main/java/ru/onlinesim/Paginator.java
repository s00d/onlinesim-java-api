package ru.onlinesim;

import java.util.List;

/**
 * Laravel paginator page.
 *
 * @param <T> item type
 */
public class Paginator<T> extends Model {
    private int currentPage;
    private List<T> data;
    private Integer from;
    private Integer to;
    private int lastPage;
    private int perPage;
    private int total;
    private String firstPageUrl;
    private String lastPageUrl;
    private String nextPageUrl;
    private String prevPageUrl;
    private String path;

    Paginator() {}

    public int currentPage() {
        return currentPage;
    }

    /** @return items of this page */
    public List<T> data() {
        return Json.list(data);
    }

    /** @return index of the first item, {@code null} on an empty page */
    public Integer from() {
        return from;
    }

    /** @return index of the last item, {@code null} on an empty page */
    public Integer to() {
        return to;
    }

    public int lastPage() {
        return lastPage;
    }

    public int perPage() {
        return perPage;
    }

    public int total() {
        return total;
    }

    public String firstPageUrl() {
        return firstPageUrl;
    }

    public String lastPageUrl() {
        return lastPageUrl;
    }

    public String nextPageUrl() {
        return nextPageUrl;
    }

    public String prevPageUrl() {
        return prevPageUrl;
    }

    public String path() {
        return path;
    }
}
