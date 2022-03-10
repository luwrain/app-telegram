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

final class ChatPreviewLayout extends LayoutBase
{
    static private final String
	LOG_COMPONENT = Core.LOG_COMPONENT;

    private final App app;
    final ListArea<Message> historyArea;
    private final List<Message> items = new ArrayList<>();

    ChatPreviewLayout(App app, Message[] messages, ActionHandler closing)
    {
	super(app);
	this.app = app;
		items.addAll(Arrays.asList(messages));
	this.historyArea = new ListArea<>(listParams((params)->{
		    params.name = "Просмотр";
		    params.model = new ListModel<>(items);
		    params.appearance = new Appearance();
		}));
	setCloseHandler(closing);
	setAreaLayout(historyArea, null);
    }

    private final class Appearance extends AbstractAppearance<Message>
    {
	private final MessageAppearance appearance = new MessageAppearance(getLuwrain(), app.getObjects());
	@Override public void announceItem(Message message, Set<Flags> flags) { appearance.announceItem(message); }
	@Override public String getScreenAppearance(Message message, Set<Flags> flags) { return appearance.getTextAppearance(message); }
    }
}
