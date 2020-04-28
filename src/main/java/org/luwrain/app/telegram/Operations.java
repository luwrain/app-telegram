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

    void importContact(String phone, String firstName, String lastName)
    {
	final TdApi.Contact contact = new TdApi.Contact(phone, firstName, lastName, "", 0);
	getClient().send(new TdApi.ImportContacts(new TdApi.Contact[]{contact}), new Client.ResultHandler() {
		@Override public void onResult(TdApi.Object object) {
		    switch (object.getConstructor())
		    {
		    case TdApi.ImportedContacts.CONSTRUCTOR:
			Log.debug(LOG_COMPONENT, "response on ImportContacts: " + object);
			return;
		    case TdApi.Error.CONSTRUCTOR:
			Log.error(LOG_COMPONENT, "Receive an error for ImportContacts: " + object);
			return;
		    default:
			Log.error(LOG_COMPONENT, "Receive wrong response from TDLib: " + object);
		    }
		}});
    }

    void sendMessage(TdApi.Chat chat, String text)
    {
	TdApi.InlineKeyboardButton[] row = {new TdApi.InlineKeyboardButton("https://telegram.org?1", new TdApi.InlineKeyboardButtonTypeUrl()), new TdApi.InlineKeyboardButton("https://telegram.org?2", new TdApi.InlineKeyboardButtonTypeUrl()), new TdApi.InlineKeyboardButton("https://telegram.org?3", new TdApi.InlineKeyboardButtonTypeUrl())};
        TdApi.ReplyMarkup replyMarkup = new TdApi.ReplyMarkupInlineKeyboard(new TdApi.InlineKeyboardButton[][]{row, row, row});
	final TdApi.InputMessageContent content = new TdApi.InputMessageText(new TdApi.FormattedText(text, null), false, true);
	getClient().send(new TdApi.SendMessage(chat.id, 0, null, replyMarkup, content), new Client.ResultHandler() {
		@Override public void onResult(TdApi.Object object) {
		    switch (object.getConstructor())
		    {
		    case TdApi.Message.CONSTRUCTOR:
			Log.debug(LOG_COMPONENT, "response on message sending: " + object);
			return;
		    case TdApi.Error.CONSTRUCTOR:
			Log.error(LOG_COMPONENT, "Receive an error for GetContacts: " + object);
			return;
		    default:
			Log.error(LOG_COMPONENT, "Receive wrong response from TDLib: " + object);
		    }
		}});
    }

    void getContacts(Runnable onSuccess)
    {
	getClient().send(new TdApi.GetContacts(),
			 new DefaultHandler(TdApi.Users.CONSTRUCTOR, (obj)->{
				 synchronized(objects){
				     final TdApi.Users users = (TdApi.Users)obj;
				     objects.contacts = users.userIds;
				 }
				 app.getLuwrain().runUiSafely(()->onSuccess.run());
			 }));
    }

    void openChat(TdApi.User user)
    {
	Log.debug(LOG_COMPONENT, "opening chat for " + user.firstName + " " + user.lastName);
	getClient().send(new TdApi.OpenChat(user.id), new Client.ResultHandler() {
		@Override public void onResult(TdApi.Object object) {
		    switch (object.getConstructor())
		    {
		    case TdApi.Ok.CONSTRUCTOR:
			Log.debug(LOG_COMPONENT, "response on open chat: " + object);
			return;
		    case TdApi.Error.CONSTRUCTOR:
			Log.error(LOG_COMPONENT, "Receive an error for open chat: " + object);
			return;
		    default:
			Log.error(LOG_COMPONENT, "Receive wrong response from TDLib: " + object);
		    }
		}});
    }

    void createPrivateChat(TdApi.User user)
    {
	Log.debug(LOG_COMPONENT, "creating private chat for " + user.firstName + " " + user.lastName);
	getClient().send(new TdApi.CreatePrivateChat(user.id, false), new Client.ResultHandler() {
		@Override public void onResult(TdApi.Object object) {
		    switch (object.getConstructor())
		    {
		    case TdApi.Chat.CONSTRUCTOR:
			Log.debug(LOG_COMPONENT, "response on CreatePrivateChat: " + object);
			return;
		    case TdApi.Error.CONSTRUCTOR:
			Log.error(LOG_COMPONENT, "Receive an error for CreatePrivateChat: " + object);
			return;
		    default:
			Log.error(LOG_COMPONENT, "Receive wrong response from TDLib: " + object);
		    }
		}});
    }

    void getChatHistory(TdApi.Chat chat, ChatHistoryCallback callback)
    {
	NullCheck.notNull(chat, "chat");
	NullCheck.notNull(callback, "callback");
	getClient().send(new TdApi.GetChatHistory(chat.id, chat.lastMessage.id, 0, 50, false), new Client.ResultHandler() {
		@Override public void onResult(TdApi.Object object) {
		    switch (object.getConstructor())
		    {
		    case TdApi.Messages.CONSTRUCTOR:
			//			    Log.debug(LOG_COMPONENT, "response on CreatePrivateChat: " + object);
			app.getLuwrain().runUiSafely(()->callback.onChatHistoryMessages(chat, (TdApi.Messages)object));
			return;
		    case TdApi.Error.CONSTRUCTOR:
			Log.error(LOG_COMPONENT, "Receive an error for CreatePrivateChat: " + object);
			return;
		    default:
			Log.error(LOG_COMPONENT, "Receive wrong response from TDLib: " + object);
		    }
		}});
    }

    void getChats(int limit)
    {
        synchronized (objects) {
            if (!objects.haveFullMainChatList && limit > objects.mainChatList.size()) {
                // have enough chats in the chat list or chat list is too small
                long offsetOrder = Long.MAX_VALUE;
		long offsetChatId = 0;
                if (!objects.mainChatList.isEmpty()) {
                    OrderedChat last = objects.mainChatList.last();
                    offsetOrder = last.order;
                    offsetChatId = last.chatId;
                }
                getClient().send(new TdApi.GetChats(new TdApi.ChatListMain(), offsetOrder, offsetChatId, limit - objects.mainChatList.size()), new Client.ResultHandler() {
			@Override public void onResult(TdApi.Object object)
			{
			    switch (object.getConstructor()) {
                            case TdApi.Error.CONSTRUCTOR:
				Log.error(LOG_COMPONENT, "Receive an error for GetChats: " + object);
                                break;
                            case TdApi.Chats.CONSTRUCTOR:
                                long[] chatIds = ((TdApi.Chats) object).chatIds;
                                if (chatIds.length == 0) {
                                    synchronized (objects) {
                                        objects.haveFullMainChatList = true;
                                    }
                                }
                                // chats had already been received through updates, let's retry request
                                getChats(limit);
                                break;
                            default:
                                Log.error(LOG_COMPONENT, "Receive wrong response from TDLib: " + object);
			    }
			}
		    });
                return;
            }
            // have enough chats in the chat list to answer request
            java.util.Iterator<OrderedChat> iter = objects.mainChatList.iterator();
            System.out.println("First " + limit + " chat(s) out of " + objects.mainChatList.size() + " known chat(s):");
            for (int i = 0; i < limit; i++)
	    {
                final long chatId = iter.next().chatId;
                final TdApi.Chat chat = objects.chats.get(chatId);
                synchronized (objects) {
                    System.out.println(chatId + ": " + chat.title);
                }
            }
	    //            print("");
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
