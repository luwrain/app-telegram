//
// Copyright 2020-2022 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram;

import java.util.*;
import java.util.concurrent.*;

import org.drinkless.tdlib.*;
import org.drinkless.tdlib.TdApi.*;

import org.luwrain.core.*;

final class Objects
{
    interface ChatsListener { void onChatsUpdate(Chat chat); }
    interface UsersListener { void onUsersUpdate(User user); }
    interface FilesListener { void onFilesUpdate(File file); }
    interface NewMessageListener { void onNewMessage(Chat chat, Message message); }
    interface NewInputWaiterListener{ void onNewInputWaiter(UpdatesHandler.InputWaiter inputWaiter); }
        interface ErrorListener{ void onError(TdApi.Error error); }

    private final Luwrain luwrain;

    final Map<Long, User> users = new ConcurrentHashMap<>();
    final Map<Integer, File> files = new ConcurrentHashMap<>();
    final Map<Long, Chat> chats = new ConcurrentHashMap<>();
    final NavigableSet<OrderedChat> mainChats = new TreeSet<>();
    final Map<Long, BasicGroup> basicGroups = new ConcurrentHashMap<>();
    final Map<Long, Supergroup> supergroups = new ConcurrentHashMap<>();
        final Map<Long, SupergroupFullInfo> supergroupsFullInfo = new ConcurrentHashMap<>();
    final Map<Integer, SecretChat> secretChats = new ConcurrentHashMap<>();

    private long[] contacts = new long[0];
    boolean haveFullMainChatList = false;

    final List<ChatsListener> chatsListeners = new ArrayList<>();
    final List<UsersListener> usersListeners = new ArrayList<>();
    final List<FilesListener> filesListeners = new ArrayList<>();
    final List<NewMessageListener> newMessageListeners = new ArrayList<>();
	    final List<NewInputWaiterListener> newInputWaiterListeners = new ArrayList<>();
    final List<ErrorListener> errorListeners = new ArrayList<>();

    Objects(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	this.luwrain = luwrain;
    }

    void clearListeners()
    {
	chatsListeners.clear();
	usersListeners.clear();
	filesListeners.clear();
	newMessageListeners.clear();
	newInputWaiterListeners.clear();
	errorListeners.clear();
    }

    void chatsUpdated(Chat chat)
    {
	for(ChatsListener l: chatsListeners)
	    luwrain.runUiSafely(()->l.onChatsUpdate(chat));
    }

    void usersUpdated(User user)
    {
	for(UsersListener l: usersListeners)
	    luwrain.runUiSafely(()->l.onUsersUpdate(user));
    }

    void filesUpdated(File file)
    {
	for(FilesListener l: filesListeners)
	    luwrain.runUiSafely(()->l.onFilesUpdate(file));
    }

    void newMessage(Chat chat, Message message)
    {
	NullCheck.notNull(chat, "chat");
	NullCheck.notNull(message, "message");
	for(NewMessageListener l: newMessageListeners)
	    luwrain.runUiSafely(()->l.onNewMessage(chat, message));
    }

        void newInputWaiter(UpdatesHandler.InputWaiter inputWaiter)
    {
	NullCheck.notNull(inputWaiter, "ionputWaiter");
	for(NewInputWaiterListener l: newInputWaiterListeners)
	    luwrain.runUiSafely(()->l.onNewInputWaiter(inputWaiter));
    }

            void error(TdApi.Error error)
    {
	NullCheck.notNull(error, "error");
	for(ErrorListener l: errorListeners)
	    luwrain.runUiSafely(()->l.onError(error));
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
