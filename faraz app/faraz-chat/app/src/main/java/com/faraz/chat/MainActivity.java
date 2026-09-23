package com.faraz.chat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LinearLayout loginPage, chatListPage, chatPage;
    private LinearLayout chatListContainer, messagesContainer;
    private EditText usernameInput, messageInput;
    private Button loginBtn, pickImageBtn, sendBtn;
    private ImageView avatarPreview, myProfile, chatAvatar;
    private TextView chatName, backBtn;
    private ScrollView messagesScroll;

    private String currentUser = "";
    private Bitmap userAvatar = null;
    private int currentChatId = -1;
    private static final int PICK_IMAGE = 100;

    // Chats data
    private List<Chat> chats = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find views
        loginPage = findViewById(R.id.loginPage);
        chatListPage = findViewById(R.id.chatListPage);
        chatPage = findViewById(R.id.chatPage);
        chatListContainer = findViewById(R.id.chatListContainer);
        messagesContainer = findViewById(R.id.messagesContainer);
        usernameInput = findViewById(R.id.usernameInput);
        messageInput = findViewById(R.id.messageInput);
        loginBtn = findViewById(R.id.loginBtn);
        pickImageBtn = findViewById(R.id.pickImageBtn);
        sendBtn = findViewById(R.id.sendBtn);
        avatarPreview = findViewById(R.id.avatarPreview);
        myProfile = findViewById(R.id.myProfile);
        chatAvatar = findViewById(R.id.chatAvatar);
        chatName = findViewById(R.id.chatName);
        backBtn = findViewById(R.id.backBtn);
        messagesScroll = findViewById(R.id.messagesScroll);

        // Demo chats
        chats.add(new Chat(1, "Dost 1", "👤", "Salam bhai!"));
        chats.add(new Chat(2, "Dost 2", "👤", "Kya haal hai?"));
        chats.add(new Chat(3, "Dost 3", "👤", "Mast!"));
        chats.add(new Chat(4, "Dost 4", "👤", "Theek hoon"));
        chats.add(new Chat(5, "Group Chat", "👥", "Sab log online"));

        // Pick image
        pickImageBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE);
        });

        // Login
        loginBtn.setOnClickListener(v -> {
            String name = usernameInput.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Naam daalo!", Toast.LENGTH_SHORT).show();
                return;
            }
            currentUser = name;
            
            if (userAvatar != null) {
                myProfile.setImageBitmap(userAvatar);
            }
            
            loginPage.setVisibility(View.GONE);
            chatListPage.setVisibility(View.VISIBLE);
            renderChatList();
        });

        // Back
        backBtn.setOnClickListener(v -> {
            chatPage.setVisibility(View.GONE);
            chatListPage.setVisibility(View.VISIBLE);
        });

        // Send
        sendBtn.setOnClickListener(v -> sendMessage());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri == null) {
                Toast.makeText(this, "Image select nahi hui.", Toast.LENGTH_SHORT).show();
                return;
            }

            try (InputStream inputStream = getContentResolver().openInputStream(imageUri)) {
                if (inputStream == null) {
                    Toast.makeText(this, "Image read nahi hui.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream);
                if (selectedBitmap != null) {
                    userAvatar = selectedBitmap;
                    avatarPreview.setImageBitmap(userAvatar);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Image load karte waqt masla hua.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void renderChatList() {
        chatListContainer.removeAllViews();
        for (Chat chat : chats) {
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.HORIZONTAL);
            item.setPadding(30, 30, 30, 30);
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setBackgroundColor(0xFF111B21);

            TextView avatar = new TextView(this);
            avatar.setText(chat.avatar);
            avatar.setTextSize(24);
            avatar.setGravity(Gravity.CENTER);
            avatar.setBackgroundColor(0xFF25D366);
            LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(120, 120);
            avatarParams.setMargins(0, 0, 30, 0);
            avatar.setLayoutParams(avatarParams);

            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            info.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            TextView name = new TextView(this);
            name.setText(chat.name);
            name.setTextColor(0xFFFFFFFF);
            name.setTextSize(16);
            name.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView lastMsg = new TextView(this);
            lastMsg.setText(chat.lastMsg);
            lastMsg.setTextColor(0xFF8696A0);
            lastMsg.setTextSize(13);

            info.addView(name);
            info.addView(lastMsg);

            item.addView(avatar);
            item.addView(info);

            item.setOnClickListener(v -> openChat(chat));

            chatListContainer.addView(item);
        }
    }

    private void openChat(Chat chat) {
        currentChatId = chat.id;
        chatListPage.setVisibility(View.GONE);
        chatPage.setVisibility(View.VISIBLE);
        chatName.setText(chat.name);
        chatAvatar.setImageBitmap(null);
        chatAvatar.setBackgroundColor(0xFF25D366);

        renderMessages(chat);
    }

    private void renderMessages(Chat chat) {
        messagesContainer.removeAllViews();

        if (chat.messages.isEmpty()) {
            chat.messages.add(new Message(chat.name, "Salam bhai!", false));
            chat.messages.add(new Message(currentUser, "Walaikum Salam!", true));
            chat.messages.add(new Message(chat.name, "Kya haal hai?", false));
            chat.messages.add(new Message(currentUser, "Mast! 😎", true));
        }

        for (Message msg : chat.messages) {
            TextView tv = new TextView(this);
            tv.setText(msg.text);
            tv.setTextSize(16);
            tv.setPadding(25, 25, 25, 25);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(10, 10, 10, 10);

            if (msg.own) {
                tv.setBackgroundColor(0xFF005C4B);
                tv.setTextColor(0xFFFFFFFF);
                params.gravity = Gravity.END;
            } else {
                tv.setBackgroundColor(0xFF202C33);
                tv.setTextColor(0xFFFFFFFF);
                params.gravity = Gravity.START;
            }

            tv.setLayoutParams(params);
            messagesContainer.addView(tv);
        }

        messagesScroll.post(() -> messagesScroll.fullScroll(View.FOCUS_DOWN));
    }

    private void sendMessage() {
        String text = messageInput.getText().toString().trim();
        if (text.isEmpty() || currentChatId == -1) return;

        Chat chat = null;
        for (Chat item : chats) {
            if (item.id == currentChatId) {
                chat = item;
                break;
            }
        }

        if (chat == null) {
            Toast.makeText(this, "Chat nahi mila.", Toast.LENGTH_SHORT).show();
            return;
        }

        chat.messages.add(new Message(currentUser, text, true));
        messageInput.setText("");
        renderMessages(chat);
    }

    // Inner classes
    static class Chat {
        int id;
        String name, avatar, lastMsg;
        List<Message> messages = new ArrayList<>();
        Chat(int id, String name, String avatar, String lastMsg) {
            this.id = id;
            this.name = name;
            this.avatar = avatar;
            this.lastMsg = lastMsg;
        }
    }

    static class Message {
        String user, text;
        boolean own;
        Message(String user, String text, boolean own) {
            this.user = user;
            this.text = text;
            this.own = own;
        }
    }
}