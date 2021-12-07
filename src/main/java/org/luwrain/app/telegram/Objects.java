//
// Copyright 2020 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram;

import java.util.*;
import java.util.concurrent.*;

import org.drinkless.tdlib.*;
import org.drinkless.tdlib.TdApi.*;
//import org.drinkless.tdlib.TdApi.User;
//import org.drinkless.tdlib.TdApi.Chat;
//import org.drinkless.tdlib.TdApi.File;

import org.luwrain.core.*;

final class Objects
{
    interface ChatsListener { void onChatsUpdate(Chat chat); }
    interface UsersListener { void onUsersUpdate(User user); }
    interface FilesListener { void onFilesUpdate(File file); }
    interface NewMessageListener { void onNewMessage(Chat chat, Message message); }

    private final App app;

    final Map<Long, User> users = new ConcurrentHashMap<>();

    final Map<Integer, File> files = new ConcurrentHashMap<>();
    final Map<Long, Chat> chats = new ConcurrentHashMap<>();
    final NavigableSet<OrderedChat> mainChats = new TreeSet<>();
    final Map<Long, TdApi.BasicGroup> basicGroups = new ConcurrentHashMap<>();
    final Map<Integer, TdApi.Supergroup> supergroups = new ConcurrentHashMap<>();
    final Map<Integer, TdApi.SecretChat> secretChats = new ConcurrentHashMap<>();
    private long[] contacts = new long[0];
    boolean haveFullMainChatList = false;

    final List<ChatsListener> chatsListeners = new ArrayList<>();
    final List<UsersListener> usersListeners = new ArrayList<>();
    final List<FilesListener> filesListeners = new ArrayList<>();
    final List<NewMessageListener> newMessageListeners = new ArrayList<>();

    Objects(App app)
    {
	NullCheck.notNull(app, "app");
	this.app = app;
    }

    void chatsUpdated(Chat chat)
    {
	for(ChatsListener l: chatsListeners)
	    app.getLuwrain().runUiSafely(()->l.onChatsUpdate(chat));
    }

    void usersUpdated(User user)
    {
	for(UsersListener l: usersListeners)
	    app.getLuwrain().runUiSafely(()->l.onUsersUpdate(user));
    }

    void filesUpdated(File file)
    {
	for(FilesListener l: filesListeners)
	    app.getLuwrain().runUiSafely(()->l.onFilesUpdate(file));
    }

    void newMessage(Chat chat, Message message)
    {
	NullCheck.notNull(chat, "chat");
	NullCheck.notNull(message, "message");
	for(NewMessageListener l: newMessageListeners)
	    app.getLuwrain().runUiSafely(()->l.onNewMessage(chat, message));
    }

    synchronized void setContacts(long[] contacts)
    {
	NullCheck.notNull(contacts, "contacts");
	this.contacts = contacts.clone();
    }

    synchronized long[] getContacts()
    {
	return this.contacts.clone();
    }
}
