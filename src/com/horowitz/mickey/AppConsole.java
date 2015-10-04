package com.horowitz.mickey;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * 
 * @author Zhivko Hristov
 * @deprecated
 */
public class AppConsole {
	private final static Logger	        LOGGER	 = Logger.getLogger(MainFrame.class.getName());
	private static List<ConsoleVisitor>	visitors	= new ArrayList<>();

	private AppConsole() {
		// singleton
	}

	public static void addVisitor(ConsoleVisitor visitor) {
		visitors.add(visitor);
	}

	public static void println() {
		print("\n");
	}

	public static void println(String message) {
		print(message);
		println();
	}

	public static void print(String message) {
		System.out.print(message);
		LOGGER.info(message);
		for (ConsoleVisitor visitor : visitors) {
			visitor.visit(message);
		}
	}

	public static void clear() {
		System.out.println();
		System.out.println();
		System.out.println();
		for (ConsoleVisitor visitor : visitors) {
			visitor.clear();
		}
	}
}
