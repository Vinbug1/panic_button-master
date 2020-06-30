public class PollingUnitCoordinates {

    double lat;
    double lng;
    String pollingUnitName;

    public PollingUnitCoordinates() {
    }

    public PollingUnitCoordinates(double lat, double lng, String pollingUnitName) {
        this.lat = lat;
        this.lng = lng;
        this.pollingUnitName = pollingUnitName;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getPollingUnitName() {
        return pollingUnitName;
    }

    public void setPollingUnitName(String pollingUnitName) {
        this.pollingUnitName = pollingUnitName;
    }
}
