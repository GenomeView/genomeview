/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.genomeview.core;

import be.abeel.util.LRUCache;

public class LRUSet<E> {

	private static final Object IGNORE = new Object();

	private final LRUCache<E,Object> cache;

	/**
	 * Constructs a LRUCache with a maximum capacity
	 * 
	 * @param maximumCacheSize
	 */
	public LRUSet(int maximumCacheSize) {
		this.cache = new LRUCache<E, Object>(maximumCacheSize);
	}

	public boolean contains(E o) {
		return cache.get(o) != null;
	}

	public void add(E o) {
		cache.put(o, IGNORE);
	}

	public void clear() {
		cache.clear();
	}

}
