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

    String authPhoneNumberIntro();
    String authPhoneNumberInput();
    String authCodeIntro();
    String authContinue();

    String actionNewChannel();
    String newChannelPopupName();
    String newChannelTitlePopupPrefix();
        String newChannelDescrPopupPrefix();
    String channelCreated(String title);

    String actionComposeText();
    String actionEditMessageText();
    String composeTextAreaName();
    String composedTextEmpty();

    String actionDeleteChat();
    String chatDeletingPopupName();
    String chatDeletingPopupText(String title);

    String actionChatStat();
    String chatStatAreaName();

    String actionPrivacySettings();

    String actionSendPhoto();

    String actionSetChatPhoto();
    String actionChatMembers();

            String actionSendAudio();
    String composeAudioTitle();
    String composeAudioAuthor();
    String composeAudioFile();
    String composeAudioComment();
}
