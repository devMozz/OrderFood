package com.example.milymozz.orderfood.Model;

/**
 * Created by milymozz on 2018. 2. 7..
 */

public class User {
    private String name;
    private String password;
    private String phone;
    private String isStaff;
    private String secureCode;
    private String homeAddress;
    private Object balance;

    public User() {
    }

    public User(String name, String pass, String secureCode) {
        this.name = name;
        this.password = pass;
        this.isStaff = "false";
        this.secureCode = secureCode;
    }

    public Object getBalance() {
        return balance;
    }

    public void setBalance(Object balance) {
        this.balance = balance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String pass) {
        this.password = pass;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getIsStaff() {
        return isStaff;
    }

    public void setIsStaff(String isStaff) {
        this.isStaff = isStaff;
    }

    public String getSecureCode() {
        return secureCode;
    }

    public void setSecureCode(String secureCode) {
        this.secureCode = secureCode;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }
}
