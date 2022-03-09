//
// Copyright 2020-2022 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram;

import java.util.*;

import org.drinkless.tdlib.TdApi.Chat;
import org.drinkless.tdlib.TdApi.User;
import org.drinkless.tdlib.TdApi.Message;
import org.drinkless.tdlib.TdApi.MessageText;
import org.drinkless.tdlib.TdApi.MessageAudio;

import org.drinkless.tdlib.TdApi;
import org.drinkless.tdlib.TdApi.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;

final class MessageAppearance implements ConsoleArea.Appearance<Message>
{
    private final Luwrain luwrain;
    private final Objects objects;

    MessageAppearance(Luwrain luwrain, Objects objects)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(objects, "objects");
	this.luwrain = luwrain;
	this.objects = objects;
    }

    @Override public void announceItem(Message message)
    {
	NullCheck.notNull(message, "message");
	if (message.content == null)
	{
	    luwrain.setEventResponse(DefaultEventResponse.hint(Hint.EMPTY_LINE));
	    return;
	}
	if (message.content instanceof MessageText)
	{
	    final MessageText text = (MessageText)message.content;
	    announceMessageText(message, text);
	    return;
	}

	if (message.content instanceof MessageAudio)
	{
	    final MessageAudio audio = (MessageAudio)message.content;
	    announceMessageAudio(message, audio);
	    return;
	}

	luwrain.setEventResponse(DefaultEventResponse.text(message.content.getClass().getName()));
    }

    void announceMessageText(Message message, MessageText text)
    {
	NullCheck.notNull(message, "message");
	NullCheck.notNull(text, "text");
	final User user;
	if (message.senderId instanceof TdApi.MessageSenderUser)
	{
	    user = objects.users.get(((TdApi.MessageSenderUser)message.senderId).userId);
	} else
	    user = null;
	final StringBuilder b = new StringBuilder();
	b.append(luwrain.getSpeakableText(text.text.text, Luwrain.SpeakableTextType.PROGRAMMING ));
	String name = "";
	if (user != null && user.firstName != null && !user.firstName.trim().isEmpty())
	    name = user.firstName.trim();
	name += " ";
    	if (user != null && user.lastName != null && !user.lastName.trim().isEmpty())
	    name += user.lastName.trim();
	name = name.trim();
	if (!name.isEmpty())
	    b.append(", ").append(name);
	long date = message.date;
	date *= 1000;
	b.append(", ").append(luwrain.i18n().getPastTimeBrief(new java.util.Date(date)));
	luwrain.setEventResponse(DefaultEventResponse.listItem(new String(b)));
    }

    void announceMessageAudio(Message message, MessageAudio audio)
    {
	NullCheck.notNull(message, "message");
	NullCheck.notNull(audio, "audio");
	final User user =null;// objects.users.get(message.senderUserId);
	final StringBuilder b = new StringBuilder();
	b.append("аудио ");
	b.append(audio.audio.audio.local.downloadedSize).append("/").append(audio.audio.audio.expectedSize);
	b.append(audio.audio.audio.local.path).append(" ");
	b.append(audio.audio.audio.local.canBeDownloaded).append(" ");
	if (user != null && user.firstName != null && !user.firstName.trim().isEmpty())
	    b.append(" ").append(user.firstName.trim());
	luwrain.setEventResponse(DefaultEventResponse.listItem(new String(b)));
    }

    @Override public String getTextAppearance(Message message)
    {
	NullCheck.notNull(message, "message");
	if (message.content == null)
	    return "";
	if (message.content instanceof MessageText)
	{
	    final MessageText text = (MessageText)message.content;
	    return text.text.text;
	}
	return message.content.getClass().getName();
    }

    static String getMessageText(Message message)
    {
	NullCheck.notNull(message, "message");
	if (message.content == null)
	    return "";
	if (message.content instanceof MessageText)
	{
	    final MessageText text = (MessageText)message.content;
	    return text.text.text;
	}
	return "";
    }
    }
