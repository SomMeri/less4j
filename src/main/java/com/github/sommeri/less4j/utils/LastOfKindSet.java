package com.github.sommeri.less4j.utils;

import java.util.Iterator;
import java.util.LinkedHashSet;

public class LastOfKindSet<K, V> implements Iterable<V> {

  private final LinkedHashSet<Entry<K, V>> entries = new LinkedHashSet<Entry<K, V>>();

  public void add(K kind, V value) {
    Entry<K, V> entry = new Entry<K, V>(kind, value);
    if (entries.contains(entry)) {
      entries.remove(entry);
    }
    entries.add(entry);
  }

  @Override
  public Iterator<V> iterator() {
    return new ValueIterator<K, V>(entries.iterator());
  }
}

class ValueIterator<K, V> implements Iterator<V> {

  private final Iterator<Entry<K,V>> entryIterator;
  
  public ValueIterator(Iterator<Entry<K, V>> entryIterator) {
    super();
    this.entryIterator = entryIterator;
  }

  @Override
  public boolean hasNext() {
    return entryIterator.hasNext();
  }

  @Override
  public V next() {
    return entryIterator.next().value;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

}

class Entry<K, V> {

  public Entry(K kind, V value) {
    super();
    this.kind = kind;
    this.value = value;
  }

  K kind;
  V value;

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((kind == null) ? 0 : kind.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    @SuppressWarnings("rawtypes")
    Entry other = (Entry) obj;
    if (kind == null) {
      if (other.kind != null)
        return false;
    } else if (!kind.equals(other.kind))
      return false;
    return true;
  }

}