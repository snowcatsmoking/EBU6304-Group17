package LoginScreen;

public class User {
    private String account;
    private String password;
    private String role;
    private String name;

    public User() {}

    public User(String account, String password, String role) {
        this.account = account;
        this.password = password;
        this.role = role;
        this.name = "";
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
