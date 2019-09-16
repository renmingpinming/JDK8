/*
 * Copyright (c) 1997, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package java.util;

import java.io.InvalidObjectException;
import sun.misc.SharedSecrets;

/**
 *此类实现<tt> Set </ tt>接口，由哈希表支持
   *（实际上是一个<tt> HashMap </ tt>实例）。 它不能保证
   *集的迭代顺序; 特别是，它并不能保证
   *订单将随着时间的推移保持不变。 该类允许<tt> null </ tt>
   *元素。

 * <p>此类为基本操作提供恒定的时间性能
   *（<tt>添加</ tt>，<tt>删除</ tt>，<tt>包含</ tt>和<tt>尺寸</ tt>），
   *假设散列函数在元素之间正确地分散元素
   *水桶 迭代此集合需要与总和成比例的时间
   * <tt> HashSet </ tt>实例的大小（元素数量）加上
   *支持<tt> HashMap </ tt>实例的“容量”（数量）
   *水桶）。 因此，不设置初始容量也非常重要
   *如果迭代性能很重要，则高（或负载因子太低）。
 *
 * <p> <strong>请注意，此实现未同步。</ strong>
  *如果多个线程同时访问哈希集，并且至少有一个
  *线程修改集合，<i>必须</ i>在外部同步。
  *这通常通过同步某个对象来完成
  *自然封装集。
  *
  *如果不存在此类对象，则应使用“包裹”该集合
  * {@link Collections＃synchronizedSet Collections.synchronizedSet}
  * 方法。这最好在创建时完成，以防止意外
  *对集合的非同步访问：<pre>
  * Set s = Collections.synchronizedSet（new HashSet（...））; </ pre>
  *
  * <p>此类的<tt> iterator </ tt>方法返回的迭代器是
  * <i> fail-fast </ i>：如果在迭代器之后的任何时间修改了该集合
  *以任何方式创建，除非通过迭代器自己的<tt> remove </ tt>
  *方法，迭代器抛出{@link ConcurrentModificationException}。
  *因此，面对并发修改，迭代器很快就会失败
  *干净利落，而不是冒着任意的，非确定性的行为冒险
  *未来不确定的时间。
  *
  * <p>请注意，无法保证迭代器的快速失败行为
  *一般来说，不可能做出任何艰难的保证
  *存在未同步的并发修改。失败快速的迭代器
  *尽最大努力抛出<tt> ConcurrentModificationException </ tt>。
  *因此，编写一个依赖于此的程序是错误的
  *正确性异常：<i>迭代器的失败快速行为
  *应仅用于检测错误。</ i>
  *
  * <p>此课程是该课程的成员
  * <a href="{@docRoot}/../technotes/guides/collections/index.html">
  * Java Collections Framework </a>。
 *
 * @param <E> the type of elements maintained by this set
 *
 * @author  Josh Bloch
 * @author  Neal Gafter
 * @see     Collection
 * @see     Set
 * @see     TreeSet
 * @see     HashMap
 * @since   1.2
 */

