//
// Copyright 2020 Michael Pozhidaev <msp@luwrain.org>
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
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;
import org.luwrain.app.base.*;

public final class App extends AppBase<Strings> implements MonoApp
{
    static private final int CHAT_NUM_LIMIT = 200;

    static final String
	LOG_COMPONENT = Core.LOG_COMPONENT;

    final long startTimeMillis = System.currentTimeMillis();

    private Conversations conv = null;
    private Core core = null;
    private MainLayout mainLayout = null;
    private ContactsLayout contactsLayout = null;
    private AuthLayout authLayout = null;

    public App() { super(Strings.NAME, Strings.class, "luwrain.telegram"); }

    @Override protected AreaLayout onAppInit()
    {
	this.conv = new Conversations(this);
	this.core = new Core(getLuwrain(), this::onReady);
	this.mainLayout = new MainLayout(this);
	this.contactsLayout = new ContactsLayout(this);
	this.authLayout = new AuthLayout(this);
	setAppName(getStrings().appName());
	return authLayout.getLayout();
    }

    private void onReady()
    {
	setAreaLayout(mainLayout);
core.operations.fillMainChatList(CHAT_NUM_LIMIT);
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
	    @Override public void main()
	    {
		setAreaLayout(mainLayout);
		getLuwrain().announceActiveArea();
	    }
	    	    @Override public void contacts()
	    {
		getLayout().setBasicLayout(contactsLayout.getLayout());
		getLuwrain().announceActiveArea();
		contactsLayout.updateContactsList();
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
	super.closeApp();
    }

    @Override public MonoApp.Result onMonoAppSecondInstance(Application app)
    {
	NullCheck.notNull(app, "app");
	return MonoApp.Result.BRING_FOREGROUND;
    }

    interface Layouts
{
    void main();
    void contacts();
    }

            Conversations getConv() { return this.conv; }
    Objects getObjects() { return this.core.objects; }
    Operations getOperations() { return this.core.operations; }

}
