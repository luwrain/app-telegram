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
import org.drinkless.tdlib.TdApi.User;
import org.drinkless.tdlib.TdApi.Chat;
import org.drinkless.tdlib.TdApi.File;

import org.luwrain.core.*;

final class Objects
{
    interface ChatsListener
    {
	void onChatsUpdate(Chat chat);
    }

    interface UsersListener
    {
	void onUsersUpdate(User user);
    }

    interface FilesListener
    {
	void onFilesUpdate(File file);
    }

    final ConcurrentMap<Long, User> users = new ConcurrentHashMap();
    private long[] contacts = new long[0];
    final ConcurrentMap<Integer, File> files = new ConcurrentHashMap();
    final ConcurrentMap<Long, Chat> chats = new ConcurrentHashMap();
    final NavigableSet<OrderedChat> mainChats = new TreeSet();
    boolean haveFullMainChatList = false;
    final ConcurrentMap<Long, TdApi.BasicGroup> basicGroups = new ConcurrentHashMap<>();
    final ConcurrentMap<Integer, TdApi.Supergroup> supergroups = new ConcurrentHashMap<Integer, TdApi.Supergroup>();
    final ConcurrentMap<Integer, TdApi.SecretChat> secretChats = new ConcurrentHashMap<Integer, TdApi.SecretChat>();

    final List<ChatsListener> chatsListeners = new LinkedList();
    final List<UsersListener> usersListeners = new LinkedList();
        final List<FilesListener> filesListeners = new LinkedList();

    private final App app;

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
