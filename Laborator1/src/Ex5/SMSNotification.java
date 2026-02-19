package Ex5;

public class SMSNotification extends Notification implements Notifiable {
    @Override
    public void sendNotification() {
        System.out.println("Sending SMS: " + getMessage());
    }
}
