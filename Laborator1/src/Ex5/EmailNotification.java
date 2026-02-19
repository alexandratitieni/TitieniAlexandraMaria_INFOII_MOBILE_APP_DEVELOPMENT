package Ex5;

public class EmailNotification extends Notification implements Notifiable {
    @Override
    public void sendNotification() {
        System.out.println("Sending Email: " + getMessage());
    }
}
