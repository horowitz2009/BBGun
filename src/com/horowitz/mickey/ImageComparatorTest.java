package com.horowitz.mickey;

import java.io.IOException;

public class ImageComparatorTest {

	public static void main(String[] args) {
		ScreenScanner scanner = new ScreenScanner(null);
		
		try {
	    scanner.compare("pointerMail2.png", "pointerMail.png");
    } catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    }
	}

}