public class HashSet<E>
    extends AbstractSet<E>
    implements Set<E>, Cloneable, java.io.Serializable
{
    static final long serialVersionUID = -5024744406713321676L;

    //对于HashSet而言，它是基于HashMap实现的，HashSet底层使用HashMap来保存所有元素
    private transient HashMap<E,Object> map;

    // 定义一个虚拟的Object对象作为HashMap的value，将此对象定义为static final
    private static final Object PRESENT = new Object();

    /**
     * 默认的无参构造器，构造一个空的HashSet。
     *
     * 实际底层会初始化一个空的HashMap，并使用默认初始容量为16和加载因子0.75。
     */
    public HashSet() {
        map = new HashMap<>();
    }

    /**
     * 构造一个包含指定collection中的元素的新set。
     *
     * 实际底层使用默认的加载因子0.75和足以包含指定
     * collection中所有元素的初始容量来创建一个HashMap。
     * @param c 其中的元素将存放在此set中的collection。
     */
    public HashSet(Collection<? extends E> c) {
        map = new HashMap<>(Math.max((int) (c.size()/.75f) + 1, 16));
        addAll(c);
    }

    /**
     * 以指定的initialCapacity和loadFactor构造一个空的HashSet
     * 实际底层以相应的参数构造一个空的HashMap
     *
     * @param      initialCapacity   初始容量
     * @param      loadFactor        加载因子
     * @throws     IllegalArgumentException if the initial capacity is less
     *             than zero, or if the load factor is nonpositive
     */
    public HashSet(int initialCapacity, float loadFactor) {
        map = new HashMap<>(initialCapacity, loadFactor);
    }

    /**
     * 以指定的initialCapacity构造一个空的HashSet
     * 实际底层以相应的参数及加载因子loadFactor为0.75构造一个空的HashMap
     *
     * @param      initialCapacity   初始容量
     * @throws     IllegalArgumentException if the initial capacity is less
     *             than zero
     */
    public HashSet(int initialCapacity) {
        map = new HashMap<>(initialCapacity);
    }

    /**
     * 以指定的initialCapacity和loadFactor构造一个新的空链接哈希集合。
     * 此构造函数为包访问权限，不对外公开，实际只是是对LinkedHashSet的支持。
     *
     * 实际底层会以指定的参数构造一个空LinkedHashMap实例来实现
     * @param      initialCapacity   初始容量
     * @param      loadFactor        加载因子
     * @param      dummy            标记
     * @throws     IllegalArgumentException if the initial capacity is less
     *             than zero, or if the load factor is nonpositive
     */
    HashSet(int initialCapacity, float loadFactor, boolean dummy) {
        map = new LinkedHashMap<>(initialCapacity, loadFactor);
    }

    /**
     * 返回对此set中元素进行迭代的迭代器。返回元素的顺序并不是特定的。
     *
     * 底层实际调用底层HashMap的keySet来返回所有的key。
     * 可见HashSet中的元素，只是存放在了底层HashMap的key上，
     * value使用一个static final的Object对象标识。
     * @return 对此set中元素进行迭代的Iterator。
     */
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    /**
     * 返回此set中的元素的数量（set的容量）。
     *
     * 底层实际调用HashMap的size()方法返回Entry的数量，就得到该Set中元素的个数。
     * @return 此set中的元素的数量（set的容量）。
     */
    public int size() {
        return map.size();
    }

    /**
     * 如果此set不包含任何元素，则返回true。
     *
     * 底层实际调用HashMap的isEmpty()判断该HashSet是否为空。
     * @return 如果此set不包含任何元素，则返回true。
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * 如果此set包含指定元素，则返回true。
     * 更确切地讲，当且仅当此set包含一个满足(o==null ? e==null : o.equals(e))
     * 的e元素时，返回true。
     *
     * 底层实际调用HashMap的containsKey判断是否包含指定key。
     * @param o 在此set中的存在已得到测试的元素。
     * @return 如果此set包含指定元素，则返回true。
     */
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    /**
     * 如果此set中尚未包含指定元素，则添加指定元素。
     * 更确切地讲，如果此 set 没有包含满足(e==null ? e2==null : e.equals(e2))
     * 的元素e2，则向此set 添加指定的元素e。
     * 如果此set已包含该元素，则该调用不更改set并返回false。
     *
     * 底层实际将将该元素作为key放入HashMap。
     * 由于HashMap的put()方法添加key-value对时，当新放入HashMap的Entry中key
     * 与集合中原有Entry的key相同（hashCode()返回值相等，通过equals比较也返回true），
     * 新添加的Entry的value会将覆盖原来Entry的value，但key不会有任何改变，
     * 因此如果向HashSet中添加一个已经存在的元素时，新添加的集合元素将不会被放入HashMap中，
     * 原来的元素也不会有任何改变，这也就满足了Set中元素不重复的特性。
     * @param e 将添加到此set中的元素。
     * @return 如果此set尚未包含指定元素，则返回true。
     */
    public boolean add(E e) {
        return map.put(e, PRESENT)==null;
    }

    /**
     * 如果指定元素存在于此set中，则将其移除。
     * 更确切地讲，如果此set包含一个满足(o==null ? e==null : o.equals(e))的元素e，
     * 则将其移除。如果此set已包含该元素，则返回true
     * （或者：如果此set因调用而发生更改，则返回true）。（一旦调用返回，则此set不再包含该元素）。
     *
     * 底层实际调用HashMap的remove方法删除指定Entry。
     * @param o 如果存在于此set中则需要将其移除的对象。
     * @return 如果set包含指定元素，则返回true。
     */
    public boolean remove(Object o) {
        return map.remove(o)==PRESENT;
    }

    /**
     * 从此set中移除所有元素。此调用返回后，该set将为空。
     *
     * 底层实际调用HashMap的clear方法清空Entry中所有元素。
     */
    public void clear() {
        map.clear();
    }

    /**
     * 返回此HashSet实例的浅表副本：并没有复制这些元素本身。
     *
     * 底层实际调用HashMap的clone()方法，获取HashMap的浅表副本，并设置到  HashSet中。
     */
    @SuppressWarnings("unchecked")
    public Object clone() {
        try {
            HashSet<E> newSet = (HashSet<E>) super.clone();
            newSet.map = (HashMap<E, Object>) map.clone();
            return newSet;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    /**
     * Save the state of this <tt>HashSet</tt> instance to a stream (that is,
     * serialize it).
     *
     * @serialData The capacity of the backing <tt>HashMap</tt> instance
     *             (int), and its load factor (float) are emitted, followed by
     *             the size of the set (the number of elements it contains)
     *             (int), followed by all of its elements (each an Object) in
     *             no particular order.
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        // Write out any hidden serialization magic
        s.defaultWriteObject();

        // Write out HashMap capacity and load factor
        s.writeInt(map.capacity());
        s.writeFloat(map.loadFactor());

        // Write out size
        s.writeInt(map.size());

        // Write out all elements in the proper order.
        for (E e : map.keySet())
            s.writeObject(e);
    }

    /**
     * Reconstitute the <tt>HashSet</tt> instance from a stream (that is,
     * deserialize it).
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        // Read in any hidden serialization magic
        s.defaultReadObject();

        // Read capacity and verify non-negative.
        int capacity = s.readInt();
        if (capacity < 0) {
            throw new InvalidObjectException("Illegal capacity: " +
                                             capacity);
        }

        // Read load factor and verify positive and non NaN.
        float loadFactor = s.readFloat();
        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new InvalidObjectException("Illegal load factor: " +
                                             loadFactor);
        }

        // Read size and verify non-negative.
        int size = s.readInt();
        if (size < 0) {
            throw new InvalidObjectException("Illegal size: " +
                                             size);
        }
        // Set the capacity according to the size and load factor ensuring that
        // the HashMap is at least 25% full but clamping to maximum capacity.
        capacity = (int) Math.min(size * Math.min(1 / loadFactor, 4.0f),
                HashMap.MAXIMUM_CAPACITY);

        // Constructing the backing map will lazily create an array when the first element is
        // added, so check it before construction. Call HashMap.tableSizeFor to compute the
        // actual allocation size. Check Map.Entry[].class since it's the nearest public type to
        // what is actually created.

        SharedSecrets.getJavaOISAccess()
                     .checkArray(s, Map.Entry[].class, HashMap.tableSizeFor(capacity));

        // Create backing HashMap
        map = (((HashSet<?>)this) instanceof LinkedHashSet ?
               new LinkedHashMap<E,Object>(capacity, loadFactor) :
               new HashMap<E,Object>(capacity, loadFactor));

        // Read in all elements in the proper order.
        for (int i=0; i<size; i++) {
            @SuppressWarnings("unchecked")
                E e = (E) s.readObject();
            map.put(e, PRESENT);
        }
    }

    /**
     * Creates a <em><a href="Spliterator.html#binding">late-binding</a></em>
     * and <em>fail-fast</em> {@link Spliterator} over the elements in this
     * set.
     *
     * <p>The {@code Spliterator} reports {@link Spliterator#SIZED} and
     * {@link Spliterator#DISTINCT}.  Overriding implementations should document
     * the reporting of additional characteristic values.
     *
     * @return a {@code Spliterator} over the elements in this set
     * @since 1.8
     */
    public Spliterator<E> spliterator() {
        return new HashMap.KeySpliterator<E,Object>(map, 0, -1, 0, 0);
    }
}
