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

final class MessageContentLayout extends LayoutBase
{
    private final App app;
    private final SimpleArea textArea;
    private final Message message;

    MessageContentLayout(App app, Message message, ActionHandler closing)
    {
	super(app);
	this.app = app;
		this.message = message;
		final List<String> lines = new ArrayList<>();
		if (message.content != null && message.content instanceof MessageText)
		{
		    final MessageText text = (MessageText)message.content;
		    lines.addAll(Arrays.asList(splitLinesAnySeparator(text.text.text)));
		}
		this.textArea = new SimpleArea(getControlContext(), "FIXME", lines.toArray(new String[lines.size()]));
			setCloseHandler(closing);
	setAreaLayout(textArea, null);
    }
}
