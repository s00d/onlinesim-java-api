package ru.onlinesim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.onlinesim.Fake.map;

import org.junit.jupiter.api.Test;

class FreeTest {
    @Test
    void listHasNoPhpSuffixAndSendsOnlyTheSetParameters() {
        Fake f = Fake.client().json(Fake.fixture("getFreeList"));
        FreeList list = f.client
                .free()
                .list(FreeListOptions.defaults()
                        .country(44)
                        .number("7459608320")
                        .count(20));
        assertEquals("/api/getFreeList", f.path());
        assertEquals(map("country", "44", "number", "7459608320", "count", "20"), f.query());
        assertEquals(2, list.countries().size());
        assertEquals("united_kingdom", list.countries().get(0).countryOriginal());
        assertEquals("447459608320", list.numbers().get("7459608320").fullNumber());
        assertEquals("195895", list.messages().data().get(0).code());
        assertEquals("Amazon", list.messages().data().get(0).inNumber());
        assertEquals("7459608320", list.messages().number());
        assertEquals(44, list.messages().country());
        assertEquals(426, list.messages().total());
    }

    @Test
    void listNormalisesEmptyNumbers() {
        Fake f = Fake.client().json("{\"response\":1,\"countries\":[],\"numbers\":[],\"messages\":{\"data\":[]}}");
        FreeList list = f.client.free().list();
        assertTrue(list.numbers().isEmpty());
        assertEquals(map(), f.query());
    }
}
