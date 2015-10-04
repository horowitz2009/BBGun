package com.horowitz.commons;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.horowitz.mickey.Pixel;

public class ImageMask {

	private static final int	    MIN_LEVEL	= 1;
	private Pixel[]	              _mask;
	private Map<Integer, Color[]>	_colors;

	public ImageMask(String name) {
		loadMask(name);
		loadColors(name);
	}

	private List<Pixel> parseLine(String line, int y, int minLevel) {
		List<Pixel> result = new ArrayList<Pixel>();
		for (int x = 0; x < line.length(); ++x) {
			final char ch = line.charAt(x);
			if (ch != ' ') {
				int n = Integer.parseInt("" + ch);
				if (n >= minLevel) {
					result.add(new Pixel(x, y, n));
				}
			}
		}
		return result;
	}

	private void loadMask(String name) {
		List<Pixel> all = null;

		InputStream stream = ImageMask.class.getClassLoader().getResourceAsStream(name + ".txt");
		if (stream != null) {
			all = new ArrayList<Pixel>();

			// InputStreamReader reader = new InputStreamReader(stream);
			DataInputStream reader = new DataInputStream(stream);
			BufferedReader br = new BufferedReader(new InputStreamReader(reader));
			try {
				String line = null;
				int y = 0;

				while ((line = br.readLine()) != null) {
					// System.err.println(line);
					if (line.trim().length() > 0) {
						List<Pixel> pixels = parseLine(line, y++, MIN_LEVEL);
						all.addAll(pixels);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			_mask = all.toArray(new Pixel[0]);
		} else {
			stream = ImageMask.class.getClassLoader().getResourceAsStream(name + "_ind.txt");
			if (stream != null) {
				all = new ArrayList<Pixel>();

				// InputStreamReader reader = new InputStreamReader(stream);
				DataInputStream reader = new DataInputStream(stream);
				BufferedReader br = new BufferedReader(new InputStreamReader(reader));
				try {
					String line = null;
					while ((line = br.readLine()) != null) {
						// System.err.println(line);
						if (line.trim().length() > 0) {
							String[] split = line.split(" ");
							all.add(new Pixel(Integer.parseInt(split[0]), Integer.parseInt(split[1])));
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				Collections.sort(all);
				_mask = all.toArray(new Pixel[0]);

			}
		}
	}

	private void loadColors(String name) {
		_colors = null;

		InputStream stream = ImageMask.class.getClassLoader().getResourceAsStream(name + ".colors.txt");
		if (stream != null) {
			_colors = new Hashtable<>(3);
			DataInputStream reader = new DataInputStream(stream);
			BufferedReader br = new BufferedReader(new InputStreamReader(reader));
			try {
				String line = null;
				while ((line = br.readLine()) != null) {
					// System.err.println(line);
					line = line.trim();
					if (line.length() > 0) {
						String[] s1 = line.split("-");
						int key = Integer.parseInt(s1[0]);
						String[] s2 = s1[1].split(";");
						List<Color> colorsList = new ArrayList<Color>(1);
						for (int i = 0; i < s2.length; i++) {
							String[] s3 = s2[i].split(",");
							Color c = new Color(Integer.parseInt(s3[0].trim()), Integer.parseInt(s3[1].trim()),
							        Integer.parseInt(s3[2].trim()));
							colorsList.add(c);
						}
						_colors.put(key, colorsList.toArray(new Color[0]));
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public Pixel[] getMask() {
		return _mask;
	}

	public Map<Integer, Color[]> getColors() {
		return _colors;
	}

}
