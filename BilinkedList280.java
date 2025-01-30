package lib280.list;


import lib280.base.BilinearIterator280;
import lib280.base.CursorPosition280;
import lib280.base.Pair280;
import lib280.exception.*;

/**	This list class incorporates the functions of an iterated 
	dictionary such as has, obtain, search, goFirst, goForth, 
	deleteItem, etc.  It also has the capabilities to iterate backwards 
	in the list, goLast and goBack. */
public class BilinkedList280<I> extends LinkedList280<I> implements BilinearIterator280<I>
{
	/* 	Note that because firstRemainder() and remainder() should not cut links of the original list,
		the previous node reference of firstNode is not always correct.
		Also, the instance variable prev is generally kept up to date, but may not always be correct.  
		Use previousNode() instead! */

	/**	Construct an empty list.
		Analysis: Time = O(1) */
	public BilinkedList280()
	{
		super();
		this.tail = null;
	}

	/**
	 * Create a BilinkedNode280 this Bilinked list.  This routine should be
	 * overridden for classes that extend this class that need a specialized node.
	 * @param item - element to store in the new node
	 * @return a new node containing item
	 */
	protected BilinkedNode280<I> createNewNode(I item)
	{
		// TODO
		return new BilinkedNode280<I>(item);
	}

	/**
	 * Insert element at the beginning of the list
	 * @param x item to be inserted at the beginning of the list 
	 */
	public void insertFirst(I x) 
	{
		// TODO
		BilinkedNode280<I> temp = createNewNode(x);
		temp.setNextNode(this.head);
		if(!this.isEmpty()){
			((BilinkedNode280<I>) this.head).setPreviousNode(temp);
		}
		if(this.tail == null) {
			this.tail = temp;
		}
		this.head = temp;
	}

	/**
	 * Insert element at the beginning of the list
	 * @param x item to be inserted at the beginning of the list 
	 */
	public void insert(I x) 
	{
		this.insertFirst(x);
	}

	/**
	 * Insert an item before the current position.
	 * @param x - The item to be inserted.
	 */
	public void insertBefore(I x) throws InvalidState280Exception {
		if( this.before() ) throw new InvalidState280Exception("Cannot insertBefore() when the cursor is already before the first element.");
		
		// If the item goes at the beginning or the end, handle those special cases.
		if( this.head == position ) {
			insertFirst(x);  // special case - inserting before first element
		}
		else if( this.after() ) {
			insertLast(x);   // special case - inserting at the end
		}
		else {
			// Otherwise, insert the node between the current position and the previous position.
			BilinkedNode280<I> newNode = createNewNode(x);
			newNode.setNextNode(position);
			newNode.setPreviousNode((BilinkedNode280<I>)this.prevPosition);
			prevPosition.setNextNode(newNode);
			((BilinkedNode280<I>)this.position).setPreviousNode(newNode);
			
			// since position didn't change, but we changed it's predecessor, prevPosition needs to be updated to be the new previous node.
			prevPosition = newNode;			
		}
	}
	
	
	/**	Insert x before the current position and make it current item. <br>
		Analysis: Time = O(1)
		@param x item to be inserted before the current position */
	public void insertPriorGo(I x) 
	{
		this.insertBefore(x);
		this.goBack();
	}

	/**	Insert x after the current item. <br>
		Analysis: Time = O(1) 
		@param x item to be inserted after the current position */
	public void insertNext(I x) 
	{
		if (isEmpty() || before())
			insertFirst(x); 
		else if (this.position==lastNode())
			insertLast(x); 
		else if (after()) // if after then have to deal with previous node  
		{
			insertLast(x); 
			this.position = this.prevPosition.nextNode();
		}
		else // in the list, so create a node and set the pointers to the new node 
		{
			BilinkedNode280<I> temp = createNewNode(x);
			temp.setNextNode(this.position.nextNode());
			temp.setPreviousNode((BilinkedNode280<I>)this.position);
			((BilinkedNode280<I>) this.position.nextNode()).setPreviousNode(temp);
			this.position.setNextNode(temp);
		}
	}

