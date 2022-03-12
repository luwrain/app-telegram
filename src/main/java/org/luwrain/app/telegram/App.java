//
// Copyright 2020-2022 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram;

import java.util.*;
import java.io.*;

import org.drinkless.tdlib.*;
import org.luwrain.core.Log;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.app.base.*;

public final class App extends AppBase<Strings> implements MonoApp
{
    static final InputEvent
	HOTKEY_MAIN = new InputEvent(InputEvent.Special.F5),
    	HOTKEY_SEARCH_CHATS = new InputEvent(InputEvent.Special.F6),
	HOTKEY_CONTACTS = new InputEvent(InputEvent.Special.F9);

    static private final int CHAT_NUM_LIMIT = 200;

    static final String
	LOG_COMPONENT = Core.LOG_COMPONENT;

    final long startTimeMillis = System.currentTimeMillis();

    final Core core;
    private Conversations conv = null;
    private MainLayout mainLayout = null;
    private ContactsLayout contactsLayout = null;
    private AuthLayout authLayout = null;
    private SearchChatsLayout searchChatsLayout = null;

    public App(Core core)
    {
	super(Strings.NAME, Strings.class, "luwrain.telegram");
	NullCheck.notNull(core, "core");
	this.core = core;
    }

    @Override protected AreaLayout onAppInit()
    {
	this.conv = new Conversations(this);
	this.mainLayout = new MainLayout(this);
	this.contactsLayout = new ContactsLayout(this);
	this.authLayout = new AuthLayout(this);
	this.searchChatsLayout = new SearchChatsLayout(this);
	this.core.objects.errorListeners.add((error)->{
		message(error.message, Luwrain.MessageType.ERROR);
	    });
	setAppName(getStrings().appName());
	return core.isReady()?mainLayout.getAreaLayout():authLayout.getAreaLayout();
    }

    private void onReady()
    {
	setAreaLayout(mainLayout);

mainLayout.setActiveArea(App.this.mainLayout.chatsArea);
    }

    void layout(AreaLayout layout)
    {
	NullCheck.notNull(layout, "layout");
			getLayout().setBasicLayout(layout);
		getLuwrain().announceActiveArea();
    }

    Layouts layouts()
    {
	return new Layouts(){
	    @Override public boolean main()
	    {
		setAreaLayout(mainLayout);
		getLuwrain().announceActiveArea();
		return true;
	    }
	    	    @Override public boolean contacts()
	    {
		setAreaLayout(contactsLayout);
		getLuwrain().announceActiveArea();
		contactsLayout.updateContactsList();
		return true;
	    }
	    	    	    @Override public boolean searchChats()
	    {
		setAreaLayout(searchChatsLayout);
		getLuwrain().announceActiveArea();
		contactsLayout.updateContactsList();
		return true;
	    }
	};
    }

    @Override public boolean onEscape()
    {
	closeApp();
	return true;
    }

    @Override public void closeApp()
    {
	core.objects.clearListeners();
	super.closeApp();
    }

    @Override public MonoApp.Result onMonoAppSecondInstance(Application app)
    {
	NullCheck.notNull(app, "app");
	return MonoApp.Result.BRING_FOREGROUND;
    }

            Conversations getConv() { return this.conv; }
    public Objects getObjects() { return this.core.objects; }
    public Operations getOperations() { return this.core.operations; }

        interface Layouts
{
    boolean main();
    boolean contacts();
    boolean searchChats();
    }
}
