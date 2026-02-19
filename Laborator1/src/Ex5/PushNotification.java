package Ex5;

public class PushNotification extends Notification implements Notifiable {
    @Override
    public void sendNotification() {
        System.out.println("Sending Push Notification: " + getMessage());
    }
}