	/**
	 * Insert a new element at the end of the list
	 * @param x item to be inserted at the end of the list 
	 */
	public void insertLast(I x) 
	{
		// TODO
		if(isEmpty())
			insertFirst(x);
		else {
			BilinkedNode280<I> temp = createNewNode(x);
			temp.setPreviousNode((BilinkedNode280<I>) this.tail);
			this.tail.setNextNode(temp);
			this.tail = temp;
		}

	}

	/**
	 * Delete the item at which the cursor is positioned
	 * @precond itemExists() must be true (the cursor must be positioned at some element)
	 */
	public void deleteItem() throws NoCurrentItem280Exception
	{
		// TODO
		if(this.position == null)
			throw new NoCurrentItem280Exception();
		if(this.position == this.head)
		{
			this.deleteFirst();
		}
		else if(this.position == this.tail)
		{
			this.deleteLast();
		}
		else
		{
			this.prevPosition.nextNode = this.position.nextNode();
			this.position = this.position.nextNode();
			((BilinkedNode280<I>)this.position).setPreviousNode((BilinkedNode280<I>)this.prevPosition);

		}

	}

	
	@Override
	public void delete(I x) throws ItemNotFound280Exception {
		if( this.isEmpty() ) throw new ContainerEmpty280Exception("Cannot delete from an empty list.");

		// Save cursor position
		LinkedIterator280<I> savePos = this.currentPosition();
		
		// Find the item to be deleted.
		search(x);
		if( !this.itemExists() ) throw new ItemNotFound280Exception("Item to be deleted wasn't in the list.");

		// If we are about to delete the item that the cursor was pointing at,
		// advance the cursor in the saved position, but leave the predecessor where
		// it is because it will remain the predecessor.
		if( this.position == savePos.cur ) savePos.cur = savePos.cur.nextNode();
		
		// If we are about to delete the predecessor to the cursor, the predecessor 
		// must be moved back one item.
		if( this.position == savePos.prev ) {
			
			// If savePos.prev is the first node, then the first node is being deleted
			// and savePos.prev has to be null.
			if( savePos.prev == this.head ) savePos.prev = null;
			else {
				// Otherwise, Find the node preceding savePos.prev
				LinkedNode280<I> tmp = this.head;
				while(tmp.nextNode() != savePos.prev) tmp = tmp.nextNode();
				
				// Update the cursor position to be restored.
				savePos.prev = tmp;
			}
		}
				
		// Unlink the node to be deleted.
		if( this.prevPosition != null)
			// Set previous node to point to next node.
			// Only do this if the node we are deleting is not the first one.
			this.prevPosition.setNextNode(this.position.nextNode());
		
		if( this.position.nextNode() != null )
			// Set next node to point to previous node 
			// But only do this if we are not deleting the last node.
			((BilinkedNode280<I>)this.position.nextNode()).setPreviousNode(((BilinkedNode280<I>)this.position).previousNode());
		
		// If we deleted the first or last node (or both, in the case
		// that the list only contained one element), update head/tail.
		if( this.position == this.head ) this.head = this.head.nextNode();
		if( this.position == this.tail ) this.tail = this.prevPosition;
		
		// Clean up references in the node being deleted.
		this.position.setNextNode(null);
		((BilinkedNode280<I>)this.position).setPreviousNode(null);
		
		// Restore the old, possibly modified cursor.
		this.goPosition(savePos);
		
	}
	/**
	 * Remove the first item from the list.
	 * @precond !isEmpty() - the list cannot be empty
	 */
	public void deleteFirst() throws ContainerEmpty280Exception
	{
		// TODO
		if(this.isEmpty())
			throw new ContainerEmpty280Exception("Cannot delete from an empty list.");

		this.head = this.head.nextNode();
		this.prevPosition = null;

	}

	/**
	 * Remove the last item from the list.
	 * @precond !isEmpty() - the list cannot be empty
	 */
	public void deleteLast() throws ContainerEmpty280Exception
	{
		// TODO
		if(this.isEmpty())
			throw new ContainerEmpty280Exception("Cannot delete from an empty list.");
		if(this.tail == this.head)
		{
			this.head = null;
			this.tail = null;
		}
		else
		{
			this.tail = ((BilinkedNode280<I>)this.tail).previousNode();
			this.tail.setNextNode(null);
		}


	}

	
	/**
	 * Move the cursor to the last item in the list.
	 * @precond The list is not empty.
	 */
	public void goLast() throws ContainerEmpty280Exception
	{
		// TODO
		if(this.isEmpty()) {
			this.position = null;
			this.prevPosition = null;
		}
		else
		{
			this.prevPosition = ((BilinkedNode280<I>)this.tail).previousNode();
			this.position = this.tail;
		}





	}
  
