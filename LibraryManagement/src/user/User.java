package user;

import java.io.Serializable;

public class User implements Serializable {
    private int id;
    private String username;
    private char[] password;
    private String surname;
    private String givenName;
    private String birthday;
    private String citizenID;
    private String phone;
    private String address;
    private boolean gender;                    // Nam: true; Nữ: false
    private int role;
    private boolean status;                    // actived: true; block: false

    public User() {
    }

    public User(int id) {
        this.id = id;
    }

    public User(int id, String username, char[] password, String surname, String givenName, String birthday,
                String citizenID, String phone, String address, boolean gender, int role, boolean status) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.surname = surname;
        this.givenName = givenName;
        this.birthday = birthday;
        this.citizenID = citizenID;
        this.phone = phone;
        this.address = address;
        this.gender = gender;
        this.role = role;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public char[] getPassword() {
        return password;
    }

    public void setPassword(char[] password) {
        this.password = password;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getCitizenID() {
        return citizenID;
    }

    public void setCitizenID(String citizenID) {
        this.citizenID = citizenID;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean getGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    @Override
    public String toString(){
        return String.format("ID: %d | Username: %s | Password: %s | Họ tên: %s %s\n"
                + "Ngày sinh: %s | CCCD: %s | ĐT: %s\n"
                + "Địa chỉ: %s | Giới tính: %s | Role: %s | Tình trạng %s\n",
                id, username, new String(password), surname, givenName,
                birthday, citizenID, phone, address, (gender ? "nam" : "nữ"),
                roleToStr(role),(status ? "kích hoạt" : "khóa"));
    }
    private String roleToStr(int role){
        return switch (role){
            case UserService.ROLE_ADMIN -> "Admin \uD83D\uDEE1\uFE0F";
            case UserService.ROLE_MANAGER -> "Manager \uD83D\uDD11";
            default -> "User \uD83D\uDC64";
        };
    }
}

