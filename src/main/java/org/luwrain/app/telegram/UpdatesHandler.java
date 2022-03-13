//
// Copyright Aliaksei Levin (levlam@telegram.org), Arseny Smirnov (arseny30@gmail.com) 2014-2020
// Copyright 2020-2022 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram;

import java.util.*;
import java.io.*;

import org.drinkless.tdlib.*;
import org.drinkless.tdlib.TdApi.*;

import org.luwrain.core.*;
import org.luwrain.core.Log;

abstract class UpdatesHandler implements Client.ResultHandler
{
    static private final String
	LOG_COMPONENT = Core.LOG_COMPONENT,
	PHONE_NUMBER_BANNED = "PHONE_NUMBER_BANNED";

    private final Luwrain luwrain;
    private final java.io.File tdlibDir;
    private final Objects objects;
        private AuthorizationState authorizationState = null;
    private InputWaiter inputWaiter = null;
        volatile private  boolean haveAuthorization = false;

    UpdatesHandler(Luwrain luwrain, java.io.File tdlibDir, Objects objects)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(tdlibDir, "tdlibDir");
	NullCheck.notNull(objects, "objects");
	this.luwrain = luwrain;
	this.tdlibDir = tdlibDir;
	this.objects = objects;
    }

    abstract Client getClient();
    abstract void onReady();

    @Override public void onResult(TdApi.Object object)
    {
	if (object == null)
	{
	    Log.warning(LOG_COMPONENT, "null update object");
	    return;
	}
	switch (object.getConstructor())
	{

	case UpdateNewMessage.CONSTRUCTOR: {
		    	    final UpdateNewMessage newMessage = (UpdateNewMessage)object;
			    final Message message = newMessage.message;
			    if (message == null)
				break;
			    final Chat chat = objects.chats.get(message.chatId);
			    if (chat == null)
				break;
			    objects.newMessage(chat, message);
	    break;
	}

	case UpdateAuthorizationState.CONSTRUCTOR:
	    authStateUpdated(((TdApi.UpdateAuthorizationState) object).authorizationState);
	    break;

	case UpdateFile.CONSTRUCTOR: {
		    	    final UpdateFile updateFile = (TdApi.UpdateFile) object;
			    synchronized(objects) {
				objects.files.put(updateFile.file.id, updateFile.file);	
			    }
			    objects.filesUpdated(updateFile.file);
			    break;
	}

	case UpdateOption.CONSTRUCTOR: {
	    break;
	}

	case UpdateUser.CONSTRUCTOR: {
	    final UpdateUser updateUser = (UpdateUser) object;
	    synchronized(objects){
		                        objects.users.put(updateUser.user.id, updateUser.user);
	    }
	    					objects.usersUpdated(updateUser.user);
                    break;
	}

                case UpdateUserStatus.CONSTRUCTOR:  {
                    final UpdateUserStatus updateUserStatus = (TdApi.UpdateUserStatus) object;
                    synchronized (objects) {
					                        final User user = objects.users.get(updateUserStatus.userId);
                        user.status = updateUserStatus.status;
                    }
                    break;
                }

	case UpdateBasicGroup.CONSTRUCTOR:{
                    final UpdateBasicGroup updateBasicGroup = (UpdateBasicGroup) object;
		    synchronized(objects){
objects.basicGroups.put(updateBasicGroup.basicGroup.id, updateBasicGroup.basicGroup);
		    }
	}
                    break;

	case UpdateSupergroup.CONSTRUCTOR: {
                    final UpdateSupergroup updateSupergroup = (UpdateSupergroup) object;
		    synchronized (object) {
		                        objects.supergroups.put(updateSupergroup.supergroup.id, updateSupergroup.supergroup);
		    }
	}
                    break;

		    	case UpdateSupergroupFullInfo.CONSTRUCTOR: {
                    final UpdateSupergroupFullInfo fullInfo = (UpdateSupergroupFullInfo) object;
		    synchronized (object) {
		                        objects.supergroupsFullInfo.put(fullInfo.supergroupId, fullInfo.supergroupFullInfo);
		    }
	}
                    break;


	case UpdateSecretChat.CONSTRUCTOR: {
final UpdateSecretChat updateSecretChat = (UpdateSecretChat) object;
synchronized (objects) {
		                        objects.secretChats.put(updateSecretChat.secretChat.id, updateSecretChat.secretChat);
}
	}
                    break;

	case UpdateChatPosition.CONSTRUCTOR: {
	    Log.debug(LOG_COMPONENT, "Update chat position");
	    final UpdateChatPosition updateChat = (UpdateChatPosition) object;
	    if (updateChat.position.list.getConstructor() != ChatListMain.CONSTRUCTOR) 
		break;
	    final long newOrder = updateChat.position.order;
	    final Chat chat = objects.chats.get(updateChat.chatId);
	    if (chat == null)
		break;
	    synchronized (objects) {
		int i;
		//Searching for the position which corresponds to the main chat list
		for (i = 0; i < chat.positions.length; i++)
		    if (chat.positions[i].list.getConstructor() == ChatListMain.CONSTRUCTOR)
			break;
		final ChatPosition[] new_positions = new ChatPosition[chat.positions.length +
								      (newOrder == 0 ?0:1) -
								      (i < chat.positions.length ? 1 : 0)];
		int pos = 0;
		//Adding the new value
		if (newOrder != 0)
		    new_positions[pos++] = updateChat.position;
		//Copying old values excluding the value to be replaced
		for (int j = 0; j < chat.positions.length; j++)
		    if (j != i)
			new_positions[pos++] = chat.positions[j];
		if (pos != new_positions.length)
		    Log.warning(LOG_COMPONENT, "updating the chat position: pos != new_positions.length");
		setChatPositions(chat, new_positions);
	    }
	    objects.chatsUpdated(chat);
	    break;
	}

	case UpdateNewChat.CONSTRUCTOR: {
	    final UpdateNewChat updateNewChat = (UpdateNewChat) object;
	    final Chat chat = updateNewChat.chat;
	    synchronized (objects) {
		objects.chats.put(chat.id, chat);
		final ChatPosition[] positions = chat.positions;
		chat.positions = new TdApi.ChatPosition[0];
		setChatPositions(chat, positions);
	    }
	    objects.chatsUpdated(chat);
	    break;
	}

                case UpdateChatTitle.CONSTRUCTOR: {
                    final UpdateChatTitle updateChat = (UpdateChatTitle) object;
		                        synchronized (objects) {
                    final Chat chat = objects.chats.get(updateChat.chatId);
                        chat.title = updateChat.title;
                    }
                    break;
                }

                case UpdateChatPhoto.CONSTRUCTOR: {
                    final UpdateChatPhoto updateChat = (UpdateChatPhoto) object;
		    /*
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.photo = updateChat.photo;
                    }
		    */
                    break;
                }

                case UpdateChatLastMessage.CONSTRUCTOR: {
                    final UpdateChatLastMessage updateChat = (UpdateChatLastMessage) object;
		    final Chat chat;
		                        synchronized (objects) {
chat = objects.chats.get(updateChat.chatId);
                        chat.lastMessage = updateChat.lastMessage;
                    }
					objects.chatsUpdated(chat);
                    break;
                }

                case UpdateChatReadInbox.CONSTRUCTOR: {
                    final TdApi.UpdateChatReadInbox updateChat = (TdApi.UpdateChatReadInbox) object;
                    synchronized (objects) {
                    final TdApi.Chat chat = objects.chats.get(updateChat.chatId);
                        chat.lastReadInboxMessageId = updateChat.lastReadInboxMessageId;
                        chat.unreadCount = updateChat.unreadCount;
                    }
                    break;
                }

                case UpdateChatReadOutbox.CONSTRUCTOR: {
                    final TdApi.UpdateChatReadOutbox updateChat = (TdApi.UpdateChatReadOutbox) object;
		                        synchronized (objects) {
                    final TdApi.Chat chat = objects.chats.get(updateChat.chatId);
                        chat.lastReadOutboxMessageId = updateChat.lastReadOutboxMessageId;
                    }
                    break;
                }

		    /*
	case UpdateChatUnreadMentionCount.CONSTRUCTOR: {
                    TdApi.UpdateChatUnreadMentionCount updateChat = (TdApi.UpdateChatUnreadMentionCount) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.unreadMentionCount = updateChat.unreadMentionCount;
                    }
                    break;
                }
		    */

	case UpdateMessageMentionRead.CONSTRUCTOR: {
                    final TdApi.UpdateMessageMentionRead updateChat = (TdApi.UpdateMessageMentionRead) object;
		                        synchronized (objects) {
                    final TdApi.Chat chat = objects.chats.get(updateChat.chatId);
                        chat.unreadMentionCount = updateChat.unreadMentionCount;
                    }
                    break;
                }

	    /*
	case UpdateChatReplyMarkup.CONSTRUCTOR: {
                    TdApi.UpdateChatReplyMarkup updateChat = (TdApi.UpdateChatReplyMarkup) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.replyMarkupMessageId = updateChat.replyMarkupMessageId;
                    }
                    break;
                }
	    */

	    /*
	case UpdateChatDraftMessage.CONSTRUCTOR: {
                    TdApi.UpdateChatDraftMessage updateChat = (TdApi.UpdateChatDraftMessage) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.draftMessage = updateChat.draftMessage;
                        setChatOrder(chat, updateChat.order);
                    }
                    break;
                }
	    */

	    /*
	case TdApi.UpdateChatNotificationSettings.CONSTRUCTOR: {
                    TdApi.UpdateChatNotificationSettings update = (TdApi.UpdateChatNotificationSettings) object;
                    TdApi.Chat chat = chats.get(update.chatId);
                    synchronized (chat) {
                        chat.notificationSettings = update.notificationSettings;
                    }
                    break;
                }
	    */

	    /*
	case TdApi.UpdateChatDefaultDisableNotification.CONSTRUCTOR: {
                    TdApi.UpdateChatDefaultDisableNotification update = (TdApi.UpdateChatDefaultDisableNotification) object;
                    TdApi.Chat chat = chats.get(update.chatId);
                    synchronized (chat) {
                        chat.defaultDisableNotification = update.defaultDisableNotification;
                    }
                    break;
                }
	    */

	case TdApi.UpdateChatIsMarkedAsUnread.CONSTRUCTOR: {
                    TdApi.UpdateChatIsMarkedAsUnread update = (TdApi.UpdateChatIsMarkedAsUnread) object;
		    /*
                    TdApi.Chat chat = chats.get(update.chatId);
                    synchronized (chat) {
                        chat.isMarkedAsUnread = update.isMarkedAsUnread;
                    }
		    */
                    break;
                }

	case UpdateUserFullInfo.CONSTRUCTOR:
                    TdApi.UpdateUserFullInfo updateUserFullInfo = (TdApi.UpdateUserFullInfo) object;
		    //                    usersFullInfo.put(updateUserFullInfo.userId, updateUserFullInfo.userFullInfo);
                    break;
                case TdApi.UpdateBasicGroupFullInfo.CONSTRUCTOR:
                    TdApi.UpdateBasicGroupFullInfo updateBasicGroupFullInfo = (TdApi.UpdateBasicGroupFullInfo) object;
		    //                    basicGroupsFullInfo.put(updateBasicGroupFullInfo.basicGroupId, updateBasicGroupFullInfo.basicGroupFullInfo);
                    break;

	default:
	    Log.debug(LOG_COMPONENT, "Unsupported update: " + object);
            }
    }

    private void authStateUpdated(TdApi.AuthorizationState authorizationState)
    {
        if (authorizationState != null)
this.authorizationState = authorizationState;
        switch (this.authorizationState.getConstructor())
	{
	case AuthorizationStateWaitTdlibParameters.CONSTRUCTOR:
	    final TdlibParameters parameters = new TdApi.TdlibParameters();
	    parameters.databaseDirectory = tdlibDir.getAbsolutePath();
	    parameters.useMessageDatabase = true;
	    parameters.useSecretChats = true;
	    parameters.apiId = 94575;
	    parameters.apiHash = "a3406de8d171bb422bb6ddf3bbd800e2";
	    parameters.systemLanguageCode = "ru";
	    parameters.deviceModel = "Desktop";
	    parameters.systemVersion = "Unknown";
	    parameters.applicationVersion = "1.0";
	    parameters.enableStorageOptimizer = true;
	    getClient().send(new TdApi.SetTdlibParameters(parameters), new AuthorizationRequestHandler());
	    break;
	case TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR:
	    getClient().send(new TdApi.CheckDatabaseEncryptionKey(), new AuthorizationRequestHandler());
	    break;
	case AuthorizationStateWaitPhoneNumber.CONSTRUCTOR: {
	    this.inputWaiter = new InputWaiter(InputWaiter.Type.PhoneNumber);
	    objects.newInputWaiter(inputWaiter);
	    final String res = this.inputWaiter.getValue();
	    this.inputWaiter = null;
	    getClient().send(new TdApi.SetAuthenticationPhoneNumber(res, null), new AuthorizationRequestHandler());
	    break;
	}
	case AuthorizationStateWaitOtherDeviceConfirmation.CONSTRUCTOR: {
	    String link = ((TdApi.AuthorizationStateWaitOtherDeviceConfirmation) this.authorizationState).link;
	    System.out.println("Please confirm this login link on another device: " + link);
	    break;
	}
	case AuthorizationStateWaitCode.CONSTRUCTOR: {
	    	    this.inputWaiter = new InputWaiter(InputWaiter.Type.AuthCode);
	    objects.newInputWaiter(inputWaiter);
	    final String res = this.inputWaiter.getValue();
	    this.inputWaiter = null;
	    getClient().send(new TdApi.CheckAuthenticationCode(res), new AuthorizationRequestHandler());
	    break;
	}
	case TdApi.AuthorizationStateWaitRegistration.CONSTRUCTOR: {
	    /*
	    String firstName = promptString("Please enter your first name: ");
	    String lastName = promptString("Please enter your last name: ");
	    client.send(new TdApi.RegisterUser(firstName, lastName), new AuthorizationRequestHandler());
	    */
	    break;
	}
	case AuthorizationStateWaitPassword.CONSTRUCTOR: {
	    /*
	    String password = promptString("Please enter password: ");
	    client.send(new TdApi.CheckAuthenticationPassword(password), new AuthorizationRequestHandler());
	    */
	    break;
	}
	case TdApi.AuthorizationStateReady.CONSTRUCTOR:
	    haveAuthorization = true;
	    onReady();
	    break;
	case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR:
	    haveAuthorization = false;
	    //	    print("Logging out");
	    break;
	case TdApi.AuthorizationStateClosing.CONSTRUCTOR:
	    haveAuthorization = false;
	    //	    print("Closing");
	    break;
	case TdApi.AuthorizationStateClosed.CONSTRUCTOR:
	    //	    print("Closed");
	    /*
	    if (!quiting) {
		client = Client.create(new UpdatesHandler(), null, null); // recreate client after previous has closed
	    }
	    */
	    break;

	    //Skipping
	case 		 UpdateConnectionState.CONSTRUCTOR:
	    	    break;
	default:
	    Log.error(LOG_COMPONENT, "Unsupported authorization state: " + this.authorizationState);
        }
    }

    InputWaiter getInputWaiter()
    {
	return inputWaiter;
    }


        private final class AuthorizationRequestHandler implements Client.ResultHandler
	{
        @Override public void onResult(TdApi.Object object)
	    {
            switch (object.getConstructor())
	    {
	    case TdApi.Error.CONSTRUCTOR: {
		final TdApi.Error err = (TdApi.Error)object;
		                    Log.error(LOG_COMPONENT, object.toString());
				    if (err.message.equals(PHONE_NUMBER_BANNED))
					luwrain.message("Номер телефона заблокирован", Luwrain.MessageType.ERROR); else //FIXME:
		luwrain.message(err.message, Luwrain.MessageType.ERROR);
                    authStateUpdated(null); // repeat last action
	    }
                    break;
                case TdApi.Ok.CONSTRUCTOR:
                    // result is already received through UpdateAuthorizationState, nothing to do
                    break;
                default:
                    Log.error(LOG_COMPONENT, "Receive wrong response from TDLib: " + object);
            }
        }
    }

    private void setChatPositions(Chat chat, ChatPosition[] positions)
    {
	for (ChatPosition position : chat.positions)
	    if (position.list.getConstructor() == ChatListMain.CONSTRUCTOR)
		objects.mainChats.remove(new OrderedChat(chat.id, position));
	chat.positions = positions;
	for (ChatPosition position : chat.positions)
	    if (position.list.getConstructor() == ChatListMain.CONSTRUCTOR)
		objects.mainChats.add(new OrderedChat(chat.id, position));
    }

static final class InputWaiter
{
    enum Type {
	PhoneNumber,
	AuthCode
    };
    final Type type;
    private volatile String value = null;
    InputWaiter(Type type)
    {
	NullCheck.notNull(type, "type");
	this.type = type;
    }
    synchronized String getValue()
    {
	try {
	    while(this.value == null)
		wait();
	    }
	catch(InterruptedException e)
	{
	    Thread.currentThread().interrupt();
	}
	return this.value;
    }
    synchronized void setValue(String value)
    {
	NullCheck.notNull(value, "value");
	this.value = value;
	notifyAll();
    }
}
}
