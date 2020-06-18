//
// Copyright 2020 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram;

import org.luwrain.base.*;
import org.luwrain.core.*;

public final class Extension extends org.luwrain.core.extensions.EmptyExtension
{
    static private final String LOG_COMPONENT = App.LOG_COMPONENT;

    @Override public String init(Luwrain luwrain)
    {
	final JniLoader jniLoader = new JniLoader();
	if (!jniLoader.loadByShortName(luwrain.getClass().getClassLoader(), "tdjni"))
	{
	    Log.warning(LOG_COMPONENT, "unable to load tdjni");
	    return "Unable to load tdjni";
	}
	return null;
    }

    
    @Override public Command[] getCommands(Luwrain luwrain)
    {
	return new Command[]{new SimpleShortcutCommand("telegram")};
    }

    @Override public ExtensionObject[] getExtObjects(Luwrain luwrain)
    {
	return new Shortcut[]{
	    new Shortcut(){
		@Override public String getExtObjName()
		{
		    return "telegram";
		}
		@Override public Application[] prepareApp(String[] args)
		{
		    NullCheck.notNull(args, "args");
		    return new Application[]{new App()};
		}
	    }};
    }
}
