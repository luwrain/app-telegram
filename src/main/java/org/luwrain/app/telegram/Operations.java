//
// Copyright 2020-2022 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram;

import java.util.*;
import java.util.function.Consumer;
import java.io.*;

import org.drinkless.tdlib.*;
import org.drinkless.tdlib.TdApi.*;
//import org.drinkless.tdlib.TdApi.Chat;
//import org.drinkless.tdlib.TdApi.Message;
//import org.drinkless.tdlib.TdApi.User;
//import org.drinkless.tdlib.TdApi.Contact;
//import org.drinkless.tdlib.TdApi.Supergroup;
//import org.drinkless.tdlib.TdApi.BasicGroup;

import org.luwrain.core.*;
//import org.luwrain.core.events.*;
//import org.luwrain.core.queries.*;
//import org.luwrain.controls.*;
import org.luwrain.app.base.*;

public abstract class Operations
{
    static private final String
	LOG_COMPONENT = Core.LOG_COMPONENT;

    interface ChatHistoryCallback { void onChatHistoryMessages(TdApi.Chat chat, TdApi.Messages messages); }
    interface SupergroupCallback { void onSupergroup(Supergroup supergroup); }
    interface BasicGroupCallback { void onBasicGroup(BasicGroup basicGroup); }

    private final Luwrain luwrain;
    private final Objects objects;

