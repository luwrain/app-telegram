//
// Copyright 2020-2022 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram;

import java.util.*;
import java.io.*;

import org.drinkless.tdlib.TdApi;
import org.drinkless.tdlib.TdApi.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.app.base.*;
import static org.luwrain.util.TextUtils.*;

final class ComposeTextLayout extends LayoutBase
{
    private final App app;
    private final EditArea editArea;
    private final Chat chat;
    private final Message message;

    ComposeTextLayout(App app, Chat chat, Message message, ActionHandler closing, Runnable afterSending)
    {
	super(app);
	this.app = app;
		this.chat = chat;
		this.message = message;
	this.editArea = new EditArea(editParams((params)->{
		    params.name = app.getStrings().composeTextAreaName();
		}));
	if (message != null && message.content != null && message.content instanceof MessageText)
	{
	    final MessageText text = (MessageText)message.content;
	    editArea.setText(splitLinesAnySeparator(text.text.text));
	}
	setCloseHandler(closing);
	setOkHandler(()->onOk(closing, afterSending));
	setAreaLayout(editArea, null);
    }

    private boolean onOk(ActionHandler closing, Runnable afterSending)
    {
	final String[] lines = editArea.getText();
	if (lines.length == 0 ||
	    (lines.length == 1 && lines[0].trim().isEmpty()))
	{
	    app.message(app.getStrings().composedTextEmpty(), Luwrain.MessageType.ERROR);
	    return true;
	}
	final StringBuilder b = new StringBuilder();
	for(String l: lines)
	    b.append(l).append("\n");
	if (message != null)
	    app.getOperations().editMessageText(chat, message, new String(b), afterSending); else
	app.getOperations().sendTextMessage(chat, new String(b), afterSending);
	return closing.onAction();
    }
}
