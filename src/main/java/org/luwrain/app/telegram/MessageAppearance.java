//
// Copyright 2020-2022 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram;

import java.util.*;

import org.drinkless.tdlib.*;
import org.drinkless.tdlib.TdApi.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;

import static org.luwrain.core.DefaultEventResponse.*;

final class MessageAppearance implements ConsoleArea.Appearance<Message>
{
    private final Luwrain luwrain;
    private final Objects objects;

    MessageAppearance(Luwrain luwrain, Objects objects)
    {
	this.luwrain = luwrain;
	this.objects = objects;
    }

    @Override public void announceItem(Message message)
    {
	if (message.content == null)
	{
	    luwrain.setEventResponse(hint(Hint.EMPTY_LINE));
	    return;
	}
	if (message.content instanceof MessageText)
	{
	    announceText(message, (MessageText)message.content);
	    return;
	}
	if (message.content instanceof MessageAudio)
	{
	    announceMessageAudio(message, (MessageAudio)message.content);
	    return;
	}
	if (message.content instanceof MessagePhoto)
	{
	    announcePhoto(message, (MessagePhoto)message.content);
	    return;
	}
	if (message.content instanceof MessageVideo)
	{
	    announceVideo(message, (MessageVideo)message.content);
	    return;
	}
		if (message.content instanceof MessageDocument)
	{
	    announceDocument(message, (MessageDocument)message.content);
	    return;
	}
	luwrain.setEventResponse(DefaultEventResponse.text(message.content.getClass().getName()));
    }

    @Override public String getTextAppearance(Message message)
    {
	if (message.content == null)
	    return "";
	if (message.content instanceof MessageText)
	{
	    final MessageText text = (MessageText)message.content;
	    return text.text.text;
	}
	if (message.content instanceof MessageAudio)
	{
	    final MessageAudio audio = (MessageAudio)message.content;
	    return audio.caption.text;
	}
	if (message.content instanceof MessagePhoto)
	{
	    final MessagePhoto photo = (MessagePhoto)message.content;
	    return photo.caption.text;
	}
	if (message.content instanceof MessageDocument)
	{
	    final MessageDocument doc = (MessageDocument)message.content;
	    return doc.caption.text;
	}
	if (message.content instanceof MessageVideo)
	{
	    final MessageVideo video = (MessageVideo)message.content;
	    return video.caption.text;
	}
	return message.content.getClass().getName();
    }

    private String getAnnouncementSuffix(Message message)
    {
	final User user;
	if (message.senderId instanceof TdApi.MessageSenderUser)
	    user = objects.users.get(((MessageSenderUser)message.senderId).userId); else
	    user = null;
	final StringBuilder b = new StringBuilder();
	if (user != null)
	{
	    if (user.firstName != null && !user.firstName.trim().isEmpty())
		b.append(user.firstName.trim()).append(" ");
	    if (user.lastName != null && !user.lastName.trim().isEmpty())
		b.append(user.lastName.trim()).append(" ");
	}
	long date = message.date;
	date *= 1000;
	b.append(luwrain.i18n().getPastTimeBrief(new java.util.Date(date)));
	return new String(b);
    }

    static String getMessageText(Message message)
    {
	if (message.content == null)
	    return "";
	if (message.content instanceof MessageText)
	{
	    final MessageText text = (MessageText)message.content;
	    return text.text.text.trim();
	}
	if (message.content instanceof MessagePhoto)
	{
	    final MessagePhoto photo = (MessagePhoto)message.content;
	    return photo.caption.text.trim();
	}
	if (message.content instanceof MessageVideo)
	{
	    final MessageVideo video = (MessageVideo)message.content;
	    return video.caption.text.trim();
	}
	return "";
    }

    private void announceText(Message message, MessageText text)
    {
	luwrain.setEventResponse(listItem(luwrain.getSpeakableText(text.text.text, Luwrain.SpeakableTextType.NATURAL) + ", " + getAnnouncementSuffix(message), Suggestions.CLICKABLE_LIST_ITEM));
    }

    void announceMessageAudio(Message message, MessageAudio audio)
    {

		luwrain.setEventResponse(listItem(Sounds.LIST_ITEM, "АУДИО " + audio.caption.text + ", " + getAnnouncementSuffix(message), Suggestions.CLICKABLE_LIST_ITEM));
		/*
	final User user =null;// objects.users.get(message.senderUserId);
	final StringBuilder b = new StringBuilder();
	b.append("аудио ");
	b.append(audio.audio.audio.local.downloadedSize).append("/").append(audio.audio.audio.expectedSize);
	b.append(audio.audio.audio.local.path).append(" ");
	b.append(audio.audio.audio.local.canBeDownloaded).append(" ");
	if (user != null && user.firstName != null && !user.firstName.trim().isEmpty())
	    b.append(" ").append(user.firstName.trim());
	luwrain.setEventResponse(DefaultEventResponse.listItem(new String(b)));
		*/
    }

    private void announcePhoto(Message message, MessagePhoto photo)
    {
	luwrain.setEventResponse(listItem(Sounds.PICTURE, photo.caption.text + ", " + getAnnouncementSuffix(message), Suggestions.CLICKABLE_LIST_ITEM));
    }

    private void announceVideo(Message message, MessageVideo video)
    {
	luwrain.setEventResponse(listItem(Sounds.PICTURE, "ВИДЕО " + video.caption.text + ", " + getAnnouncementSuffix(message), Suggestions.CLICKABLE_LIST_ITEM));
    }

        private void announceDocument(Message message, MessageDocument doc)
    {
		luwrain.setEventResponse(listItem(Sounds.PICTURE, "ДОКУМЕНТ " + doc.caption.text + ", " + getAnnouncementSuffix(message), Suggestions.CLICKABLE_LIST_ITEM));
    }


    static final class ForList extends ListUtils.AbstractAppearance<Message>
    {
	private final MessageAppearance appearance;
	ForList(App app) { this.appearance = new MessageAppearance(app.getLuwrain(), app.getObjects()); }
	@Override public void announceItem(Message message, Set<Flags> flags) { appearance.announceItem(message); }
	@Override public String getScreenAppearance(Message message, Set<Flags> flags) { return appearance.getTextAppearance(message); }
    }
}
