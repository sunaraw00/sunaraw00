package lib280.list;
/* LinkedList280.java
 * ---------------------------------------------
 * Copyright (c) 2010 University of Saskatchewan
 * All Rights Reserved
 * --------------------------------------------- */


import lib280.base.CursorPosition280;
import lib280.base.Pair280;
import lib280.exception.*;


public class LinkedList280<I> implements SimpleList280<I> {

	/**
	 * First node in the list, or null if the list is empty.
	 */
	protected LinkedNode280<I> head;
	
	/** 
	 * Last node in the list, or null if the list is empty.
	 */
	protected LinkedNode280<I> tail;
	
	/**
	 * Node at which the cursor is positioned.
	 */
	protected LinkedNode280<I> position;
	
	/**
	 * Node prior to the node at which the cursor is positioned. 
	 */
	protected LinkedNode280<I> prevPosition;

	/**
	 * Do searches continue or start anew?
	 */
	protected boolean continueSearch;

	
	/**
	 * Create an empty list.
	 */
	public LinkedList280() {
		head = null;
		tail = null;
		position = null;
		prevPosition = null;
	}


	/**
	 * Create a LinkedNode280 this linked list.  This routine should be
	 * overridden for classes that extend this class that need a specialized node.
	 * @param item - element to store in the new node
	 * @return a new node containing item
	 */
	protected LinkedNode280<I> createNewNode(I item)
	{
		return new LinkedNode280<I>(item);
	}


	/**
	 * Insert an element before the current cursor position.
	 * @param x - Element to be inserted.
	 * @precond itemExists()
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
			LinkedNode280<I> newNode = createNewNode(x);
			newNode.setNextNode(position);
			prevPosition.setNextNode(newNode);
			
			// since position didn't change, but we changed it's predecessor, prevPosition needs to be updated to be the new previous node.
			prevPosition = newNode;			
		}
	}
	
	
	
	/**
	 * Obtain the last node in the list.
	 * @precond !isEmpty()
	 * @return the last node in  he list.
	 * @throws ContainerEmpty280Exception if the list is empty.
	 */
	public LinkedNode280<I> lastNode() throws ContainerEmpty280Exception {
		if( this.isEmpty() ) throw new ContainerEmpty280Exception("Tried to get last node of an empty list.");
		return tail;
	}
	
	/**
	 * Obtainthe first node in the list.
	 * @precond !isEmpty()
	 * @return the first node in the list.
	 * @throws ContainerEmpty280Exception if the list is empty.
	 */
	public LinkedNode280<I> firstNode() throws ContainerEmpty280Exception {
		if( this.isEmpty() ) throw new ContainerEmpty280Exception("Tried to get first node of an empty list.");
		return head;
	}
	
