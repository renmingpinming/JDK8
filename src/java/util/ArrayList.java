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

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import sun.misc.SharedSecrets;

/**
 * 概述：
 * List接口可调整大小的数组实现。实现所有可选的List操作，并允许所有元素，包括null，元素可重复。
 * 除了列表接口外，该类提供了一种方法来操作该数组的大小来存储该列表中的数组的大小。
 * 时间复杂度：
 * 方法size、isEmpty、get、set、iterator和listIterator的调用是常数时间的。
 * 添加删除的时间复杂度为O(N)。其他所有操作也都是线性时间复杂度。
 * 容量：
 * 每个ArrayList都有容量，容量大小至少为List元素的长度，默认初始化为10。
 * 容量可以自动增长。
 * 如果提前知道数组元素较多，可以在添加元素前通过调用ensureCapacity()方法提前增加容量以减小后期容量自动增长的开销。
 * 也可以通过带初始容量的构造器初始化这个容量。
 * 线程不安全：
 * ArrayList不是线程安全的。
 * 如果需要应用到多线程中，需要在外部做同步
 * modCount：
 * 定义在AbstractList中：protected transient int modCount = 0;
 * 已从结构上修改此列表的次数。从结构上修改是指更改列表的大小，或者打乱列表，从而使正在进行的迭代产生错误的结果。
 * 此字段由iterator和listiterator方法返回的迭代器和列表迭代器实现使用。
 * 如果意外更改了此字段中的值，则迭代器（或列表迭代器）将抛出concurrentmodificationexception来响应next、remove、previous、set或add操作。
 * 在迭代期间面临并发修改时，它提供了快速失败 行为，而不是非确定性行为。
 * 子类是否使用此字段是可选的。
 * 如果子类希望提供快速失败迭代器（和列表迭代器），则它只需在其 add(int,e)和remove(int)方法（以及它所重写的、导致列表结构上修改的任何其他方法）中增加此字段。
 * 对add(int, e)或remove(int)的单个调用向此字段添加的数量不得超过 1，否则迭代器（和列表迭代器）将抛出虚假的 concurrentmodificationexceptions。
 * 如果某个实现不希望提供快速失败迭代器，则可以忽略此字段。
 * transient：
 * 默认情况下,对象的所有成员变量都将被持久化.在某些情况下,如果你想避免持久化对象的一些成员变量,你可以使用transient关键字来标记他们,transient也是java中的保留字(JDK 1.8)
 */

