//
// Copyright Aliaksei Levin (levlam@telegram.org), Arseny Smirnov (arseny30@gmail.com) 2014-2020
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

    final ConcurrentMap<Integer, User> users = new ConcurrentHashMap();
    int[] contacts = new int[0];
        final ConcurrentMap<Long, Chat> chats = new ConcurrentHashMap();
    final NavigableSet<OrderedChat> mainChatList = new TreeSet();
    boolean haveFullMainChatList = false;
    final ConcurrentMap<Integer, TdApi.BasicGroup> basicGroups = new ConcurrentHashMap<Integer, TdApi.BasicGroup>();
    final ConcurrentMap<Integer, TdApi.Supergroup> supergroups = new ConcurrentHashMap<Integer, TdApi.Supergroup>();
    final ConcurrentMap<Integer, TdApi.SecretChat> secretChats = new ConcurrentHashMap<Integer, TdApi.SecretChat>();

    final List<ChatsListener> chatsListeners = new LinkedList();
    final List<UsersListener> usersListeners = new LinkedList();

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
}
