//
// Copyright Aliaksei Levin (levlam@telegram.org), Arseny Smirnov (arseny30@gmail.com) 2014-2020
// Copyright 2020 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram;

import org.drinkless.tdlib.*;

final class DefaultHandler implements Client.ResultHandler
{
    @Override public void onResult(TdApi.Object object)
    {
	    //            print(object.toString());
    }
}
