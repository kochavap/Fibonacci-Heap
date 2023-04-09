import java.util.Arrays;
//username - noamshaib, kochavap
//id1      - 209277888
//name1    - noam shaib
//id2      - 208910117
//name2    - kochava pavlov

/**
 * FibonacciHeap
 *
 * An implementation of a Fibonacci Heap over integers.
 */
public class FibonacciHeap
{
    HeapNode min;
    HeapNode rightestRoot;       // the "oldest" tree added
    HeapNode leftestRoot;        // the "newest" tree added
    int n = 0;                   // the size of the heap
    int marked = 0;
    int numberOfTrees = 0;
    static int links;
    static int cuts;

    /**
     * public boolean isEmpty()
     *
     * Returns true if and only if the heap is empty.
     *
     *comp: O(1)
     */
    public boolean isEmpty()
    {
        return rightestRoot == null;
    }

    /**
     * public HeapNode insert(int key)
     *
     * Creates a node (of type HeapNode) which contains the given key, and inserts it into the heap.
     * The added key is assumed not to already belong to the heap.
     *
     * Returns the newly created node.
     *
     * comp:O(1)
     */
    public HeapNode insert(int key) {
        HeapNode newNode = new HeapNode(key);
        if (this.isEmpty()) {
            this.leftestRoot = newNode;
            this.rightestRoot = newNode;
            this.min = newNode;
        }
        else {
            newNode.right = leftestRoot;
            leftestRoot.left = newNode;
            newNode.left = rightestRoot;
            rightestRoot.right = newNode;
            this.leftestRoot = newNode;
            if (min.getKey() > key) {
                this.min = newNode;
            }
        }
        this.n ++ ;
        this.numberOfTrees ++;
        return newNode;
    }

    /**
     * public void deleteMin()
     *
     * Deletes the node containing the minimum key.
     *
     * O(n)
     */
    public void deleteMin() {
        if (this.isEmpty()) {
            return;
        }
        if (n == 1) {
            this.min = null;
            this.rightestRoot = null;
            this.leftestRoot = null;
            this.numberOfTrees -= 1;           // decrease 1 for deleting min
            this.n -= 1;

        }

        else {                                // size >= 2 , below: changing pointers so deleted min will no longer be pointed at
            HeapNode helper;                    // this is the first node in the linking process
            if (this.rightestRoot == this.min) {
                this.rightestRoot = this.min.child == null ? this.min.getLeft() : this.min.getChild().getLeft();
            }
            if (this.leftestRoot == this.min) {
                helper = this.min.child == null ? this.min.getRight() : this.min.child;
                this.leftestRoot = helper;
            }
            else {                            // min isn't the leftest
                helper = this.leftestRoot;
            }
            if (this.min.getChild() != null && this.numberOfTrees > 1) {
                HeapNode temp = this.min.child.left;
                this.min.child.setLeft(this.min.getLeft());
                this.min.left.setRight(this.min.child);
                temp.setRight(this.min.getRight());
                this.min.getRight().setLeft(temp);
                if (this.rightestRoot == min) { this.rightestRoot = temp; }
            }
            if (min.getChild() == null) {
                this.min.getLeft().setRight(this.min.getRight());
                this.min.getRight().setLeft(this.min.getLeft());
                if (this.rightestRoot == min) { this.rightestRoot = this.min.getLeft(); }
            }
            this.numberOfTrees += this.min.getRank(); // all min's children are roots after min is deleted
            this.numberOfTrees -= 1;          // decrease 1 for deleting min
            this.n -= 1;
            this.min = helper;                // helper is the first candidate for being the minimum (may change)
            this.consolidate();               // this is also where parents and marking are going to be fixed
        }
    }

