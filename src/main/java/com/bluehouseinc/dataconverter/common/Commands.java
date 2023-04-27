package com.bluehouseinc.dataconverter.common;

import java.util.EnumSet;

public enum Commands {
	HELP, QUIT, CONFIG,

	STOP,

	ABORT, CONTINUE, RESTART,

	LOAD, EXPORT, IMPORT,

	TESTCM;

	public static final EnumSet<Commands> helpCommands = EnumSet.of(HELP, QUIT, CONFIG, LOAD, STOP, EXPORT, TESTCM, IMPORT);
}
