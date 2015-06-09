
package net.rimoto.core.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class SCEService {

    @Expose
    private int id;

    @Expose
    private String name;

    @Expose
    private String slug;

    @Expose
    private String description;

    @SerializedName("android_bundle_id")
    @Expose
    private String androidBundleId;

    @Expose
    private List<ServiceTag> tags = new ArrayList<>();

    /**
     * 
     * @return The id
     */
    public int getId() {
        return id;
    }

    /**
     * 
     * @param id The id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * 
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * 
     * @param name The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     * @return The slug
     */
    @SuppressWarnings("unused")
    public String getSlug() {
        return slug;
    }

    /**
     * 
     * @param slug The slug
     */
    @SuppressWarnings("unused")
    public void setSlug(String slug) {
        this.slug = slug;
    }

    /**
     *
     * @return The description
     */
    @SuppressWarnings("unused")
    public String getDescription() {
        return description;
    }

    /**
     *
     * @param description The description
     */
    @SuppressWarnings("unused")
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *
     * @return The tags
     */
    @SuppressWarnings("unused")
    public List<ServiceTag> getTags() {
        return tags;
    }

    /**
     *
     * @param tags The tags
     */
    @SuppressWarnings("unused")
    public void setTags(List<ServiceTag> tags) {
        this.tags = tags;
    }

    /**
     * Check if equals
     * @param o Object to compare
     * @return boolean
     */
    @Override
    public boolean equals(Object o) {
        return (o instanceof SCEService) && (((SCEService) o).getId() == this.getId());
    }

    /**
     * Get android bundle id
     * @return String
     */
    @SuppressWarnings("unused")
    public String getAndroidBundleId() {
        return androidBundleId;
    }

    /**
     * Set android bundle id
     * @param androidBundleId String
     */
    @SuppressWarnings("unused")
    public void setAndroidBundleId(String androidBundleId) {
        this.androidBundleId = androidBundleId;
    }
}