    /**
     * private void consolidate()
     *
     * doing the Consolidating / Successive Linking process.
     * fixing parents for roots.
     * fixing marking for roots.
     * updating numberOfTrees.
     * updating min.
     *
     * comp: O(n)
     */
    public void consolidate() {
        int maxRankPossible = (int) (Math.ceil(Math.log(this.size()) / Math.log(2)))+1;
        HeapNode[] bucketsOfTrees = new HeapNode[maxRankPossible+1];
        HeapNode currentNode = this.min;
        HeapNode nextNode;                       // for the for-loop
        HeapNode nodeWithTheSameRank;            // for linking two roots with the same rank
        int currentRank;
        int m = this.numberOfTrees;
        for (int i=0; i<m; i++) {
            nextNode = currentNode.getRight();   // saving pointer for the next iteration
            if (currentNode.getParent() != null) { currentNode.setParent(null); } // fix if it's the previous min's child
            if (currentNode.getMark()) {                                          // fix if it's the previous min's child
                currentNode.setMark(false);
                this.marked -= 1;
            }
            currentRank = currentNode.getRank();
            while (bucketsOfTrees[currentRank] != null){
                links += 1;
                nodeWithTheSameRank = bucketsOfTrees[currentRank];
                if (currentNode.getKey() < nodeWithTheSameRank.getKey()) {
                    this.addChild(currentNode, nodeWithTheSameRank);
                }
                else {
                    this.addChild(nodeWithTheSameRank, currentNode);
                    currentNode = nodeWithTheSameRank;
                }
                bucketsOfTrees[currentRank] = null;
                currentRank = currentNode.getRank();
            }
            if (currentNode.getKey() < this.min.getKey()) { this.min = currentNode; }
            bucketsOfTrees[currentRank] = currentNode;
            currentNode = nextNode;
        }
        HeapNode newRightest = rightestRoot;
        boolean firstTimeNotNull = true;
        for(int i=0; i<maxRankPossible+1; i++){    // sort the roots by ranks
            if(bucketsOfTrees[i] == null){continue;}
            if (firstTimeNotNull){
                this.leftestRoot = bucketsOfTrees[i];
                newRightest = bucketsOfTrees[i];
                firstTimeNotNull = false;
            }
            else {
                newRightest.setRight(bucketsOfTrees[i]);
                bucketsOfTrees[i].setLeft(newRightest);
                newRightest = newRightest.getRight();
            }
        }
        this.rightestRoot = newRightest;
        this.rightestRoot.setRight(this.leftestRoot);
        this.leftestRoot.setLeft(this.rightestRoot);
    }

    /**
     * public void addChild(HeapNode node)
     *
     * adds childNode as the leftest child of motherNode
     * updates pointers (parent, right, left)
     * updates rightestRoot, leftestRoot, numberOfTrees
     * updates HeapNode rank
     * doesn't update min
     *
     * comp: O(1)
     */
    private void addChild(HeapNode motherNode, HeapNode childNode) {
        HeapNode rightsOfChild = childNode.getRight();
        HeapNode leftOfChiled = childNode.getLeft();
        if (motherNode.getChild() == null) {
            motherNode.setChild(childNode);
            childNode.setParent(motherNode);
            childNode.setRight(childNode);
            childNode.setLeft(childNode);
        }
        else {
            HeapNode previousChild = motherNode.getChild();
            HeapNode rightestChild = previousChild.getLeft();
            motherNode.setChild(childNode);
            childNode.setParent(motherNode);
            childNode.setRight(previousChild);
            previousChild.setLeft(childNode);
            rightestChild.setRight(childNode);
            childNode.setLeft(rightestChild);
        }
        rightsOfChild.setLeft(leftOfChiled);
        leftOfChiled.setRight(rightsOfChild);
        int previousMothersRank = motherNode.getRank();
        motherNode.setRank(previousMothersRank + 1);
        this.numberOfTrees -= 1;
    }


    /**
     * public HeapNode findMin()
     *
     * Returns the node of the heap whose key is minimal, or null if the heap is empty.
     *
     * comp: O(1)
     */
    public HeapNode findMin()
    {
        return this.min;      // if heap is empty, min is null
    }

    /**
     * public void meld (FibonacciHeap heap2)
     *
     * Melds heap2 with the current heap.
     *
     * comp: O(1)
     */
    public void meld (FibonacciHeap heap2) {
        if (!heap2.isEmpty()) {                                  // if heap2 is empty don't do anything
            if (this.isEmpty()) {
                this.min = heap2.findMin();
                this.rightestRoot = heap2.rightestRoot;
                this.leftestRoot = heap2.leftestRoot;
                this.n += heap2.size();
                this.numberOfTrees += heap2.getNumberOfTrees();
                this.marked += heap2.marked;
                return;
            }
            else {
                if (heap2.findMin().getKey() < this.min.getKey()) { this.min = heap2.findMin(); }   //update minimum if needed
            }
            this.rightestRoot.right = heap2.leftestRoot;
            heap2.leftestRoot.left = this.rightestRoot;
            this.leftestRoot.left = heap2.rightestRoot;
            heap2.rightestRoot.right = this.leftestRoot;
            this.rightestRoot = heap2.rightestRoot;
            this.n += heap2.size();
            this.numberOfTrees += heap2.getNumberOfTrees();
            this.marked += heap2.marked;
        }
    }

