package xyz.teamcatalyst.breedr.data;

import com.squareup.moshi.Json;

import java.util.List;

/**
 * @author A-Ar Andrew Concepcion
 * @createdOn 24/07/2017
 */
public class POI {
    private Geometry geometry;
    private String name;
    private float rating;
    private List<Photo> photos;
    @Json(name = "formatted_address") private String formattedAddress;

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }
}
