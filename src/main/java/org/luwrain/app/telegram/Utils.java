//
// Copyright Aliaksei Levin (levlam@telegram.org), Arseny Smirnov (arseny30@gmail.com) 2014-2020
// Copyright 2020 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram;

import java.util.*;

import org.drinkless.tdlib.TdApi.Chat;
import org.drinkless.tdlib.TdApi.Message;
import org.drinkless.tdlib.TdApi.MessageText;

import org.luwrain.core.*;
import org.luwrain.controls.*;

final class Utils
{
    static String getMessageText(Message message)
    {
	NullCheck.notNull(message, "message");
	if (message.content instanceof MessageText)
	{
	    final MessageText text = (MessageText)message.content;
	    return text.text.text;
	}
	return "";
    }
}