    /**
     * public int size()
     *
     * Returns the number of elements in the heap.
     *
     * comp: O(1)
     */
    public int size()
    {
        return this.n;
    }

    /**
     * public int getNumberOfTrees()
     *
     * Returns the number of trees in the heap.
     *
     * comp: O(1)
     */
    public int getNumberOfTrees()
    {
        return this.numberOfTrees;
    }

    /**
     * public int[] countersRep()
     *
     * Return an array of counters. The i-th entry contains the number of trees of order i in the heap.
     * (Note: The size of of the array depends on the maximum order of a tree.)
     *
     * comp: O(n)
     */
    public int[] countersRep()
    {
        if (this.isEmpty()) {
            return new int[0];
        }
        int maxRank = leftestRoot.getRank();
        HeapNode curr = this.leftestRoot.getRight();
        while (curr != leftestRoot){
            maxRank = Math.max(maxRank, curr.getRank());
            curr = curr.getRight();
        }
        int[] arr = new int[maxRank + 1];   // default values in arr are 0
        HeapNode root = leftestRoot;
        for(int i = 0; i < this.numberOfTrees; i++){
            arr[root.getRank()] += 1;
            root = root.right;
        }
        return arr;
    }

    /**
     * public void delete(HeapNode x)
     *
     * Deletes the node x from the heap.
     * It is assumed that x indeed belongs to the heap.
     *
     * comp: O(n)
     */
    public void delete(HeapNode x) {
        this.decreaseKey(x, Integer.MAX_VALUE);
        this.deleteMin();
    }

    /**
     * public void decreaseKey(HeapNode x, int delta)
     *
     * Decreases the key of the node x by a non-negative value delta. The structure of the heap should be updated
     * to reflect this change (for example, the cascading cuts procedure should be applied if needed).
     *
     * comp: O(n)
     */
    public void decreaseKey(HeapNode x, int delta) {
        int oldKey = x.getKey();
        x.setKey(oldKey - delta);
        HeapNode parentX = x.getParent();
        if (parentX != null && x.getKey() < parentX.getKey()){
            cut(x, parentX);
            cascadingCut(parentX);
        }
        if (x.getKey() < min.getKey()){
            min = x;
        }
    }

    /**
     * public void cut(HeapNode x, HeapNode parentX)
     *
     * cuts x from his parent (x's parent is parentX)
     *
     * comp: O(1)
     */
    public void cut(HeapNode x, HeapNode parentX) {
        cuts ++;
        numberOfTrees ++;
        x.getRight().setLeft(x.getLeft());
        x.getLeft().setRight(x.getRight());
        if (x.getRight() == x){
            parentX.setChild(null);
        }
        else{
            parentX.setChild(x.getRight());
        }
        parentX.setRank(parentX.getRank() - 1);
        x.setRight(this.leftestRoot);
        this.leftestRoot.setLeft(x);
        this.leftestRoot = x;
        x.setLeft(this.rightestRoot);
        this.rightestRoot.setRight(x);
        x.setParent(null);
        if (x.getMark()){
            x.setMark(false);
            this.marked -= 1;
        }
    }


    /**
     * public void cascadingCut(HeapNode parentX)
     *
     * doing the cascading cuts process as seen in class
     *
     * comp: O(logn)
     */
    public void cascadingCut(HeapNode parentX) {
        HeapNode grandpaX = parentX.getParent();
        if (grandpaX != null) {
            if (!parentX.getMark()) {
                parentX.setMark(true);
                marked += 1;
            }
            else {
                cut(parentX, grandpaX);
                cascadingCut(grandpaX);
            }
        }
    }



    /**
     * public int nonMarked()
     *
     * This function returns the current number of non-marked items in the heap
     *
     * comp: O(1)
     */
    public int nonMarked()
    {
        return this.size() - this.marked; // should be replaced by student code
    }

