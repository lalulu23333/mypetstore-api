package org.csu.mypetstore.api.vo;

import lombok.Data;

@Data
public class AccountVO {

    // account 表字段
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private String status;
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String zip;
    private String country;
    private String phone;

    // profile 表字段
    private String favouriteCategoryId;
    private String languagePreference;
    private boolean listOption;
    private boolean bannerOption;

    // bannerdata 表字段
    private String bannerName;

    public AccountVO(String username, String password) {
        this.username = username;
        this.password = password;
        email = "";
        firstName = "";
        lastName = "";
        status = "";
        address1 = "";
        address2 = "";
        city = "";
        state = "";
        zip = "";
        country = "";
        phone = "";
        favouriteCategoryId = "DOGS";
        languagePreference = "";
        listOption = false;
        bannerOption = false;
        bannerName = "";
    }

    public AccountVO(){}
}