	/**	Move back one item in the list. 
		Analysis: Time = O(1)
		@precond !before() 
	 */
	public void goBack() throws BeforeTheStart280Exception
	{
		// TODO
		if(this.before())
			throw new BeforeTheStart280Exception("Cannot go back further.");
		else if(this.position==this.head)
		{
			this.position = this.prevPosition;
			this.prevPosition = null;
		}
		else
		{
			this.position = this.prevPosition;
			this.prevPosition = ((BilinkedNode280<I>)this.position).previousNode();
		}



	}

	/**	Iterator for list initialized to first item. 
		Analysis: Time = O(1) 
	*/
	public BilinkedIterator280<I> iterator()
	{
		return new BilinkedIterator280<I>(this);
	}

	/**	Go to the position in the list specified by c. <br>
		Analysis: Time = O(1) 
		@param c position to which to go */
	@SuppressWarnings("unchecked")
	public void goPosition(CursorPosition280 c)
	{
		if (!(c instanceof BilinkedIterator280))
			throw new InvalidArgument280Exception("The cursor position parameter" 
					    + " must be a BilinkedIterator280<I>");
		BilinkedIterator280<I> lc = (BilinkedIterator280<I>) c;
		this.position = lc.cur;
		this.prevPosition = lc.prev;
	}

	/**	The current position in this list. 
		Analysis: Time = O(1) */
	public BilinkedIterator280<I> currentPosition()
	{
		return  new BilinkedIterator280<I>(this, this.prevPosition, this.position);
	}

	
  
	/**	A shallow clone of this object. 
		Analysis: Time = O(1) */
	public BilinkedList280<I> clone() throws CloneNotSupportedException
	{
		return (BilinkedList280<I>) super.clone();
	}


	/* Regression test. */
	public static void main(String[] args) {
		// TODO

		BilinkedList280<String> testList = new BilinkedList280<>();
		testList.insertFirst("wow");
		testList.insertFirst("but");
		testList.insertFirst("dsaw");
		testList.goLast();
		//testList.goBack();

		while(!testList.before()) {
			System.out.println(testList.item());
			testList.goBack();
		}
		testList.insertLast("Strong");
		testList.insertLast("Harwin");
		testList.insertLast("Strong");
		testList.insertLast("dsaw");
		testList.goLast();
		System.out.println("\nOne time, Kantutan na ignition");
		while(!testList.before()) {
			System.out.println(testList.item());
			testList.goBack();
		}
		testList.deleteLast();
		testList.deleteLast();
		testList.deleteLast();
		testList.goLast();
		System.out.println("\nOne more time, Kantutan na ignition");
		while(!testList.before()) {
			System.out.println(testList.item());
			testList.goBack();
		}
		testList.deleteFirst();
		testList.deleteFirst();
		System.out.println("Tail after three deleteLast: "+testList.tail.item());
		System.out.println("Head after two deleteFirst: "+testList.head.item());

		BilinkedList280<Integer> testList2 = new BilinkedList280<>();
		testList2.insertLast(1);
		testList2.insertLast(2);
		testList2.insertLast(3);
		testList2.insertLast(1);
		testList2.goFirst();
		System.out.println(testList2.item());
		testList2.goLast();
		System.out.println(testList2.item());
		testList2.deleteLast();
		System.out.println(testList2.isEmpty());
		System.out.println(testList2.tail.item());

		BilinkedList280<Double> testList3 = new BilinkedList280<>();
		testList3.insertLast(1.1);
		testList3.insertLast(2.2);
		testList3.insertLast(3.1);
		testList3.insertLast(1.1);
		testList3.insertLast(123.99);
		testList3.goFirst();
		System.out.println(testList3.item());
		testList3.goForth();
		System.out.println(testList3.item());
		testList3.goForth();
		testList3.deleteItem();
		testList3.goForth();
		System.out.println(testList3.item());
		testList3.goBack();
		System.out.println(testList3.item());



	}
} 