    /**
     * public int potential()
     *
     * This function returns the current potential of the heap, which is:
     * Potential = #trees + 2*#marked
     *
     * In words: The potential equals to the number of trees in the heap
     * plus twice the number of marked nodes in the heap.
     *
     * comp: O(1)
     */
    public int potential()
    {
        return this.numberOfTrees + 2*this.marked;
    }

    /**
     * public static int totalLinks()
     *
     * This static function returns the total number of link operations made during the
     * run-time of the program. A link operation is the operation which gets as input two
     * trees of the same rank, and generates a tree of rank bigger by one, by hanging the
     * tree which has larger value in its root under the other tree.
     *
     * comp: O(1)
     */
    public static int totalLinks()
    {
        return links;
    }

    /**
     * public static int totalCuts()
     *
     * This static function returns the total number of cut operations made during the
     * run-time of the program. A cut operation is the operation which disconnects a subtree
     * from its parent (during decreaseKey/delete methods).
     *
     * comp: O(1)
     */
    public static int totalCuts()
    {
        return cuts;
    }

    /**
     * public static int[] kMin(FibonacciHeap H, int k)
     *
     * This static function returns the k smallest elements in a Fibonacci heap that contains a single tree.
     * The function should run in O(k*deg(H)). (deg(H) is the degree of the only tree in H.)
     *
     * ###CRITICAL### : you are NOT allowed to change H.
     *
     * comp: O(k*deg(H))
     */
    public static int[] kMin(FibonacciHeap H, int k) {
        int[] arr = new int[k];                                //returned arr
        FibonacciHeap help_heap = new FibonacciHeap();         //help heap

        HeapNode new_node = help_heap.insert(H.min.key);                   //add first min to the help list
        new_node.setOrigin_node(H.min);                                    //save pointer to the origin node

        for (int i = 0; i < k; i++) {
            HeapNode origin_curr_min_node = help_heap.min.origin_node;                   //point to the current min node (the origin one)
            arr[i] = origin_curr_min_node.key;                                           //add the key
            if (i==k-1){                                                                 //added k minimums, no need to continue
                break;
            }
            help_heap.deleteMin();                                                       //delete min from help heap
            if (origin_curr_min_node.child != null) {                                     //curr min has no child
                HeapNode origin_son_of_curr = origin_curr_min_node.child;

                HeapNode first_son_new_node = help_heap.insert(origin_son_of_curr.key);
                first_son_new_node.setOrigin_node(origin_son_of_curr);

                HeapNode next_origin_son = origin_son_of_curr.left;

                while (next_origin_son.key != first_son_new_node.key) {
                    new_node = help_heap.insert(next_origin_son.key);                   //add first min to the help list
                    new_node.setOrigin_node(next_origin_son);
                    next_origin_son = next_origin_son.left;
                }
            }
        }
        return arr;
    }

    /**
     * public class HeapNode
     *
     * If you wish to implement classes other than FibonacciHeap
     * (for example HeapNode), do it in this file, not in another file.
     *
     *comp: all methods cost O(1)
     */

    public static class HeapNode{
        HeapNode parent;
        HeapNode left;
        HeapNode right;
        HeapNode child;
        HeapNode origin_node;
        int rank;
        boolean mark;
        public int key;

        public HeapNode(int key) {
            this.key = key;
            this.rank = 0;
            this.mark = false;
            this.parent = null;
            this.left = this;
            this.right = this;
            this.child = null;
            this.origin_node = null;
        }

        public int getKey() {
            return this.key;
        }

        public void setKey(int k) {
            this.key = k;
        }

        public void setParent(HeapNode node) {
            this.parent = node;
        }

        public HeapNode getParent() {
            return this.parent;
        }

        public void setLeft(HeapNode node) {
            this.left = node;
        }

        HeapNode getLeft() {
            return this.left;
        }

        public void setRight(HeapNode node) {
            this.right = node;
        }

        public HeapNode getRight() {
            return this.right;
        }

        public void setChild(HeapNode node) {
            this.child = node;
        }

        public HeapNode getChild() {
            return this.child;
        }

        public void setRank(int r) {
            this.rank = r;
        }

        public int getRank() {
            return this.rank;
        }

        void setMark(boolean m) {
            this.mark = m;
        }

        boolean getMark() {
            return this.mark;
        }

        public HeapNode getOrigin_node() { return this.child; }

        public void setOrigin_node(HeapNode H) { this.origin_node = H; }
    }
}

