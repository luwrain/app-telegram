//
// Copyright Aliaksei Levin (levlam@telegram.org), Arseny Smirnov (arseny30@gmail.com) 2014-2020
// Copyright 2020 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.popups.Popups;

final class Conversations
{
    private final App app;
    private final Luwrain luwrain;
    private final Strings strings;

    Conversations(App app)
    {
	NullCheck.notNull(app, "app");
	this.app = app;
	this.luwrain = app.getLuwrain();
	this.strings = app.getStrings();
    }
}
