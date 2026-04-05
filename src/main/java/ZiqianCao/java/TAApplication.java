package ZiqianCao.java;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.SQLOutput;

public class TAApplication {
    private String name;
    @JsonProperty("taid")
    private String TAId;
    private String major;
    private String phone;
    private String email;
    private String availableTime;
    private String skill;
    private String password;

    public TAApplication(){};

    public TAApplication(String name, String TAId, String major, String phone, String email, String availableTime, String skill){
        this.name = name;
        this.TAId = TAId;
        this.major = major;
        this.phone = phone;
        this.email = email;
        this.availableTime = availableTime;
        this.skill = skill;
        this.password = "123456";
    }

    public TAApplication(String name, String TAId, String major, String phone, String email, String availableTime, String skill, String password){
        this.name = name;
        this.TAId = TAId;
        this.major = major;
        this.phone = phone;
        this.email = email;
        this.availableTime = availableTime;
        this.skill = skill;
        this.password = password;
    }
    public String getName(){
        return name;
    }
    public String getTAId(){
        return TAId;
    }
    public String getMajor(){
        return major;
    }
    public String getPhone(){
        return phone;
    }
    public String getEmail(){
        return email;
    }
    public String getAvailableTime(){
        return availableTime;
    }
    public String getSkill(){
        return skill;
    }
    public void setName(String name){
        this.name = name;
    }
    public void setTAId(String TAId){
        this.TAId = TAId;
    }
    public void setMajor(String major){
        this.major = major;
    }
    public void setPhone(String phone){
        this.phone = phone;
    }
    public void setEmail(String email){
        this.email = email;
    }
    public void setAvailableTime(String availableTime){
        this.availableTime = availableTime;
    }
    public void setSkill(String skill){
        this.skill = skill;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
