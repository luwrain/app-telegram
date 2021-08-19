//
// Copyright Aliaksei Levin (levlam@telegram.org), Arseny Smirnov (arseny30@gmail.com) 2014-2020
// Copyright 2020 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram;

import java.util.*;
import java.io.*;

import org.drinkless.tdlib.*;

import org.luwrain.core.*;
import org.luwrain.core.Log;

abstract class UpdatesHandler implements Client.ResultHandler
{
    static private final String LOG_COMPONENT = App.LOG_COMPONENT;

    private final File tdlibDir;
    private final Objects objects;
        private TdApi.AuthorizationState authorizationState = null;
        volatile private  boolean haveAuthorization = false;

    UpdatesHandler(File tdlibDir, Objects objects)
    {
	NullCheck.notNull(tdlibDir, "tdlibDir");
	NullCheck.notNull(objects, "objects");
	this.tdlibDir = tdlibDir;
	this.objects = objects;
    }

    abstract Client getClient();
    abstract void onReady();

    @Override public void onResult(TdApi.Object object)
    {
	if (object == null)
	    Log.warning(LOG_COMPONENT, "null update object");
	//			    Log.debug(LOG_COMPONENT, "handling " + object.toString());
	//..	Log.debug(LOG_COMPONENT, "test update");
	switch (object.getConstructor())
	{
	case TdApi.UpdateAuthorizationState.CONSTRUCTOR:
	    authStateUpdated(((TdApi.UpdateAuthorizationState) object).authorizationState);
	    break;
	case TdApi.UpdateFile.CONSTRUCTOR: {
		    	    final TdApi.UpdateFile updateFile = (TdApi.UpdateFile) object;
			    synchronized(objects) {
				objects.files.put(updateFile.file.id, updateFile.file);	
			    }
			    objects.filesUpdated(updateFile.file);
			    break;
	}
	case TdApi.UpdateUser.CONSTRUCTOR: {
	    final TdApi.UpdateUser updateUser = (TdApi.UpdateUser) object;
	    synchronized(objects){
		                        objects.users.put(updateUser.user.id, updateUser.user);
	    }
	    					objects.usersUpdated(updateUser.user);
                    break;
	}
                case TdApi.UpdateUserStatus.CONSTRUCTOR:  {
                    final TdApi.UpdateUserStatus updateUserStatus = (TdApi.UpdateUserStatus) object;
                    synchronized (objects) {
					                        final TdApi.User user = objects.users.get(updateUserStatus.userId);
                        user.status = updateUserStatus.status;
                    }
                    break;
                }
                case TdApi.UpdateBasicGroup.CONSTRUCTOR:
                    final TdApi.UpdateBasicGroup updateBasicGroup = (TdApi.UpdateBasicGroup) object;
		    synchronized(objects){
objects.basicGroups.put(updateBasicGroup.basicGroup.id, updateBasicGroup.basicGroup);
		    }
                    break;
                case TdApi.UpdateSupergroup.CONSTRUCTOR:
                    TdApi.UpdateSupergroup updateSupergroup = (TdApi.UpdateSupergroup) object;
		    //                    supergroups.put(updateSupergroup.supergroup.id, updateSupergroup.supergroup);
                    break;
                case TdApi.UpdateSecretChat.CONSTRUCTOR:
                    TdApi.UpdateSecretChat updateSecretChat = (TdApi.UpdateSecretChat) object;
		    //                    secretChats.put(updateSecretChat.secretChat.id, updateSecretChat.secretChat);
                    break;
                case TdApi.UpdateNewChat.CONSTRUCTOR: {
                    final TdApi.UpdateNewChat updateNewChat = (TdApi.UpdateNewChat) object;
		                        final TdApi.Chat chat = updateNewChat.chat;
		                        synchronized (objects) {
                        objects.chats.put(chat.id, chat);
			objects.mainChats.add(new OrderedChat(/*chat.order*/0, chat.id));
			//			                        final long order = chat.order;
			//                        chat.order = 0;
			//			                        setChatOrder(chat, order);
                    }
								objects.chatsUpdated(chat);
                    break;
                }
                case TdApi.UpdateChatTitle.CONSTRUCTOR: {
                    final TdApi.UpdateChatTitle updateChat = (TdApi.UpdateChatTitle) object;
		                        synchronized (objects) {
                    final TdApi.Chat chat = objects.chats.get(updateChat.chatId);
                        chat.title = updateChat.title;
                    }
                    break;
                }
                case TdApi.UpdateChatPhoto.CONSTRUCTOR: {
                    TdApi.UpdateChatPhoto updateChat = (TdApi.UpdateChatPhoto) object;
		    /*
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.photo = updateChat.photo;
                    }
		    */
                    break;
                }
		    /*
                case TdApi.UpdateChatChatList.CONSTRUCTOR: {
                    final TdApi.UpdateChatChatList updateChat = (TdApi.UpdateChatChatList) object;
		    synchronized (objects){
                    final TdApi.Chat chat = objects.chats.get(updateChat.chatId);
                            assert chat.order == 0; // guaranteed by TDLib
                            chat.chatList = updateChat.chatList;
                        }
                    break;
                }
		    */
                case TdApi.UpdateChatLastMessage.CONSTRUCTOR: {
                    final TdApi.UpdateChatLastMessage updateChat = (TdApi.UpdateChatLastMessage) object;
		    final TdApi.Chat chat;
		                        synchronized (objects) {
chat = objects.chats.get(updateChat.chatId);
                        chat.lastMessage = updateChat.lastMessage;
			//                        setChatOrder(chat, updateChat.order);
                    }
					objects.chatsUpdated(chat);
                    break;
                }
		    /*
                case TdApi.UpdateChatOrder.CONSTRUCTOR: {
                    final TdApi.UpdateChatOrder updateChat = (TdApi.UpdateChatOrder) object;
		    final TdApi.Chat chat;
		    synchronized (objects) {
chat = objects.chats.get(updateChat.chatId);
					    objects.mainChats.remove(new OrderedChat(0, chat.id));
					    objects.mainChats.add(new OrderedChat(updateChat.order, chat.id));
                    }
		    objects.chatsUpdated(chat);
                    break;
                }
		    */
		    /*
                case TdApi.UpdateChatIsPinned.CONSTRUCTOR: {
                    TdApi.UpdateChatIsPinned updateChat = (TdApi.UpdateChatIsPinned) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.isPinned = updateChat.isPinned;
                        setChatOrder(chat, updateChat.order);
                    }
                    break;
                }
		    */
                case TdApi.UpdateChatReadInbox.CONSTRUCTOR: {
                    final TdApi.UpdateChatReadInbox updateChat = (TdApi.UpdateChatReadInbox) object;
                    synchronized (objects) {
                    final TdApi.Chat chat = objects.chats.get(updateChat.chatId);
                        chat.lastReadInboxMessageId = updateChat.lastReadInboxMessageId;
                        chat.unreadCount = updateChat.unreadCount;
                    }
                    break;
                }
                case TdApi.UpdateChatReadOutbox.CONSTRUCTOR: {
                    final TdApi.UpdateChatReadOutbox updateChat = (TdApi.UpdateChatReadOutbox) object;
		                        synchronized (objects) {
                    final TdApi.Chat chat = objects.chats.get(updateChat.chatId);
                        chat.lastReadOutboxMessageId = updateChat.lastReadOutboxMessageId;
                    }
                    break;
                }
                case TdApi.UpdateChatUnreadMentionCount.CONSTRUCTOR: {
                    TdApi.UpdateChatUnreadMentionCount updateChat = (TdApi.UpdateChatUnreadMentionCount) object;
		    /*
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.unreadMentionCount = updateChat.unreadMentionCount;
                    }
		    */
                    break;
                }
                case TdApi.UpdateMessageMentionRead.CONSTRUCTOR: {
                    final TdApi.UpdateMessageMentionRead updateChat = (TdApi.UpdateMessageMentionRead) object;
		                        synchronized (objects) {
                    final TdApi.Chat chat = objects.chats.get(updateChat.chatId);
                        chat.unreadMentionCount = updateChat.unreadMentionCount;
                    }
                    break;
                }
                case TdApi.UpdateChatReplyMarkup.CONSTRUCTOR: {
                    TdApi.UpdateChatReplyMarkup updateChat = (TdApi.UpdateChatReplyMarkup) object;
		    /*
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.replyMarkupMessageId = updateChat.replyMarkupMessageId;
                    }
		    */
                    break;
                }
                case TdApi.UpdateChatDraftMessage.CONSTRUCTOR: {
                    TdApi.UpdateChatDraftMessage updateChat = (TdApi.UpdateChatDraftMessage) object;
		    /*
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.draftMessage = updateChat.draftMessage;
                        setChatOrder(chat, updateChat.order);
                    }
		    */
                    break;
                }
                case TdApi.UpdateChatNotificationSettings.CONSTRUCTOR: {
                    TdApi.UpdateChatNotificationSettings update = (TdApi.UpdateChatNotificationSettings) object;
		    /*
                    TdApi.Chat chat = chats.get(update.chatId);
                    synchronized (chat) {
                        chat.notificationSettings = update.notificationSettings;
                    }
		    */
                    break;
                }
                case TdApi.UpdateChatDefaultDisableNotification.CONSTRUCTOR: {
                    TdApi.UpdateChatDefaultDisableNotification update = (TdApi.UpdateChatDefaultDisableNotification) object;
		    /*
                    TdApi.Chat chat = chats.get(update.chatId);
                    synchronized (chat) {
                        chat.defaultDisableNotification = update.defaultDisableNotification;
                    }
		    >*/
                    break;
                }
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

                case TdApi.UpdateUserFullInfo.CONSTRUCTOR:
                    TdApi.UpdateUserFullInfo updateUserFullInfo = (TdApi.UpdateUserFullInfo) object;
		    //                    usersFullInfo.put(updateUserFullInfo.userId, updateUserFullInfo.userFullInfo);
                    break;
                case TdApi.UpdateBasicGroupFullInfo.CONSTRUCTOR:
                    TdApi.UpdateBasicGroupFullInfo updateBasicGroupFullInfo = (TdApi.UpdateBasicGroupFullInfo) object;
		    //                    basicGroupsFullInfo.put(updateBasicGroupFullInfo.basicGroupId, updateBasicGroupFullInfo.basicGroupFullInfo);
                    break;
                case TdApi.UpdateSupergroupFullInfo.CONSTRUCTOR:
                    TdApi.UpdateSupergroupFullInfo updateSupergroupFullInfo = (TdApi.UpdateSupergroupFullInfo) object;
		    //                    supergroupsFullInfo.put(updateSupergroupFullInfo.supergroupId, updateSupergroupFullInfo.supergroupFullInfo);
                    break;
                default:
                    // print("Unsupported update:" + newLine + object);
            }
    }

