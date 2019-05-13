package mohamed14riad.friendchat.models;

public class Profile {
    private String uid;
    private String name;
    private String email;
    private String photo;
    private boolean status;
    private boolean favorite;

    public Profile() {

    }

    public Profile(String uid, String name, String email, String photo, boolean status, boolean favorite) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.photo = photo;
        this.status = status;
        this.favorite = favorite;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public boolean getFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
