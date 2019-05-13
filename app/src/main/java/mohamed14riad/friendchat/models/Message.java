package mohamed14riad.friendchat.models;

public class Message {
    private String id;
    private String senderUid;
    private String receiverUid;
    private String senderImage;
    private String text;
    private String imageUrl;
    private String type;
    private long timeStamp;

    public Message() {

    }

    public Message(String senderUid, String receiverUid, String senderImage, String text, String imageUrl, String type, long timeStamp) {
        this.senderUid = senderUid;
        this.receiverUid = receiverUid;
        this.senderImage = senderImage;
        this.text = text;
        this.imageUrl = imageUrl;
        this.type = type;
        this.timeStamp = timeStamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public void setSenderUid(String senderUid) {
        this.senderUid = senderUid;
    }

    public String getReceiverUid() {
        return receiverUid;
    }

    public void setReceiverUid(String receiverUid) {
        this.receiverUid = receiverUid;
    }

    public String getSenderImage() {
        return senderImage;
    }

    public void setSenderImage(String senderImage) {
        this.senderImage = senderImage;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
