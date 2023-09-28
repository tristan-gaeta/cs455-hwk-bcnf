import java.util.HashSet;
import java.util.Set;

/**
 * This class provides static methods for performing normalization
 * 
 * @author Tristan Gaeta
 * @version 11-22
 */
public class Normalizer {

  private static FD violator;

  /**
   * Performs BCNF decomposition
   * 
   * @param rel   A relation (as an attribute set)
   * @param fdset A functional dependency set
   * @return a set of relations (as attribute sets) that are in BCNF
   */
  public static Set<Set<String>> BCNFDecompose(Set<String> rel, FDSet fdset) {
    System.out.println("Current Schema: "+rel);
    System.out.println("Current Schema's Super Keys: "+findSuperkeys(rel, fdset));
    Set<Set<String>> output = new HashSet<>();
    if (isBCNF(rel, fdset)){
      output.add(rel);
      return output;
    }

    System.out.println("*** Splitting on "+violator+" ***");
    // new relations
    Set<String> r1 = new HashSet<>();
    r1.addAll(violator.getLeft());
    r1.addAll(violator.getRight());

    Set<String> r2 = new HashSet<>(rel);
    r2.removeAll(violator.getRight());
    r2.addAll(violator.getLeft());
    // new fd sets

    FDSet d1 = new FDSet();
    FDSet d2 = new FDSet();
    Set<String> attributes = new HashSet<>();
    FDSet closure = FDUtil.fdSetClosure(fdset);
    for (FD fd: closure){
      attributes.clear();
      attributes.addAll(fd.getLeft());
      attributes.addAll(fd.getRight());
      if (r1.containsAll(attributes)){
        d1.add(fd);
      } else if(r2.containsAll(attributes))  {
        d2.add(fd);
      }
    }

    System.out.println("Left Schema: "+r1);
    System.out.println("Left Schema's Super Keys: "+findSuperkeys(r1, d1));
    output.addAll(BCNFDecompose(r1, d1));

    System.out.println("Right Schema: "+r2);
    System.out.println("Right Schema's Super Keys: "+findSuperkeys(r2, d2)+"\n\n");
    output.addAll(BCNFDecompose(r2, d2));
    return output;
  }

  /**
   * Tests whether the given relation is in BCNF. A relation is in BCNF iff the
   * left-hand attribute set of all nontrivial FDs is a super key.
   * 
   * @param rel   A relation (as an attribute set)
   * @param fdset A functional dependency set
   * @return true if the relation is in BCNF with respect to the specified FD set
   */
  public static boolean isBCNF(Set<String> rel, FDSet fdset) {
    Set<Set<String>> keys = findSuperkeys(rel, fdset);
    System.out.println(keys);
    for (FD fd: fdset){
      if (!fd.isTrivial() && !keys.contains(fd.getLeft())){
        violator = new FD(fd);
        return false;
      }
    }
    return true;
  }

  /**
   * This method returns a set of super keys
   * 
   * @param rel   A relation (as an attribute set)
   * @param fdset A functional dependency set
   * @return a set of super keys
   */
  public static Set<Set<String>> findSuperkeys(Set<String> rel, FDSet fdset) {
    // check for valid input
    for (FD fd: fdset){
      if (!rel.containsAll(fd.getLeft()) || ! rel.containsAll(fd.getRight()))
        throw new IllegalArgumentException("The following FD refers to unknown attributes: "+fd);
    }

    // loop through power set
    Set<Set<String>> output = new HashSet<>();
    Set<String> attributes = new HashSet<>();
    for (Set<String> subset : FDUtil.powerSet(rel)) {
      attributes.clear();
      attributes.addAll(subset);
      boolean changed;
      do {
        changed = false;
        for (FD fd : fdset) {
          if (attributes.containsAll(fd.getLeft()))
            changed |= attributes.addAll(fd.getRight());
        }
      } while (changed);
      if (attributes.containsAll(rel))
        output.add(subset);
    }
    return output;
  }

}