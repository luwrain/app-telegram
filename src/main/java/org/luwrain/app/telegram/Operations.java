//
// Copyright Aliaksei Levin (levlam@telegram.org), Arseny Smirnov (arseny30@gmail.com) 2014-2020
// Copyright 2020 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram;

import java.util.*;
import java.util.function.Consumer;
import java.io.*;

import org.drinkless.tdlib.*;
import org.drinkless.tdlib.TdApi.Chat;
import org.drinkless.tdlib.TdApi.User;
import org.drinkless.tdlib.TdApi.Contact;

import org.luwrain.core.*;
import org.luwrain.core.Log;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;
import org.luwrain.template.*;

abstract class Operations
{
    static private final String LOG_COMPONENT = App.LOG_COMPONENT;

    interface ChatHistoryCallback
    {
	void onChatHistoryMessages(TdApi.Chat chat, TdApi.Messages messages);
    }

    private final App app;
    private final Objects objects;

    Operations(App app)
    {
	NullCheck.notNull(app, "app");
	this.app = app;
	this.objects = app.getObjects();
    }

    abstract Client getClient();

    void addContact(String phone, String firstName, String lastName, Runnable onSuccess)
    {
	NullCheck.notEmpty(phone, "phone");
	NullCheck.notEmpty(firstName, "firstName");
	NullCheck.notEmpty(lastName, "lastName");
	NullCheck.notNull(onSuccess, "onSuccess");
	final TdApi.Contact contact = new TdApi.Contact(phone, firstName, lastName, "", 0);
	getClient().send(new TdApi.ImportContacts(new TdApi.Contact[]{contact}),
			 new DefaultHandler(TdApi.ImportedContacts.CONSTRUCTOR, (obj)->{
				 app.getLuwrain().runUiSafely(onSuccess);
			 }));
    }

    void sendMessage(TdApi.Chat chat, String text, Runnable onSuccess)
    {
	NullCheck.notNull(chat, "chat");
	NullCheck.notEmpty(text, "text");
	NullCheck.notNull(onSuccess, "onSuccess");
	TdApi.InlineKeyboardButton[] row = {new TdApi.InlineKeyboardButton("https://telegram.org?1", new TdApi.InlineKeyboardButtonTypeUrl()), new TdApi.InlineKeyboardButton("https://telegram.org?2", new TdApi.InlineKeyboardButtonTypeUrl()), new TdApi.InlineKeyboardButton("https://telegram.org?3", new TdApi.InlineKeyboardButtonTypeUrl())};
        TdApi.ReplyMarkup replyMarkup = new TdApi.ReplyMarkupInlineKeyboard(new TdApi.InlineKeyboardButton[][]{row, row, row});
	final TdApi.InputMessageContent content = new TdApi.InputMessageText(new TdApi.FormattedText(text, null), false, true);
	getClient().send(new TdApi.SendMessage(chat.id, 0, null, replyMarkup, content),
			 new DefaultHandler(TdApi.Message.CONSTRUCTOR, (obj)->{
				 app.getLuwrain().runUiSafely(()->onSuccess.run());
			 }));
    }

    void getContacts(Runnable onSuccess)
    {
	NullCheck.notNull(onSuccess, "onSuccess");
	getClient().send(new TdApi.GetContacts(),
			 new DefaultHandler(TdApi.Users.CONSTRUCTOR, (obj)->{
				 final TdApi.Users users = (TdApi.Users)obj;
				 objects.setContacts(users.userIds);
				 app.getLuwrain().runUiSafely(()->onSuccess.run());
			 }));
    }

    void openChat(Chat chat, Runnable onSuccess)
    {
	NullCheck.notNull(chat, "chat");
	NullCheck.notNull(onSuccess, "onSuccess");
	getClient().send(new TdApi.OpenChat(chat.id),
			 new DefaultHandler(TdApi.Ok.CONSTRUCTOR, (obj)->{
				 app.getLuwrain().runUiSafely(onSuccess);
			 }));
    }

    void createPrivateChat(int userId, Runnable onSuccess)
    {
	NullCheck.notNull(onSuccess, "onSuccess");
	getClient().send(new TdApi.CreatePrivateChat(userId, false),
			 new DefaultHandler(TdApi.Chat.CONSTRUCTOR, (obj)->{
				 app.getLuwrain().runUiSafely(onSuccess);
			 }));
    }

        void downloadFile(TdApi.File file)
    {
	NullCheck.notNull(file, "file");
	getClient().send(new TdApi.DownloadFile(file.id, 1, 0, 0, false),
			 new DefaultHandler(TdApi.File.CONSTRUCTOR, (obj)->{
			 }));
    }


    void getChatHistory(TdApi.Chat chat, ChatHistoryCallback callback)
    {
	NullCheck.notNull(chat, "chat");
	NullCheck.notNull(callback, "callback");
	getClient().send(new TdApi.GetChatHistory(chat.id, chat.lastMessage != null?chat.lastMessage.id:0, 0, 100, false),
			 new DefaultHandler(TdApi.Messages.CONSTRUCTOR, (obj)->{
				 app.getLuwrain().runUiSafely(()->callback.onChatHistoryMessages(chat, (TdApi.Messages)obj));
			 }));
    }

    void fillMainChatList(int limit)
    {
        synchronized (objects) {
            if (objects.haveFullMainChatList || limit <= objects.mainChats.size())
		return;
	    long offsetOrder = Long.MAX_VALUE;
	    long offsetChatId = 0;
	    if (!objects.mainChats.isEmpty())
	    {
		final OrderedChat last = objects.mainChats.last();
		offsetOrder = last.order;
		offsetChatId = last.chatId;
	    }
	    getClient().send(new TdApi.GetChats(new TdApi.ChatListMain(), offsetOrder, offsetChatId, limit - objects.mainChats.size()),
			     new DefaultHandler(TdApi.Chats.CONSTRUCTOR, (object)->{
				     final long[] chatIds = ((TdApi.Chats) object).chatIds;
				     if (chatIds.length == 0)
					 synchronized(objects){
					     objects.haveFullMainChatList = true;
					 }
				     fillMainChatList(limit);
			     }));
	}
    }

private final class DefaultHandler implements Client.ResultHandler
{
    private final int constructor;
    private final Consumer onSuccess;
    DefaultHandler(int constructor, Consumer onSuccess)
    {
	this.constructor = constructor;
	this.onSuccess = onSuccess;
    }
		@Override public void onResult(TdApi.Object object)
    {
	if (object == null)
	    return;
	if (object.getConstructor() == constructor)
	{
	    onSuccess.accept(object);
	    return;
	}
	if (object.getConstructor() == TdApi.Error.CONSTRUCTOR)
	{
	    Log.error(LOG_COMPONENT, "Receive an error for " + String.valueOf(constructor) + ":" + object.toString());
			return;
	}
	Log.error(LOG_COMPONENT, "Receive wrong response for " + String.valueOf(constructor) + ":" + object.toString());
		    }
}
}
