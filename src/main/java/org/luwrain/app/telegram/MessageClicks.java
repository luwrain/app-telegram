//
// Copyright 2020-2022 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram;

import java.util.*;

import org.drinkless.tdlib.TdApi;
import org.drinkless.tdlib.TdApi.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.controls.ListArea.*;
import org.luwrain.controls.ListUtils.*;
import org.luwrain.app.base.*;

import static org.luwrain.core.DefaultEventResponse.*;

final class MessageClicks
{
    private final App app;

    MessageClicks(App app)
    {
	NullCheck.notNull(app, "app");
	this.app = app;
    }

    
boolean onMessageClick(Message message)
    {
	NullCheck.notNull(message, "message");
	if (message.content != null && message.content instanceof MessageAudio)
	{
	    final MessageAudio audio = (MessageAudio)message.content;
	    if (audio.audio.audio.local.isDownloadingActive)
		return false;
	    	    if (audio.audio.audio.local.isDownloadingCompleted)
		    {
			if (audio.audio.audio.local.path == null || audio.audio.audio.local.path.isEmpty())
			    return false;
			if (app.getLuwrain().getPlayer() == null)
			    return false;
			app.getLuwrain().getPlayer().play(new org.luwrain.player.FixedPlaylist(new String[]{
				    org.luwrain.util.UrlUtils.fileToUrl(new java.io.File(audio.audio.audio.local.path))
			    }), 0, 0, org.luwrain.player.Player.DEFAULT_FLAGS, new Properties());
		return true;
		    }
		    app.getOperations().downloadFile(audio.audio.audio);
		    app.getLuwrain().message("Выполняется доставка файла");//FIXME:
		    return true;
	}

		if (message.content != null && message.content instanceof MessageDocument)
	{
	    final MessageDocument doc = (MessageDocument)message.content;
	    if (doc.document.document.local.isDownloadingActive)
		return false;
	    /*
	    	    if (audio.audio.audio.local.isDownloadingCompleted)
		    {
			if (audio.audio.audio.local.path == null || audio.audio.audio.local.path.isEmpty())
			    return false;
			if (app.getLuwrain().getPlayer() == null)
			    return false;
			app.getLuwrain().getPlayer().play(new org.luwrain.player.FixedPlaylist(new String[]{
				    org.luwrain.util.UrlUtils.fileToUrl(new java.io.File(audio.audio.audio.local.path))
			    }), 0, 0, org.luwrain.player.Player.DEFAULT_FLAGS, new Properties());
		return true;
		    }
	    */
		    app.getOperations().downloadFile(doc.document.document);
		    app.getLuwrain().message("Выполняется доставка файла");//FIXME:
		    return true;
	}

				if (message.content != null && message.content instanceof MessagePhoto)
	{
	    final MessagePhoto photo = (MessagePhoto)message.content;
	    if (photo.photo.sizes.length == 0)
		return false;
PhotoSize size = photo.photo.sizes[photo.photo.sizes.length - 1];
	    if (size.photo.local.isDownloadingActive)
		return false;
		    app.getOperations().downloadFile(size.photo);
		    app.getLuwrain().message("Выполняется доставка файла");//FIXME:
		    return true;
	}

		if (message.content != null && message.content instanceof MessageVoiceNote)
	{
	    final MessageVoiceNote voiceNoteContent = (MessageVoiceNote)message.content;
	    final VoiceNote voiceNote = voiceNoteContent.voiceNote;
	    if (voiceNote.voice.local.isDownloadingActive)
		return false;
	    	    if (voiceNote.voice.local.isDownloadingCompleted)
		    {
			final LocalFile localFile = voiceNote.voice.local;
			if (localFile.path == null || localFile.path.isEmpty())
			    return false;
			if (app.getLuwrain().getPlayer() == null)
			    return false;
			app.getLuwrain().getPlayer().play(new org.luwrain.player.FixedPlaylist(new String[]{
				    org.luwrain.util.UrlUtils.fileToUrl(new java.io.File(localFile.path))
			    }), 0, 0, org.luwrain.player.Player.DEFAULT_FLAGS, new Properties());
		return true;
		    }
		    app.getOperations().downloadFile(voiceNote.voice);
		    app.getLuwrain().message("Выполняется доставка файла");//FIXME:
		    return true;
	}

	return false;
    }

}
