package com.horowitz.mickey;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class MyLogger {
	static private FileHandler	fh;
	static private Formatter	 formatter;

	static public void setup() throws IOException {

		// get the global logger to configure it
		Logger logger = Logger.getLogger("");// Logger.GLOBAL_LOGGER_NAME

		// suppress the logging output to the console
		Handler[] handlers = logger.getHandlers();
		if (handlers[0] instanceof ConsoleHandler) {
			logger.removeHandler(handlers[0]);
		}

//		 logger.setLevel(Level.WARNING);
//		 fh = new FileHandler("mickey.log", 2048*1024, 5, true);
//		
//		 // create an HTML formatter
//		 formatter = new MyLogFormatter();
//		 fh.setFormatter(formatter);
//		
//		 logger.addHandler(fh);
	}
}
