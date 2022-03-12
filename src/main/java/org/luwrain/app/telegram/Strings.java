//
// Copyright 2020-2022 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram;

import java.util.Date;

public interface Strings
{
    static final String NAME = "luwrain.telegram";

    String appName();
    String actionMainChats();
    String actionContacts();
    String actionSearchChats();
    String actionJoin();

    String actionCloseChat();
    String actionDeleteMessage();
    String actionNewContact();

    String chatsAreaName();
    String chatPropsAreaName();
    String contactsAreaName();

    String actionNewChannel();
    String newChannelPopupName();
    String newChannelTitlePopupPrefix();
        String newChannelDescrPopupPrefix();
    String channelCreated(String title);
}
