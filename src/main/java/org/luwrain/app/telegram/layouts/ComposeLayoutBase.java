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

abstract class ComposeLayoutBase extends LayoutBase
{
    final App app;
    final FormArea formArea;
    final Chat chat;
    final Message message;

    abstract protected boolean onOk(ActionHandler closing, Runnable afterSending);

    protected ComposeLayoutBase(App app, Chat chat, Message message, ActionHandler closing, Runnable afterSending)
    {
	super(app);
	this.app = app;
		this.chat = chat;
		this.message = message;
		this.formArea = new FormArea(getControlContext());
	setCloseHandler(closing);
	setOkHandler(()->onOk(closing, afterSending));
    }

    protected String[] getText()
    {
	final String[] lines = formArea.getMultilineEditText();
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

    protected String replaceChars(String line)
    {
	return line.replaceAll("---", "—").replaceAll("<<", "«").replaceAll(">>", "»");
    }
}