    private void authStateUpdated(TdApi.AuthorizationState authorizationState)
    {
        if (authorizationState != null)
this.authorizationState = authorizationState;
	Log.debug(LOG_COMPONENT, "handling " + this.authorizationState.getClass().getName());
        switch (this.authorizationState.getConstructor())
	{
	case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR:
	    TdApi.TdlibParameters parameters = new TdApi.TdlibParameters();
	    parameters.databaseDirectory = tdlibDir.getAbsolutePath();
	    parameters.useMessageDatabase = true;
	    parameters.useSecretChats = true;
	    parameters.apiId = 94575;
	    parameters.apiHash = "a3406de8d171bb422bb6ddf3bbd800e2";
	    parameters.systemLanguageCode = "en";
	    parameters.deviceModel = "Desktop";
	    parameters.systemVersion = "Unknown";
	    parameters.applicationVersion = "1.0";
	    parameters.enableStorageOptimizer = true;
	    getClient().send(new TdApi.SetTdlibParameters(parameters), new AuthorizationRequestHandler());
	    break;
	case TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR:
	    getClient().send(new TdApi.CheckDatabaseEncryptionKey(), new AuthorizationRequestHandler());
	    break;
	case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR: {
	    /*TODO
	    String phoneNumber = promptString("Please enter phone number: ");
	    client.send(new TdApi.SetAuthenticationPhoneNumber(phoneNumber, null), new AuthorizationRequestHandler());
	    */
	    break;
	}
	case TdApi.AuthorizationStateWaitOtherDeviceConfirmation.CONSTRUCTOR: {
	    String link = ((TdApi.AuthorizationStateWaitOtherDeviceConfirmation) this.authorizationState).link;
	    System.out.println("Please confirm this login link on another device: " + link);
	    break;
	}
	case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR: {
	    /*
	    String code = promptString("Please enter authentication code: ");
	    client.send(new TdApi.CheckAuthenticationCode(code), new AuthorizationRequestHandler());
	    */
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
	case TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR: {
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
	default:
	    Log.error(LOG_COMPONENT, "Unsupported authorization state: " + this.authorizationState);
        }
    }


        private final class AuthorizationRequestHandler implements Client.ResultHandler
	{
        @Override public void onResult(TdApi.Object object)
	    {
            switch (object.getConstructor())
	    {
                case TdApi.Error.CONSTRUCTOR:
                    Log.error(LOG_COMPONENT, object.toString());
                    authStateUpdated(null); // repeat last action
                    break;
                case TdApi.Ok.CONSTRUCTOR:
                    // result is already received through UpdateAuthorizationState, nothing to do
                    break;
                default:
                    Log.error(LOG_COMPONENT, "Receive wrong response from TDLib: " + object);
            }
        }
    }

    
}


