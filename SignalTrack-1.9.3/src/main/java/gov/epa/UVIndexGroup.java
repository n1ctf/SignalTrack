package gov.epa;

import static time.ConsolidatedTime.fromEPAEnvirofactsStyleDateTimeGroup;
import java.time.ZonedDateTime;

public class UVIndexGroup {

        private final String repliedZipCode;
        private final int order;
        private final int uvIndex;
        private final String city;
        private final String state;
        private final String dateTimeString;

        public UVIndexGroup(String repliedZipCode, int order, int uvIndex, String city, String state, String dateTimeString) {
            this.repliedZipCode = repliedZipCode;
            this.order = order;
            this.uvIndex = uvIndex;
            this.city = city;
            this.state = state;
            this.dateTimeString = dateTimeString;
        }

        public String getCity() {
            return city;
        }

        public String getState() {
            return state;
        }

        public ZonedDateTime getDateTimeString() {
            return fromEPAEnvirofactsStyleDateTimeGroup(dateTimeString);
        }

        public String getRepliedZipCode() {
            return repliedZipCode;
        }

        public int getUvIndex() {
            return uvIndex;
        }

        public int getOrder() {
            return order;
        }
    }