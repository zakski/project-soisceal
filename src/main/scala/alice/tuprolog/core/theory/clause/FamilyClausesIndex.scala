package alice.tuprolog.core.theory.clause

import java.{util => ju}

import alice.tuprolog.rbtree.{Color, Node, RBTree}


/**
 * <code>FamilyClausesIndex</code> enables family clauses indexing
 * in {@link alice.tuprolog.core.theory.ClauseDatabase}.
 *
 * @author Paolo Contessi
 * @since 2.2
 */
class FamilyClausesIndex[K <: Comparable[K]] extends RBTree[K, ju.LinkedList[ClauseInfo]] {
  private val varsClauses: ju.LinkedList[ClauseInfo] = new ju.LinkedList[ClauseInfo]

  private def createNewNode(key: K, clause: ClauseInfo, first: Boolean): Node[K, ju.LinkedList[ClauseInfo]] = {
    val list: ju.LinkedList[ClauseInfo] = new ju.LinkedList[ClauseInfo](varsClauses)
    if (first) {
      list.addFirst(clause)
    }
    else {
      list.addLast(clause)
    }
    new Node[K, ju.LinkedList[ClauseInfo]](key, list, Color.RED, null, null)
  }

  @deprecated
  override def insert(key: K, value: ju.LinkedList[ClauseInfo]) {
    super.insert(key, value)
  }
  /*
   * I want to store a reference to the clause in the same order that they
   *
   * If the key does not exist
   * Add a new node
   */
  def insertAsShared(clause: ClauseInfo, first: Boolean) {
    if (first) {
      varsClauses.addFirst(clause)
    }
    else {
      varsClauses.addLast(clause)
    }

    //Update all existing nodes
    if (root != null) {
      val buf = new ju.LinkedList[Node[K, ju.LinkedList[ClauseInfo]]]
      buf.add(root)
      while (buf.size > 0) {
        val n = buf.remove
        if (first) {
          n.value.addFirst(clause)
        }
        else {
          n.value.addLast(clause)
        }
        if (n.left != null) {
          buf.addLast(n.left)
        }
        if (n.right != null) {
          buf.addLast(n.right)
        }
      }
    }
  }

  /**
   * Creates a new entry (<code>key</code>) in the index, relative to the
   * given <code>clause</code>. If other clauses is associated to <code>key</code>
   * <code>first</code> parameter is used to decide if it is the first or
   * the last clause to be retrieved.
   *
   * @param key       The key of the index
   * @param clause    The value to be binded to the given key
   * @param first     If the clause must be binded as first or last element
   */
  def insert(key: K, clause: ClauseInfo, first: Boolean) {
    var insertedNode: Node[K, ju.LinkedList[ClauseInfo]] = null
    if (root == null) {
      insertedNode = {root = createNewNode(key, clause, first); root}
    }
    else {
      var n: Node[K, ju.LinkedList[ClauseInfo]] = root
      var isAddedBranch = false
      while (!isAddedBranch) {
        val compResult: Int = key.compareTo(n.key)
        if (compResult == 0) {
          if (first) {
            n.value.addFirst(clause)
          }
          else {
            n.value.addLast(clause)
          }
          return
        }
        else if (compResult < 0) {
          if (n.left == null) {
            insertedNode = {n.left = createNewNode(key, clause, first); n.left}
            isAddedBranch = true
          } else {
            n = n.left
          }
        } else {
          if (n.right == null) {
            insertedNode = {n.right = createNewNode(key, clause, first); n.right}
            isAddedBranch = true
          }
          else {
            n = n.right
          }
        }
      }
      insertedNode.parent = n
    }
    insertCase1(insertedNode)
    verifyProperties()
  }

  /**
   * Removes all clauses related to the given key
   *
   * @param key   The key
   */
  def remove(key: K, clause: ClauseInfo) {
    super.delete(key, clause)
  }

  def removeShared(clause: ClauseInfo) {
    if (varsClauses.remove(clause)) {
      if (root != null) {
        if (root != null) {
          val buf: ju.LinkedList[Node[K, ju.LinkedList[ClauseInfo]]] = new ju.LinkedList[Node[K, ju.LinkedList[ClauseInfo]]]
          buf.add(root)
          while (buf.size > 0) {
            val n: Node[K, ju.LinkedList[ClauseInfo]] = buf.remove
            n.value.remove(clause)
            if (n.left != null) {
              buf.addLast(n.left)
            }
            if (n.right != null) {
              buf.addLast(n.right)
            }
          }
        }
      }
    }
    else {
      throw new IllegalArgumentException("Invalid clause: not registered in this index")
    }
  }

  /**
   * Retrieves all the clauses related to the key
   *
   * @param key   The key
   * @return      The related clauses
   */
  def get(key: K): ju.LinkedList[ClauseInfo] = {
    var res: ju.LinkedList[ClauseInfo] = null
    if (root != null) {
      res = super.lookup(key)
    }
    if (res == null) {
      return varsClauses
    }
    res
  }
}