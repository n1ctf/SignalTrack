package geocode;

import java.util.List;
import java.util.Objects;

public class GeocodeResponse {

	private Status status;
    private List<Result> results;
    
    public enum Status {
        OK, 
        ZERO_RESULTS, 
        OVER_QUERY_LIMIT, 
        REQUEST_DENIED, 
        INVALID_REQUEST;
    }

    public static class Result {

        public enum Type {
            street_address,
            route,
            intersection,
            political,
            country,
            administrative_area_level_1,
            administrative_area_level_2,
            administrative_area_level_3,
            colloquial_area,
            locality,
            sublocality,
            neighborhood,
            premise,
            subpremise,
            postal_code,
            natural_feature,
            airport,
            park,
            point_of_interest,
            post_box,
            street_number,
            floor,
            room;
        }

        public static class AddressComponent {

            private String longName;
            private String shortName;
            private Type[] types;

            public String getLongName() {
                return longName;
            }

            public void setLongName(String longName) {
                this.longName = longName;
            }

            public String getShortName() {
                return shortName;
            }

            public void setShortName(String shortName) {
                this.shortName = shortName;
            }

            public Type[] getTypes() {
                return types;
            }

            public void setTypes(Type[] types) {
                this.types = types;
            }
        }

        private String formattedAddress;
        private List<AddressComponent> addressComponents;
        private Geometry geometry;
        private Type[] types;

        public Type[] getTypes() {
            return types;
        }

        public void setTypes(Type[] types) {
            this.types = types;
        }

        public String getFormattedAddress() {
            return formattedAddress;
        }

        public void setFormattedAddress(String formattedAddress) {
            this.formattedAddress = formattedAddress;
        }

        public List<AddressComponent> getAddressComponents() {
            return addressComponents;
        }

        public void setAddressComponents(List<AddressComponent> addressComponents) {
            this.addressComponents = addressComponents;
        }

        public Geometry getGeometry() {
            return geometry;
        }

        public void setGeometry(Geometry geometry) {
            this.geometry = geometry;
        }

    }

    public static class Geometry {
        public enum LocationType {
            ROOFTOP, RANGE_INTERPOLATED, GEOMETRIC_CENTER, APPROXIMATE;
        }

        public static class ViewPort {
            private Location northeast;
            private Location southwest;

            public Location getNortheast() {
                return northeast;
            }

            public void setNortheast(Location northeast) {
                this.northeast = northeast;
            }

            public Location getSouthwest() {
                return southwest;
            }

            public void setSouthwest(Location southwest) {
                this.southwest = southwest;
            }
        }

        private Location location;
        private LocationType locationType;
        private ViewPort viewport;

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }

        public LocationType getLocation_type() {
            return locationType;
        }

        public void setLocationType(LocationType locationType) {
            this.locationType = locationType;
        }

        public ViewPort getViewport() {
            return viewport;
        }

        public void setViewport(ViewPort viewport) {
            this.viewport = viewport;
        }

    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

	@Override
	public String toString() {
		return "GeocodeResponse [status=" + status + ", results=" + results + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(results, status);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final GeocodeResponse other = (GeocodeResponse) obj;
		return Objects.equals(results, other.results) && status == other.status;
	}
    
    

}