public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable
{
    //serialVersionUID 是实现 Serializable 接口而来的，而 Serializable 则是应用于Java 对象序列化/反序列化。
    private static final long serialVersionUID = 8683452581122892189L;

    /**
     * 默认容量
     */
    private static final int DEFAULT_CAPACITY = 10;

    /**
     * 空的对象数组
     */
    private static final Object[] EMPTY_ELEMENTDATA = {};

    /**
     *  默认的空数组
     *  无参构造函数创建的数组
     */
    private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};

    /**
     *  存放数据的数组的缓存变量，transient修饰 不可序列化
     */
    transient Object[] elementData; // 非私有，以简化嵌套类访问

    /**
     * 元素数量
     * @serial
     */
    private int size;

    /**
     *  带有容量initialCapacity的构造方法
     *
     * @param  initialCapacity  初始容量列表的初始容量
     * @throws IllegalArgumentException 如果指定容量为负
     */
    public ArrayList(int initialCapacity) {
        if (initialCapacity > 0) {
            this.elementData = new Object[initialCapacity];
        } else if (initialCapacity == 0) {
            //空的对象数组
            this.elementData = EMPTY_ELEMENTDATA;
        } else {
            //容量为负
            throw new IllegalArgumentException("Illegal Capacity: "+
                                               initialCapacity);
        }
    }

    /**
     * 无参构造方法
     */
    public ArrayList() {
        //默认的空数组
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }

    /**
     *带参数Collection的构造方法
     * @param c 其元素将被放入此列表中的集合
     * @throws NullPointerException 如果指定的集合是空的
     */
    public ArrayList(Collection<? extends E> c) {
        elementData = c.toArray();
        if ((size = elementData.length) != 0) {
            // c.toarray可能（错误地）不返回对象[]（见JAVA BUG编号6260652）
            if (elementData.getClass() != Object[].class)
                elementData = Arrays.copyOf(elementData, size, Object[].class);
        } else {
            // 使用空数组
            this.elementData = EMPTY_ELEMENTDATA;
        }
    }

    /**
     * 因为容量常常会大于实际元素的数量。内存紧张时，可以调用该方法删除预留的位置，调整容量为元素实际数量。
     * 如果确定不会再有元素添加进来时也可以调用该方法来节约空间
     */
    public void trimToSize() {
        //已从结构上修改此列表的次数+1
        modCount++;
        if (size < elementData.length) {
            elementData = (size == 0)
              ? EMPTY_ELEMENTDATA
              : Arrays.copyOf(elementData, size);
        }
    }

    /**
     *使用指定参数设置数组容量
     * @param   minCapacity   所需的最小容量
     */
    public void ensureCapacity(int minCapacity) {
        int minExpand = (elementData != DEFAULTCAPACITY_EMPTY_ELEMENTDATA)
            // 如果数组不为空，容量预取0，否则去默认值(10)
            ? 0 : DEFAULT_CAPACITY;

        //若参数大于预设的容量，在使用该参数进一步设置数组容量
        if (minCapacity > minExpand) {
            ensureExplicitCapacity(minCapacity);
        }
    }

    /**
     *空数组时最小扩容量
     */
    private static int calculateCapacity(Object[] elementData, int minCapacity) {
        ////如果是个空的数组,取10和参数间大的
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
            return Math.max(DEFAULT_CAPACITY, minCapacity);
        }
        return minCapacity;
    }

    /**
     * 得到最小扩容量
     */
    private void ensureCapacityInternal(int minCapacity) {
        ensureExplicitCapacity(calculateCapacity(elementData, minCapacity));
    }

    /**
     * 判断是否需要扩容
     */
    private void ensureExplicitCapacity(int minCapacity) {
        //AbstractList字段:修改次数,Fail-Fast 机制用到
        modCount++;

        // 如果最小需要空间比elementData的内存空间要大，则需要扩容
        if (minCapacity - elementData.length > 0)
            //扩容
            grow(minCapacity);
    }

    /**
     *  数组的最大容量，可能会导致内存溢出(VM内存限制)
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    /**
     * 扩容，以确保它可以至少持有由参数指定的元素的数目
     */
    private void grow(int minCapacity) {
        // 获取到ArrayList中elementData数组的内存空间长度
        int oldCapacity = elementData.length;
        //oldCapacity >> 1：位运算，除2。扩容至原来的1.5倍
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        //和输入的比较，使用大的
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        //若预设值大于默认的最大值检查是否溢出
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        // 调用Arrays.copyOf方法将elementData数组指向新的内存空间时newCapacity的连续空间
        // 并将elementData的数据复制到新的内存空间
        elementData = Arrays.copyOf(elementData, newCapacity);
    }

    /**
     * 检查是否溢出，若没有溢出，返回最大整数值(java中的int为4字节，所以最大为0x7fffffff)或默认最大值
     */
    private static int hugeCapacity(int minCapacity) {
        //扩展数量为负数直接溢出error
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }

    /**
     * 返回ArrayList的大小
     */
    public int size() {
        return size;
    }

    /**
     * 是否为空
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * 是否包含某个元素
     */
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    /**
     * 返回第一个指定元素的值，会根据是否为null使用不同方式判断，没有返回-1
     */
    public int indexOf(Object o) {
        //为空
        if (o == null) {
            for (int i = 0; i < size; i++)
                if (elementData[i]==null)
                    return i;
        } else {
            for (int i = 0; i < size; i++)
                if (o.equals(elementData[i]))
                    return i;
        }
        return -1;
    }

    /**
     * 返回最后一个是指定元素的位置，会根据是否为null使用不同方式判断，没有返回-1
     */
    public int lastIndexOf(Object o) {
        if (o == null) {
            for (int i = size-1; i >= 0; i--)
                if (elementData[i]==null)
                    return i;
        } else {
            for (int i = size-1; i >= 0; i--)
                if (o.equals(elementData[i]))
                    return i;
        }
        return -1;
    }

    /**
     *返回副本，元素本身没有被复制，复制过程数组发生改变会抛出异常
     */
    public Object clone() {
        try {
            // 调用父类(翻看源码可见是Object类)的clone方法得到一个ArrayList副本
            ArrayList<?> v = (ArrayList<?>) super.clone();
            // 调用Arrays类的copyOf，将ArrayList的elementData数组赋值给副本的elementData数组
            v.elementData = Arrays.copyOf(elementData, size);
            //已从结构上修改此列表的次数重新设置为0
            v.modCount = 0;
            return v;
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }
    }

    /**
     * 转换为Object数组，使用Arrays.copyOf()方法
     */
    public Object[] toArray() {
        return Arrays.copyOf(elementData, size);
    }

    /**
     * 将ArrayList里面的元素赋值到一个数组中去
     * 如果a的长度小于ArrayList的长度，直接调用Arrays类的copyOf，返回一个比a数组长度要大的新数组，里面元素就是ArrayList里面的元素；
     * 如果a的长度比ArrayList的长度大，那么就调用System.arraycopy，将ArrayList的elementData数组赋值到a数组，然后把a数组的size位置赋值为空。
     *
     * @param a 如果它的长度大的话，列表元素将存储在这个数组中; 否则，将为此分配一个相同运行时类型的新数组。
     * @return 一个包含ArrayList元素的数组
     * @throws ArrayStoreException  将与数组类型不兼容的值赋值给数组元素时抛出的异常
     * @throws NullPointerException 数组为空
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < size)
            // 创建一个新的a的运行时类型数组，内容不变
            return (T[]) Arrays.copyOf(elementData, size, a.getClass());
        // 如果a能装下list中的数据就复制到a里面
        //src:源数组；	srcPos:源数组要复制的起始位置；
        //dest:目的数组；	destPos:目的数组放置的起始位置；	length:复制的长度。
        System.arraycopy(elementData, 0, a, 0, size);
        if (a.length > size)
            a[size] = null;//这一步的作用还是促进GC
        return a;
    }

    // 位置访问操作

    @SuppressWarnings("unchecked")
    E elementData(int index) {
        return (E) elementData[index];
    }

    /**
     *返回下标值，但是会先检查这个位置数否超出数组长度
     * @param  index 索引
     * @return list中指定位置的元素
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E get(int index) {
        // 检查是否越界
        rangeCheck(index);

        return elementData(index);
    }

    /**
     * 替代index元素为新元素，返回旧元素
     * @param index 要替换的元素索引
     * @param element 要存储在指定位置的元素
     * @return 之前在指定位置的元素
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E set(int index, E element) {
        // 检查是否越界
        rangeCheck(index);
        //旧元素
        E oldValue = elementData(index);
        elementData[index] = element;
        return oldValue;
    }

    /**
     *在最后添加元素
     * @param e 要添加到此列表中的元素
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     */
    public boolean add(E e) {
        //是否扩容,会增加modCount!!
        ensureCapacityInternal(size + 1);
        elementData[size++] = e;
        return true;
    }

    /**
     * 特定位置插入元素
     * @param index 指定元素将被插入的索引
     * @param element 要插入的元素
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public void add(int index, E element) {
        //范围判断
        rangeCheckForAdd(index);
        //是否扩容,会增加modCount!!
        ensureCapacityInternal(size + 1);
        //src:源数组；	srcPos:源数组要复制的起始位置；
        //dest:目的数组；	destPos:目的数组放置的起始位置；	length:复制的长度。
        System.arraycopy(elementData, index, elementData, index + 1,
                         size - index);
        elementData[index] = element;
        size++;
    }

    /**
     *删除特定位置的元素
     * @param index 要删除的位置索引
     * @return 删除元素的值
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E remove(int index) {
        //是否越界
        rangeCheck(index);
        //已从结构上修改此列表的次数+1
        modCount++;
        //旧元素值
        E oldValue = elementData(index);
        //获取index位置开始到最后一个位置的个数
        int numMoved = size - index - 1;
        //不是最后一个,index之后的往前移
        if (numMoved > 0)
            System.arraycopy(elementData, index+1, elementData, index,
                             numMoved);
        elementData[--size] = null; // 便于垃圾回收器回收

        return oldValue;
    }

    /**
     *移除第一个指定元素
     * @param o 要删除的元素
     * @return <tt>true</tt> if this list contained the specified element
     */
    public boolean remove(Object o) {
        if (o == null) {
            for (int index = 0; index < size; index++)
                if (elementData[index] == null) {
                    //快速删除指定位置的元素
                    fastRemove(index);
                    return true;
                }
        } else {
            for (int index = 0; index < size; index++)
                if (o.equals(elementData[index])) {
                    //快速删除指定位置的元素
                    fastRemove(index);
                    return true;
                }
        }
        return false;
    }

    /*
     * 快速删除指定位置的元素,不需要检查和返回值
     */
    private void fastRemove(int index) {
        //已从结构上修改此列表的次数+1
        modCount++;
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(elementData, index+1, elementData, index,
                             numMoved);
        elementData[--size] = null; // 便于垃圾回收器回收
    }

    /**
     * 清空数组，把每一个值设为null,方便垃圾回收(不同于reset，数组默认大小有改变的话不会重置)
     */
    public void clear() {
        //已从结构上修改此列表的次数+1
        modCount++;

        // 便于垃圾回收器回收
        for (int i = 0; i < size; i++)
            elementData[i] = null;

        size = 0;
    }

    /**
     *将指定集合中的所有元素追加到末尾
     * @param c 要添加的集合
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     */
    public boolean addAll(Collection<? extends E> c) {
        // 将c转换为数组a
        Object[] a = c.toArray();
        //a的长度
        int numNew = a.length;
        // 扩容至size + numNew
        ensureCapacityInternal(size + numNew);  // 已从结构上修改此列表的次数+1
        //把a复制到数组末尾
        System.arraycopy(a, 0, elementData, size, numNew);
        size += numNew;
        return numNew != 0;
    }

    /**
     *在指定位置插入集合
     * @param index 位置索引
     * @param c 要添加的集合
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws NullPointerException if the specified collection is null
     */
    public boolean addAll(int index, Collection<? extends E> c) {
        //添加范围判断
        rangeCheckForAdd(index);
        // 将c转换为数组a
        Object[] a = c.toArray();
        //a的长度
        int numNew = a.length;
        // 扩容至size + numNew
        ensureCapacityInternal(size + numNew);  // 已从结构上修改此列表的次数+1
        //要移动的个数  如index=1,size = 3,移动2个数
        int numMoved = size - index;
        if (numMoved > 0)
            System.arraycopy(elementData, index, elementData, index + numNew,
                             numMoved);
        //a复制到index开始的位置
        System.arraycopy(a, 0, elementData, index, numNew);
        size += numNew;
        return numNew != 0;
    }

    /**
     *删除指定范围元素。参数为开始删的位置和结束位置
     * @throws IndexOutOfBoundsException if {@code fromIndex} or
     *         {@code toIndex} is out of range
     *         ({@code fromIndex < 0 ||
     *          fromIndex >= size() ||
     *          toIndex > size() ||
     *          toIndex < fromIndex})
     */
    protected void removeRange(int fromIndex, int toIndex) {
        // 已从结构上修改此列表的次数+1
        modCount++;
        //移动个数(toIndex不删除)
        int numMoved = size - toIndex;
        System.arraycopy(elementData, toIndex, elementData, fromIndex,
                         numMoved);

        // 便于垃圾回收器回收
        int newSize = size - (toIndex-fromIndex);
        for (int i = newSize; i < size; i++) {
            elementData[i] = null;
        }
        size = newSize;
    }

    /**
     * 检查index是否超出数组长度
     */
    private void rangeCheck(int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    /**
     * 添加范围判断
     */
    private void rangeCheckForAdd(int index) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    /**
     * 下标越界提示信息
     */
    private String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+size;
    }

    /**
     *删除指定集合包含的元素
     * @param c 包含要从此列表中移除的元素的集合
     * @return {@code true} if this list changed as a result of the call
     * @throws ClassCastException if the class of an element of this list
     *         is incompatible with the specified collection
     * (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if this list contains a null element and the
     *         specified collection does not permit null elements
     * (<a href="Collection.html#optional-restrictions">optional</a>),
     *         or if the specified collection is null
     * @see Collection#contains(Object)
     */
    public boolean removeAll(Collection<?> c) {
        // 如果c为空，则抛出空指针异常
        Objects.requireNonNull(c);
        // 调用批量删除batchRemove方法移除c中的元素
        return batchRemove(c, false);
    }

    /**
     *仅仅保留指定集合包含的元素
     * @param c 包含要从此列表中保留的元素的集合
     * @return {@code true} if this list changed as a result of the call
     * @throws ClassCastException if the class of an element of this list
     *         is incompatible with the specified collection
     * (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if this list contains a null element and the
     *         specified collection does not permit null elements
     * (<a href="Collection.html#optional-restrictions">optional</a>),
     *         or if the specified collection is null
     * @see Collection#contains(Object)
     */
    public boolean retainAll(Collection<?> c) {
        // 如果c为空，则抛出空指针异常
        Objects.requireNonNull(c);
        // 调用批量删除batchRemove方法移除不在c中的元素
        return batchRemove(c, true);
    }

    /**
     * 根据complement,批量删除或不删除
     * @param c
     * @param complement true时从数组保留指定集合中元素的值，为false时从数组删除指定集合中元素的值
     * @return
     */
    private boolean batchRemove(Collection<?> c, boolean complement) {
        final Object[] elementData = this.elementData;
        // 定义一个w，一个r，两个同时右移
        int r = 0, w = 0;
        boolean modified = false;
        try {
            //r遍历一遍
            for (; r < size; r++)
                //complement为true:如果c中包含elementData[r]
                //complement为false:如果c中不包含elementData[r]
                if (c.contains(elementData[r]) == complement)
                    //将r位置的元素赋值给w位置的元素，w自增
                    elementData[w++] = elementData[r];
        } finally {
            // 防止抛出异常导致上面r的右移过程没完成
            if (r != size) {
                // 将r未右移完成的位置的元素赋值给w右边位置的元素
                System.arraycopy(elementData, r,
                                 elementData, w,
                                 size - r);
                w += size - r;
            }
            // 如果有被覆盖掉的元素，则将w后面的元素都赋值为null
            if (w != size) {
                // 便于垃圾回收器回收
                for (int i = w; i < size; i++)
                    elementData[i] = null;
                // 已从结构上修改此列表的次数+(size - w)
                modCount += size - w;
                //新的大小为保留的元素的个数
                size = w;
                modified = true;
            }
        }
        return modified;
    }

    /**
     *保存数组实例的状态到一个流(序列化)
     * @serialData The length of the array backing the <tt>ArrayList</tt>
     *             instance is emitted (int), followed by all of its elements
     *             (each an <tt>Object</tt>) in the proper order.
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException{
        //写出元素计数和任何隐藏的东西
        int expectedModCount = modCount;
        //执行默认的反序列化/序列化过程。将当前类的非静态和非瞬态字段写入此流
        s.defaultWriteObject();

        // 读入数组长度
        s.writeInt(size);

        //读入所有元素
        for (int i=0; i<size; i++) {
            s.writeObject(elementData[i]);
        }
        //如果发生了修改,抛出异常
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }

    /**
     * 从流中重构ArrayList实例（即反序列化）。
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        // 声明为空数组
        elementData = EMPTY_ELEMENTDATA;

        //执行默认的反序列化/序列化过程
        s.defaultReadObject();

        // 读入数组长度  没什么用，只是因为写出的时候写了size属性，读的时候也要按顺序来读
        s.readInt(); // ignored

        if (size > 0) {
            // 空数组时最小扩容量
            int capacity = calculateCapacity(elementData, size);
            SharedSecrets.getJavaOISAccess().checkArray(s, Object[].class, capacity);
            // 检查是否需要扩容
            ensureCapacityInternal(size);

            Object[] a = elementData;
            // 按正确的顺序读入所有元素。
            for (int i=0; i<size; i++) {
                a[i] = s.readObject();
            }
        }
    }

    /**
     *返回一个从index开始的ListIterator(列表迭代器)对象
     * <p>The returned list iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
     *fail-fast机制
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public ListIterator<E> listIterator(int index) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("Index: "+index);
        //ListIterator迭代器
        return new ListItr(index);
    }

    /**
     *返回一个ListIterator对象，ListItr为ArrayList的一个内部类，其实现了ListIterator<E> 接口
     * @see #listIterator(int)
     */
    public ListIterator<E> listIterator() {
        return new ListItr(0);
    }

    /**
     *返回一个Iterator对象，Itr为ArrayList的一个内部类，其实现了Iterator<E>接口
     * @return an iterator over the elements in this list in proper sequence
     */
    public Iterator<E> iterator() {
        return new Itr();
    }

    /**
     * 通用的迭代器实现
     */
    private class Itr implements Iterator<E> {
        int cursor;       // 游标，下一个元素的索引，默认初始化为0
        int lastRet = -1; // 上次访问的元素的位置; -1 表示没有
        int expectedModCount = modCount;

        Itr() {}

        //是否有下一个,游标不等于数组大小
        public boolean hasNext() {
            return cursor != size;
        }

        //返回下一个元素
        @SuppressWarnings("unchecked")
        public E next() {
            //fail-fast机制
            checkForComodification();
            int i = cursor;
            if (i >= size)
                throw new NoSuchElementException();
            Object[] elementData = ArrayList.this.elementData;
            if (i >= elementData.length)
                throw new ConcurrentModificationException();
            //游标+1
            cursor = i + 1;
            //返回下一个元素,lastRet记录
            return (E) elementData[lastRet = i];
        }

        //删除元素
        public void remove() {
            //-1不能删除
            if (lastRet < 0)
                throw new IllegalStateException();
            //fail-fast机制
            checkForComodification();

            try {
                //掉用remove方法删除索引为lastRet的元素
                ArrayList.this.remove(lastRet);
                //数组整体前移了,游标从上次访问的地方继续
                cursor = lastRet;
                lastRet = -1;
                //fail-fast机制
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        //能够将Iterator中迭代剩余的元素传递给一个函数
        //forEachRemaining()使用迭代器Iterator的所有元素，并且第二次调用它将不会做任何事情，因为不再有下一个元素。
        @Override
        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super E> consumer) {
            //consumer不为空
            Objects.requireNonNull(consumer);
            final int size = ArrayList.this.size;
            int i = cursor;
            if (i >= size) {
                return;
            }
            final Object[] elementData = ArrayList.this.elementData;
            if (i >= elementData.length) {
                throw new ConcurrentModificationException();
            }
            while (i != size && modCount == expectedModCount) {
                //对给定的参数执行此操作
                consumer.accept((E) elementData[i++]);
            }
            // 把i赋回游标和上次访问的元素的位置
            //在迭代结束时更新一次以减少堆写入流量
            cursor = i;
            lastRet = i - 1;
            //fail-fast机制
            checkForComodification();
        }

        //fail-fast机制,迭代期间不能有修改
        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    /**
     * ListIterator迭代器实现,继承通用迭代器
     */
    private class ListItr extends Itr implements ListIterator<E> {
        //初始化,游标赋值
        ListItr(int index) {
            super();
            cursor = index;
        }

        //是否有前一个
        public boolean hasPrevious() {
            return cursor != 0;
        }
        //下一个元素的索引
        public int nextIndex() {
            return cursor;
        }
        //前一个元素的索引
        public int previousIndex() {
            return cursor - 1;
        }

        //获取前一个元素
        @SuppressWarnings("unchecked")
        public E previous() {
            //fail-fast机制
            checkForComodification();
            //游标-1
            int i = cursor - 1;
            if (i < 0)
                throw new NoSuchElementException();
            Object[] elementData = ArrayList.this.elementData;
            if (i >= elementData.length)
                throw new ConcurrentModificationException();
            //游标赋值
            cursor = i;
            //返回元素值,记录上次访问的元素的位置
            return (E) elementData[lastRet = i];
        }

        //替换当前值
        public void set(E e) {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                ArrayList.this.set(lastRet, e);
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        //当前位置插入
        public void add(E e) {
            checkForComodification();

            try {
                int i = cursor;
                ArrayList.this.add(i, e);
                cursor = i + 1;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * 返回指定的此列表部分的视图,子列表,如果两个数相同返回空
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public List<E> subList(int fromIndex, int toIndex) {
        //检查范围是否合法
        subListRangeCheck(fromIndex, toIndex, size);
        //返回子列表
        return new SubList(this, 0, fromIndex, toIndex);
    }

    //检查范围是否合法
    static void subListRangeCheck(int fromIndex, int toIndex, int size) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        if (toIndex > size)
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                                               ") > toIndex(" + toIndex + ")");
    }

    //子列表,添加修改查询都在父列表中操作,根据偏移量取值
    private class SubList extends AbstractList<E> implements RandomAccess {
        //父列表
        private final AbstractList<E> parent;
        //父偏移(父开始位置)
        private final int parentOffset;
        //子偏移(子开始位置)
        private final int offset;
        int size;

        SubList(AbstractList<E> parent,
                int offset, int fromIndex, int toIndex) {
            this.parent = parent;
            this.parentOffset = fromIndex;
            this.offset = offset + fromIndex;
            this.size = toIndex - fromIndex;
            this.modCount = ArrayList.this.modCount;
        }

        public E set(int index, E e) {
            //索引是否超出范围
            rangeCheck(index);
            //fail-fast机制
            checkForComodification();
            E oldValue = ArrayList.this.elementData(offset + index);
            ArrayList.this.elementData[offset + index] = e;
            return oldValue;
        }

        public E get(int index) {
            rangeCheck(index);
            checkForComodification();
            return ArrayList.this.elementData(offset + index);
        }

        public int size() {
            checkForComodification();
            return this.size;
        }

        public void add(int index, E e) {
            rangeCheckForAdd(index);
            checkForComodification();
            parent.add(parentOffset + index, e);
            this.modCount = parent.modCount;
            this.size++;
        }

        public E remove(int index) {
            rangeCheck(index);
            checkForComodification();
            E result = parent.remove(parentOffset + index);
            this.modCount = parent.modCount;
            this.size--;
            return result;
        }

        protected void removeRange(int fromIndex, int toIndex) {
            checkForComodification();
            parent.removeRange(parentOffset + fromIndex,
                               parentOffset + toIndex);
            this.modCount = parent.modCount;
            this.size -= toIndex - fromIndex;
        }

        public boolean addAll(Collection<? extends E> c) {
            return addAll(this.size, c);
        }

        public boolean addAll(int index, Collection<? extends E> c) {
            rangeCheckForAdd(index);
            int cSize = c.size();
            if (cSize==0)
                return false;

            checkForComodification();
            parent.addAll(parentOffset + index, c);
            this.modCount = parent.modCount;
            this.size += cSize;
            return true;
        }

        public Iterator<E> iterator() {
            return listIterator();
        }

        public ListIterator<E> listIterator(final int index) {
            checkForComodification();
            rangeCheckForAdd(index);
            final int offset = this.offset;

            return new ListIterator<E>() {
                int cursor = index;
                int lastRet = -1;
                int expectedModCount = ArrayList.this.modCount;

                public boolean hasNext() {
                    return cursor != SubList.this.size;
                }

                @SuppressWarnings("unchecked")
                public E next() {
                    checkForComodification();
                    int i = cursor;
                    if (i >= SubList.this.size)
                        throw new NoSuchElementException();
                    Object[] elementData = ArrayList.this.elementData;
                    if (offset + i >= elementData.length)
                        throw new ConcurrentModificationException();
                    cursor = i + 1;
                    return (E) elementData[offset + (lastRet = i)];
                }

                public boolean hasPrevious() {
                    return cursor != 0;
                }

                @SuppressWarnings("unchecked")
                public E previous() {
                    checkForComodification();
                    int i = cursor - 1;
                    if (i < 0)
                        throw new NoSuchElementException();
                    Object[] elementData = ArrayList.this.elementData;
                    if (offset + i >= elementData.length)
                        throw new ConcurrentModificationException();
                    cursor = i;
                    return (E) elementData[offset + (lastRet = i)];
                }

                @SuppressWarnings("unchecked")
                public void forEachRemaining(Consumer<? super E> consumer) {
                    Objects.requireNonNull(consumer);
                    final int size = SubList.this.size;
                    int i = cursor;
                    if (i >= size) {
                        return;
                    }
                    final Object[] elementData = ArrayList.this.elementData;
                    if (offset + i >= elementData.length) {
                        throw new ConcurrentModificationException();
                    }
                    while (i != size && modCount == expectedModCount) {
                        consumer.accept((E) elementData[offset + (i++)]);
                    }
                    // update once at end of iteration to reduce heap write traffic
                    lastRet = cursor = i;
                    checkForComodification();
                }

                public int nextIndex() {
                    return cursor;
                }

                public int previousIndex() {
                    return cursor - 1;
                }

                public void remove() {
                    if (lastRet < 0)
                        throw new IllegalStateException();
                    checkForComodification();

                    try {
                        SubList.this.remove(lastRet);
                        cursor = lastRet;
                        lastRet = -1;
                        expectedModCount = ArrayList.this.modCount;
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                public void set(E e) {
                    if (lastRet < 0)
                        throw new IllegalStateException();
                    checkForComodification();

                    try {
                        ArrayList.this.set(offset + lastRet, e);
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                public void add(E e) {
                    checkForComodification();

                    try {
                        int i = cursor;
                        SubList.this.add(i, e);
                        cursor = i + 1;
                        lastRet = -1;
                        expectedModCount = ArrayList.this.modCount;
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                final void checkForComodification() {
                    if (expectedModCount != ArrayList.this.modCount)
                        throw new ConcurrentModificationException();
                }
            };
        }

        public List<E> subList(int fromIndex, int toIndex) {
            subListRangeCheck(fromIndex, toIndex, size);
            return new SubList(this, offset, fromIndex, toIndex);
        }

        //索引是否超出范围
        private void rangeCheck(int index) {
            if (index < 0 || index >= this.size)
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }

        private void rangeCheckForAdd(int index) {
            if (index < 0 || index > this.size)
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }

        private String outOfBoundsMsg(int index) {
            return "Index: "+index+", Size: "+this.size;
        }

        //fail-fast机制
        private void checkForComodification() {
            if (ArrayList.this.modCount != this.modCount)
                throw new ConcurrentModificationException();
        }

        public Spliterator<E> spliterator() {
            checkForComodification();
            return new ArrayListSpliterator<E>(ArrayList.this, offset,
                                               offset + this.size, this.modCount);
        }
    }

    //lambda:list1.forEach(l-> System.out.println(l));
    @Override
    public void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        final int expectedModCount = modCount;
        @SuppressWarnings("unchecked")
        final E[] elementData = (E[]) this.elementData;
        final int size = this.size;
        for (int i=0; modCount == expectedModCount && i < size; i++) {
            action.accept(elementData[i]);
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }

    /**
     *可分割迭代器,Spliterator可以拆分成多份去遍历,有点像二分法,每次把某个Spliterator平均分成两份,但是改的只是下标
     * @return a {@code Spliterator} over the elements in this list
     * @since 1.8
     */
    @Override
    public Spliterator<E> spliterator() {
        return new ArrayListSpliterator<>(this, 0, -1, 0);
    }

    /** Index-based split-by-two, lazily initialized Spliterator */
    //第一个方法tryAdvance就是顺序处理每个元素，类似Iterator，如果还有元素要处理，则返回true，否则返回false
    //第二个方法trySplit，这就是为Spliterator专门设计的方法，区分与普通的Iterator，该方法会把当前元素划分一部分出去创建一个新的Spliterator作为返回，两个Spliterator变会并行执行，如果元素个数小到无法划分则返回null
    //第三个方法estimateSize，该方法用于估算还剩下多少个元素需要遍历
    //第四个方法characteristics，其实就是表示该Spliterator有哪些特性，用于可以更好控制和优化Spliterator的使用，具体属性你可以随便百度到，这里就不再赘言
    static final class ArrayListSpliterator<E> implements Spliterator<E> {

        //用于存放ArrayList对象
        private final ArrayList<E> list;
        //起始位置（包含），advance/split操作时会修改
        private int index;
        //结束位置（不包含），-1 表示到最后一个元素
        private int fence;
        //用于存放list的modCount
        private int expectedModCount;

        /** Create new spliterator covering the given  range */
        ArrayListSpliterator(ArrayList<E> list, int origin, int fence,
                             int expectedModCount) {
            this.list = list; // OK if null unless traversed
            this.index = origin;
            this.fence = fence;
            this.expectedModCount = expectedModCount;
        }

        //获取结束位置（存在意义：首次初始化石需对fence和expectedModCount进行赋值）
        private int getFence() { // initialize fence to size on first use
            int hi; // (a specialized variant appears in method forEach)
            ArrayList<E> lst;
            //fence<0时（第一次初始化时，fence才会小于0）：
            if ((hi = fence) < 0) {
                //list 为 null时，fence=0
                if ((lst = list) == null)
                    hi = fence = 0;
                else {
                    //否则，fence = list的长度。
                    expectedModCount = lst.modCount;
                    hi = fence = lst.size;
                }
            }
            return hi;
        }

        //对任务分割，返回一个新的Spliterator迭代器
        public ArrayListSpliterator<E> trySplit() {
            //hi为当前的结束位置
            //lo 为起始位置
            //计算中间的位置
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            //当lo>=mid,表示不能在分割，返回null
            //当lo<mid时,可分割，切割（lo，mid）出去，同时更新index=mid
            return (lo >= mid) ? null :
                new ArrayListSpliterator<E>(list, lo, index = mid,
                                            expectedModCount);
        }

        //单个对元素执行给定的动作，如果有剩下元素未处理返回true，否则返回false
        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null)
                throw new NullPointerException();
            //hi为当前的结束位置
            //i 为起始位置
            int hi = getFence(), i = index;
            //还有剩余元素未处理时
            if (i < hi) {
                //处理i位置，index+1
                index = i + 1;
                @SuppressWarnings("unchecked") E e = (E)list.elementData[i];
                action.accept(e);
                //遍历时，结构发生变更，抛错
                if (list.modCount != expectedModCount)
                    throw new ConcurrentModificationException();
                return true;
            }
            return false;
        }

        //对每个剩余元素执行给定的动作，依次处理，直到所有元素已被处理或被异常终止。默认方法调用tryAdvance方法
        public void forEachRemaining(Consumer<? super E> action) {
            int i, hi, mc; // hoist accesses and checks from loop
            ArrayList<E> lst; Object[] a;
            if (action == null)
                throw new NullPointerException();
            if ((lst = list) != null && (a = lst.elementData) != null) {
                //当fence<0时，表示fence和expectedModCount未初始化
                if ((hi = fence) < 0) {
                    mc = lst.modCount;
                    hi = lst.size;
                }
                else
                    mc = expectedModCount;
                if ((i = index) >= 0 && (index = hi) <= a.length) {
                    for (; i < hi; ++i) {
                        @SuppressWarnings("unchecked") E e = (E) a[i];
                        //调用action.accept处理元素
                        action.accept(e);
                    }
                    //遍历时发生结构变更时抛出异常  不相等时向下执行,抛出异常
                    if (lst.modCount == mc)
                        return;
                }
            }
            throw new ConcurrentModificationException();
        }

        //用于估算还剩下多少个元素需要遍历
        public long estimateSize() {
            return (long) (getFence() - index);
        }

        //返回当前对象有哪些特征值
        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
    }

    /**
     * 按照一定规则过滤集合中的元素
     * @param filter
     * @return
     */
    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        Objects.requireNonNull(filter);
        // 找出要删除的元素
        // 在此阶段从过滤谓词抛出的任何异常
        // 将保持集合不被修改
        int removeCount = 0;
        final BitSet removeSet = new BitSet(size);
        final int expectedModCount = modCount;
        final int size = this.size;
        for (int i=0; modCount == expectedModCount && i < size; i++) {
            @SuppressWarnings("unchecked")
            final E element = (E) elementData[i];
            //满足filter的
            if (filter.test(element)) {
                removeSet.set(i);
                removeCount++;
            }
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }

        // shift surviving elements left over the spaces left by removed elements
        final boolean anyToRemove = removeCount > 0;
        if (anyToRemove) {
            final int newSize = size - removeCount;
            for (int i=0, j=0; (i < size) && (j < newSize); i++, j++) {
                i = removeSet.nextClearBit(i);
                elementData[j] = elementData[i];
            }
            for (int k=newSize; k < size; k++) {
                elementData[k] = null;  // Let gc do its work
            }
            this.size = newSize;
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            modCount++;
        }

        return anyToRemove;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void replaceAll(UnaryOperator<E> operator) {
        Objects.requireNonNull(operator);
        final int expectedModCount = modCount;
        final int size = this.size;
        for (int i=0; modCount == expectedModCount && i < size; i++) {
            elementData[i] = operator.apply((E) elementData[i]);
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        modCount++;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void sort(Comparator<? super E> c) {
        final int expectedModCount = modCount;
        Arrays.sort((E[]) elementData, 0, size, c);
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        modCount++;
    }
}