    Operations(Luwrain luwrain, Objects objects)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(objects, "objects");
	this.luwrain = luwrain;
	this.objects = objects;
    }

    abstract Client getClient();

    public void callFunc(TdApi.Function func, int constructor, Consumer<TdApi.Object> res)
    {
		getClient().send(func,
			 new Handler(constructor, (obj)->res.accept(obj)));
    }

    void addContact(String phone, String firstName, String lastName, Runnable onSuccess)
    {
	final TdApi.Contact contact = new TdApi.Contact(phone, firstName, lastName, "", 0);
	getClient().send(new TdApi.ImportContacts(new TdApi.Contact[]{contact}),
			 new DefaultHandler(TdApi.ImportedContacts.CONSTRUCTOR, (obj)->{
				 luwrain.runUiSafely(onSuccess);
			 }));
    }

    void sendTextMessage(Chat chat, String text, Runnable onSuccess)
    {
	final InlineKeyboardButton[] row = {new TdApi.InlineKeyboardButton("https://telegram.org?1", new TdApi.InlineKeyboardButtonTypeUrl()), new TdApi.InlineKeyboardButton("https://telegram.org?2", new TdApi.InlineKeyboardButtonTypeUrl()), new TdApi.InlineKeyboardButton("https://telegram.org?3", new TdApi.InlineKeyboardButtonTypeUrl())};
        final ReplyMarkup replyMarkup = new ReplyMarkupInlineKeyboard(new TdApi.InlineKeyboardButton[][]{row, row, row});
	final InputMessageContent content = new InputMessageText(new FormattedText(text, null), false, true);
	getClient().send(new TdApi.SendMessage(chat.id, 0, 0, null, replyMarkup, content),
			 new Handler(Message.CONSTRUCTOR, (obj)->onSuccess.run()));
    }

    void deleteMessage(TdApi.Chat chat, TdApi.Message[] messages, Runnable onSuccess)
    {
	NullCheck.notNull(chat, "chat");
	NullCheck.notNullItems(messages, "messages");
	NullCheck.notNull(onSuccess, "onSuccess");
	final long[] ids = new long[messages.length];
	for(int i = 0;i < messages.length;i++)
	    ids[i] = messages[i].id;
	getClient().send(new TdApi.DeleteMessages(chat.id, ids, true),
			 new DefaultHandler(TdApi.Message.CONSTRUCTOR, (obj)->{
				 luwrain.runUiSafely(()->onSuccess.run());
			 }));
    }

    void getContacts(Runnable onSuccess)
    {
	getClient().send(new TdApi.GetContacts(),
			 new DefaultHandler(TdApi.Users.CONSTRUCTOR, (obj)->{
				 final TdApi.Users users = (TdApi.Users)obj;
				 objects.setContacts(users.userIds);
				 luwrain.runUiSafely(()->onSuccess.run());
			 }));
    }

    void searchChats(String query, Consumer<Chats> onSuccess)
    {
	getClient().send(new SearchPublicChats(query
					 ), //search limit
			 new DefaultHandler(Chats.CONSTRUCTOR, (obj)->{
				 final Chats chats = (Chats)obj;
				 luwrain.runUiSafely(()->onSuccess.accept(chats));
			 }));
    }


    void openChat(Chat chat, Runnable onSuccess)
    {
	NullCheck.notNull(chat, "chat");
	NullCheck.notNull(onSuccess, "onSuccess");
	getClient().send(new OpenChat(chat.id),
			 new DefaultHandler(Ok.CONSTRUCTOR, (obj)->{
				 luwrain.runUiSafely(onSuccess);
			 }));
    }

    void addChatToList(Chat chat, ChatList chatList, Runnable onSuccess)
    {
	getClient().send(new AddChatToList(chat.id, chatList),
			 new DefaultHandler(Ok.CONSTRUCTOR, (obj)->{
				 luwrain.runUiSafely(onSuccess);
			 }));
    }

        void closeChat(Chat chat, Runnable onSuccess)
    {
	NullCheck.notNull(chat, "chat");
	NullCheck.notNull(onSuccess, "onSuccess");
	getClient().send(new TdApi.CloseChat(chat.id),
			 new DefaultHandler(Ok.CONSTRUCTOR, (obj)->{
				 luwrain.runUiSafely(onSuccess);
			 }));
    }

    void addChatMember(long chatId, long userId, Runnable onSuccess)
    {
	getClient().send(new AddChatMember(chatId, userId, 0),
			 new DefaultHandler(Ok.CONSTRUCTOR, (obj)->{
				 luwrain.runUiSafely(onSuccess);
			 }));
    }


            void leaveChat(Chat chat, Runnable onSuccess)
    {
	NullCheck.notNull(chat, "chat");
	NullCheck.notNull(onSuccess, "onSuccess");
	getClient().send(new TdApi.LeaveChat(chat.id),
			 new DefaultHandler(TdApi.Ok.CONSTRUCTOR, (obj)->{
				 luwrain.runUiSafely(onSuccess);
			 }));
    }


    void getSupergroup(long supergroupId, SupergroupCallback callback)
    {
	NullCheck.notNull(callback, "callback");
	getClient().send(new TdApi.GetSupergroup(supergroupId),
			 new DefaultHandler(TdApi.Supergroup.CONSTRUCTOR, (obj)->{
				 luwrain.runUiSafely(()->callback.onSupergroup((Supergroup)obj));
			 }));
    }

        void getBasicGroup(long basicGroupId, BasicGroupCallback callback)
    {
	NullCheck.notNull(callback, "callback");
	getClient().send(new TdApi.GetBasicGroup(basicGroupId),
			 new DefaultHandler(TdApi.BasicGroup.CONSTRUCTOR, (obj)->{
				 luwrain.runUiSafely(()->callback.onBasicGroup((BasicGroup)obj));
			 }));
    }


    void createPrivateChat(long userId, Consumer<Chat> onSuccess)
    {
	getClient().send(new TdApi.CreatePrivateChat(userId, false),
			 new Handler(Chat.CONSTRUCTOR, (obj)->onSuccess.accept((Chat)obj)));
    }

    void createSupergroupChat(String title, String descr, boolean isChannel, Consumer<Chat> onSuccess)
    {
	getClient().send(new CreateNewSupergroupChat(title, isChannel, descr, null, false),
			 new Handler(Chat.CONSTRUCTOR, (obj)->{
onSuccess.accept((Chat)obj);
			 }));
    }


        void downloadFile(TdApi.File file)
    {
	NullCheck.notNull(file, "file");
	getClient().send(new TdApi.DownloadFile(file.id, 1, 0, 0, false),
			 new DefaultHandler(TdApi.File.CONSTRUCTOR, (obj)->{
			 }));
    }


    void getChatHistory(Chat chat, ChatHistoryCallback callback)
    {
	getClient().send(new GetChatHistory(chat.id, chat.lastMessage != null?chat.lastMessage.id:0, 0, 100, false),
			 new DefaultHandler(Messages.CONSTRUCTOR, (obj)->{
				 luwrain.runUiSafely(()->callback.onChatHistoryMessages(chat, (Messages)obj));
			 }));
    }

        void viewMessages(Chat chat, Message[] messages)
    {
	NullCheck.notNull(chat, "chat");
	NullCheck.notNull(messages, "messages");
	if (messages.length == 0)
	    return;
	final long[] ids = new long[messages.length];
	for(int i = 0;i < messages.length;i++)
	    ids[i] = messages[i].id;
	getClient().send(new TdApi.ViewMessages(chat.id, 0, ids, false),
			 new DefaultHandler(TdApi.Ok.CONSTRUCTOR, (obj)->{
			 }));
    }


    /*
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
    */

        void fillMainChatList(final int limit) {
        synchronized (objects) {
            if (!objects.haveFullMainChatList && limit > objects.mainChats.size()) {
                // send GetChats request if there are some unknown chats and have not enough known chats
                getClient().send(new TdApi.LoadChats(new TdApi.ChatListMain(), limit - objects.mainChats.size()), new Client.ResultHandler() {
                    @Override
                    public void onResult(TdApi.Object object) {
                        switch (object.getConstructor()) {
                            case TdApi.Error.CONSTRUCTOR:
                                if (((TdApi.Error) object).code == 404) {
                                    synchronized (objects) {
                                        objects.haveFullMainChatList = true;
                                    }
                                } else {
                                    System.err.println("Receive an error for GetChats: " + object);
                                }
                                break;
                            case TdApi.Ok.CONSTRUCTOR:
                                // chats had already been received through updates, let's retry request
                                fillMainChatList(limit);
                                break;
                            default:
                                System.err.println("Receive wrong response from TDLib: " + object);
                        }
                    }
                });
                return;
            }
	}
	}

    private final class DefaultHandler implements Client.ResultHandler
    {
	private final int constructor;
	private final Consumer<TdApi.Object> onSuccess;
	DefaultHandler(int constructor, Consumer<TdApi.Object> onSuccess)
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
		final TdApi.Error error = (TdApi.Error)object;
		org.luwrain.core.Log.error(LOG_COMPONENT, "TdApi error: " + String.valueOf(constructor) + ": " + error.toString());
		objects.error(error);
		return;
	    }
	    org.luwrain.core.Log.error(LOG_COMPONENT, "the wrong response for " + String.valueOf(constructor) + ": " + object.toString());
	}
    }

        private final class Handler implements Client.ResultHandler
    {
	private final int constructor;
	private final Consumer<TdApi.Object> onSuccess;
	Handler(int constructor, Consumer<TdApi.Object> onSuccess)
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
		luwrain.runUiSafely(()->onSuccess.accept(object));
		return;
	    }
	    if (object.getConstructor() == TdApi.Error.CONSTRUCTOR)
	    {
		final TdApi.Error error = (TdApi.Error)object;
		org.luwrain.core.Log.error(LOG_COMPONENT, "TdApi error: " + String.valueOf(constructor) + ": " + error.toString());
		objects.error(error);
		return;
	    }
	    org.luwrain.core.Log.error(LOG_COMPONENT, "the wrong response for " + String.valueOf(constructor) + ": " + object.toString());
	}
    }

    
}
