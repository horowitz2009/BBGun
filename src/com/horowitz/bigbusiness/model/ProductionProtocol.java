package com.horowitz.bigbusiness.model;

import java.util.ArrayList;
import java.util.List;

public class ProductionProtocol {
	public static class Entry {
		public Product product;
		public int priority;
		public int minimum;
		public int maximum;

		public Entry(Product product, int priority, int minimum, int maximum) {
			super();
			this.product = product;
			this.priority = priority;
			this.minimum = minimum;
			this.maximum = maximum;
		}

	}

	private List<ProductionProtocol.Entry> _entries;

	public ProductionProtocol() {
		super();
		_entries = new ArrayList<ProductionProtocol.Entry>();
	}

	public List<ProductionProtocol.Entry> getEntries() {
		return _entries;
	}

	public void addEntry(Product product, int priority, int min, int max) {
		_entries.add(new Entry(product, priority, min, max));
	}
}
