package com.stackbase.mobapp.objects;

public class Thumbnail extends JSONObj {
    private String pictureName = "";
    private String description = "";

    public String getPictureName() {
        return pictureName;
    }

    public void setPictureName(String pictureName) {
        this.pictureName = pictureName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Thumbnail thumbnail = (Thumbnail) o;

        if (!description.equals(thumbnail.description)) return false;
        if (!pictureName.equals(thumbnail.pictureName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = pictureName.hashCode();
        result = 31 * result + description.hashCode();
        return result;
    }
}
