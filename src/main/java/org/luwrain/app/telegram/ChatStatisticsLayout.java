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
import org.luwrain.app.base.*;

final class ChatStatisticsLayout extends LayoutBase
{
    private final App app;
    private final SimpleArea textArea;

    ChatStatisticsLayout(App app, Chat chat, ChatStatistics stat, ActionHandler closing)
    {
	super(app);
	this.app = app;
	final List<String> lines = new ArrayList<>();
	lines.add("");
	if (stat instanceof ChatStatisticsChannel)
	{
	    final ChatStatisticsChannel chan = (ChatStatisticsChannel)stat;
	    if (chan.recentMessageInteractions != null)
		for(ChatStatisticsMessageInteractionInfo i:  chan.recentMessageInteractions)
		    lines.add(i.viewCount + ", " + i.forwardCount);
	}
	lines.add("");
	this.textArea = new SimpleArea(getControlContext(), app.getStrings().chatStatAreaName(), lines.toArray(new String[lines.size()]));
	setCloseHandler(closing);
	setAreaLayout(textArea, null);
    }
}
