/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

/**
 * LinkedList是List和Deque接口的双向链表的实现。实现了所有可选List操作，并允许包括null值。
 * LinkedList既然是通过双向链表去实现的，那么它可以被当作堆栈、队列或双端队列进行操作。并且其顺序访问非常高效，而随机访问效率比较低。
 * 内部方法，注释会描述为节点的操作(如删除第一个节点)，公开的方法会描述为元素的操作(如删除第一个元素)
 * 注意，此实现不是同步的。 如果多个线程同时访问一个LinkedList实例，而其中至少一个线程从结构上修改了列表，那么它必须保持外部同步。
 * LinkedList不是线程安全的，如果在多线程中使用（修改），需要在外部作同步处理。
 * 这通常是通过同步那些用来封装列表的对象来实现的。
 * 但如果没有这样的对象存在，则该列表需要运用{@link Collections#synchronizedList Collections.synchronizedList}来进行“包装”，该方法最好是在创建列表对象时完成，为了避免对列表进行突发的非同步操作。
 */

public class LinkedList<E>
    extends AbstractSequentialList<E>
    implements List<E>, Deque<E>, Cloneable, java.io.Serializable
{
    //元素数量
    transient int size = 0;

    /**
     * 首结点引用
     * Invariant: (first == null && last == null) ||
     *            (first.prev == null && first.item != null)
     */
    transient Node<E> first;

    /**
     * 尾节点引用
     * Invariant: (first == null && last == null) ||
     *            (last.next == null && last.item != null)
     */
    transient Node<E> last;

    /**
     * 无参构造方法
     */
    public LinkedList() {
    }

    /**
     * 通过一个集合初始化LinkedList，元素顺序有这个集合的迭代器返回顺序决定
     * @param  c 其元素将被放入此列表中的集合
     * @throws NullPointerException if the specified collection is null
     */
    public LinkedList(Collection<? extends E> c) {
        this();
        addAll(c);
    }

    /**
     * 将节点值为e的节点设置为链表首节点
     */
    private void linkFirst(E e) {
        //当前节点
        final Node<E> f = first;
        //创建新节点,前节点为空,后节点为f
        final Node<E> newNode = new Node<>(null, e, f);
        first = newNode;
        //空时,头尾节点都指向e
        if (f == null)
            last = newNode;
        else
            //f的前节点为新
            f.prev = newNode;
        //元素数量+1
        size++;
        //已从结构上修改此列表的次数+1
        modCount++;
    }

    /**
     * 将节点值为e的节点设置为链表尾节点
     * 同理
     */
    void linkLast(E e) {
        final Node<E> l = last;
        final Node<E> newNode = new Node<>(l, e, null);
        last = newNode;
        if (l == null)
            first = newNode;
        else
            l.next = newNode;
        size++;
        //已从结构上修改此列表的次数+1
        modCount++;
    }

    /**
     * 中间插入，在非空节点succ之前插入节点值e
     */
    void linkBefore(E e, Node<E> succ) {
        // assert succ != null;
        //succ前节点
        final Node<E> pred = succ.prev;
        //新节点,前节点为pred,后节点为succ
        final Node<E> newNode = new Node<>(pred, e, succ);
        //前后节点和新节点关联
        succ.prev = newNode;
        if (pred == null)
            first = newNode;
        else
            pred.next = newNode;
        size++;
        //已从结构上修改此列表的次数+1
        modCount++;
    }

    /**
     * 删除首结点，返回存储的元素
     */
    private E unlinkFirst(Node<E> f) {
        // assert f == first && f != null;
        final E element = f.item;
        final Node<E> next = f.next;
        f.item = null;
        f.next = null; // help GC
        first = next;
        if (next == null)
            last = null;
        else
            next.prev = null;
        size--;
        //已从结构上修改此列表的次数+1
        modCount++;
        return element;
    }

    /**
     * 删除尾结点，返回存储的元素
     */
    private E unlinkLast(Node<E> l) {
        // assert l == last && l != null;
        final E element = l.item;
        final Node<E> prev = l.prev;
        l.item = null;
        l.prev = null; // help GC
        last = prev;
        if (prev == null)
            first = null;
        else
            prev.next = null;
        size--;
        //已从结构上修改此列表的次数+1
        modCount++;
        return element;
    }

    /**
     * 删除指定非空结点，返回存储的元素
     */
    E unlink(Node<E> x) {
        // assert x != null;
        final E element = x.item;
        final Node<E> next = x.next;
        final Node<E> prev = x.prev;

        if (prev == null) {
            first = next;
        } else {
            prev.next = next;
            x.prev = null;
        }

        if (next == null) {
            last = prev;
        } else {
            next.prev = prev;
            x.next = null;
        }

        x.item = null;
        size--;
        //已从结构上修改此列表的次数+1
        modCount++;
        return element;
    }

    /**
     * 获取首结点存储的元素
     *
     * @return the first element in this list
     * @throws NoSuchElementException if this list is empty
     */
    public E getFirst() {
        final Node<E> f = first;
        if (f == null)
            throw new NoSuchElementException();
        return f.item;
    }

    /**
     * Returns 获取尾结点存储的元素
     *
     * @return the last element in this list
     * @throws NoSuchElementException if this list is empty
     */
    public E getLast() {
        final Node<E> l = last;
        if (l == null)
            throw new NoSuchElementException();
        return l.item;
    }

    /**
     * 删除首节点，返回存储的元素
     *
     * @return the first element from this list
     * @throws NoSuchElementException if this list is empty
     */
    public E removeFirst() {
        final Node<E> f = first;
        if (f == null)
            throw new NoSuchElementException();
        //调用删除首结点方法
        return unlinkFirst(f);
    }

    /**
     * 删除尾结点，返回存储的元素
     *
     * @return the last element from this list
     * @throws NoSuchElementException if this list is empty
     */
    public E removeLast() {
        final Node<E> l = last;
        if (l == null)
            throw new NoSuchElementException();
        return unlinkLast(l);
    }

    /**
     * 头部插入指定元素
     *
     * @param e the element to add
     */
    public void addFirst(E e) {
        linkFirst(e);
    }

    /**
     * 尾部插入指定元素，该方法等价于add()
     * @param e the element to add
     */
    public void addLast(E e) {
        linkLast(e);
    }

    /**
     *尾部插入指定元素，该方法等价于add()
     * @param o 列表中要判断的元素
     * @return {@code true} if this list contains the specified element
     */
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    /**
     * 获取元素数量
     *
     * @return the number of elements in this list
     */
    public int size() {
        return size;
    }

    /**
     *插入指定元素，返回操作结果,默认添加到末尾作为最后一个元素
     * <p>This method is equivalent to {@link #addLast}.
     *
     * @param e 要添加的元素
     * @return {@code true} (as specified by {@link Collection#add})
     */
    public boolean add(E e) {
        linkLast(e);
        return true;
    }

    /**
     * 删除指定元素，默认从first节点开始，删除第一次出现的那个元素
     * @param o element to be removed from this list, if present
     * @return {@code true} if this list contained the specified element
     */
    public boolean remove(Object o) {
        //空
        if (o == null) {
            //遍历链表
            for (Node<E> x = first; x != null; x = x.next) {
                //删除值为null的元素
                if (x.item == null) {
                    unlink(x);
                    return true;
                }
            }
        }
        //非空
        else {
            for (Node<E> x = first; x != null; x = x.next) {
                if (o.equals(x.item)) {
                    unlink(x);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *将集合插入到链表尾部
     * @param c collection containing elements to be added to this list
     * @return {@code true} if this list changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     */
    public boolean addAll(Collection<? extends E> c) {
        return addAll(size, c);
    }

    /**
     * 指定索引位置之后插入集合
     * @param index index at which to insert the first element
     *              from the specified collection
     * @param c collection containing elements to be added to this list
     * @return {@code true} if this list changed as a result of the call
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws NullPointerException if the specified collection is null
     */
    public boolean addAll(int index, Collection<? extends E> c) {
        //检查位置索引 0<= index <=size
        checkPositionIndex(index);

        //要插入的集合c转换为数组
        Object[] a = c.toArray();
        //集合长度
        int numNew = a.length;
        //长度为0插入失败
        if (numNew == 0)
            return false;

        //succ:要插入位置节点;pred:插入位置的前一个节点
        Node<E> pred, succ;
        if (index == size) {
            succ = null;
            pred = last;
        }
        //中间插入
        else {
            //获取指定索引的节点
            succ = node(index);
            pred = succ.prev;
        }

        //遍历目标集合
        for (Object o : a) {
            //强制类型转换
            @SuppressWarnings("unchecked") E e = (E) o;
            //创建新节点,前一个节点是pred
            Node<E> newNode = new Node<>(pred, e, null);
            if (pred == null)
                first = newNode;
            else
                pred.next = newNode;
            pred = newNode;
        }

        //尾部插入,把最后一个节点设置为尾节点
        if (succ == null) {
            last = pred;
        }
        //如果中间插入,把后面一部分接上
        else {
            pred.next = succ;
            succ.prev = pred;
        }

        //数量更新
        size += numNew;
        //已从结构上修改此列表的次数+1
        modCount++;
        return true;
    }

    /**
     * 删除所有元素
     */
    public void clear() {
        // Clearing all of the links between nodes is "unnecessary", but:
        // - helps a generational GC if the discarded nodes inhabit
        //   more than one generation
        // - is sure to free memory even if there is a reachable Iterator
        for (Node<E> x = first; x != null; ) {
            Node<E> next = x.next;
            x.item = null;
            x.next = null;
            x.prev = null;
            x = next;
        }
        first = last = null;
        size = 0;
        //已从结构上修改此列表的次数+1
        modCount++;
    }


    // 位置访问操作

    /**
     * 返回特定索引的元素
     *
     * @param index index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E get(int index) {
        //检查元素索引 0<= index <size
        checkElementIndex(index);
        //获取指定索引的节点的值
        return node(index).item;
    }

    /**
     * 修改指定位置的元素，返回之前元素
     * @param index 要替换的位置索引
     * @param element 要替换的值
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E set(int index, E element) {
        //检查元素索引 0<= index <size
        checkElementIndex(index);
        ////获取指定索引的节点
        Node<E> x = node(index);
        //旧值
        E oldVal = x.item;
        //替换新值
        x.item = element;
        return oldVal;
    }

    /**
     * 指定索引位置插入元素
     * @param index index at which the specified element is to be inserted
     * @param element element to be inserted
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public void add(int index, E element) {
        //检查位置索引 0<= index <=size
        checkPositionIndex(index);

        //索引等于元素数量,在最后插入
        if (index == size)
            linkLast(element);
        //在索引节点之前插入
        else
            linkBefore(element, node(index));
    }

    /**
     * 删除指定索引位置的元素
     * @param index the index of the element to be removed
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E remove(int index) {
        //检查元素索引 0<= index <size
        checkElementIndex(index);
        return unlink(node(index));
    }

    /**
     * 判断索引是否有效 0<= index <size
     */
    private boolean isElementIndex(int index) {
        return index >= 0 && index < size;
    }

    /**
     * 判断索引是否有效 0<= index <=size
     */
    private boolean isPositionIndex(int index) {
        return index >= 0 && index <= size;
    }

    /**
     * 下标越界提示信息
     */
    private String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+size;
    }

    //判断索引是否有效 0<= index <size
    private void checkElementIndex(int index) {
        if (!isElementIndex(index))
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    //检查索引 0<= index <=size
    private void checkPositionIndex(int index) {
        if (!isPositionIndex(index))
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    /**
     * 获取指定索引的节点
     */
    Node<E> node(int index) {
        // assert isElementIndex(index);

        //索引在前一半,从前往后遍历
        if (index < (size >> 1)) {
            Node<E> x = first;
            for (int i = 0; i < index; i++)
                x = x.next;
            return x;
        }
        //在后一半,从后往前遍历
        else {
            Node<E> x = last;
            for (int i = size - 1; i > index; i--)
                x = x.prev;
            return x;
        }
    }

    // 查询操作

    /**
     * 返回获取顺序下首次出现元素的索引 -1表示没有该元素
     * @param o 要搜索的元素
     * @return the index of the first occurrence of the specified element in
     *         this list, or -1 if this list does not contain the element
     */
    public int indexOf(Object o) {
        int index = 0;
        //查找空元素
        if (o == null) {
            //遍历列表
            for (Node<E> x = first; x != null; x = x.next) {
                //为空返回
                if (x.item == null)
                    return index;
                //索引+1
                index++;
            }
        }
        //非空
        else {
            for (Node<E> x = first; x != null; x = x.next) {
                if (o.equals(x.item))
                    return index;
                index++;
            }
        }
        return -1;
    }

    /**
     *获取逆序下首次出现指定元素的位置
     * @param o element to search for
     * @return the index of the last occurrence of the specified element in
     *         this list, or -1 if this list does not contain the element
     */
    public int lastIndexOf(Object o) {
        int index = size;
        if (o == null) {
            for (Node<E> x = last; x != null; x = x.prev) {
                index--;
                if (x.item == null)
                    return index;
            }
        } else {
            for (Node<E> x = last; x != null; x = x.prev) {
                index--;
                if (o.equals(x.item))
                    return index;
            }
        }
        return -1;
    }

    // 队列操作

    /**
     * 出队（从前端），获得第一个元素，不存在会返回null，不会删除元素（节点）
     * 获取首元素
     * @return the head of this list, or {@code null} if this list is empty
     * @since 1.5
     */
    public E peek() {
        final Node<E> f = first;
        return (f == null) ? null : f.item;
    }

    /**
     * 出队（从前端），不删除元素，若为null会抛出异常而不是返回null
     * 获取首元素
     * @return the head of this list
     * @throws NoSuchElementException if this list is empty
     * @since 1.5
     */
    public E element() {
        return getFirst();
    }

    /**
     * 出队（从前端），如果不存在会返回null，存在的话会返回值并移除这个元素（节点）
     * 获取并删除首元素
     * @return the head of this list, or {@code null} if this list is empty
     * @since 1.5
     */
    public E poll() {
        final Node<E> f = first;
        return (f == null) ? null : unlinkFirst(f);
    }

    /**
     * 出队（从前端），如果不存在会抛出异常而不是返回null，存在的话会返回值并移除这个元素（节点）
     * 获取并删除首元素
     * @return the head of this list
     * @throws NoSuchElementException if this list is empty
     * @since 1.5
     */
    public E remove() {
        return removeFirst();
    }

    /**
     * 入队（从后端），始终返回true
     * @param e the element to add
     * @return {@code true} (as specified by {@link Queue#offer})
     * @since 1.5
     */
    public boolean offer(E e) {
        return add(e);
    }

    // 双端队列操作
    /**
     * 入队（从前端），始终返回true
     * @param e the element to insert
     * @return {@code true} (as specified by {@link Deque#offerFirst})
     * @since 1.6
     */
    public boolean offerFirst(E e) {
        addFirst(e);
        return true;
    }

    /**
     * 入队（从后端），始终返回true
     * @param e the element to insert
     * @return {@code true} (as specified by {@link Deque#offerLast})
     * @since 1.6
     */
    public boolean offerLast(E e) {
        addLast(e);
        return true;
    }

    /**
     * 出队（从后端），获得最后一个元素，不存在会返回null，不会删除元素（节点）
     * @return the first element of this list, or {@code null}
     *         if this list is empty
     * @since 1.6
     */
    public E peekFirst() {
        final Node<E> f = first;
        return (f == null) ? null : f.item;
     }

    /**
     * 出队（从后端），获得最后一个元素，不存在会返回null，不会删除元素（节点）
     * @return the last element of this list, or {@code null}
     *         if this list is empty
     * @since 1.6
     */
    public E peekLast() {
        final Node<E> l = last;
        return (l == null) ? null : l.item;
    }

    /**
     * 出队（从前端），获得第一个元素，不存在会返回null，会删除元素（节点）
     * @return the first element of this list, or {@code null} if
     *     this list is empty
     * @since 1.6
     */
    public E pollFirst() {
        final Node<E> f = first;
        return (f == null) ? null : unlinkFirst(f);
    }

    /**
     * 出队（从后端），获得最后一个元素，不存在会返回null，会删除元素（节点）
     * @return the last element of this list, or {@code null} if
     *     this list is empty
     * @since 1.6
     */
    public E pollLast() {
        final Node<E> l = last;
        return (l == null) ? null : unlinkLast(l);
    }

    //栈操作
    /**
     * 入栈，从前面添加
     * <p>This method is equivalent to {@link #addFirst}.
     *
     * @param e the element to push
     * @since 1.6
     */
    public void push(E e) {
        addFirst(e);
    }

    /**
     * 出栈，返回栈顶元素，从前面移除（会删除）
     * <p>This method is equivalent to {@link #removeFirst()}.
     *
     * @return the element at the front of this list (which is the top
     *         of the stack represented by this list)
     * @throws NoSuchElementException if this list is empty
     * @since 1.6
     */
    public E pop() {
        return removeFirst();
    }

    /**
     * 删除顺序下首次出现的指定元素，返回操作结果
     * @param o element to be removed from this list, if present
     * @return {@code true} if the list contained the specified element
     * @since 1.6
     */
    public boolean removeFirstOccurrence(Object o) {
        return remove(o);
    }

    /**
     * 删除逆序下首次出现的指定元素，返回操作结果
     * @param o element to be removed from this list, if present
     * @return {@code true} if the list contained the specified element
     * @since 1.6
     */
    public boolean removeLastOccurrence(Object o) {
        if (o == null) {
            for (Node<E> x = last; x != null; x = x.prev) {
                if (x.item == null) {
                    unlink(x);
                    return true;
                }
            }
        } else {
            for (Node<E> x = last; x != null; x = x.prev) {
                if (o.equals(x.item)) {
                    unlink(x);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *  双向遍历迭代器 从指定位置开始 快速失败
     * @param index index of the first element to be returned from the
     *              list-iterator (by a call to {@code next})
     * @return a ListIterator of the elements in this list (in proper
     *         sequence), starting at the specified position in the list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @see List#listIterator(int)
     */
    public ListIterator<E> listIterator(int index) {
        //检查索引 0<= index <=size
        checkPositionIndex(index);
        //创建迭代器
        return new ListItr(index);
    }

    private class ListItr implements ListIterator<E> {
        //上次访问的元素
        private Node<E> lastReturned;
        //下一个元素
        private Node<E> next;
        //下一个索引
        private int nextIndex;
        //修改次数
        private int expectedModCount = modCount;

        ListItr(int index) {
            // assert isPositionIndex(index);
            //下个元素
            next = (index == size) ? null : node(index);
            //下个索引
            nextIndex = index;
        }

        public boolean hasNext() {
            return nextIndex < size;
        }

        //下一个
        public E next() {
            //fail-fast检查
            checkForComodification();
            if (!hasNext())
                throw new NoSuchElementException();
            //上次访问
            lastReturned = next;
            //下个
            next = next.next;
            nextIndex++;
            return lastReturned.item;
        }

        //是否有前一个
        public boolean hasPrevious() {
            return nextIndex > 0;
        }

        //前一个
        public E previous() {
            //fail-fast检查
            checkForComodification();
            if (!hasPrevious())
                throw new NoSuchElementException();

            lastReturned = next = (next == null) ? last : next.prev;
            nextIndex--;
            return lastReturned.item;
        }

        public int nextIndex() {
            return nextIndex;
        }

        public int previousIndex() {
            return nextIndex - 1;
        }

        public void remove() {
            checkForComodification();
            if (lastReturned == null)
                throw new IllegalStateException();

            Node<E> lastNext = lastReturned.next;
            unlink(lastReturned);
            //尾
            if (next == lastReturned)
                next = lastNext;
            //没到尾
            else
                nextIndex--;
            lastReturned = null;
            expectedModCount++;
        }

        public void set(E e) {
            if (lastReturned == null)
                throw new IllegalStateException();
            checkForComodification();
            lastReturned.item = e;
        }

        public void add(E e) {
            checkForComodification();
            lastReturned = null;
            if (next == null)
                linkLast(e);
            else
                linkBefore(e, next);
            nextIndex++;
            expectedModCount++;
        }

        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            while (modCount == expectedModCount && nextIndex < size) {
                action.accept(next.item);
                lastReturned = next;
                next = next.next;
                nextIndex++;
            }
            checkForComodification();
        }

        //已从结构上修改此列表的次数  fail-fast
        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    //结点
    private static class Node<E> {
        //存储的元素
        E item;
        //后节点
        Node<E> next;
        //前节点
        Node<E> prev;

        Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }

    /**
     * @since 1.6
     */
    public Iterator<E> descendingIterator() {
        return new DescendingIterator();
    }

    /**
     * 返回逆序的迭代器对象
     */
    private class DescendingIterator implements Iterator<E> {
        private final ListItr itr = new ListItr(size());
        public boolean hasNext() {
            return itr.hasPrevious();
        }
        public E next() {
            return itr.previous();
        }
        public void remove() {
            itr.remove();
        }
    }

    //拷贝
    @SuppressWarnings("unchecked")
    private LinkedList<E> superClone() {
        try {
            return (LinkedList<E>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    /**
     * 返回一个链表的拷贝，返回值为Object 类型
     * @return a shallow copy of this {@code LinkedList} instance
     */
    public Object clone() {
        LinkedList<E> clone = superClone();

        // Put clone into "virgin" state
        clone.first = clone.last = null;
        clone.size = 0;
        clone.modCount = 0;

        // Initialize clone with our elements
        for (Node<E> x = first; x != null; x = x.next)
            clone.add(x.item);

        return clone;
    }

    /**
     * 转换成数组,从头到尾
     * @return an array containing all of the elements in this list
     *         in proper sequence
     */
    public Object[] toArray() {
        Object[] result = new Object[size];
        int i = 0;
        for (Node<E> x = first; x != null; x = x.next)
            result[i++] = x.item;
        return result;
    }

    /**
     * 返回LinkedList的模板数组。所谓模板数组，即可以将T设为任意的数据类型
     * @param a the array into which the elements of the list are to
     *          be stored, if it is big enough; otherwise, a new array of the
     *          same runtime type is allocated for this purpose.
     * @return an array containing the elements of the list
     * @throws ArrayStoreException if the runtime type of the specified array
     *         is not a supertype of the runtime type of every element in
     *         this list
     * @throws NullPointerException if the specified array is null
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        //数组长度不够,则新建一个T[]数组，T[]的大小为LinkedList大小，并将该T[]赋值给a。
        if (a.length < size)
            a = (T[])java.lang.reflect.Array.newInstance(
                                a.getClass().getComponentType(), size);
        //将链表中所有节点的数据都添加到数组a中
        int i = 0;
        Object[] result = a;
        for (Node<E> x = first; x != null; x = x.next)
            result[i++] = x.item;

        if (a.length > size)
            a[size] = null;

        return a;
    }

    //序列化/反序列化
    private static final long serialVersionUID = 876323262645176354L;

    /**
     * 保存数组实例的状态到一个流(序列化)
     * @serialData The size of the list (the number of elements it
     *             contains) is emitted (int), followed by all of its
     *             elements (each an Object) in the proper order.
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        // Write out any hidden serialization magic
        s.defaultWriteObject();

        // Write out size
        s.writeInt(size);

        // Write out all elements in the proper order.
        for (Node<E> x = first; x != null; x = x.next)
            s.writeObject(x.item);
    }

    /**
     * 从流中重构ArrayList实例（即反序列化）。
     */
    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        // Read in any hidden serialization magic
        s.defaultReadObject();

        // Read in size
        int size = s.readInt();

        // Read in all elements in the proper order.
        for (int i = 0; i < size; i++)
            linkLast((E)s.readObject());
    }

    /**
     * 可分割迭代器,Spliterator可以拆分成多份去遍历,有点像二分法,每次把某个Spliterator平均分成两份,但是改的只是下标
     * @return a {@code Spliterator} over the elements in this list
     * @since 1.8
     */
    @Override
    public Spliterator<E> spliterator() {
        return new LLSpliterator<E>(this, -1, 0);
    }

    /** A customized variant of Spliterators.IteratorSpliterator */
    static final class LLSpliterator<E> implements Spliterator<E> {
        static final int BATCH_UNIT = 1 << 10;  // batch array size increment
        static final int MAX_BATCH = 1 << 25;  // max batch array size;
        final LinkedList<E> list; // null OK unless traversed
        Node<E> current;      // current node; null until initialized
        int est;              // size estimate; -1 until first needed
        int expectedModCount; // initialized when est set
        int batch;            // batch size for splits

        LLSpliterator(LinkedList<E> list, int est, int expectedModCount) {
            this.list = list;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        final int getEst() {
            int s; // force initialization
            final LinkedList<E> lst;
            if ((s = est) < 0) {
                if ((lst = list) == null)
                    s = est = 0;
                else {
                    expectedModCount = lst.modCount;
                    current = lst.first;
                    s = est = lst.size;
                }
            }
            return s;
        }

        public long estimateSize() { return (long) getEst(); }

        public Spliterator<E> trySplit() {
            Node<E> p;
            int s = getEst();
            if (s > 1 && (p = current) != null) {
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                Object[] a = new Object[n];
                int j = 0;
                do { a[j++] = p.item; } while ((p = p.next) != null && j < n);
                current = p;
                batch = j;
                est = s - j;
                return Spliterators.spliterator(a, 0, j, Spliterator.ORDERED);
            }
            return null;
        }

        public void forEachRemaining(Consumer<? super E> action) {
            Node<E> p; int n;
            if (action == null) throw new NullPointerException();
            if ((n = getEst()) > 0 && (p = current) != null) {
                current = null;
                est = 0;
                do {
                    E e = p.item;
                    p = p.next;
                    action.accept(e);
                } while (p != null && --n > 0);
            }
            if (list.modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            Node<E> p;
            if (action == null) throw new NullPointerException();
            if (getEst() > 0 && (p = current) != null) {
                --est;
                E e = p.item;
                current = p.next;
                action.accept(e);
                if (list.modCount != expectedModCount)
                    throw new ConcurrentModificationException();
                return true;
            }
            return false;
        }

        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
    }

}
