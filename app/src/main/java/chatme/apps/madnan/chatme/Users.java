package chatme.apps.madnan.chatme;

/**
 * Created by mahmoud adnan on 10/29/2017.
 */

public class Users {

    public String username;
    public String status;
    public String image;
    public String thumb_image;

    public Users(){}

    public Users(String name, String status, String image, String thumb_image) {
        this.username = name;
        this.status = status;
        this.thumb_image = thumb_image;
        this.image = image;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