	@Override
	public boolean has(I y) {
		// save cursor state
		CursorPosition280 savePos = this.currentPosition();
		
		// Search for y
		this.search(y);
		boolean result = itemExists();
		
		// Restore cursor state
		this.goPosition(savePos);
		
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean membershipEquals(I x, I y) {
		if ((x instanceof Comparable) && (y instanceof Comparable))
			return  0==((Comparable<I>)x).compareTo(y);
		else return x.equals(y);
	}

	@Override
	public void deleteItem() throws NoCurrentItem280Exception {
		if(!this.itemExists()) throw new NoCurrentItem280Exception("There is no item at the cursor to delete.");
		
		// If we are deleting the first item...
		if( this.position == this.head ) {
			// Handle the special case...
			this.deleteFirst();
			this.position = this.head;
		}
		else {
			// Set the previous node to point to the successor node. 
			this.prevPosition.setNextNode(this.position.nextNode());
			
			// Reset the tail reference if we deleted the last node.
			if( this.position == this.tail ) {
				this.tail = this.prevPosition;
			}
			this.position = this.position.nextNode();
		}
	}

	@Override
	public I item() throws NoCurrentItem280Exception {
		if( !itemExists() ) throw new NoCurrentItem280Exception("There is no current item to obtain.");
		return this.position.item();
	}

	@Override
	public boolean itemExists() {
		return !this.before() && !this.after();
	}

	@Override
	public void clear() {
		this.head = null;
		this.tail = null;
		this.position = null;
		this.prevPosition = null;
	}

	@Override
	public boolean isEmpty() {
		return this.head == null && this.tail == null; 
	}

	@Override
	public boolean isFull() {
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public LinkedList280<I> clone() throws CloneNotSupportedException {
		return (LinkedList280<I>)super.clone();
	}

	@Override
	public String toString() {
		// If the list is empty, we're done.
		if( this.isEmpty() ) return "<Empty>";
		
		// Save cursor position.
		CursorPosition280 p = this.currentPosition();
		
		String result = "";
		
		// Iterate over all elements...
		this.goFirst();
		while( !this.after() ) {
			result = result + this.item() + ", ";
			this.goForth();
		}
		
		// Restore cursor
		this.goPosition(p);
		
		return result;
	} 

	@Override
	public void deleteFirst() throws ContainerEmpty280Exception {
		if( this.isEmpty() ) throw new ContainerEmpty280Exception("Cannot delete an item from an empty list.");
		
		// If the cursor is on the second node, set the prev pointer to null.
		if( this.prevPosition == this.head ) this.prevPosition = null;
		// Otherwise, if the cursor is on the first node, set the cursor to the next node.
		else if (this.position == this.head )  this.position = this.position.nextNode();
		
		// If we're deleting the last item, set the tail to null.
		// Setting the head to null gets handled automatically in the following
		// unlinking.
		if( this.head == this.tail ) this.tail = null;
		
		// Unlink the first node.
		LinkedNode280<I> oldhead = this.head;
		this.head = this.head.nextNode();
		oldhead.setNextNode(null);
	}

	@Override
	public void deleteLast() throws ContainerEmpty280Exception {
		// Special cases if there are 0 or 1 nodes.
		if( this.isEmpty() ) throw new ContainerEmpty280Exception("Cannot delete an item from an empty list.");
		else if( this.head != null && this.head == this.tail ) this.deleteFirst();
		else {
			// There are at least two nodes.
		
			// If the cursor is on the last node, we need to update the cursor.
			if( this.position == this.tail ) {
                if(prevPosition==head && position==tail){
                    tail = head;
                    position = head;
                    prevPosition = null;
                    this.head.nextNode = null;
                    return;
                }
                // Find the node prior to this.position
				LinkedNode280<I> newPrev = this.head;
				while( newPrev.nextNode() != this.prevPosition)
                    newPrev = newPrev.nextNode();
				this.position = this.prevPosition;
				this.prevPosition = newPrev;
			}
		
			// 	Find the second-last node -- note this makes the deleteLast() algorithm O(n)
			LinkedNode280<I> penultimate = this.head;
			while(penultimate.nextNode() != this.tail) penultimate = penultimate.nextNode();
		
			// If the cursor is in the after() position, then prevPosition
			// has to become the second last node.
			if( this.after() ) {
				this.prevPosition = penultimate;
			}

			// Unlink the last node.
			penultimate.setNextNode(null);
			this.tail = penultimate;
		}
	}

	@Override
	public I firstItem() throws ContainerEmpty280Exception {
		if( this.isEmpty() ) throw new ContainerEmpty280Exception("Cannot obtain beginning of an empty list.");
		return this.head.item();
	}

	@Override
	public void insertFirst(I x) throws ContainerFull280Exception {
		
		LinkedNode280<I> newItem = createNewNode(x);
		newItem.setNextNode(this.head);
		
		// If the cursor is at the first node, cursor predecessor becomes the new node.
		// We must not do this if the list is empty or the cursor ends up in the "after" position
		// instead of the "before" position.  This wouldn't be a big problem, but it's not
		// what one would intuitively expect.
		if( !this.isEmpty() && this.position == this.head ) this.prevPosition = newItem;
				
		// Special case: if the list is empty, the new item also becomes the tail.
		if( this.isEmpty() ) this.tail = newItem;
		this.head = newItem;
	}

	@Override
	public void insertLast(I x) throws ContainerFull280Exception {
		LinkedNode280<I> newItem = createNewNode(x);
		newItem.setNextNode(null);
		
		// If the cursor is after(), then cursor predecessor becomes the new node.
		// We don't want to do this if the list was empty, or the cursor unintuitively
		// ends up in the after position instead of the before position.
		if( !isEmpty() && this.after() ) this.prevPosition = newItem;
		
		// If list is empty, handle special case
		if( this.isEmpty() ) {
			this.head = newItem;
			this.tail = newItem;
		}
		else {
			this.tail.setNextNode(newItem);
			this.tail = newItem;
		}
	}

	@Override
	public I lastItem() throws ContainerEmpty280Exception {
		if( this.isEmpty() ) throw new ContainerEmpty280Exception("Cannot obtain item at the end of an empty list.");

		return this.tail.item();
	}

	/**	
	 * Iterator for list initialized to first item. 
	 * @timing O(1) 
	 */
	public LinkedIterator280<I> iterator()
	{
		return new LinkedIterator280<I>(this);
	}

	@Override
	public boolean after() {
		return (this.position==null) && (this.prevPosition !=null || this.isEmpty());
	}

	@Override
	public boolean before() {
		return (this.prevPosition == null) && (this.position == null);
	}

	@Override
	public void goAfter() {
		this.position = null;
		this.prevPosition = this.tail;
	}

	@Override
	public void goBefore() {
		this.position = null;
		this.prevPosition = null;		
	}

	@Override
	public void goFirst() throws ContainerEmpty280Exception {
		if( this.isEmpty() ) throw new ContainerEmpty280Exception("Cannot position cursor at first element of an empty list.");

		this.position = this.head;
		this.prevPosition = null;
	}

	@Override
	public void goForth() throws AfterTheEnd280Exception {
		if (after())
			throw new AfterTheEnd280Exception("Cannot advance to next item when already after the end.");

		if (before())
			goFirst();
		else
		{
			this.prevPosition = this.position;
			this.position = this.position.nextNode();
		}		
	}

	@Override
	public void restartSearches() {
		this.continueSearch = false;
	}

	@Override
	public void resumeSearches() {
		this.continueSearch = true;
	}

	@Override
	public void search(I x) {
		if(this.isEmpty()) {
			this.goAfter();
			return;
		}

		if (!continueSearch)
			goFirst();
		else if (!after())
			goForth();
	
		while (!after() && !membershipEquals(x, item()))
			goForth();
	}

	@Override
	public CursorPosition280 currentPosition() {
		LinkedIterator280<I> iter = new LinkedIterator280<I>(this, this.prevPosition, this.position);
		return iter;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void goPosition(CursorPosition280 c) {
		if( !(c instanceof LinkedIterator280) )
			throw new IllegalArgumentException("Arguement is not a LinkedListIterator280.");
		LinkedIterator280<I> iter = (LinkedIterator280<I>)c;
		
		this.position = iter.cur;
		this.prevPosition = iter.prev;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void delete(I x) throws ItemNotFound280Exception {
		if( this.isEmpty() ) throw new ContainerEmpty280Exception("Cannot delete from an empty list.");

		// Save cursor position
		LinkedIterator280<I> savePos = (LinkedIterator280<I>)this.currentPosition();
		
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
			// Only do this if the node we are deleting is not the first one.
			this.prevPosition.setNextNode(this.position.nextNode());
		
		// If we deleted the first or last node (or both, in the case
		// that the list only contained one element), update head/tail.
		if( this.position == this.head ) this.head = this.head.nextNode();
		if( this.position == this.tail ) this.tail = this.prevPosition;
		
		this.position.setNextNode(null);
		
		// Restore the old, possibly modified cursor.
		this.goPosition(savePos);
		
	}

	@Override
	public void insert(I x) throws ContainerFull280Exception {
		insertFirst(x);
	}

	@Override
	public I obtain(I y) throws ItemNotFound280Exception {
		// save cursor state
		CursorPosition280 savePos = this.currentPosition();
		
		// Search for y
		this.search(y);
		if(!this.itemExists()) throw new ItemNotFound280Exception("Can't obtain and item that is not in the list.");
		I result = this.item();
		
		// Restore cursor state
		this.goPosition(savePos);
		return result;
	}

}
