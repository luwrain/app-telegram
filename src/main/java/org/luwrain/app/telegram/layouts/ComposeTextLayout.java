//
// Copyright 2020-2022 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram.layouts;

import java.util.*;
import java.io.*;

import org.drinkless.tdlib.TdApi;
import org.drinkless.tdlib.TdApi.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.app.base.*;
import org.luwrain.app.telegram.*;

import static org.luwrain.util.TextUtils.*;

public final class ComposeTextLayout extends LayoutBase
{
    private final App app;
    private final EditArea editArea;
    private final Chat chat;
    private final Message message;

    public ComposeTextLayout(App app, Chat chat, Message message, ActionHandler closing, Runnable afterSending)
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
	final String[] lines = getText();
	if (lines.length == 0)
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

    private String[] getText()
    {
		final String[] lines = editArea.getText();
		if (lines.length == 0 || (lines.length == 1 && lines[0].trim().isEmpty()))
		    return new String[0];
		final List<String> res = new ArrayList<>();
		StringBuilder b = new StringBuilder();
		for(String i: lines)
		{
		    if (i.trim().isEmpty())
		    {
			if (b.length() == 0)
			    			continue;
			res.add(replaceChars(new String(b).trim()));
			b = new StringBuilder();
			continue;
		    }
		    b.append(i.trim()).append(" ");
		}
		if (b.length() > 0)
		    res.add(replaceChars(new String(b).trim()));
		return res.toArray(new String[res.size()]);
    }

    private String replaceChars(String line)
    {
	return line.replaceAll("---", "—").replaceAll("<<", "«").replaceAll(">>", "»");
    }
}
