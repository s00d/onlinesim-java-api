package ru.onlinesim;

import java.util.List;
import java.util.Map;

/** Result of {@link FreeApi#list(FreeListOptions)}. */
public final class FreeList extends Model {
    private List<Country> countries;
    private Map<String, Phone> numbers;
    private Messages messages;

    private FreeList() {}

    public List<Country> countries() {
        return Json.list(countries);
    }

    /** @return numbers of the selected country, keyed by the number without the country code */
    public Map<String, Phone> numbers() {
        return Json.map(numbers);
    }

    /** @return SMS of the selected number */
    public Messages messages() {
        return messages;
    }

    /** A country with free numbers. */
    public static final class Country extends Model {
        private int country;
        private String countryText;
        private String countryOriginal;

        private Country() {}

        public int country() {
            return country;
        }

        /** @return localised country name, or {@code null} */
        public String countryText() {
            return countryText;
        }

        /** @return country slug, or {@code null} */
        public String countryOriginal() {
            return countryOriginal;
        }
    }

    /** A free number. */
    public static final class Phone extends Model {
        private int country;
        private String countryOriginal;
        private String fullNumber;
        private String dataHumans;
        private boolean isArchive;

        private Phone() {}

        public int country() {
            return country;
        }

        /** @return country slug, or {@code null} */
        public String countryOriginal() {
            return countryOriginal;
        }

        /** @return number with the country code, without {@code +} */
        public String fullNumber() {
            return fullNumber;
        }

        public String dataHumans() {
            return dataHumans;
        }

        public boolean isArchive() {
            return isArchive;
        }
    }

    /** An SMS of a free number. */
    public static final class Message extends Model {
        private long id;
        private String text;
        private String inNumber;
        private String myNumber;
        private String code;
        private String createdAt;
        private String dataHumans;

        private Message() {}

        public long id() {
            return id;
        }

        public String text() {
            return text;
        }

        /** @return sender */
        public String inNumber() {
            return inNumber;
        }

        public String myNumber() {
            return myNumber;
        }

        /** @return the code, {@code -} when none was found */
        public String code() {
            return code;
        }

        public String createdAt() {
            return createdAt;
        }

        public String dataHumans() {
            return dataHumans;
        }
    }

    /** Paginated SMS plus the selected number and country. */
    public static final class Messages extends Paginator<Message> {
        private String number;
        private int country;

        private Messages() {}

        /** @return the selected number (short form), or {@code null} */
        public String number() {
            return number;
        }

        public int country() {
            return country;
        }
    }
}
