package xyz.teamcatalyst.breedr.chat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.OpenChannel;
import com.sendbird.android.PreviousMessageListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.UserMessage;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import xyz.teamcatalyst.breedr.R;

public class ChatActivity extends AppCompatActivity {

    String threadKey;
    DatabaseReference database;
    private OpenChannel joinedChannel;
    MessagesListAdapter messageListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat2);

        String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String othersId = getIntent().getStringExtra("EXTRA_CHAT_USER_ID");

        int compare = myId.compareTo(othersId);

        if (compare < 0) {
            //a is smaller
            threadKey = myId + othersId;
        } else if (compare > 0) {
            //a is larger
            threadKey = othersId + myId;
        } else {
            //a is equal to b
            threadKey = myId + othersId;
        }

        database = FirebaseDatabase.getInstance().getReference("chat-threads").child(threadKey);

        ImageLoader imageLoader = (imageView, url) -> {
            Glide.with(this).load(url).into(imageView);
        };

        messageListAdapter = new MessagesListAdapter<Message>(myId, imageLoader);
        MessagesList chats = findViewById(R.id.messagesList);
        chats.setAdapter(messageListAdapter);

        MessageInput chatInput = findViewById(R.id.chatInput);
        chatInput.setInputListener(input -> {
            if (joinedChannel == null) return false;

            joinedChannel.sendUserMessage(input.toString(), (userMessage, e) -> {
                messageListAdapter.addToStart(new Message(
                        UUID.randomUUID().toString(),
                        new Date(),
                        new ChatUser("", "Me", myId),
                        input.toString()
                    ), true);
            });
            return true;
        });

        SendBird.connect(FirebaseAuth.getInstance().getUid(), (user, e) -> {
            if (e != null) return;

            openChannel();
        });
    }

    private void openChannel() {
        // try to see if channel is not existing yet
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String channelUrl = (String) dataSnapshot.child("channelUrl").getValue();
                    joinChannel(channelUrl);
                } else {
                    createChannel();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void createChannel() {
        OpenChannel.createChannel((openChannel, e) -> {
            if (e != null) {
                return;
            }

            getMessages(openChannel);
        });
    }

    private void joinChannel(String channelUrl) {
        OpenChannel.getChannel(channelUrl, (openChannel, e) -> {
            if (e != null) {
                return;
            }

            getMessages(openChannel);
        });
    }

    private void getMessages(OpenChannel openChannel) {
        joinedChannel = openChannel;
        PreviousMessageListQuery prevMessageListQuery = openChannel.createPreviousMessageListQuery();
        prevMessageListQuery.load(30, true, (List<BaseMessage> messages, SendBirdException e) -> {
            if (e != null) {
                // Error.
                return;
            }

            Observable.from(messages)
                    .observeOn(AndroidSchedulers.mainThread())
                    .filter(message -> message instanceof UserMessage)
                    .map(message -> {
                        UserMessage userMessage = (UserMessage) message;
                        return new Message(
                                String.valueOf(userMessage.getMessageId()),
                                new Date(message.getCreatedAt()),
                                new ChatUser("", userMessage.getSender().getNickname(), userMessage.getSender().getUserId()),
                                userMessage.getMessage()
                        );
                    })
                    .toList()
                    .subscribe(chatMessages -> {
                        showChatMessages(chatMessages);
                    }, error -> {

                    });


        });
    }

    private void showChatMessages(List<Message> chatMessages) {
        messageListAdapter.addToEnd(chatMessages, false);

        joinedChannel.enter(e1 -> {
            Map<String, String> threadDetails = new HashMap<>();
            threadDetails.put("channelUrl", joinedChannel.getUrl());
            database.setValue(threadDetails);

            SendBird.addChannelHandler(threadKey, new SendBird.ChannelHandler() {
                @Override
                public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
                    if (baseMessage instanceof UserMessage) {
                        messageListAdapter.addToStart(new Message(
                                        String.valueOf(baseMessage.getMessageId()),
                                        new Date(baseMessage.getCreatedAt()),
                                        new ChatUser(
                                                "",
                                                ((UserMessage) baseMessage).getSender().getNickname(),
                                                ((UserMessage) baseMessage).getSender().getUserId()
                                        ),
                                        ((UserMessage) baseMessage).getMessage()),
                                true);
                    }
                }
            });
        });
    }
}
