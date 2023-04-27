package com.bluehouseinc.dataconverter.parsers.esp.model.util;

public enum EspJobDependencyTerminationStatus {
	A, // current job terminates Abnormally
	U, // current job terminates Normally or Abnormally, i.e., it's Unknown.
	N // current job terminates Normally.
}
