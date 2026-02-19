package Ex5;

public abstract class Notification {
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void displayNotification() {
        System.out.println(message);
    }
}
