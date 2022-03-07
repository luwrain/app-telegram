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

final class Core
{
    static private final int CHAT_NUM_LIMIT = 200;

    static final String
	LOG_COMPONENT = "telegram";

    final Luwrain luwrain;
    final File tdlibDir;
    final Operations operations;
    final Objects objects;
    final Client client;
    private boolean ready = false;

    Core(Luwrain luwrain, Runnable onReady)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(onReady, "onReady");
	this.luwrain = luwrain;
	this.tdlibDir = new File(luwrain.getAppDataDir("luwrain.telegram").toFile(), "tdlib");
	this.objects = new Objects(luwrain);
	this.operations = newOperations();
	this.client = Client.create(newResultHandler(onReady), null, null);
        Client.execute(new TdApi.SetLogVerbosityLevel(0));
	final String logFile = new File(luwrain.getFileProperty("luwrain.dir.userhome"), "td.log").getAbsolutePath();
	Log.debug(LOG_COMPONENT, "tdlib log file is " + logFile);
        if (Client.execute(new TdApi.SetLogStream(new TdApi.LogStreamFile(logFile, 1 << 27, true))) instanceof TdApi.Error)
            throw new IOError(new IOException("Write access to the current directory is required"));
    }

    private Client.ResultHandler newResultHandler(Runnable onReadyFunc)
    {
	NullCheck.notNull(onReadyFunc, "onReadyFunc");
	return new UpdatesHandler(tdlibDir, objects){
	    @Override public void onReady()
	    {
		Core.this.ready = true;
		luwrain.runUiSafely(()->{
			operations.fillMainChatList(CHAT_NUM_LIMIT);
			onReadyFunc.run();
		    });
	    }
	    @Override Client getClient()
	    {
		if (Core.this.client == null)
		    Log.warning(LOG_COMPONENT, "providing a null pointer as a client to the updates handler");
		return Core.this.client;
	    }
	};
    }

    private Operations newOperations()
    {
	return new Operations(luwrain, objects){
	    @Override Client getClient()
	    {
		if (Core.this.client == null)
		    Log.warning(LOG_COMPONENT, "providing a null pointer as a client to operations");
		return Core.this.client;
	    }
	};
    }

    boolean isReady() { return ready; }
}
