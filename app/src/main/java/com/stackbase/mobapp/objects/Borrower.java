package com.stackbase.mobapp.objects;

import java.util.Date;

public class Borrower extends JSONObj {
    private String id = "";
    private String name = "";
    private String gender = "";
    private String nation = "";
    private Date birthday = null;
    private String address = "";
    private String location = "";
    private Date expiryFrom = null;
    private Date expiryTo = null;
    private String idPicture1 = "";
    private String idPicture2 = "";
    private String jsonFile = "";

    public Borrower() {
        super();
    }

    public Borrower(String jsonFile) {
        this.fromJSON(jsonFile);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getNation() {
        return nation;
    }

    public void setNation(String nation) {
        this.nation = nation;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String issue) {
        this.location = issue;
    }

    public Date getExpiryFrom() {
        return expiryFrom;
    }

    public void setExpiryFrom(Date validityDateFrom) {
        this.expiryFrom = validityDateFrom;
    }

    public Date getExpiryTo() {
        return expiryTo;
    }

    public void setExpiryTo(Date validityDateTo) {
        this.expiryTo = validityDateTo;
    }

    public String getIdPicture1() {
        return idPicture1;
    }

    public void setIdPicture1(String idPicture1) {
        this.idPicture1 = idPicture1;
    }

    public String getIdPicture2() {
        return idPicture2;
    }

    public void setIdPicture2(String idPicture2) {
        this.idPicture2 = idPicture2;
    }

    public String getJsonFile() {
        return jsonFile;
    }

    public void setJsonFile(String jsonFile) {
        this.jsonFile = jsonFile;
    }
}
