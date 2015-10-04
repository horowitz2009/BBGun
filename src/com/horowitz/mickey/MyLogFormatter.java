package com.horowitz.mickey;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class MyLogFormatter extends Formatter {

	@Override
	public String format(LogRecord rec) {
		StringBuffer buf = new StringBuffer(1000);
		buf.append(rec.getLevel()).append(" ");
		buf.append(calcDate(rec.getMillis())).append(" ");
		buf.append(formatMessage(rec));
		buf.append("\n");
		return buf.toString();

	}


	@Override
	public String getHead(Handler h) {
		return "\n\n\nMickey START " + calcDate(Calendar.getInstance().getTimeInMillis()) + "\n";
	}

	@Override
	public String getTail(Handler h) {
		return "Mickey END " + calcDate(Calendar.getInstance().getTimeInMillis()) + "\n";
	}

	private String calcDate(long millisecs) {
		SimpleDateFormat date_format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
		Date resultdate = new Date(millisecs);
		return date_format.format(resultdate);
	}

}
