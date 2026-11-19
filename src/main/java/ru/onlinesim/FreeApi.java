package ru.onlinesim;

/** Public free numbers: {@code client.free()}. No key needed. */
public final class FreeApi {
    private final Requester request;

    FreeApi(Requester request) {
        this.request = request;
    }

    /** @return countries, the numbers of the default country and the SMS of its first number */
    public FreeList list() {
        return list(FreeListOptions.defaults());
    }

    /**
     * Countries, the numbers of the selected country and the SMS of the selected number, in one call.
     *
     * @param options country, number, page, count; unset ones are not sent
     * @return the list
     */
    public FreeList list(FreeListOptions options) {
        return Json.convert(request.get("getFreeList", options.query()), FreeList.class);
    }
}
