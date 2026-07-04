package reader;

import java.io.Serializable;

public class Reader implements Serializable {
    private String id;                 // Duy nhất
    private char[] password;
    private int type;                   // 1: Sinh viên; 2: Giáo viên; 3: Khác
    private String surname;
    private String givenName;
    private String birthday;
    private String citizenId;
    private String phone;
    private String address;
    private String email;
    private boolean gender;                 // true: nam; nữ: false
    private String createdDate;
    private String expiryDate;
    private boolean isBlock;                // Mặc định khi lập thẻ mới sẽ là false (Hoạt động bình thường)

    public Reader() {
    }

    public Reader(String id, char[] password, int type, String surname, String givenName, String birthday,
                  String citizenId, String phone, String address, String email, boolean gender, String createdDate,
                  String expiryDate, boolean isBlock) {
        this.id = id;
        this.password = password;
        this.type = type;
        this.surname = surname;
        this.givenName = givenName;
        this.birthday = birthday;
        this.citizenId = citizenId;
        this.phone = phone;
        this.address = address;
        this.email = email;
        this.gender = gender;
        this.createdDate = createdDate;
        this.expiryDate = expiryDate;
        this.isBlock = isBlock;
    }

    public String getId() { return id;    }

    public void setId(String id) { this.id = id;    }

    public char[] getPassword() { return password;    }

    public void setPassword(char[] password) { this.password = password;    }

    public int getType() { return type;    }

    public void setType(int type) { this.type = type;    }

    public String getSurname() { return surname;    }

    public void setSurname(String surname) { this.surname = surname;    }

    public String getGivenName() { return givenName;    }

    public void setGivenName(String givenName) { this.givenName = givenName;    }

    public String getBirthday() { return birthday;    }

    public void setBirthday(String birthday) { this.birthday = birthday;    }

    public String getCitizenId() { return citizenId;    }

    public void setCitizenId(String citizenId) { this.citizenId = citizenId;    }

    public String getPhone() { return phone;    }

    public void setPhone(String phone) { this.phone = phone;    }

    public String getAddress() { return address;    }

    public void setAddress(String address) { this.address = address;    }

    public String getEmail() { return email;    }

    public void setEmail(String email) { this.email = email;    }

    public boolean getGender() { return gender;    }

    public void setGender(boolean gender) { this.gender = gender;    }

    public String getCreatedDate() { return createdDate;    }

    public void setCreatedDate(String createdDate) { this.createdDate = createdDate;    }

    public String getExpiryDate() { return expiryDate;    }

    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate;    }

    public boolean isBlock() { return isBlock;    }

    public void setBlock(boolean block) { isBlock = block;    }

    @Override
    public String toString(){
        return String.format("ID: %s | Password: %s | Họ tên: %s %s\n"
                        + "Ngày sinh: %s | CCCD: %s | ĐT: %s\n"
                        + "Địa chỉ: %s | Email: %s | Giới tính: %s | Ngày lập thẻ: %s | Ngày hết hạn: %s\n"
                        + "Tình trạng thẻ: %s\n",
                id, new String(password), surname, givenName, birthday, citizenId, phone, address,
                email, (gender ? "nam" : "nữ"), createdDate, expiryDate, (isBlock ? "Thẻ bị Khóa" : "Thẻ không bị Khóa"));
    }
}
